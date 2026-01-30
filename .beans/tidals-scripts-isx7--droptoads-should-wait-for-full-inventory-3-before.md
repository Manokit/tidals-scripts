---
# tidals-scripts-isx7
title: DropToads should wait for full inventory (3) before dropping
status: completed
type: bug
priority: normal
created_at: 2026-01-30T04:26:00Z
updated_at: 2026-01-30T04:26:30Z
---

DropToads activates with any inventory toad, preventing InflateToads from batching to 3. Fix: only activate when inventory >= 3 (MAX_INVENTORY_TOADS).