package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class WaitAtSafety extends Task {

    public WaitAtSafety(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // activate if at safety tile and guard hasn't passed yet
        return isAtSafetyTile() && !guardTracker.isSafeToReturn();
    }

    @Override
    public boolean execute() {
        task = "Waiting for guard...";

        // fast poll until guard has moved past
        script.pollFramesUntil(() -> {
            boolean safe = guardTracker.isSafeToReturn();
            if (safe) {
                script.log("WAIT", "Guard passed!");
            }
            return safe;
        }, 10000);

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
