package tasks;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class PrepareForBreak extends Task {
    
    private static final WorldPosition SAFETY_TILE = new WorldPosition(1867, 3294, 0);
    private static long lastCheckTime = 0;
    private static final long MIN_TIME_BETWEEN_CHECKS_MS = 2000;
    
    private boolean activatedForWhiteDot = false;
    private boolean activatedForBreak = false;
    private boolean activatedForHop = false;
    private boolean activatedForAFK = false;
    
    private static final int MAX_PLAYERS_BEFORE_HOP = 1;
    
    public PrepareForBreak(Script script) {
        super(script);
    }
    
    public static void resetState() {
        lastCheckTime = 0;
    }
    
    private int getNearbyPlayerCount() {
        try {
            int playerCount = script.getWidgetManager().getMinimap().getPlayerPositions().size();
            return Math.max(0, playerCount - 1);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private boolean shouldHopDueToPlayers() {
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
        if (!setupDone) return false;
        if (currentlyThieving && lastXpGain.timeElapsed() < 1500) return false;
        
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < MIN_TIME_BETWEEN_CHECKS_MS) return false;
        lastCheckTime = now;
        
        activatedForWhiteDot = false;
        activatedForBreak = false;
        activatedForHop = false;
        activatedForAFK = false;
        
        if (shouldHopDueToPlayers()) {
            activatedForWhiteDot = true;
            script.log("BREAK", "White dot detected - " + getNearbyPlayerCount() + " player(s) nearby!");
            return true;
        }
        
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
            
            if (script.getProfileManager().isAFKEnabled() && script.getProfileManager().isDueToAFK()) {
                activatedForAFK = true;
                script.log("BREAK", "ProfileManager: Due for AFK pause");
                return true;
            }
        } catch (Exception e) {
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
        
        currentlyThieving = false;
        
        if (!tapOnTile(SAFETY_TILE)) {
            script.log("BREAK", "Failed to tap on safety tile, using walker fallback...");
            script.getWalker().walkTo(SAFETY_TILE);
        }
        
        script.pollFramesUntil(() -> isAtSafetyTile(), 3000);
        
        if (!isAtSafetyTile()) {
            script.log("BREAK", "Failed to reach safety tile");
            return true;
        }
        
        script.log("BREAK", "At safety tile - triggering " + reason);
        
        try {
            if (activatedForWhiteDot) {
                script.getProfileManager().forceHop();
                script.log("BREAK", "Forced white dot hop!");
            } else if (activatedForBreak) {
                script.getProfileManager().forceBreak();
                script.log("BREAK", "Forced scheduled break");
            } else if (activatedForHop) {
                script.getProfileManager().forceHop();
                script.log("BREAK", "Forced scheduled hop");
            } else if (activatedForAFK) {
                script.getProfileManager().forceAFK();
                script.log("BREAK", "Forced AFK pause");
            }
            
            StartThieving.resetAfterBreak();
            
        } catch (Exception e) {
            script.log("BREAK", "Error executing " + reason + ": " + e.getMessage());
        }
        
        return true;
    }
    
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
