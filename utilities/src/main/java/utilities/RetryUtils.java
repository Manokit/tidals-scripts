package utilities;

import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;

/**
 * Retry utilities for menu interactions.
 *
 * ALWAYS use these for menu interactions unless speed is explicitly critical.
 * Default: 10 retry attempts with logging.
 *
 * Usage:
 *   RetryUtils.equipmentInteract(script, itemId, "Teleport", "crafting cape teleport");
 *   RetryUtils.objectInteract(script, bankChest, "Use", "bank chest");
 *   RetryUtils.tap(script, polygon, "Pick", "fungus");
 *   RetryUtils.inventoryInteract(script, item, "Eat", "food");
 */
public class RetryUtils {

    private static final int DEFAULT_MAX_ATTEMPTS = 10;
    private static final int RETRY_DELAY_MIN = 300;
    private static final int RETRY_DELAY_MAX = 500;

    /**
     * Retry equipment interaction up to maxAttempts times.
     * Logs each attempt as "description attempt X/Y".
     */
    public static boolean equipmentInteract(Script script, int itemId, String action, String description) {
        return equipmentInteract(script, itemId, action, description, DEFAULT_MAX_ATTEMPTS);
    }

    public static boolean equipmentInteract(Script script, int itemId, String action, String description, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            script.log(RetryUtils.class, description + " attempt " + attempt + "/" + maxAttempts);

            boolean success = script.getWidgetManager().getEquipment().interact(itemId, action);
            if (success) {
                return true;
            }

            script.pollFramesUntil(() -> false, script.random(RETRY_DELAY_MIN, RETRY_DELAY_MAX), true);
        }
        script.log(RetryUtils.class, description + " failed after " + maxAttempts + " attempts");
        return false;
    }

    /**
     * Retry object interaction up to maxAttempts times.
     * Logs each attempt as "description attempt X/Y".
     */
    public static boolean objectInteract(Script script, RSObject obj, String action, String description) {
        return objectInteract(script, obj, action, description, DEFAULT_MAX_ATTEMPTS);
    }

    public static boolean objectInteract(Script script, RSObject obj, String action, String description, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            script.log(RetryUtils.class, description + " attempt " + attempt + "/" + maxAttempts);

            boolean success = obj.interact(action);
            if (success) {
                return true;
            }

            script.pollFramesUntil(() -> false, script.random(RETRY_DELAY_MIN, RETRY_DELAY_MAX), true);
        }
        script.log(RetryUtils.class, description + " failed after " + maxAttempts + " attempts");
        return false;
    }

    public static boolean objectInteract(Script script, RSObject obj, String[] actions, String description) {
        return objectInteract(script, obj, actions, description, DEFAULT_MAX_ATTEMPTS);
    }

    public static boolean objectInteract(Script script, RSObject obj, String[] actions, String description, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            script.log(RetryUtils.class, description + " attempt " + attempt + "/" + maxAttempts);

            boolean success = obj.interact(actions);
            if (success) {
                return true;
            }

            script.pollFramesUntil(() -> false, script.random(RETRY_DELAY_MIN, RETRY_DELAY_MAX), true);
        }
        script.log(RetryUtils.class, description + " failed after " + maxAttempts + " attempts");
        return false;
    }

    /**
     * Retry polygon tap interaction up to maxAttempts times.
     * Logs each attempt as "description attempt X/Y".
     */
    public static boolean tap(Script script, Polygon poly, String action, String description) {
        return tap(script, poly, action, description, DEFAULT_MAX_ATTEMPTS);
    }

    public static boolean tap(Script script, Polygon poly, String action, String description, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            script.log(RetryUtils.class, description + " attempt " + attempt + "/" + maxAttempts);

            boolean success = script.getFinger().tap(poly, action);
            if (success) {
                return true;
            }

            script.pollFramesUntil(() -> false, script.random(RETRY_DELAY_MIN, RETRY_DELAY_MAX), true);
        }
        script.log(RetryUtils.class, description + " failed after " + maxAttempts + " attempts");
        return false;
    }

    /**
     * Retry inventory item interaction up to maxAttempts times.
     * Logs each attempt as "description attempt X/Y".
     */
    public static boolean inventoryInteract(Script script, ItemSearchResult item, String action, String description) {
        return inventoryInteract(script, item, action, description, DEFAULT_MAX_ATTEMPTS);
    }

    public static boolean inventoryInteract(Script script, ItemSearchResult item, String action, String description, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            script.log(RetryUtils.class, description + " attempt " + attempt + "/" + maxAttempts);

            boolean success = item.interact(action);
            if (success) {
                return true;
            }

            script.pollFramesUntil(() -> false, script.random(RETRY_DELAY_MIN, RETRY_DELAY_MAX), true);
        }
        script.log(RetryUtils.class, description + " failed after " + maxAttempts + " attempts");
        return false;
    }
}
