# ImageSearchResult

**Type:** Class

**Extends:** Object

**Direct Known Subclasses:** ComponentSearchResult, ItemSearchResult

## Fields

| Type | Field |
|------|-------|
| `protected final int` | `x` |
| `protected final int` | `y` |
| `protected final int` | `width` |
| `protected final int` | `height` |

## Constructors

| Constructor |
|-------------|
| `ImageSearchResult()` |
| `ImageSearchResult(SearchableImage foundImage, int x, int y, int w, int h, double score, UUID screenUpdateUUID, long searchTime)` |

## Methods

| Return Type | Method |
|------------|--------|
| `Point` | `getAsPoint()` |
| `Rectangle` | `getBounds()` |
| `SearchableImage` | `getFoundImage()` |
| `int` | `getHeight()` |
| `double` | `getScore()` |
| `UUID` | `getScreenUpdateUUID()` |
| `long` | `getSearchTime()` |
| `int` | `getWidth()` |
| `int` | `getX()` |
| `int` | `getY()` |
| `String` | `toString()` - overrides Object.toString() |

## Method Details

### ImageSearchResult
```java
public ImageSearchResult()
```

```java
public ImageSearchResult(SearchableImage foundImage, int x, int y, int w, int h, double score, UUID screenUpdateUUID, long searchTime)
```

### getScore
```java
public double getScore()
```

### getSearchTime
```java
public long getSearchTime()
```

### getScreenUpdateUUID
```java
public UUID getScreenUpdateUUID()
```

### getAsPoint
```java
public Point getAsPoint()
```

### getBounds
```java
public Rectangle getBounds()
```

### getX
```java
public int getX()
```

### getY
```java
public int getY()
```

### getWidth
```java
public int getWidth()
```

### getHeight
```java
public int getHeight()
```

### toString
```java
public String toString()
```
**Overrides:** toString in class Object

### getFoundImage
```java
public SearchableImage getFoundImage()
```
