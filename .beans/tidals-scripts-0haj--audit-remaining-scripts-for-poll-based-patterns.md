---
# tidals-scripts-0haj
title: Audit remaining scripts for poll-based patterns
status: completed
type: task
priority: normal
created_at: 2026-01-28T07:11:18Z
updated_at: 2026-01-28T07:31:54Z
parent: tidals-scripts-8cpu
blocking:
    - tidals-scripts-te2i
    - tidals-scripts-iybj
---

# Audit All Other Scripts for Poll-Based Patterns

After fixing GemMiner and CannonballThiever, audit ALL other Tidals scripts for the same issues.

## Scripts to Audit

Check each script for:
1. Linear execution in single poll (should be check-handle-return)
2. Using `tap()` instead of `tapGameScreen()` for 3D objects
3. Static timeout values (should be randomized)
4. `pollFramesUntil(() -> true, ...)` for delays (should return false)
5. Missing visibility checks before interaction
6. Walker without break conditions

## Checklist

- [x] TidalsChompyHunter - CLEAN (already uses correct patterns)
- [x] TidalsGemCutter - CLEAN (already uses correct patterns)
- [x] TidalsFungusWoodcutter - N/A (script does not exist in repo)
- [x] TidalsZMIAltar - N/A (script does not exist in repo)
- [x] TidalsGoldSuperheater - CLEAN (already uses correct patterns)
- [x] TidalsGemMiner - FIXED 7 instances of `pollFramesUntil(() -> true, ...)`
- [x] TidalsCannonballThiever - FIXED 12 instances of `pollFramesUntil(() -> true, ...)`

## Audit Results

### Scripts Already Following Correct Patterns
- **TidalsChompyHunter**: Uses `RandomUtils.weightedRandom()`, `gaussianRandom()`, `pollFramesUntil(() -> false, ms)`, `pollFramesHuman(() -> true, ms)` correctly throughout
- **TidalsGemCutter**: Uses correct patterns for all delays and interactions
- **TidalsGoldSuperheater**: Uses correct patterns for all delays and interactions

### Issues Fixed

#### TidalsGemMiner (7 fixes)
- Bank.java: Fixed 3 instances of `pollFramesUntil(() -> true, ...)`
- Cut.java: Fixed 4 instances of `pollFramesUntil(() -> true, ...)`

#### TidalsCannonballThiever (12 fixes)
- StartThieving.java: Fixed 4 instances of `pollFramesUntil(() -> true, ...)`
- EscapeJail.java: Fixed 8 instances of `pollFramesUntil(() -> true, ...)`
- DepositOres.java: Already used correct pattern (no fixes needed)
- MonitorThieving.java: No delays used (uses pollFramesUntil with actual conditions)
- Retreat.java: No delays used

## Notes

All production scripts have been audited and corrected. The pattern `pollFramesUntil(() -> true, delay)` was incorrectly used in place of `pollFramesUntil(() -> false, delay)` for fixed delays - this caused the delays to exit immediately instead of waiting.
