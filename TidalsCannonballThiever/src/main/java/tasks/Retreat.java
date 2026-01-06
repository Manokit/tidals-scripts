package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class Retreat extends Task {
    private static final WorldPosition WALKBACK_TILE = new WorldPosition(1867, 3299, 0);

    public Retreat(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // highest priority - activate if any npc in danger zone and we're not already at walkback
        return !isAtWalkbackTile() && guardTracker.isAnyGuardInDangerZone();
    }

    @Override
    public boolean execute() {
        task = "RETREATING!";
        currentlyThieving = false; // stop thieving, we need to retreat
        script.log("RETREAT", "EMERGENCY - retreating NOW!");

        // FAST direct click - no menu hook, just click the tile
        // left-click on ground defaults to "walk here"
        Polygon tilePoly = script.getSceneProjector().getTileCube(WALKBACK_TILE, 0);

        if (tilePoly != null) {
            // fast direct tap - no menu hook overhead
            script.getFinger().tap(tilePoly);
            script.log("RETREAT", "Fast clicked safety tile");
        } else {
            // fallback to walker if tile not visible
            script.getWalker().walkTo(WALKBACK_TILE);
        }

        // wait until we reach walkback tile (no humanization - critical)
        script.pollFramesUntil(() -> isAtWalkbackTile(), 3000);

        script.log("RETREAT", "Safe!");
        return true;
    }

    private boolean isAtWalkbackTile() {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;
        // exact match only
        int x = (int) current.getX();
        int y = (int) current.getY();
        return x == 1867 && y == 3299 && current.getPlane() == 0;
    }
}
