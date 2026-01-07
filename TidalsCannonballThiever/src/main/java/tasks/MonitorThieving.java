package tasks;

import com.osmb.api.script.Script;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class MonitorThieving extends Task {

    public MonitorThieving(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // only activate if we're currently thieving (after clicking stall)
        return currentlyThieving;
    }

    @Override
    public boolean execute() {
        task = "Thieving...";

        // poll with danger check as break condition - checks every frame but doesn't spam
        // breaks immediately if guard enters danger zone
        boolean dangerDetected = script.pollFramesUntil(() -> {
            return guardTracker.isAnyGuardInDangerZone();
        }, 500); // check for 500ms, break early if danger

        if (dangerDetected) {
            script.log("MONITOR", "DANGER! Guard in zone - retreating!");
        }

        return true;
    }
}
