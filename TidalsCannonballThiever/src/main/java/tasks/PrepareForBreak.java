package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

/**
 * Moves to a safe tile when conditions require it (breaks, hops, AFKs, white dot detection).
 * Uses ProfileManager API to check if the framework is due for break/hop/AFK.
 * Also handles white dot (player) detection hopping.
 * 
 * IMPORTANT: This task proactively moves to safety BEFORE allowing hops/breaks.
 * canHopWorlds() blocks hops while mid-thieve, so we must move to safety first.
 */
public class PrepareForBreak extends Task {
    
    // Safety tile - one tile south of cannonball stall
    private static final WorldPosition SAFETY_TILE = new WorldPosition(1867, 3294, 0);
    
    // Cooldown to prevent rapid checks (shorter for responsive WDH)
    private static long lastCheckTime = 0;
    private static final long MIN_TIME_BETWEEN_CHECKS_MS = 2000; // 2 second cooldown
    
    // Track why we activated (for execute)
    private boolean activatedForWhiteDot = false;
    private boolean activatedForBreak = false;
    private boolean activatedForHop = false;
    private boolean activatedForAFK = false;
    
    // White dot hop settings (read from OSMB's built-in settings would be ideal, but we use a sensible default)
    private static final int MAX_PLAYERS_BEFORE_HOP = 1;
    
    public PrepareForBreak(Script script) {
        super(script);
    }
    
    /**
     * Reset state (e.g., after a break happens)
     */
    public static void resetState() {
        lastCheckTime = 0;
    }
    
    /**
     * Get number of nearby players (white dots on minimap)
     */
    private int getNearbyPlayerCount() {
        try {
            int playerCount = script.getWidgetManager().getMinimap().getPlayerPositions().size();
            // Subtract 1 for our own player
            return Math.max(0, playerCount - 1);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Check if too many players are nearby
     */
    private boolean shouldHopDueToPlayers() {
        // Only check if hop profile is configured
        try {
            if (!script.getProfileManager().hasHopProfile()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        int nearbyPlayers = getNearbyPlayerCount();
        return nearbyPlayers >= MAX_PLAYERS_BEFORE_HOP;
    }
    
    @Override
    public boolean activate() {
        // Don't activate if not set up yet
        if (!setupDone) return false;
        
        // Don't interrupt if currently thieving mid-action (just got XP)
        if (currentlyThieving && lastXpGain.timeElapsed() < 1500) return false;
        
        // Cooldown to prevent rapid checks
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < MIN_TIME_BETWEEN_CHECKS_MS) return false;
        
        // Update lastCheckTime
        lastCheckTime = now;
        
        // Reset activation flags
        activatedForWhiteDot = false;
        activatedForBreak = false;
        activatedForHop = false;
        activatedForAFK = false;
        
        // Check for white dot hop (highest priority - players nearby)
        if (shouldHopDueToPlayers()) {
            activatedForWhiteDot = true;
            script.log("BREAK", "White dot detected - " + getNearbyPlayerCount() + " player(s) nearby!");
            return true;
        }
        
        // Check ProfileManager for scheduled break/hop/AFK
        try {
            if (script.getProfileManager().hasBreakProfile() && script.getProfileManager().isDueToBreak()) {
                activatedForBreak = true;
                script.log("BREAK", "ProfileManager: Due for break");
                return true;
            }
            
            if (script.getProfileManager().hasHopProfile() && script.getProfileManager().isDueToHop()) {
                activatedForHop = true;
                script.log("BREAK", "ProfileManager: Due for scheduled hop");
                return true;
            }
            
            // Check for humanized AFK - breaks up high APM actions
            if (script.getProfileManager().isAFKEnabled() && script.getProfileManager().isDueToAFK()) {
                activatedForAFK = true;
                script.log("BREAK", "ProfileManager: Due for AFK pause");
                return true;
            }
        } catch (Exception e) {
            // ProfileManager may not be available, silently skip
        }
        
        return false;
    }
    
    @Override
    public boolean execute() {
        String reason = activatedForWhiteDot ? "white dot hop" :
                       activatedForBreak ? "scheduled break" :
                       activatedForHop ? "scheduled hop" :
                       activatedForAFK ? "scheduled AFK" : "unknown";
        
        task = "Preparing for " + reason + "...";
        script.log("BREAK", "Moving to safety tile for " + reason);
        
        // Stop thieving
        currentlyThieving = false;
        
        // Move to safety tile (short distance, use direct tap)
        if (!tapOnTile(SAFETY_TILE)) {
            script.log("BREAK", "Failed to tap on safety tile, using walker fallback...");
            script.getWalker().walkTo(SAFETY_TILE);
        }
        
        // Wait until we're at safety tile
        script.pollFramesUntil(() -> isAtSafetyTile(), 3000);
        
        if (!isAtSafetyTile()) {
            script.log("BREAK", "Failed to reach safety tile");
            return true;
        }
        
        script.log("BREAK", "At safety tile - triggering " + reason);
        
        // Execute the appropriate action
        try {
            if (activatedForWhiteDot) {
                // White dot hop - force hop immediately
                script.getProfileManager().forceHop();
                script.log("BREAK", "Forced white dot hop!");
            } else if (activatedForBreak) {
                script.getProfileManager().forceBreak();
                script.log("BREAK", "Forced scheduled break");
            } else if (activatedForHop) {
                script.getProfileManager().forceHop();
                script.log("BREAK", "Forced scheduled hop");
            } else if (activatedForAFK) {
                // Humanized AFK pause - breaks up high APM thieving
                script.getProfileManager().forceAFK();
                script.log("BREAK", "Forced AFK pause");
            }
            
            // After any break/hop/AFK, reset StartThieving positioning flag
            // so we wait for a fresh guard cycle before starting again
            StartThieving.resetAfterBreak();
            
        } catch (Exception e) {
            script.log("BREAK", "Error executing " + reason + ": " + e.getMessage());
        }
        
        return true;
    }
    
    /**
     * Tap directly on a nearby tile (for short distance walking)
     */
    private boolean tapOnTile(WorldPosition tile) {
        try {
            Polygon tilePoly = script.getSceneProjector().getTileCube(tile, 0);
            if (tilePoly == null) {
                return false;
            }
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
