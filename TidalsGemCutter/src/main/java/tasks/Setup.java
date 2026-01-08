package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import com.osmb.api.ui.component.tabs.skill.SkillsTabComponent;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.script.Script;
import utils.Task;

import java.util.Set;

import static main.TidalsGemCutter.*;

public class Setup extends Task {

    public Setup(Script script) {
        super(script);
    }

    public boolean activate() {
        return !setupDone;
    }

    public boolean execute() {
        // check skill level
        task = "Get level";
        SkillType skill = makeBoltTips ? SkillType.FLETCHING : SkillType.CRAFTING;
        SkillsTabComponent.SkillLevel level = script.getWidgetManager().getSkillTab().getSkillLevel(skill);
        if (level == null) {
            script.log(getClass(), "failed to get level");
            return false;
        }
        startLevel = level.getLevel();
        currentLevel = level.getLevel();

        int minLevel = makeBoltTips ? 11 : 20;
        String skillName = makeBoltTips ? "fletching" : "crafting";
        if (currentLevel < minLevel) {
            script.log(getClass(), skillName + " too low, need " + minLevel);
            script.stop();
            return false;
        }

        // open inventory
        task = "Open inventory";
        script.log(getClass(), "opening inventory");
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);

        boolean opened = script.pollFramesUntil(() ->
            script.getWidgetManager().getInventory().search(Set.of()) != null,
            3000
        );

        if (!opened) {
            script.log(getClass(), "inventory not open");
            return false;
        }

        // check chisel
        task = "Check chisel";
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(ItemID.CHISEL));

        if (inv == null) {
            return false;
        }

        if (!inv.contains(ItemID.CHISEL)) {
            script.log(getClass(), "no chisel, stopping");
            script.stop();
            return false;
        }

        task = "Ready";
        setupDone = true;
        hasReqs = true;
        shouldBank = true;
        return false;
    }
}
