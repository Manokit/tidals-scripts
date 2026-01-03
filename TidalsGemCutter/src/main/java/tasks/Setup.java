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

import static main.TidalsGemCutter.*;


public class Setup extends Task {
    public Setup(Script script) {
        super(script);
    }

    public boolean activate() {
        return !setupDone;
    }

    public boolean execute() {

        // Check required skill level (Crafting for gems, Fletching for bolt tips)
        task = "Get skill level";
        SkillType skillToCheck = makeBoltTips ? SkillType.FLETCHING : SkillType.CRAFTING;
        SkillsTabComponent.SkillLevel skillLevel = script.getWidgetManager().getSkillTab().getSkillLevel(skillToCheck);
        if (skillLevel == null) {
            script.log(getClass(), "Failed to get skill levels.");
            return false;
        }
        startLevel = skillLevel.getLevel();
        currentLevel = skillLevel.getLevel();

        // Check minimum level (Sapphire requires 20 crafting, bolt tips require 11 fletching)
        int minLevel = makeBoltTips ? 11 : 20;
        String skillName = makeBoltTips ? "Fletching" : "Crafting";
        if (currentLevel < minLevel) {
            script.log(getClass(), skillName + " level too low! Minimum level " + minLevel + " required.");
            script.stop();
            return false;
        }

        task = "Open inventory";
        script.log(getClass(), "Opening inventory tab");
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);

        // Wait for inventory to open
        boolean inventoryOpened = script.pollFramesUntil(() ->
            script.getWidgetManager().getInventory().search(Set.of()) != null,
            3000
        );

        if (!inventoryOpened) {
            script.log(getClass(), "Inventory did not open, retrying...");
            return false;
        }

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
