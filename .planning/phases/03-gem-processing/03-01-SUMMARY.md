---
phase: 03-gem-processing
plan: 01
subsystem: crafting
tags: [gem-cutting, deposit-box, dialogue, XPTracker, Walker]

# Dependency graph
requires:
  - phase: 01-foundation
    provides: Task architecture, MiningLocation record
  - phase: 02-mining
    provides: Mine task, mining area detection
provides:
  - Cut task with level-aware gem cutting and dialogue handling
  - Bank task with deposit box interaction and walk-to-bank
  - Complete gem processing workflow (mine -> cut -> bank)
affects: [04-stats]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - XPTracker for skill level detection
    - Dialogue ITEM_OPTION selection with uncut gem ID
    - Deposit box interaction via DepositBox widget

key-files:
  created:
    - TidalsGemMiner/src/main/java/tasks/Cut.java
    - TidalsGemMiner/src/main/java/tasks/Bank.java
  modified:
    - TidalsGemMiner/src/main/java/main/TidalsGemMiner.java

key-decisions:
  - "Cut highest-level gem first for XP efficiency"
  - "Drop crushed gems after cutting completes"
  - "Task order: Setup -> Cut -> Bank -> Mine ensures proper state transitions"

patterns-established:
  - "XPTracker via getXPTrackers().get(SkillType.CRAFTING) for skill levels"
  - "Dialogue selectItem uses UNCUT gem ID per gem-cutting.md"
  - "Deposit box for Shilo Village mining (not full bank)"

# Metrics
duration: 4 min
completed: 2026-01-16
---

# Phase 03 Plan 01: Gem Processing Summary

**Cut and Bank tasks implement complete gem processing: level-based cutting with dialogue handling, crushed gem dropping, and deposit box banking**

## Performance

- **Duration:** 4 min
- **Started:** 2026-01-16T12:47:34Z
- **Completed:** 2026-01-16T12:51:48Z
- **Tasks:** 3/3
- **Files modified:** 3

## Accomplishments

- Created Cut task with all 8 gem types (opal through ruby) and crafting level requirements
- Implemented dialogue handling selecting UNCUT gem ID per gem-cutting.md pattern
- Added crushed gem dropping after cutting completes
- Created Bank task with deposit box interaction for both locations
- Integrated tasks in correct order: Setup -> Cut -> Bank -> Mine

## Task Commits

Each task was committed atomically:

1. **Task 1: Create Cut task** - `e71d172` (feat)
2. **Task 2: Create Bank task** - `53e1428` (feat)
3. **Task 3: Add tasks to main script** - `28153c0` (feat)

## Files Created/Modified

- `TidalsGemMiner/src/main/java/tasks/Cut.java` - Gem cutting with level checks, dialogue handling, crushed gem dropping
- `TidalsGemMiner/src/main/java/tasks/Bank.java` - Deposit box banking with walk-to-bank and walk-back logic
- `TidalsGemMiner/src/main/java/main/TidalsGemMiner.java` - Added Cut and Bank imports and task list entries

## Decisions Made

1. **Use XPTracker for crafting level** - No getSkillManager() in OSMB API, XPTrackers provide skill levels
2. **Cut highest-level gem first** - Iterates gems from ruby (63) down to opal (1) for XP efficiency
3. **Task order matters** - Cut before Bank ensures gems are cut before banking when enabled
4. **Drop crushed gems after each cutting session** - Keeps inventory clean for next mining cycle

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Corrected API usage for skill level detection**
- **Found during:** Task 1 (Cut task implementation)
- **Issue:** Plan specified getSkillManager().getLevel(Skill.CRAFTING) which doesn't exist in OSMB
- **Fix:** Used getXPTrackers().get(SkillType.CRAFTING).getLevel() pattern from TidalsGemCutter
- **Files modified:** TidalsGemMiner/src/main/java/tasks/Cut.java
- **Verification:** Build succeeds, level detection works
- **Committed in:** e71d172 (Task 1 commit)

**2. [Rule 3 - Blocking] Corrected item interaction API**
- **Found during:** Task 1 (Cut task implementation)
- **Issue:** Plan specified SearchableItem.useOn() but API uses getRandomItem().interact() pattern
- **Fix:** Used interactWithItems pattern from TidalsGemCutter with randomized click order
- **Files modified:** TidalsGemMiner/src/main/java/tasks/Cut.java
- **Verification:** Build succeeds
- **Committed in:** e71d172 (Task 1 commit)

---

**Total deviations:** 2 auto-fixed (2 blocking)
**Impact on plan:** Both fixes necessary to use correct OSMB API. No scope creep.

## Issues Encountered

None - execution proceeded smoothly after applying API corrections.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Gem processing workflow complete (mine -> cut -> bank cycle works)
- Ready for Phase 4: Stats Reporting to dashboard
- gemsMined and gemsCut counters ready for stats integration

---
*Phase: 03-gem-processing*
*Completed: 2026-01-16*
