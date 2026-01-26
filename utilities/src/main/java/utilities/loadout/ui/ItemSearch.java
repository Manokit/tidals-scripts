package utilities.loadout.ui;

import com.osmb.api.ScriptCore;
import com.osmb.api.javafx.JavaFXUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import utilities.loadout.LoadoutItem;
import utilities.loadout.QuantityMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * JavaFX popup component for item selection with search and autocomplete.
 *
 * <p>Features:
 * <ul>
 *   <li>TextField for search input</li>
 *   <li>ListView showing matching items (max 10 visible)</li>
 *   <li>Each cell shows sprite + item name</li>
 *   <li>Case-insensitive substring filtering</li>
 *   <li>Debounced search (250ms delay)</li>
 *   <li>Lazy item list population on first show</li>
 *   <li>Ocean theme matching existing ScriptUI</li>
 * </ul>
 */
public class ItemSearch extends VBox {

    // osrs equipment tab theme colors
    private static final String BG_COLOR = "#3E3529";
    private static final String TEXT_COLOR = "#FF981F";
    private static final String ACCENT_COLOR = "#5C5142";
    private static final String BORDER_COLOR = "#5C5142";

    // search configuration
    private static final int DEBOUNCE_MS = 250;
    private static final int MAX_VISIBLE_ITEMS = 10;
    private static final int MAX_ITEM_ID = 30000;  // osrs items go up to ~30k

    // sprite dimensions for list cells
    private static final int SPRITE_WIDTH = 16;
    private static final int SPRITE_HEIGHT = 16;

    private final ScriptCore core;
    private final TextField searchField;
    private final ListView<ItemEntry> resultsList;
    private final List<ItemEntry> allItems;

    private Consumer<LoadoutItem> onItemSelected;
    private Popup popup;
    private Timer debounceTimer;
    private boolean initialized;

    /**
     * Creates a new ItemSearch component.
     *
     * @param core the ScriptCore for item lookups
     */
    public ItemSearch(ScriptCore core) {
        this.core = core;
        this.allItems = new ArrayList<>();
        this.initialized = false;

        // layout configuration
        setSpacing(8);
        setPadding(new Insets(10));
        setStyle(
            "-fx-background-color: " + BG_COLOR + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );

        // search field
        searchField = new TextField();
        searchField.setPromptText("Search items...");
        searchField.setStyle(
            "-fx-background-color: #252116;" +
            "-fx-text-fill: " + TEXT_COLOR + ";" +
            "-fx-prompt-text-fill: #8A8070;" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;"
        );
        searchField.setPrefWidth(220);

        // results list
        resultsList = new ListView<>();
        resultsList.setPrefHeight(MAX_VISIBLE_ITEMS * 24); // ~24px per cell
        resultsList.setPrefWidth(220);
        resultsList.setStyle(
            "-fx-background-color: #252116;" +
            "-fx-control-inner-background: #252116;"
        );

        // custom cell factory for sprite + name display
        resultsList.setCellFactory(lv -> new ItemCell());

        // selection handler
        resultsList.setOnMouseClicked(e -> {
            ItemEntry selected = resultsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleSelection(selected);
            }
        });

        // enter key also selects
        resultsList.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                ItemEntry selected = resultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    handleSelection(selected);
                }
            }
        });

        // debounced search on text change
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            scheduleSearch(newVal);
        });

        getChildren().addAll(searchField, resultsList);
    }

    /**
     * Sets the callback for when an item is selected.
     *
     * @param onItemSelected consumer receiving the selected LoadoutItem
     */
    public void setOnItemSelected(Consumer<LoadoutItem> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    /**
     * Shows the search popup at the specified position.
     *
     * @param owner the owner stage
     * @param x screen x coordinate
     * @param y screen y coordinate
     */
    public void showAsPopup(Stage owner, double x, double y) {
        // lazy initialization of item list
        if (!initialized) {
            initializeItems();
        }

        // create popup if needed
        if (popup == null) {
            popup = new Popup();
            popup.getContent().add(this);
            popup.setAutoHide(true);

            // close on focus loss
            popup.setOnAutoHide(e -> cleanup());
        }

        // reset state
        searchField.clear();
        filterItems("");
        resultsList.getSelectionModel().clearSelection();

        // show popup
        popup.show(owner, x, y);

        // focus search field
        Platform.runLater(() -> searchField.requestFocus());
    }

    /**
     * Hides the popup.
     */
    public void hide() {
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }
        cleanup();
    }

    /**
     * Initializes the item list by iterating through common item IDs.
     * Called lazily on first show to avoid startup cost.
     * Filters out unbankable items (LMS, animation, broken, locked variants).
     */
    private void initializeItems() {
        allItems.clear();

        for (int id = 0; id <= MAX_ITEM_ID; id++) {
            try {
                String name = core.getItemManager().getItemName(id);
                if (name != null && !name.isEmpty() && !name.equals("null")) {
                    if (!isFilteredItem(name)) {
                        allItems.add(new ItemEntry(id, name));
                    }
                }
            } catch (RuntimeException e) {
                // skip invalid IDs
            }
        }

        initialized = true;
    }

    /**
     * Checks if an item should be filtered from the list.
     * Filters unbankable items like LMS variants, animation items, broken/locked variants.
     *
     * @param name the item name to check
     * @return true if the item should be excluded
     */
    private boolean isFilteredItem(String name) {
        String lower = name.toLowerCase();

        // LMS items cannot be banked
        if (lower.contains("(last man standing)")) {
            return true;
        }

        // animation items are internal game items
        if (lower.contains("(animation item)")) {
            return true;
        }

        // broken variants (Fire cape#Broken, etc.)
        if (lower.contains("#broken") || lower.contains("(broken)")) {
            return true;
        }

        // locked variants (Fire cape#Locked, etc.)
        if (lower.contains("#locked") || lower.contains("(locked)")) {
            return true;
        }

        // placeholder items
        if (lower.contains("placeholder")) {
            return true;
        }

        // npc-only items
        if (lower.endsWith("(npc)")) {
            return true;
        }

        return false;
    }

    /**
     * Schedules a debounced search.
     *
     * @param query the search query
     */
    private void scheduleSearch(String query) {
        // cancel any pending search
        if (debounceTimer != null) {
            debounceTimer.cancel();
        }

        debounceTimer = new Timer();
        debounceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> filterItems(query));
            }
        }, DEBOUNCE_MS);
    }

    /**
     * Filters items based on search query.
     *
     * @param query the search query (case-insensitive substring match)
     */
    private void filterItems(String query) {
        resultsList.getItems().clear();

        String lowerQuery = query.toLowerCase().trim();

        int count = 0;
        for (ItemEntry entry : allItems) {
            if (lowerQuery.isEmpty() || entry.nameLower.contains(lowerQuery)) {
                resultsList.getItems().add(entry);
                count++;
                if (count >= MAX_VISIBLE_ITEMS * 5) {
                    // limit results for performance
                    break;
                }
            }
        }
    }

    /**
     * Handles item selection.
     *
     * @param entry the selected item entry
     */
    private void handleSelection(ItemEntry entry) {
        if (onItemSelected != null) {
            LoadoutItem item = LoadoutItem.builder()
                .itemId(entry.itemId)
                .name(entry.name)
                .quantity(1)
                .mode(QuantityMode.EXACT)
                .build();
            onItemSelected.accept(item);
        }
        hide();
    }

    /**
     * Cleans up resources.
     */
    private void cleanup() {
        if (debounceTimer != null) {
            debounceTimer.cancel();
            debounceTimer = null;
        }
    }

    /**
     * Internal entry class for item data.
     */
    private static class ItemEntry {
        final int itemId;
        final String name;
        final String nameLower;

        ItemEntry(int itemId, String name) {
            this.itemId = itemId;
            this.name = name;
            this.nameLower = name.toLowerCase();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Custom list cell showing sprite and item name.
     */
    private class ItemCell extends ListCell<ItemEntry> {
        private final HBox container;
        private final ImageView spriteView;
        private final javafx.scene.control.Label nameLabel;

        ItemCell() {
            container = new HBox(8);
            container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            spriteView = new ImageView();
            spriteView.setFitWidth(SPRITE_WIDTH);
            spriteView.setFitHeight(SPRITE_HEIGHT);
            spriteView.setPreserveRatio(true);

            nameLabel = new javafx.scene.control.Label();
            nameLabel.setStyle("-fx-text-fill: " + TEXT_COLOR + ";");

            container.getChildren().addAll(spriteView, nameLabel);

            // cell styling
            setStyle("-fx-background-color: transparent;");
        }

        @Override
        protected void updateItem(ItemEntry entry, boolean empty) {
            super.updateItem(entry, empty);

            if (empty || entry == null) {
                setGraphic(null);
            } else {
                // load sprite
                ImageView itemImage = JavaFXUtils.getItemImageView(core, entry.itemId);
                if (itemImage != null && itemImage.getImage() != null) {
                    spriteView.setImage(itemImage.getImage());
                } else {
                    spriteView.setImage(null);
                }

                nameLabel.setText(entry.name);
                setGraphic(container);
            }
        }
    }
}
