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
import com.osmb.api.ui.spellbook.StandardSpellbook;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.utils.UIResult;
import com.osmb.api.visual.PixelCluster;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import com.osmb.api.walker.WalkConfig;
import main.TidalsSecondaryCollector.State;
import utilities.RetryUtils;

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

    private static final int[] DUELING_RINGS = {
            2566, 2564, 2562, 2560, 2558, 2556, 2554, 2552
    };

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
    
    // the 4 log positions around the standing tile
    private static final WorldPosition[] LOG_POSITIONS = {
        new WorldPosition(3666, 3256, 0),
        new WorldPosition(3666, 3254, 0),
        new WorldPosition(3668, 3254, 0),
        new WorldPosition(3668, 3255, 0)
    };
    private static final WorldPosition CRAFTING_GUILD_BANK_CHEST = new WorldPosition(2936, 3280, 0);
    private static final WorldPosition KANDARIN_ALTAR = new WorldPosition(2605, 3211, 0);
    private static final WorldPosition LUMBRIDGE_ALTAR = new WorldPosition(3241, 3208, 0);

    // region ids for mort myre / ver sinhaza area (4-log tile spans 14642/14643)
    private static final int REGION_MORT_MYRE_1 = 14642;
    private static final int REGION_MORT_MYRE_2 = 14643;

    // track which bloom tool we found during setup
    private int equippedBloomToolId = 0;
    
    // track detected prayer method: "ardy_inventory", "ardy_equipped", or "lumbridge"
    private String detectedPrayerMethod = "lumbridge";

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

        // priority 2: check if inventory full
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
        if (inv != null && inv.isFull()) {
            script.log(getClass(), "inventory full, need to bank");
            return State.BANKING;
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
        script.pollFramesHuman(() -> false, script.random(300, 500));

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

        // 3. check banking method (at least one required)
        boolean hasBankingMethod = false;
        String bankingMethodUsed = "";

        UIResult<ItemSearchResult> craftingCape = script.getWidgetManager().getEquipment().findItem(CRAFTING_CAPES);
        if (craftingCape.isFound()) {
            hasBankingMethod = true;
            bankingMethodUsed = "crafting cape";
        }

        if (!hasBankingMethod) {
            UIResult<ItemSearchResult> duelingRing = script.getWidgetManager().getEquipment().findItem(DUELING_RINGS);
            if (duelingRing.isFound()) {
                hasBankingMethod = true;
                bankingMethodUsed = "ring of dueling";
            }
        }

        if (!hasBankingMethod && hasVarrockMediumDiary) {
            hasBankingMethod = true;
            bankingMethodUsed = "varrock teleport (ge)";
        }

        if (!hasBankingMethod) {
            script.log(getClass(), "ERROR: no banking method available");
            script.log(getClass(), "need: crafting cape, ring of dueling, or varrock medium diary");
            return false;
        }
        script.log(getClass(), "banking method: " + bankingMethodUsed);

        // 4. check prayer restoration method
        boolean hasPrayerMethod = false;
        String prayerMethodUsed = "";

        // check inventory for ardy cloak
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
        script.pollFramesHuman(() -> false, script.random(200, 400));

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
            script.pollFramesHuman(() -> false, script.random(200, 400));

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

        // back to inventory
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
        script.pollFramesHuman(() -> false, script.random(200, 400));

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
                                script.pollFramesHuman(() -> false, script.random(2000, 3000));
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
            script.pollFramesHuman(() -> false, script.random(200, 400));

            for (int cloakId : ARDOUGNE_CLOAKS) {
                UIResult<ItemSearchResult> ardyCloak = script.getWidgetManager().getEquipment().findItem(cloakId);
                if (ardyCloak.isFound()) {
                    boolean success = RetryUtils.equipmentInteract(script,cloakId, "Monastery Teleport", "pre-trip ardy cloak (equipped)");
                    if (success) {
                        script.pollFramesHuman(() -> false, script.random(2000, 3000));
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
                    script.pollFramesHuman(() -> false, script.random(2000, 3000));
                    walkToLumbridgeAltarAndPray();
                }
            } catch (Exception e) {
                script.log(getClass(), "failed to cast lumbridge teleport: " + e.getMessage());
            }
        }
    }

    private void walkToAltarAndPray() {
        // walk to kandarin altar and pray
        WalkConfig config = new WalkConfig.Builder()
                .breakCondition(() -> {
                    RSObject altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
                    return altar != null && altar.getWorldPosition().distanceTo(script.getWorldPosition()) <= 3;
                })
                .breakDistance(3)
                .build();
        script.getWalker().walkTo(KANDARIN_ALTAR, config);
        script.pollFramesHuman(() -> false, script.random(500, 800));

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
                    RSObject altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
                    return altar != null && altar.getWorldPosition().distanceTo(script.getWorldPosition()) <= 3;
                })
                .breakDistance(3)
                .build();
        script.getWalker().walkTo(LUMBRIDGE_ALTAR, config);
        script.pollFramesHuman(() -> false, script.random(500, 800));

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
        // check prayer before every bloom
        Integer prayer = script.getWidgetManager().getMinimapOrbs().getPrayerPoints();
        if (prayer == null) {
            script.log(getClass(), "can't read prayer, retrying");
            return 600;
        }

        if (prayer < 6) {
            script.log(getClass(), "prayer too low (" + prayer + "), switching to restore");
            return 0;
        }

        // check inventory
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
        if (inv != null && inv.isFull()) {
            script.log(getClass(), "inventory full, switching to bank");
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
            WalkConfig config = new WalkConfig.Builder()
                    .breakCondition(() -> {
                        WorldPosition p = script.getWorldPosition();
                        return p != null 
                            && p.getX() == FOUR_LOG_TILE.getX() 
                            && p.getY() == FOUR_LOG_TILE.getY();
                    })
                    .breakDistance(0)
                    .build();
            script.getWalker().walkTo(FOUR_LOG_TILE, config);
            return 600;
        }

        // open equipment tab
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesUntil(() -> false, script.random(150, 250));

        // cast bloom using equipment interact
        if (equippedBloomToolId == 0) {
            script.log(getClass(), "ERROR: bloom tool id not set");
            script.stop();
            return 0;
        }

        // disable afk/hop for entire bloom + collection cycle
        allowAFK = false;
        boolean bloomSuccess = RetryUtils.equipmentInteract(script,equippedBloomToolId, "Bloom", "casting bloom (prayer: " + prayer + ")");

        if (!bloomSuccess) {
            allowAFK = true;
            return 600;
        }

        bloomCasts++;

        // wait for bloom animation (~3 ticks = 1800ms), ignoreTasks to prevent random tab opens
        script.pollFramesUntil(() -> false, script.random(1800, 2000), true);

        // switch to inventory for pickup
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
        script.pollFramesUntil(() -> false, script.random(100, 200), true);

        // collect fungus
        return collectGroundFungus();
    }

    private int collectGroundFungus() {
        // step 1: detect which log positions have fungus and cache them
        List<WorldPosition> fungusPositions = detectFungusPositions();
        
        if (fungusPositions.isEmpty()) {
            allowAFK = true;
            script.log(getClass(), "no fungus detected, re-enabling afk/hop");
            return script.random(300, 500);
        }
        
        script.log(getClass(), "detected fungus on " + fungusPositions.size() + " log(s), ignoring breaks/hops/afk until done");
        
        int collectedCount = 0;
        
        // step 2: tap each cached position quickly using animation detection
        // ignoreTasks = true to prevent breaks/hops/afk during collection
        for (WorldPosition logPos : fungusPositions) {
            // check inventory before each pickup
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
            if (inv != null && inv.isFull()) {
                script.log(getClass(), "inventory full during collection");
                break;
            }
            
            // check prayer
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
            
            // cache fungus count before attempting to pick
            ItemGroupResult invBefore = script.getWidgetManager().getInventory().search(Set.of(MORT_MYRE_FUNGUS));
            int countBefore = (invBefore != null) ? invBefore.getAmount(MORT_MYRE_FUNGUS) : 0;

            // attempt to pick with retries
            int maxAttempts = 3;
            boolean picked = false;

            for (int attempt = 1; attempt <= maxAttempts && !picked; attempt++) {
                script.log(getClass(), "attempt " + attempt + " - picking fungus at " + logPos.getX() + "," + logPos.getY());

                // tap the tile with Pick action
                boolean tapped = script.getFinger().tap(tilePoly, "Pick");

                if (!tapped) {
                    script.log(getClass(), "tap failed, retrying...");
                    script.pollFramesUntil(() -> false, script.random(300, 500), true);
                    continue;
                }

                // wait for item to appear in inventory (pick animation ~1 tick)
                script.pollFramesUntil(() -> false, script.random(800, 1000), true);

                // check if fungus count increased
                ItemGroupResult invAfter = script.getWidgetManager().getInventory().search(Set.of(MORT_MYRE_FUNGUS));
                int countAfter = (invAfter != null) ? invAfter.getAmount(MORT_MYRE_FUNGUS) : 0;

                if (countAfter > countBefore) {
                    picked = true;
                    collectedCount++;
                    itemsCollected++;
                    script.log(getClass(), "picked fungus (count: " + countAfter + ")");
                } else {
                    script.log(getClass(), "count unchanged (" + countBefore + " -> " + countAfter + "), retrying...");
                }
            }

            if (!picked) {
                script.log(getClass(), "failed to pick fungus after " + maxAttempts + " attempts, moving on");
            }
        }
        
        if (collectedCount == 0) {
            script.log(getClass(), "no fungus picked up");
        } else {
            script.log(getClass(), "collected " + collectedCount + " fungus");
        }
        
        // re-enable afk/hop after collection complete
        allowAFK = true;
        script.log(getClass(), "collection cycle complete, re-enabling afk/hop");

        // occasional human delay after collection cycle for anti-ban
        if (script.random(0, 3) == 0) {
            script.pollFramesHuman(() -> false, script.random(200, 400));
        }

        return script.random(50, 150);
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
        if (bankObject != null && bankObject.getWorldPosition().distanceTo(script.getWorldPosition()) <= 5) {
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
        script.pollFramesHuman(() -> false, script.random(300, 500));

        // deposit all except ardougne cloak
        Set<Integer> keepItems = toIntegerSet(ARDOUGNE_CLOAKS);
        script.getWidgetManager().getBank().depositAll(keepItems);

        script.pollFramesHuman(() -> false, script.random(300, 600));

        // verify deposit
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(MORT_MYRE_FUNGUS));
        if (inv != null && inv.contains(MORT_MYRE_FUNGUS)) {
            script.log(getClass(), "warning: fungus still in inventory after deposit");
        }

        // close bank
        script.getWidgetManager().getBank().close();
        script.pollFramesHuman(() -> false, script.random(200, 400));

        bankTrips++;
        script.log(getClass(), "banking complete, bank trips: " + bankTrips);

        return 0;
    }

    private int teleportToBank() {
        script.log(getClass(), "teleporting to bank");

        // priority 1: crafting cape
        UIResult<ItemSearchResult> craftingCape = script.getWidgetManager().getEquipment().findItem(CRAFTING_CAPES);
        if (craftingCape.isFound()) {
            return useCraftingCapeTeleport();
        }

        // priority 2: ring of dueling
        UIResult<ItemSearchResult> duelingRing = script.getWidgetManager().getEquipment().findItem(DUELING_RINGS);
        if (duelingRing.isFound()) {
            return useDuelingRingTeleport();
        }

        // priority 3: varrock teleport to ge
        if (hasVarrockMediumDiary) {
            return useVarrockTeleport();
        }

        script.log(getClass(), "ERROR: no banking teleport available");
        script.stop();
        return 0;
    }

    private int useCraftingCapeTeleport() {
        script.log(getClass(), "using crafting cape teleport");

        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> false, script.random(200, 400));

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

        script.pollFramesHuman(() -> false, script.random(2000, 3000));

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

    private int useDuelingRingTeleport() {
        script.log(getClass(), "using ring of dueling teleport");

        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> false, script.random(200, 400));

        // find which ring we have
        int ringId = 0;
        for (int id : DUELING_RINGS) {
            if (script.getWidgetManager().getEquipment().findItem(id).isFound()) {
                ringId = id;
                break;
            }
        }

        if (ringId == 0) {
            script.log(getClass(), "ring of dueling not found");
            return 600;
        }

        boolean success = RetryUtils.equipmentInteract(script,ringId, "Castle Wars", "ring of dueling teleport");
        if (!success) {
            return 600;
        }

        script.pollFramesHuman(() -> false, script.random(2000, 3000));

        // bank should be nearby at castle wars
        RSObject bankChest = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Bank chest");
        if (bankChest != null) {
            return openBank(bankChest);
        }

        script.log(getClass(), "looking for ferox bank chest");
        return 600;
    }

    private int useVarrockTeleport() {
        script.log(getClass(), "using varrock teleport to ge");

        try {
            boolean success = script.getWidgetManager().getSpellbook().selectSpell(
                    StandardSpellbook.VARROCK_TELEPORT,
                    null
            );

            if (!success) {
                script.log(getClass(), "failed to cast varrock teleport");
                return 600;
            }

            // wait for menu
            boolean menuAppeared = script.pollFramesUntil(() -> {
                DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
                return type == DialogueType.TEXT_OPTION;
            }, script.random(3000, 5000));

            if (!menuAppeared) {
                script.log(getClass(), "varrock teleport menu didn't appear");
                return 600;
            }

            // select grand exchange
            boolean selected = script.getWidgetManager().getDialogue().selectOption("Grand Exchange");
            if (!selected) {
                script.log(getClass(), "failed to select grand exchange option");
                return 600;
            }

            script.pollFramesHuman(() -> false, script.random(2000, 3000));
            return walkToGEBank();

        } catch (Exception e) {
            script.log(getClass(), "error casting varrock teleport: " + e.getMessage());
            return 600;
        }
    }

    private int walkToGEBank() {
        RSObject bankBooth = script.getObjectManager().getClosestObject(
                script.getWorldPosition(),
                "Bank booth", "Grand Exchange booth"
        );

        if (bankBooth != null && bankBooth.getWorldPosition().distanceTo(script.getWorldPosition()) <= 5) {
            return openBank(bankBooth);
        }

        script.log(getClass(), "walking to ge bank");
        if (bankBooth != null) {
            WalkConfig config = new WalkConfig.Builder()
                    .breakDistance(3)
                    .build();
            script.getWalker().walkTo(bankBooth.getWorldPosition(), config);
        }
        return 600;
    }

    @Override
    public int restorePrayer() {
        // check if already at an altar
        RSObject altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
        if (altar != null && altar.getWorldPosition().distanceTo(script.getWorldPosition()) <= 5) {
            return prayAtAltar(altar);
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
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
        script.pollFramesHuman(() -> false, script.random(200, 400));

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(toIntegerSet(ARDOUGNE_CLOAKS));
        if (inv != null) {
            for (int cloakId : ARDOUGNE_CLOAKS) {
                if (inv.contains(cloakId)) {
                    ItemSearchResult cloak = inv.getItem(cloakId);
                    if (cloak != null) {
                        boolean success = RetryUtils.inventoryInteract(script,cloak, "Monastery Teleport", "ardougne cloak (inventory)");
                        if (success) {
                            script.pollFramesHuman(() -> false, script.random(2000, 3000));
                            return true;
                        }
                    }
                }
            }
        }

        // check equipped
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> false, script.random(200, 400));

        for (int cloakId : ARDOUGNE_CLOAKS) {
            UIResult<ItemSearchResult> ardyCloak = script.getWidgetManager().getEquipment().findItem(cloakId);
            if (ardyCloak.isFound()) {
                boolean success = RetryUtils.equipmentInteract(script,cloakId, "Monastery Teleport", "ardougne cloak (equipped)");
                if (success) {
                    script.pollFramesHuman(() -> false, script.random(2000, 3000));
                    return true;
                }
            }
        }

        script.log(getClass(), "no ardougne cloak found");
        return false;
    }

    private int walkToKandarinAltar() {
        RSObject altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");

        if (altar != null && altar.getWorldPosition().distanceTo(script.getWorldPosition()) <= 5) {
            return prayAtAltar(altar);
        }

        script.log(getClass(), "walking to kandarin altar");
        WalkConfig config = new WalkConfig.Builder()
                .breakCondition(() -> {
                    RSObject a = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
                    return a != null && a.getWorldPosition().distanceTo(script.getWorldPosition()) <= 3;
                })
                .breakDistance(3)
                .build();

        script.getWalker().walkTo(KANDARIN_ALTAR, config);
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

            script.pollFramesHuman(() -> false, script.random(2000, 3000));
            return walkToLumbridgeChurch();

        } catch (Exception e) {
            script.log(getClass(), "ERROR: lumbridge teleport not available - stopping");
            script.stop();
            return 0;
        }
    }

    private int walkToLumbridgeChurch() {
        RSObject altar = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");

        if (altar != null && altar.getWorldPosition().distanceTo(script.getWorldPosition()) <= 5) {
            return prayAtAltar(altar);
        }

        script.log(getClass(), "walking to lumbridge church");
        WalkConfig config = new WalkConfig.Builder()
                .breakCondition(() -> {
                    RSObject a = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Altar");
                    return a != null && a.getWorldPosition().distanceTo(script.getWorldPosition()) <= 3;
                })
                .breakDistance(3)
                .build();

        script.getWalker().walkTo(LUMBRIDGE_ALTAR, config);
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
        script.pollFramesHuman(() -> false, script.random(200, 400));

        boolean success = RetryUtils.equipmentInteract(script,DRAKANS_MEDALLION, "Ver Sinhaza", "drakan's medallion teleport");
        if (!success) {
            script.log(getClass(), "ERROR: failed to use drakan's medallion");
            script.stop();
            return 0;
        }

        script.pollFramesHuman(() -> false, script.random(2000, 3000));
        return walkToLogTile();
    }

    private int walkToLogTile() {
        // check if already there
        WorldPosition pos = script.getWorldPosition();
        if (pos != null && LOG_AREA.contains(pos)) {
            script.log(getClass(), "arrived at log area");
            return 0;
        }

        script.log(getClass(), "walking to 4 log tile");

        WalkConfig config = new WalkConfig.Builder()
                .breakCondition(() -> LOG_AREA.contains(script.getWorldPosition()))
                .breakDistance(0)
                .build();

        script.getWalker().walkTo(FOUR_LOG_TILE, config);

        // wait until we arrive or timeout
        boolean arrived = script.pollFramesUntil(() -> {
            WorldPosition p = script.getWorldPosition();
            return p != null && LOG_AREA.contains(p);
        }, 15000);

        if (arrived) {
            script.log(getClass(), "arrived at log area");
            return 0;
        }

        script.log(getClass(), "still walking to log area");
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
