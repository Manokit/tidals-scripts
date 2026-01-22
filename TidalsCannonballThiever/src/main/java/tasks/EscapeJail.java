package tasks;

import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.UIResult;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.walker.WalkConfig;
import utilities.RetryUtils;
import utilities.TabUtils;
import utils.Task;

import java.util.Arrays;
import java.util.List;

import static main.TidalsCannonballThiever.*;

public class EscapeJail extends Task {
    public static final RectangleArea JAIL_CELL = new RectangleArea(1883, 3272, 2, 2, 0);

    // sailor's amulet for fast escape via teleport
    private static final int SAILORS_AMULET = 32399;
    // teleport lands around (1889, 3291) - near the market, not the actual dock
    private static final RectangleArea PORT_ROBERTS_TELEPORT = new RectangleArea(1885, 3288, 1895, 3295, 0);

    // waypoint path from jail back to stalls (tight path for better minimap visibility)
    private static final WorldPosition[] JAIL_PATH = {
            new WorldPosition(1886, 3273, 0),
            new WorldPosition(1889, 3276, 0),
            new WorldPosition(1892, 3277, 0),
            new WorldPosition(1892, 3280, 0),
            new WorldPosition(1892, 3284, 0),
            new WorldPosition(1888, 3286, 0),
            new WorldPosition(1888, 3290, 0),
            new WorldPosition(1884, 3292, 0),
            new WorldPosition(1877, 3293, 0),
            new WorldPosition(1873, 3293, 0),
            new WorldPosition(1869, 3295, 0)
    };
    private static final WorldPosition THIEVING_TILE_SINGLE = new WorldPosition(1867, 3298, 0);
    private static final WorldPosition THIEVING_TILE_TWO_STALL = new WorldPosition(1867, 3295, 0);

    private WorldPosition getThievingTile() {
        return twoStallMode ? THIEVING_TILE_TWO_STALL : THIEVING_TILE_SINGLE;
    }

    private boolean isEscaping = false;
    private int escapeAttempts = 0;
    private static final int MAX_ESCAPE_ATTEMPTS = 5;

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

            // check for sailor's amulet - fast escape via teleport
            if (hasSailorsAmulet()) {
                script.log("JAIL", "Sailor's Amulet detected - teleporting to Port Roberts");
                if (teleportWithAmulet()) {
                    escapeAttempts = 0;
                    return walkFromDockToStall();
                }
                script.log("JAIL", "Amulet teleport failed, falling back to lock pick...");
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

            // reset escape attempts now that we're out of the cell
            // pathing back to stall is a separate concern
            escapeAttempts = 0;

            script.pollFramesHuman(() -> false, script.random(600, 1000));
            boolean pathed = pathBackToStall();

            if (pathed) {
                script.log("JAIL", "Escaped and back at stall!");

                // reset thieving cycle state so first drop assumption works again
                StartThieving.resetForNewCycle();
                if (guardTracker != null) {
                    guardTracker.resetCbCycle();
                    guardTracker.resetGuardTracking();
                    guardTracker.enableGuardSync(); // wait to see guard leave before starting
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
        script.log("JAIL", "Walking back to stall...");

        try {
            // screen walk to first waypoint (step out of cell area)
            script.log("JAIL", "Stepping out of cell");
            WalkConfig exitConfig = new WalkConfig.Builder()
                    .setWalkMethods(true, false)
                    .breakDistance(1)
                    .tileRandomisationRadius(0)
                    .timeout(10000)
                    .build();

            if (!script.getWalker().walkTo(JAIL_PATH[0], exitConfig)) {
                script.log("JAIL", "Failed to step out of cell");
                return false;
            }

            script.pollFramesHuman(() -> false, script.random(200, 400));

            // minimap walk the rest of the path (clicking ahead while moving)
            List<WorldPosition> minimapPath = Arrays.asList(JAIL_PATH).subList(1, JAIL_PATH.length);
            if (!walkMinimapPath(minimapPath)) {
                script.log("JAIL", "Failed minimap path");
                return false;
            }

            // final approach: screen walk to exact thieving tile
            script.log("JAIL", "Screen walking to thieving tile");
            WalkConfig screenConfig = new WalkConfig.Builder()
                    .setWalkMethods(true, false)
                    .breakDistance(0)
                    .tileRandomisationRadius(0)
                    .timeout(10000)
                    .build();

            script.getWalker().walkTo(getThievingTile(), screenConfig);
            script.pollFramesHuman(() -> false, script.random(300, 500));

            return isAtThievingTile();

        } catch (Exception e) {
            script.log("JAIL", "Error pathing back: " + e.getMessage());
            return false;
        }
    }

    // click ahead on minimap while moving - don't wait for arrival at each waypoint
    private boolean walkMinimapPath(List<WorldPosition> waypoints) {
        if (waypoints.isEmpty()) return true;

        int currentIndex = 0;
        long lastClickTime = 0;
        long startTime = System.currentTimeMillis();
        long timeout = 30000;

        // click first waypoint to start moving
        script.log("JAIL", "Clicking waypoint 1/" + waypoints.size());
        clickMinimap(waypoints.get(0));
        lastClickTime = System.currentTimeMillis();

        while (currentIndex < waypoints.size()) {
            if (System.currentTimeMillis() - startTime > timeout) {
                script.log("JAIL", "Minimap path timed out");
                return false;
            }

            WorldPosition myPos = script.getWorldPosition();
            if (myPos == null) {
                script.pollFramesHuman(() -> false, 100);
                continue;
            }

            WorldPosition currentTarget = waypoints.get(currentIndex);
            int distToCurrentTarget = (int) myPos.distanceTo(currentTarget);

            // when close to current waypoint, advance and click next
            if (distToCurrentTarget <= 5) {
                currentIndex++;
                if (currentIndex < waypoints.size()) {
                    script.log("JAIL", "Clicking waypoint " + (currentIndex + 1) + "/" + waypoints.size());
                    clickMinimap(waypoints.get(currentIndex));
                    lastClickTime = System.currentTimeMillis();
                }
            } else {
                // re-click current target periodically to keep momentum
                long now = System.currentTimeMillis();
                if (now - lastClickTime > script.random(800, 1200)) {
                    clickMinimap(currentTarget);
                    lastClickTime = now;
                }
            }

            // close containers while walking
            script.getWidgetManager().getTabManager().closeContainer();
            script.pollFramesHuman(() -> false, script.random(80, 150));
        }

        // wait a moment after last waypoint
        script.pollFramesHuman(() -> false, script.random(200, 400));
        return true;
    }

    private void clickMinimap(WorldPosition pos) {
        WalkConfig config = new WalkConfig.Builder()
                .setWalkMethods(false, true)
                .tileRandomisationRadius(0)
                .timeout(500) // short timeout - just click and return
                .allowInterrupt(true)
                .build();

        script.getWalker().walkTo(pos, config);
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

    private boolean hasSailorsAmulet() {
        try {
            TabUtils.openAndWaitEquipment(script);
            UIResult<ItemSearchResult> amulet = script.getWidgetManager().getEquipment().findItem(SAILORS_AMULET);
            return amulet != null && amulet.isFound();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean teleportWithAmulet() {
        script.log("JAIL", "Attempting Sailor's Amulet teleport...");

        // save current position to detect teleport
        WorldPosition startPos = script.getWorldPosition();
        if (startPos == null) return false;

        boolean success = RetryUtils.equipmentInteract(script, SAILORS_AMULET, "Port Roberts", "sailor's amulet teleport", 5);
        if (!success) {
            script.log("JAIL", "Failed to interact with amulet");
            return false;
        }

        // wait for teleport - check we're out of jail and near teleport area
        boolean teleported = script.pollFramesUntil(() -> {
            WorldPosition pos = script.getWorldPosition();
            if (pos == null) return false;
            // out of jail cell and either in teleport area or moved significantly
            return !JAIL_CELL.contains(pos) &&
                   (PORT_ROBERTS_TELEPORT.contains(pos) || pos.distanceTo(startPos) > 10);
        }, 5000);

        if (teleported) {
            WorldPosition newPos = script.getWorldPosition();
            script.log("JAIL", "Teleport successful! Now at " + (newPos != null ?
                (int)newPos.getX() + ", " + (int)newPos.getY() : "unknown"));
            script.pollFramesHuman(() -> false, script.random(600, 1000));
            return true;
        }

        script.log("JAIL", "Teleport did not complete");
        return false;
    }

    private boolean walkFromDockToStall() {
        script.log("JAIL", "Walking from dock to stall...");

        try {
            WalkConfig config = new WalkConfig.Builder()
                    .breakDistance(2)
                    .tileRandomisationRadius(1)
                    .timeout(30000)
                    .build();

            boolean walked = script.getWalker().walkTo(getThievingTile(), config);
            script.pollFramesHuman(() -> false, script.random(300, 500));

            if (walked || isAtThievingTile()) {
                script.log("JAIL", "Arrived at stall from dock!");

                // reset thieving cycle state
                StartThieving.resetForNewCycle();
                if (guardTracker != null) {
                    guardTracker.resetCbCycle();
                    guardTracker.resetGuardTracking();
                    guardTracker.enableGuardSync();
                }

                return true;
            }
        } catch (NullPointerException e) {
            script.log("JAIL", "Walker NPE during dock walk, retrying...");
            return false;
        }

        script.log("JAIL", "Failed to walk from dock to stall");
        return false;
    }
}
