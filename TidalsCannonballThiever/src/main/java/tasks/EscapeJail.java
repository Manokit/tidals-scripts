package tasks;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class EscapeJail extends Task {
    private static final RectangleArea JAIL_CELL = new RectangleArea(1883, 3272, 2, 2, 0);
    private static final WorldPosition DOOR_TILE = new WorldPosition(1885, 3273, 0);
    private static final WorldPosition WAYPOINT_1_BASE = new WorldPosition(1889, 3288, 0);
    private static final WorldPosition WAYPOINT_2_BASE = new WorldPosition(1876, 3293, 0);
    private static final WorldPosition THIEVING_TILE_SINGLE = new WorldPosition(1867, 3298, 0);
    private static final WorldPosition THIEVING_TILE_TWO_STALL = new WorldPosition(1867, 3295, 0);

    private WorldPosition getThievingTile() {
        return twoStallMode ? THIEVING_TILE_TWO_STALL : THIEVING_TILE_SINGLE;
    }

    private final WalkConfig minimapOnlyConfig;
    private boolean isEscaping = false;

    public EscapeJail(Script script) {
        super(script);
        this.minimapOnlyConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(2)
                .tileRandomisationRadius(0)
                .build();
    }

    @Override
    public boolean activate() {
        if (isEscaping) return false;
        return isInJail();
    }

    @Override
    public boolean execute() {
        task = "Escaping jail!";
        isEscaping = true;
        currentlyThieving = false;
        script.log("JAIL", "Caught! Attempting to escape...");

        if (isOutOfCell()) {
            script.log("JAIL", "Already outside cell, skipping door pick");
        } else {
            if (!pickCellDoor()) {
                script.log("JAIL", "Failed to pick door, retrying...");
                isEscaping = false;
                return false;
            }

            script.log("JAIL", "Picking lock, waiting for success...");
            boolean success = script.pollFramesUntil(() -> chatContainsSuccess() || isOutOfCell(), 15000);

            if (!success && !isOutOfCell()) {
                script.log("JAIL", "Lock pick failed or timed out, retrying...");
                isEscaping = false;
                return false;
            }

            script.log("JAIL", "Lock picked successfully! Escaping...");
        }

        script.pollFramesHuman(() -> false, script.random(600, 1000));
        pathBackToStall();

        script.log("JAIL", "Escaped and back at stall!");
        isEscaping = false;
        return true;
    }

    private boolean pickCellDoor() {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

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
            if (chatText == null) return false;

            for (String line : chatText.asList()) {
                if (line != null && line.contains("succeed")) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private void pathBackToStall() {
        script.log("JAIL", "Pathing back to stall...");

        WorldPosition exitTile = new WorldPosition(1887, 3273, 0);
        script.log("JAIL", "Stepping out of cell to: " + exitTile);
        script.getWalker().walkTo(exitTile, minimapOnlyConfig);
        script.pollFramesUntil(() -> !isInJail(), 5000);
        script.pollFramesHuman(() -> false, script.random(400, 700));

        WorldPosition wp1 = randomizePosition(WAYPOINT_1_BASE, 2);
        script.log("JAIL", "Walking north to: " + wp1);
        script.getWalker().walkTo(wp1, minimapOnlyConfig);
        script.pollFramesUntil(() -> {
            WorldPosition pos = script.getWorldPosition();
            return pos != null && pos.getY() > 3280;
        }, 8000);
        script.pollFramesHuman(() -> false, script.random(300, 500));

        WorldPosition wp2 = randomizePosition(WAYPOINT_2_BASE, 2);
        script.log("JAIL", "Walking west to: " + wp2);
        script.getWalker().walkTo(wp2, minimapOnlyConfig);
        script.pollFramesUntil(() -> {
            WorldPosition pos = script.getWorldPosition();
            return pos != null && pos.getX() < 1880;
        }, 8000);
        script.pollFramesHuman(() -> false, script.random(300, 500));

        WorldPosition targetTile = getThievingTile();
        script.log("JAIL", "Walking to thieving tile: " + (twoStallMode ? "two-stall (3295)" : "single (3298)"));
        WalkConfig exactConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .build();
        script.getWalker().walkTo(targetTile, exactConfig);
        script.pollFramesUntil(() -> isAtThievingTile(), 10000);
    }

    private WorldPosition randomizePosition(WorldPosition base, int radius) {
        int offsetX = script.random(-radius, radius);
        int offsetY = script.random(-radius, radius);
        return new WorldPosition(
                (int) base.getX() + offsetX,
                (int) base.getY() + offsetY,
                0
        );
    }

    private boolean isInJail() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        return JAIL_CELL.contains(pos);
    }

    private boolean isOutOfCell() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        return !JAIL_CELL.contains(pos);
    }

    private boolean isAtThievingTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        WorldPosition target = getThievingTile();
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == (int) target.getX() && y == (int) target.getY();
    }
}
