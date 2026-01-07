package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

/**
 * Two-stall mode: Switch from ore stall to cannonball stall when guard has passed
 */
public class SwitchToCannonballStall extends Task {

    public SwitchToCannonballStall(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // only in two-stall mode
        if (!twoStallMode) return false;

        // only if at ore stall (doesn't need to be actively thieving - we do quick hit and run)
        if (!atOreStall) return false;

        // PIXEL DETECTION: Watch guard at (1863, 3292) - the INSTANT they move, GO!
        // All 3 guards move at once, so we need to be fast!
        if (guardTracker.shouldSwitchToCannonball()) {
            script.log("SWITCH", "Ore guard moving - GO TO CANNONBALL NOW!");
            return true;
        }

        // FALLBACK: Also return if cannonball is confirmed safe (guard at x >= 1868)
        if (guardTracker.isCannonballStallSafe()) {
            script.log("SWITCH", "Cannonball safe (tile check) - returning");
            return true;
        }

        return false;
    }

    @Override
    public boolean execute() {
        task = "Switching to cannonball stall";
        currentlyThieving = false;
        
        int oreThieves = guardTracker.getOreThiefCount();
        script.log("SWITCH", "Returning to cannonball stall! (did " + oreThieves + " ore thieves)");

        // Reset guard tracking for fresh detection at cannonball stall
        guardTracker.resetGuardTracking();
        guardTracker.resetOreThiefCount();

        // Just click the cannonball stall - game will auto-walk and start thieving
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

        // FAST: simple left-click - no menu overhead
        boolean tapped = script.getFinger().tap(stallPoly);
        if (tapped) {
            script.log("SWITCH", "Left-clicked Cannonball stall!");
            return true;
        }

        return false;
    }
}
