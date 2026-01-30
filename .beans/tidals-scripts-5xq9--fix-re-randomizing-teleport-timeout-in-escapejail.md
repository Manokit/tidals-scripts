---
# tidals-scripts-5xq9
title: Fix re-randomizing teleport timeout in EscapeJail
status: completed
type: bug
created_at: 2026-01-30T09:42:19Z
updated_at: 2026-01-30T09:42:19Z
---

In EscapeJail.java checkTeleportProgress(), RandomUtils.weightedRandom() was called every frame instead of once when the teleport starts. Fixed by computing the timeout once at teleport start and reusing it. Per OSMB code review feedback item #3 (randomize timeouts once, not every check).