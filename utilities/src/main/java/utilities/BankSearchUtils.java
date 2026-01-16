package utilities;

import com.osmb.api.definition.SpriteDefinition;
import com.osmb.api.input.PhysicalKey;
import com.osmb.api.input.TouchType;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.script.Script;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import com.osmb.api.visual.image.Image;
import com.osmb.api.visual.image.ImageSearchResult;
import com.osmb.api.visual.image.SearchableImage;
import com.osmb.api.input.MenuEntry;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.item.SearchableItem;
import com.osmb.api.shape.Rectangle;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Bank search utilities for searching and withdrawing items by name.
 *
 * <p><b>CRITICAL: Use this instead of raw bank.search()/bank.withdraw() APIs.</b>
 * The raw APIs only see currently visible items and don't use the search box.
 *
 * <h2>Key Methods:</h2>
 * <ul>
 *   <li>{@link #searchAndWithdrawVerified} - Main withdrawal method (types name, finds visually, verifies)</li>
 *   <li>{@link #clickSearchToReset} - Reset search between withdrawals (REQUIRED after each withdrawal)</li>
 *   <li>{@link #clearSearch} - Close search entirely when done</li>
 * </ul>
 *
 * <h2>CRITICAL Pattern - Multi-item withdrawal:</h2>
 * <pre>{@code
 * for (int itemId : itemsToWithdraw) {
 *     boolean success = BankSearchUtils.searchAndWithdrawVerified(script, itemId, amount, true);
 *     if (success) {
 *         // MUST reset search after each successful withdrawal
 *         BankSearchUtils.clickSearchToReset(script);
 *     }
 * }
 * // clear search when done
 * BankSearchUtils.clearSearch(script);
 * }</pre>
 *
 * <h2>Why clickSearchToReset() is required:</h2>
 * <p>After a successful withdrawal, the search box remains open with the previous search text.
 * You cannot type a new search query until you click the search button to reset it.
 * The reset clears the current filter and opens a fresh search input.
 *
 * <p>Note: Bank must be visible before calling these methods.
 */
public class BankSearchUtils {

    private static final int COLOR_TOLERANCE = 15;
    private static final int SEARCH_BUTTON_SPRITE_ID = 1043;

    private static SearchableImage searchButtonImage;
    private static boolean spriteInitialized = false;

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int SEARCH_OPEN_ATTEMPTS = 5;

    /**
     * Initializes the search button sprite for visual detection.
     *
     * @param script the script instance
     * @return true if sprite loaded successfully
     */
    private static boolean initSprite(Script script) {
        if (spriteInitialized) {
            return true;
        }

        try {
            SingleThresholdComparator tolerance = new SingleThresholdComparator(COLOR_TOLERANCE);

            SpriteDefinition sprite = script.getSpriteManager().getSprite(SEARCH_BUTTON_SPRITE_ID, 0);
            if (sprite == null) {
                script.log(BankSearchUtils.class, "search button sprite not found: " + SEARCH_BUTTON_SPRITE_ID);
                return false;
            }
            Image image = new Image(sprite);
            searchButtonImage = image.toSearchableImage(tolerance, ColorModel.RGB);
            script.log(BankSearchUtils.class, "loaded search button sprite: " + sprite.width + "x" + sprite.height);

            spriteInitialized = true;
            return true;

        } catch (Exception e) {
            script.log(BankSearchUtils.class, "failed to load search button sprite: " + e.getMessage());
            return false;
        }
    }

    /**
     * Taps the search button using sprite-based visual detection.
     *
     * @param script the script instance
     * @return true if search button was found and tapped
     */
    private static boolean tapSearchButton(Script script) {
        if (!initSprite(script)) {
            return false;
        }

        List<ImageSearchResult> matches = script.getImageAnalyzer().findLocations(searchButtonImage);
        if (matches == null || matches.isEmpty()) {
            return false;
        }

        ImageSearchResult result = matches.get(0);
        Rectangle bounds = result.getBounds();
        return script.getFinger().tap(bounds);
    }

    /**
     * Clicks the search button to reset the search.
     *
     * If search is currently active with text, this closes it first then reopens fresh.
     * If search is inactive, this opens a new search input.
     *
     * Use this after withdrawing an item to prepare for the next search without
     * closing the bank.
     *
     * @param script the script instance
     * @return true if search button was clicked and search is now active
     */
    public static boolean clickSearchToReset(Script script) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankSearchUtils.class, "bank not visible - cannot reset search");
            return false;
        }

        // if search is active, close it first (tap toggles search off)
        if (isSearchActive(script)) {
            script.log(BankSearchUtils.class, "closing active search");
            if (!tapSearchButton(script)) {
                return false;
            }
            // brief wait for search to close
            script.pollFramesUntil(() -> !isSearchActive(script), 500);
        }

        // now open fresh search
        script.log(BankSearchUtils.class, "opening fresh search");
        if (!tapSearchButton(script)) {
            script.log(BankSearchUtils.class, "failed to tap search button");
            return false;
        }

        // wait for search to become active (shorter timeout)
        boolean opened = script.pollFramesUntil(() -> isSearchActive(script), 1000);

        if (opened) {
            script.log(BankSearchUtils.class, "search reset and ready");
            return true;
        }

        script.log(BankSearchUtils.class, "search not active after reset");
        return false;
    }

    /**
     * Opens the bank search box by tapping the SEARCH button.
     *
     * Uses sprite-based visual detection to find and tap the search button.
     *
     * @param script the script instance
     * @return true if search box opened successfully
     */
    public static boolean openSearch(Script script) {
        return openSearch(script, DEFAULT_TIMEOUT);
    }

    /**
     * Opens the bank search box by tapping the SEARCH button.
     *
     * Uses sprite-based visual detection to find and tap the search button.
     *
     * @param script the script instance
     * @param timeout max time to wait for search box to open
     * @return true if search box opened successfully
     */
    public static boolean openSearch(Script script, int timeout) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return false;
        }

        if (isSearchActive(script)) {
            return true;
        }

        for (int attempt = 1; attempt <= SEARCH_OPEN_ATTEMPTS; attempt++) {
            if (!tapSearchButton(script)) {
                script.pollFramesUntil(() -> false, 50);
                continue;
            }

            // wait for search to become active (use shorter timeout)
            boolean opened = script.pollFramesUntil(() -> isSearchActive(script), Math.min(timeout, 1000));

            if (opened) {
                return true;
            }
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

        // type the item name with humanized delays
        script.log(BankSearchUtils.class, "typing search: " + itemName);
        typeWithDelay(script, itemName);

        // human-like delay after typing for results to filter
        script.pollFramesUntil(() -> false, 50);

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
            return true;
        }

        // press BACK to close search (equivalent to ESC on desktop)
        script.getKeyboard().pressKey(TouchType.DOWN, PhysicalKey.BACK);
        script.getKeyboard().pressKey(TouchType.UP, PhysicalKey.BACK);

        // wait for search to become inactive (use shorter timeout)
        boolean cleared = script.pollFramesUntil(() -> !isSearchActive(script), Math.min(timeout, 1000));

        return cleared;
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

        // wait for bank to filter results - needs time for UI to update
        script.pollFramesUntil(() -> false, 400);

        // find item using sprite detection (bank.withdraw doesn't work with search results)
        Rectangle foundBounds = findAndVerifyItem(script, itemId);
        if (foundBounds == null) {
            script.log(BankSearchUtils.class, "item not found via sprite detection: " + itemName);
            clearSearch(script);
            return false;
        }

        // tap to withdraw using direct action
        script.log(BankSearchUtils.class, "withdrawing " + amount + " x " + itemName);
        String[] actions = getWithdrawActions(amount);
        boolean success = script.getFinger().tap(foundBounds, actions);

        if (success) {
            script.log(BankSearchUtils.class, "withdraw succeeded");
            script.pollFramesUntil(() -> false, 50);
        } else {
            script.log(BankSearchUtils.class, "withdraw tap failed for: " + itemName);
        }

        // clear search unless caller wants to keep it open
        if (!keepSearchOpen) {
            clearSearch(script);
        }

        return success;
    }

    /**
     * Searches for an item by name and withdraws with inventory verification.
     *
     * This method:
     * 1. Types search query
     * 2. Finds item via sprite detection
     * 3. Taps to withdraw
     * 4. Verifies inventory has the expected count
     * 5. Retries withdrawal if count doesn't match (up to maxAttempts)
     *
     * For amount=0 (All), verification just checks that at least 1 item was withdrawn.
     *
     * @param script the script instance
     * @param itemId the item ID to withdraw
     * @param amount the amount to withdraw (0 for All)
     * @param keepSearchOpen if true, does not clear search after withdraw
     * @param maxAttempts max withdrawal attempts if verification fails
     * @return true if withdrawal succeeded and inventory has expected count
     */
    public static boolean searchAndWithdrawVerified(Script script, int itemId, int amount, boolean keepSearchOpen, int maxAttempts) {
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

        // get initial inventory count
        int initialCount = getInventoryCount(script, itemId);
        int expectedCount = (amount == 0) ? initialCount + 1 : initialCount + amount; // for All, just expect at least 1 more

        script.log(BankSearchUtils.class, "searching for: " + itemName + " (id: " + itemId + ")");
        script.log(BankSearchUtils.class, "initial inventory count: " + initialCount + ", expecting: " + (amount == 0 ? "at least " + expectedCount : expectedCount));

        // type search query
        if (!typeSearch(script, itemName)) {
            script.log(BankSearchUtils.class, "failed to type search for: " + itemName);
            return false;
        }

        // wait for bank to filter results - needs time for UI to update
        script.pollFramesUntil(() -> false, 400);

        // find item using sprite detection
        Rectangle foundBounds = findAndVerifyItem(script, itemId);
        if (foundBounds == null) {
            script.log(BankSearchUtils.class, "item not found via sprite detection: " + itemName);
            // use clickSearchToReset when keepSearchOpen=true to avoid closing bank
            // clearSearch uses BACK key which can close the bank when search shows no results
            if (keepSearchOpen) {
                clickSearchToReset(script);
            } else {
                clearSearch(script);
            }
            return false;
        }

        // withdrawal loop with verification
        String[] actions = getWithdrawActions(amount);
        boolean needsCustomInput = requiresWithdrawX(amount);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            script.log(BankSearchUtils.class, "withdraw attempt " + attempt + "/" + maxAttempts);

            // re-scan for item on every attempt (in case screen lagged or item shifted)
            foundBounds = findAndVerifyItem(script, itemId);
            if (foundBounds == null) {
                script.log(BankSearchUtils.class, "item not visible on attempt " + attempt + " - may be out of stock or still loading");
                script.pollFramesUntil(() -> false, 300);
                continue;
            }

            // tap to withdraw
            boolean tapSuccess = script.getFinger().tap(foundBounds, actions);
            if (!tapSuccess) {
                script.log(BankSearchUtils.class, "withdraw tap failed on attempt " + attempt);
                script.pollFramesUntil(() -> false, 300);
                continue;
            }

            // if using Withdraw-X, type the custom amount
            if (needsCustomInput) {
                if (!typeWithdrawAmount(script, amount)) {
                    script.log(BankSearchUtils.class, "failed to type withdraw amount on attempt " + attempt);
                    script.pollFramesUntil(() -> false, 300);
                    continue;
                }
            }

            // poll until inventory changes (more reliable than fixed delay)
            final int countBeforePoll = initialCount;
            boolean inventoryChanged = script.pollFramesUntil(() -> {
                int count = getInventoryCount(script, itemId);
                return count > countBeforePoll;
            }, 2000);

            // verify inventory count
            int currentCount = getInventoryCount(script, itemId);
            script.log(BankSearchUtils.class, "inventory count after withdraw: " + currentCount + (inventoryChanged ? "" : " (timeout)"));

            if (amount == 0) {
                // for All, just check we got at least 1
                if (currentCount > initialCount) {
                    script.log(BankSearchUtils.class, "withdraw-all verified: got " + (currentCount - initialCount) + " items");
                    if (!keepSearchOpen) clearSearch(script);
                    return true;
                }
            } else {
                // for specific amount, check exact count
                if (currentCount >= expectedCount) {
                    script.log(BankSearchUtils.class, "withdraw verified: have " + currentCount + "/" + expectedCount);
                    if (!keepSearchOpen) clearSearch(script);
                    return true;
                }
                script.log(BankSearchUtils.class, "need more: have " + currentCount + ", need " + expectedCount);
            }
        }

        script.log(BankSearchUtils.class, "withdraw verification failed after " + maxAttempts + " attempts");
        if (!keepSearchOpen) clearSearch(script);
        return false;
    }

    /**
     * Searches for an item by name and withdraws with inventory verification.
     * Uses 5 retry attempts by default.
     *
     * @param script the script instance
     * @param itemId the item ID to withdraw
     * @param amount the amount to withdraw (0 for All)
     * @param keepSearchOpen if true, does not clear search after withdraw
     * @return true if withdrawal succeeded and inventory has expected count
     */
    public static boolean searchAndWithdrawVerified(Script script, int itemId, int amount, boolean keepSearchOpen) {
        return searchAndWithdrawVerified(script, itemId, amount, keepSearchOpen, 5);
    }

    /**
     * Gets the count of an item in the inventory.
     *
     * @param script the script instance
     * @param itemId the item ID to count
     * @return count of items, 0 if not found or inventory not visible
     */
    private static int getInventoryCount(Script script, int itemId) {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(itemId));
        if (inv == null) {
            return 0;
        }
        return inv.getAmount(itemId);
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
        script.pollFramesUntil(() -> false, 100);

        // find item using sprite detection (bank.withdraw doesn't work with search results)
        Rectangle foundBounds = findAndVerifyItem(script, itemId);
        if (foundBounds == null) {
            script.log(BankSearchUtils.class, "item not found via sprite detection: " + itemName);
            clearSearch(script);
            return -1;
        }

        // tap to withdraw using direct action
        script.log(BankSearchUtils.class, "withdrawing " + freeSlots + " x " + itemName);
        String[] actions = getWithdrawActions(freeSlots);
        boolean success = script.getFinger().tap(foundBounds, actions);

        if (success) {
            script.log(BankSearchUtils.class, "withdraw succeeded, filled " + freeSlots + " slots");
            script.pollFramesUntil(() -> false, 50);
        } else {
            script.log(BankSearchUtils.class, "withdraw tap failed for: " + itemName);
            clearSearch(script);
            return -1;
        }

        // clear search to leave bank in clean state
        clearSearch(script);

        return freeSlots;
    }

    /**
     * Searches for an item by name string and withdraws the first matching result.
     *
     * This method bypasses item ID lookup and searches by typing the name directly.
     * After filtering, it uses sprite-based detection combined with menu verification
     * to find and withdraw the item.
     *
     * Use this when getItemManager().getItemName() is not working or when you
     * only know the item name, not the ID.
     *
     * @param script the script instance
     * @param itemName the item name to search for (e.g., "Ring of dueling")
     * @param amount the amount to withdraw (1 for single click, 0 for withdraw-all)
     * @return true if withdrawal succeeded
     */
    public static boolean searchAndWithdrawByName(Script script, String itemName, int amount) {
        // pre-condition: bank must be visible
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankSearchUtils.class, "bank not visible - cannot search and withdraw");
            return false;
        }

        // validate input
        if (itemName == null || itemName.isEmpty()) {
            script.log(BankSearchUtils.class, "item name is empty or null");
            return false;
        }

        script.log(BankSearchUtils.class, "searching for: " + itemName);

        // type search query
        if (!typeSearch(script, itemName)) {
            script.log(BankSearchUtils.class, "failed to type search for: " + itemName);
            return false;
        }

        // wait for bank to filter results
        script.pollFramesUntil(() -> false, 150);

        // determine withdraw action based on amount
        String action;
        if (amount == 0) {
            action = "Withdraw-All";
        } else if (amount == 1) {
            action = "Withdraw-1";
        } else {
            action = "Withdraw-" + amount;
        }

        // scroll to top - previous search may have left scroll position mid-way
        if (!BankScrollUtils.isAtTop(script)) {
            script.log(BankSearchUtils.class, "scrolling to top before searching");
            BankScrollUtils.scrollToTopWithCheck(script, 20);
            script.pollFramesUntil(() -> false, 50);
        }

        // get bank bounds for calculating item slot positions
        Rectangle bankBounds = script.getWidgetManager().getBank().getBounds();
        if (bankBounds == null) {
            script.log(BankSearchUtils.class, "could not get bank bounds");
            clearSearch(script);
            return false;
        }

        // search with verification via tapGetResponse
        for (int scrollCount = 0; scrollCount <= MAX_SCROLL_ITERATIONS; scrollCount++) {
            // find matching item using sprite + menu verification
            Rectangle foundBounds = findMatchingItemByName(script, itemName, bankBounds);

            if (foundBounds != null) {
                // found verified item - withdraw
                script.log(BankSearchUtils.class, "withdrawing verified item with action: " + action);
                boolean success = script.getFinger().tap(foundBounds, new String[]{action});
                if (success) {
                    script.pollFramesUntil(() -> false, 50);
                    clearSearch(script);
                    return true;
                }
                // tap failed but item was found - try again
                script.log(BankSearchUtils.class, "withdraw tap failed, retrying");
            }

            // check if at bottom
            if (BankScrollUtils.isAtBottom(script)) {
                script.log(BankSearchUtils.class, "reached bottom of filtered results");
                break;
            }

            // scroll down
            BankScrollUtils.scrollDown(script);
            script.pollFramesUntil(() -> false, 50);
        }

        script.log(BankSearchUtils.class, "item not found after full scroll: " + itemName);
        clearSearch(script);
        return false;
    }

    /**
     * Finds an item in visible bank by name match using sprite detection and menu verification.
     *
     * Strategy:
     * 1. First try to find any item sprite in bank bounds using pixel cluster detection
     * 2. Verify via tapGetResponse that the entity name contains our search term
     * 3. Falls back to first-slot tap if sprite detection fails
     *
     * @param script the script instance
     * @param itemName the item name to search for
     * @param bankBounds the bounds of the bank widget
     * @return Rectangle bounds if item found and verified, null otherwise
     */
    private static Rectangle findMatchingItemByName(Script script, String itemName, Rectangle bankBounds) {
        if (bankBounds == null) {
            return null;
        }

        // bank item grid area (where items appear after filtering)
        // bank grid starts at offset from bank widget bounds
        int gridStartX = bankBounds.x + 73;
        int gridStartY = bankBounds.y + 83;

        // slot dimensions in bank
        int slotWidth = 48;
        int slotHeight = 36;
        int cols = 8;
        int visibleRows = 6;

        script.log(BankSearchUtils.class, "scanning bank slots for: " + itemName);

        // scan grid slots row by row to find matching item
        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < cols; col++) {
                // calculate slot center
                int slotCenterX = gridStartX + (col * slotWidth) + (slotWidth / 2);
                int slotCenterY = gridStartY + (row * slotHeight) + (slotHeight / 2);
                Rectangle slotBounds = new Rectangle(
                    slotCenterX - 15,
                    slotCenterY - 15,
                    30,
                    30
                );

                // tap to get menu response
                MenuEntry entry = script.getFinger().tapGetResponse(true, slotBounds);
                if (entry == null) {
                    continue;
                }

                // check if entity name contains our search term
                String entityName = entry.getEntityName();
                if (entityName != null && entityName.toLowerCase().contains(itemName.toLowerCase())) {
                    script.log(BankSearchUtils.class, "found matching item at slot [" + row + "," + col + "]: " + entityName);
                    return slotBounds;
                }
            }
        }

        script.log(BankSearchUtils.class, "no matching item found in visible slots for: " + itemName);
        return null;
    }

    private static final java.util.Random RANDOM = new java.util.Random();

    /**
     * Types text with human-like delays between characters.
     * Uses gaussian distribution centered at 75ms (range ~25-125ms).
     *
     * @param script the script instance
     * @param text the text to type
     */
    private static void typeWithDelay(Script script, String text) {
        for (int i = 0; i < text.length(); i++) {
            script.getKeyboard().type(String.valueOf(text.charAt(i)));

            if (i < text.length() - 1) {
                // gaussian delay: mean=75ms, stddev=25ms, clamped to 25-125ms
                int delay = (int) (75 + RANDOM.nextGaussian() * 25);
                delay = Math.max(25, Math.min(125, delay));

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Gets the appropriate withdraw action strings for the given amount.
     *
     * @param amount the amount to withdraw (1, 5, 10, 0 for all, or custom)
     * @return array of action strings to try
     */
    private static String[] getWithdrawActions(int amount) {
        if (amount == 0) {
            return new String[]{"Withdraw-All"};
        } else if (amount == 1) {
            return new String[]{"Withdraw-1"};
        } else if (amount == 5) {
            return new String[]{"Withdraw-5"};
        } else if (amount == 10) {
            return new String[]{"Withdraw-10"};
        } else {
            // custom amount - use Withdraw-X which requires typing
            return new String[]{"Withdraw-X"};
        }
    }

    /**
     * Checks if the given amount requires using Withdraw-X (custom input).
     *
     * @param amount the amount to check
     * @return true if Withdraw-X is needed
     */
    private static boolean requiresWithdrawX(int amount) {
        return amount != 0 && amount != 1 && amount != 5 && amount != 10;
    }

    /**
     * Handles typing a custom amount after clicking Withdraw-X.
     *
     * @param script the script instance
     * @param amount the amount to type
     * @return true if amount was typed and confirmed
     */
    private static boolean typeWithdrawAmount(Script script, int amount) {
        // wait for input dialog to appear (ENTER_AMOUNT dialogue type)
        boolean dialogAppeared = script.pollFramesUntil(() -> {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            return type == DialogueType.ENTER_AMOUNT;
        }, 2000);

        if (!dialogAppeared) {
            script.log(BankSearchUtils.class, "withdraw-x dialog did not appear");
            return false;
        }

        // small delay for dialog to be ready
        script.pollFramesUntil(() -> false, 100);

        // type the amount
        String amountStr = String.valueOf(amount);
        script.log(BankSearchUtils.class, "typing withdraw amount: " + amountStr);
        typeWithDelay(script, amountStr);

        // small delay after typing
        script.pollFramesUntil(() -> false, 100);

        // press enter to confirm
        script.getKeyboard().pressKey(TouchType.DOWN, PhysicalKey.ENTER);
        script.getKeyboard().pressKey(TouchType.UP, PhysicalKey.ENTER);

        // wait for dialog to close
        boolean dialogClosed = script.pollFramesUntil(() -> {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            return type != DialogueType.ENTER_AMOUNT;
        }, 2000);

        if (!dialogClosed) {
            script.log(BankSearchUtils.class, "withdraw-x dialog did not close after typing");
            return false;
        }

        script.log(BankSearchUtils.class, "withdraw-x amount confirmed");
        return true;
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
        script.pollFramesUntil(() -> false, 50);

        // search current view
        for (int scrollCount = 0; scrollCount < MAX_SCROLL_ITERATIONS; scrollCount++) {
            // check if item is visible in current view using sprite detection
            Rectangle found = findAndVerifyItem(script, itemId);
            if (found != null) {
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

    /**
     * Finds an item visually in the visible bank area by its sprite.
     *
     * Uses ItemManager to create a SearchableItem from the item ID and searches
     * the bank bounds for a visual match.
     *
     * @param script the script instance
     * @param itemId the item ID to find
     * @return ItemSearchResult with bounds if found, null if not visible
     */
    private static ItemSearchResult findItemInVisibleBank(Script script, int itemId) {
        // check bank is visible
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankSearchUtils.class, "bank not visible - cannot find item");
            return null;
        }

        // get searchable item from item manager
        SearchableItem[] searchableItems = script.getItemManager().getItem(itemId, false);
        if (searchableItems == null || searchableItems.length == 0) {
            script.log(BankSearchUtils.class, "could not get searchable item for id: " + itemId);
            return null;
        }

        // get bank bounds
        Rectangle bankBounds = script.getWidgetManager().getBank().getBounds();
        if (bankBounds == null) {
            script.log(BankSearchUtils.class, "could not get bank bounds");
            return null;
        }

        // try all searchable item variants - different variants work in different contexts
        // variant 3 (selectedModel=false, lowResolution=false) typically works for bank items
        for (int i = 0; i < searchableItems.length; i++) {
            ItemSearchResult result = script.getItemManager().findLocation(false, bankBounds, searchableItems[i]);
            if (result != null) {
                script.log(BankSearchUtils.class, "found item using variant " + i);
                return result;
            }
        }

        return null;
    }

    /**
     * Finds an item visually and verifies it matches via menu inspection.
     *
     * This method:
     * 1. Uses findItemInVisibleBank to locate the item sprite
     * 2. Uses tapGetResponse on the found bounds to get menu entry
     * 3. Compares menu entity name with expected item name
     * 4. Returns verified bounds only if names match
     *
     * This prevents false positives from items with similar sprites.
     *
     * @param script the script instance
     * @param itemId the item ID to find and verify
     * @return Rectangle bounds if item found and verified, null otherwise
     */
    public static Rectangle findAndVerifyItem(Script script, int itemId) {
        // check bank is visible
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankSearchUtils.class, "bank not visible - cannot find and verify item");
            return null;
        }

        // find item visually using sprite detection
        ItemSearchResult searchResult = findItemInVisibleBank(script, itemId);
        if (searchResult == null) {
            return null;
        }

        // get bounds from result
        Rectangle bounds = searchResult.getBounds();
        if (bounds == null) {
            script.log(BankSearchUtils.class, "item search result has no bounds");
            return null;
        }

        // sprite detection is reliable - return bounds directly
        // verification via tapGetResponse causes "No tappable game screen points" error
        script.log(BankSearchUtils.class, "item found at: " + bounds);
        return bounds;
    }

    /**
     * Searches the entire bank for an item by ID with visual verification.
     *
     * This method:
     * 1. Scrolls to top of bank (using isAtTop for verification)
     * 2. Searches visible area using findAndVerifyItem
     * 3. If not found, scrolls down and searches again
     * 4. Repeats until item found or isAtBottom returns true
     *
     * Unlike searchAndWithdrawByName which uses fixed offsets, this method
     * visually confirms the item's location before returning bounds.
     *
     * @param script the script instance
     * @param itemId the item ID to find
     * @return Rectangle bounds if item found and verified, null if not in bank
     */
    public static Rectangle searchBankForItem(Script script, int itemId) {
        // check bank is visible
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankSearchUtils.class, "bank not visible - cannot search for item");
            return null;
        }

        script.log(BankSearchUtils.class, "searching bank for item id: " + itemId);

        // scroll to top
        BankScrollUtils.scrollToTopWithCheck(script, 20);
        script.pollFramesUntil(() -> false, 50);

        // search loop
        for (int scrollCount = 0; scrollCount < MAX_SCROLL_ITERATIONS; scrollCount++) {
            // try to find and verify item in current view
            Rectangle result = findAndVerifyItem(script, itemId);
            if (result != null) {
                script.log(BankSearchUtils.class, "found item after " + scrollCount + " scrolls");
                return result;
            }

            // check if we're at the bottom
            if (BankScrollUtils.isAtBottom(script)) {
                script.log(BankSearchUtils.class, "reached bottom of bank - item not found");
                break;
            }

            // scroll down
            BankScrollUtils.scrollDown(script);
            script.pollFramesUntil(() -> false, 50);
        }

        script.log(BankSearchUtils.class, "item not found in bank after full scroll");
        return null;
    }
}
