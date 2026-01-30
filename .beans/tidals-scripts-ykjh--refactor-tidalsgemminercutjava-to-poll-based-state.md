---
# tidals-scripts-ykjh
title: Refactor TidalsGemMiner/Cut.java to poll-based states
status: completed
type: feature
priority: normal
created_at: 2026-01-28T08:45:36Z
updated_at: 2026-01-28T09:21:59Z
parent: apzr
---

# Refactor GemMiner Cut.java to Poll-Based States

Review and refactor gem cutting logic to clean state machine pattern.

## Checklist

- [x] Read current Cut.java
- [x] Identify if refactoring needed
- [x] If needed: separate states for chisel selection, gem selection, cutting animation
- [x] Test cutting flow

## Changes Made

1. Added `CutState` enum with 6 states: IDLE, USING_ITEMS, WAIT_DIALOGUE, SELECT_GEM, CUTTING, DROP_CRUSHED
2. Added state tracking fields: `currentState`, `currentGemId`, `lastGemCount`, `cuttingTimer`
3. Converted `execute()` from while-loop to state machine dispatch
4. Created separate handler methods for each state:
   - `handleIdle()` - finds next gem to cut
   - `handleUsingItems()` - uses chisel on gem
   - `handleWaitDialogue()` - waits for item dialogue
   - `handleSelectGem()` - selects gem in dialogue
   - `handleCutting()` - monitors cutting progress
   - `handleDropCrushed()` - drops ONE crushed gem per poll
5. Removed old blocking methods: `interactWithItems()`, `waitUntilFinishedCrafting()`, `dropCrushedGems()`
6. Build verified successful