package main;

import com.osmb.api.item.ItemGroupResult;
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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ScriptDefinition(name = "TidalsCannonballThiever", description = "Thieves cannonballs from Port Roberts stalls while avoiding guards", skillCategory = SkillCategory.THIEVING, version = 1.0, author = "Tidalus")
public class TidalsCannonballThiever extends Script {
    public static final String scriptVersion = "1.8";
    private static final String SCRIPT_NAME = "CannonballThiever";
    private static final String SESSION_ID = UUID.randomUUID().toString();
    private static long lastStatsSent = 0;
    private static final long STATS_INTERVAL_MS = 600_000L; // 10 minutes

    // track last sent values for incremental reporting
    private static int lastSentXp = 0;
    private static int lastSentCannonballs = 0;
    private static int lastSentOres = 0;
    private static int lastSentGp = 0;
    private static long lastSentRuntime = 0;

    // gp tracking - prices locked in at theft time
    public static long totalGpEarned = 0;
    private static final Map<Integer, Integer> itemPrices = new ConcurrentHashMap<>();
    private static boolean pricesLoaded = false;

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
    private static final Map<String, Integer> CANNONBALL_TYPES = new LinkedHashMap<>() {
        {
            put("Bronze CB", 31906);
            put("Iron CB", 31908);
            put("Steel CB", 2);
            put("Mithril CB", 31910);
            put("Adamant CB", 31912);
            put("Rune CB", 31914);
        }
    };

    // ore types
    private static final Map<String, Integer> ORE_TYPES = new LinkedHashMap<>() {
        {
            put("Iron ore", 440);
            put("Coal", 453);
            put("Silver ore", 442);
            put("Gold ore", 444);
            put("Mithril ore", 447);
            put("Adamantite ore", 449);
            put("Runite ore", 451);
        }
    };

    public static Map<String, Integer> cannonballCounts = new LinkedHashMap<>();
    public static Map<String, Integer> oreCounts = new LinkedHashMap<>();

    private Map<Integer, Integer> lastInventorySnapshot = new HashMap<>();
    private boolean inventoryInitialized = false;

    public static boolean currentlyThieving = false;

    public static String task = "Initializing...";
    public static long startTime = System.currentTimeMillis();

    private List<Task> tasks;
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 12);
    private static final Font FONT_VALUE = new Font("Arial", Font.BOLD, 12);

    private Image logoImage = null;

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
        return new int[] { 7475 };
    }

    @Override
    public boolean canHopWorlds() {
        if (doingDepositRun)
            return true;
        if (!currentlyThieving)
            return true;
        if (isAtSafetyTile())
            return true;
        if (isInJailCell(getWorldPosition()))
            return true;
        return false;
    }

    @Override
    public boolean canBreak() {
        if (currentlyThieving)
            return false;
        if (doingDepositRun)
            return true;
        if (isInJailCell(getWorldPosition()))
            return true;
        return isAtSafetyTile();
    }

    @Override
    public boolean canAFK() {
        if (currentlyThieving)
            return false;
        if (doingDepositRun)
            return true;
        if (isInJailCell(getWorldPosition()))
            return true;
        return isAtSafetyTile();
    }

    private boolean isAtSafetyTile() {
        WorldPosition pos = getWorldPosition();
        if (pos == null)
            return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return (x == 1867 && y == 3299) || (x == 1867 && y == 3294);
    }

    // jail cell area check (matches EscapeJail.JAIL_CELL: 1883, 3272, 2x2)
    private boolean isInJailCell(WorldPosition pos) {
        if (pos == null)
            return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x >= 1883 && x <= 1884 && y >= 3272 && y <= 3273 && pos.getPlane() == 0;
    }

    @Override
    public void onStart() {
        log("INFO", "Starting TidalsCannonballThiever v" + scriptVersion);

        // fetch item prices in background (locked in for session)
        updateItemPrices();

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
                new DismissDialogue(this),
                new SwitchToOreStall(this),
                new SwitchToCannonballStall(this),
                new Retreat(this),
                new WaitAtSafety(this),
                new ReturnToThieving(this),
                new StartThieving(this),
                new MonitorThieving(this));

        log("INFO", "Tasks initialized: " + tasks.size());
    }

    @Override
    public int poll() {
        // send stats periodically
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastStatsSent >= STATS_INTERVAL_MS) {
            long elapsed = nowMs - startTime;
            int xpGained = xpTracking != null && xpTracking.getThievingTracker() != null
                    ? (int) xpTracking.getThievingTracker().getXpGained()
                    : 0;

            // calculate increments since last send
            int xpIncrement = xpGained - lastSentXp;
            int cannonballIncrement = cannonballsStolen - lastSentCannonballs;
            int oreIncrement = oresStolen - lastSentOres;
            int gpIncrement = (int) (totalGpEarned - lastSentGp);
            long runtimeIncrement = (elapsed / 1000) - lastSentRuntime;

            sendStats(xpIncrement, cannonballIncrement, oreIncrement, gpIncrement, runtimeIncrement);

            // update last sent values
            lastSentXp = xpGained;
            lastSentCannonballs = cannonballsStolen;
            lastSentOres = oresStolen;
            lastSentGp = (int) totalGpEarned;
            lastSentRuntime = elapsed / 1000;
            lastStatsSent = nowMs;
        }

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
        WorldPosition pos = getWorldPosition();
        if (pos == null)
            return;

        // jail detection: if suddenly in jail while thieving, reset state
        if (setupDone && currentlyThieving) {
            if (isInJailCell(pos)) {
                log("JAIL", "Detected teleport to jail! Resetting thieving state...");
                currentlyThieving = false;
                // EscapeJail task will now activate in next poll
                return;
            }
        }

        if (!currentlyThieving || !setupDone || isAtSafetyTile())
            return;

        // check inventory for changes every frame when thieving
        // this triggers addThievingXp() when items are gained
        checkInventoryForChanges();

        // cycle tracking in two-stall mode - xp tracking only
        // note: shouldSwitchToX() calls removed - they do pixel analysis
        // which can trigger recursive frame updates. guard checking happens
        // properly in MonitorThieving.java inside pollFramesUntil() lambdas
        if (twoStallMode && guardTracker != null) {
            double currentXp = xpTracking.getCurrentXp();
            if (atOreStall) {
                guardTracker.checkOreXpDrop(currentXp);
            } else {
                guardTracker.checkCbXpDrop(currentXp);
            }
        }
    }

    public void initializeInventorySnapshot() {
        if (inventoryInitialized)
            return;

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

    // thieving xp per steal at port roberts stalls
    private static final double CANNONBALL_STALL_XP = 223.0;
    private static final double ORE_STALL_XP = 191.0;

    private void checkInventoryForChanges() {
        try {
            Set<Integer> allIds = getAllTrackedItemIds();
            ItemGroupResult inv = getWidgetManager().getInventory().search(allIds);

            if (inv == null)
                return;

            boolean cannonballGained = false;
            boolean oreGained = false;

            for (Map.Entry<String, Integer> entry : CANNONBALL_TYPES.entrySet()) {
                String type = entry.getKey();
                int itemId = entry.getValue();

                int currentCount = inv.getAmount(itemId);
                int lastCount = lastInventorySnapshot.getOrDefault(itemId, 0);

                if (currentCount > lastCount) {
                    int gained = currentCount - lastCount;
                    cannonballCounts.merge(type, gained, Integer::sum);
                    cannonballsStolen += gained;

                    // calculate gp earned using locked-in price
                    int price = getItemPrice(itemId);
                    long gpGained = (long) gained * price;
                    totalGpEarned += gpGained;

                    log("LOOT", "+" + gained + " " + type + " (total: " + cannonballCounts.get(type) + ", +" + gpGained + " gp)");
                    cannonballGained = true;
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

                    // calculate gp earned using locked-in price
                    int price = getItemPrice(itemId);
                    long gpGained = (long) gained * price;
                    totalGpEarned += gpGained;

                    log("LOOT", "+" + gained + " " + type + " (total: " + oreCounts.get(type) + ", +" + gpGained + " gp)");
                    oreGained = true;
                }
                lastInventorySnapshot.put(itemId, currentCount);
            }

            // track XP based on which stall was stolen from
            if (xpTracking != null) {
                if (cannonballGained) {
                    xpTracking.addThievingXp(CANNONBALL_STALL_XP);
                }
                if (oreGained) {
                    xpTracking.addThievingXp(ORE_STALL_XP);
                }
            }

        } catch (Exception e) {
        }
    }

    public void resetInventorySnapshot() {
        lastInventorySnapshot.clear();
        inventoryInitialized = false;
    }

    // public method for manual inventory check (called for first xp drop assumption)
    public void checkInventoryForChangesManual() {
        checkInventoryForChanges();
    }

    /**
     * Fetches current prices from GE Tracker API for all tracked items.
     * Prices are locked in at script start and don't change during the session.
     */
    private void updateItemPrices() {
        Thread t = new Thread(() -> {
            try {
                Set<Integer> allItemIds = getAllTrackedItemIds();
                log("PRICES", "Fetching prices for " + allItemIds.size() + " items...");

                for (int itemId : allItemIds) {
                    try {
                        URL url = new URL("https://www.ge-tracker.com/api/items/" + itemId);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);

                        int responseCode = conn.getResponseCode();
                        if (responseCode != 200) {
                            continue;
                        }

                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line);
                        }
                        in.close();

                        String json = response.toString();

                        // parse "selling" price from json
                        int sellingIdx = json.indexOf("\"selling\":");
                        if (sellingIdx != -1) {
                            int start = sellingIdx + 10;
                            int end = json.indexOf(",", start);
                            if (end == -1) end = json.indexOf("}", start);
                            if (end != -1) {
                                String priceStr = json.substring(start, end).trim();
                                try {
                                    int price = Integer.parseInt(priceStr);
                                    itemPrices.put(itemId, price);
                                } catch (NumberFormatException ignored) {}
                            }
                        }

                        // small delay between requests to avoid rate limiting
                        Thread.sleep(100);

                    } catch (Exception e) {
                        // silently skip failed items
                    }
                }

                pricesLoaded = true;
                log("PRICES", "Loaded prices for " + itemPrices.size() + " items from GE Tracker");

                // log loaded prices for debugging
                for (Map.Entry<String, Integer> entry : CANNONBALL_TYPES.entrySet()) {
                    int itemId = entry.getValue();
                    Integer price = itemPrices.get(itemId);
                    if (price != null) {
                        log("PRICES", entry.getKey() + " (" + itemId + "): " + price + " gp");
                    }
                }
                for (Map.Entry<String, Integer> entry : ORE_TYPES.entrySet()) {
                    int itemId = entry.getValue();
                    Integer price = itemPrices.get(itemId);
                    if (price != null) {
                        log("PRICES", entry.getKey() + " (" + itemId + "): " + price + " gp");
                    }
                }

            } catch (Exception e) {
                log("PRICES", "Failed to update prices: " + e.getMessage());
            }
        }, "PriceUpdater");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Gets the price of an item, returning 0 if not loaded.
     */
    private int getItemPrice(int itemId) {
        return itemPrices.getOrDefault(itemId, 0);
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

                // level sync (only increases)
                final int MAX_LEVEL = 99;
                int guard = 0;
                while (currentLevel < MAX_LEVEL
                        && currentXp >= tracker.getExperienceForLevel(currentLevel + 1)
                        && guard++ < 10) {
                    currentLevel++;
                }

                ttlText = tracker.timeToNextLevelString();

                // calculate ETL and progress manually for accuracy
                int curLevelXpStart = tracker.getExperienceForLevel(currentLevel);
                int nextLevelXpTarget = tracker.getExperienceForLevel(Math.min(MAX_LEVEL, currentLevel + 1));
                int span = Math.max(1, nextLevelXpTarget - curLevelXpStart);

                etl = Math.max(0, nextLevelXpTarget - currentXp);

                levelProgressFraction = Math.max(0.0, Math.min(1.0,
                        (currentXp - curLevelXpStart) / (double) span));

                if (currentLevel >= MAX_LEVEL) {
                    ttlText = "MAXED";
                    etl = 0;
                    levelProgressFraction = 1.0;
                }
            }
        }

        int xpPerHourLive = (int) Math.round(xpGainedLive / hours);
        int xpGainedInt = (int) Math.round(xpGainedLive);

        int cannonballsHr = (int) Math.round(cannonballsStolen / hours);

        if (startLevel <= 0)
            startLevel = currentLevel;
        int levelsGained = Math.max(0, currentLevel - startLevel);
        String currentLevelText = (levelsGained > 0)
                ? (currentLevel + " (+" + levelsGained + ")")
                : String.valueOf(currentLevel);

        double pct = Math.max(0, Math.min(100, levelProgressFraction * 100.0));
        String levelProgressText;
        if (currentLevel >= 99) {
            levelProgressText = "MAXED";
        } else {
            levelProgressText = (Math.abs(pct - Math.rint(pct)) < 1e-9)
                    ? String.format(java.util.Locale.US, "%.0f%%", pct)
                    : String.format(java.util.Locale.US, "%.1f%%", pct);
        }

        java.text.DecimalFormat intFmt = new java.text.DecimalFormat("#,###");
        java.text.DecimalFormatSymbols sym = new java.text.DecimalFormatSymbols();
        sym.setGroupingSeparator('.');
        intFmt.setDecimalFormatSymbols(sym);

        // colors - dark teal theme with gold accents
        final Color bgColor = new Color(22, 49, 52); // #163134 - dark teal background
        final Color borderColor = new Color(40, 75, 80); // lighter teal border
        final Color accentGold = new Color(255, 215, 0); // gold accent
        final Color accentYellow = new Color(255, 235, 130); // lighter gold/yellow
        final Color textLight = new Color(238, 237, 233); // #eeede9 - off-white text
        final Color textMuted = new Color(170, 185, 185); // muted teal-gray for labels
        final Color valueGreen = new Color(180, 230, 150); // soft green for positive values

        // layout
        final int x = 5;
        final int baseY = 40;
        final int width = 220;
        final int borderThickness = 2;
        final int paddingX = 10; // side padding
        final int topGap = 6; // top padding
        final int lineGap = 16; // line padding
        final int logoBottomGap = 8; // logo bottom padding

        int innerX = x;
        int innerY = baseY;
        int innerWidth = width;

        ensureLogoLoaded();
        int logoHeight = (logoImage != null) ? logoImage.height + logoBottomGap : 0;

        int nonZeroCBTypes = 0;
        for (int count : cannonballCounts.values()) {
            if (count > 0)
                nonZeroCBTypes++;
        }

        int nonZeroOreTypes = 0;
        for (int count : oreCounts.values()) {
            if (count > 0)
                nonZeroOreTypes++;
        }

        int cbLines = (cannonballsStolen > 0 || nonZeroCBTypes > 0) ? 1 + nonZeroCBTypes : 0;
        int oreLines = (oresStolen > 0 || nonZeroOreTypes > 0) ? 1 + nonZeroOreTypes : 0;
        int dividerLines = (cbLines > 0 && oreLines > 0) ? 1 : 0;
        int modeLines = twoStallMode ? 1 : 0;
        int gpLines = (totalGpEarned > 0) ? 2 : 0; // gp earned + gp/hr
        int totalLines = 9 + cbLines + dividerLines + oreLines + gpLines + modeLines;
        int separatorCount = 1 + dividerLines;
        int separatorOverhead = separatorCount * 12; // separator padding (per separator)
        int bottomPadding = 20; // bottom padding
        int contentHeight = topGap + logoHeight + (totalLines * lineGap) + separatorOverhead + bottomPadding;
        int innerHeight = Math.max(200, contentHeight);

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
        curY += 16; // post-logo separator padding

        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Runtime", runtime, textMuted.getRGB(), textLight.getRGB());

        if (cannonballsStolen > 0 || nonZeroCBTypes > 0) {
            for (Map.Entry<String, Integer> entry : cannonballCounts.entrySet()) {
                int count = entry.getValue();
                if (count > 0) {
                    curY += lineGap;
                    int perHour = (int) Math.round(count / hours);
                    String text = intFmt.format(count) + " (" + intFmt.format(perHour) + "/hr)";
                    drawStatLine(c, innerX, innerWidth, paddingX, curY, entry.getKey(), text, textMuted.getRGB(),
                            valueGreen.getRGB());
                }
            }

            curY += lineGap;
            String cannonballsText = intFmt.format(cannonballsStolen) + " (" + intFmt.format(cannonballsHr) + "/hr)";
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Total CB", cannonballsText, textMuted.getRGB(),
                    accentGold.getRGB());
        }

        if ((cannonballsStolen > 0 || nonZeroCBTypes > 0) && (oresStolen > 0 || nonZeroOreTypes > 0)) {
            curY += lineGap - 4; // pre-separator padding
            c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
            curY += 16; // post-separator padding
        }

        if (oresStolen > 0 || nonZeroOreTypes > 0) {
            for (Map.Entry<String, Integer> entry : oreCounts.entrySet()) {
                int count = entry.getValue();
                if (count > 0) {
                    curY += lineGap;
                    int perHour = (int) Math.round(count / hours);
                    String text = intFmt.format(count) + " (" + intFmt.format(perHour) + "/hr)";
                    drawStatLine(c, innerX, innerWidth, paddingX, curY, entry.getKey(), text, textMuted.getRGB(),
                            accentYellow.getRGB());
                }
            }

            curY += lineGap;
            int oresHr = (int) Math.round(oresStolen / hours);
            String oresText = intFmt.format(oresStolen) + " (" + intFmt.format(oresHr) + "/hr)";
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Total Ores", oresText, textMuted.getRGB(),
                    accentGold.getRGB());
        }

        // gp earned (only show if prices were loaded and we have gp)
        if (totalGpEarned > 0) {
            curY += lineGap;
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "GP earned", intFmt.format(totalGpEarned), textMuted.getRGB(),
                    accentGold.getRGB());

            curY += lineGap;
            long gpPerHour = Math.round(totalGpEarned / hours);
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "GP/hr", intFmt.format(gpPerHour), textMuted.getRGB(),
                    accentYellow.getRGB());
        }

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "XP gained", intFmt.format(xpGainedInt), textMuted.getRGB(),
                valueGreen.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "XP/hr", intFmt.format(xpPerHourLive), textMuted.getRGB(),
                accentYellow.getRGB());

        curY += lineGap;
        String etlText = (currentLevel >= 99) ? "MAXED" : intFmt.format(Math.round(etl));
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "ETL", etlText, textMuted.getRGB(), textLight.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "TTL", ttlText, textMuted.getRGB(), textLight.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Level progress", levelProgressText, textMuted.getRGB(),
                valueGreen.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Current level", currentLevelText, textMuted.getRGB(),
                textLight.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Task", String.valueOf(task), textMuted.getRGB(),
                textLight.getRGB());

        // separator before version
        curY += lineGap - 4; // pre-separator padding
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
        curY += 16; // post-separator padding

        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Version", scriptVersion, textMuted.getRGB(),
                textMuted.getRGB());

        if (twoStallMode) {
            curY += lineGap;
            String modeText = atOreStall ? "Ore Stall" : "Cannonball Stall";
            drawStatLine(c, innerX, innerWidth, paddingX, curY, "Mode", "Two Stall (" + modeText + ")",
                    textMuted.getRGB(), accentGold.getRGB());
        }
    }

    private void drawStatLine(Canvas c, int innerX, int innerWidth, int paddingX, int y,
            String label, String value, int labelColor, int valueColor) {
        c.drawText(label, innerX + paddingX, y, labelColor, FONT_LABEL);
        int valW = c.getFontMetrics(FONT_VALUE).stringWidth(value);
        int valX = innerX + innerWidth - paddingX - valW;
        c.drawText(value, valX, y, valueColor, FONT_VALUE);
    }

    private void ensureLogoLoaded() {
        if (logoImage != null)
            return;

        try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
            if (in == null) {
                log(getClass(), "logo '/logo.png' not found in resources");
                return;
            }

            BufferedImage src = ImageIO.read(in);
            if (src == null) {
                log(getClass(), "failed to decode logo.png");
                return;
            }

            BufferedImage argb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = argb.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(src, 0, 0, null);
            g.dispose();

            int w = argb.getWidth();
            int h = argb.getHeight();
            int[] px = new int[w * h];
            argb.getRGB(0, 0, w, h, px, 0, w);

            // premultiply alpha
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

    private void sendStats(int xpIncrement, int cannonballIncrement, int oreIncrement, int gpIncrement, long runtimeSecs) {
        try {
            if (obf.Secrets.STATS_URL == null || obf.Secrets.STATS_URL.isEmpty()) {
                return;
            }

            // skip if nothing to report
            if (xpIncrement == 0 && cannonballIncrement == 0 && oreIncrement == 0 && gpIncrement == 0 && runtimeSecs == 0) {
                return;
            }

            String json = String.format(
                    "{\"script\":\"%s\",\"session\":\"%s\",\"gp\":%d,\"xp\":%d,\"runtime\":%d,\"cannonballsStolen\":%d,\"oresStolen\":%d}",
                    SCRIPT_NAME,
                    SESSION_ID,
                    gpIncrement,
                    xpIncrement,
                    runtimeSecs,
                    cannonballIncrement,
                    oreIncrement);

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
                log("STATS", "Stats reported: xp=" + xpIncrement + ", gp=" + gpIncrement + ", cannonballs=" + cannonballIncrement + ", ores="
                        + oreIncrement + ", runtime=" + runtimeSecs + "s");
            }
        } catch (Exception e) {
            log("STATS", "Error sending stats: " + e.getClass().getSimpleName());
        }
    }
}
