---
# tidals-scripts-a1qm
title: Add walk + breakCondition for bank loading in GemCutter
status: scrapped
type: task
priority: normal
created_at: 2026-01-28T10:49:03Z
updated_at: 2026-01-28T11:52:22Z
parent: tidals-scripts-6q5p
---

## Context
When bank objects aren't in scene, the script should walk toward the bank area with a breakCondition that exits once the objects load.

## Current Code
`TidalsGemCutter/src/main/java/tasks/Bank.java` lines 120-128:
```java
List<RSObject> banks = script.getObjectManager().getObjects(TidalsGemCutter.bankQuery);
if (banks.isEmpty()) {
    script.log(getClass(), "no bank found");
    return;  // just returns, doesn't walk
}
```

## What to Change
Walk toward bank area with breakCondition:
```java
if (banks.isEmpty()) {
    script.log(getClass(), "bank not in scene, walking closer...");
    WalkConfig config = new WalkConfig.Builder()
        .breakCondition(() -> {
            List<RSObject> found = script.getObjectManager().getObjects(bankQuery);
            return !found.isEmpty();
        })
        .build();
    script.getWalker().walkTo(bankArea.getRandomPosition(), config);
    return; // re-poll after walk
}
```

## Implementation Notes
- Need to define a bank area (RectangleArea or use script.selectedLocation.bankArea() if available)
- If bank has a known center position, use that
- breakCondition should query same bank query used in main check

## Acceptance Criteria
- [x] Script walks toward bank when objects not in scene
- [x] breakCondition stops walk when bank objects load
- [x] Build passes

## Reference
- `osmb_code_review_feedback.md` section "Walker Best Practices"
- `docs/Walker.md`

## Implementation Summary
Added walk+breakCondition pattern to `openBank()` method in Bank.java:
- When bank objects not found, walks a short randomized distance (±3 tiles) from current position
- breakCondition continuously checks if bank objects have loaded using same `bankQuery` predicate
- Logs when breakCondition triggers for debugging
- Returns after walk to re-poll and let task system find the now-loaded bank
