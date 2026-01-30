---
# tidals-scripts-g9gw
title: Remove tile-based color detection fallback in GemMiner
status: completed
type: task
priority: normal
created_at: 2026-01-28T10:48:00Z
updated_at: 2026-01-29T13:10:41Z
parent: tidals-scripts-6q5p
---

## Context
The original OSMB feedback stated: "Object manager will always return the objects if you are in the mine, so this fallback is unnecessary."

## Current Code
`TidalsGemMiner/src/main/java/tasks/Mine.java` lines 226-289:
- `findBestTarget()` tries tile-based targeting first via `findTileBasedTarget()`
- Falls back to `findObjectManagerTarget()` if tile method fails
- The tile-based path uses pixel color detection which is more fragile

## What to Change
1. Remove `findTileBasedTarget()` method entirely
2. Remove the tile-based color detection constants (lines 48-61)
3. Remove `UNDERGROUND_ROCK_POSITIONS` hardcoded set (lines 63-83)
4. Simplify `findBestTarget()` to just call `findObjectManagerTarget()`
5. Remove or simplify `tapTarget()` since it won't need tile-specific logic

## Acceptance Criteria
- [ ] ObjectManager-only rock detection
- [ ] No pixel color matching for rocks
- [ ] Code is simpler and easier to maintain
- [ ] Build passes

## Reference
- `osmb_code_review_feedback.md` section "Color Detection Fallback is Pointless"