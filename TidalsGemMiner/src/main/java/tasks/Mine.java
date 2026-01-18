package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.input.MenuEntry;
import com.osmb.api.utils.timing.Timer;
import utils.Task;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static main.TidalsGemMiner.*;

// gem rock mining XP
import main.TidalsGemMiner;

public class Mine extends Task {

    private static final String TARGET_OBJECT_NAME = "Gem rocks";
    private static final long STUCK_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

    // stuck detection
    private long lastSuccessfulAction = 0;

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
                !respawnCircles.contains(obj.getWorldPosition())
        );

        // handle no rocks available
        if (gemRocks == null || gemRocks.isEmpty()) {
            script.log(getClass(), "no available gem rocks");
            if (selectedLocation.name().equals("upper")) {
                // upper mine has limited rocks - world hop when depleted
                task = "Hopping worlds";
                script.log(getClass(), "upper mine depleted, hopping worlds");
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
            return false;
        }

        // wait for mining to complete
        WorldPosition rockPos = targetRock.getWorldPosition();
        boolean mined = waitForMiningCompletion(rockPos);
        if (mined) {
            gemsMined++;
            lastSuccessfulAction = System.currentTimeMillis();
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

        return script.submitTask(() -> {
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
        return script.submitTask(() -> {
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
        }, script.random(6_000, 8_000));
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

        return script.submitHumanTask(() -> {
            MenuEntry response = script.getFinger().tapGetResponse(false, targetHull);
            if (response == null) {
                return false;
            }
            String action = response.getAction();
            String name = response.getEntityName();
            return action != null && name != null &&
                    "mine".equalsIgnoreCase(action) &&
                    TARGET_OBJECT_NAME.equalsIgnoreCase(name);
        }, 2_000);
    }

}
