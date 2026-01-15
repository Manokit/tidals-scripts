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
            script.log(BankSearchUtils.class, "search button not found on screen");
            return false;
        }

        ImageSearchResult result = matches.get(0);
        Rectangle bounds = result.getBounds();
        script.log(BankSearchUtils.class, "tapping search button within: " + bounds);
        boolean tapped = script.getFinger().tap(bounds);

        if (tapped) {
            script.pollFramesHuman(() -> false, script.random(200, 400));
            script.log(BankSearchUtils.class, "search button tapped successfully");
        } else {
            script.log(BankSearchUtils.class, "failed to tap search button");
        }

        return tapped;
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
            script.log(BankSearchUtils.class, "bank not visible - cannot open search");
            return false;
        }

        if (isSearchActive(script)) {
            script.log(BankSearchUtils.class, "search already active");
            return true;
        }

        for (int attempt = 1; attempt <= SEARCH_OPEN_ATTEMPTS; attempt++) {
            script.log(BankSearchUtils.class, "opening search attempt " + attempt + "/" + SEARCH_OPEN_ATTEMPTS);

            // tap search button sprite
            if (!tapSearchButton(script)) {
                script.log(BankSearchUtils.class, "failed to tap search button on attempt " + attempt);
                script.pollFramesHuman(() -> false, script.random(200, 400));
                continue;
            }

            // wait for search to become active
            boolean opened = script.pollFramesUntil(() -> isSearchActive(script), timeout);

            if (opened) {
                script.log(BankSearchUtils.class, "search opened successfully");
                script.pollFramesHuman(() -> false, script.random(100, 200));
                return true;
            }

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

        // type the item name with humanized delays
        script.log(BankSearchUtils.class, "typing search: " + itemName);
        typeWithDelay(script, itemName);

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

        // verify item exists in filtered bank using sprite detection
        Rectangle foundBounds = findAndVerifyItem(script, itemId);
        boolean foundViaSearch = foundBounds != null;

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

                // found via scroll - need to re-find bounds for withdrawal
                Rectangle scrollBounds = findAndVerifyItem(script, itemId);
                if (scrollBounds == null) {
                    script.log(BankSearchUtils.class, "item found via scroll but bounds lost");
                    return false;
                }

                // withdraw using verified bounds
                script.log(BankSearchUtils.class, "item found via scroll, withdrawing " + amount + " x " + itemName);
                String[] actions = getWithdrawActions(amount);
                boolean success = script.getFinger().tap(scrollBounds, actions);

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

        // item found via search - withdraw using verified bounds
        script.log(BankSearchUtils.class, "withdrawing " + amount + " x " + itemName);
        String[] actions = getWithdrawActions(amount);
        boolean success = script.getFinger().tap(foundBounds, actions);

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

        // verify item exists in filtered bank using sprite detection
        Rectangle foundBounds = findAndVerifyItem(script, itemId);
        boolean foundViaSearch = foundBounds != null;

        if (!foundViaSearch) {
            script.log(BankSearchUtils.class, "item not found via search, trying scroll fallback");
            clearSearch(script);

            boolean foundViaScroll = findItemByScrolling(script, itemId);
            if (!foundViaScroll) {
                script.log(BankSearchUtils.class, "item not found in bank after full scroll: " + itemName);
                return -1;
            }

            // found via scroll - need to re-find bounds for withdrawal
            Rectangle scrollBounds = findAndVerifyItem(script, itemId);
            if (scrollBounds == null) {
                script.log(BankSearchUtils.class, "item found via scroll but bounds lost");
                return -1;
            }

            // withdraw using verified bounds
            script.log(BankSearchUtils.class, "item found via scroll, withdrawing " + freeSlots + " x " + itemName);
            String[] actions = getWithdrawActions(freeSlots);
            boolean success = script.getFinger().tap(scrollBounds, actions);

            if (success) {
                script.log(BankSearchUtils.class, "withdraw succeeded (via scroll fallback), filled " + freeSlots + " slots");
                script.pollFramesHuman(() -> false, script.random(200, 400));
                return freeSlots;
            } else {
                script.log(BankSearchUtils.class, "withdraw failed for: " + itemName);
                return -1;
            }
        }

        // item found via search - withdraw using verified bounds
        script.log(BankSearchUtils.class, "withdrawing " + freeSlots + " x " + itemName);
        String[] actions = getWithdrawActions(freeSlots);
        boolean success = script.getFinger().tap(foundBounds, actions);

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
     * Searches for an item by name string and withdraws the first matching result.
     *
     * This method bypasses item ID lookup and searches by typing the name directly.
     * After filtering, it uses tapGetResponse to verify the item matches before withdrawing.
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
        script.pollFramesHuman(() -> false, script.random(400, 600));

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
            script.pollFramesHuman(() -> false, script.random(200, 400));
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
            // find matching item using menu verification
            Rectangle foundBounds = findMatchingItemByName(script, itemName);

            if (foundBounds != null) {
                // found verified item - withdraw
                script.log(BankSearchUtils.class, "withdrawing verified item with action: " + action);
                boolean success = script.getFinger().tap(foundBounds, new String[]{action});
                if (success) {
                    script.pollFramesHuman(() -> false, script.random(200, 400));
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
            script.pollFramesHuman(() -> false, script.random(200, 400));
        }

        script.log(BankSearchUtils.class, "item not found after full scroll: " + itemName);
        clearSearch(script);
        return false;
    }

    /**
     * Finds an item in visible bank by name match using tapGetResponse.
     *
     * Scans all visible bank slots (8 columns x visible rows) and uses
     * tapGetResponse to verify each slot's entity name matches the search term.
     *
     * @param script the script instance
     * @param itemName the item name to search for
     * @return Rectangle bounds if item found and verified, null otherwise
     */
    private static Rectangle findMatchingItemByName(Script script, String itemName) {
        Rectangle bankBounds = script.getWidgetManager().getBank().getBounds();
        if (bankBounds == null) {
            return null;
        }

        // bank item grid layout
        // slot dimensions based on OSRS bank interface
        int startX = bankBounds.x + 73;
        int startY = bankBounds.y + 83;
        int slotWidth = 48;
        int slotHeight = 36;
        int cols = 8;
        int visibleRows = 6;

        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < cols; col++) {
                // calculate slot bounds
                int slotX = startX + (col * slotWidth);
                int slotY = startY + (row * slotHeight);
                Rectangle slotBounds = new Rectangle(slotX, slotY, slotWidth - 6, slotHeight - 4);

                // tap to get menu response
                MenuEntry entry = script.getFinger().tapGetResponse(true, slotBounds);
                if (entry == null) {
                    // empty slot or no response - continue to next
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

    /**
     * Types text with random delays between characters for humanization.
     *
     * @param script the script instance
     * @param text the text to type
     */
    private static void typeWithDelay(Script script, String text) {
        for (int i = 0; i < text.length(); i++) {
            script.getKeyboard().type(String.valueOf(text.charAt(i)));
            // human-like delay between characters (50-150ms)
            if (i < text.length() - 1) {
                script.pollFramesHuman(() -> false, script.random(50, 150));
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
            // custom amount - try specific first, fall back to all
            return new String[]{"Withdraw-" + amount, "Withdraw-All"};
        }
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

        // search for item in bank bounds
        ItemSearchResult result = script.getItemManager().findLocation(false, bankBounds, searchableItems[0]);
        return result;
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

        // find item visually
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

        // use tapGetResponse to get menu entry
        MenuEntry menuEntry = script.getFinger().tapGetResponse(true, bounds);
        if (menuEntry == null) {
            script.log(BankSearchUtils.class, "could not get menu for item");
            return null;
        }

        // get expected item name
        String expectedName = script.getItemManager().getItemName(itemId);
        if (expectedName == null || expectedName.isEmpty()) {
            script.log(BankSearchUtils.class, "could not get expected item name for id: " + itemId);
            return null;
        }

        // compare menu entity name with expected name (case-insensitive contains)
        String actualName = menuEntry.getEntityName();
        if (actualName != null && actualName.toLowerCase().contains(expectedName.toLowerCase())) {
            script.log(BankSearchUtils.class, "item verified: " + expectedName);
            return bounds;
        }

        script.log(BankSearchUtils.class, "item verification failed: expected [" + expectedName + "] but got [" + actualName + "]");
        return null;
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
        script.pollFramesHuman(() -> false, script.random(200, 400));

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
            script.pollFramesHuman(() -> false, script.random(200, 400));
        }

        script.log(BankSearchUtils.class, "item not found in bank after full scroll");
        return null;
    }
}
