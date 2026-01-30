---
# tidals-scripts-8cpu
title: OSMB-Feedback
status: completed
type: epic
priority: normal
created_at: 2026-01-28T07:09:08Z
updated_at: 2026-01-28T08:36:45Z
---

# OSMB Code Review Feedback Implementation

Major architectural overhaul based on code review feedback that fundamentally changes how scripts should be written.

## Core Philosophy

**Don't do many consecutive things in a single poll.** Make scripts more dynamic by performing minimal actions per poll.

### The Shift: Linear → Poll-Based

**Before (Anti-Pattern):**
```java
// All in one poll - BAD
handleDialogues();
walkToDepositBox();
openDepositBox();
depositItems();
closeDepositBox();
walkBack();
```

**After (Correct Pattern):**
```java
if (dialogueVisible) {
    handleIt();
    return true; // re-poll
}

if (depositInterfaceOpen) {
    handleIt();
    return true; // re-poll
}
// etc.
```

---

## Key Changes Required

### 1. Interaction Method Changes
- **MUST use `tapGameScreen()`** for 3D game screen interactions, NOT `tap()`
- `tap()` can click on UI overlays; `tapGameScreen()` restricts to visible game area
- Check visibility with `insideGameScreenFactor()` before interacting

### 2. Visibility Validation
- `getConvexHull()` being non-null does NOT mean object is visible
- Hull projects 3D→2D even when overlapped by UI
- Use `WidgetManager.insideGameScreenFactor(shape, excludeList)` to verify
- Recommended threshold: 0.3+ visibility factor

### 3. Timeout Randomization
- ALL timeout values must be randomized
- Re-randomize on each use (no static timeout values)
- Prevents detectable patterns

### 4. pollFramesUntil Behavior
- Returns immediately when condition is `true` with NO delay
- For custom delays, return `false` (waits full timeout)
- `pollFramesHuman` adds humanized variance after completion

### 5. Walker Break Conditions
- Use break conditions to stop walking when objects load
- Check for objects in scene, not just destination reached
- Check for hull visibility when doing custom interactions

### 6. Movement Timeout Pattern
- Track position before interaction
- Poll for interface OR movement timeout
- Prevents getting stuck on misclicks

---

## Checklist

### Phase 1: Fix Existing Scripts
- [ ] Audit TidalsGemMiner for feedback issues
  - [ ] Remove color detection fallback (ObjectManager always returns objects in mine)
  - [ ] Move area validation BEFORE rock search
  - [ ] Randomize SWING_PICK_TIMEOUT_MS on each use
  - [ ] Randomize the 2000ms static timeout in deposit polling
- [ ] Audit TidalsCannonballThiever for feedback issues
  - [ ] Fix pollFramesUntil usage (return false for delays)
  - [ ] Break up linear execute() into poll-based checks
  - [ ] Remove "walk back to stall" from deposit method
  - [ ] Fix openDepositBoxWithMenu() to use tapGameScreen + visibility checks

### Phase 2: Update Documentation
- [ ] Create `docs/poll-based-architecture.md` - core polling philosophy
- [ ] Create `docs/interaction-patterns.md` - tap vs tapGameScreen, visibility checks
- [ ] Update `docs/Walker.md` with break condition patterns
- [ ] Update `docs/common-mistakes.md` with new anti-patterns
- [ ] Update `docs/banking-patterns.md` with MovementChecker pattern

### Phase 3: Create Reference Patterns
- [ ] Add MovementChecker utility class to TidalsUtilities
- [ ] Create example poll-based deposit flow
- [ ] Create example walker with object-loading break condition
- [ ] Create example walker with visibility break condition

### Phase 4: Update CLAUDE.md
- [ ] Add poll-based architecture to core principles
- [ ] Document tapGameScreen vs tap distinction
- [ ] Add visibility checking requirements
- [ ] Reference new documentation files

---

## Reference: Key API Methods

### Visibility Checking
```java
// Check if shape is inside game screen (excluding UI components)
WidgetManager.insideGameScreen(Shape, List<Class>)

// Get visibility factor (0.0 = hidden, 1.0 = fully visible)
WidgetManager.insideGameScreenFactor(Shape, List<Class>)

// Basic check (only 0.2 factor - often not enough)
RSObject.isInteractableOnScreen()
```

### Game Screen Interaction
```java
// CORRECT - restricts tap to game screen area
Finger.tapGameScreen(boolean rightClick, Shape shape)

// WRONG for 3D objects - can click on UI overlays
Finger.tap(Shape shape, String action)
```

### Movement Tracking
```java
public static class MovementChecker {
    private final long timeout;
    private WorldPosition initialPosition;
    private long lastMovementTime;

    public MovementChecker(WorldPosition initialPosition) {
        this.initialPosition = initialPosition;
        this.timeout = RandomUtils.uniformRandom(800, 2000);
        this.lastMovementTime = System.currentTimeMillis();
    }

    public boolean hasTimedOut(WorldPosition currentPosition) {
        if (!currentPosition.equalsPrecisely(this.initialPosition)) {
            lastMovementTime = System.currentTimeMillis();
            initialPosition = currentPosition;
            return false;
        }
        return System.currentTimeMillis() - lastMovementTime > timeout;
    }
}
```

---

## Success Criteria

1. All existing scripts updated to poll-based architecture
2. No static timeout values in any script
3. All 3D interactions use tapGameScreen with visibility checks
4. Walker usage follows break condition patterns
5. Documentation reflects new patterns
6. CLAUDE.md updated so new scripts follow these patterns automatically