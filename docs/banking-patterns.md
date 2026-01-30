# Banking Patterns

Complete guide to banking, inventory management, and deposits.

## Safe Banking Pattern

```java
private boolean openBank() {
    Bank bank = getWidgetManager().getBank();
    if (bank.isVisible()) return true;
    
    RSObject bankBooth = getObjectManager().getClosestObject(
        getWorldPosition(), "Bank booth", "Bank chest"
    );
    
    if (bankBooth != null && bankBooth.interact("Bank")) {
        return pollFramesUntil(() -> bank.isVisible(), 5000);
    }
    return false;
}

private boolean depositAllExcept(Set<Integer> keepItems) {
    Bank bank = getWidgetManager().getBank();
    if (!bank.isVisible()) return false;
    return bank.depositAll(keepItems);
}
```

---

## Inventory Operations

### Search Inventory
```java
// Search for specific items - pass empty set to get all items
ItemGroupResult snapshot = getWidgetManager().getInventory().search(Set.of());
if (snapshot != null && snapshot.isFull()) {
    // Inventory is full
}
int freeSlots = snapshot.getFreeSlots();

// Search for specific items
ItemGroupResult items = getWidgetManager().getInventory().search(
    Set.of(ItemID.LOGS, ItemID.OAK_LOGS)
);
if (items != null && items.contains(ItemID.LOGS)) {
    int logCount = items.getAmount(ItemID.LOGS);
}
```

### Inventory Methods
```java
Inventory inv = getWidgetManager().getInventory();

inv.isFull();
inv.getFreeSlots();
inv.contains(itemId);
inv.getItem(itemId);
inv.dropItems(itemId1, itemId2); // Drop specific items

// Interact with items
ItemGroupResult result = inv.search(Set.of(ItemID.LOGS));
if (result != null) {
    SearchableItem item = result.getItem(ItemID.LOGS);
    if (item != null) {
        item.interact("Drop");
    }
}
```

---

## Bank Loading Delay (CRITICAL!)

### The Problem
```java
// WRONG - Searching bank immediately after it opens
if (!script.getWidgetManager().getBank().isVisible()) {
    openBank();
    return false;
}

// Next poll - bank just opened, items not loaded yet!
ItemGroupResult bankSnapshot = script.getWidgetManager().getBank().search(Set.of(itemID));
// Returns null or doesn't find items!
```

### The Solution
```java
// CORRECT - Wait for bank to fully load
if (!script.getWidgetManager().getBank().isVisible()) {
    openBank();
    return false;
}

// Give bank a moment to fully load before searching
script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));

// Now search for items
ItemGroupResult bankSnapshot = script.getWidgetManager().getBank().search(Set.of(itemID));
```

---

## Bank Withdrawal Timing Issue

### The Problem
```java
// WRONG - Calculating empty slots before depositing!
ItemGroupResult inventorySnapshot = getWidgetManager().getInventory().search(Collections.emptySet());
int emptySlots = inventorySnapshot.getFreeSlots(); // = 0 (inventory full!)

// Deposit items
bank.depositAll(Set.of(ItemID.CHISEL));

// Try to withdraw with stale empty slots count
bank.withdraw(selectedItemID, emptySlots); // Tries to withdraw 0 items!
```

### The Solution
```java
// CORRECT - Recalculate after depositing
// Deposit items
bank.depositAll(Set.of(ItemID.CHISEL));

// Wait for deposit to complete
script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 600));

// Get FRESH inventory snapshot after deposit
ItemGroupResult inventorySnapshot = getWidgetManager().getInventory().search(Collections.emptySet());
int emptySlots = inventorySnapshot.getFreeSlots(); // = 27 (correct!)

// Now withdraw correct amount
bank.withdraw(selectedItemID, emptySlots);
```

**Why this matters**: If you try to withdraw 0 items, OSRS shows a quantity selection menu which breaks the script flow.

---

## Advanced Banking with Visibility Checks

### Problem
Objects can be partially off-screen or obstructed by UI elements, leading to failed interactions.

### Solution
```java
private boolean openBank() {
    RSObject chest = getObjectManager().getClosestObject(
        getWorldPosition(), "Bank chest"
    );

    if (chest == null) return false;

    Polygon hull = chest.getConvexHull();
    if (hull == null) return false;

    // Check how visible the bank chest is (ignoring chatbox obstruction)
    double insideFactor = getWidgetManager().insideGameScreenFactor(
        hull, List.of(ChatboxComponent.class)
    );

    if (insideFactor < 1.0) {
        // Walk closer if not fully visible
        WalkConfig config = new WalkConfig.Builder()
            .disableWalkScreen(true)
            .breakCondition(() -> {
                Polygon h = chest.getConvexHull();
                return h != null &&
                    getWidgetManager().insideGameScreenFactor(
                        h, List.of(ChatboxComponent.class)) >= 1.0;
            })
            .enableRun(true)
            .build();
        getWalker().walkTo(chest.getWorldPosition(), config);
        return false;
    }

    // Verify all polygon points are within screen bounds before tapping
    if (!isPolygonTapSafe(hull)) {
        return false;
    }

    // Now safe to tap
    getFinger().tap(hull, "Bank");
    return true;
}

private boolean isPolygonTapSafe(Polygon poly) {
    int screenWidth = getScreen().getWidth();
    int screenHeight = getScreen().getHeight();

    for (int i = 0; i < poly.size(); i++) {
        int x = poly.xpoints[i];
        int y = poly.ypoints[i];

        if (x < 0 || x > screenWidth || y < 0 || y > screenHeight) {
            return false;
        }
    }
    return true;
}
```

---

## Movement Stall Detection (MovementChecker)

Use `MovementChecker` from TidalsUtilities to detect when player movement has stalled (misclicks, interrupts, obstacles).

### Why It's Needed

Walking can fail silently:
- Clicked on game object instead of tile
- Path blocked by closed door
- Player interrupted by NPC dialogue
- Misclick landed on UI element

Without timeout detection, scripts get stuck forever.

### Basic Usage

```java
import utilities.MovementChecker;

// Create checker with player's starting position
MovementChecker checker = new MovementChecker(script.getWorldPosition());

// In your polling loop
while (walking) {
    WorldPosition current = script.getWorldPosition();
    if (current == null) continue;

    if (checker.hasTimedOut(current)) {
        // Player hasn't moved for 800-2000ms (randomized)
        script.log(getClass(), "movement stalled - retrying");
        break;
    }

    // Check if arrived
    if (current.distanceTo(destination) < 3) {
        return true;  // success
    }

    script.pollFramesUntil(() -> false, 100);
}
```

### How It Works

1. Records initial position and starts timer
2. Each `hasTimedOut()` call compares current position
3. If position changed → resets timer automatically
4. If position unchanged for `timeout` ms → returns true

### Custom Timeouts

```java
// Default: random 800-2000ms
MovementChecker checker = new MovementChecker(position);

// Custom range: 500-1500ms
MovementChecker checker = new MovementChecker(position, 500, 1500);

// Get the randomized timeout value
int timeout = checker.getTimeout();

// Manual reset (e.g., before starting movement)
checker.reset(newPosition);
```

### Complete Walk-to-Bank Example

```java
private boolean walkToBank() {
    WorldPosition target = BANK_POSITION;
    WorldPosition start = script.getWorldPosition();
    if (start == null) return false;

    // Initiate walking
    script.getWalker().walkTo(target, new WalkConfig.Builder().build());

    // Monitor for stalls
    MovementChecker checker = new MovementChecker(start);

    boolean arrived = script.pollFramesUntil(() -> {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;

        // Check for stall
        if (checker.hasTimedOut(current)) {
            script.log(getClass(), "walk stalled, will retry");
            return true;  // exit poll to retry
        }

        // Check for arrival
        return current.distanceTo(target) < 3;
    }, 30000);

    // Verify we actually arrived (not just stall-exited)
    WorldPosition finalPos = script.getWorldPosition();
    return finalPos != null && finalPos.distanceTo(target) < 3;
}
```

### Legacy Pattern (Not Recommended)

The old approach using `AtomicReference<Timer>` still works but is more verbose:

```java
// Track position changes to detect if player got stuck (LEGACY)
AtomicReference<Timer> positionChangeTimer = new AtomicReference<>(new Timer());
AtomicReference<WorldPosition> pos = new AtomicReference<>(null);

pollFramesHuman(() -> {
    WorldPosition current = getWorldPosition();
    if (current == null) return false;

    if (!current.equals(pos.get())) {
        pos.set(current);
        positionChangeTimer.get().reset(); // Reset if moving
    }

    // Fail if idle for 4+ seconds
    return getWidgetManager().getBank().isVisible()
            || positionChangeTimer.get().timeElapsed() > 4000;
}, 20000);
```

**Prefer `MovementChecker`** - it handles the complexity for you with proper randomization.

---

## Deposit Box (Alternative to Banks)

**Use Case**: Mining areas, certain quests, activities without full bank access.

### Basic Deposit Box Pattern

```java
private boolean handleDepositBox() {
    DepositBox depositBox = getWidgetManager().getDepositBox();

    if (!depositBox.isVisible()) {
        RSObject box = getObjectManager().getClosestObject(
            getWorldPosition(), "Bank deposit box"
        );
        if (box != null && box.interact("Deposit")) {
            pollFramesUntil(() -> depositBox.isVisible(), 5000);
        }
        return false;
    }

    // Deposit all items except tools
    Set<Integer> keepItems = Set.of(ItemID.RUNE_PICKAXE);
    if (!depositBox.depositAll(keepItems)) {
        log(getClass(), "Failed to deposit items.");
        return false;
    }

    depositBox.close();
    return true;
}
```

### Poll-Based Deposit Box Flow

For proper poll-based architecture, check state and perform one action per poll:

```java
@Override
public boolean execute() {
    WorldPosition myPos = script.getWorldPosition();
    if (myPos == null) return false;

    DepositBox depositBox = script.getWidgetManager().getDepositBox();

    // State: Not at deposit box? Walk there
    if (!DEPOSIT_BOX_AREA.contains(myPos)) {
        task = "Walking to deposit box";
        script.getWalker().walkTo(DEPOSIT_BOX_AREA.getRandomPosition());
        return false;
    }

    // State: Deposit box not open? Open it
    if (!depositBox.isVisible()) {
        task = "Opening deposit box";
        RSObject box = script.getObjectManager().getClosestObject(myPos, "Bank deposit box");

        if (box == null) {
            script.log(getClass(), "deposit box not found");
            return false;
        }

        // Visibility check before interacting
        Polygon hull = box.getConvexHull();
        if (hull == null) return false;

        double visibility = script.getWidgetManager().insideGameScreenFactor(
            hull, List.of(ChatboxComponent.class)
        );

        if (visibility < 0.3) {
            // Walk closer
            script.getWalker().walkTo(box.getWorldPosition());
            return false;
        }

        // Interact
        script.getFinger().tapGameScreen(hull, "Deposit");
        script.pollFramesUntil(() -> depositBox.isVisible(), RandomUtils.gaussianRandom(4000, 6000, 5000, 500));
        return false;
    }

    // State: Have items? Deposit them
    ItemGroupResult inv = script.getWidgetManager().getInventory().search(Collections.emptySet());
    if (inv != null && !inv.isEmpty()) {
        task = "Depositing items";
        Set<Integer> keepItems = Set.of(ItemID.RUNE_PICKAXE, ItemID.DRAGON_PICKAXE);
        depositBox.depositAll(keepItems);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 600));
        return false;
    }

    // State: Done depositing, close and move on
    task = "Closing deposit box";
    depositBox.close();
    script.pollFramesUntil(() -> !depositBox.isVisible(), 2000);
    return false;  // task will deactivate when inventory is empty and box is closed
}
```

### Deposit Box vs Regular Bank

| Feature | Deposit Box | Full Bank |
|---------|-------------|-----------|
| Deposit items | ✓ | ✓ |
| Withdraw items | ✗ | ✓ |
| View bank contents | ✗ | ✓ |
| Search function | ✗ | ✓ |
| Locations | Mining areas, specific quest areas | Towns, banks |

**Use deposit box when**: You only need to deposit (mining, woodcutting) and a full bank isn't nearby.

---

## Complete Banking Example

```java
private int bank() {
    Bank bank = getWidgetManager().getBank();
    
    // Step 1: Open bank if not visible
    if (!bank.isVisible()) {
        RSObject bankBooth = getObjectManager().getClosestObject(
            getWorldPosition(), "Bank booth"
        );
        if (bankBooth != null && bankBooth.interact("Bank")) {
            pollFramesUntil(() -> bank.isVisible(), 5000);
        }
        return 600;
    }
    
    // Step 2: Wait for bank to load
    pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 500));
    
    // Step 3: Deposit all except pickaxe
    Set<Integer> keepItems = Set.of(PICKAXE_ID);
    bank.depositAll(keepItems);
    
    // Step 4: Wait for deposit, then verify
    pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 600));
    
    // Step 5: Get fresh inventory snapshot
    ItemGroupResult inv = getWidgetManager().getInventory().search(Set.of(ORE_ID));
    
    // Step 6: Check if deposit was successful
    if (inv == null || inv.getAmount(ORE_ID) == 0) {
        bank.close();
    }
    
    return RandomUtils.uniformRandom(300, 600);
}
```

---

## Common Banking Mistakes

### 1. Not Waiting for Bank to Load
**Problem**: Searching bank immediately after opening  
**Solution**: Add 300-500ms delay after bank opens

### 2. Stale Inventory Snapshots
**Problem**: Using old snapshot after deposit/withdraw  
**Solution**: Always get fresh snapshot after operations

### 3. Withdrawing 0 Items
**Problem**: Triggers quantity selection menu  
**Solution**: Recalculate free slots after depositing

### 4. Bank Not Fully Visible
**Problem**: Clicking off-screen polygon  
**Solution**: Check visibility factor, walk closer if needed

### 5. No Null Checking
**Problem**: NullPointerException on bank object  
**Solution**: Always null-check before operations

---

## Walking to Bank with breakCondition (OSMB Recommended)

When walking to a bank or deposit box, use a `breakCondition` to stop early once the object is loaded in scene.

### Simple Approach: Check Object Exists

Per OSMB: just check if the object is non-null, then let `RSObject::interact` or `RetryUtils` handle the rest.

```java
private WalkConfig buildWalkConfig(String objectName) {
    WalkConfig.Builder walkConfig = new WalkConfig.Builder();
    walkConfig.breakCondition(() -> {
        RSObject target = script.getObjectManager().getClosestObject(
                script.getWorldPosition(), objectName
        );
        if (target != null) {
            script.log(getClass(), "[breakCondition] object found in scene, stopping");
        }
        return target != null;
    });
    return walkConfig.build();
}

private boolean isObjectInScene(String objectName) {
    RSObject target = script.getObjectManager().getClosestObject(
            script.getWorldPosition(), objectName
    );
    return target != null;
}
```

### Alternative: Check Hull Visibility

Use this approach if you're manually tapping coordinates instead of using `RSObject::interact`:

```java
walkConfig.breakCondition(() -> {
    RSObject bank = script.getObjectManager().getClosestObject(myPos, "Bank chest");
    if (bank == null) return false;

    Polygon hull = bank.getConvexHull();
    if (hull == null) return false;

    // Check >30% visible, ignoring chatbox (we can tap through it)
    double visibility = script.getWidgetManager().insideGameScreenFactor(
        hull, List.of(ChatboxComponent.class)
    );
    return visibility >= 0.3;
});
```

### Critical: Check UI State FIRST

**Problem**: When bank/deposit UI is open, the 3D object behind it is blocked by the UI overlay. The visibility check returns false, causing walk spam.

```java
// WRONG - checks object before UI
@Override
public boolean execute() {
    if (!isObjectInScene("Bank Deposit Chest")) {
        walkToBank();  // Spam! UI is open but object "not visible"
        return false;
    }
    // ...
}

// CORRECT - checks UI first
@Override
public boolean execute() {
    DepositBox depositBox = script.getWidgetManager().getDepositBox();

    // Check if UI is already open - skip all walking logic
    if (depositBox.isVisible()) {
        return handleDepositing(depositBox);
    }

    // Now safe to check object visibility
    if (!isObjectInScene("Bank Deposit Chest")) {
        walkToBank();
        return false;
    }

    openDepositBox();
    return false;
}
```

### Complete Walk-to-Bank Pattern

```java
@Override
public boolean execute() {
    // 1. Check if bank UI is already open (skip all walking)
    Bank bank = script.getWidgetManager().getBank();
    if (bank.isVisible()) {
        return handleBanking(bank);
    }

    WorldPosition myPos = script.getWorldPosition();
    if (myPos == null) return false;

    // 2. Check if bank object is in scene (close enough)
    String bankName = "Bank booth";
    if (!isObjectInScene(bankName)) {
        task = "Walking to bank";
        script.getWalker().walkTo(BANK_POSITION, buildWalkConfig(bankName));
        return false;
    }

    // 3. Object in scene - open bank
    task = "Opening bank";
    RSObject bankObj = script.getObjectManager().getClosestObject(myPos, bankName);
    RetryUtils.objectInteract(script, bankObj, "Bank", "open bank");
    return false;
}
```

### Why Simple > Complex

| Approach | Pros | Cons |
|----------|------|------|
| `object != null` | Simple, fast, works with RetryUtils | Might stop slightly further away |
| Hull visibility check | Precise, stops when clickable | Complex, fails when UI open |

**Recommendation**: Use the simple `object != null` check when using `RSObject::interact` or `RetryUtils.objectInteract`. They handle visibility/retrying for you.

---

## Best Practices

1. **Always wait 300-500ms after opening bank** before searching
2. **Get fresh snapshots** after any deposit/withdraw operation
3. **Verify operations succeeded** by checking inventory/bank state
4. **Check visibility** before clicking bank objects
5. **Use depositAll with keepItems** instead of manual item selection
6. **Handle edge cases** like full bank, missing items, etc.
7. **Check UI state first** before checking object visibility (prevents walk spam)
8. **Use breakCondition** to stop walking early when target is reachable
