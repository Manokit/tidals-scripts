---
# tidals-scripts-84zv
title: Break up PrepareForBreak.execute() into poll-based states
status: completed
type: feature
priority: normal
created_at: 2026-01-28T11:19:29Z
updated_at: 2026-01-30T03:57:35Z
parent: tidals-scripts-6q5p
---

## Context
The original feedback says: "Don't do many consecutive things in a single poll."

## Current Code
`TidalsCannonballThiever/src/main/java/tasks/PrepareForBreak.java` execute() (lines 88-136):
```java
public boolean execute() {
    currentlyThieving = false;
    
    // 1. tap on safety tile
    if (!tapOnTile(SAFETY_TILE)) {
        script.getWalker().walkTo(SAFETY_TILE);
    }
    
    // 2. wait to arrive
    script.pollFramesUntil(() -> isAtSafetyTile(), 3000);
    
    // 3. check arrival
    if (!isAtSafetyTile()) {
        return true;
    }
    
    // 4. trigger action based on reason
    if (activatedForWhiteDot) {
        script.getProfileManager().forceHop();
    } else if (activatedForBreak) {
        script.getProfileManager().forceBreak();
    }
    // ... etc
    
    // 5. reset state
    StartThieving.resetAfterBreak();
    guardTracker.resetCbCycle();
    
    return true;
}
```

This does tap → wait → trigger → reset all in one poll.

## What to Change
Convert to state machine:
1. `WALKING` - tap/walk to safety tile, return immediately
2. `WAITING` - poll for arrival (one frame check per poll)
3. `TRIGGERING` - execute forceHop/forceBreak/forceAFK
4. `CLEANUP` - reset state, return control

Each execute() handles ONE state and returns.

## Implementation Notes
- Add instance field for current state
- Cache the activation reason (whiteDot/break/hop/AFK) so it persists across polls
- Reset state when task completes or deactivates

## Acceptance Criteria
- [ ] State tracking via enum or field
- [ ] Each execute() does ONE thing
- [ ] Activation reason preserved across polls
- [ ] Build passes

## Reference
- `docs/poll-based-architecture.md`