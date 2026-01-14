# BankSearchUtils

## What This Is

A reusable utility for TidalsUtilities that searches for and withdraws items from the bank by name, eliminating the need for users to specify bank tabs or reorganize their banks. Uses the bank's search box feature with fallback scrolling to reliably locate items.

## Core Value

Reliable search — finding items by name must work 100% of the time, regardless of bank organization.

## Requirements

### Validated

- ✓ RetryUtils — menu interaction retries with configurable attempts — existing
- ✓ BankingUtils — bank open/close, deposits, basic withdrawals — existing
- ✓ TabUtils — tab opening with verification — existing
- ✓ DialogueUtils — dialogue handling — existing
- ✓ Search for items by name using bank search box — v1.0
- ✓ Scroll bank contents as fallback when search doesn't find item — v1.0
- ✓ Withdraw single item by name with specified amount — v1.0
- ✓ Withdraw list of items with amounts in sequence — v1.0
- ✓ Fill remaining inventory slots with searched item — v1.0
- ✓ Return false/null when item not found (caller handles) — v1.0
- ✓ Integrate with existing BankingUtils patterns — v1.0
- ✓ Visual sprite-based search button activation — v1.1

### Active

(None — v1.1 shipped)

### Out of Scope

- Deposit operations — use existing Bank methods; focus on withdrawals only
- Tab caching/memory — don't track which tabs items are in; always search fresh
- Quantity detection — don't read how many of an item exists in bank

## Context

**Technical environment:**
- OSMB color bot framework (visual detection, not injection)
- Bank.md API: `Bank` interface with `withdraw()`, `close()`, search button
- Keyboard.md API: `type()` for search input, `pressKey()` for confirmation
- Existing utilities: RetryUtils pattern for reliable interactions

**Integration points:**
- Lives in `utilities/src/main/java/utilities/BankSearchUtils.java`
- Depends on OSMB API (Bank, Keyboard, Script reference)
- Used by all Tidals scripts that need flexible item retrieval

**User workflow:**
- Scripts call `BankSearchUtils.searchAndWithdraw(script, "Shark", 10)`
- Utility handles: open search → type name → locate item → withdraw → close search
- Calling script doesn't need to know bank organization

## Constraints

- **API compatibility**: Must use OSMB Bank and Keyboard interfaces as documented in Bank.md and Keyboard.md
- **Pattern consistency**: Follow existing *Utils static method patterns (Script as first parameter)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Search box first, scroll fallback | Search is fastest for known items; scroll handles edge cases | ✓ Good |
| Return null/false on not found | Let calling scripts decide behavior; don't force exception handling | ✓ Good |
| Withdrawals only in v1 | Deposits already work fine with existing Bank methods; focus on the pain point | ✓ Good |
| Keyboard shortcut over button click | No direct API for clicking BankButtonType buttons; keyboard approach is cleaner | ⚠️ Revisit — didn't work |
| Backspace key for activation | Safer than letter keys as it clears any partial search without adding characters | ⚠️ Revisit — didn't activate search |
| Sprite-based SEARCH button tap | Keyboard shortcuts don't activate search; visual tap using sprite ID 1043 works reliably | ✓ Good — v1.1 |
| PhysicalKey.BACK over ESCAPE | OSMB PhysicalKey enum does not have ESCAPE; BACK is mobile equivalent | ✓ Good |
| Sprite-based scroll detection | PixelAnalyzer.findSubImages() doesn't exist; sprite + ImageAnalyzer proven reliable | ✓ Good |
| Continue batch on partial failure | Batch operations complete even if some items fail to withdraw | ✓ Good |
| keepSearchOpen for batch efficiency | Avoids repeated search clear/open cycles during multi-item withdrawals | ✓ Good |

## Context

**Current state:**
- v1.1 shipped: BankSearchUtils complete with 1,171 LOC Java
- Tech stack: OSMB API (Bank, Keyboard, SpriteManager, ImageAnalyzer)
- Utility location: `utilities/src/main/java/utilities/BankSearchUtils.java`
- Supporting classes: BankScrollUtils, WithdrawalRequest, BatchWithdrawalResult

**API methods delivered:**
- openSearch() (now sprite-based), typeSearch(), clearSearch(), isSearchActive()
- searchAndWithdraw(), searchAndFillInventory()
- withdrawBatch() with List and varargs overloads
- BankScrollUtils: scrollDown/Up, canScroll, scrollToTop/Bottom

---
*Last updated: 2026-01-14 after v1.1 milestone*
