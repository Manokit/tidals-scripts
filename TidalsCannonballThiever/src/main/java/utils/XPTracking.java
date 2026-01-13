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

    // custom thieving tracker - OSMB's core.getXPTrackers() returns null
    private XPTracker customThievingTracker;
    private double customXpGained = 0.0;

    public XPTracking(ScriptCore core) {
        this.core = core;
    }

    // initialize custom tracker with starting level
    public void initCustomTracker(int startingLevel) {
        XPTracker temp = new XPTracker(core, 0);
        int startingXp = temp.getExperienceForLevel(startingLevel);
        customThievingTracker = new XPTracker(core, startingXp);
        customXpGained = 0.0;
        initialized = true;
        if (core instanceof com.osmb.api.script.Script) {
            ((com.osmb.api.script.Script) core).log("XP", "Custom tracker initialized for level " + startingLevel + " (startXp=" + startingXp + ")");
        }
    }

    // increment xp manually when steal is detected
    public void addThievingXp(double xp) {
        customXpGained += xp;
        if (customThievingTracker != null) {
            customThievingTracker.incrementXp(xp);
        }
        lastXpGain.reset();
        if (core instanceof com.osmb.api.script.Script) {
            ((com.osmb.api.script.Script) core).log("XP", "Added " + xp + " XP (total gained: " + customXpGained + ")");
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public double getCurrentXp() {
        // return custom tracked XP
        return customXpGained;
    }

    private XPTracker getTracker(SkillType skill) {
        Map<SkillType, XPTracker> trackers = core.getXPTrackers();
        if (trackers == null) return null;
        return trackers.get(skill);
    }

    public XPTracker getThievingTracker() {
        // prefer custom tracker, fall back to OSMB tracker
        if (customThievingTracker != null) {
            return customThievingTracker;
        }
        return getTracker(SkillType.THIEVING);
    }

    public double getThievingXpGained() {
        // return custom tracked XP for cycle detection
        return customXpGained;
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
        // with custom tracking, we use addThievingXp() to track XP gains
        // this method is now just for compatibility
        return false;
    }

    public void checkXP() {
        // no-op with custom tracking
    }
}
