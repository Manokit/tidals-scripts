---
# tidals-scripts-io9b
title: Break monolithic banking teleport methods into poll-friendly steps
status: completed
type: bug
priority: high
created_at: 2026-01-31T02:09:41Z
updated_at: 2026-01-31T02:12:44Z
---

useCraftingCapeTeleport(), useVerSinhazaBanking(), and useZanaris methods chain teleport+walk+bank in one call. Should return 0 after teleport and let state machine re-enter.