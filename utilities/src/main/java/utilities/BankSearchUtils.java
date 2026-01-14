package utilities;

import com.osmb.api.input.PhysicalKey;
import com.osmb.api.input.TouchType;
import com.osmb.api.script.Script;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;

/**
 * Bank search utilities for searching items by name.
 *
 * Provides methods to:
 * - Open the bank search box
 * - Type search queries
 * - Clear search results
 * - Search and withdraw items by name
 *
 * Usage:
 *   // open search box
 *   if (BankSearchUtils.openSearch(script)) {
 *       // type item name
 *       script.getKeyboard().type("shark");
 *   }
 *
 * Note: Bank must be visible before calling these methods.
 */
public class BankSearchUtils {

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int SEARCH_OPEN_ATTEMPTS = 5;

    /**
     * Opens the bank search box by pressing the search hotkey.
     *
     * In OSRS, pressing a letter key while the bank is open activates the search.
     * We press a neutral key first, then detect if search input is active.
     *
     * @param script the script instance
     * @return true if search box opened successfully
     */
    public static boolean openSearch(Script script) {
        return openSearch(script, DEFAULT_TIMEOUT);
    }

    /**
     * Opens the bank search box by pressing the search hotkey.
     *
     * @param script the script instance
     * @param timeout max time to wait for search box to open
     * @return true if search box opened successfully
     */
    public static boolean openSearch(Script script, int timeout) {
        // pre-condition: bank must be visible
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankSearchUtils.class, "bank not visible - cannot open search");
            return false;
        }

        // check if search is already active (TEXT_SEARCH dialogue visible)
        if (isSearchActive(script)) {
            script.log(BankSearchUtils.class, "search already active");
            return true;
        }

        // try to open search using keyboard shortcut
        // in OSRS, typing any letter while bank is open activates search
        for (int attempt = 1; attempt <= SEARCH_OPEN_ATTEMPTS; attempt++) {
            script.log(BankSearchUtils.class, "opening search attempt " + attempt + "/" + SEARCH_OPEN_ATTEMPTS);

            // press and release a key to activate search (backspace is safe - clears any partial)
            script.getKeyboard().pressKey(TouchType.DOWN, PhysicalKey.BACKSPACE);
            script.getKeyboard().pressKey(TouchType.UP, PhysicalKey.BACKSPACE);

            // wait for search to become active
            boolean opened = script.pollFramesUntil(() -> isSearchActive(script), timeout);

            if (opened) {
                script.log(BankSearchUtils.class, "search opened successfully");
                // small delay for search box to be ready
                script.pollFramesHuman(() -> false, script.random(100, 200));
                return true;
            }

            // small delay between attempts
            script.pollFramesHuman(() -> false, script.random(200, 400));
        }

        script.log(BankSearchUtils.class, "failed to open search after " + SEARCH_OPEN_ATTEMPTS + " attempts");
        return false;
    }

    /**
     * Checks if the bank search is currently active.
     *
     * @param script the script instance
     * @return true if search input is active
     */
    public static boolean isSearchActive(Script script) {
        // check for TEXT_SEARCH dialogue type which indicates search input is active
        DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
        return type == DialogueType.TEXT_SEARCH || type == DialogueType.ITEM_SEARCH;
    }

    /**
     * Types a search query into the bank search box.
     *
     * Pre-condition: Search box should be open (will attempt to open if not).
     *
     * @param script the script instance
     * @param itemName the item name to search for
     * @return true if typing completed successfully
     */
    public static boolean typeSearch(Script script, String itemName) {
        // validate input
        if (itemName == null || itemName.isEmpty()) {
            script.log(BankSearchUtils.class, "cannot type search - item name is empty or null");
            return false;
        }

        // ensure search is open
        if (!isSearchActive(script)) {
            script.log(BankSearchUtils.class, "search not active - attempting to open");
            if (!openSearch(script)) {
                script.log(BankSearchUtils.class, "failed to open search for typing");
                return false;
            }
        }

        // type the item name
        script.log(BankSearchUtils.class, "typing search: " + itemName);
        script.getKeyboard().type(itemName);

        // human-like delay after typing for results to filter
        script.pollFramesHuman(() -> false, script.random(200, 400));

        script.log(BankSearchUtils.class, "search typed successfully");
        return true;
    }

    /**
     * Clears the bank search box by pressing ESC.
     *
     * In OSRS, pressing ESC while search is active clears the search
     * and returns to showing all bank items.
     *
     * @param script the script instance
     * @return true if search was cleared (or wasn't active)
     */
    public static boolean clearSearch(Script script) {
        return clearSearch(script, DEFAULT_TIMEOUT);
    }

    /**
     * Clears the bank search box by pressing ESC.
     *
     * @param script the script instance
     * @param timeout max time to wait for search to clear
     * @return true if search was cleared (or wasn't active)
     */
    public static boolean clearSearch(Script script, int timeout) {
        // if search not active, nothing to clear
        if (!isSearchActive(script)) {
            script.log(BankSearchUtils.class, "search not active - nothing to clear");
            return true;
        }

        script.log(BankSearchUtils.class, "clearing search with ESC");

        // press ESC to close search
        script.getKeyboard().pressKey(TouchType.DOWN, PhysicalKey.ESCAPE);
        script.getKeyboard().pressKey(TouchType.UP, PhysicalKey.ESCAPE);

        // wait for search to become inactive
        boolean cleared = script.pollFramesUntil(() -> !isSearchActive(script), timeout);

        if (cleared) {
            script.log(BankSearchUtils.class, "search cleared successfully");
            // human-like delay after clearing
            script.pollFramesHuman(() -> false, script.random(100, 200));
            return true;
        }

        script.log(BankSearchUtils.class, "failed to clear search");
        return false;
    }
}
