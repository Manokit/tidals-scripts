---
phase: 08-item-id-screen-search
plan: 01
subsystem: utilities
tags: [item-search, visual-detection, sprite, bank, verification]

# Dependency graph
requires:
  - phase: 07-scroll-position-detection
    provides: isAtTop() and isAtBottom() scroll position methods
provides:
  - findItemInVisibleBank - visual item location by sprite
  - findAndVerifyItem - menu-verified item detection
  - searchBankForItem - full bank scroll search with verification
affects: [09-robust-withdrawal-flow]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - ItemManager.getItem() for sprite-based item detection
    - tapGetResponse for menu verification of visual matches

key-files:
  created: []
  modified:
    - utilities/src/main/java/utilities/BankSearchUtils.java
    - utilities/jar/TidalsUtilities.jar

key-decisions:
  - "findItemInVisibleBank is private - internal helper not exposed"
  - "Case-insensitive contains for name matching"
  - "MAX_SCROLL_ITERATIONS (30) reused as safety limit"

patterns-established:
  - "Visual item search + menu verification pattern for reliable item detection"

issues-created: []

# Metrics
duration: 8min
completed: 2026-01-14
---

# Phase 8 Plan 1: Item ID Screen Search Summary

**Three methods for finding and verifying items in bank by visual sprite detection with tapGetResponse verification**

## Performance

- **Duration:** 8 min
- **Started:** 2026-01-14T21:34:00Z
- **Completed:** 2026-01-14T21:42:00Z
- **Tasks:** 3
- **Files modified:** 2

## Accomplishments

- Added findItemInVisibleBank private method for sprite-based item location
- Added findAndVerifyItem public method with menu verification
- Added searchBankForItem public method combining scroll + verify for full bank search
- Rebuilt TidalsUtilities.jar with all new methods

## Task Commits

Each task was committed atomically:

1. **Task 1: Add findItemInVisibleBank method** - `789e168` (feat)
2. **Task 2: Add findAndVerifyItem method** - `5fc7aa8` (feat)
3. **Task 3: Add searchBankForItem method with scroll** - `fc2cfdd` (feat)

## Files Created/Modified

- `utilities/src/main/java/utilities/BankSearchUtils.java` - Added 3 new methods for item ID visual search
- `utilities/jar/TidalsUtilities.jar` - Rebuilt with new methods

## Decisions Made

- Made findItemInVisibleBank private since it's an internal helper used by findAndVerifyItem
- Used case-insensitive contains check for name matching (flexible for partial matches)
- Reused MAX_SCROLL_ITERATIONS constant (30) as safety limit for scroll search

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## Next Phase Readiness

- Item ID search with verification complete
- Ready for Phase 9: Robust Withdrawal Flow
- searchBankForItem provides verified item bounds that can be used for reliable withdrawals

---
*Phase: 08-item-id-screen-search*
*Completed: 2026-01-14*
