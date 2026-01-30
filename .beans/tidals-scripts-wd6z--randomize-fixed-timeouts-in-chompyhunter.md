---
# tidals-scripts-wd6z
title: Randomize fixed timeouts in ChompyHunter
status: completed
type: task
priority: normal
created_at: 2026-01-28T10:48:42Z
updated_at: 2026-01-28T11:31:55Z
parent: tidals-scripts-6q5p
---

## Context
Static timeouts create detectable patterns that could be used for bot detection.

## Current Code - Full Audit

### AttackChompy.java constants:
```java
private static final int KILL_CONFIRMATION_TIMEOUT_MS = 20000;  // static 20s
private static final int MONITORING_POLL_MS = 600;  // static 600ms
```

### AttackChompy.java pollFramesUntil calls (verify line numbers):
```java
pollFramesUntil(..., 3000)   // various locations
pollFramesUntil(..., 2000)
pollFramesUntil(..., 6000)
pollFramesUntil(..., 5000)
```

### InflateToads.java:
```java
// line 266
script.pollFramesUntil(..., 8000);  // static 8s timeout
```

### DropToads.java:
```java
// line 332
WalkConfig.timeout(3000)  // static 3s

// line 383/417
waitForPlayerToStop(..., 3000)  // static 3s timeout parameter
```

### HopWorld.java:
```java
private static final long POST_HOP_STABILIZATION_MS = 10000;  // static 10s
```

## What to Change
Replace ALL static values with randomized equivalents:
```java
// Constants → randomize on each use
long killTimeout = RandomUtils.uniformRandom(18000, 22000);
long monitoringPoll = RandomUtils.uniformRandom(400, 800);

// pollFramesUntil calls
script.pollFramesUntil(..., RandomUtils.weightedRandom(6000, 10000, 0.002));

// WalkConfig timeouts
.timeout(RandomUtils.weightedRandom(2500, 4000, 0.002))

// waitForPlayerToStop
waitForPlayerToStop(5, RandomUtils.uniformRandom(2500, 3500));
```

## Files to Audit
1. AttackChompy.java - all constants and poll calls
2. InflateToads.java - line 266 and any others
3. DropToads.java - WalkConfig.timeout and waitForPlayerToStop
4. FillBellows.java - verify (some already randomized)
5. HopWorld.java - POST_HOP_STABILIZATION_MS

## Acceptance Criteria
- [x] No hardcoded ms timeout constants
- [x] All pollFramesUntil calls use RandomUtils
- [x] All WalkConfig.timeout calls use RandomUtils
- [x] waitForPlayerToStop timeout randomized
- [x] Build passes

## Reference
- `osmb_code_review_feedback.md` section "Randomize Timeouts to Avoid Patterns"