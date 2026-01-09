package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import com.osmb.api.ui.component.tabs.skill.SkillsTabComponent;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.script.Script;

import main.TidalsGoldSuperheater;
import utils.Task;

import java.util.Set;

import static main.TidalsGoldSuperheater.*;

public class Setup extends Task {
    
    private static final int STAFF_OF_FIRE = 1387;
    private static final int FIRE_BATTLESTAFF = 1393;
    private static final int LAVA_BATTLESTAFF = 21198;
    private static final int TOME_OF_FIRE = 20714;
    private static final int GOLDSMITH_GAUNTLETS = 776;
    
    public Setup(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        return !setupDone;
    }

    @Override
    public boolean execute() {
        task = "Setup";
        
        // open inventory
        task = "Open inventory";
        script.log(getClass(), "opening inventory");
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);

        boolean opened = script.pollFramesUntil(() ->
            script.getWidgetManager().getInventory().search(Set.of()) != null,
            3000
        );

        if (!opened) {
            script.log(getClass(), "inventory not open, retrying");
            return false;
        }
        
        // check nature runes
        task = "Check runes";
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(ItemID.NATURE_RUNE));
        
        if (inv == null || !inv.contains(ItemID.NATURE_RUNE)) {
            script.log(getClass(), "no nature runes, stopping");
            script.stop();
            return false;
        }
        
        int runeCount = inv.getAmount(ItemID.NATURE_RUNE);
        script.log(getClass(), "found " + runeCount + " nature runes");
        
        // check fire source
        task = "Check equipment";
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> false, script.random(300, 500));
        
        com.osmb.api.utils.UIResult<ItemSearchResult> fireResult = script.getWidgetManager().getEquipment().findItem(
            STAFF_OF_FIRE,
            FIRE_BATTLESTAFF,
            LAVA_BATTLESTAFF,
            TOME_OF_FIRE
        );
        
        if (!fireResult.isFound()) {
            script.log(getClass(), "no fire source equipped, stopping");
            script.stop();
            return false;
        }
        
        script.log(getClass(), "fire source found");
        
        // check goldsmith gauntlets
        task = "Check gauntlets";
        com.osmb.api.utils.UIResult<ItemSearchResult> gauntlets = script.getWidgetManager().getEquipment().findItem(GOLDSMITH_GAUNTLETS);
        hasGoldsmithGauntlets = gauntlets.isFound();
        
        if (hasGoldsmithGauntlets) {
            script.log(getClass(), "goldsmith gauntlets: " + SMITHING_XP_WITH_GAUNTLETS + " xp/bar");
        } else {
            script.log(getClass(), "no gauntlets: " + SMITHING_XP_NO_GAUNTLETS + " xp/bar");
        }
        
        // get levels
        task = "Get levels";
        SkillsTabComponent.SkillLevel magic = script.getWidgetManager().getSkillTab().getSkillLevel(SkillType.MAGIC);
        if (magic == null) {
            script.log(getClass(), "failed to get magic level");
            return false;
        }
        startMagicLevel = magic.getLevel();
        currentMagicLevel = magic.getLevel();
        script.log(getClass(), "magic: " + currentMagicLevel);
        
        SkillsTabComponent.SkillLevel smithing = script.getWidgetManager().getSkillTab().getSkillLevel(SkillType.SMITHING);
        if (smithing == null) {
            script.log(getClass(), "failed to get smithing level");
            return false;
        }
        startSmithingLevel = smithing.getLevel();
        currentSmithingLevel = smithing.getLevel();
        script.log(getClass(), "smithing: " + currentSmithingLevel);

        // initialize custom smithing tracker for ttl calculation
        if (script instanceof TidalsGoldSuperheater) {
            ((TidalsGoldSuperheater) script).getXpTracking().initSmithingTracker(currentSmithingLevel);
            script.log(getClass(), "smithing tracker initialized");
        }
        
        // check level req
        if (currentMagicLevel < 43) {
            script.log(getClass(), "need 43 magic for superheat");
            script.stop();
            return false;
        }
        
        // back to inventory
        task = "Open inventory";
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
        script.pollFramesHuman(() -> false, script.random(200, 400));
        
        task = "Ready";
        script.log(getClass(), "setup done");
        setupDone = true;
        hasReqs = true;
        
        return false;
    }
}
