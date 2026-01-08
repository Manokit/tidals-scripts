package main;

import com.osmb.api.script.Script;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class ScriptUI {
    private final Preferences prefs = Preferences.userRoot().node("tidals_cannonball_thiever");
    private static final String PREF_TWO_STALL_MODE = "two_stall_mode";

    private final Script script;
    private CheckBox twoStallModeCheckBox;

    public ScriptUI(Script script) {
        this.script = script;
    }

    public Scene buildScene(Script script) {
        // === Root container (Ocean Dark Blue) ===
        VBox root = new VBox(15);
        root.setPadding(new Insets(20, 25, 20, 25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #0A1F3D;");

        // === Title ===
        Label titleLabel = new Label("Tidals Cannonball Thiever");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #40E0D0;"); // Turquoise

        Label versionLabel = new Label("v" + TidalsCannonballThiever.scriptVersion);
        versionLabel.setStyle("-fx-text-fill: #6495ED; -fx-font-size: 10px;"); // Ocean accent

        // === Settings Box ===
        VBox settingsBox = new VBox(12);
        settingsBox.setPadding(new Insets(18));
        settingsBox.setStyle(
            "-fx-background-color: #0F3460; " +
            "-fx-border-color: #1E5080; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 0; " +
            "-fx-border-radius: 0;"
        );

        // === Mode Selection Title ===
        Label modeTitle = new Label("Thieving Mode");
        modeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        modeTitle.setStyle("-fx-text-fill: #FF7F50; -fx-padding: 0 0 5 0;"); // Coral/Orange

        // === Single Stall Description ===
        VBox singleStallBox = new VBox(5);
        singleStallBox.setStyle(
            "-fx-background-color: #0A2540; " +
            "-fx-border-color: #1E5080; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 10;"
        );

        Label singleTitle = new Label("Single Stall Mode");
        singleTitle.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label singleDesc = new Label("Thieve cannonballs from one stall. Retreat to safety tile when guard approaches, then return when safe.");
        singleDesc.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 11px;");
        singleDesc.setWrapText(true);
        singleDesc.setMaxWidth(280);

        singleStallBox.getChildren().addAll(singleTitle, singleDesc);

        // === Two Stall Mode Checkbox ===
        twoStallModeCheckBox = new CheckBox("Enable Two Stall Mode");
        twoStallModeCheckBox.setSelected(prefs.getBoolean(PREF_TWO_STALL_MODE, false));
        twoStallModeCheckBox.setStyle(
            "-fx-text-fill: #FFD700; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 0 5 0;"
        );

        // === Two Stall Description ===
        VBox twoStallBox = new VBox(5);
        twoStallBox.setStyle(
            "-fx-background-color: #0A2540; " +
            "-fx-border-color: #1E5080; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 10;"
        );

        Label twoStallTitle = new Label("Two Stall Mode");
        twoStallTitle.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label twoStallDesc = new Label("Alternate between Cannonball and Ore stalls following guard patrol pattern for maximum XP/hr.");
        twoStallDesc.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 11px;");
        twoStallDesc.setWrapText(true);
        twoStallDesc.setMaxWidth(280);

        twoStallBox.getChildren().addAll(twoStallTitle, twoStallDesc);

        // === Warning Note (shown when two-stall enabled) ===
        HBox warningBox = new HBox(8);
        warningBox.setAlignment(Pos.CENTER_LEFT);
        warningBox.setStyle(
            "-fx-background-color: #1A0A00; " +
            "-fx-border-color: #FF7F50; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 8;"
        );

        Label warningIcon = new Label("!");
        warningIcon.setStyle(
            "-fx-text-fill: #FF7F50; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-min-width: 20;"
        );

        Label warningText = new Label("Start 2-stall mode with empty inventory for ore deposits.");
        warningText.setStyle("-fx-text-fill: #FF7F50; -fx-font-size: 10px;");
        warningText.setWrapText(true);
        warningText.setMaxWidth(250);

        warningBox.getChildren().addAll(warningIcon, warningText);

        // Show/hide warning based on checkbox
        warningBox.setVisible(twoStallModeCheckBox.isSelected());
        warningBox.setManaged(twoStallModeCheckBox.isSelected());
        twoStallModeCheckBox.setOnAction(e -> {
            warningBox.setVisible(twoStallModeCheckBox.isSelected());
            warningBox.setManaged(twoStallModeCheckBox.isSelected());
        });

        settingsBox.getChildren().addAll(
            modeTitle,
            singleStallBox,
            twoStallModeCheckBox,
            twoStallBox,
            warningBox
        );

        // === Start Button ===
        Button startButton = new Button("START THIEVING");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setPrefHeight(38);
        startButton.setStyle(
            "-fx-background-color: #40E0D0; " +
            "-fx-text-fill: #0A1F3D; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 0; " +
            "-fx-border-radius: 0; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: #1E5080; " +
            "-fx-border-width: 2;"
        );

        startButton.setOnMouseEntered(e ->
            startButton.setStyle(
                "-fx-background-color: #5AF0E0; " +
                "-fx-text-fill: #0A1F3D; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 0; " +
                "-fx-border-radius: 0; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: #40E0D0; " +
                "-fx-border-width: 2;"
            )
        );

        startButton.setOnMouseExited(e ->
            startButton.setStyle(
                "-fx-background-color: #40E0D0; " +
                "-fx-text-fill: #0A1F3D; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px; " +
                "-fx-background-radius: 0; " +
                "-fx-border-radius: 0; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: #1E5080; " +
                "-fx-border-width: 2;"
            )
        );

        startButton.setOnAction(e -> {
            saveSettings();
            ((Stage) startButton.getScene().getWindow()).close();
        });

        root.getChildren().addAll(
            titleLabel,
            versionLabel,
            settingsBox,
            startButton
        );

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.web("#0A1F3D"));
        scene.getRoot().autosize();

        try {
            String css = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            // CSS not found, use inline styles only
        }

        return scene;
    }

    private void saveSettings() {
        prefs.putBoolean(PREF_TWO_STALL_MODE, isTwoStallMode());
        script.log("SETTINGS", "Mode saved: " + (isTwoStallMode() ? "Two Stall" : "Single Stall"));
    }

    // Getters
    public boolean isTwoStallMode() {
        return twoStallModeCheckBox != null && twoStallModeCheckBox.isSelected();
    }
}
