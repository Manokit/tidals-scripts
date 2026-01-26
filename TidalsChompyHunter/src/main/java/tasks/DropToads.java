package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.area.impl.PolyArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.visual.PixelCluster;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import com.osmb.api.walker.WalkConfig;
import main.TidalsChompyHunter;
import utils.Task;

import java.util.ArrayList;
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
    private static final int TARGET_GROUND_TOADS = 5;

    // bloated toad ground detection - for finding untracked toads
    private static final SearchablePixel BLOATED_TOAD_GROUND = new SearchablePixel(
            -8215240,  // RGB int for bloated toad on ground
            new SingleThresholdComparator(2),
            ColorModel.RGB
    );
    private static final int BLOATED_TOAD_CLUSTER_DISTANCE = 5;
    private static final int BLOATED_TOAD_CLUSTER_MIN_SIZE = 3;

    public DropToads(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // CRITICAL: don't activate if crash detected - let HopWorld handle it
        if (DetectPlayers.crashDetected) {
            return false;
        }

        // only activate after setup is complete
        if (!TidalsChompyHunter.setupComplete) {
            return false;
        }

        // don't place new toads when hop/break is due - let them drain
        if (script.getProfileManager().isDueToHop() || script.getProfileManager().isDueToBreak()) {
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
        // CRITICAL: abort immediately if crash detected
        if (DetectPlayers.crashDetected) {
            script.log(getClass(), "ABORTING - crash detected, yielding to HopWorld");
            return false;
        }

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
            // CRITICAL: abort if crash detected
            if (DetectPlayers.crashDetected) {
                script.log(getClass(), "crash detected - stopping drops, yielding to HopWorld");
                return false;
            }

            // INTERRUPT: check for live chompy spawn before each drop (filters out corpses)
            // only interrupt if we have ownership claim - otherwise that chompy isn't ours
            if (TidalsChompyHunter.hasOwnershipClaim() && AttackChompy.hasLiveChompy(script)) {
                script.log(getClass(), "chompy detected - stopping drops to attack");
                break;  // exit loop, return true below
            }

            // check if next drop position has a corpse - move away first
            if (isNextDropPositionBlocked()) {
                script.log(getClass(), "corpse at next drop position - moving to new location");
                walkToNewPosition();
                retries++;
                continue;
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

                // no collision - ALWAYS find the toad visually before proceeding
                // wait for toad to be visible on ground before searching
                script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(800, 1200, 1000, 100));

                script.log(getClass(), "drop succeeded - searching for toad to track...");

                // search around lastDropPosition (where we were standing), not current position
                WorldPosition foundToad = findUntrackedToadNearby(lastDropPosition);
                if (foundToad != null) {
                    TidalsChompyHunter.droppedToadPositions.put(foundToad, System.currentTimeMillis());
                    TidalsChompyHunter.lastToadPresentTime = System.currentTimeMillis();
                    script.log(getClass(), "TRACKED toad at " + foundToad.getX() + "," + foundToad.getY() +
                            " (" + TidalsChompyHunter.droppedToadPositions.size() + " total tracked)");
                } else {
                    // couldn't find toad - try lastDropPosition as fallback
                    if (lastDropPosition != null) {
                        TidalsChompyHunter.droppedToadPositions.put(lastDropPosition, System.currentTimeMillis());
                        TidalsChompyHunter.lastToadPresentTime = System.currentTimeMillis();
                        script.log(getClass(), "FALLBACK: tracked at lastDropPosition " + lastDropPosition.getX() + "," + lastDropPosition.getY());
                    } else {
                        script.log(getClass(), "ERROR: could not find or track toad - this drop is lost");
                    }
                }

                dropped++;
                TidalsChompyHunter.groundToadCount++;
                script.log(getClass(), "dropped toad " + dropped + "/" + toDrop + " (ground count: " + TidalsChompyHunter.groundToadCount + ")");

                // wait for game to auto-move player after drop (creates straight line of toads)
                if (dropped < toDrop) {
                    script.log(getClass(), "waiting for auto-move...");
                    script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(900, 1100));
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

        // precise walk - minimap only, no screen walking
        WalkConfig config = new WalkConfig.Builder()
                .setWalkMethods(false, true)
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
     * check if the next toad drop position has a known dead chompy
     * player drops toad 1 tile east, so check (player.x + 1, player.y)
     * @return true if corpse blocking, false if clear to drop
     */
    private boolean isNextDropPositionBlocked() {
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) return false;

        // toad lands 1 tile east of player
        WorldPosition nextToadPos = new WorldPosition(playerPos.getX() + 1, playerPos.getY(), 0);

        // check if this position is in ignoredPositionTimestamps (known corpse)
        int posKey = AttackChompy.posKey(nextToadPos);
        if (!AttackChompy.isPositionIgnored(posKey)) {
            return false; // not a known corpse position
        }

        // verify corpse is actually still there via pixel detection
        if (AttackChompy.isCorpseVisibleAt(script, nextToadPos)) {
            script.log(getClass(), "corpse blocking next drop at " + nextToadPos.getX() + "," + nextToadPos.getY());
            return true;
        }

        // position was ignored but corpse despawned - clean up the stale ignore
        AttackChompy.removeIgnoredPosition(posKey);
        script.log(getClass(), "cleared stale ignore at " + nextToadPos.getX() + "," + nextToadPos.getY());
        return false;
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

        // precise walk - minimap only, no screen walking
        WalkConfig config = new WalkConfig.Builder()
                .setWalkMethods(false, true)
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

        // capture position BEFORE drop - toad lands exactly where player is standing
        WorldPosition preDropPos = script.getWorldPosition();
        if (preDropPos == null) {
            script.log(getClass(), "PRE-DROP position null - aborting");
            return false;
        }
        script.log(getClass(), "PRE-DROP position (toad will land here): " + preDropPos.getX() + "," + preDropPos.getY());

        boolean dropped = item.interact("Drop");
        if (dropped) {
            // toad position is WHERE WE WERE when we dropped - capture it now before any movement
            lastDropPosition = new WorldPosition(preDropPos.getX(), preDropPos.getY(), 0);
            script.log(getClass(), "TRACKED position (pre-drop capture): " + lastDropPosition.getX() + "," + lastDropPosition.getY());

            // wait for drop animation + auto-walk to complete before returning
            // this ensures player has moved away so toad is visible for verification
            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(2000, 3000, 2500, 300));

            // wait for player to fully stop moving
            waitForPlayerToStop(5, 3000);

            WorldPosition postDropPos = script.getWorldPosition();
            script.log(getClass(), "POST-DROP position (player moved to): " + (postDropPos != null ? postDropPos.getX() + "," + postDropPos.getY() : "null"));

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

    /**
     * search nearby tiles for an untracked bloated toad using pixel detection
     * @param centerPos position to search around (where toad was dropped)
     * @return position of untracked toad, or null if none found
     */
    private WorldPosition findUntrackedToadNearby(WorldPosition centerPos) {
        // use provided center, fall back to player position
        WorldPosition searchCenter = centerPos;
        if (searchCenter == null) {
            searchCenter = script.getWorldPosition();
        }
        if (searchCenter == null) {
            script.log(getClass(), "no search center available");
            return null;
        }

        script.log(getClass(), "searching for toad around " + searchCenter.getX() + "," + searchCenter.getY());

        // search a 5x5 area centered on drop position
        List<WorldPosition> tilesToCheck = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                tilesToCheck.add(new WorldPosition(searchCenter.getX() + dx, searchCenter.getY() + dy, 0));
            }
        }

        // check each tile for bloated toad pixel cluster
        for (WorldPosition tile : tilesToCheck) {
            // skip already tracked positions
            if (TidalsChompyHunter.droppedToadPositions.containsKey(tile)) {
                continue;
            }

            // get tile polygon on screen
            Polygon tilePoly = script.getSceneProjector().getTileCube(tile, 40);
            if (tilePoly == null) {
                continue;
            }

            // search for bloated toad pixels on this tile
            PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                    BLOATED_TOAD_CLUSTER_DISTANCE,
                    BLOATED_TOAD_CLUSTER_MIN_SIZE,
                    new SearchablePixel[]{BLOATED_TOAD_GROUND}
            );

            PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(tilePoly, query);
            if (result != null && !result.getClusters().isEmpty()) {
                script.log(getClass(), "found untracked toad at " + tile.getX() + "," + tile.getY() +
                        " (cluster size: " + result.getClusters().get(0).getPoints().size() + ")");
                return tile;
            }
        }

        return null;
    }
}
