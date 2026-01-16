package utilities.loadout.ui;

import com.osmb.api.ScriptCore;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import utilities.items.StackabilityUtil;
import utilities.loadout.Loadout;
import utilities.loadout.LoadoutItem;
import utilities.loadout.QuantityMode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * JavaFX component displaying the 28 inventory slots in a 4x7 grid.
 *
 * <p>This grid shows a visual preview of what the inventory will look like
 * after all items are withdrawn. For non-stackable items with quantity > 1,
 * the items are expanded to fill multiple slots.
 *
 * <p>Layout matches the OSRS inventory:
 * <ul>
 *   <li>4 columns, 7 rows</li>
 *   <li>Slots numbered left-to-right, top-to-bottom (0-27)</li>
 *   <li>2px gap between slots</li>
 * </ul>
 *
 * <p>Total size approximately 154x270 pixels (4*36 + 3*2 gap, 7*36 + 6*2 gap).
 */
public class InventoryGrid extends GridPane {

    // grid dimensions
    private static final int COLUMNS = 4;
    private static final int ROWS = 7;
    private static final int GAP = 2;

    // expansion slot styling - slightly dimmed to indicate auto-generated
    private static final String EXPANSION_SLOT_STYLE = "-fx-opacity: 0.85;";

    private final ScriptCore core;
    private final ItemSlot[] slots;
    private final ItemSearch itemSearch;

    // tracks which slots are primary (user-assigned) vs expansion
    // primarySlotIndex[i] = -1 means slot i is a primary slot
    // primarySlotIndex[i] = N means slot i is an expansion of primary slot N
    private final int[] primarySlotIndex;

    // internal representation of items for visual display
    private final SlotEntry[] visualSlots;

    private Loadout currentLoadout;
    private Consumer<Integer> onSlotClick;

    /**
     * Internal class to track what's displayed in each visual slot.
     */
    private static class SlotEntry {
        LoadoutItem item;
        boolean isPrimary;
        int primaryIndex;  // for expansion slots, the index of their primary

        SlotEntry() {
            this.item = null;
            this.isPrimary = true;
            this.primaryIndex = -1;
        }
    }

    /**
     * Creates a new InventoryGrid.
     *
     * @param core the ScriptCore for item lookups
     */
    public InventoryGrid(ScriptCore core) {
        this.core = core;
        this.slots = new ItemSlot[Loadout.INVENTORY_SIZE];
        this.primarySlotIndex = new int[Loadout.INVENTORY_SIZE];
        this.visualSlots = new SlotEntry[Loadout.INVENTORY_SIZE];
        this.itemSearch = new ItemSearch(core);

        // initialize tracking arrays
        for (int i = 0; i < Loadout.INVENTORY_SIZE; i++) {
            primarySlotIndex[i] = -1;
            visualSlots[i] = new SlotEntry();
        }

        // grid configuration
        setHgap(GAP);
        setVgap(GAP);

        // create all 28 slots in 4x7 grid
        for (int i = 0; i < Loadout.INVENTORY_SIZE; i++) {
            createSlot(i);
        }

        // setup item search callback
        itemSearch.setOnItemSelected(this::handleItemSelected);
    }

    /**
     * Creates a slot at the specified index in the grid.
     *
     * @param index the slot index (0-27)
     */
    private void createSlot(int index) {
        ItemSlot slot = new ItemSlot(core);

        // calculate grid position
        int column = index % COLUMNS;
        int row = index / COLUMNS;

        // click handler
        final int slotIndex = index;
        slot.setOnClick(s -> {
            // only allow clicking on primary or empty slots
            if (visualSlots[slotIndex].isPrimary || visualSlots[slotIndex].item == null) {
                if (onSlotClick != null) {
                    onSlotClick.accept(slotIndex);
                }
                showItemSearch(slotIndex, s);
            }
        });

        // mode change handler (from right-click menu)
        slot.setOnModeChange((s, newMode) -> {
            handleModeChange(slotIndex, s, newMode);
        });

        slots[index] = slot;
        add(slot, column, row);
    }

    /**
     * Handles a mode change from the context menu.
     *
     * @param slotIndex the visual slot index
     * @param slot the ItemSlot that changed
     * @param newMode the new mode, or null if slot was cleared
     */
    private void handleModeChange(int slotIndex, ItemSlot slot, QuantityMode newMode) {
        if (currentLoadout == null) {
            return;
        }

        SlotEntry entry = visualSlots[slotIndex];

        // find the primary slot (might be this slot or a referenced one)
        int primary = entry.isPrimary ? slotIndex : entry.primaryIndex;
        if (primary < 0) {
            return;
        }

        if (newMode == null) {
            // clear the item from loadout
            clearPrimarySlot(primary);
        } else {
            // get the updated item from the slot (may have updated quantity or mode)
            LoadoutItem slotItem = slot.getItem();
            if (slotItem != null) {
                // use the slot's item which has the latest quantity and mode
                setPrimaryItem(primary, slotItem);
            }
        }
    }

    /**
     * Shows the item search popup for the specified slot.
     *
     * @param slotIndex the slot index being edited
     * @param slot the ItemSlot component clicked
     */
    private void showItemSearch(int slotIndex, ItemSlot slot) {
        // get screen position of slot
        javafx.geometry.Point2D screenPos = slot.localToScreen(0, 36 + 5);
        if (screenPos == null) {
            return;
        }

        // store which slot we're editing
        itemSearch.setUserData(slotIndex);

        // show popup
        Stage owner = (Stage) getScene().getWindow();
        itemSearch.showAsPopup(owner, screenPos.getX(), screenPos.getY());
    }

    /**
     * Handles item selection from the search popup.
     *
     * @param item the selected item
     */
    private void handleItemSelected(LoadoutItem item) {
        Object userData = itemSearch.getUserData();
        if (userData == null || currentLoadout == null) {
            return;
        }

        int slotIndex = (Integer) userData;

        // set item at this slot
        setPrimaryItem(slotIndex, item);
    }

    /**
     * Sets an item at a primary slot and handles visual expansion.
     *
     * @param slotIndex the primary slot index
     * @param item the item to set
     */
    private void setPrimaryItem(int slotIndex, LoadoutItem item) {
        if (item == null) {
            clearPrimarySlot(slotIndex);
            return;
        }

        // first clear any existing item at this slot
        clearPrimarySlot(slotIndex);

        // determine if this item stacks
        boolean stacks = StackabilityUtil.isStackable(item.getItemId());
        int slotsNeeded = stacks ? 1 : item.getQuantity();

        // count how many slots are available starting from slotIndex
        int availableSlots = countAvailableSlotsFrom(slotIndex);

        // cap slots needed to what's available
        int actualSlots = Math.min(slotsNeeded, availableSlots);

        // update loadout with potentially adjusted quantity
        LoadoutItem adjustedItem = item;
        if (!stacks && actualSlots < item.getQuantity()) {
            // reduce quantity to what fits
            adjustedItem = LoadoutItem.builder()
                .itemId(item.getItemId())
                .name(item.getName())
                .quantity(actualSlots)
                .mode(item.getMode())
                .fuzzy(item.isFuzzy())
                .build();
        }

        // update loadout (store in first available slot)
        currentLoadout.setInventorySlot(slotIndex, adjustedItem);

        // fill visual slots
        visualSlots[slotIndex].item = adjustedItem;
        visualSlots[slotIndex].isPrimary = true;
        visualSlots[slotIndex].primaryIndex = -1;
        primarySlotIndex[slotIndex] = -1;

        // for stackable items, show with quantity badge
        if (stacks) {
            slots[slotIndex].setItem(adjustedItem);
            slots[slotIndex].setStyle("");  // clear any expansion styling
        } else {
            // show single item (qty 1 display) in primary
            LoadoutItem singleDisplay = LoadoutItem.builder()
                .itemId(adjustedItem.getItemId())
                .name(adjustedItem.getName())
                .quantity(1)
                .mode(adjustedItem.getMode())
                .fuzzy(adjustedItem.isFuzzy())
                .build();
            slots[slotIndex].setItem(singleDisplay);
            slots[slotIndex].setStyle("");

            // fill expansion slots
            int filled = 1;
            for (int i = slotIndex + 1; i < Loadout.INVENTORY_SIZE && filled < actualSlots; i++) {
                if (visualSlots[i].item == null) {
                    visualSlots[i].item = adjustedItem;
                    visualSlots[i].isPrimary = false;
                    visualSlots[i].primaryIndex = slotIndex;
                    primarySlotIndex[i] = slotIndex;

                    slots[i].setItem(singleDisplay);
                    slots[i].setStyle(EXPANSION_SLOT_STYLE);
                    filled++;
                }
            }
        }
    }

    /**
     * Clears a primary slot and its expansion slots.
     *
     * @param primaryIndex the primary slot to clear
     */
    private void clearPrimarySlot(int primaryIndex) {
        if (primaryIndex < 0 || primaryIndex >= Loadout.INVENTORY_SIZE) {
            return;
        }

        // clear primary
        visualSlots[primaryIndex].item = null;
        visualSlots[primaryIndex].isPrimary = true;
        visualSlots[primaryIndex].primaryIndex = -1;
        primarySlotIndex[primaryIndex] = -1;
        slots[primaryIndex].clearItem();
        slots[primaryIndex].setStyle("");

        if (currentLoadout != null) {
            currentLoadout.setInventorySlot(primaryIndex, null);
        }

        // clear all expansion slots referencing this primary
        for (int i = 0; i < Loadout.INVENTORY_SIZE; i++) {
            if (primarySlotIndex[i] == primaryIndex) {
                visualSlots[i].item = null;
                visualSlots[i].isPrimary = true;
                visualSlots[i].primaryIndex = -1;
                primarySlotIndex[i] = -1;
                slots[i].clearItem();
                slots[i].setStyle("");
            }
        }
    }

    /**
     * Counts available (empty) slots starting from a given index.
     *
     * @param startIndex the starting index
     * @return count of empty slots from startIndex to end of inventory
     */
    private int countAvailableSlotsFrom(int startIndex) {
        int count = 0;
        for (int i = startIndex; i < Loadout.INVENTORY_SIZE; i++) {
            if (visualSlots[i].item == null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Sets the loadout to display and edit.
     *
     * @param loadout the Loadout to display
     * @param core the ScriptCore (unused, kept for API consistency)
     */
    public void setLoadout(Loadout loadout, ScriptCore core) {
        this.currentLoadout = loadout;

        // clear all visual state
        for (int i = 0; i < Loadout.INVENTORY_SIZE; i++) {
            visualSlots[i].item = null;
            visualSlots[i].isPrimary = true;
            visualSlots[i].primaryIndex = -1;
            primarySlotIndex[i] = -1;
            slots[i].clearItem();
            slots[i].setStyle("");
        }

        if (loadout == null) {
            return;
        }

        // populate from loadout, expanding non-stackables
        // collect all non-null items first
        List<int[]> items = new ArrayList<>();  // [slotIndex, itemIdForPriority]
        for (int i = 0; i < Loadout.INVENTORY_SIZE; i++) {
            LoadoutItem item = loadout.getInventorySlot(i);
            if (item != null) {
                items.add(new int[]{i, item.getItemId()});
            }
        }

        // process each item
        for (int[] entry : items) {
            int slotIndex = entry[0];
            LoadoutItem item = loadout.getInventorySlot(slotIndex);
            if (item != null && visualSlots[slotIndex].item == null) {
                // set without updating loadout (it's already there)
                setItemVisual(slotIndex, item);
            }
        }
    }

    /**
     * Sets visual display for an item (without modifying loadout).
     * Used when populating from existing loadout.
     *
     * @param slotIndex the slot index
     * @param item the item to display
     */
    private void setItemVisual(int slotIndex, LoadoutItem item) {
        if (item == null) {
            return;
        }

        boolean stacks = StackabilityUtil.isStackable(item.getItemId());
        int slotsNeeded = stacks ? 1 : item.getQuantity();

        // mark primary
        visualSlots[slotIndex].item = item;
        visualSlots[slotIndex].isPrimary = true;
        visualSlots[slotIndex].primaryIndex = -1;
        primarySlotIndex[slotIndex] = -1;

        if (stacks) {
            slots[slotIndex].setItem(item);
            slots[slotIndex].setStyle("");
        } else {
            // single display for non-stackable
            LoadoutItem singleDisplay = LoadoutItem.builder()
                .itemId(item.getItemId())
                .name(item.getName())
                .quantity(1)
                .mode(item.getMode())
                .fuzzy(item.isFuzzy())
                .build();
            slots[slotIndex].setItem(singleDisplay);
            slots[slotIndex].setStyle("");

            // fill expansion slots
            int filled = 1;
            for (int i = slotIndex + 1; i < Loadout.INVENTORY_SIZE && filled < slotsNeeded; i++) {
                if (visualSlots[i].item == null) {
                    visualSlots[i].item = item;
                    visualSlots[i].isPrimary = false;
                    visualSlots[i].primaryIndex = slotIndex;
                    primarySlotIndex[i] = slotIndex;

                    slots[i].setItem(singleDisplay);
                    slots[i].setStyle(EXPANSION_SLOT_STYLE);
                    filled++;
                }
            }
        }
    }

    /**
     * Sets the callback for slot clicks.
     *
     * @param onSlotClick consumer receiving the slot index (0-27)
     */
    public void setOnSlotClick(Consumer<Integer> onSlotClick) {
        this.onSlotClick = onSlotClick;
    }

    /**
     * Gets the current loadout being edited.
     *
     * @return the current Loadout, or null if none set
     */
    public Loadout getLoadout() {
        return currentLoadout;
    }

    /**
     * Gets the ItemSlot at the specified index.
     *
     * @param index the slot index (0-27)
     * @return the ItemSlot
     */
    public ItemSlot getSlot(int index) {
        return slots[index];
    }

    /**
     * Gets the total slots used including expansions.
     *
     * @return count of filled visual slots
     */
    public int getTotalSlotsUsed() {
        int count = 0;
        for (SlotEntry entry : visualSlots) {
            if (entry.item != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the count of unique items (primary slots only).
     *
     * @return count of primary items
     */
    public int getPrimaryItemCount() {
        int count = 0;
        for (SlotEntry entry : visualSlots) {
            if (entry.item != null && entry.isPrimary) {
                count++;
            }
        }
        return count;
    }
}
