# OSMB API - OCR & Text Recognition

Optical character recognition for text reading

---

## Classes in this Module

- [Class Font](#class-font) [class]
- [Font.Loader](#font.loader) [class]
- [OCR](#ocr) [class]
- [RSFont](#rsfont) [class]
- [RSFontChar](#rsfontchar) [class]

---

## Class Font

**Package:** `com.osmb.api.visual.ocr.fonts`

**Type:** Class

**Extends/Implements:** extends Enum<Font>

### Methods

#### `values()`

**Returns:** `Font[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `Font`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `get(Font font)`

**Returns:** `RSFont`

#### `init(Font.Loader loader)`

#### `getArchiveID()`

**Returns:** `int`


---

## Font.Loader

**Package:** `com.osmb.api.visual.ocr.fonts`

**Type:** Class

### Methods

#### `get(int archive)`

**Returns:** `SpriteDefinition[]`


---

## OCR

**Package:** `com.osmb.api.visual.ocr`

**Type:** Class

### Methods

#### `getText(Font font, Rectangle textBounds, int... textColors)`

**Returns:** `String`

#### `getText(Font font, int[][] columnFlags, boolean isArrayTrimmed)`

**Returns:** `String`

#### `getText(Font font, Image image, Rectangle textBounds, int... textColors)`

**Returns:** `String`


---

## RSFont

**Package:** `com.osmb.api.visual.ocr.fonts`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `isSimple()`

**Returns:** `boolean`

#### `getId()`

**Returns:** `int`

#### `getSpaceWidth()`

**Returns:** `int`

#### `getFontHeight()`

**Returns:** `int`

#### `getCharacters()`

**Returns:** `List<RSFontChar>`


---

## RSFontChar

**Package:** `com.osmb.api.visual.ocr.fonts`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getFontType()`

**Returns:** `RSFont`

#### `getTotalPixels()`

**Returns:** `int`

#### `getFlags()`

**Returns:** `int[][]`

#### `getCharacter()`

**Returns:** `char`

#### `toString()`

**Returns:** `String`


---

