package main;

import com.osmb.api.ScriptCore;
import com.osmb.api.item.ItemID;
import com.osmb.api.script.Script;
import com.osmb.api.javafx.JavaFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.prefs.Preferences;

public class ScriptUI {
    private final Preferences prefs = Preferences.userRoot().node("main");
    private static final String PREF_SELECTED_GEM = "tgemcutter_selected_gem";

    private static final String PREF_WEBHOOK_ENABLED = "tgemcutter_webhook_enabled";
    private static final String PREF_WEBHOOK_URL = "tgemcutter_webhook_url";
    private static final String PREF_WEBHOOK_INTERVAL = "tgemcutter_webhook_interval";
    private static final String PREF_WEBHOOK_INCLUDE_USER = "tgemcutter_webhook_include_user";

    private final Script script;
    private ComboBox<Integer> gemComboBox;

    private CheckBox webhookEnabledCheckBox;
    private TextField webhookUrlField;
    private ComboBox<Integer> webhookIntervalComboBox;
    private CheckBox includeUsernameCheckBox;

    // Uncut gem options (common gems only)
    private static final Integer[] GEM_OPTIONS = {
            ItemID.UNCUT_SAPPHIRE,
            ItemID.UNCUT_EMERALD,
            ItemID.UNCUT_RUBY,
            ItemID.UNCUT_DIAMOND
    };

    public ScriptUI(Script script) {
        this.script = script;
    }

    public Scene buildScene(ScriptCore core) {
        TabPane tabPane = new TabPane();

        // === Main Tab (Ocean Theme) ===
        VBox mainBox = new VBox(12);
        mainBox.setStyle("-fx-background: linear-gradient(to bottom, #18547A, #0F3460); -fx-padding: 20; -fx-alignment: center");

        Label gemLabel = new Label("Choose gem type to cut");
        gemLabel.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 14px; -fx-font-weight: bold;");
        gemComboBox = createGemComboBox(core);
        gemComboBox.setStyle("-fx-background-color: #0F3460; -fx-text-fill: white; -fx-border-color: #40E0D0; -fx-border-width: 2;");

        int savedGemId = prefs.getInt(PREF_SELECTED_GEM, ItemID.UNCUT_RUBY);
        for (Integer option : GEM_OPTIONS) {
            if (option.equals(savedGemId)) {
                gemComboBox.getSelectionModel().select(option);
                break;
            }
        }

        script.log("SAVESETTINGS", "Loaded selected gem from preferences: " + savedGemId);
        mainBox.getChildren().addAll(gemLabel, gemComboBox);
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
        Button confirmButton = new Button("Start Cutting Gems");
        confirmButton.setStyle("-fx-background-color: #FF7F50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        confirmButton.setOnMouseEntered(e -> confirmButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;"));
        confirmButton.setOnMouseExited(e -> confirmButton.setStyle("-fx-background-color: #FF7F50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;"));
        confirmButton.setOnAction(event -> saveSettings());

        VBox layout = new VBox(tabPane, confirmButton);
        layout.setSpacing(15);
        layout.setStyle("-fx-background: linear-gradient(to bottom, #0F3460, #0A1F3D); -fx-padding: 15;");

        Scene scene = new Scene(layout, 300, 320);
        scene.getStylesheets().add("style.css");
        return scene;
    }

    private ComboBox<Integer> createGemComboBox(ScriptCore core) {
        ComboBox<Integer> comboBox = new ComboBox<>();
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer itemId) {
                return itemId != null ? core.getItemManager().getItemName(itemId) : "";
            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
        });

        comboBox.setCellFactory(param -> createItemCell(core));
        comboBox.setButtonCell(createItemCell(core));
        comboBox.getItems().addAll(GEM_OPTIONS);
        return comboBox;
    }

    private ListCell<Integer> createItemCell(ScriptCore core) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Integer itemId, boolean empty) {
                super.updateItem(itemId, empty);
                if (itemId != null && !empty) {
                    String name = core.getItemManager().getItemName(itemId);
                    ImageView imageView = JavaFXUtils.getItemImageView(core, itemId);
                    if (imageView != null) {
                        imageView.setFitWidth(16);
                        imageView.setFitHeight(16);
                    }
                    setGraphic(imageView);
                    setText(name);
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        };
    }

    private void saveSettings() {
        Integer selected = gemComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            prefs.putInt(PREF_SELECTED_GEM, selected);
            script.log("SAVESETTINGS", "Saved selected gem to preferences: " + selected);
        }

        prefs.putBoolean(PREF_WEBHOOK_ENABLED, isWebhookEnabled());
        prefs.put(PREF_WEBHOOK_URL, getWebhookUrl());
        prefs.putInt(PREF_WEBHOOK_INTERVAL, getWebhookInterval());
        prefs.putBoolean(PREF_WEBHOOK_INCLUDE_USER, isUsernameIncluded());

        ((Stage) gemComboBox.getScene().getWindow()).close();
    }

    // Getters
    public int getSelectedUncutGemId() {
        return gemComboBox.getSelectionModel().getSelectedItem();
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
