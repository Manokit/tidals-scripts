package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class Retreat extends Task {
    private static final WorldPosition SAFETY_TILE = new WorldPosition(1867, 3299, 0);

    private boolean retreating = false;

    public Retreat(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        if (twoStallMode) return false;
        // stay active while retreating even if guard moved away
        if (retreating) return !isAtSafetyTile();
        return currentlyThieving && guardTracker.isAnyGuardInDangerZone();
    }

    @Override
    public boolean execute() {
        task = "RETREATING!";
        currentlyThieving = false;

        // state: already retreating - wait for arrival
        if (retreating) {
            if (isAtSafetyTile()) {
                script.log("RETREAT", "Safe! Waiting for guard to pass...");
                retreating = false;
                return true;
            }
            // still moving, re-poll
            return true;
        }

        // state: initiate retreat - tap tile or walker fallback
        script.log("RETREAT", "Guard danger - stepping back!");
        if (!tapOnTile(SAFETY_TILE)) {
            script.log("RETREAT", "Tap failed, using walker fallback...");
            script.getWalker().walkTo(SAFETY_TILE);
        }
        retreating = true;
        return true;
    }
    
    private boolean tapOnTile(WorldPosition tile) {
        Polygon tilePoly = script.getSceneProjector().getTileCube(tile, 0);
        if (tilePoly == null) return false;
        return script.getFinger().tapGameScreen(tilePoly);
    }

    private boolean isAtSafetyTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == 1867 && y == 3299;
    }
}
