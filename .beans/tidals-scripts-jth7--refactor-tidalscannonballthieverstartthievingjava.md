---
# tidals-scripts-jth7
title: Refactor TidalsCannonballThiever/StartThieving.java to poll-based states
status: completed
type: feature
priority: high
created_at: 2026-01-28T08:45:32Z
updated_at: 2026-01-28T09:00:26Z
parent: apzr
---

# Refactor StartThieving.java to Poll-Based States

Break up the execute() method's sequential positioning and stealing logic into clean states.

## Current Problems

- Initial positioning logic mixed with guard waiting and stealing
- Multiple operations before returning
- Retry loops within execute()

## Target Structure

```java
@Override
public boolean execute() {
    // state: not at exact thieving tile? walk there
    if (!initialPositionDone && !isAtExactThievingTile()) {
        walkToThievingTile();
        return true;
    }
    
    // state: at position but guard not synced? wait
    if (!initialPositionDone && !guardSynced()) {
        return true; // just re-poll, guard tracker updates
    }
    
    // state: initial setup complete
    if (!initialPositionDone) {
        initialPositionDone = true;
        return true;
    }
    
    // state: guard in danger zone? abort
    if (guardInDangerZone()) { return false; }
    
    // state: ready to steal
    tapStall();
    currentlyThieving = true;
    return true;
}
```

## Checklist

- [x] Identify all distinct states in current execute()
- [x] Separate initial positioning from guard sync from stealing
- [x] Remove retry loop - let framework handle retries via re-poll
- [x] Extract state check methods (used existing helpers as state checks)
- [ ] Test single-stall mode
- [ ] Test two-stall mode