package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.input.MenuEntry;
import com.osmb.api.ui.chatbox.Chatbox;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.PixelCluster;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static main.TidalsGemMiner.*;

// gem rock mining XP
import main.TidalsGemMiner;

public class Mine extends Task {

    private static final String TARGET_OBJECT_NAME = "Gem rocks";
    private static final long STUCK_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes
    private static final String NO_ORE_MESSAGE = "there is currently no ore available in this rock";

    // gem rock color detection (different colors per mine)
    private static final int GEM_ROCK_COLOR_UPPER = -7990908;
    private static final int GEM_ROCK_COLOR_UNDERGROUND = -9105036;
    private static final int COLOR_TOLERANCE = 10;
    private static final int CLUSTER_MAX_DISTANCE = 5;
    private static final int CLUSTER_MIN_SIZE = 20;

    // stuck detection
    private long lastSuccessfulAction = 0;

    // track empty rocks in upper mine (positions that gave "no ore" message)
    private Set<WorldPosition> emptyRockPositions = new HashSet<>();
    private int consecutiveNoOreCount = 0;

    public Mine(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // don't activate if deposit box is open
        if (script.getWidgetManager().getDepositBox().isVisible()) {
            return false;
        }
        // only activate when setup is done and inventory is not full
        return setupDone && !isInventoryFull();
    }

    @Override
    public boolean execute() {
        task = "Mining";

        // initialize stuck timer on first run
        if (lastSuccessfulAction == 0) {
            lastSuccessfulAction = System.currentTimeMillis();
        }

        // stuck detection - stop if no successful action for too long
        long timeSinceSuccess = System.currentTimeMillis() - lastSuccessfulAction;
        if (timeSinceSuccess > STUCK_TIMEOUT_MS) {
            script.log(getClass(), "stuck for " + (timeSinceSuccess / 1000) + " seconds, stopping script");
            task = "STUCK - Stopping";
            script.stop();
            return false;
        }

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) {
            return false;
        }

        boolean isUpperMine = selectedLocation.name().equals("upper");

        // get positions where respawn circles are visible (depleted rocks)
        List<WorldPosition> respawnCircles = getRespawnCirclePositions();

        // find available gem rocks that are interactable and not depleted
        List<RSObject> gemRocks = script.getObjectManager().getObjects(obj ->
                obj != null &&
                obj.isInteractableOnScreen() &&
                obj.getName() != null &&
                obj.getName().equalsIgnoreCase(TARGET_OBJECT_NAME) &&
                obj.getActions() != null &&
                Arrays.asList(obj.getActions()).contains("Mine") &&
                !respawnCircles.contains(obj.getWorldPosition()) &&
                // for upper mine, also filter out rocks we know are empty (distance-based check)
                (!isUpperMine || !isPositionMarkedEmpty(obj.getWorldPosition()))
        );

        // if no rocks from ObjectManager, try color detection as fallback
        if (gemRocks == null || gemRocks.isEmpty()) {
            script.log(getClass(), "no available gem rocks from object manager, trying color detection");

            List<Point> gemRockPixels = findGemRocksByColor();
            if (!gemRockPixels.isEmpty()) {
                script.log(getClass(), "found " + gemRockPixels.size() + " gem rock clusters by color");
                // click the nearest cluster center
                Point nearest = findNearestPoint(gemRockPixels, myPos);
                if (nearest != null) {
                    task = "Mining (color)";
                    script.log(getClass(), "clicking gem rock at " + nearest.x + "," + nearest.y);
                    boolean clicked = script.getFinger().tap(new Rectangle(nearest.x - 5, nearest.y - 5, 10, 10), "Mine");
                    if (clicked) {
                        // wait and check for "no ore" message
                        script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(2000, 6000, 0.002));
                        if (checkForNoOreMessage()) {
                            script.log(getClass(), "color-detected rock was empty");
                        } else {
                            // might have worked, wait for mining
                            waitForMiningCompletion(null);
                            consecutiveNoOreCount = 0;
                        }
                    }
                    return false;
                }
            }

            // no rocks visible at all - check if we should walk to mining area or hop
            if (!selectedLocation.miningArea().contains(myPos)) {
                task = "Walking to mine";
                script.log(getClass(), "no rocks visible, walking to mining area");
                script.getWalker().walkTo(selectedLocation.minePosition(), new WalkConfig.Builder().build());
                return false;
            }

            // in mining area but no rocks - wait or hop
            if (isUpperMine) {
                task = "Hopping worlds";
                script.log(getClass(), "upper mine depleted, hopping worlds");
                emptyRockPositions.clear(); // reset for new world
                consecutiveNoOreCount = 0;
                script.getProfileManager().forceHop();
            } else {
                // underground has many rocks - just wait for respawn
                task = "Waiting for respawn";
            }
            return false;
        }

        task = "Mining";

        // sort by distance to player (nearest first)
        gemRocks.sort(Comparator.comparingDouble(o -> {
            WorldPosition pos = o.getWorldPosition();
            return pos != null ? pos.distanceTo(myPos) : Double.MAX_VALUE;
        }));

        RSObject targetRock = gemRocks.get(0);
        if (targetRock == null) {
            return false;
        }

        // wait for player to be idle before interacting
        if (!waitForPlayerIdle()) {
            return false;
        }

        // tap the rock to mine it
        if (!tapGemRock(targetRock)) {
            // brief delay to prevent spam clicking on failed taps
            script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(300, 1000, 0.002));
            return false;
        }

        // wait for mining to complete
        WorldPosition rockPos = targetRock.getWorldPosition();
        boolean mined = waitForMiningCompletion(rockPos);

        // check for "no ore" message (upper mine only)
        if (isUpperMine && checkForNoOreMessage()) {
            script.log(getClass(), "rock was empty (no ore message), marking position: " + rockPos);
            if (rockPos != null) {
                emptyRockPositions.add(rockPos);
                script.log(getClass(), "empty positions now: " + emptyRockPositions.size());
            }
            consecutiveNoOreCount++;
            script.log(getClass(), "consecutive no ore count: " + consecutiveNoOreCount);
            // wait before trying next rock to avoid spam clicking
            script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(800, 2400, 0.002));
            return false;
        }

        // upper mine: if mining failed without "no ore" message, still mark as suspicious
        // this handles cases where the rock is visually empty but ObjectManager reports it as valid
        if (isUpperMine && !mined && rockPos != null) {
            script.log(getClass(), "mining failed without message, marking position suspicious: " + rockPos);
            emptyRockPositions.add(rockPos);
            consecutiveNoOreCount++;
            // wait before trying next rock to avoid spam clicking
            script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(600, 2000, 0.002));
            return false;
        }

        if (mined) {
            gemsMined++;
            lastSuccessfulAction = System.currentTimeMillis();
            consecutiveNoOreCount = 0;
            // clear empty positions on successful mine (rocks may have respawned)
            if (isUpperMine && !emptyRockPositions.isEmpty()) {
                emptyRockPositions.clear();
                script.log(getClass(), "cleared empty rock positions after successful mine");
            }
            // add mining XP (65 XP per gem rock)
            if (TidalsGemMiner.xpTracking != null) {
                TidalsGemMiner.xpTracking.addMiningXp(65.0);
            }
            script.log(getClass(), "mined gem rock, total: " + gemsMined);
        }

        return false; // re-evaluate state
    }

    private boolean isInventoryFull() {
        var inventoryComponent = script.getWidgetManager().getInventory();
        if (inventoryComponent == null) {
            return false;
        }
        ItemGroupResult inventory = inventoryComponent.search(Collections.emptySet());
        return inventory != null && inventory.isFull();
    }

    private List<WorldPosition> getRespawnCirclePositions() {
        try {
            List<Rectangle> respawnCircles = script.getPixelAnalyzer().findRespawnCircles();
            return script.getUtils().getWorldPositionForRespawnCircles(respawnCircles, 20);
        } catch (RuntimeException e) {
            script.log(getClass(), "error getting respawn circles: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean waitForPlayerIdle() {
        Timer stationaryTimer = new Timer();
        WorldPosition[] lastPosition = { script.getWorldPosition() };

        return script.pollFramesUntil(() -> {
            WorldPosition current = script.getWorldPosition();
            if (current == null) {
                return false;
            }

            // reset timer if player moved
            if (lastPosition[0] == null || !current.equals(lastPosition[0])) {
                lastPosition[0] = current;
                stationaryTimer.reset();
            }

            boolean stationary = stationaryTimer.timeElapsed() > 250;
            boolean animating = script.getPixelAnalyzer().isPlayerAnimating(0.4);
            return stationary && !animating;
        }, 4_000);
    }

    private boolean waitForMiningCompletion(WorldPosition targetPos) {
        return script.pollFramesUntil(() -> {
            // check if inventory is full
            ItemGroupResult inventory = script.getWidgetManager().getInventory().search(Collections.emptySet());
            if (inventory != null && inventory.isFull()) {
                return true;
            }

            // check if respawn circle appeared at target position
            List<WorldPosition> respawnCircles = getRespawnCirclePositions();
            if (targetPos != null && respawnCircles.contains(targetPos)) {
                return true;
            }

            return false;
        }, RandomUtils.uniformRandom(6000, 8000));
    }

    private boolean tapGemRock(RSObject rock) {
        if (rock == null) {
            return false;
        }
        Polygon hull = script.getSceneProjector().getConvexHull(rock);
        if (hull == null || hull.numVertices() == 0) {
            return false;
        }
        // shrink hull to 0.7 for more reliable clicks
        Polygon shrunk = hull.getResized(0.7);
        Polygon targetHull = shrunk != null ? shrunk : hull;

        // use direct tap instead of tapGetResponse to actually click the rock
        return script.getFinger().tap(targetHull, "Mine");
    }

    /**
     * checks if a position is near any marked empty position (within 1 tile)
     */
    private boolean isPositionMarkedEmpty(WorldPosition pos) {
        if (pos == null || emptyRockPositions.isEmpty()) {
            return false;
        }
        for (WorldPosition emptyPos : emptyRockPositions) {
            if (emptyPos != null && pos.distanceTo(emptyPos) <= 1.5) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks chatbox for "no ore available" message
     */
    private boolean checkForNoOreMessage() {
        try {
            Chatbox chatbox = script.getWidgetManager().getChatbox();
            if (chatbox == null) {
                return false;
            }

            UIResultList<String> currentLines = chatbox.getText();
            if (currentLines == null || currentLines.isEmpty()) {
                return false;
            }

            // check for messages containing "no ore" (check first few lines - most recent)
            int checked = 0;
            for (String line : currentLines) {
                if (checked >= 3) break; // only check recent messages
                if (line != null && line.toLowerCase().contains(NO_ORE_MESSAGE)) {
                    return true;
                }
                checked++;
            }

            return false;
        } catch (RuntimeException e) {
            script.log(getClass(), "error checking chatbox: " + e.getMessage());
            return false;
        }
    }

    /**
     * finds gem rocks by color detection (fallback when ObjectManager fails)
     */
    private List<Point> findGemRocksByColor() {
        try {
            // use different color based on mine location
            boolean isUpperMine = selectedLocation.name().equals("upper");
            int rockColor = isUpperMine ? GEM_ROCK_COLOR_UPPER : GEM_ROCK_COLOR_UNDERGROUND;

            SingleThresholdComparator tolerance = new SingleThresholdComparator(COLOR_TOLERANCE);
            SearchablePixel gemColor = new SearchablePixel(rockColor, tolerance, ColorModel.RGB);

            PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                    CLUSTER_MAX_DISTANCE,
                    CLUSTER_MIN_SIZE,
                    new SearchablePixel[]{gemColor}
            );

            // search within mining area bounds on screen
            PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(null, query);

            if (result == null) {
                return Collections.emptyList();
            }

            List<PixelCluster> clusters = result.getClusters();
            if (clusters == null || clusters.isEmpty()) {
                return Collections.emptyList();
            }

            // get center points of valid clusters
            List<Point> centers = new ArrayList<>();
            for (PixelCluster cluster : clusters) {
                if (cluster.getPoints().size() >= CLUSTER_MIN_SIZE) {
                    centers.add(cluster.getCenter());
                }
            }

            return centers;
        } catch (RuntimeException e) {
            script.log(getClass(), "error in color detection: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * finds the nearest screen point to player position
     */
    private Point findNearestPoint(List<Point> points, WorldPosition playerPos) {
        if (points == null || points.isEmpty()) {
            return null;
        }

        // get player screen position
        Polygon playerPoly = script.getSceneProjector().getTileCube(playerPos, 0);
        if (playerPoly == null) {
            return points.get(0); // fallback to first point
        }

        Rectangle playerBounds = playerPoly.getBounds();
        int playerScreenX = playerBounds.x + playerBounds.width / 2;
        int playerScreenY = playerBounds.y + playerBounds.height / 2;

        Point nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Point p : points) {
            double dist = Math.sqrt(Math.pow(p.x - playerScreenX, 2) + Math.pow(p.y - playerScreenY, 2));
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }

        return nearest;
    }

}
