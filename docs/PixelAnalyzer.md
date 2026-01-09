# PixelAnalyzer Interface

**Package:** `com.osmb.api.visual`

**Type:** Interface

## Overview

Interface for analyzing pixels within images or shapes, detecting clusters, animations, and specific pixel patterns. This is the core visual detection system in OSMB for color-based bot scripting.

## Nested Classes

### PixelAnalyzer.RespawnCircle
Represents a respawn circle with its bounds and type. Used for detecting resource respawn indicators on the game screen.

---

### PixelAnalyzer.RespawnCircleDrawType
Enum defining the type of respawn circle draw style (different visual representations in-game).

---

## Methods

### Pixel Finding Methods

#### `findPixel(Shape shape, SearchablePixel... pixels)`
Finds the first pixel matching the specified criteria within the given shape.

**Parameters:**
- `shape` - The shape to search within
- `pixels` - The pixel search criteria (varargs)

**Returns:** `Point` - A point where the pixel matches, or `null` if no match is found

---

#### `findPixel(Image mainImage, Shape shape, SearchablePixel... pixels)`
Finds the first pixel matching the specified criteria in a given image and shape.

**Parameters:**
- `mainImage` - The image to search in. If `null`, uses the current screen image
- `shape` - The shape to search within
- `pixels` - The pixel search criteria (varargs)

**Returns:** `Point` - A point where the pixel matches, or `null` if no match is found

---

#### `findPixels(Shape shape, SearchablePixel... pixels)`
Finds all pixels matching the specified criteria within the given shape.

**Parameters:**
- `shape` - The shape to search within
- `pixels` - The pixel search criteria (varargs)

**Returns:** `List<Point>` - A list of points where pixels match

---

#### `findPixels(Image mainImage, Shape shape, SearchablePixel... pixels)`
Finds all pixels matching the specified criteria in the given image and shape.

**Parameters:**
- `mainImage` - The image to search in. If `null`, uses the current screen image
- `shape` - The shape to search within
- `pixels` - The pixel search criteria (varargs)

**Returns:** `List<Point>` - A list of points where pixels match

---

### Game Screen Pixel Finding

#### `findPixelsOnGameScreen(Shape shape, SearchablePixel... pixels)`
Finds all pixels matching the specified criteria within the given shape on the game screen, excluding pixels inside widget boundaries.

**Parameters:**
- `shape` - The shape to search within
- `pixels` - The pixel search criteria (varargs)

**Returns:** `List<Point>` - A list of points where the pixels match

**Note:** This method automatically excludes UI widgets, searching only the actual game viewport

---

#### `findPixelsOnGameScreen(int pixelSkipSize, Shape shape, SearchablePixel... pixels)`
Finds all matching pixels within the specified shape and on the game's screen, excluding any pixels inside widget boundaries.

**Parameters:**
- `pixelSkipSize` - The number of pixels to skip during the search for performance optimization
- `shape` - The shape to search within
- `pixels` - The pixel search criteria (varargs)

**Returns:** `List<Point>` - A list of points where the pixels match

**Performance:** Use `pixelSkipSize` > 1 to skip pixels for faster searching (e.g., 2 = check every other pixel)

---

#### `findPixelsOnGameScreenMulti(Shape shape, List<SearchablePixel[]> pixelArrays)`
Efficiently finds all matching pixels for multiple pixel search arrays within the specified shape and on the game's screen, excluding any pixels inside widget boundaries. This method improves performance by querying all pixel arrays in a single iteration over the screen image, instead of performing multiple passes for each array.

**Parameters:**
- `shape` - The shape to search within
- `pixelArrays` - A list of pixel search criteria arrays

**Returns:** `List<List<Point>>` - A list of lists of points, each corresponding to the matches for each pixel array

**Performance:** Much faster than calling `findPixelsOnGameScreen()` multiple times

---

### Pixel Inspection Methods

#### `getPixelAt(int x, int y)`
Retrieves the RGB value of the pixel at the given coordinates.

**Parameters:**
- `x` - The x-coordinate
- `y` - The y-coordinate

**Returns:** `int` - The RGB value at the specified location

---

#### `isPixelAt(int x, int y, SearchablePixel... pixels)`
Checks if a specific pixel at the given coordinates matches any of the specified criteria.

**Parameters:**
- `x` - The x-coordinate
- `y` - The y-coordinate
- `pixels` - The pixel search criteria (varargs)

**Returns:** `boolean` - `true` if a match is found; otherwise, `false`

---

#### `isPixelAt(int x, int y, SearchablePixel searchablePixel)`
Checks if a specific pixel at the given coordinates matches the specified criteria.

**Parameters:**
- `x` - The x-coordinate
- `y` - The y-coordinate
- `searchablePixel` - The pixel search criteria

**Returns:** `boolean` - `true` if a match is found; otherwise, `false`

---

### Clustering Methods

#### `findClusters(PixelCluster.ClusterQuery clusterQuery)`
Finds clusters of pixels based on the provided cluster query.

**Parameters:**
- `clusterQuery` - The cluster query containing search criteria

**Returns:** `PixelCluster.ClusterSearchResult` - A ClusterSearchResult containing the clusters found

---

#### `findClusters(Shape shape, PixelCluster.ClusterQuery clusterQuery)`
Finds clusters of pixels within the specified shape based on the provided cluster query.

**Parameters:**
- `shape` - The shape to search within. If `null`, searches the entire game screen
- `clusterQuery` - The cluster query containing search criteria

**Returns:** `PixelCluster.ClusterSearchResult` - A ClusterSearchResult containing the clusters found

---

#### `findClusters(int pixelSkipSize, Shape shape, PixelCluster.ClusterQuery clusterQuery)`
Finds clusters of pixels within the specified shape that match the given pixel criteria.

**Parameters:**
- `pixelSkipSize` - The number of pixels to skip during the search for performance optimization
- `shape` - The shape to search within. If `null`, searches the entire game screen
- `clusterQuery` - The cluster query containing pixel search criteria

**Returns:** `PixelCluster.ClusterSearchResult` - A ClusterSearchResult containing the clusters found, or `null` if no pixels match

---

#### `findClustersMulti(Shape shape, List<PixelCluster.ClusterQuery> clusterQueries)`
Finds clusters of pixels based on multiple cluster queries.

**Parameters:**
- `shape` - The shape to search within. If `null`, searches the entire game screen
- `clusterQueries` - A list of cluster queries containing search criteria

**Returns:** `List<PixelCluster.ClusterSearchResult>` - A list of ClusterSearchResult objects, each containing the search criteria and the clusters found

**Performance:** More efficient than calling `findClusters()` multiple times

---

#### `groupPixels(List<Point> pixelsToGroup, double maxDistance, int minGroupSize)`
Groups a list of points into clusters based on the maximum allowed distance between points and the minimum group size.

**Parameters:**
- `pixelsToGroup` - List of points to group into clusters
- `maxDistance` - Maximum distance between pixels to be in the same cluster
- `minGroupSize` - Minimum number of pixels required to form a cluster

**Returns:** `List<PixelCluster>` - List of pixel clusters

---

### Animation Detection Methods

#### `isAnimating(double minDifferenceFactor, Shape shape)`
Compares the current screen image to the previous cached image within the given shape and checks if the difference percentage exceeds the specified threshold.

**Parameters:**
- `minDifferenceFactor` - The minimum percentage (0.0 - 1.0) of different pixels required to return `true`
- `shape` - The bounds where pixel differences are checked. If `null`, compares the full image

**Returns:** `boolean` - `true` if the pixel difference exceeds the threshold, otherwise `false`

**Use case:** Detect if an area is animating (e.g., NPC is attacking, object is changing)

---

#### `isPlayerAnimating(double minDifferenceFactor)`
Compares the current screen image to the previous cached image within the bounds of the player and checks if the difference percentage exceeds the specified threshold. This method can be debugged inside the visual debug tool, using the 'Scene viewer' plugin.

**Parameters:**
- `minDifferenceFactor` - The minimum percentage (0.0 - 1.0) of different pixels required to return `true`

**Returns:** `boolean` - `true` if the pixel difference exceeds the threshold, otherwise `false`

**Use case:** Detect if the local player is animating (e.g., attacking, mining, woodcutting)

---

### Highlight Detection Methods

#### `getHighlightBounds(Shape shape, SearchablePixel... highlightColors)`
Finds the bounding rectangle of highlighted pixels within the specified shape.

This method searches for pixels matching any of the provided SearchablePixel criteria within the given shape bounds and returns the smallest rectangle that encompasses all matching pixels. If no shape is provided, searches the entire screen.

**Important:** If using this method for NPCs - Be sure to disable the tile highlight + name & only highlight the NPC itself

**Parameters:**
- `shape` - The shape to search within. If `null`, searches the entire screen
- `highlightColors` - Variable number of SearchablePixel objects defining the highlight colors to search for

**Returns:** `Rectangle` - The bounding rectangle containing all highlighted pixels, or `null` if no highlights found

**Use case:** Get the exact bounds of a highlighted NPC or object for precise interaction

---

### Respawn Circle Detection Methods

#### `findRespawnCircleTypes()`
Finds all respawn circle types within the current screen bounds.

**Returns:** `List<PixelAnalyzer.RespawnCircle>` - A list of RespawnCircle objects representing the found respawn circles

---

#### `findRespawnCircleTypes(Rectangle bounds)`
Finds all respawn circle types within the specified bounds.

**Parameters:**
- `bounds` - The rectangle bounds to search within

**Returns:** `List<PixelAnalyzer.RespawnCircle>` - A list of RespawnCircle objects representing the found respawn circles

---

#### `getRespawnCircle(RectangleArea area, PixelAnalyzer.RespawnCircleDrawType drawType, int zOffset, int distanceTolerance)`
Finds the respawn circle within the given area using the specified draw type, z-offset, and distance tolerance.

Calculates the center point of the area, projects it to screen coordinates, and searches for respawn circles within a padded bounding box around that point. Returns the closest respawn circle whose center or top-center matches the projected point within the given distance tolerance.

**Parameters:**
- `area` - The rectangular area to search within
- `drawType` - The draw type for respawn circle detection
- `zOffset` - The z-axis offset for projection
- `distanceTolerance` - Maximum distance from projected point to circle center

**Returns:** `PixelAnalyzer.RespawnCircle` - The matching respawn circle, or `null` if none found

---

#### `getRespawnCircleObjects(List<RSObject> objects, PixelAnalyzer.RespawnCircleDrawType drawType, int zOffset, int distanceTolerance)`
Maps each given RSObject to its corresponding RespawnCircle, if found.

Iterates through the provided list of RSObjects, determines the respawn circle for each object using the specified draw type, z-offset, and distance tolerance, and returns a map of objects to circles. Null objects are skipped. If no objects are provided, returns an empty map.

**Parameters:**
- `objects` - List of RSObjects to process
- `drawType` - The draw type for respawn circle detection
- `zOffset` - The z-axis offset for projection
- `distanceTolerance` - Maximum distance tolerance

**Returns:** `Map<RSObject, PixelAnalyzer.RespawnCircle>` - Map of objects to their respawn circles

---

### Deprecated Methods

#### `findRespawnCircles()` ⚠️ Deprecated
**Deprecated.** Use `findRespawnCircleTypes()` instead for better type information.

Detects respawn circles inside the provided boundaries.

**Returns:** `List<Rectangle>` - A list of rectangles representing the bounds of detected respawn circles

---

#### `findRespawnCircles(Rectangle bounds)` ⚠️ Deprecated
**Deprecated.** Use `findRespawnCircleTypes(Rectangle)` instead for better type information.

Detects respawn circles inside the provided boundaries.

**Parameters:**
- `bounds` - The bounds of where to perform detection

**Returns:** `List<Rectangle>` - A list of rectangles representing the bounds of detected respawn circles

---

## Usage Examples

### Basic Pixel Finding

```java
PixelAnalyzer analyzer = ctx.getPixelAnalyzer();

// Define the color to search for
SearchablePixel cyanHighlight = new SearchablePixel(180, 100, 57.5, 5); // HSL values

// Find first matching pixel in a shape
Rectangle searchArea = new Rectangle(100, 100, 200, 200);
Point firstMatch = analyzer.findPixel(searchArea, cyanHighlight);

if (firstMatch != null) {
    ctx.mouse.click(firstMatch);
}

// Find all matching pixels
List<Point> allMatches = analyzer.findPixels(searchArea, cyanHighlight);
System.out.println("Found " + allMatches.size() + " matching pixels");
```

### NPC Detection with Clusters

```java
// Search for cyan NPC highlights on game screen
SearchablePixel npcHighlight = new SearchablePixel(180, 100, 57.5, 5);
List<Point> pixels = analyzer.findPixelsOnGameScreen(null, npcHighlight);

// Group pixels into clusters
List<PixelCluster> clusters = analyzer.groupPixels(pixels, 10.0, 20);

// Find the closest cluster
Point playerPos = ctx.players.getLocal().getPosition().toScreen();
PixelCluster closestNPC = clusters.stream()
    .min(Comparator.comparingDouble(c -> 
        playerPos.distance(c.getCenter())))
    .orElse(null);

if (closestNPC != null) {
    ctx.mouse.click(closestNPC.getCenter());
}
```

### Using ClusterQuery for Advanced Detection

```java
// Create a cluster query
SearchablePixel[] highlightColors = {
    new SearchablePixel(180, 100, 57.5, 5) // Cyan
};

PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
    highlightColors,
    10.0,  // maxDistance between pixels
    20     // minClusterSize
);

// Find clusters
PixelCluster.ClusterSearchResult result = analyzer.findClusters(null, query);
List<PixelCluster> clusters = result.getClusters();

// Process clusters
for (PixelCluster cluster : clusters) {
    Rectangle bounds = cluster.getBounds();
    if (bounds.getWidth() >= 20 && bounds.getHeight() >= 20) {
        // Valid NPC size
        ctx.mouse.click(cluster.getCenter());
        break;
    }
}
```

### Animation Detection

```java
// Check if player is animating (mining, attacking, etc.)
boolean playerAnimating = analyzer.isPlayerAnimating(0.1); // 10% threshold

if (!playerAnimating) {
    // Player is idle, click on the next rock
    ctx.mouse.click(nextRock);
}

// Check if a specific area is animating
Rectangle npcArea = new Rectangle(300, 200, 50, 50);
boolean npcAnimating = analyzer.isAnimating(0.05, npcArea); // 5% threshold

if (npcAnimating) {
    // NPC is moving/attacking
    Time.sleep(100);
}
```

### Highlight Bounds Detection

```java
// Get exact bounds of highlighted NPC
// Make sure only NPC highlight is enabled (no tile, no name)
SearchablePixel cyan = new SearchablePixel(180, 100, 57.5, 5);
Rectangle npcBounds = analyzer.getHighlightBounds(null, cyan);

if (npcBounds != null) {
    // Click on the center of the highlighted NPC
    Point center = new Point(
        npcBounds.getX() + npcBounds.getWidth() / 2,
        npcBounds.getY() + npcBounds.getHeight() / 2
    );
    ctx.mouse.click(center);
}
```

### Multi-Target Detection

```java
// Search for multiple different colored highlights at once
List<SearchablePixel[]> multiSearch = Arrays.asList(
    new SearchablePixel[] { new SearchablePixel(180, 100, 57.5, 5) }, // Cyan NPCs
    new SearchablePixel[] { new SearchablePixel(0, 100, 50, 5) },     // Red objects
    new SearchablePixel[] { new SearchablePixel(120, 100, 50, 5) }    // Green items
);

List<List<Point>> results = analyzer.findPixelsOnGameScreenMulti(null, multiSearch);

List<Point> cyanPixels = results.get(0);
List<Point> redPixels = results.get(1);
List<Point> greenPixels = results.get(2);

System.out.println("Found " + cyanPixels.size() + " cyan pixels");
System.out.println("Found " + redPixels.size() + " red pixels");
System.out.println("Found " + greenPixels.size() + " green pixels");
```

### Respawn Circle Detection

```java
// Find all respawn circles on screen
List<PixelAnalyzer.RespawnCircle> circles = analyzer.findRespawnCircleTypes();

for (PixelAnalyzer.RespawnCircle circle : circles) {
    Rectangle bounds = circle.getBounds();
    PixelAnalyzer.RespawnCircleDrawType type = circle.getDrawType();
    System.out.println("Respawn circle at " + bounds + " type: " + type);
}

// Get respawn circle for specific objects
List<RSObject> trees = ctx.objects.getAll(obj -> obj.getName().equals("Tree"));
Map<RSObject, PixelAnalyzer.RespawnCircle> treeCircles = 
    analyzer.getRespawnCircleObjects(
        trees, 
        PixelAnalyzer.RespawnCircleDrawType.DEFAULT,
        0,  // zOffset
        10  // distanceTolerance
    );

// Find trees without respawn circles (available to chop)
List<RSObject> availableTrees = trees.stream()
    .filter(tree -> !treeCircles.containsKey(tree))
    .collect(Collectors.toList());
```

### Performance Optimization with Pixel Skipping

```java
// Skip every other pixel for faster searching
List<Point> pixels = analyzer.findPixelsOnGameScreen(
    2,  // Skip size (checks every 2nd pixel)
    null,
    new SearchablePixel(180, 100, 57.5, 5)
);

// For even faster searching with lower accuracy
List<Point> fastPixels = analyzer.findPixelsOnGameScreen(
    4,  // Skip size (checks every 4th pixel)
    null,
    new SearchablePixel(180, 100, 57.5, 5)
);
```

## Important Notes

- **Performance:** Use pixel skipping for large search areas to improve performance
- **Game Screen vs Full Screen:** `findPixelsOnGameScreen()` automatically excludes UI widgets
- **Multiple Searches:** Use `Multi` variants for better performance when searching multiple patterns
- **Animation Detection:** Requires frame comparison, so ensure enough time between checks
- **Highlight Bounds:** Only works reliably when ONLY the entity highlight is enabled (no tile, no name)
- **HSL Color Space:** OSMB uses HSL (Hue, Saturation, Lightness) for color matching
- **Tolerance:** Use appropriate tolerance values in SearchablePixel for color matching flexibility
- **Null Handling:** Many methods return `null` when no matches found - always null-check

## Related Classes

- `SearchablePixel` - Defines color search criteria with HSL values and tolerance
- `PixelCluster` - Represents a group of clustered pixels
- `PixelCluster.ClusterQuery` - Query parameters for cluster searching
- `PixelCluster.ClusterSearchResult` - Results from cluster searches
- `Shape` / `Rectangle` - Defines search boundaries
- `Image` - Screen or custom images to analyze

## Best Practices

1. **Use appropriate search areas** - Smaller areas = faster searches
2. **Cluster similar pixels** - Group pixels before acting on them
3. **Cache search results** - Don't search every frame if not needed
4. **Use pixel skipping** - For large areas, skip pixels to improve performance
5. **Multi searches** - Use `Multi` variants when searching for multiple patterns
6. **Animation detection** - Check animation before clicking to avoid wasted actions
7. **Validate clusters** - Check cluster size/bounds before using
8. **HSL tuning** - Fine-tune HSL values and tolerance for accurate detection
