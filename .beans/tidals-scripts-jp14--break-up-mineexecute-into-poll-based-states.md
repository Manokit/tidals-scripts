---
# tidals-scripts-jp14
title: Break up Mine.execute() into poll-based states
status: completed
type: feature
priority: normal
created_at: 2026-01-28T11:19:27Z
updated_at: 2026-01-29T08:45:49Z
parent: tidals-scripts-6q5p
---

## Context
The original feedback says: "Don't do many consecutive things in a single poll." and "Break up linear execute() methods - one action per poll."

## Current Code
`TidalsGemMiner/src/main/java/tasks/Mine.java`:

### execute() method (lines 136-175):
```java
public boolean execute() {
    // state: stuck too long? stop script
    if (isStuckTooLong()) { handleStuck(); return false; }
    
    // state: not in mining area? walk there
    if (!inMiningArea(myPos)) { walkToMine(); return false; }
    
    // refresh rock positions
    refreshKnownRockPositions(myPos);
    
    // state: find target rock
    RockTarget target = findBestTarget(myPos, isUpperMine);
    
    // state: mine the target
    return mineTarget(target, myPos, isUpperMine);  // ← This does A LOT
}
```

### mineTarget() method (lines 335-373):
```java
private boolean mineTarget(RockTarget target, ...) {
    // 1. wait for player idle (or skip)
    if (!waitForPlayerIdle()) return false;
    
    // 2. snapshot inventory
    snapshotGemCounts();
    
    // 3. tap the rock
    if (!tapTarget(target)) return false;
    
    // 4. wait for approach
    if (!waitForApproachToRock(...)) return false;
    
    // 5. wait for mining completion
    MiningResult result = waitForMiningCompletion(rockPos);
    
    // 6. handle result
    return handleMiningResult(result, ...);
}
```

This chains 6 sequential operations in `mineTarget()`.

## What to Change
Convert to state machine with enum:
```java
private enum MiningState {
    IDLE,           // waiting for player to stop
    TAPPING,        // sending tap action
    APPROACHING,    // walking to rock
    MINING,         // waiting for completion
    PROCESSING      // handling result
}
```

Each execute() handles ONE state transition:
- IDLE: check player idle → transition to TAPPING
- TAPPING: tap rock → transition to APPROACHING
- APPROACHING: poll movement → transition to MINING when adjacent
- MINING: poll for chat/respawn → transition to PROCESSING
- PROCESSING: update counters, reset state

## Acceptance Criteria
- [ ] State enum or equivalent tracking
- [ ] Each execute() does ONE action then returns
- [ ] State persists between polls via instance fields
- [ ] Target rock cached between states (don't re-find each poll)
- [ ] Build passes

## Reference
- `docs/poll-based-architecture.md`
- `osmb_code_review_feedback.md` lines 5, 349