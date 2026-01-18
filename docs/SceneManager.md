# SceneManager

**Package:** `com.osmb.api.scene`

**Type:** Interface

**Access:** `script.getSceneManager()`

The `SceneManager` interface provides access to scene-level data including tiles, collision maps, and heightmaps. This is primarily used for advanced pathfinding, tile analysis, and understanding the game world structure.

> **Note:** For finding NPCs, use the Minimap's NPC positions - see [highlight-npc-detection.md](highlight-npc-detection.md). NPCs are NOT accessed through SceneManager.

---

## Constants

| Constant | Type | Description |
|----------|------|-------------|
| `SCENE_TILE_SIZE` | `int` | Size of the scene in tiles (typically 104) |

---

## Method Summary

| Method | Returns | Description |
|--------|---------|-------------|
| `getTiles()` | `RSTile[][][]` | Get all tiles (plane, x, y) |
| `getTile(Position)` | `RSTile` | Get specific tile at position |
| `getLevelCollisionMap()` | `CollisionMap[]` | Get collision maps for all planes |
| `getLevelCollisionMap(int)` | `CollisionMap` | Get collision map for specific plane |
| `getTileCollisionFlag(Position)` | `int` | Get collision flags at position |
| `getTileSettingFlag(Position)` | `int` | Get tile settings at position |
| `getLevelHeightmap()` | `int[][][]` | Get terrain height data |
| `getLevelTileFlags()` | `byte[][][]` | Get tile flag data |
| `inScene(int, int)` | `boolean` | Check if coordinates are in scene |
| `getSceneBaseTileX/Y()` | `int` | Get scene base coordinates |
| `getSceneEndTileX/Y()` | `int` | Get scene end coordinates |
| `refresh()` | `void` | Refresh scene data |

---

## Tile Methods

### getTile(Position)

```java
RSTile getTile(Position pos)
```

Gets the tile at a specific position.

**Parameters:**
- `pos` - The position to query

**Returns:** The `RSTile` at that position, or `null` if out of scene.

**Example:**
```java
Position myPos = script.getWorldPosition();
RSTile currentTile = script.getSceneManager().getTile(myPos);
```

---

### getTiles()

```java
RSTile[][][] getTiles()
```

Gets all tiles in the scene as a 3D array indexed by [plane][x][y].

**Returns:** 3D array of all tiles.

---

### inScene(int, int)

```java
boolean inScene(int x, int y)
```

Checks if the given coordinates are within the current scene bounds.

**Parameters:**
- `x` - The x coordinate
- `y` - The y coordinate

**Returns:** `true` if coordinates are in the scene.

---

## Collision Methods

### getLevelCollisionMap(int)

```java
CollisionMap getLevelCollisionMap(int plane)
```

Gets the collision map for a specific plane.

**Parameters:**
- `plane` - The plane level (0 = ground floor)

**Returns:** The `CollisionMap` for that plane.

---

### getTileCollisionFlag(Position)

```java
int getTileCollisionFlag(Position pos)
```

Gets the collision flags for a specific position. Flags indicate what types of movement are blocked.

**Parameters:**
- `pos` - The position to query

**Returns:** Collision flag bitmask.

**Example:**
```java
Position targetPos = new WorldPosition(3200, 3200, 0);
int flags = script.getSceneManager().getTileCollisionFlag(targetPos);

// check if tile is walkable (no full block)
boolean walkable = (flags & CollisionFlags.BLOCK_MOVEMENT_FULL) == 0;
```

---

## Scene Bounds

### getSceneBaseTileX() / getSceneBaseTileY()

```java
int getSceneBaseTileX()
int getSceneBaseTileY()
```

Gets the base (minimum) world coordinates of the current scene.

---

### getSceneEndTileX() / getSceneEndTileY()

```java
int getSceneEndTileX()
int getSceneEndTileY()
```

Gets the end (maximum) world coordinates of the current scene.

---

## Common Patterns

### Check If Position Is Walkable

```java
public boolean isWalkable(Position pos) {
    int flags = script.getSceneManager().getTileCollisionFlag(pos);
    return (flags & CollisionFlags.BLOCK_MOVEMENT_FULL) == 0;
}
```

### Get Current Scene Bounds

```java
int baseX = script.getSceneManager().getSceneBaseTileX();
int baseY = script.getSceneManager().getSceneBaseTileY();
int endX = script.getSceneManager().getSceneEndTileX();
int endY = script.getSceneManager().getSceneEndTileY();

script.log(getClass(), "Scene bounds: " + baseX + "," + baseY + " to " + endX + "," + endY);
```

---

## Important Notes

1. **Collision Map is STATIC** - The collision map loaded at scene start doesn't update when doors open/close. Always verify door states visually via menu interaction.

2. **NPCs are NOT in SceneManager** - Use minimap positions for NPC detection:
   ```java
   List<WorldPosition> npcPositions = script.getWidgetManager().getMinimap().getNPCPositions();
   ```

3. **Use Walker for Pathfinding** - Don't manually parse collision data. Use the built-in `getWalker()` for navigation.

4. **Scene Refreshes on Movement** - The scene reloads as you move through the world. Objects at the edge of your scene may appear/disappear.

---

## See Also

- [Walker.md](Walker.md) - Navigation and pathfinding
- [highlight-npc-detection.md](highlight-npc-detection.md) - NPC detection via minimap
- [critical-concepts.md](critical-concepts.md) - Door verification
