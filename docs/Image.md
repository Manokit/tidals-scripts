# Image

## Image

**Type:** Class

**Extends:** Object

**Direct Known Subclasses:** ItemImage, SearchableImage, VersionedImage

### Fields

| Type | Field |
|------|-------|
| `int` | `width` |
| `int` | `height` |
| `int[]` | `pixels` |

### Constructors

| Constructor |
|-------------|
| `Image()` |
| `Image(int spriteID, ScriptCore core)` |
| `Image(int[] pixels, int width, int height)` |
| `Image(int width, int height)` |
| `Image(BufferedImage)` |
| `Image(String)` - throws IOException |

### Methods

| Return Type | Method |
|------------|--------|
| `static int[]` | `bufferedImageToPixelArray(BufferedImage image)` |
| `static Image` | `base64ToImage(String)` - throws IOException |
| `Image` | `copy()` |
| `Rectangle` | `getBounds()` |
| `int` | `getHeight()` |
| `int[][]` | `getMask()` |
| `int[]` | `getPixels()` |
| `int` | `getRGB(int x, int y)` |
| `Image` | `getScaled(double percent)` |
| `int` | `getWidth()` |
| `boolean` | `isIdenticalTo(Image other)` |
| `Image` | `resize(int newWidth, int newHeight)` |
| `void` | `setPixels(int[] pixels)` |
| `void` | `setRGB(int x, int y, int rgb)` |
| `void` | `show()` |
| `Image` | `subImage(Rectangle)` |
| `Image` | `subImage(int x, int y, int subWidth, int subHeight)` |
| `BufferedImage` | `toBufferedImage()` |
| `BufferedImage` | `toRGBBufferedImage()` - Used for jpeg's as it doesn't support alphas |
| `SearchableImage` | `toSearchableImage(ToleranceComparator, ColorModel)` |

### Method Details

#### bufferedImageToPixelArray
```java
public static int[] bufferedImageToPixelArray(BufferedImage image)
```

#### base64ToImage
```java
public static Image base64ToImage(String)
```
**Throws:** IOException

#### getScaled
```java
public Image getScaled(double percent)
```

#### resize
```java
public Image resize(int newWidth, int newHeight)
```

#### getBounds
```java
public Rectangle getBounds()
```

#### getMask
```java
public int[][] getMask()
```

#### show
```java
public void show()
```

#### getRGB
```java
public int getRGB(int x, int y)
```

#### setRGB
```java
public void setRGB(int x, int y, int rgb)
```

#### toSearchableImage
```java
public SearchableImage toSearchableImage(ToleranceComparator, ColorModel)
```

#### getPixels
```java
public int[] getPixels()
```

#### setPixels
```java
public void setPixels(int[] pixels)
```

#### getWidth
```java
public int getWidth()
```

#### getHeight
```java
public int getHeight()
```

#### subImage
```java
public Image subImage(Rectangle)
```

```java
public Image subImage(int x, int y, int subWidth, int subHeight)
```

#### copy
```java
public Image copy()
```

#### toBufferedImage
```java
public BufferedImage toBufferedImage()
```

#### toRGBBufferedImage
```java
public BufferedImage toRGBBufferedImage()
```
Used for jpeg's as it doesn't support alphas

#### isIdenticalTo
```java
public boolean isIdenticalTo(Image other)
```

---

## SearchableImage

**Type:** Class

**Extends:** Image

**Direct Known Subclasses:** SearchableItem

### Constructors

| Constructor |
|-------------|
| `SearchableImage(Image)` |
| `SearchableImage(Image, ToleranceComparator)` |
| `SearchableImage(Image, ColorModel)` |
| `SearchableImage(int[] pixels, int width, int height, ToleranceComparator toleranceComparator, ColorModel colorModel)` |
| `SearchableImage(Image, ToleranceComparator, ColorModel)` |
| `SearchableImage(SearchableImage)` |
| `SearchableImage(BufferedImage, ToleranceComparator, ColorModel)` |

### Methods

| Return Type | Method |
|------------|--------|
| `ToleranceComparator` | `getToleranceComparator()` |
| `void` | `setToleranceComparator(ToleranceComparator)` |
| `void` | `setColorModel(ColorModel colorModel)` |
| `SearchablePixel[]` | `getDoesNotContainPixels()` |
| `SearchablePixel[]` | `getDoesContainPixels()` |
| `ColorModel` | `getColorModel()` |
| `SearchableImage` | `subImage(int x, int y, int subWidth, int subHeight)` - overrides Image.subImage() |

### Inherited Methods from Image

base64ToImage, bufferedImageToPixelArray, copy, getBounds, getHeight, getMask, getPixels, getRGB, getScaled, getWidth, isIdenticalTo, resize, setPixels, setRGB, show, subImage, toBufferedImage, toRGBBufferedImage, toSearchableImage

### Method Details

#### getToleranceComparator
```java
public ToleranceComparator getToleranceComparator()
```

#### setToleranceComparator
```java
public void setToleranceComparator(ToleranceComparator)
```

#### setColorModel
```java
public void setColorModel(ColorModel colorModel)
```

#### getDoesNotContainPixels
```java
public SearchablePixel[] getDoesNotContainPixels()
```

#### getDoesContainPixels
```java
public SearchablePixel[] getDoesContainPixels()
```

#### getColorModel
```java
public ColorModel getColorModel()
```

#### subImage
```java
public SearchableImage subImage(int x, int y, int subWidth, int subHeight)
```
**Overrides:** subImage in class Image

---

## ImageAnalyzer

**Type:** Interface

### Methods

| Return Type | Method |
|------------|--------|
| `List<Rectangle>` | `findContainers(Rectangle bounds, int nwSpriteID, int neSpriteID, int swSpriteID, int seSpriteID)` |
| `List<Point>` | `findLocation(SearchableImage)` |
| `List<Point>` | `findLocation(SearchableImage, Shape)` |
| `List<Point>` | `findLocation(Image, SearchableImage, Shape)` |
| `List<Point>` | `findLocations(SearchableImage)` |
| `List<Point>` | `findLocations(SearchableImage, Shape)` |
| `List<Point>` | `findLocations(Image, SearchableImage)` |
| `List<Point>` | `findLocations(Image, SearchableImage, Shape)` |
| `List<Point>` | `findLocations(Image, SearchableImage, Shape, int, int)` |
| `List<Point>` | `findLocationsParallel(Image, SearchableImage, Shape)` |
| `boolean` | `isSubImageAt(Image, SearchableImage, int, int, int, int)` |
| `boolean` | `isSubImageAt(Image mainImage, SearchableImage image, int x, int y, int loopIncrementX, int loopIncrementY)` |
| `boolean` | `preCheck(SearchableImage)` |

### Method Details

#### findLocationsParallel
```java
List<Point> findLocationsParallel(Image, SearchableImage, Shape)
```

#### findLocations
```java
List<Point> findLocations(SearchableImage)
```

```java
List<Point> findLocations(SearchableImage, Shape)
```

```java
List<Point> findLocations(Image, SearchableImage)
```

```java
List<Point> findLocations(Image, SearchableImage, Shape)
```

```java
List<Point> findLocations(Image, SearchableImage, Shape, int, int)
```

#### findContainers
```java
List<Rectangle> findContainers(Rectangle bounds, int nwSpriteID, int neSpriteID, int swSpriteID, int seSpriteID)
```

#### findLocation
```java
List<Point> findLocation(SearchableImage)
```

```java
List<Point> findLocation(SearchableImage, Shape)
```

```java
List<Point> findLocation(Image, SearchableImage, Shape)
```

#### preCheck
```java
boolean preCheck(SearchableImage)
```

#### isSubImageAt
```java
boolean isSubImageAt(Image, SearchableImage, int, int, int, int)
```

```java
boolean isSubImageAt(Image mainImage, SearchableImage image, int x, int y, int loopIncrementX, int loopIncrementY)
```
