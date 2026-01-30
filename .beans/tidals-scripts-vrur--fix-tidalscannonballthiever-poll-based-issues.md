---
# tidals-scripts-vrur
title: Fix TidalsCannonballThiever poll-based issues
status: completed
type: task
priority: normal
created_at: 2026-01-28T07:09:55Z
updated_at: 2026-01-28T07:27:01Z
parent: tidals-scripts-8cpu
blocking:
    - tidals-scripts-te2i
    - tidals-scripts-iybj
    - tidals-scripts-0haj
---

# Fix TidalsCannonballThiever Issues from Code Review

Apply the 3 specific issues identified plus the openDepositBoxWithMenu fixes.

## Checklist

- [x] **Fix pollFramesUntil usage** - Return `false` for custom delays (currently returns `true` which exits immediately)
- [x] **Break up linear execute()** - Currently does 6+ actions in one poll; should be separate poll-based checks
- [x] **Remove walking back from deposit method** - Keep deposit method focused only on walking TO and depositing
- [x] **Fix openDepositBoxWithMenu()** - Use `tapGameScreen()` instead of `tap()`, add visibility checks

## Implementation Notes

### pollFramesUntil Fix
```java
// BEFORE (exits immediately with no delay)
script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(300, 1000, 0.002));

// AFTER (waits the full timeout)
script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(300, 1000, 0.002));
```

### Poll-Based Structure
```java
public boolean execute() {
    // Check interfaces first
    if (getWidgetManager().getDepositBox().isVisible()) {
        // handle depositing
        return true; // re-poll
    }
    
    // Check if we need to open deposit box
    RSObject depositBox = getObjectManager().getClosestObject("Bank deposit box");
    if (depositBox == null) {
        // walk to it
        return true;
    }
    
    // Check visibility before interacting
    Polygon boxPoly = depositBox.getConvexHull();
    if (boxPoly == null || !depositBox.isInteractableOnScreen()) {
        // walk closer
        return true;
    }
    
    // Interact with visibility-safe method
    getFinger().tapGameScreen(boxPoly);
    // ...
}
```

### Visibility Check Pattern
```java
// Must check visibility, not just null
Polygon boxPoly = depositBox.getConvexHull();
if (boxPoly == null) return false;

// Check visibility factor (hull can be non-null but hidden by UI)
if (getWidgetManager().insideGameScreenFactor(boxPoly, List.of(ChatboxComponent.class)) < 0.3) {
    // not visible enough, walk closer
    return false;
}
```