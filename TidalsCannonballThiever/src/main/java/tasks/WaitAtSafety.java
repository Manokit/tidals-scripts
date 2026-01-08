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
        if (twoStallMode) return false;
        return isAtSafetyTile() && !guardTracker.isSafeToReturn();
    }

    @Override
    public boolean execute() {
        task = "Waiting for guard...";

        script.pollFramesUntil(() -> {
            boolean safe = guardTracker.isSafeToReturn();
            if (safe) script.log("WAIT", "Guard passed!");
            return safe;
        }, 10000);

        if (script.random(1, 100) <= 30) {
            script.pollFramesHuman(() -> false, script.random(150, 400));
        }

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
