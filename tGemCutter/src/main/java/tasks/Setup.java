package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import com.osmb.api.ui.component.tabs.skill.SkillsTabComponent;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.script.Script;

// OTHER CLASS IMPORTS
import utils.Task;

import java.util.Set;

import static main.tGemCutter.*;


public class Setup extends Task {
    public Setup(Script script) {
        super(script);
    }

    public boolean activate() {
        return !setupDone;
    }

    public boolean execute() {

        // Check required crafting level
        task = "Get crafting level";
        SkillsTabComponent.SkillLevel craftingSkillLevel = script.getWidgetManager().getSkillTab().getSkillLevel(SkillType.CRAFTING);
        if (craftingSkillLevel == null) {
            script.log(getClass(), "Failed to get skill levels.");
            return false;
        }
        startLevel = craftingSkillLevel.getLevel();
        currentLevel = craftingSkillLevel.getLevel();

        // Check minimum level (Sapphire requires 20, but we'll check for 20 minimum)
        if (currentLevel < 20) {
            script.log(getClass(), "Crafting level too low! Minimum level 20 required.");
            script.stop();
            return false;
        }

        task = "Open inventory";
        script.log(getClass(), "Opening inventory tab");
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);

        task = "Take invent snapshot";
        ItemGroupResult inventorySnapshot = script.getWidgetManager().getInventory().search(Set.of(ItemID.CHISEL));

        if (inventorySnapshot == null) {
            // Inventory not visible
            return false;
        }

        if (!inventorySnapshot.contains(ItemID.CHISEL)) {
            script.log(getClass(), "No chisel in inventory, stopping script.");
            script.stop();
            return false;
        }

        task = "Update flags";
        setupDone = true;
        hasReqs = true;
        shouldBank = true;
        return false;
    }
}
