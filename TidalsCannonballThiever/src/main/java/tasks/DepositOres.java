package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.ui.chatbox.dialogue.Dialogue;

import com.osmb.api.utils.UIResult;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import java.util.Set;

import static main.TidalsCannonballThiever.*;

/**
 * Two-stall mode: Deposit ores at deposit box when inventory is full
 */
public class DepositOres extends Task {
    // deposit box position
    private static final WorldPosition DEPOSIT_BOX_TILE = new WorldPosition(1872, 3301, 0);
    
    // cannonball stall position for return (two-stall mode)
    private static final WorldPosition CANNONBALL_STALL_TILE = new WorldPosition(1867, 3295, 0);

    private final WalkConfig exactTileConfig;

    public DepositOres(Script script) {
        super(script);
        // Exact tile positioning - no randomization, stop exactly at destination
        this.exactTileConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .build();
    }

    @Override
    public boolean activate() {
        // only in two-stall mode
        if (!twoStallMode) return false;

        // CRITICAL: If we're mid-deposit-run (interrupted by AFK/hop), continue it!
        if (doingDepositRun) {
            script.log("DEPOSIT", "Resuming interrupted deposit run...");
            return true;
        }

        // PRIMARY: Simple inventory full check using built-in .isFull() method
        try {
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
            if (inv != null && inv.isFull()) {
                script.log("DEPOSIT", "Inventory full (28/28) - need to deposit!");
                return true;
            }
        } catch (Exception e) {
            script.log("DEPOSIT", "Error checking inventory: " + e.getMessage());
        }

        // BACKUP: check for "inventory is too full" dialogue (edge case)
        if (hasInventoryFullDialogue()) {
            script.log("DEPOSIT", "Detected 'inventory too full' dialogue (fallback)!");
            return true;
        }

        return false;
    }
    
    /**
     * Check if the "inventory is too full" dialogue is present
     */
    private boolean hasInventoryFullDialogue() {
        try {
            Dialogue dialogue = script.getWidgetManager().getDialogue();
            if (dialogue == null || !dialogue.isVisible()) return false;
            
            // Try to get dialogue text (any type)
            UIResult<String> textResult = dialogue.getText();
            if (textResult == null || !textResult.isFound()) return false;
            
            String text = textResult.get().toLowerCase();
            // Check for various inventory full messages
            return text.contains("inventory is too full") || 
                   text.contains("inventory is full") ||
                   text.contains("can't carry any more");
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean execute() {
        task = "Depositing ores";
        currentlyThieving = false;  // Allow breaks/hops/AFKs during deposit run
        doingDepositRun = true;     // Flag for canBreak/canHop/canAFK

        // dismiss dialogue if present
        if (hasInventoryFullDialogue()) {
            script.log("DEPOSIT", "Dismissing 'inventory full' dialogue...");
            try {
                script.getWidgetManager().getDialogue().continueChatDialogue();
                script.pollFramesHuman(() -> false, script.random(300, 500));
            } catch (Exception e) {
                script.log("DEPOSIT", "Failed to dismiss dialogue: " + e.getMessage());
            }
        }

        // STATE-AWARE EXECUTION: Check what stage we're at
        boolean hasItems = !isInventoryEmpty();
        boolean nearDepositBox = isNearDepositBox();
        boolean atStall = isAtCannonballStallExact();
        
        // STAGE 1: If we have items, need to deposit them
        if (hasItems) {
            // Walk to deposit box if not near it
            if (!nearDepositBox) {
                script.log("DEPOSIT", "Walking to deposit box...");
                script.getWalker().walkTo(DEPOSIT_BOX_TILE, exactTileConfig);
                script.pollFramesUntil(() -> isNearDepositBox(), 8000, false);
                script.pollFramesHuman(() -> false, script.random(300, 600));
            }
            
            // Open deposit box
            if (!openDepositBoxWithMenu()) {
                script.log("DEPOSIT", "Failed to open deposit box, retrying...");
                return false;  // Keep doingDepositRun true for retry
            }

            // Wait for deposit interface
            script.pollFramesUntil(() -> isDepositInterfaceOpen(), 5000);
            script.pollFramesHuman(() -> false, script.random(200, 400));

            if (!isDepositInterfaceOpen()) {
                script.log("DEPOSIT", "Deposit interface didn't open, retrying...");
                return false;  // Keep doingDepositRun true for retry
            }

            // Deposit all
            if (!depositAll()) {
                script.log("DEPOSIT", "Failed to deposit, retrying...");
                return false;  // Keep doingDepositRun true for retry
            }

            // Wait for inventory to empty
            script.pollFramesUntil(() -> isInventoryEmpty(), 3000);
            script.pollFramesHuman(() -> false, script.random(300, 500));

            // Close deposit interface
            closeDepositInterface();
            
            script.log("DEPOSIT", "Ores deposited!");
        }

        // STAGE 2: Items deposited, return to stall
        if (!atStall) {
            script.log("DEPOSIT", "Returning to cannonball stall...");
            script.getWalker().walkTo(CANNONBALL_STALL_TILE, exactTileConfig);
            script.pollFramesUntil(() -> isAtCannonballStallExact(), 8000, false);
            script.pollFramesHuman(() -> false, script.random(300, 600));
        }

        // DONE: Clear flags
        atOreStall = false;
        doingDepositRun = false;
        script.log("DEPOSIT", "Deposit run complete - back at cannonball stall!");
        return true;
    }
    
    /**
     * Open deposit box using menu entry to avoid misclicks
     */
    private boolean openDepositBoxWithMenu() {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

        RSObject depositBox = script.getObjectManager().getClosestObject(myPos, "Bank deposit box");
        if (depositBox == null) {
            script.log("DEPOSIT", "Can't find deposit box!");
            return false;
        }

        Polygon boxPoly = depositBox.getConvexHull();
        if (boxPoly == null) {
            script.log("DEPOSIT", "Deposit box hull null");
            return false;
        }

        // Use menu entry "Deposit" to ensure we click the right thing
        script.log("DEPOSIT", "Opening deposit box via menu...");
        boolean tapped = script.getFinger().tap(boxPoly, "Deposit");
        
        if (tapped) {
            script.log("DEPOSIT", "Deposit action sent!");
            return true;
        }
        
        script.log("DEPOSIT", "Failed to send Deposit action");
        return false;
    }

    private boolean isInventoryEmpty() {
        try {
            // Empty = 28 free slots. Simple check, no item IDs needed.
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
            return inv != null && inv.getFreeSlots() == 28;
        } catch (Exception e) {
            return true; // assume empty on error
        }
    }

    private boolean isNearDepositBox() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        // within 2 tiles of deposit box
        return Math.abs(x - 1872) <= 2 && Math.abs(y - 3301) <= 2;
    }

    private boolean isDepositInterfaceOpen() {
        try {
            // check if deposit box interface is visible
            return script.getWidgetManager().getDepositBox().isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean depositAll() {
        try {
            // deposit ALL items (empty set = ignore nothing)
            boolean result = script.getWidgetManager().getDepositBox().depositAll(Set.of());
            
            if (result) {
                script.log("DEPOSIT", "Deposited all items!");
                
                // Reset inventory snapshot so next thieving session starts fresh
                if (script instanceof main.TidalsCannonballThiever) {
                    ((main.TidalsCannonballThiever) script).resetInventorySnapshot();
                }
            }
            
            return result;
        } catch (Exception e) {
            script.log("DEPOSIT", "Error depositing: " + e.getMessage());
            return false;
        }
    }

    private void closeDepositInterface() {
        try {
            if (isDepositInterfaceOpen()) {
                script.getWidgetManager().getDepositBox().close();
                script.pollFramesHuman(() -> false, script.random(200, 400));
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isAtCannonballStallExact() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        // Exact tile check
        return x == 1867 && y == 3295;
    }
}
