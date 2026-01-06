# Paint Overlay Best Practices

Professional, clean overlay design patterns for OSMB scripts.

## User Preferences (Modern Style)

Based on real user feedback from tGemCutter development.

### ✅ DO:
- Keep it clean and minimal (reference: dSunbleakWCer style)
- Logo centered at the top
- Simple white border on dark background
- Muted, professional colors (turquoise, light blue, white)
- Clear hierarchy: labels on left, values on right
- Consistent spacing (16px line gap)
- Standard fonts (Arial, 12pt)

### ❌ DON'T:
- Loud accent colors (coral pink, bright orange)
- Decorative elements (wave lines, fancy titles)
- Multiple borders or gradients
- Oversized panels
- Inconsistent color schemes

---

## Modern Paint Template

### Color Palette
```java
// Clean, professional colors
final Color bgColor = new Color(15, 52, 96);        // Deep blue background
final Color labelColor = new Color(64, 224, 208);   // Turquoise labels
final Color valueColor = Color.WHITE;               // White values
final Color accentColor = new Color(100, 149, 237); // Cornflower blue highlights
final Color progressColor = new Color(152, 251, 152); // Seafoam green progress
```

### Layout Constants
```java
final int x = 5;
final int baseY = 40;
final int width = 260;
final int borderThickness = 2;
final int paddingX = 10;
final int lineGap = 16;
final int logoBottomGap = 8;
```

---

## Complete Paint Implementation

### Fields
```java
// Paint fields
private Image logoImage = null;
private long startTime;
private int startLevel = 0;  // IMPORTANT: Initialize to 0, not 1!
private int currentLevel = 1;

private static final Font FONT_LABEL = new Font("Arial", Font.PLAIN, 12);
private static final Font FONT_VALUE_BOLD = new Font("Arial", Font.BOLD, 12);
```

### onStart - Initialize
```java
@Override
public void onStart() {
    startTime = System.currentTimeMillis();
    ensureLogoLoaded();  // Load logo from resources
}
```

### onPaint - Main Rendering
```java
@Override
public void onPaint(Canvas c) {
    long elapsed = System.currentTimeMillis() - startTime;
    double hours = Math.max(1e-9, elapsed / 3_600_000.0);
    String runtime = formatRuntime(elapsed);

    // Get live XP data (if using XP tracker)
    String ttlText = "-";
    double etl = 0.0;
    double xpGainedLive = 0.0;
    double currentXp = 0.0;
    double levelProgressFraction = 0.0;

    if (xpTracking != null) {
        XPTracker tracker = xpTracking.getWoodcuttingTracker();
        if (tracker != null) {
            xpGainedLive = tracker.getXpGained();
            currentXp = tracker.getXp();

            // Sync level (only increases)
            final int MAX_LEVEL = 99;
            int guard = 0;
            while (currentLevel < MAX_LEVEL
                    && currentXp >= tracker.getExperienceForLevel(currentLevel + 1)
                    && guard++ < 10) {
                currentLevel++;
            }

            // Handle max level
            if (currentLevel >= 99) {
                ttlText = "MAXED";
                etl = 0;
                levelProgressFraction = 1.0;
            } else {
                ttlText = tracker.timeToNextLevelString();

                // Calculate progress to next level
                int curLevelXpStart = tracker.getExperienceForLevel(currentLevel);
                int nextLevelXpTarget = tracker.getExperienceForLevel(
                    Math.min(MAX_LEVEL, currentLevel + 1));
                int span = Math.max(1, nextLevelXpTarget - curLevelXpStart);

                etl = Math.max(0, nextLevelXpTarget - currentXp);
                levelProgressFraction = Math.max(0.0, Math.min(1.0,
                    (currentXp - curLevelXpStart) / (double) span));
            }
        }
    }

    // Calculate rates
    int xpPerHour = (int) Math.round(xpGainedLive / hours);

    // Handle level gain display
    if (startLevel <= 0) startLevel = currentLevel;
    int levelsGained = Math.max(0, currentLevel - startLevel);
    String currentLevelText = (levelsGained > 0)
        ? (currentLevel + " (+" + levelsGained + ")")
        : String.valueOf(currentLevel);

    // === Panel Layout ===
    final int x = 5;
    final int baseY = 40;
    final int width = 225;
    final int borderThickness = 2;
    final int paddingX = 10;
    final int lineGap = 16;

    // Clean, professional colors
    final int labelGray = new Color(180, 180, 180).getRGB();
    final int valueWhite = Color.WHITE.getRGB();
    final int valueGreen = new Color(80, 220, 120).getRGB();
    final Color bgColor = Color.decode("#01031C");  // Deep blue/black

    int innerX = x;
    int innerY = baseY;
    int innerWidth = width;

    // Calculate panel height
    int totalLines = 8;
    int y = innerY + 6;
    if (logoImage != null) y += logoImage.height + 8;
    y += totalLines * lineGap + 16;
    int innerHeight = Math.max(200, y - innerY);

    // Draw panel border and background
    c.fillRect(innerX - borderThickness, innerY - borderThickness,
        innerWidth + (borderThickness * 2),
        innerHeight + (borderThickness * 2),
        Color.WHITE.getRGB(), 1);
    c.fillRect(innerX, innerY, innerWidth, innerHeight,
        bgColor.getRGB(), 1);
    c.drawRect(innerX, innerY, innerWidth, innerHeight,
        Color.WHITE.getRGB());

    int curY = innerY + 6;

    // Draw logo if loaded
    if (logoImage != null) {
        int imgX = innerX + (innerWidth - logoImage.width) / 2;
        c.drawAtOn(logoImage, imgX, curY);
        curY += logoImage.height + 8;
    }

    // Draw stat lines (clean, no decorations)
    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "Runtime", runtime, labelGray, valueWhite,
        FONT_VALUE_BOLD, FONT_LABEL);

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "XP/hr", formatInt(xpPerHour), labelGray, valueWhite,
        FONT_VALUE_BOLD, FONT_LABEL);

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "Level", currentLevelText, labelGray, valueGreen,
        FONT_VALUE_BOLD, FONT_LABEL);

    curY += lineGap;
    String etlText = (currentLevel >= 99) ? "MAXED" : formatInt((int) Math.round(etl));
    drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "XP to level", etlText, labelGray, valueWhite,
        FONT_VALUE_BOLD, FONT_LABEL);

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "Time to level", ttlText, labelGray, valueWhite,
        FONT_VALUE_BOLD, FONT_LABEL);
}
```

### Helper Methods
```java
private void drawStatLine(Canvas c, int innerX, int innerWidth, int paddingX, int y,
                          String label, String value, int labelColor, int valueColor,
                          Font valueFont, Font labelFont) {
    c.drawText(label, innerX + paddingX, y, labelColor, labelFont);
    int valW = c.getFontMetrics(valueFont).stringWidth(value);
    int valX = innerX + innerWidth - paddingX - valW;
    c.drawText(value, valX, y, valueColor, valueFont);
}

private void ensureLogoLoaded() {
    if (logoImage != null) return;

    try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
        if (in == null) {
            log(getClass(), "Logo '/logo.png' not found on classpath.");
            return;
        }

        BufferedImage src = ImageIO.read(in);
        if (src == null) return;

        // Convert to ARGB
        BufferedImage argb = new BufferedImage(
            src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = argb.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(src, 0, 0, null);
        g.dispose();

        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] px = new int[w * h];
        argb.getRGB(0, 0, w, h, px, 0, w);

        // Premultiply alpha for correct rendering
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            int a = (p >>> 24) & 0xFF;
            if (a == 0) {
                px[i] = 0x00000000;
            } else {
                int r = (p >>> 16) & 0xFF;
                int g_val = (p >>> 8) & 0xFF;
                int b = p & 0xFF;
                r = (r * a + 127) / 255;
                g_val = (g_val * a + 127) / 255;
                b = (b * a + 127) / 255;
                px[i] = (a << 24) | (r << 16) | (g_val << 8) | b;
            }
        }

        logoImage = new Image(px, w, h);
    } catch (Exception e) {
        log(getClass(), "Error loading logo: " + e.getMessage());
    }
}

private String formatRuntime(long ms) {
    long seconds = ms / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;

    return String.format("%02d:%02d:%02d",
        hours, minutes % 60, seconds % 60);
}

private String formatInt(int value) {
    return String.format("%,d", value);
}
```

---

## Level 99 Handling (CRITICAL!)

### The Problem
```java
// WRONG - Shows negative XP and "99 (+98)" levels gained
public static int startLevel = 1;  // Initialized to 1

// In onPaint()
if (startLevel <= 0) startLevel = currentLevel;  // Never triggers because 1 > 0!
int levelsGained = Math.max(0, currentLevel - startLevel);  // 99 - 1 = 98!
```

### The Solution
```java
// CORRECT - Initialize to 0
public static int startLevel = 0;  // Initialized to 0

// In onPaint()
if (startLevel <= 0) startLevel = currentLevel;  // Triggers correctly!

// Handle max level specially
if (currentLevel >= 99) {
    ttlText = "MAXED";
    etl = 0;
    levelProgressFraction = 1.0;
    levelProgressText = "MAXED";
} else {
    // Normal level calculations
}

// ETL display
String etlText = (currentLevel >= 99) ? "MAXED" : intFmt.format(Math.round(etl));
```

---

## Resources Folder Location (CRITICAL!)

### The Problem
```java
// WRONG - Resources in wrong location
// tGemCutter/resources/logo.png  ❌ Won't be found!
```

### The Solution
```java
// CORRECT - Resources must be in src/main/resources
// tGemCutter/src/main/resources/logo.png  ✅ Found by classloader!

// Loading code
try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
    // This looks for logo.png at the root of the classpath
    // which is src/main/resources/ after building
}
```

**Fix**: Always put resources in `src/main/resources/`, not just `resources/`

---

## Progress Bar Example (Optional)

```java
// If you want to add a progress bar
private void drawProgressBar(Canvas c, int x, int y, int width, int height, 
                             double progress, Color fillColor, Color bgColor) {
    // Background
    c.fillRect(x, y, width, height, bgColor.getRGB(), 1);
    
    // Progress fill
    int fillWidth = (int) (width * Math.min(1.0, Math.max(0.0, progress)));
    if (fillWidth > 0) {
        c.fillRect(x, y, fillWidth, height, fillColor.getRGB(), 1);
    }
    
    // Border
    c.drawRect(x, y, width, height, Color.WHITE.getRGB());
}

// Usage in onPaint:
drawProgressBar(c, innerX + paddingX, curY, 
    innerWidth - (paddingX * 2), 8,
    levelProgressFraction, progressColor, new Color(40, 40, 40));
```

---

## Best Practices Summary

1. **Initialize startLevel to 0**, not 1
2. **Put resources in src/main/resources/**
3. **Use muted, professional colors**
4. **Keep layout clean and minimal**
5. **Handle level 99 specially** (show "MAXED")
6. **Center logo at top**
7. **Consistent spacing** (16px line gap)
8. **Labels on left, values on right**
9. **Premultiply alpha** for logo rendering
10. **Add thousands separator** to large numbers

---

## Quick Checklist

Before releasing a script, verify:
- [ ] Logo loads correctly from src/main/resources/
- [ ] startLevel initialized to 0
- [ ] Level 99 shows "MAXED" not negative values
- [ ] Colors are muted and professional
- [ ] Layout is clean with consistent spacing
- [ ] XP calculations handle division by zero
- [ ] Paint doesn't cause lag (keep it simple)
