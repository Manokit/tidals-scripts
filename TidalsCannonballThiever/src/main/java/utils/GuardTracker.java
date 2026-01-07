package utils;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.utils.UIResultList;

import java.util.ArrayList;
import java.util.List;

public class GuardTracker {

    private final Script script;

    // early warning tile - guard sits here for ~3 seconds
    private static final int EARLY_WARNING_X = 1865;
    private static final int PATROL_Y = 3295;

    // immediate danger tiles
    private static final int DANGER_X_1 = 1866;
    private static final int DANGER_X_2 = 1867;

    // track when we first saw guard at early warning tile
    private long earlyWarningStartTime = 0;
    private static final long EARLY_WARNING_DELAY_MS = 3500; // wait 3.5 seconds before retreating from 1865

    // store last known npc positions for paint/debugging
    private List<WorldPosition> lastNpcPositions = new ArrayList<>();

    public GuardTracker(Script script) {
        this.script = script;
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

        // handle early warning with delay
        if (guardAtEarlyWarning) {
            if (earlyWarningStartTime == 0) {
                // first time seeing guard at 1865
                earlyWarningStartTime = System.currentTimeMillis();
                script.log("GUARD", "Early warning - guard at 1865, starting 2s countdown");
            }

            long elapsed = System.currentTimeMillis() - earlyWarningStartTime;
            if (elapsed >= EARLY_WARNING_DELAY_MS) {
                script.log("GUARD", "Early warning expired after " + elapsed + "ms - retreating!");
                return true;
            }
            // still waiting, don't retreat yet
        } else {
            // guard no longer at 1865, reset timer
            earlyWarningStartTime = 0;
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
