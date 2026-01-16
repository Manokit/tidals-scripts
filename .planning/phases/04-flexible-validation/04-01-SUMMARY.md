---
phase: 04-flexible-validation
plan: 01
subsystem: strategies
tags: [equipment-validation, auto-equip, inventory-fallback, fairy-ring, quest-cape]

# Dependency graph
requires:
  - phase: 01-mode-detection
    provides: Mode enum, verifyRequirements() structure
provides:
  - Flexible equipment validation (inventory fallback)
  - Auto-equip dramen staff from inventory
  - Quest cape support as ardy cloak alternative
affects: [05-region-priority]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Check equipment first, then inventory fallback"
    - "Auto-equip pattern with verification"

key-files:
  created: []
  modified:
    - TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java

key-decisions:
  - "Fairy ring mode: auto-equip dramen staff if found in inventory"
  - "Fairy ring mode: accept ardy cloak OR quest cape (equipped or inventory)"
  - "Ver sinhaza mode: keep strict - items must be equipped for teleport/bloom to work"

patterns-established:
  - "Inventory fallback pattern: check equipment first, then inventory, then auto-equip if applicable"

# Metrics
duration: 2min
completed: 2026-01-16
---

# Phase 4 Plan 1: Flexible Validation Summary

**Flexible equipment validation with inventory fallback and auto-equip for fairy ring mode, plus quest cape support as ardy cloak alternative**

## Performance

- **Duration:** 2 min
- **Started:** 2026-01-16T12:58:48Z
- **Completed:** 2026-01-16T13:01:00Z
- **Tasks:** 3
- **Files modified:** 1

## Accomplishments

- Added QUEST_CAPE constant (ID 9813) for fairy ring mode alternative teleport
- Implemented autoEquipDramenStaff() helper with verification
- Updated fairy ring mode to accept dramen staff in inventory (auto-equips)
- Updated fairy ring mode to accept ardy cloak OR quest cape in equipment OR inventory
- Added clarifying documentation for ver sinhaza mode equipment requirements

## Task Commits

Each task was committed atomically:

1. **Task 1: Add quest cape constant and helper method** - `93b4973` (feat)
2. **Task 2: Update fairy ring validation for inventory fallback** - `a413f10` (feat)
3. **Task 3: Clarify ver sinhaza mode requirements** - `5f59c35` (docs)

## Files Created/Modified

- `TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java` - Added quest cape constant, autoEquipDramenStaff() helper, updated verifyRequirements() for inventory fallback

## Decisions Made

- **Auto-equip dramen staff:** If dramen staff is in inventory (not equipped), automatically equip it during verification. This allows users to start with dramen staff in inventory.
- **Quest cape support:** Quest cape accepted as alternative to ardy cloak for monastery teleport in fairy ring mode. Either item works from equipment or inventory.
- **Ver sinhaza strict validation:** Kept ver sinhaza mode validation strict (items must be equipped) because bloom casting requires equipped bloom tool and teleport requires equipped medallion.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Flexible validation complete for fairy ring mode
- Users can now start with dramen staff and/or ardy cloak/quest cape in inventory
- Ready for Phase 05: Region Priority (add region 13877 to regionsToPrioritise())

---
*Phase: 04-flexible-validation*
*Completed: 2026-01-16*
