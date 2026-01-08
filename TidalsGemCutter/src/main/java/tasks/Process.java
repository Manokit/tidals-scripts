package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.script.Script;
import utils.Task;

import java.util.Set;
import java.util.function.BooleanSupplier;

import static main.TidalsGemCutter.*;

public class Process extends Task {

    public Process(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        ItemGroupResult inv;

        // banked cut gems mode - need cut gems
        if (useBankedGems && makeBoltTips) {
            inv = script.getWidgetManager().getInventory().search(Set.of(selectedCutGemID));
            if (inv == null) return false;
            return inv.contains(selectedCutGemID);
        }

        // bolt tips mode - need uncut or cut gems
        if (makeBoltTips) {
            inv = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID, selectedCutGemID));
            if (inv == null) return false;
            return inv.contains(selectedUncutGemID) || inv.contains(selectedCutGemID);
        }

        // normal mode - need uncut gems
        inv = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID));
        if (inv == null) return false;
        return inv.contains(selectedUncutGemID);
    }

    @Override
    public boolean execute() {
        if (script.getWidgetManager().getBank().isVisible()) {
            script.log(getClass(), "closing bank");
            return script.getWidgetManager().getBank().close();
        }

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(
            Set.of(selectedUncutGemID, selectedCutGemID, ItemID.CHISEL)
        );

        if (inv == null) {
            return false;
        }

        boolean hasUncut = inv.contains(selectedUncutGemID);
        boolean hasCut = inv.contains(selectedCutGemID);

        if (makeBoltTips && hasCut) {
            return makeBoltTipsFromCutGems(inv);
        } else if (hasUncut) {
            return cutUncutGems(inv);
        }

        return false;
    }

    private boolean cutUncutGems(ItemGroupResult inv) {
        if (!script.getWidgetManager().getInventory().unSelectItemIfSelected()) {
            return false;
        }

        boolean interacted = interactWithItems(inv, selectedUncutGemID);

        if (!interacted) {
            script.log(getClass(), "interact failed");
            return false;
        }

        task = "Select gem";
        DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
        if (dialogueType == DialogueType.ITEM_OPTION) {
            boolean selected = script.getWidgetManager().getDialogue().selectItem(selectedUncutGemID);
            if (!selected) {
                script.log(getClass(), "selection failed");
                return false;
            }

            script.log(getClass(), "cutting");
            waitUntilFinishedCrafting(selectedUncutGemID, selectedCutGemID);
            script.pollFramesHuman(() -> false, script.random(400, 800));
        }

        return false;
    }

    private boolean makeBoltTipsFromCutGems(ItemGroupResult inv) {
        if (!script.getWidgetManager().getInventory().unSelectItemIfSelected()) {
            return false;
        }

        boolean interacted = interactWithItems(inv, selectedCutGemID);

        if (!interacted) {
            script.log(getClass(), "interact failed");
            return false;
        }

        task = "Select bolt tips";
        DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
        if (dialogueType == DialogueType.ITEM_OPTION) {
            boolean selected = script.getWidgetManager().getDialogue().selectItem(selectedBoltTipID);
            if (!selected) {
                script.log(getClass(), "selection failed");
                return false;
            }

            script.log(getClass(), "making bolt tips");
            waitUntilFinishedCrafting(selectedCutGemID, selectedBoltTipID);
            script.pollFramesHuman(() -> false, script.random(400, 800));
        }

        return false;
    }

    private boolean interactWithItems(ItemGroupResult inv, int gemID) {
        boolean firstIsGem = script.random(2) == 0;

        int firstID = firstIsGem ? gemID : ItemID.CHISEL;
        int secondID = firstIsGem ? ItemID.CHISEL : gemID;

        task = "Use item 1";
        if (!inv.getRandomItem(firstID).interact()) {
            script.log(getClass(), "first item failed");
            return false;
        }

        script.pollFramesUntil(() -> false, script.random(150, 300));

        task = "Use item 2";
        if (!inv.getRandomItem(secondID).interact()) {
            script.log(getClass(), "second item failed");
            return false;
        }

        BooleanSupplier condition = () -> {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            return type == DialogueType.ITEM_OPTION;
        };

        task = "Wait dialogue";
        return script.pollFramesHuman(condition, script.random(3000, 5000));
    }

    private void waitUntilFinishedCrafting(int consumedID, int producedID) {
        task = "Processing";
        Timer timer = new Timer();

        final int[] lastCount = {getItemCount(producedID)};

        BooleanSupplier condition = () -> {
            // level up
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            if (type == DialogueType.TAP_HERE_TO_CONTINUE) {
                script.log(getClass(), "level up");
                script.getWidgetManager().getDialogue().continueChatDialogue();
                script.pollFramesHuman(() -> false, script.random(1000, 3000));
                return true;
            }

            // timeout
            if (timer.timeElapsed() > script.random(70000, 78000)) {
                return true;
            }

            // track crafted items
            int currentCount = getItemCount(producedID);
            if (currentCount > lastCount[0]) {
                int crafted = currentCount - lastCount[0];
                craftCount += crafted;
                lastCount[0] = currentCount;
            }

            // check if done
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(consumedID));
            if (inv == null) return false;

            return !inv.contains(consumedID);
        };

        script.log(getClass(), "waiting for crafting");
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
