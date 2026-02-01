---
# tidals-scripts-68hh
title: Add dynamic region prioritization to PathExecutor
status: completed
type: task
priority: normal
created_at: 2026-02-01T07:36:36Z
updated_at: 2026-02-01T07:37:31Z
---

Use setExpectedRegionId() and WorldPosition.getRegionID() to dynamically hint OSMB about upcoming regions during teleports and long walks. Removes need for static regionsToPrioritise() overrides in test scripts.