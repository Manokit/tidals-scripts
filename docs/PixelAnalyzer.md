# PixelAnalyzer

**Type:** Interface

Interface for analyzing pixels within images or shapes, detecting clusters, animations, and specific pixel patterns.

## Nested Classes

| Type | Class |
|------|-------|
| `static class` | `PixelAnalyzer.RespawnCircle` - Represents a respawn circle with its bounds and type. |
| `static enum` | `PixelAnalyzer.RespawnCircle.DrawType` |

## Methods

| Return Type | Method |
|------------|--------|
| `List<List<Point>>` | `findClusters(ClusterQuery clusterQuery)` |
| `List<List<Point>>` | `findClusters(Shape shape, ClusterQuery clusterQuery)` |
| `List<List<Point>>` | `findClusters(int pixelSkipSize, Shape shape, ClusterQuery clusterQuery)` |
| `List<ClusterSearchResult>` | `findClustersMulti(Shape shape, List<ClusterQuery> clusterQueries)` |
| `Point` | `findPixel(Shape shape, SearchablePixel... pixels)` |
| `Point` | `findPixel(Image mainImage, Shape shape, SearchablePixel... pixels)` |
| `List<Point>` | `findPixels(Shape shape, SearchablePixel... pixels)` |
| `List<Point>` | `findPixels(Image mainImage, Shape shape, SearchablePixel... pixels)` |
| `List<Point>` | `findPixelsOnGameScreen(Shape shape, SearchablePixel... pixels)` |
| `List<Point>` | `findPixelsOnGameScreen(int pixelSkipSize, Shape shape, SearchablePixel... pixels)` |
| `List<List<Point>>` | `findPixelsOnGameScreenMulti(Shape shape, List<SearchablePixel[]> pixelArrays)` |
| `List<Rectangle>` | `findRespawnCircles()` |
| `List<Rectangle>` | `findRespawnCircles(Rectangle bounds)` |
| `List<PixelAnalyzer.RespawnCircle>` | `findRespawnCircleTypes()` |
| `List<PixelAnalyzer.RespawnCircle>` | `findRespawnCircleTypes(Rectangle bounds)` |
| `Rectangle` | `getHighlightBounds(Shape shape, SearchablePixel... highlightColors)` |
| `int` | `getPixelAt(int x, int y)` |
| `PixelAnalyzer.RespawnCircle` | `getRespawnCircle(Rectangle area, PixelAnalyzer.RespawnCircle.DrawType drawType, int zOffset, double distanceTolerance)` |
| `Map<RSObject, PixelAnalyzer.RespawnCircle>` | `getRespawnCircleObjects(List<RSObject> objects, PixelAnalyzer.RespawnCircle.DrawType drawType, int zOffset, double distanceTolerance)` |
| `List<List<Point>>` | `groupPixels(List<Point> pixelsToGroup, double maxDistance, int minGroupSize)` |
| `boolean` | `isAnimating(double minDifferenceFactor, Shape shape)` |
| `boolean` | `isPixelAt(int x, int y, SearchablePixel... pixels)` |
| `boolean` | `isPixelAt(int x, int y, SearchablePixel searchablePixel)` |
| `boolean` | `isPlayerAnimating(double minDifferenceFactor)` |

## Method Details

### findPixel
```java
Point findPixel(Shape shape, SearchablePixel... pixels)
```

Finds the first pixel matching the specified criteria within the given shape.

**Parameters:**
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A point where the pixel matches, or null if no match is found.

---

```java
Point findPixel(Image mainImage, Shape shape, SearchablePixel... pixels)
```

Finds the first pixel matching the specified criteria in a given image and shape.

**Parameters:**
- `mainImage` - The image to search in. If null, uses the current screen image.
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A point where the pixel matches, or null if no match is found.

### findPixels
```java
List<Point> findPixels(Shape shape, SearchablePixel... pixels)
```

Finds all pixels matching the specified criteria within the given shape.

**Parameters:**
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A list of points where pixels match.

---

```java
List<Point> findPixels(Image mainImage, Shape shape, SearchablePixel... pixels)
```

Finds all pixels matching the specified criteria in the given image and shape.

**Parameters:**
- `mainImage` - The image to search in. If null, uses the current screen image.
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A list of points where pixels match.

### getPixelAt
```java
int getPixelAt(int x, int y)
```

Retrieves the RGB value of the pixel at the given coordinates.

**Parameters:**
- `x` - The x-coordinate.
- `y` - The y-coordinate.

**Returns:** The RGB value at the specified location.

### isPixelAt
```java
boolean isPixelAt(int x, int y, SearchablePixel... pixels)
```

Checks if a specific pixel at the given coordinates matches any of the specified criteria.

**Parameters:**
- `x` - The x-coordinate.
- `y` - The y-coordinate.
- `pixels` - The pixel search criteria.

**Returns:** True if a match is found; otherwise, false.

---

```java
boolean isPixelAt(int x, int y, SearchablePixel searchablePixel)
```

Checks if a specific pixel at the given coordinates matches the specified criteria.

**Parameters:**
- `x` - The x-coordinate.
- `y` - The y-coordinate.
- `searchablePixel` - The pixel search criteria.

**Returns:** True if a match is found; otherwise, false.

### findPixelsOnGameScreen
```java
List<Point> findPixelsOnGameScreen(Shape shape, SearchablePixel... pixels)
```

Finds all pixels matching the specified criteria within the given shape on the game screen, excluding pixels inside widget boundaries.

**Parameters:**
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A list of points where the pixels match.

---

```java
List<Point> findPixelsOnGameScreen(int pixelSkipSize, Shape shape, SearchablePixel... pixels)
```

Finds all matching pixels within the specified shape and on the game's screen, excluding any pixels inside widget boundaries.

**Parameters:**
- `pixelSkipSize` - The number of pixels to skip during the search for performance optimization.
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A list of points where the pixels match.

### findPixelsOnGameScreenMulti
```java
List<List<Point>> findPixelsOnGameScreenMulti(Shape shape, List<SearchablePixel[]> pixelArrays)
```

Efficiently finds all matching pixels for multiple pixel search arrays within the specified shape and on the game's screen, excluding any pixels inside widget boundaries. This method improves performance by querying all pixel arrays in a single iteration over the screen image, instead of performing multiple passes for each array.

**Parameters:**
- `shape` - The shape to search within.
- `pixelArrays` - A list of pixel search criteria arrays.

**Returns:** A list of lists of points, each corresponding to the matches for each pixel array.

### findClusters
```java
List<List<Point>> findClusters(ClusterQuery clusterQuery)
```

Finds clusters of pixels based on the provided cluster query.

**Parameters:**
- `clusterQuery` - The cluster query containing search criteria.

**Returns:** A ClusterSearchResult containing the clusters found.

---

```java
List<List<Point>> findClusters(Shape shape, ClusterQuery clusterQuery)
```

Finds clusters of pixels within the specified shape based on the provided cluster query.

**Parameters:**
- `shape` - The shape to search within. If null, searches the entire game screen.
- `clusterQuery` - The cluster query containing search criteria.

**Returns:** A ClusterSearchResult containing the clusters found.

---

```java
List<List<Point>> findClusters(int pixelSkipSize, Shape shape, ClusterQuery clusterQuery)
```

Finds clusters of pixels within the specified shape that match the given pixel criteria.

**Parameters:**
- `pixelSkipSize` - The number of pixels to skip during the search for performance optimization.
- `shape` - The shape to search within. If null, searches the entire game screen.
- `clusterQuery` - The cluster query containing pixel search criteria.

**Returns:** A ClusterSearchResult containing the clusters found, or null if no pixels match.

### findClustersMulti
```java
List<ClusterSearchResult> findClustersMulti(Shape shape, List<ClusterQuery> clusterQueries)
```

Finds clusters of pixels based on multiple cluster queries.

**Parameters:**
- `shape` - The shape to search within. If null, searches the entire game screen.
- `clusterQueries` - A list of cluster queries containing search criteria.

**Returns:** A list of ClusterSearchResult objects, each containing the search criteria and the clusters found.

### groupPixels
```java
List<List<Point>> groupPixels(List<Point> pixelsToGroup, double maxDistance, int minGroupSize)
```

Groups a list of points into clusters based on the maximum allowed distance between points and the minimum group size.

**Parameters:**
- `pixelsToGroup` - The list of points to group.
- `maxDistance` - The maximum allowed distance between points in a group.
- `minGroupSize` - The minimum number of points required for a valid group.

**Returns:** A list of clusters where each cluster is a list of points.

### isAnimating
```java
boolean isAnimating(double minDifferenceFactor, Shape shape)
```

Compares the current screen image to the previous cached image within the given shape and checks if the difference percentage exceeds the specified threshold.

**Parameters:**
- `minDifferenceFactor` - The minimum percentage (0.0 - 1.0) of different pixels required to return true.
- `shape` - The bounds where pixel differences are checked. If null, compares the full image.

**Returns:** True if the pixel difference exceeds the threshold, otherwise false.

### isPlayerAnimating
```java
boolean isPlayerAnimating(double minDifferenceFactor)
```

Compares the current screen image to the previous cached image within the bounds of the player and checks if the difference percentage exceeds the specified threshold. This is the method can be debugged inside the visual debug tool, using the 'Scene viewer' plugin.

**Parameters:**
- `minDifferenceFactor` - The minimum percentage (0.0 - 1.0) of different pixels required to return true.

**Returns:** True if the pixel difference exceeds the threshold, otherwise false.

### findRespawnCircles
```java
List<Rectangle> findRespawnCircles()
```

Detects respawn circles inside the provided boundaries.

**Returns:** A list of rectangles representing the bounds of detected respawn circles.

---

```java
List<Rectangle> findRespawnCircles(Rectangle bounds)
```

Detects respawn circles inside the provided boundaries.

**Parameters:**
- `bounds` - The bounds of where to perform detection

**Returns:** A list of rectangles representing the bounds of detected respawn circles.

### findRespawnCircleTypes
```java
List<PixelAnalyzer.RespawnCircle> findRespawnCircleTypes()
```

Finds all respawn circle types within the current screen bounds.

**Returns:** A list of RespawnCircle objects representing the found respawn circles.

---

```java
List<PixelAnalyzer.RespawnCircle> findRespawnCircleTypes(Rectangle bounds)
```

Finds all respawn circle types within the specified bounds.

**Parameters:**
- `bounds` - The rectangle bounds to search within.

**Returns:** A list of RespawnCircle objects representing the found respawn circles.

### getRespawnCircleObjects
```java
Map<RSObject, PixelAnalyzer.RespawnCircle> getRespawnCircleObjects(List<RSObject> objects, PixelAnalyzer.RespawnCircle.DrawType drawType, int zOffset, double distanceTolerance)
```

Maps each given RSObject to its corresponding RespawnCircle, if found. Iterates through the provided list of RSObjects, determines the respawn circle for each object using the specified draw type, z-offset, and distance tolerance, and returns a map of objects to circles. Null objects are skipped. If no objects are provided, returns an empty map.

**Parameters:**
- `objects` - List of RSObjects to process.
- `drawType` - The draw type for respawn circle detection.
- `zOffset` - The z-axis offset for projection.
- `distanceTolerance` - The maximum allowed distance for matching circles.

**Returns:** A map of RSObject to RespawnCircle for all objects with a detected respawn circle.

### getRespawnCircle
```java
PixelAnalyzer.RespawnCircle getRespawnCircle(Rectangle area, PixelAnalyzer.RespawnCircle.DrawType drawType, int zOffset, double distanceTolerance)
```

Finds the respawn circle within the given area using the specified draw type, z-offset, and distance tolerance. Calculates the center point of the area, projects it to screen coordinates, and searches for respawn circles within a padded bounding box around that point. Returns the closest respawn circle whose center or top-center matches the projected point within the given distance tolerance.

**Parameters:**
- `area` - The rectangle area to search for a respawn circle.
- `drawType` - The draw type to use for matching (CENTER or TOP_CENTER).
- `zOffset` - The z-axis offset for projection.
- `distanceTolerance` - The maximum allowed distance for matching circles.

**Returns:** The matching RespawnCircle if found, otherwise null.

### getHighlightBounds
```java
Rectangle getHighlightBounds(Shape shape, SearchablePixel... highlightColors)
```

Finds the bounding rectangle of highlighted pixels within the specified shape.

This method searches for pixels matching any of the provided SearchablePixel criteria within the given shape bounds and returns the smallest rectangle that encompasses all matching pixels. If no shape is provided, searches the entire screen.

If using this method for NPCs - Be sure to disable the tile highlight + name & only highlight the NPC its self

**Parameters:**
- `shape` - The shape to search within. If null, searches the entire screen.
- `highlightColors` - Variable number of SearchablePixel objects defining the colors and tolerance criteria to search for.

**Returns:** A Rectangle representing the bounds of all matching highlighted pixels, or null if no pixels match the criteria.

**Throws:** `IllegalArgumentException` - if bounds exceed screen dimensions
