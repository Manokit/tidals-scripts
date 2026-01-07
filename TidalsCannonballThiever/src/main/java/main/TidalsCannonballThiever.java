package main;

import com.osmb.api.ScriptCore;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.visual.image.Image;
import javafx.scene.Scene;
import tasks.*;
import utils.GuardTracker;
import utils.Task;
import utils.XPTracking;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

@ScriptDefinition(
        name = "TidalsCannonballThiever",
        description = "Thieves cannonballs from Port Roberts stalls while avoiding guards",
        skillCategory = SkillCategory.THIEVING,
        version = 1.0,
        author = "Tidalus"
)
public class TidalsCannonballThiever extends Script {
    public static final String scriptVersion = "1.0";
    private final String scriptName = "CannonballThiever";

    public static int screenWidth = 0;
    public static int screenHeight = 0;

    public static boolean setupDone = false;
    public static int cannonballsStolen = 0;
    public static int oresStolen = 0;
    public static Timer lastXpGain = new Timer();

    // mode flags
    public static boolean twoStallMode = false;
    public static boolean atOreStall = false;  // track which stall we're at in two-stall mode

    // cannonball types from port roberts stall (sailing content)
    // map: name -> itemID
    private static final Map<String, Integer> CANNONBALL_TYPES = new LinkedHashMap<>() {{
        put("Bronze", 29387);
        put("Iron", 29389);
        put("Steel", 29391);
        put("Mithril", 29393);
        put("Adamant", 29395);
        put("Rune", 29397);
    }};

    // track counts for each type
    public static Map<String, Integer> cannonballCounts = new LinkedHashMap<>();
    private Map<String, Integer> lastInventoryCounts = new HashMap<>();
    private long lastInventoryCheck = 0;
    private static final long INVENTORY_CHECK_INTERVAL_MS = 500; // only check every 500ms

    // flag to track if we're actively thieving (set when we click stall, cleared on retreat)
    public static boolean currentlyThieving = false;

    public static String task = "Initializing...";
    public static long startTime = System.currentTimeMillis();

    private List<Task> tasks;
    private static final Font FONT_LABEL       = new Font("Arial", Font.PLAIN, 12);
    private static final Font FONT_VALUE_BOLD  = new Font("Arial", Font.BOLD, 12);

    public static double levelProgressFraction = 0.0;
    public static int currentLevel = 1;
    public static int startLevel = 1;

    private final XPTracking xpTracking;
    private int xpGained = 0;

    // Guard tracker
    public static GuardTracker guardTracker;

    // Logo image
    private Image logoImage = null;
    private boolean logoLoadAttempted = false;

    // UI
    private ScriptUI scriptUI;

    public TidalsCannonballThiever(Object scriptCore) {
        super(scriptCore);
        this.xpTracking = new XPTracking(this);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[]{7475}; // port roberts cannonball stall area
    }

    // only allow world hop / break / afk when at safe position and not thieving
    // two-stall mode: allow during stall transitions (before switching to ore stall)
    // single mode: at safety tile
    @Override
    public boolean canHopWorlds() {
        if (currentlyThieving) return false;
        if (twoStallMode) {
            // in two-stall mode, allow during transition when at ore stall
            return atOreStall && !currentlyThieving;
        }
        return isAtSafetyTile();
    }

    @Override
    public boolean canBreak() {
        if (currentlyThieving) return false;
        if (twoStallMode) {
            return atOreStall && !currentlyThieving;
        }
        return isAtSafetyTile();
    }

    @Override
    public boolean canAFK() {
        if (currentlyThieving) return false;
        if (twoStallMode) {
            return atOreStall && !currentlyThieving;
        }
        return isAtSafetyTile();
    }

    private boolean isAtSafetyTile() {
        WorldPosition pos = getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == 1867 && y == 3299;
    }

    @Override
    public void onStart() {
        log("INFO", "Starting TidalsCannonballThiever v" + scriptVersion);

        // Show UI for mode selection
        scriptUI = new ScriptUI(this);
        Scene scene = scriptUI.buildScene(this);
        getStageController().show(scene, "Cannonball Thieving Options", false);

        // Get selected mode
        twoStallMode = scriptUI.isTwoStallMode();
        log("UI", "Mode selected: " + (twoStallMode ? "Two Stall" : "Single Stall"));

        // Initialize guard tracker
        guardTracker = new GuardTracker(this);

        // Initialize cannonball counts
        for (String type : CANNONBALL_TYPES.keySet()) {
            cannonballCounts.put(type, 0);
            lastInventoryCounts.put(type, 0);
        }

        // Initialize tasks in priority order
        // Two-stall mode tasks are added but only activate when twoStallMode is true
        tasks = Arrays.asList(
                new Setup(this),
                new EscapeJail(this),           // high priority - escape jail if caught
                new DepositOres(this),          // two-stall: deposit when inventory full
                new SwitchToOreStall(this),     // two-stall: switch to ore stall when guard approaches
                new SwitchToCannonballStall(this), // two-stall: switch back when guard near ore stall
                new Retreat(this),              // single-stall: retreat to safety
                new WaitAtSafety(this),         // single-stall: wait at safety tile
                new ReturnToThieving(this),
                new StartThieving(this),
                new MonitorThieving(this)
        );

        log("INFO", "Tasks initialized: " + tasks.size());
    }

    @Override
    public int poll() {
        for (Task task : tasks) {
            if (task.activate()) {
                task.execute();
                return 0;
            }
        }
        return 0;
    }

    @Override
    public void onNewFrame() {
        xpTracking.checkXP();
        
        // TWO-STALL MODE: Continuously watch guard at specific danger tiles for instant detection
        // This runs every frame for pixel-level movement detection (10-20x faster than tile updates!)
        if (twoStallMode && setupDone && guardTracker != null && currentlyThieving) {
            if (atOreStall) {
                // At ore stall: watch guard at (1863, 3292)
                guardTracker.shouldSwitchToCannonball();
            } else {
                // At cannonball stall: watch guard at (1865, 3295)
                guardTracker.shouldSwitchToOre();
            }
        }
        
        // CRITICAL: only check inventory when actively thieving
        // prevents conflicts with world hop / break handler trying to open logout tab
        if (!currentlyThieving) {
            return;
        }
        
        // throttle inventory checks to prevent spam (only every 500ms)
        long now = System.currentTimeMillis();
        if (!setupDone || now - lastInventoryCheck < INVENTORY_CHECK_INTERVAL_MS) {
            return;
        }
        lastInventoryCheck = now;
        
        // track cannonball counts by type (like Chop.java does for logs)
        try {
            // get all cannonball item IDs
            Set<Integer> allIds = new HashSet<>(CANNONBALL_TYPES.values());
            ItemGroupResult inv = getWidgetManager().getInventory().search(allIds);
            
            if (inv != null) {
                for (Map.Entry<String, Integer> entry : CANNONBALL_TYPES.entrySet()) {
                    String type = entry.getKey();
                    int itemId = entry.getValue();
                    
                    int currentCount = inv.getAmount(itemId);
                    int lastCount = lastInventoryCounts.getOrDefault(type, 0);
                    
                    if (currentCount > lastCount) {
                        int gained = currentCount - lastCount;
                        cannonballCounts.merge(type, gained, Integer::sum);
                        cannonballsStolen += gained;
                    }
                    lastInventoryCounts.put(type, currentCount);
                }
            }
            
            // track ores in two-stall mode (when at ore stall)
            if (twoStallMode && atOreStall) {
                // count total inventory slots used - ores are unstackable
                // just track how many items we have total when at ore stall
                int currentOreCount = 0;
                ItemGroupResult invCheck = getWidgetManager().getInventory().search(allIds);
                if (invCheck != null) {
                    // get count of non-cannonball items (subtract cannonballs from total)
                    currentOreCount = invCheck.getAmount();
                }
                // Since we can't easily count "all" items, we'll track ore gains via XP instead
                // or by counting slot changes - for now, skip direct ore counting here
                // The DepositOres task will increment oresStolen when it deposits
            }
        } catch (Exception ignored) {
            // inventory read failed - silently skip this check
        }
    }

    @Override
    public void onPaint(Canvas c) {
        long elapsed = System.currentTimeMillis() - startTime;
        double hours = Math.max(1e-9, elapsed / 3_600_000.0);
        String runtime = formatRuntime(elapsed);

        // === Live XP via tracker (Thieving) ===
        String ttlText = "-";
        double etl = 0.0;
        double xpGainedLive = 0.0;
        double currentXp = 0.0;

        if (xpTracking != null) {
            XPTracker tracker = xpTracking.getThievingTracker();
            if (tracker != null) {
                xpGainedLive = tracker.getXpGained();
                currentXp = tracker.getXp();
                ttlText = tracker.timeToNextLevelString();
                etl = tracker.getXpForNextLevel();

                // Level sync (only increases)
                final int MAX_LEVEL = 99;
                int guard = 0;
                while (currentLevel < MAX_LEVEL
                        && currentXp >= tracker.getExperienceForLevel(currentLevel + 1)
                        && guard++ < 10) {
                    currentLevel++;
                }

                // Handle level 99 specially
                if (currentLevel >= MAX_LEVEL) {
                    ttlText = "MAXED";
                    etl = 0;
                    levelProgressFraction = 1.0;
                } else {
                    int curLevelXpStart = tracker.getExperienceForLevel(currentLevel);
                    int nextLevelXpTarget = tracker.getExperienceForLevel(currentLevel + 1);
                    int span = Math.max(1, nextLevelXpTarget - curLevelXpStart);

                    levelProgressFraction = Math.max(0.0, Math.min(1.0,
                            (currentXp - curLevelXpStart) / (double) span));
                }
            }
        }

        int xpPerHourLive = (int) Math.round(xpGainedLive / hours);
        int xpGainedInt = (int) Math.round(xpGainedLive);
        xpGained = xpGainedInt;

        // Totals & rates
        int cannonballsHr = (int) Math.round(cannonballsStolen / hours);

        // Current level text with (+N)
        if (startLevel <= 0) startLevel = currentLevel;
        int levelsGained = Math.max(0, currentLevel - startLevel);
        String currentLevelText = (levelsGained > 0)
                ? (currentLevel + " (+" + levelsGained + ")")
                : String.valueOf(currentLevel);

        // Percent text
        double pct = Math.max(0, Math.min(100, levelProgressFraction * 100.0));
        String levelProgressText;
        if (currentLevel >= 99) {
            levelProgressText = "MAXED";
        } else {
            levelProgressText = (Math.abs(pct - Math.rint(pct)) < 1e-9)
                    ? String.format(java.util.Locale.US, "%.0f%%", pct)
                    : String.format(java.util.Locale.US, "%.1f%%", pct);
        }

        // Formatting with dots
        java.text.DecimalFormat intFmt = new java.text.DecimalFormat("#,###");
        java.text.DecimalFormatSymbols sym = new java.text.DecimalFormatSymbols();
        sym.setGroupingSeparator('.');
        intFmt.setDecimalFormatSymbols(sym);

        // === Ocean Theme Colors (Clean & Modern) ===
        final Color oceanDeep = new Color(15, 52, 96);           // Deep ocean blue background
        final Color turquoise = new Color(64, 224, 208);         // Turquoise for labels
        final Color seafoamGreen = new Color(152, 251, 152);     // Seafoam green for progress
        final Color oceanAccent = new Color(100, 149, 237);      // Cornflower blue for highlights

        // === Panel + layout ===
        final int x = 5;
        final int baseY = 40;
        final int width = 260;
        final int borderThickness = 2;
        final int paddingX = 10;
        final int topGap = 6;
        final int lineGap = 16;
        final int logoBottomGap = 8;

        final int labelColor = turquoise.getRGB();
        final int valueWhite = Color.WHITE.getRGB();
        final int valueGreen = seafoamGreen.getRGB();
        final int valueBlue = oceanAccent.getRGB();

        int innerX = x;
        int innerY = baseY;
        int innerWidth = width;

        // load logo first so we can account for its height
        ensureLogoLoaded();
        int logoHeight = (logoImage != null) ? logoImage.height + logoBottomGap : 0;

        // count non-zero cannonball types for display
        int nonZeroTypes = 0;
        for (int count : cannonballCounts.values()) {
            if (count > 0) nonZeroTypes++;
        }
        
        // extra line for ores in two-stall mode, plus mode indicator
        int extraLines = twoStallMode ? 2 : 0; // mode line + ores line (if any)
        int totalLines = 10 + nonZeroTypes + extraLines; // base lines + one per non-zero cannonball type
        int contentHeight = topGap + logoHeight + (totalLines * lineGap) + 10;
        int innerHeight = Math.max(230, contentHeight);

        // clean ocean panel
        c.fillRect(innerX - borderThickness, innerY - borderThickness,
                innerWidth + (borderThickness * 2),
                innerHeight + (borderThickness * 2),
                Color.WHITE.getRGB(), 1);
        c.fillRect(innerX, innerY, innerWidth, innerHeight, oceanDeep.getRGB(), 1);
        c.drawRect(innerX, innerY, innerWidth, innerHeight, Color.WHITE.getRGB());

        int curY = innerY + topGap;

        // logo at top (centered)
        if (logoImage != null) {
            int logoX = innerX + (innerWidth - logoImage.width) / 2;
            c.drawAtOn(logoImage, logoX, curY);
            curY += logoImage.height + logoBottomGap;
        }

        // === Stat Lines ===

        // 1) Runtime
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Runtime", runtime, labelColor, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 2) Individual cannonball types (only show non-zero)
        for (Map.Entry<String, Integer> entry : cannonballCounts.entrySet()) {
            int count = entry.getValue();
            if (count > 0) {
                curY += lineGap;
                int perHour = (int) Math.round(count / hours);
                String text = intFmt.format(count) + " (" + intFmt.format(perHour) + "/hr)";
                drawStatLine(c, innerX, innerWidth, paddingX, curY,
                        entry.getKey(), text, labelColor, valueBlue,
                        FONT_VALUE_BOLD, FONT_LABEL);
            }
        }

        // 3) Total cannonballs
        curY += lineGap;
        String cannonballsText = intFmt.format(cannonballsStolen) + " (" + intFmt.format(cannonballsHr) + "/hr)";
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Total CB", cannonballsText, labelColor, valueGreen,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 3b) Ores (two-stall mode only)
        if (twoStallMode) {
            curY += lineGap;
            int oresHr = (int) Math.round(oresStolen / hours);
            String oresText = intFmt.format(oresStolen) + " (" + intFmt.format(oresHr) + "/hr)";
            drawStatLine(c, innerX, innerWidth, paddingX, curY,
                    "Ores", oresText, labelColor, valueBlue,
                    FONT_VALUE_BOLD, FONT_LABEL);
        }

        // 4) XP gained
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "XP gained", intFmt.format(xpGainedInt), labelColor, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 4) XP/hr
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "XP/hr", intFmt.format(xpPerHourLive), labelColor, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 5) ETL
        curY += lineGap;
        String etlText = (currentLevel >= 99) ? "MAXED" : intFmt.format(Math.round(etl));
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "ETL", etlText, labelColor, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 6) TTL
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "TTL", ttlText, labelColor, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 7) Level progress
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Level progress", levelProgressText, labelColor, valueGreen,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 8) Current level
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Current level", currentLevelText, labelColor, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 9) Task
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Task", String.valueOf(task), labelColor, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 10) Version
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Version", scriptVersion, labelColor, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 11) Mode (two-stall mode only)
        if (twoStallMode) {
            curY += lineGap;
            String modeText = atOreStall ? "Ore Stall" : "Cannonball Stall";
            drawStatLine(c, innerX, innerWidth, paddingX, curY,
                    "Mode", "Two Stall (" + modeText + ")", labelColor, turquoise.getRGB(),
                    FONT_VALUE_BOLD, FONT_LABEL);
        }
    }

    private void drawStatLine(Canvas c, int innerX, int innerWidth, int paddingX, int y,
                              String label, String value, int labelColor, int valueColor,
                              Font labelFont, Font valueFont) {
        c.drawText(label, innerX + paddingX, y, labelColor, labelFont);
        int valW = c.getFontMetrics(valueFont).stringWidth(value);
        int valX = innerX + innerWidth - paddingX - valW;
        c.drawText(value, valX, y, valueColor, valueFont);
    }

    private String formatRuntime(long millis) {
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

    private void ensureLogoLoaded() {
        if (logoImage != null || logoLoadAttempted) return;
        logoLoadAttempted = true;

        try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
            if (in == null) {
                log(getClass(), "Logo '/logo.png' not found in resources.");
                return;
            }

            BufferedImage src = ImageIO.read(in);
            if (src == null) {
                log(getClass(), "Failed to decode logo.png");
                return;
            }

            // Convert to ARGB with premultiplied alpha
            BufferedImage argb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = argb.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(src, 0, 0, null);
            g.dispose();

            int w = argb.getWidth();
            int h = argb.getHeight();
            int[] px = new int[w * h];
            argb.getRGB(0, 0, w, h, px, 0, w);

            // Premultiply alpha
            for (int i = 0; i < px.length; i++) {
                int p = px[i];
                int a = (p >>> 24) & 0xFF;
                if (a == 0) { px[i] = 0; continue; }
                int r = (p >>> 16) & 0xFF;
                int gch = (p >>> 8) & 0xFF;
                int b = p & 0xFF;
                r = (r * a + 127) / 255;
                gch = (gch * a + 127) / 255;
                b = (b * a + 127) / 255;
                px[i] = (a << 24) | (r << 16) | (gch << 8) | b;
            }

            logoImage = new Image(px, w, h);
            log(getClass(), "Logo loaded: " + w + "x" + h);

        } catch (Exception e) {
            log(getClass(), "Error loading logo: " + e.getMessage());
        }
    }
}
