package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.utils.RandomUtils;
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
            // waitUntilFinishedCrafting already has built-in delay, no extra needed
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
            // waitUntilFinishedCrafting already has built-in delay, no extra needed
        }

        return false;
    }

    private boolean interactWithItems(ItemGroupResult inv, int gemID) {
        boolean firstIsGem = RandomUtils.uniformRandom(2) == 0;

        int firstID = firstIsGem ? gemID : ItemID.CHISEL;
        int secondID = firstIsGem ? ItemID.CHISEL : gemID;

        task = "Use item 1";
        ItemSearchResult firstItem = inv.getRandomItem(firstID);
        if (firstItem == null || !firstItem.interact()) {
            script.log(getClass(), "first item failed");
            return false;
        }
        // interact() has built-in humanized delay, no extra delay needed

        task = "Use item 2";
        ItemSearchResult secondItem = inv.getRandomItem(secondID);
        if (secondItem == null || !secondItem.interact()) {
            script.log(getClass(), "second item failed");
            return false;
        }

        BooleanSupplier condition = () -> {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            return type == DialogueType.ITEM_OPTION;
        };

        task = "Wait dialogue";
        return script.pollFramesHuman(condition, RandomUtils.gaussianRandom(3000, 6000, 4000, 800));
    }

    private void waitUntilFinishedCrafting(int consumedID, int producedID) {
        task = "Processing";
        Timer timer = new Timer();
        int timeout = RandomUtils.gaussianRandom(70000, 80000, 74000, 2000);

        // check if this gem can crush
        boolean isCrushable = CRUSHABLE_GEMS.contains(consumedID);

        final int[] lastCount = {getItemCount(producedID)};
        final int[] lastCrushed = {isCrushable ? getItemCount(CRUSHED_GEM_ID) : 0};

        BooleanSupplier condition = () -> {
            // level up
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            if (type == DialogueType.TAP_HERE_TO_CONTINUE) {
                script.log(getClass(), "level up");
                script.getWidgetManager().getDialogue().continueChatDialogue();
                script.pollFramesHuman(() -> true, RandomUtils.gaussianRandom(1000, 3500, 1500, 500));
                return true;
            }

            // timeout
            if (timer.timeElapsed() > timeout) {
                return true;
            }

            // track crafted items
            int currentCount = getItemCount(producedID);
            if (currentCount > lastCount[0]) {
                int crafted = currentCount - lastCount[0];
                craftCount += crafted;
                lastCount[0] = currentCount;
            }

            // track crushed gems for semi-precious
            if (isCrushable) {
                int currentCrushed = getItemCount(CRUSHED_GEM_ID);
                if (currentCrushed > lastCrushed[0]) {
                    int newCrushed = currentCrushed - lastCrushed[0];
                    crushedCount += newCrushed;
                    lastCrushed[0] = currentCrushed;
                    script.log(getClass(), "gem crushed! total crushed: " + crushedCount);
                }
            }

            // check if done
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(consumedID));
            if (inv == null) return false;

            return !inv.contains(consumedID);
        };

        script.log(getClass(), "waiting for crafting");
        script.pollFramesHuman(condition, timeout);
    }

    private int getItemCount(int itemID) {
        ItemGroupResult snapshot = script.getWidgetManager().getInventory().search(Set.of(itemID));
        if (snapshot == null || !snapshot.contains(itemID)) {
            return 0;
        }
        return snapshot.getAmount(itemID);
    }
}
