package utilities.loadout.ui;

import com.osmb.api.ScriptCore;
import com.osmb.api.javafx.JavaFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.StageStyle;
import utilities.loadout.LoadoutItem;
import utilities.loadout.QuantityMode;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Reusable JavaFX component that displays a single loadout slot.
 *
 * <p>Features:
 * <ul>
 *   <li>36x36 pixel size (standard OSRS slot)</li>
 *   <li>Dark background with border</li>
 *   <li>Item sprite centered</li>
 *   <li>Quantity badge in bottom-right corner</li>
 *   <li>Quantity mode indicator: "*" for UNLIMITED, ">" for MINIMUM</li>
 *   <li>Hover effect with teal border</li>
 *   <li>Click handler support</li>
 * </ul>
 */
public class ItemSlot extends StackPane {

    // standard OSRS slot dimensions
    private static final int SLOT_SIZE = 36;

    // osrs equipment tab theme colors
    private static final String BG_COLOR = "#252116";        // dark slot interior
    private static final String BORDER_COLOR = "#5C5142";    // tan/orange border
    private static final String HOVER_BORDER_COLOR = "#FF981F"; // osrs orange on hover
    private static final String BADGE_BG_COLOR = "rgba(0, 0, 0, 0.7)";
    private static final String BADGE_TEXT_COLOR = "#FFFF00"; // osrs yellow

    private final ScriptCore core;
    private final Rectangle background;
    private final ImageView placeholderView;
    private final ImageView spriteView;
    private final Label quantityBadge;
    private final ContextMenu contextMenu;

    private LoadoutItem currentItem;
    private Consumer<ItemSlot> onClick;
    private BiConsumer<ItemSlot, QuantityMode> onModeChange;

    /**
     * Creates a new ItemSlot.
     *
     * @param core the ScriptCore for ItemManager/sprite access
     */
    public ItemSlot(ScriptCore core) {
        this.core = core;

        // fixed size
        setPrefSize(SLOT_SIZE, SLOT_SIZE);
        setMinSize(SLOT_SIZE, SLOT_SIZE);
        setMaxSize(SLOT_SIZE, SLOT_SIZE);

        // background rectangle
        background = new Rectangle(SLOT_SIZE, SLOT_SIZE);
        background.setFill(Color.web(BG_COLOR));
        background.setStroke(Color.web(BORDER_COLOR));
        background.setStrokeWidth(1);
        background.setArcWidth(2);
        background.setArcHeight(2);

        // placeholder for empty slot (faded slot type icon)
        placeholderView = new ImageView();
        placeholderView.setFitWidth(32);
        placeholderView.setFitHeight(32);
        placeholderView.setPreserveRatio(true);
        placeholderView.setOpacity(0.4);
        StackPane.setAlignment(placeholderView, Pos.CENTER);

        // sprite for actual item (centered)
        spriteView = new ImageView();
        spriteView.setFitWidth(32);
        spriteView.setFitHeight(32);
        spriteView.setPreserveRatio(true);
        StackPane.setAlignment(spriteView, Pos.CENTER);

        // quantity badge (bottom-right)
        quantityBadge = new Label();
        quantityBadge.setStyle(
            "-fx-background-color: " + BADGE_BG_COLOR + ";" +
            "-fx-text-fill: " + BADGE_TEXT_COLOR + ";" +
            "-fx-font-size: 9px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 1 3 1 3;" +
            "-fx-background-radius: 2;"
        );
        quantityBadge.setVisible(false);
        StackPane.setAlignment(quantityBadge, Pos.BOTTOM_RIGHT);

        // context menu for quantity mode
        contextMenu = createContextMenu();

        // layer components
        getChildren().addAll(background, placeholderView, spriteView, quantityBadge);

        // hover effect
        setOnMouseEntered(e -> background.setStroke(Color.web(HOVER_BORDER_COLOR)));
        setOnMouseExited(e -> background.setStroke(Color.web(BORDER_COLOR)));

        // click handler - left click for item search, right click for context menu
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (onClick != null) {
                    onClick.accept(this);
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                if (currentItem != null) {
                    contextMenu.show(this, e.getScreenX(), e.getScreenY());
                }
            }
        });

        // cursor indicates clickable
        setStyle("-fx-cursor: hand;");
    }

    /**
     * Sets the placeholder image for empty slot (slot type indicator).
     *
     * @param image the placeholder image
     */
    public void setPlaceholder(javafx.scene.image.Image image) {
        placeholderView.setImage(image);
    }

    /**
     * Sets the item to display in this slot.
     *
     * @param item the LoadoutItem to display
     */
    public void setItem(LoadoutItem item) {
        this.currentItem = item;

        if (item == null) {
            clearItem();
            return;
        }

        // hide placeholder when item is set
        placeholderView.setVisible(false);

        // load sprite using JavaFXUtils
        ImageView itemImage = JavaFXUtils.getItemImageView(core, item.getItemId());
        if (itemImage != null && itemImage.getImage() != null) {
            spriteView.setImage(itemImage.getImage());
        } else {
            spriteView.setImage(null);
        }

        // update quantity badge
        updateQuantityBadge(item);
    }

    /**
     * Clears the slot, showing an empty state.
     */
    public void clearItem() {
        this.currentItem = null;
        spriteView.setImage(null);
        quantityBadge.setVisible(false);
        placeholderView.setVisible(true);
    }

    /**
     * Updates the quantity badge based on item quantity and mode.
     *
     * @param item the item to display badge for
     */
    private void updateQuantityBadge(LoadoutItem item) {
        int qty = item.getQuantity();
        QuantityMode mode = item.getMode();

        // build badge text with mode indicator
        String text;
        if (mode == QuantityMode.UNLIMITED) {
            text = "*";
        } else if (mode == QuantityMode.MINIMUM) {
            text = ">" + formatQuantity(qty);
        } else {
            // EXACT mode - only show if qty > 1
            if (qty <= 1) {
                quantityBadge.setVisible(false);
                return;
            }
            text = formatQuantity(qty);
        }

        quantityBadge.setText(text);
        quantityBadge.setVisible(true);
    }

    /**
     * Formats a quantity for display (e.g., 1000 -> 1K, 1000000 -> 1M).
     *
     * @param qty the quantity to format
     * @return formatted string
     */
    private String formatQuantity(int qty) {
        if (qty >= 1_000_000) {
            return (qty / 1_000_000) + "M";
        } else if (qty >= 1_000) {
            return (qty / 1_000) + "K";
        }
        return String.valueOf(qty);
    }

    /**
     * Gets the current LoadoutItem displayed in this slot.
     *
     * @return the current item, or null if empty
     */
    public LoadoutItem getItem() {
        return currentItem;
    }

    /**
     * Sets the click handler for this slot.
     *
     * @param onClick consumer called when slot is clicked
     */
    public void setOnClick(Consumer<ItemSlot> onClick) {
        this.onClick = onClick;
    }

    /**
     * Sets the callback for when quantity mode is changed via context menu.
     *
     * @param onModeChange consumer receiving the slot and new mode
     */
    public void setOnModeChange(BiConsumer<ItemSlot, QuantityMode> onModeChange) {
        this.onModeChange = onModeChange;
    }

    /**
     * Creates the context menu for quantity mode selection.
     *
     * @return the configured ContextMenu
     */
    private ContextMenu createContextMenu() {
        ContextMenu menu = new ContextMenu();

        // style the context menu to match OSRS theme
        menu.setStyle(
            "-fx-background-color: #3E3529;" +
            "-fx-border-color: #5C5142;" +
            "-fx-border-width: 1;"
        );

        MenuItem setQuantityItem = new MenuItem("Set Quantity...");
        setQuantityItem.setStyle("-fx-text-fill: #FF981F; -fx-font-weight: bold;");
        setQuantityItem.setOnAction(e -> showQuantityDialog());

        MenuItem allItem = new MenuItem("Set to All (*)");
        allItem.setStyle("-fx-text-fill: #FF981F;");
        allItem.setOnAction(e -> handleModeChange(QuantityMode.UNLIMITED));

        MenuItem exactItem = new MenuItem("Set to Exact");
        exactItem.setStyle("-fx-text-fill: #FF981F;");
        exactItem.setOnAction(e -> handleModeChange(QuantityMode.EXACT));

        MenuItem minimumItem = new MenuItem("Set to Minimum (>)");
        minimumItem.setStyle("-fx-text-fill: #FF981F;");
        minimumItem.setOnAction(e -> handleModeChange(QuantityMode.MINIMUM));

        MenuItem clearItem = new MenuItem("Clear Slot");
        clearItem.setStyle("-fx-text-fill: #C0B8A8;");
        clearItem.setOnAction(e -> {
            clearItem();
            if (onModeChange != null) {
                onModeChange.accept(this, null);  // null signals clear
            }
        });

        menu.getItems().addAll(setQuantityItem, allItem, exactItem, minimumItem, clearItem);

        return menu;
    }

    /**
     * Shows a dialog to input a custom quantity.
     */
    private void showQuantityDialog() {
        if (currentItem == null) {
            return;
        }

        // get item name, falling back to ItemManager lookup if null
        String itemName = currentItem.getName();
        if (itemName == null || itemName.isEmpty()) {
            itemName = core.getItemManager().getItemName(currentItem.getItemId());
        }
        if (itemName == null) {
            itemName = "Item #" + currentItem.getItemId();
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(currentItem.getQuantity()));
        dialog.setTitle("Set Quantity");
        dialog.setHeaderText("Enter quantity for " + itemName);
        dialog.setContentText("Quantity:");

        // style the dialog
        dialog.initStyle(StageStyle.UTILITY);

        final String finalItemName = itemName;
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                int newQuantity = Integer.parseInt(input.trim());
                if (newQuantity > 0) {
                    handleQuantityChange(newQuantity, finalItemName);
                }
            } catch (NumberFormatException ex) {
                // ignore invalid input
            }
        });
    }

    /**
     * Handles a quantity change from the dialog.
     *
     * @param newQuantity the new quantity
     * @param itemName the resolved item name
     */
    private void handleQuantityChange(int newQuantity, String itemName) {
        if (currentItem == null) {
            return;
        }

        // setting a specific quantity implies EXACT mode
        LoadoutItem updatedItem = LoadoutItem.builder()
            .itemId(currentItem.getItemId())
            .name(itemName)
            .quantity(newQuantity)
            .mode(QuantityMode.EXACT)
            .fuzzy(currentItem.isFuzzy())
            .build();

        // update display
        setItem(updatedItem);

        // notify callback with the new mode
        if (onModeChange != null) {
            onModeChange.accept(this, QuantityMode.EXACT);
        }
    }

    /**
     * Handles a mode change from the context menu.
     *
     * @param newMode the new quantity mode
     */
    private void handleModeChange(QuantityMode newMode) {
        if (currentItem == null) {
            return;
        }

        // create new item with updated mode
        LoadoutItem updatedItem = LoadoutItem.builder()
            .itemId(currentItem.getItemId())
            .name(currentItem.getName())
            .quantity(currentItem.getQuantity())
            .mode(newMode)
            .fuzzy(currentItem.isFuzzy())
            .build();

        // update display
        setItem(updatedItem);

        // notify callback
        if (onModeChange != null) {
            onModeChange.accept(this, newMode);
        }
    }

    /**
     * Gets the ScriptCore associated with this slot.
     *
     * @return the ScriptCore
     */
    public ScriptCore getCore() {
        return core;
    }
}
