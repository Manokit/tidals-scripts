---
# tidals-scripts-790d
title: Refactor InflateToads to poll-based state machine
status: completed
type: feature
priority: normal
created_at: 2026-01-28T10:48:40Z
updated_at: 2026-01-30T03:54:16Z
parent: tidals-scripts-6q5p
---

## Context
InflateToads had nested loops in execute() + findAndInflateToad() doing detection + tap + wait in one poll.

## What Changed
Flattened into a 6-state machine: CALCULATE → FIND_TOAD → TAP_TOAD → WAIT_FOR_PICKUP → CHECK_COMPLETION → DONE.

Removed findAndInflateToad() method entirely — its logic is now distributed across the TAP_TOAD, WAIT_FOR_PICKUP, and CHECK_COMPLETION states.

## Acceptance Criteria
- [x] No nested loops in execute()
- [x] Each execute() does one action
- [x] Attempt counting preserved
- [x] Bellows empty detection preserved
- [x] Build passes