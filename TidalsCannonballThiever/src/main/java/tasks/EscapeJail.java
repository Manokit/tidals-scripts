package tasks;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.walker.WalkConfig;
import utilities.RetryUtils;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class EscapeJail extends Task {
    public static final RectangleArea JAIL_CELL = new RectangleArea(1883, 3272, 2, 2, 0);
    private static final WorldPosition WAYPOINT_1_BASE = new WorldPosition(1889, 3288, 0);
    private static final WorldPosition WAYPOINT_2_BASE = new WorldPosition(1876, 3293, 0);
    private static final WorldPosition THIEVING_TILE_SINGLE = new WorldPosition(1867, 3298, 0);
    private static final WorldPosition THIEVING_TILE_TWO_STALL = new WorldPosition(1867, 3295, 0);

    private WorldPosition getThievingTile() {
        return twoStallMode ? THIEVING_TILE_TWO_STALL : THIEVING_TILE_SINGLE;
    }

    // smooth walking settings
    private static final int RECLICK_MIN_MS = 700;
    private static final int RECLICK_MAX_MS = 1200;
    private static final int LONG_WALK_THRESHOLD = 10;
    private static final long MOVEMENT_TIMEOUT_MS = 30000; // 30 seconds stall = stop

    private boolean isEscaping = false;
    private int escapeAttempts = 0;
    private static final int MAX_ESCAPE_ATTEMPTS = 5;

    // movement tracking for stall detection
    private WorldPosition lastKnownPosition = null;
    private long lastMovementTime = System.currentTimeMillis();

    public EscapeJail(Script script) {
        super(script);
    }

    public static void resetStaticState() {
        // called on script restart if needed
    }

    @Override
    public boolean activate() {
        if (isEscaping)
            return false;
        return isInJail();
    }

    @Override
    public boolean execute() {
        task = "Escaping jail!";
        isEscaping = true;
        currentlyThieving = false;
        escapeAttempts++;
        script.log("JAIL", "Caught! Escape attempt " + escapeAttempts + "/" + MAX_ESCAPE_ATTEMPTS);

        try {
            if (escapeAttempts > MAX_ESCAPE_ATTEMPTS) {
                script.log("JAIL", "ERROR: Max escape attempts reached, stopping script");
                script.stop();
                return false;
            }


            // standard lock pick escape
            if (isOutOfCell()) {
                script.log("JAIL", "Already outside cell, skipping door pick");
            } else {
                if (!pickCellDoor()) {
                    script.log("JAIL", "Failed to pick door, retrying...");
                    return false;
                }

                script.log("JAIL", "Picking lock, waiting for success...");
                boolean success = script.pollFramesUntil(() -> chatContainsSuccess() || isOutOfCell(), 15000);

                if (!success && !isOutOfCell()) {
                    script.log("JAIL", "Lock pick failed or timed out, retrying...");
                    return false;
                }

                script.log("JAIL", "Lock picked successfully! Escaping...");
            }

            script.pollFramesHuman(() -> false, script.random(600, 1000));
            boolean pathed = pathBackToStall();

            if (pathed) {
                script.log("JAIL", "Escaped and back at stall!");
                escapeAttempts = 0;

                // reset thieving cycle state so first drop assumption works again
                StartThieving.resetForNewCycle();
                if (guardTracker != null) {
                    guardTracker.resetCbCycle();
                    guardTracker.resetGuardTracking();
                }

                return true;
            }

            script.log("JAIL", "Path back to stall failed, will retry");
            return false;

        } finally {
            isEscaping = false;
        }
    }

    private boolean pickCellDoor() {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null)
            return false;

        RSObject door = script.getObjectManager().getClosestObject(myPos, "Cell door");
        if (door == null) {
            script.log("JAIL", "Can't find cell door!");
            return false;
        }

        Polygon doorPoly = door.getConvexHull();
        if (doorPoly == null) {
            script.log("JAIL", "Door convex hull null");
            return false;
        }

        script.log("JAIL", "Attempting Picklock on cell door...");
        boolean tapped = script.getFinger().tap(doorPoly, "Picklock");

        if (tapped) {
            script.log("JAIL", "Picklock action sent!");
            return true;
        }

        script.log("JAIL", "Failed to send Picklock action");
        return false;
    }

    private boolean chatContainsSuccess() {
        try {
            UIResultList<String> chatText = script.getWidgetManager().getChatbox().getText();
            if (chatText == null)
                return false;

            for (String line : chatText.asList()) {
                if (line != null && line.contains("succeed")) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean pathBackToStall() {
        script.log("JAIL", "Pathing back to stall using smooth walking...");

        try {
            // step out of cell (short walk, no need for smooth)
            WorldPosition exitTile = new WorldPosition(1887, 3273, 0);
            script.log("JAIL", "Stepping out of cell to: " + exitTile);

            WalkConfig shortConfig = new WalkConfig.Builder()
                    .setWalkMethods(false, true)
                    .breakDistance(1)
                    .tileRandomisationRadius(0)
                    .timeout(10000)
                    .build();

            boolean walked = script.getWalker().walkTo(exitTile, shortConfig);
            if (!walked) {
                script.log("JAIL", "Failed to step out of cell");
                return false;
            }

            script.pollFramesHuman(() -> false, script.random(400, 700));

            // waypoint 1 (north) - use smooth walking for 10+ tile distance
            WorldPosition wp1 = randomizePosition(WAYPOINT_1_BASE, 2);
            script.log("JAIL", "Walking north to waypoint 1: " + wp1);
            if (!walkToPositionSmooth(wp1, 2)) {
                script.log("JAIL", "Failed to reach waypoint 1");
                return false;
            }

            script.pollFramesHuman(() -> false, script.random(300, 500));

            // waypoint 2 (west) - smooth walk continues
            WorldPosition wp2 = randomizePosition(WAYPOINT_2_BASE, 2);
            script.log("JAIL", "Walking west to waypoint 2: " + wp2);
            if (!walkToPositionSmooth(wp2, 2)) {
                script.log("JAIL", "Failed to reach waypoint 2");
                return false;
            }

            script.pollFramesHuman(() -> false, script.random(300, 500));

            // final destination: thieving tile (exact position needed)
            WorldPosition targetTile = getThievingTile();
            script.log("JAIL", "Walking to thieving tile: " + (twoStallMode ? "two-stall" : "single"));
            if (!walkToPositionSmooth(targetTile, 0)) {
                script.log("JAIL", "Failed to reach thieving tile");
                return false;
            }

            // verify we made it
            script.pollFramesHuman(() -> false, script.random(300, 500));
            if (isAtThievingTile()) {
                return true;
            }

            script.log("JAIL", "Did not reach exact thieving tile");
            return false;

        } catch (Exception e) {
            script.log("JAIL", "Error pathing back to stall: " + e.getMessage());
            return false;
        }
    }

    private WorldPosition randomizePosition(WorldPosition base, int radius) {
        int offsetX = script.random(-radius, radius);
        int offsetY = script.random(-radius, radius);
        return new WorldPosition(
                (int) base.getX() + offsetX,
                (int) base.getY() + offsetY,
                0);
    }

    // smooth walking for long distances - re-clicks while moving to prevent stalls
    private boolean walkToPositionSmooth(WorldPosition target, int breakDistance) {
        resetMovementTracking();

        long lastReclickAt = 0;
        long nextInterval = script.random(RECLICK_MIN_MS, RECLICK_MAX_MS);

        WalkConfig baseConfig = new WalkConfig.Builder()
                .setWalkMethods(false, true) // minimap only
                .breakDistance(breakDistance)
                .tileRandomisationRadius(0)
                .timeout(30000)
                .build();

        // start initial walk
        script.getWalker().walkTo(target, baseConfig);

        // keep nudging until we arrive or stall
        while (true) {
            WorldPosition myPos = script.getWorldPosition();
            if (myPos == null) {
                script.pollFramesHuman(() -> false, script.random(100, 200));
                continue;
            }

            int dist = (int) myPos.distanceTo(target);

            // arrived?
            if (dist <= breakDistance) {
                script.log("JAIL", "Arrived at destination (dist=" + dist + ")");
                return true;
            }

            // update stall detection
            updateMovementTracking(myPos);
            if (isStalled()) {
                script.log("JAIL", "Movement stalled for too long, aborting walk");
                return false;
            }

            // re-click if enough time has passed and still far away
            long now = System.currentTimeMillis();
            if (dist > LONG_WALK_THRESHOLD && (now - lastReclickAt) >= nextInterval) {
                script.log("JAIL", "Re-clicking walk (dist=" + dist + ")");

                WalkConfig stepConfig = new WalkConfig.Builder()
                        .timeout(8000)
                        .breakDistance(breakDistance)
                        .setWalkMethods(false, true) // minimap only
                        .tileRandomisationRadius(0)
                        .minimapTapDelay(120, 260)
                        .allowInterrupt(true)
                        .build();

                script.getWalker().walkTo(target, stepConfig);
                lastReclickAt = now;
                nextInterval = script.random(RECLICK_MIN_MS, RECLICK_MAX_MS);
            }

            // close any open containers while walking
            script.getWidgetManager().getTabManager().closeContainer();
            script.pollFramesHuman(() -> false, script.random(150, 250));
        }
    }

    private void resetMovementTracking() {
        lastKnownPosition = script.getWorldPosition();
        lastMovementTime = System.currentTimeMillis();
    }

    private void updateMovementTracking(WorldPosition currentPos) {
        if (lastKnownPosition != null && !positionsEqual(lastKnownPosition, currentPos)) {
            lastMovementTime = System.currentTimeMillis();
        }
        lastKnownPosition = currentPos;
    }

    private boolean isStalled() {
        return (System.currentTimeMillis() - lastMovementTime) >= MOVEMENT_TIMEOUT_MS;
    }

    private boolean positionsEqual(WorldPosition a, WorldPosition b) {
        if (a == null || b == null) return false;
        return (int) a.getX() == (int) b.getX() && (int) a.getY() == (int) b.getY();
    }

    private boolean isInJail() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null)
            return false;
        return JAIL_CELL.contains(pos);
    }

    private boolean isOutOfCell() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null)
            return false;
        return !JAIL_CELL.contains(pos);
    }

    private boolean isAtThievingTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null)
            return false;
        WorldPosition target = getThievingTile();
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == (int) target.getX() && y == (int) target.getY();
    }
}
