package strategies;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.ui.spellbook.SpellNotFoundException;
import com.osmb.api.ui.spellbook.StandardSpellbook;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import com.osmb.api.walker.WalkConfig;
import main.TidalsSecondaryCollector.State;
import utilities.RetryUtils;

import java.awt.Point;
import java.util.*;

import static main.TidalsSecondaryCollector.*;

public class MortMyreFungusCollector implements SecondaryCollectorStrategy {

    // mode detection
    public enum Mode { VER_SINHAZA, FAIRY_RING }
    private Mode detectedMode = null;

    // dramen staff item id
    private static final int DRAMEN_STAFF = 772;

    private final Script script;

    // item ids - bloom tools
    private static final int SILVER_SICKLE_B = 2963;
    private static final int EMERALD_SICKLE_B = 22433;
    private static final int RUBY_SICKLE_B = 24693;
    private static final int IVANDIS_FLAIL = 22398;
    private static final int BLISTERWOOD_FLAIL = 24699;

    private static final int[] BLOOM_TOOLS = {
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
    private static final int ARDOUGNE_CLOAK_1 = 13121;
    private static final int ARDOUGNE_CLOAK_2 = 13122;
    private static final int ARDOUGNE_CLOAK_3 = 13123;
    private static final int ARDOUGNE_CLOAK_4 = 13124;
    private static final int[] ARDOUGNE_CLOAKS = {
            ARDOUGNE_CLOAK_1, ARDOUGNE_CLOAK_2, ARDOUGNE_CLOAK_3, ARDOUGNE_CLOAK_4
    };

    // quest cape (alternative to ardy cloak for fairy ring mode teleport)
    private static final int QUEST_CAPE = 9813;

    // return teleport
    private static final int DRAKANS_MEDALLION = 22400;

    // item to collect
    private static final int MORT_MYRE_FUNGUS = 2970;

    // fungus pixel colors (RGB values from debug tool)
    private static final int FUNGUS_COLOR_1 = -7453660;
    private static final int FUNGUS_COLOR_2 = -10933482;
    private static final int FUNGUS_COLOR_3 = -883273;
    private static final int FUNGUS_COLOR_4 = -8635360;

    // pixel detection settings
    private static final int COLOR_TOLERANCE = 8;

    // ver sinhaza mode locations
    private static final WorldPosition FOUR_LOG_TILE = new WorldPosition(3667, 3255, 0);
    private static final RectangleArea LOG_AREA = new RectangleArea(3665, 3253, 3669, 3257, 0);

    // waypoint path from ver sinhaza teleport to log tile
    private static final List<WorldPosition> VER_SINHAZA_TO_LOGS_PATH = Arrays.asList(
        new WorldPosition(3654, 3231, 0),
        new WorldPosition(3661, 3232, 0),
        new WorldPosition(3662, 3236, 0),
        new WorldPosition(3662, 3240, 0),
        new WorldPosition(3660, 3244, 0),
        new WorldPosition(3660, 3251, 0),
        new WorldPosition(3662, 3256, 0),
        new WorldPosition(3666, 3260, 0),
        new WorldPosition(3666, 3257, 0),
        new WorldPosition(3668, 3255, 0)
    );

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

    // zanaris banking (fairy ring mode)
    private static final WorldPosition MORT_MYRE_FAIRY_RING = new WorldPosition(3469, 3431, 0);
    private static final WorldPosition ZANARIS_BANK_TILE = new WorldPosition(2384, 4459, 0);
    private static final RectangleArea ZANARIS_AREA = new RectangleArea(2375, 4419, 64, 48, 0);
    private static final RectangleArea ZANARIS_BANK_AREA = new RectangleArea(2381, 4454, 7, 7, 0);
    private static final RectangleArea ZANARIS_FAIRY_RING_AREA = new RectangleArea(2408, 4431, 8, 6, 0);
    private static final RectangleArea MORT_MYRE_FAIRY_RING_AREA = new RectangleArea(3466, 3428, 6, 6, 0);

    // monastery fairy ring return (fairy ring mode)
    private static final WorldPosition MONASTERY_FAIRY_RING = new WorldPosition(2658, 3230, 0);
    private static final RectangleArea MONASTERY_FAIRY_AREA = new RectangleArea(2653, 3226, 10, 9, 0);

    // shared locations
    private static final WorldPosition CRAFTING_GUILD_BANK_CHEST = new WorldPosition(2936, 3280, 0);
    private static final WorldPosition VER_SINHAZA_BANK_TILE = new WorldPosition(3651, 3211, 0);
    private static final WorldPosition KANDARIN_ALTAR = new WorldPosition(2605, 3211, 0);
    private static final WorldPosition LUMBRIDGE_ALTAR = new WorldPosition(3241, 3208, 0);

    // region ids for mort myre / ver sinhaza area
    private static final int REGION_MORT_MYRE_1 = 14642;
    private static final int REGION_MORT_MYRE_2 = 14643;

    // prayer restoration areas
    private static final RectangleArea MONASTERY_AREA = new RectangleArea(2601, 3207, 10, 14, 0);

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

        // pre-trip prayer restore if not near collection area and prayer not full
        WorldPosition currentPos = script.getWorldPosition();
        WorldPosition targetLogTile = isFairyRingMode() ? THREE_LOG_TILE : FOUR_LOG_TILE;
        boolean nearLogArea = currentPos != null && currentPos.distanceTo(targetLogTile) <= 20;

        if (!nearLogArea) {
            Integer prayerPercent = script.getWidgetManager().getMinimapOrbs().getPrayerPointsPercentage();
            if (prayerPercent != null && prayerPercent < 100) {
                script.log(getClass(), "not near log area and prayer not full (" + prayerPercent + "%), restoring before first trip");
                restorePrayerBeforeTrip();
            }
        } else {
            script.log(getClass(), "already near log area, skipping pre-trip prayer restore");
        }

        script.log(getClass(), "all requirements verified");
        return true;
    }

    private void restorePrayerBeforeTrip() {
        script.log(getClass(), "restoring prayer using: " + detectedPrayerMethod);

        if (detectedPrayerMethod.equals("ardy_inventory")) {
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(toIntegerSet(ARDOUGNE_CLOAKS));
            if (inv != null) {
                for (int cloakId : ARDOUGNE_CLOAKS) {
                    if (inv.contains(cloakId)) {
                        ItemSearchResult cloak = inv.getItem(cloakId);
                        if (cloak != null) {
                            boolean success = RetryUtils.inventoryInteract(script, cloak, "Monastery Teleport", "pre-trip ardy cloak (inventory)");
                            if (success) {
                                script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
                                walkToAltarAndPray(KANDARIN_ALTAR, "kandarin altar");
                                return;
                            }
                        }
                    }
                }
            }
        } else if (detectedPrayerMethod.equals("ardy_equipped")) {
            script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

            for (int cloakId : ARDOUGNE_CLOAKS) {
                UIResult<ItemSearchResult> ardyCloak = script.getWidgetManager().getEquipment().findItem(cloakId);
                if (ardyCloak.isFound()) {
                    boolean success = RetryUtils.equipmentInteract(script, cloakId, "Kandarin Monastery", "pre-trip ardy cloak (equipped)");
                    if (success) {
                        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
                        walkToAltarAndPray(KANDARIN_ALTAR, "kandarin altar");
                        return;
                    }
                }
            }
        } else {
            try {
                boolean success = script.getWidgetManager().getSpellbook().selectSpell(
                        StandardSpellbook.LUMBRIDGE_TELEPORT, null);
                if (success) {
                    script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
                    walkToAltarAndPray(LUMBRIDGE_ALTAR, "lumbridge altar");
                }
            } catch (SpellNotFoundException e) {
                script.log(getClass(), "failed to cast lumbridge teleport: " + e.getMessage());
            }
        }
    }

    // walk to altar and pray - used only during setup (restorePrayerBeforeTrip)
    private void walkToAltarAndPray(WorldPosition altarPos, String altarName) {
        WalkConfig config = new WalkConfig.Builder()
                .breakCondition(() -> {
                    WorldPosition myPos = script.getWorldPosition();
                    if (myPos == null) return false;
                    RSObject altar = script.getObjectManager().getClosestObject(myPos, "Altar");
                    return altar != null && altar.getWorldPosition().distanceTo(myPos) <= 3;
                })
                .breakDistance(3)
                .build();
        script.getWalker().walkTo(altarPos, config);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(500, 800));

        RSObject altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
        if (altar != null) {
            boolean success = RetryUtils.objectInteract(script, altar, "Pray-at", altarName);
            if (success) {
                script.pollFramesUntil(() -> {
                    Integer prayerPercent = script.getWidgetManager().getMinimapOrbs().getPrayerPointsPercentage();
                    return prayerPercent != null && prayerPercent >= 100;
                }, 5000);
                script.log(getClass(), "prayer restored");
            }
        }
    }

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

        // wait for bloom animation (~3 ticks = 1800ms), ignoreTasks to prevent random tab opens
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(1800, 2000), true);

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
                if (textResult.isFound()) {
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
        if (RandomUtils.uniformRandom(0, 3) == 0) {
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));
        }

        return RandomUtils.weightedRandom(50, 150);
    }

    private List<WorldPosition> detectFungusPositions() {
        List<WorldPosition> positions = new ArrayList<>();

        SingleThresholdComparator tolerance = new SingleThresholdComparator(COLOR_TOLERANCE);
        SearchablePixel[] fungusColors = {
            new SearchablePixel(FUNGUS_COLOR_1, tolerance, ColorModel.RGB),
            new SearchablePixel(FUNGUS_COLOR_2, tolerance, ColorModel.RGB),
            new SearchablePixel(FUNGUS_COLOR_3, tolerance, ColorModel.RGB),
            new SearchablePixel(FUNGUS_COLOR_4, tolerance, ColorModel.RGB)
        };

        // select log positions based on mode
        WorldPosition[] logPositions = isFairyRingMode() ? THREE_LOG_POSITIONS : LOG_POSITIONS;
        int expectedLogs = isFairyRingMode() ? 3 : 4;

        for (WorldPosition logPos : logPositions) {
            Polygon tilePoly = script.getSceneProjector().getTileCube(logPos, 80);
            if (tilePoly == null) {
                continue;
            }

            Point fungusPixel = script.getPixelAnalyzer().findPixel(tilePoly, fungusColors);
            if (fungusPixel != null) {
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

    @Override
    public int bank() {
        // check if bank is already open
        if (script.getWidgetManager().getBank().isVisible()) {
            return handleBankInterface();
        }

        // try to find and open bank at current location
        RSObject bankObject = findNearbyBank();
        WorldPosition myPos = script.getWorldPosition();
        if (bankObject != null && myPos != null) {
            double dist = bankObject.getWorldPosition().distanceTo(myPos);
            if (dist <= 5) {
                return openBank(bankObject);
            }
            // bank found but too far - walk toward it
            if (dist <= 30) {
                script.log(getClass(), "bank " + dist + " tiles away, walking closer");
                WalkConfig config = new WalkConfig.Builder()
                        .breakCondition(() -> {
                            WorldPosition p = script.getWorldPosition();
                            if (p == null) return false;
                            RSObject bank = findNearbyBank();
                            return bank != null && bank.getWorldPosition().distanceTo(p) <= 5;
                        })
                        .breakDistance(3)
                        .build();
                script.getWalker().walkTo(bankObject.getWorldPosition(), config);
                return 0;  // re-enter to open bank
            }
        }

        // need to teleport to bank
        return teleportToBank();
    }

    private RSObject findNearbyBank() {
        return script.getObjectManager().getClosestObject(
                script.getWorldPosition(),
                "Bank booth", "Bank chest", "Grand Exchange booth"
        );
    }

    private int openBank(RSObject bankObject) {
        Polygon bankPoly = bankObject.getConvexHull();
        if (bankPoly == null) {
            script.log(getClass(), "bank polygon null");
            return 600;
        }

        String objectName = bankObject.getName();
        String action = (objectName != null && objectName.toLowerCase().contains("chest")) ? "Use" : "Bank";
        boolean success = RetryUtils.objectInteract(script, bankObject, action, "bank (" + objectName + ")");
        if (!success) {
            return 600;
        }

        boolean opened = script.pollFramesUntil(() ->
                        script.getWidgetManager().getBank().isVisible(),
                5000
        );

        if (!opened) {
            script.log(getClass(), "bank didn't open");
            return 600;
        }

        return handleBankInterface();
    }

    private int handleBankInterface() {
        script.log(getClass(), "handling bank interface");

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));

        // count fungus in inventory before depositing
        ItemGroupResult invBeforeDeposit = script.getWidgetManager().getInventory().search(Set.of(MORT_MYRE_FUNGUS));
        int fungusToBank = 0;
        if (invBeforeDeposit != null && invBeforeDeposit.contains(MORT_MYRE_FUNGUS)) {
            fungusToBank = invBeforeDeposit.getAmount(MORT_MYRE_FUNGUS);
        }

        // deposit all except items we need to keep
        Set<Integer> keepItems = new HashSet<>(toIntegerSet(ARDOUGNE_CLOAKS));

        // fairy ring mode: also keep bloom tool (it's in inventory, not equipped)
        if (isFairyRingMode()) {
            keepItems.addAll(toIntegerSet(BLOOM_TOOLS));
        }

        script.getWidgetManager().getBank().depositAll(keepItems);

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 600));

        // track banked items
        if (fungusToBank > 0) {
            itemsBanked += fungusToBank;
            script.log(getClass(), "banked " + fungusToBank + " fungus (total: " + itemsBanked + ")");
        }

        // sync cached inventory count after deposit
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
        if (inv != null) {
            cachedInventoryCount = INVENTORY_SIZE - inv.getFreeSlots();
            script.log(getClass(), "synced cached inventory count: " + cachedInventoryCount);
        } else {
            cachedInventoryCount = 0;
        }

        // close bank
        script.getWidgetManager().getBank().close();
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        bankTrips++;
        script.log(getClass(), "banking complete, bank trips: " + bankTrips);

        return 0;
    }

    private int teleportToBank() {
        script.log(getClass(), "teleporting to bank");

        // fairy ring mode: use zanaris banking
        if (isFairyRingMode()) {
            return useZanarisBanking();
        }

        // priority 1: crafting cape (ver sinhaza mode only)
        try {
            for (int capeId : CRAFTING_CAPES) {
                UIResult<ItemSearchResult> cape = script.getWidgetManager().getEquipment().findItem(capeId);
                if (cape.isFound()) {
                    return useCraftingCapeTeleport();
                }
            }
        } catch (RuntimeException e) {
            script.log(getClass(), "error checking crafting cape: " + e.getMessage());
        }

        // priority 2: ver sinhaza bank
        return useVerSinhazaBanking();
    }

    private int useCraftingCapeTeleport() {
        script.log(getClass(), "using crafting cape teleport");

        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        int capeId = 0;
        for (int id : CRAFTING_CAPES) {
            if (script.getWidgetManager().getEquipment().findItem(id).isFound()) {
                capeId = id;
                break;
            }
        }

        if (capeId == 0) {
            script.log(getClass(), "crafting cape not found");
            return 600;
        }

        boolean success = RetryUtils.equipmentInteract(script, capeId, "Teleport", "crafting cape teleport");
        if (!success) {
            return 600;
        }

        // wait for teleport, then re-enter bank() which will find the nearby bank
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
        return 0;
    }

    private int useVerSinhazaBanking() {
        script.log(getClass(), "using ver sinhaza bank (drakan's medallion)");

        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        boolean success = RetryUtils.equipmentInteract(script, DRAKANS_MEDALLION, "Ver Sinhaza", "drakan's medallion teleport");
        if (!success) {
            return 600;
        }

        // wait for teleport, then re-enter bank() to walk + open
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
        return 0;
    }

    // --- zanaris banking (fairy ring mode) ---

    private int useZanarisBanking() {
        script.log(getClass(), "using zanaris bank (fairy ring mode)");

        WorldPosition pos = script.getWorldPosition();
        if (pos == null) {
            script.log(getClass(), "can't read position");
            return 600;
        }

        // check if already in zanaris
        if (ZANARIS_AREA.contains(pos)) {
            script.log(getClass(), "already in zanaris, walking to bank");
            return walkToZanarisBank();
        }

        // determine which fairy ring to use based on location
        boolean nearMonastery = MONASTERY_AREA.contains(pos) || MONASTERY_FAIRY_AREA.contains(pos)
                || pos.distanceTo(MONASTERY_FAIRY_RING) <= 30;
        boolean nearThreeLogSpot = THREE_LOG_AREA.contains(pos) || MORT_MYRE_FAIRY_RING_AREA.contains(pos)
                || pos.distanceTo(MORT_MYRE_FAIRY_RING) <= 30;

        if (nearMonastery) {
            return useMonasteryFairyRingToZanaris();
        } else if (nearThreeLogSpot) {
            return useMortMyreFairyRingToZanaris();
        } else {
            // not near either fairy ring - teleport to monastery first
            script.log(getClass(), "not near any fairy ring, teleporting to monastery");
            boolean teleported = tryArdougneCloakTeleport();
            if (!teleported) {
                script.log(getClass(), "ERROR: failed to teleport to monastery for banking");
                return 600;
            }
            return 0;  // re-enter state machine to continue from monastery
        }
    }

    private int useMonasteryFairyRingToZanaris() {
        script.log(getClass(), "using monastery fairy ring to zanaris");

        RSObject fairyRing = getSpecificObjectAt("Fairy ring", MONASTERY_FAIRY_RING.getX(), MONASTERY_FAIRY_RING.getY(), 0);
        if (fairyRing == null || !fairyRing.isInteractableOnScreen()) {
            script.log(getClass(), "monastery fairy ring not on screen, walking to it");
            WalkConfig config = new WalkConfig.Builder()
                .enableRun(true)
                .breakCondition(() -> {
                    RSObject ring = getSpecificObjectAt("Fairy ring", MONASTERY_FAIRY_RING.getX(), MONASTERY_FAIRY_RING.getY(), 0);
                    return ring != null && ring.isInteractableOnScreen();
                })
                .build();
            script.getWalker().walkTo(MONASTERY_FAIRY_AREA.getRandomPosition(), config);
            fairyRing = getSpecificObjectAt("Fairy ring", MONASTERY_FAIRY_RING.getX(), MONASTERY_FAIRY_RING.getY(), 0);
        }

        if (fairyRing == null) {
            script.log(getClass(), "ERROR: monastery fairy ring not found");
            return 600;
        }

        boolean success = RetryUtils.objectInteract(script, fairyRing, "Zanaris", "monastery fairy ring to zanaris");
        if (!success) {
            script.log(getClass(), "failed to interact with monastery fairy ring");
            return 600;
        }

        // wait for teleport and verify arrival
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(3000, 4000));
        boolean arrived = script.pollFramesUntil(() -> {
            WorldPosition p = script.getWorldPosition();
            return p != null && ZANARIS_AREA.contains(p);
        }, 10000);

        if (!arrived) {
            script.log(getClass(), "failed to arrive in zanaris from monastery");
            return 600;
        }

        return walkToZanarisBank();
    }

    private int useMortMyreFairyRingToZanaris() {
        script.log(getClass(), "using mort myre fairy ring to zanaris");

        RSObject fairyRing = getSpecificObjectAt("Fairy ring", MORT_MYRE_FAIRY_RING.getX(), MORT_MYRE_FAIRY_RING.getY(), 0);
        if (fairyRing == null || !fairyRing.isInteractableOnScreen()) {
            script.log(getClass(), "mort myre fairy ring not on screen, walking to it");
            WalkConfig config = new WalkConfig.Builder()
                .enableRun(true)
                .breakCondition(() -> {
                    RSObject ring = getSpecificObjectAt("Fairy ring", MORT_MYRE_FAIRY_RING.getX(), MORT_MYRE_FAIRY_RING.getY(), 0);
                    return ring != null && ring.isInteractableOnScreen();
                })
                .build();
            script.getWalker().walkTo(MORT_MYRE_FAIRY_RING_AREA.getRandomPosition(), config);
            fairyRing = getSpecificObjectAt("Fairy ring", MORT_MYRE_FAIRY_RING.getX(), MORT_MYRE_FAIRY_RING.getY(), 0);
        }

        if (fairyRing == null) {
            script.log(getClass(), "ERROR: mort myre fairy ring not found");
            return 600;
        }

        boolean success = RetryUtils.objectInteract(script, fairyRing, "Zanaris", "mort myre fairy ring to zanaris");
        if (!success) {
            script.log(getClass(), "failed to interact with mort myre fairy ring");
            return 600;
        }

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(3000, 4000));
        boolean arrived = script.pollFramesUntil(() -> {
            WorldPosition p = script.getWorldPosition();
            return p != null && ZANARIS_AREA.contains(p);
        }, 10000);

        if (!arrived) {
            script.log(getClass(), "failed to arrive in zanaris from mort myre");
            return 600;
        }

        return walkToZanarisBank();
    }

    private int walkToZanarisBank() {
        script.log(getClass(), "walking to zanaris bank");

        WalkConfig config = new WalkConfig.Builder()
            .enableRun(true)
            .breakCondition(() -> {
                RSObject bank = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Bank chest");
                return bank != null && bank.isInteractableOnScreen();
            })
            .build();

        script.getWalker().walkTo(ZANARIS_BANK_AREA.getRandomPosition(), config);

        RSObject bankChest = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Bank chest");
        if (bankChest != null && bankChest.isInteractableOnScreen()) {
            return openBank(bankChest);
        }

        script.log(getClass(), "zanaris bank chest not on screen");
        return 0;  // re-enter state machine
    }

    // --- prayer restoration ---

    @Override
    public int restorePrayer() {
        WorldPosition myPos = script.getWorldPosition();
        RSObject altar = myPos != null ? script.getObjectManager().getClosestObject(myPos, "Altar") : null;
        if (altar != null && myPos != null && altar.getWorldPosition().distanceTo(myPos) <= 5) {
            return prayAtAltar(altar);
        }

        WorldPosition pos = script.getWorldPosition();
        if (pos != null && MONASTERY_AREA.contains(pos)) {
            script.log(getClass(), "already at monastery area, walking to altar");
            return walkToKandarinAltar();
        }

        // priority 1: ardougne cloak (free teleport)
        if (tryArdougneCloakTeleport()) {
            return walkToKandarinAltar();
        }

        // priority 2: lumbridge teleport
        return useLumbridgeTeleport();
    }

    private boolean tryArdougneCloakTeleport() {
        // check inventory first
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(toIntegerSet(ARDOUGNE_CLOAKS));
        if (inv != null) {
            for (int cloakId : ARDOUGNE_CLOAKS) {
                if (inv.contains(cloakId)) {
                    ItemSearchResult cloak = inv.getItem(cloakId);
                    if (cloak != null) {
                        boolean success = RetryUtils.inventoryInteract(script, cloak, "Monastery Teleport", "ardougne cloak (inventory)");
                        if (success) {
                            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
                            return true;
                        }
                    }
                }
            }
        }

        // check equipped
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        for (int cloakId : ARDOUGNE_CLOAKS) {
            UIResult<ItemSearchResult> ardyCloak = script.getWidgetManager().getEquipment().findItem(cloakId);
            if (ardyCloak.isFound()) {
                boolean success = RetryUtils.equipmentInteract(script, cloakId, "Kandarin Monastery", "ardougne cloak (equipped)");
                if (success) {
                    script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
                    return true;
                }
            }
        }

        script.log(getClass(), "no ardougne cloak found");
        return false;
    }

    private int walkToKandarinAltar() {
        WorldPosition myPos = script.getWorldPosition();
        RSObject altar = myPos != null ? script.getObjectManager().getClosestObject(myPos, "Altar") : null;

        if (altar != null && myPos != null && altar.getWorldPosition().distanceTo(myPos) <= 5) {
            return prayAtAltar(altar);
        }

        script.log(getClass(), "walking to kandarin altar");
        WalkConfig config = new WalkConfig.Builder()
                .breakCondition(() -> {
                    WorldPosition pos = script.getWorldPosition();
                    if (pos == null) return false;
                    RSObject a = script.getObjectManager().getClosestObject(pos, "Altar");
                    return a != null && a.getWorldPosition().distanceTo(pos) <= 3;
                })
                .breakDistance(2)
                .timeout(10000)
                .build();

        script.getWalker().walkTo(KANDARIN_ALTAR, config);

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(400, 600));

        altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
        if (altar != null) {
            script.log(getClass(), "found altar after walking, praying");
            return prayAtAltar(altar);
        }

        script.log(getClass(), "altar not visible, searching nearby");
        return 600;
    }

    private int useLumbridgeTeleport() {
        script.log(getClass(), "using lumbridge teleport");

        try {
            boolean success = script.getWidgetManager().getSpellbook().selectSpell(
                    StandardSpellbook.LUMBRIDGE_TELEPORT, null);

            if (!success) {
                script.log(getClass(), "ERROR: failed to cast lumbridge teleport - stopping");
                script.stop();
                return 0;
            }

            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
            return walkToLumbridgeChurch();

        } catch (SpellNotFoundException e) {
            script.log(getClass(), "ERROR: lumbridge teleport not available - stopping");
            script.stop();
            return 0;
        }
    }

    private int walkToLumbridgeChurch() {
        WorldPosition myPos = script.getWorldPosition();
        RSObject altar = myPos != null ? script.getObjectManager().getClosestObject(myPos, "Altar") : null;

        if (altar != null && myPos != null && altar.getWorldPosition().distanceTo(myPos) <= 5) {
            return prayAtAltar(altar);
        }

        script.log(getClass(), "walking to lumbridge church");
        WalkConfig config = new WalkConfig.Builder()
                .breakCondition(() -> {
                    WorldPosition pos = script.getWorldPosition();
                    if (pos == null) return false;
                    RSObject a = script.getObjectManager().getClosestObject(pos, "Altar");
                    return a != null && a.getWorldPosition().distanceTo(pos) <= 3;
                })
                .breakDistance(2)
                .timeout(15000)
                .build();

        script.getWalker().walkTo(LUMBRIDGE_ALTAR, config);

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(400, 600));

        altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
        if (altar != null) {
            script.log(getClass(), "found altar after walking, praying");
            return prayAtAltar(altar);
        }

        script.log(getClass(), "lumbridge altar not visible, searching nearby");
        return 600;
    }

    private int prayAtAltar(RSObject altar) {
        boolean success = RetryUtils.objectInteract(script, altar, "Pray-at", "altar");
        if (!success) {
            return 600;
        }

        boolean restored = script.pollFramesUntil(() -> {
            Integer prayer = script.getWidgetManager().getMinimapOrbs().getPrayerPoints();
            return prayer != null && prayer >= 30;
        }, RandomUtils.gaussianRandom(2500, 3500, 3000, 250));

        if (restored) {
            script.log(getClass(), "prayer restored");
        } else {
            script.log(getClass(), "prayer restoration timeout");
        }

        return 0;
    }

    // --- return to area ---

    @Override
    public int returnToArea() {
        WorldPosition pos = script.getWorldPosition();

        // fairy ring mode: use ardy cloak -> monastery -> fairy ring -> BKR
        if (isFairyRingMode()) {
            if (pos != null && THREE_LOG_AREA.contains(pos)) {
                script.log(getClass(), "already at 3-log area");
                return 0;
            }
            return useFairyRingReturn();
        }

        // ver sinhaza mode
        if (pos != null && LOG_AREA.contains(pos)) {
            script.log(getClass(), "already at log area");
            return 0;
        }

        int currentRegion = getCurrentRegion();
        script.log(getClass(), "current region: " + currentRegion);

        boolean inMortMyre = currentRegion == REGION_MORT_MYRE_1 || currentRegion == REGION_MORT_MYRE_2;
        if (!inMortMyre) {
            return useDrakansMedallionTeleport();
        }

        return walkToLogTile();
    }

    private int getCurrentRegion() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return 0;
        return ((pos.getX() >> 6) << 8) | (pos.getY() >> 6);
    }

    private int useDrakansMedallionTeleport() {
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        boolean success = RetryUtils.equipmentInteract(script, DRAKANS_MEDALLION, "Ver Sinhaza", "drakan's medallion teleport");
        if (!success) {
            script.log(getClass(), "ERROR: failed to use drakan's medallion");
            script.stop();
            return 0;
        }

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
        return walkToLogTile();
    }

    private int walkToLogTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos != null && LOG_AREA.contains(pos)) {
            script.log(getClass(), "arrived at log area");
            return 0;
        }

        script.log(getClass(), "walking to 4 log tile via waypoint path");

        WalkConfig config = new WalkConfig.Builder()
                .setWalkMethods(false, true)
                .tileRandomisationRadius(0)
                .breakDistance(2)
                .timeout(30000)
                .build();

        boolean arrived = script.getWalker().walkPath(VER_SINHAZA_TO_LOGS_PATH, config);

        if (arrived) {
            script.log(getClass(), "arrived at log area");
            return 0;
        }

        script.log(getClass(), "walk incomplete, will retry");
        return 600;
    }

    // --- fairy ring return methods ---

    private int useFairyRingReturn() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) {
            script.log(getClass(), "can't read position");
            return 600;
        }

        // if in zanaris - use fairy ring directly (skip monastery)
        if (ZANARIS_AREA.contains(pos)) {
            script.log(getClass(), "in zanaris, using fairy ring to return to bkr");
            return useZanarisFairyRingReturn();
        }

        // if fairy ring already interactable on screen
        RSObject fairyRing = script.getObjectManager().getClosestObject(pos, "Fairy ring");
        boolean ringOnScreen = fairyRing != null && fairyRing.isInteractableOnScreen();

        if (ringOnScreen || MONASTERY_FAIRY_AREA.contains(pos)) {
            return interactWithMonasteryFairyRing();
        }

        // if in monastery area, walk to fairy ring (prayer restore handled by determineState)
        if (MONASTERY_AREA.contains(pos)) {
            script.log(getClass(), "walking to monastery fairy ring");
            WalkConfig config = new WalkConfig.Builder()
                .enableRun(true)
                .breakCondition(() -> {
                    RSObject ring = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Fairy ring");
                    return ring != null && ring.isInteractableOnScreen();
                })
                .build();

            script.getWalker().walkTo(MONASTERY_FAIRY_AREA.getRandomPosition(), config);
            return 0;  // will re-enter and hit the interactable check above
        }

        // not at monastery or zanaris - teleport to monastery
        script.log(getClass(), "teleporting to monastery");
        boolean teleported = tryArdougneCloakTeleport();
        if (!teleported) {
            script.log(getClass(), "ERROR: failed to teleport to monastery");
            return 600;
        }

        return 0;  // re-enter state machine
    }

    private int useZanarisFairyRingReturn() {
        RSObject fairyRing = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Fairy ring");

        if (fairyRing == null || !fairyRing.isInteractableOnScreen()) {
            script.log(getClass(), "zanaris fairy ring not on screen, walking to it");
            WalkConfig config = new WalkConfig.Builder()
                .enableRun(true)
                .breakCondition(() -> {
                    RSObject ring = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Fairy ring");
                    return ring != null && ring.isInteractableOnScreen();
                })
                .build();
            script.getWalker().walkTo(ZANARIS_FAIRY_RING_AREA.getRandomPosition(), config);
            fairyRing = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Fairy ring");
        }

        if (fairyRing == null) {
            script.log(getClass(), "ERROR: zanaris fairy ring not found");
            return 600;
        }

        script.log(getClass(), "using zanaris fairy ring last-destination (bkr)");
        boolean success = RetryUtils.objectInteract(script, fairyRing, "last-destination (bkr)", "zanaris fairy ring to bkr");
        if (!success) {
            script.log(getClass(), "failed to interact with zanaris fairy ring");
            return 600;
        }

        // wait for teleport animation
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(4000, 5000));

        boolean arrived = script.pollFramesUntil(() -> {
            WorldPosition p = script.getWorldPosition();
            if (p == null) return false;
            return THREE_LOG_AREA.contains(p) || p.distanceTo(MORT_MYRE_FAIRY_RING) <= 10;
        }, 10000);

        if (!arrived) {
            script.log(getClass(), "failed to arrive in mort myre from zanaris");
            return 600;
        }

        WorldPosition currentPos = script.getWorldPosition();
        if (THREE_LOG_AREA.contains(currentPos)) {
            script.log(getClass(), "arrived at mort myre collection area");
            return 0;
        }

        return walkToFairyRingLogTile();
    }

    private int interactWithMonasteryFairyRing() {
        RSObject fairyRing = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Fairy ring");
        if (fairyRing == null) {
            script.log(getClass(), "fairy ring not found");
            return 600;
        }

        // use RetryUtils for reliable interaction
        script.log(getClass(), "using fairy ring last-destination (bkr)");
        boolean success = RetryUtils.objectInteract(script, fairyRing, "last-destination (bkr)", "monastery fairy ring to bkr");
        if (!success) {
            script.log(getClass(), "failed to interact with fairy ring");
            return 600;
        }

        // wait for teleport animation
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(4000, 5000));

        boolean arrived = script.pollFramesUntil(() -> {
            WorldPosition p = script.getWorldPosition();
            if (p == null) return false;
            return THREE_LOG_AREA.contains(p) || p.distanceTo(MORT_MYRE_FAIRY_RING) <= 10;
        }, 10000);

        if (!arrived) {
            script.log(getClass(), "failed to arrive in mort myre");
            return 600;
        }

        WorldPosition currentPos = script.getWorldPosition();
        if (THREE_LOG_AREA.contains(currentPos)) {
            script.log(getClass(), "arrived at mort myre collection area");
            return 0;
        }

        script.log(getClass(), "arrived near mort myre fairy ring, walking to collection tile");
        return walkToFairyRingLogTile();
    }

    private int walkToFairyRingLogTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos != null && THREE_LOG_AREA.contains(pos)) {
            script.log(getClass(), "arrived at 3-log area");
            return 0;
        }

        script.log(getClass(), "walking to 3-log tile");
        WalkConfig config = new WalkConfig.Builder()
            .breakDistance(2)
            .timeout(10000)
            .build();

        script.getWalker().walkTo(THREE_LOG_TILE, config);
        return 0;
    }

    // --- helpers ---

    private Set<Integer> toIntegerSet(int[] arr) {
        Set<Integer> set = new HashSet<>();
        for (int i : arr) {
            set.add(i);
        }
        return set;
    }

    // find object at specific world coordinates (for reliable fairy ring detection)
    private RSObject getSpecificObjectAt(String name, int worldX, int worldY, int plane) {
        return script.getObjectManager().getRSObject(obj ->
                obj != null
                        && obj.getName() != null
                        && name.equalsIgnoreCase(obj.getName())
                        && obj.getWorldX() == worldX
                        && obj.getWorldY() == worldY
                        && obj.getPlane() == plane
        );
    }
}
