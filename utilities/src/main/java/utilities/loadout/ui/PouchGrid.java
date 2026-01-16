package utilities.loadout.ui;

import com.osmb.api.ScriptCore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utilities.loadout.LoadoutItem;

import java.util.function.Consumer;

/**
 * Reusable JavaFX component for pouch containers (rune pouch, bolt pouch, quiver).
 *
 * <p>Features:
 * <ul>
 *   <li>Configurable slot count (1-4)</li>
 *   <li>Title label at top</li>
 *   <li>Enable/disable checkbox</li>
 *   <li>Horizontal row of ItemSlot instances</li>
 *   <li>Click handler callback with slot index</li>
 *   <li>Disabled state grays out the component</li>
 * </ul>
 *
 * <p>Usage:
 * <ul>
 *   <li>Rune Pouch: new PouchGrid("Rune Pouch", 4, core)</li>
 *   <li>Bolt Pouch: new PouchGrid("Bolt Pouch", 4, core)</li>
 *   <li>Quiver: new PouchGrid("Quiver", 1, core)</li>
 * </ul>
 */
public class PouchGrid extends VBox {

    // osrs equipment tab theme colors
    private static final String TITLE_COLOR = "#FF981F";  // osrs orange
    private static final String LABEL_STYLE =
        "-fx-text-fill: " + TITLE_COLOR + ";" +
        "-fx-font-size: 11px;" +
        "-fx-font-weight: bold;";
    private static final String CHECKBOX_STYLE =
        "-fx-text-fill: #C0B8A8;" +  // muted tan text
        "-fx-font-size: 10px;";
    private static final double DISABLED_OPACITY = 0.5;

    private final ScriptCore core;
    private final String title;
    private final int slotCount;
    private final ItemSlot[] slots;
    private final HBox slotsContainer;
    private final CheckBox enableCheckbox;
    private final ItemSearch itemSearch;

    private Consumer<Integer> onSlotClick;
    private boolean enabled;

    /**
     * Creates a new PouchGrid.
     *
     * @param title the title to display (e.g., "Rune Pouch")
     * @param slotCount the number of slots (1-4)
     * @param core the ScriptCore for item lookups
     */
    public PouchGrid(String title, int slotCount, ScriptCore core) {
        this.core = core;
        this.title = title;
        this.slotCount = slotCount;
        this.slots = new ItemSlot[slotCount];
        this.itemSearch = new ItemSearch(core);
        this.enabled = false;

        // layout configuration
        setSpacing(4);
        setPadding(new Insets(4));
        setAlignment(Pos.TOP_CENTER);

        // header with checkbox and title
        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);

        enableCheckbox = new CheckBox();
        enableCheckbox.setStyle(CHECKBOX_STYLE);
        enableCheckbox.setSelected(false);
        enableCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            setEnabled(newVal);
        });

        Label titleLabel = new Label(title);
        titleLabel.setStyle(LABEL_STYLE);

        header.getChildren().addAll(enableCheckbox, titleLabel);

        // slots container
        slotsContainer = new HBox(2);
        slotsContainer.setAlignment(Pos.CENTER);

        // create slots
        for (int i = 0; i < slotCount; i++) {
            createSlot(i);
        }

        getChildren().addAll(header, slotsContainer);

        // setup item search callback
        itemSearch.setOnItemSelected(this::handleItemSelected);

        // initial disabled state
        updateEnabledState();
    }

    /**
     * Creates a slot at the specified index.
     *
     * @param index the slot index
     */
    private void createSlot(int index) {
        ItemSlot slot = new ItemSlot(core);

        final int slotIndex = index;
        slot.setOnClick(s -> {
            if (!enabled) {
                return;
            }
            if (onSlotClick != null) {
                onSlotClick.accept(slotIndex);
            }
            showItemSearch(slotIndex, s);
        });

        slots[index] = slot;
        slotsContainer.getChildren().add(slot);
    }

    /**
     * Shows the item search popup for the specified slot.
     *
     * @param slotIndex the slot index being edited
     * @param slot the ItemSlot component clicked
     */
    private void showItemSearch(int slotIndex, ItemSlot slot) {
        if (!enabled) {
            return;
        }

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
        if (userData == null) {
            return;
        }

        int slotIndex = (Integer) userData;
        slots[slotIndex].setItem(item);
    }

    /**
     * Sets the items to display in this pouch.
     *
     * @param items array of items (up to slotCount), or null to clear
     */
    public void setItems(LoadoutItem[] items) {
        if (items == null) {
            for (ItemSlot slot : slots) {
                slot.clearItem();
            }
            return;
        }

        for (int i = 0; i < slotCount; i++) {
            if (i < items.length && items[i] != null) {
                slots[i].setItem(items[i]);
            } else {
                slots[i].clearItem();
            }
        }
    }

    /**
     * Gets the items from all slots.
     *
     * @return array of items (may contain nulls for empty slots)
     */
    public LoadoutItem[] getItems() {
        LoadoutItem[] items = new LoadoutItem[slotCount];
        for (int i = 0; i < slotCount; i++) {
            items[i] = slots[i].getItem();
        }
        return items;
    }

    /**
     * Sets whether this pouch is enabled.
     * Disabled pouches are grayed out and non-interactive.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enableCheckbox.isSelected() != enabled) {
            enableCheckbox.setSelected(enabled);
        }
        updateEnabledState();
    }

    /**
     * Returns whether this pouch is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Updates the visual state based on enabled flag.
     */
    private void updateEnabledState() {
        slotsContainer.setOpacity(enabled ? 1.0 : DISABLED_OPACITY);
        slotsContainer.setDisable(!enabled);
    }

    /**
     * Sets the callback for slot clicks.
     *
     * @param onSlotClick consumer receiving the slot index (0 to slotCount-1)
     */
    public void setOnSlotClick(Consumer<Integer> onSlotClick) {
        this.onSlotClick = onSlotClick;
    }

    /**
     * Gets the title of this pouch.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the number of slots in this pouch.
     *
     * @return the slot count
     */
    public int getSlotCount() {
        return slotCount;
    }

    /**
     * Gets the ItemSlot at the specified index.
     *
     * @param index the slot index
     * @return the ItemSlot
     */
    public ItemSlot getSlot(int index) {
        return slots[index];
    }
}
