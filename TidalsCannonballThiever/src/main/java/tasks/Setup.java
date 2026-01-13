package tasks;

import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import com.osmb.api.ui.component.tabs.skill.SkillsTabComponent;
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

        // xp tracker initializes on first xp gain, no need to wait
        script.log("SETUP", "Setup complete! Starting cannonball thieving...");
        setupDone = true;

        if (script instanceof main.TidalsCannonballThiever) {
            ((main.TidalsCannonballThiever) script).initializeInventorySnapshot();
        }

        return true;
    }

    private static final int TARGET_ZOOM_LEVEL = 3;

    private void checkZoomLevel() {
        try {
            boolean opened = script.getWidgetManager().getSettings().open();
            if (!opened) {
                script.log("SETUP", "Could not open settings tab to check zoom");
                return;
            }

            script.pollFramesHuman(() -> false, script.random(200, 400));
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
            script.pollFramesHuman(() -> false, script.random(200, 400));

        } catch (Exception e) {
            script.log("SETUP", "Error checking zoom level: " + e.getMessage());
        }
    }
}
