package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.ui.depositbox.DepositBox;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.walker.WalkConfig;
import utils.Task;
import utilities.RetryUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static main.TidalsGemMiner.*;

public class Bank extends Task {

    // all uncut gem IDs
    private static final Set<Integer> ALL_UNCUT_GEM_IDS = Set.of(
            ItemID.UNCUT_OPAL,
            ItemID.UNCUT_JADE,
            ItemID.UNCUT_RED_TOPAZ,
            ItemID.UNCUT_SAPPHIRE,
            ItemID.UNCUT_EMERALD,
            ItemID.UNCUT_DIAMOND,
            ItemID.UNCUT_DRAGONSTONE,
            ItemID.UNCUT_RUBY
    );

    // cut gem IDs (deposit these when cutting is enabled)
    private static final int[] CUT_GEM_IDS = {
            ItemID.OPAL,        // 1609
            ItemID.JADE,        // 1611
            ItemID.RED_TOPAZ,   // 1613
            ItemID.SAPPHIRE,    // 1607
            ItemID.EMERALD,     // 1605
            ItemID.RUBY,        // 1603
            ItemID.DIAMOND,     // 1601
            ItemID.DRAGONSTONE  // 1615
    };

    public Bank(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        if (!setupDone) {
            return false;
        }

        // always activate if deposit box is already open (so we can finish depositing)
        if (script.getWidgetManager().getDepositBox().isVisible()) {
            return true;
        }

        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inventory == null) {
            return false;
        }

        // cutting disabled: only bank when inventory is full
        if (!cuttingEnabled) {
            return inventory.isFull();
        }

        // cutting enabled: bank when we have cut gems and no uncut gems remaining
        // (inventory may not be full due to dropped crushed gems)
        ItemGroupResult uncutCheck = script.getWidgetManager().getInventory().search(ALL_UNCUT_GEM_IDS);
        boolean hasUncutGems = false;
        if (uncutCheck != null) {
            for (int gemId : ALL_UNCUT_GEM_IDS) {
                if (uncutCheck.contains(gemId)) {
                    hasUncutGems = true;
                    break;
                }
            }
        }

        // if we still have uncut gems, let Cut task handle them
        if (hasUncutGems) {
            return false;
        }

        // check if we have any cut gems to deposit
        ItemGroupResult cutCheck = script.getWidgetManager().getInventory().search(Set.of(
                ItemID.OPAL, ItemID.JADE, ItemID.RED_TOPAZ, ItemID.SAPPHIRE,
                ItemID.EMERALD, ItemID.RUBY, ItemID.DIAMOND, ItemID.DRAGONSTONE
        ));
        if (cutCheck == null) {
            return false;
        }

        // bank if we have cut gems (even if inventory isn't full - crushed gems were dropped)
        for (int gemId : CUT_GEM_IDS) {
            if (cutCheck.contains(gemId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean execute() {
        task = "Banking";

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) {
            return false;
        }

        WorldPosition bankPos = selectedLocation.bankPosition();

        // check if we're close enough to bank
        double distanceToBank = myPos.distanceTo(bankPos);
        if (distanceToBank > 10) {
            task = "Walking to bank";
            script.log(getClass(), "walking to bank at " + bankPos);
            script.getWalker().walkTo(bankPos, new WalkConfig.Builder().build());
            return false;
        }

        // find deposit object based on location
        String depositObjectName = selectedLocation.depositObjectName();
        String depositAction = selectedLocation.depositAction();

        DepositBox depositBox = script.getWidgetManager().getDepositBox();

        if (!depositBox.isVisible()) {
            task = "Opening deposit box";
            openDepositObject(depositObjectName, depositAction);
            return false;
        }

        // wait for deposit box to load
        script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(300, 1000, 0.002));

        task = "Depositing";
        if (cuttingEnabled) {
            // deposit cut gems only (keep chisel)
            script.log(getClass(), "depositing cut gems individually");
            depositGemsIndividually(depositBox, CUT_GEM_IDS);
        } else {
            // no chisel needed, deposit everything
            script.log(getClass(), "depositing all");
            depositBox.depositAll(Set.of());
        }

        // wait for deposit to complete
        script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(300, 1200, 0.002));

        // close deposit box
        task = "Closing deposit box";
        depositBox.close();
        script.pollFramesUntil(() -> !depositBox.isVisible(), RandomUtils.weightedRandom(5000, 10000, 0.002));

        // don't walk back - Mine task will handle walking to rocks
        return false; // re-evaluate state
    }

    private void openDepositObject(String objectName, String action) {
        script.log(getClass(), "finding " + objectName);

        RSObject depositObject = script.getObjectManager().getClosestObject(
                script.getWorldPosition(), objectName
        );

        if (depositObject == null) {
            script.log(getClass(), "no " + objectName + " found");
            return;
        }

        boolean interacted = RetryUtils.objectInteract(script, depositObject, action, objectName + " interact");
        if (!interacted) {
            script.log(getClass(), "interact failed");
            return;
        }

        // wait for deposit box to open with movement tracking
        AtomicReference<Timer> posTimer = new AtomicReference<>(new Timer());
        AtomicReference<WorldPosition> prevPos = new AtomicReference<>(null);

        script.pollFramesUntil(() -> {
            WorldPosition current = script.getWorldPosition();
            if (current == null) return false;

            if (!Objects.equals(current, prevPos.get())) {
                posTimer.get().reset();
                prevPos.set(current);
            }

            // done when deposit box visible or idle for 2+ seconds
            return script.getWidgetManager().getDepositBox().isVisible() ||
                    posTimer.get().timeElapsed() > 2000;
        }, RandomUtils.weightedRandom(15000, 30000, 0.002));
    }

    private void depositGemsIndividually(DepositBox depositBox, int[] gemIds) {
        for (int gemId : gemIds) {
            // search for this gem in deposit box
            ItemGroupResult items = depositBox.search(Set.of(gemId));
            if (items == null || !items.contains(gemId)) {
                continue;
            }

            // get a random item of this type and deposit it (all of them)
            ItemSearchResult gem = items.getRandomItem(gemId);
            if (gem == null) {
                continue;
            }

            script.log(getClass(), "depositing gem: " + gemId);
            boolean deposited = RetryUtils.inventoryInteract(script, gem, "Deposit-All", "deposit gem " + gemId);
            if (deposited) {
                script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(200, 800, 0.002));
            } else {
                script.log(getClass(), "failed to deposit gem: " + gemId);
            }
        }
    }
}
