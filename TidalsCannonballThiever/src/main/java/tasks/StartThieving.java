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
    // single-stall mode positions
    private static final WorldPosition THIEVING_TILE_SINGLE = new WorldPosition(1867, 3298, 0);
    private static final RectangleArea THIEVING_AREA_SINGLE = new RectangleArea(1865, 3296, 1869, 3300, 0);

    // two-stall mode positions (cannonball stall at different Y)
    private static final WorldPosition THIEVING_TILE_TWO_STALL = new WorldPosition(1867, 3295, 0);
    private static final RectangleArea THIEVING_AREA_TWO_STALL = new RectangleArea(1862, 3293, 1869, 3297, 0);

    // track if we've done initial positioning (only need exact tile once)
    private static boolean initialPositionDone = false;

    /**
     * Reset static state - call this from onStart() to ensure fresh state on script restart
     */
    public static void resetStaticState() {
        initialPositionDone = false;
    }
    
    /**
     * Reset positioning flag - call after breaks/world hops to ensure fresh guard cycle
     */
    public static void resetAfterBreak() {
        initialPositionDone = false;
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

    @Override
    public boolean activate() {
        // don't activate if we're already thieving
        if (currentlyThieving) {
            return false;
        }

        // activate if at thieving tile OR at safety tile when guard has passed
        return isAtThievingTile() || (isAtSafetyTile() && guardTracker.isSafeToReturn());
    }

    @Override
    public boolean execute() {
        task = "Starting thieving";

        // STEP 1: only on first run, walk to EXACT thieving tile via minimap
        if (!initialPositionDone) {
            if (!isAtExactThievingTile()) {
                task = "Initial positioning";
                script.log("THIEVE", "First run - walking to exact thieving tile via minimap...");

                // use minimap-only to avoid clicking any entities
                WalkConfig minimapOnly = new WalkConfig.Builder()
                        .disableWalkScreen(true)
                        .breakDistance(0)
                        .tileRandomisationRadius(0)
                        .build();

                script.getWalker().walkTo(getThievingTile(), minimapOnly);
                script.pollFramesUntil(() -> isAtExactThievingTile(), 5000);

                // small delay after arriving
                script.pollFramesHuman(() -> false, script.random(200, 400));
                return false;
            }
            
            // CRITICAL: For initial setup, use STRICT guard check
            // Two-stall mode: Must actually SEE guard past stall (x >= 1868)
            // Single-stall mode: No guard in patrol zone 1864-1867
            if (twoStallMode) {
                // Stricter check - require seeing guard PAST the stall
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

        // STEP 2: instant danger check - NO DELAYS, speed is critical
        if (guardTracker.isAnyGuardInDangerZone()) {
            script.log("THIEVE", "ABORT - Guard in danger zone");
            return false; // retreat task will handle it
        }
        
        // extra safety: also check isSafeToReturn
        if (!guardTracker.isSafeToReturn()) {
            script.log("THIEVE", "ABORT - Guard still in patrol zone");
            return false;
        }

        script.log("THIEVE", "Clear - starting to steal...");

        // ~25% chance to add humanized delay before clicking (balances XP vs detection)
        if (script.random(1, 100) <= 25) {
            script.pollFramesHuman(() -> false, script.random(80, 200));
            
            // quick recheck after delay (guard might have moved fast)
            if (guardTracker.isAnyGuardInDangerZone()) {
                script.log("THIEVE", "ABORT - Guard moved in during delay!");
                return false;
            }
        }

        // STEP 3: find and click the stall
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

        // final danger check right before clicking
        if (guardTracker.isAnyGuardInDangerZone()) {
            script.log("THIEVE", "ABORT - Guard appeared at last second!");
            return false;
        }

        // get stall polygon
        Polygon stallPoly = stall.getConvexHull();
        if (stallPoly == null) {
            script.log("THIEVE", "Stall convex hull is null, retrying...");
            return false;
        }

        // check if polygon is tap-safe (within screen bounds)
        if (!isPolygonTapSafe(stallPoly)) {
            script.log("THIEVE", "Stall hull outside screen bounds, repositioning...");
            return false;
        }

        // FAST: Simple left-click - "Steal-from" is the default action
        // No menu overhead = faster startup = more time for full 4-thieve cycle
        boolean tapped = script.getFinger().tap(stallPoly);

        if (tapped) {
            script.log("THIEVE", "Clicked Cannonball stall!");
            currentlyThieving = true;
            lastXpGain.reset();
            
            // Initialize XP-based cycle tracking for two-stall mode
            if (twoStallMode) {
                // Ensure XP tracking is initialized
                if (!xpTracking.isInitialized()) {
                    script.log("THIEVE", "XP tracking not initialized, initializing now...");
                    xpTracking.initialize();
                }
                
                // Get current XP and initialize guard tracker
                double currentXp = xpTracking.getCurrentXp();
                guardTracker.initXpTracking(currentXp);
                guardTracker.resetCbCycle();
                script.log("THIEVE", "Initialized XP cycle tracking (baseline: " + currentXp + ")");
            }
            
            // humanized delay after clicking
            script.pollFramesHuman(() -> false, script.random(200, 400));
            return true;
        }

        script.log("THIEVE", "Failed to click stall, retrying...");
        return false;
    }

    private boolean isAtThievingTile() {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;

        // allow some flexibility - within thieving area
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
