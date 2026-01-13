package tasks;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
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
    private static final int MAX_STEAL_ATTEMPTS = 3;

    // cached config with timeout
    private final WalkConfig minimapConfig;

    public static void resetStaticState() {
        initialPositionDone = false;
        firstDropAssumed = false;
    }

    public static void resetAfterBreak() {
        // full reset for break/hop/afk - same as new cycle
        initialPositionDone = false;
        firstDropAssumed = false;
    }

    // reset for new thieving cycle (after deposit, jail, etc.)
    public static void resetForNewCycle() {
        initialPositionDone = false;
        firstDropAssumed = false;
    }

    private WorldPosition getThievingTile() {
        return twoStallMode ? THIEVING_TILE_TWO_STALL : THIEVING_TILE_SINGLE;
    }

    private RectangleArea getThievingArea() {
        return twoStallMode ? THIEVING_AREA_TWO_STALL : THIEVING_AREA_SINGLE;
    }

    public StartThieving(Script script) {
        super(script);
        this.minimapConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .timeout(10000)
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

        // first run: walk to exact thieving tile via minimap
        if (!initialPositionDone) {
            if (!isAtExactThievingTile()) {
                task = "Initial positioning";
                script.log("THIEVE", "First run - walking to exact thieving tile via minimap...");

                boolean walked = script.getWalker().walkTo(getThievingTile(), minimapConfig);
                script.pollFramesHuman(() -> false, script.random(200, 400));

                if (!walked && !isAtExactThievingTile()) {
                    script.log("THIEVE", "Walk failed, will retry");
                }
                return false;
            }
            
            // wait for guard to pass before starting
            if (twoStallMode) {
                if (!guardTracker.isCannonballStallSafe()) {
                    task = "Waiting for guard cycle";
                    script.log("THIEVE", "Waiting to see guard pass stall (x >= 1868)...");
                    return false;
                }
            } else {
                if (!guardTracker.isSafeToReturn()) {
                    task = "Waiting for guard (setup)";
                    script.log("THIEVE", "At position but guard in patrol zone - waiting for them to pass...");
                    return false;
                }
            }
            
            initialPositionDone = true;
            script.log("THIEVE", "Initial positioning complete - starting fresh cycle!");
        }

        // danger check
        if (guardTracker.isAnyGuardInDangerZone()) {
            script.log("THIEVE", "ABORT - Guard in danger zone");
            return false;
        }
        
        if (!guardTracker.isSafeToReturn()) {
            script.log("THIEVE", "ABORT - Guard still in patrol zone");
            return false;
        }

        script.log("THIEVE", "Clear - starting to steal...");

        // ~25% chance to add humanized delay
        if (script.random(1, 100) <= 25) {
            script.pollFramesHuman(() -> false, script.random(80, 200));
            if (guardTracker.isAnyGuardInDangerZone()) {
                script.log("THIEVE", "ABORT - Guard moved in during delay!");
                return false;
            }
        }

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) {
            script.log("THIEVE", "Position null, waiting...");
            return false;
        }

        RSObject stall = script.getObjectManager().getClosestObject(myPos, "Cannonball stall");

        if (stall == null) {
            script.log("THIEVE", "ERROR: Can't find Cannonball stall!");
            return false;
        }

        if (guardTracker.isAnyGuardInDangerZone()) {
            script.log("THIEVE", "ABORT - Guard appeared at last second!");
            return false;
        }

        Polygon stallPoly = stall.getConvexHull();
        if (stallPoly == null) {
            script.log("THIEVE", "Stall convex hull is null, retrying...");
            return false;
        }

        if (!isPolygonTapSafe(stallPoly)) {
            script.log("THIEVE", "Stall hull outside screen bounds, repositioning...");
            return false;
        }

        // tap with action and retries for reliability
        boolean tapped = false;
        for (int attempt = 1; attempt <= MAX_STEAL_ATTEMPTS; attempt++) {
            script.log("THIEVE", "Steal attempt " + attempt + "/" + MAX_STEAL_ATTEMPTS);
            tapped = script.getFinger().tap(stallPoly, "steal");
            if (tapped) break;
            script.pollFramesHuman(() -> false, script.random(200, 400));
        }

        if (tapped) {
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
            // if tracker also catches it, the flag prevents double-counting
            if (!firstDropAssumed) {
                if (script instanceof main.TidalsCannonballThiever) {
                    ((main.TidalsCannonballThiever) script).checkInventoryForChangesManual();
                }
                if (twoStallMode) {
                    guardTracker.assumeFirstCbDrop();
                }
                firstDropAssumed = true;
            }

            script.pollFramesHuman(() -> false, script.random(200, 400));
            return true;
        }

        script.log("THIEVE", "Failed to click stall after " + MAX_STEAL_ATTEMPTS + " attempts");
        return false;
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
