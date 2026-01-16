---
phase: 03-banking-return
plan: 01
subsystem: banking
tags: [fairy-ring, zanaris, teleport, walking]

# Dependency graph
requires:
  - phase: 01-mode-detection
    provides: isFairyRingMode() method for mode detection
  - phase: 02-collection
    provides: 3-log tile collection working for fairy ring mode
provides:
  - Zanaris banking support for fairy ring mode
  - useZanarisBanking() method for fairy ring teleport to zanaris
  - walkToZanarisBank() method for smooth pathing to bank chest
  - Mode-aware teleportToBank() routing
affects: [03-02-return-to-area]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Mode-conditional banking flow in teleportToBank()"
    - "ZANARIS_BANK_PATH for smooth walking through zanaris"

key-files:
  created: []
  modified:
    - TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java

key-decisions:
  - "Use 'Zanaris' menu action for direct fairy ring teleport (from dkTravel.java reference)"
  - "Walk from fairy ring landing (2412, 4434) to bank chest via 8-waypoint path"
  - "Check isFairyRingMode() first in teleportToBank() before crafting cape check"

patterns-established:
  - "Mode-aware banking: check mode first, then route to appropriate banking method"

# Metrics
duration: 2min
completed: 2026-01-16
---

# Phase 3 Plan 1: Zanaris Banking Summary

**Fairy ring mode banking via Zanaris fairy ring teleport with smooth walking path to bank chest**

## Performance

- **Duration:** 2 min
- **Started:** 2026-01-16T12:22:28Z
- **Completed:** 2026-01-16T12:23:57Z
- **Tasks:** 3
- **Files modified:** 1

## Accomplishments
- Added Zanaris banking constants (positions, area, walking path)
- Implemented useZanarisBanking() for fairy ring teleport to zanaris
- Implemented walkToZanarisBank() for smooth pathing via waypoints
- Made teleportToBank() mode-aware to route fairy ring mode to zanaris

## Task Commits

Each task was committed atomically:

1. **Task 1: Add Zanaris constants** - `e4af00d` (feat)
2. **Task 2: Add useZanarisBanking() and walkToZanarisBank() methods** - `34fc8f1` (feat)
3. **Task 3: Make teleportToBank() mode-aware** - `557ad73` (feat)

## Files Created/Modified
- `TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java` - Added Zanaris banking support with constants, methods, and mode-aware routing

## Decisions Made
- Used "Zanaris" (capital Z) as the menu action for fairy ring direct teleport - matches pattern from dkTravel.java reference
- Created 8-waypoint path from fairy ring landing to bank chest for smooth walking
- Added isFairyRingMode() check at top of teleportToBank() to route before checking crafting cape

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Zanaris banking complete for fairy ring mode
- Ready for 03-02 (return to area after banking)
- returnToArea() will need mode-aware updates for fairy ring return path

---
*Phase: 03-banking-return*
*Completed: 2026-01-16*
