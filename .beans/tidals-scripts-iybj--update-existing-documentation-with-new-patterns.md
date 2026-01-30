---
# tidals-scripts-iybj
title: Update existing documentation with new patterns
status: completed
type: task
priority: normal
created_at: 2026-01-28T07:09:57Z
updated_at: 2026-01-28T07:48:30Z
parent: tidals-scripts-8cpu
blocking:
    - tidals-scripts-3f9f
---

# Update Existing Documentation

Integrate the new patterns into existing documentation files.

## Checklist

- [x] Update `docs/Walker.md` with break condition patterns
  - Object-loading break condition
  - Visibility-based break condition
  - Example code for both patterns
- [x] Update `docs/common-mistakes.md` with new anti-patterns
  - Using tap() instead of tapGameScreen() for 3D objects
  - Linear execution in single poll
  - Static timeout values
  - pollFramesUntil(() -> true) for delays (already covered in section 7)
  - Checking hull non-null without visibility check
- [x] Update `docs/banking-patterns.md`
  - Add MovementChecker pattern
  - Add proper deposit box interaction flow

## Walker Break Condition Examples

### Wait for Objects to Load
```java
WalkConfig.Builder walkConfig = new WalkConfig.Builder();
walkConfig.breakCondition(() -> {
    RSObject bank = getObjectManager().getRSObject(BANK_QUERY);
    return bank != null;
});
getWalker().walkTo(bankArea.getRandomPosition(), walkConfig.build());
```

### Wait for Hull Visibility
```java
walkConfig.breakCondition(() -> {
    Polygon bankPoly = bank.getConvexHull();
    if (bankPoly == null) return false;
    return getWidgetManager().insideGameScreenFactor(bankPoly, List.of(ChatboxComponent.class)) >= 0.3;
});
```