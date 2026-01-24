# Utils

**Type:** Class

**Extends:** Object

## Constructors

| Constructor |
|-------------|
| `Utils()` |

## Methods

| Return Type | Method |
|------------|--------|
| `static Rectangle` | `calculateBoundingBox(List<Point>)` |
| `static Point` | `calculateCentroid(List<Point> points)` |
| `static <T> T[]` | `combineArray(T[] array1, T[] array2)` |
| `static byte[]` | `convertImageToBytes(Image)` |
| `static Rectangle` | `createBoundingRectangle(List<Point> points)` |
| `static List<Point>` | `generateHumanLikePath(Point, Point)` |
| `static List<Point>` | `generateStraightPath(Point, Point)` |
| `static Object` | `getClosest(Position, List)` |
| `static Point` | `getClosestPoint(Point mainPosition, int maxDistance, List<Point> positions)` |
| `static Position` | `getClosestPosition(Position, int, List)` |
| `static Position` | `getClosestPosition(Position mainPosition, int maxDistance, Position... positions)` |
| `static Position` | `getClosestPosition(Position, Position...)` |
| `static Image` | `getImageResource(String)` |
| `static List<Position>` | `getPositionsWithinRadius(Position, int, List)` |
| `static List<Position>` | `getPositionsWithinRadius(Position, int, Position...)` |
| `static Point` | `getRandomPointOutside(Rectangle rectangle, Rectangle parent, int maxDistance)` |
| `Rectangle[]` | `getTextBounds(Rectangle bounds, int maxHeight, int textColor)` |
| `List<WorldPosition>` | `getWorldPositionForRespawnCircles(List<Rectangle> circleBounds, int respawnCircleHeight)` |
| `@Deprecated List<WorldPosition>` | `getWorldPositionForRespawnCircles(List<RespawnCircle> circles, int circleZHeight, boolean debug)` |
| `static javafx.scene.image.ImageView` | `imageToImageView(Image image)` |
| `static boolean` | `lineIntersectsRectangle(Line line, Rectangle rectangle)` |
| `@Deprecated static int` | `random(int num)` |
| `@Deprecated static int` | `random(int low, int high)` |
| `static Point` | `randomisePointByRadius(Point point, int radius)` |
| `static Timestamp` | `stringToTimestamp(String)` |

### Inherited Methods from Object

clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait

## Constructor Details

### Utils
```java
public Utils()
```

## Method Details

### random
```java
@Deprecated
public static int random(int num)
```

**Deprecated.**

---

```java
@Deprecated
public static int random(int low, int high)
```

**Deprecated.**

### lineIntersectsRectangle
```java
public static boolean lineIntersectsRectangle(Line line, Rectangle rectangle)
```

### stringToTimestamp
```java
public static Timestamp stringToTimestamp(String)
```

### randomisePointByRadius
```java
public static Point randomisePointByRadius(Point point, int radius)
```

### convertImageToBytes
```java
public static byte[] convertImageToBytes(Image)
```

**Throws:** `IOException`

### combineArray
```java
public static <T> T[] combineArray(T[] array1, T[] array2)
```

### getClosestPosition
```java
public static Position getClosestPosition(Position, int, List)
```

---

```java
public static Position getClosestPosition(Position mainPosition, int maxDistance, Position... positions)
```

---

```java
public static Position getClosestPosition(Position, Position...)
```

### getClosestPoint
```java
public static Point getClosestPoint(Point mainPosition, int maxDistance, List<Point> positions)
```

### generateHumanLikePath
```java
public static List<Point> generateHumanLikePath(Point, Point)
```

### generateStraightPath
```java
public static List<Point> generateStraightPath(Point, Point)
```

### createBoundingRectangle
```java
public static Rectangle createBoundingRectangle(List<Point> points)
```

Creates the smallest rectangle (bounding rectangle) that completely encompasses a given list of points.

**Parameters:**
- `points` - the list of `Point` objects representing the points to be enclosed within the rectangle. Each point must have valid integer x and y coordinates. The list must not be null or empty.

**Returns:** a `Rectangle` object representing the smallest rectangle that can fully enclose all the given points.
- The top-left corner of the rectangle is determined by the smallest x and y values among the points.
- The width is calculated as (maxX - minX).
- The height is calculated as (maxY - minY).

**Throws:** `IllegalArgumentException` - if the points list is null or empty.

### getPositionsWithinRadius
```java
public static List<Position> getPositionsWithinRadius(Position, int, List)
```

---

```java
public static List<Position> getPositionsWithinRadius(Position, int, Position...)
```

### getRandomPointOutside
```java
public static Point getRandomPointOutside(Rectangle rectangle, Rectangle parent, int maxDistance)
```

### imageToImageView
```java
public static javafx.scene.image.ImageView imageToImageView(Image image)
```

### getImageResource
```java
public static Image getImageResource(String)
```

**Throws:** `IOException`

### calculateCentroid
```java
public static Point calculateCentroid(List<Point> points)
```

Returns the center point of the list of points, calculated as the average of all points' coordinates.

**Returns:** A Point object representing the center of the list of points.

### calculateBoundingBox
```java
public static Rectangle calculateBoundingBox(List<Point>)
```

Returns the bounding rectangle that contains all points. The rectangle is defined by its top-left corner (minX, minY) and its width and height.

**Returns:** A Rectangle object representing the bounding box of the list of points.

### getClosest
```java
public static Object getClosest(Position, List)
```

### getTextBounds
```java
public Rectangle[] getTextBounds(Rectangle bounds, int maxHeight, int textColor)
```

Retrieves the rectangular bounds of the text displayed in a given area.

**Parameters:**
- `bounds` - The rectangle area on the screen where the text is expected. It specifies the starting position (x, y) and the limit for width and height.
- `maxHeight` - The maximum height each line of text can have. This is useful when lines of text are close together, with no whitespace in between. This height can be calculated based on the distance from the top of ascending characters to the bottom of descending characters.
- `textColor` - The color of the text as an integer. The bounds will be calculated for the text pixels that match this color.

**Returns:** An array of Rectangle objects where each rectangle corresponds to a line of text. The rectangle provides the bounds of the line of text on the screen (x, y, width, height). The returned array is ordered by vertical position (top to bottom). Index 0 corresponds to the top-most detected line, index 1 to the next line down, and so on. If no text matching the color is found within the supplied bounds, an empty array is returned.

### getWorldPositionForRespawnCircles
```java
public List<WorldPosition> getWorldPositionForRespawnCircles(List<Rectangle> circleBounds, int respawnCircleHeight)
```

This method matches detected respawn circles (in screen coordinates) to their corresponding world positions by:

1. Calculating center points of all detected circles
2. Projecting each game tile to screen space
3. Finding the closest matching circle for each projected tile point
4. Converting matching tile coordinates to world positions

**Note:** The matching uses a proximity threshold of 6 pixels between tile projections and circle centers.

**Parameters:**
- `circleBounds` - List of rectangles representing detected respawn circles. Each rectangle should contain a valid respawn circle.
- `respawnCircleHeight` - The height/z-offset (in pixels) at which respawn circles are drawn relative to their base tile position. This affects the projection.

**Returns:** List of `WorldPosition` objects corresponding to the detected respawn circles. The list may be shorter than the input if some circles couldn't be matched to tiles.

**Throws:**
- `RuntimeException` - if the current world position cannot be determined
- `NullPointerException` - if circleBounds is null

**See Also:**
- `WorldPosition`
- `PixelAnalyzer.findRespawnCircles()`

---

```java
@Deprecated
public List<WorldPosition> getWorldPositionForRespawnCircles(List<RespawnCircle> circles, int circleZHeight, boolean debug)
```

**Deprecated.**

This method matches detected respawn circles (in screen coordinates) to their corresponding world positions by:

1. Calculating center points of all detected circles
2. Projecting each game tile to screen space
3. Finding the closest matching circle for each projected tile point
4. Converting matching tile coordinates to world positions

**Note:** The matching uses a proximity threshold of 6 pixels between tile projections and circle centers.

**Parameters:**
- `circles` - List of Respawn circle objects representing detected respawn circles.
- `circleZHeight` - The height/z-offset (in pixels) at which respawn circles are drawn (center of the circle) relative to their base tile position.
- `debug` - If true, draws the respawn circle center points on screen for visual debugging.

**Returns:** List of `WorldPosition` objects corresponding to the detected respawn circles. The list may be shorter than the input if some circles couldn't be matched to tiles.

**Throws:**
- `RuntimeException` - if the current world position cannot be determined
- `NullPointerException` - if circles is null

**See Also:**
- `WorldPosition`
- `PixelAnalyzer.findRespawnCircles()`
