package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;

import static main.TidalsCannonballThiever.lastXpGain;

import java.util.Map;

public class XPTracking {

    private final ScriptCore core;
    private double lastKnownXp = 0.0;

    public XPTracking(ScriptCore core) {
        this.core = core;
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
