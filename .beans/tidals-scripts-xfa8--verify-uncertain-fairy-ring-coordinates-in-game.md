---
# tidals-scripts-xfa8
title: Verify uncertain fairy ring coordinates in-game
status: todo
type: task
created_at: 2026-02-02T08:23:36Z
updated_at: 2026-02-02T08:23:36Z
---

These fairy ring codes have NO matching cache spawn at their current coordinates and need in-game verification:

- **AIS** (Auburn Valley): current (1632, 2976), nearest cache spawn is (1651, 3010) — same as AJP??
- **CKQ** (Aldarin): current (1551, 2948), nearest cache spawn (1359, 2941) — 192 tiles off
- **BKS** (Zanaris): current (2412, 4434), nearest cache (2328, 4426) — 84 tiles, Zanaris has multiple ring objects
- **CJQ** (Great Conch): current (2158, 2753), no obvious cache match
- **AKS/DLP conflict**: both now map to (2571, 2956) — AKS (feldip hunter) and DLP (grimstone) can't share coords. One is wrong.

## How to verify
Stand at each ring in-game and note the exact world position from the debug overlay.