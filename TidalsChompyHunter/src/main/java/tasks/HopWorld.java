package tasks;

import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.script.Script;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;
import main.TidalsChompyHunter;
import utils.Task;

/**
 * responds to crash detection by hopping worlds and resetting state
 * runs at highest priority to handle crashDetected flag immediately
 */
public class HopWorld extends Task {

    // cooldown between consecutive hops (8 seconds per context)
    private static final long HOP_COOLDOWN_MS = 8000;
    private static long lastHopTime = 0;

    // stabilization delay after hop - gives OSMB time to identify our position
    // before Setup checks for other players (prevents false "occupied" detection)
    // randomized 9-11s to match login grace period and ensure ScriptCore is fully ready

    // guard flag to prevent re-entry during hop
    private static volatile boolean isHopping = false;

    // ogre bow ids for post-hop validation
    private static final int[] OGRE_BOWS = {2883, 4827};  // OGRE_BOW, COMP_OGRE_BOW

    public HopWorld(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // prevent re-entry while hop is in progress
        if (isHopping) {
            return false;
        }

        // don't hop while in combat - finish the kill first
        if (AttackChompy.inCombat) {
            return false;
        }

        // only activate when crash detected (works before OR after setup)
        if (!DetectPlayers.crashDetected) {
            return false;
        }

        // check cooldown to prevent rapid hop loops
        long now = System.currentTimeMillis();
        if (now - lastHopTime < HOP_COOLDOWN_MS) {
            return false;
        }

        return true;
    }

    @Override
    public boolean execute() {
        // set guard flag immediately to prevent re-entry
        isHopping = true;

        // CRITICAL: clear crash flag IMMEDIATELY before anything can interrupt us
        // if OSMB's internal handler interrupts, we won't loop back here
        DetectPlayers.crashDetected = false;

        // set hop timestamp NOW so grace period works even if we're interrupted
        // this prevents Setup from detecting our own dot as another player
        DetectPlayers.lastHopTimestamp = System.currentTimeMillis();
        lastHopTime = System.currentTimeMillis();

        // mark setup incomplete so it re-runs after hop
        TidalsChompyHunter.setupComplete = false;

        try {
            TidalsChompyHunter.task = "hopping worlds...";
            script.log(getClass(), "hopping due to crash/occupied world");

            // check if hop profile configured before attempting hop
            if (!script.getProfileManager().hasHopProfile()) {
                script.log(getClass(), "no hop profile configured - stopping script");
                script.stop();
                return false;
            }

            // initiate hop - forceHop() blocks until world load complete
            script.getProfileManager().forceHop();

            // refresh timestamps after actual hop completes (more accurate timing)
            DetectPlayers.lastHopTimestamp = System.currentTimeMillis();
            lastHopTime = System.currentTimeMillis();

            script.log(getClass(), "hop complete, waiting for position stabilization...");

            // wait for OSMB to stabilize and identify our position
            // prevents Setup from seeing our own dot as "another player"
            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(9000, 11000, 10000, 500));
            script.log(getClass(), "stabilization complete, resetting state");

            // reset state for new world
            resetStateForNewWorld();

            // brief validation
            if (!validateEquipment()) {
                script.log(getClass(), "equipment validation failed after hop - Setup will re-validate");
            }

            TidalsChompyHunter.task = "checking new world...";
            script.log(getClass(), "ready for Setup to check new world");

            return true;
        } finally {
            // always clear guard flag
            isHopping = false;
        }
    }

    /**
     * reset state for clean restart on new world
     */
    private void resetStateForNewWorld() {
        // clear crash flag (atomic due to volatile)
        DetectPlayers.crashDetected = false;

        // reset ground toad counter (toads lost on world change)
        TidalsChompyHunter.groundToadCount = 0;

        // clear tracked positions (toads/corpses lost on world change)
        TidalsChompyHunter.droppedToadPositions.clear();
        TidalsChompyHunter.corpsePositions.clear();

        // reset ownership claim - fresh world, no claim until we drop toads
        TidalsChompyHunter.lastToadPresentTime = 0;

        // reset state flags for clean hunting restart
        TidalsChompyHunter.bellowsEmpty = false;
        TidalsChompyHunter.toadAlreadyPlaced = false;

        // reset player tracking for fresh detection on new world
        DetectPlayers.resetTrackingState();

        // reset AttackChompy state (tracked chompies, ignored positions, combat state)
        AttackChompy.resetAllState();

        // mark setup as incomplete so Setup runs again on new world
        TidalsChompyHunter.setupComplete = false;

        script.log(getClass(), "state reset for new world");
    }

    /**
     * quick equipment validation after hop
     * just checks bow is equipped - full validation is Setup's job
     */
    private boolean validateEquipment() {
        // open equipment tab
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

        // verify bow equipped
        UIResult<ItemSearchResult> bowCheck = script.getWidgetManager()
            .getEquipment()
            .findItem(OGRE_BOWS);

        if (!bowCheck.isFound()) {
            script.log(getClass(), "warning: bow not equipped");
            return false;
        }

        return true;
    }

    /**
     * reset hop state (for fresh script start)
     */
    public static void resetState() {
        lastHopTime = 0;
        isHopping = false;
    }
}
