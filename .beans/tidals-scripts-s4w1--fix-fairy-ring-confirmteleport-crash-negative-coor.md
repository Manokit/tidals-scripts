---
# tidals-scripts-s4w1
title: Fix fairy ring confirmTeleport crash - negative coordinate bounds
status: completed
type: bug
created_at: 2026-02-02T08:59:28Z
updated_at: 2026-02-02T08:59:28Z
---

confirmTeleport() used a non-existent OverlayBoundary.transformOverlayPosition() static method, producing (0,0) transform that led to negative coordinates (-628, -200) when offsets were applied. Fixed by computing the button position from screen bounds (getScreen().getBounds()) minus the debug-tool offsets.