---
# tidals-scripts-5ml0
title: Fix FillBellows only filling one bellows charge then losing priority
status: completed
type: bug
priority: normal
created_at: 2026-01-30T07:26:33Z
updated_at: 2026-01-30T07:27:33Z
---

After one suck, bellowsEmpty gets cleared and allBellowsEmpty() returns false (one bellows has charge). FillBellows loses priority to InflateToads. Fix: add fillingInProgress flag to keep FillBellows active during multi-suck cycles, and check for ANY empty bellows not just ALL empty.