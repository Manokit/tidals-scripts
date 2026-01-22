package tasks;

import com.osmb.api.script.Script;
import main.TidalsChompyHunter;
import utils.Task;

/**
 * handles scheduled hop/break preparation by draining ground toads first.
 * activates when ProfileManager signals a hop/break is due, waits for:
 * 1. all ground toads to be consumed by chompies
 * 2. all corpses to be plucked (if plucking enabled) or timeout
 * then executes the hop/break and resets state.
 */
public class PrepareForHop extends Task {

    // static flag to gate toad placement tasks
    public static volatile boolean hopPending = false;

    // timeout for corpse draining (corpses despawn after ~30s)
    private static final long CORPSE_DRAIN_TIMEOUT_MS = 35000;

    // cooldown to prevent re-entry spam
    private static final long CHECK_COOLDOWN_MS = 2000;
    private static long lastCheckTime = 0;

    // stabilization delay after hop - gives OSMB time to identify our position
    private static final int POST_HOP_STABILIZATION_MS = 3000;

    public PrepareForHop(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // only activate after setup
        if (!TidalsChompyHunter.setupComplete) {
            return false;
        }

        // already processing a hop
        if (hopPending) {
            return true;  // keep processing
        }

        // cooldown
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < CHECK_COOLDOWN_MS) {
            return false;
        }
        lastCheckTime = now;

        // check if hop profile is configured
        try {
            if (!script.getProfileManager().hasHopProfile()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // check if due to hop or break
        try {
            if (script.getProfileManager().isDueToHop() || script.getProfileManager().isDueToBreak()) {
                script.log(getClass(), "hop/break due - starting drain sequence");
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    @Override
    public boolean execute() {
        // set pending flag immediately to block toad tasks
        hopPending = true;

        try {
            // wait for ground toads to be consumed before hopping
            TidalsChompyHunter.task = "draining toads...";
            script.log(getClass(), "waiting for toads to drain");

            boolean toadsDrained = waitForToadsDrained();
            if (!toadsDrained) {
                script.log(getClass(), "toad drain timed out, proceeding anyway");
            }

            // wait for corpses to be plucked (if plucking enabled) or timeout
            if (TidalsChompyHunter.pluckingEnabled) {
                TidalsChompyHunter.task = "plucking corpses...";
                boolean corpsesDrained = waitForCorpsesDrained();
                if (!corpsesDrained) {
                    script.log(getClass(), "corpse drain timed out, proceeding anyway");
                }
            }

            // execute the hop/break
            TidalsChompyHunter.task = "hopping worlds...";
            script.log(getClass(), "draining complete, executing hop");

            try {
                if (script.getProfileManager().isDueToBreak()) {
                    script.getProfileManager().forceBreak();
                    script.log(getClass(), "forced break");
                } else {
                    script.getProfileManager().forceHop();
                    script.log(getClass(), "forced hop");
                }
            } catch (Exception e) {
                script.log(getClass(), "hop/break error: " + e.getMessage());
            }

            // set hop timestamp for grace period (skips occupied check while position stabilizes)
            DetectPlayers.lastHopTimestamp = System.currentTimeMillis();

            // wait for OSMB to stabilize and identify our position
            // prevents Setup from seeing our own dot as "another player"
            script.log(getClass(), "waiting for position stabilization...");
            script.submitTask(() -> false, POST_HOP_STABILIZATION_MS);

            // reset state for new world
            resetStateForNewWorld();

            return true;
        } finally {
            // clear pending flag
            hopPending = false;
        }
    }

    /**
     * wait for all ground toads to be consumed (droppedToadPositions empty)
     */
    private boolean waitForToadsDrained() {
        // max wait 60s for toads to drain
        long timeout = 60000;
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeout) {
            int groundToads = TidalsChompyHunter.droppedToadPositions.size();
            script.log(getClass(), "toads remaining: " + groundToads);

            if (groundToads == 0) {
                return true;
            }

            // small delay between checks
            script.submitTask(() -> false, 1000);
        }

        return false;
    }

    /**
     * wait for all corpses to be plucked (corpsePositions empty) or timeout
     */
    private boolean waitForCorpsesDrained() {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < CORPSE_DRAIN_TIMEOUT_MS) {
            int corpses = TidalsChompyHunter.corpsePositions.size();
            script.log(getClass(), "corpses remaining: " + corpses);

            if (corpses == 0) {
                return true;
            }

            // small delay between checks
            script.submitTask(() -> false, 1000);
        }

        return false;
    }

    /**
     * reset state for clean restart on new world
     */
    private void resetStateForNewWorld() {
        // clear crash flag
        DetectPlayers.crashDetected = false;

        // reset ground toad counter
        TidalsChompyHunter.groundToadCount = 0;

        // clear tracked positions (toads/corpses lost on world change)
        TidalsChompyHunter.droppedToadPositions.clear();
        TidalsChompyHunter.corpsePositions.clear();

        // reset state flags
        TidalsChompyHunter.bellowsEmpty = false;
        TidalsChompyHunter.toadAlreadyPlaced = false;

        // reset player tracking
        DetectPlayers.resetTrackingState();

        // reset AttackChompy state
        AttackChompy.resetAllState();

        // mark setup incomplete for fresh validation
        TidalsChompyHunter.setupComplete = false;

        script.log(getClass(), "state reset for new world");
    }

    /**
     * reset static state (for script restart)
     */
    public static void resetState() {
        hopPending = false;
        lastCheckTime = 0;
    }
}
