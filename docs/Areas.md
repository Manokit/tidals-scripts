# Areas & Positions

**Package:** `com.osmb.api.location.area` and `com.osmb.api.location.position`

This document covers area definitions and position handling for defining regions and working with world coordinates.

---

## WorldPosition

**Package:** `com.osmb.api.location.position.types`

**Type:** Class (extends `Position`)

Represents a position in the game world with X, Y coordinates and plane (floor level).

### Constructors

```java
// create with integer coordinates
WorldPosition pos = new WorldPosition(3200, 3200, 0);

// create with precise (double) coordinates
WorldPosition precisePos = new WorldPosition(3200.5, 3200.5, 0);
```

### Key Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `getX()` | `int` | X coordinate |
| `getY()` | `int` | Y coordinate |
| `getPlane()` | `int` | Plane/floor level (0 = ground) |
| `distance(Position)` | `double` | Distance to another position |
| `distanceTo(int, int)` | `double` | Distance to x,y coordinates |
| `getRegionID()` | `int` | Get the region ID for this position |
| `getClosest(List<WorldPosition>)` | `WorldPosition` | Find closest position from a list |
| `getSurroundingTiles(boolean)` | `List<Position>` | Get adjacent tiles |
| `equals(Object)` | `boolean` | Check equality |
| `equalsIgnorePlane(Position)` | `boolean` | Compare ignoring plane |

### Common Patterns

```java
// get player position
WorldPosition myPos = script.getWorldPosition();

// calculate distance
double dist = myPos.distance(targetPos);

// check if within range
if (myPos.distance(bankPos) < 10) {
    // close to bank
}

// find closest from multiple options
List<WorldPosition> spots = Arrays.asList(pos1, pos2, pos3);
WorldPosition closest = myPos.getClosest(spots);
```

---

## RectangleArea

**Package:** `com.osmb.api.location.area.impl`

**Type:** Class (implements `Area`)

Defines a rectangular region in the game world. Use for simple rectangular areas like rooms, banks, or mining spots.

> **See existing doc:** [RectangleArea.md](RectangleArea.md)

### Quick Reference

```java
// create from two corners
RectangleArea bankArea = new RectangleArea(
    new WorldPosition(3180, 3430, 0),  // bottom-left
    new WorldPosition(3195, 3445, 0)   // top-right
);

// check if player is in area
if (bankArea.contains(script.getWorldPosition())) {
    // player is in bank
}

// get random position for humanized walking
WorldPosition walkTo = bankArea.getRandomPosition();
```

---

## PolyArea

**Package:** `com.osmb.api.location.area.impl`

**Type:** Class (implements `Area`)

Defines a polygon-shaped region in the game world. Use for irregular shapes like mining areas, complex rooms, or non-rectangular zones.

### Constructor

```java
public PolyArea(List<WorldPosition> positions)
```

Creates a polygon from a list of vertices. Points should be in order (clockwise or counter-clockwise).

### Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `add(WorldPosition)` | `void` | Add a vertex to the polygon |
| `contains(WorldPosition)` | `boolean` | Check if position is inside |
| `contains(int, int, int)` | `boolean` | Check if coordinates are inside |
| `getBounds()` | `Rectangle` | Get bounding rectangle |
| `getCenter()` | `Point` | Get center point |
| `getPlane()` | `int` | Get plane level |
| `getRandomPosition()` | `WorldPosition` | Get random position inside |
| `getClosestPosition(WorldPosition)` | `WorldPosition` | Find closest point in area |
| `distanceTo(WorldPosition)` | `double` | Distance to area |
| `getAllWorldPositions()` | `List<WorldPosition>` | Get all tiles in area |

### Example: Irregular Mining Area

```java
// define an L-shaped mining area
List<WorldPosition> vertices = Arrays.asList(
    new WorldPosition(3180, 3430, 0),
    new WorldPosition(3190, 3430, 0),
    new WorldPosition(3190, 3440, 0),
    new WorldPosition(3200, 3440, 0),
    new WorldPosition(3200, 3450, 0),
    new WorldPosition(3180, 3450, 0)
);

PolyArea miningArea = new PolyArea(vertices);

// check if in area
if (miningArea.contains(script.getWorldPosition())) {
    // in mining spot
}

// walk to random position in area
WorldPosition spot = miningArea.getRandomPosition();
script.getWalker().walkTo(spot);
```

### Example: Custom Skilling Zone

```java
// create area dynamically
PolyArea zone = new PolyArea(new ArrayList<>());
zone.add(new WorldPosition(3200, 3200, 0));
zone.add(new WorldPosition(3210, 3200, 0));
zone.add(new WorldPosition(3215, 3210, 0));
zone.add(new WorldPosition(3205, 3215, 0));
zone.add(new WorldPosition(3195, 3210, 0));

// use for object filtering
RSObject rock = script.getObjectManager().getRSObject(obj ->
    obj.getName() != null &&
    obj.getName().equals("Rocks") &&
    zone.contains(obj.getWorldPosition())
);
```

---

## Area Interface

Both `RectangleArea` and `PolyArea` implement the `Area` interface:

```java
public interface Area {
    boolean contains(WorldPosition position);
    boolean contains(int x, int y, int plane);
    Rectangle getBounds();
    Point getCenter();
    int getPlane();
    WorldPosition getRandomPosition();
    WorldPosition getClosestPosition(WorldPosition position);
    double distanceTo(WorldPosition position);
    List<WorldPosition> getAllWorldPositions();
}
```

This means you can use either type interchangeably:

```java
private final Area operatingArea;  // can be Rectangle or Poly

public boolean inOperatingArea() {
    return operatingArea.contains(script.getWorldPosition());
}
```

---

## When to Use Each Type

| Type | Best For |
|------|----------|
| `RectangleArea` | Banks, rooms, simple rectangular zones |
| `PolyArea` | L-shaped areas, irregular mining spots, complex zones |
| `WorldPosition` | Single locations, distance checks, walking targets |

---

## See Also

- [RectangleArea.md](RectangleArea.md) - Detailed RectangleArea documentation
- [Walker.md](Walker.md) - Navigation to areas
- [ObjectManager.md](ObjectManager.md) - Filtering objects by area
