# TidalsGemMiner Development Context

## Session Summary
This document captures the changes made during the current development session for context handoff.

---

## Issues Fixed

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

## Current Workflow (Cutting Enabled)

1. **Mine** until inventory full (27 gems + chisel)
2. **Cut** activates → walks to bank area → cuts ALL gem types in loop → drops ALL crushed gems
3. **Bank** activates → deposits cut gems individually → closes deposit box
4. **Mine** activates → returns to mining

---

## Known Issues / Observations

### XP Tracking
- Crafting XP tracks correctly
- Mining XP may not track in some cases
- Both use identical code pattern (lines 117-118 for init, 136-140 for reading)
- If mining XP doesn't track, likely an OSMB XPTracker initialization issue

### Task Order (TidalsGemMiner.java lines 108-112)
```java
tasks.add(new Setup(this));
tasks.add(new Cut(this));
tasks.add(new Bank(this));
tasks.add(new Mine(this));
```

---

## File Changes Summary

| File | Changes |
|------|---------|
| `Bank.java` | Deposit gems individually, updated activate() for cutting mode, removed walk back |
| `Cut.java` | Walk to bank first, cut ALL gems in loop, drop crushed at end only, strict full inventory check |
| `TidalsGemMiner.java` | Added Mining XP and Crafting XP total display lines |
| `ScriptUI.java` | White text for location dropdown |

---

## Build Command
```bash
osmb build TidalsGemMiner
```

Output: `/Users/zaffre/Documents/Engineering/Projects/Scripts-Project/TidalsGemMiner/TidalsGemMiner/jar/TidalsGemMiner.jar`
