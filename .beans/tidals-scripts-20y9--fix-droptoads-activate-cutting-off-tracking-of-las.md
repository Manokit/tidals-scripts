---
# tidals-scripts-20y9
title: Fix DropToads activate() cutting off tracking of last toad
status: completed
type: bug
priority: normal
created_at: 2026-01-30T04:22:05Z
updated_at: 2026-01-30T04:22:24Z
---

After dropping the last toad, inventory is 0 so activate() returns false. The state machine never reaches TRACKING for the last toad, so it's untracked. Fix: if state machine is mid-execution, activate() should always return true.