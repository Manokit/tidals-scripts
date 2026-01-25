package utilities;

import com.osmb.api.script.Script;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.utils.RandomUtils;

import java.util.Set;

/**
 * Tab utilities for opening and verifying game tabs.
 *
 * Provides reliable tab opening with verification that the tab's
 * contents are accessible before returning.
 *
 * Usage:
 *   TabUtils.openAndVerifyInventory(script, 3000);
 *   TabUtils.openAndVerifyEquipment(script, 3000);
 *   TabUtils.openAndVerifySkills(script, 3000);
 */
public class TabUtils {

    private static final int DEFAULT_TIMEOUT = 3000;

    /**
     * Open the inventory tab and verify it's accessible.
     * Waits until inventory.search() returns a non-null result.
     *
     * @param script the script instance
     * @param timeout max time to wait in ms
     * @return true if inventory tab opened and is accessible
     */
    public static boolean openAndVerifyInventory(Script script, int timeout) {
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);

        return script.pollFramesUntil(() ->
            script.getWidgetManager().getInventory().search(Set.of()) != null,
            timeout
        );
    }

    /**
     * Open the inventory tab with default timeout (3000ms).
     *
     * @param script the script instance
     * @return true if inventory tab opened and is accessible
     */
    public static boolean openAndVerifyInventory(Script script) {
        return openAndVerifyInventory(script, DEFAULT_TIMEOUT);
    }

    /**
     * Open the equipment tab and wait for it to be ready.
     * Uses a human-like delay since equipment tab doesn't have a direct isVisible check.
     *
     * @param script the script instance
     * @param waitMs time to wait after opening (recommended 200-400ms)
     * @return true (always returns true after waiting)
     */
    public static boolean openAndWaitEquipment(Script script, int waitMs) {
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, waitMs);
        return true;
    }

    /**
     * Open the equipment tab with default wait (300ms).
     *
     * @param script the script instance
     * @return true (always returns true after waiting)
     */
    public static boolean openAndWaitEquipment(Script script) {
        return openAndWaitEquipment(script, RandomUtils.weightedRandom(200, 400));
    }

    /**
     * Open the equipment tab and verify by checking if an item can be found.
     * Useful when you expect a specific item to be equipped.
     *
     * @param script the script instance
     * @param itemIds item IDs to search for as verification
     * @param timeout max time to wait in ms
     * @return true if equipment tab opened and item was found
     */
    public static boolean openAndVerifyEquipment(Script script, int[] itemIds, int timeout) {
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(150, 300));

        return script.pollFramesUntil(() -> {
            for (int itemId : itemIds) {
                if (script.getWidgetManager().getEquipment().findItem(itemId).isFound()) {
                    return true;
                }
            }
            return false;
        }, timeout);
    }

    /**
     * Open the skills tab and verify it's accessible.
     * Waits until skill tab is not null.
     *
     * @param script the script instance
     * @param timeout max time to wait in ms
     * @return true if skills tab opened and is accessible
     */
    public static boolean openAndVerifySkills(Script script, int timeout) {
        script.getWidgetManager().getTabManager().openTab(Tab.Type.SKILLS);

        return script.pollFramesUntil(() ->
            script.getWidgetManager().getSkillTab() != null,
            timeout
        );
    }

    /**
     * Open the skills tab with default timeout (3000ms).
     *
     * @param script the script instance
     * @return true if skills tab opened and is accessible
     */
    public static boolean openAndVerifySkills(Script script) {
        return openAndVerifySkills(script, DEFAULT_TIMEOUT);
    }

    /**
     * Open any tab type and wait a fixed duration.
     * Use this for tabs without specific verification methods.
     *
     * @param script the script instance
     * @param tabType the tab type to open
     * @param waitMs time to wait after opening
     * @return true (always returns true after waiting)
     */
    public static boolean openTabAndWait(Script script, Tab.Type tabType, int waitMs) {
        script.getWidgetManager().getTabManager().openTab(tabType);
        script.pollFramesUntil(() -> false, waitMs);
        return true;
    }
}
