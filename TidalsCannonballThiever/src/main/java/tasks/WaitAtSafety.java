package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class WaitAtSafety extends Task {
    private static final WorldPosition WALKBACK_TILE = new WorldPosition(1867, 3299, 0);

    public WaitAtSafety(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // activate if at walkback tile AND not safe to return yet (guard still too close)
        return isAtWalkbackTile() && !guardTracker.isSafeToReturn();
    }

    @Override
    public boolean execute() {
        task = "Waiting for guard to pass...";

        // poll until guard has moved past the stall (x >= 1868)
        script.pollFramesHuman(() -> {
            boolean safe = guardTracker.isSafeToReturn();

            if (safe) {
                script.log("WAIT", "Guard has passed! Safe to return.");
            }

            return safe;
        }, script.random(8000, 12000)); // wait longer for guard to pass

        return true;
    }

    private boolean isAtWalkbackTile() {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;
        return current.equals(WALKBACK_TILE);
    }
}
