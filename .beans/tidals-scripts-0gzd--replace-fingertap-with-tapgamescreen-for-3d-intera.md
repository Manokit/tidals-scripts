---
# tidals-scripts-0gzd
title: Replace Finger.tap with tapGameScreen for 3D interactions
status: completed
type: task
priority: normal
created_at: 2026-01-28T10:03:58Z
updated_at: 2026-01-28T10:08:57Z
parent: tidals-scripts-stot
---

Use `tapGameScreen()` instead of `Finger.tap()` for 3D game screen interactions (objects, NPCs, tiles).

## Why This Matters
- `Finger.tap(shape)` can click through UI overlays if the shape overlaps both game screen and UI
- `Finger.tapGameScreen(shape)` ensures clicks only land on the visible game screen portion
- Critical when convex hulls may be partially obscured by inventory, chatbox, etc.

## Checklist

### Cannonball Thiever
- [x] `TidalsCannonballThiever/src/main/java/tasks/StartThieving.java:180`
- [x] `TidalsCannonballThiever/src/main/java/tasks/EscapeJail.java:203`
- [x] `TidalsCannonballThiever/src/main/java/tasks/SwitchToCannonballStall.java:86`
- [x] `TidalsCannonballThiever/src/main/java/tasks/SwitchToOreStall.java:93`
- [x] `TidalsCannonballThiever/src/main/java/tasks/PrepareForBreak.java:144`
- [x] `TidalsCannonballThiever/src/main/java/tasks/Retreat.java:44`
- [x] `TidalsCannonballThiever/src/main/java/tasks/ReturnToThieving.java:118`

### Gem Miner
- [x] `TidalsGemMiner/src/main/java/tasks/Mine.java:375-384` - line 383 uses Point (kept as tap), line 388 already used tapGameScreen

### Chompy Hunter
- [x] `TidalsChompyHunter/src/main/java/tasks/InflateToads.java:249` - uses Point, kept as tap (tapGameScreen only accepts Shape)
- [x] `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java:1043`
- [x] `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java:1103`
- [x] `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java:1785`
- [x] `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java:1806`