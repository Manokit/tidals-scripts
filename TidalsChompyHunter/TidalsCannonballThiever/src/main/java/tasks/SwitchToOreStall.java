package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class SwitchToOreStall extends Task {

    public SwitchToOreStall(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        if (!twoStallMode) return false;
        if (!currentlyThieving || atOreStall) return false;

        // priority 1: switch on 4 XP drops
        if (guardTracker.shouldSwitchToOreByXp()) {
            script.log("SWITCH", "4 CB thieves done - switching to ore (XP cycle)");
            return true;
        }

        // priority 2: preemptive switch (guard at 1865 + low count + timer elapsed)
        if (guardTracker.shouldPreemptiveSwitchToOre()) {
            script.log("SWITCH", "Preemptive switch - low theft count and guard approaching!");
            return true;
        }

        // priority 3: movement-based detection (guard actively walking towards us)
        if (guardTracker.shouldSwitchToOre()) {
            script.log("SWITCH", "Guard moving right - switching to ore (pixel detection)");
            return true;
        }

        // priority 4: position-based fallback respects cooldown (guard might be near but walking away)
        if (!guardTracker.isInXpSwitchCooldown() && guardTracker.isGuardPastWatchTile()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean execute() {
        task = "Switching to ore stall";
        currentlyThieving = false;
        script.log("SWITCH", "Switching to ore stall!");

        // click stall FIRST - only modify state after confirmed success
        if (!startOreThieving()) {
            script.log("SWITCH", "Failed to click ore stall after retries");
            return false;
        }

        // state changes only after click confirmed
        guardTracker.resetOreCycle();
        guardTracker.resetGuardTracking();

        atOreStall = true;
        currentlyThieving = true;
        lastXpGain.reset();

        // no assumption needed - xp tracker is already initialized from CB stealing
        // wait for actual XP drops: 1/2 on first, 2/2 on second, then switch back
        // no delay - timing critical for guard cycle

        script.log("SWITCH", "Ore stall started - waiting for 2 XP drops then return...");
        return true;
    }

    private boolean startOreThieving() {
        // no delays - timing critical, poll cycle handles retry
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

        RSObject stall = script.getObjectManager().getClosestObject(myPos, "Ore stall");
        if (stall == null) {
            script.log("SWITCH", "Can't find Ore stall!");
            return false;
        }

        Polygon stallPoly = stall.getConvexHull();
        if (stallPoly == null) {
            script.log("SWITCH", "Ore stall hull null");
            return false;
        }

        boolean tapped = script.getFinger().tap(stallPoly);
        if (tapped) {
            script.log("SWITCH", "Left-clicked Ore stall!");
            return true;
        }

        return false;
    }
}
