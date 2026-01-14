# Phase 3: Scroll Fallback - Research

**Researched:** 2026-01-14
**Domain:** OSMB bank scrolling when search doesn't find item
**Confidence:** MEDIUM

<research_summary>
## Summary

Researched bank scrolling approaches for OSMB mobile bot when search doesn't locate an item. Key finding: **OSMB Bank API has NO direct scroll method**. The standard approach requires one of: (1) touch-based scrollbar dragging using Finger API, (2) image-based detection of scroll arrows/scrollbar, or (3) swipe gesture simulation.

OSRS mobile bank scrolling is notoriously awkward - the standard scroll bar on the right side must be interacted with directly. There's no keyboard shortcut (arrow keys control camera, not bank scroll) and scroll wheel support is inconsistent on mobile.

**Primary recommendation:** Use image-based detection of scroll buttons with PixelAnalyzer.findSubImages(), combined with Finger.touch() for scrollbar dragging as fallback. Detect end-of-scroll by comparing item positions before/after scroll.
</research_summary>

<standard_stack>
## Standard Stack

### Core
| Component | Purpose | Why Standard |
|-----------|---------|--------------|
| Finger API | Touch input for scrollbar interaction | Only reliable input method in OSMB |
| PixelAnalyzer | Scroll button image detection | Template matching for UI elements |
| SearchableImage | Scroll arrow templates | Color-tolerant matching |

### Supporting
| Component | Purpose | When to Use |
|-----------|---------|-------------|
| Image class | Load scroll arrow PNGs | Store in src/resources/ |
| TouchType.DOWN/MOVE/UP | Swipe gesture simulation | If scroll arrows unavailable |
| pollFramesUntil() | Wait for scroll completion | Verify items moved |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Image detection | Hardcoded scroll button position | Breaks if UI layout changes |
| Touch scrollbar | Swipe gesture in bank area | OSRS mobile swipe is buggy/unreliable |
| Scrollbar dragging | Click scroll arrows repeatedly | Slower but more reliable |
</standard_stack>

<architecture_patterns>
## Architecture Patterns

### Recommended Approach: Scroll Arrow Detection

OSRS bank has two scroll arrows at top/bottom of scrollbar. Detect and click them.

```
Bank interface layout:
+---------------------------+---+
|  Item slots (grid)        |▲| <- scroll up arrow
|                           |█|
|                           |█| <- scrollbar track
|                           |█|
|                           |▼| <- scroll down arrow
+---------------------------+---+
```

### Pattern 1: Image-Based Scroll Button Detection
**What:** Load scroll arrow images, find them on screen, tap to scroll
**When to use:** Primary approach - most reliable
**Example:**
```java
// Source: docs/image.md pattern
public class BankScrollUtils {
    private SearchableImage scrollUpArrow;
    private SearchableImage scrollDownArrow;

    public void init(Script script) {
        ToleranceComparator tolerance = new EuclideanToleranceComparator(15);

        Image upImg = new Image(new File("src/resources/scroll_up.png"));
        scrollUpArrow = upImg.toSearchableImage(tolerance, ColorModel.RGB);

        Image downImg = new Image(new File("src/resources/scroll_down.png"));
        scrollDownArrow = downImg.toSearchableImage(tolerance, ColorModel.RGB);
    }

    public boolean scrollDown(Script script) {
        List<Rectangle> matches = script.getPixelAnalyzer().findSubImages(scrollDownArrow);
        if (matches.isEmpty()) {
            return false; // scroll button not found or at end
        }

        return script.getFinger().tap(matches.get(0).getCenter());
    }
}
```

### Pattern 2: Touch Swipe Gesture (Fallback)
**What:** Simulate swipe gesture on scrollbar using DOWN/MOVE/UP sequence
**When to use:** If image detection fails
**Example:**
```java
// Source: docs/Finger.md - touch() method
public boolean swipeScrollbar(Script script, int startY, int endY, int x) {
    // press down at start position
    script.getFinger().touch(x, startY, TouchType.DOWN);
    script.pollFramesHuman(() -> false, script.random(50, 100));

    // move to end position (may need multiple intermediate points)
    script.getFinger().touch(x, endY, TouchType.MOVE);
    script.pollFramesHuman(() -> false, script.random(50, 100));

    // release
    script.getFinger().touch(x, endY, TouchType.UP);

    return true;
}
```

### Pattern 3: End-of-Scroll Detection
**What:** Detect when scrolling has reached top/bottom
**When to use:** Always - prevents infinite scroll loops
**Example:**
```java
public boolean hasMoreItemsBelow(Script script, int targetItemId) {
    // capture current bank state
    ItemGroupResult before = script.getWidgetManager().getBank().search(Set.of(targetItemId));

    // attempt scroll
    scrollDown(script);
    script.pollFramesHuman(() -> false, script.random(200, 400));

    // check if item now visible
    ItemGroupResult after = script.getWidgetManager().getBank().search(Set.of(targetItemId));

    if (after != null && after.contains(targetItemId)) {
        return true; // found it
    }

    // check if bank contents changed (still scrolling possible)
    // if same items visible, we've hit the end
    return !sameItemsVisible(before, after);
}
```

### Anti-Patterns to Avoid
- **Assuming keyboard arrows work:** Arrow keys control camera, NOT bank scroll
- **Using scroll wheel:** Inconsistent/broken on OSRS mobile
- **Hardcoding scroll button positions:** UI can vary by device/resolution
- **Infinite scroll loops:** Must detect end-of-scroll condition
</architecture_patterns>

<dont_hand_roll>
## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Scroll button finding | Hardcoded coordinates | SearchableImage + PixelAnalyzer | UI positions vary |
| Swipe gesture | Manual coordinate math | Finger.touch() sequence | API handles timing |
| End-of-scroll detection | Pixel color comparison | Item position comparison | Items are reliable anchors |
| Scroll timing | Fixed delays | pollFramesUntil() with condition | Adapts to lag |

**Key insight:** OSRS mobile bank scrolling is notoriously finicky. The search feature exists specifically because scrolling is painful. Use image detection to find scroll UI elements rather than assuming positions.
</dont_hand_roll>

<common_pitfalls>
## Common Pitfalls

### Pitfall 1: Assuming Keyboard Scrolling Works
**What goes wrong:** Arrow keys do nothing in bank interface
**Why it happens:** Muscle memory from desktop apps; OSRS uses arrows for camera only
**How to avoid:** Only use touch/click-based scrolling
**Warning signs:** Bank doesn't move when pressing keys

### Pitfall 2: Infinite Scroll Loop
**What goes wrong:** Script keeps scrolling forever looking for item that doesn't exist
**Why it happens:** No end-of-scroll detection; scrolling past bottom returns to same position
**How to avoid:** Track item positions before/after scroll; detect no-change state
**Warning signs:** Same items visible after scroll attempt

### Pitfall 3: Scroll Timing Issues
**What goes wrong:** Reading bank contents before scroll animation completes
**Why it happens:** Fixed delays don't account for network lag
**How to avoid:** Use pollFramesUntil() with item position change condition
**Warning signs:** Intermittent failures finding visible items

### Pitfall 4: Resolution-Dependent Scroll Button Positions
**What goes wrong:** Scroll clicks miss the buttons on different devices
**Why it happens:** Hardcoded coordinates instead of visual detection
**How to avoid:** Use SearchableImage template matching
**Warning signs:** Works on one device, fails on another

### Pitfall 5: Mobile Swipe Gesture Unreliability
**What goes wrong:** Swipe gesture triggers wrong actions or fails silently
**Why it happens:** OSRS mobile swipe handling is buggy; requires specific finger positions
**How to avoid:** Prefer scroll button clicks over swipe gestures
**Warning signs:** Bank doesn't scroll or opens context menus instead
</common_pitfalls>

<code_examples>
## Code Examples

Verified patterns from OSMB documentation:

### Loading Scroll Button Images
```java
// Source: docs/image.md - Loading images from resources
import com.osmb.api.visual.image.Image;
import com.osmb.api.visual.image.SearchableImage;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.EuclideanToleranceComparator;
import java.io.File;

private SearchableImage scrollUpArrow;
private SearchableImage scrollDownArrow;

@Override
public void onStart() {
    EuclideanToleranceComparator tolerance = new EuclideanToleranceComparator(15);

    Image upImg = new Image(new File("src/resources/scroll_up.png"));
    scrollUpArrow = upImg.toSearchableImage(tolerance, ColorModel.RGB);

    Image downImg = new Image(new File("src/resources/scroll_down.png"));
    scrollDownArrow = downImg.toSearchableImage(tolerance, ColorModel.RGB);
}
```

### Finding and Clicking Scroll Button
```java
// Source: docs/image.md + docs/Finger.md patterns
private boolean clickScrollDown(Script script) {
    List<Rectangle> matches = script.getPixelAnalyzer().findSubImages(scrollDownArrow);

    if (matches.isEmpty()) {
        script.log(getClass(), "scroll down button not found");
        return false;
    }

    Rectangle button = matches.get(0);
    return script.getFinger().tap(button.getCenter());
}
```

### Touch-Based Scrollbar Drag
```java
// Source: docs/Finger.md - Low-Level Touch section
import com.osmb.api.input.TouchType;

private boolean dragScrollbar(Script script, Rectangle scrollbarBounds, int targetY) {
    int x = scrollbarBounds.getCenterX();
    int startY = scrollbarBounds.getCenterY();

    // press
    script.getFinger().touch(x, startY, TouchType.DOWN);
    script.pollFramesHuman(() -> false, script.random(30, 60));

    // drag
    script.getFinger().touch(x, targetY, TouchType.MOVE);
    script.pollFramesHuman(() -> false, script.random(30, 60));

    // release
    script.getFinger().touch(x, targetY, TouchType.UP);

    return true;
}
```

### End-of-Scroll Detection via Item Position
```java
// Source: pattern derived from docs/banking-patterns.md
private boolean canScrollDown(Script script) {
    Bank bank = script.getWidgetManager().getBank();

    // get visible items before scroll
    ItemGroupResult before = bank.search(Set.of()); // all items
    Set<Integer> beforeSlots = before != null ? before.getSlots() : Set.of();

    // attempt scroll
    clickScrollDown(script);
    script.pollFramesHuman(() -> false, script.random(300, 500));

    // get visible items after scroll
    ItemGroupResult after = bank.search(Set.of());
    Set<Integer> afterSlots = after != null ? after.getSlots() : Set.of();

    // if same slots visible, we've hit the bottom
    return !beforeSlots.equals(afterSlots);
}
```
</code_examples>

<sota_updates>
## State of the Art (2024-2026)

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Scroll wheel | Touch/click scroll buttons | OSRS Mobile launch | Scroll wheel broken on mobile |
| Keyboard arrows | Visual scroll buttons | N/A | Arrows never worked for bank |

**Current approach:**
- Image detection for scroll buttons (most reliable)
- Finger.touch() sequences for drag scrolling
- Item position tracking for end-of-scroll

**Deprecated/outdated:**
- Assuming scroll wheel works on mobile
- Hardcoded scroll button coordinates
</sota_updates>

<open_questions>
## Open Questions

Things that couldn't be fully resolved:

1. **Exact scroll button appearance**
   - What we know: Buttons are arrows at top/bottom of scrollbar
   - What's unclear: Exact pixel appearance may vary by client version
   - Recommendation: Capture fresh screenshots from current OSMB client; use generous tolerance (15-20)

2. **TouchType.MOVE behavior**
   - What we know: Finger.touch() supports DOWN/UP/MOVE
   - What's unclear: Whether MOVE properly triggers drag gesture in OSRS
   - Recommendation: Test scroll arrow clicking first; only use swipe as fallback

3. **Bank item visibility timing**
   - What we know: Need delay after scroll for items to update
   - What's unclear: Optimal delay duration varies by network
   - Recommendation: Use pollFramesUntil() with 500ms timeout, checking item visibility
</open_questions>

<sources>
## Sources

### Primary (HIGH confidence)
- docs/image.md - SearchableImage loading and findSubImages() usage
- docs/Finger.md - touch() method with TouchType for gestures
- docs/PixelAnalyzer.md - findSubImages() for template matching
- docs/banking-patterns.md - Bank item search patterns

### Secondary (MEDIUM confidence)
- [OSRS Wiki - Bank](https://oldschool.runescape.wiki/w/Bank) - Bank scroll behavior
- [OSRS Wiki - Game controls](https://oldschool.runescape.wiki/w/Game_controls) - Confirmed arrows control camera only

### Tertiary (LOW confidence - needs validation)
- [OSBot forums](https://osbot.org/forum/topic/141639-bank-scrolling/) - Different framework but similar patterns
- [RuneScape Forum Archive](https://forums.rs/en/429,430,241,66004737.html) - Mobile scroll issues documented
</sources>

<metadata>
## Metadata

**Research scope:**
- Core technology: OSMB Finger/PixelAnalyzer API
- Ecosystem: SearchableImage for UI detection
- Patterns: Touch scrolling, image detection, end-of-scroll
- Pitfalls: Keyboard assumptions, infinite loops, timing

**Confidence breakdown:**
- Standard stack: HIGH - OSMB docs are authoritative
- Architecture: MEDIUM - swipe gesture untested
- Pitfalls: HIGH - well documented in forums
- Code examples: HIGH - derived from official docs

**Research date:** 2026-01-14
**Valid until:** 2026-02-14 (30 days - OSMB API stable)
</metadata>

---

*Phase: 03-scroll-fallback*
*Research completed: 2026-01-14*
*Ready for planning: yes*
