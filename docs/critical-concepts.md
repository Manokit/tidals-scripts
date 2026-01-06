# Critical Concepts for Color Botting

> **OSMB is a COLOR BOT** - This is the most important thing to understand. All your interactions are based on visual detection, not memory injection.

## 1. NPCs Aren't in ObjectManager - Use Minimap Positions!

### The Problem
**NPCs do NOT appear in ObjectManager.** They move around and their positions must be obtained from the minimap.

### The Solution
```java
// WRONG - This won't work!
RSObject npc = getObjectManager().getClosestObject(pos, "Guard"); // NPCs not here!

// CORRECT - Get positions from minimap
List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

for (WorldPosition npcPos : npcPositions) {
    // Create tile cube at NPC position
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
    if (tileCube == null) continue;
    
    // Verify what's at this position
    MenuEntry response = getFinger().tapGetResponse(true, tileCube);
    if (response != null) {
        String npcName = response.getEntityName();
        if (npcName.contains("Guard")) {
            getFinger().tap(tileCube, "Attack");
            break;
        }
    }
}
```

### For Highlighted NPCs (Combat)
```java
// If NPCs are highlighted (red/yellow), use highlight detection
SearchablePixel highlightColor = new SearchablePixel(0, 100, 50, 10, 10, 10); // Red

List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

for (WorldPosition npcPos : npcPositions) {
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
    if (tileCube == null) continue;
    
    // Get the highlight bounds
    Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, highlightColor);
    if (bounds != null) {
        getFinger().tap(bounds, "Attack");
        break;
    }
}
```

### Why This Matters
- NPCs move every game tick
- They're rendered as sprites, not objects
- Minimap updates show all visible NPCs
- You must translate minimap positions → tile cubes → clickable areas

**For complete NPC finding guide including pixel clusters, large NPCs, and troubleshooting, see the detailed NPC section in `docs/walking-npcs.md`**

---

## 2. Items with Identical Sprites CANNOT Be Distinguished

### The Problem
Many items look identical but have different IDs based on charges, states, or contents.

### Examples of Problematic Items
- **Waterskins**: Waterskin(0) through Waterskin(4) - all look the same
- **Jewelry charges**: Ring of dueling(8) through Ring of dueling(1)
- **Seeds**: Different seed types may share sprites
- **Potions**: Same visual appearance for different dose counts

### Solutions

#### Use BuffOverlay for Charge-Based Items
```java
BuffOverlay waterskinBuff = new BuffOverlay(core, WATERSKIN_ID);
if (waterskinBuff.isVisible()) {
    String charges = waterskinBuff.getText(); // Read overlay text
}
```

#### Use Right-Click Menu
```java
// Interact and check the menu response
MenuEntry response = getFinger().tapGetResponse(true, itemBounds);
if (response != null) {
    String entityName = response.getEntityName(); // "Waterskin(4)"
    // Parse the charge count from the name
}
```

#### Be Creative with Detection
- Check quantities and deduce which item type
- Use process of elimination
- Track state changes externally
- Use OCR on item overlays

---

## 3. HSL vs RGB Color Detection

### When to Use HSL (Gamescreen Objects)
HSL comparators are **superior** for gamescreen detection because they're more tolerant to lighting variations.

```java
// For gamescreen objects - use HSL
SearchablePixel pixel = new SearchablePixel(
    hue,         // 0-360
    saturation,  // 0-100  
    lightness,   // 0-100
    tolerance    // Usually 10-25 for each
);
```

### When to Use RGB (Minimap)
RGB works better for minimap because the colors are more consistent.

```java
// For minimap - RGB is acceptable
```

### Finding the Right Colors
1. Use the Debug Tool
2. Sample pixels from the object you want to detect
3. Start with high tolerance (25+) and refine down
4. Test in different lighting conditions

---

## 4. Visual Door Detection (Collision Map is Static!)

### The Problem
The collision map shows the **DEFAULT** world state, NOT real-time. Doors, gates, and other movable objects do NOT update the collision map when they change state.

### WRONG Approach
```java
// This assumes the collision map is accurate
if (collisionMap.isBlocked(doorTile)) {
    // Door might actually be open!
}
```

### CORRECT Approach - Visual Verification
```java
// Check what action is available
MenuEntry response = getFinger().tapGetResponse(true, doorPoly);
if (response != null) {
    String action = response.getAction();
    if (action.equalsIgnoreCase("Open")) {
        // Door is closed, open it
        getFinger().tap(doorPoly, "Open");
    } else if (action.equalsIgnoreCase("Close")) {
        // Door is open, walk through
    }
}
```

### Alternative - Pixel Detection
You can also detect open/closed states using color detection if the door has distinct open/closed appearances.

---

## 5. NPC Interaction Pattern (NPCs Move!)

### The Problem
NPCs aren't in ObjectManager - they move around and their positions come from the minimap.

### Solution
```java
// Get NPC positions from minimap
List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

for (WorldPosition npcPos : npcPositions) {
    // Create tile cube at NPC position
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60); // 60 = height
    if (tileCube == null) continue;
    
    // Get highlighted bounds for interaction
    Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, highlightColors);
    if (bounds != null) {
        getFinger().tap(bounds, "Attack");
    }
}
```

### For 2x2 NPCs (Large NPCs)
```java
// Increase tile dimensions for larger NPCs
Polygon largeCube = getSceneProjector().getTileCube(
    npcPos.getX(), npcPos.getY(), 0,  // position
    2, 2,  // tile width, height (for 2x2 NPC)
    0, 60, // z offset, height
    false  // require all vertices on screen
);
```

**See `docs/walking-npcs.md` for complete NPC finding patterns including pixel clusters and troubleshooting.**

---

## 6. Verify Interactions with tapGetResponse

### Why This Matters
Blind clicking can misclick adjacent objects, especially when zoomed out.

### Better Pattern
```java
// Get what was actually clicked
MenuEntry response = getFinger().tapGetResponse(true, objectPoly);

if (response != null) {
    String action = response.getAction();
    String entity = response.getEntityName();
    
    if (action.equalsIgnoreCase("Mine")) {
        log("Successfully clicked mine action on " + entity);
        // Now wait for mining to complete
    } else {
        log("Misclicked! Got action: " + action);
        // Retry
    }
}
```

---

## 7. Shrink Click Areas to Avoid Misclicks

### The Problem
When zoomed out, object polygons can overlap with adjacent objects.

### Solution
```java
RSObject rock = getObjectManager().getClosestObject(getWorldPosition(), "Rocks");
Polygon rockPoly = rock.getConvexHull();

if (rockPoly != null) {
    // Shrink to 70% to avoid clicking adjacent objects
    Polygon shrunkPoly = rockPoly.getResized(0.7);
    if (shrunkPoly != null) {
        getFinger().tap(shrunkPoly, "Mine");
    }
}
```

Typical shrink values:
- 0.7 (70%) - Conservative, very safe
- 0.8 (80%) - Balanced
- 0.9 (90%) - Minimal shrinking

---

## 8. Widget Visibility Checks

### Check Object Visibility Before Clicking
Objects can be partially off-screen or obstructed by UI elements.

```java
Polygon objectPoly = object.getConvexHull();

// Check how visible the object is (ignoring chatbox)
double visibleFactor = getWidgetManager().insideGameScreenFactor(
    objectPoly, 
    List.of(ChatboxComponent.class) // Ignore chatbox obstruction
);

if (visibleFactor < 0.3) {
    // Less than 30% visible, walk closer or rotate camera
    return false;
}
```

---

## Best Practices Summary

1. **Get NPC positions from minimap** - They're NOT in ObjectManager
2. **Use highlight detection for combat NPCs** - getHighlightBounds with HSL colors
3. **Never assume identical sprites are different items** - Use overlays, menus, or external tracking
4. **Use HSL for gamescreen, RGB for minimap** - HSL is more lighting-tolerant
5. **Always verify door states visually** - Collision map is static
6. **Use tapGetResponse to verify clicks** - Prevents misclicks
7. **Shrink polygons when zoomed out** - Avoid overlapping click areas
8. **Check visibility before interacting** - Walk closer if needed

---

*When in doubt, think: "How would I detect this visually?" The API gives you eyes, not memory access.*
