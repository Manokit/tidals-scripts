package tasks;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class ReturnToThieving extends Task {
    private static final WorldPosition THIEVING_TILE = new WorldPosition(1867, 3298, 0);
    
    // the general thieving area - if we're in here, normal flow works
    private static final RectangleArea THIEVING_AREA = new RectangleArea(1865, 3296, 1869, 3300, 0);

    // minimap-only config for repositioning
    private final WalkConfig minimapOnlyConfig;

    public ReturnToThieving(Script script) {
        super(script);
        this.minimapOnlyConfig = new WalkConfig.Builder()
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
        
        // if we're in the area but not at exact tiles, activate when safe
        return !isAtSafetyTile() && !isAtThievingTile() && guardTracker.isSafeToReturn();
    }

    @Override
    public boolean execute() {
        task = "Walking to stall";
        
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) {
            script.log("RETURN", "Position null, waiting...");
            return false;
        }
        
        script.log("RETURN", "Walking to thieving tile via minimap...");
        script.getWalker().walkTo(THIEVING_TILE, minimapOnlyConfig);
        
        // wait longer if we're far away
        int timeout = isInThievingArea() ? 3000 : 15000;
        script.pollFramesUntil(() -> isAtThievingTile() || isAtSafetyTile(), timeout);

        script.log("RETURN", "Arrived at stall area!");
        return true;
    }

    private boolean isInThievingArea() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        return THIEVING_AREA.contains(pos);
    }

    private boolean isAtSafetyTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == 1867 && y == 3299;
    }

    private boolean isAtThievingTile() {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;
        int x = (int) current.getX();
        int y = (int) current.getY();
        return x == 1867 && y == 3298;
    }
}
