# ObjectManager

**Package:** `com.osmb.api.scene`

**Type:** Interface

**Access:** `script.getObjectManager()`

The `ObjectManager` interface provides methods for finding and interacting with game objects (RSObjects) like trees, rocks, banks, altars, doors, and other interactable scenery.

---

## Method Summary

| Method | Returns | Description |
|--------|---------|-------------|
| `getObjects()` | `Map<Integer, List<RSObject>>` | Get all objects grouped by plane |
| `getObjects(int plane)` | `List<RSObject>` | Get all objects on a specific plane |
| `getObjects(Predicate<RSObject>)` | `List<RSObject>` | Find objects matching a filter |
| `getRSObject(Predicate<RSObject>)` | `RSObject` | Find first object matching filter |
| `getClosestObject(WorldPosition, String...)` | `RSObject` | Find closest object by name(s) |
| `removeObject(String, WorldPosition)` | `void` | Remove cached object at position |

---

## Core Methods

### getClosestObject(WorldPosition, String...)

```java
RSObject getClosestObject(WorldPosition worldPosition, String... names)
```

Finds the closest object to the given position that matches any of the provided names.

**Parameters:**
- `worldPosition` - The reference position (usually player position)
- `names` - One or more object names to search for

**Returns:** The closest matching `RSObject`, or `null` if none found.

**Example:**
```java
// find closest bank booth
RSObject bank = script.getObjectManager().getClosestObject(
    script.getWorldPosition(),
    "Bank booth"
);

// find closest of multiple object types
RSObject bankOrChest = script.getObjectManager().getClosestObject(
    script.getWorldPosition(),
    "Bank booth", "Bank chest", "Grand Exchange booth"
);
```

---

### getRSObject(Predicate<RSObject>)

```java
RSObject getRSObject(Predicate<RSObject> objectFilter)
```

Finds the first object matching the provided predicate filter.

**Parameters:**
- `objectFilter` - A predicate to filter objects

**Returns:** The first matching `RSObject`, or `null` if none found.

**Example:**
```java
// find bank that can be reached
RSObject bank = script.getObjectManager().getRSObject(obj ->
    obj.getName() != null &&
    obj.getName().equals("Bank booth") &&
    obj.canReach()
);

// find object by ID
RSObject altar = script.getObjectManager().getRSObject(obj ->
    obj.getId() == 29150  // Nature altar
);
```

---

### getObjects(Predicate<RSObject>)

```java
List<RSObject> getObjects(Predicate<RSObject> objectFilter)
```

Finds all objects matching the provided predicate filter.

**Parameters:**
- `objectFilter` - A predicate to filter objects

**Returns:** A list of matching `RSObject`s.

**Example:**
```java
// find all trees
List<RSObject> trees = script.getObjectManager().getObjects(obj ->
    obj.getName() != null && obj.getName().equals("Tree")
);

// find all reachable rocks
List<RSObject> rocks = script.getObjectManager().getObjects(obj ->
    obj.getName() != null &&
    obj.getName().equals("Rocks") &&
    obj.canReach()
);
```

---

### getObjects(int plane)

```java
List<RSObject> getObjects(int plane)
```

Gets all objects on a specific plane/floor level.

**Parameters:**
- `plane` - The plane level (0 = ground floor, 1 = first floor, etc.)

**Returns:** A list of all `RSObject`s on that plane.

**Example:**
```java
// get all ground-level objects
List<RSObject> groundObjects = script.getObjectManager().getObjects(0);

// get first floor objects
List<RSObject> firstFloor = script.getObjectManager().getObjects(1);
```

---

### getObjects()

```java
Map<Integer, List<RSObject>> getObjects()
```

Gets all objects in the scene, grouped by plane.

**Returns:** A map where keys are plane levels and values are lists of objects.

---

### removeObject(String, WorldPosition)

```java
void removeObject(String name, WorldPosition worldPosition)
```

Removes a cached object from the manager. Useful when an object has been interacted with and should be excluded from future searches.

**Parameters:**
- `name` - The object name
- `worldPosition` - The object's world position

---

## Common Patterns

### Standard Bank Query

The recommended pattern for finding banks that works across all locations:

```java
public static final String[] BANK_NAMES = {
    "Bank", "Chest", "Bank booth", "Bank chest",
    "Grand Exchange booth", "Bank counter", "Bank table"
};
public static final String[] BANK_ACTIONS = {"bank", "open", "use"};

public static final Predicate<RSObject> BANK_QUERY = obj ->
    obj.getName() != null &&
    obj.getActions() != null &&
    Arrays.stream(BANK_NAMES).anyMatch(name ->
        name.equalsIgnoreCase(obj.getName())) &&
    Arrays.stream(obj.getActions()).anyMatch(action ->
        Arrays.stream(BANK_ACTIONS).anyMatch(bankAction ->
            bankAction.equalsIgnoreCase(action))) &&
    obj.canReach();

// usage
RSObject bank = script.getObjectManager().getRSObject(BANK_QUERY);
```

### Finding Objects with Retry

Menu interactions can fail - always use retry logic:

```java
RSObject altar = script.getObjectManager().getClosestObject(
    script.getWorldPosition(),
    "Altar"
);

if (altar != null) {
    // use RetryUtils for reliable interaction
    RetryUtils.objectInteract(script, altar, "Pray", "altar");
}
```

### Filtering by Distance

```java
WorldPosition myPos = script.getWorldPosition();

RSObject nearbyTree = script.getObjectManager().getRSObject(obj ->
    obj.getName() != null &&
    obj.getName().equals("Tree") &&
    obj.getWorldPosition().distance(myPos) < 10
);
```

### Checking Object Actions

```java
RSObject door = script.getObjectManager().getRSObject(obj ->
    obj.getName() != null &&
    obj.getName().equals("Door") &&
    obj.getActions() != null &&
    Arrays.asList(obj.getActions()).contains("Open")  // door is closed
);
```

---

## Important Notes

1. **Use `canReach()` for Interactable Objects** - Always check if an object can be reached before trying to interact with it.

2. **Null Checks are Essential** - Object names and actions can be null. Always check before comparing.

3. **Collision Map is Static** - The collision map doesn't update dynamically. For doors, verify state visually via menu response rather than assuming collision data is accurate.

4. **Use RetryUtils for Interactions** - Direct `obj.interact()` can fail. Use `RetryUtils.objectInteract()` for reliability.

---

## See Also

- [RSObject.md](RSObject.md) - Properties and methods of RSObject
- [ObjectIdentifier.md](ObjectIdentifier.md) - Object identification patterns
- [critical-concepts.md](critical-concepts.md) - Visual verification for doors
