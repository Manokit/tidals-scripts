---
# tidals-scripts-tn78
title: Add walk + breakCondition for bank loading in GoldSuperheater
status: scrapped
type: task
priority: normal
created_at: 2026-01-28T10:49:04Z
updated_at: 2026-01-28T11:52:36Z
parent: tidals-scripts-6q5p
---

## Context
When bank objects aren't in scene, the script should walk toward the bank area with a breakCondition.

## Current Code
`TidalsGoldSuperheater/src/main/java/tasks/Bank.java` lines 92-99:
```java
List<RSObject> banks = script.getObjectManager().getObjects(TidalsGoldSuperheater.bankQuery);
if (banks.isEmpty()) {
    script.log(getClass(), "[openBank] no bank found");
    return;  // just returns, doesn't walk
}
```

## What to Change
Same pattern as GemCutter:
```java
if (banks.isEmpty()) {
    script.log(getClass(), "[openBank] bank not in scene, walking closer...");
    WalkConfig config = new WalkConfig.Builder()
        .breakCondition(() -> {
            List<RSObject> found = script.getObjectManager().getObjects(bankQuery);
            return !found.isEmpty();
        })
        .build();
    script.getWalker().walkTo(bankArea.getRandomPosition(), config);
    return;
}
```

## Acceptance Criteria
- [ ] Script walks toward bank when objects not in scene
- [ ] breakCondition stops walk when bank objects load
- [ ] Build passes

## Reference
- `osmb_code_review_feedback.md` section "Walker Best Practices"