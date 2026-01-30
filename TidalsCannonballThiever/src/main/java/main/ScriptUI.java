package main;

import com.osmb.api.script.Script;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

public class ScriptUI {
    private final Preferences prefs = Preferences.userRoot().node("tidals_cannonball_thiever");
    private static final String PREF_TWO_STALL_MODE = "two_stall_mode";
    private static final String PREF_DEBUG_ENABLED = "debug_enabled";

    // colors matching the paint overlay
    private static final String BG_COLOR = "#163134";
    private static final String BORDER_COLOR = "#284b50";
    private static final String GOLD = "#ffd700";
    private static final String TEXT_LIGHT = "#eeede9";
    private static final String TEXT_MUTED = "#aab9b9";

    private final Script script;
    private CheckBox twoStallModeCheckBox;
    private CheckBox debugCheckBox;

    public ScriptUI(Script script) {
        this.script = script;
    }

    public Scene buildScene(Script script) {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // === Main Tab ===
        VBox mainBox = new VBox(16);
        mainBox.setPadding(new Insets(20, 24, 20, 24));
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // logo
        ImageView logoView = loadLogo();
        if (logoView != null) {
            mainBox.getChildren().add(logoView);
        }

        // version label
        Label versionLabel = new Label("v" + TidalsCannonballThiever.scriptVersion);
        versionLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        mainBox.getChildren().add(versionLabel);

        // spacer
        Region spacer = new Region();
        spacer.setPrefHeight(8);
        mainBox.getChildren().add(spacer);

        // checkbox with custom styling
        twoStallModeCheckBox = new CheckBox("Two Stall Mode");
        twoStallModeCheckBox.setSelected(prefs.getBoolean(PREF_TWO_STALL_MODE, true));
        twoStallModeCheckBox.setStyle(
            "-fx-text-fill: " + TEXT_LIGHT + "; " +
            "-fx-font-size: 14px; " +
            "-fx-cursor: hand;"
        );

        // description that changes based on mode
        Label modeDesc = new Label();
        modeDesc.setWrapText(true);
        modeDesc.setMaxWidth(220);
        modeDesc.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        updateModeDescription(modeDesc);

        // warning for two-stall mode
        Label warningLabel = new Label("Start with empty inventory");
        warningLabel.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 10px;");
        warningLabel.setVisible(twoStallModeCheckBox.isSelected());
        warningLabel.setManaged(twoStallModeCheckBox.isSelected());

        twoStallModeCheckBox.setOnAction(e -> {
            updateModeDescription(modeDesc);
            warningLabel.setVisible(twoStallModeCheckBox.isSelected());
            warningLabel.setManaged(twoStallModeCheckBox.isSelected());
        });

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
        optionBox.getChildren().addAll(twoStallModeCheckBox, modeDesc, warningLabel);

        mainBox.getChildren().add(optionBox);

        Tab mainTab = new Tab("Main", mainBox);
        mainTab.setClosable(false);

        // === Debug Tab ===
        VBox debugBox = new VBox(12);
        debugBox.setPadding(new Insets(20, 24, 20, 24));
        debugBox.setAlignment(Pos.TOP_CENTER);
        debugBox.setStyle("-fx-background-color: " + BG_COLOR + ";");

        VBox debugSection = createSection("Debug");
        debugCheckBox = createCheckbox("Enable verbose logging", prefs.getBoolean(PREF_DEBUG_ENABLED, false));
        Label debugDesc = createDesc("Logs detailed task activate/execute info for troubleshooting.");
        debugSection.getChildren().addAll(debugCheckBox, debugDesc);
        debugBox.getChildren().add(debugSection);

        Tab debugTab = new Tab("Debug", debugBox);
        debugTab.setClosable(false);

        tabPane.getTabs().addAll(mainTab, debugTab);

        // start button
        Button startButton = new Button("Start");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setPrefHeight(36);
        startButton.setStyle(
            "-fx-background-color: " + GOLD + "; " +
            "-fx-text-fill: " + BG_COLOR + "; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand;"
        );

        startButton.setOnMouseEntered(e ->
            startButton.setStyle(
                "-fx-background-color: derive(" + GOLD + ", 15%); " +
                "-fx-text-fill: " + BG_COLOR + "; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;"
            )
        );

        startButton.setOnMouseExited(e ->
            startButton.setStyle(
                "-fx-background-color: " + GOLD + "; " +
                "-fx-text-fill: " + BG_COLOR + "; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;"
            )
        );

        startButton.setOnAction(e -> {
            saveSettings();
            ((Stage) startButton.getScene().getWindow()).close();
        });

        VBox layout = new VBox(tabPane, startButton);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Scene scene = new Scene(layout, 340, 380);
        scene.setFill(Color.web(BG_COLOR));

        return scene;
    }

    private ImageView loadLogo() {
        try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
            if (in == null) return null;
            Image img = new Image(in);
            ImageView view = new ImageView(img);
            view.setPreserveRatio(true);
            view.setFitWidth(180);
            return view;
        } catch (IOException e) {
            return null;
        }
    }

    private void updateModeDescription(Label label) {
        if (twoStallModeCheckBox.isSelected()) {
            label.setText("Alternate between stalls following guard patrol for max XP/hr.");
        } else {
            label.setText("Thieve cannonballs from one stall, retreating when guard approaches.");
        }
    }

    private VBox createSection(String title) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10));
        box.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -15%); " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );

        Label header = new Label(title);
        header.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        box.getChildren().add(header);

        return box;
    }

    private CheckBox createCheckbox(String text, boolean selected) {
        CheckBox cb = new CheckBox(text);
        cb.setSelected(selected);
        cb.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 13px; -fx-cursor: hand;");
        return cb;
    }

    private Label createDesc(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(280);
        label.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        return label;
    }

    private void saveSettings() {
        prefs.putBoolean(PREF_TWO_STALL_MODE, isTwoStallMode());
        prefs.putBoolean(PREF_DEBUG_ENABLED, debugCheckBox.isSelected());

        TidalsCannonballThiever.verboseLogging = debugCheckBox.isSelected();

        script.log("SETTINGS", "Mode saved: " + (isTwoStallMode() ? "Two Stall" : "Single Stall") +
                   ", debug: " + debugCheckBox.isSelected());
    }

    public boolean isTwoStallMode() {
        return twoStallModeCheckBox != null && twoStallModeCheckBox.isSelected();
    }
}
