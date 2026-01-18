# ItemDefinition

**Package:** `com.osmb.api.definition`

**Type:** Class

The `ItemDefinition` class contains all the metadata about an item - its name, properties, appearance, and behavior. You access item definitions through the `ItemManager`.

---

## Getting Item Definitions

```java
// get definition by item ID
ItemDefinition def = script.getItemManager().getItemDefinition(ItemID.GOLD_ORE);

// get item name directly
String name = script.getItemManager().getItemName(ItemID.GOLD_ORE);

// check if stackable
boolean stackable = script.getItemManager().isStackable(ItemID.GOLD_ORE);
```

---

## Key Fields

### Basic Info

| Field | Type | Description |
|-------|------|-------------|
| `id` | `int` | The item's unique ID |
| `name` | `String` | Display name ("Gold ore") |
| `examine` | `String` | Examine text |
| `cost` | `int` | Base value in coins |
| `weight` | `int` | Item weight |
| `stackable` | `int` | Stackability (0 = no, 1 = yes) |
| `members` | `boolean` | Members-only item |
| `isTradeable` | `boolean` | Can be traded |

### Menu Actions

| Field | Type | Description |
|-------|------|-------------|
| `options` | `String[]` | Ground item actions ("Take", etc.) |
| `interfaceOptions` | `String[]` | Inventory actions ("Use", "Drop", etc.) |

### Noted/Placeholder Variants

| Field | Type | Description |
|-------|------|-------------|
| `notedID` | `int` | Noted version ID (-1 if none) |
| `notedTemplate` | `int` | Template ID for noted items |
| `placeholderId` | `int` | Bank placeholder ID |
| `placeholderTemplateId` | `int` | Template for placeholders |

### Equipment Info

| Field | Type | Description |
|-------|------|-------------|
| `wearPos1` | `int` | Primary equipment slot |
| `wearPos2` | `int` | Secondary slot (if any) |
| `wearPos3` | `int` | Tertiary slot (if any) |
| `maleModel0/1/2` | `int` | Male character model IDs |
| `femaleModel0/1/2` | `int` | Female character model IDs |

### Visual Properties

| Field | Type | Description |
|-------|------|-------------|
| `inventoryModel` | `int` | 3D model ID for inventory |
| `zoom2d` | `int` | Inventory icon zoom |
| `xOffset2d` | `int` | X offset for icon |
| `yOffset2d` | `int` | Y offset for icon |
| `xan2d` | `int` | X rotation angle |
| `yan2d` | `int` | Y rotation angle |
| `zan2d` | `int` | Z rotation angle |
| `resizeX/Y/Z` | `int` | Scale factors |
| `colorFind` | `short[]` | Colors to replace |
| `colorReplace` | `short[]` | Replacement colors |
| `ambient` | `int` | Lighting ambient value |
| `contrast` | `int` | Lighting contrast value |

### Stack Appearance

| Field | Type | Description |
|-------|------|-------------|
| `countObj` | `int[]` | Item IDs for different stack sizes |
| `countCo` | `int[]` | Count thresholds for stack sprites |

### Misc

| Field | Type | Description |
|-------|------|-------------|
| `category` | `int` | Item category ID |
| `team` | `int` | Team cape number (if applicable) |
| `shiftClickDropIndex` | `int` | Shift-click action index |
| `params` | `Map<Integer, Object>` | Additional parameters |

---

## Common Patterns

### Check If Item Is Stackable

```java
public boolean isStackable(int itemId) {
    ItemDefinition def = script.getItemManager().getItemDefinition(itemId);
    return def != null && def.stackable == 1;
}
```

### Get Item Actions

```java
public String[] getInventoryActions(int itemId) {
    ItemDefinition def = script.getItemManager().getItemDefinition(itemId);
    if (def != null) {
        return def.interfaceOptions;
    }
    return new String[0];
}

// check if item has a specific action
public boolean hasAction(int itemId, String action) {
    String[] actions = getInventoryActions(itemId);
    for (String a : actions) {
        if (a != null && a.equalsIgnoreCase(action)) {
            return true;
        }
    }
    return false;
}
```

### Get Noted Item ID

```java
public int getNotedId(int itemId) {
    ItemDefinition def = script.getItemManager().getItemDefinition(itemId);
    if (def != null && def.notedID > 0) {
        return def.notedID;
    }
    return -1;  // no noted version
}
```

### Check If Members Item

```java
public boolean isMembersItem(int itemId) {
    ItemDefinition def = script.getItemManager().getItemDefinition(itemId);
    return def != null && def.members;
}
```

### Get Item Value

```java
public int getItemValue(int itemId) {
    ItemDefinition def = script.getItemManager().getItemDefinition(itemId);
    return def != null ? def.cost : 0;
}
```

### Check Equipment Slot

```java
public int getEquipmentSlot(int itemId) {
    ItemDefinition def = script.getItemManager().getItemDefinition(itemId);
    if (def != null) {
        return def.wearPos1;  // primary equipment slot
    }
    return -1;
}
```

---

## Methods

### linkNote(ItemDefinition, ItemDefinition)

```java
public void linkNote(ItemDefinition notedItem, ItemDefinition unnotedItem)
```

Links a noted item definition to its unnoted counterpart.

---

### linkBought(ItemDefinition, ItemDefinition)

```java
public void linkBought(ItemDefinition var1, ItemDefinition var2)
```

Links bought/sold versions of items (for GE).

---

### linkPlaceholder(ItemDefinition, ItemDefinition)

```java
public void linkPlaceholder(ItemDefinition var1, ItemDefinition var2)
```

Links an item to its bank placeholder version.

---

## Important Notes

1. **Use ItemManager Methods** - Prefer `getItemName()`, `isStackable()`, etc. over accessing fields directly when available.

2. **Null Checks** - Always check if the definition is not null before accessing fields.

3. **Field Values May Be Defaults** - Some fields may have default values (0, -1, null) if not applicable to that item.

4. **Noted Items Have Different IDs** - A noted item has a different ID than its unnoted version. Use `notedID` field to find the relationship.

---

## See Also

- [ItemManager.md](ItemManager.md) - Searching for and managing items
- [api-reference.md](api-reference.md) - Core API overview
