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
        ItemGroupResult inventorySnapshot;

        // If using banked cut gems mode, activate when we have cut gems
        if (useBankedGems && makeBoltTips) {
            inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(selectedCutGemID));
            if (inventorySnapshot == null) return false;
            return inventorySnapshot.contains(selectedCutGemID);
        }

        // If making bolt tips (not from banked), activate when we have uncut gems OR cut gems
        if (makeBoltTips) {
            inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID, selectedCutGemID));
            if (inventorySnapshot == null) return false;
            return inventorySnapshot.contains(selectedUncutGemID) || inventorySnapshot.contains(selectedCutGemID);
        }

        // Normal mode: activate when we have uncut gems
        inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID));
        if (inventorySnapshot == null) return false;
        return inventorySnapshot.contains(selectedUncutGemID);
    }

    @Override
    public boolean execute() {
        if (script.getWidgetManager().getBank().isVisible()) {
            script.log(getClass(), "Bank still open, closing it.");
            return script.getWidgetManager().getBank().close();
        }

        // Check what we have in inventory
        ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(
            Set.of(selectedUncutGemID, selectedCutGemID, ItemID.CHISEL)
        );

        if (inventorySnapshot == null) {
            return false;
        }

        boolean hasUncut = inventorySnapshot.contains(selectedUncutGemID);
        boolean hasCut = inventorySnapshot.contains(selectedCutGemID);

        // Determine what to craft
        if (makeBoltTips && hasCut) {
            // Make bolt tips from cut gems
            return makeBoltTipsFromCutGems(inventorySnapshot);
        } else if (hasUncut) {
            // Cut uncut gems into cut gems
            return cutUncutGems(inventorySnapshot);
        }

        return false;
    }

    private boolean cutUncutGems(ItemGroupResult inventorySnapshot) {
        if (!script.getWidgetManager().getInventory().unSelectItemIfSelected()) {
            return false;
        }

        boolean interacted = interactWithItems(inventorySnapshot, selectedUncutGemID);

        if (!interacted) {
            script.log(getClass().getSimpleName(), "Failed to interact with items. Re-polling...");
            return false;
        }

        task = "Select dialogue item";
        // For gem cutting, the dialogue shows the UNCUT gem, not the cut gem
        DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
        if (dialogueType == DialogueType.ITEM_OPTION) {
            boolean selected = script.getWidgetManager().getDialogue().selectItem(selectedUncutGemID);
            if (!selected) {
                script.log(getClass().getSimpleName(), "Selection failed, re-polling...");
                return false;
            }

            script.log(getClass().getSimpleName(), "Selected gem to cut.");
            waitUntilFinishedCrafting(selectedUncutGemID, selectedCutGemID);
            // Add small delay after crafting completes to prevent immediate re-activation
            script.pollFramesHuman(() -> false, script.random(400, 800));
        }

        return false;
    }

    private boolean makeBoltTipsFromCutGems(ItemGroupResult inventorySnapshot) {
        if (!script.getWidgetManager().getInventory().unSelectItemIfSelected()) {
            return false;
        }

        boolean interacted = interactWithItems(inventorySnapshot, selectedCutGemID);

        if (!interacted) {
            script.log(getClass().getSimpleName(), "Failed to interact with items for bolt tips. Re-polling...");
            return false;
        }

        task = "Select bolt tip option";
        // When making bolt tips, dialogue shows the bolt tip options
        DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
        if (dialogueType == DialogueType.ITEM_OPTION) {
            boolean selected = script.getWidgetManager().getDialogue().selectItem(selectedBoltTipID);
            if (!selected) {
                script.log(getClass().getSimpleName(), "Bolt tip selection failed, re-polling...");
                return false;
            }

            script.log(getClass().getSimpleName(), "Selected bolt tips to make.");
            waitUntilFinishedCrafting(selectedCutGemID, selectedBoltTipID);
            // Add small delay after crafting completes to prevent immediate re-activation
            script.pollFramesHuman(() -> false, script.random(400, 800));
        }

        return false;
    }

    private boolean interactWithItems(ItemGroupResult inventSnapshot, int gemID) {
        boolean firstIsGem = script.random(2) == 0;

        int firstID = firstIsGem ? gemID : ItemID.CHISEL;
        int secondID = firstIsGem ? ItemID.CHISEL : gemID;

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

    private void waitUntilFinishedCrafting(int consumedItemID, int producedItemID) {
        task = "Wait till done processing";
        Timer amountChangeTimer = new Timer();

        // track produced items to count individual crafts
        final int[] lastProducedCount = {getItemCount(producedItemID)};

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

            // track individual items crafted by monitoring produced item count
            int currentProducedCount = getItemCount(producedItemID);
            if (currentProducedCount > lastProducedCount[0]) {
                int newlyCrafted = currentProducedCount - lastProducedCount[0];
                craftCount += newlyCrafted;
                lastProducedCount[0] = currentProducedCount;
            }

            // check if we ran out of consumed items
            ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(consumedItemID));
            if (inventorySnapshot == null) {return false;}

            return !inventorySnapshot.contains(consumedItemID);
        };

        script.log(getClass(), "Using human task to wait until crafting finishes.");
        script.pollFramesHuman(condition, script.random(70000, 78000));
    }

    private int getItemCount(int itemID) {
        ItemGroupResult snapshot = script.getWidgetManager().getInventory().search(Set.of(itemID));
        if (snapshot == null || !snapshot.contains(itemID)) {
            return 0;
        }
        return snapshot.getAmount(itemID);
    }
}
