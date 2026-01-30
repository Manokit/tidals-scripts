---
# tidals-scripts-87ye
title: Fix TidalsGemMiner poll-based issues
status: completed
type: task
priority: normal
created_at: 2026-01-28T07:09:54Z
updated_at: 2026-01-28T07:20:19Z
parent: tidals-scripts-8cpu
blocking:
    - tidals-scripts-te2i
    - tidals-scripts-iybj
    - tidals-scripts-0haj
---

# Fix TidalsGemMiner Issues from Code Review

Apply the 4 specific issues identified in the code review.

## Checklist

- [x] **Remove color detection fallback** - ObjectManager will always return objects if in the mine, so the fallback is unnecessary
- [x] **Move area validation to pre-check** - Validate location BEFORE searching for rocks, not after
- [x] **Randomize SWING_PICK_TIMEOUT_MS** - Currently static 2.5s interval creates detectable patterns
- [x] **Randomize static 2000ms timeout** - The deposit polling timeout should be randomized and re-randomized each execution

## Implementation Notes

### Area Validation Pattern
```java
// BEFORE (wrong order)
if (gemRocks == null || gemRocks.isEmpty()) {
    if (!selectedLocation.miningArea().contains(myPos)) {
        // walk to area
    }
}

// AFTER (correct order)  
if (!selectedLocation.miningArea().contains(myPos)) {
    // walk to area first
    return false;
}
// Then search for rocks
```

### Timeout Randomization
```java
// BEFORE
private static final long SWING_PICK_TIMEOUT_MS = 2500;

// AFTER - randomize each use
long swingTimeout = RandomUtils.uniformRandom(2000, 3000);
```