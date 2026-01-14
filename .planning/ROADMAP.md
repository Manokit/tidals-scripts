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

#### Phase 7: Scroll Position Detection (Complete)

**Goal**: Sprite-based at-top/at-bottom detection for bank scroll position
**Depends on**: Phase 6
**Research**: Unlikely (extends existing BankScrollUtils patterns)
**Plans**: 1/1

Plans:
- [x] 07-01: Add isAtTop() and isAtBottom() methods — completed 2026-01-14

#### Phase 8: Item ID Screen Search (Complete)

**Goal**: Search visible bank area for item by ID with tapGetResponse verification
**Depends on**: Phase 7
**Research**: Unlikely (existing API patterns)
**Plans**: 1/1

Plans:
- [x] 08-01: Add item ID visual search methods — completed 2026-01-14

#### Phase 9: Robust Withdrawal Flow (Complete)

**Goal**: Refactor searchAndWithdrawByName with new verified item search flow
**Depends on**: Phase 8
**Research**: Unlikely (internal refactor)
**Plans**: 1/1

Plans:
- [x] 09-01: Refactor searchAndWithdrawByName with verified item search — completed 2026-01-14

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
| 7. Scroll Position Detection | v1.2 | 1/1 | Complete | 2026-01-14 |
| 8. Item ID Screen Search | v1.2 | 1/1 | Complete | 2026-01-14 |
| 9. Robust Withdrawal Flow | v1.2 | 1/1 | Complete | 2026-01-14 |

**Total:** 9 phases, 11 plans (11 complete)
