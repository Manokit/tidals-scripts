package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;

import java.util.Map;

public class XPTracking {

    private final ScriptCore core;

    public XPTracking(ScriptCore core) {
        this.core = core;
    }

    // internal helper to retrieve a specific tracker
    private XPTracker getTracker(SkillType skill) {
        Map<SkillType, XPTracker> trackers = core.getXPTrackers();
        if (trackers == null) return null;
        return trackers.get(skill);
    }

    // crafting-specific methods
    public XPTracker getCraftingTracker() {
        return getTracker(SkillType.CRAFTING);
    }

    public double getCraftingXpGained() {
        XPTracker tracker = getCraftingTracker();
        if (tracker == null) return 0.0;
        return tracker.getXpGained();
    }

    public int getCraftingXpPerHour() {
        XPTracker tracker = getCraftingTracker();
        if (tracker == null) return 0;
        return tracker.getXpPerHour();
    }

    public int getCraftingLevel() {
        XPTracker tracker = getCraftingTracker();
        if (tracker == null) return 0;
        return tracker.getLevel();
    }

    public String getCraftingTimeToNextLevel() {
        XPTracker tracker = getCraftingTracker();
        if (tracker == null) return "-";
        return tracker.timeToNextLevelString();
    }

    public void checkXP() {
        // called in onNewFrame() - no actual method call needed
        // xp trackers update automatically
        XPTracker tracker = getCraftingTracker();
        if (tracker == null) return;
        
        // could add custom logic here if needed
        double currentXp = tracker.getXpGained();
    }
}
