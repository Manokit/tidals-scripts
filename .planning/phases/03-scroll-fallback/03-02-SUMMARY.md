---
phase: 03-scroll-fallback
plan: 02
subsystem: utilities
tags: [bank-scrolling, scroll-fallback, sprite-detection, bank-search]

# Dependency graph
requires:
  - phase: 03-01
    provides: BankScrollUtils with scroll methods and sprite detection
provides:
  - BankScrollUtils.scrollDownAndCheck() for end-of-scroll detection
  - BankScrollUtils.scrollUpAndCheck() for end-of-scroll detection
  - BankScrollUtils.scrollToTopWithCheck() with safeguards
  - BankSearchUtils.searchAndWithdraw() with scroll fallback
affects: [04-batch-withdrawal, 05-fill-inventory]

# Tech tracking
tech-stack:
  added: []
  patterns: [scroll-then-search-fallback, sprite-based-end-detection]

key-files:
  created: []
  modified:
    - utilities/src/main/java/utilities/BankScrollUtils.java
    - utilities/src/main/java/utilities/BankSearchUtils.java
    - utilities/jar/TidalsUtilities.jar

key-decisions:
  - "Used sprite-based end detection instead of item slot comparison"
  - "Default useScrollFallback=true for searchAndWithdraw"
  - "30 max scroll iterations as safeguard"

patterns-established:
  - "Search-first with scroll fallback pattern for robust item finding"
  - "Sprite visibility as end-of-scroll indicator"

issues-created: []

# Metrics
duration: 4min
completed: 2026-01-14
---

# Phase 3 Plan 2: Scroll Fallback Integration Summary

**Integrated scroll fallback into searchAndWithdraw() with sprite-based end-of-scroll detection**

## Performance

- **Duration:** 4 min
- **Started:** 2026-01-14T17:52:18Z
- **Completed:** 2026-01-14T17:55:50Z
- **Tasks:** 3/3
- **Files modified:** 3

## Accomplishments

- Added end-of-scroll detection methods to BankScrollUtils using sprite visibility
- Integrated scroll fallback into searchAndWithdraw() as default behavior
- Built utilities JAR with complete scroll functionality

## Task Commits

1. **Task 1: Add end-of-scroll detection** - `d744665` (feat)
2. **Task 2: Integrate scroll fallback** - `e464473` (feat)
3. **Task 3: Build utilities JAR** - `820a850` (chore)

## Files Created/Modified

- `utilities/src/main/java/utilities/BankScrollUtils.java` - Added scrollDownAndCheck(), scrollUpAndCheck(), scrollToTopWithCheck()
- `utilities/src/main/java/utilities/BankSearchUtils.java` - Added scroll fallback to searchAndWithdraw(), new findItemByScrolling() helper
- `utilities/jar/TidalsUtilities.jar` - Rebuilt with scroll functionality

## API Approach

Originally planned to compare visible bank item slots before/after scroll to detect end-of-scroll. However, `ItemGroupResult.getSlots()` doesn't exist in OSMB API.

**Solution:** Leveraged the existing sprite-based detection from plan 03-01:
- `canScrollDown()` returns false when at bottom (scroll button not visible)
- `canScrollUp()` returns false when at top (scroll button not visible)
- This provides reliable end-of-scroll detection without needing slot comparison

## Scroll Fallback Logic

When `searchAndWithdraw()` doesn't find an item via search:
1. Clear the search to show all bank items
2. Scroll to top of bank
3. Search current view for item
4. If not found, scroll down and repeat
5. Stop when item found OR end of bank reached
6. Max 30 scrolls safeguard prevents infinite loops

## Decisions Made

1. **Used sprite detection for end-of-scroll instead of item slot comparison**
   - ItemGroupResult.getSlots() doesn't exist
   - Sprite detection already proven working from plan 03-01
   - Simpler and more reliable approach

2. **Default scroll fallback enabled**
   - Search-first is fast path for most cases
   - Scroll fallback handles edge cases automatically
   - Can disable with useScrollFallback=false if needed

3. **Max 30 scroll iterations**
   - Prevents infinite loops if item truly doesn't exist
   - Most banks would be fully scrolled well within this limit

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Changed end-of-scroll approach due to missing API method**
- **Found during:** Task 1 (scrollDownAndCheck implementation)
- **Issue:** Plan specified using bank.search().getSlots() to compare slots before/after scroll, but getSlots() doesn't exist on ItemGroupResult
- **Fix:** Used existing sprite-based canScrollDown()/canScrollUp() methods which already detect scroll button visibility
- **Files modified:** BankScrollUtils.java
- **Verification:** Build compiles, approach validated by existing 03-01 implementation
- **Committed in:** d744665 (Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Simpler solution that reuses existing infrastructure. No functionality loss.

## Issues Encountered

None

## Next Phase Readiness

- Scroll fallback functionality complete and integrated
- Phase 3 (Scroll Fallback) is now complete
- Ready for Phase 4 (Batch Withdrawal) which depends on Phase 2

---
*Phase: 03-scroll-fallback*
*Completed: 2026-01-14*
