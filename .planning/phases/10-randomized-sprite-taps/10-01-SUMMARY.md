---
phase: 10-randomized-sprite-taps
plan: 01
subsystem: utilities
tags: [humanization, sprite-detection, tap-randomization, rectangle, bounds]

# Dependency graph
requires:
  - phase: 06-tap-based-search
    provides: sprite-based search button detection
  - phase: 07-scroll-position
    provides: sprite-based scroll buttons
provides:
  - randomized tap coordinates within sprite bounds
  - humanized click positions instead of exact center points
affects: [11-humanized-typing, 12-integration-testing, TidalsWithdrawer]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Rectangle-based tap: use getBounds() instead of getAsPoint() for humanized clicks"

key-files:
  created: []
  modified:
    - utilities/src/main/java/utilities/BankSearchUtils.java
    - utilities/src/main/java/utilities/BankScrollUtils.java
    - utilities/jar/TidalsUtilities.jar

key-decisions:
  - "Keep Point import in BankScrollUtils since it's still used for scrollbar position tracking"

patterns-established:
  - "Sprite taps use getBounds() for random click positions within bounds"

issues-created: []

# Metrics
duration: 3min
completed: 2026-01-14
---

# Phase 10 Plan 01: Randomized Sprite Taps Summary

**Rectangle-based taps replacing Point-based taps in 3 sprite detection methods for humanized click positions**

## Performance

- **Duration:** 3 min
- **Started:** 2026-01-14T22:40:00Z
- **Completed:** 2026-01-14T22:43:00Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments

- Converted BankSearchUtils.tapSearchButton() to use Rectangle bounds
- Converted BankScrollUtils.scrollDown() to use Rectangle bounds
- Converted BankScrollUtils.scrollUp() to use Rectangle bounds
- Rebuilt TidalsUtilities.jar with randomized sprite taps

## Task Commits

Each task was committed atomically:

1. **Task 1: Convert Point taps to Rectangle taps** - `2891aa9` (feat)
2. **Task 2: Rebuild TidalsUtilities.jar** - `b6f4601` (chore)

## Files Created/Modified

- `utilities/src/main/java/utilities/BankSearchUtils.java` - tapSearchButton() now uses getBounds()
- `utilities/src/main/java/utilities/BankScrollUtils.java` - scrollDown() and scrollUp() now use getBounds()
- `utilities/jar/TidalsUtilities.jar` - Rebuilt with randomized tap changes

## Decisions Made

- Kept `java.awt.*` import in BankScrollUtils.java since Point is still used in getScrollbarPosition(), isAtTop(), and isAtBottom() methods for scrollbar position tracking

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## Next Phase Readiness

- Phase 10 complete with all sprite taps randomized
- Ready for Phase 11: Humanized Typing (random delays between characters)

---
*Phase: 10-randomized-sprite-taps*
*Completed: 2026-01-14*
