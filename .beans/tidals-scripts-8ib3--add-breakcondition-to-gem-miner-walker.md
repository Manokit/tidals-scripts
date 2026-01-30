---
# tidals-scripts-8ib3
title: Add breakCondition to Gem Miner walker
status: completed
type: feature
priority: normal
created_at: 2026-01-28T07:54:20Z
updated_at: 2026-01-28T08:35:49Z
---

## Problem
Per OSMB feedback, the walker should stop early when the target object becomes visible, rather than walking all the way to a fixed position.

## OSMB's Recommended Patterns

### Simple approach (when using RSObject::interact or RetryUtils):
```java
// Just check if object exists - let interaction handler deal with clicking
walkConfig.breakCondition(() -> {
    RSObject bank = getObjectManager().getClosestObject(myPos, "Bank chest");
    return bank != null;
});
```

### Hull visibility approach (when manually tapping):
```java
walkConfig.breakCondition(() -> {
    Polygon bankPoly = bank.getConvexHull();
    if (bankPoly == null) return false;
    return getWidgetManager().insideGameScreenFactor(bankPoly, List.of(ChatboxComponent.class)) >= 0.3;
});
```

## Implementation Notes

**Key lessons learned:**
1. Check UI state FIRST - if deposit box UI is open, the 3D object behind it is blocked
2. Simple `object != null` check is sufficient when using RetryUtils
3. Removed approach area concept - visibility check handles "close enough"

## Checklist
- [x] Add breakCondition to WalkConfig in Bank.java
- [x] Check UI state first to prevent walk spam when UI open
- [x] Simplify to object existence check per OSMB recommendation
- [x] Test underground mine - works correctly
- [x] Document pattern in docs/banking-patterns.md

## Files
- TidalsGemMiner/src/main/java/tasks/Bank.java
- docs/banking-patterns.md