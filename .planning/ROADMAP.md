# Roadmap: BankSearchUtils

## Milestones

- ✅ [v1.0 MVP](milestones/v1.0-ROADMAP.md) (Phases 1-5) — SHIPPED 2026-01-14
- 🚧 **v1.1 Fix Search Opening** — Phase 6 (in progress)

## Overview

Bank search-by-name utility with scroll fallback, batch withdrawals, and fill-to-capacity support.

## Status

**v1.1 in progress** — Fixing search button activation.

---

### 🚧 v1.1 Fix Search Opening (In Progress)

**Milestone Goal:** Fix openSearch() to actually work by tapping the SEARCH button visually instead of using non-functional keyboard shortcuts.

#### Phase 6: Tap-Based Search Activation

**Goal**: Replace keyboard shortcut with visual tap on SEARCH button
**Depends on**: v1.0 complete
**Research**: Unlikely (follows established sprite-based patterns from BankScrollUtils)
**Plans**: 1 plan

The current openSearch() uses BACKSPACE key which doesn't activate bank search. Fix by:
1. Finding SEARCH button sprite/position (like BankScrollUtils finds scroll buttons)
2. Tapping with Finger
3. Verifying search activated

Plans:
- [ ] 06-01: Sprite-based SEARCH button tap

<details>
<summary>✅ v1.0 MVP (Phases 1-5) — SHIPPED 2026-01-14</summary>

- [x] Phase 1: Core Search Infrastructure (2/2 plans) — completed 2026-01-14
- [x] Phase 2: Single Item Withdrawal (1/1 plan) — completed 2026-01-14
- [x] Phase 3: Scroll Fallback (2/2 plans) — completed 2026-01-14
- [x] Phase 4: Batch Withdrawal (1/1 plan) — completed 2026-01-14
- [x] Phase 5: Fill Inventory (1/1 plan) — completed 2026-01-14

</details>

## Progress

| Phase | Milestone | Plans | Status | Completed |
|-------|-----------|-------|--------|-----------|
| 1. Core Search Infrastructure | v1.0 | 2/2 | Complete | 2026-01-14 |
| 2. Single Item Withdrawal | v1.0 | 1/1 | Complete | 2026-01-14 |
| 3. Scroll Fallback | v1.0 | 2/2 | Complete | 2026-01-14 |
| 4. Batch Withdrawal | v1.0 | 1/1 | Complete | 2026-01-14 |
| 5. Fill Inventory | v1.0 | 1/1 | Complete | 2026-01-14 |
| 6. Tap-Based Search Activation | v1.1 | 0/1 | Not started | - |

**Total:** 6 phases, 8 plans (7 complete, 1 pending)
