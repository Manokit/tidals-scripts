---
# tidals-scripts-u07k
title: Fairy Ring Travel Log Support
status: in-progress
type: feature
priority: normal
created_at: 2026-02-02T02:44:47Z
updated_at: 2026-02-02T09:13:53Z
---

## Summary
Add fairy ring teleport support to TidalsUtilities. The fairy ring dial uses custom rendered glyphs that OCR and sprite matching cannot read. Instead, use the **travel log search** approach.

Also integrates fairy rings into the walker's teleport routing system with two-leg cost comparison.

## Implementation

### FairyRingUtils.java (new)
- All 55 fairy ring codes mapped to search terms and destination positions
- Travel log flow: Configure → wait → type search → OCR full log → tap first result → menu verify → confirm teleport
- `findNearestRing()` and `getAllDestinations()` for walker integration
- Debug paint support: `debugTravelLogBounds` and `debugResultArea` volatile fields

### TeleportRegistry.java (modified)
- Added `areFairyRingsAvailable()` — checks for dramen staff in equipment/inventory
- `setLumbridgeEliteComplete()` — manual override for diary holders
- Cached fairy ring availability check with `resetFairyRingCache()`

### TidalsWalker.java (modified)
- Two-leg fairy ring routing: player→nearest_ring + destination_ring→target
- Prefilter: only considers destination rings within 150 tiles of target
- Both legs sent through Dax bulk API (leg 1 may itself use a jewelry teleport)
- Fairy ring weight constant (45) for UI interaction time
- Falls back to direct path if fairy ring route fails

## Bugs Found During Testing

### 1. obj.interact() retries internally (12+ second waste)
`tryLastDestination` used `fairyRing.interact("Last-destination (cks)")` which retries ~6 times when the action doesn't match the ring's actual last-destination. Fixed by skipping last-destination shortcut entirely — go straight to travel log.

### 2. Magnifying glass tap unnecessary
The search input is already focused when Configure opens the travel log. Removed the magnifying glass pixel detection step entirely.

### 3. getFirstResultArea() offset math wrong
Fixed-offset calculation `(x + w - 71 - 163, y + h - 350)` placed the OCR box above and to the left of the travel log. Fixed by:
- OCR the full travel log bounds for the expected code ("C K S")
- Tap a simple relative area `(x+10, y+40, w-20, 30)` inside the log
- Verify via tapGetResponse menu check

### 4. OSMB shape types ≠ java.awt
`getConvexHull()` returns `com.osmb.api.shape.Polygon`, `getObjectArea()` returns `RectangleArea`. Neither converts to screen-space Rectangle for tapGetResponse.

### 5. confirmTeleport() used non-existent OverlayBoundary.transformOverlayPosition()
The hallucinated static method returned (0,0), and subtracting debug-tool offsets (-628, -200) produced negative screen coordinates, crashing OCR. Fixed by computing button position from `script.getScreen().getBounds()` minus offsets: `screenW - 638, screenH - 203`.

### 6. Travel log result tap was blind — no menu verification
`tap(true, resultArea)` clicked the result area without verifying the menu entry matched the fairy ring code. Replaced with `tapGetResponse(true, resultArea)` and checking that the raw menu text contains "use code C K S" (the exact menu format for travel log entries). Bails and closes the travel log if the code doesn't match.

## Checklist
- [x] Create FairyRingCode enum/map with code → search term mappings (all 55 destinations)
- [x] Create FairyRingUtils class
- [x] Implement travel log bounds detection (corner sprites 824-827)
- [x] ~~Implement magnifying glass detection~~ (removed — search already focused on Configure)
- [x] Implement search text input
- [x] Implement OCR of full travel log for code verification
- [x] Implement tap first result + menu verification (tapGetResponse confirms "use code X Y Z")
- [x] Implement confirm teleport (OCR "Teleport to this location" button overlay via screen bounds offset)
- [x] Handle edge cases: travel log not open, no results, already at destination
- [x] Integrate into TeleportRegistry (fairy ring availability check)
- [x] Integrate into TidalsWalker (two-leg routing with cost comparison)
- [x] Build utilities successfully
- [x] Add fairy ring destinations to TidalsWalkerTest ScriptUI
- [x] Add debug paint overlay (green=log bounds, red=OCR area)
- [ ] Test with 3+ destinations (in-game testing — result area tap offset may need tuning)
