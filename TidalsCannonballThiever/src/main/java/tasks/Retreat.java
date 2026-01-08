package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class Retreat extends Task {
    // safety tile - 1 tile north of thieving spot (away from stall)
    private static final WorldPosition SAFETY_TILE = new WorldPosition(1867, 3299, 0);

    public Retreat(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // only for single-stall mode - two-stall mode uses SwitchToOreStall/SwitchToCannonballStall
        if (twoStallMode) return false;

        // highest priority - activate if any npc in danger zone and we're still thieving
        return currentlyThieving && guardTracker.isAnyGuardInDangerZone();
    }

    @Override
    public boolean execute() {
        task = "RETREATING!";
        currentlyThieving = false;
        script.log("RETREAT", "Guard danger - stepping back!");

        // For short distance (1-2 tiles), just tap directly on the ground tile
        // Much faster and more natural than using the full pathfinder
        if (!tapOnTile(SAFETY_TILE)) {
            script.log("RETREAT", "Tap failed, using walker fallback...");
            script.getWalker().walkTo(SAFETY_TILE);
        }

        // wait until we're at safety tile
        script.pollFramesUntil(() -> isAtSafetyTile(), 3000);

        script.log("RETREAT", "Safe! Waiting for guard to pass...");
        return true;
    }
    
    /**
     * Tap directly on a nearby tile (for short distance walking)
     * Much faster and more natural than using the pathfinder
     */
    private boolean tapOnTile(WorldPosition tile) {
        try {
            // Get the tile polygon (height 0 = ground level)
            Polygon tilePoly = script.getSceneProjector().getTileCube(tile, 0);
            if (tilePoly == null) {
                return false;
            }
            
            // Tap on the tile to walk there
            return script.getFinger().tap(tilePoly);
        } catch (Exception e) {
            script.log("RETREAT", "Error tapping tile: " + e.getMessage());
            return false;
        }
    }

    private boolean isAtSafetyTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == 1867 && y == 3299;
    }
}
