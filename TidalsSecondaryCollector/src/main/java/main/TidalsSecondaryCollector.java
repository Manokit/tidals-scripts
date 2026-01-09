package main;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.visual.image.Image;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.ui.tabs.Tab;
import strategies.MortMyreFungusCollector;
import strategies.SecondaryCollectorStrategy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;

@ScriptDefinition(
        name = "TidalsSecondaryCollector",
        description = "Collects herblore secondaries for ironmen",
        skillCategory = SkillCategory.HERBLORE,
        version = 1.0,
        author = "Tidaleus"
)
public class TidalsSecondaryCollector extends Script {

    public static final String SCRIPT_VERSION = "1.0";

    // ui settings - hardcoded for now, can add ui later
    public static SecondaryType selectedSecondary = SecondaryType.MORT_MYRE_FUNGUS;
    public static boolean hasVarrockMediumDiary = false; // set to true if you have the diary

    // state tracking
    public static boolean setupComplete = false;
    public static State currentState = State.IDLE;
    public static String statusMessage = "starting...";
    public static boolean allowAFK = true;  // prevents afk/hop during bloom collection

    // stats
    public static int itemsCollected = 0;
    public static int bloomCasts = 0;
    public static int bankTrips = 0;
    public static long startTime = System.currentTimeMillis();

    // strategy pattern for different secondaries
    private SecondaryCollectorStrategy activeStrategy;

    // paint fonts - bold for modern look
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 12);
    private static final Font FONT_VALUE = new Font("Arial", Font.BOLD, 12);

    // logo
    private Image logoImage = null;

    public enum SecondaryType {
        MORT_MYRE_FUNGUS("Mort Myre Fungus");
        // future: RED_SPIDERS_EGGS, SNAPE_GRASS, etc.

        private final String displayName;

        SecondaryType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum State {
        IDLE,
        COLLECTING,
        BANKING,
        RESTORING_PRAYER,
        RETURNING
    }

    public TidalsSecondaryCollector(Object scriptCore) {
        super(scriptCore);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[]{
                14642, // ver sinhaza + 4 log tile (mort myre)
                10290, // kandarin monastery (ardy cloak)
                12850, // lumbridge + altar
                9776,  // castle wars (dueling ring)
                11571, // crafting guild
                12598  // grand exchange
        };
    }

    @Override
    public boolean canAFK() {
        return allowAFK;
    }

    @Override
    public void onStart() {
        log(getClass(), "starting tidals secondary collector v" + SCRIPT_VERSION);
        startTime = System.currentTimeMillis();
    }

    @Override
    public int poll() {
        // wait for setup to complete
        if (!setupComplete) {
            return doSetup();
        }

        // get current state and execute strategy
        if (activeStrategy == null) {
            log(getClass(), "no active strategy, stopping");
            stop();
            return 0;
        }

        // determine state
        currentState = activeStrategy.determineState();

        // execute based on state
        switch (currentState) {
            case COLLECTING:
                statusMessage = "collecting";
                return activeStrategy.collect();

            case BANKING:
                statusMessage = "banking";
                return activeStrategy.bank();

            case RESTORING_PRAYER:
                statusMessage = "restoring prayer";
                return activeStrategy.restorePrayer();

            case RETURNING:
                statusMessage = "returning to area";
                return activeStrategy.returnToArea();

            case IDLE:
            default:
                statusMessage = "idle";
                return 600;
        }
    }

    private int doSetup() {
        statusMessage = "setting up...";
        log(getClass(), "running setup");

        // open inventory first
        getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
        boolean opened = pollFramesUntil(() ->
                        getWidgetManager().getInventory().search(Set.of()) != null,
                3000
        );

        if (!opened) {
            log(getClass(), "failed to open inventory");
            return 600;
        }

        // create strategy based on selected secondary
        switch (selectedSecondary) {
            case MORT_MYRE_FUNGUS:
                activeStrategy = new MortMyreFungusCollector(this);
                break;
            default:
                log(getClass(), "unknown secondary type: " + selectedSecondary);
                stop();
                return 0;
        }

        // verify requirements
        if (!activeStrategy.verifyRequirements()) {
            log(getClass(), "requirements not met, stopping");
            stop();
            return 0;
        }

        log(getClass(), "setup complete, starting " + selectedSecondary.getDisplayName() + " collection");
        setupComplete = true;
        return 0;
    }

    @Override
    public void onPaint(Canvas c) {
        long elapsed = System.currentTimeMillis() - startTime;
        double hours = Math.max(1e-9, elapsed / 3_600_000.0);
        String runtime = formatRuntime(elapsed);

        int itemsPerHour = (int) Math.round(itemsCollected / hours);

        // colors - off-white theme to match logo background
        final Color bgColor = new Color(238, 237, 233);       // #eeede9 - logo background
        final Color borderColor = new Color(90, 75, 60);      // dark brown border
        final Color accentColor = new Color(139, 90, 43);     // fungus brown accent
        final Color textDark = new Color(45, 40, 35);         // dark text
        final Color textMuted = new Color(100, 90, 80);       // muted text for labels
        final Color valueHighlight = new Color(85, 60, 30);   // darker brown for values

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

        int totalLines = 8;
        int contentHeight = topGap + logoHeight + (totalLines * lineGap) + 16;
        int innerHeight = Math.max(200, contentHeight);

        // outer border
        c.fillRect(innerX - borderThickness, innerY - borderThickness,
                innerWidth + (borderThickness * 2),
                innerHeight + (borderThickness * 2),
                borderColor.getRGB(), 1);

        // main background
        c.fillRect(innerX, innerY, innerWidth, innerHeight, bgColor.getRGB(), 1);

        // inner border line
        c.drawRect(innerX, innerY, innerWidth, innerHeight, borderColor.getRGB());

        int curY = innerY + topGap;

        // draw logo centered
        if (logoImage != null) {
            int logoX = innerX + (innerWidth - logoImage.width) / 2;
            c.drawAtOn(logoImage, logoX, curY);
            curY += logoImage.height + logoBottomGap;
        }

        // thin separator after logo
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, accentColor.getRGB(), 1);
        curY += 16;

        java.text.DecimalFormat fmt = new java.text.DecimalFormat("#,###");

        // stats
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Runtime", runtime, textMuted.getRGB(), textDark.getRGB());

        curY += lineGap;
        String itemsText = fmt.format(itemsCollected) + " (" + fmt.format(itemsPerHour) + "/hr)";
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Collected", itemsText, textMuted.getRGB(), accentColor.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Bloom Casts", String.valueOf(bloomCasts), textMuted.getRGB(), textDark.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Bank Trips", String.valueOf(bankTrips), textMuted.getRGB(), textDark.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "State", currentState.name(), textMuted.getRGB(), valueHighlight.getRGB());

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Status", statusMessage, textMuted.getRGB(), textDark.getRGB());

        // separator before version
        curY += lineGap - 4;
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, new Color(200, 195, 185).getRGB(), 1);
        curY += 12;

        drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Version", SCRIPT_VERSION, textMuted.getRGB(), textMuted.getRGB());
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
