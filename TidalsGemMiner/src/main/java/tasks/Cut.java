package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.walker.WalkConfig;
import utils.Task;
import utilities.RetryUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import static main.TidalsGemMiner.*;
import main.TidalsGemMiner;

public class Cut extends Task {

    // cutting states - one action per poll
    private enum CutState {
        IDLE,              // need to find gem to cut
        USING_ITEMS,       // using chisel on gem
        WAIT_DIALOGUE,     // waiting for item dialogue
        SELECT_GEM,        // selecting gem in dialogue
        CUTTING,           // actively cutting
        DROP_CRUSHED       // dropping crushed gems
    }

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

    // state tracking
    private CutState currentState = CutState.IDLE;
    private int currentGemId = -1;
    private int lastGemCount = 0;
    private Timer cuttingTimer;

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

        // state machine - one action per poll
        switch (currentState) {
            case IDLE:
                return handleIdle();
            case USING_ITEMS:
                return handleUsingItems();
            case WAIT_DIALOGUE:
                return handleWaitDialogue();
            case SELECT_GEM:
                return handleSelectGem();
            case CUTTING:
                return handleCutting();
            case DROP_CRUSHED:
                return handleDropCrushed();
            default:
                currentState = CutState.IDLE;
                return false;
        }
    }

    private boolean handleIdle() {
        int craftingLevel = getCraftingLevel();
        int targetGemId = findBestUncutGem(craftingLevel);

        if (targetGemId == -1) {
            // no uncut gems - check for crushed gems to drop
            if (hasCrushedGems()) {
                currentState = CutState.DROP_CRUSHED;
                return false;
            }
            // nothing to do - task will deactivate
            return false;
        }

        // unselect any selected item first
        if (!script.getWidgetManager().getInventory().unSelectItemIfSelected()) {
            return false;
        }

        currentGemId = targetGemId;
        currentState = CutState.USING_ITEMS;
        script.log(getClass(), "[idle] found gem to cut: " + currentGemId);
        return false;
    }

    private boolean handleUsingItems() {
        task = "Using chisel";

        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(currentGemId, CHISEL_ID));
        if (inv == null) {
            script.log(getClass(), "[using_items] inventory null");
            currentState = CutState.IDLE;
            return false;
        }

        // randomize which item to click first (chisel or gem)
        boolean firstIsGem = RandomUtils.uniformRandom(0, 1) == 0;
        int firstID = firstIsGem ? currentGemId : CHISEL_ID;
        int secondID = firstIsGem ? CHISEL_ID : currentGemId;

        ItemSearchResult firstItem = inv.getRandomItem(firstID);
        if (firstItem == null) {
            script.log(getClass(), "[using_items] first item not found");
            currentState = CutState.IDLE;
            return false;
        }

        if (!RetryUtils.inventoryInteract(script, firstItem, "Use", "use first item")) {
            script.log(getClass(), "[using_items] first item interaction failed");
            currentState = CutState.IDLE;
            return false;
        }

        script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(150, 600, 0.002));

        ItemSearchResult secondItem = inv.getRandomItem(secondID);
        if (secondItem == null) {
            script.log(getClass(), "[using_items] second item not found");
            currentState = CutState.IDLE;
            return false;
        }

        if (!RetryUtils.inventoryInteract(script, secondItem, "Use", "use second item")) {
            script.log(getClass(), "[using_items] second item interaction failed");
            currentState = CutState.IDLE;
            return false;
        }

        currentState = CutState.WAIT_DIALOGUE;
        return false;
    }

    private boolean handleWaitDialogue() {
        task = "Waiting for dialogue";

        BooleanSupplier condition = () -> {
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            return type == DialogueType.ITEM_OPTION;
        };

        boolean dialogueOpened = script.pollFramesUntil(condition, RandomUtils.weightedRandom(3000, 6000, 0.002));

        if (dialogueOpened) {
            currentState = CutState.SELECT_GEM;
        } else {
            script.log(getClass(), "[wait_dialogue] dialogue didn't open, retrying");
            currentState = CutState.IDLE;
        }
        return false;
    }

    private boolean handleSelectGem() {
        task = "Selecting gem";

        DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
        if (dialogueType != DialogueType.ITEM_OPTION) {
            script.log(getClass(), "[select_gem] dialogue not visible, resetting");
            currentState = CutState.IDLE;
            return false;
        }

        // CRITICAL: select the UNCUT gem ID in dialogue (not the cut gem)
        boolean selected = script.getWidgetManager().getDialogue().selectItem(currentGemId);
        if (!selected) {
            script.log(getClass(), "[select_gem] failed to select gem in dialogue");
            currentState = CutState.IDLE;
            return false;
        }

        script.log(getClass(), "[select_gem] started cutting gems of type: " + currentGemId);
        lastGemCount = countGemsInInventory(currentGemId);
        cuttingTimer = new Timer();
        currentState = CutState.CUTTING;
        return false;
    }

    private boolean handleCutting() {
        task = "Cutting";

        // handle level up dialogue
        DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
        if (type == DialogueType.TAP_HERE_TO_CONTINUE) {
            script.log(getClass(), "[cutting] level up detected");
            script.getWidgetManager().getDialogue().continueChatDialogue();
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(1000, 3000, 0.002));
            return false;
        }

        // track crafted items and XP
        int currentCount = countGemsInInventory(currentGemId);
        if (currentCount < lastGemCount) {
            int crafted = lastGemCount - currentCount;
            gemsCut += crafted;
            if (TidalsGemMiner.xpTracking != null) {
                double xpPerGem = GEM_CRAFTING_XP.getOrDefault(currentGemId, 50.0);
                TidalsGemMiner.xpTracking.addCraftingXp(xpPerGem * crafted);
            }
            lastGemCount = currentCount;
        }

        // check if done cutting this gem type
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(currentGemId));
        boolean noMoreGems = inv == null || !inv.contains(currentGemId);

        // timeout check
        boolean timedOut = cuttingTimer != null && cuttingTimer.timeElapsed() > RandomUtils.gaussianRandom(70000, 78000, 74000, 2000);

        if (noMoreGems || timedOut) {
            script.log(getClass(), "[cutting] finished cutting gem type: " + currentGemId);
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(400, 1600, 0.002));
            currentState = CutState.IDLE; // check for more gem types
            return false;
        }

        // still cutting - wait a bit then re-check
        script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(600, 1200, 0.002));
        return false;
    }

    private boolean handleDropCrushed() {
        task = "Dropping crushed gem";

        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(Set.of(CRUSHED_GEM_ID));
        if (inventory == null || !inventory.contains(CRUSHED_GEM_ID)) {
            script.log(getClass(), "[drop_crushed] no more crushed gems");
            currentState = CutState.IDLE;
            return false;
        }

        ItemSearchResult crushedGem = inventory.getRandomItem(CRUSHED_GEM_ID);
        if (crushedGem == null) {
            currentState = CutState.IDLE;
            return false;
        }

        boolean dropped = RetryUtils.inventoryInteract(script, crushedGem, "Drop", "drop crushed gem");
        if (dropped) {
            script.log(getClass(), "[drop_crushed] dropped one crushed gem");
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(100, 400, 0.002));
        }
        // stay in DROP_CRUSHED state to drop more, or handleDropCrushed will transition to IDLE
        return false;
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

    private int countGemsInInventory(int gemId) {
        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(Set.of(gemId));
        if (inventory == null) {
            return 0;
        }
        return inventory.getAmount(gemId);
    }

    private boolean hasCrushedGems() {
        ItemGroupResult inventory = script.getWidgetManager().getInventory().search(Set.of(CRUSHED_GEM_ID));
        return inventory != null && inventory.contains(CRUSHED_GEM_ID);
    }
}
