# OSMB API - Scene & Objects

Scene management, NPCs, game objects, and ground items

---

## Classes in this Module

- [Class ObjectType](#class-objecttype) [class]
- [CollisionMap](#collisionmap) [class]
- [Drawable](#drawable) [class]
- [ObjectIdentifier](#objectidentifier) [class]
- [ObjectManager](#objectmanager) [class]
- [RSObject](#rsobject) [class]
- [RSTile](#rstile) [class]
- [SceneManager](#scenemanager) [class]
- [SceneObjectDefinition](#sceneobjectdefinition) [class]

---

## Class ObjectType

**Package:** `com.osmb.api.scene`

**Type:** Class

**Extends/Implements:** extends Enum<ObjectType>

### Methods

#### `values()`

**Returns:** `ObjectType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `ObjectType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `forId(int id)`

**Returns:** `ObjectType`

#### `getId()`

**Returns:** `int`

#### `isWall(ObjectType type)`

**Returns:** `boolean`

#### `isGroundDecor(ObjectType type)`

**Returns:** `boolean`

#### `isWallDecor(ObjectType type)`

**Returns:** `boolean`

#### `isCenterpiece(ObjectType type)`

**Returns:** `boolean`


---

## CollisionMap

**Package:** `com.osmb.api.scene`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public static final int FLAG_BLOCK_ENTITY_NW`
- `public static final int FLAG_BLOCK_ENTITY_N`
- `public static final int FLAG_BLOCK_ENTITY_NE`
- `public static final int FLAG_BLOCK_ENTITY_E`
- `public static final int FLAG_BLOCK_ENTITY_SE`
- `public static final int FLAG_BLOCK_ENTITY_S`
- `public static final int FLAG_BLOCK_ENTITY_SW`
- `public static final int FLAG_BLOCK_ENTITY_W`
- `public static final int FLAG_BLOCK_ENTITY`
- `public static final int FLAG_BLOCK_PROJECTILE_NW`
- `public static final int FLAG_BLOCK_PROJECTILE_N`
- `public static final int FLAG_BLOCK_PROJECTILE_NE`
- `public static final int FLAG_BLOCK_PROJECTILE_E`
- `public static final int FLAG_BLOCK_PROJECTILE_SE`
- `public static final int FLAG_BLOCK_PROJECTILE_S`
- `public static final int FLAG_BLOCK_PROJECTILE_SW`
- `public static final int FLAG_BLOCK_PROJECTILE_W`
- `public static final int FLAG_BLOCK_PROJECTILE`
- `public static final int FLAG_UNINITIALIZED`
- `public static final int FLAG_CLOSED`
- `public static final int WEST_INTERACTION_MASK`
- `public static final int SOUTH_INTERACTION_MASK`
- `public static final int EAST_INTERACTION_MASK`
- `public static final int NORTH_INTERACTION_MASK`
- `public final int[][] flags`

### Methods

#### `main(String[] args)`

#### `reset()`

#### `addWall(int x, int y, int type, int rotation, boolean projectiles)`

#### `add(boolean blocksProjectiles, int sizeX, int sizeZ, int x, int y, int rotation)`

#### `addSolid(int x, int y)`

#### `add(int x, int y, int flags)`

#### `remove(int x, int y, int rotation, int type, boolean projectiles)`

#### `remove(int x, int y, int width, int height, int rotation, boolean projectiles)`

#### `removeSolid(int x, int y)`

#### `reachedDestination(int sx, int sz, int dx, int dz, int rotation, int type)`

**Returns:** `boolean`

#### `reachedWall(int sx, int sz, int dx, int dz, int type, int rotation)`

**Returns:** `boolean`

#### `reachedLoc(int srcX, int srcY, int dstX, int dstY, int dstSizeX, int dstSizeZ, int interactionSides)`

**Returns:** `boolean`


---

## Drawable

**Package:** `com.osmb.api.scene`

**Type:** Class

### Methods

#### `getConvexHull()`

**Returns:** `Polygon`

#### `getFaces()`

**Returns:** `List<Triangle>`


---

## ObjectIdentifier

**Package:** `com.osmb.api.scene`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getPosition()`

**Returns:** `WorldPosition`

#### `getName()`

**Returns:** `String`

#### `getId()`

**Returns:** `int`


---

## ObjectManager

**Package:** `com.osmb.api.scene`

**Type:** Class

### Methods

#### `getObjects()`

**Returns:** `Map<Integer,List<RSObject>>`

#### `getObjects(int plane)`

**Returns:** `List<RSObject>`

#### `getObject(Predicate<RSObject> objectFilter)`

**Returns:** `Optional<RSObject>`

#### `getRSObject(Predicate<RSObject> objectFilter)`

**Returns:** `RSObject`

#### `getObjects(Predicate<RSObject> objectFilter)`

**Returns:** `List<RSObject>`

#### `getClosestObject(WorldPosition worldPosition, String... names)`

**Returns:** `RSObject`

#### `removeObject(String name, WorldPosition worldPosition)`


---

## RSObject

**Package:** `com.osmb.api.scene`

**Type:** Class

Represents an in-game object that can be interacted with, drawn, or navigated to. Extends Location3D for positional data and Drawable for rendering capabilities.

**Extends/Implements:** extends Location3D, Drawable

**Interfaces:** Drawable, Location3D

### Methods

#### `interact(String objectNameOverride, VisualVerifier visualVerifier, String... menuItemNames)`

**Returns:** `boolean`

Interacts with the object by matching its name and menu actions.

**Parameters:**
- `objectNameOverride` - Overrides the object's default name for menu matching (case-insensitive).
- `visualVerifier` - Optional verifier to validate the object visually before interaction.
- `menuItemNames` - Actions to match (e.g., "Open", "Use"). The first matching action is clicked.

**Returns:** true if interaction succeeded, false otherwise.

#### `interact(String objectNameOverride, VisualVerifier visualVerifier, BooleanSupplier breakCondition, String... menuItemNames)`

**Returns:** `boolean`

Interacts with the object by matching its name and menu actions.

**Parameters:**
- `objectNameOverride` - Overrides the object's default name for menu matching (case-insensitive).
- `visualVerifier` - Optional verifier to validate the object visually before interaction.
- `breakCondition` - Condition to interrupt the interaction.
- `menuItemNames` - Actions to match (e.g., "Open", "Use"). The first matching action is clicked.

**Returns:** true if interaction succeeded, false otherwise.

#### `interactableFrom(Direction direction)`

**Returns:** `boolean`

Checks if the object can be interacted with from a given direction.

**Parameters:**
- `direction` - The direction to check (e.g., NORTH, EAST).

**Returns:** true if the object is interactable from the direction, false otherwise.

**Throws:**
- IllegalArgumentException - If a diagonal direction is provided.

#### `getInteractionSideFlags()`

**Returns:** `int`

Gets the interaction flags for each side of the object (e.g., blocked sides).

**Returns:** Bitmask of interaction flags (see NORTH_INTERACTION_MASK etc.).

#### `getObjectType()`

**Returns:** `ObjectType`

Gets the type of the object (e.g., WALL, GROUND_ITEM).

#### `getSceneEntity()`

**Returns:** `Object`

Gets the underlying scene entity (engine-specific representation).

#### `getName()`

**Returns:** `String`

Gets the object's in-game name.

#### `getId()`

**Returns:** `int`

Gets the object's unique ID.

#### `getRotation()`

**Returns:** `int`

Gets the object's rotation (0-2047, where 0 is South).

#### `getActions()`

**Returns:** `String[]`

Gets the available actions for the object (e.g., ["Open", "Take"]).

#### `getModel()`

**Returns:** `Object`

Gets the object's 3D model.

#### `getSecondaryModel()`

**Returns:** `Object`

Gets the secondary model (e.g., for animated objects).

#### `isBlocksProjectiles()`

**Returns:** `boolean`

Checks if the object blocks projectiles (e.g., walls).

#### `isInteractable()`

**Returns:** `boolean`

Checks if the object is interactable (e.g., not decorative).

#### `isSolid()`

**Returns:** `boolean`

#### `getMaxWorldX()`

**Returns:** `int`

Gets the maximum X coordinate (world space) for multi-tile objects. Defaults to the object's base X coordinate for single-tile objects.

#### `getMaxWorldY()`

**Returns:** `int`

Gets the maximum Y coordinate (world space) for multi-tile objects. Defaults to the object's base Y coordinate for single-tile objects.

#### `getMaxLocalX()`

**Returns:** `int`

Gets the maximum X coordinate (local space) for multi-tile objects. Defaults to the object's base local X coordinate for single-tile objects.

#### `getMaxLocalY()`

**Returns:** `int`

Gets the maximum Y coordinate (local space) for multi-tile objects. Defaults to the object's base local Y coordinate for single-tile objects.

#### `getYaw()`

**Returns:** `int`

Synonym for getRotation().

#### `getSequenceId()`

**Returns:** `int`

Gets the animation ID. Defaults to -1 (no animation).

#### `getSequenceFrame()`

**Returns:** `int`

Gets the current animation frame. Defaults to -1 (no animation).

#### `getTileWidth()`

**Returns:** `int`

Gets the width of the object in tiles. Defaults to 1.

#### `getTileHeight()`

**Returns:** `int`

Gets the height of the object in tiles. Defaults to 1.

#### `interact(String... menuItemNames)`

**Returns:** `boolean`

Interacts with the object without using visual verification.

**Parameters:**
- `menuItemNames` - Actions to match (e.g., "Open", "Use").

**Returns:** true if interaction succeeded.

#### `interact(BooleanSupplier breakCondition, String... menuItemNames)`

**Returns:** `boolean`

Interacts with the object without using visual verification.

**Parameters:**
- `menuItemNames` - Actions to match (e.g., "Open", "Use").
- `breakCondition` - Condition to interrupt the interaction.

**Returns:** true if interaction succeeded.

#### `interact(VisualVerifier visualVerifier, String... menuItemNames)`

**Returns:** `boolean`

Interacts with the object using visual verification.

**Parameters:**
- `visualVerifier` - Verifies the object visually before interaction.
- `menuItemNames` - Actions to match.

**Returns:** true if interaction succeeded.

#### `interact(String objectNameOverride, String[] menuItemNames)`

**Returns:** `boolean`

Interacts with the object using a name override (e.g., for dynamically named objects).

**Parameters:**
- `objectNameOverride` - Overrides the object's default name for menu matching.
- `menuItemNames` - Actions to match.

**Returns:** true if interaction succeeded.

#### `isInteractableOnScreen()`

**Returns:** `boolean`

Checks if the object is visible and interactable on the game screen.

#### `interact(MenuHook menuHook)`

**Returns:** `boolean`

#### `interact(MenuHook menuHook, BooleanSupplier breakCondition)`

**Returns:** `boolean`

#### `interact(VisualVerifier visualVerifier, MenuHook menuHook)`

**Returns:** `boolean`

#### `interact(VisualVerifier visualVerifier, MenuHook menuHook, BooleanSupplier breakCondition)`

**Returns:** `boolean`

#### `interact(VisualVerifier visualVerifier, int interactDistance, MenuHook menuHook)`

**Returns:** `boolean`

#### `interact(VisualVerifier visualVerifier, int interactDistance, MenuHook menuHook, BooleanSupplier breakCondition)`

**Returns:** `boolean`

#### `getSurroundingPositions()`

**Returns:** `List<LocalPosition>`

#### `getTile()`

**Returns:** `RSTile`

Gets the tile the object occupies.

#### `getLocalX()`

**Returns:** `int`

#### `getLocalY()`

**Returns:** `int`

#### `canReach()`

**Returns:** `boolean`

Checks if the object is reachable within the default distance (1 tile). Performance Warning: This uses an A* pathfinding algorithm which is computationally expensive. Avoid calling this in loops or performance-critical code paths.

**Returns:** true if a valid path exists within 1 tile distance, false otherwise.

#### `getTileDistance(WorldPosition worldPosition)`

**Returns:** `int`

Gets the pathfinding distance to the object (in tiles) using A* pathfinding. Performance Warning: This is an expensive operation that calculates a full path. Never use this in loops or frequent polling scenarios as it may cause significant performance degradation.

**Returns:** The number of tiles in the shortest path, or 0 if unreachable.

#### `canReach(int interactDistance)`

**Returns:** `boolean`

Checks if the object is reachable within the default distance (1 tile). Performance Warning: This uses an A* pathfinding algorithm which is computationally expensive. Avoid calling this in loops or performance-critical code paths.

**Parameters:**
- `interactDistance` - The distance away from an objects tile, before we can interact.

**Returns:** true if a valid path exists within 1 tile distance, false otherwise.

#### `getObjectArea()`

**Returns:** `RectangleArea`

#### `distance(WorldPosition worldPosition)`

**Returns:** `double`

Gets the Euclidean distance to the object

#### `getSurroundingPositions(int radius)`

**Returns:** `List<LocalPosition>`


---

## RSTile

**Package:** `com.osmb.api.scene`

**Type:** Class

**Extends/Implements:** extends Location3D

**Interfaces:** Location3D

### Methods

#### `getCollisionFlag()`

**Returns:** `int`

#### `getHeight()`

**Returns:** `int`

#### `isOnMinimap()`

**Returns:** `boolean`

#### `isOnGameScreen()`

**Returns:** `boolean`

#### `isInsideScene()`

**Returns:** `boolean`

#### `getTilePoly()`

**Returns:** `Polygon`

#### `getTileCube(int height)`

**Returns:** `Polygon`

#### `getTileCube(int bottomHeight, int height)`

**Returns:** `Polygon`

#### `canReach()`

**Returns:** `boolean`

#### `distance(WorldPosition position)`

**Returns:** `double`

#### `isOnGameScreen(Class<? extends Component>... componentsToSkip)`

**Returns:** `boolean`

#### `interact(String menuItemName)`

**Returns:** `boolean`

#### `getObjects()`

**Returns:** `List<RSObject>`

#### `getBridgeTile()`

**Returns:** `RSTile`


---

## SceneManager

**Package:** `com.osmb.api.scene`

**Type:** Class

### Fields

- `static final int SCENE_TILE_SIZE`

### Methods

#### `getUUID()`

**Returns:** `UUID`

#### `getTiles()`

**Returns:** `RSTile[][][]`

#### `getLevelCollisionMap()`

**Returns:** `CollisionMap[]`

#### `getLevelCollisionMap(int plane)`

**Returns:** `CollisionMap`

#### `getLevelTileFlags()`

**Returns:** `byte[][][]`

#### `getLevelHeightmap()`

**Returns:** `int[][][]`

#### `getSceneBaseTileX()`

**Returns:** `int`

#### `getSceneBaseTileY()`

**Returns:** `int`

#### `getSceneEndTileX()`

**Returns:** `int`

#### `getSceneEndTileY()`

**Returns:** `int`

#### `getSceneCenterZoneX()`

**Returns:** `int`

#### `getSceneCenterZoneY()`

**Returns:** `int`

#### `getObjectsToAdd()`

**Returns:** `List<SceneObjectDefinition>`

#### `getObjectsToRemove()`

**Returns:** `List<Object>`

#### `refresh()`

#### `getTileCollisionFlag(Position pos)`

**Returns:** `int`

#### `getTileSettingFlag(Position pos)`

**Returns:** `int`

#### `getTile(Position pos)`

**Returns:** `RSTile`

#### `inScene(int x, int y)`

**Returns:** `boolean`


---

## SceneObjectDefinition

**Package:** `com.osmb.api.scene`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getPlane()`

**Returns:** `int`

#### `getId()`

**Returns:** `int`

#### `getType()`

**Returns:** `ObjectType`

#### `getRotation()`

**Returns:** `int`

#### `setRotation(int rotation)`

#### `getX()`

**Returns:** `int`

#### `getY()`

**Returns:** `int`

#### `toString()`

**Returns:** `String`


---

