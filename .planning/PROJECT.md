# BankSearchUtils

## What This Is

A reusable utility for TidalsUtilities that searches for and withdraws items from the bank by name, with verified item detection via tapGetResponse and humanized interactions. Eliminates the need for users to specify bank tabs or reorganize their banks. Uses the bank's search box feature with scroll fallback and visual verification to reliably locate and withdraw items. Includes randomized tap coordinates and humanized typing delays to appear more natural.

## Core Value

Reliable search — finding items by name must work 100% of the time, regardless of bank organization.

## Requirements

### Validated

- RetryUtils — menu interaction retries with configurable attempts — existing
- BankingUtils — bank open/close, deposits, basic withdrawals — existing
- TabUtils — tab opening with verification — existing
- DialogueUtils — dialogue handling — existing
- Search for items by name using bank search box — v1.0
- Scroll bank contents as fallback when search doesn't find item — v1.0
- Withdraw single item by name with specified amount — v1.0
- Withdraw list of items with amounts in sequence — v1.0
- Fill remaining inventory slots with searched item — v1.0
- Return false/null when item not found (caller handles) — v1.0
- Integrate with existing BankingUtils patterns — v1.0
- Visual sprite-based search button activation — v1.1
- Sprite-based scroll position detection (isAtTop/isAtBottom) — v1.2
- Visual item search with tapGetResponse verification — v1.2
- Verified withdrawal flow eliminating blind offset tapping — v1.2
- Randomized tap coordinates within sprite bounds — v1.3
- Humanized typing with per-character delays (50-150ms) — v1.3
- Sprite-based item detection replacing broken bank.search() — v1.4

### Active

(None — v1.4 shipped, BankSearchUtils feature-complete with reliable detection)

### Out of Scope

- Deposit operations — use existing Bank methods; focus on withdrawals only
- Tab caching/memory — don't track which tabs items are in; always search fresh
- Quantity detection — don't read how many of an item exists in bank

## Context

**Current state:**
- v1.4 shipped: BankSearchUtils complete with 2,499 LOC Java (all utilities)
- Tech stack: OSMB API (Bank, Keyboard, SpriteManager, ImageAnalyzer, ItemManager)
- Utility location: `utilities/src/main/java/utilities/BankSearchUtils.java`
- Supporting classes: BankScrollUtils, WithdrawalRequest, BatchWithdrawalResult

**API methods delivered:**
- openSearch() (sprite-based), typeSearch() (humanized), clearSearch(), isSearchActive()
- searchAndWithdraw(), searchAndWithdrawByName() (with verification)
- searchAndFillInventory()
- withdrawBatch() with List and varargs overloads
- findAndVerifyItem(), searchBankForItem() (item ID visual search)
- getWithdrawActions() (standardized action string generation)
- BankScrollUtils: scrollDown/Up (randomized), canScroll, scrollToTop/Bottom, isAtTop/isAtBottom

## Constraints

- **API compatibility**: Must use OSMB Bank and Keyboard interfaces as documented in Bank.md and Keyboard.md
- **Pattern consistency**: Follow existing *Utils static method patterns (Script as first parameter)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Search box first, scroll fallback | Search is fastest for known items; scroll handles edge cases | Good |
| Return null/false on not found | Let calling scripts decide behavior; don't force exception handling | Good |
| Withdrawals only in v1 | Deposits already work fine with existing Bank methods; focus on the pain point | Good |
| Sprite-based SEARCH button tap | Keyboard shortcuts don't activate search; visual tap using sprite ID 1043 works reliably | Good — v1.1 |
| PhysicalKey.BACK over ESCAPE | OSMB PhysicalKey enum does not have ESCAPE; BACK is mobile equivalent | Good |
| Sprite-based scroll detection | PixelAnalyzer.findSubImages() doesn't exist; sprite + ImageAnalyzer proven reliable | Good |
| Continue batch on partial failure | Batch operations complete even if some items fail to withdraw | Good |
| keepSearchOpen for batch efficiency | Avoids repeated search clear/open cycles during multi-item withdrawals | Good |
| Fixed Y coordinate scroll detection | Sprite 789/791 Y positions (334/507) reliably indicate top/bottom | Good — v1.2 |
| tapGetResponse verification | Verify item identity via menu before withdrawal action | Good — v1.2 |
| First slot verification only | Bank search consolidates to top-left; verify first slot is sufficient | Good — v1.2 |
| isAtBottom() for scroll termination | More reliable than canScrollDown() using scrollbar sprite position | Good — v1.2 |
| Rectangle bounds for sprite taps | getBounds() allows random click positions within sprite for humanization | Good — v1.3 |
| 50-150ms typing delay range | Mimics natural human typing speed variance without being too slow | Good — v1.3 |
| findAndVerifyItem() everywhere | bank.search() was failing to detect visible items; sprite detection works | Good — v1.4 |
| Re-verify bounds after scroll | Ensures withdrawal targets correct location after scroll movement | Good — v1.4 |
| getWithdrawActions() helper | Standardizes action string generation for all withdraw amounts | Good — v1.4 |

---
*Last updated: 2026-01-15 after v1.4 milestone*
