# Sprite Fallback Implementation Strategy

## Problem Statement

OSMB is a color bot that relies on visual detection and item ID mappings. Occasionally, items (especially jewelry with charges) are missing from OSMB's internal item database. When scripts attempt to interact with these missing items using standard APIs like `inventory.search(itemId)` or `bank.contains(itemId)`, they receive an `ArrayIndexOutOfBoundsException` because the item's sprite data doesn't exist.

**Affected Items (Known)**:
- Games necklace (ID: 3867)
- Ring of dueling (ID: 2566)
- Necklace of passage (ID: 21155)
- Digsite pendant (ID: 11190)
- Burning amulet (ID: 21175)
- Ring of wealth (uncharged) (ID: 2572)
- Skills necklace (uncharged) (ID: 11113)

**Error Signature**:
```
java.lang.ArrayIndexOutOfBoundsException: Index X out of bounds for length Y
    at com.osmb.api.item.ItemManager.getSprite(...)
```

## Solution Overview

Implement a **sprite-based fallback system** in TidalsUtilities that:
1. Bundles PNG sprites for known-problematic items as JAR resources
2. Uses OSMB's `ImageAnalyzer` for pixel-perfect visual matching
3. Provides a drop-in replacement API that mirrors standard item search methods
4. Gracefully falls back to standard ID-based search when sprites aren't available

## Architecture Design

### Module Structure

```
utilities/
├── src/main/java/utilities/
│   ├── sprites/
│   │   ├── SpriteLoader.java          # Load PNGs from JAR resources
│   │   ├── SpriteSearch.java          # Visual item detection
│   │   └── SpriteFallbackRegistry.java # Tracks items requiring fallback
│   └── ... (existing utils)
└── src/main/resources/
    └── sprites/
        └── items/
            ├── games_necklace.png
            ├── ring_of_dueling.png
            ├── necklace_of_passage.png
            ├── digsite_pendant.png
            ├── burning_amulet.png
            ├── ring_of_wealth.png
            └── skills_necklace.png
```

### Component Responsibilities

| Component | Responsibility |
|-----------|---------------|
| `SpriteLoader` | Load PNG resources from JAR, cache in memory |
| `SpriteSearch` | Execute visual searches in inventory/bank bounds |
| `SpriteFallbackRegistry` | Map item IDs to sprite names, track which items need fallback |

## Implementation Details

### 1. SpriteLoader

Handles loading PNG images from embedded JAR resources.

```java
package utilities.sprites;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SpriteLoader {

    private static final String SPRITE_PATH = "/sprites/items/";
    private static final Map<String, BufferedImage> SPRITE_CACHE = new HashMap<>();

    /**
     * Load a sprite from JAR resources by name.
     * Automatically appends .png extension.
     *
     * @param spriteName Name of sprite (e.g., "games_necklace")
     * @return BufferedImage or null if not found
     */
    public static BufferedImage loadSprite(String spriteName) {
        // Check cache first
        if (SPRITE_CACHE.containsKey(spriteName)) {
            return SPRITE_CACHE.get(spriteName);
        }

        String resourcePath = SPRITE_PATH + spriteName + ".png";
        try (InputStream is = SpriteLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[SpriteLoader] Sprite not found: " + resourcePath);
                return null;
            }

            BufferedImage sprite = ImageIO.read(is);
            SPRITE_CACHE.put(spriteName, sprite);
            return sprite;

        } catch (Exception e) {
            System.err.println("[SpriteLoader] Failed to load sprite: " + spriteName);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if a sprite exists without loading it.
     */
    public static boolean spriteExists(String spriteName) {
        if (SPRITE_CACHE.containsKey(spriteName)) {
            return true;
        }

        String resourcePath = SPRITE_PATH + spriteName + ".png";
        try (InputStream is = SpriteLoader.class.getResourceAsStream(resourcePath)) {
            return is != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Preload all known sprites into cache.
     * Call during script startup for faster runtime performance.
     */
    public static void preloadAll() {
        for (String spriteName : SpriteFallbackRegistry.getAllSpriteNames()) {
            loadSprite(spriteName);
        }
    }

    /**
     * Clear the sprite cache (useful for testing).
     */
    public static void clearCache() {
        SPRITE_CACHE.clear();
    }
}
```

### 2. SpriteFallbackRegistry

Central registry mapping item IDs to sprite names.

```java
package utilities.sprites;

import java.util.*;

public class SpriteFallbackRegistry {

    // Map of item ID -> sprite name (lowercase, underscored)
    private static final Map<Integer, String> FALLBACK_ITEMS = new HashMap<>();

    static {
        // Enchanted jewelry missing from OSMB
        register(3853, "games_necklace");        // Games necklace(8) through (1)
        register(3855, "games_necklace");
        register(3857, "games_necklace");
        register(3859, "games_necklace");
        register(3861, "games_necklace");
        register(3863, "games_necklace");
        register(3865, "games_necklace");
        register(3867, "games_necklace");

        register(2552, "ring_of_dueling");       // Ring of dueling(8) through (1)
        register(2554, "ring_of_dueling");
        register(2556, "ring_of_dueling");
        register(2558, "ring_of_dueling");
        register(2560, "ring_of_dueling");
        register(2562, "ring_of_dueling");
        register(2564, "ring_of_dueling");
        register(2566, "ring_of_dueling");

        register(21146, "necklace_of_passage");  // Necklace of passage(5) through (1)
        register(21149, "necklace_of_passage");
        register(21152, "necklace_of_passage");
        register(21155, "necklace_of_passage");
        register(21158, "necklace_of_passage");

        register(11190, "digsite_pendant");      // Digsite pendant(5) through (1)
        register(11191, "digsite_pendant");
        register(11192, "digsite_pendant");
        register(11193, "digsite_pendant");
        register(11194, "digsite_pendant");

        register(21166, "burning_amulet");       // Burning amulet(5) through (1)
        register(21169, "burning_amulet");
        register(21172, "burning_amulet");
        register(21175, "burning_amulet");
        register(21178, "burning_amulet");

        register(2572, "ring_of_wealth");        // Ring of wealth (uncharged)
        register(11980, "ring_of_wealth");       // Ring of wealth (1) through (5)
        register(11982, "ring_of_wealth");
        register(11984, "ring_of_wealth");
        register(11986, "ring_of_wealth");
        register(11988, "ring_of_wealth");

        register(11113, "skills_necklace");      // Skills necklace (uncharged)
        register(11105, "skills_necklace");      // Skills necklace(6) through (1)
        register(11107, "skills_necklace");
        register(11109, "skills_necklace");
        register(11111, "skills_necklace");
    }

    private static void register(int itemId, String spriteName) {
        FALLBACK_ITEMS.put(itemId, spriteName);
    }

    /**
     * Check if an item ID requires sprite-based fallback.
     */
    public static boolean requiresFallback(int itemId) {
        return FALLBACK_ITEMS.containsKey(itemId);
    }

    /**
     * Get the sprite name for an item ID.
     * @return sprite name or null if not registered
     */
    public static String getSpriteName(int itemId) {
        return FALLBACK_ITEMS.get(itemId);
    }

    /**
     * Get all registered sprite names (for preloading).
     */
    public static Set<String> getAllSpriteNames() {
        return new HashSet<>(FALLBACK_ITEMS.values());
    }

    /**
     * Register a custom item/sprite mapping at runtime.
     * Useful for script-specific items that may be missing.
     */
    public static void registerCustom(int itemId, String spriteName) {
        FALLBACK_ITEMS.put(itemId, spriteName);
    }

    /**
     * Generate standard sprite name from item name.
     * "Games necklace" -> "games_necklace"
     */
    public static String toSpriteName(String itemName) {
        return itemName.toLowerCase()
            .replace(" ", "_")
            .replace("'", "")
            .replaceAll("\\([0-9]+\\)", "")  // Remove charge numbers
            .trim();
    }
}
```

### 3. SpriteSearch

Core visual search using OSMB's ImageAnalyzer.

```java
package utilities.sprites;

import com.osmb.api.Script;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.visual.image.ColorModel;
import com.osmb.api.visual.image.ImageSearchResult;
import com.osmb.api.visual.image.SearchableImage;
import com.osmb.api.visual.image.ToleranceComparator;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public class SpriteSearch {

    // ==================== INVENTORY METHODS ====================

    /**
     * Check if inventory contains an item by sprite.
     * Opens inventory tab if not already open.
     */
    public static boolean hasInInventory(Script script, String spriteName) {
        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            script.log("[SpriteSearch] Warning: Sprite not found: " + spriteName);
            return false;
        }

        // Ensure inventory is open
        if (!script.getWidgetManager().getInventory().isVisible()) {
            script.getWidgetManager().getTabs().openTab(
                com.osmb.api.ui.tabs.Tab.INVENTORY
            );
            script.pollFramesUntil(() ->
                script.getWidgetManager().getInventory().isVisible(), 2000);
        }

        Rectangle invBounds = getInventoryBounds(script);
        return findSpriteInBounds(script, sprite, invBounds) != null;
    }

    /**
     * Count items in inventory by sprite.
     * Note: Returns number of slots containing the item, not stack count.
     */
    public static int countInInventory(Script script, String spriteName) {
        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            script.log("[SpriteSearch] Warning: Sprite not found: " + spriteName);
            return 0;
        }

        // Ensure inventory is open
        if (!script.getWidgetManager().getInventory().isVisible()) {
            script.getWidgetManager().getTabs().openTab(
                com.osmb.api.ui.tabs.Tab.INVENTORY
            );
            script.pollFramesUntil(() ->
                script.getWidgetManager().getInventory().isVisible(), 2000);
        }

        Rectangle invBounds = getInventoryBounds(script);
        return findAllSpritesInBounds(script, sprite, invBounds).size();
    }

    /**
     * Find first matching item in inventory and return its bounds.
     */
    public static Rectangle findInInventory(Script script, String spriteName) {
        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            script.log("[SpriteSearch] Warning: Sprite not found: " + spriteName);
            return null;
        }

        // Ensure inventory is open
        if (!script.getWidgetManager().getInventory().isVisible()) {
            script.getWidgetManager().getTabs().openTab(
                com.osmb.api.ui.tabs.Tab.INVENTORY
            );
            script.pollFramesUntil(() ->
                script.getWidgetManager().getInventory().isVisible(), 2000);
        }

        Rectangle invBounds = getInventoryBounds(script);
        ImageSearchResult result = findSpriteInBounds(script, sprite, invBounds);

        if (result != null) {
            return new Rectangle(
                result.getX(), result.getY(),
                sprite.getWidth(), sprite.getHeight()
            );
        }
        return null;
    }

    // ==================== BANK METHODS ====================

    /**
     * Check if bank contains an item by sprite.
     * Bank must already be open.
     */
    public static boolean hasInBank(Script script, String spriteName) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log("[SpriteSearch] Bank is not visible");
            return false;
        }

        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            script.log("[SpriteSearch] Warning: Sprite not found: " + spriteName);
            return false;
        }

        Rectangle bankBounds = getBankBounds(script);
        return findSpriteInBounds(script, sprite, bankBounds) != null;
    }

    /**
     * Count items in visible bank area by sprite.
     * Note: Returns number of slots, not stack count.
     */
    public static int countInBank(Script script, String spriteName) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log("[SpriteSearch] Bank is not visible");
            return 0;
        }

        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            script.log("[SpriteSearch] Warning: Sprite not found: " + spriteName);
            return 0;
        }

        Rectangle bankBounds = getBankBounds(script);
        return findAllSpritesInBounds(script, sprite, bankBounds).size();
    }

    /**
     * Find first matching item in bank and return its bounds.
     */
    public static Rectangle findInBank(Script script, String spriteName) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log("[SpriteSearch] Bank is not visible");
            return null;
        }

        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            script.log("[SpriteSearch] Warning: Sprite not found: " + spriteName);
            return null;
        }

        Rectangle bankBounds = getBankBounds(script);
        ImageSearchResult result = findSpriteInBounds(script, sprite, bankBounds);

        if (result != null) {
            return new Rectangle(
                result.getX(), result.getY(),
                sprite.getWidth(), sprite.getHeight()
            );
        }
        return null;
    }

    // ==================== EQUIPMENT METHODS ====================

    /**
     * Check if equipment slot contains an item by sprite.
     */
    public static boolean hasEquipped(Script script, String spriteName) {
        if (!script.getWidgetManager().getEquipment().isVisible()) {
            script.getWidgetManager().getTabs().openTab(
                com.osmb.api.ui.tabs.Tab.EQUIPMENT
            );
            script.pollFramesUntil(() ->
                script.getWidgetManager().getEquipment().isVisible(), 2000);
        }

        BufferedImage sprite = SpriteLoader.loadSprite(spriteName);
        if (sprite == null) {
            script.log("[SpriteSearch] Warning: Sprite not found: " + spriteName);
            return false;
        }

        // Equipment tab bounds
        Rectangle equipBounds = getEquipmentBounds(script);
        return findSpriteInBounds(script, sprite, equipBounds) != null;
    }

    // ==================== CORE SEARCH IMPLEMENTATION ====================

    /**
     * Find first sprite match within bounds.
     */
    private static ImageSearchResult findSpriteInBounds(
            Script script, BufferedImage sprite, Rectangle bounds) {
        try {
            SearchableImage searchable = new SearchableImage(
                sprite,
                ToleranceComparator.ZERO_TOLERANCE,
                ColorModel.RGB
            );

            if (bounds != null) {
                return script.getImageAnalyzer().findLocation(bounds, searchable);
            } else {
                return script.getImageAnalyzer().findLocation(searchable);
            }
        } catch (Exception e) {
            System.err.println("[SpriteSearch] Search failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Find all sprite matches within bounds.
     */
    private static List<ImageSearchResult> findAllSpritesInBounds(
            Script script, BufferedImage sprite, Rectangle bounds) {
        try {
            SearchableImage searchable = new SearchableImage(
                sprite,
                ToleranceComparator.ZERO_TOLERANCE,
                ColorModel.RGB
            );

            if (bounds != null) {
                return script.getImageAnalyzer().findLocations(bounds, searchable);
            } else {
                return script.getImageAnalyzer().findLocations(searchable);
            }
        } catch (Exception e) {
            System.err.println("[SpriteSearch] Search failed: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==================== BOUNDS HELPERS ====================

    private static Rectangle getInventoryBounds(Script script) {
        try {
            // Try to get inventory widget bounds
            var inventory = script.getWidgetManager().getInventory();
            if (inventory != null) {
                return inventory.getBounds();
            }
        } catch (Exception e) {
            // Fall through to null
        }
        return null; // Will search entire screen
    }

    private static Rectangle getBankBounds(Script script) {
        try {
            var bank = script.getWidgetManager().getBank();
            if (bank != null) {
                return bank.getBounds();
            }
        } catch (Exception e) {
            // Fall through to null
        }
        return null;
    }

    private static Rectangle getEquipmentBounds(Script script) {
        try {
            var equipment = script.getWidgetManager().getEquipment();
            if (equipment != null) {
                return equipment.getBounds();
            }
        } catch (Exception e) {
            // Fall through to null
        }
        return null;
    }
}
```

### 4. Integration with Existing Utilities

Create a facade that seamlessly handles fallback.

```java
package utilities;

import com.osmb.api.Script;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.shape.Rectangle;
import utilities.sprites.SpriteFallbackRegistry;
import utilities.sprites.SpriteSearch;

/**
 * Item utilities with automatic sprite fallback for missing OSMB items.
 * Use these methods instead of raw inventory/bank APIs for problematic items.
 */
public class ItemFallbackUtils {

    /**
     * Search inventory for item with automatic sprite fallback.
     *
     * @return ItemSearchResult or null if not found
     */
    public static ItemSearchResult inventorySearch(Script script, int itemId) {
        // Try standard API first
        try {
            ItemSearchResult result = script.getWidgetManager()
                .getInventory().search(itemId);
            if (result != null) {
                return result;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Item missing from OSMB - fall through to sprite search
            script.log("[ItemFallbackUtils] Item ID " + itemId +
                " missing from OSMB, using sprite fallback");
        }

        // Sprite fallback
        if (SpriteFallbackRegistry.requiresFallback(itemId)) {
            String spriteName = SpriteFallbackRegistry.getSpriteName(itemId);
            Rectangle bounds = SpriteSearch.findInInventory(script, spriteName);
            if (bounds != null) {
                // Return a pseudo ItemSearchResult with the bounds
                // Note: This won't have full item metadata
                return createPseudoResult(itemId, bounds);
            }
        }

        return null;
    }

    /**
     * Check if inventory contains item with automatic sprite fallback.
     */
    public static boolean inventoryContains(Script script, int itemId) {
        try {
            ItemSearchResult result = script.getWidgetManager()
                .getInventory().search(itemId);
            if (result != null) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Fall through to sprite
        }

        if (SpriteFallbackRegistry.requiresFallback(itemId)) {
            String spriteName = SpriteFallbackRegistry.getSpriteName(itemId);
            return SpriteSearch.hasInInventory(script, spriteName);
        }

        return false;
    }

    /**
     * Count items in inventory with automatic sprite fallback.
     * Note: For sprite-based items, returns slot count not stack count.
     */
    public static int inventoryCount(Script script, int itemId) {
        try {
            var results = script.getWidgetManager()
                .getInventory().searchAll(itemId);
            if (results != null && !results.isEmpty()) {
                return results.size();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Fall through to sprite
        }

        if (SpriteFallbackRegistry.requiresFallback(itemId)) {
            String spriteName = SpriteFallbackRegistry.getSpriteName(itemId);
            return SpriteSearch.countInInventory(script, spriteName);
        }

        return 0;
    }

    /**
     * Check if bank contains item with automatic sprite fallback.
     * Bank must already be open.
     */
    public static boolean bankContains(Script script, int itemId) {
        try {
            return script.getWidgetManager().getBank().contains(itemId);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Fall through to sprite
        }

        if (SpriteFallbackRegistry.requiresFallback(itemId)) {
            String spriteName = SpriteFallbackRegistry.getSpriteName(itemId);
            return SpriteSearch.hasInBank(script, spriteName);
        }

        return false;
    }

    /**
     * Check if item is equipped with automatic sprite fallback.
     */
    public static boolean isEquipped(Script script, int itemId) {
        try {
            return script.getWidgetManager().getEquipment()
                .contains(itemId);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Fall through to sprite
        }

        if (SpriteFallbackRegistry.requiresFallback(itemId)) {
            String spriteName = SpriteFallbackRegistry.getSpriteName(itemId);
            return SpriteSearch.hasEquipped(script, spriteName);
        }

        return false;
    }

    // Helper to create a pseudo ItemSearchResult
    private static ItemSearchResult createPseudoResult(int itemId, Rectangle bounds) {
        // This is a simplified wrapper - actual implementation depends on OSMB API
        // You may need to create a custom wrapper class
        return new ItemSearchResult() {
            @Override
            public int getItemId() { return itemId; }

            @Override
            public Rectangle getBounds() { return bounds; }

            // Implement other required methods as no-ops or defaults
        };
    }
}
```

## Sprite Creation Guidelines

### Capturing Sprites

1. **Source**: Take screenshots from OSMB running in resizable mode
2. **Size**: Sprites should be exactly 36x32 pixels (inventory slot size)
3. **Background**: Capture with the default inventory background (dark brown)
4. **Format**: Save as PNG with no transparency modifications
5. **Naming**: Use `item_name.png` format (lowercase, underscores, no charges)

### Sprite Directory Structure

```
src/main/resources/sprites/items/
├── jewelry/
│   ├── games_necklace.png
│   ├── ring_of_dueling.png
│   └── ...
├── potions/           # Future expansion
├── tools/             # Future expansion
└── other/             # Miscellaneous
```

### Adding New Sprites

1. Capture the sprite PNG (36x32 pixels)
2. Save to `src/main/resources/sprites/items/`
3. Register in `SpriteFallbackRegistry`:
   ```java
   register(ITEM_ID, "sprite_name");
   ```
4. Rebuild utilities JAR

## Usage Examples

### Basic Usage in Scripts

```java
import utilities.ItemFallbackUtils;
import utilities.sprites.SpriteLoader;

public class MyScript extends Script {

    @Override
    public void onStart() {
        // Optional: Preload sprites for faster runtime
        SpriteLoader.preloadAll();
    }

    public boolean hasTeleportJewelry() {
        // Automatically uses sprite fallback if item is missing from OSMB
        return ItemFallbackUtils.inventoryContains(this, ItemID.GAMES_NECKLACE_8);
    }

    public void bankForJewelry() {
        if (ItemFallbackUtils.bankContains(this, ItemID.RING_OF_DUELING_8)) {
            // Withdraw using standard methods or sprite-based tap
            withdrawRingOfDueling();
        }
    }
}
```

### Direct Sprite Search (Advanced)

```java
import utilities.sprites.SpriteSearch;

// When you know you need sprite search
Rectangle itemBounds = SpriteSearch.findInInventory(script, "games_necklace");
if (itemBounds != null) {
    script.getFinger().tap(itemBounds, "Rub");
}

// Count items
int count = SpriteSearch.countInInventory(script, "ring_of_dueling");
script.log("Found " + count + " rings of dueling");
```

### Custom Item Registration

```java
import utilities.sprites.SpriteFallbackRegistry;

// Register a script-specific item that's missing from OSMB
SpriteFallbackRegistry.registerCustom(12345, "custom_item");

// Now ItemFallbackUtils will use sprite search for this item
boolean hasItem = ItemFallbackUtils.inventoryContains(script, 12345);
```

## Error Handling Strategy

### Layered Fallback

```
1. Standard OSMB API
   ├── Success → Return result
   └── ArrayIndexOutOfBoundsException → Continue to fallback

2. Sprite Fallback (if item registered)
   ├── Sprite found → Load and search
   │   ├── Match found → Return result
   │   └── No match → Return null/false
   └── Sprite not found → Log warning, return null/false

3. Graceful Degradation
   └── Never throw exceptions - always return null/false/0
```

### Logging

All fallback operations should log:
- When falling back to sprite search
- When sprites are not found
- Search results for debugging

```java
script.log("[ItemFallbackUtils] Item ID 3867 missing from OSMB, using sprite fallback");
script.log("[SpriteSearch] Warning: Sprite not found: unknown_item");
script.log("[SpriteSearch] Found 3 matches for games_necklace in inventory");
```

## Build Configuration

### Gradle Changes

```gradle
// In utilities/build.gradle

sourceSets {
    main {
        resources {
            srcDirs = ['src/main/resources']
        }
    }
}

jar {
    // Ensure resources are included in JAR
    from sourceSets.main.resources
}
```

### Resource Verification

After building, verify sprites are in JAR:
```bash
jar tf utilities/jar/TidalsUtilities.jar | grep sprites
# Should show:
# sprites/items/games_necklace.png
# sprites/items/ring_of_dueling.png
# ...
```

## Performance Considerations

| Operation | Time Complexity | Notes |
|-----------|-----------------|-------|
| Sprite load (first) | ~10-50ms | File I/O from JAR |
| Sprite load (cached) | O(1) | HashMap lookup |
| Image search | ~5-20ms | Depends on search area |
| Bounded search | Faster | Always prefer bounded |

### Optimizations

1. **Preload sprites** at script start for predictable performance
2. **Use bounded searches** - always constrain to inventory/bank bounds
3. **Cache search results** if checking same item repeatedly in a loop
4. **Limit full-screen searches** - they're slow and usually unnecessary

## Testing Checklist

- [ ] Sprites load correctly from JAR
- [ ] Cache prevents repeated file I/O
- [ ] Inventory search finds items
- [ ] Bank search finds items (when bank open)
- [ ] Equipment search finds items
- [ ] Fallback triggers on ArrayIndexOutOfBoundsException
- [ ] Missing sprites don't crash script
- [ ] Bounded searches work correctly
- [ ] Item counting works
- [ ] Custom registration works

## Future Enhancements

1. **Tolerance Matching**: Add configurable tolerance for lighting variations
2. **Multi-Item Sprites**: Support items with charge numbers in sprite
3. **Dynamic Sprite Download**: Fetch sprites from server when missing
4. **Sprite Verification**: Validate sprites match expected dimensions
5. **Integration with RetryUtils**: Add sprite-based tap methods with retries

## References

- Original solution: [druscripts/enchanter](https://github.com/druscripts/scripts/tree/master/src/com/druscripts/enchanter)
- OSMB ImageAnalyzer API: `com.osmb.api.visual.image.SearchableImage`
- Color matching: `ToleranceComparator.ZERO_TOLERANCE`, `ColorModel.RGB`
