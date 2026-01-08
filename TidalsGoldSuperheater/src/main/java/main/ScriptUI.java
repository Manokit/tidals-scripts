package main;

import com.osmb.api.ScriptCore;
import com.osmb.api.script.Script;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class ScriptUI {
    private final Preferences prefs = Preferences.userRoot().node("main");
    
    // Mode preferences
    private static final String PREF_AFK_MODE = "tgoldsuperheater_afk_mode";
    
    // Webhook preferences
    private static final String PREF_WEBHOOK_ENABLED = "tgoldsuperheater_webhook_enabled";
    private static final String PREF_WEBHOOK_URL = "tgoldsuperheater_webhook_url";
    private static final String PREF_WEBHOOK_INTERVAL = "tgoldsuperheater_webhook_interval";
    private static final String PREF_WEBHOOK_INCLUDE_USER = "tgoldsuperheater_webhook_include_user";

    private final Script script;
    
    // Mode selection
    private RadioButton afkModeRadio;
    private RadioButton activeModeRadio;
    private ToggleGroup modeToggleGroup;
    
    // Webhook controls
    private CheckBox webhookEnabledCheckBox;
    private TextField webhookUrlField;
    private ComboBox<Integer> webhookIntervalComboBox;
    private CheckBox includeUsernameCheckBox;

    public ScriptUI(Script script) {
        this.script = script;
    }

    public Scene buildScene(ScriptCore core) {
        TabPane tabPane = new TabPane();

        // === Main Tab (Ocean Theme) ===
        VBox mainBox = new VBox(12);
        mainBox.setStyle("-fx-background: linear-gradient(to bottom, #18547A, #0F3460); -fx-padding: 20; -fx-alignment: center");

        // Mode selection label
        Label modeLabel = new Label("Superheating Mode");
        modeLabel.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Mode toggle group
        modeToggleGroup = new ToggleGroup();

        afkModeRadio = new RadioButton("AFK Mode (Auto-cast, slower)");
        afkModeRadio.setStyle("-fx-text-fill: #98FB98; -fx-font-size: 12px;");
        afkModeRadio.setToggleGroup(modeToggleGroup);

        activeModeRadio = new RadioButton("Active Mode (Manual cast, faster)");
        activeModeRadio.setStyle("-fx-text-fill: #98FB98; -fx-font-size: 12px;");
        activeModeRadio.setToggleGroup(modeToggleGroup);

        // Load saved preference (default to AFK mode)
        boolean savedAFKMode = prefs.getBoolean(PREF_AFK_MODE, true);
        if (savedAFKMode) {
            afkModeRadio.setSelected(true);
        } else {
            activeModeRadio.setSelected(true);
        }

        // Mode description
        Label modeDescLabel = new Label("");
        modeDescLabel.setStyle("-fx-text-fill: #6495ED; -fx-font-size: 11px; -fx-wrap-text: true;");
        modeDescLabel.setWrapText(true);
        modeDescLabel.setMaxWidth(260);
        updateModeDescription(modeDescLabel);

        // Update description when mode changes
        modeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            updateModeDescription(modeDescLabel);
        });

        // Tooltip for modes
        Tooltip afkTip = new Tooltip("Cast once and let the game auto-cast through all ores. Slower but fewer actions.");
        afkTip.setStyle("-fx-font-size: 11px;");
        afkModeRadio.setTooltip(afkTip);

        Tooltip activeTip = new Tooltip("Manually cast on each ore every 5-6 ticks. Faster XP but more clicks.");
        activeTip.setStyle("-fx-font-size: 11px;");
        activeModeRadio.setTooltip(activeTip);

        mainBox.getChildren().addAll(modeLabel, afkModeRadio, activeModeRadio, modeDescLabel);
        Tab mainTab = new Tab("Main", mainBox);
        mainTab.setClosable(false);

        // === Webhook Tab (Ocean Theme) ===
        VBox webhookBox = new VBox(12);
        webhookBox.setStyle("-fx-background: linear-gradient(to bottom, #18547A, #0F3460); -fx-padding: 20; -fx-alignment: center");

        webhookEnabledCheckBox = new CheckBox("Enable Webhooks");
        webhookEnabledCheckBox.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 13px;");
        webhookEnabledCheckBox.setSelected(prefs.getBoolean(PREF_WEBHOOK_ENABLED, false));

        webhookUrlField = new TextField(prefs.get(PREF_WEBHOOK_URL, ""));
        webhookUrlField.setPromptText("Enter Webhook URL...");
        webhookUrlField.setStyle("-fx-background-color: #0F3460; -fx-text-fill: white; -fx-prompt-text-fill: #98FB98; -fx-border-color: #40E0D0; -fx-border-width: 1;");
        webhookUrlField.setDisable(!webhookEnabledCheckBox.isSelected());

        Label intervalLabel = new Label("Send interval (minutes)");
        intervalLabel.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 12px;");

        webhookIntervalComboBox = new ComboBox<>();
        webhookIntervalComboBox.setStyle("-fx-background-color: #0F3460; -fx-text-fill: white; -fx-border-color: #40E0D0;");
        for (int i = 1; i <= 60; i++) webhookIntervalComboBox.getItems().add(i);
        webhookIntervalComboBox.getSelectionModel().select(prefs.getInt(PREF_WEBHOOK_INTERVAL, 5) - 1);
        webhookIntervalComboBox.setDisable(!webhookEnabledCheckBox.isSelected());

        includeUsernameCheckBox = new CheckBox("Include Username");
        includeUsernameCheckBox.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 13px;");
        includeUsernameCheckBox.setSelected(prefs.getBoolean(PREF_WEBHOOK_INCLUDE_USER, true));
        includeUsernameCheckBox.setDisable(!webhookEnabledCheckBox.isSelected());

        webhookEnabledCheckBox.setOnAction(e -> {
            boolean enabled = webhookEnabledCheckBox.isSelected();
            webhookUrlField.setDisable(!enabled);
            webhookIntervalComboBox.setDisable(!enabled);
            includeUsernameCheckBox.setDisable(!enabled);
        });

        webhookBox.getChildren().addAll(
                webhookEnabledCheckBox,
                webhookUrlField,
                intervalLabel,
                webhookIntervalComboBox,
                includeUsernameCheckBox
        );

        Tab webhookTab = new Tab("Webhooks", webhookBox);
        webhookTab.setClosable(false);

        tabPane.getTabs().addAll(mainTab, webhookTab);

        // Confirm Button (Ocean themed)
        Button confirmButton = new Button("Start Superheating");
        confirmButton.setStyle("-fx-background-color: #FF7F50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        confirmButton.setOnMouseEntered(e -> confirmButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;"));
        confirmButton.setOnMouseExited(e -> confirmButton.setStyle("-fx-background-color: #FF7F50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;"));
        confirmButton.setOnAction(event -> saveSettings());

        VBox layout = new VBox(tabPane, confirmButton);
        layout.setSpacing(15);
        layout.setStyle("-fx-background: linear-gradient(to bottom, #0F3460, #0A1F3D); -fx-padding: 15;");

        Scene scene = new Scene(layout, 300, 280);
        return scene;
    }

    private void updateModeDescription(Label label) {
        if (afkModeRadio.isSelected()) {
            label.setText("Cast once and let the game auto-cast. Lower actions, good for mobile.");
        } else {
            label.setText("Manually cast every 5-6 ticks. Faster XP rates but more actions.");
        }
    }

    private void saveSettings() {
        // Save mode preference
        prefs.putBoolean(PREF_AFK_MODE, isAFKMode());
        script.log("SAVESETTINGS", "Saved mode preference: " + (isAFKMode() ? "AFK" : "Active"));

        // Save webhook preferences
        prefs.putBoolean(PREF_WEBHOOK_ENABLED, isWebhookEnabled());
        prefs.put(PREF_WEBHOOK_URL, getWebhookUrl());
        prefs.putInt(PREF_WEBHOOK_INTERVAL, getWebhookInterval());
        prefs.putBoolean(PREF_WEBHOOK_INCLUDE_USER, isUsernameIncluded());

        ((Stage) afkModeRadio.getScene().getWindow()).close();
    }

    // Getters
    public boolean isAFKMode() {
        return afkModeRadio != null && afkModeRadio.isSelected();
    }

    public boolean isWebhookEnabled() {
        return webhookEnabledCheckBox != null && webhookEnabledCheckBox.isSelected();
    }

    public String getWebhookUrl() {
        return webhookUrlField != null ? webhookUrlField.getText().trim() : "";
    }

    public int getWebhookInterval() {
        return webhookIntervalComboBox != null && webhookIntervalComboBox.getValue() != null
                ? webhookIntervalComboBox.getValue()
                : 5;
    }

    public boolean isUsernameIncluded() {
        return includeUsernameCheckBox != null && includeUsernameCheckBox.isSelected();
    }
}
