# ItemManager

**Package:** `com.osmb.api.item`

**Type:** Interface

The `ItemManager` interface defines the contract for managing and interacting with items inside a Component which implements an `ItemGroup` (e.g., Inventory). It provides methods for retrieving item definitions, searching for items, managing item groups, and performing actions like dropping items.

## Field

### ITEM_TOLERANCE_COMPARATOR

```java
static final ToleranceComparator ITEM_TOLERANCE_COMPARATOR
```

Default tolerance comparator for item matching.

---

## Static Methods

### itemGroupVisible(ScriptCore, ItemGroup)

```java
static boolean itemGroupVisible(ScriptCore core, ItemGroup itemGroup)
```

Checks if an item group is visible and, if the parent implements expandable, ensures it is open.

**Parameters:**
- `itemGroup` - The item group to check.

**Returns:** `true` if the item group is visible and open (if applicable), otherwise `false`.

**Throws:**
- `IllegalArgumentException` - If the item group does not implement `Component` or `Expandable`.

---

## Item Definition Methods

### getItemDefinition(int)

```java
ItemDefinition getItemDefinition(int itemID)
```

Retrieves the definition of an item by its ID.

**Parameters:**
- `itemID` - The ID of the item to retrieve.

**Returns:** The `ItemDefinition` corresponding to the item ID.

---

### getItemDefinitions()

```java
List<ItemDefinition> getItemDefinitions()
```

Retrieves a list of all item definitions.

**Returns:** A list of `ItemDefinition` objects.

---

### getItemName(int)

```java
String getItemName(int itemID)
```

Retrieves the name of an item by its ID.

**Parameters:**
- `itemID` - The ID of the item.

**Returns:** The name of the item.

---

### getNameForItemID(int)

```java
String getNameForItemID(int itemID)
```

Gets the name for a given item ID.

**Parameters:**
- `itemID` - The item ID.

**Returns:** The item name.

---

### isStackable(int)

```java
boolean isStackable(int itemID)
```

Checks if an item is stackable by its ID.

**Parameters:**
- `itemID` - The item ID to check.

**Returns:** `true` if the item is stackable, `false` otherwise.

---

## Item Scanning Methods

### scanItemGroup(ItemGroup, Set<Integer>)

```java
ItemGroupResult scanItemGroup(ItemGroup itemGroup, Set<Integer> itemIds)
```

Scans an item group for specified items, automatically handling group visibility and expansion.

**Key Features:**
- **Selective Scanning**: Only searches for items matching the provided IDs for optimal performance
- **Complete Slot Tracking**: Records all occupied slots regardless of requested items
- **Smart Visibility Handling**: Automatically manages expandable/visible states

**Why Provide Item IDs?**
RuneScape contains tens of thousands of possible items. Rather than inefficiently checking every possible item:
- You specify exactly which items to look for
- We still track *all* occupied slots in the container
- Results will include both your requested items and general slot occupancy data

**Visibility Handling:**
- For `Expandable` groups: Attempts to open if closed (requires `core.getFinger()` to be non-null)
- For regular components: Only checks visibility (does not attempt to make visible)
- Returns `null` immediately if the group cannot be made visible

**Operation Flow:**
1. Visibility Check: Attempts to make the group visible if needed
2. Slot Scanning: Checks every slot while tracking:
   - Occupied status (regardless of item ID)
   - Best matches for your specified items
   - Currently selected slot
3. Result Compilation: Returns both targeted matches and general slot information

**Parameters:**
- `itemGroup` - The container to scan (inventory, bank, etc.)
- `itemIds` - The specific items to search for (empty list will still return slot occupancy)

**Returns:** Detailed scan results including:
- `ItemSearchResult` matches for your specified items
- Complete slot occupancy data
- Selected slot information
OR `null` if container can't be made visible

**Throws:**
- `IllegalArgumentException` - If the itemGroup is invalid

---

### getSlotForPoint(ItemGroup, Point)

```java
UIResult<Integer> getSlotForPoint(ItemGroup itemGroup, Point p)
```

Retrieves the slot index for a specific point in the item group.

**Parameters:**
- `itemGroup` - The item group to check.
- `p` - The point to check.

**Returns:** A `UIResult` containing the slot index, or an error result.

---

## Item Search Methods

### findLocation(boolean, Rectangle, SearchableItem...)

```java
ItemSearchResult findLocation(boolean checkBorder, Rectangle bounds, SearchableItem... items)
```

Finds the first location of the specified items within a given rectangular area on the screen.

**Parameters:**
- `checkBorder` - Whether to check the border of items.
- `bounds` - The rectangular bounds to search within.
- `items` - The items to search for.

**Returns:** An `ItemSearchResult` containing the first match, or null if not found.

---

### findLocations(boolean, Rectangle, SearchableItem...)

```java
List<ItemSearchResult> findLocations(boolean checkBorder, Rectangle bounds, SearchableItem... items)
```

Finds locations of specified items within a given rectangular area on the screen.

**Parameters:**
- `checkBorder` - Whether to check the border of items.
- `bounds` - The rectangular bounds to search within.
- `items` - The items to search for.

**Returns:** A list of `ItemSearchResult` objects for all matches.

---

### findLocations(Rectangle, SearchableItem...)

```java
List<ItemSearchResult> findLocations(Rectangle bounds, SearchableItem... items)
```

Finds locations of specified items within a given rectangular area on the screen.

**Parameters:**
- `bounds` - The rectangular bounds to search within.
- `items` - The items to search for.

**Returns:** A list of `ItemSearchResult` objects for all matches.

---

### isItemAt(SearchableItem, int, int)

```java
ImageSearchResult isItemAt(SearchableItem image, int x, int y)
```

Checks if an item is at the specified coordinates.

**Parameters:**
- `image` - The searchable item to check for.
- `x` - The x coordinate.
- `y` - The y coordinate.

**Returns:** An `ImageSearchResult` indicating if the item was found.

---

### isItemAt(SearchableItem, int, int, boolean)

```java
ImageSearchResult isItemAt(SearchableItem image, int x, int y, boolean checkBorder)
```

Checks if an item is at the specified coordinates with optional border checking.

**Parameters:**
- `image` - The searchable item to check for.
- `x` - The x coordinate.
- `y` - The y coordinate.
- `checkBorder` - Whether to check the border.

**Returns:** An `ImageSearchResult` indicating if the item was found.

---

## Item Retrieval Methods

### getItem(int, boolean)

```java
SearchableItem[] getItem(int itemId, boolean border)
```

Retrieves an array of `SearchableItem` objects for a specific item ID.

**Parameters:**
- `itemId` - The ID of the item.
- `border` - Whether to include the border in the item image.

**Returns:** An array of `SearchableItem` objects.

---

### getItem(int, boolean, int)

```java
SearchableItem[] getItem(int itemId, boolean border, int rasterBackgroundColor)
```

Retrieves searchable items with a specific background color.

**Parameters:**
- `itemId` - The item ID.
- `border` - Whether to include the border.
- `rasterBackgroundColor` - The background color to use.

**Returns:** An array of `SearchableItem` objects.

---

### getItem(int, boolean, ToleranceComparator, ColorModel)

```java
SearchableItem[] getItem(int itemId, boolean border, ToleranceComparator toleranceComparator, ColorModel colorModel)
```

Retrieves searchable items with custom tolerance and color model.

**Parameters:**
- `itemId` - The item ID.
- `border` - Whether to include the border.
- `toleranceComparator` - The tolerance comparator to use for matching.
- `colorModel` - The color model to use.

**Returns:** An array of `SearchableItem` objects.

---

### getItem(int, boolean, ToleranceComparator, ColorModel, ZoomType)

```java
SearchableItem[] getItem(int itemId, boolean border, ToleranceComparator toleranceComparator, ColorModel colorModel, ZoomType zoomType)
```

Retrieves searchable items with custom tolerance, color model, and zoom type.

**Parameters:**
- `itemId` - The item ID.
- `border` - Whether to include the border.
- `toleranceComparator` - The tolerance comparator to use.
- `colorModel` - The color model to use.
- `zoomType` - The zoom type for the item image.

**Returns:** An array of `SearchableItem` objects.

---

### getItem(int, boolean, ToleranceComparator, ColorModel, ZoomType, int)

```java
SearchableItem[] getItem(int itemId, boolean border, ToleranceComparator toleranceComparator, ColorModel colorModel, ZoomType zoomType, int backgroundColor)
```

Retrieves searchable items with full customization options.

**Parameters:**
- `itemId` - The item ID.
- `border` - Whether to include the border.
- `toleranceComparator` - The tolerance comparator to use.
- `colorModel` - The color model to use.
- `zoomType` - The zoom type for the item image.
- `backgroundColor` - The background color to use.

**Returns:** An array of `SearchableItem` objects.

---

## Item Image Methods

### getItemImage(int, int, ZoomType, int)

```java
Image getItemImage(int id, int amount, ZoomType zoomType, int backgroundColor)
```

Retrieves the image of an item with the specified parameters.

**Parameters:**
- `id` - The item ID.
- `amount` - The item amount (for stack display).
- `zoomType` - The zoom type.
- `backgroundColor` - The background color.

**Returns:** An `Image` of the item.

---

### getItemImage(int, int, ZoomType, int, int, int)

```java
ItemImage getItemImage(int id, int amount, ZoomType zoomType, int borderColor, int rasterBackgroundColor, int backgroundColor)
```

Retrieves an item image for the specified item ID with full customization.

**Parameters:**
- `id` - The item ID.
- `amount` - The item amount.
- `zoomType` - The zoom type.
- `borderColor` - The border color.
- `rasterBackgroundColor` - The raster background color.
- `backgroundColor` - The background color.

**Returns:** An `ItemImage` object.

---

### getItemImage(ItemDefinition, ZoomType, int)

```java
Image getItemImage(ItemDefinition itemDefinition, ZoomType zoomType, int backgroundColor)
```

Retrieves the image of an item using its definition.

**Parameters:**
- `itemDefinition` - The item definition.
- `zoomType` - The zoom type.
- `backgroundColor` - The background color.

**Returns:** An `Image` of the item.

---

## Color Configuration Methods

### overrideDefaultComparator(int, ToleranceComparator)

```java
void overrideDefaultComparator(int itemID, ToleranceComparator comparator)
```

Overrides the default comparator for a specific item ID.

**Parameters:**
- `itemID` - The ID of the item to override.
- `comparator` - The new comparator to set, or null to remove the override.

---

### overrideDefaultColorModel(int, ColorModel)

```java
void overrideDefaultColorModel(int itemID, ColorModel colorModel)
```

Overrides the default color model for a specific item ID.

**Parameters:**
- `itemID` - The ID of the item to override.
- `colorModel` - The new color model to set, or null to remove the override.

---

### getComparator(int)

```java
ToleranceComparator getComparator(int itemID)
```

Retrieves the comparator for a specific item ID.

**Parameters:**
- `itemID` - The ID of the item.

**Returns:** The `ToleranceComparator` for the item, or the default (23,23,23) if no override is set.

---

### getColorModel(int)

```java
ColorModel getColorModel(int itemID)
```

Retrieves the color model for a specific item ID.

**Parameters:**
- `itemID` - The ID of the item.

**Returns:** The `ColorModel` for the item, or the default (RGB) if no override is set.
