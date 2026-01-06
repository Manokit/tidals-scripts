package tasks;

import com.osmb.api.input.MenuEntry;
import com.osmb.api.input.MenuHook;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import java.util.concurrent.atomic.AtomicReference;

import static main.TidalsCannonballThiever.*;

public class ReturnToThieving extends Task {
    private static final WorldPosition THIEVING_TILE = new WorldPosition(1867, 3298, 0);
    private static final RectangleArea THIEVING_AREA = new RectangleArea(1865, 3296, 1869, 3300, 0);

    public ReturnToThieving(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // activate if NOT at thieving tile AND guard has passed (safe to return)
        return !isAtThievingTile() && guardTracker.isSafeToReturn();
    }

    @Override
    public boolean execute() {
        task = "Returning to stall";
        script.log("RETURN", "Guard has passed, returning to thieving tile");

        // get tile polygon for direct click (more reliable for short distances)
        // height 0 gives flat tile surface
        Polygon tilePoly = script.getSceneProjector().getTileCube(THIEVING_TILE, 0);

        if (tilePoly == null) {
            // fallback to walker if tile not visible
            script.log("RETURN", "Tile not visible, using walker...");
            script.getWalker().walkTo(THIEVING_TILE);
        } else {
            // use menu hook to click "walk here" on tile
            AtomicReference<String> selectedAction = new AtomicReference<>(null);
            MenuHook hook = getWalkHereMenuHook(selectedAction);

            boolean clicked = script.getFinger().tap(tilePoly, hook);

            if (clicked && selectedAction.get() != null) {
                script.log("RETURN", "Clicked walk here on thieving tile");
            } else {
                // fallback to walker
                script.log("RETURN", "Walk click failed, using walker...");
                script.getWalker().walkTo(THIEVING_TILE);
            }
        }

        // wait until we reach thieving tile
        script.pollFramesUntil(() -> isAtThievingTile(), 3000);

        // humanized delay after arriving (not critical)
        script.pollFramesHuman(() -> false, script.random(300, 600));

        script.log("RETURN", "Back at thieving tile, ready to resume");
        return true;
    }

    /**
     * menu hook that only selects "walk here" action
     */
    private static MenuHook getWalkHereMenuHook(AtomicReference<String> selected) {
        return menuEntries -> {
            selected.set(null);

            if (menuEntries == null) return null;

            for (MenuEntry entry : menuEntries) {
                String action = entry.getAction().toLowerCase();

                if (action.contains("walk")) {
                    selected.set(entry.getAction());
                    return entry;
                }
            }

            return null;
        };
    }

    private boolean isAtThievingTile() {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;
        return THIEVING_AREA.contains(current);
    }
}
