---
phase: 02-item-resolution
plan: 01
subsystem: items
tags: [barrows, crystal, degradable, variant-mapping, fuzzy-matching]

# Dependency graph
requires:
  - phase: 01-data-model
    provides: LoadoutItem with fuzzy flag
provides:
  - ItemVariantMap class with static variant mappings
  - getBaseId(), getAllVariants(), getPreferredVariant() methods
  - isVariant(), areVariants() for fuzzy matching support
affects: [item-resolution, loadout-matching, bank-search]

# Tech tracking
tech-stack:
  added: []
  patterns: [static-variant-mapping, defensive-array-copies]

key-files:
  created:
    - utilities/src/main/java/utilities/items/ItemVariantMap.java
  modified: []

key-decisions:
  - "Ordered variants highest charge first for getPreferredVariant()"
  - "Defensive array copies in getAllVariants() to prevent external mutation"
  - "Crystal halberd mapped as single variant (no degradation states in OSRS inventory)"

patterns-established:
  - "Static registerVariants() helper for variant group registration"
  - "HashMap-based dual mapping (VARIANT_TO_BASE and BASE_TO_VARIANTS)"

issues-created: []

# Metrics
duration: 2 min
completed: 2026-01-16
---

# Phase 2 Plan 1: ItemVariantMap Summary

**Static variant mapping for Barrows, Crystal, and charged equipment enabling fuzzy item matching**

## Performance

- **Duration:** 2 min
- **Started:** 2026-01-16T00:05:43Z
- **Completed:** 2026-01-16T00:07:49Z
- **Tasks:** 2
- **Files modified:** 1

## Accomplishments

- Created ItemVariantMap with static mappings for all 24 Barrows items (6 variants each)
- Added Crystal bow/shield mappings with 11 variants each (new, full, 9/10 through 1/10)
- Added Serpentine helm, Magma helm, Tanzanite helm, and Toxic blowpipe variants
- Implemented all 5 helper methods for variant lookup and matching

## Task Commits

Each task was committed atomically:

1. **Task 1: Create ItemVariantMap with Barrows mappings** - `712cfe2` (feat)
2. **Task 2: Add Crystal equipment and helper methods** - `fd39f73` (feat)

## Files Created/Modified

- `utilities/src/main/java/utilities/items/ItemVariantMap.java` - Static variant mappings and lookup methods for degradable items

## Decisions Made

- **Variants ordered highest charge first** - Enables getPreferredVariant() to return the best state
- **Defensive array copies** - getAllVariants() returns clone to prevent external mutation
- **Crystal halberd as single variant** - OSRS crystal halberd has no charge states in inventory (unlike bow/shield)
- **Included helm variants** - Added Magma and Tanzanite helm alongside Serpentine for completeness

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## Next Phase Readiness

- ItemVariantMap ready for use in item resolution
- All Barrows sets covered: Dharok, Ahrim, Karil, Torag, Guthan, Verac
- Crystal equipment covered: bow, shield, halberd
- Charged equipment covered: Serpentine/Magma/Tanzanite helms, Toxic blowpipe
- Methods are static for easy use: `ItemVariantMap.areVariants(id1, id2)`

---
*Phase: 02-item-resolution*
*Completed: 2026-01-16*
