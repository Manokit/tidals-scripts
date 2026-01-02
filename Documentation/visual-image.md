# OSMB API - Image Analysis

Image analysis and drawing utilities

---

## Classes in this Module

- [BorderPalette](#borderpalette) [class]
- [Canvas](#canvas) [class]
- [Class PixelAnalyzer.RespawnCircle.Type](#class-pixelanalyzer.respawncircle.type) [class]
- [Class PixelAnalyzer.RespawnCircleDrawType](#class-pixelanalyzer.respawncircledrawtype) [class]
- [Image](#image) [class]
- [ImageAnalyzer](#imageanalyzer) [class]
- [ImageImport](#imageimport) [class]
- [ImageSearchResult](#imagesearchresult) [class]
- [ImageUtils](#imageutils) [class]
- [PixelAnalyzer](#pixelanalyzer) [class]
- [PixelAnalyzer.RespawnCircle](#pixelanalyzer.respawncircle) [class]
- [PixelCluster](#pixelcluster) [class]
- [PixelCluster.ClusterQuery](#pixelcluster.clusterquery) [class]
- [PixelCluster.ClusterSearchResult](#pixelcluster.clustersearchresult) [class]
- [SceneProjector](#sceneprojector) [class]
- [SearchableImage](#searchableimage) [class]
- [SearchablePixel](#searchablepixel) [class]
- [VersionedImage](#versionedimage) [class]
- [VisualVerifier](#visualverifier) [class]

---

## BorderPalette

**Package:** `com.osmb.api.visual.drawing`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public static final BorderPalette MODERN_BORDER` - Palette used in the new mobile gameframe widgets.
- `public static final BorderPalette STEEL_BORDER`
- `public static final BorderPalette STONE_BORDER`

### Methods

#### `getTopRightBorderID()`

**Returns:** `int`

#### `getTopLeftBorderID()`

**Returns:** `int`

#### `getBottomLeftBorderID()`

**Returns:** `int`

#### `getBottomRightBorderID()`

**Returns:** `int`

#### `getTopBorderID()`

**Returns:** `int`

#### `getBottomBorderID()`

**Returns:** `int`

#### `getLeftBorderID()`

**Returns:** `int`

#### `getRightBorderID()`

**Returns:** `int`


---

## Canvas

**Package:** `com.osmb.api.visual.drawing`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public int[] pixels`
- `public int canvasWidth`
- `public int canvasHeight`
- `public int drawingAreaLeft`
- `public int drawingAreaTop`
- `public int drawingAreaRight`
- `public int drawingAreaBottom`
- `public int drawAreaWidth`
- `public int drawAreaHeight`

### Methods

#### `replaceAllPixels(int colorToReplace, int replaceWith)`

#### `drawLine(int x1, int y1, int x2, int y2, int rgb)`

#### `drawLine(int x1, int y1, int x2, int y2, int rgb, double opacity)`

Bresenham's line algorithm with opacity.

**Parameters:**
- `x1` - the first x coordinate.
- `y1` - the first y coordinate.
- `x2` - the second x coordinate.
- `y2` - the second y coordinate.
- `rgb` - the color (in ARGB format).
- `opacity` - the opacity (0.0 = fully transparent, 1.0 = fully opaque).

#### `blendColors(int fgColor, int bgColor, double opacity)`

**Returns:** `int`

Blends two colors using the specified opacity.

**Parameters:**
- `fgColor` - the foreground color (in ARGB format).
- `bgColor` - the background color (in ARGB format).
- `opacity` - the opacity (0.0 = fully transparent, 1.0 = fully opaque).

**Returns:** the blended color.

#### `drawLineX(int x, int y, int length, int rgb)`

#### `drawLineX(int x, int y, int length, int rgb, double opacity)`

#### `drawLineY(int x, int y, int length, int rgb)`

#### `drawLineY(int x, int y, int length, int rgb, double opacity)`

#### `drawText(String text, int x, int y, int color, Font font, int maxWidth)`

#### `drawText(String text, int x, int y, int color, Font font)`

#### `getFontMetrics(Font font)`

**Returns:** `FontMetrics`

#### `drawRect(int x, int y, int width, int height, int rgb)`

#### `drawRect(int x, int y, int width, int height, int rgb, double opacity)`

#### `drawRect(Rectangle rectangle, int rgb)`

#### `drawRect(Rectangle rectangle, int rgb, double opacity)`

#### `fillRect(Rectangle rectangle, int rgb)`

#### `fillRect(Rectangle rectangle, int rgb, double opacity)`

#### `fillRect(int x, int y, int width, int height, int rgb)`

#### `fillRect(int x, int y, int width, int height, int rgb, double opacity)`

#### `drawSpritePixelsCentered(SpriteDefinition spriteDefinition)`

Draws a sprite to the center of our canvas draw area.

**Parameters:**
- `spriteDefinition` - The sprite to draw

#### `drawSpritePixelsCentered(int spriteID, ScriptCore core)`

#### `createBackground(ScriptCore core, BorderPalette spritePalette, SpriteDefinition fill)`

#### `createBackgroundCorners(ScriptCore core, BorderPalette spritePalette)`

#### `drawAtOn(Image image, int x, int y)`

#### `drawSpritePixels(ScriptCore core, int spriteID, int x, int y)`

#### `drawSpritePixels(SpriteDefinition spriteDefinition, int x, int y)`

#### `drawSpritePixels(SpriteDefinition spriteDefinition, int x, int y, boolean flipHorizontal, boolean flipVertical)`

#### `drawPixels(int[] pixelsToDraw, int x, int y, int width, int height)`

#### `drawPixels(int[] pixelsToDraw, int x, int y, int width, int height, boolean flipHorizontal, boolean flipVertical)`

#### `drawBorder(int color)`

Creates an outline of the contents inside the canvas, mainly used for drawing item outline borders.

**Parameters:**
- `color` - The color of the outline

#### `getBorderMask(int cropX, int cropY, int cropWidth, int cropHeight)`

**Returns:** `int[]`

#### `drawShadow(int color)`

#### `setPixel(int x, int y, int rgb)`

#### `setDrawArea(int left, int top, int right, int bottom)`

#### `reset(int baseColor)`

#### `toImage()`

**Returns:** `Image`

#### `toVersionedImage(UUID uuid)`

**Returns:** `VersionedImage`

#### `toImageCopy()`

**Returns:** `Image`

#### `toSearchableImage(ToleranceComparator toleranceComparator, ColorModel colorModel)`

**Returns:** `SearchableImage`

#### `drawOval(int x, int y, int width, int height, int rgb)`

Draws the outline of an oval (ellipse) on the canvas.

**Parameters:**
- `x` - The x-coordinate of the top-left corner of the bounding rectangle.
- `y` - The y-coordinate of the top-left corner of the bounding rectangle.
- `width` - The width of the bounding rectangle.
- `height` - The height of the bounding rectangle.
- `rgb` - The color of the oval.

#### `setRGB(int x, int y, int rgb, double opacity)`

#### `drawPolygon(Polygon polygon, int rgb, double opacity)`

#### `drawPolygon(int[] xPoints, int[] yPoints, int nPoints, int rgb, double opacity)`

#### `drawPolygon(int[] xPoints, int[] yPoints, int nPoints, int rgb)`

#### `fillPolygon(Polygon polygon, int rgb, double opacity)`

#### `fillPolygon(int[] xPoints, int[] yPoints, int nPoints, int rgb, double opacity)`


---

## Class PixelAnalyzer.RespawnCircle.Type

**Package:** `com.osmb.api.visual`

**Type:** Class

**Extends/Implements:** extends Enum<PixelAnalyzer.RespawnCircle.Type>

### Methods

#### `values()`

**Returns:** `PixelAnalyzer.RespawnCircle.Type[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `PixelAnalyzer.RespawnCircle.Type`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Class PixelAnalyzer.RespawnCircleDrawType

**Package:** `com.osmb.api.visual`

**Type:** Class

**Extends/Implements:** extends Enum<PixelAnalyzer.RespawnCircleDrawType>

### Methods

#### `values()`

**Returns:** `PixelAnalyzer.RespawnCircleDrawType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `PixelAnalyzer.RespawnCircleDrawType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Image

**Package:** `com.osmb.api.visual.image`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public int width`
- `public int height`
- `public int[] pixels`

### Methods

#### `bufferedImageToPixelArray(BufferedImage image)`

**Returns:** `int[]`

#### `base64ToImage(String base64String)`

**Returns:** `Image`

**Throws:**
- IOException

#### `getScaled(double percent)`

**Returns:** `Image`

#### `resize(int newWidth, int newHeight)`

**Returns:** `Image`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getMask()`

**Returns:** `int[][]`

#### `show()`

#### `getRGB(int x, int y)`

**Returns:** `int`

#### `setRGB(int x, int y, int rgb)`

#### `toSearchableImage(ToleranceComparator toleranceComparator, ColorModel colorModel)`

**Returns:** `SearchableImage`

#### `getPixels()`

**Returns:** `int[]`

#### `setPixels(int[] pixels)`

#### `getWidth()`

**Returns:** `int`

#### `getHeight()`

**Returns:** `int`

#### `subImage(Rectangle rectangle)`

**Returns:** `Image`

#### `subImage(int x, int y, int subWidth, int subHeight)`

**Returns:** `Image`

#### `copy()`

**Returns:** `Image`

#### `toBufferedImage()`

**Returns:** `BufferedImage`

#### `toRGBBufferedImage()`

**Returns:** `BufferedImage`

Used for jpeg's as it doesn't support alphas

#### `isIdenticalTo(Image other)`

**Returns:** `boolean`


---

## ImageAnalyzer

**Package:** `com.osmb.api.visual`

**Type:** Class

### Methods

#### `findLocationsParallel(Image mainImage, SearchableImage subImage)`

**Returns:** `List<ImageSearchResult>`

#### `findLocations(Image mainImage, SearchableImage... images)`

**Returns:** `List<ImageSearchResult>`

#### `findLocations(SearchableImage... images)`

**Returns:** `List<ImageSearchResult>`

#### `findLocations(List<SearchableImage> searchableImages)`

**Returns:** `List<ImageSearchResult>`

#### `findLocations(Shape shape, SearchableImage... images)`

**Returns:** `List<ImageSearchResult>`

#### `findContainers(Rectangle bounds, int nwSpriteID, int neSpriteID, int swSpriteID, int seSpriteID)`

**Returns:** `List<Rectangle>`

#### `findLocations(Image mainImage, Shape shape, SearchableImage... images)`

**Returns:** `List<ImageSearchResult>`

#### `findLocation(SearchableImage... images)`

**Returns:** `ImageSearchResult`

#### `findLocation(List<SearchableImage> searchableImages)`

**Returns:** `ImageSearchResult`

#### `findLocation(Shape shape, SearchableImage... images)`

**Returns:** `ImageSearchResult`

#### `preCheck(Image mainImage, int x, int y, SearchableImage searchableImage)`

**Returns:** `boolean`

#### `isSubImageAt(Point location, SearchableImage subImage)`

**Returns:** `ImageSearchResult`

#### `isSubImageAt(int x, int y, SearchableImage subImage)`

**Returns:** `ImageSearchResult`

#### `isSubImageAt(Image mainImage, SearchableImage image, int x, int y)`

**Returns:** `ImageSearchResult`

#### `isSubImageAt(Image mainImage, SearchableImage image, int x, int y, int loopIncrementX, int loopIncrementY)`

**Returns:** `ImageSearchResult`


---

## ImageImport

**Package:** `com.osmb.api.visual.image`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getImageName()`

**Returns:** `String`

#### `getToleranceComarator()`

**Returns:** `ToleranceComparator`

#### `getColorModel()`

**Returns:** `ColorModel`


---

## ImageSearchResult

**Package:** `com.osmb.api.visual.image`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `protected final int x`
- `protected final int y`
- `protected final int width`
- `protected final int height`

### Methods

#### `getScore()`

**Returns:** `double`

#### `getSearchTime()`

**Returns:** `long`

#### `getScreenUpdateUUID()`

**Returns:** `UUID`

#### `getAsPoint()`

**Returns:** `Point`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getX()`

**Returns:** `int`

#### `getY()`

**Returns:** `int`

#### `getWidth()`

**Returns:** `int`

#### `getHeight()`

**Returns:** `int`

#### `toString()`

**Returns:** `String`

#### `getFoundImage()`

**Returns:** `SearchableImage`


---

## ImageUtils

**Package:** `com.osmb.api.visual.image`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `compareImages(Image image1, Image image2)`

**Returns:** `int`

Compares two images pixel by pixel and returns the count of different pixels.

**Parameters:**
- `image1` - The first image to compare.
- `image2` - The second image to compare.

**Returns:** The count of different pixels between the two images.

**Throws:**
- IllegalArgumentException - If either image is null.

#### `rotate(Image image, int yaw)`

**Returns:** `Image`

#### `rotate(Image image, int anchorX, int anchorY, int yaw, int zoom)`

**Returns:** `Image`


---

## PixelAnalyzer

**Package:** `com.osmb.api.visual`

**Type:** Class

Interface for analyzing pixels within images or shapes, detecting clusters, animations, and specific pixel patterns.

### Methods

#### `findPixel(Shape shape, SearchablePixel... pixels)`

**Returns:** `Point`

Finds the first pixel matching the specified criteria within the given shape.

**Parameters:**
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A point where the pixel matches, or null if no match is found.

#### `findPixel(Image mainImage, Shape shape, SearchablePixel... pixels)`

**Returns:** `Point`

Finds the first pixel matching the specified criteria in a given image and shape.

**Parameters:**
- `mainImage` - The image to search in. If null, uses the current screen image.
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A point where the pixel matches, or null if no match is found.

#### `findPixels(Shape shape, SearchablePixel... pixels)`

**Returns:** `List<Point>`

Finds all pixels matching the specified criteria within the given shape.

**Parameters:**
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A list of points where pixels match.

#### `findPixels(Image mainImage, Shape shape, SearchablePixel... pixels)`

**Returns:** `List<Point>`

Finds all pixels matching the specified criteria in the given image and shape.

**Parameters:**
- `mainImage` - The image to search in. If null, uses the current screen image.
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A list of points where pixels match.

#### `getPixelAt(int x, int y)`

**Returns:** `int`

Retrieves the RGB value of the pixel at the given coordinates.

**Parameters:**
- `x` - The x-coordinate.
- `y` - The y-coordinate.

**Returns:** The RGB value at the specified location.

#### `isPixelAt(int x, int y, SearchablePixel... pixels)`

**Returns:** `boolean`

Checks if a specific pixel at the given coordinates matches any of the specified criteria.

**Parameters:**
- `x` - The x-coordinate.
- `y` - The y-coordinate.
- `pixels` - The pixel search criteria.

**Returns:** True if a match is found; otherwise, false.

#### `isPixelAt(int x, int y, SearchablePixel searchablePixel)`

**Returns:** `boolean`

Checks if a specific pixel at the given coordinates matches the specified criteria.

**Parameters:**
- `x` - The x-coordinate.
- `y` - The y-coordinate.
- `searchablePixel` - The pixel search criteria.

**Returns:** True if a match is found; otherwise, false.

#### `findPixelsOnGameScreen(Shape shape, SearchablePixel... pixels)`

**Returns:** `List<Point>`

Finds all pixels matching the specified criteria within the given shape on the game screen, excluding pixels inside widget boundaries.

**Parameters:**
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A list of points where the pixels match.

#### `findPixelsOnGameScreen(int pixelSkipSize, Shape shape, SearchablePixel... pixels)`

**Returns:** `List<Point>`

Finds all matching pixels within the specified shape and on the game's screen, excluding any pixels inside widget boundaries.

**Parameters:**
- `pixelSkipSize` - The number of pixels to skip during the search for performance optimization.
- `shape` - The shape to search within.
- `pixels` - The pixel search criteria.

**Returns:** A list of points where the pixels match.

#### `findPixelsOnGameScreenMulti(Shape shape, List<SearchablePixel[]> pixelArrays)`

**Returns:** `List<List<Point>>`

Efficiently finds all matching pixels for multiple pixel search arrays within the specified shape and on the game's screen, excluding any pixels inside widget boundaries. This method improves performance by querying all pixel arrays in a single iteration over the screen image, instead of performing multiple passes for each array.

**Parameters:**
- `shape` - The shape to search within.
- `pixelArrays` - A list of pixel search criteria arrays.

**Returns:** A list of lists of points, each corresponding to the matches for each pixel array.

#### `findClusters(PixelCluster.ClusterQuery clusterQuery)`

**Returns:** `PixelCluster.ClusterSearchResult`

Finds clusters of pixels based on the provided cluster query.

**Parameters:**
- `clusterQuery` - The cluster query containing search criteria.

**Returns:** A ClusterSearchResult containing the clusters found.

#### `findClusters(Shape shape, PixelCluster.ClusterQuery clusterQuery)`

**Returns:** `PixelCluster.ClusterSearchResult`

Finds clusters of pixels within the specified shape based on the provided cluster query.

**Parameters:**
- `shape` - The shape to search within. If null, searches the entire game screen.
- `clusterQuery` - The cluster query containing search criteria.

**Returns:** A ClusterSearchResult containing the clusters found.

#### `findClusters(int pixelSkipSize, Shape shape, PixelCluster.ClusterQuery clusterQuery)`

**Returns:** `PixelCluster.ClusterSearchResult`

Finds clusters of pixels within the specified shape that match the given pixel criteria.

**Parameters:**
- `pixelSkipSize` - The number of pixels to skip during the search for performance optimization.
- `shape` - The shape to search within. If null, searches the entire game screen.
- `clusterQuery` - The cluster query containing pixel search criteria.

**Returns:** A ClusterSearchResult containing the clusters found, or null if no pixels match.

#### `findClustersMulti(Shape shape, List<PixelCluster.ClusterQuery> clusterQueries)`

**Returns:** `List<PixelCluster.ClusterSearchResult>`

Finds clusters of pixels based on multiple cluster queries.

**Parameters:**
- `shape` - The shape to search within. If null, searches the entire game screen.
- `clusterQueries` - A list of cluster queries containing search criteria.

**Returns:** A list of ClusterSearchResult objects, each containing the search criteria and the clusters found.

#### `groupPixels(List<Point> pixelsToGroup, double maxDistance, int minGroupSize)`

**Returns:** `List<PixelCluster>`

Groups a list of points into clusters based on the maximum allowed distance between points and the minimum group size.

**Parameters:**
- `pixelsToGroup` - The list of points to group.
- `maxDistance` - The maximum allowed distance between points in a group.
- `minGroupSize` - The minimum number of points required for a valid group.

**Returns:** A list of clusters where each cluster is a list of points.

#### `isAnimating(double minDifferenceFactor, Shape shape)`

**Returns:** `boolean`

Compares the current screen image to the previous cached image within the given shape and checks if the difference percentage exceeds the specified threshold.

**Parameters:**
- `minDifferenceFactor` - The minimum percentage (0.0 - 1.0) of different pixels required to return true.
- `shape` - The bounds where pixel differences are checked. If null, compares the full image.

**Returns:** True if the pixel difference exceeds the threshold, otherwise false.

#### `isPlayerAnimating(double minDifferenceFactor)`

**Returns:** `boolean`

Compares the current screen image to the previous cached image within the bounds of the player and checks if the difference percentage exceeds the specified threshold. This is the method can be debugged inside the visual debug tool, using the 'Scene viewer' plugin.

**Parameters:**
- `minDifferenceFactor` - The minimum percentage (0.0 - 1.0) of different pixels required to return true.

**Returns:** True if the pixel difference exceeds the threshold, otherwise false.

#### `findRespawnCircles()`

**Returns:** `List<Rectangle>`

Detects respawn circles inside the provided boundaries.

**Returns:** A list of rectangles representing the bounds of detected respawn circles.

#### `findRespawnCircles(Rectangle bounds)`

**Returns:** `List<Rectangle>`

Detects respawn circles inside the provided boundaries.

**Parameters:**
- `bounds` - The bounds of where to perform detection

**Returns:** A list of rectangles representing the bounds of detected respawn circles.

#### `findRespawnCircleTypes()`

**Returns:** `List<PixelAnalyzer.RespawnCircle>`

Finds all respawn circle types within the current screen bounds.

**Returns:** A list of RespawnCircle objects representing the found respawn circles.

#### `findRespawnCircleTypes(Rectangle bounds)`

**Returns:** `List<PixelAnalyzer.RespawnCircle>`

Finds all respawn circle types within the specified bounds.

**Parameters:**
- `bounds` - The rectangle bounds to search within.

**Returns:** A list of RespawnCircle objects representing the found respawn circles.

#### `getRespawnCircleObjects(List<RSObject> objects, PixelAnalyzer.RespawnCircleDrawType drawType, int zOffset, int distanceTolerance)`

**Returns:** `Map<RSObject,PixelAnalyzer.RespawnCircle>`

Maps each given RSObject to its corresponding RespawnCircle, if found. Iterates through the provided list of RSObjects, determines the respawn circle for each object using the specified draw type, z-offset, and distance tolerance, and returns a map of objects to circles. Null objects are skipped. If no objects are provided, returns an empty map.

**Parameters:**
- `objects` - List of RSObjects to process.
- `drawType` - The draw type for respawn circle detection.
- `zOffset` - The z-axis offset for projection.
- `distanceTolerance` - The maximum allowed distance for matching circles.

**Returns:** A map of RSObject to RespawnCircle for all objects with a detected respawn circle.

#### `getRespawnCircle(RectangleArea area, PixelAnalyzer.RespawnCircleDrawType drawType, int zOffset, int distanceTolerance)`

**Returns:** `PixelAnalyzer.RespawnCircle`

Finds the respawn circle within the given area using the specified draw type, z-offset, and distance tolerance. Calculates the center point of the area, projects it to screen coordinates, and searches for respawn circles within a padded bounding box around that point. Returns the closest respawn circle whose center or top-center matches the projected point within the given distance tolerance.

**Parameters:**
- `area` - The rectangle area to search for a respawn circle.
- `drawType` - The draw type to use for matching (CENTER or TOP_CENTER).
- `zOffset` - The z-axis offset for projection.
- `distanceTolerance` - The maximum allowed distance for matching circles.

**Returns:** The matching RespawnCircle if found, otherwise null.

#### `getHighlightBounds(Shape shape, SearchablePixel... highlightColors)`

**Returns:** `Rectangle`

Finds the bounding rectangle of highlighted pixels within the specified shape. This method searches for pixels matching any of the provided SearchablePixel criteria within the given shape bounds and returns the smallest rectangle that encompasses all matching pixels. If no shape is provided, searches the entire screen. If using this method for NPCs - Be sure to disable the tile highlight + name & only highlight the NPC its self

**Parameters:**
- `shape` - The shape to search within. If null, searches the entire screen.
- `highlightColors` - Variable number of SearchablePixel objects defining the colors and tolerance criteria to search for.

**Returns:** A Rectangle representing the bounds of all matching highlighted pixels, or null if no pixels match the criteria.

**Throws:**
- IllegalArgumentException - if bounds exceed screen dimensions


---

## PixelAnalyzer.RespawnCircle

**Package:** `com.osmb.api.visual`

**Type:** Class

Represents a respawn circle with its bounds and type.

**Extends/Implements:** extends Object

### Methods

#### `getBounds()`

**Returns:** `Rectangle`

Returns the bounds of the respawn circle.

**Returns:** The bounds of the respawn circle.

#### `getType()`

**Returns:** `PixelAnalyzer.RespawnCircle.Type`

Returns the type (color) of the respawn circle.

**Returns:** The type of the respawn circle.


---

## PixelCluster

**Package:** `com.osmb.api.visual`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getPoints()`

**Returns:** `List<Point>`

Returns the list of points that make up this cluster.

**Returns:** A list of Point objects representing the pixels in this cluster.

#### `getCenter()`

**Returns:** `Point`

Returns the center point of the cluster, calculated as the average of all points' coordinates.

**Returns:** A Point object representing the center of the cluster.

#### `getBounds()`

**Returns:** `Rectangle`

Returns the bounding rectangle that contains all points in this cluster. The rectangle is defined by its top-left corner (minX, minY) and its width and height.

**Returns:** A Rectangle object representing the bounding box of the cluster.


---

## PixelCluster.ClusterQuery

**Package:** `com.osmb.api.visual`

**Type:** Class

Represents the query parameters for a cluster search, including the maximum distance between pixels to be considered part of the same cluster, the minimum size of a cluster, and the array of searchable pixels.

**Extends/Implements:** extends Object

### Methods

#### `getSearchablePixels()`

**Returns:** `SearchablePixel[]`

Returns the array of searchable pixels used in the cluster search.

**Returns:** An array of SearchablePixel objects.

#### `getMaxDistance()`

**Returns:** `int`

Returns the maximum distance between pixels to be considered part of the same cluster.

**Returns:** The maximum distance as an integer.

#### `getMinSize()`

**Returns:** `int`

Returns the minimum size of a cluster to be considered valid.

**Returns:** The minimum size as an integer.


---

## PixelCluster.ClusterSearchResult

**Package:** `com.osmb.api.visual`

**Type:** Class

Represents the result of a cluster search, containing the search criteria (pixel array) and the clusters found.

**Extends/Implements:** extends Object

### Fields

- `public final PixelCluster.ClusterQuery searchCriteria`
- `public final List<PixelCluster> clusters`

### Methods

#### `getClusters()`

**Returns:** `List<PixelCluster>`

Returns the clusters found during the search.

**Returns:** A list of clusters, where each cluster is a list of points.

#### `getSearchCriteria()`

**Returns:** `PixelCluster.ClusterQuery`

Returns the search criteria used for this cluster search. This can be used to distinguish the cluster results between the passed pixel arrays.

**Returns:** An array of SearchablePixel objects representing the search criteria.


---

## SceneProjector

**Package:** `com.osmb.api.visual.drawing`

**Type:** Class

### Fields

- `static final int TILE_SIZE`
- `static final int TILE_FLAG_BRIDGE`
- `static final int TILE_FLAG_UNDER_ROOF`
- `static final int NOT_VISIBLE_ABOVE`

### Methods

#### `getConvexHull(RSObject object)`

**Returns:** `Polygon`

Computes the convex hull of the projected 2D points of the given RSObject's model. The convex hull is calculated from all visible face vertices of the model, projected onto the canvas. Faces with a faceInfo value of -1 are skipped.

**Parameters:**
- `object` - the RSObject whose model's convex hull to compute

**Returns:** a Polygon representing the convex hull of the object's projected model, or null if the model is not available or fewer than 3 points are found

#### `getFaceTriangles(RSObject object)`

**Returns:** `List<Triangle>`

Returns a list of triangles representing the faces of the given RSObject model, projected onto the 2D canvas.

**Parameters:**
- `object` - the RSObject whose model faces to project

**Returns:** a list of Triangle objects representing the projected faces, or null if the model is not available or no valid faces are found

#### `getTilePoly(Position position)`

**Returns:** `Polygon`

Returns a Polygon representing the 2D canvas projection of the tile at the given position.

**Parameters:**
- `position` - the Position of the tile (can be WorldPosition or LocalPosition).

**Returns:** a Polygon representing the projected tile, or null if the position is invalid or not visible

#### `getTilePoly(Position position, boolean fullyOnScreen)`

**Returns:** `Polygon`

Returns a Polygon representing the 2D canvas projection of the tile at the given position.

**Parameters:**
- `position` - the Position of the tile (can be WorldPosition or LocalPosition)
- `fullyOnScreen` - if true, returns null unless all projected points are on screen

**Returns:** a Polygon representing the projected tile, or null if the position is invalid or not visible

#### `getTilePoly(double localX, double localY, int plane)`

**Returns:** `Polygon`

Returns a Polygon representing the 2D canvas projection of a tile at the given local coordinates and plane.

**Parameters:**
- `localX` - the local X coordinate of the tile (may be fractional, in tile units)
- `localY` - the local Y coordinate of the tile (may be fractional, in tile units)
- `plane` - the plane (height level) of the tile

**Returns:** a Polygon representing the projected tile, or null if the position is invalid or not visible

#### `getTilePoly(double localX, double localY, int plane, boolean fullyOnScreen)`

**Returns:** `Polygon`

Returns a Polygon representing the 2D canvas projection of a tile at the given local coordinates and plane.

**Parameters:**
- `localX` - The local X coordinate of the tile (may be fractional, in tile units).
- `localY` - The local Y coordinate of the tile (may be fractional, in tile units).
- `plane` - The plane (height level) of the tile.
- `fullyOnScreen` - If true, returns null unless all projected points are on screen.

**Returns:** A Polygon representing the projected tile, or null if the position is invalid or not visible.

#### `getAreaCenter(double localX, double localY, int tileWidth, int tileHeight, int plane, int zHeight)`

**Returns:** `Point`

Calculates the 2D canvas point for the center of a rectangular area covering multiple tiles. The center is computed based on the provided local tile coordinates, area width and height (in tiles), plane, and an optional Z height offset. The method projects the center of the area to canvas coordinates, taking into account the scene's heightmap and camera position.

**Parameters:**
- `localX` - The local X coordinate (tile units, may be fractional) of the area's starting tile.
- `localY` - The local Y coordinate (tile units, may be fractional) of the area's starting tile.
- `tileWidth` - The width of the area in tiles.
- `tileHeight` - The height of the area in tiles.
- `plane` - The plane (height level) of the area.
- `zHeight` - The Z offset to subtract from the area's height (for elevation adjustment).

**Returns:** The projected Point on the canvas representing the area's center, or null if not visible.

#### `getAreaCenter(double localX, double localY, int tileWidth, int tileHeight, int plane, int xOffset, int yOffset, int zOffset)`

**Returns:** `Point`

Calculates the 2D canvas point for the center of a rectangular area covering multiple tiles. The center is computed based on the provided local tile coordinates, area width and height (in tiles), plane, and an optional Z height offset. The method projects the center of the area to canvas coordinates, taking into account the scene's heightmap and camera position.

**Parameters:**
- `localX` - The local X coordinate (tile units, may be fractional) of the area's starting tile.
- `localY` - The local Y coordinate (tile units, may be fractional) of the area's starting tile.
- `tileWidth` - The width of the area in tiles.
- `tileHeight` - The height of the area in tiles.
- `plane` - The plane (height level) of the area.
- `zOffset` - The Z offset to subtract from the area's height (for elevation adjustment).

**Returns:** The projected Point on the canvas representing the area's center, or null if not visible.

#### `getTilePoint(double localX, double localY, int plane, TileEdge edge)`

**Returns:** `Point`

Projects a specific point on a tile (center or edge) to 2D canvas coordinates.

**Parameters:**
- `localX` - The local X coordinate (tile units, may be fractional).
- `localY` - The local Y coordinate (tile units, may be fractional).
- `plane` - The plane (height level) of the tile.
- `edge` - The TileEdge specifying which edge or corner to project, or null for the tile center.

**Returns:** The projected Point on the canvas, or null if the position is invalid or not visible.

#### `getTilePoint(Position position, TileEdge edge, int zOffset)`

**Returns:** `Point`

Projects a specific point on a tile (center or edge) to 2D canvas coordinates.

**Parameters:**
- `position` - The Position of the tile (can be WorldPosition or LocalPosition)
- `edge` - The TileEdge specifying which edge or corner to project, or null for the tile center.
- `zOffset` - The Z offset to subtract from the tile's height (for elevation adjustment).

**Returns:** The projected Point on the canvas, or null if the position is invalid or not visible.

#### `getTilePoint(double localX, double localY, int plane, TileEdge edge, int zOffset)`

**Returns:** `Point`

Projects a specific point on a tile (center or edge) to 2D canvas coordinates.

**Parameters:**
- `localX` - The local X coordinate (tile units, may be fractional).
- `localY` - The local Y coordinate (tile units, may be fractional).
- `plane` - The plane (height level) of the tile.
- `edge` - The TileEdge specifying which edge or corner to project, or null for the tile center.
- `zOffset` - The Z offset to subtract from the tile's height (for elevation adjustment).

**Returns:** The projected Point on the canvas, or null if the position is invalid or not visible.

#### `getTileCube(Position position, int cubeHeight)`

**Returns:** `Polygon`

Projects a 3D tile-aligned cube at the given position to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `position` - The Position of the tile (can be WorldPosition or LocalPosition)
- `cubeHeight` - The height of the cube to project (in scene units).

**Returns:** A Polygon representing the projected cube, or null if not visible or invalid.

#### `getTileCube(Position position, int cubeHeight, boolean fullyOnScreen)`

**Returns:** `Polygon`

Projects a 3D tile-aligned cube to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `position` - The Position of the tile (can be WorldPosition or LocalPosition)
- `cubeHeight` - The height of the cube to project (in scene units).
- `fullyOnScreen` - If true, returns null unless all projected points are on screen.

**Returns:** A Polygon representing the projected cube, or null if not visible or invalid.

#### `getTileCube(double localX, double localY, int plane, int cubeHeight)`

**Returns:** `Polygon`

Projects a 3D tile-aligned cube to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `localX` - The local X coordinate of the tile (tile units).
- `localY` - The local Y coordinate of the tile (tile units).
- `plane` - The plane (height level) of the tile.
- `cubeHeight` - The height of the cube to project (in scene units).

**Returns:** A Polygon representing the projected cube, or null if not all points or off-screen or invalid.

#### `getTileCube(double localX, double localY, int plane, int baseHeight, int cubeHeight)`

**Returns:** `Polygon`

Projects a 3D tile-aligned cube to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `localX` - The local X coordinate of the tile (tile units).
- `localY` - The local Y coordinate of the tile (tile units).
- `plane` - The plane (height level) of the tile.
- `baseHeight` - The base height offset to subtract from the tile's ground height.
- `cubeHeight` - The height of the cube to project (in scene units).

**Returns:** A Polygon representing the projected cube, or null if not all points or off-screen or invalid.

#### `getTileCube(double localX, double localY, int plane, int baseHeight, int cubeHeight, boolean fullyOnScreen)`

**Returns:** `Polygon`

Projects a 3D tile-aligned cube to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `localX` - The local X coordinate of the tile (tile units).
- `localY` - The local Y coordinate of the tile (tile units).
- `plane` - The plane (height level) of the tile.
- `baseHeight` - The base height offset to subtract from the tile's ground height.
- `cubeHeight` - The height of the cube to project (in scene units).
- `fullyOnScreen` - If true, returns null unless all projected points are on screen.

**Returns:** A Polygon representing the projected cube, or null if not all points or off-screen or invalid.

#### `getTileCube(double localX, double localY, int plane, int baseHeight, int cubeHeight, int tileWidth, int tileHeight, boolean fullyOnScreen)`

**Returns:** `Polygon`

Projects a 3D rectangular prism (cube or cuboid) covering multiple tiles to 2D canvas coordinates and returns its polygon.

**Parameters:**
- `localX` - The local X coordinate of the starting tile (tile units).
- `localY` - The local Y coordinate of the starting tile (tile units).
- `plane` - The plane (height level) of the tile.
- `baseHeight` - The base height offset to subtract from the tile's ground height.
- `cubeHeight` - The height of the cube to project (in scene units).
- `tileWidth` - The width of the cube in tiles.
- `tileHeight` - The height of the cube in tiles.
- `fullyOnScreen` - If true, returns null unless all projected points are on screen.

**Returns:** A Polygon representing the projected cube, or null if not all points or off-screen or invalid.

#### `sceneToCanvas(int sceneX, int sceneY, int sceneZ)`

**Returns:** `Point`

Projects a 3D scene coordinate to 2D canvas coordinates.

**Parameters:**
- `sceneX` - the X coordinate in scene space
- `sceneY` - the Y coordinate in scene space
- `sceneZ` - the Z coordinate (height) in scene space

**Returns:** a Point representing the projected 2D canvas coordinates, or null if the point is not visible or outside the scene bounds


---

## SearchableImage

**Package:** `com.osmb.api.visual.image`

**Type:** Class

**Extends/Implements:** extends Image

### Methods

#### `getToleranceComparator()`

**Returns:** `ToleranceComparator`

#### `setToleranceComparator(ToleranceComparator toleranceComparator)`

#### `setColorModel(ColorModel colorModel)`

#### `getDoesNotContainPixels()`

**Returns:** `List<SearchablePixel>`

#### `getDoesContainPixels()`

**Returns:** `List<SearchablePixel>`

#### `getColorModel()`

**Returns:** `ColorModel`

#### `subImage(int x, int y, int subWidth, int subHeight)`

**Returns:** `SearchableImage`


---

## SearchablePixel

**Package:** `com.osmb.api.visual`

**Type:** Class

**Extends/Implements:** extends Pixel

### Methods

#### `getToleranceComparator()`

**Returns:** `ToleranceComparator`

#### `getColorModel()`

**Returns:** `ColorModel`

#### `toString()`

**Returns:** `String`


---

## VersionedImage

**Package:** `com.osmb.api.visual.image`

**Type:** Class

**Extends/Implements:** extends Image

### Methods

#### `getUuid()`

**Returns:** `UUID`


---

## VisualVerifier

**Package:** `com.osmb.api.visual`

**Type:** Class

### Methods

#### `verify(ScriptCore core, Shape shape)`

**Returns:** `boolean`


---

