---
# tidals-scripts-0a36
title: Fix fairy ring route stuck when player already near ring
status: completed
type: bug
priority: normal
created_at: 2026-02-02T09:33:44Z
updated_at: 2026-02-02T09:34:09Z
---

## Problem
PathExecutor gets stuck in a 6-iteration loop when the player is already ~4 tiles from the fairy ring. The OSMB built-in walker says 'Destination reached' instantly (its internal break distance is ~5 tiles) but the PathExecutor's ARRIVAL_DISTANCE=4 doesn't advance the waypoint index since the distance is 4.12 tiles. Result: infinite stuck loop → failure → fallback.

## Root Cause
`executeFairyRingRoute()` always runs `pathExecutor.execute(leg1)` even when the player is already standing next to the ring. Short paths (< ~8 tiles) cause the OSMB walker to think it's arrived without actually moving.

## Fix
In `executeFairyRingRoute()`, check if the player is already close to the source ring (say within 8 tiles). If so, skip the leg1 PathExecutor walk entirely.

## File
`utilities/src/main/java/utilities/webwalker/TidalsWalker.java` line ~366-368