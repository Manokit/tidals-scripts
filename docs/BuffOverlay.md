# BuffOverlay

**Package:** `com.osmb.api.ui.overlay`

**Type:** Class (extends `OverlayBoundary`)

The `BuffOverlay` class detects and reads buff/debuff icons that appear near the minimap. This is essential for reading item charges (waterskins, jewelry), active effects (poison, venom), and other overlay information that can't be distinguished by sprite matching alone.

---

## Why Use BuffOverlay?

OSMB is a color bot - items with identical sprites CANNOT be distinguished visually. For example:
- Waterskin(0) through Waterskin(4) look exactly the same
- Ring of dueling(8) through Ring of dueling(1) are identical
- Different potion doses share the same appearance

The buff overlay displays charge counts and effect durations as text on these icons, which `BuffOverlay` can read.

---

## Constructor

```java
public BuffOverlay(ScriptCore core, int itemID)
```

Creates a new BuffOverlay tracker for a specific item/effect.

**Parameters:**
- `core` - The script core (usually `this` from your script)
- `itemID` - The sprite/item ID to track

**Example:**
```java
private static final int WATERSKIN_SPRITE_ID = 1823;  // find via debug tool
BuffOverlay waterskinBuff = new BuffOverlay(this, WATERSKIN_SPRITE_ID);
```

---

## Key Methods

### isVisible()

```java
public boolean isVisible()
```

Checks if the buff overlay is currently visible on screen.

**Returns:** `true` if the overlay is found and visible.

---

### getBuffText()

```java
public String getBuffText()
```

Reads the text displayed on the buff overlay (usually charges or duration).

**Returns:** The text string from the overlay.

**Example:**
```java
BuffOverlay waterskinBuff = new BuffOverlay(this, WATERSKIN_SPRITE_ID);
if (waterskinBuff.isVisible()) {
    String charges = waterskinBuff.getBuffText();
    int chargeCount = Integer.parseInt(charges);
    log(getClass(), "Waterskin has " + chargeCount + " charges");
}
```

---

### getBounds()

```java
public Rectangle getBounds()
```

Gets the screen bounds of the overlay.

**Returns:** A `Rectangle` representing the overlay's position and size.

---

### getValue(String)

```java
public String getValue(String valueName)
```

Gets a specific named value from the overlay.

**Parameters:**
- `valueName` - The name of the value to retrieve

**Returns:** The value string.

---

## Constants

| Constant | Type | Description |
|----------|------|-------------|
| `BASE_COLOR` | `int` | Base color for overlay detection |
| `BLACK_PIXEL` | `SearchablePixel` | Black pixel for border detection |
| `BORDER_PIXELS` | `int[]` | Border pixel colors |
| `COLOR_TOLERANCE` | `ToleranceComparator` | Color matching tolerance |
| `TEXT` | `String` | Text identifier |

---

## Common Patterns

### Tracking Waterskin Charges

```java
private static final int WATERSKIN_SPRITE_ID = 1823;

public int getWaterskinCharges() {
    BuffOverlay waterskin = new BuffOverlay(this, WATERSKIN_SPRITE_ID);

    if (!waterskin.isVisible()) {
        return -1;  // not visible, unknown state
    }

    String text = waterskin.getBuffText();
    try {
        return Integer.parseInt(text.trim());
    } catch (NumberFormatException e) {
        return 0;
    }
}

public boolean needsRefill() {
    int charges = getWaterskinCharges();
    return charges >= 0 && charges <= 1;
}
```

### Checking Poison Status

```java
private static final int POISON_SPRITE_ID = 1234;  // find via debug tool

public boolean isPoisoned() {
    BuffOverlay poisonOverlay = new BuffOverlay(this, POISON_SPRITE_ID);
    return poisonOverlay.isVisible();
}
```

### Reading Jewelry Charges

```java
private static final int RING_OF_DUELING_SPRITE_ID = 5678;

public int getRingCharges() {
    BuffOverlay ringBuff = new BuffOverlay(this, RING_OF_DUELING_SPRITE_ID);

    if (!ringBuff.isVisible()) {
        // ring not equipped or no charges visible
        return -1;
    }

    String chargeText = ringBuff.getBuffText();
    try {
        return Integer.parseInt(chargeText.trim());
    } catch (NumberFormatException e) {
        log(getClass(), "Failed to parse ring charges: " + chargeText);
        return 0;
    }
}
```

### Conditional Action Based on Charges

```java
public void handleWaterskin() {
    BuffOverlay waterskin = new BuffOverlay(this, WATERSKIN_SPRITE_ID);

    if (!waterskin.isVisible()) {
        log(getClass(), "No waterskin buff visible - may need to equip");
        return;
    }

    String charges = waterskin.getBuffText();
    int count = Integer.parseInt(charges.trim());

    if (count <= 1) {
        log(getClass(), "Low water (" + count + ") - refilling");
        refillWaterskin();
    } else {
        log(getClass(), "Waterskin has " + count + " charges - continuing");
    }
}
```

---

## Finding Sprite IDs

Use the OSMB debug tool to find the correct sprite ID for the buff you want to track:

1. Open the debug tool
2. Hover over the buff icon near the minimap
3. Note the sprite ID displayed
4. Use that ID in your `BuffOverlay` constructor

---

## Important Notes

1. **Overlay Must Be Visible** - Always check `isVisible()` before reading text. The buff may not be on screen if the item isn't equipped or the effect isn't active.

2. **Use for Charges, Not Detection** - BuffOverlay reads overlay text. For detecting if an item exists in inventory, use `ItemManager`.

3. **Parse Carefully** - Overlay text can sometimes have extra characters. Always `trim()` and handle parsing exceptions.

4. **Sprite IDs Vary** - Different versions of items may have different sprite IDs. Verify with the debug tool.

---

## See Also

- [critical-concepts.md](critical-concepts.md) - Why identical sprites can't be distinguished
- [ui-widgets.md](ui-widgets.md) - Other overlay patterns
- [ItemManager.md](ItemManager.md) - Finding items in containers
