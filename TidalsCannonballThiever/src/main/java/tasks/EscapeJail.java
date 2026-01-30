package tasks;

import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;
import com.osmb.api.walker.WalkConfig;
import utilities.RetryUtils;
import utilities.TabUtils;
import utils.Task;

import static main.TidalsCannonballThiever.*;

/**
 * Poll-based jail escape task.
 *
 * States are derived from position:
 * - IN_CELL: pick lock or teleport
 * - WALKING_BACK: walk to stall
 * - AT_STALL: reset and complete
 */
public class EscapeJail extends Task {
    public static final RectangleArea JAIL_CELL = new RectangleArea(1883, 3272, 2, 2, 0);

    // sailor's amulet for fast escape via teleport
    private static final int SAILORS_AMULET = 32399;
    // teleport lands around (1889, 3291) - near the market
    private static final RectangleArea PORT_ROBERTS_TELEPORT = new RectangleArea(1885, 3288, 1895, 3295, 0);

    // thieving tiles
    private static final WorldPosition THIEVING_TILE_SINGLE = new WorldPosition(1867, 3298, 0);
    private static final WorldPosition THIEVING_TILE_TWO_STALL = new WorldPosition(1867, 3295, 0);

    // escape state tracking
    private boolean escapeInProgress = false;
    private int escapeAttempts = 0;
    private static final int MAX_ESCAPE_ATTEMPTS = 5;

    // teleport state - track if we're waiting for teleport
    private boolean waitingForTeleport = false;
    private long teleportStartTime = 0;

    public EscapeJail(Script script) {
        super(script);
    }

    public static void resetStaticState() {
        // called on script restart if needed
    }

    private WorldPosition getThievingTile() {
        return twoStallMode ? THIEVING_TILE_TWO_STALL : THIEVING_TILE_SINGLE;
    }

    @Override
    public boolean activate() {
        // if actively escaping, stay active until done
        if (escapeInProgress) {
            return true;
        }
        // start escape if in jail cell
        return isInJail();
    }

    @Override
    public boolean execute() {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

        // initialize escape if just started
        if (!escapeInProgress) {
            startEscape();
        }

        // state: at thieving tile? we're done
        if (isAtThievingTile()) {
            return finishEscape();
        }

        // state: still in jail cell? get out
        if (JAIL_CELL.contains(myPos)) {
            return handleInCell();
        }

        // state: out of cell, walk to stall
        return handleWalkingBack(myPos);
    }

    /**
     * Initialize escape attempt - called once when escape begins
     */
    private void startEscape() {
        escapeInProgress = true;
        escapeAttempts++;
        waitingForTeleport = false;
        currentlyThieving = false;
        task = "Escaping jail!";
        script.log("JAIL", "Caught! Escape attempt " + escapeAttempts + "/" + MAX_ESCAPE_ATTEMPTS);

        if (escapeAttempts > MAX_ESCAPE_ATTEMPTS) {
            script.log("JAIL", "ERROR: Max escape attempts reached, stopping script");
            script.stop();
        }
    }

    /**
     * Handle being in jail cell - try amulet teleport or pick lock
     */
    private boolean handleInCell() {
        task = "In jail cell";

        // if waiting for teleport, check if it completed
        if (waitingForTeleport) {
            return checkTeleportProgress();
        }

        // try amulet teleport first if available
        if (hasSailorsAmulet()) {
            return attemptAmuletTeleport();
        }

        // standard lock pick
        return attemptLockPick();
    }

    /**
     * Attempt teleport with sailor's amulet
     */
    private boolean attemptAmuletTeleport() {
        task = "Teleporting out";
        script.log("JAIL", "Sailor's Amulet detected - attempting teleport");

        boolean interacted = RetryUtils.equipmentInteract(
                script, SAILORS_AMULET, "Port Roberts", "sailor's amulet teleport", 5
        );

        if (!interacted) {
            script.log("JAIL", "Failed to interact with amulet, will try lock pick");
            return false; // next poll will try lock pick
        }

        // mark that we're waiting for teleport
        waitingForTeleport = true;
        teleportStartTime = System.currentTimeMillis();
        return false; // re-evaluate next poll
    }

    /**
     * Check if teleport completed or timed out
     */
    private boolean checkTeleportProgress() {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

        // teleport successful if we're out of jail cell
        if (!JAIL_CELL.contains(myPos)) {
            script.log("JAIL", "Teleport successful! Now at " +
                    (int) myPos.getX() + ", " + (int) myPos.getY());
            waitingForTeleport = false;
            // add small delay after teleport
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(400, 800, 0.002));
            return false; // next poll will handle walking
        }

        // check for timeout (5 seconds)
        if (System.currentTimeMillis() - teleportStartTime > 5000) {
            script.log("JAIL", "Teleport timed out, falling back to lock pick");
            waitingForTeleport = false;
            return false; // next poll will try lock pick
        }

        // still waiting - add small delay
        script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(200, 400, 0.002));
        return false;
    }

    /**
     * Attempt to pick the cell door lock
     */
    private boolean attemptLockPick() {
        task = "Picking lock";

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
        boolean tapped = script.getFinger().tapGameScreen(doorPoly, "Picklock");

        if (!tapped) {
            script.log("JAIL", "Failed to send Picklock action");
            return false;
        }

        // wait for lock pick result
        script.log("JAIL", "Picklock action sent, waiting for result...");
        boolean success = script.pollFramesUntil(() -> {
            // success if: chat says "succeed" OR we're out of cell
            return chatContainsSuccess() || isOutOfCell();
        }, RandomUtils.weightedRandom(10000, 15000, 0.002));

        if (success || isOutOfCell()) {
            script.log("JAIL", "Lock picked successfully!");
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(400, 1000, 0.002));
        } else {
            script.log("JAIL", "Lock pick failed or timed out, will retry");
        }

        return false; // re-evaluate next poll
    }

    /**
     * Handle walking back to stall from outside jail cell
     */
    private boolean handleWalkingBack(WorldPosition myPos) {
        WorldPosition thievingTile = getThievingTile();
        double distance = myPos.distanceTo(thievingTile);

        // close to stall? use screen walk for precision
        if (distance < 12) {
            task = "Final approach";
            script.log("JAIL", "Close to stall, screen walking to exact tile");

            WalkConfig config = new WalkConfig.Builder()
                    .setWalkMethods(true, false) // screen only
                    .breakDistance(0)
                    .tileRandomisationRadius(0)
                    .timeout(RandomUtils.weightedRandom(8000, 12000, 0.002))
                    .build();

            script.getWalker().walkTo(thievingTile, config);
            return false; // re-evaluate next poll
        }

        // farther away - use minimap walk
        task = "Walking to stall";
        script.log("JAIL", "Walking to stall (distance: " + (int) distance + ")");

        WalkConfig config = new WalkConfig.Builder()
                .setWalkMethods(false, true) // minimap only
                .breakDistance(10) // stop when close enough for screen walk
                .tileRandomisationRadius(2)
                .timeout(RandomUtils.weightedRandom(15000, 25000, 0.002))
                .breakCondition(() -> {
                    WorldPosition pos = script.getWorldPosition();
                    if (pos == null) return false;
                    return pos.distanceTo(thievingTile) < 12;
                })
                .build();

        script.getWalker().walkTo(thievingTile, config);
        return false; // re-evaluate next poll
    }

    /**
     * Finish escape - reset state and prepare for thieving
     */
    private boolean finishEscape() {
        script.log("JAIL", "Escaped and back at stall!");
        task = "Escape complete";

        // reset escape tracking
        escapeInProgress = false;
        escapeAttempts = 0;
        waitingForTeleport = false;

        // reset thieving cycle state so first drop assumption works again
        StartThieving.resetForNewCycle();
        if (guardTracker != null) {
            guardTracker.resetCbCycle();
            guardTracker.resetGuardTracking();
            guardTracker.enableGuardSync(); // wait to see guard leave before starting
        }

        return false; // task will deactivate since we're at stall
    }

    // --- Helper methods ---

    private boolean chatContainsSuccess() {
        var chatText = script.getWidgetManager().getChatbox().getText();
        if (chatText == null) return false;

        for (String line : chatText.asList()) {
            if (line != null && line.contains("succeed")) {
                return true;
            }
        }
        return false;
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
        return (int) pos.getX() == (int) target.getX() &&
               (int) pos.getY() == (int) target.getY();
    }

    private boolean hasSailorsAmulet() {
        TabUtils.openAndWaitEquipment(script);
        UIResult<ItemSearchResult> amulet = script.getWidgetManager().getEquipment().findItem(SAILORS_AMULET);
        return amulet != null && amulet.isFound();
    }
}
