package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.script.Script;
import utils.Task;

import java.util.Set;

import static main.TidalsCannonballThiever.*;

public class MonitorThieving extends Task {

    public MonitorThieving(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        return currentlyThieving;
    }

    @Override
    public boolean execute() {
        task = atOreStall ? "Thieving ores..." : "Thieving...";

        boolean conditionMet;
        
        if (twoStallMode) {
            conditionMet = atOreStall ? monitorOreStall() : monitorCannonballStall();
        } else {
            conditionMet = script.pollFramesUntil(() -> {
                return guardTracker.isAnyGuardInDangerZone();
            }, 500);
            if (conditionMet) {
                script.log("MONITOR", "DANGER! Guard in zone - retreating!");
            }
        }

        return true;
    }
    
    private boolean isInventoryFull() {
        try {
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
            return inv != null && inv.isFull();
        } catch (Exception e) {
            return false;
        }
    }
    
    // switch after 4 xp drops or guard detection
    private boolean monitorCannonballStall() {
        boolean shouldSwitch = script.pollFramesUntil(() -> {
            if (isInventoryFull()) {
                script.log("MONITOR", "Inventory full detected - stopping monitor");
                currentlyThieving = false;
                return true;
            }
            
            double currentXp = xpTracking.getThievingXpGained();
            guardTracker.checkCbXpDrop(currentXp);
            
            if (guardTracker.shouldSwitchToOreByXp()) {
                return true;
            }
            
            if (guardTracker.isInXpSwitchCooldown()) {
                return false;
            }
            
            if (guardTracker.isWatchingAtCBTile()) {
                return guardTracker.shouldSwitchToOre();
            }
            return guardTracker.shouldSwitchToOre() || guardTracker.isGuardPastWatchTile();
        }, 500);
        
        if (shouldSwitch) {
            if (guardTracker.shouldSwitchToOreByXp()) {
                script.log("MONITOR", "4 CB thieves done - time to switch!");
            } else if (!isInventoryFull()) {
                script.log("MONITOR", "Guard detected - switching (backup)!");
            }
        }
        
        return shouldSwitch;
    }

    // switch after 2 xp drops
    private boolean monitorOreStall() {
        boolean shouldSwitch = script.pollFramesUntil(() -> {
            if (isInventoryFull()) {
                script.log("MONITOR", "Inventory full detected - stopping monitor");
                currentlyThieving = false;
                return true;
            }
            
            double currentXp = xpTracking.getThievingXpGained();
            guardTracker.checkOreXpDrop(currentXp);
            
            if (guardTracker.shouldSwitchToCbByXp()) {
                return true;
            }
            
            if (guardTracker.shouldSwitchToCannonball()) {
                return true;
            }
            
            return guardTracker.isCannonballStallSafe();
        }, 500);
        
        if (shouldSwitch) {
            if (guardTracker.shouldSwitchToCbByXp()) {
                script.log("MONITOR", "2 ore thieves done - switching to CB!");
            } else if (!isInventoryFull()) {
                script.log("MONITOR", "Guard/safety check triggered - switching (backup)!");
            }
        }
        
        return shouldSwitch;
    }

}
