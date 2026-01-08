package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;

import static main.TidalsCannonballThiever.lastXpGain;

import java.util.Map;

public class XPTracking {

    private final ScriptCore core;
    private double lastKnownXp = -1.0;
    private boolean initialized = false;

    public XPTracking(ScriptCore core) {
        this.core = core;
    }
    
    public boolean initialize() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) {
            return false;
        }
        
        double currentXp = tracker.getXpGained();
        lastKnownXp = currentXp;
        initialized = true;
        
        if (core instanceof com.osmb.api.script.Script) {
            ((com.osmb.api.script.Script) core).log("XP", "XP tracking initialized. Current XP gained: " + currentXp);
        }
        
        return true;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public double getCurrentXp() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) return 0.0;
        return tracker.getXpGained();
    }

    private XPTracker getTracker(SkillType skill) {
        Map<SkillType, XPTracker> trackers = core.getXPTrackers();
        if (trackers == null) return null;
        return trackers.get(skill);
    }

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

    public boolean checkXPAndReturnIfGained() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) return false;

        double currentXp = tracker.getXpGained();
        
        if (!initialized || lastKnownXp < 0) {
            lastKnownXp = currentXp;
            initialized = true;
            return false;
        }

        if (currentXp > lastKnownXp) {
            lastXpGain.reset();
            lastKnownXp = currentXp;
            return true;
        }
        return false;
    }
    
    public void checkXP() {
        checkXPAndReturnIfGained();
    }
}
