# RectangleArea

**Package:** `com.osmb.api.location.area.impl`  
**Type:** Class  
**Implements:** `Area`

Concrete `Area` implementation representing an **axis-aligned rectangular region** on a specific plane.

Used for spatial checks, random position sampling, distance calculations, and pathing logic.

---

## Class Signature

```java
public class RectangleArea implements Area
```

---

## Core Concept

`RectangleArea` defines a rectangle using:

- **Base coordinate** `(x, y)`
- **Width** and **height**
- **Plane** (z-level)

The base position is the **south-west (bottom-left)** corner of the rectangle.

All operations are deterministic and tile-based.

---

## Constructor

### `RectangleArea(int x, int y, int width, int height, int plane)`

```java
RectangleArea area =
    new RectangleArea(3200, 3200, 5, 7, 0);
```

**Parameters**
- `x` – Base X coordinate (south-west corner)
- `y` – Base Y coordinate (south-west corner)
- `width` – Width of the rectangle (tiles)
- `height` – Height of the rectangle (tiles)
- `plane` – Plane / level the area exists on

---

## Spatial Queries

### `boolean contains(WorldPosition position)`
Checks whether a position lies inside the rectangle.

```java
area.contains(player.getWorldPosition());
```

---

### `boolean contains(int x, int y, int plane)`
Coordinate-level containment check.

Returns `false` if the plane does not match.

---

### `double distanceTo(WorldPosition position)`
Computes distance from a position to the rectangle.

Behavior:
- Returns `0` if position is inside the area
- Otherwise returns distance to the **nearest edge tile**
- Returns `Integer.MAX_VALUE` if area is empty

---

## Position Utilities

### `WorldPosition getRandomPosition()`
Returns a random tile inside the rectangle.

Commonly used for:
- Anti-pattern movement
- Randomized interaction targets

---

### `WorldPosition getBasePosition()`
Returns the **south-west corner** of the rectangle.

---

### `Point getCenter()`
Returns the geometric center of the area as a `java.awt.Point`.

Used for:
- Camera targeting
- Navigation heuristics

---

## Bounds and Geometry

### `Rectangle getBounds()`
Returns the rectangle as a `com.osmb.api.shape.Rectangle`.

Useful when interfacing with:
- Input (`Finger.tap(shape)`)
- Rendering overlays
- Collision logic

---

### `List<WorldPosition> getEdgeTiles(Direction direction)`
Returns edge tiles on one side of the rectangle.

```java
area.getEdgeTiles(Direction.NORTH);
```

Valid directions:
- `NORTH`
- `SOUTH`
- `EAST`
- `WEST`

Common use:
- Entering/exiting areas
- Pathfinding boundaries

---

### `List<WorldPosition> getSurroundingPositions(int radius)`
Returns tiles surrounding the rectangle at a given radius.

Used for:
- Proximity checks
- Threat detection
- Area expansion logic

---

## Accessors

```java
int getX()
int getY()
int getWidth()
int getHeight()
int getPlane()
```

All values are immutable after construction.

---

## Inherited Area Methods

From `Area`:
- `getAllWorldPositions()`
- `getClosestPosition(WorldPosition)`

---

## Usage Patterns

### Area containment
```java
if (bankArea.contains(playerPos)) {
    // inside bank
}
```

### Random movement target
```java
walker.walkTo(area.getRandomPosition());
```

### Input integration
```java
finger.tap(area.getBounds());
```

---

## LLM Notes

- Coordinates are **tile-based**, not pixel-based
- Rectangle is inclusive of its bounds
- Plane mismatches always fail containment checks
- Prefer `getBounds()` when bridging area logic to input APIs
- Safe, deterministic primitive for spatial reasoning
