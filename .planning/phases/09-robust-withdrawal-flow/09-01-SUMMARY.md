---
phase: 09-robust-withdrawal-flow
plan: 01
subsystem: banking
tags: [tapGetResponse, menu-verification, bank-search, withdrawal]

# Dependency graph
requires:
  - phase: 08-item-id-screen-search
    provides: findAndVerifyItem, searchBankForItem, isAtBottom methods
provides:
  - Verified searchAndWithdrawByName that confirms item via tapGetResponse before withdrawal
  - findMatchingItemByName helper for name-based menu verification
affects: [scripts using searchAndWithdrawByName]

# Tech tracking
tech-stack:
  added: []
  patterns: [tapGetResponse verification before action]

key-files:
  created: []
  modified: [utilities/src/main/java/utilities/BankSearchUtils.java, utilities/jar/TidalsUtilities.jar]

key-decisions:
  - "Used tapGetResponse verification on first slot since bank search consolidates results to top-left"
  - "Used isAtBottom() for scroll termination instead of canScrollDown()"

patterns-established:
  - "Menu verification pattern: verify item via tapGetResponse.getEntityName() before tap action"

issues-created: []

# Metrics
duration: 4min
completed: 2026-01-14
---

# Phase 9 Plan 01: Refactor searchAndWithdrawByName Summary

**searchAndWithdrawByName now verifies items via tapGetResponse before withdrawal, eliminating blind fixed-offset tapping**

## Performance

- **Duration:** 4 min
- **Started:** 2026-01-14T13:50:00Z
- **Completed:** 2026-01-14T13:54:00Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments

- Refactored searchAndWithdrawByName to use tapGetResponse verification
- Added findMatchingItemByName helper method for name-based item verification
- Replaced fixed pixel offset tap-and-hope approach with menu confirmation
- Used isAtBottom() for reliable scroll termination

## Task Commits

Each task was committed atomically:

1. **Task 1: Refactor searchAndWithdrawByName with verified item search** - `8d5a3db` (feat)
2. **Task 2: Build TidalsUtilities.jar** - `5a1353f` (chore)

## Files Created/Modified

- `utilities/src/main/java/utilities/BankSearchUtils.java` - Refactored searchAndWithdrawByName with verification, added findMatchingItemByName helper
- `utilities/jar/TidalsUtilities.jar` - Rebuilt with new verified withdrawal flow

## Decisions Made

1. **Used first slot verification only** - Since bank search consolidates filtered results to top-left, we only need to verify the first slot rather than scanning all visible slots. This is faster and simpler.

2. **Used isAtBottom() instead of canScrollDown()** - The isAtBottom() method from Phase 7 provides more reliable scroll termination by checking the scrollbar sprite position.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## Next Phase Readiness

- v1.2 milestone is now complete (all 3 phases done)
- searchAndWithdrawByName is robust with proper verification
- Ready for /gsd:complete-milestone to archive v1.2

---
*Phase: 09-robust-withdrawal-flow*
*Completed: 2026-01-14*
