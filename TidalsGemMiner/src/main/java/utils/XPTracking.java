package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;

import java.util.Map;

public class XPTracking {

    private final ScriptCore core;
    private boolean initialized = false;
    private boolean recalibrated = false;

    // custom trackers - OSMB's core.getXPTrackers() returns null at startup
    private XPTracker customMiningTracker;
    private XPTracker customCraftingTracker;
    private double customMiningXpGained = 0.0;
    private double customCraftingXpGained = 0.0;

    public XPTracking(ScriptCore core) {
        this.core = core;
    }

    // try to get actual XP from built-in tracker (may work for some skills)
    public static int tryGetActualXp(ScriptCore core, SkillType skill) {
        try {
            Map<SkillType, XPTracker> trackers = core.getXPTrackers();
            if (trackers != null && trackers.containsKey(skill)) {
                XPTracker tracker = trackers.get(skill);
                if (tracker != null) {
                    return (int) tracker.getXp();
                }
            }
        } catch (Exception e) {
            // fall through
        }
        return -1;
    }

    // initialize mining tracker with actual current XP (preferred)
    public void initMiningTracker(int startingLevel, int actualCurrentXp) {
        customMiningTracker = new XPTracker(core, actualCurrentXp);
        customMiningXpGained = 0.0;
        initialized = true;
        if (core instanceof com.osmb.api.script.Script) {
            ((com.osmb.api.script.Script) core).log("XP", "Mining tracker initialized with actual XP: " + actualCurrentXp + " (level " + startingLevel + ")");
        }
    }

    // initialize mining tracker with starting level (fallback)
    public void initMiningTracker(int startingLevel) {
        XPTracker temp = new XPTracker(core, 0);
        int startingXp = temp.getExperienceForLevel(startingLevel);
        customMiningTracker = new XPTracker(core, startingXp);
        customMiningXpGained = 0.0;
        initialized = true;
        if (core instanceof com.osmb.api.script.Script) {
            ((com.osmb.api.script.Script) core).log("XP", "Mining tracker initialized for level " + startingLevel + " (startXp=" + startingXp + ", estimated)");
        }
    }

    // initialize crafting tracker with actual current XP (preferred)
    public void initCraftingTracker(int startingLevel, int actualCurrentXp) {
        customCraftingTracker = new XPTracker(core, actualCurrentXp);
        customCraftingXpGained = 0.0;
        if (core instanceof com.osmb.api.script.Script) {
            ((com.osmb.api.script.Script) core).log("XP", "Crafting tracker initialized with actual XP: " + actualCurrentXp + " (level " + startingLevel + ")");
        }
    }

    // initialize crafting tracker with starting level (fallback)
    public void initCraftingTracker(int startingLevel) {
        XPTracker temp = new XPTracker(core, 0);
        int startingXp = temp.getExperienceForLevel(startingLevel);
        customCraftingTracker = new XPTracker(core, startingXp);
        customCraftingXpGained = 0.0;
        if (core instanceof com.osmb.api.script.Script) {
            ((com.osmb.api.script.Script) core).log("XP", "Crafting tracker initialized for level " + startingLevel + " (startXp=" + startingXp + ", estimated)");
        }
    }

    // increment mining xp when gems are mined
    public void addMiningXp(double xp) {
        customMiningXpGained += xp;
        if (customMiningTracker != null) {
            customMiningTracker.incrementXp(xp);
        }

        // try to recalibrate once after first XP gain
        if (!recalibrated) {
            tryRecalibrate();
        }
    }

    // increment crafting xp when gems are cut
    public void addCraftingXp(double xp) {
        customCraftingXpGained += xp;
        if (customCraftingTracker != null) {
            customCraftingTracker.incrementXp(xp);
        }
    }

    // attempt to recalibrate custom tracker using built-in tracker (if now available)
    private void tryRecalibrate() {
        recalibrated = true;
        try {
            int actualMiningXp = tryGetActualXp(core, SkillType.MINING);
            if (actualMiningXp > 0 && customMiningTracker != null) {
                double ourXp = customMiningTracker.getXp();
                double diff = actualMiningXp - ourXp;
                if (Math.abs(diff) > 100) {
                    // significant difference - recreate tracker with correct starting XP
                    int trueStartXp = actualMiningXp - (int) customMiningXpGained;
                    customMiningTracker = new XPTracker(core, trueStartXp);
                    customMiningTracker.incrementXp(customMiningXpGained);
                    if (core instanceof com.osmb.api.script.Script) {
                        ((com.osmb.api.script.Script) core).log("XP", "Recalibrated mining tracker: startXp=" + trueStartXp + ", currentXp=" + actualMiningXp + " (was off by " + (int)diff + ")");
                    }
                }
            }

            int actualCraftingXp = tryGetActualXp(core, SkillType.CRAFTING);
            if (actualCraftingXp > 0 && customCraftingTracker != null) {
                double ourXp = customCraftingTracker.getXp();
                double diff = actualCraftingXp - ourXp;
                if (Math.abs(diff) > 100) {
                    int trueStartXp = actualCraftingXp - (int) customCraftingXpGained;
                    customCraftingTracker = new XPTracker(core, trueStartXp);
                    customCraftingTracker.incrementXp(customCraftingXpGained);
                    if (core instanceof com.osmb.api.script.Script) {
                        ((com.osmb.api.script.Script) core).log("XP", "Recalibrated crafting tracker: startXp=" + trueStartXp + ", currentXp=" + actualCraftingXp + " (was off by " + (int)diff + ")");
                    }
                }
            }
        } catch (Exception e) {
            // recalibration failed, continue with estimated values
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    // mining tracker getters
    public XPTracker getMiningTracker() {
        if (customMiningTracker != null) {
            return customMiningTracker;
        }
        return getTracker(SkillType.MINING);
    }

    public double getMiningXpGained() {
        return customMiningXpGained;
    }

    public int getMiningXpPerHour() {
        XPTracker tracker = getMiningTracker();
        if (tracker == null) return 0;
        return tracker.getXpPerHour();
    }

    public int getMiningLevel() {
        XPTracker tracker = getMiningTracker();
        if (tracker == null) return 1;
        return tracker.getLevel();
    }

    public String getMiningTimeToNextLevel() {
        XPTracker tracker = getMiningTracker();
        if (tracker == null) return "-";
        return tracker.timeToNextLevelString();
    }

    // crafting tracker getters
    public XPTracker getCraftingTracker() {
        if (customCraftingTracker != null) {
            return customCraftingTracker;
        }
        return getTracker(SkillType.CRAFTING);
    }

    public double getCraftingXpGained() {
        return customCraftingXpGained;
    }

    public int getCraftingXpPerHour() {
        XPTracker tracker = getCraftingTracker();
        if (tracker == null) return 0;
        return tracker.getXpPerHour();
    }

    public int getCraftingLevel() {
        XPTracker tracker = getCraftingTracker();
        if (tracker == null) return 1;
        return tracker.getLevel();
    }

    public String getCraftingTimeToNextLevel() {
        XPTracker tracker = getCraftingTracker();
        if (tracker == null) return "-";
        return tracker.timeToNextLevelString();
    }

    private XPTracker getTracker(SkillType skill) {
        Map<SkillType, XPTracker> trackers = core.getXPTrackers();
        if (trackers == null) return null;
        return trackers.get(skill);
    }
}
