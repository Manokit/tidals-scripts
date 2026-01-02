# OSMB API - Walking & Pathfinding

Web walking and pathfinding systems

---

## Classes in this Module

- [AStarPathFinder](#astarpathfinder) [class]
- [BFSPathFinder](#bfspathfinder) [class]
- [Class Direction](#class-direction) [class]
- [CollisionFlags](#collisionflags) [class]
- [CollisionManager](#collisionmanager) [class]
- [Node](#node) [class]
- [PathUtils](#pathutils) [class]
- [WalkConfig](#walkconfig) [class]
- [WalkConfig.Builder](#walkconfig.builder) [class]
- [Walker](#walker) [class]

---

## AStarPathFinder

**Package:** `com.osmb.api.walker.pathing.pathfinding.astar`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `find(LocalPosition origin, LocalPosition target, boolean tryNearest)`

**Returns:** `Deque<WorldPosition>`

Finds the shortest path from the given origin to the target position. If the origin is already at the target, an empty path is returned. If no valid path can be found and tryNearest is set to true, the closest navigable tile is selected as the destination. If no path is possible, the method returns null.

**Parameters:**
- `origin` - the starting position for the path search
- `target` - the target position to navigate toward
- `tryNearest` - whether to attempt routing to the nearest reachable point if the exact target is unreachable

**Returns:** a deque of WorldPosition objects representing the path to the destination, an empty deque if already at the destination, or null if no path is possible

#### `find(LocalPosition origin, LocalPosition target, boolean tryNearest, Set<LocalPosition> avoid)`

**Returns:** `Deque<WorldPosition>`

Finds the shortest path from the given origin to the target position, optionally avoiding specified positions. If the origin is already at the target, an empty path is returned. If no valid path can be found and tryNearest is set to true, the closest navigable tile is selected as the destination. If no path is possible, the method returns null.

**Parameters:**
- `origin` - the starting position for the path search
- `target` - the target position to navigate toward
- `tryNearest` - whether to attempt routing to the nearest reachable point if the exact target is unreachable
- `avoid` - a set of positions to avoid during pathfinding (may be null)

**Returns:** a deque of WorldPosition objects representing the path to the destination, an empty deque if already at the destination, or null if no path is possible

#### `find(LocalPosition origin, RSObject object, int interactDistance, boolean tryNearest)`

**Returns:** `Deque<WorldPosition>`

Finds the shortest path from the given origin to a target RSObject, considering interaction distance. This method determines all possible tiles from which the object can be interacted with, then finds a path to the closest such tile. If no valid target tiles are found, it returns null.

**Parameters:**
- `origin` - the starting position for the path search
- `object` - the target RSObject to interact with
- `interactDistance` - the maximum distance from which the object can be interacted with
- `tryNearest` - whether to attempt routing to the nearest reachable point if the exact target is unreachable

**Returns:** a deque of WorldPosition objects representing the path to the destination, or null if no path is possible

#### `find(LocalPosition origin, List<LocalPosition> targets, boolean tryNearest)`

**Returns:** `Deque<WorldPosition>`

Finds the shortest path from a given origin to one of the specified target locations. If the origin is already on a target tile, an empty path is returned. If no valid path can be found and tryNearest is set to true, the closest navigable tile is selected as the destination. If no path is possible, the method returns null.

**Parameters:**
- `origin` - the starting position for the path search
- `targets` - a list of target positions to navigate toward
- `tryNearest` - whether to attempt routing to the nearest reachable point if the exact targets are unreachable

**Returns:** a deque of WorldPosition objects representing the path to the destination, an empty deque if already at the destination, or null if no path is possible


---

## BFSPathFinder

**Package:** `com.osmb.api.walker.pathing.pathfinding.bfs`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `find(LocalPosition currentPosition, RSObject object, boolean tryNearest)`

**Returns:** `Deque<LocalPosition>`


---

## Class Direction

**Package:** `com.osmb.api.walker.pathing`

**Type:** Class

**Extends/Implements:** extends Enum<Direction>

### Methods

#### `values()`

**Returns:** `Direction[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `Direction`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `fromRotation(int rotation)`

**Returns:** `Direction`

#### `diagonalComponents(Direction direction)`

**Returns:** `Direction[]`

Get the 2 directions which make up a diagonal direction (i.e., NORTH and EAST for NORTH_EAST).

**Parameters:**
- `direction` - The direction to get the components for.

**Returns:** The components for the given direction.

#### `fromDeltas(int deltaX, int deltaY)`

**Returns:** `Direction`

Creates a direction from the differences between X and Y.

**Parameters:**
- `deltaX` - The difference between two X coordinates.
- `deltaY` - The difference between two Y coordinates.

**Returns:** The direction.

#### `between(Position current, Position next)`

**Returns:** `Direction`

Gets the Direction between the two Positions..

**Parameters:**
- `current` - The difference between two X coordinates.
- `next` - The difference between two Y coordinates.

**Returns:** The direction.

#### `getShortName()`

**Returns:** `String`

#### `deltaX()`

**Returns:** `int`

Gets the X delta from a Position of (0, 0).

**Returns:** The delta of X from (0, 0).

#### `deltaY()`

**Returns:** `int`

Gets the Y delta from a Position of (0, 0).

**Returns:** The delta of Y from (0, 0).

#### `isDiagonal()`

**Returns:** `boolean`

Check if this direction is a diagonal direction.

**Returns:** true if this direction is a diagonal direction, false otherwise.

#### `isValidDirection(int x, int y, int[][] collisionData)`

**Returns:** `boolean`

#### `opposite()`

**Returns:** `Direction`

Get the opposite direction.

**Returns:** The opposite direction.


---

## CollisionFlags

**Package:** `com.osmb.api.walker.pathing`

**Type:** Class

This class defines various collision flags for pathfinding and movement, represented as bit flags. Each flag corresponds to a specific binary bit in a 32-bit integer. These flags are used to determine the accessibility and status of tiles in the game world.

**Extends/Implements:** extends Object

### Fields

- `public static final int OPEN` - OPEN: Tile is open (no collision). Binary: 00000000 00000000 00000000 00000000
- `public static final int OCCUPIED` - OCCUPIED: Tile is occupied by an object. Binary: 00000000 00000000 00000001 00000000
- `public static final int SOLID` - SOLID: Tile is solid and cannot be walked on. Binary: 00000000 00000010 00000000 00000000
- `public static final int BLOCKED` - BLOCKED: Tile is blocked. Binary: 00000000 00100000 00000000 00000000
- `public static final int BLOCKED_FLOOR_DECORATION` - BLOCKED_FLOOR_DECORATION: Tile is blocked by a floor decoration. Binary: 00000000 00000100 00000000 00000000
- `public static final int CLOSED` - CLOSED: Tile is completely closed. Binary: 11111111 11111111 11111111 11111111
- `public static final int INITIALIZED` - INITIALIZED: Tile has been initialized. Binary: 00000001 00000000 00000000 00000000
- `public static final int NORTH` - NORTH: Blocked to the north. Binary: 00000000 00000000 00000000 00000010
- `public static final int EAST` - EAST: Blocked to the east. Binary: 00000000 00000000 00000000 00001000
- `public static final int SOUTH` - SOUTH: Blocked to the south. Binary: 00000000 00000000 00000000 00100000
- `public static final int WEST` - WEST: Blocked to the west. Binary: 00000000 00000000 00000000 10000000
- `public static final int NORTHWEST` - NORTHWEST: Blocked to the northwest. Binary: 00000000 00000000 00000000 00000001
- `public static final int NORTHEAST` - NORTHEAST: Blocked to the northeast. Binary: 00000000 00000000 00000000 00000100
- `public static final int SOUTHEAST` - SOUTHEAST: Blocked to the southeast. Binary: 00000000 00000000 00000000 00010000
- `public static final int SOUTHWEST` - SOUTHWEST: Blocked to the southwest. Binary: 00000000 00000000 00000000 01000000
- `public static final int EAST_NORTH` - EAST_NORTH: Blocked to both the east and north. Binary: 00000000 00000000 00000000 00001010
- `public static final int EAST_SOUTH` - EAST_SOUTH: Blocked to both the east and south. Binary: 00000000 00000000 00000000 00101000
- `public static final int WEST_SOUTH` - WEST_SOUTH: Blocked to both the west and south. Binary: 00000000 00000000 00000000 10100000
- `public static final int WEST_NORTH` - WEST_NORTH: Blocked to both the west and north. Binary: 00000000 00000000 00000000 10000010
- `public static final int BLOCKED_NORTH_WALL` - BLOCKED_NORTH_WALL: Blocked by a wall to the north. Binary: 00000000 00000000 00000100 00000000
- `public static final int BLOCKED_EAST_WALL` - BLOCKED_EAST_WALL: Blocked by a wall to the east. Binary: 00000000 00000000 00010000 00000000
- `public static final int BLOCKED_SOUTH_WALL` - BLOCKED_SOUTH_WALL: Blocked by a wall to the south. Binary: 00000000 00000000 01000000 00000000
- `public static final int BLOCKED_WEST_WALL` - BLOCKED_WEST_WALL: Blocked by a wall to the west. Binary: 00000000 00000001 00000000 00000000
- `public static final int BLOCKED_NORTHEAST` - BLOCKED_NORTHEAST: Blocked by a wall to the northeast. Binary: 00000000 00000000 00001000 00000000
- `public static final int BLOCKED_SOUTHEAST` - BLOCKED_SOUTHEAST: Blocked by a wall to the southeast. Binary: 00000000 00000000 00100000 00000000
- `public static final int BLOCKED_NORTHWEST` - BLOCKED_NORTHWEST: Blocked by a wall to the northwest. Binary: 00000000 00000000 00000010 00000000
- `public static final int BLOCKED_SOUTHWEST` - BLOCKED_SOUTHWEST: Blocked by a wall to the southwest. Binary: 00000000 00000000 10000000 00000000
- `public static final int BLOCKED_EAST_NORTH` - BLOCKED_EAST_NORTH: Blocked by walls to both the east and north. Binary: 00000000 00000000 00010100 00000000
- `public static final int BLOCKED_EAST_SOUTH` - BLOCKED_EAST_SOUTH: Blocked by walls to both the east and south. Binary: 00000000 00000000 01010000 00000000
- `public static final int BLOCKED_WEST_SOUTH` - BLOCKED_WEST_SOUTH: Blocked by walls to both the west and south. Binary: 00000000 00000001 01000000 00000000
- `public static final int BLOCKED_WEST_NORTH` - BLOCKED_WEST_NORTH: Blocked by walls to both the west and north. Binary: 00000000 00000001 00000100 00000000

### Methods

#### `blockedNorth(int collisionData)`

**Returns:** `boolean`

Checks if the north side of the tile is blocked.

**Parameters:**
- `collisionData` - Collision data of the tile.

**Returns:** True if blocked north, false otherwise.

#### `blockedEast(int collisionData)`

**Returns:** `boolean`

Checks if the east side of the tile is blocked.

**Parameters:**
- `collisionData` - Collision data of the tile.

**Returns:** True if blocked east, false otherwise.

#### `blockedSouth(int collisionData)`

**Returns:** `boolean`

Checks if the south side of the tile is blocked.

**Parameters:**
- `collisionData` - Collision data of the tile.

**Returns:** True if blocked south, false otherwise.

#### `blockedWest(int collisionData)`

**Returns:** `boolean`

Checks if the west side of the tile is blocked.

**Parameters:**
- `collisionData` - Collision data of the tile.

**Returns:** True if blocked west, false otherwise.

#### `isWalkable(int collisionData)`

**Returns:** `boolean`

Checks if the tile is walkable (not occupied, solid, blocked, etc.).

**Parameters:**
- `collisionData` - Collision data of the tile.

**Returns:** True if walkable, false otherwise.

#### `isInitialized(int collisionData)`

**Returns:** `boolean`

Checks if the tile has been initialized.

**Parameters:**
- `collisionData` - Collision data of the tile.

**Returns:** True if initialized, false otherwise.

#### `checkFlag(int flag, int checkFlag)`

**Returns:** `boolean`

Utility method to check if a specific flag is set in the given collision data.

**Parameters:**
- `flag` - The flag to check.
- `checkFlag` - The specific flag to check against.

**Returns:** True if the flag is set, false otherwise.


---

## CollisionManager

**Package:** `com.osmb.api.walker.pathing`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getObjectInteractTiles(ScriptCore core, RSObject object, int interactDistance)`

**Returns:** `List<LocalPosition>`

#### `getAreaInteractTiles(ScriptCore core, RectangleArea area, int interactDistance)`

**Returns:** `List<WorldPosition>`

#### `isObstructed(Direction direction, int collisionData, com.osmb.api.walker.pathing.CollisionManager.ObjType objType)`

**Returns:** `boolean`

#### `isBlocked(Direction direction, int collisionData)`

**Returns:** `boolean`

#### `isAdjacent(Position current, Position adjacent)`

**Returns:** `boolean`

Checks if two positions are adjacent (including diagonally).

**Parameters:**
- `current` - the current position
- `adjacent` - the position to check for adjacency

**Returns:** true if the positions are adjacent, false otherwise

#### `isCardinallyAdjacent(Position current, Position adjacent)`

**Returns:** `boolean`

Checks if two positions are non-diagonally (cardinally) adjacent.

#### `isNeighbourReachable(LocalPosition current, LocalPosition adjacent)`

**Returns:** `boolean`

#### `findWalkableTilesInDirection(LocalPosition origin, Direction direction, int maxSteps)`

**Returns:** `List<LocalPosition>`

Returns a list of walkable tiles from the origin in the specified direction, up to the given maxSteps.

**Parameters:**
- `origin` - The starting tile.
- `direction` - The direction to check.
- `maxSteps` - The maximum number of tiles to check in the direction.

**Returns:** List of walkable LocalPosition tiles in the given direction (excluding the origin).

#### `findReachableTiles(LocalPosition origin, int radius)`

**Returns:** `List<LocalPosition>`


---

## Node

**Package:** `com.osmb.api.walker.pathing.pathfinding`

**Type:** Class

A Node representing a weighted LocalPosition.

**Extends/Implements:** extends Object implements Comparable<Node>

### Methods

#### `close()`

Closes this Node.

#### `compareTo(Node other)`

**Returns:** `int`

#### `equals(Object obj)`

**Returns:** `boolean`

#### `getCost()`

**Returns:** `int`

Gets the cost of this Node.

**Returns:** The cost.

#### `getParent()`

**Returns:** `Node`

Gets the parent Node of this Node.

**Returns:** The parent Node.

**Throws:**
- NoSuchElementException - If this Node does not have a parent.

#### `getPosition()`

**Returns:** `LocalPosition`

Gets the Position this Node represents.

**Returns:** The position.

#### `hashCode()`

**Returns:** `int`

#### `hasParent()`

**Returns:** `boolean`

Returns whether or not this Node has a parent Node.

**Returns:** true if this Node has a parent Node, otherwise false.

#### `isOpen()`

**Returns:** `boolean`

Returns whether or not this Node is open.

**Returns:** true if this Node is open, otherwise false.

#### `setCost(int cost)`

Sets the cost of this Node.

**Parameters:**
- `cost` - The cost.

#### `setParent(Node parent)`

Sets the parent Node of this Node.

**Parameters:**
- `parent` - The parent Node. May be null.

#### `toString()`

**Returns:** `String`


---

## PathUtils

**Package:** `com.osmb.api.walker.pathing`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `smoothPath(List<WorldPosition> path, int smoothingWindow)`

**Returns:** `List<WorldPosition>`

#### `addDetours(List<WorldPosition> path, int maxDetourDistance)`

**Returns:** `List<WorldPosition>`


---

## WalkConfig

**Package:** `com.osmb.api.walker`

**Type:** Class

Configuration class for path walking behavior in the walking API. Provides customizable settings for character movement, timing, and execution control.

**Extends/Implements:** extends Object

### Methods

#### `getRunEnergyMaxThreshold()`

**Returns:** `int`

#### `getRunEnergyMinThreshold()`

**Returns:** `int`

#### `isHandleContainerObstruction()`

**Returns:** `boolean`

**Returns:** Whether to handle container obstructions during walking (e.g., inventory, skills tab)

#### `getBreakCondition()`

**Returns:** `BooleanSupplier`

**Returns:** The break condition supplier that can interrupt path walking

#### `getDoWhileWalking()`

**Returns:** `Supplier<WalkConfig>`

**Returns:** Supplier for dynamic configuration updates during execution

#### `isWalkScreen()`

**Returns:** `boolean`

**Returns:** Whether walking via screen clicks is enabled

#### `isWalkMinimap()`

**Returns:** `boolean`

**Returns:** Whether walking via minimap clicks is enabled

#### `isAllowInterrupt()`

**Returns:** `boolean`

**Returns:** Whether the walking task can be interrupted by break, afk, or hopping tasks

#### `getTileRandomisationRadius()`

**Returns:** `int`

**Returns:** Radius for randomizing target tile positions (in tiles)

#### `isEnableRun()`

**Returns:** `boolean`

**Returns:** Whether run energy should be automatically managed

#### `getTimeout()`

**Returns:** `int`

**Returns:** Maximum time in milliseconds to attempt walking before timing out

#### `getBreakDistance()`

**Returns:** `int`

**Returns:** Distance from destination at which walking is considered complete (in tiles)

#### `getMinimapTapDelayMin()`

**Returns:** `long`

**Returns:** Minimum delay between minimap clicks (in milliseconds)

#### `getMinimapTapDelayMax()`

**Returns:** `long`

**Returns:** Maximum delay between minimap clicks (in milliseconds)

#### `getScreenTapDelayMin()`

**Returns:** `long`

**Returns:** Minimum delay between screen clicks (in milliseconds)

#### `getScreenTapDelayMax()`

**Returns:** `long`

**Returns:** Maximum delay between screen clicks (in milliseconds)

#### `toString()`

**Returns:** `String`


---

## WalkConfig.Builder

**Package:** `com.osmb.api.walker`

**Type:** Class

Builder pattern implementation for creating WalkConfig instances with fluent API.

**Extends/Implements:** extends Object

### Methods

#### `setRunEnergyThreshold(int minThreshold, int maxThreshold)`

**Returns:** `WalkConfig.Builder`

Set the run energy thresholds for automatic run management.

**Parameters:**
- `minThreshold` - Minimum run energy percentage to enable running
- `maxThreshold` - Maximum run energy percentage to enable running

**Returns:** Builder instance for method chaining

#### `enableRun(boolean enable)`

**Returns:** `WalkConfig.Builder`

Enable or disable automatic run energy management.

**Parameters:**
- `enable` - Whether to enable run energy management

**Returns:** Builder instance for method chaining

#### `timeout(int timeout)`

**Returns:** `WalkConfig.Builder`

Set the operation timeout.

**Parameters:**
- `timeout` - Maximum execution time in milliseconds

**Returns:** Builder instance for method chaining

#### `breakDistance(int breakDistance)`

**Returns:** `WalkConfig.Builder`

Set the completion distance threshold.

**Parameters:**
- `breakDistance` - Distance in tiles to consider destination reached

**Returns:** Builder instance for method chaining

#### `minimapTapDelay(long min, long max)`

**Returns:** `WalkConfig.Builder`

Set minimap click delay range.

**Parameters:**
- `min` - Minimum delay in milliseconds
- `max` - Maximum delay in milliseconds

**Returns:** Builder instance for method chaining

#### `screenTapDelay(long min, long max)`

**Returns:** `WalkConfig.Builder`

Set screen click delay range.

**Parameters:**
- `min` - Minimum delay in milliseconds
- `max` - Maximum delay in milliseconds

**Returns:** Builder instance for method chaining

#### `tileRandomisationRadius(int tileRadius)`

**Returns:** `WalkConfig.Builder`

Set tile randomization radius.

**Parameters:**
- `tileRadius` - Radius in tiles for randomizing click positions

**Returns:** Builder instance for method chaining

#### `allowInterrupt(boolean allow)`

**Returns:** `WalkConfig.Builder`

Set whether the walking task can be interrupted by break, afk or hopping tasks.

**Parameters:**
- `allow` - Whether to allow interruption

**Returns:** Builder instance for method chaining

#### `breakCondition(BooleanSupplier breakCondition)`

**Returns:** `WalkConfig.Builder`

Set the break condition supplier.

**Parameters:**
- `breakCondition` - Supplier that returns true to interrupt walking

**Returns:** Builder instance for method chaining

#### `doWhileWalking(Supplier<WalkConfig> doWhileWalking)`

**Returns:** `WalkConfig.Builder`

Supplier to be executed while walking. To update the WalkConfig dynamically, you can return a different WalkConfig & it the configs will be updated (apart from WalkConfig.timeout & WalkConfig.allowInterrupt which is initially set when called). If you don't want to update the current WalkConfig then return null.

**Parameters:**
- `doWhileWalking` - Supplier that provides updated config during execution

**Returns:** Builder instance for method chaining

#### `disableWalkScreen(boolean disable)`

**Returns:** `WalkConfig.Builder`

Enable or disable walking via screen clicks.

**Parameters:**
- `disable` - Whether to disable screen walking

**Returns:** Builder instance for method chaining

#### `disableWalkMinimap(boolean disable)`

**Returns:** `WalkConfig.Builder`

Enable or disable walking via minimap clicks.

**Parameters:**
- `disable` - Whether to disable minimap walking

**Returns:** Builder instance for method chaining

#### `setWalkMethods(boolean screen, boolean minimap)`

**Returns:** `WalkConfig.Builder`

Set both screen and minimap walking options.

**Parameters:**
- `screen` - Whether to enable screen walking
- `minimap` - Whether to enable minimap walking

**Returns:** Builder instance for method chaining

#### `setHandleContainerObstruction(boolean handleContainerObstruction)`

**Returns:** `WalkConfig.Builder`

Set whether to handle container obstructions (e.g., inventory, skills tab) during walking.

**Parameters:**
- `handleContainerObstruction` - Whether to handle container obstructions

**Returns:** Builder instance for method chaining

#### `build()`

**Returns:** `WalkConfig`

Build the WalkConfig instance with current settings.

**Returns:** Configured WalkConfig instance


---

## Walker

**Package:** `com.osmb.api.walker`

**Type:** Class

### Methods

#### `getaStar()`

**Returns:** `AStarPathFinder`

#### `getDefaultSettings()`

**Returns:** `WalkConfig`

#### `walkTo(Position position)`

**Returns:** `boolean`

Walks to the specified coordinates

**Parameters:**
- `position` - - The Position to navigate to. Either LocalPosition or WorldPosition can be passed.

**Returns:** true if reached the destination, false otherwise

#### `walkTo(Position position, WalkConfig config)`

**Returns:** `boolean`

Walks to the specified coordinates

**Parameters:**
- `position` - - The Position to navigate to. Either LocalPosition or WorldPosition can be passed.
- `config` - - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

#### `walkTo(int worldX, int worldY)`

**Returns:** `boolean`

Walks to the specified coordinates

**Parameters:**
- `worldX` - - The world X coordinate to navigate to
- `worldY` - - The world Y coordinate to navigate to

**Returns:** true if reached the destination, false otherwise

#### `walkTo(int worldX, int worldY, WalkConfig config)`

**Returns:** `boolean`

Walks to the specified coordinates

**Parameters:**
- `worldX` - - The world X coordinate to navigate to
- `worldY` - - The world Y coordinate to navigate to
- `config` - - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

#### `walkTo(RSObject object)`

**Returns:** `boolean`

Walks to the specified RSObject

**Parameters:**
- `object` - - The RSObject to navigate to

**Returns:** true if reached the destination, false otherwise

#### `walkTo(RSObject object, WalkConfig walkConfig)`

**Returns:** `boolean`

Walks to the specified RSObject

**Parameters:**
- `object` - - The RSObject to navigate to
- `walkConfig` - - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

#### `walkTo(RSObject object, int interactDistance, WalkConfig walkConfig)`

**Returns:** `boolean`

Walks to the specified RSObject

**Parameters:**
- `object` - - The RSObject to navigate to
- `interactDistance` - - The tile distance from the object to where you can interact from.
- `walkConfig` - - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

#### `walkPath(List<WorldPosition> path)`

**Returns:** `boolean`

#### `walkPath(List<WorldPosition> path, WalkConfig config)`

**Returns:** `boolean`

#### `getCollisionManager()`

**Returns:** `CollisionManager`


---

