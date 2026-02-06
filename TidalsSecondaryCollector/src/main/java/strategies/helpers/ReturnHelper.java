package strategies.helpers;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;

import com.osmb.api.utils.RandomUtils;
import com.osmb.api.walker.WalkConfig;
import utilities.RetryUtils;
import utilities.webwalker.TidalsWalker;
import utilities.webwalker.WalkResult;

import java.util.*;

// handles return to collection area: medallion teleport, fairy ring navigation, waypoint walking
public class ReturnHelper {

    private final Script script;
    private final boolean isFairyRingMode;
    private final BankingHelper bankingHelper; // for shared tryArdougneCloakTeleport
    private final TidalsWalker walker;
    private boolean verbose = false;

    // short-distance threshold: skip TidalsWalker (full API routing) for walks under this distance
    private static final int SHORT_WALK_THRESHOLD = 8;

    // ver sinhaza mode locations
    private static final WorldPosition FOUR_LOG_TILE = new WorldPosition(3667, 3255, 0);
    private static final RectangleArea LOG_AREA = new RectangleArea(3665, 3253, 3669, 3257, 0);

    // fairy ring mode locations
    private static final WorldPosition THREE_LOG_TILE = new WorldPosition(3474, 3419, 0);
    private static final RectangleArea THREE_LOG_AREA = new RectangleArea(3472, 3417, 3476, 3421, 0);
    private static final WorldPosition MORT_MYRE_FAIRY_RING = new WorldPosition(3469, 3431, 0);
    private static final RectangleArea MORT_MYRE_FAIRY_RING_AREA = new RectangleArea(3466, 3428, 6, 6, 0);
    private static final RectangleArea ZANARIS_AREA = new RectangleArea(2375, 4419, 64, 48, 0);
    private static final RectangleArea ZANARIS_FAIRY_RING_AREA = new RectangleArea(2408, 4431, 8, 6, 0);
    private static final RectangleArea MONASTERY_AREA = new RectangleArea(2601, 3207, 10, 14, 0);
    private static final RectangleArea MONASTERY_FAIRY_AREA = new RectangleArea(2653, 3226, 10, 9, 0);

    public ReturnHelper(Script script, boolean isFairyRingMode, BankingHelper bankingHelper) {
        this.script = script;
        this.isFairyRingMode = isFairyRingMode;
        this.bankingHelper = bankingHelper;
        this.walker = new TidalsWalker(script);
    }

    /** enable/disable verbose debug logging */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        this.walker.setVerbose(verbose);
    }

    public int returnToArea() {
        WorldPosition pos = script.getWorldPosition();

        // fairy ring mode: use ardy cloak -> monastery -> fairy ring -> BKR
        if (isFairyRingMode) {
            if (pos != null && THREE_LOG_AREA.contains(pos)) {
                if (verbose) script.log(getClass(), "[debug] already at 3-log area (" + pos + ")");
                else script.log(getClass(), "already at 3-log area");
                return 0;
            }
            return useFairyRingReturn();
        }

        // ver sinhaza mode
        if (pos != null && LOG_AREA.contains(pos)) {
            if (verbose) script.log(getClass(), "[debug] already in LOG_AREA (" + pos + ")");
            else script.log(getClass(), "already at log area");
            return 0;
        }

        // short-distance bypass: if close enough, use OSMB's built-in walker
        // avoids full TidalsWalker scan + 13-path API call for a 1-3 tile walk
        WorldPosition targetTile = isFairyRingMode ? THREE_LOG_TILE : FOUR_LOG_TILE;
        double dist = (pos != null) ? pos.distanceTo(targetTile) : Double.MAX_VALUE;

        if (dist <= SHORT_WALK_THRESHOLD) {
            script.log(getClass(), "short walk to log tile (dist=" + String.format("%.1f", dist) + "), using local walker");
            WalkConfig config = new WalkConfig.Builder()
                    .breakDistance(0)
                    .timeout(RandomUtils.weightedRandom(4000, 6000))
                    .build();
            script.getWalker().walkTo(targetTile, config);
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));

            // verify arrival
            WorldPosition afterPos = script.getWorldPosition();
            if (afterPos != null && LOG_AREA.contains(afterPos)) {
                script.log(getClass(), "arrived at log area (short walk)");
                return 0;
            }
            if (verbose) script.log(getClass(), "[debug] short walk ended at " + afterPos + ", not in LOG_AREA yet");
            return 600;
        }

        script.log(getClass(), "using TidalsWalker to reach 4-log tile (dist=" + String.format("%.1f", dist) + ")");
        WalkResult result = walker.walkTo(FOUR_LOG_TILE);

        if (result.isSuccess()) {
            // post-walk verification: ensure we're actually in the area
            WorldPosition afterPos = script.getWorldPosition();
            if (afterPos != null && LOG_AREA.contains(afterPos)) {
                script.log(getClass(), "arrived at log area");
                return 0;
            }
            // walker said success but we're slightly off — nudge to exact tile
            if (verbose) script.log(getClass(), "[debug] walker said arrived but pos=" + afterPos + ", nudging to tile");
            script.log(getClass(), "arrived near log area, nudging to exact tile");
            WalkConfig nudge = new WalkConfig.Builder().breakDistance(0).timeout(4000).build();
            script.getWalker().walkTo(FOUR_LOG_TILE, nudge);
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));
            return 0;
        }

        script.log(getClass(), "walk failed: " + result.getMessage() + ", will retry");
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
            return 0;
        }

        // not at monastery or zanaris - teleport to monastery
        script.log(getClass(), "teleporting to monastery");
        boolean teleported = bankingHelper.tryArdougneCloakTeleport();
        if (!teleported) {
            script.log(getClass(), "ERROR: failed to teleport to monastery");
            return 600;
        }

        return 0;
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
}
