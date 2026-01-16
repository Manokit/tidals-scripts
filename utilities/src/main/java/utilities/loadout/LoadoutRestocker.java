package utilities.loadout;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.script.Script;
import com.osmb.api.utils.UIResult;
import utilities.BankSearchUtils;
import utilities.items.ItemResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Restocks a loadout from the bank with quantity mode handling.
 *
 * <p>Fast restock flow (bank stays open):
 * <ol>
 *   <li>Withdraw equipment items (skips if not in bank = already equipped)</li>
 *   <li>Equip items directly from inventory while bank is open</li>
 *   <li>Compare inventory (works while bank open)</li>
 *   <li>Withdraw missing inventory items</li>
 * </ol>
 *
 * <p>Quantity mode behavior:
 * <ul>
 *   <li>EXACT: soft requirement - take what's available, don't fail</li>
 *   <li>UNLIMITED: soft requirement - take entire stack, don't fail</li>
 *   <li>MINIMUM: hard requirement - fail restock if unmet</li>
 * </ul>
 */
public final class LoadoutRestocker {

    private final Script script;
    private final ItemResolver itemResolver;
    private final LoadoutComparator comparator;

    /**
     * Creates a new LoadoutRestocker.
     *
     * @param script       the script instance for bank/inventory access
     * @param itemResolver resolver for fuzzy matching and variant lookups
     */
    public LoadoutRestocker(Script script, ItemResolver itemResolver) {
        this.script = script;
        this.itemResolver = itemResolver;
        this.comparator = new LoadoutComparator(itemResolver);
    }

    /**
     * Restocks only inventory items from the bank.
     *
     * <p>Use this for subsequent restocks after equipment has been validated.
     * Much faster than full restock since it skips equipment entirely.
     *
     * <p>Bank stays open the entire time.
     *
     * @param loadout the target loadout to restock
     * @return result indicating success, partial, or failure
     */
    public RestockResult restockInventory(Loadout loadout) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(getClass(), "restock failed: bank not visible");
            return RestockResult.failed(null, "bank not visible");
        }

        List<MissingItem> restocked = new ArrayList<>();
        List<MissingItem> unfulfilled = new ArrayList<>();

        // compare inventory only (works while bank open)
        List<MissingItem> inventoryMissing = comparator.compareInventoryOnly(script, loadout);

        if (inventoryMissing.isEmpty()) {
            script.log(getClass(), "inventory complete, nothing to restock");
            return RestockResult.nothingMissing();
        }

        script.log(getClass(), "found " + inventoryMissing.size() + " missing inventory items");

        for (MissingItem item : inventoryMissing) {
            boolean success = withdrawInventoryItem(item);
            if (success) {
                restocked.add(item);
            } else {
                unfulfilled.add(item);
                if (item.getLoadoutItem().getMode() == QuantityMode.MINIMUM) {
                    script.log(getClass(), "MINIMUM inventory item failed, aborting restock");
                    BankSearchUtils.clearSearch(script);
                    return RestockResult.failed(inventoryMissing, "minimum inventory requirement not met");
                }
            }
        }

        BankSearchUtils.clearSearch(script);

        if (unfulfilled.isEmpty()) {
            script.log(getClass(), "inventory restock complete");
            return RestockResult.success(restocked);
        } else {
            script.log(getClass(), "inventory restock partial: " + unfulfilled.size() + " unfulfilled");
            return RestockResult.partial(restocked, unfulfilled);
        }
    }

    /**
     * Full restock including equipment.
     *
     * <p>Use this for initial setup. For subsequent restocks after equipment
     * is validated, use {@link #restockInventory(Loadout)} instead.
     *
     * <p>Bank stays open the entire time for speed. Equipment is equipped
     * directly from inventory while bank is open.
     *
     * @param loadout the target loadout to restock
     * @return result indicating success, partial, or failure
     */
    public RestockResult restock(Loadout loadout) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(getClass(), "restock failed: bank not visible");
            return RestockResult.failed(null, "bank not visible");
        }

        List<MissingItem> restocked = new ArrayList<>();
        List<MissingItem> unfulfilled = new ArrayList<>();

        // phase 1: withdraw and equip equipment items (don't compare, just try to get them)
        LoadoutItem[] equipmentSlots = loadout.getEquipment();
        boolean anyEquipmentWithdrawn = false;

        for (int slot = 0; slot < Loadout.EQUIPMENT_SIZE; slot++) {
            LoadoutItem required = equipmentSlots[slot];
            if (required == null) continue;

            // try to withdraw from bank (if already equipped, won't be in bank - that's fine)
            boolean withdrawn = withdrawEquipmentItem(required);
            if (withdrawn) {
                anyEquipmentWithdrawn = true;
                // equip directly from inventory while bank is open
                equipItemFromInventory(required);
            }
            // don't track as unfulfilled - item might already be equipped
        }

        // clear search after equipment phase
        if (anyEquipmentWithdrawn) {
            BankSearchUtils.clearSearch(script);
        }

        // phase 2: compare inventory only (works while bank open) and withdraw missing
        List<MissingItem> inventoryMissing = comparator.compareInventoryOnly(script, loadout);

        if (!inventoryMissing.isEmpty()) {
            script.log(getClass(), "found " + inventoryMissing.size() + " missing inventory items");

            for (MissingItem item : inventoryMissing) {
                boolean success = withdrawInventoryItem(item);
                if (success) {
                    restocked.add(item);
                } else {
                    unfulfilled.add(item);
                    if (item.getLoadoutItem().getMode() == QuantityMode.MINIMUM) {
                        script.log(getClass(), "MINIMUM inventory item failed, aborting restock");
                        BankSearchUtils.clearSearch(script);
                        return RestockResult.failed(inventoryMissing, "minimum inventory requirement not met");
                    }
                }
            }

            BankSearchUtils.clearSearch(script);
        }

        // bank stays open - caller can close if needed
        if (unfulfilled.isEmpty()) {
            script.log(getClass(), "restock complete");
            return RestockResult.success(restocked);
        } else {
            script.log(getClass(), "restock partial: " + unfulfilled.size() + " unfulfilled");
            return RestockResult.partial(restocked, unfulfilled);
        }
    }

    /**
     * Withdraws an equipment item from the bank.
     * Returns false if item not in bank (may already be equipped).
     */
    private boolean withdrawEquipmentItem(LoadoutItem loadoutItem) {
        int itemId = loadoutItem.getItemId();

        if (loadoutItem.isFuzzy()) {
            int[] variants = itemResolver.getAllVariants(itemId);
            for (int variantId : variants) {
                if (BankSearchUtils.searchAndWithdrawVerified(script, variantId, 1, true)) {
                    script.log(getClass(), "withdrew equipment: " + variantId);
                    // reset (not clear) - caller may have more withdrawals; caller clears when done
                    BankSearchUtils.clickSearchToReset(script);
                    return true;
                }
            }
            return false;
        }

        if (BankSearchUtils.searchAndWithdrawVerified(script, itemId, 1, true)) {
            script.log(getClass(), "withdrew equipment: " + itemId);
            BankSearchUtils.clickSearchToReset(script);
            return true;
        }
        return false;
    }

    /**
     * Equips an item directly from inventory while bank is open.
     * Uses menu actions (Wear/Wield/Equip) which work with bank open.
     */
    private boolean equipItemFromInventory(LoadoutItem loadoutItem) {
        Set<Integer> idsToCheck;
        if (loadoutItem.isFuzzy()) {
            idsToCheck = toSet(itemResolver.getAllVariants(loadoutItem.getItemId()));
        } else {
            idsToCheck = Set.of(loadoutItem.getItemId());
        }

        ItemGroupResult invResult = script.getWidgetManager().getInventory().search(idsToCheck);
        if (invResult == null) return false;

        ItemSearchResult foundItem = null;
        for (int id : idsToCheck) {
            if (invResult.contains(id)) {
                foundItem = invResult.getItem(id);
                if (foundItem != null) break;
            }
        }

        if (foundItem == null) return false;

        // try equip actions - these work while bank is open
        String[] equipActions = {"Wear", "Wield", "Equip"};
        for (String action : equipActions) {
            if (foundItem.interact(action)) {
                script.log(getClass(), "equipped: " + foundItem.getId() + " via " + action);
                return true;
            }
        }
        return false;
    }

    /**
     * Withdraws an inventory item with quantity mode handling.
     */
    private boolean withdrawInventoryItem(MissingItem item) {
        LoadoutItem loadoutItem = item.getLoadoutItem();
        int itemId = loadoutItem.getItemId();
        QuantityMode mode = loadoutItem.getMode();

        int amount;
        switch (mode) {
            case UNLIMITED:
                amount = 0; // 0 = withdraw all in BankSearchUtils
                break;
            case EXACT:
            case MINIMUM:
            default:
                amount = item.getDeficit();
                break;
        }

        if (amount == 0 && mode != QuantityMode.UNLIMITED) {
            return true; // nothing to withdraw
        }

        if (loadoutItem.isFuzzy()) {
            int[] variants = itemResolver.getAllVariants(itemId);
            for (int variantId : variants) {
                if (BankSearchUtils.searchAndWithdrawVerified(script, variantId, amount, true)) {
                    script.log(getClass(), "withdrew inventory: " + variantId + " x" + (amount == 0 ? "all" : amount));
                    BankSearchUtils.clickSearchToReset(script);
                    return true;
                }
            }
            return false;
        }

        if (BankSearchUtils.searchAndWithdrawVerified(script, itemId, amount, true)) {
            script.log(getClass(), "withdrew inventory: " + itemId + " x" + (amount == 0 ? "all" : amount));
            BankSearchUtils.clickSearchToReset(script);
            return true;
        }
        return false;
    }

    /**
     * Converts an int array to a Set of Integers.
     */
    private Set<Integer> toSet(int[] ids) {
        Set<Integer> set = new HashSet<>();
        for (int id : ids) {
            set.add(id);
        }
        return set;
    }
}
