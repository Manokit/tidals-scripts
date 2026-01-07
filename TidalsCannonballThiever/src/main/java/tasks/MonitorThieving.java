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
        task = atOreStall ? "Thieving ores..." : "Thieving...";

        // poll with condition check - checks every frame but doesn't spam
        boolean conditionMet;
        
        if (twoStallMode) {
            // two-stall mode: XP-based cycle with guard detection as backup
            if (atOreStall) {
                // AT ORE STALL: Track XP drops, switch after 2 (don't wait for guard)
                conditionMet = monitorOreStall();
            } else {
                // AT CANNONBALL STALL: Track XP drops, switch after 4 OR guard detection
                conditionMet = monitorCannonballStall();
            }
        } else {
            // single-stall mode: original behavior
            conditionMet = script.pollFramesUntil(() -> {
                return guardTracker.isAnyGuardInDangerZone();
            }, 500);
            if (conditionMet) {
                script.log("MONITOR", "DANGER! Guard in zone - retreating!");
            }
        }

        return true;
    }
    
    /**
     * Monitor cannonball stall - track XP drops for cycle.
     * Switch after 4 XP drops OR guard detection (backup, only if not in cooldown).
     */
    private boolean monitorCannonballStall() {
        // Poll and track XP drops
        boolean shouldSwitch = script.pollFramesUntil(() -> {
            // Check for XP drop and increment counter
            double currentXp = xpTracking.getThievingXpGained();
            guardTracker.checkCbXpDrop(currentXp);
            
            // PRIMARY: Switch after 4 CB thieves
            if (guardTracker.shouldSwitchToOreByXp()) {
                return true;
            }
            
            // Skip backup guard detection if in cooldown (just switched via XP cycle)
            if (guardTracker.isInXpSwitchCooldown()) {
                return false;
            }
            
            // BACKUP: Guard detection (only for mid-cycle starts)
            if (guardTracker.isWatchingAtCBTile()) {
                return guardTracker.shouldSwitchToOre();
            }
            return guardTracker.shouldSwitchToOre() || guardTracker.isGuardPastWatchTile();
        }, 500);
        
        if (shouldSwitch) {
            if (guardTracker.shouldSwitchToOreByXp()) {
                script.log("MONITOR", "4 CB thieves done - time to switch!");
            } else {
                script.log("MONITOR", "Guard detected - switching (backup)!");
            }
        }
        
        return shouldSwitch;
    }

    /**
     * Monitor ore stall - track XP drops for cycle.
     * Switch after 2 XP drops (don't wait for guard - timing is enough).
     * Guard detection is backup only.
     * 
     * @return true when should switch back to cannonball stall
     */
    private boolean monitorOreStall() {
        // Track XP drops - switch after 2 ore thieves
        // Don't wait for guard movement - the timing of 2 ore XP drops is enough
        // for the guard to start moving by the time we reach CB stall
        
        boolean shouldSwitch = script.pollFramesUntil(() -> {
            // Check for XP drop and increment counter
            double currentXp = xpTracking.getThievingXpGained();
            guardTracker.checkOreXpDrop(currentXp);
            
            // PRIMARY: Switch after 2 ore thieves (don't wait for guard!)
            if (guardTracker.shouldSwitchToCbByXp()) {
                return true;
            }
            
            // BACKUP: Guard detection for emergencies/mid-cycle
            if (guardTracker.shouldSwitchToCannonball()) {
                return true;
            }
            
            return guardTracker.isCannonballStallSafe();
        }, 500);
        
        if (shouldSwitch) {
            if (guardTracker.shouldSwitchToCbByXp()) {
                script.log("MONITOR", "2 ore thieves done - switching to CB!");
            } else {
                script.log("MONITOR", "Guard/safety check triggered - switching (backup)!");
            }
        }
        
        return shouldSwitch;
    }

}
