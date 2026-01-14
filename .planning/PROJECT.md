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

### Active

- [ ] Search for items by name using bank search box
- [ ] Scroll bank contents as fallback when search doesn't find item
- [ ] Withdraw single item by name with specified amount
- [ ] Withdraw list of items with amounts in sequence
- [ ] Fill remaining inventory slots with searched item
- [ ] Return false/null when item not found (caller handles)
- [ ] Integrate with existing BankingUtils patterns

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
| Search box first, scroll fallback | Search is fastest for known items; scroll handles edge cases | — Pending |
| Return null/false on not found | Let calling scripts decide behavior; don't force exception handling | — Pending |
| Withdrawals only in v1 | Deposits already work fine with existing Bank methods; focus on the pain point | — Pending |

---
*Last updated: 2026-01-14 after initialization*
