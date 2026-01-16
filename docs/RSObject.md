# RSObject

**Type:** Interface

## Methods

| Return Type | Method |
|------------|--------|
| `default boolean` | `canReach()` |
| `boolean` | `canReach(int interactDistance)` |
| `double` | `distance()` |
| `String[]` | `getActions()` |
| `int` | `getId()` |
| `int` | `getInteractionSideFlags()` |
| `int` | `getLocalX()` |
| `int` | `getLocalY()` |
| `default int` | `getMaxLocalX()` |
| `default int` | `getMaxLocalY()` |
| `default int` | `getMaxWorldX()` |
| `default int` | `getMaxWorldY()` |
| `Object` | `getModel()` |
| `String` | `getName()` |
| `Rectangle` | `getObjectArea()` |
| `ObjectType` | `getObjectType()` |
| `int` | `getRotation()` |
| `Object` | `getSceneEntity()` |
| `Object` | `getSecondaryModel()` |
| `default int` | `getSequenceFrame()` |
| `default int` | `getSequenceId()` |
| `List<WorldPosition>` | `getSurroundingPositions()` |
| `List<WorldPosition>` | `getSurroundingPositions(int)` |
| `Tile` | `getTile()` |
| `int` | `getTileDistance()` |
| `default int` | `getTileHeight()` |
| `default int` | `getTileWidth()` |
| `default int` | `getYaw()` |
| `default boolean` | `interact(String... menuItemNames)` |
| `default boolean` | `interact(BooleanSupplier breakCondition, String... menuItemNames)` |
| `default boolean` | `interact(VisualVerifier visualVerifier, String... menuItemNames)` |
| `default boolean` | `interact(String objectNameOverride, String[] menuItemNames)` |
| `default boolean` | `interact(String objectNameOverride, VisualVerifier visualVerifier, String... menuItemNames)` |
| `default boolean` | `interact(String objectNameOverride, VisualVerifier visualVerifier, BooleanSupplier breakCondition, String... menuItemNames)` |
| `boolean` | `interactableFrom(Direction direction)` |
| `boolean` | `isBlocksProjectiles()` |
| `boolean` | `isInteractable()` |
| `boolean` | `isInteractableOnScreen()` |
| `boolean` | `isSolid()` |

## Method Details

### interact
```java
default boolean interact(String objectNameOverride, VisualVerifier visualVerifier, String... menuItemNames)
```

Interacts with the object by matching its name and menu actions.

**Parameters:**
- `objectNameOverride` - Overrides the object's default name for menu matching (case-insensitive).
- `visualVerifier` - Optional verifier to validate the object visually before interaction.
- `menuItemNames` - Actions to match (e.g., "Open", "Use"). The first matching action is clicked.

**Returns:** `true` if interaction succeeded, `false` otherwise.

---

```java
default boolean interact(String objectNameOverride, VisualVerifier visualVerifier, BooleanSupplier breakCondition, String... menuItemNames)
```

Interacts with the object by matching its name and menu actions.

**Parameters:**
- `objectNameOverride` - Overrides the object's default name for menu matching (case-insensitive).
- `visualVerifier` - Optional verifier to validate the object visually before interaction.
- `breakCondition` - Condition to interrupt the interaction.
- `menuItemNames` - Actions to match (e.g., "Open", "Use"). The first matching action is clicked.

**Returns:** `true` if interaction succeeded, `false` otherwise.

---

```java
default boolean interact(String... menuItemNames)
```

Interacts with the object without using visual verification.

**Parameters:**
- `menuItemNames` - Actions to match (e.g., "Open", "Use").

**Returns:** `true` if interaction succeeded.

---

```java
default boolean interact(BooleanSupplier breakCondition, String... menuItemNames)
```

Interacts with the object without using visual verification.

**Parameters:**
- `menuItemNames` - Actions to match (e.g., "Open", "Use").
- `breakCondition` - Condition to interrupt the interaction.

**Returns:** `true` if interaction succeeded.

---

```java
default boolean interact(VisualVerifier visualVerifier, String... menuItemNames)
```

Interacts with the object using visual verification.

**Parameters:**
- `visualVerifier` - Verifies the object visually before interaction.
- `menuItemNames` - Actions to match.

**Returns:** `true` if interaction succeeded.

---

```java
default boolean interact(String objectNameOverride, String[] menuItemNames)
```

Interacts with the object using a name override (e.g., for dynamically named objects).

**Parameters:**
- `objectNameOverride` - Overrides the object's default name for menu matching.
- `menuItemNames` - Actions to match.

**Returns:** `true` if interaction succeeded.

### interactableFrom
```java
boolean interactableFrom(Direction direction)
```

Checks if the object can be interacted with from a given direction.

**Parameters:**
- `direction` - The direction to check (e.g., NORTH, EAST).

**Returns:** `true` if the object is interactable from the direction, `false` otherwise.

**Throws:** `IllegalArgumentException` - If a diagonal direction is provided.

### getInteractionSideFlags
```java
int getInteractionSideFlags()
```

Gets the interaction flags for each side of the object (e.g., blocked sides).

**Returns:** Bitmask of interaction flags (see `NORTH_INTERACTION_MASK` etc.).

### getObjectType
```java
ObjectType getObjectType()
```

Gets the type of the object (e.g., WALL, GROUND_ITEM).

### getSceneEntity
```java
Object getSceneEntity()
```

Gets the underlying scene entity (engine-specific representation).

### getName
```java
String getName()
```

Gets the object's in-game name.

### getId
```java
int getId()
```

Gets the object's unique ID.

### getRotation
```java
int getRotation()
```

Gets the object's rotation (0-2047, where 0 is South).

### getActions
```java
String[] getActions()
```

Gets the available actions for the object (e.g., ["Open", "Take"]).

### getModel
```java
Object getModel()
```

Gets the object's 3D model.

### getSecondaryModel
```java
Object getSecondaryModel()
```

Gets the secondary model (e.g., for animated objects).

### isBlocksProjectiles
```java
boolean isBlocksProjectiles()
```

Checks if the object blocks projectiles (e.g., walls).

### isInteractable
```java
boolean isInteractable()
```

Checks if the object is interactable (e.g., not decorative).

### isSolid
```java
boolean isSolid()
```

### getMaxWorldX
```java
default int getMaxWorldX()
```

Gets the maximum X coordinate (world space) for multi-tile objects. Defaults to the object's base X coordinate for single-tile objects.

### getMaxWorldY
```java
default int getMaxWorldY()
```

Gets the maximum Y coordinate (world space) for multi-tile objects. Defaults to the object's base Y coordinate for single-tile objects.

### getMaxLocalX
```java
default int getMaxLocalX()
```

Gets the maximum X coordinate (local space) for multi-tile objects. Defaults to the object's base local X coordinate for single-tile objects.

### getMaxLocalY
```java
default int getMaxLocalY()
```

Gets the maximum Y coordinate (local space) for multi-tile objects. Defaults to the object's base local Y coordinate for single-tile objects.

### getYaw
```java
default int getYaw()
```

Synonym for `getRotation()`.

### getSequenceId
```java
default int getSequenceId()
```

Gets the animation ID. Defaults to -1 (no animation).

### getSequenceFrame
```java
default int getSequenceFrame()
```

Gets the current animation frame. Defaults to -1 (no animation).

### getTileWidth
```java
default int getTileWidth()
```

Gets the width of the object in tiles. Defaults to 1.

### getTileHeight
```java
default int getTileHeight()
```

Gets the height of the object in tiles. Defaults to 1.

### isInteractableOnScreen
```java
boolean isInteractableOnScreen()
```

Checks if the object is visible and interactable on the game screen.

### getSurroundingPositions
```java
List<WorldPosition> getSurroundingPositions()
```

```java
List<WorldPosition> getSurroundingPositions(int)
```

### getTile
```java
Tile getTile()
```

Gets the tile the object occupies.

### getLocalX
```java
int getLocalX()
```

**Specified by:** getLocalX in interface Location3D

### getLocalY
```java
int getLocalY()
```

**Specified by:** getLocalY in interface Location3D

### canReach
```java
default boolean canReach()
```

Checks if the object is reachable within the default distance (1 tile).

**Performance Warning:** This uses an A* pathfinding algorithm which is computationally expensive. Avoid calling this in loops or performance-critical code paths.

**Returns:** `true` if a valid path exists within 1 tile distance, `false` otherwise.

---

```java
boolean canReach(int interactDistance)
```

Checks if the object is reachable within the default distance (1 tile).

**Performance Warning:** This uses an A* pathfinding algorithm which is computationally expensive. Avoid calling this in loops or performance-critical code paths.

**Parameters:**
- `interactDistance` - The distance away from an objects tile, before we can interact.

**Returns:** `true` if a valid path exists within 1 tile distance, `false` otherwise.

### getTileDistance
```java
int getTileDistance()
```

Gets the pathfinding distance to the object (in tiles) using A* pathfinding.

**Performance Warning:** This is an expensive operation that calculates a full path. Never use this in loops or frequent polling scenarios as it may cause significant performance degradation.

**Returns:** The number of tiles in the shortest path, or 0 if unreachable.

### getObjectArea
```java
Rectangle getObjectArea()
```

### distance
```java
double distance()
```

Gets the Euclidean distance to the object
