---
# tidals-scripts-uwsy
title: Refactor FillBellows to poll-based state machine
status: completed
type: feature
priority: normal
created_at: 2026-01-28T10:48:39Z
updated_at: 2026-01-30T03:57:35Z
parent: tidals-scripts-6q5p
---

## Context
The original feedback says: "Don't do many consecutive things in a single poll. Make scripts more dynamic by performing minimal actions per poll."

## Current Code
`TidalsChompyHunter/src/main/java/tasks/FillBellows.java` execute() method (lines 92-161):

```java
// try each bubble location until one works
for (int i = 0; i < sortedBubbles.size(); i++) {
    // walk to stand position
    if (!walkToStandPosition(bubble.standPos)) {
        continue;
    }
    // try to suck bubble
    boolean sucked = RetryUtils.tap(script, tilePoly, "Suck", ...);
    if (!sucked) {
        continue;
    }
    // wait for player to walk + animation
    script.pollFramesUntil(() -> false, ...);
    // wait for bellows to fill
    return waitForBellowsToFill();
}
```

This does walk + tap + wait + fill all in one execute().

## What to Change
Convert to state machine with states:
1. `FIND_BUBBLE` - select nearest bubble, record target
2. `WALK_TO_BUBBLE` - walk to stand position (one step)
3. `INTERACT_WITH_BUBBLE` - tap to suck
4. `WAIT_FOR_FILL` - poll until bellows charged
5. `RETURN_TO_AREA` - walk back to toad drop area

Each execute() handles ONE state transition and returns.

## Implementation Notes
- Add state enum or use instance fields to track current bubble index and current state
- On activate(), reset state if bellows empty flag changed
- Check for crash/chompy interrupts between states (already partially done)

## Acceptance Criteria
- [ ] No loops in execute() that do multiple sequential actions
- [ ] Each execute() call does one thing then returns
- [ ] State persists between polls
- [ ] Interrupt handling preserved
- [ ] Build passes

## Reference
- `docs/poll-based-architecture.md`
- `osmb_code_review_feedback.md` section "Core Principle: Poll-Based Script Structure"