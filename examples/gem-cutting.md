# Gem Cutting Scripts - Specific Lessons

Critical differences and solutions from tGemCutter development.

## Critical Differences from Other Crafting Scripts

Gem cutting has unique dialogue behavior that differs from other crafting activities.

---

## 1. Dialogue Shows UNCUT Gem, Not Cut Gem

### The Problem
```java
// WRONG - This will fail!
DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
if (dialogueType == DialogueType.ITEM_OPTION) {
    boolean selected = script.getWidgetManager().getDialogue().selectItem(selectedCutGemID);
    // ^ Trying to select "Ruby" but dialogue shows "Uncut ruby"!
}
```

### The Solution
```java
// CORRECT - Select the UNCUT gem
if (dialogueType == DialogueType.ITEM_OPTION) {
    boolean selected = script.getWidgetManager().getDialogue().selectItem(selectedUncutGemID);
    // ^ Dialogue shows "Uncut ruby", so select that!
}
```

### Why This Happens
- Scripts like dAmethystCutter ask "Which item to make?" (bolt tips, arrowtips, javelin heads)
- Gem cutting asks "How many gems to cut?" with the **UNCUT** gem image shown
- The dialogue is confirming which gem to cut, not which product to make

---

## 2. Bank Withdrawal Timing Issue

### The Problem
```java
// WRONG - Calculating empty slots before depositing!
ItemGroupResult inventorySnapshot = getWidgetManager().getInventory().search(Collections.emptySet());
int emptySlots = inventorySnapshot.getFreeSlots(); // = 0 (inventory full of cut gems!)

// Deposit cut gems
bank.depositAll(Set.of(ItemID.CHISEL));

// Try to withdraw with stale empty slots count
bank.withdraw(selectedUncutGemID, emptySlots); // Tries to withdraw 0 items!
```

### The Solution
```java
// CORRECT - Recalculate after depositing
// Deposit cut gems
bank.depositAll(Set.of(ItemID.CHISEL));

// Wait for deposit to complete
script.pollFramesHuman(() -> false, script.random(300, 600));

// Get FRESH inventory snapshot after deposit
ItemGroupResult inventorySnapshot = getWidgetManager().getInventory().search(Collections.emptySet());
int emptySlots = inventorySnapshot.getFreeSlots(); // = 27 (correct!)

// Now withdraw correct amount
bank.withdraw(selectedUncutGemID, emptySlots);
```

### Why This Matters
If you try to withdraw 0 items, OSRS shows a quantity selection menu which breaks the script flow.

---

## 3. ItemID Constants Are Correct - Use Them!

### Don't Second-Guess the API
```java
// GOOD - Use ItemID constants from the API
private static final Integer[] GEM_OPTIONS = {
    ItemID.UNCUT_SAPPHIRE,  // = 1607
    ItemID.UNCUT_EMERALD,   // = 1605
    ItemID.UNCUT_RUBY,      // = 1619
    ItemID.UNCUT_DIAMOND    // = 1617
};

private static final Map<Integer, Integer> UNCUT_TO_CUT = Map.of(
    ItemID.UNCUT_SAPPHIRE, ItemID.SAPPHIRE,
    ItemID.UNCUT_EMERALD, ItemID.EMERALD,
    ItemID.UNCUT_RUBY, ItemID.RUBY,
    ItemID.UNCUT_DIAMOND, ItemID.DIAMOND
);
```

**Reference**: https://osmb.co.uk/javadocs - All ItemID constants match their numeric values exactly.

---

## Complete Gem Cutting Banking Pattern

```java
private boolean handleBanking() {
    Bank bank = script.getWidgetManager().getBank();
    
    // Step 1: Open bank
    if (!bank.isVisible()) {
        RSObject bankBooth = script.getObjectManager().getClosestObject(
            script.getWorldPosition(), "Bank booth"
        );
        if (bankBooth != null && bankBooth.interact("Bank")) {
            script.submitTask(() -> bank.isVisible(), 5000);
        }
        return false;
    }
    
    // Step 2: Wait for bank to load
    script.pollFramesHuman(() -> false, script.random(300, 500));
    
    // Step 3: Deposit cut gems (keep chisel)
    bank.depositAll(Set.of(ItemID.CHISEL));
    
    // Step 4: Wait for deposit to complete
    script.pollFramesHuman(() -> false, script.random(300, 600));
    
    // Step 5: Get FRESH inventory snapshot
    ItemGroupResult inventorySnapshot = script.getWidgetManager()
        .getInventory().search(Collections.emptySet());
    int emptySlots = inventorySnapshot.getFreeSlots();
    
    // Step 6: Check bank for uncut gems
    script.log(getClass(), "Checking bank for uncut gems...");
    ItemGroupResult bankSnapshot = bank.search(Set.of(selectedUncutGemID));
    
    if (bankSnapshot == null || !bankSnapshot.contains(selectedUncutGemID)) {
        script.log(getClass(), "Out of uncut gems!");
        script.stop();
        return false;
    }
    
    // Step 7: Withdraw uncut gems
    bank.withdraw(selectedUncutGemID, emptySlots);
    
    // Step 8: Wait for withdrawal
    script.pollFramesHuman(() -> false, script.random(300, 600));
    
    // Step 9: Close bank
    bank.close();
    
    return true;
}
```

---

## Complete Gem Cutting Dialogue Pattern

```java
private boolean startCrafting() {
    // Step 1: Get uncut gem from inventory
    ItemGroupResult inv = script.getWidgetManager().getInventory()
        .search(Set.of(selectedUncutGemID, ItemID.CHISEL));
    
    if (!inv.contains(selectedUncutGemID)) {
        script.log(getClass(), "No uncut gems in inventory!");
        return false;
    }
    
    if (!inv.contains(ItemID.CHISEL)) {
        script.log(getClass(), "No chisel in inventory!");
        return false;
    }
    
    // Step 2: Click chisel on uncut gem
    SearchableItem chisel = inv.getItem(ItemID.CHISEL);
    SearchableItem gem = inv.getItem(selectedUncutGemID);
    
    if (chisel != null && gem != null) {
        chisel.useOn(gem);
    } else {
        return false;
    }
    
    // Step 3: Wait for ITEM_OPTION dialogue
    boolean dialogueAppeared = script.pollFramesUntil(() -> {
        DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
        return type == DialogueType.ITEM_OPTION;
    }, script.random(4000, 6000));
    
    if (!dialogueAppeared) {
        script.log(getClass(), "Dialogue did not appear!");
        return false;
    }
    
    // Step 4: Select the UNCUT gem (not cut gem!)
    DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
    if (type == DialogueType.ITEM_OPTION) {
        // CRITICAL: Select UNCUT gem ID, not cut gem ID!
        boolean selected = script.getWidgetManager().getDialogue()
            .selectItem(selectedUncutGemID);
        
        if (!selected) {
            script.log(getClass(), "Failed to select gem in dialogue!");
            return false;
        }
    }
    
    return true;
}
```

---

## Debug Logging for Gem Cutting

When troubleshooting gem cutting issues, use comprehensive logging:

```java
String gemName = script.getItemManager().getItemName(selectedUncutGemID);
script.log(getClass(), "Searching for " + gemName + " (ID: " + selectedUncutGemID + ") in bank");

// Check if bank has any items
ItemGroupResult allBankItems = bank.search(Collections.emptySet());
if (allBankItems != null) {
    script.log(getClass(), "Bank has items, checking for gem...");
} else {
    script.log(getClass(), "Bank search returned null! Bank might not be ready.");
    return false;
}

// Search for specific gem
ItemGroupResult bankSnapshot = bank.search(Set.of(selectedUncutGemID));

if (bankSnapshot.contains(selectedUncutGemID)) {
    int gemCount = bankSnapshot.getAmount(selectedUncutGemID);
    script.log(getClass(), "Found " + gemCount + " " + gemName + " in bank");
} else {
    script.log(getClass(), "bankSnapshot.contains() returned false for ID " + selectedUncutGemID);
}
```

---

## Common Gem Cutting Mistakes

### 1. Selecting Cut Gem Instead of Uncut
**Problem**: Dialogue shows uncut gem sprite  
**Solution**: Always select the uncut gem ID in dialogue

### 2. Withdrawing Before Deposit Completes
**Problem**: Stale inventory snapshot shows 0 free slots  
**Solution**: Wait 300-600ms after deposit, then get fresh snapshot

### 3. Not Waiting for Bank to Load
**Problem**: Bank search returns null or doesn't find items  
**Solution**: Add 300-500ms delay after bank opens

### 4. Wrong ItemID Constants
**Problem**: Using wrong IDs from outdated sources  
**Solution**: Always use ItemID constants from OSMB API javadocs

---

## Gem Cutting Checklist

When creating a gem cutting script, verify:
- [ ] Select UNCUT gem in dialogue (not cut gem)
- [ ] Wait 300-500ms after opening bank
- [ ] Deposit cut gems first
- [ ] Wait 300-600ms after deposit
- [ ] Get FRESH inventory snapshot before withdrawing
- [ ] Withdraw correct amount (emptySlots)
- [ ] Handle "out of gems" gracefully
- [ ] Use correct ItemID constants from API

---

## Key Takeaways

1. **Gem cutting dialogue is unique** - it shows the uncut gem, not the product
2. **Timing is critical** - always wait between bank operations
3. **Fresh snapshots are essential** - never reuse stale inventory state
4. **ItemID constants are correct** - trust the API javadocs
5. **Debug logging helps** - comprehensive logging catches issues quickly

---

*The key difference: Most crafting shows the PRODUCT. Gem cutting shows the MATERIAL.*
