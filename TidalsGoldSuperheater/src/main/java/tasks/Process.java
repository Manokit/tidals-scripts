package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.ui.spellbook.SpellNotFoundException;
import com.osmb.api.ui.spellbook.StandardSpellbook;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.script.Script;

import main.TidalsGoldSuperheater;
import utils.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static main.TidalsGoldSuperheater.*;

public class Process extends Task {

    public Process(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(ItemID.GOLD_ORE));
        if (inv == null) return false;
        return inv.contains(ItemID.GOLD_ORE);
    }

    @Override
    public boolean execute() {
        if (script.getWidgetManager().getBank().isVisible()) {
            script.log(getClass(), "closing bank");
            script.getWidgetManager().getBank().close();
            script.pollFramesHuman(() -> !script.getWidgetManager().getBank().isVisible(), 3000);
            return false;
        }

        return processActiveMode();
    }

    private boolean processActiveMode() {
        task = "Superheating";

        // cache slots once, avoid re-scanning inventory between casts
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(ItemID.GOLD_ORE));
        if (inv == null || !inv.contains(ItemID.GOLD_ORE)) {
            return false;
        }

        List<Integer> slots = new ArrayList<>(inv.getSlotsForItem(ItemID.GOLD_ORE));
        if (slots.isEmpty()) {
            return false;
        }

        script.log(getClass(), "cached " + slots.size() + " slots");

        for (int slot : slots) {
            try {
                boolean selected = script.getWidgetManager().getSpellbook().selectSpell(
                    StandardSpellbook.SUPERHEAT_ITEM,
                    null
                );

                if (!selected) {
                    script.log(getClass(), "spell select failed");
                    break;
                }
            } catch (SpellNotFoundException e) {
                script.log(getClass(), "spell not found");
                script.stop();
                return false;
            }

            // tap cached slot directly
            var boundsResult = script.getWidgetManager().getInventory().getBoundsForSlot(slot);
            if (boundsResult == null || !boundsResult.isFound()) {
                script.log(getClass(), "slot bounds not found: " + slot);
                break;
            }

            Rectangle bounds = boundsResult.get();
            script.getFinger().tap(bounds);

            barsCreated++;
            double smithXp = hasGoldsmithGauntlets ? SMITHING_XP_WITH_GAUNTLETS : SMITHING_XP_NO_GAUNTLETS;
            manualSmithingXp += smithXp;

            // update custom smithing tracker for ttl calculation
            if (script instanceof TidalsGoldSuperheater) {
                ((TidalsGoldSuperheater) script).getXpTracking().addSmithingXp(smithXp);
            }

            // handle level up
            DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
            if (type == DialogueType.TAP_HERE_TO_CONTINUE) {
                script.log(getClass(), "level up");
                script.getWidgetManager().getDialogue().continueChatDialogue();
                script.submitTask(() -> 
                    script.getWidgetManager().getDialogue().getDialogueType() != DialogueType.TAP_HERE_TO_CONTINUE, 
                    2000);
            }

            // weighted delay - mostly 1200-1800 but occasionally longer
            int delay = RandomUtils.weightedRandom(1200, 3500, 0.002);

            // ~20% chance to use human delay (logs the ⏳ message)
            if (RandomUtils.uniformRandom(5) == 0) {
                script.pollFramesHuman(() -> true, delay);
            } else {
                script.submitTask(() -> true, delay);
            }
        }

        return false;
    }
}
