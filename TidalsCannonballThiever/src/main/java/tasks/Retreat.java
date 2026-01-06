package tasks;

import com.osmb.api.input.MenuEntry;
import com.osmb.api.input.MenuHook;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import utils.Task;

import java.util.concurrent.atomic.AtomicReference;

import static main.TidalsCannonballThiever.*;

public class Retreat extends Task {
    private static final WorldPosition WALKBACK_TILE = new WorldPosition(1867, 3299, 0);

    public Retreat(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // highest priority - activate if any npc in danger zone and we're not already at walkback
        return !isAtWalkbackTile() && guardTracker.isAnyGuardInDangerZone();
    }

    @Override
    public boolean execute() {
        task = "Retreating from guard!";
        currentlyThieving = false; // stop thieving, we need to retreat
        script.log("RETREAT", "DANGER! NPC detected in danger zone, retreating immediately!");

        // get tile polygon for direct click (more reliable for short distances)
        // height 0 gives flat tile surface
        Polygon tilePoly = script.getSceneProjector().getTileCube(WALKBACK_TILE, 0);

        if (tilePoly == null) {
            // fallback to walker if tile not visible
            script.log("RETREAT", "Tile not visible, using walker...");
            script.getWalker().walkTo(WALKBACK_TILE);
        } else {
            // use menu hook to click "walk here" on tile
            AtomicReference<String> selectedAction = new AtomicReference<>(null);
            MenuHook hook = getWalkHereMenuHook(selectedAction);

            boolean clicked = script.getFinger().tap(tilePoly, hook);

            if (clicked && selectedAction.get() != null) {
                script.log("RETREAT", "Clicked walk here on safety tile");
            } else {
                // fallback to walker
                script.log("RETREAT", "Walk click failed, using walker...");
                script.getWalker().walkTo(WALKBACK_TILE);
            }
        }

        // wait until we reach walkback tile (no humanization - critical)
        script.pollFramesUntil(() -> isAtWalkbackTile(), 3000);

        script.log("RETREAT", "Reached walkback tile safely");
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

    private boolean isAtWalkbackTile() {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;
        // exact match only - walkback is 1867,3299, thieving is 1867,3298
        // can't use tolerance since they're only 1 tile apart
        int x = (int) current.getX();
        int y = (int) current.getY();
        return x == 1867 && y == 3299 && current.getPlane() == 0;
    }
}
