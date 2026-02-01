---
# tidals-scripts-y7tj
title: Bake door/gate spawn locations into walker utilities
status: completed
type: task
priority: normal
created_at: 2026-01-31T04:33:37Z
updated_at: 2026-01-31T04:36:50Z
---

Extract all door and gate locations with 'Open' action from map-mcp data and bake them into a static resource file shipped with TidalsUtilities.jar. This allows PathExecutor to know about ALL doors/gates along a path without relying on runtime getObjectManager() scans (which only see ~40 tiles).

## Context
PathExecutor pre-scans for obstacles using getObjectManager(), but this only finds objects loaded in the current scene. When walking long segments (200+ waypoints), doors beyond the loaded range are invisible. The stuck detection fallback is slow and unreliable. Baking spawn data gives us full path awareness.

## Checklist
- [ ] Create Python generator script to extract door/gate spawns from map-mcp data
- [ ] Generate a JSON resource file with all openable door/gate world positions  
- [ ] Create ObstacleDatabase.java to load and query the baked data
- [ ] Integrate into PathExecutor: use baked data for obstacle scanning instead of (or in addition to) getObjectManager()
- [ ] Build and verify utilities jar compiles
- [ ] Update Phase 4 bean with changes