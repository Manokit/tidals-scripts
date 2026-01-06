package tasks;

import com.osmb.api.script.Script;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import com.osmb.api.ui.component.tabs.skill.SkillsTabComponent;
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

        // Check thieving level
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

        // Get screen dimensions
        Rectangle screenRect = script.getScreen().getBounds();
        screenWidth = screenRect.width;
        screenHeight = screenRect.height;

        script.log("SETUP", "Screen dimensions: " + screenWidth + "x" + screenHeight);

        if (screenWidth == 0 || screenHeight == 0) {
            script.log("SETUP", "ERROR: Invalid screen dimensions!");
            script.stop();
            return false;
        }

        script.log("SETUP", "Setup complete! Starting cannonball thieving...");
        setupDone = true;

        return true;
    }
}
