# Paint & Setup UI Standard

The definitive guide for creating consistent, professional Paint overlays and Setup UIs across all Tidals scripts.

---

## Color Palette (Standard)

Use these exact colors for visual consistency:

```java
// paint overlay (java.awt.Color)
final Color bgColor = new Color(22, 49, 52);       // #163134 - dark teal background
final Color borderColor = new Color(40, 75, 80);   // #284b50 - lighter teal border
final Color accentGold = new Color(255, 215, 0);   // #ffd700 - gold accent
final Color accentYellow = new Color(255, 235, 130); // lighter gold/yellow
final Color textLight = new Color(238, 237, 233);  // #eeede9 - off-white text
final Color textMuted = new Color(170, 185, 185);  // #aab9b9 - muted labels
final Color valueGreen = new Color(180, 230, 150); // soft green for positive values
```

```java
// setup ui (javafx css strings)
private static final String BG_COLOR = "#163134";
private static final String BORDER_COLOR = "#284b50";
private static final String GOLD = "#ffd700";
private static final String TEXT_LIGHT = "#eeede9";
private static final String TEXT_MUTED = "#aab9b9";
```

---

## Paint Overlay

### Layout Constants

```java
final int x = 5;                    // left edge position
final int baseY = 40;               // top edge position
final int width = 220;              // panel width
final int borderThickness = 2;      // outer border thickness
final int paddingX = 10;            // horizontal padding inside panel
final int topGap = 6;               // top padding before content
final int lineGap = 16;             // vertical spacing between stat lines
final int logoBottomGap = 8;        // space below logo
```

### Visual Structure

```
+--[border: 2px #284b50]------------------+
|                                          |
|  +--[background: #163134]--------------+ |
|  |        [LOGO - centered]            | |
|  |  ---------------------------------- | | <- gold separator (#ffd700)
|  |                                     | |
|  |  Label              Value           | | <- each line: muted left, colored right
|  |  Label              Value           | |
|  |  Label              Value           | |
|  |                                     | |
|  |  ---------------------------------- | | <- border separator (#284b50) between sections
|  |                                     | |
|  |  Label              Value           | |
|  |                                     | |
|  |  ---------------------------------- | | <- border separator before version
|  |  Version            X.X             | |
|  +-------------------------------------+ |
+------------------------------------------+
```

### Font Standards

```java
private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 12);
private static final Font FONT_VALUE = new Font("Arial", Font.BOLD, 12);
```

### Number Formatting

use period as thousands separator:

```java
java.text.DecimalFormat intFmt = new java.text.DecimalFormat("#,###");
java.text.DecimalFormatSymbols sym = new java.text.DecimalFormatSymbols();
sym.setGroupingSeparator('.');
intFmt.setDecimalFormatSymbols(sym);

// output: 1.234.567 instead of 1,234,567
```

### Drawing Helper Method

```java
private void drawStatLine(Canvas c, int innerX, int innerWidth, int paddingX, int y,
        String label, String value, int labelColor, int valueColor) {
    c.drawText(label, innerX + paddingX, y, labelColor, FONT_LABEL);
    int valW = c.getFontMetrics(FONT_VALUE).stringWidth(value);
    int valX = innerX + innerWidth - paddingX - valW;
    c.drawText(value, valX, y, valueColor, FONT_VALUE);
}
```

### Drawing Separators

```java
// gold separator (after logo, primary sections)
c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, accentGold.getRGB(), 1);
curY += 16; // post-separator padding

// border separator (between subsections)
curY += lineGap - 4; // pre-separator padding
c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
curY += 16; // post-separator padding
```

### Dynamic Height Calculation

calculate panel height based on content:

```java
int totalLines = 9;  // base stat lines
if (hasExtraContent) totalLines += extraLines;

int separatorCount = countSeparators();
int separatorOverhead = separatorCount * 12;

int logoHeight = (logoImage != null) ? logoImage.height + logoBottomGap : 0;
int bottomPadding = 20;

int contentHeight = topGap + logoHeight + (totalLines * lineGap) + separatorOverhead + bottomPadding;
int innerHeight = Math.max(200, contentHeight);
```

### Color Usage Guide

| data type | color |
|-----------|-------|
| labels (left side) | textMuted (#aab9b9) |
| generic values | textLight (#eeede9) |
| totals, important values | accentGold (#ffd700) |
| secondary totals | accentYellow |
| positive/gained values | valueGreen |
| "MAXED" text | textLight (#eeede9) |

### Complete onPaint Example

```java
@Override
public void onPaint(Canvas c) {
    long elapsed = System.currentTimeMillis() - startTime;
    double hours = Math.max(1e-9, elapsed / 3_600_000.0);
    String runtime = formatRuntime(elapsed);

    // colors
    final Color bgColor = new Color(22, 49, 52);
    final Color borderColor = new Color(40, 75, 80);
    final Color accentGold = new Color(255, 215, 0);
    final Color textLight = new Color(238, 237, 233);
    final Color textMuted = new Color(170, 185, 185);
    final Color valueGreen = new Color(180, 230, 150);

    // layout
    final int x = 5;
    final int baseY = 40;
    final int width = 220;
    final int borderThickness = 2;
    final int paddingX = 10;
    final int topGap = 6;
    final int lineGap = 16;
    final int logoBottomGap = 8;

    int innerX = x;
    int innerY = baseY;
    int innerWidth = width;

    ensureLogoLoaded();
    int logoHeight = (logoImage != null) ? logoImage.height + logoBottomGap : 0;

    // calculate height
    int totalLines = 6; // runtime, xp gained, xp/hr, etl, ttl, version
    int contentHeight = topGap + logoHeight + (totalLines * lineGap) + 12 + 20;
    int innerHeight = Math.max(200, contentHeight);

    // draw border
    c.fillRect(innerX - borderThickness, innerY - borderThickness,
            innerWidth + (borderThickness * 2),
            innerHeight + (borderThickness * 2),
            borderColor.getRGB(), 1);

    // draw background
    c.fillRect(innerX, innerY, innerWidth, innerHeight, bgColor.getRGB(), 1);
    c.drawRect(innerX, innerY, innerWidth, innerHeight, borderColor.getRGB());

    int curY = innerY + topGap;

    // logo
    if (logoImage != null) {
        int logoX = innerX + (innerWidth - logoImage.width) / 2;
        c.drawAtOn(logoImage, logoX, curY);
        curY += logoImage.height + logoBottomGap;
    }

    // gold separator
    c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, accentGold.getRGB(), 1);
    curY += 16;

    // stats
    drawStatLine(c, innerX, innerWidth, paddingX, curY, "Runtime", runtime, textMuted.getRGB(), textLight.getRGB());

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY, "XP gained", intFmt.format(xpGained), textMuted.getRGB(), valueGreen.getRGB());

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY, "XP/hr", intFmt.format(xpPerHour), textMuted.getRGB(), accentGold.getRGB());

    // separator before version
    curY += lineGap - 4;
    c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
    curY += 16;

    drawStatLine(c, innerX, innerWidth, paddingX, curY, "Version", scriptVersion, textMuted.getRGB(), textMuted.getRGB());
}
```

---

## Setup UI (JavaFX)

### Root Container

```java
VBox root = new VBox(16);  // 16px spacing between children
root.setPadding(new Insets(20, 24, 20, 24));  // top, right, bottom, left
root.setAlignment(Pos.TOP_CENTER);
root.setStyle("-fx-background-color: " + BG_COLOR + ";");
```

### Logo Loading

```java
private ImageView loadLogo() {
    try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
        if (in == null) return null;
        Image img = new Image(in);
        ImageView view = new ImageView(img);
        view.setPreserveRatio(true);
        view.setFitWidth(180);  // standard logo width
        return view;
    } catch (Exception e) {
        return null;
    }
}
```

### Version Label

```java
Label versionLabel = new Label("v" + ScriptName.scriptVersion);
versionLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
```

### Option Box (Container for Settings)

```java
VBox optionBox = new VBox(8);  // 8px spacing
optionBox.setAlignment(Pos.CENTER_LEFT);
optionBox.setPadding(new Insets(12));
optionBox.setStyle(
    "-fx-background-color: derive(" + BG_COLOR + ", -15%); " +  // slightly darker
    "-fx-border-color: " + BORDER_COLOR + "; " +
    "-fx-border-width: 1; " +
    "-fx-border-radius: 4; " +
    "-fx-background-radius: 4;"
);
```

### Checkbox Styling

```java
CheckBox checkbox = new CheckBox("Option Name");
checkbox.setSelected(defaultValue);
checkbox.setStyle(
    "-fx-text-fill: " + TEXT_LIGHT + "; " +
    "-fx-font-size: 14px; " +
    "-fx-cursor: hand;"
);
```

### Description Label

```java
Label descLabel = new Label();
descLabel.setWrapText(true);
descLabel.setMaxWidth(220);
descLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
```

### Warning Label

```java
Label warningLabel = new Label("Warning text here");
warningLabel.setStyle("-fx-text-fill: " + GOLD + "; -fx-font-size: 10px;");
warningLabel.setVisible(false);  // show conditionally
warningLabel.setManaged(false);  // don't take space when hidden
```

### Start Button

```java
Button startButton = new Button("Start");
startButton.setMaxWidth(Double.MAX_VALUE);  // full width
startButton.setPrefHeight(36);
startButton.setStyle(
    "-fx-background-color: " + GOLD + "; " +
    "-fx-text-fill: " + BG_COLOR + "; " +
    "-fx-font-weight: bold; " +
    "-fx-font-size: 13px; " +
    "-fx-background-radius: 4; " +
    "-fx-cursor: hand;"
);

// hover effect
startButton.setOnMouseEntered(e ->
    startButton.setStyle(
        "-fx-background-color: derive(" + GOLD + ", 15%); " +  // lighten on hover
        "-fx-text-fill: " + BG_COLOR + "; " +
        "-fx-font-weight: bold; " +
        "-fx-font-size: 13px; " +
        "-fx-background-radius: 4; " +
        "-fx-cursor: hand;"
    )
);

startButton.setOnMouseExited(e ->
    startButton.setStyle(
        "-fx-background-color: " + GOLD + "; " +
        "-fx-text-fill: " + BG_COLOR + "; " +
        "-fx-font-weight: bold; " +
        "-fx-font-size: 13px; " +
        "-fx-background-radius: 4; " +
        "-fx-cursor: hand;"
    )
);
```

### Scene Creation

```java
Scene scene = new Scene(root);
scene.setFill(Color.web(BG_COLOR));
scene.getRoot().autosize();
return scene;
```

### Settings Persistence

```java
private final Preferences prefs = Preferences.userRoot().node("tidals_script_name");
private static final String PREF_KEY = "setting_name";

// load
boolean value = prefs.getBoolean(PREF_KEY, defaultValue);

// save
prefs.putBoolean(PREF_KEY, checkbox.isSelected());
```

### Complete ScriptUI Template

```java
package main;

import com.osmb.api.script.Script;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.prefs.Preferences;

public class ScriptUI {
    private final Preferences prefs = Preferences.userRoot().node("tidals_script_name");

    private static final String BG_COLOR = "#163134";
    private static final String BORDER_COLOR = "#284b50";
    private static final String GOLD = "#ffd700";
    private static final String TEXT_LIGHT = "#eeede9";
    private static final String TEXT_MUTED = "#aab9b9";

    private final Script script;

    public ScriptUI(Script script) {
        this.script = script;
    }

    public Scene buildScene(Script script) {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20, 24, 20, 24));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // logo
        ImageView logoView = loadLogo();
        if (logoView != null) {
            root.getChildren().add(logoView);
        }

        // version
        Label versionLabel = new Label("v" + ScriptName.scriptVersion);
        versionLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        root.getChildren().add(versionLabel);

        // spacer
        Region spacer = new Region();
        spacer.setPrefHeight(8);
        root.getChildren().add(spacer);

        // options box
        VBox optionBox = new VBox(8);
        optionBox.setAlignment(Pos.CENTER_LEFT);
        optionBox.setPadding(new Insets(12));
        optionBox.setStyle(
            "-fx-background-color: derive(" + BG_COLOR + ", -15%); " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4;"
        );

        // add options to optionBox here

        root.getChildren().add(optionBox);

        // start button
        Button startButton = new Button("Start");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setPrefHeight(36);
        startButton.setStyle(
            "-fx-background-color: " + GOLD + "; " +
            "-fx-text-fill: " + BG_COLOR + "; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand;"
        );

        startButton.setOnAction(e -> {
            saveSettings();
            ((Stage) startButton.getScene().getWindow()).close();
        });

        root.getChildren().add(startButton);

        Scene scene = new Scene(root);
        scene.setFill(Color.web(BG_COLOR));
        scene.getRoot().autosize();
        return scene;
    }

    private ImageView loadLogo() {
        try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
            if (in == null) return null;
            Image img = new Image(in);
            ImageView view = new ImageView(img);
            view.setPreserveRatio(true);
            view.setFitWidth(180);
            return view;
        } catch (Exception e) {
            return null;
        }
    }

    private void saveSettings() {
        // save preferences here
        script.log("SETTINGS", "Settings saved");
    }
}
```

---

## Logo Requirements

- **Location**: `src/main/resources/logo.png`
- **Format**: PNG with transparency
- **Setup UI width**: 180px (scaled with aspect ratio)
- **Paint overlay**: original size, centered horizontally

### Logo Loading for Paint (with premultiplied alpha)

```java
private void ensureLogoLoaded() {
    if (logoImage != null) return;

    try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
        if (in == null) {
            log(getClass(), "logo '/logo.png' not found in resources");
            return;
        }

        BufferedImage src = ImageIO.read(in);
        if (src == null) return;

        BufferedImage argb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = argb.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(src, 0, 0, null);
        g.dispose();

        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] px = new int[w * h];
        argb.getRGB(0, 0, w, h, px, 0, w);

        // premultiply alpha for correct rendering
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            int a = (p >>> 24) & 0xFF;
            if (a == 0) {
                px[i] = 0;
                continue;
            }
            int r = (p >>> 16) & 0xFF;
            int gch = (p >>> 8) & 0xFF;
            int b = p & 0xFF;
            r = (r * a + 127) / 255;
            gch = (gch * a + 127) / 255;
            b = (b * a + 127) / 255;
            px[i] = (a << 24) | (r << 16) | (gch << 8) | b;
        }

        logoImage = new Image(px, w, h);
    } catch (Exception e) {
        log(getClass(), "error loading logo: " + e.getMessage());
    }
}
```

---

## Checklist

### Paint Overlay
- [ ] Uses standard color palette (#163134, #284b50, #ffd700)
- [ ] Width is 220px, positioned at x=5, y=40
- [ ] Logo centered at top with 8px bottom gap
- [ ] Gold separator after logo
- [ ] 16px line gap between stats
- [ ] Labels on left (muted), values on right (colored by meaning)
- [ ] Border separator before version line
- [ ] Dynamic height calculation
- [ ] Number formatting with period separator

### Setup UI
- [ ] Uses same color palette as paint
- [ ] VBox with 16px spacing
- [ ] 20/24px padding (top-bottom/left-right)
- [ ] Logo fitted to 180px width
- [ ] Version label in muted color, 11px
- [ ] Option box with darker background, 1px border, 4px radius
- [ ] Start button: full width, 36px height, gold background
- [ ] Hover effect on start button
- [ ] Settings persistence via Preferences
