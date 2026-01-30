---
# tidals-scripts-q0p8
title: Use tapGameScreen for GemMiner mining clicks
status: completed
type: task
priority: normal
created_at: 2026-01-28T10:48:02Z
updated_at: 2026-01-28T11:55:18Z
parent: tidals-scripts-6q5p
---

## Context
The feedback says: "Do NOT use Finger::tap for 3D game screen interactions. MUST use Finger::tapGameScreen."

## Current Code
`TidalsGemMiner/src/main/java/tasks/Mine.java` line 383:
```java
return script.getFinger().tap(clickPoint);
```

This is inside `tapTarget()` for tile-based targets. Even if tile-based detection is removed (see sibling task), we should ensure all rock clicks use `tapGameScreen()`.

## What to Change
1. Use `tapGameScreen()` for all rock interactions
2. Add `insideGameScreenFactor()` visibility check before tapping
3. If visibility too low, walk closer first

## Note
If the sibling task "Remove tile-based color detection" is completed first, this task may be simplified or obsolete. Check Mine.java state before starting.

## Reference
- `docs/interaction-patterns.md`
- `osmb_code_review_feedback.md` section "Using wrong tap method"