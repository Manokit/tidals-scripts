package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.area.impl.PolyArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.walker.WalkConfig;
import main.TidalsChompyHunter;
import utils.Task;

import java.util.List;
import java.util.Set;

public class DropToads extends Task {

    // bloated toad item id
    private static final int BLOATED_TOAD = 2875;

    // toad drop area - polygon with 17 positions for collision avoidance
    private static final PolyArea TOAD_DROP_AREA = new PolyArea(List.of(
        new WorldPosition(2391, 3044, 0),
        new WorldPosition(2389, 3043, 0),
        new WorldPosition(2385, 3046, 0),
        new WorldPosition(2388, 3047, 0),
        new WorldPosition(2390, 3046, 0),
        new WorldPosition(2391, 3046, 0),
        new WorldPosition(2392, 3046, 0),
        new WorldPosition(2393, 3045, 0),
        new WorldPosition(2393, 3044, 0),
        new WorldPosition(2392, 3044, 0),
        new WorldPosition(2390, 3044, 0),
        new WorldPosition(2388, 3043, 0),
        new WorldPosition(2387, 3043, 0),
        new WorldPosition(2386, 3043, 0),
        new WorldPosition(2385, 3044, 0),
        new WorldPosition(2385, 3045, 0),
        new WorldPosition(2386, 3044, 0)
    ));

    // target number of toads on ground
    private static final int TARGET_GROUND_TOADS = 3;

    public DropToads(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // only activate after setup is complete
        if (!TidalsChompyHunter.setupComplete) {
            return false;
        }

        // don't place toads when hop/break is pending - let them drain
        if (PrepareForHop.hopPending) {
            return false;
        }

        // don't interrupt combat
        if (AttackChompy.inCombat) {
            return false;
        }

        // verify tracked positions before counting (removes consumed toads)
        AttackChompy.verifyAllTrackedToads(script);

        // check if ground needs more toads
        int visualGroundCount = countActiveGroundToads();
        if (visualGroundCount >= TARGET_GROUND_TOADS) {
            return false;  // ground is full, don't activate
        }

        // check if we have bloated toads to drop
        int toadCount = countBloatedToads();
        return toadCount > 0;
    }

    @Override
    public boolean execute() {
        TidalsChompyHunter.task = "dropping toads";

        // walk to drop area first
        if (!walkToDropArea()) {
            script.log(getClass(), "failed to reach drop area");
            return false;
        }

        // verify tracked positions before calculating (removes consumed toads)
        AttackChompy.verifyAllTrackedToads(script);

        // only drop enough to reach target (3) on ground
        int groundToads = countActiveGroundToads();
        int inventoryToads = countBloatedToads();
        int toDrop = Math.min(inventoryToads, Math.max(0, TARGET_GROUND_TOADS - groundToads));

        script.log(getClass(), "ground: " + groundToads + ", inventory: " + inventoryToads + ", dropping: " + toDrop);

        // drop all toads needed, handling collisions
        int dropped = 0;
        int maxRetries = 10;  // prevent infinite loop
        int retries = 0;

        while (dropped < toDrop && countBloatedToads() > 0 && retries < maxRetries) {
            // INTERRUPT: check for live chompy spawn before each drop (filters out corpses)
            if (AttackChompy.hasLiveChompy(script)) {
                script.log(getClass(), "chompy detected - stopping drops to attack");
                break;  // exit loop, return true below
            }

            // reset collision flag before drop attempt
            TidalsChompyHunter.toadAlreadyPlaced = false;

            if (dropSingleToad()) {
                // check if collision occurred (chat message detected during drop wait)
                if (TidalsChompyHunter.toadAlreadyPlaced) {
                    script.log(getClass(), "toad collision at " +
                            (lastDropPosition != null ? lastDropPosition.getX() + "," + lastDropPosition.getY() : "null") +
                            " - NOT tracking, moving to new position");
                    walkToNewPosition();
                    retries++;
                    continue;  // retry drop without incrementing dropped
                }

                // no collision - NOW track the position
                if (lastDropPosition != null) {
                    TidalsChompyHunter.droppedToadPositions.add(lastDropPosition);
                    script.log(getClass(), "tracking toad at " + lastDropPosition.getX() + "," + lastDropPosition.getY() +
                            " (" + TidalsChompyHunter.droppedToadPositions.size() + " tracked)");
                }

                dropped++;
                TidalsChompyHunter.groundToadCount++;
                script.log(getClass(), "dropped toad " + dropped + "/" + toDrop + " (ground count: " + TidalsChompyHunter.groundToadCount + ")");

                // wait for game to auto-move player after drop (creates straight line of toads)
                if (dropped < toDrop) {
                    script.log(getClass(), "waiting for auto-move...");
                    script.submitTask(() -> false, script.random(1800, 2200));
                }
            } else {
                script.log(getClass(), "failed to drop toad");
                retries++;
            }
        }

        script.log(getClass(), "finished dropping " + dropped + " toads");
        return dropped > 0;
    }

    /**
     * walk to the toad drop area if not already there
     */
    private boolean walkToDropArea() {
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return false;
        }

        script.log(getClass(), "current position: " + playerPos.getX() + "," + playerPos.getY());

        // check if already in drop area
        if (TOAD_DROP_AREA.contains(playerPos)) {
            script.log(getClass(), "already in drop area at " + playerPos.getX() + "," + playerPos.getY());
            return true;
        }

        WorldPosition target = TOAD_DROP_AREA.getRandomPosition();
        script.log(getClass(), "walking to drop area target: " + target.getX() + "," + target.getY());

        // precise walk - no randomization, stop exactly at target
        WalkConfig config = new WalkConfig.Builder()
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .timeout(8000)
                .breakCondition(() -> {
                    WorldPosition pos = script.getWorldPosition();
                    return pos != null && TOAD_DROP_AREA.contains(pos);
                })
                .build();

        boolean arrived = script.getWalker().walkTo(target, config);

        // log final position
        WorldPosition finalPos = script.getWorldPosition();
        script.log(getClass(), "walk complete, final position: " +
                (finalPos != null ? finalPos.getX() + "," + finalPos.getY() : "null") +
                " (target was " + target.getX() + "," + target.getY() + ")");

        return arrived;
    }

    /**
     * walk to a new random position in the drop area (for collision avoidance)
     */
    private void walkToNewPosition() {
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) return;

        WorldPosition target = TOAD_DROP_AREA.getRandomPosition();

        script.log(getClass(), "stepping from " + playerPos.getX() + "," + playerPos.getY() +
                " to " + target.getX() + "," + target.getY());

        // skip if already at target
        if (playerPos.equals(target)) {
            script.log(getClass(), "already at target position");
            return;
        }

        // precise walk - no randomization
        WalkConfig config = new WalkConfig.Builder()
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .timeout(3000)
                .build();

        script.getWalker().walkTo(target, config);

        // log final position
        WorldPosition finalPos = script.getWorldPosition();
        script.log(getClass(), "step complete, now at: " +
                (finalPos != null ? finalPos.getX() + "," + finalPos.getY() : "null"));
    }

    // stores the last drop position for tracking after collision check
    private WorldPosition lastDropPosition = null;

    /**
     * drop a single bloated toad from inventory
     * stores position in lastDropPosition but does NOT track - caller must track after collision check
     */
    private boolean dropSingleToad() {
        lastDropPosition = null;

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(BLOATED_TOAD));
        if (inv == null || !inv.contains(BLOATED_TOAD)) {
            return false;
        }

        // get the item
        ItemSearchResult item = inv.getItem(BLOATED_TOAD);
        if (item == null) {
            return false;
        }

        WorldPosition preDropPos = script.getWorldPosition();
        script.log(getClass(), "PRE-DROP position: " + (preDropPos != null ? preDropPos.getX() + "," + preDropPos.getY() : "null"));

        boolean dropped = item.interact("Drop");
        if (dropped) {
            // wait for drop animation to complete
            script.pollFramesUntil(() -> false, script.random(600, 900));

            // CRITICAL: wait for player to be FULLY stopped AFTER drop animation
            // player may still be moving from walker momentum during animation
            waitForPlayerToStop(5, 3000);

            // capture position AFTER animation and movement complete - this is where toad actually landed
            WorldPosition postDropPos = script.getWorldPosition();
            script.log(getClass(), "POST-DROP position (player): " + (postDropPos != null ? postDropPos.getX() + "," + postDropPos.getY() : "null"));

            // toad lands one tile to the right of player position
            if (postDropPos != null) {
                lastDropPosition = new WorldPosition(postDropPos.getX() + 1, postDropPos.getY(), 0);
                script.log(getClass(), "TRACKED position (toad tile): " + lastDropPosition.getX() + "," + lastDropPosition.getY());
            } else {
                lastDropPosition = null;
            }
            return true;
        }

        return false;
    }

    /**
     * wait for player to stop moving by checking position stability
     * @param stableChecks number of consecutive stable position checks required
     * @param timeout max time to wait in ms
     */
    private void waitForPlayerToStop(int stableChecks, int timeout) {
        final WorldPosition[] lastPos = {null};
        final int[] stableCount = {0};

        script.pollFramesUntil(() -> {
            WorldPosition currentPos = script.getWorldPosition();
            if (currentPos == null) return false;

            if (lastPos[0] != null && currentPos.equals(lastPos[0])) {
                stableCount[0]++;
                if (stableCount[0] >= stableChecks) {
                    return true;
                }
            } else {
                stableCount[0] = 0;
            }
            lastPos[0] = currentPos;
            return false;
        }, timeout);
    }

    /**
     * count bloated toads in inventory
     */
    private int countBloatedToads() {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(BLOATED_TOAD));
        if (inv == null) {
            return 0;
        }
        return inv.getAmount(BLOATED_TOAD);
    }

    /**
     * count active bloated toads on ground using sprite detection
     */
    private int countActiveGroundToads() {
        return InflateToads.countBloatedToadsOnGround(script);
    }
}
