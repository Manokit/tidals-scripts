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
        if (!script.getWidgetManager().getBank().isVisible()) {
            openBank();
            return false;
        }

        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 1200, 0.002));

        task = "Search bank";

        // determine what to withdraw
        int itemToWithdraw;
        String itemName;

        if (useBankedGems && makeBoltTips) {
            itemToWithdraw = selectedCutGemID;
            itemName = script.getItemManager().getItemName(selectedCutGemID);
        } else {
            itemToWithdraw = selectedUncutGemID;
            itemName = script.getItemManager().getItemName(selectedUncutGemID);
        }

        script.log(getClass(), "looking for " + itemName);

        ItemGroupResult bank = script.getWidgetManager().getBank().search(Set.of(itemToWithdraw));
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());

        if (inv == null) {
            script.log(getClass(), "inventory not visible");
            return false;
        }
        if (bank == null) {
            script.log(getClass(), "bank not ready");
            return false;
        }

        task = "Check bank";

        if (bank.contains(itemToWithdraw)) {
            int count = bank.getAmount(itemToWithdraw);
            script.log(getClass(), count + " " + itemName + " in bank");
        } else {
            script.log(getClass(), "out of " + itemName + ", stopping");
            script.stop();
            return false;
        }

        // deposit all except chisel
        task = "Deposit";
        script.getWidgetManager().getBank().depositAll(Set.of(ItemID.CHISEL));
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 1500, 0.002));

        // refresh inv after deposit
        inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inv == null) {
            script.log(getClass(), "inventory gone after deposit");
            return false;
        }

        int slots = inv.getFreeSlots();
        script.log(getClass(), slots + " empty slots");

        // withdraw
        task = "Withdraw";
        if (!script.getWidgetManager().getBank().withdraw(itemToWithdraw, slots)) {
            script.log(getClass(), "withdraw failed");
            return false;
        }

        task = "Close bank";
        script.getWidgetManager().getBank().close();
        script.pollFramesHuman(() -> !script.getWidgetManager().getBank().isVisible(), 5000);
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

        script.pollFramesHuman(() -> {
            WorldPosition current = script.getWorldPosition();
            if (current == null) return false;

            if (!Objects.equals(current, prevPos.get())) {
                posTimer.get().reset();
                prevPos.set(current);
            }

            return script.getWidgetManager().getBank().isVisible() || posTimer.get().timeElapsed() > 2000;
        }, 15000);
    }
}
