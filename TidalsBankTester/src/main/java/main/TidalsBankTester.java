package main;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import javafx.scene.Scene;
import utilities.BankSearchUtils;
import utilities.BankingUtils;

import java.util.Collections;
import java.util.List;

/**
 * Test script for BankSearchUtils.
 * Withdraws multiple items with per-item quantities.
 */
@ScriptDefinition(
        name = "TidalsBankTester",
        description = "Tests BankSearchUtils with multiple items",
        skillCategory = SkillCategory.COMBAT,
        version = 1.0,
        author = "Tidaleus"
)
public class TidalsBankTester extends Script {

    private ScriptUI scriptUI;
    private List<ScriptUI.WithdrawItem> items;
    private int currentIndex = 0;

    private boolean taskComplete = false;

    public TidalsBankTester(Object scriptCore) {
        super(scriptCore);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[]{12850, 12598, 12342, 12853};
    }

    @Override
    public void onStart() {
        // show setup UI
        scriptUI = new ScriptUI(this);
        Scene scene = scriptUI.buildScene();
        getStageController().show(scene, "Bank Tester Setup", false);

        // get settings from UI
        items = scriptUI.getItems();

        log("=".repeat(50));
        log("BANK TESTER - Starting");
        log("Items to withdraw: " + items.size());
        for (int i = 0; i < items.size(); i++) {
            ScriptUI.WithdrawItem item = items.get(i);
            String name = getItemManager().getItemName(item.itemId);
            String qtyStr = item.quantity == 0 ? "All" : String.valueOf(item.quantity);
            log("  [" + (i + 1) + "] " + name + " (ID: " + item.itemId + ") x" + qtyStr);
        }
        log("=".repeat(50));

        if (items.isEmpty()) {
            log("No valid items provided!");
            taskComplete = true;
        }
    }

    @Override
    public int poll() {
        if (taskComplete) {
            return 5000;
        }

        // ensure bank is open
        if (!getWidgetManager().getBank().isVisible()) {
            log("Opening bank...");
            if (!BankingUtils.openBankAndWait(this, 15000)) {
                log("Failed to open bank!");
                return 2000;
            }
            pollFramesHuman(() -> false, random(500, 800));
            return 0;
        }

        // check if we've processed all items
        if (currentIndex >= items.size()) {
            log("=".repeat(50));
            log("All items processed!");
            log("=".repeat(50));
            taskComplete = true;
            return 5000;
        }

        // get current item
        ScriptUI.WithdrawItem item = items.get(currentIndex);
        String itemName = getItemManager().getItemName(item.itemId);
        String qtyStr = item.quantity == 0 ? "All" : String.valueOf(item.quantity);

        log("");
        log("[" + (currentIndex + 1) + "/" + items.size() + "] Withdrawing: " + itemName + " x" + qtyStr);

        // withdraw using BankSearchUtils with verification (keepSearchOpen=true since we'll reset it ourselves)
        boolean success = BankSearchUtils.searchAndWithdrawVerified(this, item.itemId, item.quantity, true);

        if (success) {
            log("[SUCCESS] Verified withdraw of " + qtyStr + " x " + itemName);

            // deposit back
            pollFramesHuman(() -> false, random(300, 500));
            getWidgetManager().getBank().depositAll(Collections.emptySet());
            pollFramesHuman(() -> false, random(200, 400));
        } else {
            log("[FAILED] Could not withdraw " + itemName);
        }

        // move to next item
        currentIndex++;

        // if more items to process, click search button to reset for next item
        if (currentIndex < items.size()) {
            BankSearchUtils.clickSearchToReset(this);
        } else {
            // last item - clear search to leave bank clean
            BankSearchUtils.clearSearch(this);
        }

        return 0;
    }
}
