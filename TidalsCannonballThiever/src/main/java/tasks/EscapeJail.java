package tasks;

import com.osmb.api.input.MenuEntry;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class EscapeJail extends Task {
    // jail cell is 2x2 at roughly (1884-1885, 3273-3274)
    private static final int JAIL_MIN_X = 1884;
    private static final int JAIL_MAX_X = 1885;
    private static final int JAIL_MIN_Y = 3273;
    private static final int JAIL_MAX_Y = 3274;

    // door position
    private static final WorldPosition DOOR_TILE = new WorldPosition(1885, 3273, 0);

    // waypoints back to stall (with randomization applied in execute)
    private static final WorldPosition WAYPOINT_1_BASE = new WorldPosition(1889, 3288, 0); // north of cell
    private static final WorldPosition WAYPOINT_2_BASE = new WorldPosition(1876, 3293, 0); // closer to stall
    private static final WorldPosition THIEVING_TILE = new WorldPosition(1867, 3298, 0);

    private final WalkConfig minimapOnlyConfig;

    // flag to prevent re-activation while escaping
    private boolean isEscaping = false;

    public EscapeJail(Script script) {
        super(script);
        this.minimapOnlyConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(2) // close enough
                .tileRandomisationRadius(0)
                .build();
    }

    @Override
    public boolean activate() {
        // don't re-activate if we're already escaping
        if (isEscaping) {
            return false;
        }
        // activate if we're in the jail cell
        return isInJail();
    }

    @Override
    public boolean execute() {
        task = "Escaping jail!";
        isEscaping = true; // prevent re-activation
        currentlyThieving = false;
        script.log("JAIL", "Caught! Attempting to escape...");

        // STEP 1: Find and pick the cell door
        if (!pickCellDoor()) {
            script.log("JAIL", "Failed to pick door, retrying...");
            isEscaping = false;
            return false;
        }

        // STEP 2: Wait specifically for success message in chat
        script.log("JAIL", "Picking lock, waiting for success...");
        boolean success = script.pollFramesUntil(() -> chatContainsSuccess(), 15000);

        if (!success) {
            script.log("JAIL", "Lock pick failed or timed out, retrying...");
            isEscaping = false;
            return false;
        }

        script.log("JAIL", "Lock picked successfully! Escaping...");

        // small delay after success message
        script.pollFramesHuman(() -> false, script.random(600, 1000));

        // STEP 3: Path back to thieving area via waypoints
        pathBackToStall();

        script.log("JAIL", "Escaped and back at stall!");
        isEscaping = false; // done escaping
        return true;
    }

    private boolean pickCellDoor() {
        // find the cell door object
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

        // check what action is available on the door
        MenuEntry response = script.getFinger().tapGetResponse(true, doorPoly);
        if (response == null) {
            script.log("JAIL", "No menu response from door");
            return false;
        }

        String action = response.getAction();
        script.log("JAIL", "Door action: " + action);

        // if picklock action available, use it
        if (action != null && action.toLowerCase().contains("picklock")) {
            return script.getFinger().tap(doorPoly, "Picklock");
        }

        // door might already be open or different action
        script.log("JAIL", "No picklock action found, action was: " + action);
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
            // ignore chat read errors
        }
        return false;
    }

    private void pathBackToStall() {
        script.log("JAIL", "Pathing back to stall...");

        // first step: walk out of the cell area (just east of door)
        WorldPosition exitTile = new WorldPosition(1887, 3273, 0);
        script.log("JAIL", "Stepping out of cell to: " + exitTile);
        script.getWalker().walkTo(exitTile, minimapOnlyConfig);
        
        // wait until we're actually out of the cell
        script.pollFramesUntil(() -> !isInJail(), 5000);
        script.pollFramesHuman(() -> false, script.random(400, 700));

        // waypoint 1: north of cell area
        WorldPosition wp1 = randomizePosition(WAYPOINT_1_BASE, 2);
        script.log("JAIL", "Walking north to: " + wp1);
        script.getWalker().walkTo(wp1, minimapOnlyConfig);
        
        // wait until Y coord is north of cell (y > 3280)
        script.pollFramesUntil(() -> {
            WorldPosition pos = script.getWorldPosition();
            return pos != null && pos.getY() > 3280;
        }, 8000);
        script.pollFramesHuman(() -> false, script.random(300, 500));

        // waypoint 2: heading toward stall
        WorldPosition wp2 = randomizePosition(WAYPOINT_2_BASE, 2);
        script.log("JAIL", "Walking west to: " + wp2);
        script.getWalker().walkTo(wp2, minimapOnlyConfig);
        
        // wait until X coord is west enough (x < 1880)
        script.pollFramesUntil(() -> {
            WorldPosition pos = script.getWorldPosition();
            return pos != null && pos.getX() < 1880;
        }, 8000);
        script.pollFramesHuman(() -> false, script.random(300, 500));

        // final: thieving tile
        script.log("JAIL", "Walking to thieving tile");
        WalkConfig exactConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .build();
        script.getWalker().walkTo(THIEVING_TILE, exactConfig);
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

        int x = (int) pos.getX();
        int y = (int) pos.getY();

        return x >= JAIL_MIN_X && x <= JAIL_MAX_X &&
               y >= JAIL_MIN_Y && y <= JAIL_MAX_Y &&
               pos.getPlane() == 0;
    }

    private boolean isAtThievingTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == 1867 && y == 3298;
    }
}
