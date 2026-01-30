---
# tidals-scripts-i4i3
title: Fix pollFramesUntil custom-delay usage across scripts
status: completed
type: task
priority: normal
created_at: 2026-01-28T10:03:56Z
updated_at: 2026-01-28T10:05:51Z
parent: tidals-scripts-stot
---

## Why This Matters
`pollFramesUntil(() -> true, ms)` exits immediately (~38ms) with NO delay because returning `true` signals "condition met."
`pollFramesUntil(() -> false, ms)` waits the full timeout since the condition is never met.

## Checklist

### Cannonball Thiever
- [x] `TidalsCannonballThiever/src/main/java/tasks/Setup.java:94`
- [x] `TidalsCannonballThiever/src/main/java/tasks/Setup.java:117`
- [x] `TidalsCannonballThiever/src/main/java/tasks/ReturnToThieving.java:83`
- [x] `TidalsCannonballThiever/src/main/java/tasks/ReturnToThieving.java:99`
- [x] `TidalsCannonballThiever/src/main/java/tasks/WaitAtSafety.java:33`

### Gold Superheater
- [x] `TidalsGoldSuperheater/src/main/java/tasks/Setup.java:56`