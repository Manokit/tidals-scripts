# OSMB Scripting Patterns & Best Practices

> Design patterns, idioms, and best practices for writing robust OSMB scripts

---

## Table of Contents

1. [Error Handling Patterns](#error-handling-patterns)
2. [Interaction Patterns](#interaction-patterns)
3. [Navigation Patterns](#navigation-patterns)
4. [UI Handling Patterns](#ui-handling-patterns)
5. [Timing Patterns](#timing-patterns)
6. [State Management](#state-management)
7. [Anti-Pattern Avoidance](#anti-pattern-avoidance)
8. [Performance Tips](#performance-tips)

---

## Error Handling Patterns

### Null-Safe Entity Access

Always check for null before interacting with game entities:

```java
// BAD - Can throw NullPointerException
RSObject tree = getObjectManager().getRSObject(o -> o.getName().equals("Tree"));
tree.interact("Chop down"); // NPE if tree is null!

// GOOD - Null-safe
RSObject tree = getObjectManager().getRSObject(o -> o.getName().equals("Tree"));
if (tree != null) {
    tree.interact("Chop down");
}

// BETTER - Using Optional
getObjectManager().getObject(o -> o.getName().equals("Tree"))
    .ifPresent(tree -> tree.interact("Chop down"));
```

### UIResult Handling

Many UI methods return `UIResult<T>` - always check the result:

```java
// BAD - Ignoring result state
UIResult<Integer> result = bank.getSelectedTabIndex();
int tab = result.get(); // Could fail if NOT_VISIBLE

// GOOD - Proper handling
UIResult<Integer> result = bank.getSelectedTabIndex();
if (result.isFound()) {
    int tab = result.get();
    // Use tab value
} else if (result == UIResult.NOT_VISIBLE) {
    // Bank is not open
    log("Bank not visible");
}
```

### Interaction Verification

Verify that interactions succeeded:

```java
// Basic verification
if (object.interact("Open")) {
    boolean success = submitTask(() -> bank.isVisible(), 5000);
    if (!success) {
        log("Failed to open bank");
        return 600; // Try again
    }
}

// With retry logic
int attempts = 0;
while (attempts < 3) {
    if (object.interact("Use") && submitTask(() -> expectedState(), 3000)) {
        break; // Success
    }
    attempts++;
}
```

---

## Interaction Patterns

### Standard Interaction Pattern

```java
public boolean interactWithObject(String objectName, String action) {
    RSObject object = getObjectManager().getRSObject(
        o -> o.getName().equals(objectName)
    );
    
    if (object == null) {
        log("Could not find: " + objectName);
        return false;
    }
    
    if (!object.interact(null, null, action)) {
        log("Failed to interact with: " + objectName);
        return false;
    }
    
    return true;
}
```

### Closest Entity Pattern

```java
// Find closest of multiple valid targets
RSObject closest = null;
int closestDistance = Integer.MAX_VALUE;

for (RSObject obj : getObjectManager().getObjects(o -> isValidTarget(o))) {
    int dist = getWorldPosition().distanceTo(obj.getWorldPosition());
    if (dist < closestDistance) {
        closest = obj;
        closestDistance = dist;
    }
}

// Or use built-in method
RSObject closest = getObjectManager().getClosestObject(
    getWorldPosition(), 
    "Tree", "Oak tree", "Willow tree"
);
```

### Sequential Actions Pattern

```java
public void useItemOnObject(int itemId, String objectName) {
    Inventory inv = getWidgetManager().getInventory();
    Item item = inv.getItem(itemId);
    
    if (item == null) {
        log("Item not in inventory");
        return;
    }
    
    // Select item first
    item.interact("Use");
    submitTask(() -> inv.isItemSelected(), 1000);
    
    // Then use on object
    RSObject target = getObjectManager().getRSObject(
        o -> o.getName().equals(objectName)
    );
    
    if (target != null) {
        target.interact(null, null, "Use");
    }
}
```

---

## Navigation Patterns

### Walking with Conditions

```java
public boolean walkToArea(WorldPosition destination, int maxDistance) {
    if (getWorldPosition().distanceTo(destination) <= maxDistance) {
        return true; // Already there
    }
    
    WalkConfig config = WalkConfig.builder()
        .breakCondition(() -> {
            // Stop if we find what we're looking for
            return getObjectManager().getRSObject(
                o -> o.getName().equals("Bank booth")
            ) != null;
        })
        .tileRandomisation(2)
        .build();
    
    return getWalker().walkTo(destination, config);
}
```

### Multi-Region Walking

```java
public void walkLongDistance(WorldPosition destination) {
    while (getWorldPosition().distanceTo(destination) > 5) {
        // Walk in steps
        getWalker().walkTo(destination);
        
        // Wait for movement to complete
        submitTask(() -> {
            return getWalkingDirection() == null ||
                   getWorldPosition().distanceTo(destination) < 5;
        }, 30000);
        
        // Small delay between walks
        sleep(100, 300);
    }
}
```

### Area Containment Check

```java
// Using RectangleArea
RectangleArea bankArea = new RectangleArea(3250, 3420, 3257, 3428, 0);

public boolean isAtBank() {
    return bankArea.contains(getWorldPosition());
}

// Using distance check
public boolean isNearLocation(WorldPosition target, int maxDistance) {
    return getWorldPosition().distanceTo(target) <= maxDistance;
}
```

---

## UI Handling Patterns

### Safe Bank Operations

```java
public boolean openBank() {
    Bank bank = getWidgetManager().getBank();
    
    if (bank.isVisible()) {
        return true; // Already open
    }
    
    RSObject bankBooth = getObjectManager().getRSObject(
        o -> o.getName().equals("Bank booth") ||
             o.getName().equals("Bank chest")
    );
    
    if (bankBooth == null) {
        log("No bank found nearby");
        return false;
    }
    
    if (bankBooth.interact(null, null, "Bank")) {
        return submitTask(() -> bank.isVisible(), 5000);
    }
    
    return false;
}

public boolean depositAllExcept(int... keepItemIds) {
    Bank bank = getWidgetManager().getBank();
    
    if (!bank.isVisible()) {
        return false;
    }
    
    Set<Integer> keepSet = Arrays.stream(keepItemIds)
        .boxed()
        .collect(Collectors.toSet());
    
    return bank.depositAll(keepSet);
}
```

### Dialogue Handler Pattern

```java
public void handleDialogue() {
    Dialogue dialogue = getWidgetManager().getDialogue();
    
    while (dialogue.isVisible()) {
        DialogueType type = dialogue.getDialogueType();
        
        switch (type) {
            case NPC_DIALOGUE:
            case PLAYER_DIALOGUE:
                dialogue.clickContinue();
                break;
                
            case OPTION:
                // Handle based on options available
                if (dialogue.containsOption("Yes")) {
                    dialogue.selectOption("Yes");
                } else {
                    dialogue.selectOption(1); // First option
                }
                break;
                
            case ITEM_OPTION:
            case SKILL:
                dialogue.typeInput("1");
                break;
                
            default:
                break;
        }
        
        submitTask(() -> !dialogue.isVisible() || 
                        dialogue.getDialogueType() != type, 3000);
    }
}
```

### Tab Management

```java
public boolean openTab(Tab.Type tabType) {
    TabManager tabs = getWidgetManager().getTabManager();
    
    if (tabs.isTabOpen(tabType)) {
        return true;
    }
    
    tabs.openTab(tabType);
    return submitTask(() -> tabs.isTabOpen(tabType), 2000);
}

// Open inventory before checking items
public Item getInventoryItem(int itemId) {
    openTab(Tab.Type.INVENTORY);
    return getWidgetManager().getInventory().getItem(itemId);
}
```

---

## Timing Patterns

### Randomized Delays

```java
// Basic random delay
private void randomSleep() {
    sleep(100, 300);
}

// Gaussian distribution for more human-like timing
private int gaussianDelay(int min, int max) {
    Utils utils = getUtils();
    int mean = (min + max) / 2;
    int stdDev = (max - min) / 4;
    int delay = (int) (mean + utils.random(-stdDev, stdDev));
    return Math.max(min, Math.min(max, delay));
}

// Action-specific delays
private static final int CLICK_DELAY_MIN = 50;
private static final int CLICK_DELAY_MAX = 150;
private static final int ACTION_DELAY_MIN = 300;
private static final int ACTION_DELAY_MAX = 800;
```

### Conditional Waiting Patterns

```java
// Simple condition
submitTask(() -> bank.isVisible(), 5000);

// Multiple conditions (OR)
submitTask(() -> bank.isVisible() || dialogue.isVisible(), 5000);

// With polling rate
submitTask(() -> inventory.isFull(), 60000, 600);

// Timeout tracking
Stopwatch timer = new Stopwatch();
timer.start();
boolean found = submitTask(() -> someCondition(), 10000);
log("Waited " + timer.getElapsedTime() + "ms, result: " + found);
```

### Anti-Pattern: Busy Waiting

```java
// BAD - Busy waiting consumes resources
while (!condition()) {
    // Spinning without sleep!
}

// GOOD - Use submitTask or add delays
submitTask(() -> condition(), 5000);

// OR
while (!condition()) {
    sleep(100, 200); // Proper delay
}
```

---

## State Management

### Enum State Machine

```java
public enum ScriptState {
    IDLE,
    GATHERING,
    BANKING,
    WALKING_TO_RESOURCE,
    WALKING_TO_BANK
}

private ScriptState currentState = ScriptState.IDLE;

@Override
public int poll() {
    currentState = determineState();
    
    switch (currentState) {
        case IDLE:
            return handleIdle();
        case GATHERING:
            return handleGathering();
        case BANKING:
            return handleBanking();
        case WALKING_TO_RESOURCE:
            return handleWalkingToResource();
        case WALKING_TO_BANK:
            return handleWalkingToBank();
        default:
            return 600;
    }
}

private ScriptState determineState() {
    Inventory inv = getWidgetManager().getInventory();
    Bank bank = getWidgetManager().getBank();
    
    // Priority-based state determination
    if (bank.isVisible()) return ScriptState.BANKING;
    if (inv.isFull()) return ScriptState.WALKING_TO_BANK;
    if (atResourceLocation()) return ScriptState.GATHERING;
    return ScriptState.WALKING_TO_RESOURCE;
}
```

### Configuration Pattern

```java
public class ScriptConfig {
    private String targetName = "Tree";
    private int minDelay = 100;
    private int maxDelay = 300;
    private boolean dropItems = true;
    
    // Builder pattern for fluent configuration
    public static class Builder {
        private ScriptConfig config = new ScriptConfig();
        
        public Builder targetName(String name) {
            config.targetName = name;
            return this;
        }
        
        public Builder delays(int min, int max) {
            config.minDelay = min;
            config.maxDelay = max;
            return this;
        }
        
        public Builder dropItems(boolean drop) {
            config.dropItems = drop;
            return this;
        }
        
        public ScriptConfig build() {
            return config;
        }
    }
}

// Usage
ScriptConfig config = new ScriptConfig.Builder()
    .targetName("Oak tree")
    .delays(150, 400)
    .dropItems(true)
    .build();
```

---

## Anti-Pattern Avoidance

### Don't: Hardcoded Coordinates

```java
// BAD - Hardcoded positions break easily
if (getWorldPosition().getX() == 3222 && getWorldPosition().getY() == 3218) {
    // At bank
}

// GOOD - Use areas and distance checks
private static final RectangleArea BANK_AREA = new RectangleArea(3220, 3215, 3227, 3222, 0);

if (BANK_AREA.contains(getWorldPosition())) {
    // At bank
}
```

### Don't: Magic Numbers

```java
// BAD - What do these numbers mean?
if (inventory.getCount(995) > 10000) {
    bank.withdraw(1511, 28);
}

// GOOD - Named constants
private static final int COINS = 995;
private static final int OAK_LOGS = 1511;
private static final int MIN_COINS = 10000;
private static final int FULL_INVENTORY = 28;

if (inventory.getCount(COINS) > MIN_COINS) {
    bank.withdraw(OAK_LOGS, FULL_INVENTORY);
}
```

### Don't: Deep Nesting

```java
// BAD - Hard to read and maintain
if (bank.isVisible()) {
    if (inventory.isFull()) {
        if (bank.depositAll(Set.of())) {
            if (bank.close()) {
                // Finally do something
            }
        }
    }
}

// GOOD - Early returns
if (!bank.isVisible()) return false;
if (!inventory.isFull()) return false;
if (!bank.depositAll(Set.of())) return false;
return bank.close();
```

### Don't: Catch-All Exception Handling

```java
// BAD - Swallowing all exceptions
try {
    doSomething();
} catch (Exception e) {
    // Ignore
}

// GOOD - Handle specific cases
try {
    doSomething();
} catch (NullPointerException e) {
    log("Entity not found: " + e.getMessage());
    return 1000; // Retry
}
```

---

## Performance Tips

### Cache Expensive Lookups

```java
// BAD - Looking up every loop
@Override
public int poll() {
    RSObject tree = getObjectManager().getRSObject(o -> o.getName().equals("Tree"));
    // Use tree...
}

// GOOD - Cache the target
private RSObject currentTarget;

@Override
public int poll() {
    // Only find new target if needed
    if (currentTarget == null || !currentTarget.exists()) {
        currentTarget = getObjectManager().getRSObject(o -> o.getName().equals("Tree"));
    }
    
    if (currentTarget != null) {
        // Use currentTarget...
    }
}
```

### Minimize Widget Manager Calls

```java
// BAD - Multiple calls to get same widget
if (getWidgetManager().getInventory().isFull()) {
    getWidgetManager().getInventory().getItem(itemId);
    // etc.
}

// GOOD - Get reference once
Inventory inv = getWidgetManager().getInventory();
if (inv.isFull()) {
    inv.getItem(itemId);
    // etc.
}
```

### Use Appropriate Sleep Times

```java
// Return value from poll() should match what you're waiting for:

// Fast actions (clicking, small movements)
return 100;

// Standard actions (walking, interactions)
return 600;

// Waiting for loading/animations
return 1000;

// When not logged in or error state
return 2000;
```

---

## Summary

1. **Always null-check** before interacting with entities
2. **Use submitTask()** for all wait operations
3. **Handle UIResult** properly - check `isFound()` before `get()`
4. **Use constants** for item IDs, positions, and delays
5. **Implement state machines** for complex scripts
6. **Add randomization** to timings for human-like behavior
7. **Cache frequently used** widgets and entities
8. **Early return** instead of deep nesting
9. **Log important events** for debugging
10. **Test edge cases** like disconnections and inventory full

---

*Following these patterns will result in more robust, maintainable, and reliable OSMB scripts.*
