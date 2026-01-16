---
phase: 05-restock-logic
plan: 02
subsystem: loadout
tags: [restock, bank-withdrawals, equipment, fuzzy-matching, quantity-modes]

# Dependency graph
requires:
  - phase: 01-data-model
    provides: Loadout, LoadoutItem, QuantityMode data classes
  - phase: 02-item-resolution
    provides: ItemResolver, ItemVariantMap for fuzzy matching
  - phase: 05-01
    provides: LoadoutComparator, MissingItem, RestockResult data classes
provides:
  - LoadoutRestocker for bank withdrawals with quantity mode handling
  - equipFromInventory() for equipping gear after withdrawal
  - Equipment verification after equipping
affects: [06-banking-integration]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - Equipment-first withdrawal order
    - Bank re-open pattern after equipping
    - Fuzzy variant preference (highest charge first)

key-files:
  created:
    - utilities/src/main/java/utilities/loadout/LoadoutRestocker.java
  modified: []

key-decisions:
  - "Re-open bank after equipping for inventory withdrawals"
  - "MINIMUM mode is hard requirement, EXACT/UNLIMITED are soft"
  - "Use ItemSearchResult.interact() for equipping, not Inventory.interact()"
  - "Verify equipment after all equips to catch silent failures"
  - "Try Wear/Wield/Equip actions in order for equipping"

patterns-established:
  - "findFirstInBank prefers highest charge variant (iteration order)"
  - "Log warnings for equip failures (may be requirements) but don't fail hard"
  - "Human delays between bank operations (100-300ms) and equips (200-400ms)"

issues-created: []

# Metrics
duration: 10 min
completed: 2026-01-16
---

# Phase 5 Plan 02: Restock Withdrawer Summary

**LoadoutRestocker for bank withdrawals with equipment-first order and quantity mode handling**

## Performance

- **Duration:** 10 min
- **Started:** 2026-01-16T05:08:00Z
- **Completed:** 2026-01-16T05:18:59Z
- **Tasks:** 2
- **Files created:** 1

## Accomplishments

- Created LoadoutRestocker class with restock() method for bank withdrawals
- Implemented quantity mode handling: MINIMUM (hard fail), EXACT/UNLIMITED (soft fail)
- Equipment-first withdrawal order: withdraw equipment, equip, then withdraw inventory
- Bank re-open pattern after equipping for inventory withdrawals
- Fuzzy matching prefers highest charge variant using findFirstInBank()
- equipFromInventory() public method for equipping gear
- Equipment verification after all equips to catch silent failures
- Human-like delays between operations

## Task Commits

Each task was committed atomically:

1. **Task 1: Create LoadoutRestocker for bank withdrawals** - `bbbca1d` (feat)
2. **Task 2: Add equipment handling with verification** - `88c44c1` (feat)

## Files Created/Modified

- `utilities/src/main/java/utilities/loadout/LoadoutRestocker.java` - Main restocker class with restock(), equipFromInventory(), and helper methods

## Decisions Made

| Decision | Rationale |
|----------|-----------|
| Re-open bank after equipping | Can't equip while bank is open; need bank open for inventory withdrawals |
| MINIMUM mode causes hard failure | Per CONTEXT.md, minimum is "I can't proceed without this" |
| EXACT/UNLIMITED are soft | Take what's available, don't fail the whole restock |
| Use ItemSearchResult.interact() | Correct API pattern per existing scripts |
| Verify equipment after equips | Some items may silently fail (level/quest requirements) |
| Try Wear/Wield/Equip actions | Different items use different equip actions |

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## Next Phase Readiness

- LoadoutRestocker provides complete restock functionality
- Integrates with existing BankingUtils for bank opening
- Ready for Phase 6: Banking Integration (script-level usage)

---
*Phase: 05-restock-logic*
*Completed: 2026-01-16*
