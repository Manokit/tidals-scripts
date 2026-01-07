package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.ui.chatbox.dialogue.Dialogue;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
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
    private static final WorldPosition DEPOSIT_BOX_TILE = new WorldPosition(1871, 3302, 0);
    
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

        // check for "inventory is too full" dialogue first (regardless of position)
        if (hasInventoryFullDialogue()) {
            script.log("DEPOSIT", "Detected 'inventory too full' dialogue!");
            return true;
        }

        // check inventory full when we've visited ore stall
        if (atOreStall && isInventoryFull()) {
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
            
            DialogueType type = dialogue.getDialogueType();
            if (type != DialogueType.TAP_HERE_TO_CONTINUE) return false;
            
            UIResult<String> textResult = dialogue.getText();
            if (textResult == null || !textResult.isFound()) return false;
            
            String text = textResult.get().toLowerCase();
            return text.contains("inventory is too full");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean execute() {
        task = "Depositing ores";
        currentlyThieving = false;
        script.log("DEPOSIT", "Inventory full - heading to deposit box!");

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

        // Click deposit box directly - walker will path there automatically
        RSObject depositBox = script.getObjectManager().getClosestObject(script.getWorldPosition(), "Bank deposit box");
        if (depositBox == null) {
            script.log("DEPOSIT", "Can't find deposit box!");
            return false;
        }
        
        // Walk to and interact with deposit box (click it directly, game paths us there)
        script.log("DEPOSIT", "Clicking deposit box...");
        Polygon boxPoly = depositBox.getConvexHull();
        if (boxPoly != null) {
            script.getFinger().tap(boxPoly);
        }
        
        // Wait until we're near and interface opens
        script.pollFramesUntil(() -> isNearDepositBox() || isDepositInterfaceOpen(), 8000);

        // wait for deposit interface to open
        script.pollFramesUntil(() -> isDepositInterfaceOpen(), 3000);
        script.pollFramesHuman(() -> false, script.random(200, 400));

        // deposit all
        if (!depositAll()) {
            script.log("DEPOSIT", "Failed to deposit, retrying...");
            return false;
        }

        // wait for inventory to be empty
        script.pollFramesUntil(() -> isInventoryEmpty(), 3000);
        script.pollFramesHuman(() -> false, script.random(300, 500));

        // close deposit interface if still open
        closeDepositInterface();

        script.log("DEPOSIT", "Ores deposited! Returning to cannonball stall...");

        // walk back to cannonball stall - exact tile positioning
        script.getWalker().walkTo(CANNONBALL_STALL_TILE, exactTileConfig);
        script.pollFramesUntil(() -> isAtCannonballStallExact(), 8000);

        atOreStall = false;
        script.log("DEPOSIT", "Back at cannonball stall!");
        return true;
    }

    private boolean isInventoryFull() {
        try {
            // check if inventory has 28 items (full)
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(ORE_IDS);
            if (inv == null) return false;
            // full if we have 28 ores (assuming we start empty for two-stall mode)
            return inv.getAmount() >= 28;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isInventoryEmpty() {
        try {
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(ORE_IDS);
            return inv == null || inv.getAmount() == 0;
        } catch (Exception e) {
            return true; // assume empty on error
        }
    }

    // common ore IDs from the ore stall
    private static final Set<Integer> ORE_IDS = Set.of(
            436,  // copper ore
            438,  // tin ore
            440,  // iron ore
            442,  // silver ore
            444,  // gold ore
            447,  // mithril ore
            449,  // adamantite ore
            451,  // runite ore
            453   // coal
    );

    private boolean isNearDepositBox() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        // within 2 tiles of deposit box
        return Math.abs(x - 1871) <= 2 && Math.abs(y - 3302) <= 2;
    }

    private boolean openDepositBox() {
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

        // fast left-click (deposit is default action)
        return script.getFinger().tap(boxPoly);
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
            // count ores before depositing to track stolen count
            ItemGroupResult inv = script.getWidgetManager().getInventory().search(ORE_IDS);
            int oreCount = (inv != null) ? inv.getAmount() : 0;
            
            // deposit ALL items (empty set = ignore nothing)
            boolean result = script.getWidgetManager().getDepositBox().depositAll(Set.of());
            
            // increment stolen count
            if (result && oreCount > 0) {
                oresStolen += oreCount;
                script.log("DEPOSIT", "Deposited " + oreCount + " ores (total: " + oresStolen + ")");
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
