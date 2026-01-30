---
# tidals-scripts-nusq
title: Fix DepositOres walk break condition firing too early
status: completed
type: bug
priority: normal
created_at: 2026-01-30T07:49:15Z
updated_at: 2026-01-30T07:49:37Z
---

The depositBoxWalkConfig break condition checks only if the deposit box hull is visible on screen (>=30%). Since the deposit box is visible from the stalls, the break condition fires immediately and the walker never moves. Fix: add a distance check so the break condition only triggers when the player is actually close enough to interact.