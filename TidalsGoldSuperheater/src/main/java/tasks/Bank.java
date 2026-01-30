package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.utils.RandomUtils;
import main.TidalsGoldSuperheater;
import utilities.MovementChecker;
import utils.Task;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static main.TidalsGoldSuperheater.*;

public class Bank extends Task {

    public Bank(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(ItemID.GOLD_ORE));
        if (inv == null) return false;
        return !inv.contains(ItemID.GOLD_ORE);
    }

    @Override
    public boolean execute() {
        // state: bank not visible? open it
        if (!script.getWidgetManager().getBank().isVisible()) {
            openBank();
            return false;
        }

        // state: have items to deposit? deposit them
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(ItemID.GOLD_BAR));
        if (inv != null && inv.contains(ItemID.GOLD_BAR)) {
            task = "Deposit";
            script.log(getClass(), "[deposit] depositing bars");
            script.getWidgetManager().getBank().depositAll(Set.of(ItemID.NATURE_RUNE));
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(150, 400, 0.003));
            return false;
        }

        // state: need to check ore supply and withdraw
        task = "Search bank";
        ItemGroupResult bank = script.getWidgetManager().getBank().search(Set.of(ItemID.GOLD_ORE));
        if (bank == null) {
            script.log(getClass(), "[execute] bank not ready");
            return false;
        }

        if (!bank.contains(ItemID.GOLD_ORE)) {
            script.log(getClass(), "[execute] out of ore, stopping");
            script.stop();
            return false;
        }

        int oreCount = bank.getAmount(ItemID.GOLD_ORE);
        script.log(getClass(), "[execute] " + oreCount + " ore in bank");

        // state: need to withdraw ore
        inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inv == null) {
            script.log(getClass(), "[execute] inventory not visible");
            return false;
        }

        int slots = inv.getFreeSlots();
        if (slots > 0) {
            task = "Withdraw";
            script.log(getClass(), "[withdraw] withdrawing " + slots + " ore");
            if (!script.getWidgetManager().getBank().withdraw(ItemID.GOLD_ORE, slots)) {
                script.log(getClass(), "[withdraw] withdraw failed");
                return false;
            }
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(150, 400, 0.003));
            return false;
        }

        // state: done banking, close and let Process task take over
        task = "Close bank";
        script.log(getClass(), "[close] inventory full, closing bank");
        script.getWidgetManager().getBank().close();
        script.pollFramesUntil(() -> !script.getWidgetManager().getBank().isVisible(), RandomUtils.weightedRandom(800, 1500, 0.003));
        return false;
    }

    private void openBank() {
        task = "Open bank";

        List<RSObject> banks = script.getObjectManager().getObjects(TidalsGoldSuperheater.bankQuery);
        if (banks.isEmpty()) {
            script.log(getClass(), "[openBank] no bank found");
            return;
        }

        RSObject bank = (RSObject) script.getUtils().getClosest(banks);
        if (!bank.interact(TidalsGoldSuperheater.BANK_ACTIONS)) {
            script.log(getClass(), "[openBank] bank interact failed");
            return;
        }

        // detect misclicks/interrupts instead of blindly waiting
        WorldPosition pos = script.getWorldPosition();
        MovementChecker movementChecker = new MovementChecker(pos != null ? pos : new WorldPosition(0, 0, 0));
        script.pollFramesUntil(() -> {
            if (script.getWidgetManager().getBank().isVisible()) return true;
            WorldPosition currentPos = script.getWorldPosition();
            if (currentPos == null) return false;
            return movementChecker.hasTimedOut(currentPos);
        }, RandomUtils.weightedRandom(10000, 15000, 0.002));
    }
}
