package tasks;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class StartThieving extends Task {
    // single-stall mode
    private static final WorldPosition THIEVING_TILE_SINGLE = new WorldPosition(1867, 3298, 0);
    private static final RectangleArea THIEVING_AREA_SINGLE = new RectangleArea(1865, 3296, 1869, 3300, 0);

    // two-stall mode
    private static final WorldPosition THIEVING_TILE_TWO_STALL = new WorldPosition(1867, 3295, 0);
    private static final RectangleArea THIEVING_AREA_TWO_STALL = new RectangleArea(1862, 3293, 1869, 3297, 0);

    private static boolean initialPositionDone = false;
    private static boolean firstDropAssumed = false;
    private static boolean justCompletedGuardSync = false; // skip delays after sync
    private static boolean delayPending = false; // pre-theft delay queued for next poll
    private static boolean delayRolled = false;  // already rolled the delay dice this cycle

    public static void resetStaticState() {
        initialPositionDone = false;
        firstDropAssumed = false;
        justCompletedGuardSync = false;
        delayPending = false;
        delayRolled = false;
    }

    public static void resetAfterBreak() {
        // full reset for break/hop/afk - same as new cycle
        initialPositionDone = false;
        firstDropAssumed = false;
        justCompletedGuardSync = false;
        delayPending = false;
        delayRolled = false;
    }

    // reset for new thieving cycle (after deposit, jail, etc.)
    public static void resetForNewCycle() {
        initialPositionDone = false;
        firstDropAssumed = false;
        justCompletedGuardSync = false;
        delayPending = false;
        delayRolled = false;
    }

    private WorldPosition getThievingTile() {
        return twoStallMode ? THIEVING_TILE_TWO_STALL : THIEVING_TILE_SINGLE;
    }

    private RectangleArea getThievingArea() {
        return twoStallMode ? THIEVING_AREA_TWO_STALL : THIEVING_AREA_SINGLE;
    }

    public StartThieving(Script script) {
        super(script);
    }

    private WalkConfig buildMinimapConfig() {
        return new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .timeout(RandomUtils.weightedRandom(8000, 12000, 0.002))
                .build();
    }

    @Override
    public boolean activate() {
        if (currentlyThieving) return false;
        return isAtThievingTile() || (isAtSafetyTile() && guardTracker.isSafeToReturn());
    }

    @Override
    public boolean execute() {
        task = "Starting thieving";

        // state: not at exact thieving tile? walk there
        if (!initialPositionDone && !isAtExactThievingTile()) {
            task = "Initial positioning";
            script.log("THIEVE", "Walking to exact thieving tile...");
            script.getWalker().walkTo(getThievingTile(), buildMinimapConfig());
            // skip humanised delay in two-stall mode - timing critical
            if (!twoStallMode) {
                script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(200, 800, 0.002));
            }
            return false;
        }

        // state: at position, need guard sync? (two-stall mode only)
        if (!initialPositionDone && twoStallMode) {
            if (guardTracker.needsGuardSync()) {
                if (!guardTracker.isGuardSyncComplete()) {
                    task = "Syncing with guard cycle";
                    return false; // keep polling, isGuardSyncComplete() logs progress
                }
                // sync complete - start IMMEDIATELY, no delays
                initialPositionDone = true;
                justCompletedGuardSync = true;
                script.log("THIEVE", "Guard sync complete - starting 4-2 cycle NOW!");
                return true; // re-poll to start stealing
            }
            // normal two-stall mode: check if safe
            if (!guardTracker.isCannonballStallSafe()) {
                task = "Waiting for guard cycle";
                script.log("THIEVE", "Waiting to see guard pass stall (x >= 1868)...");
                return false;
            }
            initialPositionDone = true;
            script.log("THIEVE", "Initial positioning complete - starting fresh cycle!");
            return true;
        }

        // state: at position, guard not safe? (single-stall mode only)
        if (!initialPositionDone) {
            if (!guardTracker.isSafeToReturn()) {
                task = "Waiting for guard (setup)";
                script.log("THIEVE", "At position but guard in patrol zone - waiting...");
                return false;
            }
            initialPositionDone = true;
            script.log("THIEVE", "Initial positioning complete - starting fresh cycle!");
            return true;
        }

        // state: guard in danger zone? abort
        if (guardTracker.isAnyGuardInDangerZone()) {
            script.log("THIEVE", "ABORT - Guard in danger zone");
            return false;
        }

        // state: guard still in patrol zone? abort
        if (!guardTracker.isSafeToReturn()) {
            script.log("THIEVE", "ABORT - Guard still in patrol zone");
            return false;
        }

        // state: executing pre-theft delay (set on previous poll)
        if (delayPending) {
            delayPending = false;
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(80, 400, 0.002));
            if (guardTracker.isAnyGuardInDangerZone()) {
                script.log("THIEVE", "ABORT - Guard moved in during delay!");
                return false;
            }
            // delay done, fall through to steal
        }

        // state: optional pre-theft delay (single-stall mode, 25% chance)
        if (!twoStallMode && !delayRolled && RandomUtils.uniformRandom(1, 100) <= 25) {
            delayPending = true;
            delayRolled = true;
            return true; // re-poll to execute delay next frame
        }
        delayRolled = false;

        // state: validate position
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) {
            script.log("THIEVE", "Position null, waiting...");
            return false;
        }

        // state: find stall
        RSObject stall = script.getObjectManager().getClosestObject(myPos, "Cannonball stall");
        if (stall == null) {
            script.log("THIEVE", "ERROR: Can't find Cannonball stall!");
            return false;
        }

        // state: last-second danger check before tap
        if (guardTracker.isAnyGuardInDangerZone()) {
            script.log("THIEVE", "ABORT - Guard appeared at last second!");
            return false;
        }

        // state: validate stall geometry
        Polygon stallPoly = stall.getConvexHull();
        if (stallPoly == null) {
            script.log("THIEVE", "Stall convex hull null, will retry...");
            return false;
        }
        if (!isPolygonTapSafe(stallPoly)) {
            script.log("THIEVE", "Stall hull outside screen bounds, will retry...");
            return false;
        }

        // state: ready to steal - single tap attempt, framework re-polls on failure
        script.log("THIEVE", "Clear - attempting steal...");
        boolean tapped = script.getFinger().tapGameScreen(stallPoly, "steal");

        if (!tapped) {
            script.log("THIEVE", "Tap failed, will retry next poll");
            return false;
        }

        // state: tap succeeded - update tracking state
        script.log("THIEVE", "Clicked Cannonball stall!");
        currentlyThieving = true;
        lastXpGain.reset();

        if (twoStallMode) {
            double currentXp = xpTracking.getCurrentXp();
            guardTracker.initXpTracking(currentXp);
            guardTracker.resetCbCycle();
            script.log("THIEVE", "Initialized XP cycle tracking (baseline: " + currentXp + ")");
        }

        // on first stall tap, assume first xp drop (tracker might miss it)
        if (!firstDropAssumed) {
            if (script instanceof main.TidalsCannonballThiever) {
                ((main.TidalsCannonballThiever) script).checkInventoryForChangesManual();
            }
            if (twoStallMode) {
                guardTracker.assumeFirstCbDrop();
            }
            firstDropAssumed = true;
        }

        // skip delay in two-stall mode - timing critical
        if (!twoStallMode) {
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(200, 800, 0.002));
        }
        justCompletedGuardSync = false;
        return true;
    }

    private boolean isAtThievingTile() {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;
        return getThievingArea().contains(current);
    }

    private boolean isAtExactThievingTile() {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;

        WorldPosition target = getThievingTile();
        int x = (int) current.getX();
        int y = (int) current.getY();
        return x == (int) target.getX() && y == (int) target.getY() && current.getPlane() == 0;
    }

    private boolean isAtSafetyTile() {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;

        int x = (int) current.getX();
        int y = (int) current.getY();
        return x == 1867 && y == 3299;
    }

    private boolean isPolygonTapSafe(Polygon poly) {
        if (poly == null || poly.numVertices() == 0) {
            return false;
        }

        int[] xs = poly.getXPoints();
        int[] ys = poly.getYPoints();

        for (int i = 0; i < xs.length; i++) {
            int x = xs[i];
            int y = ys[i];

            if (x < 0 || y < 0 || x >= screenWidth || y >= screenHeight) {
                return false;
            }
        }
        return true;
    }
}
