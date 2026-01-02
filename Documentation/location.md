# OSMB API - Location & Position

Position and area handling for world navigation

---

## Classes in this Module

- [Area](#area) [class]
- [LocalPosition](#localposition) [class]
- [Location3D](#location3d) [class]
- [PolyArea](#polyarea) [class]
- [Position](#position) [class]
- [RectangleArea](#rectanglearea) [class]
- [WorldPosition](#worldposition) [class]

---

## Area

**Package:** `com.osmb.api.location.area`

**Type:** Class

### Methods

#### `contains(int x, int y, int plane)`

**Returns:** `boolean`

Checks if the area contains the given coordinates and plane.

**Parameters:**
- `x` - the x coordinate
- `y` - the y coordinate
- `plane` - the plane

**Returns:** true if the area contains the coordinates, false otherwise

#### `getClosestPosition(WorldPosition position)`

**Returns:** `WorldPosition`

Finds the closest WorldPosition within the area to the specified position.

**Parameters:**
- `position` - the reference WorldPosition to compare distances to

**Returns:** the closest WorldPosition within the area, or null if the area has no positions

#### `contains(WorldPosition position)`

**Returns:** `boolean`

Checks if the area contains the given position.

**Parameters:**
- `position` - the position to check

**Returns:** true if the area contains the position, false otherwise

#### `getCenter()`

**Returns:** `Point`

Returns the center of the area.

**Returns:** the center point of the area as java.awt.Point

#### `getRandomPosition()`

**Returns:** `WorldPosition`

Returns a random position within the area.

**Returns:** a random WorldPosition within the area

#### `getBounds()`

**Returns:** `Rectangle`

Returns the bounds of the area as a Rectangle.

**Returns:** the bounds of the area as a Rectangle

#### `getPlane()`

**Returns:** `int`

Returns the plane of the area.

#### `getAllWorldPositions()`

**Returns:** `List<WorldPosition>`

#### `distanceTo(WorldPosition position)`

**Returns:** `double`

Calculates the distance from the given WorldPosition to the area. If the position is inside the area, returns 0. Otherwise, returns the distance to the closest position within the area. If the area has no positions, returns Integer.MAX_VALUE.

**Parameters:**
- `position` - the WorldPosition to measure distance from

**Returns:** the distance to the area, or Integer.MAX_VALUE if the area is empty


---

## LocalPosition

**Package:** `com.osmb.api.location.position.types`

**Type:** Class

**Extends/Implements:** extends Position

### Methods

#### `isValid()`

**Returns:** `boolean`

#### `toWorldPosition(ScriptCore scriptCoreService)`

**Returns:** `WorldPosition`


---

## Location3D

**Package:** `com.osmb.api.location`

**Type:** Class

### Methods

#### `getWorldX()`

**Returns:** `int`

#### `getWorldY()`

**Returns:** `int`

#### `getPlane()`

**Returns:** `int`

#### `getLocalX()`

**Returns:** `int`

#### `getLocalY()`

**Returns:** `int`

#### `getSceneX()`

**Returns:** `int`

#### `getSceneY()`

**Returns:** `int`

#### `getSceneZ()`

**Returns:** `int`

#### `getWorldPosition()`

**Returns:** `WorldPosition`

#### `getLocalPosition()`

**Returns:** `LocalPosition`


---

## PolyArea

**Package:** `com.osmb.api.location.area.impl`

**Type:** Class

**Extends/Implements:** extends Object implements Area

### Methods

#### `add(WorldPosition position)`

#### `getBounds()`

**Returns:** `Rectangle`

Description copied from interface: Area

**Returns:** the bounds of the area as a Rectangle

#### `getPlane()`

**Returns:** `int`

Description copied from interface: Area

#### `contains(WorldPosition position)`

**Returns:** `boolean`

Description copied from interface: Area

**Parameters:**
- `position` - the position to check

**Returns:** true if the area contains the position, false otherwise

#### `contains(int x, int y, int plane)`

**Returns:** `boolean`

Description copied from interface: Area

**Parameters:**
- `x` - the x coordinate
- `y` - the y coordinate
- `plane` - the plane

**Returns:** true if the area contains the coordinates, false otherwise

#### `getCenter()`

**Returns:** `Point`

Description copied from interface: Area

**Returns:** the center point of the area as java.awt.Point

#### `getRandomPosition()`

**Returns:** `WorldPosition`

Description copied from interface: Area

**Returns:** a random WorldPosition within the area


---

## Position

**Package:** `com.osmb.api.location.position`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `isWhole()`

**Returns:** `boolean`

Checks if both preciseX and preciseY are whole numbers (no decimal places). This is useful to determine if we are between two tiles.

**Returns:** true if both preciseX and preciseY are integers, false otherwise.

#### `getX()`

**Returns:** `int`

#### `getY()`

**Returns:** `int`

#### `getPlane()`

**Returns:** `int`

#### `distanceTo(int x, int y)`

**Returns:** `double`

#### `distanceTo(Position position)`

**Returns:** `double`

#### `toString()`

**Returns:** `String`

#### `equals(Object o)`

**Returns:** `boolean`

#### `equalsPrecisely(Object o)`

**Returns:** `boolean`

#### `equalsIgnorePlane(Position position)`

**Returns:** `boolean`

#### `step(int num, Direction direction)`

**Returns:** `LocalPosition`

Creates a new position num steps from this position in the given direction.

**Parameters:**
- `num` - The number of steps to make.
- `direction` - The direction to make steps in.

**Returns:** A new Position that is num steps in direction ahead of this one.

#### `getSurroundingTiles(boolean includeDiagonals)`

**Returns:** `List<LocalPosition>`

Gets all surrounding tiles/positions adjacent to this position.

**Parameters:**
- `includeDiagonals` - Whether to include diagonal directions (NW, NE, SW, SE)

**Returns:** List of all adjacent positions

#### `angleTo(Position other)`

**Returns:** `double`

Calculates the angle in degrees from this position to another position.

**Parameters:**
- `other` - The target position to compare with.

**Returns:** The angle in degrees (0Â° = East, 90Â° = North, 180Â° = West, 270Â° = South).

#### `getPreciseX()`

**Returns:** `double`

#### `getPreciseY()`

**Returns:** `double`

#### `hashCode()`

**Returns:** `int`


---

## RectangleArea

**Package:** `com.osmb.api.location.area.impl`

**Type:** Class

**Extends/Implements:** extends Object implements Area

### Methods

#### `getHeight()`

**Returns:** `int`

#### `getX()`

**Returns:** `int`

#### `getY()`

**Returns:** `int`

#### `getWidth()`

**Returns:** `int`

#### `getPlane()`

**Returns:** `int`

Description copied from interface: Area

#### `contains(int x, int y, int plane)`

**Returns:** `boolean`

Description copied from interface: Area

**Parameters:**
- `x` - the x coordinate
- `y` - the y coordinate
- `plane` - the plane

**Returns:** true if the area contains the coordinates, false otherwise

#### `getRandomPosition()`

**Returns:** `WorldPosition`

Description copied from interface: Area

**Returns:** a random WorldPosition within the area

#### `contains(WorldPosition position)`

**Returns:** `boolean`

Description copied from interface: Area

**Parameters:**
- `position` - the position to check

**Returns:** true if the area contains the position, false otherwise

#### `getCenter()`

**Returns:** `Point`

Description copied from interface: Area

**Returns:** the center point of the area as java.awt.Point

#### `getBounds()`

**Returns:** `Rectangle`

Description copied from interface: Area

**Returns:** the bounds of the area as a Rectangle

#### `getSurroundingPositions(int radius)`

**Returns:** `List<WorldPosition>`

#### `getEdgeTiles(Direction direction)`

**Returns:** `List<WorldPosition>`

Returns the edge tiles of the rectangle area for a given direction.

**Parameters:**
- `direction` - The direction: "NORTH", "SOUTH", "EAST", or "WEST"

**Returns:** List of WorldPosition representing the edge tiles in the given direction

#### `distanceTo(WorldPosition position)`

**Returns:** `double`

Description copied from interface: Area

**Parameters:**
- `position` - the WorldPosition to measure distance from

**Returns:** the distance to the area, or Integer.MAX_VALUE if the area is empty

#### `getBasePosition()`

**Returns:** `WorldPosition`

Gets the base position (south-left corner) of the rectangle area.

**Returns:** the WorldPosition representing the base position of the rectangle area


---

## WorldPosition

**Package:** `com.osmb.api.location.position.types`

**Type:** Class

**Extends/Implements:** extends Position

### Methods

#### `toLocalPosition(ScriptCore scriptCoreService)`

**Returns:** `LocalPosition`

#### `createLine(double angle, int lineLength)`

**Returns:** `List<WorldPosition>`

Generates a straight line of WorldPosition objects starting from this position in the given direction (angle in degrees) and of the specified length.

**Parameters:**
- `angle` - The angle in degrees (0Â° = East, 90Â° = North, etc.).
- `lineLength` - The length of the line to draw.

**Returns:** A list of WorldPosition objects representing the line.

#### `getRegionID()`

**Returns:** `int`

#### `getClosest(List<WorldPosition> positions)`

**Returns:** `WorldPosition`

Finds the closest WorldPosition from the given list to this position.

**Parameters:**
- `positions` - the list of WorldPosition objects to compare

**Returns:** the closest WorldPosition, or null if the list is empty

#### `toString()`

**Returns:** `String`


---

