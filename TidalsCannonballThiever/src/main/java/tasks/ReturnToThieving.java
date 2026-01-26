package tasks;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class ReturnToThieving extends Task {
    private static final WorldPosition THIEVING_TILE_SINGLE = new WorldPosition(1867, 3298, 0);
    private static final RectangleArea THIEVING_AREA_SINGLE = new RectangleArea(1865, 3296, 1869, 3300, 0);
    private static final WorldPosition THIEVING_TILE_TWO_STALL = new WorldPosition(1867, 3295, 0);
    private static final RectangleArea THIEVING_AREA_TWO_STALL = new RectangleArea(1862, 3293, 1869, 3297, 0);

    private WorldPosition getThievingTile() {
        return twoStallMode ? THIEVING_TILE_TWO_STALL : THIEVING_TILE_SINGLE;
    }

    private RectangleArea getThievingArea() {
        return twoStallMode ? THIEVING_AREA_TWO_STALL : THIEVING_AREA_SINGLE;
    }

    // cached configs with timeouts
    private final WalkConfig exactTileConfig;
    private final WalkConfig nearbyConfig;

    public ReturnToThieving(Script script) {
        super(script);
        this.exactTileConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .timeout(15000)
                .build();
        this.nearbyConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(2)
                .tileRandomisationRadius(0)
                .timeout(10000)
                .build();
    }

    @Override
    public boolean activate() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        if (currentlyThieving) return false;
        
        if (!isInThievingArea()) {
            script.log("RETURN", "Far from stall area, need to walk there");
            return true;
        }
        
        if (isAtAnySafetyTile() && !isAtThievingTile()) {
            script.log("RETURN", "At safety tile - moving to stall position to be ready");
            return true;
        }
        
        return !isAtAnySafetyTile() && !isAtThievingTile() && guardTracker.isSafeToReturn();
    }

    @Override
    public boolean execute() {
        task = "Walking to stall";

        WorldPosition pos = script.getWorldPosition();
        if (pos == null) {
            script.log("RETURN", "Position null, waiting...");
            return false;
        }

        WorldPosition target = getThievingTile();
        WalkConfig config = twoStallMode ? exactTileConfig : nearbyConfig;

        if (isAtAnySafetyTile()) {
            script.log("RETURN", "Moving from safety tile to stall position...");
            // try direct tile tap first for short distance
            if (tapOnTile(target)) {
                script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(400, 1200, 0.002));
                if (isAtThievingTile()) {
                    script.log("RETURN", "Arrived via tile tap");
                    return true;
                }
            }
            // fall back to walker
            script.log("RETURN", "Tile tap insufficient, using walker...");
        } else {
            script.log("RETURN", "Walking to thieving tile...");
        }

        try {
            boolean walked = script.getWalker().walkTo(target, config);

            // brief settle time after walk
            script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(300, 1000, 0.002));

            if (walked || isAtThievingTile()) {
                script.log("RETURN", "Arrived at stall position");
                return true;
            }
        } catch (NullPointerException e) {
            // position can become null mid-walk (loading screens, game state changes)
            script.log("RETURN", "Walker NPE - position became null mid-walk, retrying...");
            return false;
        }

        script.log("RETURN", "Did not reach stall position, will retry");
        return false;
    }
    
    private boolean tapOnTile(WorldPosition tile) {
        Polygon tilePoly = script.getSceneProjector().getTileCube(tile, 0);
        if (tilePoly == null) return false;
        return script.getFinger().tap(tilePoly);
    }

    private boolean isInThievingArea() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        return getThievingArea().contains(pos);
    }

    private boolean isAtAnySafetyTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return (x == 1867 && y == 3299) || (x == 1867 && y == 3294);
    }

    private boolean isAtThievingTile() {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;
        WorldPosition target = getThievingTile();
        int x = (int) current.getX();
        int y = (int) current.getY();
        return x == (int) target.getX() && y == (int) target.getY();
    }
}
