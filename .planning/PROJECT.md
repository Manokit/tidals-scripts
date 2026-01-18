# TidalsGemMiner

## What This Is

An OSMB automation script for mining gems at Shilo Village gem mine with optional in-place gem cutting. Supports both upper (surface) and underground mining locations, cuts mined gems to maximize value and prevent banking low-value raw gems, drops crushed gems, and deposits only the cut products.

## Core Value

Mine gems efficiently with optional cutting to turn raw gems into cut gems on-site, dropping failures rather than wasting bank trips on crushed gems.

## Requirements

### Validated

- Task-based state machine architecture — v1.0
- Shared utilities (TidalsUtilities.jar) for retry logic, banking, tabs — v1.0
- Paint overlay system with logo support — v1.0
- Stats reporting to dashboard every 10 minutes — v1.0
- ScriptUI for configuration — v1.0
- Mine gem rocks at Shilo Village (upper or underground, user choice) — v1.0
- Support both mining locations with user selection in ScriptUI — v1.0
- Optional "Cut gems and drop crushed gems" toggle in ScriptUI — v1.0
- When cutting enabled: require chisel in inventory — v1.0
- Cut all cuttable gems based on user's crafting level — v1.0
- Drop crushed gems (id: 1633) after cutting — v1.0
- Bank cut gems only (when cutting enabled) or raw gems (when disabled) — v1.0
- Track and display: Mining XP/hr, Crafting XP/hr, gems mined, gems cut — v1.0
- Report stats to dashboard (mining XP, crafting XP, gems mined, gems cut, runtime) — v1.0
- Use "Tidals Gem Miner.png" logo in paint overlay — v1.0
- Failsafe: walk to mine area if lost or can't find rocks — v1.0
- Failsafe: wait and retry for rock respawns — v1.0
- Failsafe: timeout and stop if stuck too long — v1.0
- Fix missing return statements from example code that cause stuck states — v1.0

### Active

(None - all v1 requirements shipped)

### Out of Scope

- World hopping — mine is rarely crowded enough to warrant it
- Dragonstone special handling — too rare, just treat like other gems
- Banking uncut gems when cutting is enabled — defeats the purpose
- Multiple chisel support — just need one, it doesn't break

## Context

**Current State:**
Shipped v1.0 with 1,500 LOC Java.
Tech stack: Java 17, OSMB API, TidalsUtilities.jar, JavaFX for ScriptUI.
All 21 requirements satisfied, all 4 phases complete.

**Gem Cutting Specifics:**
- Dialogue shows UNCUT gem, not cut gem (must select uncut ID in dialogue)
- Crushed gem ID: 1633 (all gem types crush to this)
- Cuts highest-level gem first for XP efficiency

**Crafting Level Requirements:**
| Gem | Level |
|-----|-------|
| Opal | 1 |
| Jade | 13 |
| Red topaz | 16 |
| Sapphire | 20 |
| Emerald | 27 |
| Diamond | 43 |
| Dragonstone | 55 |
| Ruby | 63 |

## Constraints

- **Tech stack**: Java 17, OSMB API, TidalsUtilities.jar — must match existing script patterns
- **Architecture**: Task-based state machine pattern — must follow established architecture
- **Visual detection**: OSMB is a color bot — cannot access game memory, must use screen analysis
- **Stats format**: Dashboard expects incremental values — match TidalsCannonballThiever pattern

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Cut when inventory full, not continuously | Simpler logic, fewer chisel uses, matches expected rhythm | ✓ Good |
| Single toggle for cutting behavior | Keeps UI simple, chisel requirement follows naturally | ✓ Good |
| Both locations via user choice | Different players prefer different spots, minimal extra code | ✓ Good |
| Use existing TidalsUtilities for retry logic | Code reuse, proven patterns, consistency across scripts | ✓ Good |
| Used record type for MiningLocation | Cleaner API with auto-generated accessors, immutability | ✓ Good |
| Upper mine hops worlds, underground waits | Upper has limited rocks, underground has many | ✓ Good |
| 5-minute stuck timeout | Long enough for temporary issues, catches real stuck states | ✓ Good |
| Cut highest-level gem first | XP efficiency by prioritizing ruby over opal | ✓ Good |
| Task order: Setup -> Cut -> Bank -> Mine | Ensures gems are cut before banking when enabled | ✓ Good |
| XPTracker API: getXpGained() not getXPGained() | Actual OSMB API uses lowercase 'p' | ✓ Good |
| Combined XP for dashboard total | totalXp = miningXp + craftingXp for aggregate display | ✓ Good |

---
*Last updated: 2026-01-16 after v1.0 milestone*
