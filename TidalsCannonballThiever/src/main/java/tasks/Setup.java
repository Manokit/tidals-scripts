package tasks;

import com.osmb.api.script.Script;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import com.osmb.api.ui.component.tabs.skill.SkillsTabComponent;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class Setup extends Task {
    private static final int REQUIRED_THIEVING_LEVEL = 87;

    public Setup(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        return !setupDone;
    }

    @Override
    public boolean execute() {
        task = "Setup";

        // check thieving level
        SkillsTabComponent.SkillLevel thievingSkillLevel = script.getWidgetManager()
                .getSkillTab()
                .getSkillLevel(SkillType.THIEVING);

        if (thievingSkillLevel == null) {
            script.log("SETUP", "Failed to get Thieving skill level.");
            return false;
        }

        int thievingLevel = thievingSkillLevel.getLevel();
        startLevel = thievingLevel;
        currentLevel = thievingLevel;

        script.log("SETUP", "Current Thieving level: " + thievingLevel);

        if (thievingLevel < REQUIRED_THIEVING_LEVEL) {
            script.log("SETUP", "ERROR: Requires " + REQUIRED_THIEVING_LEVEL + " Thieving! You have " + thievingLevel);
            script.stop();
            return false;
        }

        // get screen dimensions
        Rectangle screenRect = script.getScreen().getBounds();
        screenWidth = screenRect.width;
        screenHeight = screenRect.height;

        script.log("SETUP", "Screen dimensions: " + screenWidth + "x" + screenHeight);

        if (screenWidth == 0 || screenHeight == 0) {
            script.log("SETUP", "ERROR: Invalid screen dimensions!");
            script.stop();
            return false;
        }

        checkZoomLevel();

        // initialize custom xp tracker - try to get actual XP first
        int actualXp = utils.XPTracking.tryGetActualXp(script, SkillType.THIEVING);
        if (actualXp > 0) {
            xpTracking.initCustomTracker(thievingLevel, actualXp);
        } else {
            // fall back to level-based (loses progress within level)
            xpTracking.initCustomTracker(thievingLevel);
        }

        script.log("SETUP", "Setup complete! Starting cannonball thieving...");
        setupDone = true;

        if (script instanceof main.TidalsCannonballThiever) {
            ((main.TidalsCannonballThiever) script).initializeInventorySnapshot();
        }

        return true;
    }

    private static final int TARGET_ZOOM_LEVEL = 3;

    private void checkZoomLevel() {
        boolean opened = script.getWidgetManager().getSettings().open();
        if (!opened) {
            script.log("SETUP", "Could not open settings tab to check zoom");
            return;
        }

        script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(200, 800, 0.002));
        UIResult<Integer> zoomResult = script.getWidgetManager().getSettings().getZoomLevel();
        if (zoomResult != null && zoomResult.isFound()) {
            int currentZoom = zoomResult.get();
            script.log("SETUP", "Current zoom level: " + currentZoom);

            if (currentZoom != TARGET_ZOOM_LEVEL) {
                script.log("SETUP", "Setting zoom level to " + TARGET_ZOOM_LEVEL + "...");
                boolean set = script.getWidgetManager().getSettings().setZoomLevel(TARGET_ZOOM_LEVEL);
                if (set) {
                    script.log("SETUP", "Zoom level set to " + TARGET_ZOOM_LEVEL);
                } else {
                    script.log("SETUP", "Failed to set zoom level");
                }
            } else {
                script.log("SETUP", "Zoom level already optimal");
            }
        } else {
            script.log("SETUP", "Could not read zoom level, attempting to set anyway...");
            script.getWidgetManager().getSettings().setZoomLevel(TARGET_ZOOM_LEVEL);
        }

        script.getWidgetManager().getSettings().close();
        script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(200, 800, 0.002));
    }
}
