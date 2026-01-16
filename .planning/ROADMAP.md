# Roadmap: TidalsSecondaryCollector v1.1

## Overview

Flexible Validation milestone: Make equipment validation more permissive by accepting required items in inventory (not just equipped), and add region prioritization for faster fairy ring mode startup.

## Milestones

- ✅ **v1.0 Fairy Ring Mode** - Phases 1-3 (shipped 2026-01-16)
- 🚧 **v1.1 Flexible Validation** - Phases 4-5 (in progress)

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

- [ ] **Phase 4: Flexible Equipment Validation** - Accept items in inventory, not just equipped
- [ ] **Phase 5: Region Optimization** - Add region 13877 for faster startup

## Phase Details

<details>
<summary>✅ v1.0 Fairy Ring Mode (Phases 1-3) - SHIPPED 2026-01-16</summary>

### Phase 1: Mode Detection
**Goal**: Auto-detect Ver Sinhaza vs Fairy Ring mode from equipment
**Plans**: 1 plan

Plans:
- [x] 01-01: Equipment detection and mode flag

### Phase 2: Fairy Ring Collection
**Goal**: 3-log tile collection with inventory bloom
**Plans**: 1 plan

Plans:
- [x] 02-01: Collection tile and bloom from inventory

### Phase 3: Banking & Return
**Goal**: Zanaris banking and BKR return teleport
**Plans**: 2 plans

Plans:
- [x] 03-01: Zanaris banking path
- [x] 03-02: Monastery fairy ring return

</details>

### 🚧 v1.1 Flexible Validation (In Progress)

**Milestone Goal:** More permissive equipment validation and faster startup

#### Phase 4: Flexible Equipment Validation
**Goal**: Accept required items in inventory, not just equipped
**Depends on**: Phase 3 (v1.0 complete)
**Requirements**: VALID-01, VALID-02, VALID-03, VALID-04
**Success Criteria** (what must be TRUE):
  1. Script starts successfully with bloom item in inventory only
  2. Script starts successfully with ardy cloak in inventory only
  3. Script starts successfully with quest cape in inventory only
  4. Script auto-equips dramen staff from inventory at startup
**Research**: Unlikely (internal validation changes)
**Plans**: TBD

Plans:
- [ ] 04-01: TBD

#### Phase 5: Region Optimization
**Goal**: Faster startup in fairy ring mode
**Depends on**: Phase 4
**Requirements**: PERF-01
**Success Criteria** (what must be TRUE):
  1. Region 13877 included in regionsToPrioritise() return value
**Research**: Unlikely (simple code addition)
**Plans**: 1 plan

Plans:
- [ ] 05-01: Add region 13877 to regionsToPrioritise()

## Progress

**Execution Order:**
Phases execute in numeric order: 4 → 5

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. Mode Detection | v1.0 | 1/1 | Complete | 2026-01-16 |
| 2. Fairy Ring Collection | v1.0 | 1/1 | Complete | 2026-01-16 |
| 3. Banking & Return | v1.0 | 2/2 | Complete | 2026-01-16 |
| 4. Flexible Equipment Validation | v1.1 | 0/1 | Not started | - |
| 5. Region Optimization | v1.1 | 0/1 | Not started | - |
