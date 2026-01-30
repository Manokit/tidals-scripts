package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.RandomUtils;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class PrepareForBreak extends Task {

    private static final WorldPosition SAFETY_TILE = new WorldPosition(1867, 3294, 0);
    private static long lastCheckTime = 0;
    // randomized per-check to avoid detectable patterns
    private static long currentMinTimeBetweenChecks = randomizeCheckInterval();
    
    // poll-based state machine
    private enum State {
        WALKING,
        WAITING,
        TRIGGERING,
        CLEANUP
    }

    private State state = State.WALKING;
    private boolean activatedForWhiteDot = false;
    private boolean activatedForBreak = false;
    private boolean activatedForHop = false;
    private boolean activatedForAFK = false;

    private static final int MAX_PLAYERS_BEFORE_HOP = 1;

    public PrepareForBreak(Script script) {
        super(script);
    }

    private void resetInternalState() {
        state = State.WALKING;
    }
    
    private static long randomizeCheckInterval() {
        return RandomUtils.gaussianRandom(1500, 2500, 2000, 250);
    }

    private static long randomizeXpThreshold() {
        return RandomUtils.gaussianRandom(1200, 1800, 1500, 150);
    }

    public static void resetState() {
        lastCheckTime = 0;
        currentMinTimeBetweenChecks = randomizeCheckInterval();
    }
    
    private int getNearbyPlayerCount() {
        int playerCount = script.getWidgetManager().getMinimap().getPlayerPositions().size();
        return Math.max(0, playerCount - 1);
    }
    
    private boolean shouldHopDueToPlayers() {
        if (!script.getProfileManager().hasHopProfile()) {
            return false;
        }

        int nearbyPlayers = getNearbyPlayerCount();
        return nearbyPlayers >= MAX_PLAYERS_BEFORE_HOP;
    }
    
    @Override
    public boolean activate() {
        if (!setupDone) return false;
        if (currentlyThieving && lastXpGain.timeElapsed() < randomizeXpThreshold()) return false;

        long now = System.currentTimeMillis();
        if (now - lastCheckTime < currentMinTimeBetweenChecks) return false;
        lastCheckTime = now;
        currentMinTimeBetweenChecks = randomizeCheckInterval(); // re-randomize for next check
        
        activatedForWhiteDot = false;
        activatedForBreak = false;
        activatedForHop = false;
        activatedForAFK = false;
        
        if (shouldHopDueToPlayers()) {
            activatedForWhiteDot = true;
            script.log("BREAK", "White dot detected - " + getNearbyPlayerCount() + " player(s) nearby!");
            return true;
        }
        
        if (script.getProfileManager().hasBreakProfile() && script.getProfileManager().isDueToBreak()) {
            activatedForBreak = true;
            script.log("BREAK", "ProfileManager: Due for break");
            return true;
        }

        if (script.getProfileManager().hasHopProfile() && script.getProfileManager().isDueToHop()) {
            activatedForHop = true;
            script.log("BREAK", "ProfileManager: Due for scheduled hop");
            return true;
        }

        if (script.getProfileManager().isAFKEnabled() && script.getProfileManager().isDueToAFK()) {
            activatedForAFK = true;
            script.log("BREAK", "ProfileManager: Due for AFK pause");
            return true;
        }

        return false;
    }
    
    @Override
    public boolean execute() {
        String reason = activatedForWhiteDot ? "white dot hop" :
                       activatedForBreak ? "scheduled break" :
                       activatedForHop ? "scheduled hop" :
                       activatedForAFK ? "scheduled AFK" : "unknown";

        task = "Preparing for " + reason + "...";
        currentlyThieving = false;

        switch (state) {
            case WALKING:
                return handleWalking(reason);
            case WAITING:
                return handleWaiting(reason);
            case TRIGGERING:
                return handleTriggering(reason);
            case CLEANUP:
                return handleCleanup();
            default:
                resetInternalState();
                return true;
        }
    }

    private boolean handleWalking(String reason) {
        script.log("BREAK", "Moving to safety tile for " + reason);

        if (isAtSafetyTile()) {
            state = State.TRIGGERING;
            return true;
        }

        if (!tapOnTile(SAFETY_TILE)) {
            script.log("BREAK", "Failed to tap on safety tile, using walker fallback...");
            script.getWalker().walkTo(SAFETY_TILE);
        }

        state = State.WAITING;
        return true;
    }

    private boolean handleWaiting(String reason) {
        script.pollFramesUntil(() -> isAtSafetyTile(), RandomUtils.weightedRandom(2500, 4000, 0.002));

        if (!isAtSafetyTile()) {
            script.log("BREAK", "Failed to reach safety tile");
            // retry walking next poll
            state = State.WALKING;
            return true;
        }

        state = State.TRIGGERING;
        return true;
    }

    private boolean handleTriggering(String reason) {
        script.log("BREAK", "At safety tile - triggering " + reason);

        if (activatedForWhiteDot) {
            script.getProfileManager().forceHop();
            script.log("BREAK", "Forced white dot hop!");
        } else if (activatedForBreak) {
            script.getProfileManager().forceBreak();
            script.log("BREAK", "Forced scheduled break");
        } else if (activatedForHop) {
            script.getProfileManager().forceHop();
            script.log("BREAK", "Forced scheduled hop");
        } else if (activatedForAFK) {
            script.getProfileManager().forceAFK();
            script.log("BREAK", "Forced AFK pause");
        }

        state = State.CLEANUP;
        return true;
    }

    private boolean handleCleanup() {
        StartThieving.resetAfterBreak();

        if (guardTracker != null) {
            guardTracker.resetCbCycle();
            guardTracker.resetGuardTracking();
            guardTracker.enableGuardSync();
        }

        resetInternalState();
        return true;
    }
    
    private boolean tapOnTile(WorldPosition tile) {
        Polygon tilePoly = script.getSceneProjector().getTileCube(tile, 0);
        if (tilePoly == null) {
            return false;
        }
        return script.getFinger().tapGameScreen(tilePoly);
    }
    
    private boolean isAtSafetyTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == 1867 && y == 3294;
    }
}
