---
phase: 02-mining
plan: 01
subsystem: mining
tags: [ObjectManager, PixelAnalyzer, Walker, respawn-circles, failsafes]

# Dependency graph
requires:
  - phase: 01-foundation
    provides: Task architecture, MiningLocation record, Setup task
provides:
  - Mine task with rock detection and mining loop
  - Mining area bounds for position checking
  - Failsafe behaviors (walk-back, world hop, stuck detection)
affects: [03-gem-processing, 04-stats]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - RespawnCircle detection for depleted rock tracking
    - WalkConfig break conditions for smart walking
    - Stuck timeout pattern with lastSuccessfulAction tracking

key-files:
  created:
    - TidalsGemMiner/src/main/java/tasks/Mine.java
  modified:
    - TidalsGemMiner/src/main/java/data/Locations.java
    - TidalsGemMiner/src/main/java/main/TidalsGemMiner.java

key-decisions:
  - "Upper mine hops worlds when depleted (limited rocks)"
  - "Underground mine waits for respawn (many rocks available)"
  - "5-minute stuck timeout before script stops"

patterns-established:
  - "Respawn circle filtering via getWorldPositionForRespawnCircles()"
  - "Break condition on walkTo() for early exit when goal visible"

# Metrics
duration: 4min
completed: 2026-01-16
---

# Phase 2 Plan 1: Core Mining Loop Summary

**Mine.java implements rock detection via ObjectManager, respawn tracking via PixelAnalyzer, walk-back via Walker, and world hop/stuck timeout failsafes**

## Performance

- **Duration:** 4 min
- **Started:** 2026-01-16T12:35:32Z
- **Completed:** 2026-01-16T12:39:02Z
- **Tasks:** 3
- **Files modified:** 3

## Accomplishments

- Added miningArea field to MiningLocation record for position checking
- Created Mine task with full mining loop (detect rocks, wait idle, tap, wait completion)
- Implemented respawn circle tracking to avoid depleted rocks
- Added walk-back failsafe when player is outside mining area
- Added world hop for upper location when all rocks depleted
- Added stuck detection with 5-minute timeout that stops script

## Task Commits

Each task was committed atomically:

1. **Task 1: Update Locations with mining area bounds** - `2c9a638` (feat)
2. **Task 2: Create Mine task with core mining loop** - `7a439ec` (feat)
3. **Task 3: Add failsafes to Mine task** - `f7c931b` (feat)

## Files Created/Modified

- `TidalsGemMiner/src/main/java/tasks/Mine.java` - Core mining task with rock detection, mining loop, and failsafes
- `TidalsGemMiner/src/main/java/data/Locations.java` - Added miningArea field and area bounds for both locations
- `TidalsGemMiner/src/main/java/main/TidalsGemMiner.java` - Added Mine task to task list

## Decisions Made

1. **Upper mine world hops, underground waits** - Upper has limited rocks so hopping is more efficient; underground has many rocks so waiting for respawn is better
2. **5-minute stuck timeout** - Long enough to handle temporary issues but short enough to catch real stuck states
3. **Break condition on walk-back** - Stop walking early if mineable rock becomes visible on screen

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- Gradle wrapper was missing, used system gradle instead
- Deprecation warning on tapGetResponse() (cosmetic, build succeeds)

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Mining loop is functional for both locations
- Ready for Phase 3 (Gem Processing) which will add cutting and banking
- Banking task will need to detect when inventory is full (already checked in Mine.activate())

---
*Phase: 02-mining*
*Completed: 2026-01-16*
