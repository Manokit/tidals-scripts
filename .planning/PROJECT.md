# BankSearchUtils

## What This Is

A reusable utility for TidalsUtilities that searches for and withdraws items from the bank by name, with verified item detection via tapGetResponse. Eliminates the need for users to specify bank tabs or reorganize their banks. Uses the bank's search box feature with scroll fallback and visual verification to reliably locate and withdraw items.

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

### Active

(None — v1.2 shipped, BankSearchUtils feature-complete)

### Out of Scope

- Deposit operations — use existing Bank methods; focus on withdrawals only
- Tab caching/memory — don't track which tabs items are in; always search fresh
- Quantity detection — don't read how many of an item exists in bank

## Context

**Current state:**
- v1.2 shipped: BankSearchUtils complete with 1,446 LOC Java
- Tech stack: OSMB API (Bank, Keyboard, SpriteManager, ImageAnalyzer, ItemManager)
- Utility location: `utilities/src/main/java/utilities/BankSearchUtils.java`
- Supporting classes: BankScrollUtils, WithdrawalRequest, BatchWithdrawalResult

**API methods delivered:**
- openSearch() (sprite-based), typeSearch(), clearSearch(), isSearchActive()
- searchAndWithdraw(), searchAndWithdrawByName() (now with verification)
- searchAndFillInventory()
- withdrawBatch() with List and varargs overloads
- findAndVerifyItem(), searchBankForItem() (item ID visual search)
- BankScrollUtils: scrollDown/Up, canScroll, scrollToTop/Bottom, isAtTop/isAtBottom

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

---
*Last updated: 2026-01-14 after v1.2 milestone*
