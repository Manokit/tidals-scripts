package main;

import com.osmb.api.script.Script;
import data.Locations;
import data.Locations.MiningLocation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

public class ScriptUI {
    private final Preferences prefs = Preferences.userRoot().node("tidals_gem_miner");
    private static final String PREF_LOCATION = "selected_location";
    private static final String PREF_CUTTING_ENABLED = "cutting_enabled";
    private static final String PREF_DEBUG_ENABLED = "debug_enabled";

    // colors matching the paint overlay
    private static final String BG_COLOR = "#163134";
    private static final String BORDER_COLOR = "#284b50";
    private static final String GOLD = "#ffd700";
    private static final String TEXT_LIGHT = "#eeede9";
    private static final String TEXT_MUTED = "#aab9b9";

    private final Script script;
    private ComboBox<MiningLocation> locationComboBox;
    private CheckBox cuttingCheckBox;
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
        Label versionLabel = new Label("v" + TidalsGemMiner.scriptVersion);
        versionLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        mainBox.getChildren().add(versionLabel);

        // spacer
        Region spacer = new Region();
        spacer.setPrefHeight(8);
        mainBox.getChildren().add(spacer);

        // location selection option box
        VBox locationBox = createSection("Mining Location");

        locationComboBox = new ComboBox<>();
        locationComboBox.getItems().addAll(Locations.ALL_LOCATIONS);
        locationComboBox.setMaxWidth(Double.MAX_VALUE);
        locationComboBox.setStyle(
                "-fx-background-color: derive(" + BG_COLOR + ", 10%); " +
                "-fx-text-fill: " + TEXT_LIGHT + "; " +
                "-fx-font-size: 13px;"
        );

        locationComboBox.setCellFactory(lv -> new ListCell<MiningLocation>() {
            @Override
            protected void updateItem(MiningLocation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.displayName());
                }
                setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-background-color: derive(" + BG_COLOR + ", 10%);");
            }
        });

        locationComboBox.setButtonCell(new ListCell<MiningLocation>() {
            @Override
            protected void updateItem(MiningLocation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.displayName());
                }
                setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
            }
        });

        locationComboBox.setConverter(new StringConverter<MiningLocation>() {
            @Override
            public String toString(MiningLocation object) {
                return object == null ? "" : object.displayName();
            }

            @Override
            public MiningLocation fromString(String string) {
                return MiningLocation.fromDisplay(string);
            }
        });

        // load saved preference
        String savedLocation = prefs.get(PREF_LOCATION, Locations.UPPER.displayName());
        MiningLocation defaultLocation = MiningLocation.fromDisplay(savedLocation);
        locationComboBox.setValue(defaultLocation);

        Label locationDesc = createDesc("Upper mine has gem rocks on surface. Underground mine is inside cave.");
        locationBox.getChildren().addAll(locationComboBox, locationDesc);
        mainBox.getChildren().add(locationBox);

        // cutting option box
        VBox cuttingBox = createSection("Gem Cutting");

        cuttingCheckBox = createCheckbox("Cut gems (drops crushed)", prefs.getBoolean(PREF_CUTTING_ENABLED, false));

        Label cuttingDesc = createDesc("");
        updateCuttingDescription(cuttingDesc);

        Label chiselWarning = new Label("Requires chisel in inventory");
        chiselWarning.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 10px;");
        chiselWarning.setVisible(cuttingCheckBox.isSelected());
        chiselWarning.setManaged(cuttingCheckBox.isSelected());

        cuttingCheckBox.setOnAction(e -> {
            updateCuttingDescription(cuttingDesc);
            chiselWarning.setVisible(cuttingCheckBox.isSelected());
            chiselWarning.setManaged(cuttingCheckBox.isSelected());
        });

        cuttingBox.getChildren().addAll(cuttingCheckBox, cuttingDesc, chiselWarning);
        mainBox.getChildren().add(cuttingBox);

        Tab mainTab = new Tab("Main", mainBox);
        mainTab.setClosable(false);

        // === Debug Tab ===
        VBox debugRoot = new VBox(12);
        debugRoot.setPadding(new Insets(20, 24, 20, 24));
        debugRoot.setAlignment(Pos.TOP_CENTER);
        debugRoot.setStyle("-fx-background-color: " + BG_COLOR + ";");

        VBox debugSection = createSection("Debug");
        debugCheckBox = createCheckbox("Enable verbose logging", prefs.getBoolean(PREF_DEBUG_ENABLED, false));
        Label debugDesc = createDesc("Logs detailed task activate/execute info for troubleshooting.");
        debugSection.getChildren().addAll(debugCheckBox, debugDesc);
        debugRoot.getChildren().add(debugSection);

        Tab debugTab = new Tab("Debug", debugRoot);
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

        Scene scene = new Scene(layout, 340, 520);
        scene.setFill(Color.web(BG_COLOR));
        return scene;
    }

    private ImageView loadLogo() {
        try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
            if (in == null) {
                return null;
            }
            Image img = new Image(in);
            ImageView view = new ImageView(img);
            view.setPreserveRatio(true);
            view.setFitWidth(180);
            return view;
        } catch (IOException e) {
            return null;
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

    private void updateCuttingDescription(Label label) {
        if (cuttingCheckBox.isSelected()) {
            label.setText("Cuts gems when inventory full. Crushed gems are dropped. Banks only cut gems.");
        } else {
            label.setText("Banks raw gems without cutting.");
        }
    }

    private void saveSettings() {
        prefs.put(PREF_LOCATION, getSelectedLocation().displayName());
        prefs.putBoolean(PREF_CUTTING_ENABLED, isCuttingEnabled());
        prefs.putBoolean(PREF_DEBUG_ENABLED, debugCheckBox.isSelected());

        TidalsGemMiner.verboseLogging = debugCheckBox.isSelected();

        script.log("SETTINGS", "Location: " + getSelectedLocation().displayName() +
                   ", cutting: " + (isCuttingEnabled() ? "enabled" : "disabled") +
                   ", debug: " + debugCheckBox.isSelected());
    }

    public MiningLocation getSelectedLocation() {
        if (locationComboBox != null && locationComboBox.getValue() != null) {
            return locationComboBox.getValue();
        }
        return Locations.UPPER;
    }

    public boolean isCuttingEnabled() {
        return cuttingCheckBox != null && cuttingCheckBox.isSelected();
    }
}
