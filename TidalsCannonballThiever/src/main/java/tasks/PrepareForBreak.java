package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

/**
 * Periodically moves to a safe tile to allow breaks/world hops/AFKs to trigger.
 * This task activates after X completed cycles to give the framework a chance
 * to execute scheduled breaks.
 */
public class PrepareForBreak extends Task {
    
    // Safety tile - one tile south of cannonball stall
    private static final WorldPosition SAFETY_TILE = new WorldPosition(1867, 3294, 0);
    
    // How often to allow breaks (every N full cycles)
    private static final int CYCLES_BEFORE_BREAK_CHECK = 8;
    
    // Track completed cycles
    private static int completedCycles = 0;
    private static long lastBreakCheckTime = 0;
    private static final long MIN_TIME_BETWEEN_CHECKS_MS = 90000; // at least 1.5 minutes between checks
    
    public PrepareForBreak(Script script) {
        super(script);
    }
    
    /**
     * Call this when a full cycle completes (4 CB + 2 ore)
     */
    public static void incrementCycleCount() {
        completedCycles++;
    }
    
    /**
     * Reset cycle count (e.g., after a break happens)
     */
    public static void resetCycleCount() {
        completedCycles = 0;
    }
    
    @Override
    public boolean activate() {
        // Only in two-stall mode
        if (!twoStallMode) return false;
        
        // Only check if we've completed enough cycles
        if (completedCycles < CYCLES_BEFORE_BREAK_CHECK) return false;
        
        // Don't check too frequently
        long now = System.currentTimeMillis();
        if (now - lastBreakCheckTime < MIN_TIME_BETWEEN_CHECKS_MS) return false;
        
        // Only activate when at cannonball stall and not mid-thieve
        // (we just switched from ore, good time to check for breaks)
        if (atOreStall) return false;
        
        // Don't interrupt if XP cycle just started
        if (guardTracker.getCbXpDropCount() > 0) return false;
        
        script.log("BREAK", "Cycle count reached " + completedCycles + " - checking for breaks...");
        return true;
    }
    
    @Override
    public boolean execute() {
        task = "Checking for breaks...";
        lastBreakCheckTime = System.currentTimeMillis();
        
        script.log("BREAK", "Moving to safety tile to allow breaks/hops/AFKs...");
        
        // Stop thieving
        currentlyThieving = false;
        
        // For short distance (1 tile), just tap on the destination tile directly
        // Much more natural than using the full pathfinder!
        if (!tapOnTile(SAFETY_TILE)) {
            script.log("BREAK", "Failed to tap on safety tile, using walker fallback...");
            script.getWalker().walkTo(SAFETY_TILE);
        }
        
        // Wait until we're at safety tile
        script.pollFramesUntil(() -> isAtSafetyTile(), 3000);
        
        if (isAtSafetyTile()) {
            script.log("BREAK", "At safety tile - breaks/hops/AFKs can now trigger");
            
            // Wait a moment to give framework time to trigger break/hop/AFK
            // If one triggers, this poll will be interrupted by TaskInterruptedException
            script.pollFramesHuman(() -> false, script.random(1500, 2500));
            
            script.log("BREAK", "No break triggered, resuming thieving...");
        }
        
        // Reset cycle count after break check
        resetCycleCount();
        
        // Reset StartThieving positioning flag so we wait for a fresh guard cycle
        // This ensures we see the guard pass before starting after a break
        StartThieving.resetAfterBreak();
        
        return true;
    }
    
    /**
     * Tap directly on a nearby tile (for short distance walking)
     * Much more natural than using the pathfinder for 1-2 tile movements
     */
    private boolean tapOnTile(WorldPosition tile) {
        try {
            // Get the tile polygon (height 0 = ground level)
            Polygon tilePoly = script.getSceneProjector().getTileCube(tile, 0);
            if (tilePoly == null) {
                return false;
            }
            
            // Tap on the tile to walk there
            return script.getFinger().tap(tilePoly);
        } catch (Exception e) {
            script.log("BREAK", "Error tapping tile: " + e.getMessage());
            return false;
        }
    }
    
    private boolean isAtSafetyTile() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == 1867 && y == 3294;
    }
}
