package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class Retreat extends Task {
    // safety tile - 1 tile north of thieving spot (away from stall)
    private static final WorldPosition SAFETY_TILE = new WorldPosition(1867, 3299, 0);

    // minimap-only config to avoid clicking stall
    private final WalkConfig minimapOnlyConfig;

    public Retreat(Script script) {
        super(script);
        this.minimapOnlyConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)  // minimap only - won't click stall
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .build();
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
        script.log("RETREAT", "Guard danger - stepping back via minimap!");

        // walk 1 tile north via minimap to escape stall interaction
        script.getWalker().walkTo(SAFETY_TILE, minimapOnlyConfig);

        // wait until we're at safety tile
        script.pollFramesUntil(() -> isAtSafetyTile(), 3000);

        script.log("RETREAT", "Safe! Waiting for guard to pass...");
        return true;
    }

    private boolean isAtSafetyTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == 1867 && y == 3299;
    }
}
