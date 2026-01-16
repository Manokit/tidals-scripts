# Loadout Maker Utility

## What This Is

A comprehensive loadout management utility for OSMB combat and bossing scripts. Provides a visual editor to define equipment + inventory baselines, imports RuneLite Inventory Setups JSON, and handles intelligent restocking at banks — only withdrawing what's missing from the defined loadout.

## Core Value

Reliable loadout restocking — scripts define a baseline gear/inventory configuration, and the utility accurately determines what's missing and withdraws only those items, handling degradables, potions, and quantity requirements correctly.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Visual JavaFX loadout editor (equipment left, inventory right, clickable slots)
- [ ] Item search with autocomplete and sprites (OSMB ItemManager, Wiki API fallback)
- [ ] RuneLite Inventory Setups JSON import
- [ ] Custom JSON export format for sharing loadouts
- [ ] All containers: equipment (14), inventory (28), rune pouch (4), bolt pouch (4), quiver (1)
- [ ] Three quantity modes per item: exact, unlimited (*), minimum threshold (>)
- [ ] Fuzzy matching flag (user-configurable) for potions/jewelry dose/charge variants
- [ ] Automatic degradable matching for Barrows/Crystal/etc (prefer highest charge)
- [ ] Loadout persistence via Java Preferences
- [ ] Popup window launcher (button in ScriptUI opens editor)
- [ ] Restock logic: compare current state to baseline, withdraw missing items
- [ ] Loadout object API for scripts to pass to banking tasks
- [ ] TidalsLoadoutTester validation script

### Out of Scope

- Gear switching / multiple loadout profiles per trip — v1 is single loadout
- Bank organization / layout array management — we ignore the RuneLite layout field
- Preset saving to disk files — preferences + export covers sharing needs

## Context

**Existing codebase:**
- TidalsBankTester exists as proof-of-concept for BankSearchUtils
- BankingUtils, BankSearchUtils, RetryUtils already in utilities jar
- ScriptUI patterns established in existing scripts (TidalsGemCutter, etc.)

**RuneLite format:**
- Import format documented in TidalsBankTester/EXPORT_FORMAT.md
- Uses short field names (inv, eq, rp, bp, qv, etc.)
- fuzzy flag (`f`) on items for potion/jewelry matching
- Quantities default to 1 when omitted

**Item sprites:**
- OSMB ItemManager has gaps — some items/sprites missing
- Wiki API can provide fallback sprites and ID mapping
- Must always maintain item ID for BankUtils compatibility

**Degradable items:**
- Barrows gear: AHRIMS_ROBETOP_100, _75, _50, _25
- Crystal equipment, Serpentine helm, etc.
- Default behavior: match any charge variant, prefer highest when withdrawing

**Quantity modes:**
- Exact: restock to exactly N (e.g., 10k coins for ship fee)
- Unlimited (*): take entire stack from bank (runes, bolts, arrows)
- Minimum (>): must have at least N, fail restock check if not (e.g., >50 ruby bolts)

## Constraints

- **Tech stack**: Java, JavaFX for UI, must integrate with TidalsUtilities.jar
- **Compatibility**: Must work with existing OSMB Script architecture and patterns

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Popup window vs embedded panel | Reusable across scripts, cleaner separation | — Pending |
| RuneLite import, custom export | Import existing setups easily, export our extended format | — Pending |
| Automatic degradable matching | User shouldn't need to specify all charge variants | — Pending |
| Three quantity modes | Covers exact amounts, full stacks, and minimum thresholds | — Pending |
| OSMB sprites + Wiki fallback | Best coverage with graceful degradation | — Pending |

---
*Last updated: 2025-01-15 after initialization*
