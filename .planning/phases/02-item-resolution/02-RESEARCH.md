# Phase 2: Item Resolution - Research

**Researched:** 2026-01-15
**Domain:** OSRS Item lookup, sprite resolution, degradable item matching (Java/OSMB)
**Confidence:** HIGH

<research_summary>
## Summary

Researched item resolution strategies for the OSMB loadout utility. The standard approach uses OSMB's built-in ItemManager as the primary source for sprites and item lookups, with the official Jagex OSRS Grand Exchange API as a fallback for gaps.

Key finding: OSMB ItemManager provides ID-to-name lookup (`getItemName()`), item sprites (`getItem()`), and images (`getItemImage()`), but **does NOT provide name-to-ID lookup**. This gap must be addressed by building a local mapping from RuneLite's ItemID constants or by querying the official OSRS GE API.

For degradable items (Barrows, Crystal), each charge state has a unique item ID. The loadout's `fuzzy` flag enables matching any charge variant. Implementation should build variant groups mapping base items to all degraded IDs.

**Primary recommendation:** Use OSMB ItemManager for sprites/lookups where possible. Build local variant mapping from RuneLite ItemID constants for degradable matching. Use Jagex GE API only for missing items (it has rate limits and returns GE-tradeable items only).
</research_summary>

<standard_stack>
## Standard Stack

The established tools/APIs for OSRS item resolution:

### Core
| Component | Access | Purpose | Why Standard |
|-----------|--------|---------|--------------|
| OSMB ItemManager | `script.getItemManager()` | Item sprites, names, definitions | Built into OSMB, fast, no network calls |
| RuneLite ItemID | Compile-time constants | Item ID constants | Authoritative, complete coverage |
| Jagex OSRS GE API | HTTP REST | Fallback for sprites/data | Official, includes icons |

### Supporting
| Component | Purpose | When to Use |
|-----------|---------|-------------|
| OSMB SpriteManager | UI sprites | Button icons, interface elements |
| osrsbox-db (static JSON) | Bulk item data | Pre-building local item database |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Jagex GE API | osrsbox-api | osrsbox appears unmaintained since 2022 |
| Local variant map | Wiki API queries | Wiki requires parsing wikitext, slower |
| RuneLite ItemID | OSMB getItemDefinitions() | ItemID is compile-time safe, easier |

### API Endpoints

**OSMB ItemManager (local, no network):**
```java
// ID to name
String name = script.getItemManager().getItemName(itemId);

// ID to sprite (for visual matching)
SearchableItem[] items = script.getItemManager().getItem(itemId, false);

// ID to rendered image (for UI display)
Image image = script.getItemManager().getItemImage(itemId, 1, ZoomType.ZOOM_IN, backgroundColor);

// Check if stackable
boolean stackable = script.getItemManager().isStackable(itemId);

// Get all definitions (for building local database)
List<ItemDefinition> defs = script.getItemManager().getItemDefinitions();
```

**Jagex OSRS Grand Exchange API (network, rate-limited):**
```
// Item sprite (inventory icon)
GET https://secure.runescape.com/m=itemdb_oldschool/obj_sprite.gif?id={itemId}

// Item sprite (large)
GET https://secure.runescape.com/m=itemdb_oldschool/obj_big.gif?id={itemId}

// Item details (price, name, description)
GET https://secure.runescape.com/m=itemdb_oldschool/api/catalogue/detail.json?item={itemId}

// Browse items by letter
GET https://secure.runescape.com/m=itemdb_oldschool/api/catalogue/items.json?category=1&alpha={letter}&page={page}
```
</standard_stack>

<architecture_patterns>
## Architecture Patterns

### Recommended Project Structure
```
utilities/src/main/java/utilities/
├── loadout/                    # Existing data model (Phase 1)
│   ├── LoadoutItem.java
│   ├── Loadout.java
│   └── QuantityMode.java
└── items/                      # New item resolution (Phase 2)
    ├── ItemResolver.java       # Main entry point for lookups
    ├── ItemVariantMap.java     # Degradable variant grouping
    ├── SpriteCache.java        # Sprite caching layer
    └── WikiFallback.java       # Fallback for OSMB gaps (optional)
```

### Pattern 1: Layered Resolution Strategy
**What:** Try OSMB first, fallback to network APIs only when needed
**When to use:** Every item lookup
**Example:**
```java
// Source: Best practice for OSMB scripts
public class ItemResolver {
    private final ScriptCore script;
    private final Map<Integer, Image> spriteCache = new HashMap<>();

    public Image getSprite(int itemId) {
        // layer 1: cache
        if (spriteCache.containsKey(itemId)) {
            return spriteCache.get(itemId);
        }

        // layer 2: OSMB ItemManager (local, fast)
        Image sprite = script.getItemManager().getItemImage(
            itemId, 1, ZoomType.ZOOM_IN, null);
        if (sprite != null) {
            spriteCache.put(itemId, sprite);
            return sprite;
        }

        // layer 3: Jagex GE API (network, rate-limited)
        sprite = fetchFromGEApi(itemId);
        if (sprite != null) {
            spriteCache.put(itemId, sprite);
        }
        return sprite;
    }
}
```

### Pattern 2: Static Variant Mapping
**What:** Pre-build map of degradable item relationships at class load time
**When to use:** For fuzzy matching on Barrows/Crystal/etc items
**Example:**
```java
// Source: Derived from RuneLite ItemID patterns
public class ItemVariantMap {
    // map from any variant ID to base ID
    private static final Map<Integer, Integer> VARIANT_TO_BASE = new HashMap<>();

    // map from base ID to all variant IDs (ordered by charge, highest first)
    private static final Map<Integer, int[]> BASE_TO_VARIANTS = new HashMap<>();

    static {
        // Dharok's platebody: 4720 (base), 4892-4896 (100,75,50,25,0)
        registerVariants(4720, 4720, 4892, 4893, 4894, 4895, 4896);

        // Dharok's platelegs: 4722 (base), 4898-4902
        registerVariants(4722, 4722, 4898, 4899, 4900, 4901, 4902);

        // ... all Barrows items
        // ... Crystal items (10 variants each: full, 9/10, 8/10, ..., 1/10)
    }

    private static void registerVariants(int baseId, int... variants) {
        BASE_TO_VARIANTS.put(baseId, variants);
        for (int variant : variants) {
            VARIANT_TO_BASE.put(variant, baseId);
        }
    }

    public static int getBaseId(int itemId) {
        return VARIANT_TO_BASE.getOrDefault(itemId, itemId);
    }

    public static int[] getAllVariants(int itemId) {
        int baseId = getBaseId(itemId);
        return BASE_TO_VARIANTS.getOrDefault(baseId, new int[]{itemId});
    }

    public static int getPreferredVariant(int itemId) {
        // return highest charge variant (first in array)
        int[] variants = getAllVariants(itemId);
        return variants[0];
    }
}
```

### Pattern 3: Name-to-ID Lookup via Static Constants
**What:** Use RuneLite ItemID constants for compile-time safe lookups
**When to use:** When you know the item name at compile time
**Example:**
```java
// Source: RuneLite ItemID.java
import com.osmb.api.item.ItemID;

// direct constant access (preferred)
int abyssalWhipId = ItemID.ABYSSAL_WHIP;  // 4151

// for runtime name lookup, build a reverse map at startup
Map<String, Integer> NAME_TO_ID = new HashMap<>();
static {
    // populate from OSMB getItemDefinitions()
    for (ItemDefinition def : script.getItemManager().getItemDefinitions()) {
        NAME_TO_ID.put(def.getName().toLowerCase(), def.getId());
    }
}
```

### Anti-Patterns to Avoid
- **Querying Wiki for every lookup:** Slow, requires HTML/wikitext parsing, unreliable
- **Using osrsbox-api in production:** Unmaintained since 2022, data is stale
- **Building variant map at runtime from ItemID names:** Parsing "DHAROKS_PLATEBODY_100" is fragile; hardcode the mappings
- **Not caching sprites:** Network calls for each sprite will kill performance
</architecture_patterns>

<dont_hand_roll>
## Don't Hand-Roll

Problems that have existing solutions:

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Item sprites | Custom image downloads | `ItemManager.getItemImage()` | OSMB caches and renders correctly |
| Item name lookup | Custom database | `ItemManager.getItemName()` | Already in OSMB |
| Visual item matching | Pixel comparison | `ItemManager.getItem()` → `SearchableItem` | Handles color models, tolerances |
| Item ID constants | Scrape from Wiki | RuneLite `ItemID.java` | Authoritative, compile-time safe |
| Degradable ID patterns | Parse item names | Static mapping (hardcoded) | Pattern is stable, rarely changes |
| Sprite rendering | Custom drawing | `ItemManager.getItemImage()` | Handles borders, backgrounds, zoom |

**Key insight:** OSMB's ItemManager handles the hard parts of item resolution (sprite loading, visual matching, color models). The only gap is name-to-ID lookup, which is solved by using RuneLite constants or building a one-time reverse map from `getItemDefinitions()`.
</dont_hand_roll>

<common_pitfalls>
## Common Pitfalls

### Pitfall 1: No Name-to-ID Method in OSMB
**What goes wrong:** Trying to call `getItemId("Shark")` — method doesn't exist
**Why it happens:** OSMB provides ID-to-name but not the reverse
**How to avoid:** Build a reverse map at startup from `getItemDefinitions()`, or use RuneLite ItemID constants
**Warning signs:** Can't find method, autocomplete fails

### Pitfall 2: Rate Limiting on Jagex GE API
**What goes wrong:** API returns errors or empty responses after many requests
**Why it happens:** Official API has undocumented rate limits
**How to avoid:** Cache aggressively, batch requests, use OSMB first
**Warning signs:** Intermittent failures, empty responses after working initially

### Pitfall 3: GE API Only Has Tradeable Items
**What goes wrong:** Query for quest items, untradeable gear returns nothing
**Why it happens:** Grand Exchange API only indexes GE-tradeable items
**How to avoid:** Use OSMB ItemManager for untradeables, GE API only for sprite fallback
**Warning signs:** Missing sprites for quest rewards, ironman items

### Pitfall 4: Degradable Items Have 6+ Different IDs
**What goes wrong:** Loadout doesn't match equipped Dharok's platebody because it's at 75% charge
**Why it happens:** Each charge state is a unique item ID (4720, 4892, 4893, 4894, 4895, 4896)
**How to avoid:** Build variant mapping, use fuzzy flag, check all variants when matching
**Warning signs:** Equipment not recognized after combat use

### Pitfall 5: Crystal Equipment Has 10+ Variants
**What goes wrong:** Crystal bow matching fails after some use
**Why it happens:** Crystal items degrade in 10% increments (full, 9/10, 8/10, ... 1/10)
**How to avoid:** Include all 10-11 variants in mapping (new, full, and 9 degradation states)
**Warning signs:** Equipment not recognized after partial use
</common_pitfalls>

<code_examples>
## Code Examples

Verified patterns from OSMB docs and existing scripts:

### Get Item Sprite for UI Display
```java
// Source: OSMB ItemManager docs
public Image getItemSprite(int itemId) {
    // getItemImage handles all rendering
    return script.getItemManager().getItemImage(
        itemId,
        1,  // quantity (for stack display)
        ZoomType.ZOOM_IN,
        null  // background color (null = transparent)
    );
}
```

### Find Item in Bank by ID (with visual matching)
```java
// Source: BankSearchUtils.java in TidalsUtilities
public ItemSearchResult findItemInBank(int itemId, Rectangle bankBounds) {
    SearchableItem[] searchableItems = script.getItemManager().getItem(itemId, false);
    if (searchableItems == null || searchableItems.length == 0) {
        script.log(getClass(), "could not get searchable item for id: " + itemId);
        return null;
    }

    // try all variants (different contexts need different color models)
    for (SearchableItem item : searchableItems) {
        ItemSearchResult result = script.getItemManager().findLocation(false, bankBounds, item);
        if (result != null) {
            return result;
        }
    }
    return null;
}
```

### Build Name-to-ID Map at Startup
```java
// Source: Derived from OSMB API
public class ItemNameResolver {
    private final Map<String, Integer> nameToId = new HashMap<>();

    public ItemNameResolver(ScriptCore script) {
        List<ItemDefinition> defs = script.getItemManager().getItemDefinitions();
        for (ItemDefinition def : defs) {
            if (def.getName() != null && !def.getName().isEmpty()) {
                nameToId.put(def.getName().toLowerCase(), def.getId());
            }
        }
    }

    public Integer getItemId(String name) {
        return nameToId.get(name.toLowerCase());
    }
}
```

### Fetch Sprite from Jagex GE API (Fallback)
```java
// Source: Jagex GE API documentation
public Image fetchSpriteFromGE(int itemId) {
    try {
        String url = "https://secure.runescape.com/m=itemdb_oldschool/obj_sprite.gif?id=" + itemId;
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "OSMB-Script");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (conn.getResponseCode() == 200) {
            return ImageIO.read(conn.getInputStream());
        }
    } catch (Exception e) {
        // network error, return null to fall back
    }
    return null;
}
```

### Check if Items are Variant Matches
```java
// Source: Loadout fuzzy matching requirement
public boolean isVariantMatch(int loadoutItemId, int actualItemId) {
    if (loadoutItemId == actualItemId) {
        return true;
    }

    // check if they share the same base item
    int loadoutBase = ItemVariantMap.getBaseId(loadoutItemId);
    int actualBase = ItemVariantMap.getBaseId(actualItemId);

    return loadoutBase == actualBase;
}
```
</code_examples>

<sota_updates>
## State of the Art (2024-2026)

What's changed recently:

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| osrsbox-api for item data | Direct Jagex GE API | 2022+ | osrsbox unmaintained, use official API |
| Scraping Wiki for IDs | RuneLite ItemID constants | Always | Constants are authoritative |
| Network calls for every sprite | OSMB ItemManager caching | Built-in | Much faster, no rate limits |

**New tools/patterns to consider:**
- **OSMB ItemDefinition iteration:** Can build full item database locally at startup
- **Jagex obj_sprite.gif endpoint:** Direct sprite access without API parsing

**Deprecated/outdated:**
- **osrsbox-api (api.osrsbox.com):** Unmaintained since ~2022, data is stale
- **osrsbox PyPI package:** Last update years ago, don't rely on it
- **Scraping OSRS Wiki HTML:** Too fragile, use structured APIs instead
</sota_updates>

<open_questions>
## Open Questions

Things that couldn't be fully resolved:

1. **Rate limits on Jagex GE API**
   - What we know: Official API exists, returns item data and sprites
   - What's unclear: Exact rate limits, throttling behavior
   - Recommendation: Cache aggressively, use OSMB first, test under load

2. **Complete list of all degradable items**
   - What we know: Barrows (6 variants each), Crystal (10+ variants), Serpentine helm, Blowpipe charges
   - What's unclear: Full exhaustive list of all degradable item ID mappings
   - Recommendation: Build mapping incrementally, start with Barrows/Crystal, add others as needed

3. **OSMB ItemManager coverage gaps**
   - What we know: Most items have sprites available
   - What's unclear: Which specific items (if any) are missing from OSMB's cache
   - Recommendation: Build fallback to GE API, log any misses for investigation
</open_questions>

<sources>
## Sources

### Primary (HIGH confidence)
- OSMB ItemManager API - docs/ItemManager.md, docs/api-reference.md
- TidalsUtilities BankSearchUtils.java - existing usage patterns
- Jagex OSRS GE API - https://secure.runescape.com/m=itemdb_oldschool/api/

### Secondary (MEDIUM confidence)
- RuneScape Wiki API documentation - https://runescape.wiki/w/Application_programming_interface
- RuneLite ItemID.java - https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/ItemID.java
- OSRS Wiki Dharok's platebody - item ID mapping verified

### Tertiary (LOW confidence - needs validation)
- osrsbox-db static JSON - may be outdated, verify before using
</sources>

<metadata>
## Metadata

**Research scope:**
- Core technology: OSMB ItemManager API, Jagex GE API
- Ecosystem: RuneLite ItemID constants, OSRS Wiki
- Patterns: Layered resolution, variant mapping, caching
- Pitfalls: Rate limits, missing name-to-ID, degradable variants

**Confidence breakdown:**
- Standard stack: HIGH - OSMB docs verified, GE API tested
- Architecture: HIGH - patterns from existing TidalsUtilities code
- Pitfalls: HIGH - discovered from OSMB limitations and project requirements
- Code examples: HIGH - adapted from working BankSearchUtils

**Research date:** 2026-01-15
**Valid until:** 2026-02-15 (30 days - OSMB/Jagex APIs stable)
</metadata>

---

*Phase: 02-item-resolution*
*Research completed: 2026-01-15*
*Ready for planning: yes*
