package strategies;

import com.osmb.api.input.MenuEntry;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.ui.spellbook.SpellNotFoundException;
import com.osmb.api.ui.spellbook.StandardSpellbook;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;
import com.osmb.api.visual.PixelCluster;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import com.osmb.api.walker.WalkConfig;
import main.TidalsSecondaryCollector.State;
import utilities.RetryUtils;

import java.io.IOException;

import java.awt.Point;
import java.util.ArrayList;

import java.util.*;

import static main.TidalsSecondaryCollector.*;

public class MortMyreFungusCollector implements SecondaryCollectorStrategy {

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

    // return teleport
    private static final int DRAKANS_MEDALLION = 22400;

    // item to collect
    private static final int MORT_MYRE_FUNGUS = 2970;

    // fungus pixel colors (RGB values from debug tool)
    // these colors identify fungus growing on logs
    private static final int FUNGUS_COLOR_1 = -7453660;
    private static final int FUNGUS_COLOR_2 = -10933482;
    private static final int FUNGUS_COLOR_3 = -883273;
    private static final int FUNGUS_COLOR_4 = -8635360;

    // cluster detection settings
    private static final int CLUSTER_MAX_DISTANCE = 10;
    private static final int CLUSTER_MIN_SIZE = 2;
    private static final int COLOR_TOLERANCE = 8;

    // locations
    private static final WorldPosition FOUR_LOG_TILE = new WorldPosition(3667, 3255, 0);
    private static final RectangleArea LOG_AREA = new RectangleArea(3665, 3253, 3669, 3257, 0);

    // waypoint path from ver sinhaza teleport to log tile for smooth walking
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
    
    // the 4 log positions around the standing tile
    private static final WorldPosition[] LOG_POSITIONS = {
        new WorldPosition(3666, 3256, 0),
        new WorldPosition(3666, 3254, 0),
        new WorldPosition(3668, 3254, 0),
        new WorldPosition(3668, 3255, 0)
    };
    private static final WorldPosition CRAFTING_GUILD_BANK_CHEST = new WorldPosition(2936, 3280, 0);
    private static final WorldPosition VER_SINHAZA_BANK_TILE = new WorldPosition(3651, 3211, 0);
    private static final WorldPosition KANDARIN_ALTAR = new WorldPosition(2605, 3211, 0);
    private static final WorldPosition LUMBRIDGE_ALTAR = new WorldPosition(3241, 3208, 0);

    // region ids for mort myre / ver sinhaza area (4-log tile spans 14642/14643)
    private static final int REGION_MORT_MYRE_1 = 14642;
    private static final int REGION_MORT_MYRE_2 = 14643;

    // prayer restoration areas
    private static final RectangleArea MONASTERY_AREA = new RectangleArea(2601, 3207, 10, 14, 0);

    // track which bloom tool we found during setup
    private int equippedBloomToolId = 0;

    // track detected prayer method: "ardy_inventory", "ardy_equipped", or "lumbridge"
    private String detectedPrayerMethod = "lumbridge";

    // cached inventory count for efficient collection (avoids tab switching)
    // each log pickup gives exactly 2 fungus, so we can track without checking inventory
    private int cachedInventoryCount = -1;  // -1 = not initialized, sync on first bank/trip
    private static final int FUNGUS_PER_LOG = 2;
    private static final int INVENTORY_SIZE = 28;

    public MortMyreFungusCollector(Script script) {
        this.script = script;
    }

    @Override
    public State determineState() {
        // priority 1: check prayer (need at least 6 for bloom)
        Integer prayer = script.getWidgetManager().getMinimapOrbs().getPrayerPoints();
        if (prayer != null && prayer < 6) {
            script.log(getClass(), "low prayer (" + prayer + "), need to restore");
            return State.RESTORING_PRAYER;
        }

        // priority 2: check if inventory full (use cached count if available)
        // only bank when truly full (0 slots) - we can still pick with 1 slot to not waste it
        if (cachedInventoryCount >= 0) {
            int availableSlots = INVENTORY_SIZE - cachedInventoryCount;
            if (availableSlots <= 0) {
                script.log(getClass(), "inventory full (cached: " + cachedInventoryCount + "), need to bank");
                return State.BANKING;
            }
        } else {
            // fallback to actual check if cache not initialized
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
            if (inv != null && inv.isFull()) {
                script.log(getClass(), "inventory full, need to bank");
                return State.BANKING;
            }
        }

        // priority 3: check if at collection area
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) {
            return State.IDLE;
        }

        if (!LOG_AREA.contains(pos)) {
            script.log(getClass(), "not at log area, need to return");
            return State.RETURNING;
        }

        return State.COLLECTING;
    }

    @Override
    public boolean verifyRequirements() {
        script.log(getClass(), "verifying equipment requirements...");

        // open equipment tab
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));

        // 1. check bloom tool (weapon slot)
        UIResult<ItemSearchResult> bloomTool = script.getWidgetManager().getEquipment().findItem(BLOOM_TOOLS);
        if (!bloomTool.isFound()) {
            script.log(getClass(), "ERROR: no bloom tool equipped (blessed sickle or flail)");
            return false;
        }

        // remember which tool we found for later use
        for (int toolId : BLOOM_TOOLS) {
            UIResult<ItemSearchResult> check = script.getWidgetManager().getEquipment().findItem(toolId);
            if (check.isFound()) {
                equippedBloomToolId = toolId;
                script.log(getClass(), "bloom tool found: id=" + toolId);
                break;
            }
        }

        if (equippedBloomToolId == 0) {
            script.log(getClass(), "ERROR: could not determine bloom tool id");
            return false;
        }

        // 2. check drakan's medallion (neck slot)
        UIResult<ItemSearchResult> medallion = script.getWidgetManager().getEquipment().findItem(DRAKANS_MEDALLION);
        if (!medallion.isFound()) {
            script.log(getClass(), "ERROR: no drakan's medallion equipped");
            return false;
        }
        script.log(getClass(), "drakan's medallion: found");

        // 3. determine banking method (ver sinhaza via drakan's medallion is always available)
        String bankingMethodUsed = "ver sinhaza (drakan's medallion)";

        // check for crafting cape (best option)
        try {
            for (int capeId : CRAFTING_CAPES) {
                UIResult<ItemSearchResult> cape = script.getWidgetManager().getEquipment().findItem(capeId);
                if (cape != null && cape.isFound()) {
                    bankingMethodUsed = "crafting cape";
                    break;
                }
            }
        } catch (RuntimeException e) {
            script.log(getClass(), "error checking crafting cape: " + e.getMessage());
        }

        script.log(getClass(), "banking method: " + bankingMethodUsed);

        // 4. check prayer restoration method
        boolean hasPrayerMethod = false;
        String prayerMethodUsed = "";

        // check inventory for ardy cloak
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

        // check if ardy cloak equipped
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

        // fallback to lumbridge teleport
        if (!hasPrayerMethod) {
            hasPrayerMethod = true;
            prayerMethodUsed = "lumbridge teleport (spell)";
            detectedPrayerMethod = "lumbridge";
            script.log(getClass(), "warning: no ardougne cloak found, will use lumbridge teleport");
        }

        script.log(getClass(), "prayer method: " + prayerMethodUsed);

        // 5. pre-trip prayer restore if not near mort myre log area and prayer not full
        // skip if we're already close to the collection area - just start the trip
        WorldPosition currentPos = script.getWorldPosition();
        boolean nearLogArea = currentPos != null && currentPos.distanceTo(FOUR_LOG_TILE) <= 20;
        
        if (!nearLogArea) {
            Integer prayerPercent = script.getWidgetManager().getMinimapOrbs().getPrayerPointsPercentage();
            
            if (prayerPercent != null && prayerPercent < 100) {
                script.log(getClass(), "not near mort myre and prayer not full (" + prayerPercent + "%), restoring before first trip");
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
            // use ardy cloak from inventory
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(toIntegerSet(ARDOUGNE_CLOAKS));
            if (inv != null) {
                for (int cloakId : ARDOUGNE_CLOAKS) {
                    if (inv.contains(cloakId)) {
                        ItemSearchResult cloak = inv.getItem(cloakId);
                        if (cloak != null) {
                            boolean success = RetryUtils.inventoryInteract(script, cloak, "Monastery Teleport", "pre-trip ardy cloak (inventory)");
                            if (success) {
                                script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
                                walkToAltarAndPray();
                                return;
                            }
                        }
                    }
                }
            }
        } else if (detectedPrayerMethod.equals("ardy_equipped")) {
            // use ardy cloak from equipment
            script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

            for (int cloakId : ARDOUGNE_CLOAKS) {
                UIResult<ItemSearchResult> ardyCloak = script.getWidgetManager().getEquipment().findItem(cloakId);
                if (ardyCloak.isFound()) {
                    boolean success = RetryUtils.equipmentInteract(script,cloakId, "Kandarin Monastery", "pre-trip ardy cloak (equipped)");
                    if (success) {
                        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
                        walkToAltarAndPray();
                        return;
                    }
                }
            }
        } else {
            // lumbridge teleport fallback
            try {
                boolean success = script.getWidgetManager().getSpellbook().selectSpell(
                        StandardSpellbook.LUMBRIDGE_TELEPORT,
                        null
                );
                if (success) {
                    script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
                    walkToLumbridgeAltarAndPray();
                }
            } catch (SpellNotFoundException e) {
                script.log(getClass(), "failed to cast lumbridge teleport: " + e.getMessage());
            }
        }
    }

    private void walkToAltarAndPray() {
        // walk to kandarin altar and pray
        WalkConfig config = new WalkConfig.Builder()
                .breakCondition(() -> {
                    WorldPosition myPos = script.getWorldPosition();
                    if (myPos == null) return false;
                    RSObject altar = script.getObjectManager().getClosestObject(myPos, "Altar");
                    return altar != null && altar.getWorldPosition().distanceTo(myPos) <= 3;
                })
                .breakDistance(3)
                .build();
        script.getWalker().walkTo(KANDARIN_ALTAR, config);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(500, 800));

        RSObject altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
        if (altar != null) {
            boolean success = RetryUtils.objectInteract(script,altar, "Pray-at", "kandarin altar");
            if (success) {
                script.pollFramesUntil(() -> {
                    Integer prayerPercent = script.getWidgetManager().getMinimapOrbs().getPrayerPointsPercentage();
                    return prayerPercent != null && prayerPercent >= 100;
                }, 5000);
                script.log(getClass(), "prayer restored");
            }
        }
    }

    private void walkToLumbridgeAltarAndPray() {
        // walk to lumbridge church altar and pray
        WalkConfig config = new WalkConfig.Builder()
                .breakCondition(() -> {
                    WorldPosition myPos = script.getWorldPosition();
                    if (myPos == null) return false;
                    RSObject altar = script.getObjectManager().getClosestObject(myPos, "Altar");
                    return altar != null && altar.getWorldPosition().distanceTo(myPos) <= 3;
                })
                .breakDistance(3)
                .build();
        script.getWalker().walkTo(LUMBRIDGE_ALTAR, config);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(500, 800));

        RSObject altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
        if (altar != null) {
            boolean success = RetryUtils.objectInteract(script,altar, "Pray-at", "lumbridge altar");
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
        // initialize cached inventory count if not set (first trip without banking)
        if (cachedInventoryCount < 0) {
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
            cachedInventoryCount = (inv != null) ? (INVENTORY_SIZE - inv.getFreeSlots()) : 0;
            script.log(getClass(), "initialized cached inventory count: " + cachedInventoryCount);
        }

        // check prayer before every bloom (uses minimap orbs, no tab switch needed)
        Integer prayer = script.getWidgetManager().getMinimapOrbs().getPrayerPoints();
        if (prayer == null) {
            script.log(getClass(), "can't read prayer, retrying");
            return 600;
        }

        if (prayer < 6) {
            script.log(getClass(), "prayer too low (" + prayer + "), switching to restore");
            return 0;
        }

        // check inventory using cached count (no tab switch needed)
        // only switch to bank when truly full - we can pick with 1 slot to not waste it
        int availableSlots = INVENTORY_SIZE - cachedInventoryCount;
        if (availableSlots <= 0) {
            script.log(getClass(), "inventory full (cached: " + cachedInventoryCount + "), switching to bank");
            return 0;
        }

        // verify standing on exact 4-log tile before casting bloom
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) {
            script.log(getClass(), "can't read position, retrying");
            return 600;
        }

        boolean onExactTile = pos.getX() == FOUR_LOG_TILE.getX()
                && pos.getY() == FOUR_LOG_TILE.getY();

        if (!onExactTile) {
            script.log(getClass(), "not on 4-log tile (" + pos.getX() + ", " + pos.getY() + "), walking...");
            // simple walk with no expensive breakCondition, just reach the tile
            WalkConfig config = new WalkConfig.Builder()
                    .breakDistance(0)
                    .timeout(5000)
                    .build();
            script.getWalker().walkTo(FOUR_LOG_TILE, config);
            // short wait after walking to let position settle
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));
            return 600;
        }

        // ensure equipment tab is open (stay here for bloom + collection)
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 250));

        // cast bloom using equipment interact
        if (equippedBloomToolId == 0) {
            script.log(getClass(), "ERROR: bloom tool id not set");
            script.stop();
            return 0;
        }

        // disable afk/hop for entire bloom + collection cycle
        allowAFK = false;
        boolean bloomSuccess = RetryUtils.equipmentInteract(script, equippedBloomToolId, "Bloom", "casting bloom (prayer: " + prayer + ")");

        if (!bloomSuccess) {
            allowAFK = true;
            return 600;
        }

        bloomCasts++;

        // wait for bloom animation (~3 ticks = 1800ms), ignoreTasks to prevent random tab opens
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(1800, 2000), true);

        // collect fungus (stays on equipment tab - no inventory checking needed)
        return collectGroundFungus();
    }

    private int collectGroundFungus() {
        // detect which log positions have fungus
        List<WorldPosition> fungusPositions = detectFungusPositions();

        if (fungusPositions.isEmpty()) {
            allowAFK = true;
            script.log(getClass(), "no fungus detected, re-enabling afk/hop");
            return RandomUtils.weightedRandom(300, 500);
        }

        // calculate how many logs we can pick based on available slots
        // use ceiling division - if we have 1 slot left, still pick to fill it
        int availableSlots = INVENTORY_SIZE - cachedInventoryCount;
        int maxPickups = (availableSlots + FUNGUS_PER_LOG - 1) / FUNGUS_PER_LOG;  // ceiling division
        int logsToCollect = Math.min(fungusPositions.size(), maxPickups);

        script.log(getClass(), "detected " + fungusPositions.size() + " log(s), can pick " + logsToCollect + " (slots: " + availableSlots + ")");

        int collectedCount = 0;

        // tap each log position quickly - no inventory verification needed
        // each pickup gives exactly 2 fungus, so we just update cached count
        for (int i = 0; i < logsToCollect; i++) {
            WorldPosition logPos = fungusPositions.get(i);

            // check prayer via minimap orbs (no tab switch)
            Integer prayer = script.getWidgetManager().getMinimapOrbs().getPrayerPoints();
            if (prayer != null && prayer < 6) {
                script.log(getClass(), "low prayer during collection, stopping");
                break;
            }

            // get tile polygon for this log position
            Polygon tilePoly = script.getSceneProjector().getTileCube(logPos, 50);
            if (tilePoly == null) {
                script.log(getClass(), "can't get tile poly for " + logPos.getX() + "," + logPos.getY());
                continue;
            }

            // tap the tile with Pick action (retry up to 3 times)
            boolean tapped = false;
            for (int attempt = 1; attempt <= 3 && !tapped; attempt++) {
                tapped = script.getFinger().tap(tilePoly, "Pick");
                if (!tapped) {
                    script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 300), true);
                }
            }

            if (tapped) {
                // wait for pick animation (~1 tick = 600ms)
                script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(600, 800), true);

                // update cached count - get min of 2 and remaining slots (handles partial fill)
                int slotsRemaining = INVENTORY_SIZE - cachedInventoryCount;
                int fungusGained = Math.min(FUNGUS_PER_LOG, slotsRemaining);
                cachedInventoryCount += fungusGained;
                collectedCount++;
                script.log(getClass(), "picked log " + (i + 1) + "/" + logsToCollect + " (+" + fungusGained + ", inv: " + cachedInventoryCount + "/" + INVENTORY_SIZE + ")");
            } else {
                script.log(getClass(), "failed to tap log at " + logPos.getX() + "," + logPos.getY());
            }
        }

        script.log(getClass(), "picked " + collectedCount + " log(s), inventory now " + cachedInventoryCount + "/" + INVENTORY_SIZE);

        // re-enable afk/hop after collection complete
        allowAFK = true;

        // occasional human delay after collection cycle
        if (RandomUtils.uniformRandom(0, 3) == 0) {
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));
        }

        return RandomUtils.weightedRandom(50, 150);
    }
    
    private List<WorldPosition> detectFungusPositions() {
        List<WorldPosition> positions = new ArrayList<>();
        
        // create searchable pixels for fungus colors
        SingleThresholdComparator tolerance = new SingleThresholdComparator(COLOR_TOLERANCE);
        SearchablePixel[] fungusColors = {
            new SearchablePixel(FUNGUS_COLOR_1, tolerance, ColorModel.RGB),
            new SearchablePixel(FUNGUS_COLOR_2, tolerance, ColorModel.RGB),
            new SearchablePixel(FUNGUS_COLOR_3, tolerance, ColorModel.RGB),
            new SearchablePixel(FUNGUS_COLOR_4, tolerance, ColorModel.RGB)
        };
        
        // check each log position for fungus pixels
        for (WorldPosition logPos : LOG_POSITIONS) {
            Polygon tilePoly = script.getSceneProjector().getTileCube(logPos, 80);
            if (tilePoly == null) {
                continue;
            }
            
            // search for fungus pixels within this tile's area
            Point fungusPixel = script.getPixelAnalyzer().findPixel(tilePoly, fungusColors);
            if (fungusPixel != null) {
                positions.add(logPos);
                script.log(getClass(), "fungus detected at " + logPos.getX() + "," + logPos.getY());
            }
        }
        
        return positions;
    }

    private List<PixelCluster> findFungusClusters() {
        // create searchable pixels for each fungus color
        SingleThresholdComparator tolerance = new SingleThresholdComparator(COLOR_TOLERANCE);

        SearchablePixel[] fungusColors = {
            new SearchablePixel(FUNGUS_COLOR_1, tolerance, ColorModel.RGB),
            new SearchablePixel(FUNGUS_COLOR_2, tolerance, ColorModel.RGB),
            new SearchablePixel(FUNGUS_COLOR_3, tolerance, ColorModel.RGB),
            new SearchablePixel(FUNGUS_COLOR_4, tolerance, ColorModel.RGB)
        };

        // create cluster query (int maxDistance, int minSize, SearchablePixel[] pixels)
        PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
            CLUSTER_MAX_DISTANCE,
            CLUSTER_MIN_SIZE,
            fungusColors
        );

        // find clusters on game screen
        PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(null, query);

        if (result == null) {
            return new ArrayList<>();
        }

        List<PixelCluster> clusters = result.getClusters();
        if (clusters == null) {
            return new ArrayList<>();
        }

        // filter clusters by minimum size to avoid noise
        List<PixelCluster> validClusters = new ArrayList<>();
        for (PixelCluster cluster : clusters) {
            if (cluster.getPoints().size() >= CLUSTER_MIN_SIZE) {
                Rectangle bounds = cluster.getBounds();
                // filter out very small or very large clusters
                if (bounds.getWidth() >= 5 && bounds.getHeight() >= 5 &&
                    bounds.getWidth() <= 100 && bounds.getHeight() <= 100) {
                    validClusters.add(cluster);
                }
            }
        }

        script.log(getClass(), "found " + validClusters.size() + " valid fungus clusters");
        return validClusters;
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
        if (bankObject != null && myPos != null && bankObject.getWorldPosition().distanceTo(myPos) <= 5) {
            return openBank(bankObject);
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

        // bank chests use "Use", bank booths use "Bank"
        String objectName = bankObject.getName();
        String action = (objectName != null && objectName.toLowerCase().contains("chest")) ? "Use" : "Bank";
        boolean success = RetryUtils.objectInteract(script,bankObject, action, "bank (" + objectName + ")");
        if (!success) {
            return 600;
        }

        // wait for bank to open
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

        // wait for bank to load
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));

        // count fungus in inventory before depositing
        ItemGroupResult invBeforeDeposit = script.getWidgetManager().getInventory().search(Set.of(MORT_MYRE_FUNGUS));
        int fungusToBank = 0;
        if (invBeforeDeposit != null && invBeforeDeposit.contains(MORT_MYRE_FUNGUS)) {
            fungusToBank = invBeforeDeposit.getAmount(MORT_MYRE_FUNGUS);
        }

        // deposit all except ardougne cloak
        Set<Integer> keepItems = toIntegerSet(ARDOUGNE_CLOAKS);
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

        // priority 1: crafting cape
        try {
            for (int capeId : CRAFTING_CAPES) {
                UIResult<ItemSearchResult> cape = script.getWidgetManager().getEquipment().findItem(capeId);
                if (cape != null && cape.isFound()) {
                    return useCraftingCapeTeleport();
                }
            }
        } catch (RuntimeException e) {
            script.log(getClass(), "error checking crafting cape: " + e.getMessage());
        }

        // priority 2: ver sinhaza bank (drakan's medallion - always available)
        return useVerSinhazaBanking();
    }

    private int useCraftingCapeTeleport() {
        script.log(getClass(), "using crafting cape teleport");

        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        // find which cape we have
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

        boolean success = RetryUtils.equipmentInteract(script,capeId, "Teleport", "crafting cape teleport");
        if (!success) {
            return 600;
        }

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));

        // tap bank chest directly - game will path us there
        script.log(getClass(), "tapping bank chest");
        Polygon chestPoly = script.getSceneProjector().getTileCube(CRAFTING_GUILD_BANK_CHEST, 60);
        if (chestPoly == null) {
            script.log(getClass(), "can't get bank chest polygon, trying object manager");
            RSObject bankChest = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Bank chest");
            if (bankChest != null) {
                chestPoly = bankChest.getConvexHull();
            }
        }

        if (chestPoly == null) {
            script.log(getClass(), "bank chest not visible");
            return 600;
        }

        boolean tapped = RetryUtils.tap(script,chestPoly, "Use", "bank chest");
        if (!tapped) {
            return 600;
        }

        // wait for bank to open
        boolean opened = script.pollFramesUntil(() ->
                        script.getWidgetManager().getBank().isVisible(),
                10000
        );

        if (!opened) {
            script.log(getClass(), "bank didn't open after tapping chest");
            return 600;
        }

        return handleBankInterface();
    }

    private int useVerSinhazaBanking() {
        script.log(getClass(), "using ver sinhaza bank (drakan's medallion)");

        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        // teleport to ver sinhaza
        boolean success = RetryUtils.equipmentInteract(script, DRAKANS_MEDALLION, "Ver Sinhaza", "drakan's medallion teleport");
        if (!success) {
            return 600;
        }

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));

        // walk to bank area
        script.log(getClass(), "walking to ver sinhaza bank");
        WalkConfig config = new WalkConfig.Builder()
                .breakCondition(() -> {
                    WorldPosition myPos = script.getWorldPosition();
                    if (myPos == null) return false;
                    RSObject bank = script.getObjectManager().getClosestObject(myPos, "Bank booth");
                    return bank != null && bank.getWorldPosition().distanceTo(myPos) <= 5;
                })
                .breakDistance(3)
                .build();

        script.getWalker().walkTo(VER_SINHAZA_BANK_TILE, config);

        // wait for arrival
        script.pollFramesUntil(() -> {
            WorldPosition myPos = script.getWorldPosition();
            if (myPos == null) return false;
            RSObject bank = script.getObjectManager().getClosestObject(myPos, "Bank booth");
            return bank != null && bank.getWorldPosition().distanceTo(myPos) <= 5;
        }, 15000);

        // find and open bank
        RSObject bankBooth = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Bank booth");
        if (bankBooth != null) {
            return openBank(bankBooth);
        }

        script.log(getClass(), "ver sinhaza bank booth not found");
        return 600;
    }

    @Override
    public int restorePrayer() {
        // check if already at an altar
        WorldPosition myPos = script.getWorldPosition();
        RSObject altar = myPos != null ? script.getObjectManager().getClosestObject(myPos, "Altar") : null;
        if (altar != null && myPos != null && altar.getWorldPosition().distanceTo(myPos) <= 5) {
            return prayAtAltar(altar);
        }

        // check if already at monastery area - skip teleport and just walk
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
                        boolean success = RetryUtils.inventoryInteract(script,cloak, "Monastery Teleport", "ardougne cloak (inventory)");
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
                boolean success = RetryUtils.equipmentInteract(script,cloakId, "Kandarin Monastery", "ardougne cloak (equipped)");
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

        // wait for position to settle after walking
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(400, 600));

        // try to find and pray at altar after walking
        altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
        if (altar != null) {
            script.log(getClass(), "found altar after walking, praying");
            return prayAtAltar(altar);
        }

        // altar still not found - may need to look around
        script.log(getClass(), "altar not visible, searching nearby");
        return 600;
    }

    private int useLumbridgeTeleport() {
        script.log(getClass(), "using lumbridge teleport");

        try {
            boolean success = script.getWidgetManager().getSpellbook().selectSpell(
                    StandardSpellbook.LUMBRIDGE_TELEPORT,
                    null
            );

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

        // wait for position to settle after walking
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(400, 600));

        // try to find and pray at altar after walking
        altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
        if (altar != null) {
            script.log(getClass(), "found altar after walking, praying");
            return prayAtAltar(altar);
        }

        script.log(getClass(), "lumbridge altar not visible, searching nearby");
        return 600;
    }

    private int prayAtAltar(RSObject altar) {
        boolean success = RetryUtils.objectInteract(script,altar, "Pray-at", "altar");
        if (!success) {
            return 600;
        }

        // wait for prayer to restore
        boolean restored = script.pollFramesUntil(() -> {
            Integer prayer = script.getWidgetManager().getMinimapOrbs().getPrayerPoints();
            return prayer != null && prayer >= 30;
        }, 3000);

        if (restored) {
            script.log(getClass(), "prayer restored");
        } else {
            script.log(getClass(), "prayer restoration timeout");
        }

        return 0;
    }

    @Override
    public int returnToArea() {
        // check if already at log area
        WorldPosition pos = script.getWorldPosition();
        if (pos != null && LOG_AREA.contains(pos)) {
            script.log(getClass(), "already at log area");
            return 0;
        }

        // check current region
        int currentRegion = getCurrentRegion();
        script.log(getClass(), "current region: " + currentRegion);

        // if not in mort myre region, teleport with drakan's medallion
        boolean inMortMyre = currentRegion == REGION_MORT_MYRE_1 || currentRegion == REGION_MORT_MYRE_2;
        if (!inMortMyre) {
            return useDrakansMedallionTeleport();
        }

        // walk to 4 log tile
        return walkToLogTile();
    }

    private int getCurrentRegion() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return 0;
        return (pos.getX() >> 6) | ((pos.getY() >> 6) << 8);
    }

    private int useDrakansMedallionTeleport() {
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        boolean success = RetryUtils.equipmentInteract(script,DRAKANS_MEDALLION, "Ver Sinhaza", "drakan's medallion teleport");
        if (!success) {
            script.log(getClass(), "ERROR: failed to use drakan's medallion");
            script.stop();
            return 0;
        }

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(2000, 3000));
        return walkToLogTile();
    }

    private int walkToLogTile() {
        // check if already there
        WorldPosition pos = script.getWorldPosition();
        if (pos != null && LOG_AREA.contains(pos)) {
            script.log(getClass(), "arrived at log area");
            return 0;
        }

        script.log(getClass(), "walking to 4 log tile via waypoint path");

        // smooth walking config: minimap-only, low randomization for predictable pathing
        WalkConfig config = new WalkConfig.Builder()
                .setWalkMethods(false, true)  // minimap only for long distance
                .tileRandomisationRadius(0)   // deterministic targeting
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

    // helper to convert int array to Set<Integer>
    private Set<Integer> toIntegerSet(int[] arr) {
        Set<Integer> set = new HashSet<>();
        for (int i : arr) {
            set.add(i);
        }
        return set;
    }
}
