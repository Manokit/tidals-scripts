---
phase: 02-single-item-withdrawal
plan: 01
subsystem: utilities
tags: [bank, search, withdraw, item-name, osmb-api]

# Dependency graph
requires:
  - phase: 01-core-search-infrastructure
    provides: openSearch(), typeSearch(), clearSearch(), isSearchActive() methods
provides:
  - searchAndWithdraw(script, itemId, amount) method
  - searchAndWithdraw(script, itemId, amount, keepSearchOpen) overload for batch operations
affects: [phase-03-scroll-fallback, phase-04-batch-withdrawal, phase-05-fill-inventory]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Search by item name from ID via ItemManager.getItemName()"
    - "Verify item exists before withdrawal to avoid silent failures"

key-files:
  created: []
  modified:
    - utilities/src/main/java/utilities/BankSearchUtils.java
    - utilities/jar/TidalsUtilities.jar

key-decisions:
  - "Default method clears search after withdrawal - keepSearchOpen param for batch callers"
  - "Verify item in bank after search before attempting withdraw"

patterns-established:
  - "Search-and-withdraw pattern: type name -> wait for filter -> verify -> withdraw -> clear"

issues-created: []

# Metrics
duration: 1min
completed: 2026-01-14
---

# Phase 2 Plan 01: Single Item Withdrawal Summary

**searchAndWithdraw() method that searches bank by item name and withdraws specified amount, with optional keepSearchOpen for batch operations**

## Performance

- **Duration:** ~1 min
- **Started:** 2026-01-14T08:45:25Z
- **Completed:** 2026-01-14T08:46:28Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments

- Implemented searchAndWithdraw(script, itemId, amount) method
- Added overload with keepSearchOpen parameter for batch withdraw scenarios
- Method uses ItemManager.getItemName() to get item name from ID
- Verifies item exists in filtered bank before attempting withdrawal
- Properly clears search after withdrawal (unless keepSearchOpen=true)

## Task Commits

Each task was committed atomically:

1. **Task 1: Implement searchAndWithdraw() method** - `a36b4fc` (feat)
2. **Task 2: Build and verify utilities JAR** - `8108853` (chore)

## Files Created/Modified

- `utilities/src/main/java/utilities/BankSearchUtils.java` - Added searchAndWithdraw() with both overloads
- `utilities/jar/TidalsUtilities.jar` - Rebuilt with searchAndWithdraw() methods

## Decisions Made

1. **keepSearchOpen parameter** - Added optional boolean to skip clearing search, useful for batch operations that need to withdraw multiple items without repeatedly clearing
2. **Verify before withdraw** - Check that item exists in filtered results before attempting withdrawal to provide clear failure reason

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## Next Phase Readiness

- Phase 2 complete - searchAndWithdraw() implemented and ready for use
- BankSearchUtils now provides full search-and-withdraw capability
- Ready for Phase 3 (Scroll Fallback) or Phase 4 (Batch Withdrawal) which build on this foundation

---
*Phase: 02-single-item-withdrawal*
*Completed: 2026-01-14*
