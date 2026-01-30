---
# tidals-scripts-2bfq
title: Add MovementChecker to CannonballThiever deposit box opening
status: completed
type: task
priority: normal
created_at: 2026-01-28T10:48:03Z
updated_at: 2026-01-29T12:09:33Z
parent: tidals-scripts-6q5p
---

## Context
The feedback says: "definitely add a movement timeout here also in case you get interrupted while walking or misclick so you aren't stood until the timeout"

## Current Code
`TidalsCannonballThiever/src/main/java/tasks/DepositOres.java` line 128:
```java
script.pollFramesUntil(() -> isDepositInterfaceOpen(), RandomUtils.weightedRandom(4000, 6000, 0.002));
```

This only waits for interface visibility. If the player misclicks or gets interrupted, they'll stand still for 4-6 seconds before retrying.

## What to Change
1. Capture initial position before tapping deposit box
2. Use MovementChecker in the poll condition
3. If player stopped moving without interface open → retry tap

Example pattern from feedback:
```java
MovementChecker movementChecker = new MovementChecker(worldPosition);
return pollFramesHuman(() -> {
    if(getWidgetManager().getDepositBox().isVisible()) {
        return true;
    }
    WorldPosition currentWorldPosition = script.getWorldPosition();
    if (currentWorldPosition == null) {
        return false;
    }
    return movementChecker.hasTimedOut(currentWorldPosition);
}, RandomUtils.uniformRandom(10000, 15000));
```

## Acceptance Criteria
- [ ] MovementChecker used after deposit box tap
- [ ] Early exit on movement timeout (misclick detection)
- [ ] Build passes

## Reference
- `osmb_code_review_feedback.md` section "executeReview() Example"
- `utilities/src/main/java/utilities/MovementChecker.java`