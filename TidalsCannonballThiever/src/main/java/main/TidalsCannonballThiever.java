package main;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.visual.drawing.Canvas;
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

import java.awt.*;

@ScriptDefinition(
        name = "TidalsCannonballThiever",
        description = "Thieves cannonballs from Port Roberts stalls while avoiding guards",
        skillCategory = SkillCategory.THIEVING,
        version = 1.0,
        author = "Tidalus"
)
public class TidalsCannonballThiever extends Script {
    public static final String scriptVersion = "1.2";

    public static int screenWidth = 0;
    public static int screenHeight = 0;

    public static boolean setupDone = false;
    public static int cannonballsStolen = 0;
    public static int oresStolen = 0;
    public static Timer lastXpGain = new Timer();

    // mode flags
    public static boolean twoStallMode = false;
    public static boolean atOreStall = false;  // track which stall we're at in two-stall mode
    public static boolean doingDepositRun = false;  // track if we're doing a deposit run (allows breaks)

    // Cannonball types from Port Roberts stall (correct IDs)
    private static final Map<String, Integer> CANNONBALL_TYPES = new LinkedHashMap<>() {{
        put("Bronze CB", 31906);
        put("Iron CB", 31908);
        put("Steel CB", 2);
        put("Mithril CB", 31910);
        put("Adamant CB", 31912);
        put("Rune CB", 31914);
    }};

    // Ore types from Port Roberts stall
    private static final Map<String, Integer> ORE_TYPES = new LinkedHashMap<>() {{
        put("Iron ore", 440);
        put("Coal", 453);
        put("Silver ore", 442);
        put("Gold ore", 444);
        put("Mithril ore", 447);
        put("Adamantite ore", 449);
        put("Runite ore", 451);
    }};

    // Track counts for each type (public for paint access)
    public static Map<String, Integer> cannonballCounts = new LinkedHashMap<>();
    public static Map<String, Integer> oreCounts = new LinkedHashMap<>();
    
    // Last inventory snapshot for detecting changes
    private Map<Integer, Integer> lastInventorySnapshot = new HashMap<>();
    private boolean inventoryInitialized = false;

    // flag to track if we're actively thieving (set when we click stall, cleared on retreat)
    public static boolean currentlyThieving = false;

    public static String task = "Initializing...";
    public static long startTime = System.currentTimeMillis();

    private List<Task> tasks;
    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_LABEL = new Font("Arial", Font.PLAIN, 12);

    public static double levelProgressFraction = 0.0;
    public static int currentLevel = 1;
    public static int startLevel = 1;

    public static XPTracking xpTracking;
    private int xpGained = 0;

    // Guard tracker
    public static GuardTracker guardTracker;



    // UI
    private ScriptUI scriptUI;

    public TidalsCannonballThiever(Object scriptCore) {
        super(scriptCore);
        xpTracking = new XPTracking(this);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[]{7475}; // port roberts cannonball stall area
    }

    // World hop: Only when safe (at safety tile, doing deposit, or not thieving)
    // PrepareForBreak will proactively move to safety when hop is needed
    @Override
    public boolean canHopWorlds() {
        // Allow during deposit runs
        if (doingDepositRun) return true;
        // Allow when not actively thieving
        if (!currentlyThieving) return true;
        // Allow when at safety tile
        if (isAtSafetyTile()) return true;
        // Block hop while mid-thieve - PrepareForBreak will move us to safety
        return false;
    }

    @Override
    public boolean canBreak() {
        // Breaks are longer - only allow at safety tile
        if (currentlyThieving) return false;
        if (doingDepositRun) return true;
        return isAtSafetyTile();
    }

    @Override
    public boolean canAFK() {
        // AFK pauses are longer - only allow at safety tile
        if (currentlyThieving) return false;
        if (doingDepositRun) return true;
        return isAtSafetyTile();
    }

    private boolean isAtSafetyTile() {
        WorldPosition pos = getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        // Original safety tile (single stall) or break safety tile (two stall)
        return (x == 1867 && y == 3299) || (x == 1867 && y == 3294);
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

        // Reset static state from tasks (important for script restarts!)
        StartThieving.resetStaticState();

        // Initialize cannonball and ore counts
        for (String type : CANNONBALL_TYPES.keySet()) {
            cannonballCounts.put(type, 0);
        }
        for (String type : ORE_TYPES.keySet()) {
            oreCounts.put(type, 0);
        }

        // Initialize tasks in priority order
        // Two-stall mode tasks are added but only activate when twoStallMode is true
        tasks = Arrays.asList(
                new Setup(this),
                new EscapeJail(this),           // high priority - escape jail if caught
                new DepositOres(this),          // two-stall: deposit when inventory full
                new PrepareForBreak(this),      // two-stall: periodically allow breaks/hops/AFKs
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
        // Skip all operations if world position is null (loading, hopping, etc.)
        // This prevents interfering with world hops by opening inventory
        if (getWorldPosition() == null) {
            return;
        }
        
        // Check for XP drops and trigger inventory check if XP gained
        boolean xpGained = xpTracking.checkXPAndReturnIfGained();
        
        // TWO-STALL MODE: Track XP drops for cycle counting EVERY FRAME
        // This ensures we don't miss drops during task transitions (e.g., StartThieving -> MonitorThieving)
        if (twoStallMode && setupDone && guardTracker != null && currentlyThieving) {
            double currentXp = xpTracking.getCurrentXp();
            if (atOreStall) {
                guardTracker.checkOreXpDrop(currentXp);
                guardTracker.shouldSwitchToCannonball();
            } else {
                guardTracker.checkCbXpDrop(currentXp);
                guardTracker.shouldSwitchToOre();
            }
        }
        
        // Only check inventory when actively thieving and NOT at safety tile
        // (at safety tile = might be hopping, don't interfere with inventory)
        if (!currentlyThieving || !setupDone || isAtSafetyTile()) {
            return;
        }
        
        // Check inventory when XP is gained (successful steal)
        if (xpGained) {
            checkInventoryForChanges();
        }
    }
    
    /**
     * Initialize inventory snapshot on first check
     */
    public void initializeInventorySnapshot() {
        if (inventoryInitialized) return;
        
        try {
            Set<Integer> allIds = getAllTrackedItemIds();
            ItemGroupResult inv = getWidgetManager().getInventory().search(allIds);
            
            if (inv != null) {
                lastInventorySnapshot.clear();
                for (int itemId : allIds) {
                    int count = inv.getAmount(itemId);
                    if (count > 0) {
                        lastInventorySnapshot.put(itemId, count);
                    }
                }
                inventoryInitialized = true;
                log("INVENTORY", "Initialized inventory snapshot with " + lastInventorySnapshot.size() + " item types");
            }
        } catch (Exception e) {
            log("INVENTORY", "Failed to initialize inventory: " + e.getMessage());
        }
    }
    
    /**
     * Get all tracked item IDs (cannonballs + ores)
     */
    private Set<Integer> getAllTrackedItemIds() {
        Set<Integer> allIds = new HashSet<>();
        allIds.addAll(CANNONBALL_TYPES.values());
        allIds.addAll(ORE_TYPES.values());
        return allIds;
    }
    
    /**
     * Check inventory for changes and update counts
     */
    private void checkInventoryForChanges() {
        try {
            Set<Integer> allIds = getAllTrackedItemIds();
            ItemGroupResult inv = getWidgetManager().getInventory().search(allIds);
            
            if (inv == null) return;
            
            // Check cannonballs
            for (Map.Entry<String, Integer> entry : CANNONBALL_TYPES.entrySet()) {
                String type = entry.getKey();
                int itemId = entry.getValue();
                
                int currentCount = inv.getAmount(itemId);
                int lastCount = lastInventorySnapshot.getOrDefault(itemId, 0);
                
                if (currentCount > lastCount) {
                    int gained = currentCount - lastCount;
                    cannonballCounts.merge(type, gained, Integer::sum);
                    cannonballsStolen += gained;
                    log("LOOT", "+" + gained + " " + type + " (total: " + cannonballCounts.get(type) + ")");
                }
                lastInventorySnapshot.put(itemId, currentCount);
            }
            
            // Check ores
            for (Map.Entry<String, Integer> entry : ORE_TYPES.entrySet()) {
                String type = entry.getKey();
                int itemId = entry.getValue();
                
                int currentCount = inv.getAmount(itemId);
                int lastCount = lastInventorySnapshot.getOrDefault(itemId, 0);
                
                if (currentCount > lastCount) {
                    int gained = currentCount - lastCount;
                    oreCounts.merge(type, gained, Integer::sum);
                    oresStolen += gained;
                    log("LOOT", "+" + gained + " " + type + " (total: " + oreCounts.get(type) + ")");
                }
                lastInventorySnapshot.put(itemId, currentCount);
            }
            
        } catch (Exception e) {
            // Inventory read failed - silently skip
        }
    }
    
    /**
     * Reset inventory snapshot (call after depositing)
     */
    public void resetInventorySnapshot() {
        lastInventorySnapshot.clear();
        inventoryInitialized = false;
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

        // === Ocean Theme Colors ===
        final Color oceanDeep = new Color(15, 52, 96, 240);      // Deep ocean blue background
        final Color oceanDark = new Color(10, 35, 65, 240);      // Darker blue for title bar
        final Color turquoise = new Color(64, 224, 208);         // Turquoise for accents
        final Color seafoamGreen = new Color(152, 251, 152);     // Seafoam green for success
        final Color oceanAccent = new Color(100, 149, 237);      // Cornflower blue for highlights
        final Color oceanBorder = new Color(0, 0, 0);            // Black border

        // === Panel Layout (KyyzMasterFarmer style) ===
        final int x = 10;
        final int baseY = 50;
        final int width = 240;
        final int paddingX = 12;
        final int topGap = 8;
        final int lineGap = 18;
        final int titleHeight = 40;

        final int labelColor = Color.WHITE.getRGB();
        final int valueYellow = turquoise.getRGB();
        final int valueGreen = seafoamGreen.getRGB();

        int innerX = x;
        int innerY = baseY;
        int innerWidth = width;

        // Count non-zero cannonball types for dynamic height
        int nonZeroCBTypes = 0;
        for (int count : cannonballCounts.values()) {
            if (count > 0) nonZeroCBTypes++;
        }
        
        // Count non-zero ore types for dynamic height
        int nonZeroOreTypes = 0;
        for (int count : oreCounts.values()) {
            if (count > 0) nonZeroOreTypes++;
        }

        // Calculate dynamic height
        // Base lines: Runtime, XP gained, XP/hr, ETL, TTL, Level progress, Current level, Task, Version = 9
        // + Total CB line (if any cannonballs)
        // + non-zero cannonball types
        // + divider (if ores exist)
        // + Total Ores line (if any ores)
        // + non-zero ore types
        // + mode line (two-stall only)
        int cbLines = (cannonballsStolen > 0 || nonZeroCBTypes > 0) ? 1 + nonZeroCBTypes : 0;
        int oreLines = (oresStolen > 0 || nonZeroOreTypes > 0) ? 1 + nonZeroOreTypes : 0;
        int dividerLines = (cbLines > 0 && oreLines > 0) ? 1 : 0; // divider between CB and ores
        int modeLines = twoStallMode ? 1 : 0;
        int totalLines = 9 + cbLines + dividerLines + oreLines + modeLines;
        int innerHeight = titleHeight + (totalLines * lineGap) + topGap + 18;

        // Draw border (black)
        c.fillRect(innerX - 2, innerY - 2, innerWidth + 4, innerHeight + 4, oceanBorder.getRGB(), 1);

        // Draw main background (ocean blue)
        c.fillRect(innerX, innerY, innerWidth, innerHeight, oceanDeep.getRGB(), 1);

        // Draw title bar (darker ocean)
        c.fillRect(innerX, innerY, innerWidth, titleHeight, oceanDark.getRGB(), 1);

        // Draw title text (centered)
        String title = "Tidals Cannonball Thiever";
        int titleX = innerX + (innerWidth / 2) - (c.getFontMetrics(FONT_TITLE).stringWidth(title) / 2);
        int titleY = innerY + 26;
        c.drawText(title, titleX, titleY, valueYellow, FONT_TITLE);

        // Draw separator line under title
        int sepY = innerY + titleHeight;
        c.fillRect(innerX, sepY, innerWidth, 1, oceanBorder.getRGB(), 1);

        int curY = innerY + titleHeight + topGap + lineGap;

        // === Stat Lines ===

        // Runtime
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Runtime", runtime, labelColor, labelColor);

        // === CANNONBALLS SECTION ===
        if (cannonballsStolen > 0 || nonZeroCBTypes > 0) {
            // Individual cannonball types (only show non-zero)
            for (Map.Entry<String, Integer> entry : cannonballCounts.entrySet()) {
                int count = entry.getValue();
                if (count > 0) {
                    curY += lineGap;
                    int perHour = (int) Math.round(count / hours);
                    String text = intFmt.format(count) + " (" + intFmt.format(perHour) + "/hr)";
                    drawStatLine(c, innerX, innerWidth, paddingX, curY, entry.getKey(), text, labelColor, valueGreen);
                }
            }

            // Total cannonballs
            curY += lineGap;
            String cannonballsText = intFmt.format(cannonballsStolen) + " (" + intFmt.format(cannonballsHr) + "/hr)";
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Total CB", cannonballsText, labelColor, valueYellow);
        }

        // === DIVIDER between Cannonballs and Ores ===
        if ((cannonballsStolen > 0 || nonZeroCBTypes > 0) && (oresStolen > 0 || nonZeroOreTypes > 0)) {
            curY += lineGap / 2 + 4;
            c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, oceanBorder.getRGB(), 1);
            curY += lineGap / 2 + 4;
        }

        // === ORES SECTION ===
        if (oresStolen > 0 || nonZeroOreTypes > 0) {
            // Individual ore types (only show non-zero)
            for (Map.Entry<String, Integer> entry : oreCounts.entrySet()) {
                int count = entry.getValue();
                if (count > 0) {
                    curY += lineGap;
                    int perHour = (int) Math.round(count / hours);
                    String text = intFmt.format(count) + " (" + intFmt.format(perHour) + "/hr)";
                    drawStatLine(c, innerX, innerWidth, paddingX, curY, entry.getKey(), text, labelColor, oceanAccent.getRGB());
                }
            }

            // Total ores
            curY += lineGap;
            int oresHr = (int) Math.round(oresStolen / hours);
            String oresText = intFmt.format(oresStolen) + " (" + intFmt.format(oresHr) + "/hr)";
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Total Ores", oresText, labelColor, valueYellow);
        }

        // XP gained
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "XP gained", intFmt.format(xpGainedInt), labelColor, valueGreen);

        // XP/hr
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "XP/hr", intFmt.format(xpPerHourLive), labelColor, valueYellow);

        // ETL
        curY += lineGap;
        String etlText = (currentLevel >= 99) ? "MAXED" : intFmt.format(Math.round(etl));
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "ETL", etlText, labelColor, labelColor);

        // TTL
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "TTL", ttlText, labelColor, labelColor);

        // Level progress
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Level progress", levelProgressText, labelColor, valueGreen);

        // Current level
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Current level", currentLevelText, labelColor, labelColor);

        // Task
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Task", String.valueOf(task), labelColor, labelColor);

        // Version
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Version", scriptVersion, labelColor, labelColor);

        // Mode (two-stall mode only)
        if (twoStallMode) {
            curY += lineGap;
            String modeText = atOreStall ? "Ore Stall" : "Cannonball Stall";
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Mode", "Two Stall (" + modeText + ")", labelColor, valueYellow);
        }
    }

    private void drawStatLine(Canvas c, int x, int width, int padding, int yPos,
                              String leftText, String rightText, int leftCol, int rightCol) {
        c.drawText(leftText, x + padding, yPos, leftCol, FONT_LABEL);
        int rightWidth = c.getFontMetrics(FONT_LABEL).stringWidth(rightText);
        c.drawText(rightText, x + width - padding - rightWidth, yPos, rightCol, FONT_LABEL);
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

}
