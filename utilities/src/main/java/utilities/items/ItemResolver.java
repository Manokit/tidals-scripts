package utilities.items;

import com.osmb.api.script.Script;
import com.osmb.api.visual.image.Image;
import utilities.loadout.LoadoutItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified interface for all item lookups.
 *
 * <p>Provides:
 * <ul>
 *   <li>Sprite resolution via SpriteCache</li>
 *   <li>Name lookup via OSMB ItemManager</li>
 *   <li>Name-to-ID reverse lookup (lazy initialized)</li>
 *   <li>Variant matching for degradable items via ItemVariantMap</li>
 * </ul>
 *
 * <p>The name-to-ID map is lazily initialized on first use to avoid
 * startup overhead if name lookups aren't needed.
 */
public final class ItemResolver {

    private final Script script;
    private final SpriteCache spriteCache;
    private final Map<String, Integer> nameToId;
    private boolean initialized;

    /**
     * Creates a new ItemResolver.
     *
     * @param script the script instance for ItemManager access
     */
    public ItemResolver(Script script) {
        this.script = script;
        this.spriteCache = new SpriteCache(script);
        this.nameToId = new HashMap<>();
        this.initialized = false;
    }

    /**
     * Ensures the name-to-ID map is built from ItemManager.
     * Iterates through common item ID range and maps names to IDs.
     * Called lazily on first name lookup.
     */
    private void ensureInitialized() {
        if (initialized) {
            return;
        }

        // OSRS item IDs range from 0 to about 30000
        // iterate and build reverse map from names we can retrieve
        for (int id = 0; id <= 30000; id++) {
            try {
                String name = script.getItemManager().getItemName(id);
                if (name != null && !name.isEmpty() && !name.equals("null")) {
                    // map lowercase name to ID for case-insensitive lookup
                    // note: multiple IDs may share a name; we keep the first
                    nameToId.putIfAbsent(name.toLowerCase(), id);
                }
            } catch (Exception e) {
                // skip invalid IDs
            }
        }

        initialized = true;
    }

    /**
     * Gets the sprite for an item ID.
     *
     * @param itemId the item ID
     * @return the sprite image, or null if not found
     */
    public Image getSprite(int itemId) {
        return spriteCache.getSprite(itemId);
    }

    /**
     * Gets the name for an item ID.
     *
     * @param itemId the item ID
     * @return the item name, or null if not found
     */
    public String getName(int itemId) {
        return script.getItemManager().getItemName(itemId);
    }

    /**
     * Gets the item ID for a name (case-insensitive).
     * Lazily initializes the name-to-ID map on first call.
     *
     * @param name the item name to look up
     * @return the item ID, or null if not found
     */
    public Integer getItemId(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        ensureInitialized();
        return nameToId.get(name.toLowerCase());
    }

    /**
     * Checks if two items are variants of the same base item.
     * Uses ItemVariantMap for degradable equipment matching.
     *
     * @param loadoutItemId the item ID from the loadout
     * @param actualItemId  the actual item ID found
     * @return true if items are variants of the same base
     */
    public boolean isVariantMatch(int loadoutItemId, int actualItemId) {
        return ItemVariantMap.areVariants(loadoutItemId, actualItemId);
    }

    /**
     * Gets the preferred (highest charge) variant for an item.
     *
     * @param itemId any variant ID of the item
     * @return the preferred variant ID
     */
    public int getPreferredVariant(int itemId) {
        return ItemVariantMap.getPreferredVariant(itemId);
    }

    /**
     * Gets all variant IDs for an item.
     *
     * @param itemId any variant ID of the item
     * @return array of all variant IDs (highest charge first)
     */
    public int[] getAllVariants(int itemId) {
        return ItemVariantMap.getAllVariants(itemId);
    }

    /**
     * Checks if an actual item matches a loadout item.
     *
     * <p>Returns true if:
     * <ul>
     *   <li>IDs match exactly, OR</li>
     *   <li>loadoutItem.isFuzzy() and items are variants of the same base</li>
     * </ul>
     *
     * @param loadoutItem  the loadout item specification
     * @param actualItemId the actual item ID found
     * @return true if the actual item satisfies the loadout requirement
     */
    public boolean matchesLoadoutItem(LoadoutItem loadoutItem, int actualItemId) {
        int loadoutItemId = loadoutItem.getItemId();

        // exact match always succeeds
        if (loadoutItemId == actualItemId) {
            return true;
        }

        // fuzzy matching checks for variant relationship
        if (loadoutItem.isFuzzy()) {
            return isVariantMatch(loadoutItemId, actualItemId);
        }

        return false;
    }

    /**
     * Gets the underlying SpriteCache for advanced usage.
     *
     * @return the sprite cache
     */
    public SpriteCache getSpriteCache() {
        return spriteCache;
    }
}
