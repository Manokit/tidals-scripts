---
# tidals-scripts-3es0
title: Fix DropToads priority - toads not all dropped before inflate reactivates
status: completed
type: bug
priority: normal
created_at: 2026-01-30T04:20:02Z
updated_at: 2026-01-30T04:20:18Z
---

DropToads has lower priority than InflateToads, so after dropping 1 toad (2 remaining), InflateToads reactivates because inventoryToads < MAX_INVENTORY_TOADS (2 < 3). Fix: swap priority order so DropToads runs before InflateToads.