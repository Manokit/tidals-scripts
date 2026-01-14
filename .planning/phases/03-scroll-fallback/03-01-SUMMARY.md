---
phase: 03-scroll-fallback
plan: 01
subsystem: utilities
tags: [sprite-detection, bank-scrolling, osmb-api, image-analyzer]

# Dependency graph
requires:
  - phase: 02-single-item-withdrawal
    provides: BankSearchUtils for item search
provides:
  - BankScrollUtils.scrollDown() method
  - BankScrollUtils.scrollUp() method
  - Sprite-based scroll button detection via ImageAnalyzer
affects: [04-scroll-search-integration, bank-utilities]

# Tech tracking
tech-stack:
  added: []
  patterns: [sprite-template-matching, image-analyzer-detection]

key-files:
  created:
    - utilities/src/main/java/utilities/BankScrollUtils.java
  modified: []

key-decisions:
  - "Used game sprites (773, 788) instead of custom PNG images"
  - "Used ImageAnalyzer.findLocations() for template matching"
  - "Added scrollbar position tracking with sprites 789, 791"

patterns-established:
  - "SpriteManager + ImageAnalyzer for UI element detection"
  - "SearchableImage from sprite for template matching"

issues-created: []

# Metrics
duration: 45min (including API discovery)
completed: 2026-01-14
---

# Phase 3 Plan 1: Scroll Button Detection Summary

**Implemented bank scroll detection using SpriteManager and ImageAnalyzer for sprite-based template matching**

## Performance

- **Duration:** 45 min (initial approach failed, required API research)
- **Started:** 2026-01-14
- **Completed:** 2026-01-14
- **Tasks:** 2/2
- **Files modified:** 1 Java file

## Accomplishments

- Implemented BankScrollUtils.java with scrollDown(), scrollUp() methods
- Added canScrollDown(), canScrollUp() helper methods for scroll availability detection
- Added scrollToTop(), scrollToBottom() convenience methods
- Added getScrollbarPosition() for scroll progress tracking
- Used proper OSMB API: SpriteManager + ImageAnalyzer.findLocations()

## Task Commits

1. **Task 1: Capture scroll button images** - User identified sprite IDs instead (773, 788, 789, 791)
2. **Task 2: Implement BankScrollUtils** - `086152b` (fix)

## Files Created/Modified

- `utilities/src/main/java/utilities/BankScrollUtils.java` - Scroll detection and tap methods
- `utilities/jar/TidalsUtilities.jar` - Rebuilt with BankScrollUtils

## API Discovery

The original plan and docs/image.md specified APIs that don't exist:
- `PixelAnalyzer.findSubImages()` - DOES NOT EXIST
- `EuclideanToleranceComparator` - DOES NOT EXIST
- `WidgetManager.getSpriteManager()` - Wrong accessor

**Correct API** (discovered via user-provided javadocs):
- `Script.getSpriteManager()` - Access SpriteManager directly from Script
- `Script.getImageAnalyzer()` - Access ImageAnalyzer directly from Script
- `ImageAnalyzer.findLocations(SearchableImage)` - Returns List<ImageSearchResult>
- `ImageSearchResult.getAsPoint()` - Get location as Point
- `SingleThresholdComparator` - Tolerance comparator that exists

## Sprite IDs Used

| Element | Sprite ID | Frame |
|---------|-----------|-------|
| Scroll Up Arrow | 773 | 0 |
| Scroll Down Arrow | 788 | 0 |
| Scrollbar Top | 789 | 0 |
| Scrollbar Bottom | 791 | 0 |

## Implementation Pattern

```java
// Load sprite via SpriteManager
SpriteDefinition sprite = script.getSpriteManager().getSprite(SPRITE_ID, 0);
Image image = new Image(sprite);
SearchableImage searchable = image.toSearchableImage(tolerance, ColorModel.RGB);

// Find on screen via ImageAnalyzer
List<ImageSearchResult> matches = script.getImageAnalyzer().findLocations(searchable);
if (!matches.isEmpty()) {
    Point location = matches.get(0).getAsPoint();
    script.getFinger().tap(location);
}
```

## Decisions Made

1. **Used game sprites instead of PNG images**
   - Sprites are loaded directly from game, no external files needed
   - More reliable than custom screenshots
   - User identified sprite IDs via testing

2. **Used ImageAnalyzer for template matching**
   - Proper OSMB API for finding SearchableImage on screen
   - Returns List<ImageSearchResult> with location and score

3. **Added scrollbar position tracking**
   - Can determine scroll progress via scrollbar sprite positions
   - Enables smarter scroll-then-search logic

## Documentation Updates

Added new OSMB API documentation files:
- docs/Script.md - Script class methods
- docs/Image.md - Image and SearchableImage classes
- docs/ImageSearchResult.md - Search result structure
- docs/Sprite.md - SpriteManager and SpriteDefinition
- docs/PixelAnalyzer.md - Updated with actual API

## Next Phase Readiness

- BankScrollUtils ready for integration with BankSearchUtils
- Sprite-based detection proven working
- Phase 03 plan 02 can proceed with scroll-search integration

---
*Phase: 03-scroll-fallback*
*Completed: 2026-01-14*
