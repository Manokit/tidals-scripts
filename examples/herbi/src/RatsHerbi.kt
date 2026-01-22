import com.osmb.api.item.ItemGroupResult
import com.osmb.api.item.ItemID
import util.HerbiUtils.chatDialogOpen
import util.HerbiUtils.closestObjectStartingRock
import util.HerbiUtils.depositItems
import util.HerbiUtils.determineNextTile
import util.HerbiUtils.detectFootPrints
import util.HerbiUtils.getSuccessRate
import util.HerbiUtils.getNorthRowBoat
import util.HerbiUtils.getStaminaPots
import util.HerbiUtils.harvestHerbi
import util.HerbiUtils.interactWithMuddyPatch
import util.HerbiUtils.interactWithSeaweed
import util.HerbiUtils.interactWithSmellyMushroom
import util.HerbiUtils.interactWithStartingObject
import util.HerbiUtils.interactWithTunnel
import util.HerbiUtils.needsToBank
import util.HerbiUtils.openChestBank
import util.HerbiUtils.processNextTile
import util.HerbiUtils.saveImageToFile
import util.HerbiUtils.traverseViaBoat
import util.HerbiUtils.walkPath
import util.HerbiUtils.walkToClosestStartingPoint
import util.HerbiUtils.walkToEndPosition
import util.HerbiUtils.walkToPosition
import util.HerbiUtils.withdrawalItems
import com.osmb.api.location.position.types.WorldPosition
import com.osmb.api.script.Script
import com.osmb.api.script.ScriptDefinition
import com.osmb.api.script.SkillCategory
import com.osmb.api.trackers.experience.XPTracker
import com.osmb.api.ui.GameState
import com.osmb.api.ui.chatbox.ChatboxFilterTab
import com.osmb.api.ui.component.tabs.skill.SkillType
import com.osmb.api.utils.RandomUtils
import com.osmb.api.visual.drawing.Canvas
import data.HerbiboarSearchSpot
import data.HerbiboarStart
import data.Paths.BACKUP_PATH
import data.Paths.B_TO_A_PATH
import data.Paths.CENTER_STUCK_AREA_PATH
import data.Paths.E_TO_END_I
import data.Paths.F_TO_E
import data.Paths.I_TO_E
import data.Paths.I_TO_F
import data.Paths.START_TO_A
import ui.HerbGains
import ui.RatsHerbiUI
import ui.HerbiPaintDelegate
import util.ChatBoxHandler
import util.CombatHelper
import util.HerbiConstants.BOAT_WALK_TO_TILE
import util.HerbiConstants.CENTER_STUCK_AREA
import util.HerbiConstants.D_STUCK_TILE
import util.HerbiConstants.F_STUCK_TILE
import util.HerbiConstants.HERBI_PIXELS
import util.HerbiConstants.ISLAND_AREA
import util.HerbiConstants.I_STUCK_TILE
import util.HerbiUtils.D_STACKED_NPC_AREA
import util.HerbiUtils.MINIMAL_MINIMAP_CONFIG
import util.HerbiUtils.STRICT_MINIMAP_CONFIG
import util.HerbiUtils.detectHerbGains
import util.HerbiUtils.getHerbCounts
import util.HerbiUtils.insideCrabArea
import util.NpcUtil
import util.PlayerMovementTracker
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.time.Instant

private const val VERSION_NUMBER = 1.30

@ScriptDefinition(
    name = "Rats Herbi",
    author = "Rats",
    version = VERSION_NUMBER,
    description = "Herblore + Hunter Gains!",
    skillCategory = SkillCategory.HUNTER
)
class RatsHerbi(obj: Any) : Script(obj) {
    private val version: String
        get() = VERSION_NUMBER.toString()

    private lateinit var npcUtil: NpcUtil
    private lateinit var herbiUI: RatsHerbiUI
    private var chatBoxHandler: ChatBoxHandler? = null
    private var combatHelper: CombatHelper? = null

    private var playerMovementTracker: PlayerMovementTracker? = null

    private var harvestCount = 0
    private val listOfFailedRoutes: MutableList<String> = mutableListOf()

    private var failureCount = 0
    private val MAX_FAILURE_COUNT = 10

    private var stopWorthyFailureCount = 0
    private var runRegenAmount = 0

    private var useStaminas = false
    private var potionCount = 0
    private var useHerbSack = true
    private val paintDelegate = HerbiPaintDelegate()
    private var herbGains = HerbGains()
    private var zoomLevel: Int = -1

    // Timing variables for herbiboar catches
    private var catchStartTime: Long = 0
    private var lastCatchTime: Long = 0
    private var totalCatchTime: Long = 0
    private var averageCatchTime: Long = 0
    private var cachedHerbItemGroup: ItemGroupResult? = null

    private var hasDepositedItems = false

    // Success rate tracking
    private var successfulHunts = 0
    private var failedHunts = 0

    private var capturedFlag = false

    private var subscriptionClient: SubscriptionClient? = null
    private var sessionId: UUID? = null

    override fun onStart() {
        subscriptionClient = SubscriptionClient().apply {
            startSession(
                discordUsername,
                "herbi-hunter",
                Instant.now(),
            ) { result ->
                result.onSuccess { sessionId ->
                    this@RatsHerbi.sessionId = sessionId
                    log("Session started with ID: $sessionId")
                }.onFailure { error ->
                    log(error.message)
                }
            }
        }

        combatHelper = CombatHelper(this)

        chatBoxHandler = ChatBoxHandler(
            script = this,
            onChatboxEvent = { event ->
            when (event) {
                ChatBoxHandler.Event.HerbiCaptured -> {
                    log("Herbiboar captured message detected!")
                    capturedFlag = true
                }
                ChatBoxHandler.Event.HerbiEscaped -> {
                    log("Herbiboar escaped message detected!")
                    reset()
                }
            }
        })

        npcUtil = NpcUtil(this)
        herbiUI = RatsHerbiUI(script = this, currentVersion = VERSION_NUMBER)
        playerMovementTracker = PlayerMovementTracker(this)

        val scene = herbiUI.buildScene(this)
        stageController.show(scene, "Rats Herbiboar Settings", true)

        useStaminas = herbiUI.isUseStaminas()
        potionCount = herbiUI.getPotionCount()
        useHerbSack = herbiUI.isUseHerbSack()

        log("Use Herb Sack: $useHerbSack")
        if (useStaminas) {
            runRegenAmount = RandomUtils.uniformRandom(20, 35)
            log("Selected potion: ${herbiUI.getSelectedPotion().displayName}, amount to withdraw: $potionCount")
        }
    }

    override fun stop() {
        sessionId?.let {
            subscriptionClient?.endSession(
                it,
                discordUsername,
                Instant.now(),
                (System.currentTimeMillis() - startTime) / 1000,
            ) { result ->
                result.onSuccess {
                    log("Session ended successfully.")
                }.onFailure { error ->
                    log("Failed to end session: ${error.message}")
                }
            }
            sessionId = null
        }
        super.stop()
        log("Script stopped.")
    }

    override fun onGameStateChanged(newGameState: GameState) {
        when (newGameState) {
            GameState.APP_NOT_OPEN -> {
                log("Game state changed to $newGameState, resetting script state.")
                reset()
            }
            else -> {}
        }
    }

    private var prevState: HerbiState? = null

    private var hasStarted = false
    private var currentPath: MutableList<HerbiboarSearchSpot> = mutableListOf()
    private var currentDestinationQueue = ConcurrentLinkedQueue<HerbiboarSearchSpot>()
    private var startedObject: HerbiboarStart? = null

    override fun onNewFrame() {
        chatBoxHandler?.updateChatBoxLines()
        combatHelper?.isInCombat()
    }

    override fun poll(): Int {
        playerMovementTracker?.updateLocation()

        val state = getState()

        if (state != prevState) {
            log(state.toUIString())
            prevState = state
        }

        val groups = currentPath.map { it.group }.distinct()

        when (state) {
            is HerbiState.SettingGameChatFilter -> {
                widgetManager.chatbox.openFilterTab(ChatboxFilterTab.GAME)
            }
            is HerbiState.DropVials ->
                widgetManager.inventory.dropItems(ItemID.VIAL)
            is HerbiState.ClosingChatDialog ->
                widgetManager.dialogue?.selectOption("Yes and don't ask again")
            is HerbiState.DrinkingEnergyRestore -> {
                val pot = getStaminaPots(herbiUI.getSelectedPotionIds()).firstOrNull()
                val ogEnergy = widgetManager.minimapOrbs.runEnergy
                if (pot != null) {
                    pot.interact("Drink") && pollFramesHuman({ widgetManager.minimapOrbs.runEnergy != ogEnergy }, 3000)
                    runRegenAmount = random(31L, 54L)
                }
            }
            is HerbiState.Banking -> {
                when (state) {
                    is HerbiState.Error -> {
                        log("Error encountered: ${state.reason}")
                        stop()
                    }
                    is HerbiState.Banking.WithdrawalItems -> {
                        val success = withdrawalItems(useStaminas, herbiUI.getSelectedPotionIds(), potionCount)

                        if (!success) {
                            stopWorthyFailureCount += 1
                        } else {
                            stopWorthyFailureCount = 0
                        }
                    }
                    is HerbiState.Banking.ClosingBank -> {
                        herbGains = cachedHerbItemGroup?.let { detectHerbGains(herbGains, it) } ?: HerbGains()

                        if (widgetManager.bank.close()) {
                            hasDepositedItems = false
                        }
                    }
                    is HerbiState.Banking.DepositingItems -> hasDepositedItems = depositItems(useHerbSack, useStaminas, herbiUI.getSelectedPotionIds())
                    is HerbiState.Banking.OpeningBank -> if (openChestBank()) {
                        hasDepositedItems = false
                        cachedHerbItemGroup = getHerbCounts()
                    }
                    is HerbiState.Banking.TraversingViaBoat -> traverseViaBoat(state.destination)
                    is HerbiState.Banking.WalkingToBank -> walkToEndPosition(position = BOAT_WALK_TO_TILE, distance = 2, config = STRICT_MINIMAP_CONFIG)
                }
            }
            is HerbiState.SettingZoomLevel -> {
                zoomLevel = widgetManager.settings.zoomLevel.get() ?: 15

                widgetManager.settings.setBrightnessLevel()

                if (zoomLevel !in 10..15) {
                    widgetManager.settings.setZoomLevel(random(10L, 15L))
                } else {
                    log("Zoom level is already set to an acceptable level: $zoomLevel")
                }
            }
            is HerbiState.Resetting -> {
                if (state.relog) {
                    val image = screen.image
                    saveImageToFile(image, "herbi_reset_screenshot")
                }
                if (combatHelper?.isBeingAttacked() == true || insideCrabArea()) {
                    val closestStartPos = HerbiboarStart.values().sortedBy { it.position.distanceTo(worldPosition) }.first { it != HerbiboarStart.DRIFTWOOD }
                    walkToPosition(closestStartPos.position, MINIMAL_MINIMAP_CONFIG.breakCondition { !insideCrabArea() && combatHelper?.isBeingAttacked() == false })
                } else if (state.relog && widgetManager.logoutTab.logout()) {
                    resetWithFailure()
                } else {
                    resetWithFailure()
                }
            }
            is HerbiState.CloseInventory -> widgetManager.inventory.close()
            is HerbiState.OpenInventory -> widgetManager.inventory.open()
            is HerbiState.HarvestingHerbi -> if (widgetManager.inventory.open() && harvestHerbi(npcUtil, state.longPress) && waitForCaptureFlag()) {
                log("We've harvested the herbiboar!")
                log("Final path: $startedObject -> $currentPath")

                // Record the catch time
                lastCatchTime = System.currentTimeMillis() - catchStartTime
                totalCatchTime += lastCatchTime
                harvestCount += 1

                averageCatchTime = if (harvestCount > 0) totalCatchTime / harvestCount else 0
                catchStartTime = 0
                // Track successful hunt
                successfulHunts += 1

                log("Total herbi harvested: $harvestCount")
                log("Success rate: ${getSuccessRate(successfulHunts, failedHunts)}%")
                reset()
            } else {
                log("Failed to harvest the herbiboar.")
                failureCount += 1
            }
            is HerbiState.InteractingWithTunnel -> if (interactWithTunnel(npcUtil, state.loc)) {
                pollFramesUntil({ false }, RandomUtils.uniformRandom(1100, 1200))
            } else {
                failureCount += 1
            }
            is HerbiState.InteractingWithMuddyPatch -> handleInteraction(interactWithMuddyPatch(state.loc), "Muddy patch", groups)
            is HerbiState.InteractingWithMushroom -> handleInteraction(interactWithSmellyMushroom(state.loc), "Mushroom", groups)
            is HerbiState.InteractingWithSeaweed -> handleInteraction(interactWithSeaweed(state.worldPosition), "Seaweed", groups)
            is HerbiState.DetermineNextLocation -> {
                val tile = determineNextTile(currentPath)

                if (tile != null) {
                    failureCount = 0
                    log("Determined next tile: $tile")
                    processNextTile(tile, currentPath, startedObject, currentDestinationQueue)
                    return 0
                } else {
                    failureCount += 1
                    log("Determined next tile: is null, $startedObject -> $currentPath")
                    pollFramesHuman({ !pixelAnalyzer.isPlayerAnimating(0.2) }, 1200)
                }
            }
            is HerbiState.MovingToTile -> {
                val currentSpot = currentPath.lastOrNull()
                val nextPosition = currentSpot?.let {
                    (detectFootPrints(it.group, currentPath.map(HerbiboarSearchSpot::group).distinct())?.position).also { pos ->
                        if (pos == null) log("Footprints not detected for group ${it.group} when trying to move off stuck tile.")
                    }
                } ?: state.worldPosition

                sceneProjector.getTileCube(nextPosition, 100)?.let { tile ->
                    if (finger.tapGetResponse(false, tile.convexHull())?.action?.lowercase()?.contains("walk here") == true) {
                        pollFramesHuman({ false }, random(200L, 500L))
                    }
                }
            }
            is HerbiState.WalkingToStartingObject -> {
                if (CENTER_STUCK_AREA.contains(worldPosition)) {
                    log("We're stuck in the center area, moving out...")
                    walkPath(CENTER_STUCK_AREA_PATH)
                } else if (worldPosition == WorldPosition(3678, 3840, 0)) {
                    log("We're stuck on the center tile, moving out...")
                    walkPath(I_TO_F)
                } else if (worldPosition.x <= WorldPosition(3675, 3860, 0).x) {
                    log("We're stuck in West area, moving out...")
                    walkPath(BACKUP_PATH)
                } else {
                    walkToClosestStartingPoint()
                }.also { success -> if (!success) stopWorthyFailureCount += 1 else stopWorthyFailureCount = 0 }
            }
            is HerbiState.InteractWithStartingObject -> if (interactWithStartingObject()) {
                capturedFlag = false
                catchStartTime = System.currentTimeMillis()
                failureCount = 0
                log("We've clicked on the starting object!")
                hasStarted = true
                startedObject =
                    HerbiboarStart.values().first { it.position == closestObjectStartingRock()?.worldPosition }
                log("Our starting point is now $startedObject")
            } else {
                log("Failed to interact with starting object")
                failureCount += 1
            }
            is HerbiState.WalkingToNextLocation -> {
                val nextGroup = state.nextLoc.group
                val lastGroup = currentPath.lastOrNull()?.group

                // Handle custom path walking based on group transitions
                when {
                    nextGroup == HerbiboarSearchSpot.Group.A && lastGroup == HerbiboarSearchSpot.Group.B -> {
                        log("Walking custom path from B to A")
                        walkPath(B_TO_A_PATH)
                        return 0
                    }
                    nextGroup == HerbiboarSearchSpot.Group.A && currentPath.isEmpty() -> {
                        log("Walking custom path from START to A")
                        walkPath(START_TO_A)
                        return 0
                    }
                    nextGroup == HerbiboarSearchSpot.Group.B && lastGroup == HerbiboarSearchSpot.Group.A -> {
                        log("Walking custom path from A to B")
                        walkPath(B_TO_A_PATH.reversed())
                        return 0
                    }
                    nextGroup == HerbiboarSearchSpot.Group.E && lastGroup == HerbiboarSearchSpot.Group.F -> {
                        log("Walking custom path from F to E")
                        walkPath(F_TO_E)
                        return 0
                    }
                    nextGroup == HerbiboarSearchSpot.Group.E && lastGroup == HerbiboarSearchSpot.Group.I -> {
                        log("Walking custom path from I to E")
                        walkPath(I_TO_E)
                        return 0
                    }
                    nextGroup == HerbiboarSearchSpot.Group.F && lastGroup == HerbiboarSearchSpot.Group.E -> {
                        log("Walking custom path from E to F")
                        walkPath(F_TO_E.reversed())
                        return 0
                    }
                    nextGroup == HerbiboarSearchSpot.Group.F && lastGroup == HerbiboarSearchSpot.Group.I -> {
                        log("Walking custom path from I to F")
                        walkPath(I_TO_F)
                        return 0
                    }
                    nextGroup == HerbiboarSearchSpot.Group.I && lastGroup == HerbiboarSearchSpot.Group.E -> {
                        log("Walking custom path from E to I")
                        walkPath(I_TO_E.reversed())
                        return 0
                    }
                    nextGroup == HerbiboarSearchSpot.Group.END_I && lastGroup == HerbiboarSearchSpot.Group.E -> {
                        log("Walking custom path from E to END_I")
                        walkPath(E_TO_END_I, isEnd = true)
                        return 0
                    }
                    nextGroup == HerbiboarSearchSpot.Group.END_G && lastGroup == HerbiboarSearchSpot.Group.A -> {
                        log("Walking custom path from A to END_G")
                        walkPath(START_TO_A.reversed(), isEnd = true)
                        return 0
                    }
                    state.nextLoc.type == HerbiboarSearchSpot.SpotType.TUNNEL -> walkToEndPosition(state.nextLoc.location)
                    else -> walkToPosition(state.nextLoc.location)
                }
            }
            is HerbiState.WaitingForIdle -> {}
        }
        return 0
    }

    private fun getState(): HerbiState {
        val destination = currentDestinationQueue.peek()
        return if (failureCount >= MAX_FAILURE_COUNT || worldPosition == null) {
            HerbiState.Resetting(relog = true)
        } else if (worldPosition.regionID in listOf(12850, 12342, 11828, 12600)) {
            HerbiState.Error("In respawn area... looks like we may have died")
       } else if (zoomLevel < 0) {
            HerbiState.SettingZoomLevel
        } else if (widgetManager.dialogue.dialogueType == null && widgetManager.chatbox.activeFilterTab != ChatboxFilterTab.GAME) {
            HerbiState.SettingGameChatFilter
        } else if (!hasStarted) {
            val closestObjectStartingRock = closestObjectStartingRock()
            return if (!widgetManager.inventory.isOpen) {
                HerbiState.OpenInventory
            } else if (widgetManager.inventory.search(setOf(ItemID.VIAL)).contains(ItemID.VIAL)) {
                 HerbiState.DropVials
            } else if (needsToBank(useStaminas, useHerbSack, herbiUI.getSelectedPotionIds(), potionCount)) {
                if (stopWorthyFailureCount >= 5) {
                    HerbiState.Error("Banking error: If using Herb sack make sure that it's opened before starting the bot")
                } else if (!ISLAND_AREA.contains(worldPosition)) {
                    if (!getNorthRowBoat()) {
                        HerbiState.Banking.WalkingToBank
                    } else {
                        HerbiState.Banking.TraversingViaBoat(HerbiState.Banking.TraversingViaBoat.Destination.ISLAND)
                    }
                } else if (!widgetManager.bank.isVisible) {
                    HerbiState.Banking.OpeningBank
                } else if (widgetManager.bank.freeBankSlots <= 1) {
                    HerbiState.Error("Full bank detected")
                } else if (!hasDepositedItems) {
                    HerbiState.Banking.DepositingItems
                } else {
                    HerbiState.Banking.WithdrawalItems
                }
            } else if (widgetManager.bank.isVisible) {
                HerbiState.Banking.ClosingBank
            } else if (closestObjectStartingRock == null || !closestObjectStartingRock.canReach()) {
                if (ISLAND_AREA.contains(worldPosition)) {
                    HerbiState.Banking.TraversingViaBoat(HerbiState.Banking.TraversingViaBoat.Destination.MAIN_LAND)
                } else if (useStaminas && !widgetManager.minimapOrbs.hasStaminaEffect() && widgetManager.minimapOrbs.runEnergy <= runRegenAmount) {
                    if (!widgetManager.inventory.isOpen) {
                        HerbiState.OpenInventory
                    } else if (getStaminaPots(herbiUI.getSelectedPotionIds()).isNotEmpty()) {
                        HerbiState.DrinkingEnergyRestore
                    } else {
                        HerbiState.WalkingToStartingObject
                    }
                } else if (worldPosition == I_STUCK_TILE) {
                    HerbiState.MovingToTile(worldPosition = WorldPosition(I_STUCK_TILE.x + 1, I_STUCK_TILE.y, 0))
                } else {
                    HerbiState.WalkingToStartingObject
                }
            } else {
                if (useStaminas && !widgetManager.minimapOrbs.hasStaminaEffect() && widgetManager.minimapOrbs.runEnergy <= runRegenAmount) {
                    if (!widgetManager.inventory.isOpen) {
                        HerbiState.OpenInventory
                    } else if (getStaminaPots(ids = herbiUI.getSelectedPotionIds()).isNotEmpty()) {
                        HerbiState.DrinkingEnergyRestore
                    } else if (chatDialogOpen()) {
                        HerbiState.ClosingChatDialog
                    } else {
                        HerbiState.InteractWithStartingObject
                    }
                } else if (npcUtil.getNpc(*HERBI_PIXELS.toIntArray()) != null) {
                    if ((finger.lastTapMillis + 5_000) > System.currentTimeMillis()) {
                        HerbiState.WaitingForIdle
                    } else {
                        HerbiState.HarvestingHerbi(longPress = false)
                    }
                } else if (chatDialogOpen()) {
                    HerbiState.ClosingChatDialog
                } else {
                    HerbiState.InteractWithStartingObject
                }
            }
        } else if (destination == null) {
            HerbiState.DetermineNextLocation
        } else if (worldPosition.distanceTo(destination.location) > 5) {
            when (worldPosition) {
                F_STUCK_TILE ->
                    HerbiState.MovingToTile(worldPosition = WorldPosition(F_STUCK_TILE.x + 1, F_STUCK_TILE.y - 1, 0))
                D_STUCK_TILE ->
                    HerbiState.MovingToTile(worldPosition = WorldPosition(D_STUCK_TILE.x + 1, D_STUCK_TILE.y, 0))
                I_STUCK_TILE ->
                    HerbiState.MovingToTile(worldPosition = WorldPosition(I_STUCK_TILE.x + 1, I_STUCK_TILE.y - 1, 0))
                else -> HerbiState.WalkingToNextLocation(destination)
            }
        } else if (pixelAnalyzer.isPlayerAnimating(0.2) && (finger.lastTapMillis + 2000) > System.currentTimeMillis()) {
            HerbiState.WaitingForIdle
        } else {
            when (destination.type) {
                HerbiboarSearchSpot.SpotType.MUSHROOM -> {
                    if (worldPosition == WorldPosition(3680, 3839, 0)) {
                        HerbiState.MovingToTile(worldPosition = WorldPosition(3681, 3838, 0))
                    } else {
                        HerbiState.InteractingWithMushroom(destination.location)
                    }
                }
                HerbiboarSearchSpot.SpotType.SEAWEED -> HerbiState.InteractingWithSeaweed(destination.location)
                HerbiboarSearchSpot.SpotType.PATCH -> HerbiState.InteractingWithMuddyPatch(destination.location)
                HerbiboarSearchSpot.SpotType.TUNNEL -> {
                    if (failureCount >= 2 && D_STACKED_NPC_AREA.contains(worldPosition)) {
                        return HerbiState.HarvestingHerbi(longPress = true)
                    } else if (npcUtil.getNpc(*HERBI_PIXELS.toIntArray()) != null) {
                        HerbiState.HarvestingHerbi(longPress = false)
                    } else {
                        HerbiState.InteractingWithTunnel(destination.location)
                    }
                }
            }
        }
    }

    private fun handleInteraction(
        success: Boolean,
        interactionName: String,
        groups: List<HerbiboarSearchSpot.Group>
    ) {
        if (success) {
            failureCount = 0
            val lastLoc = currentDestinationQueue.poll() ?: return
            when {
                lastLoc.group == HerbiboarSearchSpot.Group.E &&
                        currentPath.map { it.group }.distinct().lastOrNull() == HerbiboarSearchSpot.Group.F -> {
                    // optional: handle special case
                }
                pollFramesHuman({ detectFootPrints(lastLoc.group, groups) != null }, 1000) -> currentDestinationQueue.clear()
            }

            currentPath.add(lastLoc)
            log("We've interacted with the $interactionName! Current path: $startedObject -> $currentPath")
            log("$currentDestinationQueue left in the queue")
        } else {
            failureCount += 1
        }
    }


    override fun promptBankTabDialogue(): Boolean {
        return true
    }

    private fun reset() {
        capturedFlag = false
        hasStarted = false
        startedObject = null
        currentPath.clear()
        failureCount = 0
        currentDestinationQueue.clear()
        playerMovementTracker?.reset()
    }
    
    private fun resetWithFailure() {
        // Track failed hunt
        failedHunts += 1
        log("Hunt failed. Success rate: ${getSuccessRate(successfulHunts, failedHunts)}%")
        listOfFailedRoutes.add("[$startedObject -> ${currentPath.joinToString(" -> ")}]")
        reset()
    }

    override fun regionsToPrioritise(): IntArray {
        return intArrayOf(14907, 14651, 14652, 14908)
    }

    override fun canAFK(): Boolean {
        val inCombat = combatHelper?.isBeingAttacked() == true
        if (inCombat) log("In combat, cannot AFK")

        return !hasStarted && !inCombat && !insideCrabArea()
    }

    override fun canHopWorlds(): Boolean {
        val inCombat = combatHelper?.isBeingAttacked() == true
        if (inCombat) log("In combat, cannot hop worlds")

        return !hasStarted && !inCombat && !insideCrabArea()
    }

    override fun canBreak(): Boolean {
        val inCombat = combatHelper?.isBeingAttacked() == true
        if (inCombat) log("In combat, cannot break")

        return !hasStarted && !inCombat && !insideCrabArea()
    }

    override fun onPaint(c: Canvas) {
        paintDelegate.onPaint(c, createPaintData())
    }
    
    private fun createPaintData(): HerbiPaintDelegate.PaintData {
        return object : HerbiPaintDelegate.PaintData {
            override val startTime: Long = this@RatsHerbi.startTime
            override val harvestCount: Int = this@RatsHerbi.harvestCount
            override val prevState: String? = this@RatsHerbi.prevState?.toUIString()
            override val startedObject: String? = this@RatsHerbi.startedObject?.name
            override val catchStartTime: Long = this@RatsHerbi.catchStartTime
            override val averageCatchTime: Long = this@RatsHerbi.averageCatchTime
            override val useStaminas: Boolean = this@RatsHerbi.useStaminas
            override val selectedPotionDisplayName: String = herbiUI.getSelectedPotion().displayName
            override val successRate: Double = getSuccessRate(successfulHunts, failedHunts)
            override val herbGains: HerbGains = this@RatsHerbi.herbGains
            override val version: String = this@RatsHerbi.version
            override val canHop: Boolean = canHopWorlds()
            override val hunterExpTracker: XPTracker? = xpTrackers[SkillType.HUNTER]
            override val herbloreExpTracker: XPTracker? = xpTrackers[SkillType.HERBLORE]
        }
    }

    private fun waitForCaptureFlag(): Boolean {
        return pollFramesUntil({ capturedFlag }, RandomUtils.uniformRandom(7_000, 10_000))
    }

    override fun onRelog() {
        log("Relog detected, resetting script state.")
        reset()
    }


}