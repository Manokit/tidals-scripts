package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;

import static main.TidalsCannonballThiever.lastXpGain;

import java.util.Map;

public class XPTracking {

    private final ScriptCore core;
    private double lastKnownXp = -1.0;  // Start at -1 to indicate not initialized
    private boolean initialized = false;

    public XPTracking(ScriptCore core) {
        this.core = core;
    }
    
    /**
     * Initialize XP tracking with current tracker value.
     * Call this during setup AFTER waiting for tracker to be ready.
     * @return true if initialization successful
     */
    public boolean initialize() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) {
            return false;
        }
        
        // Get current XP from tracker - this is the baseline
        double currentXp = tracker.getXpGained();
        lastKnownXp = currentXp;
        initialized = true;
        
        if (core instanceof com.osmb.api.script.Script) {
            ((com.osmb.api.script.Script) core).log("XP", "XP tracking initialized. Current XP gained: " + currentXp);
        }
        
        return true;
    }
    
    /**
     * Check if XP tracking has been properly initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get the current XP from tracker (for external initialization)
     */
    public double getCurrentXp() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) return 0.0;
        return tracker.getXpGained();
    }

    // --- Internal helper to retrieve a specific tracker ---
    private XPTracker getTracker(SkillType skill) {
        Map<SkillType, XPTracker> trackers = core.getXPTrackers();
        if (trackers == null) return null;
        return trackers.get(skill);
    }

    // --- Thieving-specific methods ---
    public XPTracker getThievingTracker() {
        return getTracker(SkillType.THIEVING);
    }

    public double getThievingXpGained() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) return 0.0;
        return tracker.getXpGained();
    }

    public int getThievingXpPerHour() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) return 0;
        return tracker.getXpPerHour();
    }

    public int getThievingLevel() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) return 0;
        return tracker.getLevel();
    }

    public String getThievingTimeToNextLevel() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) return "-";
        return tracker.timeToNextLevelString();
    }

    /**
     * Check if XP was gained since last check
     * @return true if XP increased (successful steal)
     */
    public boolean checkXPAndReturnIfGained() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) return false;

        double currentXp = tracker.getXpGained();
        
        // If not initialized yet, initialize now with current value
        // This prevents false positives on first check
        if (!initialized || lastKnownXp < 0) {
            lastKnownXp = currentXp;
            initialized = true;
            return false;
        }

        // Check if XP actually increased (new xp drop = successful steal)
        if (currentXp > lastKnownXp) {
            lastXpGain.reset();
            lastKnownXp = currentXp;
            return true;
        }
        return false;
    }
    
    /**
     * Legacy method for backwards compatibility
     */
    public void checkXP() {
        checkXPAndReturnIfGained();
    }
}
