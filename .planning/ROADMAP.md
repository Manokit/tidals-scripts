# Roadmap: BankSearchUtils

## Overview

Build a utility that searches the bank by name and withdraws items, starting with core search box infrastructure, then single-item withdrawal, scroll fallback for edge cases, batch operations, and fill-to-capacity functionality. Each phase delivers a complete, testable capability.

## Domain Expertise

None

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

- [ ] **Phase 1: Core Search Infrastructure** - Bank search box interaction and typing
- [ ] **Phase 2: Single Item Withdrawal** - Search and withdraw one item by name
- [ ] **Phase 3: Scroll Fallback** - Scroll bank when search doesn't find item
- [ ] **Phase 4: Batch Withdrawal** - Withdraw list of items in sequence
- [ ] **Phase 5: Fill Inventory** - Fill remaining slots with searched item

## Phase Details

### Phase 1: Core Search Infrastructure
**Goal**: Implement bank search box interaction - open search, type item name, clear search
**Depends on**: Nothing (first phase)
**Research**: Likely (Bank search box UI, Keyboard API)
**Research topics**: Bank search button location/interaction, Keyboard.type() usage, search box clearing mechanism, search result timing
**Plans**: TBD

### Phase 2: Single Item Withdrawal
**Goal**: Search for item by name, locate it in results, withdraw specified amount
**Depends on**: Phase 1
**Research**: Unlikely (builds on phase 1 + existing BankingUtils patterns)
**Plans**: TBD

### Phase 3: Scroll Fallback
**Goal**: Scroll through bank contents when search doesn't find item (edge case handling)
**Depends on**: Phase 2
**Research**: Likely (discover best scroll method before implementation)
**Research topics**:
- Bank API scroll/pagination methods (check Bank interface for scroll())
- Keyboard arrow keys while bank focused (simplest if works)
- Finger/mouse scroll wheel support (check getFinger() for scroll methods)
- Image-based scroll buttons (fallback - capture scroll_up.png/scroll_down.png in utilities/src/resources/, use SearchableImage + PixelAnalyzer.findSubImages())
- Detecting scroll availability (grayed arrows = end of scroll)
**Discovery approach**: Exhaust API/keyboard methods first; image detection only if simpler methods unavailable
**Plans**: TBD

### Phase 4: Batch Withdrawal
**Goal**: Withdraw a list of items with amounts in sequence, handling partial failures
**Depends on**: Phase 2
**Research**: Unlikely (orchestration of existing withdrawal logic)
**Plans**: TBD

### Phase 5: Fill Inventory
**Goal**: Fill all remaining inventory slots with a searched item
**Depends on**: Phase 2
**Research**: Unlikely (variation of withdrawal with slot counting)
**Plans**: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Core Search Infrastructure | 0/2 | Planned | - |
| 2. Single Item Withdrawal | 0/TBD | Not started | - |
| 3. Scroll Fallback | 0/TBD | Not started | - |
| 4. Batch Withdrawal | 0/TBD | Not started | - |
| 5. Fill Inventory | 0/TBD | Not started | - |
