package utilities;

import com.osmb.api.definition.SpriteDefinition;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import com.osmb.api.visual.image.Image;
import com.osmb.api.visual.image.ImageSearchResult;
import com.osmb.api.visual.image.SearchableImage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Banking utilities for common bank operations.
 *
 * Provides shared bank query predicate and helper methods for:
 * - Finding and opening banks
 * - Depositing with exceptions
 * - Withdrawing items
 * - Closing banks safely
 *
 * Usage:
 *   RSObject bank = BankingUtils.findNearestBank(script);
 *   BankingUtils.openBankAndWait(script, bank, 15000);
 *   BankingUtils.depositAllExcept(script, Set.of(ItemID.CHISEL));
 *   BankingUtils.withdrawToFillInventory(script, ItemID.GOLD_ORE);
 *   BankingUtils.closeBankAndWait(script, 5000);
 */
public class BankingUtils {

    // bank object names that can be interacted with
    public static final String[] BANK_NAMES = {
        "Bank", "Chest", "Bank booth", "Bank chest",
        "Grand Exchange booth", "Bank counter", "Bank table"
    };

    // valid actions for bank objects
    public static final String[] BANK_ACTIONS = {"bank", "open", "use"};

    /**
     * Predicate for finding bank objects.
     * Matches objects with bank-like names and actions that are reachable.
     */
    public static final Predicate<RSObject> BANK_QUERY = obj ->
        obj.getName() != null && obj.getActions() != null &&
        Arrays.stream(BANK_NAMES).anyMatch(name -> name.equalsIgnoreCase(obj.getName())) &&
        Arrays.stream(obj.getActions()).anyMatch(action ->
            Arrays.stream(BANK_ACTIONS).anyMatch(bankAction -> bankAction.equalsIgnoreCase(action))) &&
        obj.canReach();

    /**
     * Find the nearest bank object using the standard bank query.
     *
     * @param script the script instance
     * @return the nearest bank object, or null if none found
     */
    public static RSObject findNearestBank(Script script) {
        List<RSObject> banks = script.getObjectManager().getObjects(BANK_QUERY);
        if (banks.isEmpty()) {
            return null;
        }
        return (RSObject) script.getUtils().getClosest(banks);
    }

    /**
     * Open the nearest bank and wait for it to be visible.
     * Uses movement tracking to detect when player stops moving (reached bank).
     *
     * @param script the script instance
     * @param timeout max time to wait in ms
     * @return true if bank opened successfully
     */
    public static boolean openBankAndWait(Script script, int timeout) {
        RSObject bank = findNearestBank(script);
        if (bank == null) {
            script.log(BankingUtils.class, "no bank found");
            return false;
        }
        return openBankAndWait(script, bank, timeout);
    }

    /**
     * Open a specific bank object and wait for it to be visible.
     * Uses movement tracking to detect when player stops moving.
     *
     * @param script the script instance
     * @param bank the bank object to interact with
     * @param timeout max time to wait in ms
     * @return true if bank opened successfully
     */
    public static boolean openBankAndWait(Script script, RSObject bank, int timeout) {
        if (bank == null) {
            script.log(BankingUtils.class, "bank object is null");
            return false;
        }

        // already open
        if (script.getWidgetManager().getBank().isVisible()) {
            return true;
        }

        // interact with bank
        if (!RetryUtils.objectInteract(script, bank, BANK_ACTIONS, "bank interaction")) {
            script.log(BankingUtils.class, "bank interact failed");
            return false;
        }

        // wait with movement tracking - stops waiting 2s after player stops moving
        AtomicReference<Timer> posTimer = new AtomicReference<>(new Timer());
        AtomicReference<WorldPosition> prevPos = new AtomicReference<>(null);

        boolean opened = script.pollFramesUntil(() -> {
            WorldPosition current = script.getWorldPosition();
            if (current == null) return false;

            if (!Objects.equals(current, prevPos.get())) {
                posTimer.get().reset();
                prevPos.set(current);
            }

            // bank visible or stopped moving for 2s
            return script.getWidgetManager().getBank().isVisible() || posTimer.get().timeElapsed() > 2000;
        }, timeout);

        return script.getWidgetManager().getBank().isVisible();
    }

    /**
     * Deposit all items except those in the keep set.
     * Adds a human-like delay after deposit.
     *
     * @param script the script instance
     * @param keepItems item IDs to keep in inventory
     * @return true if deposit succeeded
     */
    public static boolean depositAllExcept(Script script, Set<Integer> keepItems) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankingUtils.class, "bank not visible for deposit");
            return false;
        }

        boolean result = script.getWidgetManager().getBank().depositAll(keepItems);
        if (result) {
            script.pollFramesHuman(() -> false, script.random(300, 600));
        }
        return result;
    }

    /**
     * Withdraw items to fill all available inventory slots.
     * Calculates free slots and withdraws that amount.
     *
     * @param script the script instance
     * @param itemId the item ID to withdraw
     * @return the number of items withdrawn, or -1 on failure
     */
    public static int withdrawToFillInventory(Script script, int itemId) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankingUtils.class, "bank not visible for withdraw");
            return -1;
        }

        // get current inventory state
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inv == null) {
            script.log(BankingUtils.class, "inventory not visible");
            return -1;
        }

        int slots = inv.getFreeSlots();
        if (slots <= 0) {
            script.log(BankingUtils.class, "no free slots");
            return 0;
        }

        // check bank has item
        ItemGroupResult bankItems = script.getWidgetManager().getBank().search(Set.of(itemId));
        if (bankItems == null || !bankItems.contains(itemId)) {
            script.log(BankingUtils.class, "item not found in bank");
            return -1;
        }

        // withdraw
        if (!script.getWidgetManager().getBank().withdraw(itemId, slots)) {
            script.log(BankingUtils.class, "withdraw failed");
            return -1;
        }

        script.pollFramesHuman(() -> false, script.random(200, 400));
        return slots;
    }

    /**
     * Withdraw a specific amount of an item.
     *
     * @param script the script instance
     * @param itemId the item ID to withdraw
     * @param amount the amount to withdraw
     * @return true if withdraw succeeded
     */
    public static boolean withdraw(Script script, int itemId, int amount) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankingUtils.class, "bank not visible for withdraw");
            return false;
        }

        // check bank has item
        ItemGroupResult bankItems = script.getWidgetManager().getBank().search(Set.of(itemId));
        if (bankItems == null || !bankItems.contains(itemId)) {
            script.log(BankingUtils.class, "item not found in bank");
            return false;
        }

        boolean result = script.getWidgetManager().getBank().withdraw(itemId, amount);
        if (result) {
            script.pollFramesHuman(() -> false, script.random(200, 400));
        }
        return result;
    }

    /**
     * Check if the bank contains an item.
     *
     * @param script the script instance
     * @param itemId the item ID to check
     * @return true if item exists in bank, false otherwise
     */
    public static boolean bankContains(Script script, int itemId) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return false;
        }
        ItemGroupResult bankItems = script.getWidgetManager().getBank().search(Set.of(itemId));
        return bankItems != null && bankItems.contains(itemId);
    }

    /**
     * Get the amount of an item in the bank.
     *
     * @param script the script instance
     * @param itemId the item ID to check
     * @return the amount in bank, or 0 if not found
     */
    public static int getBankAmount(Script script, int itemId) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return 0;
        }
        ItemGroupResult bankItems = script.getWidgetManager().getBank().search(Set.of(itemId));
        if (bankItems == null || !bankItems.contains(itemId)) {
            return 0;
        }
        return bankItems.getAmount(itemId);
    }

    /**
     * Close the bank and wait for it to close.
     *
     * @param script the script instance
     * @param timeout max time to wait in ms
     * @return true if bank closed successfully
     */
    public static boolean closeBankAndWait(Script script, int timeout) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return true; // already closed
        }

        script.getWidgetManager().getBank().close();
        return script.pollFramesHuman(() -> !script.getWidgetManager().getBank().isVisible(), timeout);
    }

    // sprite ID for deposit worn items button
    private static final int DEPOSIT_WORN_SPRITE_ID = 1042;
    private static SearchableImage depositWornImage;

    /**
     * Taps the "Deposit worn items" button in the bank interface.
     * Uses sprite detection (ID 1042, frame 0) to find the button.
     *
     * <p>Requires bank to be open before calling.
     *
     * @param script the script instance
     * @return true if button was found and tapped
     */
    public static boolean depositWornItems(Script script) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return false;
        }

        // lazy init sprite
        if (depositWornImage == null) {
            SpriteDefinition sprite = script.getSpriteManager().getSprite(DEPOSIT_WORN_SPRITE_ID, 0);
            if (sprite == null) {
                script.log(BankingUtils.class, "deposit worn items sprite not found");
                return false;
            }
            SingleThresholdComparator tolerance = new SingleThresholdComparator(15);
            Image image = new Image(sprite);
            depositWornImage = image.toSearchableImage(tolerance, ColorModel.RGB);
        }

        List<ImageSearchResult> matches = script.getImageAnalyzer().findLocations(depositWornImage);
        if (matches == null || matches.isEmpty()) {
            return false;
        }

        Rectangle bounds = matches.get(0).getBounds();
        return script.getFinger().tap(bounds);
    }

    /**
     * Deposits all worn equipment and inventory items for a clean slate.
     * Useful before restocking to ensure starting from empty state.
     *
     * <p>Requires bank to be open before calling.
     *
     * @param script the script instance
     * @return true if both deposits succeeded
     */
    public static boolean depositAll(Script script) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return false;
        }

        // deposit worn items first
        boolean wornSuccess = depositWornItems(script);
        script.pollFramesUntil(() -> false, 300);

        // deposit inventory
        boolean invSuccess = script.getWidgetManager().getBank().depositAll(Collections.emptySet());
        return wornSuccess && invSuccess;
    }
}
