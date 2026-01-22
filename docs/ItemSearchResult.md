# ItemSearchResult

**Type:** Class

**Extends:** ImageSearchResult

Represents a found item from a `ItemGroup` search, with additional metadata and interaction capabilities. Extends `ImageSearchResult` to include visual matching information.

## Fields

| Type | Field |
|------|-------|
| `static final int` | `K_TEXT_COLOR` - Color value for 'K' (thousand) text in stack amounts |
| `static final int` | `M_TEXT_COLOR` - Color value for 'M' (million) text in stack amounts |
| `static final int` | `YELLOW_TEXT_COLOR` - Color value for yellow text in stack amounts |

## Constructors

| Constructor | Description |
|-------------|-------------|
| `ItemSearchResult(ScriptCore core, int id, ImageSearchResult imageSearchResult, ItemGroup itemGroup, int slot)` | Constructor for inventory items |
| `ItemSearchResult(ScriptCore core, int id, ImageSearchResult imageSearchResult, EquipmentSlot equipmentSlot)` | Constructor for equipped items |

## Methods

| Return Type | Method |
|------------|--------|
| `EquipmentSlot` | `getEquipmentSlot()` |
| `int` | `getId()` |
| `ItemGroup` | `getItemGroup()` |
| `int` | `getItemSlot()` |
| `UIResult<Point>` | `getRandomPointInSlot()` |
| `int` | `getSlot()` |
| `int` | `getStackAmount()` |
| `UIResult<Rectangle>` | `getTappableBounds()` |
| `boolean` | `interact()` |
| `boolean` | `interact(boolean bypassHumanDelay)` |
| `boolean` | `interact(MenuHook menuHook)` |
| `boolean` | `interact(String... options)` |
| `boolean` | `isSelected()` |
| `String` | `toString()` - overrides ImageSearchResult.toString() |

## Field Details

### M_TEXT_COLOR
```java
public static final int M_TEXT_COLOR
```

Color value for 'M' (million) text in stack amounts

### K_TEXT_COLOR
```java
public static final int K_TEXT_COLOR
```

Color value for 'K' (thousand) text in stack amounts

### YELLOW_TEXT_COLOR
```java
public static final int YELLOW_TEXT_COLOR
```

Color value for yellow text in stack amounts

## Constructor Details

### ItemSearchResult
```java
public ItemSearchResult(ScriptCore core, int id, ImageSearchResult imageSearchResult, ItemGroup itemGroup, int slot)
```

Constructor for inventory items

**Parameters:**
- `core` - The ScriptCore instance
- `id` - The item ID
- `imageSearchResult` - The base image search result
- `itemGroup` - The item group this belongs to
- `slot` - The inventory slot number

---

```java
public ItemSearchResult(ScriptCore core, int id, ImageSearchResult imageSearchResult, EquipmentSlot equipmentSlot)
```

Constructor for equipped items

**Parameters:**
- `core` - The ScriptCore instance
- `id` - The item ID
- `imageSearchResult` - The base image search result
- `equipmentSlot` - The equipment slot this item is in

## Method Details

### isSelected
```java
public boolean isSelected()
```

### getSlot
```java
public int getSlot()
```

**Returns:** The `ItemGroup` slot number (-1 if equipment)

### getEquipmentSlot
```java
public EquipmentSlot getEquipmentSlot()
```

**Returns:** The equipment slot (null if inventory item)

### getId
```java
public int getId()
```

**Returns:** The item ID

### getRandomPointInSlot
```java
public UIResult<Point> getRandomPointInSlot()
```

Gets a random point within the item's bounds for clicking

**Returns:** UIResult containing a random point or visibility status

### getTappableBounds
```java
public UIResult<Rectangle> getTappableBounds()
```

Gets the clickable bounds of the item

**Returns:** UIResult containing the bounds rectangle or visibility status

### getItemGroup
```java
public ItemGroup getItemGroup()
```

**Returns:** The item group this belongs to (null if equipment)

### getStackAmount
```java
public int getStackAmount()
```

**Returns:** The stack amount of the item

### getItemSlot
```java
public int getItemSlot()
```

**Returns:** The `ItemGroup` slot number (alias for getSlot())

### interact
```java
public boolean interact(String... options)
```

Interacts with the item using specified options

**Parameters:**
- `options` - The interaction options (e.g., "Use", "Drop")

**Returns:** true if interaction was successful

---

```java
public boolean interact()
```

Default interaction with the item (single click)

**Returns:** true if interaction was successful

---

```java
public boolean interact(boolean bypassHumanDelay)
```

Default interaction with the item (single click)

**Parameters:**
- `bypassHumanDelay` - if true, skips the human delay (Not recommended unless you know what you're doing)

**Returns:** true if interaction was successful

---

```java
public boolean interact(MenuHook menuHook)
```

Interacts with the item using a custom menu hook

### toString
```java
public String toString()
```

**Overrides:** toString in class ImageSearchResult
