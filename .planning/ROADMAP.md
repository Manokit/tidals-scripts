# Roadmap: TidalsSecondaryCollector - Fairy Ring Mode

## Overview

Add fairy ring support to TidalsSecondaryCollector for players without Drakan's Medallion. The script will auto-detect equipment to choose between Ver Sinhaza mode (existing) and Fairy Ring mode (new), then handle collection, banking, and return accordingly.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Mode Detection** - Auto-detect equipment configuration
- [ ] **Phase 2: Collection** - 3-log tile fungus collection with inventory bloom
- [ ] **Phase 3: Banking & Return** - Zanaris banking + monastery return loop

## Phase Details

### Phase 1: Mode Detection
**Goal**: Auto-detect fairy ring vs Ver Sinhaza mode from equipment
**Depends on**: Nothing (first phase)
**Requirements**: MODE-01, MODE-02
**Success Criteria** (what must be TRUE):
  1. Script detects Dramen staff + inventory bloom + ardy cloak → uses Fairy Ring mode
  2. Script detects Drakan's Medallion + equipped bloom → uses Ver Sinhaza mode (existing)
  3. Mode selection happens automatically without user configuration
**Research**: Unlikely (internal equipment checking)
**Plans**: TBD

Plans:
- [ ] 01-01: Equipment detection and mode selection logic

### Phase 2: Collection
**Goal**: Collect fungus at 3-log tile with inventory bloom
**Depends on**: Phase 1
**Requirements**: COLL-01, COLL-02
**Success Criteria** (what must be TRUE):
  1. Script navigates to 3-log tile at (3474, 3419, 0) when in Fairy Ring mode
  2. Script casts bloom from inventory item (not equipment slot)
  3. Script collects fungus from all 3 logs around the tile
**Research**: Unlikely (existing pattern, different tile)
**Plans**: TBD

Plans:
- [ ] 02-01: 3-log tile collection and inventory bloom casting

### Phase 3: Banking & Return
**Goal**: Bank via Zanaris, return via monastery fairy ring
**Depends on**: Phase 2
**Requirements**: BANK-01, BANK-02, RETN-01, RETN-02, RETN-03
**Success Criteria** (what must be TRUE):
  1. Script uses fairy ring at (3469, 3431, 0) to teleport to Zanaris
  2. Script walks to Zanaris bank chest and deposits fungus
  3. Script uses ardy cloak → monastery → walks to fairy ring → "Last-destination (BKR)"
  4. Script validates BKR is configured before using "Last-destination"
  5. Script terminates safely if BKR not configured
**Research**: Unlikely (fairy ring pattern in dkTravel.java, paths documented)
**Plans**: TBD

Plans:
- [ ] 03-01: Zanaris banking via fairy ring
- [ ] 03-02: Monastery return and BKR validation

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Mode Detection | 0/1 | Not started | - |
| 2. Collection | 0/1 | Not started | - |
| 3. Banking & Return | 0/2 | Not started | - |
