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
import tasks.DetectPlayers;

import java.io.InputStream;
import java.util.prefs.Preferences;

/**
 * setup ui with tabbed layout for chompy hunter configuration
 */
public class ScriptUI {
    private final Preferences prefs = Preferences.userRoot().node("tidals_chompy_hunter");

    // preference keys
    private static final String PREF_PLUCKING_ENABLED = "plucking_enabled";
    private static final String PREF_WEBHOOK_ENABLED = "webhook_enabled";
    private static final String PREF_WEBHOOK_URL = "webhook_url";
    private static final String PREF_WEBHOOK_INCLUDE_USER = "webhook_include_user";
    private static final String PREF_WEBHOOK_INTERVAL = "webhook_interval";
    private static final String PREF_ANTICRASH_ENABLED = "anticrash_enabled";
    private static final String PREF_DETECTION_RADIUS = "detection_radius";
    private static final String PREF_CRASH_THRESHOLD = "crash_threshold";

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
    private Spinner<Integer> radiusSpinner;
    private Spinner<Integer> thresholdSpinner;
    private VBox antiCrashSpinnerBox;

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

        // spacer
        Region spacer = new Region();
        spacer.setPrefHeight(8);
        mainBox.getChildren().add(spacer);

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

        // plucking checkbox
        pluckingCheckbox = new CheckBox("Enable Plucking Mode");
        pluckingCheckbox.setSelected(prefs.getBoolean(PREF_PLUCKING_ENABLED, false));
        pluckingCheckbox.setStyle(
            "-fx-text-fill: " + TEXT_LIGHT + "; " +
            "-fx-font-size: 14px; " +
            "-fx-cursor: hand;"
        );

        // plucking description
        Label pluckingDesc = new Label("Pluck chompy corpses for feathers and extra pet chance. Reduces kills/hr.");
        pluckingDesc.setWrapText(true);
        pluckingDesc.setMaxWidth(220);
        pluckingDesc.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");

        optionBox.getChildren().addAll(pluckingCheckbox, pluckingDesc);
        mainBox.getChildren().add(optionBox);

        // anti-crash box
        VBox antiCrashBox = new VBox(8);
        antiCrashBox.setAlignment(Pos.CENTER_LEFT);
        antiCrashBox.setPadding(new Insets(12));
        antiCrashBox.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -15%); " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );

        // section header
        Label antiCrashHeader = new Label("Anti-Crash Settings");
        antiCrashHeader.setStyle(
            "-fx-text-fill: " + TEXT_LIGHT + "; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold;"
        );

        // master checkbox
        antiCrashCheckbox = new CheckBox("Auto-hop on crashers");
        antiCrashCheckbox.setSelected(prefs.getBoolean(PREF_ANTICRASH_ENABLED, true));
        antiCrashCheckbox.setStyle(
            "-fx-text-fill: " + TEXT_LIGHT + "; " +
            "-fx-font-size: 14px; " +
            "-fx-cursor: hand;"
        );

        // load saved values
        int savedRadius = prefs.getInt(PREF_DETECTION_RADIUS, 9);
        int savedThreshold = prefs.getInt(PREF_CRASH_THRESHOLD, 3);

        // initialize DetectPlayers static fields from saved prefs
        DetectPlayers.detectionRadius = savedRadius;
        DetectPlayers.crashThresholdMs = savedThreshold * 1000L;

        // spinner container
        antiCrashSpinnerBox = new VBox(8);
        antiCrashSpinnerBox.setAlignment(Pos.CENTER_LEFT);
        antiCrashSpinnerBox.managedProperty().bind(antiCrashSpinnerBox.visibleProperty());
        antiCrashSpinnerBox.setVisible(antiCrashCheckbox.isSelected());

        // radius spinner row
        HBox radiusRow = new HBox(8);
        radiusRow.setAlignment(Pos.CENTER_LEFT);
        Label radiusLabel = new Label("Detection radius:");
        radiusLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        radiusSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 15, savedRadius));
        radiusSpinner.setEditable(false);
        radiusSpinner.setMaxWidth(80);
        radiusSpinner.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -25%); " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );
        Tooltip radiusTooltip = new Tooltip("How close other players must be to trigger detection (2-15 tiles)");
        radiusSpinner.setTooltip(radiusTooltip);
        radiusRow.getChildren().addAll(radiusLabel, radiusSpinner);

        // threshold spinner row
        HBox thresholdRow = new HBox(8);
        thresholdRow.setAlignment(Pos.CENTER_LEFT);
        Label thresholdLabel = new Label("Duration threshold:");
        thresholdLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        thresholdSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 30, savedThreshold));
        thresholdSpinner.setEditable(false);
        thresholdSpinner.setMaxWidth(80);
        thresholdSpinner.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -25%); " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );
        Tooltip thresholdTooltip = new Tooltip("How long a player must linger before hopping (3-30 seconds)");
        thresholdSpinner.setTooltip(thresholdTooltip);
        thresholdRow.getChildren().addAll(thresholdLabel, thresholdSpinner);

        antiCrashSpinnerBox.getChildren().addAll(radiusRow, thresholdRow);

        // checkbox action handler
        antiCrashCheckbox.setOnAction(e -> {
            boolean enabled = antiCrashCheckbox.isSelected();
            antiCrashSpinnerBox.setVisible(enabled);
        });

        antiCrashBox.getChildren().addAll(antiCrashHeader, antiCrashCheckbox, antiCrashSpinnerBox);
        mainBox.getChildren().add(antiCrashBox);

        Tab mainTab = new Tab("Main", mainBox);
        mainTab.setClosable(false);

        // === Discord Tab ===
        VBox discordBox = new VBox(12);
        discordBox.setPadding(new Insets(20, 24, 20, 24));
        discordBox.setAlignment(Pos.TOP_CENTER);
        discordBox.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // discord options box
        VBox discordOptionsBox = new VBox(12);
        discordOptionsBox.setAlignment(Pos.CENTER_LEFT);
        discordOptionsBox.setPadding(new Insets(12));
        discordOptionsBox.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -15%); " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );

        // webhook enabled checkbox
        webhookEnabledCheckbox = new CheckBox("Enable Discord Webhooks");
        webhookEnabledCheckbox.setSelected(prefs.getBoolean(PREF_WEBHOOK_ENABLED, false));
        webhookEnabledCheckbox.setStyle(
            "-fx-text-fill: " + TEXT_LIGHT + "; " +
            "-fx-font-size: 14px; " +
            "-fx-cursor: hand;"
        );

        // webhook url label
        Label urlLabel = new Label("Webhook URL");
        urlLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");

        // webhook url field
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
        webhookUrlField.setDisable(!webhookEnabledCheckbox.isSelected());

        // include username checkbox
        includeUsernameCheckbox = new CheckBox("Include Username");
        includeUsernameCheckbox.setSelected(prefs.getBoolean(PREF_WEBHOOK_INCLUDE_USER, true));
        includeUsernameCheckbox.setStyle(
            "-fx-text-fill: " + TEXT_LIGHT + "; " +
            "-fx-font-size: 13px; " +
            "-fx-cursor: hand;"
        );
        includeUsernameCheckbox.setDisable(!webhookEnabledCheckbox.isSelected());

        // interval label
        Label intervalLabel = new Label("Update Interval");
        intervalLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");

        // interval dropdown
        intervalComboBox = new ComboBox<>();
        intervalComboBox.getItems().addAll(
            "1 minute", "5 minutes", "10 minutes", "15 minutes", "30 minutes", "60 minutes"
        );
        int savedInterval = prefs.getInt(PREF_WEBHOOK_INTERVAL, 5);
        intervalComboBox.setValue(savedInterval + (savedInterval == 1 ? " minute" : " minutes"));
        intervalComboBox.setMaxWidth(Double.MAX_VALUE);
        intervalComboBox.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -25%); " +
            "-fx-mark-color: " + TEXT_LIGHT + "; " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );
        intervalComboBox.setDisable(!webhookEnabledCheckbox.isSelected());

        // webhook description
        Label webhookDesc = new Label("Get periodic progress updates and milestone notifications.");
        webhookDesc.setWrapText(true);
        webhookDesc.setMaxWidth(220);
        webhookDesc.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");

        // enable/disable controls based on webhook checkbox
        webhookEnabledCheckbox.setOnAction(e -> {
            boolean enabled = webhookEnabledCheckbox.isSelected();
            webhookUrlField.setDisable(!enabled);
            includeUsernameCheckbox.setDisable(!enabled);
            intervalComboBox.setDisable(!enabled);
        });

        discordOptionsBox.getChildren().addAll(
            webhookEnabledCheckbox,
            urlLabel,
            webhookUrlField,
            includeUsernameCheckbox,
            intervalLabel,
            intervalComboBox,
            webhookDesc
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

        // hover effect
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

        startButton.setOnAction(e -> saveSettings());

        // layout container
        VBox layout = new VBox(tabPane, startButton);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Scene scene = new Scene(layout, 300, 520);
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
        } catch (Exception e) {
            return null;
        }
    }

    private void saveSettings() {
        // parse interval from combo box
        String intervalStr = intervalComboBox.getValue();
        int intervalMinutes = 5; // default
        if (intervalStr != null) {
            intervalMinutes = Integer.parseInt(intervalStr.split(" ")[0]);
        }

        // read anti-crash values
        boolean antiCrashEnabled = antiCrashCheckbox.isSelected();
        int radius = radiusSpinner.getValue();
        int thresholdSeconds = thresholdSpinner.getValue();

        // save preferences
        prefs.putBoolean(PREF_PLUCKING_ENABLED, pluckingCheckbox.isSelected());
        prefs.putBoolean(PREF_WEBHOOK_ENABLED, webhookEnabledCheckbox.isSelected());
        prefs.put(PREF_WEBHOOK_URL, webhookUrlField.getText().trim());
        prefs.putBoolean(PREF_WEBHOOK_INCLUDE_USER, includeUsernameCheckbox.isSelected());
        prefs.putInt(PREF_WEBHOOK_INTERVAL, intervalMinutes);
        prefs.putBoolean(PREF_ANTICRASH_ENABLED, antiCrashEnabled);
        prefs.putInt(PREF_DETECTION_RADIUS, radius);
        prefs.putInt(PREF_CRASH_THRESHOLD, thresholdSeconds);

        // set static fields on main script
        TidalsChompyHunter.pluckingEnabled = pluckingCheckbox.isSelected();
        TidalsChompyHunter.webhookEnabled = webhookEnabledCheckbox.isSelected();
        TidalsChompyHunter.webhookUrl = webhookUrlField.getText().trim();
        TidalsChompyHunter.webhookIncludeUsername = includeUsernameCheckbox.isSelected();
        TidalsChompyHunter.webhookIntervalMinutes = intervalMinutes;

        // update DetectPlayers static fields
        DetectPlayers.detectionRadius = radius;
        DetectPlayers.crashThresholdMs = thresholdSeconds * 1000L;

        script.log(getClass(), "settings saved - plucking: " + pluckingCheckbox.isSelected() +
                   ", webhook: " + webhookEnabledCheckbox.isSelected() +
                   ", interval: " + intervalMinutes + "m" +
                   ", anti-crash: " + antiCrashEnabled +
                   ", radius: " + radius +
                   ", threshold: " + thresholdSeconds + "s");

        // close the window
        ((Stage) pluckingCheckbox.getScene().getWindow()).close();
    }

    // getters for settings
    public boolean isPluckingEnabled() {
        return pluckingCheckbox != null && pluckingCheckbox.isSelected();
    }

    public boolean isWebhookEnabled() {
        return webhookEnabledCheckbox != null && webhookEnabledCheckbox.isSelected();
    }

    public String getWebhookUrl() {
        return webhookUrlField != null ? webhookUrlField.getText().trim() : "";
    }

    public boolean isUsernameIncluded() {
        return includeUsernameCheckbox != null && includeUsernameCheckbox.isSelected();
    }
}
