---
# tidals-scripts-c70f
title: Refactor Retreat.execute() to separate tap and wait states
status: completed
type: task
priority: normal
created_at: 2026-01-30T09:49:00Z
updated_at: 2026-01-30T09:55:05Z
---

Retreat.java chains tap + pollFramesUntil wait in one execute() call. Should separate into TAP and WAITING states so only one action happens per poll cycle.