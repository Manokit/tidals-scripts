package tasks;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class ReturnToThieving extends Task {
    // single-stall mode positions
    private static final WorldPosition THIEVING_TILE_SINGLE = new WorldPosition(1867, 3298, 0);
    private static final RectangleArea THIEVING_AREA_SINGLE = new RectangleArea(1865, 3296, 1869, 3300, 0);

    // two-stall mode positions
    private static final WorldPosition THIEVING_TILE_TWO_STALL = new WorldPosition(1867, 3295, 0);
    private static final RectangleArea THIEVING_AREA_TWO_STALL = new RectangleArea(1862, 3293, 1869, 3297, 0);

    private WorldPosition getThievingTile() {
        return twoStallMode ? THIEVING_TILE_TWO_STALL : THIEVING_TILE_SINGLE;
    }

    private RectangleArea getThievingArea() {
        return twoStallMode ? THIEVING_AREA_TWO_STALL : THIEVING_AREA_SINGLE;
    }

    // Config for two-stall mode - exact positioning needed for guard detection
    private final WalkConfig exactTileConfig;
    // Config for single stall mode - more lenient positioning
    private final WalkConfig singleStallConfig;

    public ReturnToThieving(Script script) {
        super(script);
        this.exactTileConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .build();
        this.singleStallConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(2)
                .tileRandomisationRadius(0)
                .build();
    }

    @Override
    public boolean activate() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        if (currentlyThieving) return false;
        
        // if we're far from thieving area (e.g. starting near jail), always activate
        if (!isInThievingArea()) {
            script.log("RETURN", "Far from stall area, need to walk there");
            return true;
        }
        
        // If at safety/break tile, walk to stall position immediately (don't wait for guard!)
        // This way we're ready to thieve the instant the guard passes
        // StartThieving will handle waiting for guard before actually clicking the stall
        if (isAtAnySafetyTile() && !isAtThievingTile()) {
            script.log("RETURN", "At safety tile - moving to stall position to be ready");
            return true;
        }
        
        // For other positions, only activate when safe
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
        
        // For short distances (from safety tile), use direct tile tap
        if (isAtAnySafetyTile()) {
            script.log("RETURN", "Moving to stall position (short distance)...");
            if (!tapOnTile(getThievingTile())) {
                script.log("RETURN", "Tile tap failed, using walker...");
                WalkConfig config = twoStallMode ? exactTileConfig : singleStallConfig;
                script.getWalker().walkTo(getThievingTile(), config);
            }
            script.pollFramesUntil(() -> isAtThievingTile(), 3000);
        } else {
            // For longer distances, use walker
            script.log("RETURN", "Walking to thieving tile...");
            WalkConfig config = twoStallMode ? exactTileConfig : singleStallConfig;
            script.getWalker().walkTo(getThievingTile(), config);
            
            int timeout = isInThievingArea() ? 3000 : 15000;
            script.pollFramesUntil(() -> isAtThievingTile(), timeout);
        }

        script.log("RETURN", "Arrived at stall position!");
        return true;
    }
    
    /**
     * Tap directly on a nearby tile (for short distance walking)
     */
    private boolean tapOnTile(WorldPosition tile) {
        try {
            Polygon tilePoly = script.getSceneProjector().getTileCube(tile, 0);
            if (tilePoly == null) return false;
            return script.getFinger().tap(tilePoly);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isInThievingArea() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        return getThievingArea().contains(pos);
    }

    /**
     * Check if at any safety/break tile
     * - Single stall safety: (1867, 3299)
     * - Two stall break: (1867, 3294)
     */
    private boolean isAtAnySafetyTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        // Single stall safety tile OR two-stall break tile
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
