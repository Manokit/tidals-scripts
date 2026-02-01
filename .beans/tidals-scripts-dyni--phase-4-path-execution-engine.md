---
# tidals-scripts-dyni
title: 'Phase 4: Path Execution Engine'
status: completed
type: feature
priority: normal
created_at: 2026-01-31T02:24:49Z
updated_at: 2026-02-01T08:44:27Z
parent: tidals-scripts-6gj8
blocking:
    - tidals-scripts-9eyc
---

Core walking engine that combines API paths, teleports, and obstacle handling into a reliable walk loop.

## Checklist

- [x] Create `PathExecutor` — main walk loop with segment-based walking
- [x] Integrate teleport detection (first waypoint far = needs teleport)
- [x] Integrate obstacle detection mid-walk (canReach check before each segment)
- [x] Add stuck detection (no progress for 15s → try obstacle handling)
- [x] Add break condition support (BooleanSupplier to abort walk)
- [x] Create `WalkResult` (success, failed, interrupted + failure reason enum)
- [x] Test full path: Lumbridge → Varrock (doors + open terrain)
- [ ] Test long path: GE → Castle Wars (teleport + walk)

## Design (Revised)

**Architecture overhaul:** replaced runtime-only object scanning with baked obstacle database.

1. **Baked obstacle database** (`ObstacleDatabase`): 2,359 door/gate spawn locations loaded from `obstacle_spawns.json` at startup. Covers the entire game world.
2. Pre-scan: query baked DB for all doors/gates near path waypoints
3. Walk freely between obstacle points in long `walkPath()` segments
4. At each obstacle waypoint, find the live `RSObject` via `findBlockingObstacle()` and handle it
5. Plane changes (stairs/ladders) handled separately by Z-coordinate detection
6. Reactive stuck detection as fallback for anything the baked DB missed

**Key principles:**
- OSMB walkPath handles long paths smoothly — no artificial delays between segments
- Baked data provides global path awareness; live detection provides accurate local state
- Obstacles near the player on startup are handled before being skipped by waypoint advancement

## Client-Side Teleport Routing

**Critical discovery:** The Dax API does NOT route through teleports server-side. The official DaxWalker (TriBot) handles teleports entirely on the client side. The `equipment`/`inventory` fields in PlayerDetails are for quest shortcuts and agility shortcuts, NOT teleport routing.

### How it works

The client creates N+1 path requests per `walkTo()` call:
- **Index 0:** Direct walk from current position → destination
- **Index 1..N:** From each available teleport destination → destination

All requests go in a single bulk API call (the `requests` array supports multiple entries). The API returns an array of path results in the same order.

The client then compares total costs:
- **Total cost = API walk cost + teleport cost** (teleport cost is a weight from TeleportDefinition, e.g. 35 for Ring of Dueling)
- Picks the lowest total cost
- If the winner is a teleport path: execute the teleport first, then walk the remaining path
- If the winner is direct walk: just walk normally

### Example: GE → Castle Wars with Ring of Dueling

```
Available teleports: Ring of Dueling (Castle Wars, Duel Arena, Ferox Enclave), etc.

Bulk request sends:
  [0] direct: (3138,3519) → (2440,3090)
  [1] Castle Wars: (2440,3090) → (2440,3090)
  [2] Duel Arena: (3316,3235) → (2440,3090)
  [3] Ferox Enclave: (3150,3635) → (2440,3090)
  ...

Results:
  [0] direct: 114 wp, walk=687, tp=0, total=687
  [1] Castle Wars: ~1 wp, walk≈0, tp=35, total=35  ← WINNER
  [2] Duel Arena: ~80 wp, walk=500, tp=35, total=535

Best: Castle Wars (total cost 35)
→ Execute Ring of Dueling teleport to Castle Wars
→ Walk 0-1 remaining waypoints
```

### Files changed for teleport routing

- **`DaxApiClient.java`** — Added `getPathsBulk(script, List<WorldPosition[]>, player)` for bulk API calls. Added `executeBulkRequest()`, `parseBulkResponse()`, `parseSinglePathSection()`, `buildBulkPathRequest()`. Bulk response parsed by splitting on `"pathStatus"` occurrences.
- **`DaxWebWalkerPOC.java`** — Rewired `walkTo()` to: gather available teleports from `TeleportRegistry.getAvailableTeleports()`, build N+1 path requests, send bulk, pick cheapest, execute teleport if needed, fall back to direct walk if teleport fails.
- **`TidalsWalkerTest.java`** — Updated `doRequestPath()` to use bulk teleport routing instead of single `apiClient.getPath()`. `doWalk()` now executes selected teleport before PathExecutor walks.

### Previous teleport-related fixes (leading to this discovery)

9. **Dax API 400 Bad Request** — Equipment/inventory sent as flat `[2552]` instead of IntPair format `[{"key":2552,"value":1}]`. Fixed `PlayerDetails.java` to use `List<int[]>` serialized as `{"key":id,"value":count}`. Also fixed field name `"settings"` → `"setting"`.
10. **PlayerDetailsBuilder not scanning items** — `build()` never populated equipment/inventory fields, sending empty arrays. Added `TELEPORT_ITEM_GROUPS` with all jewelry/cape IDs. `scanEquipment()` uses `findItem()` per group, `scanInventory()` batches all IDs via `search()`.
11. **API returns walking path despite having Ring of Dueling** — Root cause: teleport routing is client-side, not server-side. The API only does pathfinding between two points. Implemented client-side multi-path bulk routing (this section).

## Data Pipeline

map-mcp location_configs -> generate_obstacle_spawns.py -> obstacle_spawns.json (102KB in jar) -> ObstacleDatabase HashMap -> PathExecutor

Regenerate after game updates: `python3 utilities/scripts/generate_obstacle_spawns.py`

## Bug Fixes

1. Door detection distance math failure — replaced with path-aware waypoint matching
2. Infinite segment loop — added force-advance logic
3. Stale cache re-opens already-opened doors — added handledObstacles tracking set
4. Choppy movement from 10-waypoint segments — walk full path between obstacles
5. Pre-scan matched ladders/stairs — added canHorizontalHandlerHandle filter
6. Runtime pre-scan limited to loaded scene — replaced with baked ObstacleDatabase (2,359 spawns)
7. Obstacles skipped when player starts near them — handle before waypoint advancement
8. 1-2 second stutter between walk segments — removed unconditional loop delay
9. Dax API 400 Bad Request — IntPair format mismatch + wrong field name
10. PlayerDetailsBuilder not scanning equipment/inventory — added teleport item scanning
11. No teleport routing — implemented client-side bulk multi-path teleport routing
12. Tab-swapping loop — `TeleportRegistry.getAvailableTeleports()` called `hasEquippedAny()`/`hasInventoryAny()` per teleport (34× = ~70 tab switches). Refactored to batch-scan equipment (8 varargs `findItem` calls), inventory (1 `search` call), and magic level (1 call) up front, then filter against cached sets with zero tab switches.
13. Ring of Dueling dialogue mismatch — Jagex renamed destinations: "Duel Arena"→"Emir's Arena", "Castle Wars"→"Castle Wars Arena", added "Fortis Colosseum". Also split `registerJewelry` to take separate `equippedAction` (right-click menu) and `dialogueOption` (TEXT_OPTION dialogue) strings, since equipped menu uses short names but rub dialogue uses full names.