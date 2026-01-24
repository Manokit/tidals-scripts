package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;

import static main.TidalsCannonballThiever.lastXpGain;

import java.util.Map;

public class XPTracking {

    private final ScriptCore core;
    private boolean initialized = false;
    private boolean recalibrated = false;

    // custom thieving tracker - OSMB's core.getXPTrackers() returns null at startup
    private XPTracker customThievingTracker;
    private double customXpGained = 0.0;

    public XPTracking(ScriptCore core) {
        this.core = core;
    }

    // try to get actual XP from built-in tracker (may work for some skills)
    public static int tryGetActualXp(ScriptCore core, SkillType skill) {
        Map<SkillType, XPTracker> trackers = core.getXPTrackers();
        if (trackers != null && trackers.containsKey(skill)) {
            XPTracker tracker = trackers.get(skill);
            if (tracker != null) {
                return (int) tracker.getXp();
            }
        }
        return -1;
    }

    // initialize custom tracker with actual current XP (preferred)
    public void initCustomTracker(int startingLevel, int actualCurrentXp) {
        customThievingTracker = new XPTracker(core, actualCurrentXp);
        customXpGained = 0.0;
        initialized = true;
        if (core instanceof com.osmb.api.script.Script) {
            ((com.osmb.api.script.Script) core).log("XP", "Custom tracker initialized with actual XP: " + actualCurrentXp + " (level " + startingLevel + ")");
        }
    }

    // initialize custom tracker with starting level (fallback - loses progress within level)
    public void initCustomTracker(int startingLevel) {
        XPTracker temp = new XPTracker(core, 0);
        int startingXp = temp.getExperienceForLevel(startingLevel);
        customThievingTracker = new XPTracker(core, startingXp);
        customXpGained = 0.0;
        initialized = true;
        if (core instanceof com.osmb.api.script.Script) {
            ((com.osmb.api.script.Script) core).log("XP", "Custom tracker initialized for level " + startingLevel + " (startXp=" + startingXp + ", estimated)");
        }
    }

    // increment xp manually when steal is detected
    public void addThievingXp(double xp) {
        customXpGained += xp;
        if (customThievingTracker != null) {
            customThievingTracker.incrementXp(xp);
        }
        lastXpGain.reset();

        // try to recalibrate once after first XP gain (built-in tracker may now be available)
        if (!recalibrated) {
            tryRecalibrate();
        }
    }

    // attempt to recalibrate custom tracker using built-in tracker (if now available)
    private void tryRecalibrate() {
        recalibrated = true;
        int actualXp = tryGetActualXp(core, SkillType.THIEVING);
        if (actualXp > 0 && customThievingTracker != null) {
            double ourXp = customThievingTracker.getXp();
            double diff = actualXp - ourXp;
            if (Math.abs(diff) > 100) {
                // significant difference - recreate tracker with correct starting XP
                // subtract XP we've already gained to get true starting XP
                int trueStartXp = actualXp - (int) customXpGained;
                customThievingTracker = new XPTracker(core, trueStartXp);
                // re-add the XP we've gained so far
                customThievingTracker.incrementXp(customXpGained);
                if (core instanceof com.osmb.api.script.Script) {
                    ((com.osmb.api.script.Script) core).log("XP", "Recalibrated tracker: startXp=" + trueStartXp + ", currentXp=" + actualXp + " (was off by " + (int)diff + ")");
                }
            }
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
