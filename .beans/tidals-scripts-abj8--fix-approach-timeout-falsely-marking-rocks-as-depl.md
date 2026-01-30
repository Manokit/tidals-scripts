---
# tidals-scripts-abj8
title: Fix approach timeout falsely marking rocks as depleted
status: completed
type: bug
priority: normal
created_at: 2026-01-30T02:35:20Z
updated_at: 2026-01-30T02:35:46Z
---

The waitForApproachToRock method returns reached=true when the player merely gets closer to the rock (not adjacent). The 1.2-2.4s timeout is too short, and the success condition checks distance reduction rather than adjacency. This causes the bot to enter MINING state at distance 2.0, timeout waiting for swing pick, then falsely mark the rock as depleted because handleMiningResult's adjacency check passes at distance 2.0 (which is NOT actually adjacent for diagonal tiles). Fix: waitForApproachToRock should only succeed on actual adjacency, and use a longer timeout. The MINING state should also only start the swing-pick timeout after confirming adjacency.