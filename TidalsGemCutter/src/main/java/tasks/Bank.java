package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.utils.RandomUtils;
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
        ItemGroupResult inv;

        // banked cut gems mode - bank when no cut gems
        if (useBankedGems && makeBoltTips) {
            inv = script.getWidgetManager().getInventory().search(Set.of(selectedCutGemID, selectedBoltTipID));
            if (inv == null) return false;
            return !inv.contains(selectedCutGemID);
        }

        // bolt tips mode - bank when have tips and no gems
        if (makeBoltTips) {
            inv = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID, selectedCutGemID, selectedBoltTipID));
            if (inv == null) return false;
            boolean hasTips = inv.contains(selectedBoltTipID);
            boolean hasUncut = inv.contains(selectedUncutGemID);
            boolean hasCut = inv.contains(selectedCutGemID);
            return (hasTips && !hasUncut && !hasCut) || (!hasUncut && !hasCut && !hasTips);
        }

        // normal mode - bank when no uncut gems
        inv = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID));
        if (inv == null) return false;
        return !inv.contains(selectedUncutGemID);
    }

    @Override
    public boolean execute() {
        task = "Banking";

        // state: bank not visible? open it
        if (!script.getWidgetManager().getBank().isVisible()) {
            openBank();
            return false;
        }

        // determine what items we're working with
        int itemToWithdraw = (useBankedGems && makeBoltTips) ? selectedCutGemID : selectedUncutGemID;
        String itemName = script.getItemManager().getItemName(itemToWithdraw);

        // state: have items to deposit? deposit them (keep chisel)
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inv == null) {
            script.log(getClass(), "inventory not visible");
            return false;
        }

        // check if we have anything besides chisel to deposit
        boolean hasItemsToDeposit = inv.getFreeSlots() < 27; // 27 = full minus chisel
        if (hasItemsToDeposit) {
            task = "Deposit";
            script.getWidgetManager().getBank().depositAll(Set.of(ItemID.CHISEL));
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(300, 800, 0.002));
            return false;
        }

        // state: check if bank has items we need
        task = "Check bank";
        ItemGroupResult bankResult = script.getWidgetManager().getBank().search(Set.of(itemToWithdraw));
        if (bankResult == null) {
            script.log(getClass(), "bank not ready");
            return false;
        }

        if (!bankResult.contains(itemToWithdraw)) {
            if (allUncutsMode) {
                script.log(getClass(), "out of " + itemName + ", trying next gem type");
                if (((TidalsGemCutter) script).advanceToNextGem()) {
                    return false;
                }
                script.log(getClass(), "no more uncut gems in bank, stopping");
            } else {
                script.log(getClass(), "out of " + itemName + ", stopping");
            }
            script.stop();
            return false;
        }

        int bankCount = bankResult.getAmount(itemToWithdraw);
        script.log(getClass(), bankCount + " " + itemName + " in bank");

        // state: need to withdraw? withdraw items
        int slots = inv.getFreeSlots();
        if (slots > 0) {
            task = "Withdraw";
            if (!script.getWidgetManager().getBank().withdraw(itemToWithdraw, slots)) {
                script.log(getClass(), "withdraw failed");
                return false;
            }
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(200, 600, 0.002));
            return false;
        }

        // state: done banking, close and let other tasks take over
        task = "Close bank";
        script.getWidgetManager().getBank().close();
        script.pollFramesUntil(() -> !script.getWidgetManager().getBank().isVisible(), RandomUtils.weightedRandom(2500, 4000, 0.002));
        return false;
    }

    private void openBank() {
        task = "Open bank";
        script.log(getClass(), "finding bank");

        List<RSObject> banks = script.getObjectManager().getObjects(TidalsGemCutter.bankQuery);
        if (banks.isEmpty()) {
            script.log(getClass(), "no bank found");
            return;
        }

        RSObject bank = (RSObject) script.getUtils().getClosest(banks);
        if (!bank.interact(TidalsGemCutter.BANK_ACTIONS)) {
            script.log(getClass(), "bank interact failed");
            return;
        }

        AtomicReference<Timer> posTimer = new AtomicReference<>(new Timer());
        AtomicReference<WorldPosition> prevPos = new AtomicReference<>(null);
        final long idleTimeout = RandomUtils.weightedRandom(1500, 2500, 0.002);

        script.pollFramesHuman(() -> {
            WorldPosition current = script.getWorldPosition();
            if (current == null) return false;

            if (!Objects.equals(current, prevPos.get())) {
                posTimer.get().reset();
                prevPos.set(current);
            }

            return script.getWidgetManager().getBank().isVisible() || posTimer.get().timeElapsed() > idleTimeout;
        }, RandomUtils.weightedRandom(12000, 18000, 0.002));
    }
}
