# OSMB API - Shapes & Geometry

Geometric shapes for area definitions

---

## Classes in this Module

- [Line](#line) [class]
- [Polygon](#polygon) [class]
- [Rectangle](#rectangle) [class]
- [Shape](#shape) [class]
- [Triangle](#triangle) [class]

---

## Line

**Package:** `com.osmb.api.shape`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getStart()`

**Returns:** `Point`

#### `getEnd()`

**Returns:** `Point`

#### `getRandomPoint()`

**Returns:** `Point`


---

## Polygon

**Package:** `com.osmb.api.shape`

**Type:** Class

**Extends/Implements:** extends Object implements Shape

### Methods

#### `addVertex(int x, int y)`

#### `numVertices()`

**Returns:** `int`

#### `perimeter()`

**Returns:** `double`

#### `calculateArea()`

**Returns:** `double`

#### `convexHull()`

**Returns:** `Polygon`

#### `getXPoints()`

**Returns:** `int[]`

#### `getYPoints()`

**Returns:** `int[]`

#### `getCenter()`

**Returns:** `Point`

#### `contains(int x, int y)`

**Returns:** `boolean`

#### `contains(Point point)`

**Returns:** `boolean`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getRandomPoint()`

**Returns:** `Point`

#### `getResized(double factor)`

**Returns:** `Polygon`

#### `toString()`

**Returns:** `String`

#### `area()`

**Returns:** `double`


---

## Rectangle

**Package:** `com.osmb.api.shape`

**Type:** Class

**Extends/Implements:** extends Object implements Shape

### Fields

- `public int x`
- `public int y`
- `public int width`
- `public int height`

### Methods

#### `contains(Rectangle child)`

**Returns:** `boolean`

#### `intersection(Rectangle rect)`

**Returns:** `Rectangle`

Computes the intersection between this rectangle and the specified rectangle. Returns a new Rectangle representing the overlapping area, or null if there is no intersection. The intersection is calculated by finding the maximum of the left edges (x-coordinates), the maximum of the top edges (y-coordinates), the minimum of the right edges (x + width), and the minimum of the bottom edges (y + height). If the resulting width and height are positive, the rectangles intersect; otherwise, they do not.

**Parameters:**
- `rect` - the other rectangle to compute intersection with (must not be null)

**Returns:** a new Rectangle representing the intersection area, or null if no intersection exists

**Throws:**
- NullPointerException - if the specified rectangle is null

#### `union(Rectangle other)`

**Returns:** `Rectangle`

#### `getPadding(int padding)`

**Returns:** `Rectangle`

#### `getPadding(int top, int left, int bottom, int right)`

**Returns:** `Rectangle`

#### `union(Rectangle other)`

**Returns:** `Rectangle`

#### `intersects(Rectangle other)`

**Returns:** `boolean`

#### `getCenter()`

**Returns:** `Point`

#### `getX()`

**Returns:** `int`

#### `getY()`

**Returns:** `int`

#### `getWidth()`

**Returns:** `int`

#### `getHeight()`

**Returns:** `int`

#### `contains(int x, int y)`

**Returns:** `boolean`

#### `contains(double x, double y)`

**Returns:** `boolean`

#### `contains(Point point)`

**Returns:** `boolean`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getRandomPoint()`

**Returns:** `Point`

#### `getResized(double factor)`

**Returns:** `Rectangle`

#### `getSubRectangle(Rectangle rectangle)`

**Returns:** `Rectangle`

#### `toString()`

**Returns:** `String`


---

## Shape

**Package:** `com.osmb.api.shape`

**Type:** Class

### Methods

#### `contains(int x, int y)`

**Returns:** `boolean`

#### `contains(Point point)`

**Returns:** `boolean`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getRandomPoint()`

**Returns:** `Point`

#### `getCenter()`

**Returns:** `Point`

#### `getResized(double factor)`

**Returns:** `Shape`

#### `insideGameScreenFactor(ScriptCore core)`

**Returns:** `double`

#### `insideGameScreenFactor(ScriptCore core, List<Class<? extends Component>> componentsToSkip)`

**Returns:** `double`


---

## Triangle

**Package:** `com.osmb.api.shape.triangle`

**Type:** Class

**Extends/Implements:** extends Object implements Shape

### Methods

#### `getXPoints()`

**Returns:** `int[]`

#### `getYPoints()`

**Returns:** `int[]`

#### `contains(int x, int y)`

**Returns:** `boolean`

#### `getPoints()`

**Returns:** `Point[]`

#### `contains(Point point)`

**Returns:** `boolean`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getRandomPoint()`

**Returns:** `Point`

#### `getCenter()`

**Returns:** `Point`

#### `getResized(double factor)`

**Returns:** `Triangle`


---

