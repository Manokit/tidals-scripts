package utilities.loadout;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.script.Script;
import com.osmb.api.utils.UIResult;
import utilities.items.ItemResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Compares current player state against a loadout baseline to identify missing items.
 *
 * <p>Checks equipment slots first, then inventory. Supports fuzzy matching for
 * degradable items via {@link ItemResolver}.
 *
 * <p>Quantity modes affect deficit calculation:
 * <ul>
 *   <li>EXACT: need exactly N, deficit = max(0, N - current)</li>
 *   <li>UNLIMITED: treat as missing if not present (no specific deficit count)</li>
 *   <li>MINIMUM: need at least N, deficit = max(0, N - current)</li>
 * </ul>
 */
public final class LoadoutComparator {

    private final ItemResolver itemResolver;

    /**
     * Creates a new LoadoutComparator.
     *
     * @param itemResolver resolver for fuzzy matching and item lookups
     */
    public LoadoutComparator(ItemResolver itemResolver) {
        this.itemResolver = itemResolver;
    }

    /**
     * Compares current player state to the loadout baseline.
     * Note: Equipment comparison requires bank to be closed (tabs visible).
     *
     * @param script  the script instance for widget access
     * @param loadout the target loadout to compare against
     * @return list of items that are missing or need restocking (empty if nothing missing)
     */
    public List<MissingItem> compare(Script script, Loadout loadout) {
        List<MissingItem> missing = new ArrayList<>();

        // check equipment first (equipment is more critical)
        missing.addAll(compareEquipment(script, loadout));

        // then check inventory
        missing.addAll(compareInventory(script, loadout));

        // v1: skip rune pouch, bolt pouch, quiver (focus on equipment + inventory)

        return missing;
    }

    /**
     * Compares only inventory (not equipment).
     * This works while bank is open since inventory is visible.
     *
     * @param script  the script instance for widget access
     * @param loadout the target loadout to compare against
     * @return list of inventory items that are missing (empty if nothing missing)
     */
    public List<MissingItem> compareInventoryOnly(Script script, Loadout loadout) {
        return compareInventory(script, loadout);
    }

    /**
     * Compares current equipment to loadout equipment slots.
     */
    private List<MissingItem> compareEquipment(Script script, Loadout loadout) {
        List<MissingItem> missing = new ArrayList<>();
        LoadoutItem[] equipment = loadout.getEquipment();

        for (int slot = 0; slot < Loadout.EQUIPMENT_SIZE; slot++) {
            LoadoutItem required = equipment[slot];
            if (required == null) {
                continue; // empty slot in loadout, skip
            }

            // check if the required item (or a variant) is equipped
            boolean found = isItemEquipped(script, required);

            if (!found) {
                // equipment slots always have quantity 1
                MissingItem missingItem = new MissingItem(
                        required,
                        0, // current quantity
                        1, // needed quantity (equipment is always 1)
                        MissingItem.ContainerType.EQUIPMENT
                );
                missing.add(missingItem);
            }
        }

        return missing;
    }

    /**
     * Checks if a required item (or variant if fuzzy) is equipped.
     */
    private boolean isItemEquipped(Script script, LoadoutItem required) {
        int itemId = required.getItemId();

        // build list of IDs to check (exact ID, or all variants if fuzzy)
        int[] idsToCheck;
        if (required.isFuzzy()) {
            idsToCheck = itemResolver.getAllVariants(itemId);
        } else {
            idsToCheck = new int[]{itemId};
        }

        // check if any of the IDs are equipped
        UIResult<ItemSearchResult> result = script.getWidgetManager()
                .getEquipment()
                .findItem(idsToCheck);

        return result != null && result.isFound();
    }

    /**
     * Compares current inventory to loadout inventory slots.
     * Handles stackable items and multiple slots with the same item.
     */
    private List<MissingItem> compareInventory(Script script, Loadout loadout) {
        List<MissingItem> missing = new ArrayList<>();
        LoadoutItem[] inventory = loadout.getInventory();

        // build set of all item IDs we need to search for (including variants)
        Set<Integer> searchIds = new HashSet<>();
        for (LoadoutItem item : inventory) {
            if (item != null) {
                int itemId = item.getItemId();
                if (item.isFuzzy()) {
                    for (int variantId : itemResolver.getAllVariants(itemId)) {
                        searchIds.add(variantId);
                    }
                } else {
                    searchIds.add(itemId);
                }
            }
        }

        if (searchIds.isEmpty()) {
            return Collections.emptyList();
        }

        // get current inventory snapshot
        ItemGroupResult invSnapshot = script.getWidgetManager().getInventory().search(searchIds);

        // group loadout items by base ID to calculate total requirements
        // (handles multiple slots with the same item)
        java.util.Map<Integer, Integer> neededByBaseId = new java.util.HashMap<>();
        java.util.Map<Integer, LoadoutItem> itemByBaseId = new java.util.HashMap<>();
        java.util.Map<Integer, QuantityMode> modeByBaseId = new java.util.HashMap<>();

        for (LoadoutItem item : inventory) {
            if (item == null) continue;

            int baseId = item.isFuzzy()
                    ? itemResolver.getPreferredVariant(item.getItemId())
                    : item.getItemId();

            // accumulate needed quantities
            int existing = neededByBaseId.getOrDefault(baseId, 0);
            neededByBaseId.put(baseId, existing + item.getQuantity());

            // keep first item reference for each base ID
            if (!itemByBaseId.containsKey(baseId)) {
                itemByBaseId.put(baseId, item);
                modeByBaseId.put(baseId, item.getMode());
            }
        }

        // check each required item
        for (java.util.Map.Entry<Integer, Integer> entry : neededByBaseId.entrySet()) {
            int baseId = entry.getKey();
            int neededQuantity = entry.getValue();
            LoadoutItem loadoutItem = itemByBaseId.get(baseId);
            QuantityMode mode = modeByBaseId.get(baseId);

            // count current quantity (sum across all variant IDs if fuzzy)
            int currentQuantity = countItemQuantity(invSnapshot, loadoutItem);

            // determine if this item is missing based on mode
            boolean isMissing = isItemMissing(currentQuantity, neededQuantity, mode);

            if (isMissing) {
                MissingItem missingItem = new MissingItem(
                        loadoutItem,
                        currentQuantity,
                        neededQuantity,
                        MissingItem.ContainerType.INVENTORY
                );
                missing.add(missingItem);
            }
        }

        return missing;
    }

    /**
     * Counts how many of an item (or its variants) are in the inventory snapshot.
     */
    private int countItemQuantity(ItemGroupResult snapshot, LoadoutItem item) {
        if (snapshot == null) {
            return 0;
        }

        int total = 0;
        int itemId = item.getItemId();

        if (item.isFuzzy()) {
            // sum quantities across all variants
            for (int variantId : itemResolver.getAllVariants(itemId)) {
                total += snapshot.getAmount(variantId);
            }
        } else {
            total = snapshot.getAmount(itemId);
        }

        return total;
    }

    /**
     * Determines if an item is missing based on quantity mode.
     *
     * @param current current quantity
     * @param needed  required quantity
     * @param mode    how to interpret the requirement
     * @return true if the item should be considered missing
     */
    private boolean isItemMissing(int current, int needed, QuantityMode mode) {
        switch (mode) {
            case EXACT:
                // need exactly N, deficit if current < N
                return current < needed;

            case UNLIMITED:
                // treat as missing if not present at all
                // (when restocking, will take entire bank stack)
                return current == 0;

            case MINIMUM:
                // need at least N, deficit if current < N
                return current < needed;

            default:
                return current < needed;
        }
    }
}
