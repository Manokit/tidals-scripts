# Phase 10: Randomized Sprite Taps - Research

**Researched:** 2026-01-14
**Domain:** OSMB Finger API - sprite interaction humanization
**Confidence:** HIGH

<research_summary>
## Summary

Researched the OSMB Finger API for humanizing sprite tap interactions. The solution is simpler than expected: OSMB's `tap(Shape)` method already performs random coordinate selection within the shape bounds.

The current BankSearchUtils and BankScrollUtils implementations use `ImageSearchResult.getAsPoint()` which returns a fixed Point. The fix is to use `ImageSearchResult.getBounds()` which returns a Rectangle (a Shape), allowing OSMB to randomize tap coordinates automatically.

**Primary recommendation:** Replace all `result.getAsPoint()` calls with `result.getBounds()` and pass the Rectangle to `tap()` instead of Point.
</research_summary>

<standard_stack>
## Standard Stack

No external libraries needed - this uses existing OSMB API methods.

### Core
| Class | Method | Purpose | Why Use |
|-------|--------|---------|---------|
| `ImageSearchResult` | `getBounds()` | Returns sprite bounds as Rectangle | Gives tap area, not fixed point |
| `Finger` | `tap(Shape shape)` | Taps random point within shape | Built-in humanization |
| `Rectangle` | (OSMB shape) | Implements Shape interface | Works with tap(Shape) |

### Current Implementation (Point-based)
```java
ImageSearchResult result = matches.get(0);
Point location = result.getAsPoint();        // fixed coordinate
script.getFinger().tap(location);            // always same spot
```

### Target Implementation (Rectangle-based)
```java
ImageSearchResult result = matches.get(0);
Rectangle bounds = result.getBounds();       // sprite bounds
script.getFinger().tap(bounds);              // random within bounds
```
</standard_stack>

<architecture_patterns>
## Architecture Patterns

### Pattern 1: Sprite Detection to Random Tap
**What:** When detecting sprites via ImageAnalyzer, use getBounds() instead of getAsPoint() for tap targets.

**When to use:** Any sprite-based tap interaction where humanization is desired.

**Example:**
```java
// Source: OSMB Finger.md documentation
// "Performs a tap at a random point within the specified shape"

List<ImageSearchResult> matches = script.getImageAnalyzer().findLocations(spriteImage);
if (matches != null && !matches.isEmpty()) {
    ImageSearchResult result = matches.get(0);

    // WRONG: fixed coordinate
    // Point location = result.getAsPoint();
    // script.getFinger().tap(location);

    // CORRECT: random within sprite bounds
    Rectangle bounds = result.getBounds();
    script.getFinger().tap(bounds);
}
```

### Pattern 2: Preserve Point for Position Checking
**What:** When checking positions (e.g., scrollbar Y coordinate), continue using getAsPoint().

**When to use:** Position detection, comparison, logging coordinates.

**Example:**
```java
// position checking still uses Point
int y = matches.get(0).getAsPoint().y;
return y == SCROLLBAR_TOP_Y;
```

### Anti-Patterns to Avoid
- **Converting Rectangle to Point for tapping:** Defeats humanization
- **Shrinking bounds unnecessarily:** Sprite bounds are already the correct tap area
- **Adding custom random offsets:** tap(Shape) handles this internally
</architecture_patterns>

<dont_hand_roll>
## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Random tap within area | Custom random offset calculation | `tap(Rectangle bounds)` | OSMB handles human-like randomization |
| Sprite click bounds | Manual width/height calculation | `ImageSearchResult.getBounds()` | Already provided by API |
| Coordinate jitter | Adding `random(-5, 5)` to Point | Pass Shape to tap() | Built-in, consistent behavior |

**Key insight:** OSMB's Finger API is designed for humanization. The tap(Shape) overload exists specifically for this purpose. Fighting the API by using Point + manual offsets creates inconsistent behavior and maintenance burden.
</dont_hand_roll>

<common_pitfalls>
## Common Pitfalls

### Pitfall 1: Using getAsPoint() for Tap Targets
**What goes wrong:** Every tap hits exact same pixel, looks robotic
**Why it happens:** Point is simpler to understand, getAsPoint() is prominent in API
**How to avoid:** Always use getBounds() when tapping sprites
**Warning signs:** Logs show identical coordinates every tap

### Pitfall 2: Breaking Position Detection
**What goes wrong:** isAtTop()/isAtBottom() stop working after refactor
**Why it happens:** Changing getAsPoint() calls that are used for position comparison
**How to avoid:** Only change tap-related code, preserve position checks
**Warning signs:** Scrolling never stops, infinite loops

### Pitfall 3: Null Rectangle from getBounds()
**What goes wrong:** NullPointerException when sprite not found
**Why it happens:** Not checking if matches list is empty before calling getBounds()
**How to avoid:** Existing null checks on matches list are sufficient
**Warning signs:** Crash on first tap attempt
</common_pitfalls>

<code_examples>
## Code Examples

Verified patterns from existing codebase and OSMB docs:

### tapSearchButton (BankSearchUtils.java:92-116)
Current:
```java
ImageSearchResult result = matches.get(0);
Point location = result.getAsPoint();
script.log(BankSearchUtils.class, "tapping search button at: " + location.x + "," + location.y);
boolean tapped = script.getFinger().tap(location);
```

Target:
```java
ImageSearchResult result = matches.get(0);
Rectangle bounds = result.getBounds();
script.log(BankSearchUtils.class, "tapping search button (bounds: " + bounds.width + "x" + bounds.height + ")");
boolean tapped = script.getFinger().tap(bounds);
```

### scrollDown (BankScrollUtils.java:136-150)
Current:
```java
ImageSearchResult result = matches.get(0);
Point location = result.getAsPoint();
script.log(BankScrollUtils.class, "tapping scroll down at: " + location.x + "," + location.y);
boolean tapped = script.getFinger().tap(location);
```

Target:
```java
ImageSearchResult result = matches.get(0);
Rectangle bounds = result.getBounds();
script.log(BankScrollUtils.class, "tapping scroll down (bounds: " + bounds.width + "x" + bounds.height + ")");
boolean tapped = script.getFinger().tap(bounds);
```

### scrollUp (BankScrollUtils.java:176-190)
Current:
```java
ImageSearchResult result = matches.get(0);
Point location = result.getAsPoint();
script.log(BankScrollUtils.class, "tapping scroll up at: " + location.x + "," + location.y);
boolean tapped = script.getFinger().tap(location);
```

Target:
```java
ImageSearchResult result = matches.get(0);
Rectangle bounds = result.getBounds();
script.log(BankScrollUtils.class, "tapping scroll up (bounds: " + bounds.width + "x" + bounds.height + ")");
boolean tapped = script.getFinger().tap(bounds);
```
</code_examples>

<scope_of_changes>
## Scope of Changes

### Files to Modify

**1. BankSearchUtils.java**
- `tapSearchButton()` (line 104-106): Change Point to Rectangle

**2. BankScrollUtils.java**
- `scrollDown()` (line 138-140): Change Point to Rectangle
- `scrollUp()` (line 178-180): Change Point to Rectangle

### Lines NOT to Modify

Position detection must continue using Point:
- `BankScrollUtils.java:257-258` - getAsPoint() for scrollbar position calculation
- `BankScrollUtils.java:300` - getAsPoint().y for isAtTop() check
- `BankScrollUtils.java:332` - getAsPoint().y for isAtBottom() check

### Change Count
- **3 methods** need Point→Rectangle refactor
- **4 position checks** must remain unchanged
- **0 new methods** needed
</scope_of_changes>

<open_questions>
## Open Questions

None - this is straightforward internal API usage.

The only consideration is log message format: showing bounds dimensions (width x height) vs showing coordinates. Recommend showing dimensions since exact coordinates are now random.
</open_questions>

<sources>
## Sources

### Primary (HIGH confidence)
- `docs/Finger.md` - OSMB Finger interface documentation
- `docs/ImageSearchResult.md` - OSMB ImageSearchResult class
- Existing code: `utilities/src/main/java/utilities/BankSearchUtils.java`
- Existing code: `utilities/src/main/java/utilities/BankScrollUtils.java`

### Secondary (MEDIUM confidence)
- None needed - domain is internal OSMB API

### Tertiary (LOW confidence)
- None
</sources>

<metadata>
## Metadata

**Research scope:**
- Core technology: OSMB Finger API, ImageSearchResult
- Ecosystem: Internal OSMB patterns only
- Patterns: Shape-based tapping vs Point-based
- Pitfalls: Breaking position detection

**Confidence breakdown:**
- Standard stack: HIGH - verified with OSMB docs
- Architecture: HIGH - from Finger.md documentation
- Pitfalls: HIGH - from code analysis
- Code examples: HIGH - from existing codebase

**Research date:** 2026-01-14
**Valid until:** N/A - internal API, no version concerns
</metadata>

---

*Phase: 10-randomized-sprite-taps*
*Research completed: 2026-01-14*
*Ready for planning: yes*
