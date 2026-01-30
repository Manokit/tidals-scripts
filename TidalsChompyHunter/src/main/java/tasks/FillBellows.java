package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.area.impl.PolyArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.walker.WalkConfig;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.RandomUtils;
import main.TidalsChompyHunter;
import utils.Task;
import utilities.RetryUtils;

import java.util.List;
import java.util.Set;

public class FillBellows extends Task {

    // state machine for poll-based execution
    private enum State {
        FIND_BUBBLE,
        WALK_TO_BUBBLE,
        INTERACT_WITH_BUBBLE,
        WAIT_FOR_FILL,
        RETURN_TO_AREA
    }

    // swamp bubble locations with their accessible stand positions
    private static final List<BubbleLocation> BUBBLES = List.of(
        new BubbleLocation(new WorldPosition(2393, 3049, 0), new WorldPosition(2394, 3049, 0)),  // x+1
        new BubbleLocation(new WorldPosition(2393, 3049, 0), new WorldPosition(2392, 3049, 0)),  // x-1
        new BubbleLocation(new WorldPosition(2395, 3046, 0), new WorldPosition(2394, 3046, 0)),  // x-1
        new BubbleLocation(new WorldPosition(2396, 3047, 0), new WorldPosition(2396, 3048, 0)),  // y+1
        new BubbleLocation(new WorldPosition(2392, 3053, 0), new WorldPosition(2391, 3053, 0))   // x-1
    );

    private static final int OGRE_BELLOWS_EMPTY = 2871;
    private static final int OGRE_BELLOWS_3 = 2872;
    private static final int OGRE_BELLOWS_2 = 2873;
    private static final int OGRE_BELLOWS_1 = 2874;

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

    // state tracking across polls
    private State state = State.FIND_BUBBLE;
    private List<BubbleLocation> sortedBubbles;
    private int bubbleIndex = 0;

    // keeps FillBellows active across multiple suck cycles until all bellows are full
    private boolean fillingInProgress = false;

    public FillBellows(Script script) {
        super(script);
    }

    private void resetState() {
        state = State.FIND_BUBBLE;
        sortedBubbles = null;
        bubbleIndex = 0;
        fillingInProgress = false;
        fillWaitPolls = 0;
    }

    @Override
    public boolean activate() {
        if (DetectPlayers.crashDetected) {
            resetState();
            return false;
        }

        if (!TidalsChompyHunter.setupComplete) {
            return false;
        }

        if (AttackChompy.inCombat) {
            // don't activate during combat, but keep fillingInProgress
            // so we resume filling after the chompy is dealt with
            return false;
        }

        // stay active while mid-fill cycle (multiple sucks needed)
        if (fillingInProgress) {
            return true;
        }

        if (TidalsChompyHunter.bellowsEmpty) {
            return true;
        }

        return allBellowsEmpty();
    }

    @Override
    public boolean execute() {
        if (DetectPlayers.crashDetected) {
            script.log(getClass(), "ABORTING - crash detected, yielding to HopWorld");
            resetState();
            return false;
        }

        TidalsChompyHunter.task = "filling bellows";

        switch (state) {
            case FIND_BUBBLE:
                return handleFindBubble();
            case WALK_TO_BUBBLE:
                return handleWalkToBubble();
            case INTERACT_WITH_BUBBLE:
                return handleInteract();
            case WAIT_FOR_FILL:
                return handleWaitForFill();
            case RETURN_TO_AREA:
                return handleReturnToArea();
            default:
                resetState();
                return false;
        }
    }

    private boolean handleFindBubble() {
        script.log(getClass(), "bellows empty, finding swamp bubble...");
        fillingInProgress = true;
        sortedBubbles = getBubblesByDistance();
        bubbleIndex = 0;
        state = State.WALK_TO_BUBBLE;
        return true;
    }

    private boolean handleWalkToBubble() {
        if (bubbleIndex >= sortedBubbles.size()) {
            script.log(getClass(), "all bubble locations failed");
            resetState();
            return false;
        }

        BubbleLocation bubble = sortedBubbles.get(bubbleIndex);
        script.log(getClass(), "trying bubble " + (bubbleIndex + 1) + "/" + sortedBubbles.size() +
            " at " + bubble.bubblePos.getX() + "," + bubble.bubblePos.getY());

        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos != null && playerPos.distanceTo(bubble.standPos) <= 1) {
            // already close enough, move to interact
            state = State.INTERACT_WITH_BUBBLE;
            return true;
        }

        WalkConfig config = new WalkConfig.Builder()
                .setWalkMethods(false, true)
                .breakDistance(0)
                .timeout(RandomUtils.weightedRandom(8000, 12000, 0.002))
                .build();

        boolean walked = script.getWalker().walkTo(bubble.standPos, config);
        if (!walked) {
            script.log(getClass(), "failed to reach stand position, trying next bubble");
            bubbleIndex++;
            // stay in WALK_TO_BUBBLE to try next
            return true;
        }

        state = State.INTERACT_WITH_BUBBLE;
        return true;
    }

    private boolean handleInteract() {
        if (DetectPlayers.crashDetected) {
            resetState();
            return false;
        }

        BubbleLocation bubble = sortedBubbles.get(bubbleIndex);
        Polygon tilePoly = script.getSceneProjector().getTileCube(bubble.bubblePos, TILE_CUBE_HEIGHT);
        if (tilePoly == null) {
            script.log(getClass(), "bubble tile not visible, trying next bubble");
            bubbleIndex++;
            state = State.WALK_TO_BUBBLE;
            return true;
        }

        boolean sucked = RetryUtils.tapGameScreen(script, tilePoly, "Suck", "suck swamp bubble",
                () -> DetectPlayers.crashDetected);

        if (DetectPlayers.crashDetected) {
            resetState();
            return false;
        }

        if (!sucked) {
            script.log(getClass(), "failed to suck bubble, trying next location");
            bubbleIndex++;
            state = State.WALK_TO_BUBBLE;
            return true;
        }

        state = State.WAIT_FOR_FILL;
        return true;
    }

    private static final int MAX_FILL_WAIT_POLLS = 15; // safety limit to avoid infinite wait
    private int fillWaitPolls = 0;

    private boolean handleWaitForFill() {
        // wait each poll for the game to auto-fill bellows (one suck fills all, just takes time)
        script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(1500, 2500, 2000, 200));
        fillWaitPolls++;

        // check for chompy interrupt
        if (AttackChompy.hasLiveChompy(script)) {
            script.log(getClass(), "chompy detected - pausing fill cycle");
            // keep fillingInProgress so we resume after combat
            state = State.FIND_BUBBLE;
            fillWaitPolls = 0;
            return true;
        }

        int remaining = countEmptyBellows();
        if (remaining > 0) {
            if (fillWaitPolls >= MAX_FILL_WAIT_POLLS) {
                script.log(getClass(), "timeout waiting for bellows to fill (" + remaining + " still empty)");
                fillWaitPolls = 0;
                resetState();
                return false;
            }
            // still filling - stay in WAIT_FOR_FILL, don't re-suck
            script.log(getClass(), remaining + " empty bellows remaining - waiting... (" + fillWaitPolls + "/" + MAX_FILL_WAIT_POLLS + ")");
            return true;
        }

        fillWaitPolls = 0;

        script.log(getClass(), "all bellows filled");
        TidalsChompyHunter.bellowsEmpty = false;
        state = State.RETURN_TO_AREA;
        return true;
    }

    private boolean handleReturnToArea() {
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos != null && TOAD_DROP_AREA.contains(playerPos)) {
            script.log(getClass(), "already in drop area");
            resetState();
            return true;
        }

        // brief post-fill delay before walking
        script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(1800, 2400, 2100, 150));

        WorldPosition target = TOAD_DROP_AREA.getRandomPosition();
        script.log(getClass(), "returning to drop area");

        WalkConfig config = new WalkConfig.Builder()
                .setWalkMethods(false, true)
                .breakDistance(0)
                .timeout(RandomUtils.weightedRandom(8000, 12000, 0.002))
                .build();

        script.getWalker().walkTo(target, config);
        resetState();
        return true;
    }

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

    private boolean allBellowsEmpty() {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(
                Set.of(OGRE_BELLOWS_EMPTY, OGRE_BELLOWS_3, OGRE_BELLOWS_2, OGRE_BELLOWS_1)
        );

        if (inv == null) {
            return false;
        }

        int chargedCount = inv.getAmount(OGRE_BELLOWS_3)
                + inv.getAmount(OGRE_BELLOWS_2)
                + inv.getAmount(OGRE_BELLOWS_1);

        if (chargedCount > 0) {
            return false;
        }

        return inv.getAmount(OGRE_BELLOWS_EMPTY) > 0;
    }

    private int countEmptyBellows() {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(OGRE_BELLOWS_EMPTY));
        if (inv == null) {
            return 0;
        }
        return inv.getAmount(OGRE_BELLOWS_EMPTY);
    }

    private static class BubbleLocation {
        final WorldPosition bubblePos;
        final WorldPosition standPos;

        BubbleLocation(WorldPosition bubblePos, WorldPosition standPos) {
            this.bubblePos = bubblePos;
            this.standPos = standPos;
        }
    }
}
