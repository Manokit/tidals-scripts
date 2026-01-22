package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class Retreat extends Task {
    private static final WorldPosition SAFETY_TILE = new WorldPosition(1867, 3299, 0);

    public Retreat(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        if (twoStallMode) return false;
        return currentlyThieving && guardTracker.isAnyGuardInDangerZone();
    }

    @Override
    public boolean execute() {
        task = "RETREATING!";
        currentlyThieving = false;
        script.log("RETREAT", "Guard danger - stepping back!");

        if (!tapOnTile(SAFETY_TILE)) {
            script.log("RETREAT", "Tap failed, using walker fallback...");
            script.getWalker().walkTo(SAFETY_TILE);
        }

        script.pollFramesUntil(() -> isAtSafetyTile(), 3000);

        script.log("RETREAT", "Safe! Waiting for guard to pass...");
        return true;
    }
    
    private boolean tapOnTile(WorldPosition tile) {
        try {
            Polygon tilePoly = script.getSceneProjector().getTileCube(tile, 0);
            if (tilePoly == null) return false;
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
