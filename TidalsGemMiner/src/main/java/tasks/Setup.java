package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.script.Script;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import com.osmb.api.ui.component.tabs.skill.SkillsTabComponent;
import utilities.TabUtils;
import utils.Task;
import utils.XPTracking;

import java.util.Set;

import static main.TidalsGemMiner.*;

public class Setup extends Task {

    public Setup(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        if (!setupDone) {
            return true;
        }
        return false;
    }

    @Override
    public boolean execute() {
        script.log(getClass(), "Setting up...");
        task = "Setup";

        // get mining level and initialize XP tracker
        SkillsTabComponent.SkillLevel miningSkillLevel = script.getWidgetManager()
                .getSkillTab()
                .getSkillLevel(SkillType.MINING);

        if (miningSkillLevel == null) {
            script.log(getClass(), "Failed to get Mining skill level");
            return false;
        }

        int miningLevel = miningSkillLevel.getLevel();
        startMiningLevel = miningLevel;
        currentMiningLevel = miningLevel;
        script.log(getClass(), "Mining level: " + miningLevel);

        // initialize mining tracker
        int actualMiningXp = XPTracking.tryGetActualXp(script, SkillType.MINING);
        if (actualMiningXp > 0) {
            xpTracking.initMiningTracker(miningLevel, actualMiningXp);
        } else {
            xpTracking.initMiningTracker(miningLevel);
        }

        if (cuttingEnabled) {
            // get crafting level and initialize XP tracker
            SkillsTabComponent.SkillLevel craftingSkillLevel = script.getWidgetManager()
                    .getSkillTab()
                    .getSkillLevel(SkillType.CRAFTING);

            if (craftingSkillLevel == null) {
                script.log(getClass(), "Failed to get Crafting skill level");
                return false;
            }

            int craftingLevel = craftingSkillLevel.getLevel();
            startCraftingLevel = craftingLevel;
            currentCraftingLevel = craftingLevel;
            script.log(getClass(), "Crafting level: " + craftingLevel);

            // initialize crafting tracker
            int actualCraftingXp = XPTracking.tryGetActualXp(script, SkillType.CRAFTING);
            if (actualCraftingXp > 0) {
                xpTracking.initCraftingTracker(craftingLevel, actualCraftingXp);
            } else {
                xpTracking.initCraftingTracker(craftingLevel);
            }

            // open inventory tab to check for chisel
            boolean inventoryOpen = TabUtils.openAndVerifyInventory(script, 3000);
            if (!inventoryOpen) {
                script.log(getClass(), "could not open inventory tab");
                return false;
            }

            // search inventory for chisel
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(ItemID.CHISEL));
            if (inv == null || !inv.contains(ItemID.CHISEL)) {
                script.log(getClass(), "Chisel required when cutting is enabled!");
                script.stop();
                return false;
            }

            script.log(getClass(), "Chisel found - cutting enabled");
        } else {
            script.log(getClass(), "Cutting disabled - will bank raw gems");
        }

        setupDone = true;
        script.log(getClass(), "Setup complete");

        return false; // allow next task to run
    }
}
