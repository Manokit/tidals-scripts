# Project Milestones: BankSearchUtils

## v1.3 Humanization (Shipped: 2026-01-14)

**Delivered:** Human-like interactions with randomized tap coordinates and typing delays.

**Phases completed:** 10-11 (2 plans total)

**Key accomplishments:**

- Converted sprite taps from Point to Rectangle bounds for random click positions
- Added typeWithDelay() helper for humanized character-by-character typing
- Updated typeSearch() with 50-150ms random delays between keystrokes
- Rebuilt TidalsUtilities.jar with all humanization changes

**Stats:**

- 9 files created/modified
- 2,362 lines of Java (all utilities combined)
- 2 phases, 2 plans
- Same day as v1.2 ship

**Git range:** `2891aa9` → `f28567c`

**What's next:** BankSearchUtils feature-complete. Integrate into Tidals scripts as needed.

---

## v1.2 Verified Withdrawals (Shipped: 2026-01-14)

**Delivered:** Robust searchAndWithdrawByName that verifies items exist via tapGetResponse before withdrawing.

**Phases completed:** 7-9 (3 plans total)

**Key accomplishments:**

- Added sprite-based scroll position detection (isAtTop/isAtBottom methods)
- Implemented visual item search with tapGetResponse verification
- Refactored searchAndWithdrawByName to verify items before withdrawal
- Eliminated blind fixed-offset tap-and-hope approach

**Stats:**

- 12 files created/modified
- 1,446 lines of Java (BankSearchUtils + BankScrollUtils combined)
- 3 phases, 3 plans
- Same day as v1.1 ship

**Git range:** `a98a65d` → `c206736`

**What's next:** Integrate verified withdrawal flow into Tidals scripts.

---

## v1.1 Fix Search Opening (Shipped: 2026-01-14)

**Delivered:** Fixed openSearch() to use sprite-based tap instead of non-functional keyboard shortcuts.

**Phases completed:** 6 (1 plan total)

**Key accomplishments:**

- Replaced BACKSPACE keyboard shortcut with visual SEARCH button tap
- Used sprite ID 1043 for reliable button detection
- Followed established BankScrollUtils sprite patterns

**Stats:**

- 5 files created/modified
- 1,171 lines of Java (cumulative)
- 1 phase, 1 plan
- Same day as v1.0 ship

**Git range:** `7ce783b` → `bdb2747`

**What's next:** v1.2 Verified Withdrawals - make withdrawals robust with item verification.

---

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
