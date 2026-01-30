---
# tidals-scripts-ja30
title: Randomize safety-tile wait timeouts in CannonballThiever
status: completed
type: task
priority: normal
created_at: 2026-01-28T10:48:03Z
updated_at: 2026-01-28T11:26:33Z
parent: tidals-scripts-6q5p
---

## Context
Static timeouts create detectable patterns. All timeouts should be randomized.

## Current Code - Full Audit

### PrepareForBreak.java:
```java
// line 104
script.pollFramesUntil(() -> isAtSafetyTile(), 3000);  // static 3000ms

// line 15
private static final long MIN_TIME_BETWEEN_CHECKS_MS = 2000;  // static 2000ms

// line 49
if (currentlyThieving && lastXpGain.timeElapsed() < 1500) return false;  // static 1500ms
```

### Retreat.java:
```java
// line 35
script.pollFramesUntil(() -> isAtSafetyTile(), 3000);  // static 3000ms
```

### StartThieving.java / ReturnToThieving.java (verify):
```java
WalkConfig.timeout(10000)  // or 15000 - static values
```

## What to Change
Replace ALL static values with randomized ones:
```java
// Safety tile waits
script.pollFramesUntil(() -> isAtSafetyTile(), RandomUtils.weightedRandom(2500, 4000, 0.002));

// Check intervals - randomize once per session or per check
long minTimeBetweenChecks = RandomUtils.uniformRandom(1500, 2500);

// XP gain threshold
long xpGainThreshold = RandomUtils.uniformRandom(1200, 1800);

// Walk timeouts
.timeout(RandomUtils.weightedRandom(8000, 12000, 0.002))
```

## Acceptance Criteria
- [x] No static timeout in PrepareForBreak.java pollFramesUntil
- [x] No static timeout in Retreat.java pollFramesUntil
- [x] MIN_TIME_BETWEEN_CHECKS_MS randomized
- [x] lastXpGain threshold randomized
- [x] WalkConfig timeouts randomized in StartThieving/ReturnToThieving
- [x] Build passes

## Reference
- `osmb_code_review_feedback.md` section "Randomize Timeouts to Avoid Patterns"