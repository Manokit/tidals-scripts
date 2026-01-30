---
# tidals-scripts-d551
title: Skip toad verification when occluded by chompy/player, add menu fallback
status: completed
type: bug
priority: normal
created_at: 2026-01-30T06:34:12Z
updated_at: 2026-01-30T06:35:10Z
---

When verifyAllTrackedToads checks tileCubes for toad pixel clusters, a chompy or player standing on the toad occludes the sprite, causing it to be falsely removed. Fix: (1) skip verification if player or tracked chompy/corpse is on that tile, (2) add 'examine bloated toad' menu entry fallback for stuck situations where we need to confirm toad presence.