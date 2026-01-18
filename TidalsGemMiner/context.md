# TidalsGemMiner Development Context

## Session Summary
This document captures the changes made during development sessions for context handoff.

---

## Session 3 - Upper Mine Spam Click Bug Fix

### 15. tapGemRock Not Actually Tapping (Mine.java)
**Problem**: In `tapGemRock()`, `tapGetResponse(false, ...)` only PEEKS at the menu entry without actually tapping. The script thought it clicked but nothing happened, causing rapid looping.

**Solution**: Line 289-290 changed from:
```java
return script.submitHumanTask(() -> {
    MenuEntry response = script.getFinger().tapGetResponse(false, targetHull);
    if (response == null) {
        return false;
    }
    String action = response.getAction();
    String name = response.getEntityName();
    return action != null && name != null &&
            "mine".equalsIgnoreCase(action) &&
            TARGET_OBJECT_NAME.equalsIgnoreCase(name);
}, 2_000);
```
To:
```java
// use direct tap instead of tapGetResponse to actually click the rock
return script.getFinger().tap(targetHull, "Mine");
```

### 16. Track Failed Mining Attempts Without "No Ore" Message (Mine.java)
**Problem**: Rocks that failed to mine without showing "no ore" message weren't being marked as empty, causing the script to retry them indefinitely.

**Solution**: Added new check at lines 199-208 after the "no ore" message check:
```java
// upper mine: if mining failed without "no ore" message, still mark as suspicious
if (isUpperMine && !mined && rockPos != null) {
    script.log(getClass(), "mining failed without message, marking position suspicious: " + rockPos);
    emptyRockPositions.add(rockPos);
    consecutiveNoOreCount++;
    script.pollFramesUntil(() -> false, script.random(600, 1000), true);
    return false;
}
```

### 17. Delay on Failed Tap (Mine.java)
**Problem**: When `tapGemRock()` returned false, the script immediately looped back with no delay.

**Solution**: Added delay at lines 177-180:
```java
if (!tapGemRock(targetRock)) {
    // brief delay to prevent spam clicking on failed taps
    script.pollFramesUntil(() -> false, script.random(300, 500), true);
    return false;
}
```

### 18. Walk to Mine Logic Restructured (Mine.java)
**Problem**: Mining area check ran BEFORE finding rocks. If player stepped slightly outside the 20x20 area, script walked to center even with visible rocks nearby.

**Solution**: Restructured execute() logic:
1. Find rocks first (ObjectManager + color detection)
2. If rocks found, mine them regardless of area position
3. Only walk to mining area if NO rocks visible AND player is outside mining area
4. If in mining area but no rocks, wait/hop as before

### 19. Updated Gem Rock Color (Mine.java)
**Problem**: Old color value (-7990908) wasn't detecting rocks reliably.

**Solution**: Updated `GEM_ROCK_COLOR` constant from `-7990908` to `-9105036` with tolerance 10.

---

## Session 2 - Code Review Fixes & Upper Mine Improvements

### 7. RetryUtils for Menu Interactions (Bank.java)
**Problem**: Direct `.interact()` calls don't retry on failure, violating CLAUDE.md section 8.

**Solution**:
- Line 181: Changed `depositObject.interact(action)` to `RetryUtils.objectInteract(script, depositObject, action, objectName + " interact")`
- Line 221: Changed `gem.interact("Deposit-All")` to `RetryUtils.inventoryInteract(script, gem, "Deposit-All", "deposit gem " + gemId)`
- Added `import utilities.RetryUtils;`

### 8. RetryUtils for Menu Interactions (Cut.java)
**Problem**: Direct `.interact()` calls for chisel/gem and drop actions.

**Solution**:
- Line 216: Changed to `RetryUtils.inventoryInteract(script, inv.getRandomItem(firstID), "Use", "use first item")`
- Line 224: Changed to `RetryUtils.inventoryInteract(script, inv.getRandomItem(secondID), "Use", "use second item")`
- Line 307: Changed to `RetryUtils.inventoryInteract(script, inventory.getRandomItem(CRUSHED_GEM_ID), "Drop", "drop crushed gem")`
- Added `import utilities.RetryUtils;`

### 9. Cut Task Waiting Too Long (Cut.java)
**Problem**: When cutting opals, script waited 70+ seconds after all gems were cut because `inv == null` returned `false` (keep waiting) instead of `true` (done).

**Solution**: Line 274 changed from:
```java
if (inv == null) return false;
return !inv.contains(consumedID);
```
To:
```java
return inv == null || !inv.contains(consumedID);
```

### 10. Removed "Faster but less profitable" Text (ScriptUI.java)
**Problem**: User requested removal of the text hint.

**Solution**: Line 248 changed from:
```java
label.setText("Banks raw gems without cutting. Faster but less profitable.");
```
To:
```java
label.setText("Banks raw gems without cutting.");
```

### 11. Deposit All for Non-Cutting Mode (Bank.java)
**Problem**: When cutting is disabled, no need to deposit individually since there's no chisel to preserve.

**Solution**: Lines 145-154 changed to:
```java
if (cuttingEnabled) {
    // deposit cut gems only (keep chisel)
    depositGemsIndividually(depositBox, CUT_GEM_IDS);
} else {
    // no chisel needed, deposit everything
    depositBox.depositAll(Set.of());
}
```

### 12. Mine Task Not Walking Back to Mine (Mine.java)
**Problem**: After banking in upper mine, script looked for rocks at the bank area, found none, and immediately world hopped instead of walking back to the mine.

**Solution**: Added mining area check at lines 69-75:
```java
if (!selectedLocation.miningArea().contains(myPos)) {
    task = "Walking to mine";
    script.log(getClass(), "walking to mining area");
    script.getWalker().walkTo(selectedLocation.minePosition(), new WalkConfig.Builder().build());
    return false;
}
```

### 13. Upper Mine Empty Rock Detection (Mine.java)
**Problem**: In upper mine, when another player mines a rock and you don't see it, there's no respawn timer visible. The script spam-clicks these "ghost" empty rocks repeatedly because it thinks they're valid.

**Solution**: Implemented chatbox + color detection system:

**New constants:**
```java
private static final String NO_ORE_MESSAGE = "there is currently no ore available in this rock";
private static final int GEM_ROCK_COLOR = -7990908;
private static final int COLOR_TOLERANCE = 10;
private static final int CLUSTER_MAX_DISTANCE = 5;
private static final int CLUSTER_MIN_SIZE = 20;
```

**New tracking fields:**
```java
private Set<WorldPosition> emptyRockPositions = new HashSet<>();
private int consecutiveNoOreCount = 0;
```

**New methods:**
- `checkForNoOreMessage()` - Checks chatbox for "no ore available" message
- `findGemRocksByColor()` - Uses pixel cluster detection to find gem rocks by RGB color
- `findNearestPoint()` - Finds nearest screen point to player
- `isPositionMarkedEmpty()` - Distance-based check (1.5 tiles) for marked empty positions

**Logic changes:**
1. After mining attempt, check chatbox for "no ore" message
2. If detected, mark rock position as empty and wait 800-1200ms before next attempt
3. Filter out rocks within 1.5 tiles of any marked empty position
4. When all ObjectManager rocks filtered out, try color detection fallback
5. If color detection finds nothing, world hop
6. Clear empty positions on successful mine or world hop

### 14. Discord Post Created
**File**: `TidalsGemMiner/discord_post.md`

Created Discord release post with:
- Two mining locations (Upper/Underground)
- Optional gem cutting feature
- Smart banking logic
- Requirements and setup instructions
- Tips and troubleshooting

---

## Session 1 - Initial Fixes

### 1. Deposit Box Logic (Bank.java)
**Problem**: Using `depositBox.depositAll()` was depositing the chisel along with gems.

**Solution**:
- Added `CUT_GEM_IDS` array with all cut gem item IDs (lines 36-45)
- Created `depositGemsIndividually()` method that deposits each gem type one at a time using `interact("Deposit-All")` (lines 205-227)
- When cutting enabled: deposits cut gems only
- When cutting disabled: deposits uncut gems only

### 2. Walk After Deposit Removed (Bank.java)
**Problem**: After depositing, script was walking to a tile near the ladder instead of letting Mine task handle navigation.

**Solution**: Removed the `walkTo(minePos)` call after depositing (line 164). Mine task now handles walking to rocks.

### 3. Cut Task Workflow Rewrite (Cut.java)
**Problem**: Script was mining one gem, cutting it, depositing, and repeating inefficiently.

**Solution**: Completely rewrote the cutting workflow:
- `activate()` (lines 66-84): ONLY activates when inventory is FULL and has uncut gems
- `execute()` (lines 87-152):
  1. Walks to bank area first (if > 10 tiles away)
  2. Loops through ALL gem types and cuts them all in one session
  3. Only drops crushed gems AFTER all cutting is complete

### 4. Bank Activate Logic for Cutting Mode (Bank.java)
**Problem**: After cutting and dropping crushed gems, inventory isn't full anymore, so Bank wouldn't activate.

**Solution**: Updated `activate()` (lines 51-106):
- Cutting disabled: requires full inventory
- Cutting enabled: activates when has cut gems AND no uncut gems remaining (regardless of inventory fullness)

### 5. Paint Overlay - Total XP Display (TidalsGemMiner.java)
**Problem**: Only showed XP/hr, not total XP gained.

**Solution**: Added total XP lines to paint overlay:
- "Mining XP" showing `miningXpGained` (line 262)
- "Crafting XP" showing `craftingXpGained` when cutting enabled (line 272)
- Updated line count calculation for dynamic height (lines 216-218)

### 6. Location Selection Font Color (ScriptUI.java)
**Problem**: Dark font on dark background made location dropdown hard to read.

**Solution**:
- Updated cell factory with white text styling (lines 86-98)
- Added button cell with white text for selected item display (lines 100-112)

---

## Current Workflow

### Cutting Enabled
1. **Mine** until inventory full (27 gems + chisel)
2. **Cut** activates → walks to bank area → cuts ALL gem types in loop → drops ALL crushed gems
3. **Bank** activates → deposits cut gems individually (keeps chisel) → closes deposit box
4. **Mine** activates → walks back to mining area → returns to mining

### Cutting Disabled
1. **Mine** until inventory full
2. **Bank** activates → deposits ALL items → closes deposit box
3. **Mine** activates → walks back to mining area → returns to mining

### Upper Mine World Hopping
1. Rocks filtered by respawn circles AND marked empty positions
2. If no valid rocks, try color detection (RGB -7990908)
3. If color detection finds rocks, click them
4. If rock gives "no ore" message, mark position and wait before retry
5. If no rocks found at all, world hop (clears empty position tracking)

---

## File Changes Summary

| File | Session 1 Changes | Session 2 Changes | Session 3 Changes |
|------|-------------------|-------------------|-------------------|
| `Bank.java` | Deposit gems individually, updated activate(), removed walk back | RetryUtils for interactions, depositAll() for non-cutting mode | - |
| `Cut.java` | Walk to bank first, cut ALL gems, drop crushed at end | RetryUtils for interactions, fixed waiting too long bug | - |
| `Mine.java` | - | Walking back to mine, chatbox detection, color detection, empty rock tracking | Fixed tapGemRock, track failed attempts, delays, restructured walk logic, updated color |
| `TidalsGemMiner.java` | Added Mining/Crafting XP display | - | - |
| `ScriptUI.java` | White text for dropdown | Removed "Faster but less profitable" text | - |
| `discord_post.md` | - | Created Discord release post | - |

---

## Build Command
```bash
osmb build TidalsGemMiner
```

Output: `/Users/zaffre/Documents/Engineering/Projects/Scripts-Project/TidalsGemMiner/TidalsGemMiner/jar/TidalsGemMiner.jar`
