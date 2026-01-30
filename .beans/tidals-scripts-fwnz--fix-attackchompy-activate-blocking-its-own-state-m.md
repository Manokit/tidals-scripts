---
# tidals-scripts-fwnz
title: Fix AttackChompy activate() blocking its own state machine during combat
status: completed
type: bug
priority: normal
created_at: 2026-01-30T04:33:26Z
updated_at: 2026-01-30T04:33:53Z
---

When inCombat=true, activate() returns false, preventing execute() from running. State machine stuck in CONFIRMING_COMBAT unable to progress to POST_KILL. Fix: return true when state machine is mid-execution (not SCANNING).