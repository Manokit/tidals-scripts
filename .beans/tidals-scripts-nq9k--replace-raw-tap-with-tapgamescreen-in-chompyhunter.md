---
# tidals-scripts-nq9k
title: Replace raw tap() with tapGameScreen() in ChompyHunter
status: completed
type: task
priority: normal
created_at: 2026-01-28T10:48:41Z
updated_at: 2026-01-28T11:37:15Z
parent: tidals-scripts-6q5p
---

## Context
"Do NOT use Finger::tap for 3D game screen interactions. MUST use Finger::tapGameScreen."

## Current Code

### FillBellows.java line 137:
```java
boolean sucked = RetryUtils.tap(script, tilePoly, "Suck", "suck swamp bubble", ...);
```
Uses RetryUtils.tap() which wraps Finger.tap().

### InflateToads.java line 249:
```java
boolean success = script.getFinger().tap(clickPoint, "Inflate");
```
Direct raw tap() call on Point.

## What to Change

### Option A: Update RetryUtils
Add a `tapGameScreen()` variant to RetryUtils that uses `Finger.tapGameScreen()`.

### Option B: Direct replacement
1. FillBellows: Check `insideGameScreenFactor()` before tap, use `tapGameScreen(tilePoly)`
2. InflateToads: Add visibility check, use `tapGameScreen()` with Shape instead of Point

## Notes
- For InflateToads, tapping exact Point is intentional for small sprite targets
- May need to create small Polygon around click point for tapGameScreen()
- Add visibility validation before tapping

## Acceptance Criteria
- [x] No raw tap() calls for 3D game world interactions
- [x] Visibility checks before tapping (existing: tilePoly null check, isNearScreenEdge)
- [x] Build passes

## Reference
- `docs/interaction-patterns.md`
- `osmb_code_review_feedback.md` section "Using wrong tap method"