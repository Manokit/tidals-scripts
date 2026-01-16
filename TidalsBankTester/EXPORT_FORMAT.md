# Inventory Setups Export Format Documentation

This document explains the JSON export format used by the RuneLite Inventory Setups plugin, allowing you to understand and create your own importers for this format.

## Table of Contents

1. [Overview](#overview)
2. [Top-Level Structure](#top-level-structure)
3. [Setup Object](#setup-object)
4. [Item Object](#item-object)
5. [Layout Array](#layout-array)
6. [Field Reference](#field-reference)
7. [Example Walkthrough](#example-walkthrough)
8. [Implementation Guide](#implementation-guide)

---

## Overview

The Inventory Setups plugin exports configurations in a JSON format called `InventorySetupPortable`. This format contains two main parts:

1. **setup**: All the configuration data (items, settings, name, colors, etc.)
2. **layout**: Bank tab layout information (array of item IDs representing positions)

The format is optimized for size by:
- Using short field names (e.g., `inv`, `eq`, `rp`)
- Using `null` instead of default values
- Using `null` instead of `false` for boolean fields
- Omitting fields with default values

---

## Top-Level Structure

```json
{
  "setup": { ... },
  "layout": [ ... ]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `setup` | Object | The inventory setup configuration (see [Setup Object](#setup-object)) |
| `layout` | Array\<int\> | Bank tab layout as an array of item IDs. `-1` represents empty slots. |

---

## Setup Object

The setup object contains all configuration for an inventory setup.

### Structure

```json
{
  "inv": [...],
  "eq": [...],
  "rp": [...],
  "bp": [...],
  "qv": [...],
  "afi": {...},
  "name": "string",
  "notes": "string",
  "hc": "color",
  "hd": boolean,
  "dc": "color",
  "fb": boolean,
  "uh": boolean,
  "sb": integer,
  "fv": boolean,
  "iId": integer
}
```

### Fields

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `inv` | Array\<Item\> | Yes | - | **Inventory** items (28 slots). See [Item Object](#item-object) |
| `eq` | Array\<Item\> | Yes | - | **Equipment** items (14 slots). See [Item Object](#item-object) |
| `rp` | Array\<Item\> | No | `null` | **Rune Pouch** items (up to 4 slots). `null` means no rune pouch |
| `bp` | Array\<Item\> | No | `null` | **Bolt Pouch** items (up to 4 slots). `null` means no bolt pouch |
| `qv` | Array\<Item\> | No | `null` | **Quiver** items (1 slot). `null` means no quiver |
| `afi` | Map\<int, Item\> | No | `null` | **Additional Filtered Items**. Map of item ID to Item object. `null` means no additional items |
| `name` | String | Yes | - | Setup name |
| `notes` | String | No | `null` | Setup notes. `null` means empty notes |
| `hc` | String (Color) | Yes | - | **Highlight Color** in hex format `#AARRGGBB` (alpha, red, green, blue) |
| `hd` | Boolean | No | `null` = `false` | **Highlight Difference** - whether to highlight items that differ from the setup |
| `dc` | String (Color) | No | `null` | **Display Color** for the setup in the UI. `null` means no custom color |
| `fb` | Boolean | No | `null` = `false` | **Filter Bank** - whether to filter bank to show only items in this setup |
| `uh` | Boolean | No | `null` = `false` | **Unordered Highlight** - whether highlighting ignores item order in inventory |
| `sb` | Integer | No | `null` = `0` | **Spellbook** selection (see [Spellbook Values](#spellbook-values)) |
| `fv` | Boolean | No | `null` = `false` | **Favorite** - whether this setup is marked as favorite |
| `iId` | Integer | No | `null` = `-1` | **Icon ID** - custom icon item ID for the setup. `-1` or `null` means use default |

### Spellbook Values

| Value | Spellbook |
|-------|-----------|
| `0` or `null` | Standard |
| `1` | Ancient |
| `2` | Lunar |
| `3` | Arceuus |
| `4` | None |

### Equipment Slots

The `eq` array has 14 slots in this order:

| Index | Slot | Index | Slot |
|-------|------|-------|------|
| 0 | Head | 7 | Legs |
| 1 | Cape | 8 | Gloves |
| 2 | Amulet | 9 | Boots |
| 3 | Ammo | 10 | Ring |
| 4 | Weapon | 11 | (Empty) |
| 5 | Body | 12 | (Empty) |
| 6 | Shield | 13 | (Empty) |

---

## Item Object

Items can be `null` (empty slot) or an object with the following structure:

### Structure

```json
{
  "id": 12345,
  "q": 1000,
  "f": true,
  "sc": "Standard"
}
```

### Fields

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `id` | Integer | Yes | - | RuneLite item ID (e.g., `995` for coins, `556` for air runes) |
| `q` | Integer | No | `null` = `1` | **Quantity** of the item. `null` means quantity of 1 |
| `f` | Boolean | No | `null` = `false` | **Fuzzy match** - allows degraded items, noted items, potions with different doses, and jewelry with different charges to match |
| `sc` | String (Enum) | No | `null` = `"None"` | **Stack Compare** mode for highlighting (see [Stack Compare Modes](#stack-compare-modes)) |

### Stack Compare Modes

Controls how the plugin highlights stack differences:

| Value | Symbol | Description |
|-------|--------|-------------|
| `null` or `"None"` | - | No stack comparison highlighting |
| `"Standard"` | `!=` | Highlight if stacks differ at all |
| `"Less_Than"` | `<` | Highlight only if inventory stack is less than setup |
| `"Greater_Than"` | `>` | Highlight only if inventory stack is greater than setup |

### Fuzzy Matching

When `f` is `true`, the item will match:
- **Degraded items**: Barrows equipment at different charge levels
- **Noted items**: Both noted and unnoted versions
- **Potions**: Potions with different doses (4), (3), (2), (1)
- **Jewelry**: Jewelry with different charges (e.g., Ring of dueling (8) to (1))
- **Custom variations**: Items configured as variants in the plugin

---

## Layout Array

The layout array represents the bank tab organization for this setup.

### Structure

```json
"layout": [26674, 1706, -1, 9676, 11840, 22941, ...]
```

- **Index**: Position in the bank tab (0-based)
- **Value**: Item ID at that position
- **`-1`**: Empty slot

### Layout Format

The layout is a flat array where:
- Each index represents a position in the bank tab grid
- Positions are organized in rows of 8 (bank has 8 columns)
- Item IDs are placed at specific positions
- `-1` indicates an empty slot
- The array may contain trailing `-1` values that can be trimmed

### Layout Types

The plugin supports two layout types:

#### 1. Preset Layout
- Equipment on the left side (columns 0-3)
- Inventory on the right side (columns 4-7)
- Rune pouch items at positions 40-43
- Bolt pouch items at positions 48-51
- Additional items starting at position 56

#### 2. ZigZag Layout
- Equipment starts at position 0
- Inventory starts at position 16
- Items are arranged in a zigzag pattern (top-to-bottom or bottom-to-top)
- Rune pouch at positions 48-51
- Bolt pouch at positions 52-55
- Additional items at position 56+

---

## Field Reference

### Quick Reference Table

| JSON Key | Full Name | Type | Values |
|----------|-----------|------|--------|
| `inv` | Inventory | Array | 28 items |
| `eq` | Equipment | Array | 14 items |
| `rp` | Rune Pouch | Array | 0-4 items |
| `bp` | Bolt Pouch | Array | 0-4 items |
| `qv` | Quiver | Array | 0-1 items |
| `afi` | Additional Filtered Items | Map | item_id → Item |
| `name` | Name | String | Any text |
| `notes` | Notes | String | Any text |
| `hc` | Highlight Color | String | `#AARRGGBB` |
| `hd` | Highlight Difference | Boolean | `true`/`null` |
| `dc` | Display Color | String | `#AARRGGBB` |
| `fb` | Filter Bank | Boolean | `true`/`null` |
| `uh` | Unordered Highlight | Boolean | `true`/`null` |
| `sb` | Spellbook | Integer | 0-4 |
| `fv` | Favorite | Boolean | `true`/`null` |
| `iId` | Icon ID | Integer | Item ID |
| `id` | Item ID | Integer | RuneLite ID |
| `q` | Quantity | Integer | 1+ |
| `f` | Fuzzy | Boolean | `true`/`null` |
| `sc` | Stack Compare | Enum | See modes |

---

## Example Walkthrough

Let's decode the example you provided:

```json
{
  "setup": {
    "inv": [
      {"id":2440,"f":true},
      {"id":2436,"f":true},
      {"id":2434,"f":true},
      {"id":2434,"f":true},
      {"id":2440,"f":true},
      {"id":2436,"f":true},
      {"id":2434,"f":true},
      {"id":2434,"f":true},
      null,null,null,null,null,null,null,null,null,null,null,null,
      {"id":13639},
      {"id":13226},
      {"id":772},
      null,
      {"id":995,"q":824188},
      {"id":556,"q":12155},
      {"id":561,"q":13951},
      {"id":27281}
    ],
    "eq": [
      {"id":26674,"f":true},
      {"id":6570,"f":true},
      {"id":1706},
      {"id":4151,"f":true},
      {"id":9674,"f":true},
      {"id":12954},
      null,
      {"id":9676,"f":true},
      null,
      {"id":7462},
      {"id":11840},
      null,
      {"id":26770},
      {"id":22941}
    ],
    "rp": [
      {"id":563,"q":10870},
      {"id":554,"q":15950},
      {"id":555,"q":16000},
      {"id":557,"q":16000}
    ],
    "name": "Slayer",
    "hc": "#FFFF0000",
    "hd": true,
    "fb": true
  },
  "layout": [26674,1706,9674,9676,11840,22941,139,145,6570,4151,12954,7462,26770,157,141,147,2440,2434,2440,2434,13639,772,556,27281,2436,2434,2436,2434,13226,995,561,159,143,149,161,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,563,554,555,557,-1,-1,-1,-1]
}
```

### Decoded Information

**Setup Name**: "Slayer"

**Inventory** (`inv`):
- Slots 0-7: 8 items with fuzzy matching (likely food or potions that can have variations)
  - 2440 (Mushroom potato - fuzzy)
  - 2436 (Super attack(4) - fuzzy)
  - 2434 (Super strength(4) - fuzzy)
  - And repeats...
- Slots 8-19: Empty (null)
- Slot 20: Item 13639 (Slayer helmet)
- Slot 21: Item 13226 (Black mask)
- Slot 22: Item 772 (Slayer gem)
- Slot 23: Empty (null)
- Slot 24: Item 995 × 824,188 (Coins)
- Slot 25: Item 556 × 12,155 (Air runes)
- Slot 26: Item 561 × 13,951 (Nature runes)
- Slot 27: Item 27281 (Rune pouch)

**Equipment** (`eq`):
- Head (0): 26674 (Faceguard - fuzzy)
- Cape (1): 6570 (Fire cape - fuzzy)
- Amulet (2): 1706 (Amulet of glory)
- Ammo (3): 4151 (Adamant dart - fuzzy)
- Weapon (4): 9674 (Bandos godsword - fuzzy)
- Body (5): 12954 (Bandos chestplate)
- Shield (6): Empty (null)
- Legs (7): 9676 (Bandos tassets - fuzzy)
- Gloves (8): Empty (null)
- Boots (9): 7462 (Dragon boots)
- Ring (10): 11840 (Ring of wealth)
- Slots 11-12: Empty (null)
- Slot 13: 22941 (Ward)

**Rune Pouch** (`rp`):
- Slot 0: Item 563 × 10,870 (Law runes)
- Slot 1: Item 554 × 15,950 (Fire runes)
- Slot 2: Item 555 × 16,000 (Water runes)
- Slot 3: Item 557 × 16,000 (Earth runes)

**Settings**:
- Highlight Color: `#FFFF0000` (red with full opacity)
- Highlight Difference: `true` (enabled)
- Filter Bank: `true` (enabled)
- Spellbook: Not specified (`null` = Standard)
- Favorite: Not specified (`null` = false)
- Display Color: Not specified (`null` = no custom color)

**Layout**:
- The layout array defines a custom bank tab organization
- Item 26674 (Faceguard) is at position 0
- Item 1706 (Amulet of glory) is at position 1
- Empty slots are marked with `-1`
- Rune pouch runes (563, 554, 555, 557) are placed at positions 49-52

---

## Implementation Guide

### Creating an Importer

Here's a guide to implementing your own importer in pseudocode:

```python
def import_inventory_setup(json_string):
    # Parse JSON
    data = json.parse(json_string)

    # Validate structure
    if not data.has("setup") or not data.has("layout"):
        raise Error("Invalid format: missing setup or layout")

    setup = data["setup"]

    # Validate required fields
    if not setup.has("name") or not setup.has("inv") or not setup.has("eq"):
        raise Error("Invalid setup: missing required fields")

    # Parse setup data
    name = setup["name"]
    inventory = parse_item_array(setup["inv"], 28)
    equipment = parse_item_array(setup["eq"], 14)

    # Parse optional containers
    rune_pouch = parse_item_array(setup.get("rp"), 4) if setup.has("rp") else None
    bolt_pouch = parse_item_array(setup.get("bp"), 4) if setup.has("bp") else None
    quiver = parse_item_array(setup.get("qv"), 1) if setup.has("qv") else None

    # Parse additional filtered items
    additional_items = {}
    if setup.has("afi") and setup["afi"] is not None:
        for item_id, item_data in setup["afi"].items():
            additional_items[item_id] = parse_item(item_data)

    # Parse settings with defaults
    highlight_color = parse_color(setup["hc"])
    highlight_difference = setup.get("hd", False)
    display_color = parse_color(setup.get("dc")) if setup.has("dc") else None
    filter_bank = setup.get("fb", False)
    unordered_highlight = setup.get("uh", False)
    spellbook = setup.get("sb", 0)
    favorite = setup.get("fv", False)
    icon_id = setup.get("iId", -1)
    notes = setup.get("notes", "")

    # Parse layout
    layout = data["layout"]

    # Create your setup object
    return InventorySetup(
        name=name,
        inventory=inventory,
        equipment=equipment,
        rune_pouch=rune_pouch,
        bolt_pouch=bolt_pouch,
        quiver=quiver,
        additional_items=additional_items,
        highlight_color=highlight_color,
        highlight_difference=highlight_difference,
        display_color=display_color,
        filter_bank=filter_bank,
        unordered_highlight=unordered_highlight,
        spellbook=spellbook,
        favorite=favorite,
        icon_id=icon_id,
        notes=notes,
        layout=layout
    )

def parse_item_array(items, expected_size):
    """Parse an array of items, handling null values"""
    if items is None:
        return None

    result = []
    for i in range(expected_size):
        if i < len(items):
            result.append(parse_item(items[i]))
        else:
            result.append(None)
    return result

def parse_item(item_data):
    """Parse a single item object"""
    if item_data is None:
        return None

    item_id = item_data["id"]
    quantity = item_data.get("q", 1)
    fuzzy = item_data.get("f", False)
    stack_compare = item_data.get("sc", "None")

    return Item(
        id=item_id,
        quantity=quantity,
        fuzzy=fuzzy,
        stack_compare=stack_compare
    )

def parse_color(color_string):
    """Parse color in #AARRGGBB format"""
    if color_string is None:
        return None

    # Remove '#' prefix
    hex_color = color_string[1:]

    # Parse components
    alpha = int(hex_color[0:2], 16)
    red = int(hex_color[2:4], 16)
    green = int(hex_color[4:6], 16)
    blue = int(hex_color[6:8], 16)

    return Color(red, green, blue, alpha)
```

### Creating an Exporter

```python
def export_inventory_setup(setup):
    """Export an inventory setup to JSON format"""

    # Build setup object
    setup_obj = {
        "inv": export_item_array(setup.inventory, 28),
        "eq": export_item_array(setup.equipment, 14),
        "name": setup.name,
        "hc": export_color(setup.highlight_color)
    }

    # Add optional containers
    if setup.rune_pouch is not None:
        setup_obj["rp"] = export_item_array(setup.rune_pouch, 4)

    if setup.bolt_pouch is not None:
        setup_obj["bp"] = export_item_array(setup.bolt_pouch, 4)

    if setup.quiver is not None and len(setup.quiver) > 0:
        setup_obj["qv"] = export_item_array(setup.quiver, 1)

    # Add additional filtered items
    if setup.additional_items and len(setup.additional_items) > 0:
        setup_obj["afi"] = {}
        for item_id, item in setup.additional_items.items():
            setup_obj["afi"][item_id] = export_item(item)

    # Add optional settings (only if not default)
    if setup.notes:
        setup_obj["notes"] = setup.notes

    if setup.highlight_difference:
        setup_obj["hd"] = True

    if setup.display_color is not None:
        setup_obj["dc"] = export_color(setup.display_color)

    if setup.filter_bank:
        setup_obj["fb"] = True

    if setup.unordered_highlight:
        setup_obj["uh"] = True

    if setup.spellbook != 0:
        setup_obj["sb"] = setup.spellbook

    if setup.favorite:
        setup_obj["fv"] = True

    if setup.icon_id > 0:
        setup_obj["iId"] = setup.icon_id

    # Build portable object
    portable = {
        "setup": setup_obj,
        "layout": setup.layout
    }

    return json.stringify(portable)

def export_item_array(items, size):
    """Export array of items"""
    result = []
    for i in range(size):
        if i < len(items):
            result.append(export_item(items[i]))
        else:
            result.append(None)
    return result

def export_item(item):
    """Export a single item, omitting default values"""
    if item is None:
        return None

    item_obj = {"id": item.id}

    # Only include non-default values
    if item.quantity != 1:
        item_obj["q"] = item.quantity

    if item.fuzzy:
        item_obj["f"] = True

    if item.stack_compare != "None":
        item_obj["sc"] = item.stack_compare

    return item_obj

def export_color(color):
    """Export color to #AARRGGBB format"""
    if color is None:
        return None

    return f"#{color.alpha:02X}{color.red:02X}{color.green:02X}{color.blue:02X}"
```

### Validation Checklist

When importing a setup, validate:

1. ✅ Top-level object has both `setup` and `layout` fields
2. ✅ `setup.name` is present and non-empty
3. ✅ `setup.inv` is present and is an array
4. ✅ `setup.eq` is present and is an array
5. ✅ `setup.hc` is present and is a valid color string
6. ✅ Item objects have required `id` field
7. ✅ Item IDs are positive integers
8. ✅ Quantities are positive integers (if specified)
9. ✅ Spellbook value is 0-4 (if specified)
10. ✅ Colors are in `#AARRGGBB` format
11. ✅ Stack compare values are valid enum values
12. ✅ Layout array contains only integers or -1

### Common Pitfalls

1. **Boolean Defaults**: Remember that `null` means `false` for boolean fields. Don't require explicit `false` values.

2. **Empty Arrays**: Containers like inventory and equipment should have the correct size (28 and 14 respectively), filled with `null` for empty slots.

3. **Item Quantities**: When `q` is `null` or omitted, the quantity is `1`, not `0`.

4. **Color Format**: Colors are in `#AARRGGBB` format (alpha first), not the typical `#RRGGBB` or `#RRGGBBAA`.

5. **Layout Array**: `-1` represents empty slots in the layout. Don't confuse it with item ID -1 (which represents a dummy item in the internal representation).

6. **Optional Containers**: `rp`, `bp`, and `qv` can be `null` entirely (not just empty arrays). Your parser must handle this.

7. **Additional Filtered Items**: The `afi` field is a map/object where keys are item IDs (as integers or strings depending on your JSON parser) and values are item objects.

---

## Additional Notes

### Item ID Lookup

Item IDs correspond to RuneLite's `ItemID` constants. You can find these in:
- [RuneLite Item ID Constants](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/ItemID.java)
- [OSRS Wiki Item IDs](https://oldschool.runescape.wiki/)

### Version Compatibility

This format is used by Inventory Setups plugin version 1.22.0 and is backward compatible with older versions. The plugin gracefully handles:
- Missing optional fields (uses defaults)
- Extra fields (ignored)
- Different container sizes (truncates or pads with nulls)

### Performance Considerations

When implementing an importer:
- Pre-allocate arrays to the correct size (28 for inventory, 14 for equipment)
- Use efficient JSON parsing libraries
- Cache item name lookups if you're resolving names from IDs
- Validate incrementally rather than after full parse

### Security Considerations

When accepting user-provided JSON:
- Validate all item IDs are within reasonable ranges (0 to ~30000)
- Limit string lengths (names, notes) to prevent memory issues
- Validate array sizes don't exceed expected maximums
- Sanitize any displayed text to prevent injection attacks

---

## Conclusion

This format is designed to be compact, extensible, and easy to parse. By following this documentation, you should be able to:

- ✅ Parse any Inventory Setup export
- ✅ Understand what each field represents
- ✅ Create your own importer/exporter
- ✅ Validate imports for correctness
- ✅ Generate compatible exports

For more information or to report issues with this documentation, visit the [Inventory Setups GitHub repository](https://github.com/runelite/runelite/tree/master/runelite-client/src/main/java/net/runelite/client/plugins/inventorysetups).

---

*Document Version: 1.0*
*Compatible with: Inventory Setups v1.22.0+*
*Last Updated: 2026-01-15*
