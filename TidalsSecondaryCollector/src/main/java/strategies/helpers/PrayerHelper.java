package strategies.helpers;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.ui.spellbook.SpellNotFoundException;
import com.osmb.api.ui.spellbook.StandardSpellbook;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;
import com.osmb.api.walker.WalkConfig;
import utilities.RetryUtils;

import java.util.*;

// handles prayer restoration: altar teleports, walking, praying
public class PrayerHelper {

    private final Script script;
    private final String detectedPrayerMethod;
    private final int[] ardougneCloaks;
    private final BankingHelper bankingHelper; // for shared tryArdougneCloakTeleport

    // locations
    private static final WorldPosition KANDARIN_ALTAR = new WorldPosition(2605, 3211, 0);
    private static final WorldPosition LUMBRIDGE_ALTAR = new WorldPosition(3241, 3208, 0);
    private static final RectangleArea MONASTERY_AREA = new RectangleArea(2601, 3207, 10, 14, 0);

    public PrayerHelper(Script script, String detectedPrayerMethod, int[] ardougneCloaks, BankingHelper bankingHelper) {
        this.script = script;
        this.detectedPrayerMethod = detectedPrayerMethod;
        this.ardougneCloaks = ardougneCloaks;
        this.bankingHelper = bankingHelper;
    }

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
        if (bankingHelper.tryArdougneCloakTeleport()) {
            return walkToKandarinAltar();
        }

        // priority 2: lumbridge teleport
        return useLumbridgeTeleport();
    }

    // used during setup for pre-trip prayer restore
    public void restorePrayerBeforeTrip() {
        script.log(getClass(), "restoring prayer using: " + detectedPrayerMethod);

        if (detectedPrayerMethod.equals("ardy_inventory")) {
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(toIntegerSet(ardougneCloaks));
            if (inv != null) {
                for (int cloakId : ardougneCloaks) {
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

            for (int cloakId : ardougneCloaks) {
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

    // --- helpers ---

    private Set<Integer> toIntegerSet(int[] arr) {
        Set<Integer> set = new HashSet<>();
        for (int i : arr) set.add(i);
        return set;
    }
}
