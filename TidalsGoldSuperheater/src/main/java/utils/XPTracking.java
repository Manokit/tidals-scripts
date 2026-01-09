package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;

import java.util.Map;

public class XPTracking {

    private final ScriptCore core;

    // custom smithing tracker - superheat doesn't show smithing xp drops
    private XPTracker customSmithingTracker;

    public XPTracking(ScriptCore core) {
        this.core = core;
    }

    private XPTracker getTracker(SkillType skill) {
        Map<SkillType, XPTracker> trackers = core.getXPTrackers();
        if (trackers == null) return null;
        return trackers.get(skill);
    }

    public XPTracker getMagicTracker() {
        return getTracker(SkillType.MAGIC);
    }

    public double getMagicXpGained() {
        XPTracker tracker = getMagicTracker();
        if (tracker == null) return 0.0;
        return tracker.getXpGained();
    }

    public int getMagicXpPerHour() {
        XPTracker tracker = getMagicTracker();
        if (tracker == null) return 0;
        return tracker.getXpPerHour();
    }

    public int getMagicLevel() {
        XPTracker tracker = getMagicTracker();
        if (tracker == null) return 0;
        return tracker.getLevel();
    }

    public String getMagicTimeToNextLevel() {
        XPTracker tracker = getMagicTracker();
        if (tracker == null) return "-";
        return tracker.timeToNextLevelString();
    }

    // initialize custom smithing tracker with starting level
    // uses xp table lookup since we can't read exact xp from game
    public void initSmithingTracker(int startingLevel) {
        // create temp tracker to lookup xp for level, then create real tracker
        XPTracker temp = new XPTracker(core, 0);
        int startingXp = temp.getExperienceForLevel(startingLevel);
        customSmithingTracker = new XPTracker(core, startingXp);
    }

    // increment smithing xp manually when superheating
    public void addSmithingXp(double xp) {
        if (customSmithingTracker != null) {
            customSmithingTracker.incrementXp(xp);
        }
    }

    public XPTracker getSmithingTracker() {
        // use custom tracker since superheat doesn't show smithing xp drops
        return customSmithingTracker;
    }

    public double getSmithingXpGained() {
        XPTracker tracker = getSmithingTracker();
        if (tracker == null) return 0.0;
        return tracker.getXpGained();
    }

    public int getSmithingXpPerHour() {
        XPTracker tracker = getSmithingTracker();
        if (tracker == null) return 0;
        return tracker.getXpPerHour();
    }

    public int getSmithingLevel() {
        XPTracker tracker = getSmithingTracker();
        if (tracker == null) return 0;
        return tracker.getLevel();
    }

    public String getSmithingTimeToNextLevel() {
        XPTracker tracker = getSmithingTracker();
        if (tracker == null) return "-";
        return tracker.timeToNextLevelString();
    }

    public void checkXP() {
        // trackers update on access
        getMagicTracker();
        getSmithingTracker();
    }
}
