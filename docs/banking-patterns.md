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
        return submitTask(() -> bank.isVisible(), 5000);
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
script.pollFramesHuman(() -> false, script.random(300, 500));

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
script.pollFramesHuman(() -> false, script.random(300, 600));

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

## Idle Detection During Walking to Bank

```java
// Track position changes to detect if player got stuck
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

---

## Deposit Box (Alternative to Banks)

**Use Case**: Mining areas, certain quests, activities without full bank access.

```java
private boolean handleDepositBox() {
    DepositBox depositBox = getWidgetManager().getDepositBox();

    if (!depositBox.isVisible()) {
        RSObject box = getObjectManager().getClosestObject(
            getWorldPosition(), "Bank deposit box"
        );
        if (box != null && box.interact("Deposit")) {
            submitTask(() -> depositBox.isVisible(), 5000);
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
            submitTask(() -> bank.isVisible(), 5000);
        }
        return 600;
    }
    
    // Step 2: Wait for bank to load
    pollFramesHuman(() -> false, random(300, 500));
    
    // Step 3: Deposit all except pickaxe
    Set<Integer> keepItems = Set.of(PICKAXE_ID);
    bank.depositAll(keepItems);
    
    // Step 4: Wait for deposit, then verify
    pollFramesHuman(() -> false, random(300, 600));
    
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

## Best Practices

1. **Always wait 300-500ms after opening bank** before searching
2. **Get fresh snapshots** after any deposit/withdraw operation
3. **Verify operations succeeded** by checking inventory/bank state
4. **Check visibility** before clicking bank objects
5. **Use depositAll with keepItems** instead of manual item selection
6. **Handle edge cases** like full bank, missing items, etc.
