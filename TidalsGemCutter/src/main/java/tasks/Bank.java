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
        ItemGroupResult inventorySnapshot;

        // If using banked cut gems mode, activate when we don't have cut gems
        if (useBankedGems && makeBoltTips) {
            inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(selectedCutGemID, selectedBoltTipID));
            if (inventorySnapshot == null) return false;
            // Bank when we don't have cut gems (either we have bolt tips to deposit, or we're starting fresh)
            return !inventorySnapshot.contains(selectedCutGemID);
        }

        // If making bolt tips (not from banked), activate when we have bolt tips and no uncut gems
        if (makeBoltTips) {
            inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID, selectedCutGemID, selectedBoltTipID));
            if (inventorySnapshot == null) return false;
            // Bank when we have bolt tips and no uncut/cut gems, OR we're starting fresh with nothing
            boolean hasBoltTips = inventorySnapshot.contains(selectedBoltTipID);
            boolean hasUncut = inventorySnapshot.contains(selectedUncutGemID);
            boolean hasCut = inventorySnapshot.contains(selectedCutGemID);
            return (hasBoltTips && !hasUncut && !hasCut) || (!hasUncut && !hasCut && !hasBoltTips);
        }

        // Normal mode: activate when we don't have uncut gems
        inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID));
        if (inventorySnapshot == null) return false;
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

        // Determine what item we need to withdraw based on mode
        int itemToWithdraw;
        String itemName;

        if (useBankedGems && makeBoltTips) {
            // Using banked cut gems mode - withdraw cut gems
            itemToWithdraw = selectedCutGemID;
            itemName = script.getItemManager().getItemName(selectedCutGemID);
        } else {
            // Normal mode or making bolt tips from uncut - withdraw uncut gems
            itemToWithdraw = selectedUncutGemID;
            itemName = script.getItemManager().getItemName(selectedUncutGemID);
        }

        script.log(getClass(), "Searching for " + itemName + " (ID: " + itemToWithdraw + ") in bank");

        ItemGroupResult bankSnapshot = script.getWidgetManager().getBank().search(Set.of(itemToWithdraw));
        ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(Collections.emptySet());

        if (inventorySnapshot == null) {
            script.log(getClass(), "Inventory not visible");
            return false;
        }
        if (bankSnapshot == null) {
            script.log(getClass(), "Bank snapshot is null - bank might not be ready");
            return false;
        }

        task = "Check bank items";

        // Check if we found the item we need
        if (bankSnapshot.contains(itemToWithdraw)) {
            int itemCount = bankSnapshot.getAmount(itemToWithdraw);
            script.log(getClass(), "Found " + itemCount + " " + itemName + " in bank");
        } else {
            script.log(getClass(), "bankSnapshot.contains() returned false for ID " + itemToWithdraw);
            script.log(getClass(), "Ran out of " + itemName + ". Stopping script.");
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
        if (!script.getWidgetManager().getBank().withdraw(itemToWithdraw, emptySlots)) {
            script.log(getClass(), "Withdraw failed for " + itemName + ", re-polling.");
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
