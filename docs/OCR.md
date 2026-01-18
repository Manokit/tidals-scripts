# OCR

**Package:** `com.osmb.api.visual.ocr`

**Type:** Interface

**Access:** `script.getOCR()`

The `OCR` interface provides text recognition capabilities for reading text from the game screen. This is essential for reading UI elements, dialogue, item quantities, and any other on-screen text.

---

## Method Summary

| Method | Returns | Description |
|--------|---------|-------------|
| `getText(Font, Rectangle, int...)` | `String` | Read text from screen area |
| `getText(Font, Image, Rectangle, int...)` | `String` | Read text from image |
| `getText(Font, int[][], boolean)` | `String` | Read text from column flags |

---

## Core Methods

### getText(Font, Rectangle, int...)

```java
String getText(Font font, Rectangle textBounds, int... textColors)
```

Reads text from a rectangular area on the screen.

**Parameters:**
- `font` - The font used by the text (see Font enum below)
- `textBounds` - The rectangle area containing the text
- `textColors` - One or more RGB colors of the text to detect

**Returns:** The recognized text string.

**Example:**
```java
Rectangle titleArea = new Rectangle(100, 50, 200, 20);
String text = script.getOCR().getText(
    Font.STANDARD_FONT_BOLD,
    titleArea,
    0xFF981F  // orange text color
);
```

---

### getText(Font, Image, Rectangle, int...)

```java
String getText(Font font, Image image, Rectangle textBounds, int... textColors)
```

Reads text from a specific image rather than the live screen.

**Parameters:**
- `font` - The font used by the text
- `image` - The image to read from
- `textBounds` - The rectangle area within the image
- `textColors` - Text colors to detect

**Returns:** The recognized text string.

---

## Font Enum

**Package:** `com.osmb.api.visual.ocr.fonts.Font`

Common fonts used in the game:

| Font | Description | Common Uses |
|------|-------------|-------------|
| `STANDARD_FONT` | Regular game font | General UI text |
| `STANDARD_FONT_BOLD` | Bold game font | Titles, headers |
| `SMALL_FONT` | Smaller font | Button labels, item counts |
| `FANCY_STANDARD_FONT` | Decorative font | Special UI elements |
| `FANCY_BOLD_FONT_645` | Bold decorative | Headers |
| `FANCY_BOLD_BIG_FONT_646` | Large decorative | Big titles |

There are also numbered fonts (`FONT_764`, `FONT_1442`, etc.) for specific game interfaces.

---

## Common Text Colors

| Color | Hex | Use |
|-------|-----|-----|
| Orange text | `0xFF981F` | Titles, important labels |
| White text | `0xFFFFFF` | General text |
| Yellow text | `0xFFFF00` | Highlighted items |
| Green text | `0x00FF00` | Success messages |
| Red text | `0xFF0000` | Error/warning text |
| Cyan text | `0x00FFFF` | Links, special text |

---

## Common Patterns

### Reading Dialog Title

```java
public String getDialogTitle(Rectangle dialogBounds) {
    Rectangle titleArea = dialogBounds.getSubRectangle(
        new Rectangle(10, 5, dialogBounds.width - 20, 20)
    );

    return script.getOCR().getText(
        Font.STANDARD_FONT_BOLD,
        titleArea,
        0xFF981F  // orange
    );
}
```

### Reading Button Text

```java
public String getButtonText(Rectangle buttonBounds) {
    String rawText = script.getOCR().getText(
        Font.SMALL_FONT,
        buttonBounds,
        0xFF981F  // orange button text
    ).trim();

    // normalize common OCR errors (I -> l is common)
    return rawText.replace('I', 'l');
}
```

### Reading Item Count from Overlay

```java
public int readItemCount(Rectangle countArea) {
    String text = script.getOCR().getText(
        Font.SMALL_FONT,
        countArea,
        0xFFFF00  // yellow stack count
    ).trim();

    // handle "K" and "M" suffixes
    if (text.endsWith("K")) {
        return (int)(Double.parseDouble(text.replace("K", "")) * 1000);
    } else if (text.endsWith("M")) {
        return (int)(Double.parseDouble(text.replace("M", "")) * 1000000);
    }

    try {
        return Integer.parseInt(text);
    } catch (NumberFormatException e) {
        return 0;
    }
}
```

### Multiple Color Detection

```java
// detect text that could be multiple colors
String text = script.getOCR().getText(
    Font.STANDARD_FONT,
    textArea,
    0xFFFFFF,  // white
    0x00FF00,  // green
    0xFF0000   // red
);
```

---

## Tips and Best Practices

1. **Use Debug Tool to Find Colors** - The OSMB debug tool can sample exact pixel colors from the game. Use this to find the precise text color.

2. **Handle OCR Errors** - Common misreads include:
   - `I` read as `l` (lowercase L)
   - `0` read as `O`
   - Numbers confused with letters

   Always normalize text after reading:
   ```java
   String normalized = rawText.replace('I', 'l').toLowerCase();
   ```

3. **Trim Results** - OCR often captures leading/trailing spaces:
   ```java
   String text = script.getOCR().getText(...).trim();
   ```

4. **Use Tight Bounds** - The smaller and more precise your rectangle, the more accurate the OCR. Avoid including surrounding UI elements.

5. **Font Matching is Critical** - Using the wrong font will result in garbage output. Use the debug tool to identify which font a UI element uses.

---

## See Also

- [ui-widgets.md](ui-widgets.md) - OCR usage in UI components
- [advanced-patterns.md](advanced-patterns.md) - Advanced OCR patterns
- [PixelAnalyzer.md](PixelAnalyzer.md) - Color detection
