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

    // wait until inventory is full before dropping (matches InflateToads.MAX_INVENTORY_TOADS)
    private static final int MAX_INVENTORY_TOADS = 3;

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

    /**
     * check if verbose logging is enabled
     */
    private boolean isVerbose() {
        return TidalsChompyHunter.verboseLogging;
    }

    /**
     * log only when verbose mode is enabled - use for detailed debug output
     */
    private void logVerbose(String message) {
        if (!isVerbose()) {
            return;
        }
        script.log(getClass(), "[DEBUG] " + message);
    }

    @Override
    public boolean activate() {
        // CRITICAL: don't activate if crash detected - let HopWorld handle it
        if (DetectPlayers.crashDetected) {
            return false;
        }

        // if state machine is mid-execution, keep it running to finish tracking
        if (dropState != DropState.WALKING_TO_AREA) {
            return true;
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

        // only drop when inventory is full - let InflateToads batch up to 3 first
        int toadCount = countBloatedToads();
        return toadCount >= MAX_INVENTORY_TOADS;
    }

    @Override
    public boolean execute() {
        // CRITICAL: abort immediately if crash detected
        if (DetectPlayers.crashDetected) {
            script.log(getClass(), "ABORTING - crash detected, yielding to HopWorld");
            resetState();
            return false;
        }

        // INTERRUPT: check for live chompy spawn (filters out corpses)
        if (TidalsChompyHunter.hasOwnershipClaim() && AttackChompy.hasLiveChompy(script)) {
            script.log(getClass(), "chompy detected - stopping drops to attack");
            resetState();
            return false;
        }

        TidalsChompyHunter.task = "dropping toads";

        // state: walk to drop area
        if (dropState == DropState.WALKING_TO_AREA) {
            if (!walkToDropArea()) {
                script.log(getClass(), "failed to reach drop area");
                resetState();
                return false;
            }
            // verify tracked positions and calculate target
            AttackChompy.verifyAllTrackedToads(script);
            int groundToads = countActiveGroundToads();
            int inventoryToads = countBloatedToads();
            toadsTarget = Math.min(inventoryToads, Math.max(0, TARGET_GROUND_TOADS - groundToads));
            script.log(getClass(), "ground: " + groundToads + ", inventory: " + inventoryToads + ", dropping: " + toadsTarget);

            if (isVerbose()) {
                StringBuilder sb = new StringBuilder("tracked toads: ");
                for (WorldPosition pos : TidalsChompyHunter.droppedToadPositions.keySet()) {
                    sb.append(pos.getX()).append(",").append(pos.getY()).append(" ");
                }
                logVerbose(sb.toString().trim());
            }

            if (toadsTarget <= 0) {
                resetState();
                return false;
            }
            dropState = DropState.CHECK_POSITION;
            return true; // re-poll
        }

        // state: check if next drop position is blocked by corpse
        if (dropState == DropState.CHECK_POSITION) {
            if (retries >= MAX_RETRIES) {
                script.log(getClass(), "max retries reached, finishing with " + toadsDropped + " dropped");
                resetState();
                return false;
            }
            if (toadsDropped >= toadsTarget || countBloatedToads() <= 0) {
                dropState = DropState.DONE;
                return true;
            }
            if (isNextDropPositionBlocked()) {
                script.log(getClass(), "corpse at next drop position - moving to new location");
                walkToNewPosition();
                retries++;
                return true; // re-poll, stay in CHECK_POSITION
            }
            // position clear - proceed to drop
            TidalsChompyHunter.toadAlreadyPlaced = false;
            dropState = DropState.DROPPING;
            return true;
        }

        // state: drop a single toad
        if (dropState == DropState.DROPPING) {
            if (dropSingleToad()) {
                // check collision
                if (TidalsChompyHunter.toadAlreadyPlaced) {
                    script.log(getClass(), "toad collision at " +
                            (lastDropPosition != null ? lastDropPosition.getX() + "," + lastDropPosition.getY() : "null") +
                            " - NOT tracking, moving to new position");
                    walkToNewPosition();
                    retries++;
                    dropState = DropState.CHECK_POSITION;
                    return true;
                }
                dropState = DropState.WAITING_FOR_TOAD;
            } else {
                script.log(getClass(), "failed to drop toad");
                retries++;
                dropState = DropState.CHECK_POSITION;
            }
            return true;
        }

        // state: confirm toad landed via menu check on the known drop tile
        if (dropState == DropState.WAITING_FOR_TOAD) {
            // brief wait for toad to appear on ground
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(400, 700));

            if (lastDropPosition != null) {
                // fast menu check - confirm "bloated toad" exists at the drop tile
                boolean confirmed = confirmToadViaMenu(lastDropPosition);
                if (confirmed) {
                    TidalsChompyHunter.droppedToadPositions.put(lastDropPosition, System.currentTimeMillis());
                    TidalsChompyHunter.lastToadPresentTime = System.currentTimeMillis();
                    script.log(getClass(), "TRACKED toad (menu confirmed) at " + lastDropPosition.getX() + "," + lastDropPosition.getY() +
                            " (" + TidalsChompyHunter.droppedToadPositions.size() + " total tracked)");
                } else {
                    // menu didn't find it - fall back to pixel scan
                    script.log(getClass(), "menu check missed - falling back to pixel scan...");
                    dropState = DropState.TRACKING;
                    return true;
                }
            } else {
                script.log(getClass(), "ERROR: no lastDropPosition - falling back to pixel scan");
                dropState = DropState.TRACKING;
                return true;
            }

            toadsDropped++;
            TidalsChompyHunter.groundToadCount++;
            script.log(getClass(), "dropped toad " + toadsDropped + "/" + toadsTarget + " (ground count: " + TidalsChompyHunter.groundToadCount + ")");

            // more to drop? wait for auto-move then loop back
            if (toadsDropped < toadsTarget && countBloatedToads() > 0) {
                script.log(getClass(), "waiting for auto-move...");
                script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(900, 1100));
                dropState = DropState.CHECK_POSITION;
                return true;
            }
            dropState = DropState.DONE;
            return true;
        }

        // state: fallback pixel scan if menu check failed
        if (dropState == DropState.TRACKING) {
            WorldPosition foundToad = findUntrackedToadNearby(lastDropPosition);
            if (foundToad != null) {
                TidalsChompyHunter.droppedToadPositions.put(foundToad, System.currentTimeMillis());
                TidalsChompyHunter.lastToadPresentTime = System.currentTimeMillis();
                script.log(getClass(), "TRACKED toad (pixel fallback) at " + foundToad.getX() + "," + foundToad.getY() +
                        " (" + TidalsChompyHunter.droppedToadPositions.size() + " total tracked)");
            } else if (lastDropPosition != null) {
                TidalsChompyHunter.droppedToadPositions.put(lastDropPosition, System.currentTimeMillis());
                TidalsChompyHunter.lastToadPresentTime = System.currentTimeMillis();
                script.log(getClass(), "FALLBACK: tracked at lastDropPosition " + lastDropPosition.getX() + "," + lastDropPosition.getY());
            } else {
                script.log(getClass(), "ERROR: could not find or track toad - this drop is lost");
            }

            toadsDropped++;
            TidalsChompyHunter.groundToadCount++;
            script.log(getClass(), "dropped toad " + toadsDropped + "/" + toadsTarget + " (ground count: " + TidalsChompyHunter.groundToadCount + ")");

            // more to drop? wait for auto-move then loop back
            if (toadsDropped < toadsTarget && countBloatedToads() > 0) {
                script.log(getClass(), "waiting for auto-move...");
                script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(900, 1100));
                dropState = DropState.CHECK_POSITION;
                return true;
            }
            dropState = DropState.DONE;
            return true;
        }

        // state: cleanup
        if (dropState == DropState.DONE) {
            script.log(getClass(), "finished dropping " + toadsDropped + " toads");
            boolean didWork = toadsDropped > 0;
            resetState();
            return didWork;
        }

        return false;
    }

    /**
     * walk to the toad drop area if not already there
     */
    private boolean walkToDropArea() {
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return false;
        }

        script.log(getClass(),"current position: " + playerPos.getX() + "," + playerPos.getY());

        // check if already in drop area
        if (TOAD_DROP_AREA.contains(playerPos)) {
            script.log(getClass(), "already in drop area at " + playerPos.getX() + "," + playerPos.getY());
            return true;
        }

        WorldPosition target = TOAD_DROP_AREA.getRandomPosition();
        script.log(getClass(), "walking to drop area target: " + target.getX() + "," + target.getY());

        // precise walk - minimap only, no screen walking
        // no breakCondition - let walker reach the actual target tile
        // old breakCondition fired on polygon entry (edge tile) causing first toad to drop far from target
        WalkConfig config = new WalkConfig.Builder()
                .setWalkMethods(false, true)
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .timeout(RandomUtils.weightedRandom(6000, 10000, 0.002))
                .build();

        boolean arrived = script.getWalker().walkTo(target, config);

        // wait for player to fully stop at the target before dropping
        waitForPlayerToStop(3, RandomUtils.gaussianRandom(2000, 3000, 2500, 250));

        WorldPosition finalPos = script.getWorldPosition();
        script.log(getClass(),"walk complete, final position: " +
                (finalPos != null ? finalPos.getX() + "," + finalPos.getY() : "null") +
                " (target was " + target.getX() + "," + target.getY() + ")");

        // verify we actually made it into the drop area
        if (finalPos != null && !TOAD_DROP_AREA.contains(finalPos)) {
            script.log(getClass(), "ended outside drop area - walk may have failed");
            return false;
        }

        return arrived || (finalPos != null && TOAD_DROP_AREA.contains(finalPos));
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
                .timeout(RandomUtils.gaussianRandom(2500, 3500, 3000, 250))
                .build();

        script.getWalker().walkTo(target, config);

        // log final position
        WorldPosition finalPos = script.getWorldPosition();
        script.log(getClass(),"step complete, now at: " +
                (finalPos != null ? finalPos.getX() + "," + finalPos.getY() : "null"));
    }

    // -- poll-based state machine fields --
    private enum DropState {
        WALKING_TO_AREA,
        CHECK_POSITION,
        DROPPING,
        WAITING_FOR_TOAD,
        TRACKING,
        DONE
    }

    private DropState dropState = DropState.WALKING_TO_AREA;
    private int toadsDropped = 0;
    private int toadsTarget = 0;
    private int retries = 0;
    private static final int MAX_RETRIES = 10;

    // stores the last drop position for tracking after collision check
    private WorldPosition lastDropPosition = null;

    /**
     * reset state machine for a fresh drop cycle
     */
    private void resetState() {
        dropState = DropState.WALKING_TO_AREA;
        toadsDropped = 0;
        toadsTarget = 0;
        retries = 0;
        lastDropPosition = null;
    }

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
        script.log(getClass(),"PRE-DROP position (toad will land here): " + preDropPos.getX() + "," + preDropPos.getY());

        boolean dropped = item.interact("Drop");
        if (dropped) {
            // toad position is WHERE WE WERE when we dropped - capture it now before any movement
            lastDropPosition = new WorldPosition(preDropPos.getX(), preDropPos.getY(), 0);
            script.log(getClass(),"TRACKED position (pre-drop capture): " + lastDropPosition.getX() + "," + lastDropPosition.getY());

            // wait for drop animation + auto-walk to complete before returning
            // this ensures player has moved away so toad is visible for verification
            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(2000, 3000, 2500, 300));

            // wait for player to fully stop moving
            waitForPlayerToStop(5, RandomUtils.gaussianRandom(2500, 3500, 3000, 250));

            WorldPosition postDropPos = script.getWorldPosition();
            script.log(getClass(),"POST-DROP position (player moved to): " + (postDropPos != null ? postDropPos.getX() + "," + postDropPos.getY() : "null"));

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
    /**
     * fast toad confirmation via menu entry check on the known drop tile
     * opens right-click menu at the tile and checks if any entry contains "bloated toad"
     * returns null from the MenuHook to cancel without selecting anything
     */
    // radius for the small circle used to tap near the center of a tile for ground item menu checks
    private static final int TOAD_MENU_TAP_RADIUS = 3;

    private boolean confirmToadViaMenu(WorldPosition toadPos) {
        Polygon tileCube = script.getSceneProjector().getTileCube(toadPos, 40);
        if (tileCube == null) {
            script.log(getClass(), "menu check: tileCube null at " + toadPos.getX() + "," + toadPos.getY());
            return false;
        }

        // tight box around tile center - bloated toad is small and centered on the tile
        com.osmb.api.shape.Rectangle bounds = tileCube.getBounds();
        if (bounds == null) {
            return false;
        }
        int cx = bounds.x + bounds.width / 2;
        int cy = bounds.y + bounds.height / 2;
        com.osmb.api.shape.Rectangle tapArea = new com.osmb.api.shape.Rectangle(
                cx - TOAD_MENU_TAP_RADIUS, cy - TOAD_MENU_TAP_RADIUS,
                TOAD_MENU_TAP_RADIUS * 2, TOAD_MENU_TAP_RADIUS * 2
        );

        // side-effect flag - lambda sets this if bloated toad found in menu
        final boolean[] found = {false};

        try {
            script.getFinger().tapGameScreen(tapArea, menuEntries -> {
                for (var entry : menuEntries) {
                    String raw = entry.getRawText();
                    if (raw != null && raw.toLowerCase().contains("bloated toad")) {
                        found[0] = true;
                        break;
                    }
                }
                // always return null - we're just peeking, don't select anything
                return null;
            });
        } catch (RuntimeException e) {
            script.log(getClass(), "menu check failed: " + e.getMessage());
        }

        if (!found[0]) {
            script.log(getClass(), "menu check: no 'bloated toad' entry at " + toadPos.getX() + "," + toadPos.getY());
        }

        return found[0];
    }

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

        script.log(getClass(),"searching for toad around " + searchCenter.getX() + "," + searchCenter.getY());

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
                script.log(getClass(),"found untracked toad at " + tile.getX() + "," + tile.getY() +
                        " (cluster size: " + result.getClusters().get(0).getPoints().size() + ")");
                return tile;
            }
        }

        return null;
    }
}
