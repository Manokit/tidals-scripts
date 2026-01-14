package utilities;

import com.osmb.api.input.PhysicalKey;
import com.osmb.api.input.TouchType;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.script.Script;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

        script.log(BankSearchUtils.class, "clearing search with BACK key");

        // press BACK to close search (equivalent to ESC on desktop)
        script.getKeyboard().pressKey(TouchType.DOWN, PhysicalKey.BACK);
        script.getKeyboard().pressKey(TouchType.UP, PhysicalKey.BACK);

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

    /**
     * Searches for an item by name and withdraws the specified amount.
     *
     * This method:
     * 1. Gets the item name from the ID
     * 2. Types the name into bank search
     * 3. Waits for bank to filter results
     * 4. Withdraws the item by ID
     * 5. Clears the search
     *
     * @param script the script instance
     * @param itemId the item ID to withdraw
     * @param amount the amount to withdraw
     * @return true if withdrawal succeeded
     */
    public static boolean searchAndWithdraw(Script script, int itemId, int amount) {
        return searchAndWithdraw(script, itemId, amount, false);
    }

    /**
     * Searches for an item by name and withdraws the specified amount.
     * Uses scroll fallback by default if search doesn't find the item.
     *
     * @param script the script instance
     * @param itemId the item ID to withdraw
     * @param amount the amount to withdraw
     * @param keepSearchOpen if true, does not clear search after withdraw
     * @return true if withdrawal succeeded
     */
    public static boolean searchAndWithdraw(Script script, int itemId, int amount, boolean keepSearchOpen) {
        return searchAndWithdraw(script, itemId, amount, keepSearchOpen, true);
    }

    /**
     * Searches for an item by name and withdraws the specified amount.
     *
     * When search doesn't find the item and useScrollFallback is true, this method will:
     * 1. Clear the search
     * 2. Scroll to the top of the bank
     * 3. Scroll through the entire bank looking for the item
     * 4. Withdraw if found
     *
     * @param script the script instance
     * @param itemId the item ID to withdraw
     * @param amount the amount to withdraw
     * @param keepSearchOpen if true, does not clear search after withdraw (only applies to search success)
     * @param useScrollFallback if true, scrolls through bank when search fails to find item
     * @return true if withdrawal succeeded
     */
    public static boolean searchAndWithdraw(Script script, int itemId, int amount, boolean keepSearchOpen, boolean useScrollFallback) {
        // pre-condition: bank must be visible
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankSearchUtils.class, "bank not visible - cannot search and withdraw");
            return false;
        }

        // get item name from ID
        String itemName = script.getItemManager().getItemName(itemId);
        if (itemName == null || itemName.isEmpty()) {
            script.log(BankSearchUtils.class, "failed to get item name for id: " + itemId);
            return false;
        }

        script.log(BankSearchUtils.class, "searching for: " + itemName + " (id: " + itemId + ")");

        // type search query
        if (!typeSearch(script, itemName)) {
            script.log(BankSearchUtils.class, "failed to type search for: " + itemName);
            return false;
        }

        // wait for bank to filter results
        script.pollFramesHuman(() -> false, script.random(300, 500));

        // verify item exists in filtered bank
        ItemGroupResult bankItems = script.getWidgetManager().getBank().search(Set.of(itemId));
        boolean foundViaSearch = bankItems != null && bankItems.contains(itemId);

        if (!foundViaSearch) {
            script.log(BankSearchUtils.class, "item not found via search: " + itemName);

            // try scroll fallback if enabled
            if (useScrollFallback) {
                script.log(BankSearchUtils.class, "trying scroll fallback to find item");
                clearSearch(script);

                boolean foundViaScroll = findItemByScrolling(script, itemId);
                if (!foundViaScroll) {
                    script.log(BankSearchUtils.class, "item not found in bank after full scroll: " + itemName);
                    return false;
                }

                // found via scroll - withdraw it
                script.log(BankSearchUtils.class, "item found via scroll, withdrawing " + amount + " x " + itemName);
                boolean success = script.getWidgetManager().getBank().withdraw(itemId, amount);

                if (success) {
                    script.log(BankSearchUtils.class, "withdraw succeeded (via scroll fallback)");
                    script.pollFramesHuman(() -> false, script.random(200, 400));
                } else {
                    script.log(BankSearchUtils.class, "withdraw failed for: " + itemName);
                }

                return success;
            }

            // no scroll fallback - just clear and return
            clearSearch(script);
            return false;
        }

        // item found via search - withdraw it
        script.log(BankSearchUtils.class, "withdrawing " + amount + " x " + itemName);
        boolean success = script.getWidgetManager().getBank().withdraw(itemId, amount);

        if (success) {
            script.log(BankSearchUtils.class, "withdraw succeeded");
            script.pollFramesHuman(() -> false, script.random(200, 400));
        } else {
            script.log(BankSearchUtils.class, "withdraw failed for: " + itemName);
        }

        // clear search unless caller wants to keep it open
        if (!keepSearchOpen) {
            clearSearch(script);
        }

        return success;
    }

    /**
     * Withdraws multiple items in a single batch operation.
     *
     * This method processes each request in order, using searchAndWithdraw
     * for each item. Partial failures are handled gracefully - if one item
     * fails to withdraw, the method continues with remaining items.
     *
     * For efficiency, keepSearchOpen=true is used for all but the last item,
     * avoiding repeated search clear/open cycles.
     *
     * @param script the script instance
     * @param requests list of withdrawal requests to process
     * @return BatchWithdrawalResult containing successful and failed requests
     */
    public static BatchWithdrawalResult withdrawBatch(Script script, List<WithdrawalRequest> requests) {
        BatchWithdrawalResult result = new BatchWithdrawalResult();

        // pre-condition: bank must be visible
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankSearchUtils.class, "bank not visible - cannot withdraw batch");
            // mark all requests as failed
            for (WithdrawalRequest request : requests) {
                result.addFailed(request);
            }
            return result;
        }

        // handle empty list
        if (requests == null || requests.isEmpty()) {
            script.log(BankSearchUtils.class, "no requests to process");
            return result;
        }

        int totalRequests = requests.size();
        script.log(BankSearchUtils.class, "processing batch withdrawal of " + totalRequests + " items");

        // process each request
        for (int i = 0; i < totalRequests; i++) {
            WithdrawalRequest request = requests.get(i);
            boolean isLastItem = (i == totalRequests - 1);

            // use keepSearchOpen=true for all but last item (for efficiency)
            // last item clears search to leave bank in clean state
            boolean keepSearchOpen = !isLastItem;

            script.log(BankSearchUtils.class, "withdrawing item " + (i + 1) + "/" + totalRequests +
                    " (id: " + request.getItemId() + ", amount: " + request.getAmount() + ")");

            boolean success = searchAndWithdraw(script, request.getItemId(), request.getAmount(), keepSearchOpen);

            if (success) {
                result.addSuccessful(request);
            } else {
                result.addFailed(request);
            }
        }

        // log summary
        script.log(BankSearchUtils.class, "batch withdrawal complete: " +
                result.getSuccessCount() + "/" + totalRequests + " items succeeded");

        return result;
    }

    /**
     * Withdraws multiple items in a single batch operation.
     *
     * Convenience overload that accepts varargs for fluent API usage.
     *
     * @param script the script instance
     * @param requests withdrawal requests to process
     * @return BatchWithdrawalResult containing successful and failed requests
     */
    public static BatchWithdrawalResult withdrawBatch(Script script, WithdrawalRequest... requests) {
        return withdrawBatch(script, Arrays.asList(requests));
    }

    /**
     * Maximum number of scroll iterations when searching for an item.
     * Prevents infinite loops if item truly doesn't exist.
     */
    private static final int MAX_SCROLL_ITERATIONS = 30;

    /**
     * Searches for an item by name and withdraws enough to fill all remaining inventory slots.
     *
     * This method:
     * 1. Checks bank is visible
     * 2. Gets free inventory slots
     * 3. Gets item name from ID
     * 4. Types the name into bank search
     * 5. Verifies item exists in filtered results
     * 6. Falls back to scrolling if search doesn't find item
     * 7. Withdraws free-slots amount
     * 8. Clears search after withdrawal
     *
     * @param script the script instance
     * @param itemId the item ID to search for and withdraw
     * @return number of slots filled, 0 if no free slots, or -1 on failure
     */
    public static int searchAndFillInventory(Script script, int itemId) {
        // pre-condition: bank must be visible
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankSearchUtils.class, "bank not visible - cannot search and fill inventory");
            return -1;
        }

        // get free inventory slots
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inv == null) {
            script.log(BankSearchUtils.class, "inventory not visible");
            return -1;
        }

        int freeSlots = inv.getFreeSlots();
        if (freeSlots <= 0) {
            script.log(BankSearchUtils.class, "no free slots available");
            return 0;
        }

        script.log(BankSearchUtils.class, "filling " + freeSlots + " inventory slots");

        // get item name from ID
        String itemName = script.getItemManager().getItemName(itemId);
        if (itemName == null || itemName.isEmpty()) {
            script.log(BankSearchUtils.class, "failed to get item name for id: " + itemId);
            return -1;
        }

        script.log(BankSearchUtils.class, "searching for: " + itemName + " (id: " + itemId + ")");

        // type search query
        if (!typeSearch(script, itemName)) {
            script.log(BankSearchUtils.class, "failed to type search for: " + itemName);
            return -1;
        }

        // wait for bank to filter results
        script.pollFramesHuman(() -> false, script.random(300, 500));

        // verify item exists in filtered bank
        ItemGroupResult bankItems = script.getWidgetManager().getBank().search(Set.of(itemId));
        boolean foundViaSearch = bankItems != null && bankItems.contains(itemId);

        if (!foundViaSearch) {
            script.log(BankSearchUtils.class, "item not found via search, trying scroll fallback");
            clearSearch(script);

            boolean foundViaScroll = findItemByScrolling(script, itemId);
            if (!foundViaScroll) {
                script.log(BankSearchUtils.class, "item not found in bank after full scroll: " + itemName);
                return -1;
            }

            // found via scroll - withdraw it
            script.log(BankSearchUtils.class, "item found via scroll, withdrawing " + freeSlots + " x " + itemName);
            boolean success = script.getWidgetManager().getBank().withdraw(itemId, freeSlots);

            if (success) {
                script.log(BankSearchUtils.class, "withdraw succeeded (via scroll fallback), filled " + freeSlots + " slots");
                script.pollFramesHuman(() -> false, script.random(200, 400));
                return freeSlots;
            } else {
                script.log(BankSearchUtils.class, "withdraw failed for: " + itemName);
                return -1;
            }
        }

        // item found via search - withdraw it
        script.log(BankSearchUtils.class, "withdrawing " + freeSlots + " x " + itemName);
        boolean success = script.getWidgetManager().getBank().withdraw(itemId, freeSlots);

        if (success) {
            script.log(BankSearchUtils.class, "withdraw succeeded, filled " + freeSlots + " slots");
            script.pollFramesHuman(() -> false, script.random(200, 400));
        } else {
            script.log(BankSearchUtils.class, "withdraw failed for: " + itemName);
            clearSearch(script);
            return -1;
        }

        // clear search to leave bank in clean state
        clearSearch(script);

        return freeSlots;
    }

    /**
     * Finds an item by scrolling through the bank.
     *
     * This method:
     * 1. Scrolls to the top of the bank
     * 2. Searches for the item on current view
     * 3. If not found, scrolls down and searches again
     * 4. Repeats until item found or end of bank reached
     *
     * @param script the script instance
     * @param itemId the item ID to find
     * @return true if item was found and is currently visible
     */
    private static boolean findItemByScrolling(Script script, int itemId) {
        // scroll to top first to ensure we search the entire bank
        script.log(BankSearchUtils.class, "scrolling to top of bank to begin search");
        BankScrollUtils.scrollToTopWithCheck(script, 20);

        // wait for scroll to settle
        script.pollFramesHuman(() -> false, script.random(200, 400));

        // search current view
        for (int scrollCount = 0; scrollCount < MAX_SCROLL_ITERATIONS; scrollCount++) {
            // check if item is visible in current view
            ItemGroupResult bankItems = script.getWidgetManager().getBank().search(Set.of(itemId));
            if (bankItems != null && bankItems.contains(itemId)) {
                script.log(BankSearchUtils.class, "item found after " + scrollCount + " scrolls");
                return true;
            }

            // check if we can scroll further
            if (!BankScrollUtils.canScrollDown(script)) {
                script.log(BankSearchUtils.class, "reached end of bank after " + scrollCount + " scrolls - item not found");
                return false;
            }

            // scroll down
            script.log(BankSearchUtils.class, "item not visible, scrolling down (scroll " + (scrollCount + 1) + ")");
            if (!BankScrollUtils.scrollDown(script)) {
                script.log(BankSearchUtils.class, "scroll down failed");
                // retry next iteration
            }
        }

        // hit max iterations
        script.log(BankSearchUtils.class, "hit max scroll iterations (" + MAX_SCROLL_ITERATIONS + ") - item not found");
        return false;
    }
}
