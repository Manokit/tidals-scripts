---
# tidals-scripts-9tbn
title: Randomize fixed timeouts/cooldowns in GemMiner
status: completed
type: task
priority: normal
created_at: 2026-01-28T11:19:28Z
updated_at: 2026-01-28T11:24:04Z
parent: tidals-scripts-6q5p
---

## Context
Static timeouts create detectable patterns. All timeouts should be randomized and re-randomized on each use.

## Current Code
`TidalsGemMiner/src/main/java/tasks/Mine.java` constants:
```java
private static final long STUCK_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes
private static final long ROCK_SCAN_INTERVAL_MS = 4_000L;
private static final long CHAT_REPEAT_WINDOW_MS = 2000L;
private static final long ROCK_COOLDOWN_MS = 20_000;
private static final long STUCK_ROCK_COOLDOWN_MS = 60_000;
```

Line 785 in waitForPlayerIdle():
```java
boolean stationary = stationaryTimer.timeElapsed() > 250;  // static 250ms
```

### Other files to audit:
- `HopWorld.java`: `HOP_COOLDOWN_MS`, `POST_HOP_STABILIZATION_MS`
- `DetectPlayers.java`: `POST_HOP_GRACE_MS`, `POST_LOGIN_GRACE_MS`, zone min/max values

## What to Change
Replace static constants with randomized values:
```java
// Before
private static final long ROCK_COOLDOWN_MS = 20_000;

// After - randomize on each use
long cooldown = RandomUtils.uniformRandom(18000, 22000);
recentlyMinedRocks.put(pos, System.currentTimeMillis() + cooldown);
```

For values that need consistency within a session, randomize once at script start.

## Acceptance Criteria
- [ ] No hardcoded ms timeout constants used directly
- [ ] All timeouts use RandomUtils variants
- [ ] Values re-randomized on each use where appropriate
- [ ] Build passes

## Reference
- `osmb_code_review_feedback.md` section "Randomize Timeouts to Avoid Patterns"