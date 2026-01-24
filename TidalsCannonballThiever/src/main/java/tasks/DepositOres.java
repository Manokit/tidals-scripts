package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.ui.chatbox.dialogue.Dialogue;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import java.util.Set;

import static main.TidalsCannonballThiever.*;

// deposits items when inventory full (two-stall mode only)
public class DepositOres extends Task {
    private static final WorldPosition DEPOSIT_BOX_TILE = new WorldPosition(1872, 3301, 0);
    private static final WorldPosition CANNONBALL_STALL_TILE = new WorldPosition(1867, 3295, 0);

    private final WalkConfig exactTileConfig;

    public DepositOres(Script script) {
        super(script);
        this.exactTileConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .build();
    }

    @Override
    public boolean activate() {
        if (!twoStallMode) return false;

        // resume interrupted deposit run
        if (doingDepositRun) {
            script.log("DEPOSIT", "Resuming interrupted deposit run...");
            return true;
        }

        // check if inventory full
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
        if (inv != null && inv.isFull()) {
            script.log("DEPOSIT", "Inventory full (28/28) - need to deposit!");
            return true;
        }

        // fallback: dialogue check
        if (hasInventoryFullDialogue()) {
            script.log("DEPOSIT", "Detected 'inventory too full' dialogue (fallback)!");
            return true;
        }

        return false;
    }
    
    private boolean hasInventoryFullDialogue() {
        Dialogue dialogue = script.getWidgetManager().getDialogue();
        if (dialogue == null || !dialogue.isVisible()) return false;

        UIResult<String> textResult = dialogue.getText();
        if (textResult == null || !textResult.isFound()) return false;

        String text = textResult.get().toLowerCase();
        return text.contains("inventory is too full") ||
               text.contains("inventory is full") ||
               text.contains("can't carry any more");
    }
    
    @Override
    public boolean execute() {
        task = "Depositing ores";
        currentlyThieving = false;
        doingDepositRun = true;

        if (hasInventoryFullDialogue()) {
            script.log("DEPOSIT", "Dismissing 'inventory full' dialogue...");
            script.getWidgetManager().getDialogue().continueChatDialogue();
            script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(300, 1000, 0.002));
        }

        boolean hasItems = !isInventoryEmpty();
        boolean nearDepositBox = isNearDepositBox();
        boolean atStall = isAtCannonballStallExact();
        
        if (hasItems) {
            if (!nearDepositBox) {
                script.log("DEPOSIT", "Walking to deposit box...");
                script.getWalker().walkTo(DEPOSIT_BOX_TILE, exactTileConfig);
                script.pollFramesUntil(() -> isNearDepositBox(), 8000, false);
                script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(300, 1200, 0.002));
            }
            
            if (!openDepositBoxWithMenu()) {
                script.log("DEPOSIT", "Failed to open deposit box, retrying...");
                return false;
            }

            script.pollFramesUntil(() -> isDepositInterfaceOpen(), 5000);
            script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(200, 800, 0.002));

            if (!isDepositInterfaceOpen()) {
                script.log("DEPOSIT", "Deposit interface didn't open, retrying...");
                return false;
            }

            if (!depositAll()) {
                script.log("DEPOSIT", "Failed to deposit, retrying...");
                return false;
            }

            script.pollFramesUntil(() -> isInventoryEmpty(), 3000);
            script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(300, 1000, 0.002));

            closeDepositInterface();
            
            script.log("DEPOSIT", "Ores deposited!");
        }

        if (!atStall) {
            script.log("DEPOSIT", "Returning to cannonball stall...");
            script.getWalker().walkTo(CANNONBALL_STALL_TILE, exactTileConfig);
            script.pollFramesUntil(() -> isAtCannonballStallExact(), 8000, false);
            script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(300, 1200, 0.002));
        }

        atOreStall = false;
        doingDepositRun = false;

        // reset thieving cycle state so first drop assumption works again
        StartThieving.resetForNewCycle();
        if (guardTracker != null) {
            guardTracker.resetCbCycle();
            guardTracker.resetGuardTracking();
            guardTracker.enableGuardSync(); // wait to see guard leave before starting
        }

        script.log("DEPOSIT", "Deposit run complete - back at cannonball stall!");
        return true;
    }
    
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
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
        return inv != null && inv.getFreeSlots() == 28;
    }

    private boolean isNearDepositBox() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return Math.abs(x - 1872) <= 2 && Math.abs(y - 3301) <= 2;
    }

    private boolean isDepositInterfaceOpen() {
        return script.getWidgetManager().getDepositBox().isVisible();
    }

    private boolean depositAll() {
        boolean result = script.getWidgetManager().getDepositBox().depositAll(Set.of());
        if (result) {
            script.log("DEPOSIT", "Deposited all items!");
            if (script instanceof main.TidalsCannonballThiever) {
                ((main.TidalsCannonballThiever) script).resetInventorySnapshot();
            }
        }
        return result;
    }

    private void closeDepositInterface() {
        if (isDepositInterfaceOpen()) {
            script.getWidgetManager().getDepositBox().close();
            script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(200, 800, 0.002));
        }
    }

    private boolean isAtCannonballStallExact() {
        WorldPosition pos = script.getWorldPosition();
        if (pos == null) return false;
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        return x == 1867 && y == 3295;
    }
}
