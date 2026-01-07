package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

/**
 * Two-stall mode: Switch from cannonball stall to ore stall when guard approaches
 */
public class SwitchToOreStall extends Task {

    public SwitchToOreStall(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // only in two-stall mode
        if (!twoStallMode) return false;

        // only if currently thieving at cannonball stall (not already at ore stall)
        if (!currentlyThieving || atOreStall) return false;

        // PRIMARY: XP-based cycle - switch after 4 CB thieves
        if (guardTracker.shouldSwitchToOreByXp()) {
            script.log("SWITCH", "4 CB thieves done - switching to ore (XP cycle)");
            return true;
        }
        
        // If we just switched via XP cycle, DON'T use backup guard detection
        // This prevents psyching ourselves out right after a proper XP-based switch
        if (guardTracker.isInXpSwitchCooldown()) {
            return false;  // Trust the XP cycle timing, don't second-guess
        }
        
        // BACKUP: Guard detection for mid-cycle starts or emergencies ONLY
        // PIXEL-BASED DETECTION: Watch guard at (1865, 3295) and switch the INSTANT they move!
        if (guardTracker.isWatchingAtCBTile()) {
            // Currently watching - only trigger on pixel movement
            return guardTracker.shouldSwitchToOre();
        }
        
        // Not watching yet - check if guard is at watch tile (will start watching)
        // OR if guard already past watch tile (x=1866, 1867) use tile fallback
        if (guardTracker.shouldSwitchToOre()) {
            return true;
        }
        
        // Tile fallback ONLY for guards already past the watch tile (x=1866 or 1867)
        return guardTracker.isGuardPastWatchTile();
    }

    @Override
    public boolean execute() {
        task = "Switching to ore stall";
        currentlyThieving = false;
        script.log("SWITCH", "Switching to ore stall!");

        // Reset ore cycle counter and guard tracking for fresh start
        guardTracker.resetOreCycle();
        guardTracker.resetOreThiefCount();
        guardTracker.resetGuardTracking();

        // Just click the ore stall - game will auto-walk and start thieving
        if (!startOreThieving()) {
            script.log("SWITCH", "Failed to click ore stall, retrying...");
            return false;
        }

        // Mark as at ore stall and actively thieving (we want to do up to 2 thieves)
        atOreStall = true;
        currentlyThieving = true;  // Now set to true - we'll do up to 2 ore thieves
        guardTracker.incrementOreThiefCount();  // Count this as thieve #1
        
        // Wait for the thieve action to complete (~1.5-2 seconds)
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

        // FAST: simple left-click - no menu overhead
        boolean tapped = script.getFinger().tap(stallPoly);
        if (tapped) {
            script.log("SWITCH", "Left-clicked Ore stall!");
            return true;
        }

        return false;
    }
}
