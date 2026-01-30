---
# tidals-scripts-j6v6
title: Refactor TidalsGoldSuperheater tasks to poll-based states
status: completed
type: feature
priority: normal
created_at: 2026-01-28T08:45:37Z
updated_at: 2026-01-28T09:27:54Z
parent: apzr
---

# Refactor TidalsGoldSuperheater to Poll-Based States

Review and refactor Process.java and Bank.java to clean state machine pattern.

## Checklist

- [x] Read current Process.java
- [x] Read current Bank.java
- [x] Identify if refactoring needed (may already be clean per audit)
- [x] If needed: refactor to state machine pattern
- [x] Test superheating flow (build verified successful)

## Changes Made

### Process.java
- **Removed the for-loop** that processed all ores in a single execute() call
- Now processes **ONE ore per poll cycle** via `superheatOneOre()` method
- Added early-return state checks (bank open → close, level up → handle)
- Uses `getRandomItem()` instead of slot-based approach for cleaner code
- Level up handling moved to top of execute() as a state check

### Bank.java
- Added state machine pattern with early returns after each operation:
  1. Bank not visible → open it → return
  2. Have gold bars → deposit → return
  3. Need ore → check supply → withdraw → return
  4. Done → close bank → return
- Removed chained sequential operations
- Added explicit `GOLD_BAR` check for deposit state
