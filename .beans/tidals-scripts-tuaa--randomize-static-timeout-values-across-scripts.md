---
# tidals-scripts-tuaa
title: Randomize static timeout values across scripts
status: completed
type: task
priority: normal
created_at: 2026-01-28T10:03:59Z
updated_at: 2026-01-28T10:14:03Z
parent: tidals-scripts-stot
---

Replace hardcoded timeout values with randomized ones to avoid detectable patterns.

## Why This Matters
Static timeouts (e.g., `2000`, `2500`) create predictable timing patterns. Each timeout should be randomized on every use with `RandomUtils.weightedRandom()` or `RandomUtils.gaussianRandom()`.

## Checklist

### Cannonball Thiever
- [x] `TidalsCannonballThiever/src/main/java/tasks/DepositOres.java:100` - `3000` → `RandomUtils.weightedRandom(2500, 4000, 0.002)`
- [x] `TidalsCannonballThiever/src/main/java/tasks/DepositOres.java:116` - `8000` → `RandomUtils.weightedRandom(6000, 10000, 0.002)`
- [x] `TidalsCannonballThiever/src/main/java/tasks/DepositOres.java:128` - `5000` → `RandomUtils.weightedRandom(4000, 6000, 0.002)`
- [x] `TidalsCannonballThiever/src/main/java/tasks/WaitAtSafety.java:30` - `10000` → `RandomUtils.weightedRandom(8000, 12000, 0.002)`

### Gem Miner
- [x] `TidalsGemMiner/src/main/java/tasks/Mine.java:788` - `4_000` → `RandomUtils.weightedRandom(3500, 5000, 0.002)`

### Gem Cutter
- [x] `TidalsGemCutter/src/main/java/tasks/Bank.java:127` - `5000` → `RandomUtils.weightedRandom(4000, 6000, 0.002)`
- [x] `TidalsGemCutter/src/main/java/tasks/Bank.java:159-160` - `2000`/`15000` → randomized

### Gold Superheater
- [x] `TidalsGoldSuperheater/src/main/java/tasks/Process.java:39` - `3000` → `RandomUtils.weightedRandom(2500, 4000, 0.002)`
- [x] `TidalsGoldSuperheater/src/main/java/tasks/Process.java:50` - `2000` → `RandomUtils.weightedRandom(1500, 2500, 0.002)`
- [x] `TidalsGoldSuperheater/src/main/java/tasks/Bank.java:85` - `3000` → `RandomUtils.weightedRandom(2500, 4000, 0.002)`
- [x] `TidalsGoldSuperheater/src/main/java/tasks/Bank.java:104` - `10000` → `RandomUtils.weightedRandom(8000, 12000, 0.002)`

### Chompy Hunter
- [x] `TidalsChompyHunter/src/main/java/tasks/DropToads.java:261` - `.timeout(8000)` → randomized
- [x] `TidalsChompyHunter/src/main/java/tasks/FillBellows.java:175` - `.timeout(10000)` → randomized (both occurrences)
- [x] `TidalsChompyHunter/src/main/java/tasks/FillBellows.java:194` - `30000` → `RandomUtils.weightedRandom(25000, 35000, 0.002)`
- [x] `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java:327` - `.timeout(5000)` → randomized

## Out of Scope (separate beans needed)
The following items were in the original checklist but are architectural refactoring, not timeout randomization:
- Remove color-detection fallback: `TidalsGemMiner/src/main/java/tasks/Mine.java:226-239`
- Split bank task into poll-based states: `TidalsGemCutter/src/main/java/tasks/Bank.java:78-128`