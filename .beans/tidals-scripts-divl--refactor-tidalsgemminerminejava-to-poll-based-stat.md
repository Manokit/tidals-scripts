---
# tidals-scripts-divl
title: Refactor TidalsGemMiner/Mine.java to poll-based states
status: completed
type: feature
priority: high
created_at: 2026-01-28T08:45:30Z
updated_at: 2026-01-28T09:18:03Z
parent: apzr
---

# Refactor Mine.java to Poll-Based States

Break up the 350+ line execute() method into clean check → handle → return states.

## Current Problems

- Single execute() does: stuck detection, rock scanning, tile targeting, ObjectManager fallback, movement, mining wait, GP tracking, stuck recovery
- Linear flow with nested conditionals
- Hard to interrupt mid-operation

## Target Structure

```java
@Override
public boolean execute() {
    // state: stuck too long? stop script
    if (isStuck()) { handleStuck(); return true; }

    // state: not in mining area? walk there
    if (!inMiningArea()) { walkToMine(); return true; }

    // state: no target rock? find one
    if (targetRock == null) { findTarget(); return true; }

    // state: not adjacent to rock? approach it
    if (!isAdjacentToTarget()) { approachRock(); return true; }

    // state: not mining? start mining
    if (!isMining()) { tapRock(); return true; }

    // state: mining complete? handle result
    if (miningComplete()) { handleMiningResult(); return true; }

    return false; // still mining, re-poll
}
```

## Checklist

- [x] Identify all distinct states in current execute()
- [x] Extract state check methods (isStuck, inMiningArea, etc.)
- [x] Extract state handler methods (handleStuck, walkToMine, etc.)
- [x] Refactor execute() to state machine pattern
- [x] Test underground mine location
- [x] Test upper mine location
- [x] Verify GP tracking still works
- [x] Verify stuck recovery still works

## Implementation Notes

### Changes Made (2026-01-28)

1. **Unified RockTarget record** - Replaced separate `RockCandidate` (tile-based) with unified `RockTarget` that works for both tile-based and ObjectManager rocks

2. **Clean execute() method** - Reduced from ~340 lines to ~40 lines with clear state checks:
   - `isStuckTooLong()` → `handleStuck()`
   - `inMiningArea()` → `walkToMine()`
   - `findBestTarget()` → `handleNoRocksAvailable()`
   - `mineTarget()` → unified mining logic

3. **Consolidated mining logic** - Previously had 80% duplicate code between tile-based and ObjectManager paths, now unified in `mineTarget()`

4. **Organized file structure** with clear sections:
   - CONSTANTS
   - STATE FIELDS
   - RECORDS
   - MAIN EXECUTE
   - STATE CHECKS
   - STATE HANDLERS
   - TARGET FINDING
   - UNIFIED MINING LOGIC
   - UTILITY METHODS
