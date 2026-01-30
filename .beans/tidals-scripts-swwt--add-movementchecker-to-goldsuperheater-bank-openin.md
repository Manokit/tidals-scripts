---
# tidals-scripts-swwt
title: Add MovementChecker to GoldSuperheater bank opening
status: completed
type: task
priority: normal
created_at: 2026-01-28T10:49:04Z
updated_at: 2026-01-29T12:50:24Z
parent: tidals-scripts-6q5p
---

## Context
After tapping a bank, if the player misclicks they'll stand still until timeout. MovementChecker detects when player stops moving, allowing early retry.

## Current Code
`TidalsGoldSuperheater/src/main/java/tasks/Bank.java` line 107:
```java
script.pollFramesUntil(() -> script.getWidgetManager().getBank().isVisible(), 
    RandomUtils.weightedRandom(8000, 12000, 0.002));
```

Only checks for bank visibility, not movement.

## What to Change
Add MovementChecker pattern:
```java
WorldPosition startPos = script.getWorldPosition();
MovementChecker movementChecker = new MovementChecker(startPos);

script.pollFramesHuman(() -> {
    if (script.getWidgetManager().getBank().isVisible()) {
        return true;
    }
    WorldPosition current = script.getWorldPosition();
    if (current == null) return false;
    return movementChecker.hasTimedOut(current);
}, RandomUtils.uniformRandom(10000, 15000));
```

If poll exits without bank visible, the tap likely failed → retry.

## Acceptance Criteria
- [ ] MovementChecker imported from utilities
- [ ] Movement timeout detection after bank interact
- [ ] Retry logic if bank didn't open
- [ ] Build passes

## Reference
- `utilities/src/main/java/utilities/MovementChecker.java`
- `osmb_code_review_feedback.md` section "executeReview() Example"