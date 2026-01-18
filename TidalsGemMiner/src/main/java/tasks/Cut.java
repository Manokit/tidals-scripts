package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import static main.TidalsGemMiner.*;
import main.TidalsGemMiner;

public class Cut extends Task {

    // uncut gem IDs with crafting level requirements
    private static final Map<Integer, Integer> GEM_LEVEL_REQUIREMENTS = Map.of(
            ItemID.UNCUT_OPAL, 1,
            ItemID.UNCUT_JADE, 13,
            ItemID.UNCUT_RED_TOPAZ, 16,
            ItemID.UNCUT_SAPPHIRE, 20,
            ItemID.UNCUT_EMERALD, 27,
            ItemID.UNCUT_DIAMOND, 43,
            ItemID.UNCUT_DRAGONSTONE, 55,
            ItemID.UNCUT_RUBY, 63
    );

    // crafting XP per gem type
    private static final Map<Integer, Double> GEM_CRAFTING_XP = Map.of(
            ItemID.UNCUT_OPAL, 15.0,
            ItemID.UNCUT_JADE, 20.0,
            ItemID.UNCUT_RED_TOPAZ, 25.0,
            ItemID.UNCUT_SAPPHIRE, 50.0,
            ItemID.UNCUT_EMERALD, 67.5,
            ItemID.UNCUT_DIAMOND, 107.5,
            ItemID.UNCUT_DRAGONSTONE, 137.5,
            ItemID.UNCUT_RUBY, 85.0
    );

    // sorted list of gems by level (highest first) for priority cutting
    private static final int[] GEMS_BY_LEVEL_DESC = {
            ItemID.UNCUT_RUBY,        // 63
            ItemID.UNCUT_DRAGONSTONE, // 55
            ItemID.UNCUT_DIAMOND,     // 43
            ItemID.UNCUT_EMERALD,     // 27
            ItemID.UNCUT_SAPPHIRE,    // 20
            ItemID.UNCUT_RED_TOPAZ,   // 16
            ItemID.UNCUT_JADE,        // 13
            ItemID.UNCUT_OPAL         // 1
    };

    // all uncut gem IDs for inventory search
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

    private static final int CRUSHED_GEM_ID = 1633;
    private static final int CHISEL_ID = ItemID.CHISEL;

    public Cut(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // don't activate if deposit box is open
        if (script.getWidgetManager().getDepositBox().isVisible()) {
            return false;
        }
        // only activate when: setup done, cutting enabled
        if (!setupDone || !cuttingEnabled) {
            return false;
        }

        // ONLY activate when inventory is FULL
        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(Collections.emptySet());
        if (inventory == null || !inventory.isFull()) {
            return false;
        }

        // must have uncut gems to cut
        return hasUncutGems();
    }

    @Override
    public boolean execute() {
        task = "Cutting gems";

        // walk to bank area first before cutting
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) {
            return false;
        }

        WorldPosition bankPos = selectedLocation.bankPosition();
        double distanceToBank = myPos.distanceTo(bankPos);
        if (distanceToBank > 10) {
            task = "Walking to bank to cut";
            script.log(getClass(), "walking to bank area before cutting");
            script.getWalker().walkTo(bankPos, new WalkConfig.Builder().build());
            return false;
        }

        int craftingLevel = getCraftingLevel();
        script.log(getClass(), "crafting level: " + craftingLevel);

        // cut ALL gem types, not just one
        boolean cutAnyGems = false;
        while (true) {
            int targetGemId = findBestUncutGem(craftingLevel);
            if (targetGemId == -1) {
                script.log(getClass(), "no more cuttable uncut gems");
                break;
            }

            // unselect any selected item first
            if (!script.getWidgetManager().getInventory().unSelectItemIfSelected()) {
                break;
            }

            // interact with items (chisel and gem)
            boolean interacted = interactWithItems(targetGemId);
            if (!interacted) {
                script.log(getClass(), "interact failed");
                break;
            }

            task = "Select gem";
            DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
            if (dialogueType == DialogueType.ITEM_OPTION) {
                // CRITICAL: select the UNCUT gem ID in dialogue (not the cut gem)
                boolean selected = script.getWidgetManager().getDialogue().selectItem(targetGemId);
                if (!selected) {
                    script.log(getClass(), "failed to select gem in dialogue");
                    break;
                }

                script.log(getClass(), "cutting gems of type: " + targetGemId);
                waitUntilFinishedCrafting(targetGemId);
                script.pollFramesHuman(() -> false, script.random(400, 800));
                cutAnyGems = true;
            }
        }

        // only drop crushed gems after ALL cutting is done
        if (cutAnyGems) {
            dropCrushedGems();
        }

        return false; // re-evaluate state - Bank task will handle depositing
    }

    private int getCraftingLevel() {
        Map<SkillType, XPTracker> trackers = script.getXPTrackers();
        if (trackers == null) {
            return 1;
        }
        XPTracker tracker = trackers.get(SkillType.CRAFTING);
        if (tracker == null) {
            return 1;
        }
        return tracker.getLevel();
    }

    private boolean hasUncutGems() {
        int craftingLevel = getCraftingLevel();
        return findBestUncutGem(craftingLevel) != -1;
    }

    private int findBestUncutGem(int craftingLevel) {
        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(ALL_UNCUT_GEM_IDS);
        if (inventory == null) {
            return -1;
        }

        // iterate through gems from highest level to lowest
        for (int gemId : GEMS_BY_LEVEL_DESC) {
            int requiredLevel = GEM_LEVEL_REQUIREMENTS.getOrDefault(gemId, 99);
            if (craftingLevel >= requiredLevel && inventory.contains(gemId)) {
                return gemId;
            }
        }

        return -1;
    }

    private boolean interactWithItems(int gemID) {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(gemID, CHISEL_ID));
        if (inv == null) {
            return false;
        }

        // randomize which item to click first (chisel or gem)
        boolean firstIsGem = script.random(2) == 0;

        int firstID = firstIsGem ? gemID : CHISEL_ID;
        int secondID = firstIsGem ? CHISEL_ID : gemID;

        task = "Use item 1";
        if (!inv.getRandomItem(firstID).interact()) {
            script.log(getClass(), "first item failed");
            return false;
        }

        script.pollFramesUntil(() -> false, script.random(150, 300), true);

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

    private void waitUntilFinishedCrafting(int consumedID) {
        task = "Cutting";
        Timer timer = new Timer();

        final int[] lastCount = {countGemsInInventory(consumedID)};

        BooleanSupplier condition = () -> {
            // level up handling
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

            // track crafted items and XP
            int currentCount = countGemsInInventory(consumedID);
            if (currentCount < lastCount[0]) {
                int crafted = lastCount[0] - currentCount;
                gemsCut += crafted;
                // add crafting XP for each gem cut
                if (TidalsGemMiner.xpTracking != null) {
                    double xpPerGem = GEM_CRAFTING_XP.getOrDefault(consumedID, 50.0);
                    TidalsGemMiner.xpTracking.addCraftingXp(xpPerGem * crafted);
                }
                lastCount[0] = currentCount;
            }

            // check if done (no more uncut gems of this type)
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(consumedID));
            if (inv == null) return false;

            return !inv.contains(consumedID);
        };

        script.log(getClass(), "waiting for cutting to finish");
        script.pollFramesHuman(condition, script.random(70000, 78000));
    }

    private int countGemsInInventory(int gemId) {
        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(Set.of(gemId));
        if (inventory == null) {
            return 0;
        }
        return inventory.getAmount(gemId);
    }

    private void dropCrushedGems() {
        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(Set.of(CRUSHED_GEM_ID));
        if (inventory == null || !inventory.contains(CRUSHED_GEM_ID)) {
            return;
        }

        task = "Dropping crushed gems";
        script.log(getClass(), "dropping crushed gems");

        // drop all crushed gems
        while (true) {
            inventory = script.getWidgetManager().getInventory().search(Set.of(CRUSHED_GEM_ID));
            if (inventory == null || !inventory.contains(CRUSHED_GEM_ID)) {
                break;
            }

            boolean dropped = inventory.getRandomItem(CRUSHED_GEM_ID).interact("Drop");
            if (dropped) {
                script.log(getClass(), "dropped crushed gem");
                // brief wait between drops
                script.pollFramesUntil(() -> false, script.random(100, 200), true);
            } else {
                script.log(getClass(), "failed to drop crushed gem");
                break;
            }
        }
    }
}
