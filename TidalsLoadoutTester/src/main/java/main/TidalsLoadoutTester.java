package main;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.visual.drawing.Canvas;
import javafx.scene.Scene;
import utilities.BankingUtils;
import utilities.loadout.LoadoutItem;
import utilities.loadout.LoadoutManager;
import utilities.loadout.RestockResult;

import java.awt.*;

/**
 * Validation script for the loadout system.
 * Tests the full workflow: editor -> persistence -> restock -> verify.
 */
@ScriptDefinition(
        name = "TidalsLoadoutTester",
        description = "Validates LoadoutManager end-to-end workflow",
        skillCategory = SkillCategory.COMBAT,
        version = 1.0,
        author = "Tidaleus"
)
public class TidalsLoadoutTester extends Script {

    // === state machine ===

    private enum State {
        SETUP,
        VALIDATE_LOADOUT,
        OPEN_BANK,
        DEPOSIT_ALL,
        FIRST_RESTOCK,
        VERIFY_FIRST_RESTOCK,
        REMOVE_ONE_ITEM,
        SECOND_RESTOCK,
        VERIFY_SECOND_RESTOCK,
        COMPLETE
    }

    private State currentState = State.SETUP;

    // === loadout manager ===

    private LoadoutManager loadouts;

    // === test tracking ===

    private int passedAssertions = 0;
    private int failedAssertions = 0;
    private String lastActionResult = "";

    // === ui ===

    private ScriptUI scriptUI;
    private boolean editorOnlyMode = false;
    private long startTime;

    // === paint colors ===

    private static final Color BG_COLOR = new Color(22, 49, 52, 200);
    private static final Color BORDER_COLOR = new Color(40, 75, 80);
    private static final Color ACCENT_GOLD = new Color(255, 215, 0);
    private static final Color TEXT_LIGHT = new Color(238, 237, 233);
    private static final Color TEXT_MUTED = new Color(170, 185, 185);
    private static final Color VALUE_GREEN = new Color(180, 230, 150);
    private static final Color VALUE_RED = new Color(255, 100, 100);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 12);

    public TidalsLoadoutTester(Object scriptCore) {
        super(scriptCore);
    }

    @Override
    public int[] regionsToPrioritise() {
        return new int[]{12850, 12598, 12342, 12853};
    }

    @Override
    public void onStart() {
        startTime = System.currentTimeMillis();
        log("TidalsLoadoutTester starting...");

        try {
            // create loadout manager
            loadouts = new LoadoutManager(this, "TidalsLoadoutTester");

            // try to load from preferences
            if (loadouts.loadFromPreferences()) {
                log("Loaded loadout from preferences");
            } else {
                log("No saved loadout found, starting with empty loadout");
            }

            // show setup UI
            scriptUI = new ScriptUI(this, loadouts);
            Scene scene = scriptUI.buildScene(this);
            getStageController().show(scene, "Loadout Tester Setup", false);

            log("Setup UI shown");
        } catch (Exception e) {
            log("ERROR in onStart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int poll() {
        switch (currentState) {
            case SETUP:
                return handleSetup();
            case VALIDATE_LOADOUT:
                return handleValidateLoadout();
            case OPEN_BANK:
                return handleOpenBank();
            case DEPOSIT_ALL:
                return handleDepositAll();
            case FIRST_RESTOCK:
                return handleFirstRestock();
            case VERIFY_FIRST_RESTOCK:
                return handleVerifyFirstRestock();
            case REMOVE_ONE_ITEM:
                return handleRemoveOneItem();
            case SECOND_RESTOCK:
                return handleSecondRestock();
            case VERIFY_SECOND_RESTOCK:
                return handleVerifySecondRestock();
            case COMPLETE:
                return handleComplete();
            default:
                return 600;
        }
    }

    // === state handlers ===

    private int handleSetup() {
        // wait for UI to close
        return 1000;
    }

    private int handleValidateLoadout() {
        log("=".repeat(50));
        log("STATE: VALIDATE_LOADOUT");
        log("=".repeat(50));

        // check loadout has items
        int equipCount = countEquipmentItems();
        int invCount = countInventoryItems();

        log("Equipment items: " + equipCount);
        log("Inventory items: " + invCount);

        boolean hasEquipment = equipCount >= 1;
        boolean hasInventory = invCount >= 1;

        assertTrue("loadout has at least 1 equipment item", hasEquipment);
        assertTrue("loadout has at least 1 inventory item", hasInventory);

        if (!hasEquipment || !hasInventory) {
            lastActionResult = "Validation FAILED: need equipment + inventory";
            log("ERROR: Loadout must have at least 1 equipment and 1 inventory item");
            transitionTo(State.COMPLETE);
            return 0;
        }

        lastActionResult = "Validation PASSED";
        transitionTo(State.OPEN_BANK);
        return 0;
    }

    private int handleOpenBank() {
        log("=".repeat(50));
        log("STATE: OPEN_BANK");
        log("=".repeat(50));

        if (getWidgetManager().getBank().isVisible()) {
            lastActionResult = "Bank already open";
            transitionTo(State.DEPOSIT_ALL);
            return 0;
        }

        log("Opening bank...");
        lastActionResult = "Opening bank...";

        if (!BankingUtils.openBankAndWait(this, 15000)) {
            log("Failed to open bank!");
            lastActionResult = "Bank open FAILED";
            failedAssertions++;
            transitionTo(State.COMPLETE);
            return 0;
        }

        lastActionResult = "Bank opened";
        transitionTo(State.DEPOSIT_ALL);
        return 0;
    }

    private int handleDepositAll() {
        log("=".repeat(50));
        log("STATE: DEPOSIT_ALL");
        log("=".repeat(50));

        if (!getWidgetManager().getBank().isVisible()) {
            transitionTo(State.OPEN_BANK);
            return 0;
        }

        log("Depositing all items for clean slate...");
        lastActionResult = "Depositing all...";

        // deposit worn equipment and inventory
        BankingUtils.depositAll(this);

        lastActionResult = "Deposit complete";
        transitionTo(State.FIRST_RESTOCK);
        return 0;
    }

    private int handleFirstRestock() {
        log("=".repeat(50));
        log("STATE: FIRST_RESTOCK");
        log("=".repeat(50));

        if (!getWidgetManager().getBank().isVisible()) {
            transitionTo(State.OPEN_BANK);
            return 0;
        }

        log("Calling LoadoutManager.restock() - should withdraw and equip everything");
        lastActionResult = "First restock...";

        RestockResult result = loadouts.restock();

        log("Restock result: " + result);
        assertTrue("first restock succeeds", result.isSuccess());

        if (result.isSuccess()) {
            lastActionResult = "First restock: SUCCESS";
        } else {
            lastActionResult = "First restock: FAILED - " + result.getFailureReason();
        }

        transitionTo(State.VERIFY_FIRST_RESTOCK);
        return 0;
    }

    private int handleVerifyFirstRestock() {
        log("=".repeat(50));
        log("STATE: VERIFY_FIRST_RESTOCK");
        log("=".repeat(50));

        log("Verifying loadout is complete...");
        lastActionResult = "Verifying first restock...";

        // close bank to check equipment (equipment check needs tabs visible)
        if (getWidgetManager().getBank().isVisible()) {
            getWidgetManager().getBank().close();
            pollFramesUntil(() -> !getWidgetManager().getBank().isVisible(), 3000);
        }

        boolean needsRestock = loadouts.needsRestock();
        assertTrue("nothing missing after first restock", !needsRestock);

        if (needsRestock) {
            log("ERROR: Items still missing after first restock!");
            lastActionResult = "Verify first: ITEMS MISSING";
        } else {
            log("First restock verified - all items present");
            lastActionResult = "Verify first: PASSED";
        }

        transitionTo(State.REMOVE_ONE_ITEM);
        return 0;
    }

    private int handleRemoveOneItem() {
        log("=".repeat(50));
        log("STATE: REMOVE_ONE_ITEM");
        log("=".repeat(50));

        // need bank open to deposit item
        if (!getWidgetManager().getBank().isVisible()) {
            log("Opening bank to deposit one item...");
            if (!BankingUtils.openBankAndWait(this, 15000)) {
                log("Failed to open bank!");
                lastActionResult = "Bank open FAILED";
                failedAssertions++;
                transitionTo(State.COMPLETE);
                return 0;
            }
            return 0;
        }

        // find first inventory item from loadout and deposit it
        LoadoutItem[] inventory = loadouts.getLoadout().getInventory();
        Integer firstItemId = null;

        for (LoadoutItem item : inventory) {
            if (item != null) {
                firstItemId = item.getItemId();
                break;
            }
        }

        if (firstItemId == null) {
            log("ERROR: No inventory items to remove for test");
            lastActionResult = "Remove item: NO ITEMS";
            failedAssertions++;
            transitionTo(State.COMPLETE);
            return 0;
        }

        log("Depositing item ID: " + firstItemId + " to create deficit");
        lastActionResult = "Removing 1 item...";

        // deposit 1 of this item
        getWidgetManager().getBank().deposit(firstItemId, 1);

        lastActionResult = "Removed 1 item";
        transitionTo(State.SECOND_RESTOCK);
        return 0;
    }

    private int handleSecondRestock() {
        log("=".repeat(50));
        log("STATE: SECOND_RESTOCK");
        log("=".repeat(50));

        if (!getWidgetManager().getBank().isVisible()) {
            log("Opening bank for second restock...");
            if (!BankingUtils.openBankAndWait(this, 15000)) {
                log("Failed to open bank!");
                lastActionResult = "Bank open FAILED";
                failedAssertions++;
                transitionTo(State.COMPLETE);
                return 0;
            }
            return 0;
        }

        // use restockInventory() - skips equipment since it's already validated
        log("Calling LoadoutManager.restockInventory() - should only check inventory");
        lastActionResult = "Second restock (inventory only)...";

        RestockResult result = loadouts.restockInventory();

        log("Restock result: " + result);
        assertTrue("second restock succeeds", result.isSuccess());

        if (result.isSuccess()) {
            // verify it only restocked 1 item
            int restockedCount = result.getTotalRestocked();
            log("Restocked " + restockedCount + " items");
            assertTrue("second restock restocked only 1 item", restockedCount == 1);
            lastActionResult = "Second restock: " + restockedCount + " item(s)";
        } else {
            lastActionResult = "Second restock: FAILED";
        }

        transitionTo(State.VERIFY_SECOND_RESTOCK);
        return 0;
    }

    private int handleVerifySecondRestock() {
        log("=".repeat(50));
        log("STATE: VERIFY_SECOND_RESTOCK");
        log("=".repeat(50));

        log("Verifying loadout is complete after second restock...");
        lastActionResult = "Verifying second restock...";

        // close bank to check equipment (equipment check needs tabs visible)
        if (getWidgetManager().getBank().isVisible()) {
            getWidgetManager().getBank().close();
            pollFramesUntil(() -> !getWidgetManager().getBank().isVisible(), 3000);
        }

        boolean needsRestock = loadouts.needsRestock();
        assertTrue("nothing missing after second restock", !needsRestock);

        if (needsRestock) {
            log("ERROR: Items still missing after second restock!");
            lastActionResult = "Verify second: ITEMS MISSING";
        } else {
            log("Second restock verified - all items present");
            lastActionResult = "Verify second: PASSED";
        }

        transitionTo(State.COMPLETE);
        return 0;
    }

    private int handleComplete() {
        log("=".repeat(50));
        log("TEST COMPLETE");
        log("=".repeat(50));

        logTestResult();
        lastActionResult = "TEST COMPLETE";

        // stay in complete state
        return 5000;
    }

    // === assertion helpers ===

    private void assertTrue(String name, boolean condition) {
        if (condition) {
            log("[PASS] " + name);
            passedAssertions++;
        } else {
            log("[FAIL] " + name);
            failedAssertions++;
        }
    }

    private void logTestResult() {
        log("");
        log("=".repeat(50));
        log("TEST RESULTS");
        log("=".repeat(50));
        log("Passed: " + passedAssertions);
        log("Failed: " + failedAssertions);
        log("Total:  " + (passedAssertions + failedAssertions));
        log("");

        if (failedAssertions == 0) {
            log("STATUS: ALL TESTS PASSED");
        } else {
            log("STATUS: TESTS FAILED");
        }
        log("=".repeat(50));
    }

    // === utility methods ===

    private void transitionTo(State newState) {
        log("Transition: " + currentState + " -> " + newState);
        currentState = newState;
    }

    private int countEquipmentItems() {
        LoadoutItem[] equipment = loadouts.getLoadout().getEquipment();
        int count = 0;
        for (LoadoutItem item : equipment) {
            if (item != null) count++;
        }
        return count;
    }

    private int countInventoryItems() {
        LoadoutItem[] inventory = loadouts.getLoadout().getInventory();
        int count = 0;
        for (LoadoutItem item : inventory) {
            if (item != null) count++;
        }
        return count;
    }

    /**
     * Called from ScriptUI when user starts the test.
     */
    public void onStartTest(boolean editorOnly) {
        this.editorOnlyMode = editorOnly;
        log("Starting test, editorOnly=" + editorOnly);

        if (editorOnly) {
            log("Editor-only mode - staying in SETUP state");
            currentState = State.SETUP;
        } else {
            // save loadout before starting
            loadouts.saveToPreferences();
            log("Loadout saved to preferences");

            transitionTo(State.VALIDATE_LOADOUT);
        }
    }

    /**
     * Gets the LoadoutManager for UI access.
     */
    public LoadoutManager getLoadoutManager() {
        return loadouts;
    }

    // === paint overlay ===

    @Override
    public void onPaint(Canvas c) {
        long elapsed = System.currentTimeMillis() - startTime;
        String runtime = formatRuntime(elapsed);

        // layout
        int x = 10;
        int y = 10;
        int width = 220;
        int height = 120;
        int paddingX = 10;
        int lineGap = 16;

        // draw background with border
        c.fillRect(x - 2, y - 2, width + 4, height + 4, BORDER_COLOR.getRGB(), 1);
        c.fillRect(x, y, width, height, BG_COLOR.getRGB(), 1);

        int curY = y + 12;

        // header
        c.drawText("Loadout Tester", x + paddingX, curY, ACCENT_GOLD.getRGB(), FONT_LABEL);
        int runtimeWidth = c.getFontMetrics(FONT_LABEL).stringWidth(runtime);
        c.drawText(runtime, x + width - paddingX - runtimeWidth, curY, TEXT_LIGHT.getRGB(), FONT_LABEL);

        curY += lineGap;

        // gold separator
        c.fillRect(x + paddingX, curY - 4, width - (paddingX * 2), 1, ACCENT_GOLD.getRGB(), 1);

        curY += 8;

        // current state
        drawStatLine(c, x, width, paddingX, curY, "State", currentState.name(), TEXT_MUTED.getRGB(), TEXT_LIGHT.getRGB());
        curY += lineGap;

        // assertions
        String assertStr = passedAssertions + " passed, " + failedAssertions + " failed";
        int assertColor = failedAssertions == 0 ? VALUE_GREEN.getRGB() : VALUE_RED.getRGB();
        drawStatLine(c, x, width, paddingX, curY, "Assertions", assertStr, TEXT_MUTED.getRGB(), assertColor);
        curY += lineGap;

        // last action
        String truncatedAction = lastActionResult;
        if (truncatedAction.length() > 25) {
            truncatedAction = truncatedAction.substring(0, 22) + "...";
        }
        drawStatLine(c, x, width, paddingX, curY, "Last", truncatedAction, TEXT_MUTED.getRGB(), TEXT_LIGHT.getRGB());
        curY += lineGap;

        // loadout summary
        int eqCount = countEquipmentItems();
        int invCount = countInventoryItems();
        String loadoutStr = "Eq:" + eqCount + "/14, Inv:" + invCount + "/28";
        drawStatLine(c, x, width, paddingX, curY, "Loadout", loadoutStr, TEXT_MUTED.getRGB(), TEXT_MUTED.getRGB());
    }

    private void drawStatLine(Canvas c, int innerX, int innerWidth, int paddingX, int y,
                              String label, String value, int labelColor, int valueColor) {
        c.drawText(label, innerX + paddingX, y, labelColor, FONT_LABEL);
        int valW = c.getFontMetrics(FONT_LABEL).stringWidth(value);
        int valX = innerX + innerWidth - paddingX - valW;
        c.drawText(value, valX, y, valueColor, FONT_LABEL);
    }

    private String formatRuntime(long millis) {
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
