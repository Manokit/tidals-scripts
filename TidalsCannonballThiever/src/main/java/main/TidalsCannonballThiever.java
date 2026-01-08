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
    public static final String scriptVersion = "1.1";

    public static int screenWidth = 0;
    public static int screenHeight = 0;

    public static boolean setupDone = false;
    public static int cannonballsStolen = 0;
    public static int oresStolen = 0;
    public static Timer lastXpGain = new Timer();

    public static boolean twoStallMode = false;
    public static boolean atOreStall = false;
    public static boolean doingDepositRun = false;

    // cannonball types
    private static final Map<String, Integer> CANNONBALL_TYPES = new LinkedHashMap<>() {{
        put("Bronze CB", 31906);
        put("Iron CB", 31908);
        put("Steel CB", 2);
        put("Mithril CB", 31910);
        put("Adamant CB", 31912);
        put("Rune CB", 31914);
    }};

    // ore types
    private static final Map<String, Integer> ORE_TYPES = new LinkedHashMap<>() {{
        put("Iron ore", 440);
        put("Coal", 453);
        put("Silver ore", 442);
        put("Gold ore", 444);
        put("Mithril ore", 447);
        put("Adamantite ore", 449);
        put("Runite ore", 451);
    }};

    public static Map<String, Integer> cannonballCounts = new LinkedHashMap<>();
    public static Map<String, Integer> oreCounts = new LinkedHashMap<>();
    
    private Map<Integer, Integer> lastInventorySnapshot = new HashMap<>();
    private boolean inventoryInitialized = false;

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

    public static GuardTracker guardTracker;
    private ScriptUI scriptUI;

    public TidalsCannonballThiever(Object scriptCore) {
        super(scriptCore);
        xpTracking = new XPTracking(this);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[]{7475};
    }

    @Override
    public boolean canHopWorlds() {
        if (doingDepositRun) return true;
        if (!currentlyThieving) return true;
        if (isAtSafetyTile()) return true;
        return false;
    }

    @Override
    public boolean canBreak() {
        if (currentlyThieving) return false;
        if (doingDepositRun) return true;
        return isAtSafetyTile();
    }

    @Override
    public boolean canAFK() {
        if (currentlyThieving) return false;
        if (doingDepositRun) return true;
        return isAtSafetyTile();
    }

    private boolean isAtSafetyTile() {
        WorldPosition pos = getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return (x == 1867 && y == 3299) || (x == 1867 && y == 3294);
    }

    @Override
    public void onStart() {
        log("INFO", "Starting TidalsCannonballThiever v" + scriptVersion);

        scriptUI = new ScriptUI(this);
        Scene scene = scriptUI.buildScene(this);
        getStageController().show(scene, "Cannonball Thieving Options", false);

        twoStallMode = scriptUI.isTwoStallMode();
        log("UI", "Mode selected: " + (twoStallMode ? "Two Stall" : "Single Stall"));
        
        guardTracker = new GuardTracker(this);
        StartThieving.resetStaticState();

        for (String type : CANNONBALL_TYPES.keySet()) {
            cannonballCounts.put(type, 0);
        }
        for (String type : ORE_TYPES.keySet()) {
            oreCounts.put(type, 0);
        }

        tasks = Arrays.asList(
                new Setup(this),
                new EscapeJail(this),
                new DepositOres(this),
                new PrepareForBreak(this),
                new SwitchToOreStall(this),
                new SwitchToCannonballStall(this),
                new Retreat(this),
                new WaitAtSafety(this),
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
        if (getWorldPosition() == null) return;
        
        boolean xpGained = xpTracking.checkXPAndReturnIfGained();
        
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
        
        if (!currentlyThieving || !setupDone || isAtSafetyTile()) return;
        
        if (xpGained) {
            checkInventoryForChanges();
        }
    }
    
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
    
    private Set<Integer> getAllTrackedItemIds() {
        Set<Integer> allIds = new HashSet<>();
        allIds.addAll(CANNONBALL_TYPES.values());
        allIds.addAll(ORE_TYPES.values());
        return allIds;
    }
    
    private void checkInventoryForChanges() {
        try {
            Set<Integer> allIds = getAllTrackedItemIds();
            ItemGroupResult inv = getWidgetManager().getInventory().search(allIds);
            
            if (inv == null) return;
            
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
        }
    }
    
    public void resetInventorySnapshot() {
        lastInventorySnapshot.clear();
        inventoryInitialized = false;
    }

    @Override
    public void onPaint(Canvas c) {
        long elapsed = System.currentTimeMillis() - startTime;
        double hours = Math.max(1e-9, elapsed / 3_600_000.0);
        String runtime = formatRuntime(elapsed);

        // xp tracking
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

                // level sync
                final int MAX_LEVEL = 99;
                int guard = 0;
                while (currentLevel < MAX_LEVEL
                        && currentXp >= tracker.getExperienceForLevel(currentLevel + 1)
                        && guard++ < 10) {
                    currentLevel++;
                }

                // handle level 99
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

        // totals & rates
        int cannonballsHr = (int) Math.round(cannonballsStolen / hours);

        // current level text
        if (startLevel <= 0) startLevel = currentLevel;
        int levelsGained = Math.max(0, currentLevel - startLevel);
        String currentLevelText = (levelsGained > 0)
                ? (currentLevel + " (+" + levelsGained + ")")
                : String.valueOf(currentLevel);

        // percent text
        double pct = Math.max(0, Math.min(100, levelProgressFraction * 100.0));
        String levelProgressText;
        if (currentLevel >= 99) {
            levelProgressText = "MAXED";
        } else {
            levelProgressText = (Math.abs(pct - Math.rint(pct)) < 1e-9)
                    ? String.format(java.util.Locale.US, "%.0f%%", pct)
                    : String.format(java.util.Locale.US, "%.1f%%", pct);
        }

        // formatting
        java.text.DecimalFormat intFmt = new java.text.DecimalFormat("#,###");
        java.text.DecimalFormatSymbols sym = new java.text.DecimalFormatSymbols();
        sym.setGroupingSeparator('.');
        intFmt.setDecimalFormatSymbols(sym);

        // colors
        final Color oceanDeep = new Color(15, 52, 96, 240);      // Deep ocean blue background
        final Color oceanDark = new Color(10, 35, 65, 240);      // Darker blue for title bar
        final Color turquoise = new Color(64, 224, 208);         // Turquoise for accents
        final Color seafoamGreen = new Color(152, 251, 152);     // Seafoam green for success
        final Color oceanAccent = new Color(100, 149, 237);
        final Color oceanBorder = new Color(0, 0, 0);

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

        int nonZeroCBTypes = 0;
        for (int count : cannonballCounts.values()) {
            if (count > 0) nonZeroCBTypes++;
        }
        
        int nonZeroOreTypes = 0;
        for (int count : oreCounts.values()) {
            if (count > 0) nonZeroOreTypes++;
        }

        int cbLines = (cannonballsStolen > 0 || nonZeroCBTypes > 0) ? 1 + nonZeroCBTypes : 0;
        int oreLines = (oresStolen > 0 || nonZeroOreTypes > 0) ? 1 + nonZeroOreTypes : 0;
        int dividerLines = (cbLines > 0 && oreLines > 0) ? 1 : 0;
        int modeLines = twoStallMode ? 1 : 0;
        int totalLines = 9 + cbLines + dividerLines + oreLines + modeLines;
        int innerHeight = titleHeight + (totalLines * lineGap) + topGap + 18;

        c.fillRect(innerX - 2, innerY - 2, innerWidth + 4, innerHeight + 4, oceanBorder.getRGB(), 1);
        c.fillRect(innerX, innerY, innerWidth, innerHeight, oceanDeep.getRGB(), 1);
        c.fillRect(innerX, innerY, innerWidth, titleHeight, oceanDark.getRGB(), 1);
        String title = "Tidals Cannonball Thiever";
        int titleX = innerX + (innerWidth / 2) - (c.getFontMetrics(FONT_TITLE).stringWidth(title) / 2);
        int titleY = innerY + 26;
        c.drawText(title, titleX, titleY, valueYellow, FONT_TITLE);

        int sepY = innerY + titleHeight;
        c.fillRect(innerX, sepY, innerWidth, 1, oceanBorder.getRGB(), 1);

        int curY = innerY + titleHeight + topGap + lineGap;

        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Runtime", runtime, labelColor, labelColor);

        if (cannonballsStolen > 0 || nonZeroCBTypes > 0) {
            for (Map.Entry<String, Integer> entry : cannonballCounts.entrySet()) {
                int count = entry.getValue();
                if (count > 0) {
                    curY += lineGap;
                    int perHour = (int) Math.round(count / hours);
                    String text = intFmt.format(count) + " (" + intFmt.format(perHour) + "/hr)";
                    drawStatLine(c, innerX, innerWidth, paddingX, curY, entry.getKey(), text, labelColor, valueGreen);
                }
            }

            curY += lineGap;
            String cannonballsText = intFmt.format(cannonballsStolen) + " (" + intFmt.format(cannonballsHr) + "/hr)";
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Total CB", cannonballsText, labelColor, valueYellow);
        }

        if ((cannonballsStolen > 0 || nonZeroCBTypes > 0) && (oresStolen > 0 || nonZeroOreTypes > 0)) {
            curY += lineGap / 2 + 4;
            c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, oceanBorder.getRGB(), 1);
            curY += lineGap / 2 + 4;
        }

        if (oresStolen > 0 || nonZeroOreTypes > 0) {
            for (Map.Entry<String, Integer> entry : oreCounts.entrySet()) {
                int count = entry.getValue();
                if (count > 0) {
                    curY += lineGap;
                    int perHour = (int) Math.round(count / hours);
                    String text = intFmt.format(count) + " (" + intFmt.format(perHour) + "/hr)";
                    drawStatLine(c, innerX, innerWidth, paddingX, curY, entry.getKey(), text, labelColor, oceanAccent.getRGB());
                }
            }

            curY += lineGap;
            int oresHr = (int) Math.round(oresStolen / hours);
            String oresText = intFmt.format(oresStolen) + " (" + intFmt.format(oresHr) + "/hr)";
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Total Ores", oresText, labelColor, valueYellow);
        }

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "XP gained", intFmt.format(xpGainedInt), labelColor, valueGreen);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "XP/hr", intFmt.format(xpPerHourLive), labelColor, valueYellow);

        curY += lineGap;
        String etlText = (currentLevel >= 99) ? "MAXED" : intFmt.format(Math.round(etl));
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "ETL", etlText, labelColor, labelColor);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "TTL", ttlText, labelColor, labelColor);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Level progress", levelProgressText, labelColor, valueGreen);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Current level", currentLevelText, labelColor, labelColor);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Task", String.valueOf(task), labelColor, labelColor);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Version", scriptVersion, labelColor, labelColor);

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
