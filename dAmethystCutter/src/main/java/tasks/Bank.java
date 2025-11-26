package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.utils.timing.Timer;
import main.dAmethystCutter;
import utils.Task;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static main.dAmethystCutter.*;

public class Bank extends Task {

    public Bank(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(ItemID.AMETHYST));
        if (inventorySnapshot == null) {
            // Inventory not visible
            return false;
        }

        return !inventorySnapshot.contains(ItemID.AMETHYST);
    }

    @Override
    public boolean execute() {
        task = getClass().getSimpleName();
        if (!script.getWidgetManager().getBank().isVisible()) {
            openBank();
            return false;
        }

        task = "Get bank snapshot";
        ItemGroupResult bankSnapshot = script.getWidgetManager().getBank().search(Set.of(ItemID.AMETHYST));
        ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(Collections.emptySet());

        if (inventorySnapshot == null) {
            // Inventory not visible
            return false;
        }
        if (bankSnapshot == null) {
            // Bank not visible
            return false;
        }

        int emptySlots = inventorySnapshot.getFreeSlots();

        task = "Check bank items";
        if (!bankSnapshot.contains(ItemID.AMETHYST)) {
            script.log(getClass(), "Ran out of Amethyst. Stopping script.");
            script.stop();
            return false;
        }

        task = "Withdraw items";
        if (!script.getWidgetManager().getBank().withdraw(ItemID.AMETHYST, emptySlots)) {
            script.log(getClass(), "Withdraw failed for amethyst, re-polling.");
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

        List<RSObject> banksFound = script.getObjectManager().getObjects(dAmethystCutter.bankQuery);
        if (banksFound.isEmpty()) {
            script.log(getClass(), "No bank objects found.");
            return;
        }

        RSObject bank = (RSObject) script.getUtils().getClosest(banksFound);
        if (!bank.interact(dAmethystCutter.BANK_ACTIONS)) {
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
