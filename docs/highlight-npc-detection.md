# Highlight NPC Detection

Complete guide to detecting and tracking NPCs using visual highlight bounds. This is the **fastest and most reliable** method for NPC interaction in OSMB.

## 🎯 Core Concept

NPCs with enabled highlighting (combat, quest, shop NPCs, guards, etc.) can be detected by their highlight color. This is **much faster** than relying solely on minimap position updates.

**Speed Comparison:**
- Minimap position updates: 600-1000ms delay (tile-based)
- Visual bounds detection: 50-100ms delay (pixel-based, frame-by-frame)
- **Instant movement detection: Detects sub-tile movement immediately**

---

## 📊 Common Highlight Colors

Always use the Debug Tool to find your exact highlight colors. These are common defaults:

```java
// Cyan/Aqua (most aggressive NPCs, right-click attack)
SearchablePixel cyan = new SearchablePixel(180, 100, 58, 15, 15, 15);
// RGB: 38, 255, 255

// Red (combat/aggressive)
SearchablePixel red = new SearchablePixel(0, 100, 50, 15, 15, 15);

// Yellow (mouseover/hover)
SearchablePixel yellow = new SearchablePixel(60, 100, 50, 15, 15, 15);

// White (examine/neutral)
SearchablePixel white = new SearchablePixel(0, 0, 100, 15, 15, 15);

// Green (friendly)
SearchablePixel green = new SearchablePixel(120, 100, 50, 15, 15, 15);
```

### Tolerance Tuning
```java
// Not detecting? Increase tolerance
SearchablePixel color = new SearchablePixel(180, 100, 58, 20, 20, 20); // Higher
SearchablePixel color = new SearchablePixel(180, 100, 58, 25, 25, 25); // Even higher

// Too many false positives? Decrease tolerance
SearchablePixel color = new SearchablePixel(180, 100, 58, 10, 10, 10); // Lower
SearchablePixel color = new SearchablePixel(180, 100, 58, 5, 5, 5);    // Very strict
```

---

## 🔍 Basic Highlight Detection

### Finding Highlighted NPCs

```java
private SearchablePixel npcHighlight = new SearchablePixel(180, 100, 58, 15, 15, 15);

private List<HighlightedNPC> findHighlightedNPCs() {
    List<HighlightedNPC> targets = new ArrayList<>();
    
    UIResultList<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    if (npcPositions.isNotFound()) return targets;
    
    for (WorldPosition position : npcPositions) {
        // Create tile cube (100-150 height for most NPCs)
        Polygon poly = getSceneProjector().getTileCube(position, 120);
        if (poly == null) continue;
        
        // Get highlight bounds
        Rectangle bounds = getPixelAnalyzer().getHighlightBounds(poly, npcHighlight);
        if (bounds != null) {
            targets.add(new HighlightedNPC(position, bounds));
        }
    }
    
    return targets;
}

// Helper class to store NPC data
private class HighlightedNPC {
    WorldPosition position;
    Rectangle bounds;
    
    HighlightedNPC(WorldPosition pos, Rectangle b) {
        this.position = pos;
        this.bounds = b;
    }
}
```

### Verifying NPC Identity

```java
private HighlightedNPC findSpecificNPC(String targetName) {
    List<HighlightedNPC> npcs = findHighlightedNPCs();
    
    for (HighlightedNPC npc : npcs) {
        Polygon tileCube = getSceneProjector().getTileCube(npc.position, 120);
        if (tileCube == null) continue;
        
        // Verify NPC name before interacting
        MenuEntry response = getFinger().tapGetResponse(true, tileCube);
        if (response != null && response.getEntityName().contains(targetName)) {
            return npc;
        }
    }
    
    return null;
}
```

---

## ⚡ INSTANT Movement Detection (Critical for Guards/Thieving)

The FASTEST way to detect NPC movement - tracks pixel-by-pixel position changes.

### Core Movement Detection

```java
private Point lastNPCCenter = null;
private long lastCheckTime = 0;
private static final int MOVEMENT_THRESHOLD = 3; // pixels

private boolean hasNPCMoved(SearchablePixel highlight) {
    UIResultList<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    if (npcPositions.isNotFound()) return false;
    
    // Check all NPCs for the highlight
    for (WorldPosition npcPos : npcPositions) {
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 100);
        if (tileCube == null) continue;
        
        // Get highlight bounds
        Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, highlight);
        if (bounds == null) continue;
        
        // Get center of highlighted NPC
        Point currentCenter = bounds.getCenter();
        
        // First time seeing NPC
        if (lastNPCCenter == null) {
            lastNPCCenter = currentCenter;
            lastCheckTime = System.currentTimeMillis();
            return false;
        }
        
        // Calculate pixel distance moved
        int dx = Math.abs(currentCenter.x - lastNPCCenter.x);
        int dy = Math.abs(currentCenter.y - lastNPCCenter.y);
        
        // Check if moved beyond threshold
        if (dx > MOVEMENT_THRESHOLD || dy > MOVEMENT_THRESHOLD) {
            log("[ALERT] NPC moved! Distance: " + dx + "x, " + dy + "y");
            lastNPCCenter = currentCenter;
            lastCheckTime = System.currentTimeMillis();
            return true; // NPC IS MOVING!
        }
        
        // Update position for next check
        lastNPCCenter = currentCenter;
        lastCheckTime = System.currentTimeMillis();
    }
    
    return false;
}
```

### Complete Thieving Script with Guard Detection

```java
private enum ThievingState {
    STEALING,
    FLEEING,
    HIDING,
    RETURNING
}

private ThievingState state = ThievingState.STEALING;
private SearchablePixel guardHighlight = new SearchablePixel(180, 100, 58, 15, 15, 15);
private Point lastGuardCenter = null;
private int framesSinceLastCheck = 0;
private static final int CHECK_INTERVAL = 3; // Check every 3 frames

@Override
public int onLoop() {
    switch (state) {
        case STEALING:
            return doStealing();
        case FLEEING:
            return flee();
        case HIDING:
            return hide();
        case RETURNING:
            return returnToStall();
    }
    return 600;
}

@Override
public void onNewFrame() {
    // Check EVERY few frames for guard movement (instant detection)
    framesSinceLastCheck++;
    
    if (framesSinceLastCheck >= CHECK_INTERVAL && state == ThievingState.STEALING) {
        if (hasGuardMoved()) {
            log("[EMERGENCY] Guard is moving! Stopping theft!");
            state = ThievingState.FLEEING;
        }
        framesSinceLastCheck = 0;
    }
}

private boolean hasGuardMoved() {
    UIResultList<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    if (npcPositions.isNotFound()) return false;
    
    for (WorldPosition npcPos : npcPositions) {
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 100);
        if (tileCube == null) continue;
        
        Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, guardHighlight);
        if (bounds == null) continue;
        
        Point currentCenter = bounds.getCenter();
        
        if (lastGuardCenter == null) {
            lastGuardCenter = currentCenter;
            return false;
        }
        
        int dx = Math.abs(currentCenter.x - lastGuardCenter.x);
        int dy = Math.abs(currentCenter.y - lastGuardCenter.y);
        
        if (dx > 3 || dy > 3) {
            log("[GUARD ALERT] Guard moved! Distance: " + dx + "x, " + dy + "y");
            lastGuardCenter = currentCenter;
            return true;
        }
        
        lastGuardCenter = currentCenter;
    }
    
    return false;
}

private int doStealing() {
    // Find stall
    RSObject stall = getObjectManager().getClosestObject(getWorldPosition(), "Tea stall");
    if (stall == null) {
        log("Can't find stall");
        return 1000;
    }
    
    // Check guard position BEFORE stealing
    if (hasGuardMoved()) {
        log("Guard moved, aborting!");
        state = ThievingState.FLEEING;
        return 0;
    }
    
    // Steal from stall
    Polygon stallPoly = stall.getConvexHull();
    if (stallPoly != null) {
        getFinger().tap(stallPoly, "Steal-from");
        return RandomUtils.uniformRandom(300, 600);
    }
    
    return 1000;
}

private int flee() {
    log("Fleeing from guard!");
    
    WorldPosition safeSpot = new WorldPosition(1807, 3834, 0); // Your safe spot
    getWalker().walkTo(safeSpot);
    
    if (getWorldPosition().distanceTo(safeSpot) <= 2) {
        state = ThievingState.HIDING;
    }
    
    return 300;
}

private int hide() {
    log("Hiding, waiting for guard to calm down...");
    pollFramesHuman(() -> true, RandomUtils.gaussianRandom(2000, 4000, 3000, 500));
    
    if (!isGuardNearby()) {
        log("Guard gone, returning to stall");
        state = ThievingState.RETURNING;
    }
    
    return 1000;
}

private int returnToStall() {
    WorldPosition stallPos = new WorldPosition(1808, 3840, 0); // Your stall position
    
    if (getWorldPosition().distanceTo(stallPos) > 2) {
        getWalker().walkTo(stallPos);
        return 300;
    }
    
    // Reset guard tracking
    lastGuardCenter = null;
    state = ThievingState.STEALING;
    return 600;
}

private boolean isGuardNearby() {
    UIResultList<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    if (npcPositions.isNotFound()) return false;
    
    WorldPosition playerPos = getWorldPosition();
    
    for (WorldPosition npcPos : npcPositions) {
        if (npcPos.distanceTo(playerPos) > 8) continue;
        
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 100);
        if (tileCube == null) continue;
        
        Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, guardHighlight);
        if (bounds != null) {
            return true; // Guard nearby!
        }
    }
    
    return false;
}
```

---

## 🎮 Combat Pattern with Highlights

### Finding and Attacking Highlighted Enemies

```java
private SearchablePixel enemyHighlight = new SearchablePixel(180, 100, 58, 15, 15, 15);

private int attackHighlightedEnemy() {
    UIResultList<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    if (npcPositions.isNotFound()) {
        log("No NPCs on minimap");
        return 1000;
    }
    
    WorldPosition playerPos = getWorldPosition();
    List<HighlightedNPC> targets = new ArrayList<>();
    
    // Find all highlighted enemies
    for (WorldPosition npcPos : npcPositions) {
        // Skip if too far
        if (npcPos.distanceTo(playerPos) > 10) continue;
        
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 100);
        if (tileCube == null) continue;
        
        Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, enemyHighlight);
        if (bounds != null) {
            // Filter by size (avoid very small/large bounds)
            if (bounds.width >= 25 && bounds.width <= 150 &&
                bounds.height >= 25 && bounds.height <= 150) {
                targets.add(new HighlightedNPC(npcPos, bounds));
            }
        }
    }
    
    if (targets.isEmpty()) {
        log("No valid targets found");
        return 1000;
    }
    
    // Sort by distance (attack closest)
    targets.sort(Comparator.comparingInt(t -> t.position.distanceTo(playerPos)));
    
    // Attack closest target
    HighlightedNPC target = targets.get(0);
    
    // Verify it's attackable
    Polygon tileCube = getSceneProjector().getTileCube(target.position, 100);
    MenuEntry response = getFinger().tapGetResponse(true, tileCube);
    
    if (response != null && response.getAction().equalsIgnoreCase("Attack")) {
        log("Attacking: " + response.getEntityName());
        
        // CRITICAL: Tap the CENTER, not full bounds (prevents stale bounds issues)
        getFinger().tap(target.bounds.getCenter(), "Attack");
        
        // Wait for combat to engage
        pollFramesUntil(() -> isInCombat(), 3000);
        return 0;
    }
    
    return RandomUtils.uniformRandom(600, 1000);
}

private boolean isInCombat() {
    UIResultList<WorldPosition> npcs = getWidgetManager().getMinimap().getNPCPositions();
    if (npcs.isNotFound()) return false;
    
    WorldPosition playerPos = getWorldPosition();
    
    // In combat if NPC is within 1 tile
    for (WorldPosition npcPos : npcs) {
        if (npcPos.distanceTo(playerPos) <= 1) {
            return true;
        }
    }
    
    return false;
}
```

---

## 📍 Tracking Multiple NPCs

### Track All Guards/NPCs by Highlight

```java
private Map<WorldPosition, Point> trackedNPCs = new HashMap<>();
private SearchablePixel trackingHighlight = new SearchablePixel(180, 100, 58, 15, 15, 15);

private boolean hasAnyNPCMoved() {
    UIResultList<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    if (npcPositions.isNotFound()) return false;
    
    Map<WorldPosition, Point> currentNPCs = new HashMap<>();
    boolean anyMoved = false;
    
    for (WorldPosition npcPos : npcPositions) {
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 100);
        if (tileCube == null) continue;
        
        Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, trackingHighlight);
        if (bounds == null) continue;
        
        Point center = bounds.getCenter();
        currentNPCs.put(npcPos, center);
        
        // Check if this NPC moved
        Point lastPos = trackedNPCs.get(npcPos);
        if (lastPos != null) {
            int dx = Math.abs(center.x - lastPos.x);
            int dy = Math.abs(center.y - lastPos.y);
            
            if (dx > 3 || dy > 3) {
                log("[ALERT] NPC at " + npcPos + " moved " + dx + "x, " + dy + "y");
                anyMoved = true;
            }
        }
    }
    
    // Update tracked NPCs
    trackedNPCs = currentNPCs;
    
    return anyMoved;
}
```

### Tracking Specific NPC by Name

```java
private Point lastNamedNPCCenter = null;

private boolean hasNamedNPCMoved(String npcName, SearchablePixel highlight) {
    UIResultList<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    if (npcPositions.isNotFound()) return false;
    
    for (WorldPosition npcPos : npcPositions) {
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 100);
        if (tileCube == null) continue;
        
        Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, highlight);
        if (bounds == null) continue;
        
        // Verify it's the right NPC
        MenuEntry response = getFinger().tapGetResponse(true, tileCube);
        if (response == null || !response.getEntityName().contains(npcName)) {
            continue;
        }
        
        // Found the right NPC, check if moved
        Point currentCenter = bounds.getCenter();
        
        if (lastNamedNPCCenter == null) {
            lastNamedNPCCenter = currentCenter;
            return false;
        }
        
        int dx = Math.abs(currentCenter.x - lastNamedNPCCenter.x);
        int dy = Math.abs(currentCenter.y - lastNamedNPCCenter.y);
        
        if (dx > 3 || dy > 3) {
            log(npcName + " moved!");
            lastNamedNPCCenter = currentCenter;
            return true;
        }
        
        lastNamedNPCCenter = currentCenter;
        return false;
    }
    
    return false;
}
```

---

## ⚙️ Performance Optimization

### Frame-Based Checking (Recommended)

Checking highlights every frame is CPU intensive. Use interval-based checking:

```java
private int framesSinceLastCheck = 0;
private static final int CHECK_INTERVAL = 3; // Check every 3 frames

@Override
public void onNewFrame() {
    framesSinceLastCheck++;
    
    if (framesSinceLastCheck >= CHECK_INTERVAL) {
        if (hasNPCMoved()) {
            // React to movement
        }
        framesSinceLastCheck = 0;
    }
}
```

**Performance vs Detection Speed:**
- `CHECK_INTERVAL = 1`: Every frame (~50ms) - Most CPU intensive, instant detection
- `CHECK_INTERVAL = 3`: Every 3rd frame (~150ms) - Balanced, still very fast
- `CHECK_INTERVAL = 5`: Every 5th frame (~250ms) - Light CPU, still faster than minimap

### Conditional Checking

Only check when it matters:

```java
@Override
public void onNewFrame() {
    // Only check during risky activities
    if (state == ThievingState.STEALING || state == ThievingState.AT_STALL) {
        if (hasGuardMoved()) {
            state = ThievingState.FLEEING;
        }
    }
    
    // Don't check when hiding or fleeing (wasted CPU)
}
```

---

## 🔧 Movement Sensitivity Tuning

```java
// Ultra-sensitive (catches earliest hint of movement)
private static final int MOVEMENT_THRESHOLD = 2; // 2 pixels

// Sensitive (recommended for guards)
private static final int MOVEMENT_THRESHOLD = 3; // 3 pixels

// Normal (good balance)
private static final int MOVEMENT_THRESHOLD = 5; // 5 pixels

// Relaxed (only when clearly moving)
private static final int MOVEMENT_THRESHOLD = 10; // 10 pixels
```

**For thieving guards:** Use 3-5 pixels for best balance of detection speed and false positive reduction.

---

## 🐛 Troubleshooting

### Problem: Not Detecting Movement

**Causes:**
- Threshold too high
- Check interval too long
- Highlight color incorrect
- NPC not actually highlighted

**Solutions:**
```java
// Lower threshold
private static final int MOVEMENT_THRESHOLD = 2;

// Check more frequently
private static final int CHECK_INTERVAL = 1;

// Verify highlight color with Debug Tool
SearchablePixel color = new SearchablePixel(H, S, L, 20, 20, 20); // Higher tolerance

// Add logging to debug
log("Bounds: " + bounds + ", Center: " + bounds.getCenter());
```

### Problem: Too Many False Positives

**Causes:**
- Threshold too low
- Camera movement affecting pixel positions
- Other NPCs being detected

**Solutions:**
```java
// Higher threshold
private static final int MOVEMENT_THRESHOLD = 5;

// Filter by NPC name
MenuEntry response = getFinger().tapGetResponse(true, tileCube);
if (response != null && response.getEntityName().contains("Guard")) {
    // Only track guards
}

// Check multiple frames before reacting
private int movementFrameCount = 0;

if (npcMoved) {
    movementFrameCount++;
    if (movementFrameCount >= 2) {
        // Confirmed movement
        state = ThievingState.FLEEING;
    }
} else {
    movementFrameCount = 0;
}
```

### Problem: Misclicking NPCs

**Cause:** Bounds become stale between getting them and tapping

**Solution:** Always tap `.getCenter()`, never tap full bounds
```java
// ❌ DON'T: Tap full bounds
getFinger().tap(bounds, "Attack");

// ✅ DO: Tap center
getFinger().tap(bounds.getCenter(), "Attack");
```

### Problem: Null Bounds

**Causes:**
- NPC not highlighted
- NPC off-screen
- Tile cube invalid
- Wrong highlight color

**Solutions:**
```java
// Always null check
Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, highlight);
if (bounds == null) continue; // Skip this NPC

// Increase tolerance
SearchablePixel color = new SearchablePixel(180, 100, 58, 20, 20, 20);

// Increase tile cube height
Polygon tileCube = getSceneProjector().getTileCube(npcPos, 150); // was 100

// Verify highlight is enabled in game settings
```

---

## 📋 Best Practices

### General
1. **Always use Debug Tool** to get exact highlight colors
2. **Use `.getCenter()`** when tapping bounds (prevents stale bounds issues)
3. **Check in `onNewFrame()`** for instant movement detection
4. **Filter by bounds size** (skip very small/large bounds)
5. **Verify NPC name** with `tapGetResponse()` before interacting
6. **Add null checks** everywhere (bounds can be null)

### For Thieving/Guards
1. **Use 3-5 pixel threshold** for movement detection
2. **Check every 2-3 frames** for balance of speed and CPU
3. **Track guard position continuously** during risky actions
4. **Reset tracking** when changing states (fleeing/hiding)
5. **Have escape plan ready** when movement detected

### For Combat
1. **Sort targets by distance** (attack closest)
2. **Filter by bounds size** (25-150 pixels typical)
3. **Tap center of bounds** to avoid misclicks
4. **Don't attack if bounds very small** (NPC too far)
5. **Verify action is "Attack"** before tapping

### Performance
1. **Use frame intervals** (don't check every frame unless critical)
2. **Only check when needed** (conditional checking by state)
3. **Clean up tracking maps** (remove old entries)
4. **Consider CPU usage** vs detection speed needs

---

## 🚀 Why Highlight Detection is Superior

**vs Minimap Position Tracking:**
- ✅ 10-20x faster detection (50ms vs 600ms)
- ✅ Sub-tile movement detection
- ✅ Instant visual changes
- ✅ More accurate positioning

**vs Object Manager:**
- ✅ NPCs aren't in ObjectManager
- ✅ Works for any highlighted entity
- ✅ Real-time updates
- ✅ No reliance on game engine updates

**Use Cases:**
- Thieving (guard detection)
- Combat (targeting)
- Following NPCs
- Shop interactions
- Quest NPCs
- Any time-critical NPC interaction

---

*Highlight detection is the fastest and most reliable way to interact with NPCs in OSMB. Use it for any scenario where timing matters.*
