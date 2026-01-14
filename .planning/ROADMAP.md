# Roadmap: BankSearchUtils

## Milestones

- ✅ [v1.0 MVP](milestones/v1.0-ROADMAP.md) (Phases 1-5) — SHIPPED 2026-01-14
- ✅ [v1.1 Fix Search Opening](milestones/v1.1-ROADMAP.md) (Phase 6) — SHIPPED 2026-01-14
- 🚧 **v1.2 Verified Withdrawals** - Phases 7-9 (in progress)

## Overview

Bank search-by-name utility with scroll fallback, batch withdrawals, and fill-to-capacity support.

## Status

**v1.2 in progress** — Verified withdrawals with item ID search and scroll position detection.

---

### 🚧 v1.2 Verified Withdrawals (In Progress)

**Milestone Goal:** Make searchAndWithdrawByName robust by verifying items exist before withdrawing

#### Phase 7: Scroll Position Detection

**Goal**: Sprite-based at-top/at-bottom detection for bank scroll position
**Depends on**: Phase 6
**Research**: Unlikely (extends existing BankScrollUtils patterns)
**Plans**: TBD

Plans:
- [ ] 07-01: TBD (run /gsd:plan-phase 7 to break down)

#### Phase 8: Item ID Screen Search

**Goal**: Search visible bank area for item by ID with tapGetResponse verification
**Depends on**: Phase 7
**Research**: Unlikely (existing API patterns)
**Plans**: TBD

Plans:
- [ ] 08-01: TBD (run /gsd:plan-phase 8 to break down)

#### Phase 9: Robust Withdrawal Flow

**Goal**: Refactor searchAndWithdrawByName with new verified item search flow
**Depends on**: Phase 8
**Research**: Unlikely (internal refactor)
**Plans**: TBD

Plans:
- [ ] 09-01: TBD (run /gsd:plan-phase 9 to break down)

---

<details>
<summary>✅ v1.1 Fix Search Opening (Phase 6) — SHIPPED 2026-01-14</summary>

- [x] Phase 6: Tap-Based Search Activation (1/1 plan) — completed 2026-01-14

See [v1.1 archive](milestones/v1.1-ROADMAP.md) for full details.

</details>

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
| 6. Tap-Based Search Activation | v1.1 | 1/1 | Complete | 2026-01-14 |
| 7. Scroll Position Detection | v1.2 | 0/? | Not started | - |
| 8. Item ID Screen Search | v1.2 | 0/? | Not started | - |
| 9. Robust Withdrawal Flow | v1.2 | 0/? | Not started | - |

**Total:** 9 phases, 8+ plans (8 complete)
