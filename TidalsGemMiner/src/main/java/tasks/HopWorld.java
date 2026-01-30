package tasks;

import com.osmb.api.script.Script;
import com.osmb.api.utils.RandomUtils;
import main.TidalsGemMiner;
import utils.Task;

/**
 * responds to crash detection by hopping worlds and resetting state
 * runs at highest priority to handle crashDetected flag immediately
 */
public class HopWorld extends Task {

    // cooldown range between consecutive hops (7-10 seconds) - randomized per check
    private static final long HOP_COOLDOWN_MIN_MS = 7000;
    private static final long HOP_COOLDOWN_MAX_MS = 10000;
    private static long lastHopTime = 0;

    // stabilization delay range after hop (8-12s) - randomized per hop
    private static final int POST_HOP_STABILIZATION_MIN_MS = 8000;
    private static final int POST_HOP_STABILIZATION_MAX_MS = 12000;

    // guard flag to prevent re-entry during hop
    private static volatile boolean isHopping = false;

    public HopWorld(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // prevent re-entry while hop is in progress
        if (isHopping) {
            return false;
        }

        // only activate when crash detected
        if (!DetectPlayers.crashDetected) {
            return false;
        }

        // check cooldown to prevent rapid hop loops (randomized)
        long now = System.currentTimeMillis();
        long hopCooldown = RandomUtils.gaussianRandom((int) HOP_COOLDOWN_MIN_MS, (int) HOP_COOLDOWN_MAX_MS, (HOP_COOLDOWN_MIN_MS + HOP_COOLDOWN_MAX_MS) / 2.0, (HOP_COOLDOWN_MAX_MS - HOP_COOLDOWN_MIN_MS) / 4.0);
        if (now - lastHopTime < hopCooldown) {
            return false;
        }

        return true;
    }

    @Override
    public boolean execute() {
        // set guard flag immediately
        isHopping = true;

        // CRITICAL: reset tracking state IMMEDIATELY before hop
        // this clears the timer so it doesn't persist to the new world
        DetectPlayers.resetTrackingState();

        // set hop timestamp for grace period
        DetectPlayers.lastHopTimestamp = System.currentTimeMillis();
        lastHopTime = System.currentTimeMillis();

        // mark setup incomplete so it re-runs after hop
        TidalsGemMiner.setupDone = false;

        try {
            TidalsGemMiner.task = "hopping worlds...";
            script.log(getClass(), "hopping due to crash/occupied world");

            // check if hop profile configured
            if (!script.getProfileManager().hasHopProfile()) {
                script.log(getClass(), "no hop profile configured - stopping script");
                script.stop();
                return false;
            }

            // initiate hop - forceHop() blocks until world load complete
            script.getProfileManager().forceHop();

            // refresh timestamps after hop completes
            DetectPlayers.lastHopTimestamp = System.currentTimeMillis();
            lastHopTime = System.currentTimeMillis();

            script.log(getClass(), "hop complete, waiting for position stabilization...");

            // wait for OSMB to stabilize (randomized delay)
            int stabilizationDelay = RandomUtils.gaussianRandom(POST_HOP_STABILIZATION_MIN_MS, POST_HOP_STABILIZATION_MAX_MS, (POST_HOP_STABILIZATION_MIN_MS + POST_HOP_STABILIZATION_MAX_MS) / 2.0, (POST_HOP_STABILIZATION_MAX_MS - POST_HOP_STABILIZATION_MIN_MS) / 4.0);
            script.pollFramesHuman(() -> true, stabilizationDelay);
            script.log(getClass(), "stabilization complete (" + stabilizationDelay + "ms), resetting state");

            // reset state for new world
            resetStateForNewWorld();

            TidalsGemMiner.task = "checking new world...";
            script.log(getClass(), "ready for Setup to check new world");

            return true;
        } finally {
            isHopping = false;
        }
    }

    /**
     * reset state for clean restart on new world
     */
    private void resetStateForNewWorld() {
        // clear crash flag
        DetectPlayers.crashDetected = false;

        // reset player tracking for fresh detection on new world
        DetectPlayers.resetTrackingState();

        // mark setup as incomplete so Setup runs again
        TidalsGemMiner.setupDone = false;

        script.log(getClass(), "state reset for new world");
    }

    /**
     * reset hop state (for fresh script start)
     */
    public static void resetState() {
        lastHopTime = 0;
        isHopping = false;
    }
}
