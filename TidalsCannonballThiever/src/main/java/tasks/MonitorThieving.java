package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.script.Script;
import com.osmb.api.utils.RandomUtils;
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
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
        return inv != null && inv.isFull();
    }
    
    // switch after 4 xp drops or guard detection - whichever comes first
    private boolean monitorCannonballStall() {
        boolean shouldSwitch = script.pollFramesUntil(() -> {
            if (isInventoryFull()) {
                script.log("MONITOR", "Inventory full detected - stopping monitor");
                currentlyThieving = false;
                return true;
            }

            // always track XP drops
            double currentXp = xpTracking.getThievingXpGained();
            guardTracker.checkCbXpDrop(currentXp);

            // priority 1: XP-based switch (4 thieves done)
            if (guardTracker.shouldSwitchToOreByXp()) {
                return true;
            }

            // priority 2: preemptive switch (guard at 1865 + low count + timer elapsed)
            if (guardTracker.shouldPreemptiveSwitchToOre()) {
                return true;
            }

            // priority 3: movement-based detection (guard actively walking towards us)
            if (guardTracker.shouldSwitchToOre()) {
                return true;
            }

            // priority 4: position-based fallback respects cooldown (guard might be near but walking away)
            if (!guardTracker.isInXpSwitchCooldown() && guardTracker.isGuardPastWatchTile()) {
                return true;
            }

            return false;
        }, 500);

        if (shouldSwitch) {
            if (guardTracker.shouldSwitchToOreByXp()) {
                script.log("MONITOR", "4 CB thieves done - time to switch!");
            } else if (guardTracker.shouldPreemptiveSwitchToOre()) {
                script.log("MONITOR", "Preemptive switch - low count and guard approaching!");
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

            // primary: switch after 2 ore XP drops
            if (guardTracker.shouldSwitchToCbByXp()) {
                return true;
            }

            // emergency only: guard actively moving toward ore stall
            if (guardTracker.shouldSwitchToCannonball()) {
                return true;
            }

            // no position backup - if we got 4 CB thieves, we have time for 2 ore thieves
            // the guard just passed, walking away from us

            return false;
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
