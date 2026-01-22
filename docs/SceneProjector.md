# SceneProjector

**Type:** Interface

## Fields

| Type | Field |
|------|-------|
| `static final int` | `NOT_VISIBLE_ABOVE` |
| `static final int` | `TILE_FLAG_BRIDGE` |
| `static final int` | `TILE_FLAG_UNDER_ROOF` |
| `static final int` | `TILE_SIZE` |

## Methods

| Return Type | Method |
|------------|--------|
| `Point` | `getAreaCenter(double localX, double localY, int tileWidth, int tileHeight, int plane, int zHeight)` |
| `Point` | `getAreaCenter(double localX, double localY, int tileWidth, int tileHeight, int plane, int xOffset, int yOffset, int zOffset)` |
| `Polygon` | `getConvexHull(RSObject object)` |
| `List<Triangle>` | `getFaceTriangles(RSObject object)` |
| `Polygon` | `getTileCube(double localX, double localY, int plane, int cubeHeight)` |
| `Polygon` | `getTileCube(double localX, double localY, int plane, int baseHeight, int cubeHeight)` |
| `Polygon` | `getTileCube(double localX, double localY, int plane, int baseHeight, int cubeHeight, boolean fullyOnScreen)` |
| `Polygon` | `getTileCube(double localX, double localY, int plane, int baseHeight, int cubeHeight, int tileWidth, int tileHeight, boolean fullyOnScreen)` |
| `Polygon` | `getTileCube(Position position, int cubeHeight)` |
| `Polygon` | `getTileCube(Position position, int cubeHeight, boolean fullyOnScreen)` |
| `Point` | `getTilePoint(double localX, double localY, int plane, TileEdge edge)` |
| `Point` | `getTilePoint(double localX, double localY, int plane, TileEdge edge, int zOffset)` |
| `Point` | `getTilePoint(Position position, TileEdge edge, int zOffset)` |
| `Polygon` | `getTilePoly(double localX, double localY, int plane)` |
| `Polygon` | `getTilePoly(double localX, double localY, int plane, boolean fullyOnScreen)` |
| `Polygon` | `getTilePoly(Position position)` |
| `Polygon` | `getTilePoly(Position position, boolean fullyOnScreen)` |
| `Point` | `sceneToCanvas(int sceneX, int sceneY, int sceneZ)` |

## Field Details

### TILE_SIZE
```java
static final int TILE_SIZE
```

### TILE_FLAG_BRIDGE
```java
static final int TILE_FLAG_BRIDGE
```

### TILE_FLAG_UNDER_ROOF
```java
static final int TILE_FLAG_UNDER_ROOF
```

### NOT_VISIBLE_ABOVE
```java
static final int NOT_VISIBLE_ABOVE
```

## Method Details

### getConvexHull
```java
Polygon getConvexHull(RSObject object)
```

Computes the convex hull of the projected 2D points of the given `RSObject`'s model.

The convex hull is calculated from all visible face vertices of the model, projected onto the canvas. Faces with a `faceInfo` value of -1 are skipped.

**Parameters:**
- `object` - the `RSObject` whose model's convex hull to compute

**Returns:** a `Polygon` representing the convex hull of the object's projected model, or `null` if the model is not available or fewer than 3 points are found

### getFaceTriangles
```java
List<Triangle> getFaceTriangles(RSObject object)
```

Returns a list of triangles representing the faces of the given `RSObject` model, projected onto the 2D canvas.

**Parameters:**
- `object` - the `RSObject` whose model faces to project

**Returns:** a list of `Triangle` objects representing the projected faces, or `null` if the model is not available or no valid faces are found

### getTilePoly
```java
Polygon getTilePoly(Position position)
```

Returns a `Polygon` representing the 2D canvas projection of the tile at the given position.

**Parameters:**
- `position` - the `Position` of the tile (can be `WorldPosition` or `LocalPosition`).

**Returns:** a `Polygon` representing the projected tile, or `null` if the position is invalid or not visible

---

```java
Polygon getTilePoly(Position position, boolean fullyOnScreen)
```

Returns a `Polygon` representing the 2D canvas projection of the tile at the given position.

**Parameters:**
- `position` - the `Position` of the tile (can be `WorldPosition` or `LocalPosition`)
- `fullyOnScreen` - if true, returns null unless all projected points are on screen

**Returns:** a `Polygon` representing the projected tile, or `null` if the position is invalid or not visible

---

```java
Polygon getTilePoly(double localX, double localY, int plane)
```

Returns a `Polygon` representing the 2D canvas projection of a tile at the given local coordinates and plane.

**Parameters:**
- `localX` - the local X coordinate of the tile (may be fractional, in tile units)
- `localY` - the local Y coordinate of the tile (may be fractional, in tile units)
- `plane` - the plane (height level) of the tile

**Returns:** a `Polygon` representing the projected tile, or `null` if the position is invalid or not visible

---

```java
Polygon getTilePoly(double localX, double localY, int plane, boolean fullyOnScreen)
```

Returns a `Polygon` representing the 2D canvas projection of a tile at the given local coordinates and plane.

**Parameters:**
- `localX` - The local X coordinate of the tile (may be fractional, in tile units).
- `localY` - The local Y coordinate of the tile (may be fractional, in tile units).
- `plane` - The plane (height level) of the tile.
- `fullyOnScreen` - If true, returns null unless all projected points are on screen.

**Returns:** A `Polygon` representing the projected tile, or null if the position is invalid or not visible.

### getAreaCenter
```java
Point getAreaCenter(double localX, double localY, int tileWidth, int tileHeight, int plane, int zHeight)
```

Calculates the 2D canvas point for the center of a rectangular area covering multiple tiles. The center is computed based on the provided local tile coordinates, area width and height (in tiles), plane, and an optional Z height offset. The method projects the center of the area to canvas coordinates, taking into account the scene's heightmap and camera position.

**Parameters:**
- `localX` - The local X coordinate (tile units, may be fractional) of the area's starting tile.
- `localY` - The local Y coordinate (tile units, may be fractional) of the area's starting tile.
- `tileWidth` - The width of the area in tiles.
- `tileHeight` - The height of the area in tiles.
- `plane` - The plane (height level) of the area.
- `zHeight` - The Z offset to subtract from the area's height (for elevation adjustment).

**Returns:** The projected `Point` on the canvas representing the area's center, or `null` if not visible.

---

```java
Point getAreaCenter(double localX, double localY, int tileWidth, int tileHeight, int plane, int xOffset, int yOffset, int zOffset)
```

Calculates the 2D canvas point for the center of a rectangular area covering multiple tiles.

The center is computed based on the provided local tile coordinates, area width and height (in tiles), plane, and an optional Z height offset. The method projects the center of the area to canvas coordinates, taking into account the scene's heightmap and camera position.

**Parameters:**
- `localX` - The local X coordinate (tile units, may be fractional) of the area's starting tile.
- `localY` - The local Y coordinate (tile units, may be fractional) of the area's starting tile.
- `tileWidth` - The width of the area in tiles.
- `tileHeight` - The height of the area in tiles.
- `plane` - The plane (height level) of the area.
- `zOffset` - The Z offset to subtract from the area's height (for elevation adjustment).

**Returns:** The projected `Point` on the canvas representing the area's center, or `null` if not visible.

### getTilePoint
```java
Point getTilePoint(double localX, double localY, int plane, TileEdge edge)
```

Projects a specific point on a tile (center or edge) to 2D canvas coordinates.

**Parameters:**
- `localX` - The local X coordinate (tile units, may be fractional).
- `localY` - The local Y coordinate (tile units, may be fractional).
- `plane` - The plane (height level) of the tile.
- `edge` - The `TileEdge` specifying which edge or corner to project, or `null` for the tile center.

**Returns:** The projected `Point` on the canvas, or `null` if the position is invalid or not visible.

---

```java
Point getTilePoint(Position position, TileEdge edge, int zOffset)
```

Projects a specific point on a tile (center or edge) to 2D canvas coordinates.

**Parameters:**
- `position` - The `Position` of the tile (can be `WorldPosition` or `LocalPosition`)
- `edge` - The `TileEdge` specifying which edge or corner to project, or `null` for the tile center.
- `zOffset` - The Z offset to subtract from the tile's height (for elevation adjustment).

**Returns:** The projected `Point` on the canvas, or `null` if the position is invalid or not visible.

---

```java
Point getTilePoint(double localX, double localY, int plane, TileEdge edge, int zOffset)
```

Projects a specific point on a tile (center or edge) to 2D canvas coordinates.

**Parameters:**
- `localX` - The local X coordinate (tile units, may be fractional).
- `localY` - The local Y coordinate (tile units, may be fractional).
- `plane` - The plane (height level) of the tile.
- `edge` - The `TileEdge` specifying which edge or corner to project, or `null` for the tile center.
- `zOffset` - The Z offset to subtract from the tile's height (for elevation adjustment).

**Returns:** The projected `Point` on the canvas, or `null` if the position is invalid or not visible.

### getTileCube
```java
Polygon getTileCube(Position position, int cubeHeight)
```

Projects a 3D tile-aligned cube at the given position to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `position` - The `Position` of the tile (can be `WorldPosition` or `LocalPosition`)
- `cubeHeight` - The height of the cube to project (in scene units).

**Returns:** A `Polygon` representing the projected cube, or null if not visible or invalid.

---

```java
Polygon getTileCube(Position position, int cubeHeight, boolean fullyOnScreen)
```

Projects a 3D tile-aligned cube to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `position` - The `Position` of the tile (can be `WorldPosition` or `LocalPosition`)
- `cubeHeight` - The height of the cube to project (in scene units).
- `fullyOnScreen` - If true, returns null unless all projected points are on screen.

**Returns:** A `Polygon` representing the projected cube, or null if not visible or invalid.

---

```java
Polygon getTileCube(double localX, double localY, int plane, int cubeHeight)
```

Projects a 3D tile-aligned cube to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `localX` - The local X coordinate of the tile (tile units).
- `localY` - The local Y coordinate of the tile (tile units).
- `plane` - The plane (height level) of the tile.
- `cubeHeight` - The height of the cube to project (in scene units).

**Returns:** A `Polygon` representing the projected cube, or null if not all points or off-screen or invalid.

---

```java
Polygon getTileCube(double localX, double localY, int plane, int baseHeight, int cubeHeight)
```

Projects a 3D tile-aligned cube to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `localX` - The local X coordinate of the tile (tile units).
- `localY` - The local Y coordinate of the tile (tile units).
- `plane` - The plane (height level) of the tile.
- `baseHeight` - The base height offset to subtract from the tile's ground height.
- `cubeHeight` - The height of the cube to project (in scene units).

**Returns:** A `Polygon` representing the projected cube, or null if not all points or off-screen or invalid.

---

```java
Polygon getTileCube(double localX, double localY, int plane, int baseHeight, int cubeHeight, boolean fullyOnScreen)
```

Projects a 3D tile-aligned cube to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `localX` - The local X coordinate of the tile (tile units).
- `localY` - The local Y coordinate of the tile (tile units).
- `plane` - The plane (height level) of the tile.
- `baseHeight` - The base height offset to subtract from the tile's ground height.
- `cubeHeight` - The height of the cube to project (in scene units).
- `fullyOnScreen` - If true, returns null unless all projected points are on screen.

**Returns:** A `Polygon` representing the projected cube, or null if not all points or off-screen or invalid.

---

```java
Polygon getTileCube(double localX, double localY, int plane, int baseHeight, int cubeHeight, int tileWidth, int tileHeight, boolean fullyOnScreen)
```

Projects a 3D rectangular prism (cube or cuboid) covering multiple tiles to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `localX` - The local X coordinate of the starting tile (tile units).
- `localY` - The local Y coordinate of the starting tile (tile units).
- `plane` - The plane (height level) of the tile.
- `baseHeight` - The base height offset to subtract from the tile's ground height.
- `cubeHeight` - The height of the cube to project (in scene units).
- `tileWidth` - The width of the cube in tiles.
- `tileHeight` - The height of the cube in tiles.
- `fullyOnScreen` - If true, returns null unless all projected points are on screen.

**Returns:** A `Polygon` representing the projected cube, or null if not all points or off-screen or invalid.

### sceneToCanvas
```java
Point sceneToCanvas(int sceneX, int sceneY, int sceneZ)
```

Projects a 3D scene coordinate to 2D canvas coordinates.

**Parameters:**
- `sceneX` - the X coordinate in scene space
- `sceneY` - the Y coordinate in scene space
- `sceneZ` - the Z coordinate (height) in scene space

**Returns:** a `Point` representing the projected 2D canvas coordinates, or `null` if the point is not visible or outside the scene bounds
