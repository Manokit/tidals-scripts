---
# tidals-scripts-6gj8
title: Dax WebWalker - Full Implementation
status: in-progress
type: epic
created_at: 2026-01-31T02:24:07Z
updated_at: 2026-01-31T02:24:07Z
---

Full Dax WebWalker implementation for TidalsUtilities, based on PRDs/Webwalker_PRD.md.

**Goal:** Production-ready webwalker that lives in `utilities/` as a private shared library. Scripts call `DaxWebWalker.walkTo(script, destination)` and get pathfinding, teleports, and obstacle handling.

**Architecture:**
- `utilities/src/main/java/utilities/webwalker/` — all code lives here
- POC (`DaxWebWalkerPOC.java`) already validates API connectivity + basic walking
- Full implementation adds: proper JSON parsing, obstacle handlers, teleport system, path executor

**Privacy:** Code stays in `utilities/` jar — not published, not in individual script repos.

**Phases:**
1. Foundation — DaxApiClient, PlayerDetailsBuilder, PathResult models
2. Obstacle System — ObstacleHandler interface, DoorHandler, StairsHandler, etc.
3. Teleport System — TeleportRegistry with jewelry/spell/equipment teleports
4. Path Execution Engine — PathExecutor with stuck detection, obstacle integration
5. Public API & Polish — DaxWebWalker facade, WalkOptions, caching

See PRDs/Webwalker_PRD.md for full spec.