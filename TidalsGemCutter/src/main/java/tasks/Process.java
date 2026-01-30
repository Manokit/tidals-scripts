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

    private enum ProcessState { IDLE, CRAFTING }
    private ProcessState state = ProcessState.IDLE;

    // crafting tracking state
    private int craftingConsumedID;
    private int craftingProducedID;
    private boolean craftingIsCrushable;
    private Timer craftingTimer;
    private int craftingTimeout;
    private int lastProducedCount;
    private int lastCrushedCount;

    public Process(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // stay active while crafting
        if (state == ProcessState.CRAFTING) return true;

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
        // state: crafting in progress - check each poll
        if (state == ProcessState.CRAFTING) {
            return pollCrafting();
        }

        // state: IDLE - start a new crafting batch
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
            return startCrafting(inv, selectedCutGemID, selectedBoltTipID, "making bolt tips");
        } else if (hasUncut) {
            return startCrafting(inv, selectedUncutGemID, selectedCutGemID, "cutting");
        }

        return false;
    }

    private boolean startCrafting(ItemGroupResult inv, int consumedID, int producedID, String logMsg) {
        if (!script.getWidgetManager().getInventory().unSelectItemIfSelected()) {
            return false;
        }

        boolean interacted = interactWithItems(inv, consumedID == selectedCutGemID ? selectedCutGemID : consumedID);

        if (!interacted) {
            script.log(getClass(), "interact failed");
            return false;
        }

        task = "Select item";
        DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
        if (dialogueType == DialogueType.ITEM_OPTION) {
            int selectID = (consumedID == selectedCutGemID && makeBoltTips) ? selectedBoltTipID : consumedID;
            boolean selected = script.getWidgetManager().getDialogue().selectItem(selectID);
            if (!selected) {
                script.log(getClass(), "selection failed");
                return false;
            }

            // transition to crafting state
            script.log(getClass(), logMsg);
            craftingConsumedID = consumedID;
            craftingProducedID = producedID;
            craftingIsCrushable = CRUSHABLE_GEMS.contains(consumedID);
            craftingTimer = new Timer();
            craftingTimeout = RandomUtils.gaussianRandom(70000, 80000, 74000, 2000);
            lastProducedCount = getItemCount(producedID);
            lastCrushedCount = craftingIsCrushable ? getItemCount(CRUSHED_GEM_ID) : 0;
            state = ProcessState.CRAFTING;
            task = "Processing";
            return true; // yield - check crafting progress next poll
        }

        return false;
    }

    // called each poll while crafting - one check per frame, then yield
    private boolean pollCrafting() {
        task = "Processing";

        // level up interrupts crafting
        DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
        if (type == DialogueType.TAP_HERE_TO_CONTINUE) {
            script.log(getClass(), "level up");
            script.getWidgetManager().getDialogue().continueChatDialogue();
            state = ProcessState.IDLE;
            return true; // re-poll to restart crafting
        }

        // timeout
        if (craftingTimer.timeElapsed() > craftingTimeout) {
            script.log(getClass(), "crafting timeout");
            state = ProcessState.IDLE;
            return false;
        }

        // track crafted items
        int currentCount = getItemCount(craftingProducedID);
        if (currentCount > lastProducedCount) {
            int crafted = currentCount - lastProducedCount;
            craftCount += crafted;
            lastProducedCount = currentCount;
        }

        // track crushed gems
        if (craftingIsCrushable) {
            int currentCrushed = getItemCount(CRUSHED_GEM_ID);
            if (currentCrushed > lastCrushedCount) {
                int newCrushed = currentCrushed - lastCrushedCount;
                crushedCount += newCrushed;
                lastCrushedCount = currentCrushed;
                script.log(getClass(), "gem crushed! total crushed: " + crushedCount);
            }
        }

        // check if done - no more consumable items
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(craftingConsumedID));
        if (inv == null || !inv.contains(craftingConsumedID)) {
            script.log(getClass(), "crafting complete");
            state = ProcessState.IDLE;
            return false; // done, let executor re-evaluate (probably bank next)
        }

        // still crafting - yield back to executor
        return true;
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

    private int getItemCount(int itemID) {
        ItemGroupResult snapshot = script.getWidgetManager().getInventory().search(Set.of(itemID));
        if (snapshot == null || !snapshot.contains(itemID)) {
            return 0;
        }
        return snapshot.getAmount(itemID);
    }
}
