package strategies;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;
import com.osmb.api.visual.PixelCluster;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.walker.WalkConfig;
import main.TidalsSecondaryCollector.State;
import strategies.helpers.BankingHelper;
import strategies.helpers.PrayerHelper;
import strategies.helpers.ReturnHelper;
import utilities.RetryUtils;

import java.awt.Color;
import java.util.*;

import static main.TidalsSecondaryCollector.*;

public class MortMyreFungusCollector implements SecondaryCollectorStrategy {

    // mode detection
    public enum Mode { VER_SINHAZA, FAIRY_RING }
    private Mode detectedMode = null;

    // dramen staff item id
    private static final int DRAMEN_STAFF = 772;

    private final Script script;

    // helpers (initialized after mode detection in verifyRequirements)
    private BankingHelper bankingHelper;
    private PrayerHelper prayerHelper;
    private ReturnHelper returnHelper;

    // item ids - bloom tools
    private static final int SILVER_SICKLE_B = 2963;
    private static final int EMERALD_SICKLE_B = 22433;
    private static final int RUBY_SICKLE_B = 24693;
    private static final int IVANDIS_FLAIL = 22398;
    private static final int BLISTERWOOD_FLAIL = 24699;

    static final int[] BLOOM_TOOLS = {
            SILVER_SICKLE_B,
            EMERALD_SICKLE_B,
            RUBY_SICKLE_B,
            IVANDIS_FLAIL,
            BLISTERWOOD_FLAIL
    };

    // sickles use "cast bloom", flails use "bloom" (osmb lowercases menu entries)
    private static final Set<Integer> SICKLE_IDS = Set.of(SILVER_SICKLE_B, EMERALD_SICKLE_B, RUBY_SICKLE_B);

    // banking teleports
    private static final int CRAFTING_CAPE = 9780;
    private static final int CRAFTING_CAPE_T = 9781;
    private static final int[] CRAFTING_CAPES = {CRAFTING_CAPE, CRAFTING_CAPE_T};

    // prayer restoration
    static final int ARDOUGNE_CLOAK_1 = 13121;
    static final int ARDOUGNE_CLOAK_2 = 13122;
    static final int ARDOUGNE_CLOAK_3 = 13123;
    static final int ARDOUGNE_CLOAK_4 = 13124;
    static final int[] ARDOUGNE_CLOAKS = {
            ARDOUGNE_CLOAK_1, ARDOUGNE_CLOAK_2, ARDOUGNE_CLOAK_3, ARDOUGNE_CLOAK_4
    };

    // quest cape (alternative to ardy cloak for fairy ring mode teleport)
    private static final int QUEST_CAPE = 9813;

    // return teleport
    private static final int DRAKANS_MEDALLION = 22400;

    // item to collect
    private static final int MORT_MYRE_FUNGUS = 2970;

    // fungus pixel colors (RGB values from debug tool)
    // cluster detection for fungus on logs (single color + cluster = more reliable than multi-color findPixel)
    private static final int FUNGUS_CLUSTER_COLOR = -7060445;
    private static final int FUNGUS_CLUSTER_TOLERANCE = 10;
    private static final int FUNGUS_CLUSTER_MAX_DISTANCE = 11;
    private static final int FUNGUS_CLUSTER_MIN_SIZE = 5;

    // pixel detection settings

    // ver sinhaza mode locations
    private static final WorldPosition FOUR_LOG_TILE = new WorldPosition(3667, 3255, 0);
    private static final RectangleArea LOG_AREA = new RectangleArea(3665, 3253, 3669, 3257, 0);

    // the 4 log positions around the standing tile (ver sinhaza mode)
    private static final WorldPosition[] LOG_POSITIONS = {
        new WorldPosition(3666, 3256, 0),
        new WorldPosition(3666, 3254, 0),
        new WorldPosition(3668, 3254, 0),
        new WorldPosition(3668, 3255, 0)
    };

    // fairy ring mode locations
    private static final WorldPosition THREE_LOG_TILE = new WorldPosition(3474, 3419, 0);
    private static final RectangleArea THREE_LOG_AREA = new RectangleArea(3472, 3417, 3476, 3421, 0);

    // the 3 log positions around the fairy ring standing tile
    private static final WorldPosition[] THREE_LOG_POSITIONS = {
        new WorldPosition(3473, 3418, 0),
        new WorldPosition(3473, 3420, 0),
        new WorldPosition(3475, 3420, 0)
    };

    // track which bloom tool we found during setup
    private int equippedBloomToolId = 0;
    // bloom action depends on tool type: sickles="cast bloom", flails="bloom"
    private String bloomAction = "bloom";

    // track detected prayer method: "ardy_inventory", "ardy_equipped", or "lumbridge"
    private String detectedPrayerMethod = "lumbridge";

    // cached inventory count for efficient collection (avoids tab switching)
    // mory hard diary doubles fungus per log (2 instead of 1)
    // we detect diary status on first "inventory full" check by verifying actual inventory
    private int cachedInventoryCount = -1;  // -1 = not initialized
    private int fungusPerLog = 2;  // assume diary initially, will verify on first full check
    private boolean diaryStatusVerified = false;
    private static final int INVENTORY_SIZE = 28;

    // poll-based collection state: tracks bloom+pickup cycle across polls
    // null = no active bloom cycle, need to cast bloom
    // non-null = picking up fungus one log per poll
    private List<WorldPosition> pendingFungusPositions = null;
    private int pendingFungusIndex = 0;
    private int pendingCollectedCount = 0;

    public MortMyreFungusCollector(Script script) {
        this.script = script;
    }

    public Mode getDetectedMode() {
        return detectedMode;
    }

    public boolean isFairyRingMode() {
        return detectedMode == Mode.FAIRY_RING;
    }

    @Override
    public State determineState() {
        // mid-execution guard: if we're mid-pickup cycle, stay collecting
        // only prayer depletion can interrupt (checked below)
        if (pendingFungusPositions != null) {
            Integer midPrayer = script.getWidgetManager().getMinimapOrbs().getPrayerPoints();
            if (midPrayer != null && midPrayer <= 0) {
                // abort pickup cycle, restore prayer
                script.log(getClass(), "prayer depleted mid-pickup, aborting cycle");
                pendingFungusPositions = null;
                pendingFungusIndex = 0;
                pendingCollectedCount = 0;
                allowAFK = true;
                return State.RESTORING_PRAYER;
            }
            return State.COLLECTING;
        }

        // priority 1: check prayer - can cast bloom with any prayer > 0
        // only leave when prayer is fully depleted
        Integer prayer = script.getWidgetManager().getMinimapOrbs().getPrayerPoints();
        if (prayer != null && prayer <= 0) {
            script.log(getClass(), "prayer depleted (0), need to restore");
            return State.RESTORING_PRAYER;
        }

        // priority 1b: no diary mode - always fill prayer after banking
        // without mory hard diary, you get 1 fungus per log instead of 2
        // can't complete a 2nd full trip on one prayer charge, so restore to full every time
        // only trigger when NOT in collection area (after banking/at monastery)
        if (fungusPerLog == 1 && diaryStatusVerified && isFairyRingMode()) {
            WorldPosition pos = script.getWorldPosition();
            boolean inCollectionArea = pos != null && THREE_LOG_AREA.contains(pos);

            if (!inCollectionArea) {
                Integer prayerPercent = script.getWidgetManager().getMinimapOrbs().getPrayerPointsPercentage();
                if (prayerPercent != null && prayerPercent < 100) {
                    script.log(getClass(), "no diary mode: prayer not full (" + prayerPercent + "%), filling before trip");
                    return State.RESTORING_PRAYER;
                }
            }
        }

        // priority 2: check if inventory full (use cached count if available)
        if (cachedInventoryCount >= 0) {
            int availableSlots = INVENTORY_SIZE - cachedInventoryCount;
            if (availableSlots <= 0) {
                // first time we think inventory is full, verify diary status
                if (!diaryStatusVerified) {
                    script.log(getClass(), "cached count says full (" + cachedInventoryCount + "), verifying diary status...");
                    if (verifyDiaryStatus()) {
                        availableSlots = INVENTORY_SIZE - cachedInventoryCount;
                        if (availableSlots > 0) {
                            script.log(getClass(), "diary correction: " + availableSlots + " slots available, continuing");
                        } else {
                            script.log(getClass(), "inventory confirmed full, need to bank");
                            return State.BANKING;
                        }
                    } else {
                        script.log(getClass(), "diary verification failed, banking to be safe");
                        return State.BANKING;
                    }
                } else {
                    script.log(getClass(), "inventory full (cached: " + cachedInventoryCount + "), need to bank");
                    return State.BANKING;
                }
            }
        } else {
            // fallback to actual check if cache not initialized
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
            if (inv != null && inv.isFull()) {
                script.log(getClass(), "inventory full, need to bank");
                return State.BANKING;
            }
        }

        // priority 3: check if at collection area (mode-aware)
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) {
            return State.IDLE;
        }

        RectangleArea targetArea = isFairyRingMode() ? THREE_LOG_AREA : LOG_AREA;
        if (!targetArea.contains(pos)) {
            if (verboseLogging) {
                script.log(getClass(), "[debug] not in target area: pos=" + pos
                        + " area=" + targetArea);
            }
            script.log(getClass(), "not at log area, need to return");
            return State.RETURNING;
        }

        return State.COLLECTING;
    }

    // auto-equip dramen staff from inventory (for fairy ring mode)
    private boolean autoEquipDramenStaff() {
        script.log(getClass(), "attempting to auto-equip dramen staff from inventory...");

        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(DRAMEN_STAFF));
        if (inv == null || !inv.contains(DRAMEN_STAFF)) {
            script.log(getClass(), "ERROR: dramen staff not found in inventory");
            return false;
        }

        ItemSearchResult dramenItem = inv.getItem(DRAMEN_STAFF);
        if (dramenItem == null) {
            script.log(getClass(), "ERROR: could not get dramen staff item reference");
            return false;
        }

        boolean equipped = RetryUtils.inventoryInteract(script, dramenItem, "Wield", "auto-equip dramen staff");
        if (!equipped) {
            script.log(getClass(), "ERROR: failed to equip dramen staff");
            return false;
        }

        // wait for equip to complete
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(500, 700));

        // verify dramen staff is now equipped
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));

        UIResult<ItemSearchResult> dramenEquipped = script.getWidgetManager().getEquipment().findItem(DRAMEN_STAFF);
        if (!dramenEquipped.isFound()) {
            script.log(getClass(), "ERROR: dramen staff not equipped after wield attempt");
            return false;
        }

        script.log(getClass(), "dramen staff auto-equipped successfully");
        return true;
    }

    @Override
    public boolean verifyRequirements() {
        script.log(getClass(), "verifying equipment requirements...");

        // open equipment tab first
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));

        // mode detection: check for dramen staff first (fairy ring mode indicator)
        UIResult<ItemSearchResult> dramenStaff = script.getWidgetManager().getEquipment().findItem(DRAMEN_STAFF);
        boolean dramenEquipped = dramenStaff.isFound();
        boolean dramenInInventory = false;

        // if not equipped, check inventory for dramen staff
        if (!dramenEquipped) {
            script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));

            ItemGroupResult invCheck = script.getWidgetManager().getInventory().search(Set.of(DRAMEN_STAFF));
            dramenInInventory = invCheck != null && invCheck.contains(DRAMEN_STAFF);
        }

        if (dramenEquipped || dramenInInventory) {
            // potential fairy ring mode
            String dramenSource = dramenEquipped ? "equipped" : "in inventory";
            script.log(getClass(), "dramen staff detected (" + dramenSource + "), checking for fairy ring mode...");

            script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));

            // check if bloom tool is in inventory (dramen staff occupies weapon slot)
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(toIntegerSet(BLOOM_TOOLS));
            boolean bloomInInventory = false;
            if (inv != null) {
                for (int toolId : BLOOM_TOOLS) {
                    if (inv.contains(toolId)) {
                        equippedBloomToolId = toolId;
                        bloomAction = SICKLE_IDS.contains(toolId) ? "cast bloom" : "bloom";
                        bloomInInventory = true;
                        script.log(getClass(), "bloom tool in inventory: id=" + toolId + " action=\"" + bloomAction + "\"");
                        break;
                    }
                }
            }

            if (!bloomInInventory) {
                script.log(getClass(), "ERROR: dramen staff found but no bloom tool in inventory");
                return false;
            }

            // auto-equip dramen staff if it was in inventory
            if (dramenInInventory) {
                script.log(getClass(), "dramen staff in inventory, auto-equipping...");
                if (!autoEquipDramenStaff()) {
                    script.log(getClass(), "ERROR: failed to auto-equip dramen staff");
                    return false;
                }
            }

            // check for ardy cloak or quest cape (required for fairy ring mode)
            script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));

            UIResult<ItemSearchResult> ardyCloakEquipped = script.getWidgetManager().getEquipment().findItem(ARDOUGNE_CLOAKS);
            UIResult<ItemSearchResult> questCapeEquipped = script.getWidgetManager().getEquipment().findItem(QUEST_CAPE);
            boolean hasArdyEquipped = ardyCloakEquipped.isFound();
            boolean hasQuestCapeEquipped = questCapeEquipped.isFound();

            boolean hasArdyInInventory = false;
            boolean hasQuestCapeInInventory = false;
            if (!hasArdyEquipped && !hasQuestCapeEquipped) {
                script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
                script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));

                ItemGroupResult invCloakCheck = script.getWidgetManager().getInventory().search(toIntegerSet(ARDOUGNE_CLOAKS));
                if (invCloakCheck != null) {
                    for (int cloakId : ARDOUGNE_CLOAKS) {
                        if (invCloakCheck.contains(cloakId)) {
                            hasArdyInInventory = true;
                            break;
                        }
                    }
                }

                ItemGroupResult invQuestCheck = script.getWidgetManager().getInventory().search(Set.of(QUEST_CAPE));
                hasQuestCapeInInventory = invQuestCheck != null && invQuestCheck.contains(QUEST_CAPE);
            }

            boolean hasTeleportItem = hasArdyEquipped || hasArdyInInventory || hasQuestCapeEquipped || hasQuestCapeInInventory;
            if (!hasTeleportItem) {
                script.log(getClass(), "ERROR: fairy ring mode requires ardougne cloak or quest cape (equipped or in inventory)");
                return false;
            }

            String teleportSource;
            if (hasArdyEquipped) teleportSource = "ardougne cloak (equipped)";
            else if (hasArdyInInventory) teleportSource = "ardougne cloak (inventory)";
            else if (hasQuestCapeEquipped) teleportSource = "quest cape (equipped)";
            else teleportSource = "quest cape (inventory)";

            detectedMode = Mode.FAIRY_RING;
            script.log(getClass(), "detected mode: FAIRY_RING");
            script.log(getClass(), "  - dramen staff: " + (dramenInInventory ? "auto-equipped from inventory" : "equipped"));
            script.log(getClass(), "  - bloom tool: in inventory (id=" + equippedBloomToolId + ", action=\"" + bloomAction + "\")");
            script.log(getClass(), "  - teleport item: " + teleportSource);

        } else {
            // ver sinhaza mode - bloom tool + drakan's medallion must be equipped
            script.log(getClass(), "no dramen staff, checking for ver sinhaza mode...");

            UIResult<ItemSearchResult> bloomTool = script.getWidgetManager().getEquipment().findItem(BLOOM_TOOLS);
            if (!bloomTool.isFound()) {
                script.log(getClass(), "ERROR: no bloom tool equipped (blessed sickle or flail)");
                return false;
            }

            for (int toolId : BLOOM_TOOLS) {
                UIResult<ItemSearchResult> check = script.getWidgetManager().getEquipment().findItem(toolId);
                if (check.isFound()) {
                    equippedBloomToolId = toolId;
                    bloomAction = SICKLE_IDS.contains(toolId) ? "cast bloom" : "bloom";
                    script.log(getClass(), "bloom tool equipped: id=" + toolId + " action=\"" + bloomAction + "\"");
                    break;
                }
            }

            if (equippedBloomToolId == 0) {
                script.log(getClass(), "ERROR: could not determine bloom tool id");
                return false;
            }

            UIResult<ItemSearchResult> medallion = script.getWidgetManager().getEquipment().findItem(DRAKANS_MEDALLION);
            if (!medallion.isFound()) {
                script.log(getClass(), "ERROR: no drakan's medallion equipped");
                return false;
            }

            detectedMode = Mode.VER_SINHAZA;
            script.log(getClass(), "detected mode: VER_SINHAZA");
            script.log(getClass(), "  - drakan's medallion: equipped");
            script.log(getClass(), "  - bloom tool: equipped (id=" + equippedBloomToolId + ", action=\"" + bloomAction + "\")");
        }

        // determine banking method
        String bankingMethodUsed;
        if (isFairyRingMode()) {
            bankingMethodUsed = "zanaris (fairy ring)";
        } else {
            bankingMethodUsed = "ver sinhaza (drakan's medallion)";
            try {
                for (int capeId : CRAFTING_CAPES) {
                    UIResult<ItemSearchResult> cape = script.getWidgetManager().getEquipment().findItem(capeId);
                    if (cape.isFound()) {
                        bankingMethodUsed = "crafting cape";
                        break;
                    }
                }
            } catch (RuntimeException e) {
                script.log(getClass(), "error checking crafting cape: " + e.getMessage());
            }
        }
        script.log(getClass(), "banking method: " + bankingMethodUsed);

        // check prayer restoration method
        boolean hasPrayerMethod = false;
        String prayerMethodUsed = "";

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(toIntegerSet(ARDOUGNE_CLOAKS));
        if (inv != null) {
            for (int cloakId : ARDOUGNE_CLOAKS) {
                if (inv.contains(cloakId)) {
                    hasPrayerMethod = true;
                    prayerMethodUsed = "ardougne cloak (inventory)";
                    detectedPrayerMethod = "ardy_inventory";
                    break;
                }
            }
        }

        if (!hasPrayerMethod) {
            script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

            UIResult<ItemSearchResult> ardyCloak = script.getWidgetManager().getEquipment().findItem(ARDOUGNE_CLOAKS);
            if (ardyCloak.isFound()) {
                hasPrayerMethod = true;
                prayerMethodUsed = "ardougne cloak (equipped)";
                detectedPrayerMethod = "ardy_equipped";
            }
        }

        if (!hasPrayerMethod) {
            prayerMethodUsed = "lumbridge teleport (spell)";
            detectedPrayerMethod = "lumbridge";
            script.log(getClass(), "warning: no ardougne cloak found, will use lumbridge teleport");
        }
        script.log(getClass(), "prayer method: " + prayerMethodUsed);

        // initialize helpers now that mode is detected
        initHelpers();

        // pre-trip prayer restore if not near collection area and prayer not full
        WorldPosition currentPos = script.getWorldPosition();
        WorldPosition targetLogTile = isFairyRingMode() ? THREE_LOG_TILE : FOUR_LOG_TILE;
        boolean nearLogArea = currentPos != null && currentPos.distanceTo(targetLogTile) <= 20;

        if (!nearLogArea) {
            Integer prayerPercent = script.getWidgetManager().getMinimapOrbs().getPrayerPointsPercentage();
            if (prayerPercent != null && prayerPercent < 100) {
                script.log(getClass(), "not near log area and prayer not full (" + prayerPercent + "%), restoring before first trip");
                prayerHelper.restorePrayerBeforeTrip();
            }
        } else {
            script.log(getClass(), "already near log area, skipping pre-trip prayer restore");
        }

        script.log(getClass(), "all requirements verified");
        return true;
    }

    // create helper instances after mode detection
    private void initHelpers() {
        bankingHelper = new BankingHelper(
                script, isFairyRingMode(), BLOOM_TOOLS, ARDOUGNE_CLOAKS,
                () -> cachedInventoryCount,
                count -> cachedInventoryCount = count
        );
        prayerHelper = new PrayerHelper(script, detectedPrayerMethod, ARDOUGNE_CLOAKS, bankingHelper);
        returnHelper = new ReturnHelper(script, isFairyRingMode(), bankingHelper);
        returnHelper.setVerbose(verboseLogging);
    }

    // --- collection ---

    @Override
    public int collect() {
        // initialize cached inventory count if not set
        if (cachedInventoryCount < 0) {
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
            cachedInventoryCount = (inv != null) ? (INVENTORY_SIZE - inv.getFreeSlots()) : 0;
            script.log(getClass(), "initialized cached inventory count: " + cachedInventoryCount);
        }

        // if we have pending fungus to pick up, pick ONE per poll
        if (pendingFungusPositions != null) {
            return pickNextFungus();
        }

        // check prayer before every bloom (uses minimap orbs, no tab switch needed)
        Integer prayer = script.getWidgetManager().getMinimapOrbs().getPrayerPoints();
        if (prayer == null) {
            script.log(getClass(), "can't read prayer, retrying");
            return 600;
        }

        if (prayer <= 0) {
            script.log(getClass(), "prayer depleted (0), switching to restore");
            return 0;
        }

        // check inventory using cached count
        int availableSlots = INVENTORY_SIZE - cachedInventoryCount;
        if (availableSlots <= 0) {
            script.log(getClass(), "inventory full (cached: " + cachedInventoryCount + "), switching to bank");
            return 0;
        }

        // select target tile based on mode
        WorldPosition targetTile = isFairyRingMode() ? THREE_LOG_TILE : FOUR_LOG_TILE;
        String tileName = isFairyRingMode() ? "3-log tile" : "4-log tile";

        // verify standing on exact target tile before casting bloom
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) {
            script.log(getClass(), "can't read position, retrying");
            return 600;
        }

        boolean onExactTile = pos.getX() == targetTile.getX()
                && pos.getY() == targetTile.getY();

        if (!onExactTile) {
            script.log(getClass(), "not on " + tileName + " (" + pos.getX() + ", " + pos.getY() + "), walking...");
            WalkConfig config = new WalkConfig.Builder()
                    .breakDistance(0)
                    .timeout(RandomUtils.weightedRandom(4500, 6000, 0.002))
                    .build();
            script.getWalker().walkTo(targetTile, config);
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));
            return 600;
        }

        // cast bloom based on mode
        if (equippedBloomToolId == 0) {
            script.log(getClass(), "ERROR: bloom tool id not set");
            script.stop();
            return 0;
        }

        // disable afk/hop for entire bloom + collection cycle
        allowAFK = false;

        boolean bloomSuccess;
        if (isFairyRingMode()) {
            bloomSuccess = castBloomFromInventory();
        } else {
            script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));
            bloomSuccess = RetryUtils.equipmentInteract(script, equippedBloomToolId, bloomAction, "casting bloom (prayer: " + prayer + ")");
        }

        if (!bloomSuccess) {
            allowAFK = true;
            return 600;
        }

        bloomCasts++;

        // wait for bloom animation + fungus model to render (~4-5 ticks), ignoreTasks to prevent random tab opens
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2400, 2800), true);

        // detect fungus and start pickup cycle (one per poll)
        List<WorldPosition> fungusPositions = detectFungusPositions();
        if (fungusPositions.isEmpty()) {
            allowAFK = true;
            script.log(getClass(), "no fungus detected, re-enabling afk/hop");
            return RandomUtils.weightedRandom(300, 500);
        }

        // calculate how many logs we can pick based on available slots
        availableSlots = INVENTORY_SIZE - cachedInventoryCount;
        int maxPickups = (availableSlots + fungusPerLog - 1) / fungusPerLog;
        int logsToCollect = Math.min(fungusPositions.size(), maxPickups);

        script.log(getClass(), "detected " + fungusPositions.size() + " log(s), will pick " + logsToCollect + " (slots: " + availableSlots + ")");

        // store state for poll-based pickup
        pendingFungusPositions = fungusPositions.subList(0, logsToCollect);
        pendingFungusIndex = 0;
        pendingCollectedCount = 0;

        // pick first one immediately this poll
        return pickNextFungus();
    }

    // cast bloom from inventory item (fairy ring mode - dramen staff occupies weapon slot)
    private boolean castBloomFromInventory() {
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(toIntegerSet(BLOOM_TOOLS));
        if (inv == null) {
            script.log(getClass(), "ERROR: no bloom tool in inventory");
            return false;
        }

        ItemSearchResult bloomTool = null;
        for (int toolId : BLOOM_TOOLS) {
            if (inv.contains(toolId)) {
                bloomTool = inv.getItem(toolId);
                break;
            }
        }

        if (bloomTool == null) {
            script.log(getClass(), "ERROR: bloom tool not found in inventory");
            return false;
        }

        Integer prayer = script.getWidgetManager().getMinimapOrbs().getPrayerPoints();
        return RetryUtils.inventoryInteract(script, bloomTool, bloomAction, "casting bloom from inventory (prayer: " + prayer + ")");
    }

    // pick one fungus per poll - called repeatedly until all pending logs are collected
    private int pickNextFungus() {
        // check if cycle is complete
        if (pendingFungusIndex >= pendingFungusPositions.size()) {
            return finishFungusPickup();
        }

        WorldPosition logPos = pendingFungusPositions.get(pendingFungusIndex);

        // get tile polygon for this log position
        Polygon tilePoly = script.getSceneProjector().getTileCube(logPos, 50);
        if (tilePoly == null) {
            script.log(getClass(), "can't get tile poly for " + logPos.getX() + "," + logPos.getY() + ", skipping");
            pendingFungusIndex++;
            return 0;  // re-enter immediately to try next log
        }

        // visibility check before tapping ground item
        double visibility = script.getWidgetManager().insideGameScreenFactor(tilePoly, List.of());
        if (visibility < 0.05) {
            script.log(getClass(), "tile not visible on screen, skipping");
            pendingFungusIndex++;
            return 0;
        }

        // tap the tile with Pick action (retry up to 3 times)
        boolean tapped = false;
        for (int attempt = 1; attempt <= 3 && !tapped; attempt++) {
            tapped = script.getFinger().tapGameScreen(tilePoly, "Pick");
            if (!tapped) {
                script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 300), true);
            }
        }

        if (tapped) {
            // wait for pick animation (~1 tick = 600ms)
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(600, 800), true);

            // check for "inventory full" dialogue (cache correction)
            DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
            if (dialogueType == DialogueType.CHAT_DIALOGUE || dialogueType == DialogueType.TAP_HERE_TO_CONTINUE) {
                var textResult = script.getWidgetManager().getDialogue().getText();
                if (textResult.isFound() && textResult.get() != null) {
                    String dialogueText = textResult.get().toLowerCase();
                    if (dialogueText.contains("full") || dialogueText.contains("inventory")) {
                        script.log(getClass(), "inventory full dialogue detected, syncing cache to 28");
                        cachedInventoryCount = INVENTORY_SIZE;
                        script.getWidgetManager().getDialogue().continueChatDialogue();
                        script.pollFramesUntil(() ->
                            script.getWidgetManager().getDialogue().getDialogueType() == null,
                            2000);
                        return finishFungusPickup();
                    }
                }
                // dismiss any other dialogue that appeared
                script.getWidgetManager().getDialogue().continueChatDialogue();
                script.pollFramesUntil(() ->
                    script.getWidgetManager().getDialogue().getDialogueType() == null,
                    2000);
            }

            // update cached count
            int slotsRemaining = INVENTORY_SIZE - cachedInventoryCount;
            int fungusGained = Math.min(fungusPerLog, slotsRemaining);
            cachedInventoryCount += fungusGained;
            pendingCollectedCount++;
            script.log(getClass(), "picked log " + (pendingFungusIndex + 1) + "/" + pendingFungusPositions.size()
                    + " (+" + fungusGained + ", inv: " + cachedInventoryCount + "/" + INVENTORY_SIZE + ")");
        } else {
            script.log(getClass(), "failed to tap log at " + logPos.getX() + "," + logPos.getY());
        }

        pendingFungusIndex++;

        // if more logs remain, return quickly so next poll picks up the next one
        if (pendingFungusIndex < pendingFungusPositions.size()) {
            return RandomUtils.weightedRandom(50, 150);
        }

        return finishFungusPickup();
    }

    // reset collection state and re-enable afk after all logs picked
    private int finishFungusPickup() {
        script.log(getClass(), "picked " + pendingCollectedCount + " log(s), inventory now " + cachedInventoryCount + "/" + INVENTORY_SIZE);
        pendingFungusPositions = null;
        pendingFungusIndex = 0;
        pendingCollectedCount = 0;
        allowAFK = true;

        // occasional human delay after collection cycle
        if (RandomUtils.weightedRandom(0, 100) < 25) {
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));
        }

        return RandomUtils.weightedRandom(50, 150);
    }

    private List<WorldPosition> detectFungusPositions() {
        List<WorldPosition> positions = new ArrayList<>();

        SearchablePixel fungusPixel = new SearchablePixel(
                FUNGUS_CLUSTER_COLOR,
                new SingleThresholdComparator(FUNGUS_CLUSTER_TOLERANCE),
                ColorModel.RGB
        );
        PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                FUNGUS_CLUSTER_MAX_DISTANCE,
                FUNGUS_CLUSTER_MIN_SIZE,
                new SearchablePixel[]{fungusPixel}
        );

        // select log positions based on mode
        WorldPosition[] logPositions = isFairyRingMode() ? THREE_LOG_POSITIONS : LOG_POSITIONS;
        int expectedLogs = isFairyRingMode() ? 3 : 4;

        for (WorldPosition logPos : logPositions) {
            Polygon tilePoly = script.getSceneProjector().getTileCube(logPos, 80);
            if (tilePoly == null) {
                continue;
            }

            PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(tilePoly, query);
            if (result != null && !result.getClusters().isEmpty()) {
                positions.add(logPos);
            }
        }

        script.log(getClass(), "detected " + positions.size() + "/" + expectedLogs + " log(s) with fungus");
        return positions;
    }

    // verify diary status by checking actual inventory count vs cached count
    // called ONLY the first time we think inventory is full
    private boolean verifyDiaryStatus() {
        script.log(getClass(), "verifying mory hard diary status...");

        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 300));

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(MORT_MYRE_FUNGUS));
        if (inv == null) {
            script.log(getClass(), "ERROR: can't read inventory for diary verification");
            return false;
        }

        int actualUsedSlots = INVENTORY_SIZE - inv.getFreeSlots();
        int actualFungus = inv.contains(MORT_MYRE_FUNGUS) ? inv.getAmount(MORT_MYRE_FUNGUS) : 0;

        script.log(getClass(), "diary check: cached=" + cachedInventoryCount + ", actual=" + actualUsedSlots + ", fungus=" + actualFungus);

        if (actualUsedSlots < cachedInventoryCount) {
            // more free slots than expected - diary NOT active
            script.log(getClass(), "DIARY NOT DETECTED: expected " + cachedInventoryCount + " slots used, actually " + actualUsedSlots);
            script.log(getClass(), "switching to fungusPerLog=1 (no mory hard diary)");
            fungusPerLog = 1;
            cachedInventoryCount = actualUsedSlots;
        } else {
            script.log(getClass(), "DIARY CONFIRMED: mory hard diary active (fungusPerLog=2)");
            fungusPerLog = 2;
            cachedInventoryCount = actualUsedSlots;
        }

        diaryStatusVerified = true;
        return true;
    }

    // --- delegate to helpers ---

    @Override
    public int bank() {
        return bankingHelper.bank();
    }

    @Override
    public int restorePrayer() {
        return prayerHelper.restorePrayer();
    }

    @Override
    public int returnToArea() {
        return returnHelper.returnToArea();
    }

    // --- paint overlay ---

    @Override
    public void onPaint(Canvas c) {
        WorldPosition[] logPositions = isFairyRingMode() ? THREE_LOG_POSITIONS : LOG_POSITIONS;

        for (int i = 0; i < logPositions.length; i++) {
            Polygon tileCube = script.getSceneProjector().getTileCube(logPositions[i], 50);
            if (tileCube == null) continue;

            Color fill;
            Color outline;

            if (pendingFungusPositions != null) {
                if (i < pendingFungusIndex) {
                    // already picked this cycle - gray
                    fill = new Color(128, 128, 128, 50);
                    outline = Color.GRAY;
                } else if (pendingFungusPositions.contains(logPositions[i])) {
                    // pending pickup - green
                    fill = new Color(0, 255, 0, 50);
                    outline = Color.GREEN;
                } else {
                    // no fungus detected on this log
                    fill = new Color(128, 128, 128, 50);
                    outline = Color.GRAY;
                }
            } else {
                // idle - dim red
                fill = new Color(255, 0, 0, 30);
                outline = new Color(180, 60, 60);
            }

            c.fillPolygon(tileCube, fill.getRGB(), 0.3);
            c.drawPolygon(tileCube, outline.getRGB(), 1.0);
        }
    }

    // --- helpers ---

    private Set<Integer> toIntegerSet(int[] arr) {
        Set<Integer> set = new HashSet<>();
        for (int i : arr) {
            set.add(i);
        }
        return set;
    }
}
