package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.ui.spellbook.SpellNotFoundException;
import com.osmb.api.ui.spellbook.StandardSpellbook;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.script.Script;

import main.TidalsGoldSuperheater;
import utils.Task;

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
        // state: bank open? close it first
        if (script.getWidgetManager().getBank().isVisible()) {
            script.log(getClass(), "[execute] closing bank");
            script.getWidgetManager().getBank().close();
            script.pollFramesUntil(() -> !script.getWidgetManager().getBank().isVisible(), RandomUtils.weightedRandom(800, 1500, 0.003));
            return false;
        }

        // state: level up dialogue? handle it
        DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
        if (dialogueType == DialogueType.TAP_HERE_TO_CONTINUE) {
            script.log(getClass(), "[execute] handling level up");
            script.getWidgetManager().getDialogue().continueChatDialogue();
            script.pollFramesUntil(() ->
                script.getWidgetManager().getDialogue().getDialogueType() != DialogueType.TAP_HERE_TO_CONTINUE,
                RandomUtils.weightedRandom(600, 1200, 0.003));
            return false;
        }

        // state: ready to superheat - process ONE ore per poll
        return superheatOneOre();
    }

    /**
     * Superheats exactly one gold ore, then returns to let framework re-evaluate.
     * This allows higher priority tasks to interrupt between casts.
     */
    private boolean superheatOneOre() {
        task = "Superheating";

        // find a gold ore to superheat
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(ItemID.GOLD_ORE));
        if (inv == null || !inv.contains(ItemID.GOLD_ORE)) {
            return false;
        }

        // track ore count before casting for verification
        int oreBefore = inv.getAmount(ItemID.GOLD_ORE);

        ItemSearchResult oreItem = inv.getRandomItem(ItemID.GOLD_ORE);
        if (oreItem == null) {
            script.log(getClass(), "[superheat] no ore found");
            return false;
        }

        // select superheat spell
        try {
            boolean selected = script.getWidgetManager().getSpellbook().selectSpell(
                StandardSpellbook.SUPERHEAT_ITEM,
                null
            );

            if (!selected) {
                script.log(getClass(), "[superheat] spell select failed");
                return false;
            }
        } catch (SpellNotFoundException e) {
            script.log(getClass(), "[superheat] spell not found - stopping");
            script.stop();
            return false;
        }

        // tap the ore
        Rectangle bounds = oreItem.getBounds();
        if (bounds == null) {
            script.log(getClass(), "[superheat] ore bounds null");
            return false;
        }

        script.getFinger().tap(bounds);

        // wait briefly for the cast to register, then verify ore count decreased
        script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(400, 600, 0.003));

        // verify the cast worked by checking ore count
        ItemGroupResult postCast = script.getWidgetManager().getInventory().search(Set.of(ItemID.GOLD_ORE));
        int oreAfter = (postCast != null && postCast.contains(ItemID.GOLD_ORE)) ? postCast.getAmount(ItemID.GOLD_ORE) : 0;

        // only count as success if ore was actually consumed
        if (oreAfter < oreBefore) {
            barsCreated++;
            double smithXp = hasGoldsmithGauntlets ? SMITHING_XP_WITH_GAUNTLETS : SMITHING_XP_NO_GAUNTLETS;
            manualSmithingXp += smithXp;

            // update custom smithing tracker for ttl calculation
            if (script instanceof TidalsGoldSuperheater) {
                ((TidalsGoldSuperheater) script).getXpTracking().addSmithingXp(smithXp);
            }

            script.log(getClass(), "[superheat] cast #" + barsCreated + " (" + oreAfter + " ore left)");
        } else {
            script.log(getClass(), "[superheat] cast may have failed - retrying");
        }

        // short delay before next cast - superheat animation is ~600ms
        int delay = RandomUtils.weightedRandom(200, 500, 0.003);

        // ~15% chance to add human delay variant for natural variation
        if (RandomUtils.uniformRandom(7) == 0) {
            script.pollFramesHuman(() -> true, delay);
        } else {
            script.pollFramesUntil(() -> false, delay);
        }

        // return false to continue - framework will call activate() again
        // if we still have ore, we'll superheat another; otherwise Bank activates
        return false;
    }
}
