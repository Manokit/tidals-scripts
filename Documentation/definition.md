# OSMB API - Game Definitions

Game definitions for items, objects, NPCs, etc.

---

## Classes in this Module

- [ItemDefinition](#itemdefinition) [class]
- [MapDefinition](#mapdefinition) [class]
- [SpriteDefinition](#spritedefinition) [class]

---

## ItemDefinition

**Package:** `com.osmb.api.definition`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public final int id`
- `public String name`
- `public String examine`
- `public String unknown1`
- `public int resizeX`
- `public int resizeY`
- `public int resizeZ`
- `public int xan2d`
- `public int yan2d`
- `public int zan2d`
- `public int cost`
- `public boolean isTradeable`
- `public int stackable`
- `public int inventoryModel`
- `public int wearPos1`
- `public int wearPos2`
- `public int wearPos3`
- `public boolean members`
- `public short[] colorFind`
- `public short[] colorReplace`
- `public short[] textureFind`
- `public short[] textureReplace`
- `public int zoom2d`
- `public int xOffset2d`
- `public int yOffset2d`
- `public int ambient`
- `public int contrast`
- `public int[] countCo`
- `public int[] countObj`
- `public String[] options`
- `public String[] interfaceOptions`
- `public int maleModel0`
- `public int maleModel1`
- `public int maleModel2`
- `public int maleOffset`
- `public int maleHeadModel`
- `public int maleHeadModel2`
- `public int femaleModel0`
- `public int femaleModel1`
- `public int femaleModel2`
- `public int femaleOffset`
- `public int femaleHeadModel`
- `public int femaleHeadModel2`
- `public int category`
- `public int notedID`
- `public int notedTemplate`
- `public int team`
- `public int weight`
- `public int shiftClickDropIndex`
- `public int boughtId`
- `public int boughtTemplateId`
- `public int placeholderId`
- `public int placeholderTemplateId`
- `public Map<Integer,Object> params`

### Methods

#### `linkNote(ItemDefinition notedItem, ItemDefinition unnotedItem)`

#### `linkBought(ItemDefinition var1, ItemDefinition var2)`

#### `linkPlaceholder(ItemDefinition var1, ItemDefinition var2)`


---

## MapDefinition

**Package:** `com.osmb.api.definition`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getTileWidth()`

**Returns:** `int`

#### `getBaseX()`

**Returns:** `int`

#### `getBaseY()`

**Returns:** `int`

#### `getTileHeight()`

**Returns:** `int`

#### `getPlane()`

**Returns:** `int`

#### `getBorderColor()`

**Returns:** `int`


---

## SpriteDefinition

**Package:** `com.osmb.api.definition`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public int id`
- `public int frame`
- `public int offsetX`
- `public int offsetY`
- `public int width`
- `public int height`
- `public int[] pixels`
- `public int maxWidth`
- `public int maxHeight`
- `public transient byte[] pixelIdx`
- `public transient int[] palette`

### Methods

#### `getMaxWidth()`

**Returns:** `int`

#### `getMaxHeight()`

**Returns:** `int`

#### `normalize()`

#### `toBufferedImage()`

**Returns:** `BufferedImage`

#### `save(File file)`

**Throws:**
- IOException

#### `toString()`

**Returns:** `String`


---

