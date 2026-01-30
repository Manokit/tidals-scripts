---
# tidals-scripts-3k46
title: 'Document: corpse sprite clusters cause false chompy matches'
status: completed
type: task
created_at: 2026-01-30T05:03:31Z
updated_at: 2026-01-30T05:03:31Z
---

## Problem

When a dead chompy corpse is on screen, its sprite produces pixel clusters that the chompy scanner detects. The matching algorithm finds the closest NPC to each cluster. If the closest NPC is ignored (dead), it falls through to the next-closest NPC — which is often a swamp toad — and tries to attack it. This wastes 10+ attempts clicking "walk here, cancel" on non-chompy NPCs.

## Root Cause

The pixel scan algorithm separated ignored vs valid NPCs but didn't consider that a cluster might **belong** to the ignored NPC. A corpse at screen distance d=2 would be skipped, and a toad at d=25 would be matched instead.

## Evidence from Logs

- 2 clusters at fixed screen positions (463,370) and (550,229) — never moved across 10 attempts (corpses don't move)
- Cluster 0: ignored NPC at d=2, valid NPC at d=98 → clearly the corpse
- Cluster 1: ignored NPC at d=1, valid NPC at d=15-25 → also the corpse
- Every click returned `[walk here, cancel]` — no chompy present

## Fix Applied

Added proximity ratio check: if `closestIgnoredDistance < closestValidDistance * 0.5`, skip the cluster entirely. The cluster belongs to the corpse, not the distant valid NPC.

**File:** `AttackChompy.java` — pixel scan NPC matching section (~line 1440)

## Future Consideration

If corpse sprite colors differ from live chompy sprite colors, a pixel color filter could eliminate corpse clusters at detection time rather than at matching time. This would be more robust but requires identifying the corpse-specific color values.