package tasks;

import com.osmb.api.input.MenuEntry;
import com.osmb.api.input.MenuHook;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.ui.component.chatbox.ChatboxComponent;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
            
            // CRITICAL: check guard BEFORE marking positioning done
            // if guard is in danger zone, wait for them to pass first
            if (guardTracker.isAnyGuardInDangerZone()) {
                task = "Waiting for guard (setup)";
                script.log("THIEVE", "At position but guard in danger zone - waiting...");
                return false; // retreat task will handle it, or wait task
            }
            
            // also wait if guard is about to arrive (early warning active)
            if (!guardTracker.isSafeToReturn()) {
                task = "Guard nearby (setup)";
                script.log("THIEVE", "At position but guard nearby - waiting for them to pass...");
                return false;
            }
            
            initialPositionDone = true;
            script.log("THIEVE", "Initial positioning complete - guard clear!");
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

        // use menu hook to properly select steal-from action
        AtomicReference<String> selectedAction = new AtomicReference<>(null);
        AtomicReference<List<MenuEntry>> menuCache = new AtomicReference<>(null);

        MenuHook hook = getStealFromMenuHook(selectedAction, menuCache, stall);

        boolean tapped = script.getFinger().tap(stallPoly, hook);

        if (tapped && selectedAction.get() != null) {
            script.log("THIEVE", "Clicked 'Steal-from Cannonball stall' - action: " + selectedAction.get());
            currentlyThieving = true; // we're now thieving, don't click again
            lastXpGain.reset();
            
            // Initialize XP-based cycle tracking for two-stall mode
            if (twoStallMode) {
                double currentXp = xpTracking.getThievingXpGained();
                guardTracker.initXpTracking(currentXp);
                guardTracker.resetCbCycle();
                script.log("THIEVE", "Initialized XP cycle tracking");
            }
            
            // humanized delay after clicking
            script.pollFramesHuman(() -> false, script.random(200, 400));
            return true;
        }

        // tap failed, inspect menu to see why
        List<MenuEntry> menu = menuCache.get();
        if (menu != null) {
            script.log("THIEVE", "Menu entries found but no valid Steal-from action:");
            for (MenuEntry e : menu) {
                script.log("THIEVE", "  - " + e.getAction() + " " + e.getEntityName());
            }
        } else {
            script.log("THIEVE", "No menu entries found at stall location");
        }

        return false;
    }

    /**
     * menu hook that only selects "steal-from" actions on cannonball stall
     */
    private static MenuHook getStealFromMenuHook(
            AtomicReference<String> selected,
            AtomicReference<List<MenuEntry>> lastMenu,
            RSObject stall
    ) {
        return menuEntries -> {
            selected.set(null);
            lastMenu.set(menuEntries);

            if (menuEntries == null) return null;

            MenuEntry best = null;
            int bestScore = Integer.MIN_VALUE;

            for (MenuEntry entry : menuEntries) {
                String action = entry.getAction().toLowerCase();
                String entity = entry.getEntityName().toLowerCase();

                // only accept steal-from actions
                if (!action.contains("steal")) {
                    continue;
                }

                int score = 0;

                // prefer exact cannonball stall match
                if (entity.contains("cannonball stall")) score += 100;
                if (entity.contains("cannonball")) score += 50;
                if (entity.contains("stall")) score += 25;

                if (score > bestScore && score > 0) {
                    bestScore = score;
                    best = entry;
                }
            }

            if (best == null) {
                return null;
            }

            selected.set(best.getAction());
            return best;
        };
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
