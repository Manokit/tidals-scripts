---
# tidals-scripts-inzv
title: 'Phase 2: Obstacle Handler System'
status: completed
type: feature
priority: normal
created_at: 2026-01-31T02:24:49Z
updated_at: 2026-01-31T03:13:04Z
parent: tidals-scripts-6gj8
blocking:
    - tidals-scripts-dyni
---

Implement obstacle detection and handling for doors, gates, stairs, ladders, trapdoors.

## Checklist

- [x] Create `ObstacleHandler` interface (canHandle, handle, getPriority)
- [x] Create `BlockingObstacle` model
- [x] Create `ObstacleDetector` — scans nearby objects, matches handlers (no A* — OSMB lacks this API)
- [x] Implement `DoorHandler` — checks getActions() for Open/Close (avoids tapGetResponse double-tap)
- [x] Implement `GateHandler` — similar to DoorHandler
- [x] Implement `StairsHandler` — Climb-up/Climb-down, plane change detection via getPlane()
- [x] Implement `LadderHandler` — up/down determination with targetPlane hint
- [x] Implement `TrapdoorHandler` — open first, then climb (re-queries by position after open)
- [x] Implement `SpecialObjectHandler` — Draynor Manor one-way door (priority 100)
- [x] Test door handling at Lumbridge Castle
- [x] Test stairs at Lumbridge Castle

## Key Insight
OSMB collision map is STATIC — cannot tell if door is open/closed. Must use `tapGetResponse()` to check menu action ("Open" = closed, "Close" = open). See PRD §The Door Problem.