package main;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.visual.drawing.Canvas;
import tasks.*;
import utils.GuardTracker;
import utils.Task;
import utils.XPTracking;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

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
    public static Timer lastXpGain = new Timer();

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

    public TidalsCannonballThiever(Object scriptCore) {
        super(scriptCore);
        this.xpTracking = new XPTracking(this);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[]{7475}; // port roberts cannonball stall area
    }

    @Override
    public void onStart() {
        log("INFO", "Starting TidalsCannonballThiever v" + scriptVersion);

        // Initialize guard tracker
        guardTracker = new GuardTracker(this);

        // Initialize tasks in priority order
        tasks = Arrays.asList(
                new Setup(this),
                new EscapeJail(this),      // high priority - escape jail if caught
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
        xpTracking.checkXP();
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

                // Level sync (only increases)
                final int MAX_LEVEL = 99;
                int guard = 0;
                while (currentLevel < MAX_LEVEL
                        && currentXp >= tracker.getExperienceForLevel(currentLevel + 1)
                        && guard++ < 10) {
                    currentLevel++;
                }

                ttlText = tracker.timeToNextLevelString();

                int curLevelXpStart = tracker.getExperienceForLevel(currentLevel);
                int nextLevelXpTarget = tracker.getExperienceForLevel(Math.min(MAX_LEVEL, currentLevel + 1));
                int span = Math.max(1, nextLevelXpTarget - curLevelXpStart);

                etl = Math.max(0, nextLevelXpTarget - currentXp);

                levelProgressFraction = Math.max(0.0, Math.min(1.0,
                        (currentXp - curLevelXpStart) / (double) span));
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
        String levelProgressText = (Math.abs(pct - Math.rint(pct)) < 1e-9)
                ? String.format(java.util.Locale.US, "%.0f%%", pct)
                : String.format(java.util.Locale.US, "%.1f%%", pct);

        // Formatting with dots
        java.text.DecimalFormat intFmt = new java.text.DecimalFormat("#,###");
        java.text.DecimalFormatSymbols sym = new java.text.DecimalFormatSymbols();
        sym.setGroupingSeparator('.');
        intFmt.setDecimalFormatSymbols(sym);

        // === Panel + layout (standardized) ===
        final int x = 5;
        final int baseY = 40;
        final int width = 225;
        final int borderThickness = 2;
        final int paddingX = 10;
        final int topGap = 6;
        final int lineGap = 16;

        final int labelGray = new Color(180, 180, 180).getRGB();
        final int valueWhite = Color.WHITE.getRGB();
        final int valueGreen = new Color(80, 220, 120).getRGB(); // progress
        final int valueBlue = new Color(70, 130, 180).getRGB();  // highlights

        int innerX = x;
        int innerY = baseY;
        int innerWidth = width;

        int totalLines = 10;

        int y = innerY + topGap;
        y += totalLines * lineGap + 10;

        int innerHeight = Math.max(240, y - innerY);

        // Panel
        c.fillRect(innerX - borderThickness, innerY - borderThickness,
                innerWidth + (borderThickness * 2),
                innerHeight + (borderThickness * 2),
                Color.WHITE.getRGB(), 1);
        c.fillRect(innerX, innerY, innerWidth, innerHeight, Color.decode("#01031C").getRGB(), 1);
        c.drawRect(innerX, innerY, innerWidth, innerHeight, Color.WHITE.getRGB());

        int curY = innerY + topGap;

        // === Stat Lines ===

        // 1) Runtime
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Runtime", runtime, labelGray, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 2) Cannonballs stolen
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Cannonballs", intFmt.format(cannonballsStolen), labelGray, valueBlue,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 3) Cannonballs/hr
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Cannonballs/hr", intFmt.format(cannonballsHr), labelGray, valueBlue,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 4) XP gained
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "XP gained", intFmt.format(xpGainedInt), labelGray, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 5) XP/hr
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "XP/hr", intFmt.format(xpPerHourLive), labelGray, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 6) ETL
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "ETL", intFmt.format(Math.round(etl)), labelGray, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 7) TTL
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "TTL", ttlText, labelGray, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 8) Level progress
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Level progress", levelProgressText, labelGray, valueGreen,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 9) Current level
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Current level", currentLevelText, labelGray, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);

        // 10) Task
        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Task", String.valueOf(task), labelGray, valueWhite,
                FONT_VALUE_BOLD, FONT_LABEL);
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
}
