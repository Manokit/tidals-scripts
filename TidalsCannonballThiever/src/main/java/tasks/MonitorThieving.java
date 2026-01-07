package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
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
            // two-stall mode: check based on which stall we're at
            if (atOreStall) {
                // AT ORE STALL: Watch guard at (1863, 3292) - switch INSTANT they move!
                conditionMet = monitorOreStall();
            } else {
                // AT CANNONBALL STALL: Watch guard at (1865, 3295) - switch INSTANT they move!
                conditionMet = script.pollFramesUntil(() -> {
                    // If actively watching, ONLY use pixel detection
                    if (guardTracker.isWatchingAtCBTile()) {
                        return guardTracker.shouldSwitchToOre();
                    }
                    // Not watching - try to start watching, or use fallback for guards past watch tile
                    return guardTracker.shouldSwitchToOre() || 
                           guardTracker.isGuardPastWatchTile();
                }, 500);
                if (conditionMet) {
                    script.log("MONITOR", "Guard moving from watch tile - switching to ore!");
                }
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
     * Monitor ore stall with PIXEL-BASED guard tracking.
     * Watch guard at (1863, 3292) - the INSTANT they move, we must switch!
     * All 3 guards move at once, so speed is critical.
     * 
     * @return true when should switch back to cannonball stall
     */
    private boolean monitorOreStall() {
        // Continuously watch for guard movement - this is critical!
        // The instant we detect movement, we need to switch
        
        // Check if we should do a 2nd ore thieve while watching
        if (guardTracker.canDoMoreOreThieves()) {
            script.log("MONITOR", "Watching ore guard while doing thieve #2...");
            
            // Poll VERY briefly - we need to be ready to switch instantly
            boolean shouldReturn = script.pollFramesUntil(() -> {
                return guardTracker.shouldSwitchToCannonball();
            }, 300); // Very short poll - guard movement detection is priority!
            
            // If guard started moving, return immediately - don't do another thieve!
            if (shouldReturn) {
                script.log("MONITOR", "Ore guard moving - abort thieve, switch NOW!");
                return true;
            }
            
            // Guard still stationary - safe to do another thieve
            if (guardTracker.canDoMoreOreThieves()) {
                task = "Ore thieve #2...";
                if (doAnotherOreThieve()) {
                    guardTracker.incrementOreThiefCount();
                    // Short wait for thieve - but keep watching guard!
                    boolean guardMoved = script.pollFramesUntil(() -> {
                        return guardTracker.shouldSwitchToCannonball();
                    }, 1500); // Watch during thieve animation
                    
                    if (guardMoved) {
                        script.log("MONITOR", "Ore guard moved during thieve - switch NOW!");
                        return true;
                    }
                }
            }
        }
        
        // Continue watching until guard moves or leaves
        boolean shouldSwitch = script.pollFramesUntil(() -> {
            return guardTracker.shouldSwitchToCannonball() || guardTracker.isCannonballStallSafe();
        }, 500);
        
        if (shouldSwitch) {
            script.log("MONITOR", "Time to switch to cannonball!");
        }
        
        return shouldSwitch;
    }

    /**
     * Click ore stall for another thieve
     */
    private boolean doAnotherOreThieve() {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

        RSObject stall = script.getObjectManager().getClosestObject(myPos, "Ore stall");
        if (stall == null) {
            script.log("MONITOR", "Can't find Ore stall for 2nd thieve!");
            return false;
        }

        Polygon stallPoly = stall.getConvexHull();
        if (stallPoly == null) {
            script.log("MONITOR", "Ore stall hull null");
            return false;
        }

        boolean tapped = script.getFinger().tap(stallPoly);
        if (tapped) {
            script.log("MONITOR", "Clicked Ore stall for thieve #2!");
            return true;
        }

        return false;
    }
}
