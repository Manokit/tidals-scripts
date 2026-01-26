package utilities;

import com.osmb.api.script.Script;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.utils.RandomUtils;

/**
 * Dialogue utilities for handling game dialogues.
 *
 * Provides helpers for:
 * - Level up dialogues
 * - Item selection dialogues
 * - Waiting for specific dialogue types
 *
 * Usage:
 *   if (DialogueUtils.isLevelUp(script)) {
 *       DialogueUtils.handleLevelUp(script);
 *   }
 *   DialogueUtils.waitForDialogue(script, DialogueType.ITEM_OPTION, 5000);
 *   DialogueUtils.selectItem(script, itemId);
 */
public class DialogueUtils {

    private static final int DEFAULT_TIMEOUT = 2000;

    /**
     * Check if a level up dialogue is showing.
     *
     * @param script the script instance
     * @return true if level up dialogue is visible
     */
    public static boolean isLevelUp(Script script) {
        DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
        return type == DialogueType.TAP_HERE_TO_CONTINUE;
    }

    /**
     * Handle a level up dialogue by clicking continue.
     * Waits for the dialogue to close.
     *
     * @param script the script instance
     * @return true if level up was handled
     */
    public static boolean handleLevelUp(Script script) {
        return handleLevelUp(script, DEFAULT_TIMEOUT);
    }

    /**
     * Handle a level up dialogue by clicking continue.
     * Waits for the dialogue to close.
     *
     * @param script the script instance
     * @param timeout max time to wait for dialogue to close
     * @return true if level up was handled
     */
    public static boolean handleLevelUp(Script script, int timeout) {
        DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
        if (type != DialogueType.TAP_HERE_TO_CONTINUE) {
            return false;
        }

        script.log(DialogueUtils.class, "handling level up");
        script.getWidgetManager().getDialogue().continueChatDialogue();

        return script.pollFramesUntil(() ->
            script.getWidgetManager().getDialogue().getDialogueType() != DialogueType.TAP_HERE_TO_CONTINUE,
            timeout
        );
    }

    /**
     * Check if any dialogue is currently showing.
     *
     * @param script the script instance
     * @return true if any dialogue is visible
     */
    public static boolean hasDialogue(Script script) {
        DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
        return type != null;
    }

    /**
     * Get the current dialogue type.
     *
     * @param script the script instance
     * @return the current dialogue type, or null if none
     */
    public static DialogueType getDialogueType(Script script) {
        return script.getWidgetManager().getDialogue().getDialogueType();
    }

    /**
     * Wait for a specific dialogue type to appear.
     *
     * @param script the script instance
     * @param expectedType the dialogue type to wait for
     * @param timeout max time to wait in ms
     * @return true if the expected dialogue appeared
     */
    public static boolean waitForDialogue(Script script, DialogueType expectedType, int timeout) {
        return script.pollFramesHuman(() -> {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            return type == expectedType;
        }, timeout);
    }

    /**
     * Wait for an item selection dialogue to appear.
     *
     * @param script the script instance
     * @param timeout max time to wait in ms
     * @return true if item selection dialogue appeared
     */
    public static boolean waitForItemDialogue(Script script, int timeout) {
        return waitForDialogue(script, DialogueType.ITEM_OPTION, timeout);
    }

    /**
     * Select an item in an item selection dialogue.
     * The item selection dialogue must already be open.
     *
     * @param script the script instance
     * @param itemId the item ID to select
     * @return true if selection succeeded
     */
    public static boolean selectItem(Script script, int itemId) {
        DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
        if (type != DialogueType.ITEM_OPTION) {
            script.log(DialogueUtils.class, "not in item selection dialogue");
            return false;
        }

        return script.getWidgetManager().getDialogue().selectItem(itemId);
    }

    /**
     * Select an item in an item selection dialogue with retry.
     * The item selection dialogue must already be open.
     *
     * @param script the script instance
     * @param itemId the item ID to select
     * @param maxAttempts max number of selection attempts
     * @return true if selection succeeded
     */
    public static boolean selectItemWithRetry(Script script, int itemId, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            if (type != DialogueType.ITEM_OPTION) {
                script.log(DialogueUtils.class, "item dialogue closed, attempt " + attempt + "/" + maxAttempts);
                script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(200, 400), true);
                continue;
            }

            script.log(DialogueUtils.class, "select item attempt " + attempt + "/" + maxAttempts);
            boolean success = script.getWidgetManager().getDialogue().selectItem(itemId);
            if (success) {
                return true;
            }

            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(300, 500), true);
        }

        script.log(DialogueUtils.class, "item selection failed after " + maxAttempts + " attempts");
        return false;
    }

    /**
     * Continue through a chat dialogue (click to continue).
     *
     * @param script the script instance
     * @return true if continue was clicked
     */
    public static boolean continueChatDialogue(Script script) {
        DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
        if (type != DialogueType.TAP_HERE_TO_CONTINUE) {
            return false;
        }
        return script.getWidgetManager().getDialogue().continueChatDialogue();
    }

    /**
     * Dismiss a level up or continue dialogue.
     * Clicks through the dialogue and waits for it to close.
     *
     * @param script the script instance
     * @param timeout max time to wait for dialogue to close
     * @return true if dialogue was dismissed or no dialogue was open
     */
    public static boolean dismissContinueDialogue(Script script, int timeout) {
        DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();

        if (type == null) {
            return true; // no dialogue
        }

        if (type == DialogueType.TAP_HERE_TO_CONTINUE) {
            script.getWidgetManager().getDialogue().continueChatDialogue();
            return script.pollFramesUntil(() -> {
                DialogueType current = script.getWidgetManager().getDialogue().getDialogueType();
                return current == null || current != DialogueType.TAP_HERE_TO_CONTINUE;
            }, timeout);
        }

        return false; // dialogue exists but can't dismiss it
    }

    /**
     * Dismiss a level up or continue dialogue with retry.
     * Retries clicking continue if dialogue doesn't close.
     *
     * @param script the script instance
     * @param maxAttempts max number of click attempts
     * @param timeout max time to wait for dialogue to close after each click
     * @return true if dialogue was dismissed or no dialogue was open
     */
    public static boolean dismissContinueDialogueWithRetry(Script script, int maxAttempts, int timeout) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();

            if (type == null) {
                return true; // no dialogue, success
            }

            if (type != DialogueType.TAP_HERE_TO_CONTINUE) {
                script.log(DialogueUtils.class, "dialogue type is " + type + ", not TAP_HERE_TO_CONTINUE");
                return false; // can't dismiss this type
            }

            script.log(DialogueUtils.class, "dismiss dialogue attempt " + attempt + "/" + maxAttempts);
            script.getWidgetManager().getDialogue().continueChatDialogue();

            boolean dismissed = script.pollFramesUntil(() -> {
                DialogueType current = script.getWidgetManager().getDialogue().getDialogueType();
                return current == null || current != DialogueType.TAP_HERE_TO_CONTINUE;
            }, timeout);

            if (dismissed) {
                return true;
            }

            // small delay before retry
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(200, 400));
        }

        script.log(DialogueUtils.class, "failed to dismiss dialogue after " + maxAttempts + " attempts");
        return false;
    }
}
