package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class SwitchToCannonballStall extends Task {

    public SwitchToCannonballStall(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        if (!twoStallMode) return false;
        if (!atOreStall) return false;

        if (guardTracker.shouldSwitchToCbByXp()) {
            script.log("SWITCH", "2 ore thieves done - switching to CB (XP cycle)");
            return true;
        }
        
        if (guardTracker.shouldSwitchToCannonball()) {
            script.log("SWITCH", "Ore guard moving - GO TO CANNONBALL NOW! (backup)");
            return true;
        }

        if (guardTracker.isCannonballStallSafe()) {
            script.log("SWITCH", "Cannonball safe (tile check) - returning (backup)");
            return true;
        }

        return false;
    }

    @Override
    public boolean execute() {
        task = "Switching to cannonball stall";
        currentlyThieving = false;
        
        int oreThieves = guardTracker.getOreXpDropCount();
        boolean wasXpBasedSwitch = guardTracker.shouldSwitchToCbByXp();
        script.log("SWITCH", "Returning to cannonball stall! (did " + oreThieves + " ore thieves)");

        if (wasXpBasedSwitch) {
            guardTracker.markXpBasedSwitch();
        }

        guardTracker.resetCbCycle();
        guardTracker.resetGuardTracking();
        guardTracker.resetOreThiefCount();

        if (!startCannonballThieving()) {
            script.log("SWITCH", "Failed to click cannonball stall, retrying...");
            return false;
        }

        atOreStall = false;
        currentlyThieving = true;
        lastXpGain.reset();
        script.log("SWITCH", "Now thieving cannonball stall!");
        return true;
    }

    private boolean startCannonballThieving() {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

        RSObject stall = script.getObjectManager().getClosestObject(myPos, "Cannonball stall");
        if (stall == null) {
            script.log("SWITCH", "Can't find Cannonball stall!");
            return false;
        }

        Polygon stallPoly = stall.getConvexHull();
        if (stallPoly == null) {
            script.log("SWITCH", "Cannonball stall hull null");
            return false;
        }

        boolean tapped = script.getFinger().tap(stallPoly);
        if (tapped) {
            script.log("SWITCH", "Left-clicked Cannonball stall!");
            return true;
        }

        return false;
    }
}
