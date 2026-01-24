package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.area.impl.PolyArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.walker.WalkConfig;
import com.osmb.api.shape.Polygon;
import main.TidalsChompyHunter;
import utils.Task;
import utilities.RetryUtils;

import java.util.List;
import java.util.Set;

public class FillBellows extends Task {

    // swamp bubble locations with their accessible stand positions
    // bubble at 2393, 3049 - can stand at x+1 or x-1
    // bubble at 2395, 3046 - can stand at x-1
    // bubble at 2396, 3047 - can stand at y+1
    // bubble at 2392, 3053 - can stand at x-1
    private static final List<BubbleLocation> BUBBLES = List.of(
        new BubbleLocation(new WorldPosition(2393, 3049, 0), new WorldPosition(2394, 3049, 0)),  // x+1
        new BubbleLocation(new WorldPosition(2393, 3049, 0), new WorldPosition(2392, 3049, 0)),  // x-1
        new BubbleLocation(new WorldPosition(2395, 3046, 0), new WorldPosition(2394, 3046, 0)),  // x-1
        new BubbleLocation(new WorldPosition(2396, 3047, 0), new WorldPosition(2396, 3048, 0)),  // y+1
        new BubbleLocation(new WorldPosition(2392, 3053, 0), new WorldPosition(2391, 3053, 0))   // x-1
    );

    // bellows item id - empty state only (we wait until none of these remain)
    private static final int OGRE_BELLOWS_EMPTY = 2871;

    // charged bellows ids (for activation check)
    private static final int OGRE_BELLOWS_3 = 2872;
    private static final int OGRE_BELLOWS_2 = 2873;
    private static final int OGRE_BELLOWS_1 = 2874;

    // toad drop area - return here after filling bellows
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

    private static final int TILE_CUBE_HEIGHT = 40;

    public FillBellows(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // CRITICAL: don't activate if crash detected - let HopWorld handle it
        if (DetectPlayers.crashDetected) {
            return false;
        }

        if (!TidalsChompyHunter.setupComplete) {
            return false;
        }

        if (AttackChompy.inCombat) {
            return false;
        }

        // trigger if bellows empty detected via chat message
        if (TidalsChompyHunter.bellowsEmpty) {
            return true;
        }

        // check if all bellows are empty
        return allBellowsEmpty();
    }

    @Override
    public boolean execute() {
        // CRITICAL: abort immediately if crash detected
        if (DetectPlayers.crashDetected) {
            script.log(getClass(), "ABORTING - crash detected, yielding to HopWorld");
            return false;
        }

        TidalsChompyHunter.task = "filling bellows";
        script.log(getClass(), "bellows empty, finding swamp bubble...");

        // get bubbles sorted by distance (nearest first)
        List<BubbleLocation> sortedBubbles = getBubblesByDistance();

        // try each bubble location until one works
        for (int i = 0; i < sortedBubbles.size(); i++) {
            // CRITICAL: abort if crash detected
            if (DetectPlayers.crashDetected) {
                script.log(getClass(), "crash detected - stopping bellows fill, yielding to HopWorld");
                return false;
            }
            BubbleLocation bubble = sortedBubbles.get(i);

            script.log(getClass(), "trying bubble " + (i + 1) + "/" + sortedBubbles.size() +
                " at " + bubble.bubblePos.getX() + "," + bubble.bubblePos.getY());

            // walk to stand position
            if (!walkToStandPosition(bubble.standPos)) {
                script.log(getClass(), "failed to reach stand position, trying next bubble");
                continue;
            }

            // check crash AFTER walk (walk can take time)
            if (DetectPlayers.crashDetected) {
                script.log(getClass(), "crash detected after walk - yielding to HopWorld");
                return false;
            }

            // try to suck bubble with RetryUtils (10 attempts, with crash break condition)
            Polygon tilePoly = script.getSceneProjector().getTileCube(bubble.bubblePos, TILE_CUBE_HEIGHT);
            if (tilePoly == null) {
                script.log(getClass(), "bubble tile not visible, trying next bubble");
                continue;
            }

            // use break condition to abort retry loop immediately if crash detected
            boolean sucked = RetryUtils.tap(script, tilePoly, "Suck", "suck swamp bubble",
                    () -> DetectPlayers.crashDetected);

            // check crash after RetryUtils returns (in case it triggered the break)
            if (DetectPlayers.crashDetected) {
                script.log(getClass(), "crash detected - yielding to HopWorld");
                return false;
            }

            if (!sucked) {
                script.log(getClass(), "failed to suck bubble, trying next location");
                continue;
            }

            // success - wait for bellows to fill
            return waitForBellowsToFill();
        }

        // all bubbles failed
        script.log(getClass(), "all bubble locations failed");
        return false;
    }

    /**
     * walk to stand position for bubble interaction
     */
    private boolean walkToStandPosition(WorldPosition standPos) {
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos != null && playerPos.distanceTo(standPos) <= 1) {
            return true; // already close enough
        }

        WalkConfig config = new WalkConfig.Builder()
                .breakDistance(0)
                .timeout(10000)
                .build();

        return script.getWalker().walkTo(standPos, config);
    }

    /**
     * wait for all bellows to fill after clicking bubble
     */
    private boolean waitForBellowsToFill() {
        script.log(getClass(), "waiting for bellows to fill...");

        boolean allFilled = script.pollFramesUntil(() -> {
            // interrupt for chompy spawn
            if (AttackChompy.hasLiveChompy(script)) {
                script.log(getClass(), "chompy detected - aborting fill");
                return true;
            }
            return countEmptyBellows() == 0;
        }, 30000);

        // check if interrupted by chompy
        if (countEmptyBellows() > 0) {
            script.log(getClass(), "interrupted - will resume later");
            return true;
        }

        if (!allFilled) {
            script.log(getClass(), "timeout waiting for bellows to fill");
            return false;
        }

        script.log(getClass(), "all bellows filled");
        TidalsChompyHunter.bellowsEmpty = false;

        // return to drop area
        walkToDropArea();

        return true;
    }

    /**
     * get bubbles sorted by distance from player (nearest first)
     */
    private List<BubbleLocation> getBubblesByDistance() {
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return BUBBLES;
        }

        return BUBBLES.stream()
            .sorted((a, b) -> {
                double distA = playerPos.distanceTo(a.standPos);
                double distB = playerPos.distanceTo(b.standPos);
                return Double.compare(distA, distB);
            })
            .toList();
    }

    /**
     * check if all bellows in inventory are empty
     */
    private boolean allBellowsEmpty() {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(
                Set.of(OGRE_BELLOWS_EMPTY, OGRE_BELLOWS_3, OGRE_BELLOWS_2, OGRE_BELLOWS_1)
        );

        if (inv == null) {
            return false;
        }

        // if any have charges, don't need to fill
        int chargedCount = inv.getAmount(OGRE_BELLOWS_3)
                + inv.getAmount(OGRE_BELLOWS_2)
                + inv.getAmount(OGRE_BELLOWS_1);

        if (chargedCount > 0) {
            return false;
        }

        // need at least one empty bellows
        return inv.getAmount(OGRE_BELLOWS_EMPTY) > 0;
    }

    /**
     * count empty bellows in inventory
     */
    private int countEmptyBellows() {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(OGRE_BELLOWS_EMPTY));
        if (inv == null) {
            return 0;
        }
        return inv.getAmount(OGRE_BELLOWS_EMPTY);
    }

    /**
     * walk back to the toad drop area
     */
    private void walkToDropArea() {
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) return;

        if (TOAD_DROP_AREA.contains(playerPos)) {
            script.log(getClass(), "already in drop area");
            return;
        }

        WorldPosition target = TOAD_DROP_AREA.getRandomPosition();
        script.log(getClass(), "returning to drop area");

        WalkConfig config = new WalkConfig.Builder()
                .breakDistance(0)
                .timeout(10000)
                .build();

        script.getWalker().walkTo(target, config);
    }

    /**
     * simple holder for bubble + stand position pair
     */
    private static class BubbleLocation {
        final WorldPosition bubblePos;
        final WorldPosition standPos;

        BubbleLocation(WorldPosition bubblePos, WorldPosition standPos) {
            this.bubblePos = bubblePos;
            this.standPos = standPos;
        }
    }
}
