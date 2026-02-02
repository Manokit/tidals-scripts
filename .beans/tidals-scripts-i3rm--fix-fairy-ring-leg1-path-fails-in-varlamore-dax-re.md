---
# tidals-scripts-i3rm
title: Fix fairy ring leg1 path fails in Varlamore - Dax returns transport-crossing path
status: in-progress
type: bug
priority: normal
created_at: 2026-02-02T07:58:34Z
updated_at: 2026-02-02T08:55:36Z
---

## Fairy ring routing — coordinate audit + walker bugs

### Completed
- [x] Fixed 7 fairy ring coordinates from cache data (AJP, DLP, CKS, AKR, AKP, DIP, DKS)
- [x] Updated both reg() and FAIRY_RING_LOCATIONS entries
- [x] Transport gap check on direct path — prevents walker from using DAX paths with fairy ring hops
- [x] Transport gap check on ALL fallback paths (teleport fallback + fairy ring fallback)
- [x] Fairy ring leg failure logging (shows leg1/leg2 status)
- [x] Handle trivial leg2 — when destination IS the fairy ring (≤5 tiles), treat leg2 as 0-cost even if DAX errors on 0-distance routing
- [x] Skip leg2 walk execution when leg2 path failed but destination is trivial
- [x] Updated TidalsWalkerTest CKS destination to corrected coordinate (3447, 3470)
- [x] Built utilities + TidalsWalkerTest

### Awaiting test
Player at AJP ring → walk to CKS ring. Expected:
- Direct path SKIPPED (transport gap)
- Fairy ring CKS: leg1 ~5 tiles + 45 weight + 0 leg2 = ~50 cost (cheapest)
- Walker uses fairy ring, teleports to CKS, returns success