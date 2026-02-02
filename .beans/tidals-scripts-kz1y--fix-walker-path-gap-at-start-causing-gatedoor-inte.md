---
# tidals-scripts-kz1y
title: Fix walker path gap at start causing gate/door interference
status: completed
type: bug
priority: normal
created_at: 2026-02-02T09:42:49Z
updated_at: 2026-02-02T10:03:51Z
---

**Root cause**: Two PathExecutor bugs fixed:

1. **Waypoint stuck loop**: Distance-based skip (ARRIVAL_DISTANCE=4) only advances past waypoints the player is *near*. When the player walks at an angle and overshoots early waypoints by 5+ tiles, waypointIdx gets permanently stuck. OSMB walkPath() returns 'Destination reached' instantly for backwards segments, causing infinite loops. Fix: triangle inequality check — skip waypoints farther from path end than the player.

2. **Unnecessary gate/door opens**: The 'near skipped obstacle' handler fires for any obstacle within 7 tiles — including ones behind the player. Fix: only handle if obstacle is between player and destination (obstacle→end < player→end).

Also added items 13-14 to Critical Concepts in CLAUDE.md documenting these patterns.