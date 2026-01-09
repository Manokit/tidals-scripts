package main;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.ui.tabs.Tab;
import strategies.MortMyreFungusCollector;
import strategies.SecondaryCollectorStrategy;

import java.awt.*;
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

    // paint fonts
    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_LABEL = new Font("Arial", Font.PLAIN, 12);

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

        // colors
        final Color oceanDeep = new Color(15, 52, 96, 240);
        final Color oceanDark = new Color(10, 35, 65, 240);
        final Color turquoise = new Color(64, 224, 208);
        final Color oceanBorder = new Color(0, 0, 0);
        final Color fungusColor = new Color(139, 90, 43);

        // layout
        final int x = 10;
        final int baseY = 50;
        final int width = 240;
        final int paddingX = 12;
        final int topGap = 8;
        final int lineGap = 18;
        final int titleHeight = 40;

        final int labelColor = Color.WHITE.getRGB();
        final int valueColor = turquoise.getRGB();
        final int itemColor = fungusColor.getRGB();

        int innerX = x;
        int innerY = baseY;
        int innerWidth = width;

        int totalLines = 8;
        int innerHeight = titleHeight + (totalLines * lineGap) + topGap + 18;

        // draw background
        c.fillRect(innerX - 2, innerY - 2, innerWidth + 4, innerHeight + 4, oceanBorder.getRGB(), 1);
        c.fillRect(innerX, innerY, innerWidth, innerHeight, oceanDeep.getRGB(), 1);
        c.fillRect(innerX, innerY, innerWidth, titleHeight, oceanDark.getRGB(), 1);

        // title
        String title = "Tidals Secondary Collector";
        int titleX = innerX + (innerWidth / 2) - (c.getFontMetrics(FONT_TITLE).stringWidth(title) / 2);
        int titleY = innerY + 26;
        c.drawText(title, titleX, titleY, valueColor, FONT_TITLE);

        // separator
        int sepY = innerY + titleHeight;
        c.fillRect(innerX, sepY, innerWidth, 1, oceanBorder.getRGB(), 1);

        int curY = innerY + titleHeight + topGap + lineGap;

        // stats
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Runtime", runtime, labelColor, labelColor);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Secondary", selectedSecondary.getDisplayName(), labelColor, valueColor);

        curY += lineGap;
        java.text.DecimalFormat fmt = new java.text.DecimalFormat("#,###");
        String itemsText = fmt.format(itemsCollected) + " (" + fmt.format(itemsPerHour) + "/hr)";
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Collected", itemsText, labelColor, itemColor);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Bloom Casts", String.valueOf(bloomCasts), labelColor, labelColor);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Bank Trips", String.valueOf(bankTrips), labelColor, labelColor);

        curY += lineGap / 2 + 4;
        c.fillRect(innerX + paddingX, curY, innerWidth - (paddingX * 2), 1, oceanBorder.getRGB(), 1);
        curY += lineGap / 2 + 4;

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "State", currentState.name(), labelColor, valueColor);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Status", statusMessage, labelColor, labelColor);

        curY += lineGap;
        drawStatLine(c, innerX, innerWidth, paddingX, curY, "Version", SCRIPT_VERSION, labelColor, labelColor);
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
