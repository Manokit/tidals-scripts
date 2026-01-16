---
phase: 03-banking-return
plan: 02
subsystem: strategy
tags: [fairy-ring, bkr, monastery, return-teleport, ardy-cloak]

# Dependency graph
requires:
  - phase: 03-banking-return/01
    provides: Zanaris banking for fairy ring mode
  - phase: 02-collection/01
    provides: THREE_LOG_AREA and THREE_LOG_TILE constants
  - phase: 01-mode-detection/01
    provides: isFairyRingMode() detection
provides:
  - Mode-aware return to collection area
  - BKR validation with safe script termination
  - Monastery fairy ring walking path
affects: []

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "tapGetResponse for menu validation before interaction"
    - "script.stop() for unrecoverable configuration errors"

key-files:
  created: []
  modified:
    - TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java

key-decisions:
  - "BKR validation via tapGetResponse menu check before using fairy ring"
  - "Script stops with clear error if BKR not configured (safety requirement)"
  - "10-waypoint path from monastery teleport to fairy ring"

patterns-established:
  - "Menu validation: Use tapGetResponse to verify expected action before interacting"
  - "Safe termination: script.stop() with descriptive ERROR logs for config issues"

# Metrics
duration: 2min
completed: 2026-01-16
---

# Phase 3 Plan 2: Monastery Fairy Ring Return Summary

**Fairy ring mode return via ardy cloak -> monastery -> fairy ring (BKR) with validation that stops script if BKR not configured**

## Performance

- **Duration:** 2 min
- **Started:** 2026-01-16T05:25:00Z
- **Completed:** 2026-01-16T05:27:00Z
- **Tasks:** 3
- **Files modified:** 1

## Accomplishments
- Added monastery fairy ring constants and 10-waypoint walking path
- Implemented useFairyRingReturn() with BKR validation via tapGetResponse
- Made returnToArea() mode-aware to route fairy ring mode through monastery

## Task Commits

Each task was committed atomically:

1. **Task 1: Add return path constants** - `a09949e` (feat)
2. **Task 2: Add useFairyRingReturn() method** - `18dd318` (feat)
3. **Task 3: Make returnToArea() mode-aware** - `d2032f6` (feat)

## Files Created/Modified
- `TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java` - Added monastery fairy ring return with BKR validation

## Decisions Made
- **BKR validation via tapGetResponse**: Menu action is inspected to verify "last-destination" and "bkr" are present, matching dkTravel.java pattern
- **Script termination on config error**: If BKR not configured, script.stop() is called with clear error messages guiding user to set up BKR
- **10-waypoint path**: From monastery teleport landing (2609, 3221) to fairy ring (2654, 3230) for smooth walking

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

**Fairy ring mode requires BKR configured as last destination:**
1. Manually travel to any fairy ring
2. Use Configure option to dial BKR
3. This sets BKR as last destination
4. Script will verify and use "Last-destination (BKR)" on return

## Next Phase Readiness
- Phase 3 (Banking & Return) complete
- Both modes now fully functional:
  - Ver Sinhaza: Drakan's Medallion for both banking and return
  - Fairy Ring: Zanaris banking via fairy ring, return via monastery fairy ring (BKR)
- Script ready for production use

---
*Phase: 03-banking-return*
*Completed: 2026-01-16*
