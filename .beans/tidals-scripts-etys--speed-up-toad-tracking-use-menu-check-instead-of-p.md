---
# tidals-scripts-etys
title: 'Speed up toad tracking: use menu check instead of pixel scan after drop'
status: completed
type: feature
priority: normal
created_at: 2026-01-30T06:43:49Z
updated_at: 2026-01-30T06:44:48Z
---

Replace the WAITING_FOR_TOAD + TRACKING pixel cluster scan with a faster menu entry check on lastDropPosition. After dropping, check 'examine bloated toad' on the known drop tile instead of scanning 25 tiles with pixel clusters.