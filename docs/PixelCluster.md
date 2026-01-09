# PixelCluster Class

**Package:** `com.osmb.api.visual`

**Type:** Class

**Extends:** `Object`

## Overview

The `PixelCluster` class represents a group of pixels that are clustered together based on proximity. It provides methods for analyzing and working with groups of related pixels, such as finding their center point or bounding box. This is useful for visual detection tasks like identifying game objects or UI elements.

## Nested Classes

### PixelCluster.ClusterQuery
Represents the query parameters for a cluster search, including the maximum distance between pixels to be considered part of the same cluster, the minimum size of a cluster, and the array of searchable pixels.

**Related:** Used to define search criteria when looking for pixel clusters

---

### PixelCluster.ClusterSearchResult
Represents the result of a cluster search, containing the search criteria (pixel array) and the clusters found.

**Related:** Returned by cluster search operations

---

## Constructor

### `PixelCluster(List<Point> points)`
Creates a new pixel cluster from a list of points.

**Parameters:**
- `points` - A list of Point objects representing the pixels in the cluster

---

## Methods

### `getPoints()`
Returns the list of points that make up this cluster.

**Returns:** `List<Point>` - A list of Point objects representing the pixels in this cluster

**Use case:** Access individual pixels in the cluster for further analysis or processing

---

### `getCenter()`
Returns the center point of the cluster, calculated as the average of all points' coordinates.

**Returns:** `Point` - A Point object representing the center of the cluster

**Use case:** Get the approximate center of a detected object for clicking or positioning

---

### `getBounds()`
Returns the bounding rectangle that contains all points in this cluster. The rectangle is defined by its top-left corner (minX, minY) and its width and height.

**Returns:** `Rectangle` - A Rectangle object representing the bounding box of the cluster

**Use case:** Determine the total area occupied by the cluster or check if it overlaps with other regions

---

## Usage Examples

```java
// Create a cluster from detected pixels
List<Point> detectedPixels = new ArrayList<>();
detectedPixels.add(new Point(100, 150));
detectedPixels.add(new Point(101, 150));
detectedPixels.add(new Point(100, 151));
detectedPixels.add(new Point(101, 151));

PixelCluster cluster = new PixelCluster(detectedPixels);

// Get the center point for clicking
Point center = cluster.getCenter();
ctx.mouse.click(center);

// Get all points in the cluster
List<Point> points = cluster.getPoints();
System.out.println("Cluster has " + points.size() + " pixels");

// Get the bounding box
Rectangle bounds = cluster.getBounds();
System.out.println("Cluster bounds: " + bounds.getX() + "," + bounds.getY() + 
                   " size " + bounds.getWidth() + "x" + bounds.getHeight());

// Check if cluster is large enough
if (cluster.getPoints().size() >= 10) {
    // This is a significant cluster
    Point clickPoint = cluster.getCenter();
    // Click on the center
}

// Example: Find the largest cluster from search results
List<PixelCluster> clusters = searchResult.getClusters();
PixelCluster largest = null;
int maxSize = 0;

for (PixelCluster c : clusters) {
    if (c.getPoints().size() > maxSize) {
        maxSize = c.getPoints().size();
        largest = c;
    }
}

if (largest != null) {
    // Click on the largest cluster
    ctx.mouse.click(largest.getCenter());
}
```

## Common Use Cases

### 1. Object Detection
Use pixel clusters to identify game objects by their visual signature:
```java
// After searching for a specific color pattern
List<PixelCluster> clusters = findColorClusters(targetColor);
for (PixelCluster cluster : clusters) {
    Rectangle bounds = cluster.getBounds();
    // Filter by size to identify specific objects
    if (bounds.getWidth() >= 20 && bounds.getHeight() >= 20) {
        // This might be the object we're looking for
        ctx.mouse.click(cluster.getCenter());
    }
}
```

### 2. UI Element Detection
Identify UI elements by clustering their pixels:
```java
PixelCluster buttonCluster = findUICluster();
// Verify the cluster represents a button by checking its bounds
Rectangle bounds = buttonCluster.getBounds();
if (bounds.getWidth() > 50 && bounds.getHeight() > 20) {
    // Likely a button, click its center
    ctx.mouse.click(buttonCluster.getCenter());
}
```

### 3. Multi-Object Targeting
Select from multiple detected objects:
```java
List<PixelCluster> npcs = findNPCClusters();
// Sort by distance from player position
Point playerPos = ctx.players.getLocal().getPosition().toScreen();
PixelCluster closest = npcs.stream()
    .min(Comparator.comparingDouble(c -> 
        playerPos.distance(c.getCenter())))
    .orElse(null);

if (closest != null) {
    ctx.mouse.click(closest.getCenter());
}
```

### 4. Cluster Validation
Validate that a cluster matches expected characteristics:
```java
PixelCluster cluster = detectedCluster;
Rectangle bounds = cluster.getBounds();

// Check if cluster has expected dimensions
boolean validSize = bounds.getWidth() >= 10 && bounds.getWidth() <= 100 &&
                    bounds.getHeight() >= 10 && bounds.getHeight() <= 100;

// Check if cluster has enough pixels
boolean validDensity = cluster.getPoints().size() >= 50;

if (validSize && validDensity) {
    // This cluster matches our criteria
    processCluster(cluster);
}
```

## Important Notes

- The center point is calculated as the arithmetic mean of all pixel coordinates
- The bounding rectangle is the smallest rectangle that contains all cluster points
- Clusters are typically generated from pixel search operations, not created manually
- The order of points in the list is not guaranteed
- All points in a cluster are unique (no duplicates)
- Empty clusters (zero points) should be handled appropriately

## Performance Considerations

- Getting the center and bounds may involve calculations, so cache these values if using repeatedly
- For large clusters (thousands of points), consider the memory overhead
- Cluster size directly impacts the performance of operations on the point list

## Related Classes

- `Point` (java.awt) - Represents individual pixel coordinates
- `Rectangle` - Represents the bounding box
- `PixelCluster.ClusterQuery` - Defines search parameters
- `PixelCluster.ClusterSearchResult` - Contains search results

## Typical Workflow

1. **Search for pixels** using visual detection methods
2. **Cluster the pixels** based on proximity using ClusterQuery
3. **Get ClusterSearchResult** containing all found clusters
4. **Filter clusters** by size, bounds, or other criteria
5. **Select target cluster** (e.g., largest, closest)
6. **Get center point** from selected cluster
7. **Interact** with the center point (click, hover, etc.)
