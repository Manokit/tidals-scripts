package main;

import com.osmb.api.script.Script;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utilities.loadout.LoadoutItem;
import utilities.loadout.LoadoutManager;

/**
 * Setup UI for TidalsLoadoutTester.
 * Provides loadout editor access and test configuration.
 */
public class ScriptUI {

    // ocean theme colors matching LoadoutEditor
    private static final String BG_COLOR = "#163134";
    private static final String BORDER_COLOR = "#284b50";
    private static final String GOLD = "#ffd700";
    private static final String TEXT_LIGHT = "#eeede9";
    private static final String TEXT_MUTED = "#aab9b9";

    private final TidalsLoadoutTester script;
    private final LoadoutManager loadouts;

    private Label statusLabel;
    private CheckBox editorOnlyCheckbox;

    public ScriptUI(TidalsLoadoutTester script, LoadoutManager loadouts) {
        this.script = script;
        this.loadouts = loadouts;
    }

    public Scene buildScene(Script script) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20, 24, 20, 24));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // title
        Label titleLabel = new Label("Loadout Tester");
        titleLabel.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        root.getChildren().add(titleLabel);

        // version label
        Label versionLabel = new Label("v1.0 - Full Workflow Validation");
        versionLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        root.getChildren().add(versionLabel);

        // spacer
        Region spacer = new Region();
        spacer.setPrefHeight(8);
        root.getChildren().add(spacer);

        // edit loadout button
        Button editButton = new Button("Edit Loadout");
        editButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setPrefHeight(40);
        styleButton(editButton, "#FF6B6B", "#FF8E8E");
        editButton.setOnAction(e -> openLoadoutEditor());
        root.getChildren().add(editButton);

        // status label showing loadout summary
        statusLabel = new Label(getLoadoutSummary());
        statusLabel.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 12px;");
        root.getChildren().add(statusLabel);

        // spacer
        Region spacer2 = new Region();
        spacer2.setPrefHeight(8);
        root.getChildren().add(spacer2);

        // options box
        VBox optionBox = new VBox(8);
        optionBox.setAlignment(Pos.CENTER_LEFT);
        optionBox.setPadding(new Insets(12));
        optionBox.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -15%); " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );

        // editor-only checkbox
        editorOnlyCheckbox = new CheckBox("Editor Test Only");
        editorOnlyCheckbox.setSelected(false);
        editorOnlyCheckbox.setStyle(
            "-fx-text-fill: " + TEXT_LIGHT + "; " +
            "-fx-font-size: 12px; " +
            "-fx-cursor: hand;"
        );

        // description
        Label descLabel = new Label("When checked, only test editor UI.\nUncheck to run full workflow test.");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(220);
        descLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 10px;");

        optionBox.getChildren().addAll(editorOnlyCheckbox, descLabel);
        root.getChildren().add(optionBox);

        // spacer
        Region spacer3 = new Region();
        spacer3.setPrefHeight(8);
        root.getChildren().add(spacer3);

        // start button
        Button startButton = new Button("Start Test");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setPrefHeight(36);
        styleGoldButton(startButton);

        startButton.setOnAction(e -> {
            boolean editorOnly = editorOnlyCheckbox.isSelected();

            // validate loadout before starting full workflow
            if (!editorOnly) {
                int equipCount = countEquipmentItems();
                int invCount = countInventoryItems();

                if (equipCount < 1 || invCount < 1) {
                    statusLabel.setText("Need at least 1 equipment + 1 inventory!");
                    statusLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 12px;");
                    return;
                }
            }

            // close window and start
            ((Stage) startButton.getScene().getWindow()).close();
            this.script.onStartTest(editorOnly);
        });

        root.getChildren().add(startButton);

        Scene scene = new Scene(root);
        scene.setFill(Color.web(BG_COLOR));
        scene.getRoot().autosize();

        return scene;
    }

    /**
     * Opens the LoadoutEditor popup.
     */
    private void openLoadoutEditor() {
        boolean saved = loadouts.showEditor();
        if (saved) {
            script.log(getClass(), "loadout saved from editor");
        } else {
            script.log(getClass(), "loadout edit cancelled");
        }
        // update status label
        updateStatusLabel();
    }

    /**
     * Updates the status label with current loadout summary.
     */
    private void updateStatusLabel() {
        statusLabel.setText(getLoadoutSummary());
        statusLabel.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 12px;");
    }

    /**
     * Gets a summary string for the loadout.
     */
    private String getLoadoutSummary() {
        int equipCount = countEquipmentItems();
        int invCount = countInventoryItems();
        return "Equipment: " + equipCount + " items, Inventory: " + invCount + " items";
    }

    /**
     * Counts equipment items in the loadout.
     */
    private int countEquipmentItems() {
        LoadoutItem[] equipment = loadouts.getLoadout().getEquipment();
        int count = 0;
        for (LoadoutItem item : equipment) {
            if (item != null) count++;
        }
        return count;
    }

    /**
     * Counts inventory items in the loadout.
     */
    private int countInventoryItems() {
        LoadoutItem[] inventory = loadouts.getLoadout().getInventory();
        int count = 0;
        for (LoadoutItem item : inventory) {
            if (item != null) count++;
        }
        return count;
    }

    /**
     * Styles a button with the given colors.
     */
    private void styleButton(Button button, String bgColor, String hoverColor) {
        String baseStyle =
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand;";

        String hoverStyle =
            "-fx-background-color: " + hoverColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand;";

        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
    }

    /**
     * Styles the start button with gold color.
     */
    private void styleGoldButton(Button button) {
        String baseStyle =
            "-fx-background-color: " + GOLD + "; " +
            "-fx-text-fill: " + BG_COLOR + "; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand;";

        String hoverStyle =
            "-fx-background-color: derive(" + GOLD + ", 15%); " +
            "-fx-text-fill: " + BG_COLOR + "; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand;";

        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
    }
}
