package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;

import java.util.Set;
import java.util.function.BooleanSupplier;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.script.Script;
import utils.Task;

import static main.TidalsGemCutter.*;

public class Process extends Task {
    private final long startTime;

    public Process(Script script) {
        super(script);
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean activate() {
        ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID));
        if (inventorySnapshot == null) {
            // Inventory not visible
            return false;
        }

        return inventorySnapshot.contains(selectedUncutGemID);
    }

    @Override
    public boolean execute() {
        if (script.getWidgetManager().getBank().isVisible()) {
            script.log(getClass(), "Bank still open, closing it.");
            return script.getWidgetManager().getBank().close();
        }

        task = "Take invent snapshot";
        ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID, ItemID.CHISEL));

        if (inventorySnapshot == null) {
            // Inventory not visible
            return false;
        }

        if (!script.getWidgetManager().getInventory().unSelectItemIfSelected()) {
            return false;
        }

        boolean interacted = interactAndWaitForDialogue(inventorySnapshot);

        if (!interacted) {
            script.log(getClass().getSimpleName(), "Failed to interact with items. Re-polling...");
            return false;
        }

        task = "Select dialogue item";
        // Dialogue opened - select item to produce
        // For gem cutting, the dialogue shows the UNCUT gem, not the cut gem
        DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
        if (dialogueType == DialogueType.ITEM_OPTION) {
            // Click on the uncut gem image to start cutting
            boolean selected = script.getWidgetManager().getDialogue().selectItem(selectedUncutGemID);
            if (!selected) {
                script.log(getClass().getSimpleName(), "Selection failed, re-polling...");
                return false;
            }

            script.log(getClass().getSimpleName(), "Selected gem to cut.");
            waitUntilFinishedCrafting();
        }

        return false;
    }

    private boolean interactAndWaitForDialogue(ItemGroupResult inventSnapshot) {
        boolean firstIsGem = script.random(2) == 0;

        int firstID = firstIsGem ? selectedUncutGemID : ItemID.CHISEL;
        int secondID = firstIsGem ? ItemID.CHISEL : selectedUncutGemID;

        task = "Interact with item 1";
        // First interaction with retry
        if (!inventSnapshot.getRandomItem(firstID).interact()) {
            script.log(getClass(), "First item interaction failed, re-polling...");
            return false;
        }

        script.pollFramesUntil(() -> false, script.random(150, 300));

        task = "Interact with item 2";
        // Second interaction with retry
        if (!inventSnapshot.getRandomItem(secondID).interact()) {
            script.log(getClass(), "Second item interaction failed, re-polling...");
            return false;
        }

        // Wait for dialogue
        BooleanSupplier condition = () -> {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            return type == DialogueType.ITEM_OPTION;
        };

        task = "Wait for dialogue";
        return script.pollFramesHuman(condition, script.random(3000, 5000));
    }

    private void waitUntilFinishedCrafting() {
        task = "Wait till done processing";
        Timer amountChangeTimer = new Timer();

        // track cut gems to count individual cuts
        final int[] lastCutCount = {getCutGemCount()};

        BooleanSupplier condition = () -> {
            // level up check
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            if (type == DialogueType.TAP_HERE_TO_CONTINUE) {
                script.log(getClass().getSimpleName(), "Dialogue detected, leveled up?");
                script.getWidgetManager().getDialogue().continueChatDialogue();
                script.pollFramesHuman(() -> false, script.random(1000, 3000));
                return true;
            }

            // timeout
            if (amountChangeTimer.timeElapsed() > script.random(70000, 78000)) {
                return true;
            }

            // track individual gems cut by monitoring cut gem count
            int currentCutCount = getCutGemCount();
            if (currentCutCount > lastCutCount[0]) {
                int newlyCut = currentCutCount - lastCutCount[0];
                craftCount += newlyCut;
                lastCutCount[0] = currentCutCount;
            }

            // check if we ran out of uncut gems
            ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID));
            if (inventorySnapshot == null) {return false;}

            return !inventorySnapshot.contains(selectedUncutGemID);
        };

        script.log(getClass(), "Using human task to wait until crafting finishes.");
        script.pollFramesHuman(condition, script.random(70000, 78000));
    }

    private int getCutGemCount() {
        ItemGroupResult snapshot = script.getWidgetManager().getInventory().search(Set.of(selectedCutGemID));
        if (snapshot == null || !snapshot.contains(selectedCutGemID)) {
            return 0;
        }
        return snapshot.getAmount(selectedCutGemID);
    }
}
