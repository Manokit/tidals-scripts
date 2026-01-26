package utilities.loadout.ui;

import com.osmb.api.ScriptCore;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import utilities.loadout.EquipmentSlot;
import utilities.loadout.Loadout;
import utilities.loadout.LoadoutItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JavaFX component displaying the 11 equipment slots in exact OSRS layout.
 *
 * <p>Layout matches the OSRS Worn Equipment tab:
 * <pre>
 *            [Head]
 *   [Cape]  [Amulet]  [Ammo]
 *   [Weapon] [Body]  [Shield]
 *            [Legs]
 *   [Gloves]         [Boots]
 *            [Ring]
 * </pre>
 *
 * <p>Empty slots show faded placeholder icons indicating the slot type.
 * Uses absolute positioning with Pane to match OSRS layout exactly.
 */
public class EquipmentGrid extends Pane {

    // slot dimensions
    private static final int SLOT_SIZE = 36;
    private static final int SLOT_GAP = 4;
    private static final int COLUMN_WIDTH = SLOT_SIZE + SLOT_GAP;
    private static final int ROW_HEIGHT = SLOT_SIZE + 10;  // extra space for labels

    // grid dimensions: 3 columns, 6 rows
    private static final int GRID_WIDTH = 3 * COLUMN_WIDTH - SLOT_GAP;  // 116
    private static final int GRID_HEIGHT = 6 * ROW_HEIGHT;  // 276

    // placeholder image path
    private static final String PLACEHOLDER_PATH = "/loadout/slots/";

    private final ScriptCore core;
    private final Map<EquipmentSlot, ItemSlot> slots;
    private final Map<EquipmentSlot, Image> placeholderImages;
    private final ItemSearch itemSearch;

    private Loadout currentLoadout;
    private Consumer<Integer> onSlotClick;

    /**
     * Creates a new EquipmentGrid.
     *
     * @param core the ScriptCore for item lookups
     */
    public EquipmentGrid(ScriptCore core) {
        this.core = core;
        this.slots = new EnumMap<>(EquipmentSlot.class);
        this.placeholderImages = new EnumMap<>(EquipmentSlot.class);
        this.itemSearch = new ItemSearch(core);

        // fixed size
        setPrefSize(GRID_WIDTH, GRID_HEIGHT);
        setMinSize(GRID_WIDTH, GRID_HEIGHT);
        setMaxSize(GRID_WIDTH, GRID_HEIGHT);

        // load placeholder images
        loadPlaceholderImages();

        // create all 11 slots
        for (EquipmentSlot slotType : EquipmentSlot.values()) {
            createSlot(slotType);
        }

        // setup item search callback
        itemSearch.setOnItemSelected(this::handleItemSelected);
    }

    /**
     * Loads placeholder images for all slot types.
     */
    private void loadPlaceholderImages() {
        for (EquipmentSlot slotType : EquipmentSlot.values()) {
            String path = PLACEHOLDER_PATH + slotType.getPlaceholderImage();
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    Image image = new Image(is);
                    placeholderImages.put(slotType, image);
                }
            } catch (IOException e) {
                // placeholder not found, slot will show empty
            }
        }
    }

    /**
     * Creates a slot for the specified equipment slot type.
     *
     * @param slotType the equipment slot type
     */
    private void createSlot(EquipmentSlot slotType) {
        ItemSlot slot = new ItemSlot(core);

        // calculate position from grid coordinates
        int x = slotType.getGridColumn() * COLUMN_WIDTH;
        int y = slotType.getGridRow() * ROW_HEIGHT;
        slot.setLayoutX(x);
        slot.setLayoutY(y);

        // set placeholder image
        Image placeholder = placeholderImages.get(slotType);
        if (placeholder != null) {
            slot.setPlaceholder(placeholder);
        }

        // click handler
        slot.setOnClick(s -> {
            if (onSlotClick != null) {
                onSlotClick.accept(slotType.getIndex());
            }
            showItemSearch(slotType, s);
        });

        // mode change handler (handles clear and mode updates)
        slot.setOnModeChange((s, newMode) -> {
            handleModeChange(slotType, s, newMode);
        });

        slots.put(slotType, slot);
        getChildren().add(slot);
    }

    /**
     * Handles a mode change from the context menu (including clear).
     *
     * @param slotType the equipment slot type
     * @param slot the ItemSlot that changed
     * @param newMode the new mode, or null if slot was cleared
     */
    private void handleModeChange(EquipmentSlot slotType, ItemSlot slot, Object newMode) {
        if (currentLoadout == null) {
            return;
        }

        if (newMode == null) {
            // clear the slot
            currentLoadout.setEquipment(slotType.getIndex(), null);
        } else {
            // update with the slot's current item (may have updated mode)
            LoadoutItem item = slot.getItem();
            if (item != null) {
                currentLoadout.setEquipment(slotType.getIndex(), item);
            }
        }
    }

    /**
     * Shows the item search popup for the specified slot.
     *
     * @param slotType the slot type being edited
     * @param slot the ItemSlot component clicked
     */
    private void showItemSearch(EquipmentSlot slotType, ItemSlot slot) {
        // get screen position of slot
        javafx.geometry.Point2D screenPos = slot.localToScreen(0, SLOT_SIZE + 5);
        if (screenPos == null) {
            return;
        }

        // store which slot we're editing
        itemSearch.setUserData(slotType.getIndex());

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
        EquipmentSlot slotType = EquipmentSlot.fromIndex(slotIndex);
        if (slotType == null) {
            return;
        }

        // update loadout
        currentLoadout.setEquipment(slotIndex, item);

        // update slot display
        slots.get(slotType).setItem(item);
    }

    /**
     * Sets the loadout to display and edit.
     *
     * @param loadout the Loadout to display
     * @param core the ScriptCore (unused, kept for API consistency)
     */
    public void setLoadout(Loadout loadout, ScriptCore core) {
        this.currentLoadout = loadout;

        if (loadout == null) {
            // clear all slots
            for (ItemSlot slot : slots.values()) {
                slot.clearItem();
            }
            return;
        }

        // populate slots from loadout equipment
        for (EquipmentSlot slotType : EquipmentSlot.values()) {
            LoadoutItem item = loadout.getEquipmentSlot(slotType.getIndex());
            slots.get(slotType).setItem(item);
        }
    }

    /**
     * Sets the callback for slot clicks.
     *
     * @param onSlotClick consumer receiving the slot index (0-10)
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
     * @param index the slot index (0-10)
     * @return the ItemSlot, or null if invalid index
     */
    public ItemSlot getSlot(int index) {
        EquipmentSlot slotType = EquipmentSlot.fromIndex(index);
        return slotType != null ? slots.get(slotType) : null;
    }

    /**
     * Gets the ItemSlot for the specified slot type.
     *
     * @param slotType the equipment slot type
     * @return the ItemSlot
     */
    public ItemSlot getSlot(EquipmentSlot slotType) {
        return slots.get(slotType);
    }
}
