# Common Mistakes & Debugging Guide

Solutions to frequent issues and debugging best practices.

## Common Mistakes to Avoid

### 1. Not Null Checking
```java
// WRONG
RSObject tree = objects.getClosestObject(pos, "Tree");
tree.interact("Chop"); // NullPointerException!

// RIGHT
RSObject tree = objects.getClosestObject(pos, "Tree");
if (tree != null) {
    tree.interact("Chop");
}
```

**Rule**: Always null-check before calling methods on objects returned by API.

---

### 2. Using while Loops Instead of pollFramesUntil
```java
// WRONG - Blocks everything
while (!bank.isVisible()) {
    Thread.sleep(100);
}

// RIGHT - Proper waiting
pollFramesUntil(() -> bank.isVisible(), 5000);
```

**Rule**: Never use blocking loops. Use `pollFramesUntil` or `pollFramesHuman`.

---

### 3. Modifying State in onNewFrame
```java
// WRONG - onNewFrame is for READ-ONLY operations
@Override
public void onNewFrame() {
    getFinger().tap(somePoint); // NO! Don't do actions here
}

// RIGHT - Only read data
@Override  
public void onNewFrame() {
    chatLines = getChatbox().getLines(); // OK - just reading
}
```

**Rule**: onNewFrame is READ-ONLY. No clicks, no state changes.

---

### 4. Assuming Collision Map is Real-Time
```java
// WRONG - Collision map is static default state
if (collisionMap.isBlocked(x, y)) {
    // Door might actually be open!
}

// RIGHT - Visual verification
// Check door state via menu entry or pixel detection
```

**Rule**: Collision map shows DEFAULT state. Always verify visually.

---

### 5. Not Using Region Priorities
```java
// Script startup takes 90+ seconds without this
@Override
public ScriptOptions getScriptOptions() {
    return new ScriptOptions() {
        @Override
        public int[] regionsToPrioritise() {
            return new int[]{12850}; // Your operating region
        }
    };
}
```

**Rule**: Always prioritize regions for faster startup.

---

### 6. Bank Loading Delay
```java
// WRONG - Searching bank immediately after it opens
if (bank.isVisible()) {
    ItemGroupResult result = bank.search(Set.of(itemID)); // Items not loaded yet!
}

// RIGHT - Wait for bank to load
if (bank.isVisible()) {
    pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));
    ItemGroupResult result = bank.search(Set.of(itemID));
}
```

**Rule**: Always add 300-500ms delay after opening bank.

---

### 7. Wrong Lambda in Delay Patterns (CRITICAL)

The most common timing bug - using the wrong `true`/`false` in delay lambdas:

```java
// WRONG - Exits IMMEDIATELY (0ms delay!) because condition is instantly true
script.pollFramesUntil(() -> true, 2000);  // BUG: no delay at all!

// WRONG - pollFramesHuman with false adds timeout + human delay (double wait)
script.pollFramesHuman(() -> false, 500);  // waits 500ms + 200-400ms = 700-900ms total

// CORRECT - For fixed delays, use pollFramesUntil with false (waits full timeout)
script.pollFramesUntil(() -> false, 2000);  // exactly 2000ms delay

// CORRECT - For humanized delays, use pollFramesHuman with true
script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));  // instant + human delay
```

**How it works:**
| Method | `() -> true` | `() -> false` |
|--------|--------------|---------------|
| `pollFramesUntil` | Exits immediately (0ms) | Waits full timeout |
| `pollFramesHuman` | Exits + adds human delay (~200-400ms) | Waits timeout + human delay |

**Best practices:**
```java
// FIXED DELAY (animation wait, post-action pause)
script.pollFramesUntil(() -> false, 2000);

// HUMANIZED DELAY (between actions, adds natural variance)
script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

// CONDITIONAL WAIT (wait for something to happen)
script.pollFramesUntil(() -> bank.isVisible(), 5000);

// CONDITIONAL WAIT + HUMANIZED (wait for condition, then add reaction time)
script.pollFramesHuman(() -> inventory.isFull(), 30000);
```

**Rule**: For pure delays use `pollFramesUntil(() -> false, ms)`. For humanized delays use `pollFramesHuman(() -> true, ms)`. Never use `pollFramesUntil(() -> true, ms)` - it does nothing!

**DEPRECATED**: Do NOT use `submitTask` - it may be async and not block properly. Always use `pollFramesUntil` or `pollFramesHuman`.

---

### 8. Using script.random() Instead of RandomUtils (DEPRECATED)

**NEVER use `script.random()`** - it produces uniform distribution which looks robotic. Always use `RandomUtils` methods for human-like randomness.

```java
// WRONG - uniform distribution, robotic timing
script.pollFramesHuman(() -> true, script.random(200, 400));  // DON'T DO THIS

// CORRECT - use RandomUtils methods instead
script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));  // weighted toward lower
script.pollFramesHuman(() -> true, RandomUtils.gaussianRandom(200, 400, 300, 50));  // clusters around 300
```

**RandomUtils methods to use:**

| Method | Use Case |
|--------|----------|
| `gaussianRandom(min, max, mean, stdDev)` | Animation waits, action delays - clusters around mean |
| `weightedRandom(min, max)` | Short delays - weighted toward lower values (faster reactions) |
| `exponentialRandom(mean, min, max)` | Human-like pauses - most near mean, occasional longer |
| `triangularRandom(min, max, midpoint)` | When you want a specific peak value |

**Examples:**
```java
// Animation wait (1.8-2.4s, clusters around 2.1s)
script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(1800, 2400, 2100, 150));

// Short reaction delay (200-400ms, weighted toward faster)
script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

// Retry delay (300-500ms, weighted toward faster)
script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(300, 500));

// Bank loading wait (300-600ms, gaussian around 450ms)
script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(300, 600, 450, 75));
```

**Rule**: Always use `RandomUtils` methods. For animation waits use `gaussianRandom`. For short delays use `weightedRandom`. Never use `script.random()`.

**DEPRECATED**: `script.random()` produces uniform distribution - avoid it entirely.

---

### 9. Stale Inventory Snapshots
```java
// WRONG - Using old snapshot after deposit
ItemGroupResult inv = getInventory().search(Set.of());
int freeSlots = inv.getFreeSlots(); // = 0
bank.depositAll(keepItems);
bank.withdraw(itemID, freeSlots); // Tries to withdraw 0!

// RIGHT - Get fresh snapshot after operations
bank.depositAll(keepItems);
pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 600));
ItemGroupResult inv = getInventory().search(Set.of());
int freeSlots = inv.getFreeSlots(); // = 27
bank.withdraw(itemID, freeSlots);
```

**Rule**: Get fresh snapshots after any inventory/bank changes.

---

### 10. Resources in Wrong Location
```java
// WRONG - Won't be found
// MyScript/resources/logo.png

// RIGHT - Correct location
// MyScript/src/main/resources/logo.png
```

**Rule**: Resources MUST be in `src/main/resources/`

---

### 11. Level 99 Handling in onPaint
```java
// WRONG - Shows "99 (+98)" levels gained
public static int startLevel = 1;  // Never captures level 99!

// RIGHT - Initialize to 0
public static int startLevel = 0;  // Correctly captures starting level
```

**Rule**: Initialize level tracking variables to 0, not 1.

---

### 12. Not Checking Widget Visibility
```java
// WRONG - Assuming object is fully visible
getFinger().tap(objectPoly, "Mine");

// RIGHT - Check visibility first
double visibility = getWidgetManager().insideGameScreenFactor(
    objectPoly, List.of(ChatboxComponent.class)
);
if (visibility >= 0.5) {
    getFinger().tap(objectPoly, "Mine");
}
```

**Rule**: Check visibility before clicking, especially when zoomed out.

---

### 13. Double-Tap Bug with tapGetResponse
```java
// WRONG - Causes double interaction (opens menu, then taps again)
MenuEntry response = getFinger().tapGetResponse(true, bounds);
if (response != null && response.getAction().contains("Pick")) {
    getFinger().tap(bounds, response.getAction()); // BUG: taps again after menu already open!
}

// CORRECT - When you know the action, just tap directly (opens menu + selects in one step)
getFinger().tap(bounds, "Pick");

// CORRECT - When you need to verify what's there first, don't tap after
MenuEntry response = getFinger().tapGetResponse(true, bounds);
if (response != null && response.getAction().contains("Pick")) {
    // menu is already open, action was checked - don't tap again
    log("Found pickable item");
}
```

**Rule**: When speed isn't critical, prefer direct `tap(shape, "Action")`. It's safer, cleaner, and avoids the double-tap bug. Only use `tapGetResponse` when you genuinely need to check what action is available before deciding what to do.

---

## Debug Logging Best Practices

### Comprehensive Debug Pattern
```java
// Example from tGemCutter bank debugging
task = "Get bank snapshot";

String gemName = script.getItemManager().getItemName(selectedUncutGemID);
script.log(getClass(), "Searching for " + gemName + " (ID: " + selectedUncutGemID + ") in bank");

// Check if bank has any items at all
ItemGroupResult allBankItems = script.getWidgetManager().getBank().search(Collections.emptySet());
if (allBankItems != null) {
    script.log(getClass(), "Bank has items, checking for gem...");
} else {
    script.log(getClass(), "Bank search returned null! Bank might not be ready.");
    return false;
}

// Search for specific gem
ItemGroupResult bankSnapshot = script.getWidgetManager().getBank().search(Set.of(selectedUncutGemID));

// Log the result
if (bankSnapshot.contains(selectedUncutGemID)) {
    int gemCount = bankSnapshot.getAmount(selectedUncutGemID);
    script.log(getClass(), "Found " + gemCount + " " + gemName + " in bank");
} else {
    script.log(getClass(), "bankSnapshot.contains() returned false for ID " + selectedUncutGemID);
}
```

### Debug Checklist
When troubleshooting, add logging for:
1. **What you're searching for** (item name + ID)
2. **Intermediate states** (bank has items? dialogue visible?)
3. **Actual results** (found X items, got action Y)
4. **Failure points** (why did this branch execute?)

---

## Common Dialogue Issues

### Problem: Dialogue Not Appearing
```java
// Add comprehensive logging
if (!workbench.interact("Build")) {
    log(getClass(), "Failed to interact with workbench");
    return false;
}

BooleanSupplier waitForDialogue = () -> {
    DialogueType type = getWidgetManager().getDialogue().getDialogueType();
    log(getClass(), "Current dialogue type: " + type); // DEBUG
    return type == DialogueType.TEXT_OPTION;
};

if (!pollFramesHuman(waitForDialogue, RandomUtils.gaussianRandom(4000, 6000, 5000, 500))) {
    log(getClass(), "Dialogue timeout - expected TEXT_OPTION");
    return false;
}
```

### Problem: Wrong Dialogue Type
```java
// Always log expected vs actual
DialogueType type = getDialogue().getDialogueType();
if (type != DialogueType.ITEM_OPTION) {
    log(getClass(), "Expected ITEM_OPTION but got: " + type);
    return false;
}
```

---

## Anti-Ban Mistakes

### Not Adding Reaction Delays
```java
// WRONG - Robotic instant reaction
if (!rock.exists()) {
    clickNextRock(); // Instant!
}

// RIGHT - Human-like delay
if (!rock.exists()) {
    pollFramesHuman(() -> true, RandomUtils.uniformRandom(200, 600));
    clickNextRock();
}
```

### Clicking Same Exact Spot
```java
// WRONG - Same tile every time
getWalker().walkTo(new WorldPosition(3253, 3420, 0));

// RIGHT - Random within area
RectangleArea area = new RectangleArea(3250, 3418, 3256, 3422, 0);
getWalker().walkTo(area.getRandomPosition());
```

---

## ItemID Constants Issue

### Problem: Doubting the Constants
```java
// Users sometimes think ItemID constants are wrong
// They are CORRECT - always use them!

// GOOD - Use ItemID constants
private static final Integer[] GEM_OPTIONS = {
    ItemID.UNCUT_SAPPHIRE,  // = 1607 (correct!)
    ItemID.UNCUT_EMERALD,   // = 1605 (correct!)
    ItemID.UNCUT_RUBY,      // = 1619 (correct!)
};
```

**Reference**: https://osmb.co.uk/javadocs - All ItemID constants are accurate.

---

## Gem Cutting Specific Issues

### Dialogue Shows UNCUT Gem
```java
// WRONG - Trying to select cut gem in dialogue
dialogue.selectItem(ItemID.RUBY); // Fails!

// CORRECT - Dialogue shows UNCUT gem
dialogue.selectItem(ItemID.UNCUT_RUBY); // Works!
```

**Why**: Gem cutting dialogue asks "Which gem to cut?" showing UNCUT sprite.

---

## Performance Issues

### Script Running Slowly
1. **Check region priorities** - Missing regions cause 90+ second startup
2. **Reduce paint complexity** - Don't render heavy graphics
3. **Avoid excessive logging** - Remove debug logs in production
4. **Check for infinite loops** - Use proper waiting patterns

### High CPU Usage
1. **Don't poll too frequently** - Return reasonable sleep times (400-800ms)
2. **Optimize onNewFrame** - Keep it minimal and fast
3. **Cache repeated calculations** - Don't recalculate every frame
4. **Use efficient data structures** - Avoid O(n²) operations

---

## When Items Aren't Detected

### Debug Steps
1. **Use Debug Tool** - Find actual item sprite
2. **Check HSL tolerance** - Try 25+ for all three values
3. **Sample pixels** - Use debug tool to get exact colors
4. **Check lighting** - Test in different game areas
5. **Verify comparator** - Create custom if needed

### If Still Not Working
```java
// Some items need cache updates from OSMB
// Check Discord for cache update announcements
// Or use alternative detection (menus, overlays, etc.)
```

---

## Stuck Script Debugging

### Add State Logging
```java
@Override
public int poll() {
    State state = getState();
    log("Current state: " + state); // Always log state
    
    switch (state) {
        case GATHERING: return gather();
        case BANKING: return bank();
        default: return 600;
    }
}
```

### Track Position Changes
```java
private WorldPosition lastPosition = null;

@Override
public int poll() {
    WorldPosition current = getWorldPosition();
    if (current != null && !current.equals(lastPosition)) {
        log("Position changed: " + current);
        lastPosition = current;
    }
    
    // Rest of logic
}
```

---

## Best Practices Summary

1. **Always null-check** API return values
2. **Use pollFramesUntil/pollFramesHuman** not while loops (avoid deprecated submitTask)
3. **onNewFrame is READ-ONLY** - no actions
4. **Add 300-500ms delay** after opening bank
5. **Get fresh snapshots** after inventory/bank changes
6. **Resources in src/main/resources/**
7. **Initialize level tracking to 0**
8. **Verify visually** not via collision map
9. **Add comprehensive logging** when debugging
10. **Test edge cases** (level 99, full bank, etc.)
11. **Fixed delays**: `pollFramesUntil(() -> false, ms)` - never `() -> true` which exits immediately!
12. **Humanized delays**: `pollFramesHuman(() -> true, ms)` - adds natural variance
13. **Use gaussianRandom** for human-like timing on animations and action delays

---

## Quick Debugging Workflow

1. **Identify the failure point** - Where does it break?
2. **Add logging** - What's the actual state vs expected?
3. **Check nulls** - Is something unexpectedly null?
4. **Verify timing** - Did we wait long enough?
5. **Test manually** - Does it work when you do it?
6. **Review documentation** - Are we using the API correctly?
7. **Ask in Discord** - Community knowledge base

---

*Most bugs are timing issues, null checks, or misunderstanding the color bot paradigm.*
