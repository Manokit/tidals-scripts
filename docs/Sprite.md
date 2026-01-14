# Sprite

## SpriteManager

**Package:** com.osmb.api.ui

**Type:** Interface

### Methods

| Return Type | Method | Description |
|------------|--------|-------------|
| `SearchableImage[]` | `getEntityMapDot(EntityMapDot mapDot)` | |
| `Image` | `getResizedSprite(SpriteDefinition sprite, int newWidth, int newHeight)` | |
| `SpriteDefinition` | `getSprite(int id)` | |
| `SpriteDefinition` | `getSprite(int id, int frame)` | |
| `Map<Integer, SpriteDefinition[]>` | `getSprites()` | |
| `SpriteDefinition[]` | `getSprites(int id)` | |

### Method Details

#### getEntityMapDot
```java
SearchableImage[] getEntityMapDot(EntityMapDot mapDot)
```

#### getSprites
```java
Map<Integer, SpriteDefinition[]> getSprites()
```

#### getSprite
```java
SpriteDefinition getSprite(int id, int frame)
```

```java
SpriteDefinition getSprite(int id)
```

#### getSprites
```java
SpriteDefinition[] getSprites(int id)
```

#### getResizedSprite
```java
Image getResizedSprite(SpriteDefinition sprite, int newWidth, int newHeight)
```

## SpriteDefinition

**Type:** Class

**Extends:** Object

### Fields

| Type | Field |
|------|-------|
| `int` | `id` |
| `int` | `frame` |
| `int` | `offsetX` |
| `int` | `offsetY` |
| `int` | `width` |
| `int` | `height` |
| `int[]` | `pixels` |
| `int` | `maxWidth` |
| `int` | `maxHeight` |
| `byte[]` | `pixelIdx` (transient) |
| `int[]` | `palette` (transient) |

### Constructor

```java
public SpriteDefinition()
```

### Methods

| Return Type | Method |
|------------|--------|
| `int` | `getMaxWidth()` |
| `int` | `getMaxHeight()` |
| `void` | `normalize()` |
| `BufferedImage` | `toBufferedImage()` |
| `void` | `save()` - throws IOException |
| `String` | `toString()` - overrides Object.toString() |
