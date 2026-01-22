package util

import HerbiState
import com.osmb.api.item.ItemGroupResult
import com.osmb.api.item.ItemID
import com.osmb.api.item.ItemSearchResult
import com.osmb.api.location.area.impl.RectangleArea
import com.osmb.api.location.position.types.WorldPosition
import com.osmb.api.scene.RSObject
import com.osmb.api.script.Script
import com.osmb.api.ui.component.chatbox.ChatboxComponent
import com.osmb.api.utils.RandomUtils
import com.osmb.api.visual.PixelCluster
import com.osmb.api.visual.SearchablePixel
import com.osmb.api.visual.color.ColorModel
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator
import com.osmb.api.visual.image.Image
import com.osmb.api.walker.WalkConfig
import data.HerbiboarRule
import data.HerbiboarSearchSpot
import data.HerbiboarStart
import data.PixelTile
import ui.HerbGains
import util.HerbiConstants.CRAB_AREA_NORTH
import util.HerbiConstants.CRAB_AREA_SOUTH
import util.HerbiConstants.HERBI_PIXELS
import util.HerbiConstants.PIXEL_FOOTSTEPS
import util.HerbiConstants.grimyHerbs
import java.awt.Color
import java.io.File
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import javax.imageio.ImageIO

object HerbiUtils {

    val D_STACKED_NPC_AREA = WorldPosition(3710, 3880, 0).grow(10, 10)

    val MINIMAL_MINIMAP_CONFIG = WalkConfig.Builder()
        .minimapTapDelay(1200, 2000)
        .breakDistance(3)
        .tileRandomisationRadius(0)
        .timeout(10_000)
        .setWalkMethods(false, true)

    val STRICT_MINIMAP_CONFIG = WalkConfig.Builder()
        .minimapTapDelay(1200, 2000)
        .breakDistance(1)
        .tileRandomisationRadius(0)
        .timeout(10_000)
        .setWalkMethods(false, true)

    fun Script.insideCrabArea(): Boolean {
        if (worldPosition == null) return false
        return CRAB_AREA_NORTH.contains(worldPosition) || CRAB_AREA_SOUTH.contains(worldPosition)
    }

    fun Script.chatDialogOpen(): Boolean {
        return widgetManager.dialogue.isVisible &&
            widgetManager.dialogue.options.any { it.contains("Yes and don't ask again") }
    }

    fun getSuccessRate(successfulHunts: Int, failedHunts: Int): Double {
        val totalHunts = successfulHunts + failedHunts
        return if (totalHunts > 0) {
            (successfulHunts.toDouble() / totalHunts.toDouble() * 100.0)
        } else {
            0.0
        }
    }

    fun Script.determineNextTile(currentPath: List<HerbiboarSearchSpot>): HerbiboarSearchSpot? {
        return if (currentPath.isEmpty()) {
            PixelTile.toSearchSpot(detectFootPrints(HerbiboarSearchSpot.Group.START, false))
        } else {
            val lastGroup = currentPath.last().group
            val ogTile = PixelTile.toSearchSpot(detectFootPrints(lastGroup, currentPath.map { it.group }.distinct()))
            checkForImpossiblePaths(currentPath, lastGroup, ogTile)
        }
    }

    private fun Script.checkForImpossiblePaths(
        currentPath: List<HerbiboarSearchSpot>,
        lastGroup: HerbiboarSearchSpot.Group,
        ogTile: HerbiboarSearchSpot?
    ): HerbiboarSearchSpot? {
        val distinct = currentPath.map { it.group }.distinct()

        return when {
            lastGroup == HerbiboarSearchSpot.Group.B && ogTile?.group == HerbiboarSearchSpot.Group.END_D -> {
                if (distinct[distinct.lastIndex - 1] == HerbiboarSearchSpot.Group.D) {
                    log("Detected path went from D to B to END_D, impossible!")
                    PixelTile.toSearchSpot(detectFootPrints(HerbiboarSearchSpot.Group.B, (distinct + HerbiboarSearchSpot.Group.END_D)))
                } else {
                    ogTile
                }
            }
            lastGroup == HerbiboarSearchSpot.Group.E && ogTile?.group == HerbiboarSearchSpot.Group.END_I -> {
                if (distinct[distinct.lastIndex - 1] == HerbiboarSearchSpot.Group.F) {
                    log("Detected path went from F to E to END_I, this is impossible!")
                    PixelTile.toSearchSpot(detectFootPrints(HerbiboarSearchSpot.Group.E, (distinct + HerbiboarSearchSpot.Group.END_I)))
                } else {
                    ogTile
                }
            }
            lastGroup == HerbiboarSearchSpot.Group.H && ogTile?.group == HerbiboarSearchSpot.Group.END_F -> {
                if (distinct[distinct.lastIndex - 1] == HerbiboarSearchSpot.Group.J) {
                    log("Detected path went from J to H to END_F, this is impossible!")
                    PixelTile.toSearchSpot(detectFootPrints(HerbiboarSearchSpot.Group.H, (distinct + HerbiboarSearchSpot.Group.END_F)))
                } else {
                    ogTile
                }
            }
            else -> ogTile
        }
    }

    fun Script.processNextTile(
        tile: HerbiboarSearchSpot,
        currentPath: List<HerbiboarSearchSpot>,
        startedObject: HerbiboarStart?,
        currentDestinationQueue: MutableCollection<HerbiboarSearchSpot>,
    ) {
        val filteredValues = HerbiboarSearchSpot
            .values()
            .filter { spot -> spot.group == tile.group }
            .filter { !currentPath.contains(it) && it.group != HerbiboarSearchSpot.Group.START }

        val group = filteredValues.map { it.group }.first()
        val applicableRules = getApplicableRules(currentPath, startedObject, group)

        if (applicableRules.isNotEmpty()) {
            currentDestinationQueue.addAll(applyRules(applicableRules))
        } else {
            currentDestinationQueue.addAll(filteredValues.shuffled())
        }

        log("Updated current destination to ${currentDestinationQueue.map { "${it.type} at ${it.name}, Group ${it.group}" }}")
    }

    private fun getApplicableRules(
        currentPath: List<HerbiboarSearchSpot>,
        startedObject: HerbiboarStart?,
        group: HerbiboarSearchSpot.Group
    ): List<HerbiboarRule> {
        return HerbiboarRule.values().filter { rule ->
            (currentPath.isEmpty() && rule.matches(startedObject, group)) ||
                rule.matches(currentPath.lastOrNull()?.group, group)
        }
    }

    private fun Script.applyRules(rules: List<HerbiboarRule>): Set<HerbiboarSearchSpot> {
        log("A rule was applied: ${rules.first()}")
        val ruleSpots = mutableSetOf<HerbiboarSearchSpot>()

        rules.forEach { rule ->
            when (rule) {
                // Seaweed rules
                HerbiboarRule.H_SEAWEED_NE -> ruleSpots.add(HerbiboarSearchSpot.H_SEAWEED_EAST)
                HerbiboarRule.H_SEAWEED_SW -> ruleSpots.add(HerbiboarSearchSpot.H_SEAWEED_WEST)

                // Muddy patch rules
                HerbiboarRule.C_MUDDY_PATCH -> ruleSpots.add(HerbiboarSearchSpot.C_PATCH)
                HerbiboarRule.D_MUDDY_PATCH -> ruleSpots.add(HerbiboarSearchSpot.D_PATCH)

                // Mushroom rules
                HerbiboarRule.F_MUSHROOM_START -> ruleSpots.add(HerbiboarSearchSpot.F_MUSHROOM)
                HerbiboarRule.A_MUSHROOM -> ruleSpots.add(HerbiboarSearchSpot.A_MUSHROOM)
                HerbiboarRule.E_MUSHROOM -> ruleSpots.add(HerbiboarSearchSpot.E_MUSHROOM)
                HerbiboarRule.G_MUSHROOM -> ruleSpots.add(HerbiboarSearchSpot.G_MUSHROOM)
                HerbiboarRule.I_MUSHROOM_TWO,
                HerbiboarRule.I_MUSHROOM -> ruleSpots.add(HerbiboarSearchSpot.I_MUSHROOM)
                HerbiboarRule.F_MUSHROOM -> ruleSpots.add(HerbiboarSearchSpot.F_MUSHROOM)
                HerbiboarRule.I_MUDDY_PATCH -> ruleSpots.add(HerbiboarSearchSpot.I_PATCH)
            }
        }

        if (ruleSpots.isNotEmpty()) {
            log("Rule-defined next spots: ${ruleSpots.joinToString()}")
        }

        return ruleSpots
    }

    fun Script.closestObjectStartingRock(): RSObject? {
        return objectManager
            .getObjects { obj ->
                (obj.name == "Rock" || obj.name == "Driftwood") && HerbiboarStart.values()
                    .any { it.position == obj.worldPosition }
            }.minByOrNull { it.distance(worldPosition) }.also {
                if (it == null) log("No starting object found")
            }
    }

    fun Script.getStaminaPots(ids: Set<Int>): List<ItemSearchResult> {
        return widgetManager.inventory?.search(ids)?.getAllOfItems(ids)?.sortedBy { it.itemSlot } ?: emptyList()
    }

    fun Script.traverseViaBoat(destination: HerbiState.Banking.TraversingViaBoat.Destination): Boolean {
        if (widgetManager.dialogue.isVisible) {
            val result = when (destination) {
                HerbiState.Banking.TraversingViaBoat.Destination.ISLAND -> widgetManager.dialogue.selectOption("Row out to sea")
                HerbiState.Banking.TraversingViaBoat.Destination.MAIN_LAND -> widgetManager.dialogue.selectOption("Row to the north of the island")
            }
            return result && pollFramesHuman({ worldPosition.distanceTo(destination.destinationTile) < 5 }, 7000)
        } else {
            if (interactWithBackup(name = "Rowboat", action = "Travel", waitFor = { widgetManager.dialogue.isVisible })) {
                return false
            }
        }
        return false
    }

    fun Script.detectFootPrints(sourceGroup: HerbiboarSearchSpot.Group, groupsToIgnore: List<HerbiboarSearchSpot.Group>) = with(worldPosition) {
        val filteredPixelTiles = if (groupsToIgnore.size < 2) PixelTile.noEnds().filter { it.source == sourceGroup } else PixelTile.values().toList().filter { it.source == sourceGroup }
        filteredPixelTiles.filter { !groupsToIgnore.contains(it.destination) }.sortedBy { it.position.distanceTo(this) }.firstOrNull { tileHasFootprints(it.position) }?.also { drawTile(it.position) }
    }

    fun Script.detectHerbGains(prevHerbGains: HerbGains, previousSaved: ItemGroupResult): HerbGains {
        val bankCount = widgetManager.bank.search(grimyHerbs + ItemID.UNIDENTIFIED_MEDIUM_FOSSIL + ItemID.UNIDENTIFIED_LARGE_FOSSIL + ItemID.UNIDENTIFIED_SMALL_FOSSIL + ItemID.UNIDENTIFIED_RARE_FOSSIL + ItemID.NUMULITE)
        return prevHerbGains.updateHerbCount(bankCount, previousSaved)
    }

    fun Script.getHerbCounts(): ItemGroupResult {
        return widgetManager.bank.search(grimyHerbs + ItemID.UNIDENTIFIED_MEDIUM_FOSSIL + ItemID.UNIDENTIFIED_LARGE_FOSSIL + ItemID.UNIDENTIFIED_SMALL_FOSSIL + ItemID.UNIDENTIFIED_RARE_FOSSIL + ItemID.NUMULITE)
    }

    fun Script.detectFootPrints(activeGroup: HerbiboarSearchSpot.Group, considerEnd: Boolean): PixelTile? = with(worldPosition) {
        val filteredPixelTiles = PixelTile.values().filter { it.source == activeGroup }.filter { considerEnd || !it.destination.name.startsWith("END") }
        filteredPixelTiles.sortedBy { it.position.distanceTo(this) }.firstOrNull { tileHasFootprints(it.position) }?.also { drawTile(it.position) }
    }

    private fun Script.drawTile(worldPosition: WorldPosition) {
        screen.queueCanvasDrawable("Tile") { canvas ->
            val color: Color = Color.GREEN
            val tilePoly = sceneProjector.getTilePoly(worldPosition, true) ?: return@queueCanvasDrawable
            canvas.drawPolygon(tilePoly.xPoints, tilePoly.yPoints, tilePoly.xPoints.size, color.rgb)
        }
    }

    private fun Script.tileHasFootprints(tile: WorldPosition): Boolean {
        val tilePoly = sceneProjector.getTilePoly(tile, true) ?: return false
        val pixels = PIXEL_FOOTSTEPS.map { SearchablePixel(it, SingleThresholdComparator(2), ColorModel.HSL) }
        val hasFootPrints = pixelAnalyzer.findClusters(tilePoly, PixelCluster.ClusterQuery(3, 3, pixels.toTypedArray()))?.getClusters()
        return (hasFootPrints != null && hasFootPrints.isNotEmpty()).also {
            if (it) log("We've detected footprints leading to our next location")
        }
    }

    fun Script.depositItems(useHerbSack: Boolean, useStaminas: Boolean, itemID: Set<Int>): Boolean {
        if (useHerbSack) {
            val herbSack =
                widgetManager.inventory.search(setOf(ItemID.OPEN_HERB_SACK, ItemID.NUMULITE))
            if (herbSack != null) {
                herbSack.getItem(ItemID.OPEN_HERB_SACK)
                    ?.interact("Empty") == true && pollFramesHuman({ false }, random(300L, 600L))
            }
        }

        val itemsToKeep = mutableSetOf<Int>()
        itemsToKeep.add(ItemID.NUMULITE)

        if (useHerbSack) {
            itemsToKeep.addAll(setOf(ItemID.OPEN_HERB_SACK))
        }
        if (useStaminas) {
            itemsToKeep.addAll(itemID)
        }

        return widgetManager.bank.depositAll(itemsToKeep)
    }

    fun Script.withdrawalItems(useStaminas: Boolean, potionIds: Set<Int>, potionCount: Int): Boolean {
        if (useStaminas) {
            var success = false

            val inventoryPots = widgetManager.inventory
                .search(potionIds)
                .getAllOfItems(potionIds)


            potionIds.forEach {
                val amount = potionCount - inventoryPots.size
                if (!success && widgetManager.bank.withdraw(it, amount)) {
                    success = true
                }
            }

            return success
        }

        return true
    }

    fun Script.interactWithStartingObject(): Boolean {
        val obj = closestObjectStartingRock() ?: return false
        return obj.interact("Inspect") && waitForFootprints(obj)
    }

    fun Script.interactWithSeaweed(loc: WorldPosition): Boolean {
        return interactWithBackup(name = "Seaweed", position = loc, action = "Inspect", waitFor = {
            waitForFootprints(it)
        })
    }

    fun Script.interactWithMuddyPatch(loc: WorldPosition): Boolean {
        return interactWithBackup(name = "Muddy patch", action = "Inspect", loc, waitFor = {
            waitForFootprints(it)
        })
    }

    fun Script.getNorthRowBoat(): Boolean {
        val rowBoat = objectManager
            .getObjects { obj -> obj.name == "Rowboat" && obj.worldPosition == WorldPosition(3734, 3894, 0) && obj.convexHull != null }
            .firstOrNull() ?: return false
        return widgetManager.insideGameScreen(rowBoat.convexHull, listOf(ChatboxComponent::class.java))
    }

    fun Script.openChestBank(): Boolean {
        return interactWithBackup("Bank Chest", "Use", waitFor = {
            widgetManager.bank.isVisible
        })
    }

    fun Script.needsToBank(useStams: Boolean, useHerbSack: Boolean, selectedStams: Set<Int>, stamAmount: Int): Boolean {
        val inventoryItems = widgetManager.inventory.search(setOf(ItemID.OPEN_HERB_SACK) + selectedStams)
        return (invIsFull(inventoryItems, useHerbSack) || useStams && !inventoryItems.containsAny(selectedStams) && widgetManager.minimapOrbs.runEnergy < 20) || useHerbSack && !inventoryItems.contains(ItemID.OPEN_HERB_SACK) || (useStams && widgetManager.bank.isVisible && inventoryItems.getAmount(selectedStams) < stamAmount)
    }

    private fun invIsFull(items: ItemGroupResult, useHerbSack: Boolean): Boolean {
        return if (useHerbSack) items.freeSlots <= 2 else items.freeSlots <= 3
    }

    fun Script.interactWithSmellyMushroom(loc: WorldPosition): Boolean {
        return interactWithBackup(names = setOf("Mushroom", "Smelly mushroom"), action = "Inspect", position = loc, waitFor = {
            waitForFootprints(it)
        })
    }

    fun Script.interactWithTunnel(npcUtil: NpcUtil, tile: WorldPosition): Boolean {
        return interactWithBackup("Tunnel", "Attack", tile)
                && pollFramesUntil({ npcUtil.getNpc(*HERBI_PIXELS.toIntArray()) != null }, 7000)
    }

    private fun Script.interactWithBackup(name: String, action: String, position: WorldPosition, waitFor: ((RSObject) -> Boolean)? = null, timeout: Int = 7000): Boolean {
        val backupObject = objectManager
            .getObjects { obj -> obj.name == name && obj.worldPosition == position }
            .firstOrNull() ?: return false

        return performBackupInteraction(backupObject, action, waitFor, timeout)
    }

    private fun Script.interactWithBackup(names: Set<String>, action: String, position: WorldPosition, waitFor: ((RSObject) -> Boolean)? = null, timeout: Int = 7000): Boolean {
        val backupObject = objectManager
            .getObjects { obj -> names.contains(obj.name) && obj.worldPosition == position }
            .firstOrNull() ?: return false

        return performBackupInteraction(backupObject, action, waitFor, timeout)
    }

    private fun Script.interactWithBackup(name: String, action: String, waitFor: ((RSObject) -> Boolean)? = null, timeout: Int = 7000): Boolean {
        val backupObject = objectManager
            .getObjects { obj -> obj.name == name }
            .sortedBy { it.distance(worldPosition) }
            .firstOrNull() ?: return false

        return performBackupInteraction(backupObject, action, waitFor, timeout)
    }

    fun Script.interactWithBackup(names: Set<String>, action: String, waitFor: ((RSObject) -> Boolean)? = null, timeout: Int = 7000): Boolean {
        val backupObject = objectManager
            .getObjects { obj -> names.contains(obj.name) }
            .sortedBy { it.distance(worldPosition) }
            .firstOrNull() ?: return false

        return performBackupInteraction(backupObject, action, waitFor, timeout)
    }

    private fun Script.performBackupInteraction(backupObject: RSObject, action: String, waitFor: ((RSObject) -> Boolean)?, timeout: Int): Boolean {
        // Try tapGetResponse first for immediate menu response
        val resized = backupObject.convexHull.getResized(0.7)
        val result = finger.tapGetResponse(false, resized)
        if (result != null && result.action.contains(action.lowercase(), ignoreCase = true)) {
            return if (waitFor != null) {
                pollFramesHuman({ waitFor(backupObject) }, timeout)
            } else {
                true
            }
        }

        // Try finger.tap with menu selection as backup
        val tapResult = finger.tap(false, resized) { menus ->
            menus.firstOrNull { entry -> entry.action.lowercase().contains(action.lowercase()) }
        }

        if (tapResult) {
            return if (waitFor != null) {
                pollFramesHuman({ waitFor(backupObject) }, timeout)
            } else {
                true
            }
        }

        // Final fallback to standard interact
        return if (backupObject.interact(action)) {
            if (waitFor != null) {
                pollFramesHuman({ waitFor(backupObject) }, timeout)
            } else {
                true
            }
        } else {
            false
        }
    }

    fun Script.harvestHerbi(npcUtil: NpcUtil, longPress: Boolean): Boolean {
        if (longPress) {
            val location = widgetManager.minimap?.npcPositions?.random ?: return false
            val cube = sceneProjector.getTilePoly(location, true) ?: return false
            return finger.tapGameScreen(cube, "harvest")
        } else {
            return npcUtil.interactWithNpc(
                "Harvest",
                true,
                *HERBI_PIXELS.toIntArray(),
            )
        }
    }

    fun Script.walkToClosestStartingPoint(config: WalkConfig.Builder = MINIMAL_MINIMAP_CONFIG): Boolean {
        val startLoc = getNearestStartLocation(worldPosition) ?: return false
        return walker.walkTo(startLoc, config.breakCondition { worldPosition != null && worldPosition.distanceTo(startLoc) < 5 }.doWhileWalking { widgetManager.tabManager.closeContainer()
            config.build() }.build())
    }

    fun Script.walkToPosition(position: WorldPosition, config: WalkConfig.Builder = MINIMAL_MINIMAP_CONFIG): Boolean {
        return walker.walkTo(position, config.breakCondition { worldPosition != null && worldPosition.distanceTo(position) < 5 }.doWhileWalking { widgetManager.tabManager.closeContainer()
            config.build() }.build())
    }

    fun Script.walkToEndPosition(position: WorldPosition, config: WalkConfig.Builder = MINIMAL_MINIMAP_CONFIG, distance: Int = 5): Boolean {
        return walker.walkTo(position, config.breakCondition { worldPosition != null && worldPosition.distanceTo(position) < distance }.doWhileWalking { widgetManager.inventory.open()
            config.build() }.build())
    }

    fun Script.walkPath(path: List<WorldPosition>, config: WalkConfig.Builder = STRICT_MINIMAP_CONFIG, isEnd: Boolean = false): Boolean {
        return walker.walkPath(path, config.breakCondition { worldPosition.distanceTo(path.last()) < 3 }
            .doWhileWalking { if (!isEnd) widgetManager.tabManager.closeContainer() else widgetManager.inventory.open()
                config.build() }.build())
    }

    fun WorldPosition.grow(width: Int, height: Int): RectangleArea {
        val halfWidth = width / 2
        val halfHeight = height / 2
        val bottomLeftX = this.x - halfWidth
        val bottomLeftY = this.y - halfHeight
        return RectangleArea(bottomLeftX, bottomLeftY, width, height, plane)
    }

    private fun Script.interactWithRsObject(name: String, action: String, waitFor: () -> Boolean): Boolean {
        val rsObject = objectManager
            .getObjects { obj -> obj.name == name }
            .sortedBy { it.distance(worldPosition) }
            .firstOrNull() ?: return false
        return rsObject.interact(action) && pollFramesHuman({ waitFor() }, 7000)
    }

    private fun getNearestStartLocation(playerLocation: WorldPosition?): WorldPosition? {
        var neartestPoint: WorldPosition? = null
        var shortestDistance = Double.MAX_VALUE
        for (startPoint in HerbiboarStart.values()) {
            val distance: Double = playerLocation?.distanceTo(startPoint.position) ?: Double.MAX_VALUE
            if (distance < shortestDistance) {
                neartestPoint = startPoint.position
                shortestDistance = distance
            }
        }
        return neartestPoint
    }

    private fun Script.waitForFootprints(rsObject: RSObject) : Boolean {
        var hasAnimated = false
        var onPos = false
        return pollFramesHuman({
            if (hasAnimated && onPos && !pixelAnalyzer.isPlayerAnimating(0.18)) return@pollFramesHuman true

            if (worldPosition == null) return@pollFramesHuman false

            if (rsObject.surroundingPositions.contains(worldPosition.toLocalPosition(this))) {
                onPos = true
                if (pixelAnalyzer.isPlayerAnimating(0.18)) {
                    hasAnimated = true
                }
            }

            false
        } , random(6000L, 7500L))
    }

    fun Script.saveImageToFile(image: Image, prefix: String = "herbi_screenshot"): Boolean {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd").format(Date()).toString()
            val screenshotsDir = Paths.get(System.getProperty("user.home"), ".osmb", "debug-screenshots", date).toFile()

            // Create screenshots directory if it doesn't exist
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs()
            }

            // Generate timestamp for unique filename
            val timestamp = SimpleDateFormat("HH-mm-ss").format(Date())
            val filename = "${prefix}_${timestamp}.png"
            val file = File(screenshotsDir, filename)

            // Convert OSMB Image to BufferedImage and save
            val bufferedImage = image.toBufferedImage()
            val success = ImageIO.write(bufferedImage, "png", file)
            if (success) {
                log("Screenshot saved: ${file.absolutePath}")
            } else {
                log("Failed to save screenshot: ${file.absolutePath}")
            }
            success
        } catch (e: Exception) {
            log("Error saving screenshot: ${e.message}")
            false
        }
    }
}