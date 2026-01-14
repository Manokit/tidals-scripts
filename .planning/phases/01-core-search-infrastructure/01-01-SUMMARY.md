---
phase: 01-core-search-infrastructure
plan: 01
subsystem: utilities
tags: [bank, search, keyboard, osmb-api]

# Dependency graph
requires: []
provides:
  - BankSearchUtils utility class
  - openSearch() method for activating bank search
  - isSearchActive() detection helper
affects: [phase-02-single-item-withdrawal]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Keyboard shortcut activation for bank search"
    - "DialogueType detection for search state"

key-files:
  created:
    - utilities/src/main/java/utilities/BankSearchUtils.java
  modified: []

key-decisions:
  - "Use keyboard shortcut (backspace) to activate search instead of button click"
  - "Detect search state via DialogueType.TEXT_SEARCH or ITEM_SEARCH"

patterns-established:
  - "Bank search activation via keyboard input"
  - "Search state detection via dialogue type checking"

issues-created: []

# Metrics
duration: 15min
completed: 2026-01-14
---

# Phase 1 Plan 01: Bank Button API Discovery and openSearch() Summary

**Keyboard-based bank search activation using backspace key, with DialogueType detection for search state verification**

## Performance

- **Duration:** ~15 min
- **Started:** 2026-01-14T09:30:00Z
- **Completed:** 2026-01-14T09:45:00Z
- **Tasks:** 2
- **Files modified:** 1 (created)

## Accomplishments

- Researched OSMB API for bank button interaction methods
- Discovered no direct clickButton() method exists for BankButtonType
- Implemented keyboard shortcut approach to activate bank search
- Created BankSearchUtils.java following existing *Utils patterns
- Added openSearch() with retry logic and isSearchActive() detection

## Discovery Findings

**Bank button interaction research:**

1. **BankButtonType.SEARCH** is documented but no direct click API exists
2. Bank.md states: "Prefer Bank methods over clicking buttons" and "Button logic is handled internally"
3. **Alternative discovered**: In OSRS, pressing keys while bank is open activates search
4. DialogueType.TEXT_SEARCH and ITEM_SEARCH indicate active search input

**Approach selected**: Use keyboard input (backspace key) to activate search, then detect via DialogueType. This is safer than trying to visually locate and click the search button.

## Task Commits

Each task was committed atomically:

1. **Task 1 & 2: Discovery + openSearch()** - `1f19973` (feat)
   - Discovery documented in commit message
   - BankSearchUtils.java created with openSearch() method

**Plan metadata:** (this summary)

## Files Created/Modified

- `utilities/src/main/java/utilities/BankSearchUtils.java` - New utility class with openSearch() and isSearchActive() methods

## Decisions Made

1. **Keyboard shortcut over button click** - No direct API for clicking BankButtonType buttons; keyboard approach is cleaner and more reliable
2. **Backspace key for activation** - Safer than letter keys as it clears any partial search without adding characters
3. **DialogueType detection** - TEXT_SEARCH and ITEM_SEARCH types indicate active search state

## Deviations from Plan

None - plan executed as specified, with discovery findings documented.

## Issues Encountered

None - the keyboard shortcut approach worked cleanly without needing to implement image-based button detection.

## Next Phase Readiness

- BankSearchUtils created with openSearch() method
- Ready for Plan 02 to add typeSearch() and clearSearch() methods
- Foundation established for full search-by-name functionality

---
*Phase: 01-core-search-infrastructure*
*Completed: 2026-01-14*
