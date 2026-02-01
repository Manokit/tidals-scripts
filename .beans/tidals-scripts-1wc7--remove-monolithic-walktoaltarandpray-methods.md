---
# tidals-scripts-1wc7
title: Remove monolithic walkToAltarAndPray methods
status: completed
type: bug
priority: high
created_at: 2026-01-31T02:09:43Z
updated_at: 2026-01-31T02:13:47Z
---

walkToAltarAndPray() and walkToLumbridgeAltarAndPray() are monolithic and called from the main loop via useFairyRingReturn(). Deduplicate into one parameterized method and ensure they return to the state machine instead of blocking.