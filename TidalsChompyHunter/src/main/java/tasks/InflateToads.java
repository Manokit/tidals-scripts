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

    public InflateToads(Script script) {
        super(script);
    }

    /**
     * check if verbose logging is enabled
     */
    private boolean isVerbose() {
        return TidalsChompyHunter.VERBOSE_LOGGING;
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
            return false;
        }

        int currentInventory = countBloatedToads();
        int currentGround = countActiveGroundToads();

        // determine mode based on ground toads
        boolean stockpileMode = currentGround >= TARGET_GROUND_TOADS;

        if (stockpileMode) {
            TidalsChompyHunter.task = "stockpiling toads";
            script.log(getClass(), "stockpile mode: " + currentGround + " on ground, " + currentInventory + " in inv");
        } else {
            TidalsChompyHunter.task = "inflating toads";
            script.log(getClass(), "refill mode: " + currentGround + " on ground, " + currentInventory + " in inv");
        }

        // calculate how many toads we can/should inflate
        int maxToInflate;
        if (stockpileMode) {
            // stockpile: fill inventory up to max
            maxToInflate = MAX_INVENTORY_TOADS - currentInventory;
        } else {
            // refill: get ground toads back up to target
            int groundNeeded = TARGET_GROUND_TOADS - currentGround;
            // also consider inventory space for extras
            int invSpace = MAX_INVENTORY_TOADS - currentInventory;
            maxToInflate = Math.min(groundNeeded + invSpace, groundNeeded + 1); // at least enough for ground + 1
        }

        script.log(getClass(), "will inflate up to " + maxToInflate + " toads");

        // loop: find and inflate toads
        for (int i = 0; i < maxToInflate; i++) {
            // CRITICAL: abort if crash detected
            if (DetectPlayers.crashDetected) {
                script.log(getClass(), "crash detected - stopping inflation, yielding to HopWorld");
                return false;
            }

            // INTERRUPT: check for live chompy spawn (filters out corpses)
            // only interrupt if we have ownership claim - otherwise that chompy isn't ours
            if (TidalsChompyHunter.hasOwnershipClaim() && AttackChompy.hasLiveChompy(script)) {
                script.log(getClass(), "chompy detected - stopping inflation early");
                return true;
            }

            // re-check inventory count
            int invCount = countBloatedToads();
            if (invCount >= MAX_INVENTORY_TOADS) {
                script.log(getClass(), "inventory full (" + invCount + " toads)");
                break;
            }

            boolean inflated = findAndInflateToad();
            if (!inflated) {
                script.log(getClass(), "failed to inflate toad");
                return false;
            }

            script.log(getClass(), "inflated toad " + (i + 1) + "/" + maxToInflate);
        }

        script.log(getClass(), "finished inflating toads");
        return true;
    }

    /**
     * find a swamp toad and inflate it using pixel cluster detection
     * re-detects toad positions FRESH before each attempt to handle movement
     * checks for empty bellows chat message after each attempt
     */
    private boolean findAndInflateToad() {
        int previousCount = countBloatedToads();

        // reset bellows empty flag before starting
        TidalsChompyHunter.bellowsEmpty = false;

        // total attempts across all toads (reasonable upper bound)
        int maxTotalAttempts = MAX_INFLATE_ATTEMPTS * 3;

        for (int attempt = 1; attempt <= maxTotalAttempts; attempt++) {
            // INTERRUPT: check for live chompy spawn before each attempt (filters out corpses)
            // only interrupt if we have ownership claim - otherwise that chompy isn't ours
            if (TidalsChompyHunter.hasOwnershipClaim() && AttackChompy.hasLiveChompy(script)) {
                script.log(getClass(), "chompy detected - interrupting inflate to attack");
                return true;  // exit early, let AttackChompy activate
            }

            // early exit: check if we've reached max inventory toads
            int currentToads = countBloatedToads();
            if (currentToads >= MAX_INVENTORY_TOADS) {
                script.log(getClass(), "already have " + MAX_INVENTORY_TOADS + " toads, exiting early");
                return true;
            }

            // check if bellows are empty (from previous attempt's chat)
            if (TidalsChompyHunter.bellowsEmpty) {
                script.log(getClass(), "bellows empty detected - need to refill");
                return false;
            }

            // re-detect toad positions FRESH each attempt (fixes stale position bug)
            List<Point> clickPoints = findSwampToadClickPoints();
            if (clickPoints.isEmpty()) {
                script.log(getClass(), "no swamp toad sprites found on screen");
                return false;
            }

            // DEBUG: log all candidate click points
            if (isVerbose()) {
                WorldPosition playerPos = script.getWorldPosition();
                logVerbose("player(" + (playerPos != null ? playerPos.getX() + "," + playerPos.getY() : "null") +
                        ") " + clickPoints.size() + " candidates: " + clickPoints);
            }

            // target first/nearest toad from fresh detection
            Point clickPoint = clickPoints.get(0);
            script.log(getClass(),"inflate attempt " + attempt + "/" + maxTotalAttempts + " at " + clickPoint);

            // tap "Inflate" on fresh toad position (exact point for small sprites)
            boolean success = script.getFinger().tap(clickPoint, "Inflate");

            // DEBUG: log tap result
            if (isVerbose()) {
                logVerbose("tap " + (success ? "OK" : "FAIL") + " at " + clickPoint);
            }

            if (success) {
                script.log(getClass(),"inflate action started at " + clickPoint);

                // wait for toad to appear in inventory (handles walking + inflate animation)
                boolean gotToad = script.pollFramesUntil(() -> {
                    // check for bellows empty during wait
                    if (TidalsChompyHunter.bellowsEmpty) {
                        return true;  // exit poll early
                    }
                    return countBloatedToads() > previousCount;
                }, 8000);  // 8s timeout covers walking + animation

                // check why poll exited
                if (TidalsChompyHunter.bellowsEmpty) {
                    script.log(getClass(), "bellows empty detected - need to refill");
                    return false;
                }

                if (gotToad) {
                    script.log(getClass(), "toad inflated successfully");
                    // humanize: brief pause after picking up toad
                    script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 600));
                    return true;
                }

                script.log(getClass(), "toad did not arrive in inventory, will re-detect position");
            }

            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(300, 500));
        }

        script.log(getClass(), "failed to inflate any toad after " + maxTotalAttempts + " attempts");
        return false;
    }

    /**
     * find swamp toads on screen, prioritized by distance to player
     * uses minimap NPC positions to determine distance, then matches to sprite clusters
     * returns click points sorted by distance (closest first)
     */
    private List<Point> findSwampToadClickPoints() {
        List<Point> clickPoints = new ArrayList<>();

        // get player position
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return clickPoints;
        }

        // get NPC positions from minimap, sorted by distance to player
        UIResultList<WorldPosition> npcResult = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcResult == null || !npcResult.isFound()) {
            return clickPoints;
        }

        List<WorldPosition> sortedNpcs = npcResult.asList().stream()
                .filter(pos -> pos.distanceTo(playerPos) <= 15)  // within range
                .sorted(Comparator.comparingDouble(pos -> pos.distanceTo(playerPos)))
                .collect(Collectors.toList());

        // find all swamp toad sprite clusters on screen
        List<PixelCluster> clusters = findSwampToadClusters();
        if (clusters.isEmpty()) {
            return clickPoints;
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
            // add small random offset for humanization (+/- 3 pixels)
            int offsetX = RandomUtils.uniformRandom(-3, 4);
            int offsetY = RandomUtils.uniformRandom(-3, 4);
            Point clickPoint = new Point(
                    clusterBounds.x + clusterBounds.width / 2 + offsetX,
                    clusterBounds.y + clusterBounds.height / 2 + offsetY
            );

            // skip screen edge cases
            if (isNearScreenEdge(clickPoint)) {
                continue;
            }

            // remove used cluster to avoid duplicates
            clusters.remove(nearest);
            clickPoints.add(clickPoint);
        }

        return clickPoints;  // already sorted by NPC distance (closest first)
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
