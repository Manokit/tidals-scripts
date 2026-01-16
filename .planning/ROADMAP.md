# Roadmap: Loadout Maker Utility

## Overview

Build a comprehensive loadout management utility for OSMB scripts. Starting with core data structures, then item resolution with sprite support, followed by the visual JavaFX editor, import/export capabilities, intelligent restock logic, and finally script integration with a validation test script.

## Domain Expertise

None

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Data Model** - Define LoadoutItem, Loadout, containers, and quantity modes
- [x] **Phase 2: Item Resolution** - OSMB ItemManager integration, Wiki fallback, degradable matching
- [x] **Phase 3: Editor UI** - JavaFX popup with equipment/inventory slots and item search
- [x] **Phase 4: Import/Export** - RuneLite JSON import, custom format export
- [x] **Phase 5: Restock Logic** - Compare current state to baseline, calculate missing items
- [x] **Phase 6: Script Integration** - Loadout API for scripts, banking task, persistence
- [x] **Phase 7: Validation** - TidalsLoadoutTester script to verify full workflow

## Phase Details

### Phase 1: Data Model
**Goal**: Define core types — LoadoutItem (id, name, quantity, mode, fuzzy flag), Loadout (equipment, inventory, rune pouch, bolt pouch, quiver), quantity modes (exact, unlimited, minimum)
**Depends on**: Nothing (first phase)
**Research**: Unlikely (internal patterns, standard Java)
**Plans**: TBD

### Phase 2: Item Resolution
**Goal**: Build item lookup system — OSMB ItemManager for sprites/IDs, Wiki API fallback for gaps, automatic degradable item matching (Barrows, Crystal, etc.)
**Depends on**: Phase 1
**Research**: Likely (external APIs, OSMB gaps)
**Research topics**: OSMB ItemManager sprite availability, Wiki API for sprites/IDs, degradable item ID patterns
**Plans**: TBD

### Phase 3: Editor UI
**Goal**: JavaFX popup window with equipment grid (14 slots), inventory grid (28 slots), rune/bolt pouch, quiver slots, item search with autocomplete
**Depends on**: Phase 2
**Research**: Unlikely (JavaFX patterns established in existing scripts)
**Plans**: TBD

### Phase 4: Import/Export
**Goal**: Import RuneLite Inventory Setups JSON format, export custom JSON format with extended fields (quantity modes, fuzzy flags)
**Depends on**: Phase 3
**Research**: Unlikely (JSON parsing, format documented in EXPORT_FORMAT.md)
**Plans**: TBD

### Phase 5: Restock Logic
**Goal**: Compare current equipment/inventory to loadout baseline, determine missing items, handle quantity modes correctly, integrate with BankingUtils
**Depends on**: Phase 4
**Research**: Unlikely (internal logic, existing utilities)
**Plans**: TBD

### Phase 6: Script Integration
**Goal**: Loadout object API for scripts to pass to banking tasks, Java Preferences persistence, popup launcher button for ScriptUI
**Depends on**: Phase 5
**Research**: Unlikely (following existing ScriptUI patterns)
**Plans**: TBD

### Phase 7: Validation
**Goal**: TidalsLoadoutTester script — create loadout, equip items, bank, verify restock fills only missing items
**Depends on**: Phase 6
**Research**: Unlikely (internal testing)
**Plans**: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5 → 6 → 7

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Data Model | 1/1 | Complete | 2026-01-15 |
| 2. Item Resolution | 2/2 | Complete | 2026-01-15 |
| 3. Editor UI | 3/3 | Complete | 2026-01-16 |
| 4. Import/Export | 2/2 | Complete | 2026-01-16 |
| 5. Restock Logic | 2/2 | Complete | 2026-01-16 |
| 6. Script Integration | 2/2 | Complete | 2026-01-16 |
| 7. Validation | 1/1 | Complete | 2026-01-16 |
