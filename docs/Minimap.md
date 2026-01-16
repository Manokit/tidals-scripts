# Minimap

**Type:** Interface

**All Known Implementing Classes:** MinimapComponent

## Fields

| Type | Field |
|------|-------|
| `static final int` | `RADIUS` |

## Methods

| Return Type | Method |
|------------|--------|
| `void` | `arrowDetectionEnabled(boolean enabled)` |
| `Point` | `clampToMinimap(int x, int y)` |
| `Point` | `getCenter()` |
| `List<Point>` | `getItemPositions()` |
| `ArrowResult` | `getLastArrowResult()` |
| `Image` | `getMinimapImage(boolean cleanDynamicEntities)` |
| `List<Point>` | `getNPCPositions()` |
| `List<Point>` | `getPlayerPositions()` |
| `boolean` | `insideMinimap(int x, int y)` |
| `boolean` | `isArrowDetectionEnabled()` |
| `Point` | `positionToMinimap(WorldPosition)` |
| `Point` | `positionToMinimapClamped(WorldPosition)` |

## Field Details

### RADIUS
```java
static final int RADIUS
```

## Method Details

### getLastArrowResult
```java
ArrowResult getLastArrowResult()
```

### arrowDetectionEnabled
```java
void arrowDetectionEnabled(boolean enabled)
```

### isArrowDetectionEnabled
```java
boolean isArrowDetectionEnabled()
```

### positionToMinimapClamped
```java
Point positionToMinimapClamped(WorldPosition)
```

### positionToMinimap
```java
Point positionToMinimap(WorldPosition)
```

### getCenter
```java
Point getCenter()
```

### clampToMinimap
```java
Point clampToMinimap(int x, int y)
```

### insideMinimap
```java
boolean insideMinimap(int x, int y)
```

### getMinimapImage
```java
Image getMinimapImage(boolean cleanDynamicEntities)
```

### getPlayerPositions
```java
List<Point> getPlayerPositions()
```

Returns a list containing the coordinates of each player(white) dot seen on the minimap.

**Returns:** List<Point>

### getNPCPositions
```java
List<Point> getNPCPositions()
```

Returns a list containing the coordinates of each NPC(yellow) dot seen on the minimap.

**Returns:** List<Point>

### getItemPositions
```java
List<Point> getItemPositions()
```

Returns a list containing the coordinates of each item(red) dot seen on the minimap.

**Returns:** List<Point>
