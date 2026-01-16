---
phase: 03-editor-ui
plan: 03
subsystem: ui
tags: [javafx, loadout, editor, pouch-grid, context-menu, slot-expansion]

# Dependency graph
requires:
  - phase: 03-01
    provides: ItemSlot, ItemSearch for slot display and item selection
  - phase: 03-02
    provides: EquipmentGrid, InventoryGrid for equipment and inventory display
provides:
  - PouchGrid component for rune/bolt pouch and quiver
  - LoadoutEditor main popup window integrating all UI components
  - Right-click context menu for quantity mode toggle
  - Visual slot expansion for non-stackable items
affects: [scripts-using-loadout]

# Tech tracking
tech-stack:
  added: []
  patterns: [modal-popup, working-copy-pattern, osrs-theme, visual-slot-expansion]

key-files:
  created:
    - utilities/src/main/java/utilities/loadout/ui/PouchGrid.java
    - utilities/src/main/java/utilities/loadout/ui/LoadoutEditor.java
    - utilities/src/main/java/utilities/items/StackabilityUtil.java
  modified:
    - utilities/src/main/java/utilities/loadout/ui/ItemSlot.java
    - utilities/src/main/java/utilities/loadout/ui/ItemSearch.java
    - utilities/src/main/java/utilities/loadout/ui/EquipmentGrid.java
    - utilities/src/main/java/utilities/loadout/ui/InventoryGrid.java

key-decisions:
  - "OSRS equipment tab theme instead of ocean theme based on user feedback"
  - "Inventory grid is 4x7 (4 columns, 7 rows) to match actual OSRS inventory layout"
  - "MAX_ITEM_ID increased to 30000 to include all OSRS items"
  - "PouchGrid supports configurable slot count (1-4) with enable/disable checkbox"
  - "LoadoutEditor creates working copy to allow cancel without changes"
  - "Non-stackable items with qty > 1 expand to fill multiple visual slots"
  - "Right-click context menu on slots for quantity mode (All/Exact/Minimum)"

patterns-established:
  - "Working copy pattern - edit copy, save back to original on confirm"
  - "Modal popup via Stage.showAndWait() for blocking user input"
  - "OSRS theme colors: #3E3529 background, #5C5142 borders, #FF981F orange text"
  - "Visual slot expansion - non-stackables show true inventory footprint"
  - "Primary/expansion slot tracking for grouped items"

issues-created: []

known-limitations:
  - "Persistence is Phase 6 scope - loadout resets on script restart"
  - "Stackability is heuristic-based - not all items covered"

# Metrics
duration: 15min
completed: 2026-01-16
---

# Phase 03 Plan 03: Editor UI Summary

**PouchGrid component, LoadoutEditor main window, context menu, and visual slot expansion**

## Performance

- **Duration:** 15 min (including checkpoint feedback rounds)
- **Started:** 2026-01-16
- **Completed:** 2026-01-16
- **Tasks:** 3 (2 auto + 1 checkpoint with multiple fix rounds)
- **Files modified:** 7
- **Files created:** 3

## Accomplishments

### Initial Implementation
- PouchGrid component: configurable slot count (1-4) with enable checkbox
- LoadoutEditor popup: integrates equipment, inventory, and pouch grids
- Save/Cancel workflow with working copy pattern

### Theme and Layout Fixes (Checkpoint Round 1)
- Theme updated from ocean to OSRS equipment tab colors
- Inventory layout corrected to 4x7 (was 7x4)
- Item search now finds all OSRS items (MAX_ITEM_ID: 30000)

### Additional Features (Checkpoint Round 2)
- **Larger window**: 620x560 with more spacious padding/margins
- **Right-click context menu**: Toggle quantity mode (All/Exact/Minimum) and clear slot
- **Visual slot expansion**: Non-stackable items show true inventory footprint
  - 5 sharks = 5 separate visual slots
  - Expansion slots have slightly dimmed styling
  - Clearing primary slot clears all expansion slots
- **StackabilityUtil**: Determines if items stack (runes, ammo) vs expand (food, potions)

## Task Commits

Initial implementation:
1. **Task 1: Create PouchGrid component** - `fede350` (feat)
2. **Task 2: Create LoadoutEditor main window** - `3deca87` (feat)

Post-checkpoint fixes:
3. **Visual expansion + context menu** - `a60edb7` (feat)
4. **OSRS theme colors** - `2d1b75f` (style)

## Files Created/Modified

**Created:**
- `utilities/src/main/java/utilities/loadout/ui/PouchGrid.java` - Configurable pouch container
- `utilities/src/main/java/utilities/loadout/ui/LoadoutEditor.java` - Main editor popup (620x560)
- `utilities/src/main/java/utilities/items/StackabilityUtil.java` - Item stackability detection

**Modified:**
- `utilities/src/main/java/utilities/loadout/ui/ItemSlot.java`
  - Added right-click context menu for mode toggle
  - BiConsumer callback for mode changes
- `utilities/src/main/java/utilities/loadout/ui/ItemSearch.java`
  - OSRS theme colors
  - MAX_ITEM_ID 30000
- `utilities/src/main/java/utilities/loadout/ui/EquipmentGrid.java`
  - OSRS theme colors
- `utilities/src/main/java/utilities/loadout/ui/InventoryGrid.java`
  - 4x7 layout
  - Visual slot expansion for non-stackables
  - Primary/expansion slot tracking

## Technical Details

### Visual Slot Expansion
The inventory grid now shows a "post-withdrawal preview":
- **Stackable items** (runes, coins, ammo): 1 slot with quantity badge
- **Non-stackable items** (food, potions): N slots for quantity N
- Primary slots are fully opaque, expansion slots are slightly dimmed (85% opacity)
- Expansion slots are not directly clickable - interact with the primary slot

### Right-Click Context Menu
When right-clicking an occupied slot:
- "Set to All (*)" - UNLIMITED mode, withdraw all from bank
- "Set to Exact" - EXACT mode, withdraw specific quantity
- "Set to Minimum (>)" - MINIMUM mode, fail if bank has fewer
- "Clear Slot" - Remove item from loadout

### StackabilityUtil
Heuristic-based stackability detection:
- Known stackable IDs: coins, runes, arrows, bolts, darts, chinchompas, cannonballs
- Extensible via `registerStackable(itemId)` for custom items
- Defaults to non-stackable for safety (shows more slots than needed is better than fewer)

## Decisions Made

1. **OSRS equipment tab theme** - User requested matching the in-game look
   - Background: `#3E3529` (dark brown)
   - Borders: `#5C5142` (tan/orange)
   - Slot interiors: `#252116` (darker brown)
   - Text/accents: `#FF981F` (OSRS orange)

2. **Visual slot expansion** - User wanted "the loadout to show what the inventory looks like after withdrawal"
   - Non-stackables expand to true slot count
   - Provides accurate inventory space planning

3. **StackabilityUtil heuristics** - Cannot access game definitions for stackability
   - Pre-populated with common stackable items
   - Scripts can register additional stackables as needed

## Known Limitations

1. **Persistence (Phase 6 scope):** Loadout resets on script restart
2. **Stackability coverage:** Heuristic-based, may not cover all items
3. **Expansion slot clicking:** Expansion slots don't open item search (by design)

## Next Phase Readiness

- LoadoutEditor UI complete with all requested features
- Ready for Phase 04: Bank Search Utility (new functionality)
- Persistence will be added in Phase 6

---
*Phase: 03-editor-ui*
*Completed: 2026-01-16*
