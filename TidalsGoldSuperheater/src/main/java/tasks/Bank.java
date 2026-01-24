package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.utils.RandomUtils;
import main.TidalsGoldSuperheater;
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
        task = "Banking";

        if (!script.getWidgetManager().getBank().isVisible()) {
            openBank();
            return false;
        }

        script.submitTask(() -> true, RandomUtils.weightedRandom(200, 800, 0.003));

        task = "Search bank";

        ItemGroupResult bank = script.getWidgetManager().getBank().search(Set.of(ItemID.GOLD_ORE));
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());

        if (inv == null) {
            script.log(getClass(), "inventory not visible");
            return false;
        }

        if (bank == null) {
            script.log(getClass(), "bank not ready");
            return false;
        }

        if (!bank.contains(ItemID.GOLD_ORE)) {
            script.log(getClass(), "out of ore, stopping");
            script.stop();
            return false;
        }

        int oreCount = bank.getAmount(ItemID.GOLD_ORE);
        script.log(getClass(), oreCount + " ore in bank");

        // deposit all except nats
        task = "Deposit";
        script.getWidgetManager().getBank().depositAll(Set.of(ItemID.NATURE_RUNE));
        script.submitTask(() -> true, RandomUtils.weightedRandom(200, 1000, 0.003));

        // refresh inv after deposit
        inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inv == null) {
            script.log(getClass(), "inventory gone after deposit");
            return false;
        }

        int slots = inv.getFreeSlots();
        script.log(getClass(), slots + " empty slots");

        // withdraw ore
        task = "Withdraw";
        if (!script.getWidgetManager().getBank().withdraw(ItemID.GOLD_ORE, slots)) {
            script.log(getClass(), "withdraw failed");
            return false;
        }

        task = "Close bank";
        script.getWidgetManager().getBank().close();
        script.submitTask(() -> !script.getWidgetManager().getBank().isVisible(), 3000);

        return false;
    }

    private void openBank() {
        task = "Open bank";

        List<RSObject> banks = script.getObjectManager().getObjects(TidalsGoldSuperheater.bankQuery);
        if (banks.isEmpty()) {
            script.log(getClass(), "no bank found");
            return;
        }

        RSObject bank = (RSObject) script.getUtils().getClosest(banks);
        if (!bank.interact(TidalsGoldSuperheater.BANK_ACTIONS)) {
            script.log(getClass(), "bank interact failed");
            return;
        }

        script.submitTask(() -> script.getWidgetManager().getBank().isVisible(), 10000);
    }
}
