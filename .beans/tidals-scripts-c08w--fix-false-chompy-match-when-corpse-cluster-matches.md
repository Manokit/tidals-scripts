---
# tidals-scripts-c08w
title: Fix false chompy match when corpse cluster matches distant non-ignored NPC
status: completed
type: bug
priority: normal
created_at: 2026-01-30T04:58:37Z
updated_at: 2026-01-30T04:59:00Z
---

Pixel scan matches corpse sprite clusters to distant swamp toads. When closest ignored NPC is d=1-2 but closest valid NPC is d=15-98, the cluster clearly belongs to the corpse. Fix: skip cluster if closest ignored NPC is significantly closer than closest valid NPC (e.g. ignored < valid * 0.5).