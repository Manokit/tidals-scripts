---
# tidals-scripts-jocq
title: Refactor AttackChompy to poll-based state machine
status: completed
type: feature
priority: normal
created_at: 2026-01-28T11:19:30Z
updated_at: 2026-01-29T03:53:37Z
parent: tidals-scripts-6q5p
---

## Context
AttackChompy.java is ~500+ lines with complex state tracking. The execute() method likely chains detection → attack → combat wait → pluck cycle.

## What Was Done
Converted to explicit state machine with `CombatState` enum:
```java
private enum CombatState {
    SCANNING,           // scanning for chompy
    ATTACKING,          // sending attack action
    CONFIRMING_COMBAT,  // waiting for health overlay
    IN_COMBAT,          // waiting for kill
    POST_KILL,          // handle kill bookkeeping
    PLUCKING            // feather collection
}
```

Each execute() handles ONE state via switch dispatcher.

## Acceptance Criteria
- [x] Explicit state tracking (enum or equivalent)
- [x] Each execute() handles one state transition
- [x] No chained sequential actions in one poll
- [x] Cross-task flags preserved for DetectPlayers/InflateToads
- [x] Build passes
