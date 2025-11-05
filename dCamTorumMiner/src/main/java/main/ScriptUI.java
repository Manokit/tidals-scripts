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

    private static final String PREF_DEPOSIT_ACTION = "dcamtorumminer_deposit_action";
    private static final String PREF_DROP_ALSO_GEMS_CLUES = "dcamtorumminer_drop_also_gems_clues";

    private static final String PREF_USE_WDH = "dcamtorumminer_use_wdh";
    private static final String PREF_WDH_PLAYERS = "dcamtorumminer_wdh_players";

    private static final String PREF_MINING_LOCATION = "dcamtorumminer_mining_location";

    private static final String PREF_WEBHOOK_ENABLED = "dcamtorumminer_webhook_enabled";
    private static final String PREF_WEBHOOK_URL = "dcamtorumminer_webhook_url";
    private static final String PREF_WEBHOOK_INTERVAL = "dcamtorumminer_webhook_interval";
    private static final String PREF_WEBHOOK_INCLUDE_USER = "dcamtorumminer_webhook_include_user";

    private final Script script;
    private ComboBox<String> depositActionComboBox;
    private CheckBox dropAlsoGemsCluesCheckBox;

    private ComboBox<String> miningLocationComboBox;

    private CheckBox wdhCheckBox;
    private ComboBox<Integer> wdhPlayersComboBox;

    private CheckBox webhookEnabledCheckBox;
    private TextField webhookUrlField;
    private ComboBox<Integer> webhookIntervalComboBox;
    private CheckBox includeUsernameCheckBox;

    public ScriptUI(Script script) {
        this.script = script;
    }

    public Scene buildScene(ScriptCore core) {
        TabPane tabPane = new TabPane();

        // === Main Tab ===
        VBox mainBox = new VBox(10);
        mainBox.setStyle("-fx-background-color: #636E72; -fx-padding: 15; -fx-alignment: center");

        Label actionLabel = new Label("What to do with Calcified Deposits:");
        depositActionComboBox = new ComboBox<>();
        depositActionComboBox.getItems().addAll("Drop", "Smith");
        depositActionComboBox.getSelectionModel().select(prefs.get(PREF_DEPOSIT_ACTION, "Drop"));

        dropAlsoGemsCluesCheckBox = new CheckBox("Also drop gems & keys");
        dropAlsoGemsCluesCheckBox.setSelected(prefs.getBoolean(PREF_DROP_ALSO_GEMS_CLUES, false));

        boolean showExtraDrop = "Drop".equals(depositActionComboBox.getValue());
        dropAlsoGemsCluesCheckBox.setVisible(showExtraDrop);
        dropAlsoGemsCluesCheckBox.setManaged(showExtraDrop);

        depositActionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean show = "Drop".equals(newVal);
            dropAlsoGemsCluesCheckBox.setVisible(show);
            dropAlsoGemsCluesCheckBox.setManaged(show);
        });

        // === Mining Location ===
        Label miningLabel = new Label("Mining Location:");
        miningLocationComboBox = new ComboBox<>();
        miningLocationComboBox.getItems().addAll("East", "West");
        miningLocationComboBox.getSelectionModel().select(prefs.get(PREF_MINING_LOCATION, "East"));

        // === White Dot Hop ===
        wdhCheckBox = new CheckBox("Use White Dot Hop (WDH)");
        wdhCheckBox.setStyle("-fx-text-fill: white;");
        wdhCheckBox.setSelected(prefs.getBoolean(PREF_USE_WDH, false));

        wdhPlayersComboBox = new ComboBox<>();
        for (int i = 1; i <= 5; i++) wdhPlayersComboBox.getItems().add(i);
        wdhPlayersComboBox.getSelectionModel().select(
                Integer.valueOf(prefs.getInt(PREF_WDH_PLAYERS, 2))
        );

        Label wdhPlayersLabel = new Label("Hop if ≥ this many players nearby:");
        wdhPlayersLabel.setStyle("-fx-text-fill: white;");

        boolean wdhEnabled = wdhCheckBox.isSelected();
        wdhPlayersComboBox.setDisable(!wdhEnabled);
        wdhPlayersLabel.setDisable(!wdhEnabled);

        wdhCheckBox.setOnAction(e -> {
            boolean enabled = wdhCheckBox.isSelected();
            wdhPlayersComboBox.setDisable(!enabled);
            wdhPlayersLabel.setDisable(!enabled);
        });

        mainBox.getChildren().addAll(
                actionLabel,
                depositActionComboBox,
                dropAlsoGemsCluesCheckBox,
                new Separator(),
                miningLabel,
                miningLocationComboBox,
                new Separator(),
                wdhCheckBox,
                wdhPlayersLabel,
                wdhPlayersComboBox
        );

        Tab mainTab = new Tab("Main", mainBox);
        mainTab.setClosable(false);

        // === Webhook Tab ===
        VBox webhookBox = new VBox(10);
        webhookBox.setStyle("-fx-background-color: #636E72; -fx-padding: 15; -fx-alignment: center");

        webhookEnabledCheckBox = new CheckBox("Enable Webhooks");
        webhookEnabledCheckBox.setSelected(prefs.getBoolean(PREF_WEBHOOK_ENABLED, false));

        webhookUrlField = new TextField(prefs.get(PREF_WEBHOOK_URL, ""));
        webhookUrlField.setPromptText("Enter Webhook URL...");
        webhookUrlField.setDisable(!webhookEnabledCheckBox.isSelected());

        webhookIntervalComboBox = new ComboBox<>();
        for (int i = 1; i <= 60; i++) webhookIntervalComboBox.getItems().add(i);
        webhookIntervalComboBox.getSelectionModel().select(prefs.getInt(PREF_WEBHOOK_INTERVAL, 5) - 1);
        webhookIntervalComboBox.setDisable(!webhookEnabledCheckBox.isSelected());

        includeUsernameCheckBox = new CheckBox("Include Username");
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
                new Label("Send interval (minutes)"),
                webhookIntervalComboBox,
                includeUsernameCheckBox
        );

        Tab webhookTab = new Tab("Webhooks", webhookBox);
        webhookTab.setClosable(false);

        tabPane.getTabs().addAll(mainTab, webhookTab);

        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(event -> saveSettings());

        VBox layout = new VBox(tabPane, confirmButton);
        layout.setSpacing(10);
        layout.setStyle("-fx-background-color: #2d3436; -fx-padding: 10;");

        Scene scene = new Scene(layout, 300, 400);
        scene.getStylesheets().add("style.css");
        return scene;
    }

    private void saveSettings() {
        prefs.put(PREF_DEPOSIT_ACTION, getDepositAction());
        prefs.putBoolean(PREF_DROP_ALSO_GEMS_CLUES, isAlsoDropGemsAndClues());

        prefs.put(PREF_MINING_LOCATION, getMiningLocation());

        prefs.putBoolean(PREF_USE_WDH, useWDH());
        prefs.putInt(PREF_WDH_PLAYERS, getWDHPlayersRequired());

        prefs.putBoolean(PREF_WEBHOOK_ENABLED, isWebhookEnabled());
        prefs.put(PREF_WEBHOOK_URL, getWebhookUrl());
        prefs.putInt(PREF_WEBHOOK_INTERVAL, getWebhookInterval());
        prefs.putBoolean(PREF_WEBHOOK_INCLUDE_USER, isUsernameIncluded());

        ((Stage) depositActionComboBox.getScene().getWindow()).close();
    }

    // === Getters ===
    public String getDepositAction() {
        return depositActionComboBox.getSelectionModel().getSelectedItem();
    }

    public boolean isAlsoDropGemsAndClues() {
        return "Drop".equals(getDepositAction())
                && dropAlsoGemsCluesCheckBox != null
                && dropAlsoGemsCluesCheckBox.isSelected();
    }

    public String getMiningLocation() {
        return miningLocationComboBox != null
                ? miningLocationComboBox.getSelectionModel().getSelectedItem()
                : "East";
    }

    public boolean useWDH() {
        return wdhCheckBox != null && wdhCheckBox.isSelected();
    }

    public int getWDHPlayersRequired() {
        return wdhPlayersComboBox != null && wdhPlayersComboBox.getValue() != null
                ? wdhPlayersComboBox.getValue()
                : 2;
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
