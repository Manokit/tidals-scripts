---
# tidals-scripts-rtuw
title: Fix fairy ring teleport confirm - OCR the Teleport button
status: completed
type: bug
priority: normal
created_at: 2026-02-02T07:14:15Z
updated_at: 2026-02-02T07:15:10Z
---

confirmTeleport() tries obj.interact('Confirm') which doesn't exist. Need to OCR the 'Teleport to this location' button overlay (BOTTOM_RIGHT, color -12574720, FANCY_STANDARD_FONT) and tap it.