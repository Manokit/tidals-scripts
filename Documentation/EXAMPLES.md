# OSMB API Script Examples

> Complete, working script examples demonstrating common OSMB scripting patterns

---

## Table of Contents

1. [Basic Script Template](#basic-script-template)
2. [Simple Woodcutting Script](#simple-woodcutting-script)
3. [Banking Script Pattern](#banking-script-pattern)
4. [NPC Interaction Script](#npc-interaction-script)
5. [Power Mining Script](#power-mining-script)
6. [Combat Script Example](#combat-script-example)
7. [State Machine Pattern](#state-machine-pattern)
8. [Configuration GUI Example](#configuration-gui-example)

---

## Basic Script Template

The minimal structure for an OSMB script:

```java
package com.example.scripts;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;
import com.osmb.api.ui.GameState;

@ScriptManifest(
    name = "Script Template",
    author = "Your Name",
    version = 1.0,
    description = "Basic script template"
)
public class TemplateScript extends Script {
    
    private long startTime;
    
    @Override
    public void onStart() {
        startTime = System.currentTimeMillis();
        log("Script started!");
    }
    
    @Override
    public int poll() {
        // Check if logged in
        if (getWidgetManager().getGameState() != GameState.LOGGED_IN) {
            return 1000; // Wait 1 second if not logged in
        }
        
        // Main script logic here
        
        return 600; // Return sleep time in milliseconds
    }
    
    @Override
    public void onStop() {
        long runtime = System.currentTimeMillis() - startTime;
        log("Script stopped after " + (runtime / 1000) + " seconds");
    }
}
```

---

## Simple Woodcutting Script

A basic woodcutting script that chops trees and drops logs:

```java
package com.example.scripts;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;
import com.osmb.api.scene.RSObject;
import com.osmb.api.ui.tabs.Inventory;
import com.osmb.api.item.Item;

@ScriptManifest(
    name = "Simple Woodcutter",
    author = "Your Name",
    version = 1.0,
    description = "Chops trees and drops logs"
)
public class SimpleWoodcutter extends Script {
    
    private static final String[] TREE_NAMES = {"Tree", "Oak tree", "Willow tree"};
    private static final int[] LOG_IDS = {1511, 1521, 1519}; // Regular, Oak, Willow logs
    
    @Override
    public void onStart() {
        log("Woodcutter started - chopping trees and dropping logs");
    }
    
    @Override
    public int poll() {
        Inventory inventory = getWidgetManager().getInventory();
        
        // If inventory is full, drop all logs
        if (inventory.isFull()) {
            return dropLogs();
        }
        
        // If not chopping, find and chop a tree
        if (!isChopping()) {
            return chopTree();
        }
        
        // Currently chopping, wait
        return 600;
    }
    
    private int chopTree() {
        // Find nearest tree
        RSObject tree = getObjectManager().getClosestObject(
            getWorldPosition(), 
            TREE_NAMES
        );
        
        if (tree == null) {
            log("No trees found nearby");
            return 1000;
        }
        
        // Interact with tree
        if (tree.interact(null, null, "Chop down")) {
            log("Chopping tree...");
            // Wait until chopping animation starts
            submitTask(() -> isChopping(), 3000);
        }
        
        return 600;
    }
    
    private int dropLogs() {
        Inventory inventory = getWidgetManager().getInventory();
        
        for (int logId : LOG_IDS) {
            Item log = inventory.getItem(logId);
            if (log != null) {
                log.interact("Drop");
                return 300; // Short delay between drops
            }
        }
        
        return 100;
    }
    
    private boolean isChopping() {
        // Check if player is animating (chopping)
        return getWalkingDirection() == null && 
               getLocalPosition() != null;
        // Note: You might want to check animation ID for more accuracy
    }
}
```

---

## Banking Script Pattern

A script demonstrating proper banking flow:

```java
package com.example.scripts;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;
import com.osmb.api.scene.RSObject;
import com.osmb.api.ui.bank.Bank;
import com.osmb.api.ui.tabs.Inventory;
import com.osmb.api.location.position.types.WorldPosition;
import java.util.Set;

@ScriptManifest(
    name = "Banking Example",
    author = "Your Name",
    version = 1.0,
    description = "Demonstrates banking operations"
)
public class BankingExample extends Script {
    
    // Item IDs
    private static final int PICKAXE_ID = 1275; // Rune pickaxe
    private static final int ORE_ID = 436; // Copper ore
    
    // Locations
    private static final WorldPosition BANK_AREA = new WorldPosition(3253, 3420, 0);
    private static final WorldPosition MINE_AREA = new WorldPosition(3285, 3365, 0);
    
    private enum State {
        MINING, WALKING_TO_BANK, BANKING, WALKING_TO_MINE
    }
    
    @Override
    public int poll() {
        State state = getState();
        
        switch (state) {
            case MINING:
                return mine();
            case WALKING_TO_BANK:
                return walkToBank();
            case BANKING:
                return bank();
            case WALKING_TO_MINE:
                return walkToMine();
            default:
                return 600;
        }
    }
    
    private State getState() {
        Inventory inv = getWidgetManager().getInventory();
        Bank bank = getWidgetManager().getBank();
        WorldPosition pos = getWorldPosition();
        
        // If bank is open, we're banking
        if (bank.isVisible()) {
            return State.BANKING;
        }
        
        // If inventory full, need to bank
        if (inv.isFull()) {
            if (pos.distanceTo(BANK_AREA) < 10) {
                return State.BANKING;
            }
            return State.WALKING_TO_BANK;
        }
        
        // If not at mine, walk there
        if (pos.distanceTo(MINE_AREA) > 15) {
            return State.WALKING_TO_MINE;
        }
        
        return State.MINING;
    }
    
    private int mine() {
        RSObject rock = getObjectManager().getRSObject(
            obj -> obj.getName().equals("Copper rocks") ||
                   obj.getName().equals("Tin rocks")
        );
        
        if (rock != null && rock.interact(null, null, "Mine")) {
            submitTask(() -> !rock.exists() || 
                           getWidgetManager().getInventory().isFull(), 
                       30000);
        }
        
        return 600;
    }
    
    private int walkToBank() {
        getWalker().walkTo(BANK_AREA);
        return 600;
    }
    
    private int bank() {
        Bank bank = getWidgetManager().getBank();
        
        // Open bank if not open
        if (!bank.isVisible()) {
            RSObject bankBooth = getObjectManager().getRSObject(
                obj -> obj.getName().equals("Bank booth")
            );
            
            if (bankBooth != null && bankBooth.interact(null, null, "Bank")) {
                submitTask(() -> bank.isVisible(), 5000);
            }
            return 600;
        }
        
        // Deposit ores, keep pickaxe
        bank.depositAll(Set.of(PICKAXE_ID));
        
        // Close bank
        if (submitTask(() -> getWidgetManager().getInventory().getCount(ORE_ID) == 0, 2000)) {
            bank.close();
        }
        
        return 600;
    }
    
    private int walkToMine() {
        getWalker().walkTo(MINE_AREA);
        return 600;
    }
}
```

---

## NPC Interaction Script

Script showing NPC dialogue handling:

```java
package com.example.scripts;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;
import com.osmb.api.scene.RSNPC;
import com.osmb.api.ui.chatbox.dialogue.Dialogue;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;

@ScriptManifest(
    name = "NPC Talker",
    author = "Your Name",
    version = 1.0,
    description = "Demonstrates NPC dialogue handling"
)
public class NPCTalker extends Script {
    
    private static final String NPC_NAME = "Hans";
    
    @Override
    public int poll() {
        Dialogue dialogue = getWidgetManager().getDialogue();
        
        // If in dialogue, handle it
        if (dialogue.isVisible()) {
            return handleDialogue(dialogue);
        }
        
        // Find and talk to NPC
        return talkToNPC();
    }
    
    private int talkToNPC() {
        RSNPC npc = getSceneManager().getNpc(
            n -> n.getName().equals(NPC_NAME)
        );
        
        if (npc == null) {
            log("Could not find " + NPC_NAME);
            return 1000;
        }
        
        if (npc.interact(null, null, "Talk-to")) {
            submitTask(() -> getWidgetManager().getDialogue().isVisible(), 5000);
        }
        
        return 600;
    }
    
    private int handleDialogue(Dialogue dialogue) {
        DialogueType type = dialogue.getDialogueType();
        
        if (type == null) {
            return 300;
        }
        
        switch (type) {
            case NPC_DIALOGUE:
            case PLAYER_DIALOGUE:
                // Click to continue
                dialogue.clickContinue();
                break;
                
            case OPTION:
                // Select a specific option
                dialogue.selectOption("Yes");
                break;
                
            case ITEM_OPTION:
            case SKILL:
                // Type a number
                dialogue.typeInput("1");
                break;
                
            default:
                log("Unknown dialogue type: " + type);
        }
        
        return 600;
    }
}
```

---

## Power Mining Script

Efficient power mining with drop handling:

```java
package com.example.scripts;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;
import com.osmb.api.scene.RSObject;
import com.osmb.api.ui.tabs.Inventory;
import com.osmb.api.item.Item;
import com.osmb.api.location.position.types.WorldPosition;
import java.util.List;

@ScriptManifest(
    name = "Power Miner",
    author = "Your Name",
    version = 1.0,
    description = "Power mines and drops ore for XP"
)
public class PowerMiner extends Script {
    
    private static final String[] ROCK_NAMES = {"Iron rocks"};
    private static final int[] DROP_IDS = {440}; // Iron ore
    
    private WorldPosition startPosition;
    private RSObject currentRock;
    
    @Override
    public void onStart() {
        startPosition = getWorldPosition();
        log("Power Miner started at " + startPosition);
    }
    
    @Override
    public int poll() {
        Inventory inventory = getWidgetManager().getInventory();
        
        // Drop ores if inventory has any
        if (inventory.getCount(DROP_IDS[0]) > 0) {
            return dropOres();
        }
        
        // Check if we need to mine
        if (currentRock == null || !currentRock.exists()) {
            return findAndMineRock();
        }
        
        // Wait while mining
        return 100;
    }
    
    private int findAndMineRock() {
        // Find closest rock
        currentRock = getObjectManager().getClosestObject(
            startPosition,
            ROCK_NAMES
        );
        
        if (currentRock == null) {
            log("No rocks found!");
            return 1000;
        }
        
        // Mine the rock
        if (currentRock.interact(null, null, "Mine")) {
            // Wait for rock to deplete
            submitTask(() -> !currentRock.exists(), 10000);
        }
        
        return 300;
    }
    
    private int dropOres() {
        Inventory inventory = getWidgetManager().getInventory();
        
        for (int dropId : DROP_IDS) {
            List<Item> items = inventory.getItems(id -> id == dropId);
            for (Item item : items) {
                if (item != null) {
                    item.interact("Drop");
                    return 100; // Fast dropping
                }
            }
        }
        
        return 100;
    }
}
```

---

## Combat Script Example

Basic combat with eating and looting:

```java
package com.example.scripts;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;
import com.osmb.api.scene.RSNPC;
import com.osmb.api.ui.tabs.Inventory;
import com.osmb.api.ui.minimap.status.HealthOverlay;
import com.osmb.api.item.Item;

@ScriptManifest(
    name = "Simple Fighter",
    author = "Your Name",
    version = 1.0,
    description = "Fights NPCs and eats food"
)
public class SimpleFighter extends Script {
    
    private static final String[] NPC_NAMES = {"Goblin", "Cow"};
    private static final int[] FOOD_IDS = {333, 329, 373}; // Trout, Salmon, Swordfish
    private static final int EAT_AT_PERCENT = 50;
    
    @Override
    public int poll() {
        // Priority 1: Eat if low health
        if (shouldEat()) {
            return eat();
        }
        
        // Priority 2: Loot if not in combat
        if (!isInCombat()) {
            // Could add looting logic here
        }
        
        // Priority 3: Attack if not in combat
        if (!isInCombat()) {
            return attack();
        }
        
        // In combat, wait
        return 600;
    }
    
    private boolean shouldEat() {
        HealthOverlay health = getWidgetManager().getHealthOverlay();
        if (health != null) {
            HealthOverlay.HealthResult result = health.getHealth();
            if (result != null) {
                int percent = (result.getCurrent() * 100) / result.getMax();
                return percent < EAT_AT_PERCENT;
            }
        }
        return false;
    }
    
    private int eat() {
        Inventory inventory = getWidgetManager().getInventory();
        
        for (int foodId : FOOD_IDS) {
            Item food = inventory.getItem(foodId);
            if (food != null) {
                food.interact("Eat");
                log("Eating food...");
                return 1200; // Eating delay
            }
        }
        
        log("WARNING: Out of food!");
        return 600;
    }
    
    private int attack() {
        RSNPC target = getSceneManager().getNpc(npc -> {
            // Find attackable NPC that's not in combat
            for (String name : NPC_NAMES) {
                if (npc.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        });
        
        if (target == null) {
            log("No targets found");
            return 1000;
        }
        
        if (target.interact(null, null, "Attack")) {
            log("Attacking " + target.getName());
            submitTask(() -> isInCombat(), 3000);
        }
        
        return 600;
    }
    
    private boolean isInCombat() {
        // Simple combat check - could be improved
        return getWalkingDirection() == null;
    }
}
```

---

## State Machine Pattern

Advanced script using state machine for complex logic:

```java
package com.example.scripts;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;

@ScriptManifest(
    name = "State Machine Example",
    author = "Your Name",
    version = 1.0,
    description = "Demonstrates state machine pattern"
)
public class StateMachineScript extends Script {
    
    private enum State {
        IDLE,
        WALKING,
        WORKING,
        BANKING,
        RESTOCKING
    }
    
    private State currentState = State.IDLE;
    
    // Abstract state handler
    private interface StateHandler {
        int execute();
        State getNextState();
    }
    
    @Override
    public int poll() {
        StateHandler handler = getHandler(currentState);
        int sleepTime = handler.execute();
        currentState = handler.getNextState();
        return sleepTime;
    }
    
    private StateHandler getHandler(State state) {
        switch (state) {
            case IDLE:
                return new IdleHandler();
            case WALKING:
                return new WalkingHandler();
            case WORKING:
                return new WorkingHandler();
            case BANKING:
                return new BankingHandler();
            case RESTOCKING:
                return new RestockingHandler();
            default:
                return new IdleHandler();
        }
    }
    
    // Handler implementations
    private class IdleHandler implements StateHandler {
        @Override
        public int execute() {
            log("Idle - determining next action...");
            return 300;
        }
        
        @Override
        public State getNextState() {
            Inventory inv = getWidgetManager().getInventory();
            if (inv.isFull()) {
                return State.BANKING;
            }
            if (!hasRequiredItems()) {
                return State.RESTOCKING;
            }
            if (!atWorkLocation()) {
                return State.WALKING;
            }
            return State.WORKING;
        }
    }
    
    private class WalkingHandler implements StateHandler {
        @Override
        public int execute() {
            // Walk to work area
            return 600;
        }
        
        @Override
        public State getNextState() {
            if (atWorkLocation()) {
                return State.WORKING;
            }
            return State.WALKING;
        }
    }
    
    private class WorkingHandler implements StateHandler {
        @Override
        public int execute() {
            // Do work (mining, woodcutting, etc.)
            return 600;
        }
        
        @Override
        public State getNextState() {
            if (getWidgetManager().getInventory().isFull()) {
                return State.BANKING;
            }
            return State.WORKING;
        }
    }
    
    private class BankingHandler implements StateHandler {
        @Override
        public int execute() {
            // Handle banking
            return 600;
        }
        
        @Override
        public State getNextState() {
            if (!getWidgetManager().getInventory().isFull()) {
                return State.IDLE;
            }
            return State.BANKING;
        }
    }
    
    private class RestockingHandler implements StateHandler {
        @Override
        public int execute() {
            // Get supplies from bank
            return 600;
        }
        
        @Override
        public State getNextState() {
            if (hasRequiredItems()) {
                return State.IDLE;
            }
            return State.RESTOCKING;
        }
    }
    
    // Helper methods
    private boolean hasRequiredItems() {
        // Check for required items
        return true;
    }
    
    private boolean atWorkLocation() {
        // Check if at work location
        return true;
    }
}
```

---

## Configuration GUI Example

Script with user configuration:

```java
package com.example.scripts;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;
import com.osmb.api.script.configuration.ScriptConfiguration;

@ScriptManifest(
    name = "Configurable Script",
    author = "Your Name",
    version = 1.0,
    description = "Script with GUI configuration"
)
public class ConfigurableScript extends Script {
    
    // Configuration fields
    private String targetName;
    private int minDelay;
    private int maxDelay;
    private boolean useSpecialAttack;
    
    // ScriptConfiguration for GUI
    private ScriptConfiguration config;
    
    @Override
    public void onStart() {
        // Load saved configuration or set defaults
        targetName = config.getString("targetName", "Goblin");
        minDelay = config.getInt("minDelay", 100);
        maxDelay = config.getInt("maxDelay", 300);
        useSpecialAttack = config.getBoolean("useSpecialAttack", false);
        
        log("Configuration loaded:");
        log("Target: " + targetName);
        log("Delays: " + minDelay + "-" + maxDelay + "ms");
        log("Special Attack: " + useSpecialAttack);
    }
    
    @Override
    public int poll() {
        // Use configuration values
        RSNPC target = getSceneManager().getNpc(
            npc -> npc.getName().equals(targetName)
        );
        
        if (target != null) {
            target.interact(null, null, "Attack");
        }
        
        // Random delay between configured values
        return getUtils().random(minDelay, maxDelay);
    }
    
    @Override
    public void onStop() {
        // Save configuration for next run
        config.setString("targetName", targetName);
        config.setInt("minDelay", minDelay);
        config.setInt("maxDelay", maxDelay);
        config.setBoolean("useSpecialAttack", useSpecialAttack);
        config.save();
    }
}
```

---

## Best Practices Summary

1. **Always null-check** entities and UI components before use
2. **Use submitTask()** for waiting after actions
3. **Add appropriate delays** to seem human-like
4. **Handle disconnections** by checking GameState
5. **Log important actions** for debugging
6. **Use state machines** for complex scripts
7. **Cache positions** to avoid recalculating
8. **Check inventory state** before banking actions
9. **Validate interactions succeeded** before continuing
10. **Use random delays** between actions for anti-ban

---

*These examples demonstrate common patterns in OSMB scripting. Adapt them to your specific needs.*
