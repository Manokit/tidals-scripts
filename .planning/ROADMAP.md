# Roadmap: BankSearchUtils

## Milestones

- [v1.0 MVP](milestones/v1.0-ROADMAP.md) (Phases 1-5) — SHIPPED 2026-01-14
- [v1.1 Fix Search Opening](milestones/v1.1-ROADMAP.md) (Phase 6) — SHIPPED 2026-01-14
- [v1.2 Verified Withdrawals](milestones/v1.2-ROADMAP.md) (Phases 7-9) — SHIPPED 2026-01-14
- 🚧 **v1.3 Humanization** - Phases 10-13 (in progress)

## Overview

Bank search-by-name utility with scroll fallback, batch withdrawals, fill-to-capacity support, and verified item detection.

## Status

**v1.3 in progress** — Adding humanization: randomized tap coordinates and typing delays.

---

### 🚧 v1.3 Humanization (In Progress)

**Milestone Goal:** Make sprite interactions appear human-like by randomizing tap coordinates within sprite bounds and adding variable typing delays.

#### Phase 10: Randomized Sprite Taps - COMPLETE

**Goal**: Convert Point-based taps to Rectangle-based taps with random coordinates within sprite bounds
**Depends on**: v1.2 complete
**Research**: Unlikely (internal patterns)
**Plans**: 1/1

Plans:
- [x] 10-01: Convert sprite taps to Rectangle bounds — completed 2026-01-14

#### Phase 11: Humanized Typing - COMPLETE

**Goal**: Add random delays between characters when typing search queries
**Depends on**: Phase 10
**Research**: Unlikely (internal patterns)
**Plans**: 1/1

Plans:
- [x] 11-01: Add per-character typing delays — completed 2026-01-14

#### Phase 12: Integration Testing

**Goal**: Test with TidalsWithdrawer to verify tap positions vary and typing looks natural
**Depends on**: Phase 11
**Research**: Unlikely (testing)
**Plans**: TBD

Plans:
- [ ] 12-01: TBD

#### Phase 13: Utility Rebuild

**Goal**: Final build, verification, and documentation update
**Depends on**: Phase 12
**Research**: Unlikely (build process)
**Plans**: TBD

Plans:
- [ ] 13-01: TBD

---

<details>
<summary>v1.2 Verified Withdrawals (Phases 7-9) — SHIPPED 2026-01-14</summary>

- [x] Phase 7: Scroll Position Detection (1/1 plan) — completed 2026-01-14
- [x] Phase 8: Item ID Screen Search (1/1 plan) — completed 2026-01-14
- [x] Phase 9: Robust Withdrawal Flow (1/1 plan) — completed 2026-01-14

See [v1.2 archive](milestones/v1.2-ROADMAP.md) for full details.

</details>

<details>
<summary>v1.1 Fix Search Opening (Phase 6) — SHIPPED 2026-01-14</summary>

- [x] Phase 6: Tap-Based Search Activation (1/1 plan) — completed 2026-01-14

See [v1.1 archive](milestones/v1.1-ROADMAP.md) for full details.

</details>

<details>
<summary>v1.0 MVP (Phases 1-5) — SHIPPED 2026-01-14</summary>

- [x] Phase 1: Core Search Infrastructure (2/2 plans) — completed 2026-01-14
- [x] Phase 2: Single Item Withdrawal (1/1 plan) — completed 2026-01-14
- [x] Phase 3: Scroll Fallback (2/2 plans) — completed 2026-01-14
- [x] Phase 4: Batch Withdrawal (1/1 plan) — completed 2026-01-14
- [x] Phase 5: Fill Inventory (1/1 plan) — completed 2026-01-14

See [v1.0 archive](milestones/v1.0-ROADMAP.md) for full details.

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
| 10. Randomized Sprite Taps | v1.3 | 1/1 | Complete | 2026-01-14 |
| 11. Humanized Typing | v1.3 | 1/1 | Complete | 2026-01-14 |
| 12. Integration Testing | v1.3 | 0/? | Not started | - |
| 13. Utility Rebuild | v1.3 | 0/? | Not started | - |

**Total:** 13 phases, 11+ plans
