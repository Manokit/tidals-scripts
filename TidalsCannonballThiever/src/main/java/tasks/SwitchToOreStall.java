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

        if (guardTracker.shouldSwitchToOreByXp()) {
            script.log("SWITCH", "4 CB thieves done - switching to ore (XP cycle)");
            return true;
        }
        
        if (guardTracker.isInXpSwitchCooldown()) {
            return false;
        }
        
        if (guardTracker.isWatchingAtCBTile()) {
            return guardTracker.shouldSwitchToOre();
        }
        
        if (guardTracker.shouldSwitchToOre()) {
            return true;
        }
        
        return guardTracker.isGuardPastWatchTile();
    }

    @Override
    public boolean execute() {
        task = "Switching to ore stall";
        currentlyThieving = false;
        script.log("SWITCH", "Switching to ore stall!");

        guardTracker.resetOreCycle();
        guardTracker.resetOreThiefCount();
        guardTracker.resetGuardTracking();

        if (!startOreThieving()) {
            script.log("SWITCH", "Failed to click ore stall, retrying...");
            return false;
        }

        atOreStall = true;
        currentlyThieving = true;
        guardTracker.incrementOreThiefCount();
        
        script.pollFramesHuman(() -> false, script.random(1500, 2000));
        
        script.log("SWITCH", "Ore thieve #1 done - monitoring for return opportunity...");
        return true;
    }

    private boolean startOreThieving() {
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
