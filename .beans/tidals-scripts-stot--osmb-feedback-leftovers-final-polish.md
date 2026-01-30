---
# tidals-scripts-stot
title: 'OSMB Feedback Leftovers: Final Polish'
status: completed
type: epic
priority: normal
created_at: 2026-01-28T09:55:52Z
updated_at: 2026-01-28T10:14:25Z
---

Remaining action items from the OSMB code review that weren't addressed in the initial feedback epic. These are the final polish items to bring all scripts up to the recommended patterns.

## Overview

Three categories of fixes across 5 scripts:
1. **pollFramesUntil fixes** - Must return `false` for custom delays (not `true`)
2. **tapGameScreen replacements** - Use `tapGameScreen()` instead of `Finger.tap()` for 3D game screen interactions
3. **Timeout randomization** - Replace static timeout values with randomized ones

## Reference Documentation
- `docs/poll-based-architecture.md` - Correct pollFramesUntil usage
- `docs/interaction-patterns.md` - tapGameScreen vs tap
- `osmb_code_review_feedback.md` - Original feedback details

---

## Checklist

### Cannonball Thiever
- [ ] Fix pollFramesUntil custom-delay usage (must return false):
  - `TidalsCannonballThiever/src/main/java/tasks/Setup.java:94`
  - `TidalsCannonballThiever/src/main/java/tasks/Setup.java:117`
  - `TidalsCannonballThiever/src/main/java/tasks/ReturnToThieving.java:83`
  - `TidalsCannonballThiever/src/main/java/tasks/ReturnToThieving.java:99`
  - `TidalsCannonballThiever/src/main/java/tasks/WaitAtSafety.java:33`
- [ ] Replace 3D Finger.tap with tapGameScreen:
  - `TidalsCannonballThiever/src/main/java/tasks/StartThieving.java:180`
  - `TidalsCannonballThiever/src/main/java/tasks/EscapeJail.java:203`
  - `TidalsCannonballThiever/src/main/java/tasks/SwitchToCannonballStall.java:86`
  - `TidalsCannonballThiever/src/main/java/tasks/SwitchToOreStall.java:93`
  - `TidalsCannonballThiever/src/main/java/tasks/PrepareForBreak.java:144`
  - `TidalsCannonballThiever/src/main/java/tasks/Retreat.java:44`
  - `TidalsCannonballThiever/src/main/java/tasks/ReturnToThieving.java:118`
- [ ] Randomize static timeouts:
  - `TidalsCannonballThiever/src/main/java/tasks/DepositOres.java:100`
  - `TidalsCannonballThiever/src/main/java/tasks/DepositOres.java:116`
  - `TidalsCannonballThiever/src/main/java/tasks/DepositOres.java:128`
  - `TidalsCannonballThiever/src/main/java/tasks/WaitAtSafety.java:30`

### Gem Miner
- [ ] Remove color-detection fallback (tile-based targeting before ObjectManager):
  - `TidalsGemMiner/src/main/java/tasks/Mine.java:226-236`
  - `TidalsGemMiner/src/main/java/tasks/Mine.java:239`
- [ ] Replace 3D Finger.tap with tapGameScreen:
  - `TidalsGemMiner/src/main/java/tasks/Mine.java:375-384`
- [ ] Randomize static timeouts:
  - `TidalsGemMiner/src/main/java/tasks/Mine.java:785-788`

### Gem Cutter
- [ ] Split bank task into poll-based states (multiple actions in single poll):
  - `TidalsGemCutter/src/main/java/tasks/Bank.java:78-128`
- [ ] Randomize static timeouts in bank flow:
  - `TidalsGemCutter/src/main/java/tasks/Bank.java:126-127`
  - `TidalsGemCutter/src/main/java/tasks/Bank.java:159-160`

### Gold Superheater
- [ ] Fix pollFramesUntil custom-delay usage (must return false):
  - `TidalsGoldSuperheater/src/main/java/tasks/Setup.java:56`
- [ ] Randomize static timeouts:
  - `TidalsGoldSuperheater/src/main/java/tasks/Process.java:39-50`
  - `TidalsGoldSuperheater/src/main/java/tasks/Bank.java:85`
  - `TidalsGoldSuperheater/src/main/java/tasks/Bank.java:104`

### Chompy Hunter
- [ ] Replace 3D Finger.tap with tapGameScreen:
  - `TidalsChompyHunter/src/main/java/tasks/InflateToads.java:249`
  - `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java:1043`
  - `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java:1103`
  - `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java:1785`
  - `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java:1806`
- [ ] Randomize static timeouts:
  - `TidalsChompyHunter/src/main/java/tasks/DropToads.java:261`
  - `TidalsChompyHunter/src/main/java/tasks/FillBellows.java:175`
  - `TidalsChompyHunter/src/main/java/tasks/FillBellows.java:194`
  - `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java:327`