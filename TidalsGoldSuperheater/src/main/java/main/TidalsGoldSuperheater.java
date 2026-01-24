package main;

import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.visual.image.Image;
import com.osmb.api.item.ItemID;
import tasks.Bank;
import tasks.Process;
import tasks.Setup;
import utils.Task;
import utils.XPTracking;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@ScriptDefinition(
        name = "TidalsGoldSuperheater",
        description = "Superheats gold ore into gold bars using magic",
        skillCategory = SkillCategory.MAGIC,
        version = 1.5,
        author = "Tidaleus"
)
public class TidalsGoldSuperheater extends Script {
    public static final String scriptVersion = "1.5";
    private final String scriptName = "GoldSuperheater";
    private static String sessionId = UUID.randomUUID().toString();
    private static long lastStatsSent = 0;
    private static final long STATS_INTERVAL_MS = 500_000L; //500000ms = 500 seconds = 8.33 minutes

    // track last sent values for incremental reporting
    private static int lastSentMagicXp = 0;
    private static int lastSentSmithingXp = 0;
    private static int lastSentBars = 0;
    private static long lastSentRuntime = 0;
    
    public static boolean setupDone = false;
    public static boolean hasReqs = false;
    
    public static int barsCreated = 0;
    public static String task = "Initialize";
    public static long startTime = System.currentTimeMillis();
    
    // smithing xp tracked manually since widget only shows magic
    public static boolean hasGoldsmithGauntlets = false;
    public static double manualSmithingXp = 0.0;
    public static final double SMITHING_XP_NO_GAUNTLETS = 22.5;
    public static final double SMITHING_XP_WITH_GAUNTLETS = 56.2;

    private List<Task> tasks;

    private static boolean webhookEnabled = false;
    private static boolean webhookShowUser = false;
    private static String webhookUrl = "";
    private static int webhookIntervalMinutes = 5;
    private static long lastWebhookSent = 0;
    private static String user = "";
    private final AtomicBoolean webhookInFlight = new AtomicBoolean(false);
    final String authorIconUrl = "https://www.osmb.co.uk/lovable-uploads/ad86059b-ce19-4540-8e53-9fd01c61c98b.png";
    private volatile long nextWebhookEarliestMs = 0L;
    private final AtomicReference<Image> lastCanvasFrame = new AtomicReference<>();

    public static int currentMagicLevel = 1;
    public static int startMagicLevel = 0;
    public static int currentSmithingLevel = 1;
    public static int startSmithingLevel = 0;

    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 12);
    private static final Font FONT_VALUE = new Font("Arial", Font.BOLD, 12);

    private Image logoImage = null;

    private final XPTracking xpTracking;
    private int magicXpGained = 0;
    private int smithingXpGained = 0;

    public XPTracking getXpTracking() {
        return xpTracking;
    }

    public static final String[] BANK_NAMES = {"Bank", "Chest", "Bank booth", "Bank chest", "Grand Exchange booth", "Bank counter", "Bank table"};
    public static final String[] BANK_ACTIONS = {"bank", "open", "use"};
    public static final Predicate<RSObject> bankQuery = obj ->
            obj.getName() != null && obj.getActions() != null &&
                    Arrays.stream(BANK_NAMES).anyMatch(name -> name.equalsIgnoreCase(obj.getName())) &&
                    Arrays.stream(obj.getActions()).anyMatch(action -> Arrays.stream(BANK_ACTIONS).anyMatch(bankAction -> bankAction.equalsIgnoreCase(action))) &&
                    obj.canReach();

    public TidalsGoldSuperheater(Object scriptCore) {
        super(scriptCore);
        this.xpTracking = new XPTracking(this);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[]{
                13104, // Shantay Pass
                13105, // Al Kharid
                13363, // Duel Arena / PvP Arena
                12850, // Lumbridge Castle
                12338, // Draynor
                12853, // Varrock East
                12597, // Varrock West + Cooks Guild
                12598, // Grand Exchange
                12342, // Edgeville
                12084, // Falador East + Mining Guild
                11828, // Falador West
                11571, // Crafting Guild
                11319, // Warriors Guild
                11061, // Catherby
                10806, // Seers
                11310, // Shilo
                10284, // Corsair Cove
                9772,  // Myths Guild
                10288, // Yanille
                10545, // Port Khazard
                10547, // Ardougne East/South
                10292, // Ardougne East/North
                10293, // Fishing Guild
                10039, // Barbarian Assault
                9782,  // Grand Tree
                9781,  // Tree Gnome Stronghold
                9776,  // Castle Wars
                9265,  // Lletya
                8748,  // Soul Wars
                8253,  // Lunar Isle
                9275,  // Neitiznot
                9531,  // Jatiszo
                6461,  // Wintertodt
                7227,  // Port Piscarilius
                6458,  // Arceeus
                6457,  // Kourend Castle
                6968,  // Hosidius
                7223,  // Vinery
                6710,  // Sand Crabs Chest
                6198,  // Woodcutting Guild
                5941,  // Land's End
                5944,  // Shayzien
                5946,  // Lovakengj South
                5691,  // Lovekengj North
                4922,  // Farming Guild
                4919,  // Chambers of Xeric
                5938,  // Quetzacalli
                6448,  // Varlamore West
                6960,  // Varlamore East
                6191,  // Hunter Guild
                5421,  // Aldarin
                5420,  // Mistrock
                14638, // Mos'le Harmless
                14642, // TOB
                14646, // Port Phasmatys
                12344, // Ferox Enclave
                12895, // Priff North
                13150, // Priff South
                13907, // Museum Camp
                14908, // Fossil Bank Chest island
        };
    }

    @Override
    public void onStart() {
        log("INFO", "Starting TidalsGoldSuperheater v" + scriptVersion);

        if (checkForUpdates()) {
            stop();
            return;
        }

        tasks = Arrays.asList(
                new Setup(this),
                new Process(this),
                new Bank(this)
        );
    }

    @Override
    public boolean promptBankTabDialogue() {
        return true;
    }

    @Override
    public void onNewFrame() {
        if (xpTracking != null) {
            xpTracking.checkXP();
        }
    }

    @Override
    public int poll() {
        if (webhookEnabled && System.currentTimeMillis() - lastWebhookSent >= webhookIntervalMinutes * 60_000L) {
            queueSendWebhook();
        }

        long nowMs = System.currentTimeMillis();
        if (nowMs - lastStatsSent >= STATS_INTERVAL_MS) {
            long elapsed = nowMs - startTime;

            // calculate increments since last send
            int magicXpIncrement = magicXpGained - lastSentMagicXp;
            int smithingXpIncrement = smithingXpGained - lastSentSmithingXp;
            int barsIncrement = barsCreated - lastSentBars;
            long runtimeIncrement = (elapsed / 1000) - lastSentRuntime;

            sendStats(magicXpIncrement, smithingXpIncrement, barsIncrement, runtimeIncrement);

            // update last sent values
            lastSentMagicXp = magicXpGained;
            lastSentSmithingXp = smithingXpGained;
            lastSentBars = barsCreated;
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
    public void onPaint(Canvas c) {
        long elapsed = System.currentTimeMillis() - startTime;
        double hours = Math.max(1e-9, elapsed / 3_600_000.0);
        String runtime = formatRuntime(elapsed);

        // magic xp from tracker
        String magicTtlText = "-";
        double magicEtl = 0.0;
        double magicXpGainedLive = 0.0;
        double magicCurrentXp = 0.0;

        if (xpTracking != null) {
            XPTracker magicTracker = xpTracking.getMagicTracker();
            if (magicTracker != null) {
                magicXpGainedLive = magicTracker.getXpGained();
                magicCurrentXp = magicTracker.getXp();
                magicTtlText = magicTracker.timeToNextLevelString();
                magicEtl = magicTracker.getXpForNextLevel();

                final int MAX_LEVEL = 99;
                int guard = 0;
                while (currentMagicLevel < MAX_LEVEL
                        && magicCurrentXp >= magicTracker.getExperienceForLevel(currentMagicLevel + 1)
                        && guard++ < 10) {
                    currentMagicLevel++;
                }

                if (currentMagicLevel >= MAX_LEVEL) {
                    magicTtlText = "MAXED";
                    magicEtl = 0;
                }
            }
        }

        // smithing xp tracked manually, but use tracker for level/ttl calculations
        String smithingTtlText = "-";
        double smithingXpGainedLive = manualSmithingXp;

        if (xpTracking != null) {
            XPTracker smithingTracker = xpTracking.getSmithingTracker();
            if (smithingTracker != null) {
                smithingTtlText = smithingTracker.timeToNextLevelString();
                currentSmithingLevel = smithingTracker.getLevel();

                if (currentSmithingLevel >= 99) {
                    smithingTtlText = "MAXED";
                }
            }
        }

        int magicXpPerHour = (int) Math.round(magicXpGainedLive / hours);
        int magicXpGainedInt = (int) Math.round(magicXpGainedLive);
        magicXpGained = magicXpGainedInt;

        int smithingXpPerHour = (int) Math.round(smithingXpGainedLive / hours);
        int smithingXpGainedInt = (int) Math.round(smithingXpGainedLive);
        smithingXpGained = smithingXpGainedInt;

        if (startMagicLevel <= 0) startMagicLevel = currentMagicLevel;
        int magicLevelsGained = Math.max(0, currentMagicLevel - startMagicLevel);
        String currentMagicLevelText = (magicLevelsGained > 0)
                ? (currentMagicLevel + " (+" + magicLevelsGained + ")")
                : String.valueOf(currentMagicLevel);

        if (startSmithingLevel <= 0) startSmithingLevel = currentSmithingLevel;
        int smithingLevelsGained = Math.max(0, currentSmithingLevel - startSmithingLevel);
        String currentSmithingLevelText = (smithingLevelsGained > 0)
                ? (currentSmithingLevel + " (+" + smithingLevelsGained + ")")
                : String.valueOf(currentSmithingLevel);

        java.text.DecimalFormat intFmt = new java.text.DecimalFormat("#,###");
        java.text.DecimalFormatSymbols sym = new java.text.DecimalFormatSymbols();
        sym.setGroupingSeparator('.');
        intFmt.setDecimalFormatSymbols(sym);

        // colors - dark teal theme with gold accents
        final Color bgColor = new Color(22, 49, 52);             // #163134 - dark teal background
        final Color borderColor = new Color(40, 75, 80);         // lighter teal border
        final Color accentGold = new Color(255, 215, 0);         // gold accent
        final Color accentYellow = new Color(255, 235, 130);     // lighter gold/yellow
        final Color textLight = new Color(238, 237, 233);        // #eeede9 - off-white text
        final Color textMuted = new Color(170, 185, 185);        // muted teal-gray for labels
        final Color valueGreen = new Color(180, 230, 150);       // soft green for magic xp
        final Color valueBlue = new Color(130, 180, 220);        // soft blue for smithing xp

        // layout
        final int x = 5;
        final int baseY = 40;
        final int width = 220;
        final int borderThickness = 2;
        final int paddingX = 10;                // side padding
        final int topGap = 6;                   // top padding
        final int lineGap = 16;                 // line padding
        final int logoBottomGap = 8;            // logo bottom padding

        int innerX = x;
        int innerY = baseY;
        int innerWidth = width;

        ensureLogoLoaded();
        int logoHeight = (logoImage != null) ? logoImage.height + logoBottomGap : 0;

        int totalLines = 12;
        int separatorCount = 3;
        int separatorOverhead = separatorCount * 12;  // separator padding (per separator)
        int bottomPadding = 1;                       // bottom padding
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
        curY += 16;  // post-logo separator padding

        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Runtime", runtime, textMuted.getRGB(), textLight.getRGB());

        curY += lineGap;
        int barsPerHour = (int) Math.round(barsCreated / hours);
        String barsText = intFmt.format(barsCreated) + " (" + intFmt.format(barsPerHour) + "/hr)";
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Bars created", barsText, textMuted.getRGB(), accentGold.getRGB());

        // separator before magic section
        curY += lineGap - 4;  // pre-separator padding
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
        curY += 16;  // post-separator padding

        String magicXpText = intFmt.format(magicXpGainedInt) + " (" + intFmt.format(magicXpPerHour) + "/hr)";
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Magic XP", magicXpText, textMuted.getRGB(), valueGreen.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Magic Lvl", currentMagicLevelText, textMuted.getRGB(), textLight.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Magic TTL", magicTtlText, textMuted.getRGB(), textLight.getRGB());

        // separator before smithing section
        curY += lineGap - 4;  // pre-separator padding
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
        curY += 16;  // post-separator padding

        String smithingXpText = intFmt.format(smithingXpGainedInt) + " (" + intFmt.format(smithingXpPerHour) + "/hr)";
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Smith XP", smithingXpText, textMuted.getRGB(), valueBlue.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Smith Lvl", currentSmithingLevelText, textMuted.getRGB(), textLight.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Smith TTL", smithingTtlText, textMuted.getRGB(), textLight.getRGB());

        curY += lineGap;
        String gauntletsText = hasGoldsmithGauntlets ? "Yes (56.2 xp)" : "No (22.5 xp)";
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Goldsmith", gauntletsText, textMuted.getRGB(), hasGoldsmithGauntlets ? accentGold.getRGB() : textLight.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Task", String.valueOf(task), textMuted.getRGB(), textLight.getRGB());

        // separator before version
        curY += lineGap - 4;  // pre-separator padding
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
        curY += 16;  // post-separator padding

        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Version", scriptVersion, textMuted.getRGB(), textMuted.getRGB());

        try { lastCanvasFrame.set(c.toImageCopy()); } catch (Exception ignored) {}
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
            log(getClass(), "logo loaded: " + w + "x" + h);

        } catch (Exception e) {
            log(getClass(), "error loading logo: " + e.getMessage());
        }
    }

    private void sendWebhookInternal() {
        ByteArrayOutputStream baos = null;
        try {
            Image source = lastCanvasFrame.get();
            if (source == null) {
                log("WEBHOOK", "No painted frame available; skipping webhook.");
                return;
            }

            BufferedImage buffered = source.toBufferedImage();
            baos = new ByteArrayOutputStream();
            ImageIO.write(buffered, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            long elapsed = System.currentTimeMillis() - startTime;
            String runtime = formatRuntime(elapsed);

            String displayUser = (webhookShowUser && user != null) ? user : "anonymous";

            long nextMillis = System.currentTimeMillis() + (webhookIntervalMinutes * 60_000L);
            ZonedDateTime nextLocal = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(nextMillis),
                    ZoneId.systemDefault()
            );
            String nextLocalStr = nextLocal.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            String imageFilename = "canvas.png";
            StringBuilder json = new StringBuilder();
            json.append("{ \"embeds\": [ {")
                    .append("\"title\": \"Script run summary - ").append(displayUser).append("\",")
                    .append("\"color\": 5189303,")
                    .append("\"author\": {")
                    .append("\"name\": \"Tidal's ").append(scriptName).append("\",")
                    .append("\"icon_url\": \"").append(authorIconUrl).append("\"")
                    .append("},")
                    .append("\"description\": ")
                    .append("\"This is your progress report after running for **")
                    .append(runtime)
                    .append("**.\\n")
                    .append("Make sure to share your proggies in the OSMB proggies channel\\n")
                    .append("https://discord.com/channels/736938454478356570/789791439487500299")
                    .append("\",")
                    .append("\"image\": { \"url\": \"attachment://").append(imageFilename).append("\" },")
                    .append("\"footer\": { \"text\": \"Next update/webhook at: ").append(nextLocalStr).append("\" }")
                    .append("} ] }");

            String boundary = "----WebBoundary" + System.currentTimeMillis();
            HttpURLConnection conn = (HttpURLConnection) new URL(webhookUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream out = conn.getOutputStream()) {
                out.write(("--" + boundary + "\r\n").getBytes());
                out.write("Content-Disposition: form-data; name=\"payload_json\"\r\n\r\n".getBytes());
                out.write(json.toString().getBytes(StandardCharsets.UTF_8));
                out.write("\r\n".getBytes());

                out.write(("--" + boundary + "\r\n").getBytes());
                out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + imageFilename + "\"\r\n").getBytes());
                out.write("Content-Type: image/png\r\n\r\n".getBytes());
                out.write(imageBytes);
                out.write("\r\n".getBytes());

                out.write(("--" + boundary + "--\r\n").getBytes());
                out.flush();
            }

            int code = conn.getResponseCode();
            long now = System.currentTimeMillis();

            if (code == 200 || code == 204) {
                lastWebhookSent = now;
                log("WEBHOOK", "Webhook sent.");
            } else if (code == 429) {
                long backoffMs = 30_000L;
                String ra = conn.getHeaderField("Retry-After");
                if (ra != null) {
                    try {
                        double sec = Double.parseDouble(ra.trim());
                        backoffMs = Math.max(1000L, (long)Math.ceil(sec * 1000.0));
                    } catch (NumberFormatException ignored) {}
                }
                nextWebhookEarliestMs = now + backoffMs + 250;
                log("WEBHOOK", "429 rate-limited. Backing off ~" + backoffMs + "ms");
            } else {
                log("WEBHOOK", "Webhook failed. HTTP " + code);
            }

        } catch (Exception e) {
            log("WEBHOOK", "Error: " + e.getMessage());
        } finally {
            try { if (baos != null) baos.close(); } catch (IOException ignored) {}
            webhookInFlight.set(false);
        }
    }

    public void queueSendWebhook() {
        if (!webhookEnabled) return;

        long now = System.currentTimeMillis();
        if (now < nextWebhookEarliestMs) return;
        if (now - lastWebhookSent < webhookIntervalMinutes * 60_000L) return;

        if (!webhookInFlight.compareAndSet(false, true)) return;

        sendWebhookAsync();
    }

    public void sendWebhookAsync() {
        Thread t = new Thread(this::sendWebhookInternal, "WebhookSender");
        t.setDaemon(true);
        t.start();
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

    private void sendStats(int magicXp, int smithingXp, int bars, long runtimeSecs) {
        try {
            // Only send if Secrets are configured
            if (obf.Secrets.STATS_URL == null || obf.Secrets.STATS_URL.isEmpty()) {
                log("STATS", "STATS_URL not configured, skipping");
                return;
            }

            // skip if nothing to report
            if (magicXp == 0 && smithingXp == 0 && bars == 0 && runtimeSecs == 0) {
                return;
            }

            log("STATS", "Sending stats to: " + obf.Secrets.STATS_URL);

            // send magic + smithing as combined xp, but also include separate fields in metadata
            int totalXp = magicXp + smithingXp;
            String json = String.format(
                    "{\"script\":\"%s\",\"session\":\"%s\",\"gp\":0,\"xp\":%d,\"runtime\":%d,\"barsCreated\":%d,\"magicXp\":%d,\"smithingXp\":%d}",
                    scriptName,
                    sessionId,
                    totalXp,
                    runtimeSecs,
                    bars,
                    magicXp,
                    smithingXp
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
                log("STATS", "Stats reported: magic=" + magicXp + ", smith=" + smithingXp + ", bars=" + bars + ", runtime=" + runtimeSecs + "s");
            } else {
                log("STATS", "Failed to report stats, HTTP " + code);
            }
        } catch (Exception e) {
            log("STATS", "Error sending stats: " + e.getClass().getSimpleName() + " - " + e.getMessage());
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
        } catch (Exception e) {
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
        String latest = getLatestVersion("https://raw.githubusercontent.com/Manokit/tidals-scripts/main/TidalsGoldSuperheater/src/main/java/main/TidalsGoldSuperheater.java");

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
}
