---
# tidals-scripts-5bew
title: Optimize GemMiner rock scanning - batch respawn checks in pairs
status: completed
type: task
priority: normal
created_at: 2026-01-30T02:08:03Z
updated_at: 2026-01-30T02:08:44Z
---

After removing tile-based detection, findObjectManagerTarget scans ALL rocks for respawn circles every poll. This causes pink bounding boxes on every rock and slows mining. Fix: sort by distance, check 2 at a time, escalate to next 2 if depleted, relocate if all checked are depleted.