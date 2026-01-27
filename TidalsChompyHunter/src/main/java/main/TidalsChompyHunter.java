package main;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.shape.Polygon;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.ui.overlay.BuffOverlay;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.utils.UIResult;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.visual.image.Image;
import javafx.scene.Scene;
import tasks.AttackChompy;
import tasks.DetectPlayers;
import tasks.DropToads;
import tasks.FillBellows;
import tasks.HopWorld;
import tasks.InflateToads;
import tasks.Setup;
import utils.Task;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@ScriptDefinition(
        name = "TidalsChompyHunter",
        description = "Hunts chompy birds for Western Provinces Diary",
        skillCategory = SkillCategory.COMBAT,
        version = 1.5,
        author = "Tidaleus"
)
public class TidalsChompyHunter extends Script {
    public static final String SCRIPT_VERSION = "1.5";
    private static final String SCRIPT_NAME = "ChompyHunter";
    private static final String SESSION_ID = UUID.randomUUID().toString();

    // ═══════════════════════════════════════════════════════════════════════════
    // DEBUG MODE - set to true for verbose logging (use for debug builds)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final boolean VERBOSE_LOGGING = true;
    // ═══════════════════════════════════════════════════════════════════════════

    // stats reporting
    private static long lastStatsSent = 0;
    private static final long STATS_INTERVAL_MS = 600_000L; // 10 minutes
    private static int lastSentKillCount = 0;
    private static long lastSentRuntime = 0;
    private static int lastSentArrowsUsed = 0;

    // state
    public static boolean setupComplete = false;
    public static String task = "starting...";
    public static long startTime = System.currentTimeMillis();

    // stats
    public static int killCount = 0;
    public static int initialTotalKills = 0;
    public static volatile int gameReportedTotalKills = -1;  // -1 = not yet received

    // ammo tracking
    public static int initialArrowCount = 0;
    public static int currentArrowCount = -1;  // -1 = not yet checked, updated during idle
    public static volatile boolean outOfAmmo = false;
    public static int equippedArrowId = -1;  // set by Setup, used for BuffOverlay
    private BuffOverlay ammoOverlay = null;
    private long ammoOverlayMissingStart = 0;  // track when overlay went missing
    private boolean overlayVerificationPending = false;  // true when we need to verify via equipment tab

    // valid arrow item ids (for equipment tab verification)
    private static final int[] VALID_ARROWS = {
        2866,  // OGRE_ARROW
        4773,  // BRONZE_BRUTAL
        4778,  // IRON_BRUTAL
        4783,  // STEEL_BRUTAL
        4788,  // BLACK_BRUTAL
        4793,  // MITHRIL_BRUTAL
        4798,  // ADAMANT_BRUTAL
        4803   // RUNE_BRUTAL
    };


    // logical ground toad counter (tracks drops/kills instead of unreliable sprite detection)
    public static int groundToadCount = 0;

    // track dropped toad positions with timestamps (cleared when eaten by chompy or after 60s timeout)
    public static java.util.Map<WorldPosition, Long> droppedToadPositions = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long TOAD_TIMEOUT_MS = 60_000;  // 60 seconds max lifetime

    // track chompy corpse positions (cleared when plucked or despawned)
    public static List<WorldPosition> corpsePositions = new ArrayList<>();

    // ownership tracking - when we last had toads on ground (for grace period after consumption)
    public static long lastToadPresentTime = 0;
    private static final long TOAD_OWNERSHIP_GRACE_MS = 30_000;  // 30 seconds

    /**
     * check if we have an ownership claim on chompies in this world
     * true if we have toads on ground OR we're within grace period of having toads
     * prevents attacking someone else's chompies on fresh login/hop
     */
    public static boolean hasOwnershipClaim() {
        boolean hasToads = !droppedToadPositions.isEmpty();
        boolean inGrace = (System.currentTimeMillis() - lastToadPresentTime) < TOAD_OWNERSHIP_GRACE_MS;
        return hasToads || inGrace;
    }

    // settings from ScriptUI
    public static boolean pluckingEnabled = false;
    public static boolean webhookEnabled = false;
    public static String webhookUrl = "";
    public static boolean webhookIncludeUsername = true;
    public static int webhookIntervalMinutes = 5;

    // anti-crash settings
    public static boolean antiCrashEnabled = true;

    // periodic webhook tracking
    private static long lastWebhookSent = 0;

    // milestone tracking
    public static int lastMilestoneReached = 0;
    private int previousKillCount = 0;

    // ui
    private ScriptUI scriptUI;

    // paint
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 12);
    private static final Font FONT_VALUE = new Font("Arial", Font.BOLD, 12);
    private static final int[] DIARY_MILESTONES = {30, 125, 300, 1000};
    private Image logoImage = null;

    // chat parsing for kill detection and bellows empty detection
    private List<String> previousChatLines = new ArrayList<>();
    public static volatile boolean bellowsEmpty = false;
    public static volatile boolean toadAlreadyPlaced = false;

    // performance: throttle onNewFrame operations (don't need 60 FPS)
    private static final long CHAT_PARSE_INTERVAL_MS = 500;
    private static final long PLAYER_DETECT_INTERVAL_MS = 500;
    private long lastChatParseTime = 0;
    private long lastPlayerDetectTime = 0;

    private List<Task> tasks;
    private DetectPlayers detectPlayers;

    // feldip hills / chompy hunting regions
    private static final int[] CHOMPY_REGIONS = {
            9775,  // feldip hills
            9776,  // castle wars / feldip
            10031, // feldip hills east
            10032, // feldip hills swamp
            9519, // feldip hills fairy ring area
    };

    public TidalsChompyHunter(Object scriptCore) {
        super(scriptCore);
    }

    @Override
    public int[] regionsToPrioritise() {
        return CHOMPY_REGIONS;
    }

    /**
     * remove toads that have been on the ground longer than TOAD_TIMEOUT_MS
     * prevents getting stuck waiting for toads that won't be eaten
     */
    public void cleanupStaleToads() {
        if (droppedToadPositions.isEmpty()) return;

        long now = System.currentTimeMillis();
        int beforeCount = droppedToadPositions.size();

        droppedToadPositions.entrySet().removeIf(entry -> {
            long age = now - entry.getValue();
            if (age > TOAD_TIMEOUT_MS) {
                log(getClass(), "removing stale toad at " + entry.getKey() + " (age: " + (age / 1000) + "s)");
                return true;
            }
            return false;
        });

        int removed = beforeCount - droppedToadPositions.size();
        if (removed > 0) {
            log(getClass(), "cleaned up " + removed + " stale toads");
        }
    }

    /**
     * block world hops until toads are drained from ground
     * OSMB ProfileManager checks this before executing scheduled hops
     */
    @Override
    public boolean canHopWorlds() {
        // block hop while in combat
        if (AttackChompy.inCombat) {
            log(getClass(), "blocking hop - in combat");
            return false;
        }

        // clean up stale toads first
        cleanupStaleToads();

        // allow hop if no toads on ground
        if (droppedToadPositions.isEmpty()) {
            return true;
        }
        // block hop - toads need to drain first
        log(getClass(), "blocking hop - " + droppedToadPositions.size() + " toads on ground");
        return false;
    }

    /**
     * block breaks until toads are drained from ground
     * OSMB ProfileManager checks this before executing scheduled breaks
     */
    @Override
    public boolean canBreak() {
        // block break while in combat
        if (AttackChompy.inCombat) {
            log(getClass(), "blocking break - in combat");
            return false;
        }

        // clean up stale toads first
        cleanupStaleToads();

        // allow break if no toads on ground
        if (droppedToadPositions.isEmpty()) {
            return true;
        }
        // block break - toads need to drain first
        log(getClass(), "blocking break - " + droppedToadPositions.size() + " toads on ground");
        return false;
    }

    @Override
    public void onStart() {
        log(getClass(), "Starting " + SCRIPT_NAME + " v" + SCRIPT_VERSION);

        if (checkForUpdates()) {
            stop();
            return;
        }

        // show setup ui
        scriptUI = new ScriptUI(this);
        Scene scene = scriptUI.buildScene(this);
        getStageController().show(scene, "Chompy Hunter Options", false);

        startTime = System.currentTimeMillis();
        detectPlayers = new DetectPlayers(this);
        tasks = Arrays.asList(
            new HopWorld(this),       // highest priority - crash response
            new Setup(this),
            new AttackChompy(this),   // chompies interrupt everything else
            new FillBellows(this),
            new InflateToads(this),
            new DropToads(this)
        );
    }

    @Override
    public int poll() {
        // check for out of ammo condition
        if (outOfAmmo) {
            log(getClass(), "=== STOPPING: OUT OF ARROWS ===");
            task = "out of arrows";
            stop();
            return 0;
        }

        boolean taskRan = false;
        for (Task t : tasks) {
            if (t.activate()) {
                t.execute();
                taskRan = true;
                break;
            }
        }

        // set waiting status when nothing to do
        if (!taskRan && setupComplete) {
            task = "waiting for chompies...";
        }

        // update ammo count via BuffOverlay (lightweight, no tab switching)
        if (setupComplete) {
            updateAmmoFromOverlay();
        }

        // verify ammo via equipment tab when overlay has been missing
        // only verify during idle (no task running) to avoid interrupting other work
        if (setupComplete && overlayVerificationPending && !taskRan) {
            boolean confirmedEmpty = verifyAmmoFromEquipmentTab();
            if (confirmedEmpty) {
                outOfAmmo = true;
                // will stop on next poll() iteration
            }
        }

        // check for milestone when kills change
        if (killCount > previousKillCount) {
            checkMilestoneReached();
            previousKillCount = killCount;
        }

        // periodic webhook (only after setup is complete)
        if (setupComplete && webhookEnabled && !webhookUrl.isEmpty() && webhookIntervalMinutes > 0) {
            long now = System.currentTimeMillis();
            if (lastWebhookSent == 0) {
                lastWebhookSent = now; // initialize on first run
            } else if (now - lastWebhookSent >= webhookIntervalMinutes * 60_000L) {
                sendPeriodicWebhookAsync();
                lastWebhookSent = now;
            }
        }

        // dashboard stats reporting (every 10 minutes after setup)
        if (setupComplete) {
            long nowMs = System.currentTimeMillis();
            if (nowMs - lastStatsSent >= STATS_INTERVAL_MS) {
                long elapsed = nowMs - startTime;

                // calculate increments since last send
                int killIncrement = killCount - lastSentKillCount;
                long runtimeIncrement = (elapsed / 1000) - lastSentRuntime;

                // calculate arrows used (initialArrowCount - currentArrowCount)
                int currentArrowsUsed = (currentArrowCount >= 0)
                    ? initialArrowCount - currentArrowCount
                    : killCount;  // fallback estimate: ~1 arrow per kill
                int arrowsUsedIncrement = currentArrowsUsed - lastSentArrowsUsed;

                sendStats(killIncrement, runtimeIncrement, arrowsUsedIncrement);

                // update last sent values
                lastSentKillCount = killCount;
                lastSentRuntime = elapsed / 1000;
                lastSentArrowsUsed = currentArrowsUsed;
                lastStatsSent = nowMs;
            }
        }

        // return delay when no task ran to prevent tight loops
        return taskRan ? 0 : 600;
    }

    /**
     * check if a diary milestone was just reached and notify
     */
    private void checkMilestoneReached() {
        int totalKills = initialTotalKills + killCount;

        for (int milestone : DIARY_MILESTONES) {
            if (totalKills >= milestone && lastMilestoneReached < milestone) {
                lastMilestoneReached = milestone;
                log(getClass(), "Milestone reached: " + milestone + " chompy kills!");

                if (webhookEnabled && !webhookUrl.isEmpty()) {
                    sendMilestoneWebhookAsync(milestone, totalKills);
                }
            }
        }
    }

    /**
     * send milestone notification to discord webhook (non-blocking daemon thread)
     */
    private void sendMilestoneWebhookAsync(int milestone, int totalKills) {
        Thread t = new Thread(() -> {
            try {
                String content = String.format("Reached %d kills! Total: %d", milestone, totalKills);
                String json = String.format("{\"content\":\"%s\"}", content);

                java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    new java.net.URL(webhookUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code == 200 || code == 204) {
                    log(getClass(), "Milestone webhook sent for " + milestone + " kills");
                } else if (code == 429) {
                    log(getClass(), "Webhook rate limited, skipping");
                } else {
                    log(getClass(), "Webhook failed: HTTP " + code);
                }
            } catch (IOException e) {
                log(getClass(), "Webhook error: " + e.getMessage());
            }
        }, "MilestoneWebhook");
        t.setDaemon(true);
        t.start();
    }

    /**
     * send periodic progress webhook to discord (non-blocking daemon thread)
     */
    private void sendPeriodicWebhookAsync() {
        Thread t = new Thread(() -> {
            try {
                long elapsed = System.currentTimeMillis() - startTime;
                String runtime = formatRuntime(elapsed);
                int totalKills = initialTotalKills + killCount;
                int nextMilestone = getNextMilestone(totalKills);
                int toGo = Math.max(0, nextMilestone - totalKills);
                double hours = Math.max(1e-9, elapsed / 3_600_000.0);
                int killsPerHour = (int) Math.round(killCount / hours);

                // build embed for nicer formatting
                int arrowsRemaining = Math.max(0, initialArrowCount - killCount);

                StringBuilder desc = new StringBuilder();
                desc.append("**Session Kills:** ").append(killCount);
                desc.append(" (").append(killsPerHour).append("/hr)\\n");
                desc.append("**Total Kills:** ").append(totalKills).append("\\n");
                desc.append("**Next Milestone:** ").append(nextMilestone);
                desc.append(" (").append(toGo).append(" to go)\\n");
                desc.append("**Arrows:** ").append(arrowsRemaining).append(" remaining\\n");
                desc.append("**Runtime:** ").append(runtime);

                String title = "Chompy Hunter Progress";
                if (webhookIncludeUsername) {
                    String username = getWidgetManager().getChatbox().getUsername();
                    if (username != null && !username.isEmpty()) {
                        title = title + " - " + username;
                    }
                }

                String json = String.format(
                    "{\"embeds\":[{\"title\":\"%s\",\"description\":\"%s\",\"color\":16766720}]}",
                    title, desc.toString()
                );

                java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    new java.net.URL(webhookUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code == 200 || code == 204) {
                    log(getClass(), "periodic webhook sent - " + killCount + " kills, " + runtime);
                } else if (code == 429) {
                    log(getClass(), "webhook rate limited, skipping");
                } else {
                    log(getClass(), "webhook failed: HTTP " + code);
                }
            } catch (IOException e) {
                log(getClass(), "webhook error: " + e.getMessage());
            }
        }, "PeriodicWebhook");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void onNewFrame() {
        long now = System.currentTimeMillis();

        // throttle chat parsing to every 500ms (doesn't need 60 FPS)
        if (now - lastChatParseTime >= CHAT_PARSE_INTERVAL_MS) {
            updateChatBoxLines();
            lastChatParseTime = now;
        }

        // throttle player detection to every 500ms (doesn't need 60 FPS)
        if (detectPlayers != null && now - lastPlayerDetectTime >= PLAYER_DETECT_INTERVAL_MS) {
            detectPlayers.runDetection();
            lastPlayerDetectTime = now;
        }
    }

    /**
     * parse chatbox for kill confirmation message
     * called from onNewFrame - read-only, no interactions
     */
    private void updateChatBoxLines() {
        UIResultList<String> chatResult = getWidgetManager().getChatbox().getText();
        if (chatResult == null || chatResult.isNotFound()) {
            return;
        }

        List<String> currentLines = chatResult.asList();
        List<String> newLines = getNewLines(currentLines, previousChatLines);

        // DEBUG: log ALL new chat lines
        if (VERBOSE_LOGGING && !newLines.isEmpty()) {
            for (String line : newLines) {
                log(getClass(), "[DEBUG] chat: \"" + line + "\"");
            }
        }

        for (String line : newLines) {
            if (line.contains("scratch a notch")) {
                log(getClass(), "kill detected via game message");
                AttackChompy.killDetected = true;
            }

            // parse "You've scratched up a total of (x) chompy bird kills so far!"
            if (line.contains("scratched up a total of")) {
                int kills = parseTotalKills(line);
                if (kills > 0) {
                    gameReportedTotalKills = kills;

                    // set baseline on first detection (for paint display)
                    if (initialTotalKills == 0) {
                        initialTotalKills = kills - 1;  // -1 because this message is POST-kill

                        // also update lastMilestoneReached to prevent old milestone notifications
                        for (int milestone : DIARY_MILESTONES) {
                            if (initialTotalKills >= milestone) {
                                lastMilestoneReached = milestone;
                            }
                        }

                        log(getClass(), "baseline set to " + (kills - 1) + " total kills, milestone: " + lastMilestoneReached);
                    }

                    log(getClass(), "game reported total kills: " + kills);
                }
            }

            if (line.contains("air seems too thin")) {
                log(getClass(), "bellows empty detected via game message");
                bellowsEmpty = true;
            }

            // detect pluck start for verification
            if (line.contains("You start plucking the chompy bird")) {
                AttackChompy.pluckStarted = true;
            }

            // detect toad placement collision
            if (line.contains("already placed at this location")) {
                toadAlreadyPlaced = true;
            }

            // detect out of ammo
            if (line.contains("no ammo left in your quiver")) {
                log(getClass(), "OUT OF ARROWS detected via game message");
                outOfAmmo = true;
            }
        }

        previousChatLines = new ArrayList<>(currentLines);
    }

    /**
     * find lines in current that are not in previous
     */
    private List<String> getNewLines(List<String> current, List<String> previous) {
        List<String> newLines = new ArrayList<>();
        for (String line : current) {
            if (!previous.contains(line)) {
                newLines.add(line);
            }
        }
        return newLines;
    }

    /**
     * parse total kills from chatbox message
     * "You've scratched up a total of 42 chompy bird kills so far!"
     */
    private int parseTotalKills(String line) {
        if (line == null || line.isEmpty()) {
            return -1;
        }

        String[] parts = line.split("total of ");
        if (parts.length < 2) {
            return -1;
        }

        String numPart = parts[1].split(" ")[0];
        try {
            return Integer.parseInt(numPart.replaceAll(",", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * update ammo count from BuffOverlay (lightweight, no tab switching)
     * when overlay is missing for 10+ seconds, triggers equipment tab verification
     */
    private void updateAmmoFromOverlay() {
        // initialize overlay if we have arrow ID but no overlay yet
        // each arrow type has its own buff overlay ID matching its item ID
        if (ammoOverlay == null && equippedArrowId > 0) {
            ammoOverlay = new BuffOverlay(this, equippedArrowId);
            log(getClass(), "ammo overlay initialized for item ID: " + equippedArrowId);
        }

        if (ammoOverlay == null) {
            return;
        }

        if (ammoOverlay.isVisible()) {
            // reset missing timer and verification flag
            ammoOverlayMissingStart = 0;
            overlayVerificationPending = false;

            // parse ammo count from overlay text
            String buffText = ammoOverlay.getBuffText();
            if (buffText != null && !buffText.isEmpty()) {
                try {
                    String digits = buffText.replaceAll("\\D", "");
                    if (!digits.isEmpty()) {
                        int count = Integer.parseInt(digits);
                        if (count != currentArrowCount) {
                            currentArrowCount = count;
                            // only log significant changes to reduce spam
                            if (count < 100 || count % 50 == 0) {
                                log(getClass(), "ammo count: " + count);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // ignore parse errors
                }
            }
        } else {
            // overlay not visible - start tracking missing time
            if (ammoOverlayMissingStart == 0) {
                ammoOverlayMissingStart = System.currentTimeMillis();
                log(getClass(), "[debug] overlay not visible for equipped ID: " + equippedArrowId);
            }

            // if missing for 10+ seconds, flag for verification (don't assume out of ammo)
            long missingTime = System.currentTimeMillis() - ammoOverlayMissingStart;
            if (missingTime > 10000 && !overlayVerificationPending) {
                log(getClass(), "ammo overlay missing for 10+ seconds - will verify via equipment tab");
                overlayVerificationPending = true;
            }
        }
    }

    /**
     * verify ammo count by checking equipment tab directly
     * called when overlay has been missing and we need to confirm actual arrow count
     * returns true if verified (either has arrows or confirmed empty), false if check failed
     */
    private boolean verifyAmmoFromEquipmentTab() {
        log(getClass(), "verifying ammo via equipment tab...");
        task = "verifying ammo...";

        // open equipment tab
        getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        pollFramesUntil(() -> false, RandomUtils.weightedRandom(400, 600));

        // check for any valid arrows
        UIResult<ItemSearchResult> arrowCheck = getWidgetManager().getEquipment().findItem(VALID_ARROWS);
        if (arrowCheck == null || !arrowCheck.isFound()) {
            // no arrows found in equipment slot - confirmed out of ammo
            log(getClass(), "VERIFIED: no arrows in equipment slot - out of ammo");
            return true;  // verification complete, no arrows
        }

        ItemSearchResult arrowResult = arrowCheck.get();
        if (arrowResult == null) {
            log(getClass(), "arrow check returned null result - will retry");
            return false;  // verification failed, retry later
        }

        int verifiedCount = arrowResult.getStackAmount();
        log(getClass(), "VERIFIED: " + verifiedCount + " arrows in equipment (overlay was wrong)");

        // update tracking with verified count
        currentArrowCount = verifiedCount;

        // reset overlay tracking since we just verified
        ammoOverlayMissingStart = 0;
        overlayVerificationPending = false;

        // check if actually out of ammo (0 or very low)
        if (verifiedCount <= 0) {
            log(getClass(), "verified 0 arrows - out of ammo");
            return true;  // verification complete, no arrows
        }

        // still have arrows - false positive from overlay
        log(getClass(), "still have " + verifiedCount + " arrows - continuing");
        return false;  // has arrows, keep running
    }

    @Override
    public void onPaint(Canvas c) {
        // draw tileCubes around tracked toad positions (green)
        for (WorldPosition toadPos : droppedToadPositions.keySet()) {
            Polygon tileCube = getSceneProjector().getTileCube(toadPos, 30);
            if (tileCube != null) {
                c.fillPolygon(tileCube, new Color(0, 255, 0, 50).getRGB(), 0.3);
                c.drawPolygon(tileCube, Color.GREEN.getRGB(), 1.0);
            }
        }

        // draw tileCubes around tracked corpse positions (orange) - pending pluck
        for (WorldPosition corpsePos : corpsePositions) {
            Polygon tileCube = getSceneProjector().getTileCube(corpsePos, 70);
            if (tileCube != null) {
                c.fillPolygon(tileCube, new Color(255, 140, 0, 50).getRGB(), 0.3);
                c.drawPolygon(tileCube, new Color(255, 140, 0).getRGB(), 1.0);
            }
        }

        // draw tileCubes around IGNORED chompy positions (red) - dead chompies we skip
        for (WorldPosition ignoredPos : AttackChompy.getIgnoredPositions()) {
            Polygon tileCube = getSceneProjector().getTileCube(ignoredPos, 70);
            if (tileCube != null) {
                c.fillPolygon(tileCube, new Color(255, 0, 0, 50).getRGB(), 0.3);
                c.drawPolygon(tileCube, Color.RED.getRGB(), 1.0);
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        double hours = Math.max(1e-9, elapsed / 3_600_000.0);
        String runtime = formatRuntime(elapsed);

        int killsPerHour = (int) Math.round(killCount / hours);
        // use game value when available to match chatbox exactly
        int totalKills = (gameReportedTotalKills > 0)
            ? gameReportedTotalKills
            : initialTotalKills + killCount;
        int nextMilestone = getNextMilestone(totalKills);
        int toGo = Math.max(0, nextMilestone - totalKills);

        // number formatter with period separator
        DecimalFormat intFmt = new DecimalFormat("#,###");
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setGroupingSeparator('.');
        intFmt.setDecimalFormatSymbols(sym);

        // colors
        final Color bgColor = new Color(22, 49, 52);
        final Color borderColor = new Color(40, 75, 80);
        final Color accentGold = new Color(255, 215, 0);
        final Color textLight = new Color(238, 237, 233);
        final Color textMuted = new Color(170, 185, 185);

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

        // calculate height: runtime, kills, total, next, arrows, can hop, separator, status, separator, version
        int totalLines = 8;
        int contentHeight = topGap + logoHeight + (totalLines * lineGap) + 24 + 20;
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

        // gold separator after logo
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, accentGold.getRGB(), 1);
        curY += 16;

        // runtime
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Runtime", runtime, textMuted.getRGB(), textLight.getRGB());
        curY += lineGap;

        // kills with kills/hr
        String killText = intFmt.format(killCount) + " (" + intFmt.format(killsPerHour) + "/hr)";
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Kills", killText, textMuted.getRGB(), accentGold.getRGB());
        curY += lineGap;

        // total / milestone
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Total",
                intFmt.format(totalKills) + "/" + intFmt.format(nextMilestone),
                textMuted.getRGB(), textLight.getRGB());
        curY += lineGap;

        // next milestone (X to go)
        String nextText = intFmt.format(nextMilestone) + " (" + intFmt.format(toGo) + " to go)";
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Next", nextText, textMuted.getRGB(), textMuted.getRGB());
        curY += lineGap;

        // arrows (use live count if available, else estimate from initial - kills)
        int arrowsRemaining = (currentArrowCount >= 0)
            ? currentArrowCount
            : Math.max(0, initialArrowCount - killCount);
        String arrowText = intFmt.format(arrowsRemaining) + " remaining";
        // color warning if low (under 100)
        int arrowColor = arrowsRemaining < 100 ? new Color(255, 100, 100).getRGB() : textMuted.getRGB();
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Arrows", arrowText, textMuted.getRGB(), arrowColor);
        curY += lineGap;

        // can hop status - shows if toads need to drain before scheduled hop/break
        boolean canHop = droppedToadPositions.isEmpty();
        String canHopText = canHop ? "Yes" : "No (" + droppedToadPositions.size() + " toads)";
        int canHopColor = canHop ? new Color(100, 255, 100).getRGB() : new Color(255, 100, 100).getRGB();
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Can hop", canHopText, textMuted.getRGB(), canHopColor);

        // separator before status
        curY += lineGap - 4;
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
        curY += 16;

        // status
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Status", task, textMuted.getRGB(), textLight.getRGB());

        // separator before version
        curY += lineGap - 4;
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, borderColor.getRGB(), 1);
        curY += 16;

        // version
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Version", SCRIPT_VERSION, textMuted.getRGB(), textMuted.getRGB());
    }

    private void drawStatLine(Canvas c, int innerX, int innerWidth, int paddingX, int y,
                              String label, String value, int labelColor, int valueColor) {
        c.drawText(label, innerX + paddingX, y, labelColor, FONT_LABEL);
        int valW = c.getFontMetrics(FONT_VALUE).stringWidth(value);
        int valX = innerX + innerWidth - paddingX - valW;
        c.drawText(value, valX, y, valueColor, FONT_VALUE);
    }

    private String formatRuntime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        if (days > 0) {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private int getNextMilestone(int totalKills) {
        for (int milestone : DIARY_MILESTONES) {
            if (totalKills < milestone) {
                return milestone;
            }
        }
        return 4000; // next hat milestone after all diaries
    }

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
        } catch (IOException e) {
            log(getClass(), "error loading logo: " + e.getMessage());
        }
    }

    /**
     * send stats to dashboard API (incremental values)
     */
    private void sendStats(int killIncrement, long runtimeSecs, int arrowsUsedIncrement) {
        try {
            if (obf.Secrets.STATS_URL == null || obf.Secrets.STATS_URL.isEmpty()) {
                return;
            }

            // skip if nothing to report
            if (killIncrement == 0 && runtimeSecs == 0 && arrowsUsedIncrement == 0) {
                return;
            }

            String json = String.format(
                    "{\"script\":\"%s\",\"session\":\"%s\",\"gp\":0,\"xp\":0,\"runtime\":%d,\"kills\":%d,\"arrowsUsed\":%d}",
                    SCRIPT_NAME,
                    SESSION_ID,
                    runtimeSecs,
                    killIncrement,
                    arrowsUsedIncrement
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
                log(getClass(), "stats reported: kills=" + killIncrement + ", arrows=" + arrowsUsedIncrement + ", runtime=" + runtimeSecs + "s");
            }
        } catch (IOException e) {
            log(getClass(), "stats error: " + e.getClass().getSimpleName());
        }
    }

    // version checking
    public String getLatestVersion(String urlString) {
        try {
            java.net.URL url = new java.net.URL(urlString);
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
            log(getClass(), "Exception occurred while fetching version from GitHub.");
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
        String latest = getLatestVersion("https://raw.githubusercontent.com/Manokit/tidals-scripts/main/TidalsChompyHunter/src/main/java/main/TidalsChompyHunter.java");

        if (latest == null) {
            log(getClass(), "Could not fetch latest version info.");
            return false;
        }

        if (compareVersions(SCRIPT_VERSION, latest) < 0) {
            for (int i = 0; i < 10; i++) {
                log(getClass(), "New version v" + latest + " found! Please update the script before running it again.");
            }
            return true;
        }

        log(getClass(), "You are running the latest version (v" + SCRIPT_VERSION + ").");
        return false;
    }
}
