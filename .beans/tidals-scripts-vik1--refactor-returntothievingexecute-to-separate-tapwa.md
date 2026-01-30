---
# tidals-scripts-vik1
title: Refactor ReturnToThieving.execute() to separate tap/wait/walk states
status: completed
type: task
priority: normal
created_at: 2026-01-30T09:49:02Z
updated_at: 2026-01-30T09:55:05Z
---

ReturnToThieving.java chains tap attempt + wait + walker fallback in one execute() call. Should separate into distinct states (TAP_TILE, WAITING, WALK_FALLBACK) so each poll handles only one action.