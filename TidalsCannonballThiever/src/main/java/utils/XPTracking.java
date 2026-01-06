package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;

import static main.TidalsCannonballThiever.lastXpGain;
import static main.TidalsCannonballThiever.cannonballsStolen;

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

    public void checkXP() {
        XPTracker tracker = getThievingTracker();
        if (tracker == null) return;

        double currentXp = tracker.getXpGained();

        // only reset timer when XP actually increases (new xp drop)
        if (currentXp > lastKnownXp) {
            lastXpGain.reset();
            // estimate cannonballs stolen (roughly 36 xp per steal)
            double xpGained = currentXp - lastKnownXp;
            int steals = (int) Math.round(xpGained / 36.0);
            cannonballsStolen += Math.max(1, steals);
            lastKnownXp = currentXp;
        }
    }
}
