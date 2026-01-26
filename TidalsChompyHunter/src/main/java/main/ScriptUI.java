package main;

import com.osmb.api.ScriptCore;
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

/**
 * setup ui for chompy hunter - options, anti-crash, and discord webhooks
 */
public class ScriptUI {
    private final Preferences prefs = Preferences.userRoot().node("tidals_chompy_hunter");

    // preference keys
    private static final String PREF_PLUCKING_ENABLED = "plucking_enabled";
    private static final String PREF_ANTICRASH_ENABLED = "anticrash_enabled";
    private static final String PREF_WEBHOOK_ENABLED = "webhook_enabled";
    private static final String PREF_WEBHOOK_URL = "webhook_url";
    private static final String PREF_WEBHOOK_INCLUDE_USER = "webhook_include_user";
    private static final String PREF_WEBHOOK_INTERVAL = "webhook_interval";

    // tidals standard colors
    private static final String BG_COLOR = "#163134";
    private static final String BORDER_COLOR = "#284b50";
    private static final String GOLD = "#ffd700";
    private static final String TEXT_LIGHT = "#eeede9";
    private static final String TEXT_MUTED = "#aab9b9";

    private final Script script;

    // main tab controls
    private CheckBox pluckingCheckbox;
    private CheckBox antiCrashCheckbox;

    // discord tab controls
    private CheckBox webhookEnabledCheckbox;
    private TextField webhookUrlField;
    private CheckBox includeUsernameCheckbox;
    private ComboBox<String> intervalComboBox;

    public ScriptUI(Script script) {
        this.script = script;
    }

    public Scene buildScene(ScriptCore core) {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // === Main Tab ===
        VBox mainBox = new VBox(12);
        mainBox.setPadding(new Insets(20, 24, 20, 24));
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // logo
        ImageView logoView = loadLogo();
        if (logoView != null) {
            mainBox.getChildren().add(logoView);
        }

        // version
        Label versionLabel = new Label("v" + TidalsChompyHunter.SCRIPT_VERSION);
        versionLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        mainBox.getChildren().add(versionLabel);

        // === Options Section ===
        VBox optionBox = createSection("Options");
        pluckingCheckbox = createCheckbox("Enable Plucking Mode", prefs.getBoolean(PREF_PLUCKING_ENABLED, false));
        Label pluckDesc = createDesc("Pluck corpses for feathers. Reduces kills/hr.");
        optionBox.getChildren().addAll(pluckingCheckbox, pluckDesc);
        mainBox.getChildren().add(optionBox);

        // === Anti-Crash Section ===
        VBox crashBox = createSection("Anti-Crash");
        antiCrashCheckbox = createCheckbox("Auto-hop when crashed", prefs.getBoolean(PREF_ANTICRASH_ENABLED, true));
        Label crashDesc = createDesc("Hop if another player enters the hunting area for 7-12 seconds.");
        crashBox.getChildren().addAll(antiCrashCheckbox, crashDesc);
        mainBox.getChildren().add(crashBox);

        Tab mainTab = new Tab("Main", mainBox);
        mainTab.setClosable(false);

        // === Discord Tab ===
        VBox discordBox = new VBox(12);
        discordBox.setPadding(new Insets(20, 24, 20, 24));
        discordBox.setAlignment(Pos.TOP_CENTER);
        discordBox.setStyle("-fx-background-color: " + BG_COLOR + ";");

        VBox discordOptionsBox = createSection("Discord Webhooks");

        webhookEnabledCheckbox = createCheckbox("Enable webhooks", prefs.getBoolean(PREF_WEBHOOK_ENABLED, false));

        Label urlLabel = new Label("Webhook URL");
        urlLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");

        webhookUrlField = new TextField(prefs.get(PREF_WEBHOOK_URL, ""));
        webhookUrlField.setPromptText("https://discord.com/api/webhooks/...");
        webhookUrlField.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -25%); " +
            "-fx-text-fill: " + TEXT_LIGHT + "; " +
            "-fx-prompt-text-fill: " + TEXT_MUTED + "; " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );

        includeUsernameCheckbox = createCheckbox("Include username", prefs.getBoolean(PREF_WEBHOOK_INCLUDE_USER, true));

        Label intervalLabel = new Label("Update interval");
        intervalLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");

        intervalComboBox = new ComboBox<>();
        intervalComboBox.getItems().addAll("1 minute", "5 minutes", "10 minutes", "15 minutes", "30 minutes");
        int savedInterval = prefs.getInt(PREF_WEBHOOK_INTERVAL, 5);
        intervalComboBox.setValue(savedInterval + (savedInterval == 1 ? " minute" : " minutes"));
        intervalComboBox.setMaxWidth(Double.MAX_VALUE);
        intervalComboBox.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -25%); " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );
        // style the button cell (visible when closed) with white text
        intervalComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            }
        });
        // style dropdown items with white text
        intervalComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: white; -fx-background-color: " + BG_COLOR + "; -fx-font-size: 12px;");
            }
        });

        discordOptionsBox.getChildren().addAll(
            webhookEnabledCheckbox, urlLabel, webhookUrlField,
            includeUsernameCheckbox, intervalLabel, intervalComboBox
        );
        discordBox.getChildren().add(discordOptionsBox);

        Tab discordTab = new Tab("Discord", discordBox);
        discordTab.setClosable(false);

        tabPane.getTabs().addAll(mainTab, discordTab);

        // start button
        Button startButton = new Button("Start Hunting");
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
        startButton.setOnMouseEntered(e -> startButton.setStyle(
            "-fx-background-color: derive(" + GOLD + ", 15%); " +
            "-fx-text-fill: " + BG_COLOR + "; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand;"
        ));
        startButton.setOnMouseExited(e -> startButton.setStyle(
            "-fx-background-color: " + GOLD + "; " +
            "-fx-text-fill: " + BG_COLOR + "; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand;"
        ));
        startButton.setOnAction(e -> saveSettings());

        VBox layout = new VBox(tabPane, startButton);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Scene scene = new Scene(layout, 340, 420);
        scene.setFill(Color.web(BG_COLOR));
        return scene;
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

    private ImageView loadLogo() {
        try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
            if (in == null) return null;
            Image img = new Image(in);
            ImageView view = new ImageView(img);
            view.setPreserveRatio(true);
            view.setFitWidth(160);
            return view;
        } catch (IOException e) {
            return null;
        }
    }

    private void saveSettings() {
        // parse interval from combo box
        String intervalStr = intervalComboBox.getValue();
        int intervalMinutes = 5;
        if (intervalStr != null) {
            intervalMinutes = Integer.parseInt(intervalStr.split(" ")[0]);
        }

        // save preferences
        prefs.putBoolean(PREF_PLUCKING_ENABLED, pluckingCheckbox.isSelected());
        prefs.putBoolean(PREF_ANTICRASH_ENABLED, antiCrashCheckbox.isSelected());
        prefs.putBoolean(PREF_WEBHOOK_ENABLED, webhookEnabledCheckbox.isSelected());
        prefs.put(PREF_WEBHOOK_URL, webhookUrlField.getText().trim());
        prefs.putBoolean(PREF_WEBHOOK_INCLUDE_USER, includeUsernameCheckbox.isSelected());
        prefs.putInt(PREF_WEBHOOK_INTERVAL, intervalMinutes);

        // set static fields on main script
        TidalsChompyHunter.pluckingEnabled = pluckingCheckbox.isSelected();
        TidalsChompyHunter.antiCrashEnabled = antiCrashCheckbox.isSelected();
        TidalsChompyHunter.webhookEnabled = webhookEnabledCheckbox.isSelected();
        TidalsChompyHunter.webhookUrl = webhookUrlField.getText().trim();
        TidalsChompyHunter.webhookIncludeUsername = includeUsernameCheckbox.isSelected();
        TidalsChompyHunter.webhookIntervalMinutes = intervalMinutes;

        script.log(getClass(), "settings saved - plucking: " + pluckingCheckbox.isSelected() +
                   ", anti-crash: " + antiCrashCheckbox.isSelected());

        // close the window
        ((Stage) pluckingCheckbox.getScene().getWindow()).close();
    }
}
