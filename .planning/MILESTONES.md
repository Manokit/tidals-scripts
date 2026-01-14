# Project Milestones: BankSearchUtils

## v1.0 MVP (Shipped: 2026-01-14)

**Delivered:** Bank search-by-name utility with scroll fallback, batch withdrawals, and fill-to-capacity support.

**Phases completed:** 1-5 (7 plans total)

**Key accomplishments:**

- Created BankSearchUtils with keyboard-based search activation
- Implemented sprite-based scroll button detection using SpriteManager + ImageAnalyzer
- Integrated automatic scroll fallback when search doesn't find items
- Added batch withdrawal with partial failure handling
- Implemented searchAndFillInventory() matching BankingUtils patterns
- Documented 13 key API decisions for future reference

**Stats:**

- 29 files created/modified
- 1,445 lines of Java (new utilities)
- 5 phases, 7 plans, ~13 tasks
- 0.70 hours from start to ship

**Git range:** `feat(01-01)` → `docs(05-01)`

**What's next:** Integrate BankSearchUtils into Tidals scripts that need flexible bank item retrieval.

---
