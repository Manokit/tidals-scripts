# Phase 6: Tap-Based Search Activation - Research

**Researched:** 2026-01-14
**Domain:** OSMB sprite-based UI interaction (internal pattern)
**Confidence:** HIGH

<research_summary>
## Summary

This phase follows an established internal pattern (BankScrollUtils sprite-based detection) rather than requiring external ecosystem research. The approach is proven and documented in the codebase.

**Problem:** `openSearch()` uses `BACKSPACE` keyboard shortcut which doesn't actually activate the bank search box in OSRS/OSMB.

**Solution:** Replace keyboard shortcut with visual sprite-based tap on the SEARCH button, following the exact same pattern as BankScrollUtils uses for scroll buttons.

**Primary recommendation:** Copy BankScrollUtils pattern - load SEARCH button sprite via SpriteManager, find on screen via ImageAnalyzer, tap with Finger.
</research_summary>

<standard_stack>
## Standard Stack

This phase uses only internal OSMB APIs already in use:

### Core APIs
| API | Purpose | Example |
|-----|---------|---------|
| `SpriteManager` | Load button sprite by ID | `script.getSpriteManager().getSprite(SPRITE_ID, 0)` |
| `ImageAnalyzer` | Find sprite location on screen | `script.getImageAnalyzer().findLocations(searchableImage)` |
| `Finger` | Tap the button location | `script.getFinger().tap(location)` |
| `Dialogue` | Verify search activated | `dialogue.getDialogueType() == DialogueType.TEXT_SEARCH` |

### Supporting
| API | Purpose |
|-----|---------|
| `SingleThresholdComparator` | Color tolerance for sprite matching |
| `ColorModel.RGB` | Color model for image comparison |
| `Image.toSearchableImage()` | Convert sprite to searchable format |

### Installation
Already available - no new dependencies.
</standard_stack>

<architecture_patterns>
## Architecture Patterns

### Proven Pattern: BankScrollUtils Sprite Detection

The exact pattern to follow, from BankScrollUtils.java:

```java
// 1. Define sprite ID constants
private static final int SEARCH_BUTTON_SPRITE_ID = ???;  // TBD via debug tool

// 2. Lazy initialization with caching
private static SearchableImage searchButtonImage;
private static boolean initialized = false;

// 3. Init method loads sprite
public static boolean init(Script script) {
    if (initialized) return true;

    SingleThresholdComparator tolerance = new SingleThresholdComparator(15);

    SpriteDefinition sprite = script.getSpriteManager().getSprite(SPRITE_ID, 0);
    if (sprite == null) {
        script.log("sprite not found: " + SPRITE_ID);
        return false;
    }

    Image image = new Image(sprite);
    searchButtonImage = image.toSearchableImage(tolerance, ColorModel.RGB);

    initialized = true;
    return true;
}

// 4. Find and tap
public static boolean tapSearchButton(Script script) {
    List<ImageSearchResult> matches = script.getImageAnalyzer().findLocations(searchButtonImage);
    if (matches == null || matches.isEmpty()) return false;

    Point location = matches.get(0).getAsPoint();
    return script.getFinger().tap(location);
}
```

### Project Structure

No new files - modify existing BankSearchUtils.java:

```
utilities/src/main/java/utilities/
├── BankSearchUtils.java   # Modify openSearch() to use sprite tap
└── BankScrollUtils.java   # Reference implementation
```

### Anti-Patterns to Avoid
- **Keyboard shortcuts for UI buttons** - Don't work reliably in OSRS
- **Hard-coded screen coordinates** - Brittle, breaks with UI changes
- **Skipping verification** - Always check DialogueType after tap
</architecture_patterns>

<dont_hand_roll>
## Don't Hand-Roll

| Problem | Use Instead | Why |
|---------|-------------|-----|
| Button location detection | `ImageAnalyzer.findLocations()` | Sprite IDs are stable, pixel coords are not |
| Search activation check | `DialogueType.TEXT_SEARCH` | Already implemented in isSearchActive() |
| Color matching | `SingleThresholdComparator` | Handles tolerance properly |
| Image conversion | `Image.toSearchableImage()` | OSMB's optimized format |

**Key insight:** All the infrastructure exists in BankScrollUtils. The implementation is copy-paste with a different sprite ID.
</dont_hand_roll>

<common_pitfalls>
## Common Pitfalls

### Pitfall 1: Sprite ID Discovery
**What goes wrong:** Can't find the sprite ID in code/docs
**Why it happens:** OSRS sprite IDs aren't publicly documented
**How to avoid:** Use OSMB's debug tool in-game to find SEARCH button sprite
**Warning signs:** Sprite ID 0 or hardcoded values from guessing

### Pitfall 2: Bank Not Visible Check
**What goes wrong:** Trying to tap button when bank is closed
**Why it happens:** Race condition or state mismatch
**How to avoid:** Always check `bank.isVisible()` before sprite operations
**Warning signs:** "sprite not found" when bank should be open

### Pitfall 3: Multiple Matches
**What goes wrong:** findLocations() returns multiple results
**Why it happens:** Similar sprites elsewhere on screen
**How to avoid:** Take first match (sorted by relevance) or filter by bounds
**Warning signs:** Tapping wrong location

### Pitfall 4: Initialization Order
**What goes wrong:** Using sprite before SpriteManager is ready
**Why it happens:** Called too early in script lifecycle
**How to avoid:** Lazy init on first use, not in static initializer
**Warning signs:** NPE or "sprite manager not available"
</common_pitfalls>

<code_examples>
## Code Examples

### Reference: BankScrollUtils Pattern (lines 50-106)

```java
// Source: utilities/src/main/java/utilities/BankScrollUtils.java
private static final int SCROLL_DOWN_SPRITE_ID = 788;
private static SearchableImage scrollDownImage;
private static boolean initialized = false;

public static boolean init(Script script) {
    if (initialized) return true;

    try {
        SingleThresholdComparator tolerance = new SingleThresholdComparator(15);

        SpriteDefinition downSprite = script.getSpriteManager().getSprite(SCROLL_DOWN_SPRITE_ID, 0);
        if (downSprite == null) {
            script.log(BankScrollUtils.class, "scroll down sprite not found");
            return false;
        }
        Image downImage = new Image(downSprite);
        scrollDownImage = downImage.toSearchableImage(tolerance, ColorModel.RGB);

        initialized = true;
        return true;
    } catch (Exception e) {
        script.log(BankScrollUtils.class, "failed to load sprites: " + e.getMessage());
        return false;
    }
}
```

### Reference: Tap Pattern (lines 114-147)

```java
// Source: utilities/src/main/java/utilities/BankScrollUtils.java
public static boolean scrollDown(Script script) {
    if (!script.getWidgetManager().getBank().isVisible()) {
        script.log(BankScrollUtils.class, "bank not visible");
        return false;
    }

    if (!init(script)) return false;

    List<ImageSearchResult> matches = script.getImageAnalyzer().findLocations(scrollDownImage);
    if (matches == null || matches.isEmpty()) {
        script.log(BankScrollUtils.class, "button not found");
        return false;
    }

    ImageSearchResult result = matches.get(0);
    Point location = result.getAsPoint();
    boolean tapped = script.getFinger().tap(location);

    if (tapped) {
        script.pollFramesHuman(() -> false, script.random(200, 400));
    }

    return tapped;
}
```
</code_examples>

<open_questions>
## Open Questions

### 1. SEARCH Button Sprite ID

**What we know:**
- Scroll buttons use sprites 773, 788, 789, 791
- BankButtonType.SEARCH exists but has no click API
- Sprite IDs are found via OSMB debug tool

**What's unclear:**
- The exact sprite ID for the SEARCH button

**Recommendation:** User must find sprite ID using OSMB debug tool in-game:
1. Open bank in OSMB
2. Open debug tool
3. Search/scan sprites visible on screen
4. Identify SEARCH button sprite ID and frame

### 2. Button State Variants

**What we know:**
- Some buttons have multiple frames (hover, pressed, disabled)

**What's unclear:**
- Whether SEARCH has multiple sprite variants

**Recommendation:** Check both frame 0 and frame 1 when discovering sprite ID
</open_questions>

<sources>
## Sources

### Primary (HIGH confidence)
- BankScrollUtils.java - Proven sprite detection implementation
- Bank.md, BankButtonType.md - API documentation confirming no click method
- 01-01-SUMMARY.md - Documents keyboard approach failure

### Secondary (MEDIUM confidence)
- OSMB debug tool - Required for sprite ID discovery

### Tertiary (LOW confidence)
- None
</sources>

<metadata>
## Metadata

**Research scope:**
- Core technology: OSMB sprite-based UI interaction
- Ecosystem: Internal APIs only (no external dependencies)
- Patterns: BankScrollUtils reference implementation
- Pitfalls: Sprite discovery, initialization timing

**Confidence breakdown:**
- Standard stack: HIGH - All APIs proven in BankScrollUtils
- Architecture: HIGH - Direct copy of working pattern
- Pitfalls: HIGH - Based on BankScrollUtils development experience
- Code examples: HIGH - From working production code

**Research date:** 2026-01-14
**Valid until:** Indefinite (internal patterns, not external ecosystem)
</metadata>

---

*Phase: 06-tap-based-search-activation*
*Research completed: 2026-01-14*
*Ready for planning: YES (pending sprite ID discovery)*
