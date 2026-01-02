# OSMB API - Inventory & Items

Inventory management and tab components

---

## Classes in this Module

- [Inventory](#inventory) [class]
- [InventoryTabComponent](#inventorytabcomponent) [class]

---

## Inventory

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

**Extends/Implements:** extends Expandable, ItemGroup

**Interfaces:** Expandable, ItemGroup, Viewable

### Methods

#### `unSelectItemIfSelected()`

**Returns:** `boolean`

#### `dropItem(int itemID, int amount)`

**Returns:** `boolean`

#### `dropItem(int itemID, int amount, boolean ignoreTapToDrop)`

**Returns:** `boolean`

#### `dropItems(int... itemIdsToDrop)`

**Returns:** `boolean`

Drops specified items from the item group.

**Parameters:**
- `itemIdsToDrop` - The IDs of the items to drop.

**Returns:** `true` if the items were successfully dropped, otherwise `false`.

#### `dropItems(Set<Integer> itemIdsToDrop)`

**Returns:** `boolean`

#### `dropItems(Set<Integer> itemIdsToDrop, boolean ignoreTapToDrop)`

**Returns:** `boolean`

#### `registerInventoryComponent(Component component)`


---

## InventoryTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent implements Inventory

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIcons()`

**Returns:** `int[]`

#### `getStartPoint()`

**Returns:** `Point`

#### `isVisible()`

**Returns:** `boolean`

#### `unSelectItemIfSelected()`

**Returns:** `boolean`

#### `dropItem(int itemID, int amount)`

**Returns:** `boolean`

#### `dropItem(int itemID, int amount, boolean ignoreTapToDrop)`

**Returns:** `boolean`

#### `dropItems(int... itemIdsToDrop)`

**Returns:** `boolean`

Description copied from interface: Inventory

**Parameters:**
- `itemIdsToDrop` - The IDs of the items to drop.

**Returns:** `true` if the items were successfully dropped, otherwise `false`.

#### `dropItems(Set<Integer> itemIdsToDrop)`

**Returns:** `boolean`

#### `dropItems(Set<Integer> itemIdsToDrop, boolean ignoreTapToDrop)`

**Returns:** `boolean`

#### `isOpen()`

**Returns:** `boolean`

#### `getType()`

**Returns:** `Tab.Type`

#### `groupWidth()`

**Returns:** `int`

#### `groupHeight()`

**Returns:** `int`

#### `xIncrement()`

**Returns:** `int`

#### `yIncrement()`

**Returns:** `int`

#### `getGroupBounds()`

**Returns:** `Rectangle`

#### `getCore()`

**Returns:** `ScriptCore`

#### `registerInventoryComponent(Component component)`


---

