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
        
        // emergency only: guard actively moving toward ore stall
        if (guardTracker.shouldSwitchToCannonball()) {
            script.log("SWITCH", "Ore guard moving - GO TO CANNONBALL NOW! (emergency)");
            return true;
        }

        // no position backup - if we got 4 CB thieves, we have time for 2 ore thieves
        // the guard just passed, walking away from us

        return false;
    }

    @Override
    public boolean execute() {
        task = "Switching to cannonball stall";
        currentlyThieving = false;

        int oreThieves = guardTracker.getOreXpDropCount();
        script.log("SWITCH", "Returning to cannonball stall! (did " + oreThieves + " ore thieves)");

        // click stall FIRST - only modify state after confirmed success
        if (!startCannonballThieving()) {
            script.log("SWITCH", "Failed to click cannonball stall after retries");
            return false;
        }

        // state changes only after click confirmed
        guardTracker.markXpBasedSwitch();
        guardTracker.resetCbCycle();
        guardTracker.resetGuardTracking();

        atOreStall = false;
        currentlyThieving = true;
        lastXpGain.reset();

        // no assumption needed - xp tracker is already initialized from previous stealing
        // wait for actual XP drops: 1/4, 2/4, 3/4, 4/4, then switch to ore

        script.log("SWITCH", "Now thieving cannonball stall!");
        return true;
    }

    private boolean startCannonballThieving() {
        // no delays - timing critical, poll cycle handles retry
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

        boolean tapped = script.getFinger().tapGameScreen(stallPoly);
        if (tapped) {
            script.log("SWITCH", "Left-clicked Cannonball stall!");
            return true;
        }

        return false;
    }
}
