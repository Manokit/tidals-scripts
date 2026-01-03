package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.utils.timing.Timer;
import main.TidalsGemCutter;
import utils.Task;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static main.TidalsGemCutter.*;

public class Bank extends Task {

    public Bank(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID));
        if (inventorySnapshot == null) {
            // Inventory not visible
            return false;
        }

        return !inventorySnapshot.contains(selectedUncutGemID);
    }

    @Override
    public boolean execute() {
        task = getClass().getSimpleName();
        if (!script.getWidgetManager().getBank().isVisible()) {
            openBank();
            return false;
        }

        // Give bank a moment to fully load before searching
        script.pollFramesHuman(() -> false, script.random(300, 500));

        task = "Get bank snapshot";

        // debug: log what we're searching for
        String gemName = script.getItemManager().getItemName(selectedUncutGemID);
        script.log(getClass(), "Searching for " + gemName + " (ID: " + selectedUncutGemID + ") in bank");

        ItemGroupResult bankSnapshot = script.getWidgetManager().getBank().search(Set.of(selectedUncutGemID));
        ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(Collections.emptySet());

        if (inventorySnapshot == null) {
            // Inventory not visible
            script.log(getClass(), "Inventory not visible");
            return false;
        }
        if (bankSnapshot == null) {
            // Bank not visible or search failed
            script.log(getClass(), "Bank snapshot is null - bank might not be ready");
            return false;
        }

        task = "Check bank items";

        // Debug: Check if we found any of the gem
        if (bankSnapshot.contains(selectedUncutGemID)) {
            int gemCount = bankSnapshot.getAmount(selectedUncutGemID);
            script.log(getClass(), "Found " + gemCount + " " + gemName + " in bank");
        } else {
            script.log(getClass(), "bankSnapshot.contains() returned false for ID " + selectedUncutGemID);
            script.log(getClass(), "Ran out of " + gemName + ". Stopping script.");
            script.stop();
            return false;
        }

        // Deposit all except chisel
        task = "Deposit items";
        script.getWidgetManager().getBank().depositAll(Set.of(ItemID.CHISEL));

        // Wait a moment for deposit to complete
        script.pollFramesHuman(() -> false, script.random(300, 600));

        // Get fresh inventory snapshot AFTER depositing to calculate correct empty slots
        inventorySnapshot = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inventorySnapshot == null) {
            script.log(getClass(), "Inventory not visible after deposit");
            return false;
        }

        int emptySlots = inventorySnapshot.getFreeSlots();
        script.log(getClass(), "Empty inventory slots after deposit: " + emptySlots);

        task = "Withdraw items";
        if (!script.getWidgetManager().getBank().withdraw(selectedUncutGemID, emptySlots)) {
            script.log(getClass(), "Withdraw failed for " + gemName + ", re-polling.");
            return false;
        }

        task = "Close bank";
        script.getWidgetManager().getBank().close();
        script.pollFramesHuman(() -> !script.getWidgetManager().getBank().isVisible(), 5000);
        return false;
    }

    private void openBank() {
        task = "Open bank";
        script.log(getClass(), "Searching for a bank...");

        List<RSObject> banksFound = script.getObjectManager().getObjects(TidalsGemCutter.bankQuery);
        if (banksFound.isEmpty()) {
            script.log(getClass(), "No bank objects found.");
            return;
        }

        RSObject bank = (RSObject) script.getUtils().getClosest(banksFound);
        if (!bank.interact(TidalsGemCutter.BANK_ACTIONS)) {
            script.log(getClass(), "Failed to interact with bank object.");
            return;
        }

        AtomicReference<Timer> positionChangeTimer = new AtomicReference<>(new Timer());
        AtomicReference<WorldPosition> previousPosition = new AtomicReference<>(null);

        script.pollFramesHuman(() -> {
            WorldPosition current = script.getWorldPosition();
            if (current == null) return false;

            if (!Objects.equals(current, previousPosition.get())) {
                positionChangeTimer.get().reset();
                previousPosition.set(current);
            }

            return script.getWidgetManager().getBank().isVisible() || positionChangeTimer.get().timeElapsed() > 2000;
        }, 15000);
    }
}
