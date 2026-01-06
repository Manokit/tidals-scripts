package utils;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.utils.UIResultList;

import java.util.ArrayList;
import java.util.List;

public class GuardTracker {

    private final Script script;

    // danger tiles where guards cause us to retreat
    // guard patrol: ... -> 1865,3295 (sits here) -> 1866,3295 -> 1867,3295 -> ...
    // we only retreat when guard moves to 1866 or 1867 (1865 is safe, guard sits there awhile)
    private static final WorldPosition DANGER_TILE_1 = new WorldPosition(1866, 3295, 0); // guard approaching stall
    private static final WorldPosition DANGER_TILE_2 = new WorldPosition(1867, 3295, 0); // at the stall

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
     * @return true if danger detected
     */
    public boolean isAnyGuardInDangerZone() {
        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (isDangerTile(npcPos)) {
                script.log("GUARD", "DANGER! NPC at danger tile: " + npcPos);
                return true;
            }
        }

        return false;
    }

    /**
     * check if position matches any danger tile exactly
     */
    private boolean isDangerTile(WorldPosition pos) {
        if (pos == null || pos.getPlane() != 0) return false;

        int x = (int) pos.getX();
        int y = (int) pos.getY();

        // only 1866 and 1867 are danger tiles
        // 1865 is safe (guard sits there awhile before moving)
        // 1866,3295 - guard approaching stall - RETREAT
        if (x == 1866 && y == 3295) return true;
        // 1867,3295 - at the stall - RETREAT
        if (x == 1867 && y == 3295) return true;

        return false;
    }

    /**
     * check if safe to return to thieving
     * guard must have moved PAST the stall (x >= 1868) or be off the patrol row
     * @return true if safe to return
     */
    public boolean isSafeToReturn() {
        List<WorldPosition> npcPositions = findAllNPCPositions();

        // check if any npc is still in the danger zone or approaching
        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // if npc is on the patrol row (y=3295) and x is 1867 or less, not safe yet
            // guard walks right, so we wait until x >= 1868
            if (y == 3295 && x <= 1867) {
                script.log("GUARD", "Not safe yet - NPC at x=" + x + ", y=" + y + " (waiting for x >= 1868)");
                return false;
            }
        }

        script.log("GUARD", "Safe to return - no NPCs in danger zone");
        return true;
    }

    /**
     * get last known npc positions (for paint overlay)
     */
    public List<WorldPosition> getLastNpcPositions() {
        return lastNpcPositions;
    }
}
