package main;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.visual.image.Image;
import data.Locations;
import data.Locations.MiningLocation;
import javafx.scene.Scene;
import tasks.Bank;
import tasks.Cut;
import tasks.DetectPlayers;
import tasks.HopWorld;
import tasks.Mine;
import tasks.Setup;
import utils.Task;
import utils.XPTracking;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ScriptDefinition(
        name = "TidalsGemMiner",
        author = "Tidal",
        threadUrl = "https://wiki.osmb.co.uk/article/tidals-gem-miner",
        skillCategory = SkillCategory.MINING,
        version = 1.3
)
public class TidalsGemMiner extends Script {
    public static final String scriptVersion = "1.3";

    // debug mode - toggled via ScriptUI Debug tab
    public static volatile boolean verboseLogging = false;

    // state fields
    public static boolean setupDone = false;
    public static String task = "Initializing...";
    public static long startTime = 0;
    public static MiningLocation selectedLocation = Locations.UPPER;
    public static boolean cuttingEnabled = false;
    public static boolean antiCrashEnabled = true;  // hop on player detection
    public static int gemsMined = 0;
    public static int gemsCut = 0;

    // XP tracking
    public static int miningXpGained = 0;
    public static int craftingXpGained = 0;
    public static XPTracking xpTracking;
    public static int startMiningLevel = 1;
    public static int currentMiningLevel = 1;
    public static int startCraftingLevel = 1;
    public static int currentCraftingLevel = 1;

    // stats reporting
    private static final String SCRIPT_NAME = "GemMiner";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static final long STATS_INTERVAL_MS = 600_000L;  // 10 minutes
    private static long lastStatsSent = 0;

    // track last sent values for incremental reporting
    private static int lastSentMiningXp = 0;
    private static int lastSentCraftingXp = 0;
    private static int lastSentGemsMined = 0;
    private static int lastSentGemsCut = 0;
    private static long lastSentRuntime = 0;
    private static long lastSentGp = 0;

    // GP tracking - gem item IDs and prices
    public static final Map<String, Integer> GEM_ITEM_IDS = Map.of(
        "Uncut opal", 1625,
        "Uncut jade", 1627,
        "Uncut red topaz", 1629,
        "Uncut sapphire", 1623,
        "Uncut emerald", 1621,
        "Uncut ruby", 1619,
        "Uncut diamond", 1617,
        "Uncut dragonstone", 1631
    );
    public static final Map<Integer, Integer> gemPrices = new ConcurrentHashMap<>();
    public static boolean pricesLoaded = false;
    public static long totalGpEarned = 0;

    // Crafting XP per gem (banked XP tracking)
    public static final Map<Integer, Double> GEM_CRAFTING_XP = Map.of(
        1625, 15.0,    // Uncut opal
        1627, 20.0,    // Uncut jade
        1629, 25.0,    // Uncut red topaz
        1623, 50.0,    // Uncut sapphire
        1621, 67.5,    // Uncut emerald
        1619, 85.0,    // Uncut ruby
        1617, 107.5,   // Uncut diamond
        1631, 137.5    // Uncut dragonstone
    );
    public static double bankedCraftingXp = 0;

    // ui and tasks
    private ScriptUI scriptUI;
    private List<Task> tasks;
    private DetectPlayers detectPlayers;

    // paint
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

    public TidalsGemMiner(Object scriptCore) {
        super(scriptCore);
        xpTracking = new XPTracking(this);
    }

    @Override
    public void onStart() {
        log("INFO", "Starting Tidals Gem Miner v" + scriptVersion);

        if (checkForUpdates()) {
            stop();
            return;
        }

        // initialize start time immediately so paint shows valid runtime
        startTime = System.currentTimeMillis();

        scriptUI = new ScriptUI(this);
        Scene scene = scriptUI.buildScene(this);
        getStageController().show(scene, "Gem Miner Settings", false);

        // read settings from UI after closed
        selectedLocation = scriptUI.getSelectedLocation();
        cuttingEnabled = scriptUI.isCuttingEnabled();

        log("INFO", "Location: " + selectedLocation.displayName());
        log("INFO", "Cutting: " + (cuttingEnabled ? "enabled" : "disabled"));

        // initialize tasks (order matters: HopWorld -> Setup -> Cut -> Bank -> Mine)
        // HopWorld is highest priority to handle crash detection immediately
        tasks = new ArrayList<>();
        tasks.add(new HopWorld(this));  // highest priority - handles world hops
        tasks.add(new Setup(this));
        tasks.add(new Cut(this));
        tasks.add(new Bank(this));
        tasks.add(new Mine(this));

        // initialize player detection (not a task - runs every poll cycle)
        detectPlayers = new DetectPlayers(this);

        log("INFO", "Tasks initialized: " + tasks.size());

        // fetch gem prices in background
        updateGemPrices();
    }

    @Override
    public int[] regionsToPrioritise() {
        if (selectedLocation != null) {
            return selectedLocation.priorityRegions();
        }
        return new int[]{11310, 11410};
    }

    @Override
    public boolean trackXP() {
        return true;
    }

    @Override
    public int poll() {
        if (tasks == null || tasks.isEmpty()) {
            return 600;
        }

        // update XP counters from custom trackers
        if (xpTracking != null) {
            miningXpGained = (int) xpTracking.getMiningXpGained();
            craftingXpGained = (int) xpTracking.getCraftingXpGained();
        }

        // run player detection when setup complete and anti-crash enabled
        if (setupDone && antiCrashEnabled && detectPlayers != null) {
            detectPlayers.runDetection();
        }

        // stats reporting
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastStatsSent >= STATS_INTERVAL_MS) {
            long elapsed = nowMs - startTime;

            // calculate increments since last send
            int miningXpIncrement = miningXpGained - lastSentMiningXp;
            int craftingXpIncrement = craftingXpGained - lastSentCraftingXp;
            int gemsMinedIncrement = gemsMined - lastSentGemsMined;
            int gemsCutIncrement = gemsCut - lastSentGemsCut;
            long gpIncrement = totalGpEarned - lastSentGp;
            long runtimeIncrement = (elapsed / 1000) - lastSentRuntime;

            sendStats(miningXpIncrement, craftingXpIncrement, gemsMinedIncrement, gemsCutIncrement, gpIncrement, runtimeIncrement);

            // update last sent values AFTER sending
            lastSentMiningXp = miningXpGained;
            lastSentCraftingXp = craftingXpGained;
            lastSentGemsMined = gemsMined;
            lastSentGemsCut = gemsCut;
            lastSentGp = totalGpEarned;
            lastSentRuntime = elapsed / 1000;
            lastStatsSent = nowMs;
        }

        for (Task t : tasks) {
            if (t.activate()) {
                t.execute();
                return 0;
            }
        }

        return 600;
    }

    @Override
    public void onPaint(Canvas c) {
        if (c == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        String runtime = formatRuntime(elapsed);
        double hours = Math.max(1e-9, elapsed / 3_600_000.0);

        // get XP/hr from custom trackers for accurate calculations
        int miningXpHr = 0;
        int craftingXpHr = 0;
        String miningTtl = "-";
        String craftingTtl = "-";

        if (xpTracking != null) {
            XPTracker miningTracker = xpTracking.getMiningTracker();
            if (miningTracker != null) {
                miningXpHr = miningTracker.getXpPerHour();
                miningTtl = miningTracker.timeToNextLevelString();

                // sync current level
                final int MAX_LEVEL = 99;
                double currentXp = miningTracker.getXp();
                int guard = 0;
                while (currentMiningLevel < MAX_LEVEL
                        && currentXp >= miningTracker.getExperienceForLevel(currentMiningLevel + 1)
                        && guard++ < 10) {
                    currentMiningLevel++;
                }
                if (currentMiningLevel >= MAX_LEVEL) {
                    miningTtl = "MAXED";
                }
            }

            XPTracker craftingTracker = xpTracking.getCraftingTracker();
            if (craftingTracker != null && cuttingEnabled) {
                craftingXpHr = craftingTracker.getXpPerHour();
                craftingTtl = craftingTracker.timeToNextLevelString();

                final int MAX_LEVEL = 99;
                double currentXp = craftingTracker.getXp();
                int guard = 0;
                while (currentCraftingLevel < MAX_LEVEL
                        && currentXp >= craftingTracker.getExperienceForLevel(currentCraftingLevel + 1)
                        && guard++ < 10) {
                    currentCraftingLevel++;
                }
                if (currentCraftingLevel >= MAX_LEVEL) {
                    craftingTtl = "MAXED";
                }
            }
        }

        // fallback to manual calculation if tracker XP/hr is 0
        if (miningXpHr == 0 && miningXpGained > 0) {
            miningXpHr = (int) (miningXpGained / hours);
        }
        if (craftingXpHr == 0 && craftingXpGained > 0) {
            craftingXpHr = (int) (craftingXpGained / hours);
        }

        int gemsHr = (int) (gemsMined / hours);

        // level display with gains
        if (startMiningLevel <= 0) startMiningLevel = currentMiningLevel;
        if (startCraftingLevel <= 0) startCraftingLevel = currentCraftingLevel;
        int miningLevelsGained = Math.max(0, currentMiningLevel - startMiningLevel);
        int craftingLevelsGained = Math.max(0, currentCraftingLevel - startCraftingLevel);
        String miningLevelText = miningLevelsGained > 0
            ? currentMiningLevel + " (+" + miningLevelsGained + ")"
            : String.valueOf(currentMiningLevel);
        String craftingLevelText = craftingLevelsGained > 0
            ? currentCraftingLevel + " (+" + craftingLevelsGained + ")"
            : String.valueOf(currentCraftingLevel);

        // colors - dark teal theme with gold accents
        final Color bgColor = new Color(22, 49, 52);
        final Color borderColor = new Color(40, 75, 80);
        final Color accentGold = new Color(255, 215, 0);
        final Color textLight = new Color(238, 237, 233);
        final Color textMuted = new Color(170, 185, 185);
        final Color valueGreen = new Color(180, 230, 150);

        // layout - matched to cannonball thieving
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

        // calculate dynamic height based on content
        // base: runtime, gems, mining xp, mining xp/hr, mining level, mining ttl, state, version = 8
        // cutting adds: gems cut, crafting xp, crafting xp/hr, crafting level, crafting ttl = 5
        // GP tracking adds: gp earned, gp/hr = 2
        // Banked crafting XP adds: 1 line
        int gpLines = (totalGpEarned > 0) ? 2 : 0;
        int bankedXpLines = (bankedCraftingXp > 0) ? 1 : 0;
        int lineCount = (cuttingEnabled ? 13 : 8) + gpLines + bankedXpLines;
        int separatorCount = cuttingEnabled ? 2 : 1;
        int separatorOverhead = separatorCount * 12;
        int footerPadding = 10;  // extra padding before state line
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

        // draw logo centered
        if (logoImage != null) {
            int logoX = innerX + (innerWidth - logoImage.width) / 2;
            c.drawAtOn(logoImage, logoX, curY);
            curY += logoImage.height + logoBottomGap;
        }

        // separator after logo
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, accentGold.getRGB(), 1);
        curY += 16;

        // stats
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Runtime", runtime, textMuted.getRGB(), textLight.getRGB());

        curY += lineGap;
        String gemsText = intFmt.format(gemsMined) + " (" + intFmt.format(gemsHr) + "/hr)";
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Gems mined", gemsText, textMuted.getRGB(), valueGreen.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Mining XP", intFmt.format(miningXpGained), textMuted.getRGB(), valueGreen.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Mining XP/hr", intFmt.format(miningXpHr), textMuted.getRGB(), accentGold.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Mining level", miningLevelText, textMuted.getRGB(), textLight.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Mining TTL", miningTtl, textMuted.getRGB(), textLight.getRGB());

        if (cuttingEnabled) {
            // separator before crafting section
            curY += lineGap - 4;
            c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
            curY += 12;

            int gemsCutHr = (int) (gemsCut / hours);
            String cutText = intFmt.format(gemsCut) + " (" + intFmt.format(gemsCutHr) + "/hr)";
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Gems cut", cutText, textMuted.getRGB(), valueGreen.getRGB());

            curY += lineGap;
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Crafting XP", intFmt.format(craftingXpGained), textMuted.getRGB(), valueGreen.getRGB());

            curY += lineGap;
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Crafting XP/hr", intFmt.format(craftingXpHr), textMuted.getRGB(), accentGold.getRGB());

            curY += lineGap;
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Crafting level", craftingLevelText, textMuted.getRGB(), textLight.getRGB());

            curY += lineGap;
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Crafting TTL", craftingTtl, textMuted.getRGB(), textLight.getRGB());
        }

        // GP earned (only show if we have earned GP)
        if (totalGpEarned > 0) {
            curY += lineGap;
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "GP earned", intFmt.format(totalGpEarned), textMuted.getRGB(), accentGold.getRGB());

            curY += lineGap;
            long gpPerHour = Math.round(totalGpEarned / hours);
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "GP/hr", intFmt.format(gpPerHour), textMuted.getRGB(), accentGold.getRGB());
        }

        // Banked Crafting XP (potential XP from cutting mined gems)
        if (bankedCraftingXp > 0) {
            curY += lineGap;
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Banked Craft XP", intFmt.format((long) bankedCraftingXp), textMuted.getRGB(), valueGreen.getRGB());
        }

        // separator before footer (with extra padding before state)
        curY += lineGap - 4;
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
        curY += 22;  // 12 + 10px extra padding

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
        if (logoImage != null) {
            return;
        }

        try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
            if (in == null) {
                log(getClass(), "logo '/logo.png' not found in resources");
                return;
            }

            BufferedImage src = ImageIO.read(in);
            if (src == null) {
                log(getClass(), "failed to decode logo");
                return;
            }

            // scale to fit width 180 preserving aspect ratio
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

        } catch (IOException e) {
            log(getClass(), "error loading logo: " + e.getMessage());
        }
    }

    private String formatRuntime(long millis) {
        if (millis <= 0) {
            return "00:00:00";
        }
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

    private void sendStats(int miningXp, int craftingXp, int gemsMined, int gemsCut, long gpEarned, long runtimeSecs) {
        try {
            if (obf.Secrets.STATS_URL == null || obf.Secrets.STATS_URL.isEmpty()) {
                return;
            }

            // skip if nothing to report
            if (miningXp == 0 && craftingXp == 0 && gemsMined == 0 && gemsCut == 0 && gpEarned == 0 && runtimeSecs == 0) {
                return;
            }

            // xp = mining + crafting combined for dashboard total
            int totalXp = miningXp + craftingXp;

            String json = String.format(
                "{\"script\":\"%s\",\"session\":\"%s\",\"gp\":%d,\"xp\":%d,\"runtime\":%d,\"gemsMined\":%d,\"gemsCut\":%d,\"miningXp\":%d,\"craftingXp\":%d}",
                SCRIPT_NAME,
                SESSION_ID,
                gpEarned,
                totalXp,
                runtimeSecs,
                gemsMined,
                gemsCut,
                miningXp,
                craftingXp
            );

            URL url = new URL(obf.Secrets.STATS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Stats-Key", obf.Secrets.STATS_API);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                log("STATS", "Stats sent: miningXp=" + miningXp + ", craftingXp=" + craftingXp +
                    ", gems=" + gemsMined + "/" + gemsCut + ", gp=" + gpEarned + ", runtime=" + runtimeSecs + "s");
            } else {
                log("STATS", "Failed to send stats, HTTP " + code);
            }
        } catch (IOException e) {
            log("STATS", "Error sending stats: " + e.getClass().getSimpleName());
        }
    }

    // version checking
    public String getLatestVersion(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("version")) {
                        String[] parts = line.split("=");
                        if (parts.length == 2) {
                            return parts[1].replace(",", "").trim();
                        }
                    }
                }
            }
        } catch (IOException e) {
            log("VERSIONCHECK", "Exception occurred while fetching version from GitHub.");
        }
        return null;
    }

    public static int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 < num2) return -1;
            if (num1 > num2) return 1;
        }
        return 0;
    }

    private boolean checkForUpdates() {
        String latest = getLatestVersion("https://raw.githubusercontent.com/Manokit/tidals-scripts/main/TidalsGemMiner/src/main/java/main/TidalsGemMiner.java");

        if (latest == null) {
            log("VERSION", "Could not fetch latest version info.");
            return false;
        }

        if (compareVersions(scriptVersion, latest) < 0) {
            for (int i = 0; i < 10; i++) {
                log("VERSION", "New version v" + latest + " found! Please update the script before running it again.");
            }
            return true;
        }

        log("VERSION", "You are running the latest version (v" + scriptVersion + ").");
        return false;
    }

    /**
     * Fetches gem prices from GE Tracker API in background thread.
     * Prices are locked in at session start.
     */
    private void updateGemPrices() {
        Thread priceThread = new Thread(() -> {
            try {
                log("PRICES", "Fetching prices for " + GEM_ITEM_IDS.size() + " gem types...");

                for (Map.Entry<String, Integer> entry : GEM_ITEM_IDS.entrySet()) {
                    int itemId = entry.getValue();
                    try {
                        URL url = new URL("https://www.ge-tracker.com/api/items/" + itemId);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                        if (conn.getResponseCode() == 200) {
                            BufferedReader reader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }
                            reader.close();

                            // parse "selling" price from JSON
                            String json = response.toString();
                            int sellingIdx = json.indexOf("\"selling\":");
                            if (sellingIdx != -1) {
                                int start = sellingIdx + 10;
                                int end = json.indexOf(",", start);
                                if (end == -1) end = json.indexOf("}", start);
                                if (end != -1) {
                                    String priceStr = json.substring(start, end).trim();
                                    int price = Integer.parseInt(priceStr);
                                    gemPrices.put(itemId, price);
                                }
                            }
                        }

                        // small delay between requests
                        Thread.sleep(100);
                    } catch (IOException | NumberFormatException | InterruptedException ignored) {}
                }

                pricesLoaded = true;
                log("PRICES", "Loaded prices for " + gemPrices.size() + " gems");

                // log prices for debugging
                for (Map.Entry<String, Integer> entry : GEM_ITEM_IDS.entrySet()) {
                    Integer price = gemPrices.get(entry.getValue());
                    if (price != null) {
                        log("PRICES", entry.getKey() + ": " + price + " gp");
                    }
                }
            } catch (RuntimeException e) {
                log("PRICES", "Failed to fetch prices: " + e.getMessage());
            }
        }, "GemPriceUpdater");
        priceThread.setDaemon(true);
        priceThread.start();
    }
}
