---
# tidals-scripts-qev1
title: Add walker breakCondition for deposit box visibility
status: completed
type: task
priority: normal
created_at: 2026-01-28T11:19:29Z
updated_at: 2026-01-29T13:03:37Z
parent: tidals-scripts-6q5p
---

## Context
The feedback section "Walking Until Hull is Visible (Custom Interaction)" shows using `insideGameScreenFactor()` in breakCondition to stop walking once the target object hull is sufficiently visible.

## Current Code
`TidalsCannonballThiever/src/main/java/tasks/DepositOres.java` line 115:
```java
script.getWalker().walkTo(DEPOSIT_BOX_TILE, exactTileConfig);
```

The exactTileConfig doesn't have a breakCondition. Walking continues until reaching the exact tile, even if the deposit box is already interactable.

## What to Change
Add breakCondition that checks hull visibility:
```java
WalkConfig config = new WalkConfig.Builder()
    .disableWalkScreen(true)
    .breakDistance(0)
    .breakCondition(() -> {
        RSObject depositBox = script.getObjectManager().getClosestObject("Bank deposit box");
        if (depositBox == null) return false;
        
        Polygon boxPoly = depositBox.getConvexHull();
        if (boxPoly == null) return false;
        
        // stop when hull is >30% visible (not hidden by UI)
        double visibility = script.getWidgetManager().insideGameScreenFactor(
            boxPoly, List.of(ChatboxComponent.class));
        return visibility >= 0.3;
    })
    .build();
```

## Benefits
- Faster interaction (don't walk all the way if already visible)
- More natural behavior (humans stop when they can see the target)
- Reduces unnecessary movement

## Acceptance Criteria
- [ ] breakCondition added to deposit box walk config
- [ ] Uses insideGameScreenFactor for visibility check
- [ ] Build passes

## Reference
- `osmb_code_review_feedback.md` section "Walking Until Hull is Visible"
- `docs/Walker.md`