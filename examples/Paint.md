# Paint Overlay Template

Complete, copy-paste ready template for paint overlays. Based on TidalsGemMiner and TidalsCannonballThiever.

---

## Quick Start

Copy the complete template at the bottom of this file. The template includes:
- XP tracking with custom trackers (reliable at startup)
- Dynamic height calculation
- Proper color scheme
- Logo loading with premultiplied alpha
- Number formatting (period as thousands separator)

---

## XP Tracking Pattern

**Problem**: OSMB's built-in `getXPTrackers()` returns null at startup and is unreliable.

**Solution**: Create custom `XPTracker` instances and manually increment XP when actions complete.

### XPTracking Utility Class

Create `utils/XPTracking.java`:

```java
package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;

import java.util.Map;

public class XPTracking {

    private final ScriptCore core;
    private boolean initialized = false;
    private boolean recalibrated = false;

    private XPTracker customTracker;
    private double customXpGained = 0.0;

    public XPTracking(ScriptCore core) {
        this.core = core;
    }

    // try to get actual XP from built-in tracker
    public static int tryGetActualXp(ScriptCore core, SkillType skill) {
        try {
            Map<SkillType, XPTracker> trackers = core.getXPTrackers();
            if (trackers != null && trackers.containsKey(skill)) {
                XPTracker tracker = trackers.get(skill);
                if (tracker != null) {
                    return (int) tracker.getXp();
                }
            }
        } catch (Exception e) {
            // fall through
        }
        return -1;
    }

    // initialize with actual current XP (preferred)
    public void initTracker(int startingLevel, int actualCurrentXp) {
        customTracker = new XPTracker(core, actualCurrentXp);
        customXpGained = 0.0;
        initialized = true;
    }

    // initialize with starting level (fallback)
    public void initTracker(int startingLevel) {
        XPTracker temp = new XPTracker(core, 0);
        int startingXp = temp.getExperienceForLevel(startingLevel);
        customTracker = new XPTracker(core, startingXp);
        customXpGained = 0.0;
        initialized = true;
    }

    // call this when action completes (e.g., item crafted, ore mined)
    public void addXp(double xp) {
        customXpGained += xp;
        if (customTracker != null) {
            customTracker.incrementXp(xp);
        }

        if (!recalibrated) {
            tryRecalibrate();
        }
    }

    private void tryRecalibrate() {
        recalibrated = true;
        // recalibration logic - see full XPTracking.java for details
    }

    public XPTracker getTracker() {
        return customTracker;
    }

    public double getXpGained() {
        return customXpGained;
    }

    public int getXpPerHour() {
        if (customTracker == null) return 0;
        return customTracker.getXpPerHour();
    }

    public String getTimeToNextLevel() {
        if (customTracker == null) return "-";
        return customTracker.timeToNextLevelString();
    }
}
```

### Initialize in Setup Task

```java
// in Setup.java execute()
SkillsTabComponent.SkillLevel skillLevel = script.getWidgetManager()
        .getSkillTab()
        .getSkillLevel(SkillType.MINING);

if (skillLevel == null) {
    script.log(getClass(), "Failed to get skill level");
    return false;
}

int level = skillLevel.getLevel();
startLevel = level;
currentLevel = level;

// initialize custom tracker
int actualXp = XPTracking.tryGetActualXp(script, SkillType.MINING);
if (actualXp > 0) {
    xpTracking.initTracker(level, actualXp);
} else {
    xpTracking.initTracker(level);
}
```

### Add XP When Action Completes

```java
// in task execute() when action completes
if (mined) {
    gemsMined++;
    if (TidalsGemMiner.xpTracking != null) {
        TidalsGemMiner.xpTracking.addXp(65.0);  // 65 XP per gem rock
    }
}
```

---

## Paint Layout

### Visual Structure

```
+--[border: 2px #284b50]------------------+
|                                          |
|  +--[background: #163134]--------------+ |
|  |        [LOGO - centered]            | |
|  |  ---------------------------------- | |  <- gold separator
|  |                                     | |
|  |  Runtime              00:12:34      | |
|  |  Items                1.234 (500/hr)| |
|  |  XP gained            12.345        | |
|  |  XP/hr                45.678        | |
|  |  Level                65 (+2)       | |
|  |  TTL                  01:23:45      | |
|  |                                     | |
|  |  ---------------------------------- | |  <- border separator + 10px padding
|  |                                     | |
|  |  State                Mining        | |
|  |  Version              1.0           | |
|  +-------------------------------------+ |
+------------------------------------------+
```

### Color Usage

| Element | Color | RGB |
|---------|-------|-----|
| Background | Dark teal | `22, 49, 52` |
| Border | Light teal | `40, 75, 80` |
| Gold accent | Gold | `255, 215, 0` |
| Text (labels) | Muted | `170, 185, 185` |
| Text (values) | Light | `238, 237, 233` |
| Values (positive) | Green | `180, 230, 150` |
| XP/hr, totals | Gold | `255, 215, 0` |

---

## Complete Paint Template

Copy this entire section into your main script class:

```java
// === PAINT FIELDS ===

private Image logoImage = null;
private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 12);
private static final Font FONT_VALUE = new Font("Arial", Font.BOLD, 12);
private static final java.text.DecimalFormat intFmt;
static {
    intFmt = new java.text.DecimalFormat("#,###");
    java.text.DecimalFormatSymbols sym = new java.text.DecimalFormatSymbols();
    sym.setGroupingSeparator('.');
    intFmt.setDecimalFormatSymbols(sym);
}

// === PAINT METHOD ===

@Override
public void onPaint(Canvas c) {
    if (c == null) return;

    long elapsed = System.currentTimeMillis() - startTime;
    String runtime = formatRuntime(elapsed);
    double hours = Math.max(1e-9, elapsed / 3_600_000.0);

    // get XP stats from custom tracker
    int xpGained = 0;
    int xpPerHour = 0;
    String ttl = "-";
    String levelText = String.valueOf(currentLevel);

    if (xpTracking != null) {
        XPTracker tracker = xpTracking.getTracker();
        if (tracker != null) {
            xpGained = (int) xpTracking.getXpGained();
            xpPerHour = tracker.getXpPerHour();
            ttl = tracker.timeToNextLevelString();

            // sync level from tracker
            final int MAX_LEVEL = 99;
            double currentXp = tracker.getXp();
            int guard = 0;
            while (currentLevel < MAX_LEVEL
                    && currentXp >= tracker.getExperienceForLevel(currentLevel + 1)
                    && guard++ < 10) {
                currentLevel++;
            }
            if (currentLevel >= MAX_LEVEL) {
                ttl = "MAXED";
            }
        }

        // fallback to manual calculation
        if (xpPerHour == 0 && xpGained > 0) {
            xpPerHour = (int) (xpGained / hours);
        }
    }

    // level display with gains
    if (startLevel <= 0) startLevel = currentLevel;
    int levelsGained = Math.max(0, currentLevel - startLevel);
    if (levelsGained > 0) {
        levelText = currentLevel + " (+" + levelsGained + ")";
    }

    int itemsHr = (int) (itemCount / hours);

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

    // calculate dynamic height
    // lines: runtime, items, xp, xp/hr, level, ttl, state, version = 8
    int lineCount = 8;
    int separatorCount = 1;  // before footer
    int separatorOverhead = separatorCount * 12;
    int footerPadding = 10;  // extra padding before state
    int bottomPadding = 10;
    int contentHeight = topGap + logoHeight + (lineCount * lineGap) + separatorOverhead + footerPadding + bottomPadding;
    int innerHeight = contentHeight;

    // outer border
    c.fillRect(innerX - borderThickness, innerY - borderThickness,
            innerWidth + (borderThickness * 2),
            innerHeight + (borderThickness * 2),
            borderColor.getRGB(), 1);

    // main background
    c.fillRect(innerX, innerY, innerWidth, innerHeight, bgColor.getRGB(), 1);
    c.drawRect(innerX, innerY, innerWidth, innerHeight, borderColor.getRGB());

    int curY = innerY + topGap;

    // logo centered
    if (logoImage != null) {
        int logoX = innerX + (innerWidth - logoImage.width) / 2;
        c.drawAtOn(logoImage, logoX, curY);
        curY += logoImage.height + logoBottomGap;
    }

    // gold separator after logo
    c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, accentGold.getRGB(), 1);
    curY += 16;

    // stats
    drawStatLine(c, innerX, innerWidth, paddingX, curY, "Runtime", runtime, textMuted.getRGB(), textLight.getRGB());

    curY += lineGap;
    String itemsText = intFmt.format(itemCount) + " (" + intFmt.format(itemsHr) + "/hr)";
    drawStatLine(c, innerX, innerWidth, paddingX, curY, "Items", itemsText, textMuted.getRGB(), valueGreen.getRGB());

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY, "XP gained", intFmt.format(xpGained), textMuted.getRGB(), valueGreen.getRGB());

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY, "XP/hr", intFmt.format(xpPerHour), textMuted.getRGB(), accentGold.getRGB());

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY, "Level", levelText, textMuted.getRGB(), textLight.getRGB());

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY, "TTL", ttl, textMuted.getRGB(), textLight.getRGB());

    // separator before footer (with 10px extra padding)
    curY += lineGap - 4;
    c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
    curY += 22;  // 12 + 10px padding

    drawStatLine(c, innerX, innerWidth, paddingX, curY, "State", task, textMuted.getRGB(), textLight.getRGB());

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY, "Version", scriptVersion, textMuted.getRGB(), textMuted.getRGB());
}

private void drawStatLine(Canvas c, int innerX, int innerWidth, int paddingX, int y,
                          String label, String value, int labelColor, int valueColor) {
    c.drawText(label, innerX + paddingX, y, labelColor, FONT_LABEL);
    int valW = c.getFontMetrics(FONT_VALUE).stringWidth(value);
    int valX = innerX + innerWidth - paddingX - valW;
    c.drawText(value, valX, y, valueColor, FONT_VALUE);
}

private void ensureLogoLoaded() {
    if (logoImage != null) return;

    try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
        if (in == null) {
            log(getClass(), "logo not found in resources");
            return;
        }

        BufferedImage src = ImageIO.read(in);
        if (src == null) return;

        // scale to 180px width
        int targetWidth = 180;
        double scale = (double) targetWidth / src.getWidth();
        int targetHeight = (int) (src.getHeight() * scale);

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        int w = scaled.getWidth();
        int h = scaled.getHeight();
        int[] px = new int[w * h];
        scaled.getRGB(0, 0, w, h, px, 0, w);

        // premultiply alpha for OSMB canvas
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
        log(getClass(), "logo loaded: " + w + "x" + h);

    } catch (Exception e) {
        log(getClass(), "error loading logo: " + e.getMessage());
    }
}

private String formatRuntime(long millis) {
    if (millis <= 0) return "00:00:00";
    long seconds = millis / 1000;
    long days = seconds / 86400;
    long hours = (seconds % 86400) / 3600;
    long minutes = (seconds % 3600) / 60;
    long secs = seconds % 60;

    if (days > 0) {
        return String.format("%dd %02d:%02d:%02d", days, hours, minutes, secs);
    } else {
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
```

---

## Required Imports

```java
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.visual.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
```

---

## Adding More Sections

To add a new section (like a second skill or item type breakdown):

```java
// separator before new section
curY += lineGap - 4;
c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
curY += 12;

// new section stats
drawStatLine(c, innerX, innerWidth, paddingX, curY, "New stat", value, textMuted.getRGB(), valueGreen.getRGB());
curY += lineGap;
// ... more stats

// update lineCount and separatorCount in height calculation
```

---

## Checklist

- [ ] XPTracking class created in utils/
- [ ] XP tracker initialized in Setup task
- [ ] XP added when actions complete (in task execute methods)
- [ ] Logo at `/src/main/resources/logo.png`
- [ ] Static fields: `startTime`, `task`, `startLevel`, `currentLevel`, `xpTracking`
- [ ] All colors use standard palette
- [ ] Width is 220px
- [ ] 10px padding before State line
- [ ] Dynamic height calculation matches content
