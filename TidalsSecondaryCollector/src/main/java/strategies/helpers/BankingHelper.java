package strategies.helpers;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;
import com.osmb.api.walker.WalkConfig;
import com.osmb.api.location.area.impl.RectangleArea;
import utilities.RetryUtils;

import java.util.*;

import static main.TidalsSecondaryCollector.*;

// handles all banking: teleports, bank interface, deposits, zanaris banking, crafting cape
public class BankingHelper {

    private final Script script;

    // item ids
    private static final int CRAFTING_CAPE = 9780;
    private static final int CRAFTING_CAPE_T = 9781;
    private static final int[] CRAFTING_CAPES = {CRAFTING_CAPE, CRAFTING_CAPE_T};
    private static final int DRAKANS_MEDALLION = 22400;
    private static final int MORT_MYRE_FUNGUS = 2970;

    // locations
    private static final WorldPosition VER_SINHAZA_BANK_TILE = new WorldPosition(3651, 3211, 0);
    private static final WorldPosition ZANARIS_BANK_TILE = new WorldPosition(2384, 4459, 0);
    private static final RectangleArea ZANARIS_AREA = new RectangleArea(2375, 4419, 64, 48, 0);
    private static final RectangleArea ZANARIS_BANK_AREA = new RectangleArea(2381, 4454, 7, 7, 0);
    private static final RectangleArea ZANARIS_FAIRY_RING_AREA = new RectangleArea(2408, 4431, 8, 6, 0);
    private static final RectangleArea MORT_MYRE_FAIRY_RING_AREA = new RectangleArea(3466, 3428, 6, 6, 0);
    private static final WorldPosition MORT_MYRE_FAIRY_RING = new WorldPosition(3469, 3431, 0);
    private static final WorldPosition MONASTERY_FAIRY_RING = new WorldPosition(2658, 3230, 0);
    private static final RectangleArea MONASTERY_AREA = new RectangleArea(2601, 3207, 10, 14, 0);
    private static final RectangleArea MONASTERY_FAIRY_AREA = new RectangleArea(2653, 3226, 10, 9, 0);

    // callback to get fairy ring mode and bloom tool ids from parent
    private final boolean isFairyRingMode;
    private final int[] bloomTools;
    private final int[] ardougneCloaks;
    private final java.util.function.IntSupplier getCachedInventoryCount;
    private final java.util.function.IntConsumer setCachedInventoryCount;

    public BankingHelper(Script script, boolean isFairyRingMode, int[] bloomTools, int[] ardougneCloaks,
                         java.util.function.IntSupplier getCachedInventoryCount,
                         java.util.function.IntConsumer setCachedInventoryCount) {
        this.script = script;
        this.isFairyRingMode = isFairyRingMode;
        this.bloomTools = bloomTools;
        this.ardougneCloaks = ardougneCloaks;
        this.getCachedInventoryCount = getCachedInventoryCount;
        this.setCachedInventoryCount = setCachedInventoryCount;
    }

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
            if (dist <= 10) {
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
                            return bank != null && bank.getWorldPosition().distanceTo(p) <= 10;
                        })
                        .breakDistance(3)
                        .build();
                script.getWalker().walkTo(bankObject.getWorldPosition(), config);
                return 0;
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

    public int openBank(RSObject bankObject) {
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
        Set<Integer> keepItems = new HashSet<>(toIntegerSet(ardougneCloaks));

        // fairy ring mode: also keep bloom tool (it's in inventory, not equipped)
        if (isFairyRingMode) {
            keepItems.addAll(toIntegerSet(bloomTools));
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
            int count = 28 - inv.getFreeSlots();
            setCachedInventoryCount.accept(count);
            script.log(getClass(), "synced cached inventory count: " + count);
        } else {
            setCachedInventoryCount.accept(0);
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
        if (isFairyRingMode) {
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

        // wait for teleport to crafting guild and verify arrival
        WorldPosition craftingGuild = new WorldPosition(2931, 3286, 0);
        boolean arrived = script.pollFramesUntil(() -> {
            WorldPosition p = script.getWorldPosition();
            return p != null && p.distanceTo(craftingGuild) <= 30;
        }, 8000);

        if (!arrived) {
            script.log(getClass(), "crafting cape teleport didn't resolve in time");
            return 600;
        }

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));
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

        // wait for teleport to ver sinhaza and verify arrival
        boolean arrived = script.pollFramesUntil(() -> {
            WorldPosition p = script.getWorldPosition();
            return p != null && p.distanceTo(VER_SINHAZA_BANK_TILE) <= 30;
        }, 8000);

        if (!arrived) {
            script.log(getClass(), "drakan's medallion teleport didn't resolve in time");
            return 600;
        }

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));
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
        boolean nearThreeLogSpot = MORT_MYRE_FAIRY_RING_AREA.contains(pos)
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
            return 0;
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
        return 0;
    }

    // shared teleport used by both banking and prayer
    public boolean tryArdougneCloakTeleport() {
        // check inventory first
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(toIntegerSet(ardougneCloaks));
        if (inv != null) {
            for (int cloakId : ardougneCloaks) {
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

        for (int cloakId : ardougneCloaks) {
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

    // --- helpers ---

    private Set<Integer> toIntegerSet(int[] arr) {
        Set<Integer> set = new HashSet<>();
        for (int i : arr) set.add(i);
        return set;
    }

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
