---
phase: 01-data-model
plan: 01
subsystem: utilities
tags: [java, loadout, data-model, builder-pattern]

# Dependency graph
requires: []
provides:
  - QuantityMode enum with EXACT/UNLIMITED/MINIMUM modes
  - LoadoutItem immutable data class with builder pattern
  - Loadout container class aggregating all slot types
affects: [item-resolution, editor-ui, import-export, restock-logic]

# Tech tracking
tech-stack:
  added: []
  patterns: [immutable-data-class, builder-pattern, defensive-copies]

key-files:
  created:
    - utilities/src/main/java/utilities/loadout/QuantityMode.java
    - utilities/src/main/java/utilities/loadout/LoadoutItem.java
    - utilities/src/main/java/utilities/loadout/Loadout.java
  modified: []

key-decisions:
  - "equals/hashCode on LoadoutItem uses itemId only (same ID = same item)"
  - "Loadout getters return defensive copies to preserve immutability"
  - "Nullable pouches (rune/bolt/quiver) match RuneLite format semantics"

patterns-established:
  - "Builder pattern: LoadoutItem.builder().itemId(x).quantity(y).build()"
  - "Slot constants: Loadout.SLOT_HEAD, SLOT_WEAPON, etc."

issues-created: []

# Metrics
duration: 2min
completed: 2026-01-15
---

# Phase 01 Plan 01: Data Model Summary

**Core loadout types: QuantityMode enum, LoadoutItem with builder, Loadout container with equipment/inventory/pouch slots**

## Performance

- **Duration:** 2 min
- **Started:** 2026-01-15T23:27:43Z
- **Completed:** 2026-01-15T23:29:34Z
- **Tasks:** 3
- **Files modified:** 3

## Accomplishments

- QuantityMode enum with EXACT (""), UNLIMITED ("*"), MINIMUM (">") modes and symbol parsing
- LoadoutItem immutable class mapping to RuneLite format (id, q, f fields) with builder pattern
- Loadout container with correct array sizes (14 equipment, 28 inventory, 4 rune/bolt pouch, 1 quiver)
- Equipment slot constants matching RuneLite indices (SLOT_HEAD=0, SLOT_WEAPON=4, etc.)

## Task Commits

Each task was committed atomically:

1. **Task 1: Create QuantityMode enum** - `4d74e51` (feat)
2. **Task 2: Create LoadoutItem class** - `b44de8a` (feat)
3. **Task 3: Create Loadout container class** - `bac300f` (feat)

## Files Created/Modified

- `utilities/src/main/java/utilities/loadout/QuantityMode.java` - Enum defining quantity interpretation modes
- `utilities/src/main/java/utilities/loadout/LoadoutItem.java` - Immutable item data with builder
- `utilities/src/main/java/utilities/loadout/Loadout.java` - Container aggregating all slot arrays

## Decisions Made

- **equals/hashCode on itemId only**: Two LoadoutItems are equal if they have the same item ID, regardless of quantity or mode. This simplifies set operations during restock.
- **Defensive array copies**: All array getters return copies to prevent external mutation of loadout state.
- **Nullable optional pouches**: runePouch, boltPouch, quiver are null when not present (matching RuneLite's null semantics rather than empty arrays).

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## Next Phase Readiness

- Data model complete, ready for Phase 2 (Item Resolution)
- Classes follow existing WithdrawalRequest pattern from utilities
- All three classes in new `utilities.loadout` package

---
*Phase: 01-data-model*
*Completed: 2026-01-15*
