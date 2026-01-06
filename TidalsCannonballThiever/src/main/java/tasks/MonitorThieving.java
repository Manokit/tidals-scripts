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
        task = "Monitoring guards";

        // FAST danger check - no delays here, speed is critical!
        if (guardTracker.isAnyGuardInDangerZone()) {
            script.log("MONITOR", "DANGER! Triggering retreat.");
            return true; // retreat task will run next poll
        }

        // no delays - we need to check danger as fast as possible
        return true;
    }
}
