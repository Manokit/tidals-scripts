---
phase: 04-import-export
plan: 01
subsystem: loadout
tags: [json, import, export, runelite, org.json]

# Dependency graph
requires:
  - phase: 01-data-model
    provides: Loadout, LoadoutItem, QuantityMode classes
provides:
  - LoadoutImporter - parse RuneLite Inventory Setups JSON
  - LoadoutExporter - export extended JSON with quantity modes
affects: [04-02-ui-integration, 05-restock-logic, 06-script-integration]

# Tech tracking
tech-stack:
  added: [org.json:json:20240303]
  patterns: [utility class pattern, builder pattern for items]

key-files:
  created:
    - utilities/src/main/java/utilities/loadout/LoadoutImporter.java
    - utilities/src/main/java/utilities/loadout/LoadoutExporter.java
  modified: []

key-decisions:
  - "org.json added as compileOnly dependency (provided by OSMB runtime)"
  - "Equipment mapping: RuneLite 14 slots -> our 11 slots (ignore slots 11-13)"
  - "Imported items default to EXACT mode (RuneLite has no mode concept)"
  - "Export omits default values for compact output"

patterns-established:
  - "Import/export utility classes with static methods"
  - "Compact JSON format omitting defaults (q=1, f=false, mode=exact)"

issues-created: []

# Metrics
duration: 3 min
completed: 2026-01-16
---

# Phase 04 Plan 01: Import/Export Utilities Summary

**JSON import from RuneLite format and export to extended format with quantity modes using org.json library**

## Performance

- **Duration:** 3 min
- **Started:** 2026-01-16T02:15:44Z
- **Completed:** 2026-01-16T02:19:22Z
- **Tasks:** 2
- **Files created:** 2

## Accomplishments

- LoadoutImporter parses RuneLite Inventory Setups portable JSON format
- Equipment slot mapping handles RuneLite 14-slot to our 11-slot conversion
- LoadoutExporter produces compact JSON with optional quantity mode field
- All containers supported: equipment, inventory, rune pouch, bolt pouch, quiver

## Task Commits

Each task was committed atomically:

1. **Task 1: Create LoadoutImporter for RuneLite JSON** - `bc5f14c` (feat)
2. **Task 2: Create LoadoutExporter for extended JSON** - `9239546` (feat)

## Files Created/Modified

- `utilities/src/main/java/utilities/loadout/LoadoutImporter.java` - Parse RuneLite JSON into Loadout
- `utilities/src/main/java/utilities/loadout/LoadoutExporter.java` - Export Loadout to extended JSON

## Decisions Made

| Decision | Rationale |
|----------|-----------|
| org.json as compileOnly | Library is provided at OSMB runtime, no need to bundle |
| Map RuneLite eq[0-10] only | RuneLite slots 11-13 are always empty fillers |
| Import defaults to EXACT mode | RuneLite format has no mode concept; user can change in UI |
| Omit defaults in export | Match RuneLite compact style (q=1 → omit, f=false → omit) |

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added org.json dependency to build.gradle**
- **Found during:** Task 1 (LoadoutImporter compilation)
- **Issue:** org.json not available in utilities module, compilation failed
- **Fix:** Added `compileOnly 'org.json:json:20240303'` to utilities/build.gradle
- **Files modified:** utilities/build.gradle (gitignored - local change only)
- **Verification:** Build succeeds, classes compile
- **Note:** build.gradle is gitignored, so dependency is local configuration only

---

**Total deviations:** 1 auto-fixed (1 blocking), 0 deferred
**Impact on plan:** Essential fix for compilation. No scope creep.

## Issues Encountered

None - plan executed smoothly after adding the org.json dependency.

## Next Phase Readiness

- Import/export utilities ready for UI integration in 04-02-PLAN.md
- LoadoutImporter can parse any RuneLite Inventory Setups export
- LoadoutExporter produces JSON that LoadoutImporter can reimport (round-trip works)

---
*Phase: 04-import-export*
*Completed: 2026-01-16*
