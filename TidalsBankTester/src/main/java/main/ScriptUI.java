package main;

import com.osmb.api.script.Script;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Holds an item ID and quantity pair for withdrawal.
 */
public class ScriptUI {

    public static class WithdrawItem {
        public final int itemId;
        public final int quantity;

        public WithdrawItem(int itemId, int quantity) {
            this.itemId = itemId;
            this.quantity = quantity;
        }
    }

    private final Preferences prefs = Preferences.userRoot().node("tidals_bank_tester");
    private static final String PREF_ITEMS = "items";

    // colors matching other tidals scripts
    private static final String BG_COLOR = "#163134";
    private static final String BORDER_COLOR = "#284b50";
    private static final String GOLD = "#ffd700";
    private static final String TEXT_LIGHT = "#eeede9";
    private static final String TEXT_MUTED = "#aab9b9";

    private final Script script;
    private TextArea itemIdsArea;

    public ScriptUI(Script script) {
        this.script = script;
    }

    public Scene buildScene(Script script) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20, 24, 20, 24));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // title
        Label titleLabel = new Label("Tidals Bank Tester");
        titleLabel.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 18px; -fx-font-weight: bold;");
        root.getChildren().add(titleLabel);

        // version label
        Label versionLabel = new Label("v1.0");
        versionLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        root.getChildren().add(versionLabel);

        // spacer
        Region spacer = new Region();
        spacer.setPrefHeight(8);
        root.getChildren().add(spacer);

        // items section
        Label itemLabel = new Label("Items (id:quantity, comma-separated):");
        itemLabel.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 13px;");

        itemIdsArea = new TextArea();
        itemIdsArea.setPromptText("e.g., 579:1, 383:5, 1511:All");
        itemIdsArea.setText(prefs.get(PREF_ITEMS, "579:1, 383:5"));
        itemIdsArea.setStyle(
            "-fx-control-inner-background: #ffffff; " +
            "-fx-text-fill: #000000; " +
            "-fx-prompt-text-fill: " + TEXT_MUTED + "; " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4; " +
            "-fx-font-size: 13px;"
        );
        itemIdsArea.setPrefWidth(280);
        itemIdsArea.setPrefHeight(80);
        itemIdsArea.setWrapText(true);

        Label itemHint = new Label("Format: itemId:quantity (use 'All' for all)\nFind IDs at osmb.co.uk/javadocs");
        itemHint.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 10px;");
        itemHint.setWrapText(true);
        itemHint.setMaxWidth(280);

        VBox itemBox = new VBox(8);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(12));
        itemBox.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -15%); " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );
        itemBox.getChildren().addAll(itemLabel, itemIdsArea, itemHint);
        root.getChildren().add(itemBox);

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

    private void saveSettings() {
        prefs.put(PREF_ITEMS, getItemsRaw());
        script.log("SETTINGS", "Items: " + getItemsRaw());
    }

    public String getItemsRaw() {
        return itemIdsArea != null ? itemIdsArea.getText().trim() : "";
    }

    /**
     * Parse items in format "id:quantity, id:quantity, ..."
     * Quantity can be a number or "All" (which becomes 0).
     *
     * @return list of WithdrawItem pairs
     */
    public List<WithdrawItem> getItems() {
        String raw = getItemsRaw();
        List<WithdrawItem> items = new ArrayList<>();

        if (raw.isEmpty()) {
            return items;
        }

        // split by comma or whitespace
        String[] parts = raw.split("[,\\s]+");

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            // parse id:quantity format
            String[] idQty = part.split(":");
            if (idQty.length < 1) continue;

            try {
                int itemId = Integer.parseInt(idQty[0].trim());
                if (itemId <= 0) continue;

                // default quantity is 1 if not specified
                int quantity = 1;
                if (idQty.length >= 2) {
                    String qtyStr = idQty[1].trim();
                    if ("all".equalsIgnoreCase(qtyStr)) {
                        quantity = 0; // 0 means withdraw all
                    } else {
                        quantity = Integer.parseInt(qtyStr);
                    }
                }

                items.add(new WithdrawItem(itemId, quantity));
            } catch (NumberFormatException e) {
                // skip invalid entries
            }
        }

        return items;
    }
}
