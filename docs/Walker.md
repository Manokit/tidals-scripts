# Walker

**Type:** Interface

## Methods

| Return Type | Method |
|------------|--------|
| `AStar` | `getaStar()` |
| `CollisionManager` | `getCollisionManager()` |
| `WalkSettings` | `getDefaultSettings()` |
| `boolean` | `walkPath(List<WorldPosition>)` |
| `boolean` | `walkPath(List<WorldPosition>, WalkConfig)` |
| `boolean` | `walkTo(Position position)` |
| `boolean` | `walkTo(Position position, WalkConfig config)` |
| `boolean` | `walkTo(int worldX, int worldY)` |
| `boolean` | `walkTo(int worldX, int worldY, WalkConfig config)` |
| `boolean` | `walkTo(RSObject object)` |
| `boolean` | `walkTo(RSObject object, WalkConfig walkConfig)` |
| `boolean` | `walkTo(RSObject object, int interactDistance, WalkConfig walkConfig)` |

## Method Details

### getaStar
```java
AStar getaStar()
```

### getDefaultSettings
```java
WalkSettings getDefaultSettings()
```

### walkTo
```java
boolean walkTo(Position position)
```

Walks to the specified coordinates

**Parameters:**
- `position` - The Position to navigate to. Either LocalPosition or WorldPosition can be passed.

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(Position position, WalkConfig config)
```

Walks to the specified coordinates

**Parameters:**
- `position` - The Position to navigate to. Either LocalPosition or WorldPosition can be passed.
- `config` - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(int worldX, int worldY)
```

Walks to the specified coordinates

**Parameters:**
- `worldX` - The world X coordinate to navigate to
- `worldY` - The world Y coordinate to navigate to

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(int worldX, int worldY, WalkConfig config)
```

Walks to the specified coordinates

**Parameters:**
- `worldX` - The world X coordinate to navigate to
- `worldY` - The world Y coordinate to navigate to
- `config` - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(RSObject object)
```

**Parameters:**
- `object` - The RSObject to navigate to

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(RSObject object, WalkConfig walkConfig)
```

**Parameters:**
- `object` - The RSObject to navigate to
- `walkConfig` - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(RSObject object, int interactDistance, WalkConfig walkConfig)
```

**Parameters:**
- `object` - The RSObject to navigate to
- `interactDistance` - The tile distance from the object to where you can interact from.
- `walkConfig` - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

### walkPath
```java
boolean walkPath(List<WorldPosition>)
```

```java
boolean walkPath(List<WorldPosition>, WalkConfig)
```

### getCollisionManager
```java
CollisionManager getCollisionManager()
```
