package tasks;

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

        // Check current zoom level
        checkZoomLevel();

        // Wait for XP tracker to be ready before completing setup
        script.log("SETUP", "Waiting for XP tracker to initialize...");
        
        boolean xpTrackerReady = script.pollFramesUntil(() -> {
            if (xpTracking == null) return false;
            return xpTracking.getThievingTracker() != null;
        }, 5000);
        
        if (!xpTrackerReady) {
            script.log("SETUP", "WARNING: XP tracker not ready after 5s, continuing anyway...");
        } else {
            // Small delay to let tracker stabilize
            script.pollFramesHuman(() -> false, script.random(300, 500));
            
            // Initialize XP tracking with current value
            if (xpTracking.initialize()) {
                script.log("SETUP", "XP tracking initialized successfully");
            } else {
                script.log("SETUP", "WARNING: Failed to initialize XP tracking");
            }
        }
        
        script.log("SETUP", "Setup complete! Starting cannonball thieving...");
        setupDone = true;
        
        // Initialize inventory snapshot for item tracking
        if (script instanceof main.TidalsCannonballThiever) {
            ((main.TidalsCannonballThiever) script).initializeInventorySnapshot();
        }

        return true;
    }
    
    // Target zoom level for optimal stall visibility
    private static final int TARGET_ZOOM_LEVEL = 3;
    
    /**
     * Check zoom level and set to target if needed
     */
    private void checkZoomLevel() {
        try {
            // Open settings tab to read zoom level
            boolean opened = script.getWidgetManager().getSettings().open();
            if (!opened) {
                script.log("SETUP", "Could not open settings tab to check zoom");
                return;
            }
            
            // Small delay for tab to fully open
            script.pollFramesHuman(() -> false, script.random(200, 400));
            
            // Get current zoom level
            UIResult<Integer> zoomResult = script.getWidgetManager().getSettings().getZoomLevel();
            if (zoomResult != null && zoomResult.isFound()) {
                int currentZoom = zoomResult.get();
                script.log("SETUP", "Current zoom level: " + currentZoom);
                
                // Set to target if different
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
            
            // Close settings tab
            script.getWidgetManager().getSettings().close();
            script.pollFramesHuman(() -> false, script.random(200, 400));
            
        } catch (Exception e) {
            script.log("SETUP", "Error checking zoom level: " + e.getMessage());
        }
    }
}
