package tasks;

import com.osmb.api.script.Script;
import com.osmb.api.ui.chatbox.dialogue.Dialogue;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import utils.Task;

import static main.TidalsCannonballThiever.*;

public class DismissDialogue extends Task {
    private static final long STUCK_THRESHOLD_MS = 10000; // 10 seconds

    public DismissDialogue(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // only check when actively thieving and not depositing
        if (!currentlyThieving || doingDepositRun) return false;

        // only trigger if we haven't gained XP in 10+ seconds
        if (lastXpGain.timeElapsed() < STUCK_THRESHOLD_MS) return false;

        // check for CHAT_DIALOGUE or TAP_HERE_TO_CONTINUE
        try {
            Dialogue dialogue = script.getWidgetManager().getDialogue();
            if (dialogue == null || !dialogue.isVisible()) return false;

            DialogueType type = dialogue.getDialogueType();
            return type == DialogueType.CHAT_DIALOGUE ||
                   type == DialogueType.TAP_HERE_TO_CONTINUE;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean execute() {
        task = "Dismissing dialogue...";
        script.log("RECOVERY", "Stuck in dialogue detected! Dismissing...");

        try {
            Dialogue dialogue = script.getWidgetManager().getDialogue();

            // dismiss dialogue repeatedly until closed
            int maxAttempts = 10;
            for (int i = 0; i < maxAttempts && dialogue.isVisible(); i++) {
                dialogue.continueChatDialogue();
                script.pollFramesHuman(() -> !dialogue.isVisible(), script.random(500, 800));
            }

            // reset thieving state
            currentlyThieving = false;
            script.log("RECOVERY", "Dialogue dismissed - resetting to stall");

            // enable guard sync for clean restart
            if (guardTracker != null) {
                StartThieving.resetForNewCycle();
                guardTracker.resetCbCycle();
                guardTracker.resetGuardTracking();
                guardTracker.enableGuardSync();
            }

        } catch (Exception e) {
            script.log("RECOVERY", "Error dismissing dialogue: " + e.getMessage());
            currentlyThieving = false;
        }

        return true;
    }
}
