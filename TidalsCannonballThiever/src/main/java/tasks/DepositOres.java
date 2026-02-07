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
import com.osmb.api.ui.component.chatbox.ChatboxComponent;
import utilities.MovementChecker;
import utils.Task;

import java.util.List;
import java.util.Set;

import static main.TidalsCannonballThiever.*;

// deposits items when inventory full (two-stall mode only)
// uses poll-based state machine - each execute() handles ONE state transition
public class DepositOres extends Task {
    private static final WorldPosition DEPOSIT_BOX_TILE = new WorldPosition(1872, 3301, 0);

    private final WalkConfig exactTileConfig;
    private final WalkConfig depositBoxWalkConfig;

    public DepositOres(Script script) {
        super(script);
        this.exactTileConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakDistance(0)
                .tileRandomisationRadius(0)
                .build();
        // stop walking when deposit box object loads in scene (close enough to interact)
        this.depositBoxWalkConfig = new WalkConfig.Builder()
                .disableWalkScreen(true)
                .breakCondition(() -> {
                    RSObject box = script.getObjectManager().getClosestObject(
                            script.getWorldPosition(), "Bank deposit box");
                    return box != null;
                })
                .build();
    }

    @Override
    public boolean activate() {
        if (!twoStallMode) return false;

        // resume interrupted deposit run
        if (doingDepositRun) {
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
        // dialogue object is never null - just check visibility
        Dialogue dialogue = script.getWidgetManager().getDialogue();
        if (!dialogue.isVisible()) return false;

        // UIResult is never null - just check isFound()
        UIResult<String> textResult = dialogue.getText();
        if (!textResult.isFound()) return false;

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

        // state 1: dismiss dialogue if present
        if (hasInventoryFullDialogue()) {
            script.log("DEPOSIT", "Dismissing 'inventory full' dialogue...");
            script.getWidgetManager().getDialogue().continueChatDialogue();
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(300, 1000, 0.002));
            return true; // re-poll to check next state
        }

        // state 2a: deposit interface open, has items → click deposit button
        if (isDepositInterfaceOpen() && !isDepositBoxEmpty()) {
            // wait for interface to be ready (visual != interactive)
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(100, 300));

            script.log("DEPOSIT", "Clicking deposit all button...");
            if (!depositAll()) {
                script.log("DEPOSIT", "Failed to click deposit, retrying...");
            }
            // wait for deposit to process, then re-poll to verify
            script.pollFramesUntil(() -> isDepositBoxEmpty(), RandomUtils.weightedRandom(2500, 4000, 0.002));
            script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 500));
            return true;
        }

        // state 2b: deposit interface open, items deposited → close interface
        if (isDepositInterfaceOpen() && isDepositBoxEmpty()) {
            script.log("DEPOSIT", "Ores deposited, closing interface...");
            closeDepositInterface();
            return true; // re-poll to finish
        }

        // state 3: deposit box interactable on screen → open it directly
        if (!isInventoryEmpty() && isDepositBoxInteractable()) {
            if (!openDepositBoxWithMenu()) {
                script.log("DEPOSIT", "Failed to open deposit box, retrying...");
                return true;
            }

            // use MovementChecker to detect misclicks/interrupts instead of blindly waiting
            WorldPosition pos = script.getWorldPosition();
            MovementChecker movementChecker = new MovementChecker(pos != null ? pos : DEPOSIT_BOX_TILE);
            script.pollFramesUntil(() -> {
                if (isDepositInterfaceOpen()) return true;
                WorldPosition currentPos = script.getWorldPosition();
                if (currentPos == null) return false;
                return movementChecker.hasTimedOut(currentPos);
            }, RandomUtils.weightedRandom(8000, 12000, 0.002));
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(200, 800, 0.002));

            if (!isDepositInterfaceOpen()) {
                script.log("DEPOSIT", "Deposit interface didn't open, retrying...");
            }
            return true; // re-poll to deposit
        }

        // state 4: deposit box not interactable → walk closer
        if (!isInventoryEmpty()) {
            script.log("DEPOSIT", "Walking to deposit box...");
            script.getWalker().walkTo(DEPOSIT_BOX_TILE, depositBoxWalkConfig);
            return true; // re-poll — next poll will check interactability
        }

        // state 5: inventory empty, deposit complete - clean up and let ReturnToThieving handle walking back
        finishDepositRun();
        return true;
    }

    // reset state and let other tasks take over (walking back to stall handled by ReturnToThieving)
    private void finishDepositRun() {
        atOreStall = false;
        doingDepositRun = false;

        // reset thieving cycle state so first drop assumption works again
        StartThieving.resetForNewCycle();
        if (guardTracker != null) {
            guardTracker.resetCbCycle();
            guardTracker.resetGuardTracking();
            guardTracker.enableGuardSync(); // wait to see guard leave before starting
        }

        script.log("DEPOSIT", "Deposit run complete!");
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

        // check if object is interactable on screen (not hidden behind UI)
        if (!depositBox.isInteractableOnScreen()) {
            script.log("DEPOSIT", "Deposit box not interactable on screen, walking closer...");
            script.getWalker().walkTo(DEPOSIT_BOX_TILE, exactTileConfig);
            return false;
        }

        // check visibility factor - hull can be non-null but hidden by chatbox
        double visibilityFactor = script.getWidgetManager().insideGameScreenFactor(boxPoly, List.of(ChatboxComponent.class));
        if (visibilityFactor < 0.3) {
            script.log("DEPOSIT", "Deposit box visibility too low (" + String.format("%.1f", visibilityFactor * 100) + "%), walking closer...");
            script.getWalker().walkTo(DEPOSIT_BOX_TILE, exactTileConfig);
            return false;
        }

        script.log("DEPOSIT", "Opening deposit box via menu...");
        boolean tapped = script.getFinger().tapGameScreen(boxPoly, "Deposit");

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

    // check deposit box contents when interface is open (inventory widget not visible)
    private boolean isDepositBoxEmpty() {
        ItemGroupResult items = script.getWidgetManager().getDepositBox().search(Set.of());
        // deposit box shows inventory - 28 free slots = empty
        return items == null || items.getFreeSlots() == 28;
    }

    private boolean isDepositBoxInteractable() {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;
        RSObject depositBox = script.getObjectManager().getClosestObject(myPos, "Bank deposit box");
        if (depositBox == null) return false;
        return depositBox.isInteractableOnScreen();
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
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(200, 800, 0.002));
        }
    }
}
