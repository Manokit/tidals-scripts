package main;

import com.osmb.api.ScriptCore;
import com.osmb.api.script.Script;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

import static main.TidalsSecondaryCollector.*;

public class ScriptUI {
    private final Preferences prefs = Preferences.userRoot().node("tidals_secondary_collector");

    // preference keys
    private static final String PREF_SECONDARY_TYPE = "secondary_type";
    private static final String PREF_VARROCK_DIARY = "varrock_medium_diary";

    private final Script script;

    // controls
    private ComboBox<SecondaryType> secondaryDropdown;
    private CheckBox varrockDiaryCheckBox;

    public ScriptUI(Script script) {
        this.script = script;
    }

    public Scene buildScene(ScriptCore core) {
        VBox mainBox = new VBox(12);
        mainBox.setStyle("-fx-background: linear-gradient(to bottom, #18547A, #0F3460); -fx-padding: 20; -fx-alignment: center");

        // title
        Label titleLabel = new Label("Ironman Secondary Collector");
        titleLabel.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 16px; -fx-font-weight: bold;");

        // secondary selection dropdown
        Label secondaryLabel = new Label("Secondary to Collect");
        secondaryLabel.setStyle("-fx-text-fill: #40E0D0; -fx-font-size: 13px;");

        secondaryDropdown = new ComboBox<>();
        secondaryDropdown.getItems().addAll(SecondaryType.values());
        secondaryDropdown.setStyle("-fx-background-color: #0F3460; -fx-text-fill: white; -fx-border-color: #40E0D0;");

        // load saved preference
        String savedSecondary = prefs.get(PREF_SECONDARY_TYPE, SecondaryType.MORT_MYRE_FUNGUS.name());
        try {
            secondaryDropdown.getSelectionModel().select(SecondaryType.valueOf(savedSecondary));
        } catch (IllegalArgumentException e) {
            secondaryDropdown.getSelectionModel().select(SecondaryType.MORT_MYRE_FUNGUS);
        }

        // custom cell factory to display nice names
        secondaryDropdown.setCellFactory(param -> new ListCell<SecondaryType>() {
            @Override
            protected void updateItem(SecondaryType item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getDisplayName());
                } else {
                    setText(null);
                }
            }
        });
        secondaryDropdown.setButtonCell(new ListCell<SecondaryType>() {
            @Override
            protected void updateItem(SecondaryType item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getDisplayName());
                } else {
                    setText(null);
                }
            }
        });

        // varrock medium diary toggle
        varrockDiaryCheckBox = new CheckBox("Varrock Medium Diary Complete");
        varrockDiaryCheckBox.setStyle("-fx-text-fill: #98FB98; -fx-font-size: 12px;");
        varrockDiaryCheckBox.setSelected(prefs.getBoolean(PREF_VARROCK_DIARY, false));

        // tooltip for diary checkbox
        Tooltip diaryTip = new Tooltip("Enable this to use Varrock teleport to Grand Exchange for banking.\nOnly used if you don't have a Crafting cape or Ring of dueling.");
        diaryTip.setStyle("-fx-font-size: 11px;");
        varrockDiaryCheckBox.setTooltip(diaryTip);

        // info label
        Label infoLabel = new Label("Make sure you have the required items equipped:\n" +
                "- Bloom tool (blessed sickle/flail) in weapon slot\n" +
                "- Drakan's medallion in neck slot\n" +
                "- Banking teleport (cape/ring) equipped\n" +
                "- Ardy cloak in inventory or equipped");
        infoLabel.setStyle("-fx-text-fill: #6495ED; -fx-font-size: 10px; -fx-wrap-text: true;");
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(260);

        // start button
        Button startButton = new Button("Start Collecting");
        startButton.setStyle("-fx-background-color: #FF7F50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-background-color: #FF7F50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;"));
        startButton.setOnAction(event -> saveAndClose());

        mainBox.getChildren().addAll(
                titleLabel,
                secondaryLabel,
                secondaryDropdown,
                varrockDiaryCheckBox,
                infoLabel,
                startButton
        );

        Scene scene = new Scene(mainBox, 300, 320);
        return scene;
    }

    private void saveAndClose() {
        // save preferences
        SecondaryType selected = secondaryDropdown.getSelectionModel().getSelectedItem();
        prefs.put(PREF_SECONDARY_TYPE, selected.name());
        prefs.putBoolean(PREF_VARROCK_DIARY, varrockDiaryCheckBox.isSelected());

        // set static vars
        selectedSecondary = selected;
        hasVarrockMediumDiary = varrockDiaryCheckBox.isSelected();

        script.log("UI", "selected: " + selected.getDisplayName() + ", varrock diary: " + hasVarrockMediumDiary);

        // close window
        ((Stage) secondaryDropdown.getScene().getWindow()).close();
    }
}
