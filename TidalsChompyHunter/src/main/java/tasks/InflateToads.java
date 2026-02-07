package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.visual.PixelCluster;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import main.TidalsChompyHunter;
import utils.Task;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InflateToads extends Task {

    // bloated toad in inventory
    private static final int BLOATED_TOAD = 2875;

    // constants
    private static final int MAX_INFLATE_ATTEMPTS = 5;
    private static final int TARGET_GROUND_TOADS = 5;  // keep this many on ground
    private static final int MIN_GROUND_TOADS = 2;  // refill when below this
    private static final int MAX_INVENTORY_TOADS = 3;  // stockpile up to this in inventory
    private static final int TILE_CUBE_HEIGHT = 40;

    // bloated toad sprite color detection (unhighlighted - uses actual sprite color)
    // this distinguishes bloated toads from highlighted swamp toads
    // public for visual verification in AttackChompy
    public static final SearchablePixel BLOATED_TOAD_SPRITE = new SearchablePixel(
            -8346826,  // RGB int from user testing
            new SingleThresholdComparator(2),
            ColorModel.RGB
    );
    public static final int BLOATED_CLUSTER_MAX_DISTANCE = 10;
    public static final int BLOATED_CLUSTER_MIN_SIZE = 4;

    // swamp toad sprite color detection (pixel cluster approach)
    private static final SearchablePixel SWAMP_TOAD_SPRITE = new SearchablePixel(
            -14286849,  // RGB int from Debug Tool
            new SingleThresholdComparator(5),
            ColorModel.RGB
    );
    private static final int SWAMP_TOAD_CLUSTER_MAX_DISTANCE = 10;
    private static final int SWAMP_TOAD_CLUSTER_MIN_SIZE = 10;

    // screen edge margin - clusters too close to edge cause tap() to fail
    private static final int SCREEN_EDGE_MARGIN = 25;

    // -- poll-based state machine --
    private enum InflateState {
        CALCULATE,          // determine how many to inflate
        FIND_TOAD,          // detect toad positions, select target
        TAP_TOAD,           // send inflate action
        WAIT_FOR_PICKUP,    // poll until toad in inventory or timeout
        CHECK_COMPLETION,   // verify count, decide if done
        DONE                // cleanup
    }

    private InflateState inflateState = InflateState.CALCULATE;
    private int toadsInflated = 0;
    private int toadsTarget = 0;
    private int attemptCount = 0;
    private int maxTotalAttempts = 0;
    private int previousToadCount = 0;
    private Rectangle currentClickTarget = null;

    private void resetState() {
        inflateState = InflateState.CALCULATE;
        toadsInflated = 0;
        toadsTarget = 0;
        attemptCount = 0;
        maxTotalAttempts = 0;
        previousToadCount = 0;
        currentClickTarget = null;
    }

    public InflateToads(Script script) {
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

        // only activate after setup is complete
        if (!TidalsChompyHunter.setupComplete) {
            return false;
        }

        // don't inflate new toads when hop/break is due - let ground drain
        if (script.getProfileManager().isDueToHop() || script.getProfileManager().isDueToBreak()) {
            return false;
        }

        // don't interrupt combat
        if (AttackChompy.inCombat) {
            return false;
        }

        // verify tracked positions before counting (removes consumed toads)
        AttackChompy.verifyAllTrackedToads(script);

        // count current toads
        int inventoryToads = countBloatedToads();
        int groundToads = countActiveGroundToads();

        // refill mode: inflate when ground toads below target AND inventory has space
        // covers groundToads 0-4 (anything below target of 5)
        if (groundToads < TARGET_GROUND_TOADS && inventoryToads < MAX_INVENTORY_TOADS) {
            return true;
        }

        // stockpile mode: keep inflating when ground is at target and inventory has space
        if (groundToads >= TARGET_GROUND_TOADS && inventoryToads < MAX_INVENTORY_TOADS) {
            return true;
        }

        return false;
    }

    @Override
    public boolean execute() {
        // CRITICAL: abort immediately if crash detected
        if (DetectPlayers.crashDetected) {
            script.log(getClass(), "ABORTING - crash detected, yielding to HopWorld");
            resetState();
            return false;
        }

        // INTERRUPT: check for live chompy spawn
        if (TidalsChompyHunter.hasOwnershipClaim() && AttackChompy.hasLiveChompy(script)) {
            script.log(getClass(), "chompy detected - stopping inflation");
            resetState();
            return false;
        }

        // check bellows empty at top of every poll
        if (TidalsChompyHunter.bellowsEmpty) {
            script.log(getClass(), "bellows empty detected - need to refill");
            resetState();
            return false;
        }

        // state: calculate how many to inflate
        if (inflateState == InflateState.CALCULATE) {
            int currentInventory = countBloatedToads();
            int currentGround = countActiveGroundToads();
            boolean stockpileMode = currentGround >= TARGET_GROUND_TOADS;

            if (stockpileMode) {
                TidalsChompyHunter.task = "stockpiling toads";
                script.log(getClass(), "stockpile mode: " + currentGround + " on ground, " + currentInventory + " in inv");
                toadsTarget = MAX_INVENTORY_TOADS - currentInventory;
            } else {
                TidalsChompyHunter.task = "inflating toads";
                script.log(getClass(), "refill mode: " + currentGround + " on ground, " + currentInventory + " in inv");
                int groundNeeded = TARGET_GROUND_TOADS - currentGround;
                int invSpace = MAX_INVENTORY_TOADS - currentInventory;
                toadsTarget = Math.min(groundNeeded + invSpace, groundNeeded + 1);
            }

            script.log(getClass(), "will inflate up to " + toadsTarget + " toads");
            if (toadsTarget <= 0) {
                resetState();
                return false;
            }

            maxTotalAttempts = MAX_INFLATE_ATTEMPTS * 3;
            TidalsChompyHunter.bellowsEmpty = false;
            inflateState = InflateState.FIND_TOAD;
            return true;
        }

        // state: detect toad positions and select target
        if (inflateState == InflateState.FIND_TOAD) {
            // check if done or exhausted attempts
            if (toadsInflated >= toadsTarget || countBloatedToads() >= MAX_INVENTORY_TOADS) {
                inflateState = InflateState.DONE;
                return true;
            }
            if (attemptCount >= maxTotalAttempts) {
                script.log(getClass(), "max attempts reached");
                inflateState = InflateState.DONE;
                return true;
            }

            previousToadCount = countBloatedToads();

            List<Rectangle> clickTargets = findSwampToadClickTargets();
            if (clickTargets.isEmpty()) {
                script.log(getClass(), "no swamp toad sprites found on screen");
                resetState();
                return false;
            }

            if (isVerbose()) {
                WorldPosition playerPos = script.getWorldPosition();
                logVerbose("player(" + (playerPos != null ? playerPos.getX() + "," + playerPos.getY() : "null") +
                        ") " + clickTargets.size() + " candidates");
            }

            currentClickTarget = clickTargets.get(0);
            inflateState = InflateState.TAP_TOAD;
            return true;
        }

        // state: send inflate action
        if (inflateState == InflateState.TAP_TOAD) {
            attemptCount++;
            script.log(getClass(), "inflate attempt " + attemptCount + "/" + maxTotalAttempts +
                    " at (" + currentClickTarget.x + "," + currentClickTarget.y + ")");

            boolean success = script.getFinger().tapGameScreen(currentClickTarget, "Inflate");

            if (isVerbose()) {
                logVerbose("tapGameScreen " + (success ? "OK" : "FAIL"));
            }

            if (success) {
                script.log(getClass(), "inflate action started at (" + currentClickTarget.x + "," + currentClickTarget.y + ")");
                inflateState = InflateState.WAIT_FOR_PICKUP;
            } else {
                // failed tap - retry with fresh detection
                script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(300, 500));
                inflateState = InflateState.FIND_TOAD;
            }
            return true;
        }

        // state: wait for toad to appear in inventory
        if (inflateState == InflateState.WAIT_FOR_PICKUP) {
            boolean gotToad = script.pollFramesUntil(() -> {
                if (TidalsChompyHunter.bellowsEmpty) {
                    return true;
                }
                return countBloatedToads() > previousToadCount;
            }, RandomUtils.gaussianRandom(7000, 9000, 8000, 500));

            if (TidalsChompyHunter.bellowsEmpty) {
                script.log(getClass(), "bellows empty detected - need to refill");
                resetState();
                return false;
            }

            inflateState = InflateState.CHECK_COMPLETION;
            if (gotToad) {
                script.log(getClass(), "toad inflated successfully");
                script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(200, 600));
                toadsInflated++;
            } else {
                script.log(getClass(), "toad did not arrive in inventory, will re-detect position");
            }
            return true;
        }

        // state: check if we should continue or finish
        if (inflateState == InflateState.CHECK_COMPLETION) {
            if (toadsInflated >= toadsTarget || countBloatedToads() >= MAX_INVENTORY_TOADS) {
                inflateState = InflateState.DONE;
            } else if (attemptCount >= maxTotalAttempts) {
                script.log(getClass(), "max attempts reached");
                inflateState = InflateState.DONE;
            } else {
                // more toads needed - loop back to find next
                inflateState = InflateState.FIND_TOAD;
            }
            return true;
        }

        // state: cleanup
        if (inflateState == InflateState.DONE) {
            script.log(getClass(), "finished inflating " + toadsInflated + " toads");
            boolean didWork = toadsInflated > 0;
            resetState();
            return didWork;
        }

        return false;
    }

    /**
     * find swamp toads on screen, prioritized by distance to player
     * uses minimap NPC positions to determine distance, then matches to sprite clusters
     * returns click target rectangles sorted by distance (closest first)
     */
    private List<Rectangle> findSwampToadClickTargets() {
        List<Rectangle> clickTargets = new ArrayList<>();

        // get player position
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return clickTargets;
        }

        // get NPC positions from minimap, sorted by distance to player
        UIResultList<WorldPosition> npcResult = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcResult == null || !npcResult.isFound()) {
            return clickTargets;
        }

        List<WorldPosition> sortedNpcs = npcResult.asList().stream()
                .filter(pos -> pos.distanceTo(playerPos) <= 15)  // within range
                .sorted(Comparator.comparingDouble(pos -> pos.distanceTo(playerPos)))
                .collect(Collectors.toList());

        // find all swamp toad sprite clusters on screen
        List<PixelCluster> clusters = findSwampToadClusters();
        if (clusters.isEmpty()) {
            return clickTargets;
        }

        script.log(getClass(),"found " + clusters.size() + " swamp toad sprites, " + sortedNpcs.size() + " NPCs nearby");

        // match NPCs to clusters by screen proximity (closest NPCs first)
        for (WorldPosition npcPos : sortedNpcs) {
            Polygon tileCube = script.getSceneProjector().getTileCube(npcPos, TILE_CUBE_HEIGHT);
            if (tileCube == null) {
                continue;
            }

            Rectangle npcBounds = tileCube.getBounds();
            int npcScreenX = npcBounds.x + npcBounds.width / 2;
            int npcScreenY = npcBounds.y + npcBounds.height / 2;

            // find nearest cluster to this NPC's screen position
            PixelCluster nearest = findNearestCluster(clusters, npcScreenX, npcScreenY);
            if (nearest == null) {
                continue;
            }

            Rectangle clusterBounds = nearest.getBounds();

            // skip screen edge cases (check center point)
            Point centerPoint = new Point(
                    clusterBounds.x + clusterBounds.width / 2,
                    clusterBounds.y + clusterBounds.height / 2
            );
            if (isNearScreenEdge(centerPoint)) {
                continue;
            }

            // remove used cluster to avoid duplicates
            clusters.remove(nearest);
            // return cluster bounds as click target - tapGameScreen handles humanization
            clickTargets.add(clusterBounds);
        }

        return clickTargets;  // already sorted by NPC distance (closest first)
    }

    /**
     * find all swamp toad sprite clusters on screen
     */
    private List<PixelCluster> findSwampToadClusters() {
        PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                SWAMP_TOAD_CLUSTER_MAX_DISTANCE,
                SWAMP_TOAD_CLUSTER_MIN_SIZE,
                new SearchablePixel[]{SWAMP_TOAD_SPRITE}
        );

        PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(null, query);
        if (result == null) {
            return new ArrayList<>();
        }

        List<PixelCluster> clusters = result.getClusters();
        return clusters != null ? new ArrayList<>(clusters) : new ArrayList<>();
    }

    /**
     * find nearest cluster to screen position
     * returns null if no cluster within max distance (50px)
     */
    private PixelCluster findNearestCluster(List<PixelCluster> clusters, int screenX, int screenY) {
        PixelCluster nearest = null;
        double nearestDist = 50;  // max screen distance to match NPC to cluster

        for (PixelCluster cluster : clusters) {
            Rectangle bounds = cluster.getBounds();
            int cx = bounds.x + bounds.width / 2;
            int cy = bounds.y + bounds.height / 2;

            double dist = Math.sqrt(Math.pow(screenX - cx, 2) + Math.pow(screenY - cy, 2));
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = cluster;
            }
        }

        return nearest;
    }

    /**
     * check if point is too close to screen edge for tap()
     */
    private boolean isNearScreenEdge(Point p) {
        return p.x < SCREEN_EDGE_MARGIN || p.y < SCREEN_EDGE_MARGIN ||
               p.x > 750 - SCREEN_EDGE_MARGIN || p.y > 700 - SCREEN_EDGE_MARGIN;
    }

    /**
     * count bloated toads on ground using tracked drop positions
     * does NOT verify - just returns tracked count
     * verification happens separately when chompy spawns
     */
    public static int countBloatedToadsOnGround(Script script) {
        return TidalsChompyHunter.droppedToadPositions.size();
    }

    /**
     * instance method for backwards compatibility
     */
    public int countActiveGroundToads() {
        return countBloatedToadsOnGround(script);
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
}
