---
phase: 05-restock-logic
plan: 01
subsystem: loadout
tags: [restock, comparison, deficit, quantity-modes, fuzzy-matching]

# Dependency graph
requires:
  - phase: 01-data-model
    provides: Loadout, LoadoutItem, QuantityMode data classes
  - phase: 02-item-resolution
    provides: ItemResolver, ItemVariantMap for fuzzy matching
provides:
  - MissingItem data class tracking container type and deficit
  - RestockResult with success/partial/failed factory methods
  - LoadoutComparator for diff calculation with mode-aware logic
affects: [05-02, 06-banking-integration]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - Immutable data classes with defensive copies
    - Static factory methods for result types
    - Mode-aware comparison logic

key-files:
  created:
    - utilities/src/main/java/utilities/loadout/MissingItem.java
    - utilities/src/main/java/utilities/loadout/RestockResult.java
    - utilities/src/main/java/utilities/loadout/LoadoutComparator.java
  modified: []

key-decisions:
  - "ContainerType enum nested inside MissingItem for cohesion"
  - "RestockResult uses static factories (success/partial/failed/nothingMissing) over constructors"
  - "UNLIMITED mode treats item as missing only if current=0 (will take full bank stack)"
  - "Equipment checked before inventory (equipment more critical for combat scripts)"
  - "Same-item across multiple slots grouped for accurate deficit calculation"

patterns-established:
  - "Mode-aware deficit: EXACT/MINIMUM check current < needed, UNLIMITED checks current == 0"
  - "Fuzzy equipment check: build variant ID array, pass to findItem(int[])"

issues-created: []

# Metrics
duration: 3 min
completed: 2026-01-16
---

# Phase 5 Plan 01: Restock Result and Comparator Summary

**MissingItem/RestockResult data classes and LoadoutComparator for mode-aware diff calculation with fuzzy matching**

## Performance

- **Duration:** 3 min
- **Started:** 2026-01-16T05:08:56Z
- **Completed:** 2026-01-16T05:11:56Z
- **Tasks:** 2
- **Files created:** 3

## Accomplishments

- Created MissingItem data class with ContainerType enum, deficit calculation, and logging
- Created RestockResult with static factory methods (success, partial, failed, nothingMissing) and defensive copies
- Created LoadoutComparator that checks equipment first, then inventory
- Implemented mode-aware comparison: EXACT/MINIMUM check quantity, UNLIMITED checks presence
- Integrated fuzzy matching via ItemResolver for degradable equipment (Barrows, crystal)

## Task Commits

Each task was committed atomically:

1. **Task 1: Create MissingItem and RestockResult data classes** - `e5c01e8` (feat)
2. **Task 2: Create LoadoutComparator for diff calculation** - `0b1dc81` (feat)

## Files Created/Modified

- `utilities/src/main/java/utilities/loadout/MissingItem.java` - Tracks missing item with container type, current/needed quantities, and deficit calculation
- `utilities/src/main/java/utilities/loadout/RestockResult.java` - Result of restock operation with success/partial/failed states
- `utilities/src/main/java/utilities/loadout/LoadoutComparator.java` - Compares player state to loadout baseline, returns missing items

## Decisions Made

| Decision | Rationale |
|----------|-----------|
| ContainerType nested in MissingItem | Cohesion - only used in context of missing items |
| Static factory methods for RestockResult | Clear semantics (success vs partial vs failed), prevents invalid states |
| UNLIMITED mode: missing only if current=0 | When restocking, will take entire bank stack; only matters if completely absent |
| Equipment checked before inventory | Equipment more critical for combat scripts; matches phase context vision |
| Group same-item across slots | Multiple slots with same item need accurate total deficit |

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## Next Phase Readiness

- RestockResult and MissingItem provide clear API for tracking restock operations
- LoadoutComparator can identify what's missing with mode-aware logic
- Ready for 05-02-PLAN.md (withdrawal logic that consumes these data structures)

---
*Phase: 05-restock-logic*
*Completed: 2026-01-16*
