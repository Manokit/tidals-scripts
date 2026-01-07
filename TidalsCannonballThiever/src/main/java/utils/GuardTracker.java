package utils;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.utils.UIResultList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GuardTracker {

    private final Script script;
    private final Random random = new Random();

    // early warning tile - guard sits here for ~3 seconds
    private static final int EARLY_WARNING_X = 1865;
    private static final int PATROL_Y = 3295;

    // immediate danger tiles
    private static final int DANGER_X_1 = 1866;
    private static final int DANGER_X_2 = 1867;

    // early warning delay params (normal distribution)
    private static final double DELAY_MIN_SEC = 2.5;
    private static final double DELAY_MAX_SEC = 3.5;
    private static final double DELAY_MEAN_SEC = 3.2;  // center of distribution
    private static final double DELAY_STD_DEV = 0.25;  // standard deviation

    // track when we first saw guard at early warning tile
    private long earlyWarningStartTime = 0;
    private long currentDelayMs = 0;  // randomized delay for current encounter

    // store last known npc positions for paint/debugging
    private List<WorldPosition> lastNpcPositions = new ArrayList<>();

    public GuardTracker(Script script) {
        this.script = script;
    }

    /**
     * Generate a random delay using normal distribution
     * centered around DELAY_MEAN_SEC, clamped to [DELAY_MIN_SEC, DELAY_MAX_SEC]
     */
    private long generateRandomDelay() {
        // normal distribution centered at mean
        double delay = DELAY_MEAN_SEC + random.nextGaussian() * DELAY_STD_DEV;
        
        // clamp to min/max
        delay = Math.max(DELAY_MIN_SEC, Math.min(DELAY_MAX_SEC, delay));
        
        return (long) (delay * 1000); // convert to ms
    }

    /**
     * find all npc positions from minimap (no tapping, just positions)
     * @return list of npc positions
     */
    public List<WorldPosition> findAllNPCPositions() {
        List<WorldPosition> npcPositions = new ArrayList<>();

        // get all npc positions from minimap
        UIResultList<WorldPosition> npcResult = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcResult == null || !npcResult.isFound()) {
            return npcPositions;
        }

        // create mutable copy since asList() returns unmodifiable list
        npcPositions = new ArrayList<>(npcResult.asList());

        // update cached positions
        lastNpcPositions = npcPositions;

        return npcPositions;
    }

    /**
     * check if any npc is at a danger tile
     * - immediate danger (1866, 1867): return true right away
     * - early warning (1865): return true after 2 seconds delay
     * @return true if danger detected
     */
    public boolean isAnyGuardInDangerZone() {
        List<WorldPosition> npcPositions = findAllNPCPositions();

        boolean guardAtEarlyWarning = false;
        boolean guardAtImmediateDanger = false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // check patrol row
            if (y != PATROL_Y) continue;

            // immediate danger - retreat NOW
            if (x == DANGER_X_1 || x == DANGER_X_2) {
                script.log("GUARD", "IMMEDIATE DANGER! NPC at x=" + x);
                earlyWarningStartTime = 0; // reset early warning timer
                return true;
            }

            // early warning - guard at 1865
            if (x == EARLY_WARNING_X) {
                guardAtEarlyWarning = true;
            }
        }

        // handle early warning with randomized delay
        if (guardAtEarlyWarning) {
            if (earlyWarningStartTime == 0) {
                // first time seeing guard at 1865, generate random delay
                earlyWarningStartTime = System.currentTimeMillis();
                currentDelayMs = generateRandomDelay();
                double delaySec = currentDelayMs / 1000.0;
                script.log("GUARD", String.format("Early warning - guard at 1865, waiting %.2fs before retreat", delaySec));
            }

            long elapsed = System.currentTimeMillis() - earlyWarningStartTime;
            if (elapsed >= currentDelayMs) {
                double actualSec = elapsed / 1000.0;
                script.log("GUARD", String.format("Early warning expired after %.2fs - retreating!", actualSec));
                return true;
            }
            // still waiting, don't retreat yet
        } else {
            // guard no longer at 1865, reset timer
            if (earlyWarningStartTime != 0) {
                earlyWarningStartTime = 0;
                currentDelayMs = 0;
            }
        }

        return false;
    }

    /**
     * check if safe to return to thieving
     * guard must have moved PAST the stall (x >= 1868) or be off the patrol row
     * @return true if safe to return
     */
    public boolean isSafeToReturn() {
        List<WorldPosition> npcPositions = findAllNPCPositions();

        // check if any npc is in the guard patrol area (x 1864-1867, y 3295)
        // ignore NPCs outside this range - they're not guards
        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // only check NPCs in the guard patrol zone (x 1864-1867 at y=3295)
            // NPCs outside this range are not the patrolling guard
            if (y == PATROL_Y && x >= 1864 && x <= DANGER_X_2) {
                script.log("GUARD", "Not safe yet - NPC at x=" + x + " in patrol zone");
                return false;
            }
        }

        // reset early warning timer when returning
        earlyWarningStartTime = 0;
        currentDelayMs = 0;

        script.log("GUARD", "Safe to return - patrol zone clear");
        return true;
    }

    /**
     * get last known npc positions (for paint overlay)
     */
    public List<WorldPosition> getLastNpcPositions() {
        return lastNpcPositions;
    }
}
