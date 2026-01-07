package main;

import com.osmb.api.ScriptCore;
import com.osmb.api.script.Script;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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

    public Scene buildScene(ScriptCore core) {
        // === Main Box (Ocean Theme) ===
        VBox mainBox = new VBox(16);
        mainBox.setStyle("-fx-background: linear-gradient(to bottom, #18547A, #0F3460); -fx-padding: 30; -fx-alignment: center");

        // Title
        Label titleLabel = new Label("Cannonball Thieving Mode");
        titleLabel.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Description for single mode
        Label singleDesc = new Label("Single Stall: Thieve cannonballs, retreat to safety when guard approaches");
        singleDesc.setStyle("-fx-text-fill: #98FB98; -fx-font-size: 11px; -fx-wrap-text: true;");
        singleDesc.setWrapText(true);
        singleDesc.setMaxWidth(250);

        // Two stall mode checkbox
        twoStallModeCheckBox = new CheckBox("Two Stall Mode");
        twoStallModeCheckBox.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 14px; -fx-padding: 15 0 5 0;");
        twoStallModeCheckBox.setSelected(prefs.getBoolean(PREF_TWO_STALL_MODE, false));

        // Description for two stall mode
        Label twoStallDesc = new Label("Two Stall: Alternate between Cannonball and Ore stalls following guard patrol for maximum XP");
        twoStallDesc.setStyle("-fx-text-fill: #98FB98; -fx-font-size: 11px; -fx-wrap-text: true;");
        twoStallDesc.setWrapText(true);
        twoStallDesc.setMaxWidth(250);

        // Info label
        Label infoLabel = new Label("Note: Two Stall mode requires empty inventory for ore deposits");
        infoLabel.setStyle("-fx-text-fill: #FF7F50; -fx-font-size: 10px; -fx-padding: 10 0 0 0;");
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(250);

        // Dynamically show/hide based on checkbox
        infoLabel.setVisible(twoStallModeCheckBox.isSelected());
        twoStallModeCheckBox.setOnAction(e -> {
            infoLabel.setVisible(twoStallModeCheckBox.isSelected());
        });

        mainBox.getChildren().addAll(titleLabel, singleDesc, twoStallModeCheckBox, twoStallDesc, infoLabel);

        // Confirm Button (Ocean themed)
        Button confirmButton = new Button("Start Thieving");
        confirmButton.setStyle("-fx-background-color: #FF7F50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 5;");
        confirmButton.setOnMouseEntered(e -> confirmButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 5;"));
        confirmButton.setOnMouseExited(e -> confirmButton.setStyle("-fx-background-color: #FF7F50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 5;"));
        confirmButton.setOnAction(event -> saveSettings());

        VBox layout = new VBox(mainBox, confirmButton);
        layout.setSpacing(15);
        layout.setStyle("-fx-background: linear-gradient(to bottom, #0F3460, #0A1F3D); -fx-padding: 15; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 280);
        scene.getStylesheets().add("style.css");
        return scene;
    }

    private void saveSettings() {
        prefs.putBoolean(PREF_TWO_STALL_MODE, isTwoStallMode());
        script.log("SETTINGS", "Mode saved: " + (isTwoStallMode() ? "Two Stall" : "Single Stall"));
        ((Stage) twoStallModeCheckBox.getScene().getWindow()).close();
    }

    // Getters
    public boolean isTwoStallMode() {
        return twoStallModeCheckBox != null && twoStallModeCheckBox.isSelected();
    }
}
