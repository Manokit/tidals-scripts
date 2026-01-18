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

import java.io.InputStream;
import java.util.prefs.Preferences;

public class ScriptUI {
    private final Preferences prefs = Preferences.userRoot().node("tidals_gem_miner");
    private static final String PREF_LOCATION = "selected_location";
    private static final String PREF_CUTTING_ENABLED = "cutting_enabled";

    // colors matching the paint overlay
    private static final String BG_COLOR = "#163134";
    private static final String BORDER_COLOR = "#284b50";
    private static final String GOLD = "#ffd700";
    private static final String TEXT_LIGHT = "#eeede9";
    private static final String TEXT_MUTED = "#aab9b9";

    private final Script script;
    private ComboBox<MiningLocation> locationComboBox;
    private CheckBox cuttingCheckBox;

    public ScriptUI(Script script) {
        this.script = script;
    }

    public Scene buildScene(Script script) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20, 24, 20, 24));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // logo
        ImageView logoView = loadLogo();
        if (logoView != null) {
            root.getChildren().add(logoView);
        }

        // version label
        Label versionLabel = new Label("v" + TidalsGemMiner.scriptVersion);
        versionLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        root.getChildren().add(versionLabel);

        // spacer
        Region spacer = new Region();
        spacer.setPrefHeight(8);
        root.getChildren().add(spacer);

        // location selection option box
        VBox locationBox = new VBox(8);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        locationBox.setPadding(new Insets(12));
        locationBox.setStyle(
                "-fx-background-color: derive(" + BG_COLOR + ", -15%); " +
                "-fx-border-color: " + BORDER_COLOR + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4;"
        );

        Label locationLabel = new Label("Mining Location");
        locationLabel.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        locationComboBox = new ComboBox<>();
        locationComboBox.getItems().addAll(Locations.ALL_LOCATIONS);
        locationComboBox.setMaxWidth(Double.MAX_VALUE);
        locationComboBox.setStyle(
                "-fx-background-color: derive(" + BG_COLOR + ", 10%); " +
                "-fx-text-fill: " + TEXT_LIGHT + "; " +
                "-fx-font-size: 13px;"
        );

        // custom cell factory to show display names with white text
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

        // style the button cell (selected item display) with white text
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

        // converter for the button cell
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

        Label locationDesc = new Label("Upper mine has gem rocks on surface. Underground mine is inside cave.");
        locationDesc.setWrapText(true);
        locationDesc.setMaxWidth(220);
        locationDesc.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");

        locationBox.getChildren().addAll(locationLabel, locationComboBox, locationDesc);
        root.getChildren().add(locationBox);

        // cutting option box
        VBox cuttingBox = new VBox(8);
        cuttingBox.setAlignment(Pos.CENTER_LEFT);
        cuttingBox.setPadding(new Insets(12));
        cuttingBox.setStyle(
                "-fx-background-color: derive(" + BG_COLOR + ", -15%); " +
                "-fx-border-color: " + BORDER_COLOR + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4;"
        );

        cuttingCheckBox = new CheckBox("Cut gems (drops crushed)");
        cuttingCheckBox.setSelected(prefs.getBoolean(PREF_CUTTING_ENABLED, false));
        cuttingCheckBox.setStyle(
                "-fx-text-fill: " + TEXT_LIGHT + "; " +
                "-fx-font-size: 14px; " +
                "-fx-cursor: hand;"
        );

        Label cuttingDesc = new Label();
        cuttingDesc.setWrapText(true);
        cuttingDesc.setMaxWidth(220);
        cuttingDesc.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
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
        root.getChildren().add(cuttingBox);

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

        root.getChildren().add(startButton);

        Scene scene = new Scene(root);
        scene.setFill(Color.web(BG_COLOR));
        scene.getRoot().autosize();

        return scene;
    }

    private ImageView loadLogo() {
        try (InputStream in = getClass().getResourceAsStream("/Tidals Gem Miner.png")) {
            if (in == null) {
                return null;
            }
            Image img = new Image(in);
            ImageView view = new ImageView(img);
            view.setPreserveRatio(true);
            view.setFitWidth(180);
            return view;
        } catch (Exception e) {
            return null;
        }
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
        script.log("SETTINGS", "Location: " + getSelectedLocation().displayName());
        script.log("SETTINGS", "Cutting: " + (isCuttingEnabled() ? "enabled" : "disabled"));
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
