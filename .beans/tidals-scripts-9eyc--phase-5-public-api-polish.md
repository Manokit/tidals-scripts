---
# tidals-scripts-9eyc
title: 'Phase 5: Public API & Polish'
status: in-progress
type: feature
priority: normal
created_at: 2026-01-31T02:24:49Z
updated_at: 2026-02-01T08:44:22Z
parent: tidals-scripts-6gj8
---

Clean public API facade, configuration, and integration testing.

## Checklist

- [x] Create `TidalsWalker` facade (walkTo, walkToBank, isReachable)
- [x] Create `WalkOptions` builder (allowTeleports, timeout, breakCondition, breakDistance, enableRun, handleObstacles)
- [x] Wire everything together: TidalsWalker → DaxApiClient → PathExecutor → ObstacleDetector → TeleportRegistry
- [x] Update POC test script to use new public API
- [x] Build utilities jar and verify it works from a consuming script
- [ ] Test from 3+ locations (Lumbridge, Falador, Draynor → Varrock)
- [x] Add walkToBank() support (uses /walker/generateBankPaths endpoint)
- [x] Delete DaxWebWalkerPOC (replaced by TidalsWalker)

## Changes Made

- Created `utilities/src/main/java/utilities/webwalker/WalkOptions.java` — fluent builder with 6 config options
- Created `utilities/src/main/java/utilities/webwalker/TidalsWalker.java` — public facade with walkTo, walkToBank, isReachable
- Deleted `utilities/src/main/java/utilities/webwalker/DaxWebWalkerPOC.java` — replaced by TidalsWalker
- Simplified `TidalsWalkerTest.java` from 245 lines to 116 lines — now uses `walker.walkTo(dest)` / `walker.walkToBank()`
- Added "Nearest Bank" destination option to TidalsWalkerTest ScriptUI

## Privacy
All code in utilities/ — consuming scripts only get the jar, not source.