package utilities.loadout.ui;

import com.osmb.api.ScriptCore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utilities.loadout.Loadout;
import utilities.loadout.LoadoutExporter;
import utilities.loadout.LoadoutImporter;
import utilities.loadout.LoadoutItem;

import java.util.Optional;

/**
 * Main LoadoutEditor popup window for visually configuring loadouts.
 *
 * <p>Layout:
 * <pre>
 * +------------------------------------------+
 * | Loadout Editor               [X] Close   |
 * +------------------------------------------+
 * |  [Equipment Grid]  |  [Inventory Grid]   |
 * |                    |                     |
 * |                    |                     |
 * +------------------------------------------+
 * | [Rune Pouch] [Bolt Pouch] [Quiver]       |
 * +------------------------------------------+
 * | [Save] [Cancel]                          |
 * +------------------------------------------+
 * </pre>
 *
 * <p>Usage:
 * {@code LoadoutEditor.show(core, loadout)}
 */
public class LoadoutEditor extends BorderPane {

    // osrs equipment tab theme colors
    private static final String BG_COLOR = "#3E3529";
    private static final String BG_STYLE = "-fx-background-color: " + BG_COLOR + ";";
    private static final String TITLE_COLOR = "#FF981F";  // osrs orange
    private static final String BUTTON_STYLE =
        "-fx-background-color: #5C5142;" +
        "-fx-text-fill: #FF981F;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 8 20 8 20;" +
        "-fx-cursor: hand;" +
        "-fx-border-color: #736A56;" +
        "-fx-border-width: 1;" +
        "-fx-background-radius: 2;" +
        "-fx-border-radius: 2;";
    private static final String BUTTON_HOVER_STYLE =
        "-fx-background-color: #6B6352;" +
        "-fx-text-fill: #FFB347;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 8 20 8 20;" +
        "-fx-cursor: hand;" +
        "-fx-border-color: #8A8070;" +
        "-fx-border-width: 1;" +
        "-fx-background-radius: 2;" +
        "-fx-border-radius: 2;";
    private static final String CANCEL_BUTTON_STYLE =
        "-fx-background-color: #4A4235;" +
        "-fx-text-fill: #C0B8A8;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 8 20 8 20;" +
        "-fx-cursor: hand;" +
        "-fx-border-color: #5C5142;" +
        "-fx-border-width: 1;" +
        "-fx-background-radius: 2;" +
        "-fx-border-radius: 2;";
    private static final String CANCEL_BUTTON_HOVER_STYLE =
        "-fx-background-color: #5A5245;" +
        "-fx-text-fill: #D0C8B8;" +
        "-fx-font-weight: bold;" +
        "-fx-padding: 8 20 8 20;" +
        "-fx-cursor: hand;" +
        "-fx-border-color: #6C6352;" +
        "-fx-border-width: 1;" +
        "-fx-background-radius: 2;" +
        "-fx-border-radius: 2;";
    private static final String TITLE_STYLE =
        "-fx-text-fill: " + TITLE_COLOR + ";" +
        "-fx-font-size: 18px;" +
        "-fx-font-weight: bold;";

    private final ScriptCore core;
    private final Loadout originalLoadout;
    private final Loadout workingCopy;

    private final EquipmentGrid equipmentGrid;
    private final InventoryGrid inventoryGrid;
    private final PouchGrid runePouchGrid;
    private final PouchGrid boltPouchGrid;
    private final PouchGrid quiverGrid;

    private Stage stage;
    private boolean saved;

    /**
     * Creates a new LoadoutEditor.
     *
     * @param core the ScriptCore for item lookups
     * @param loadout the Loadout to edit
     */
    public LoadoutEditor(ScriptCore core, Loadout loadout) {
        this.core = core;
        this.originalLoadout = loadout;
        this.workingCopy = createWorkingCopy(loadout);
        this.saved = false;

        // main styling
        setStyle(BG_STYLE);
        setPadding(new Insets(20));

        // create components
        equipmentGrid = new EquipmentGrid(core);
        inventoryGrid = new InventoryGrid(core);
        runePouchGrid = new PouchGrid("Rune Pouch", Loadout.RUNE_POUCH_SIZE, core);
        boltPouchGrid = new PouchGrid("Bolt Pouch", Loadout.BOLT_POUCH_SIZE, core);
        quiverGrid = new PouchGrid("Quiver", Loadout.QUIVER_SIZE, core);

        // setup layout
        setupHeader();
        setupCenter();
        setupBottom();

        // populate from working copy
        populateFromLoadout();
    }

    /**
     * Creates a working copy of the loadout for editing.
     *
     * @param original the original loadout
     * @return a mutable copy
     */
    private Loadout createWorkingCopy(Loadout original) {
        Loadout copy = new Loadout(original.getName());

        // copy equipment
        LoadoutItem[] equipment = original.getEquipment();
        for (int i = 0; i < Loadout.EQUIPMENT_SIZE; i++) {
            copy.setEquipment(i, equipment[i]);
        }

        // copy inventory
        LoadoutItem[] inventory = original.getInventory();
        for (int i = 0; i < Loadout.INVENTORY_SIZE; i++) {
            copy.setInventorySlot(i, inventory[i]);
        }

        // copy pouches
        if (original.hasRunePouch()) {
            copy.setRunePouch(original.getRunePouch());
        }
        if (original.hasBoltPouch()) {
            copy.setBoltPouch(original.getBoltPouch());
        }
        if (original.hasQuiver()) {
            copy.setQuiver(original.getQuiver());
        }

        return copy;
    }

    /**
     * Sets up the header with title and import/export buttons.
     */
    private void setupHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));

        Label titleLabel = new Label("Loadout Editor");
        titleLabel.setStyle(TITLE_STYLE);

        // spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // import button
        Button importButton = new Button("Import");
        importButton.setStyle(BUTTON_STYLE);
        importButton.setOnMouseEntered(e -> importButton.setStyle(BUTTON_HOVER_STYLE));
        importButton.setOnMouseExited(e -> importButton.setStyle(BUTTON_STYLE));
        importButton.setOnAction(e -> {
            System.out.println("[LoadoutEditor] Import button clicked!");
            handleImport();
        });

        // export button
        Button exportButton = new Button("Export");
        exportButton.setStyle(BUTTON_STYLE);
        exportButton.setOnMouseEntered(e -> exportButton.setStyle(BUTTON_HOVER_STYLE));
        exportButton.setOnMouseExited(e -> exportButton.setStyle(BUTTON_STYLE));
        exportButton.setOnAction(e -> {
            System.out.println("[LoadoutEditor] Export button clicked!");
            handleExport();
        });

        // button container with spacing
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(importButton, exportButton);

        header.getChildren().addAll(titleLabel, spacer, buttonBox);

        setTop(header);
    }

    /**
     * Sets up the center with equipment and inventory grids.
     */
    private void setupCenter() {
        HBox center = new HBox(30);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(15));

        // equipment grid wrapped in VBox for label
        VBox equipmentSection = new VBox(8);
        equipmentSection.setAlignment(Pos.TOP_CENTER);
        Label equipLabel = new Label("Equipment");
        equipLabel.setStyle("-fx-text-fill: " + TITLE_COLOR + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        equipmentSection.getChildren().addAll(equipLabel, equipmentGrid);

        // inventory grid wrapped in VBox for label
        VBox inventorySection = new VBox(8);
        inventorySection.setAlignment(Pos.TOP_CENTER);
        Label invLabel = new Label("Inventory");
        invLabel.setStyle("-fx-text-fill: " + TITLE_COLOR + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        inventorySection.getChildren().addAll(invLabel, inventoryGrid);

        center.getChildren().addAll(equipmentSection, inventorySection);

        setCenter(center);
    }

    /**
     * Sets up the bottom with pouches and buttons.
     */
    private void setupBottom() {
        VBox bottom = new VBox(15);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(15, 0, 0, 0));

        // pouches row
        HBox pouchesRow = new HBox(20);
        pouchesRow.setAlignment(Pos.CENTER);
        pouchesRow.getChildren().addAll(runePouchGrid, boltPouchGrid, quiverGrid);

        // buttons row
        HBox buttonsRow = new HBox(15);
        buttonsRow.setAlignment(Pos.CENTER);
        buttonsRow.setPadding(new Insets(15, 0, 0, 0));

        Button saveButton = new Button("Save");
        saveButton.setStyle(BUTTON_STYLE);
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(BUTTON_HOVER_STYLE));
        saveButton.setOnMouseExited(e -> saveButton.setStyle(BUTTON_STYLE));
        saveButton.setOnAction(e -> handleSave());

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle(CANCEL_BUTTON_STYLE);
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(CANCEL_BUTTON_HOVER_STYLE));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(CANCEL_BUTTON_STYLE));
        cancelButton.setOnAction(e -> handleCancel());

        buttonsRow.getChildren().addAll(saveButton, cancelButton);

        bottom.getChildren().addAll(pouchesRow, buttonsRow);

        setBottom(bottom);
    }

    /**
     * Populates the UI from the working copy loadout.
     */
    private void populateFromLoadout() {
        // populate grids
        equipmentGrid.setLoadout(workingCopy, core);
        inventoryGrid.setLoadout(workingCopy, core);

        // populate pouches
        if (workingCopy.hasRunePouch()) {
            runePouchGrid.setEnabled(true);
            runePouchGrid.setItems(workingCopy.getRunePouch());
        }
        if (workingCopy.hasBoltPouch()) {
            boltPouchGrid.setEnabled(true);
            boltPouchGrid.setItems(workingCopy.getBoltPouch());
        }
        if (workingCopy.hasQuiver()) {
            quiverGrid.setEnabled(true);
            quiverGrid.setItems(workingCopy.getQuiver());
        }
    }

    /**
     * Handles the save action - copies working data back to original and closes.
     */
    private void handleSave() {
        // copy equipment back
        for (int i = 0; i < Loadout.EQUIPMENT_SIZE; i++) {
            originalLoadout.setEquipment(i, workingCopy.getEquipmentSlot(i));
        }

        // copy inventory back
        for (int i = 0; i < Loadout.INVENTORY_SIZE; i++) {
            originalLoadout.setInventorySlot(i, workingCopy.getInventorySlot(i));
        }

        // copy pouches back
        if (runePouchGrid.isEnabled()) {
            originalLoadout.setRunePouch(runePouchGrid.getItems());
        } else {
            originalLoadout.setRunePouch(null);
        }

        if (boltPouchGrid.isEnabled()) {
            originalLoadout.setBoltPouch(boltPouchGrid.getItems());
        } else {
            originalLoadout.setBoltPouch(null);
        }

        if (quiverGrid.isEnabled()) {
            originalLoadout.setQuiver(quiverGrid.getItems());
        } else {
            originalLoadout.setQuiver(null);
        }

        saved = true;
        closeWindow();
    }

    /**
     * Handles the cancel action - closes without saving.
     */
    private void handleCancel() {
        saved = false;
        closeWindow();
    }

    /**
     * Handles the import action - opens dialog for pasting JSON from clipboard.
     */
    private void handleImport() {
        System.out.println("[LoadoutEditor] handleImport() called");

        try {
            // create custom dialog with text area for pasting
            System.out.println("[LoadoutEditor] Creating import dialog...");
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Import Loadout");
            dialog.setHeaderText("Paste RuneLite Inventory Setups JSON below:");
            dialog.initOwner(stage);

            // style the dialog pane
            dialog.getDialogPane().setStyle(BG_STYLE);
            dialog.getDialogPane().setPrefWidth(500);
            dialog.getDialogPane().setPrefHeight(350);

            // create text area for paste
            TextArea textArea = new TextArea();
            textArea.setPromptText("Paste JSON here...");
            textArea.setWrapText(true);
            textArea.setPrefRowCount(12);
            textArea.setStyle(
                    "-fx-control-inner-background: #2A2520;" +
                    "-fx-text-fill: #C0B8A8;" +
                    "-fx-font-family: monospace;" +
                    "-fx-border-color: #5C5142;" +
                    "-fx-border-width: 1;"
            );

            // pre-fill from clipboard if it looks like JSON
            System.out.println("[LoadoutEditor] Checking clipboard for JSON...");
            try {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                System.out.println("[LoadoutEditor] Got clipboard, hasString=" + clipboard.hasString());
                if (clipboard.hasString()) {
                    String clipContent = clipboard.getString();
                    System.out.println("[LoadoutEditor] Clipboard content: " +
                            (clipContent != null ? clipContent.substring(0, Math.min(100, clipContent.length())) + "..." : "null"));
                    if (clipContent != null && clipContent.trim().startsWith("{")) {
                        textArea.setText(clipContent);
                        System.out.println("[LoadoutEditor] Pre-filled textarea from clipboard");
                    }
                }
            } catch (IllegalStateException clipEx) {
                System.out.println("[LoadoutEditor] Error reading clipboard: " + clipEx.getMessage());
                clipEx.printStackTrace();
            }

            VBox content = new VBox(10);
            content.setPadding(new Insets(10));
            Label label = new Label("Paste RuneLite Inventory Setups JSON:");
            label.setStyle("-fx-text-fill: " + TITLE_COLOR + ";");
            content.getChildren().addAll(label, textArea);

            dialog.getDialogPane().setContent(content);

            // add buttons
            ButtonType importButtonType = new ButtonType("Import");
            dialog.getDialogPane().getButtonTypes().addAll(importButtonType, ButtonType.CANCEL);

            // convert result
            dialog.setResultConverter(buttonType -> {
                System.out.println("[LoadoutEditor] Dialog result converter called with: " + buttonType);
                if (buttonType == importButtonType) {
                    return textArea.getText();
                }
                return null;
            });

            System.out.println("[LoadoutEditor] Showing dialog...");
            Optional<String> result = dialog.showAndWait();
            System.out.println("[LoadoutEditor] Dialog closed, result present=" + result.isPresent());

            if (result.isEmpty() || result.get() == null || result.get().trim().isEmpty()) {
                System.out.println("[LoadoutEditor] User cancelled or empty input");
                return;  // user cancelled or empty input
            }

            String json = result.get();
            System.out.println("[LoadoutEditor] Got JSON input, length=" + json.length());
            System.out.println("[LoadoutEditor] JSON preview: " + json.substring(0, Math.min(200, json.length())));

            System.out.println("[LoadoutEditor] Parsing JSON with LoadoutImporter...");
            Loadout imported = LoadoutImporter.fromJson(json);
            System.out.println("[LoadoutEditor] Parsed loadout: " + imported.getName());

            // copy imported data into working copy
            System.out.println("[LoadoutEditor] Copying imported data to working copy...");
            copyImportedLoadout(imported);

            // refresh ui from working copy
            System.out.println("[LoadoutEditor] Refreshing UI from working copy...");
            populateFromLoadout();
            System.out.println("[LoadoutEditor] Import complete!");

            // show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Successful");
            alert.setHeaderText(null);
            alert.setContentText("Imported loadout: " + imported.getName());
            alert.showAndWait();

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[LoadoutEditor] Import error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to import loadout: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Copies data from an imported loadout into the working copy.
     *
     * @param imported the imported loadout
     */
    private void copyImportedLoadout(Loadout imported) {
        // copy equipment
        LoadoutItem[] equipment = imported.getEquipment();
        for (int i = 0; i < Loadout.EQUIPMENT_SIZE; i++) {
            workingCopy.setEquipment(i, equipment[i]);
        }

        // copy inventory
        LoadoutItem[] inventory = imported.getInventory();
        for (int i = 0; i < Loadout.INVENTORY_SIZE; i++) {
            workingCopy.setInventorySlot(i, inventory[i]);
        }

        // copy pouches
        if (imported.hasRunePouch()) {
            workingCopy.setRunePouch(imported.getRunePouch());
        } else {
            workingCopy.setRunePouch(null);
        }

        if (imported.hasBoltPouch()) {
            workingCopy.setBoltPouch(imported.getBoltPouch());
        } else {
            workingCopy.setBoltPouch(null);
        }

        if (imported.hasQuiver()) {
            workingCopy.setQuiver(imported.getQuiver());
        } else {
            workingCopy.setQuiver(null);
        }
    }

    /**
     * Handles the export action - copies current loadout JSON to clipboard.
     */
    private void handleExport() {
        System.out.println("[LoadoutEditor] handleExport() called");

        // sync working copy from current ui state before export
        System.out.println("[LoadoutEditor] Syncing working copy from UI...");
        syncWorkingCopyFromUI();

        try {
            // use compact JSON for clipboard (easy to share on Discord)
            System.out.println("[LoadoutEditor] Exporting loadout to JSON...");
            System.out.println("[LoadoutEditor] Working copy name: " + workingCopy.getName());
            System.out.println("[LoadoutEditor] Equipment slots filled: " + countNonNull(workingCopy.getEquipment()));
            System.out.println("[LoadoutEditor] Inventory slots filled: " + countNonNull(workingCopy.getInventory()));

            String json = LoadoutExporter.toJson(workingCopy);
            System.out.println("[LoadoutEditor] Generated JSON length: " + json.length());
            System.out.println("[LoadoutEditor] JSON preview: " + json.substring(0, Math.min(200, json.length())));

            // copy to system clipboard
            System.out.println("[LoadoutEditor] Copying to clipboard...");
            try {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(json);
                boolean setSuccess = clipboard.setContent(content);
                System.out.println("[LoadoutEditor] Clipboard setContent returned: " + setSuccess);

                // verify it was set
                if (clipboard.hasString()) {
                    String verifyContent = clipboard.getString();
                    System.out.println("[LoadoutEditor] Verified clipboard content length: " +
                            (verifyContent != null ? verifyContent.length() : "null"));
                } else {
                    System.out.println("[LoadoutEditor] WARNING: Clipboard does not have string after set!");
                }
            } catch (IllegalStateException clipEx) {
                System.out.println("[LoadoutEditor] Error setting clipboard: " + clipEx.getMessage());
                clipEx.printStackTrace();
                throw clipEx;
            }

            System.out.println("[LoadoutEditor] Export successful, showing alert...");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Loadout copied to clipboard!\n\nYou can now paste it into Discord, a text file, or share it directly.");
            alert.showAndWait();

        } catch (IllegalStateException e) {
            System.out.println("[LoadoutEditor] Export error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to export loadout: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Counts non-null items in an array.
     */
    private int countNonNull(LoadoutItem[] items) {
        int count = 0;
        for (LoadoutItem item : items) {
            if (item != null) count++;
        }
        return count;
    }

    /**
     * Syncs the working copy from current UI state.
     * This ensures any unsaved grid changes are captured before export.
     */
    private void syncWorkingCopyFromUI() {
        // sync equipment from grid
        for (int i = 0; i < Loadout.EQUIPMENT_SIZE; i++) {
            workingCopy.setEquipment(i, equipmentGrid.getSlot(i).getItem());
        }

        // sync inventory from grid
        for (int i = 0; i < Loadout.INVENTORY_SIZE; i++) {
            workingCopy.setInventorySlot(i, inventoryGrid.getSlot(i).getItem());
        }

        // sync pouches from grids
        if (runePouchGrid.isEnabled()) {
            workingCopy.setRunePouch(runePouchGrid.getItems());
        } else {
            workingCopy.setRunePouch(null);
        }

        if (boltPouchGrid.isEnabled()) {
            workingCopy.setBoltPouch(boltPouchGrid.getItems());
        } else {
            workingCopy.setBoltPouch(null);
        }

        if (quiverGrid.isEnabled()) {
            workingCopy.setQuiver(quiverGrid.getItems());
        } else {
            workingCopy.setQuiver(null);
        }
    }

    /**
     * Closes the window.
     */
    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Returns whether the user saved changes.
     *
     * @return true if save was clicked, false if cancelled
     */
    public boolean wasSaved() {
        return saved;
    }

    /**
     * Shows the LoadoutEditor as a modal popup.
     *
     * @param core the ScriptCore for item lookups
     * @param loadout the Loadout to edit
     * @return true if user saved changes, false if cancelled
     */
    public static boolean show(ScriptCore core, Loadout loadout) {
        LoadoutEditor editor = new LoadoutEditor(core, loadout);

        Stage stage = new Stage();
        editor.stage = stage;

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Loadout Editor");
        stage.setWidth(620);
        stage.setHeight(560);
        stage.setResizable(false);

        Scene scene = new Scene(editor);
        stage.setScene(scene);

        // center on screen
        stage.centerOnScreen();

        // show and wait
        stage.showAndWait();

        return editor.wasSaved();
    }

    /**
     * Gets the equipment grid component.
     *
     * @return the EquipmentGrid
     */
    public EquipmentGrid getEquipmentGrid() {
        return equipmentGrid;
    }

    /**
     * Gets the inventory grid component.
     *
     * @return the InventoryGrid
     */
    public InventoryGrid getInventoryGrid() {
        return inventoryGrid;
    }

    /**
     * Gets the rune pouch grid component.
     *
     * @return the PouchGrid for rune pouch
     */
    public PouchGrid getRunePouchGrid() {
        return runePouchGrid;
    }

    /**
     * Gets the bolt pouch grid component.
     *
     * @return the PouchGrid for bolt pouch
     */
    public PouchGrid getBoltPouchGrid() {
        return boltPouchGrid;
    }

    /**
     * Gets the quiver grid component.
     *
     * @return the PouchGrid for quiver
     */
    public PouchGrid getQuiverGrid() {
        return quiverGrid;
    }
}
