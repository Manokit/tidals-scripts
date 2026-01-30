---
# tidals-scripts-bm35
title: Refactor DropToads to poll-based state machine
status: completed
type: feature
priority: normal
created_at: 2026-01-28T11:19:31Z
updated_at: 2026-01-30T03:52:16Z
parent: tidals-scripts-6q5p
---

## Context
DropToads.execute() does multiple sequential actions in one poll.

## What Changed
Converted execute() from a while-loop with embedded waits to a poll-based state machine with 6 states:
- WALKING_TO_AREA → CHECK_POSITION → DROPPING → WAITING_FOR_TOAD → TRACKING → DONE

Instance fields track progress across polls: `dropState`, `toadsDropped`, `toadsTarget`, `retries`.

## Acceptance Criteria
- [x] State enum or equivalent
- [x] Progress tracking via instance fields
- [x] No while loops with embedded waits
- [x] Each execute() does one action
- [x] Chompy interrupt detection preserved
- [x] Build passes