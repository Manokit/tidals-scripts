# Walking, NPCs, and Object Interaction

Complete guide to pathfinding, NPC interaction, and object finding.

## NPC Finding and Interaction (DETAILED)

**CRITICAL**: NPCs are NOT in ObjectManager! They move around and must be detected differently.

### Method 1: Minimap Positions + Tile Cubes (RECOMMENDED)

This is the most reliable method for finding and interacting with moving NPCs.

#### Basic Pattern
```java
// Step 1: Get NPC positions from minimap
List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

// Step 2: Create tile cubes for each position
for (WorldPosition npcPos : npcPositions) {
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60); // 60 = height
    if (tileCube == null) continue;
    
    // Step 3: Check what's at this position using tapGetResponse
    MenuEntry response = getFinger().tapGetResponse(true, tileCube);
    if (response != null) {
        String npcName = response.getEntityName();
        String action = response.getAction();
        
        // Step 4: Verify it's the NPC we want
        if (npcName.contains("Guard")) {
            getFinger().tap(tileCube, "Talk-to");
            break;
        }
    }
}
```

### Method 2: Minimap + Highlight Bounds (For Highlighted NPCs)

If NPCs are highlighted (like in combat), use highlight detection.

```java
// Step 1: Define highlight colors (use Debug Tool to find these)
SearchablePixel[] highlightColors = {
    new SearchablePixel(0, 100, 50, 10, 10, 10),   // Red highlight (HSL)
    new SearchablePixel(60, 100, 50, 10, 10, 10)   // Yellow highlight
};

// Step 2: Get NPC positions from minimap
List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

// Step 3: Find highlighted NPCs
for (WorldPosition npcPos : npcPositions) {
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
    if (tileCube == null) continue;
    
    // Get highlight bounds within the tile cube
    Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, highlightColors);
    
    if (bounds != null) {
        // Found a highlighted NPC at this position
        getFinger().tap(bounds, "Attack");
        break;
    }
}
```

### Method 3: Pixel Clusters (Advanced)

For more precise detection when highlights exist.

```java
SearchablePixel highlightColor = new SearchablePixel(0, 100, 50, 10, 10, 10);

List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

for (WorldPosition npcPos : npcPositions) {
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
    if (tileCube == null) continue;
    
    // Find pixel clusters of the highlight color
    List<PixelCluster> clusters = getPixelAnalyzer().findClusters(
        tileCube,           // Search area
        100,                // Minimum cluster size
        0.8,                // Minimum density (0.0-1.0)
        highlightColor      // Color to find
    );
    
    if (!clusters.isEmpty()) {
        // Found clustered highlight pixels - likely an NPC
        PixelCluster largest = clusters.get(0); // First is largest
        Rectangle bounds = largest.getBounds();
        getFinger().tap(bounds, "Attack");
        break;
    }
}
```

### Handling Large NPCs (2x2, 3x3, etc.)

For NPCs that take up multiple tiles:

```java
WorldPosition npcPos = // ... from minimap

// For 2x2 NPC
Polygon largeCube = getSceneProjector().getTileCube(
    npcPos.getX(), npcPos.getY(), 0,  // position
    2, 2,          // tile width, height (2x2 NPC)
    0, 60,         // z offset, height
    false          // don't require all vertices on screen
);

// For 3x3 NPC (like bosses)
Polygon hugeCube = getSceneProjector().getTileCube(
    npcPos.getX(), npcPos.getY(), 0,
    3, 3,          // 3x3 tiles
    0, 100,        // taller height for big monsters
    false
);
```

### Complete Example: Combat Script

```java
private boolean attackNearestEnemy() {
    // Define what we're looking for
    SearchablePixel redHighlight = new SearchablePixel(0, 100, 50, 10, 10, 10);
    
    // Get all NPC positions from minimap
    List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    
    if (npcPositions.isEmpty()) {
        log("No NPCs visible on minimap");
        return false;
    }
    
    WorldPosition playerPos = getWorldPosition();
    
    // Sort by distance (closest first)
    npcPositions.sort(Comparator.comparingInt(pos -> pos.distanceTo(playerPos)));
    
    for (WorldPosition npcPos : npcPositions) {
        // Skip if too far
        if (npcPos.distanceTo(playerPos) > 10) continue;
        
        // Create tile cube
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
        if (tileCube == null) continue;
        
        // Check if this NPC is highlighted (attackable)
        Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, redHighlight);
        if (bounds == null) continue;
        
        // Verify we can click it
        MenuEntry response = getFinger().tapGetResponse(true, tileCube);
        if (response == null) continue;
        
        String npcName = response.getEntityName();
        String action = response.getAction();
        
        // Check if it's an enemy we want to attack
        if (npcName.contains("Vyre") && action.equalsIgnoreCase("Attack")) {
            log("Attacking " + npcName);
            getFinger().tap(bounds, "Attack");
            return true;
        }
    }
    
    log("No valid targets found");
    return false;
}
```

### Finding Specific NPCs by Name

```java
private WorldPosition findNPCByName(String targetName) {
    List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    
    for (WorldPosition npcPos : npcPositions) {
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
        if (tileCube == null) continue;
        
        // Use tapGetResponse to check the name without clicking
        MenuEntry response = getFinger().tapGetResponse(true, tileCube);
        if (response == null) continue;
        
        String npcName = response.getEntityName();
        if (npcName.equalsIgnoreCase(targetName) || npcName.contains(targetName)) {
            log("Found " + targetName + " at " + npcPos);
            return npcPos;
        }
    }
    
    return null; // Not found
}

// Usage:
WorldPosition guardPos = findNPCByName("Guard");
if (guardPos != null) {
    getWalker().walkTo(guardPos);
}
```

### Troubleshooting NPC Detection

#### Problem: "Can't find NPCs even though they're highlighted"

**Solution 1**: Use Debug Tool to find exact highlight colors
```java
// Sample highlight color from your game screen
// Adjust HSL tolerance if needed (start with 10, increase to 20-25 if needed)
SearchablePixel highlight = new SearchablePixel(
    hue,         // 0-360 (use Debug Tool)
    saturation,  // 0-100
    lightness,   // 0-100
    10, 10, 10   // tolerance for each
);
```

**Solution 2**: Expand search area
```java
// Increase tile cube height for taller NPCs
Polygon tileCube = getSceneProjector().getTileCube(npcPos, 100); // was 60
```

**Solution 3**: Use pixel clusters instead of highlight bounds
```java
// Sometimes getHighlightBounds fails, but clusters work
List<PixelCluster> clusters = getPixelAnalyzer().findClusters(
    tileCube, 50, 0.7, highlightColor
);
```

#### Problem: "tapGetResponse returns null"

**Causes**:
1. NPC moved since position was retrieved from minimap
2. Camera angle makes NPC off-screen
3. Tile cube doesn't encompass the full NPC

**Solutions**:
```java
// Check if tile cube is valid before using
if (tileCube == null || tileCube.size() < 3) {
    continue; // Skip this position
}

// Verify polygon is on screen
double visibility = getWidgetManager().insideGameScreenFactor(
    tileCube, List.of(ChatboxComponent.class)
);
if (visibility < 0.5) {
    continue; // NPC mostly off-screen
}
```

#### Problem: "NPCs positions from minimap are inaccurate"

**Note**: Minimap positions update every frame but may have 1-tile margin of error.

**Solution**: Expand tile search or verify with tapGetResponse
```java
// Search a 3x3 area around the minimap position
for (int dx = -1; dx <= 1; dx++) {
    for (int dy = -1; dy <= 1; dy++) {
        WorldPosition searchPos = new WorldPosition(
            npcPos.getX() + dx,
            npcPos.getY() + dy,
            npcPos.getPlane()
        );
        
        Polygon cube = getSceneProjector().getTileCube(searchPos, 60);
        // Check this position...
    }
}
```

### Best Practices for NPC Interaction

1. **Always get positions from minimap** - NPCs aren't in ObjectManager
2. **Use tapGetResponse to verify** - Check name before attacking
3. **Sort by distance** - Attack closest NPCs first
4. **Check highlight bounds** - Confirm NPC is attackable/visible
5. **Handle NPC movement** - They might move between checks
6. **Use appropriate tile sizes** - 1x1 for most, 2x2+ for large NPCs
7. **Add null checks** - tileCube, bounds, response can all be null
8. **Adjust highlight colors** - Use Debug Tool to find exact HSL values

### Anti-Ban for NPC Interaction

```java
// Don't attack the exact same NPC every time
Collections.shuffle(npcPositions); // Randomize order

// Add reaction delay after killing
if (npcDied) {
    submitTask(() -> false, RandomUtils.uniformRandom(300, 800));
}

// Vary attack range
int attackRange = RandomUtils.uniformRandom(5, 8); // Not always max range
```

---

## Object Finding and Interaction

### Finding Objects

```java
ObjectManager objects = getObjectManager();

// By name (closest)
RSObject tree = objects.getClosestObject(getWorldPosition(), "Tree", "Oak tree");

// By predicate
RSObject specific = objects.getRSObject(o -> 
    o.getName().equals("Bank booth") && 
    o.getId() == 12345
);

// All matching
List<RSObject> allTrees = objects.getObjects(o -> o.getName().contains("tree"));

// Optional version
Optional<RSObject> maybeTree = objects.getObject(o -> o.getName().equals("Tree"));
maybeTree.ifPresent(tree -> tree.interact("Chop down"));
```

### RSObject Methods
```java
RSObject object = ...;

object.getName();
object.getId();
object.getWorldPosition();
object.getActions();            // Available actions
object.getConvexHull();         // Polygon for clicking
object.exists();                // Still valid?
object.interact("Action");      // Click with action
object.interact(null, null, "Action"); // Alternative
object.canReach();              // Is object reachable?
```

### Safe Object Interaction Pattern
```java
RSObject rock = getObjectManager().getClosestObject(
    getWorldPosition(), "Copper rocks", "Tin rocks"
);

if (rock == null) {
    log("No rocks found");
    return 1000;
}

// Shrink click area to avoid misclicks
Polygon rockPoly = rock.getConvexHull();
if (rockPoly != null) {
    Polygon shrunk = rockPoly.getResized(0.7); // 70% size
    if (shrunk != null) {
        // Verify we clicked the right thing
        MenuEntry response = getFinger().tapGetResponse(true, shrunk);
        if (response != null && response.getAction().equalsIgnoreCase("Mine")) {
            // Wait for rock to deplete
            submitTask(() -> !rock.exists(), 30000);
        }
    }
}
```

---

## NPC Interaction Quick Reference

**See detailed NPC section above for complete patterns!**

### Quick Pattern (Most Common)
```java
List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

for (WorldPosition npcPos : npcPositions) {
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
    if (tileCube == null) continue;
    
    MenuEntry response = getFinger().tapGetResponse(true, tileCube);
    if (response != null && response.getEntityName().contains("Guard")) {
        getFinger().tap(tileCube, "Talk-to");
        break;
    }
}
```

**For more advanced NPC detection (highlights, pixel clusters, large NPCs), see the detailed NPC section at the top of this document.**

---

## Walking and Pathfinding

### Basic Walking
```java
// Walk to specific position
WorldPosition destination = new WorldPosition(3253, 3420, 0);
getWalker().walkTo(destination);

// Walk to area
RectangleArea area = new RectangleArea(3280, 3360, 3290, 3370, 0);
getWalker().walkTo(area.getRandomPosition());
```

### Walking with Break Conditions
```java
WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> {
        // Stop walking when we find what we're looking for
        return getObjectManager().getClosestObject(getWorldPosition(), "Tree") != null;
    })
    .breakDistance(5)         // Stop within 5 tiles
    .tileRandomisationRadius(2) // Randomize destination by 2 tiles
    .build();

getWalker().walkTo(destination, config);
```

### Walking to Bank Example
```java
private int walkToBank() {
    WalkConfig config = new WalkConfig.Builder()
        .breakCondition(() -> getWidgetManager().getBank().isVisible())
        .enableRun(true)
        .build();
    getWalker().walkTo(BANK_POS, config);
    return 600;
}
```

### Walking with Tile Randomization
```java
// Don't click same exact spot every time
WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> MINE_AREA.contains(getWorldPosition()))
    .tileRandomisationRadius(3)  // Randomize within 3 tiles
    .build();
getWalker().walkTo(MINE_AREA.getRandomPosition(), config);
```

### Walking with Visibility Check
```java
WalkConfig config = new WalkConfig.Builder()
    .disableWalkScreen(true)  // Don't use walk screen
    .breakCondition(() -> {
        Polygon hull = object.getConvexHull();
        return hull != null &&
            getWidgetManager().insideGameScreenFactor(
                hull, List.of(ChatboxComponent.class)) >= 1.0;
    })
    .enableRun(true)
    .build();
getWalker().walkTo(object.getWorldPosition(), config);
```

---

## Area Definitions

### RectangleArea
```java
RectangleArea mineArea = new RectangleArea(
    3280,  // x1
    3360,  // y1
    3290,  // x2
    3370,  // y2
    0      // plane
);

// Check if position is in area
if (mineArea.contains(getWorldPosition())) {
    // Player is in mining area
}

// Get random position in area
WorldPosition randomPos = mineArea.getRandomPosition();
```

### PolyArea (Custom Shapes)
```java
// Define custom polygon area
List<WorldPosition> vertices = Arrays.asList(
    new WorldPosition(3250, 3420, 0),
    new WorldPosition(3260, 3420, 0),
    new WorldPosition(3260, 3430, 0),
    new WorldPosition(3250, 3430, 0)
);

PolyArea customArea = new PolyArea(vertices, 0);
```

---

## Multi-Step Travel Sequence

**Use Case**: Complex travel requiring boats, NPCs, dialogue choices.

```java
private boolean travelToIsland() {
    // Step 1: Find and interact with boat
    List<RSObject> rowboats = getObjectManager().getObjects(obj ->
        "Rowboat".equalsIgnoreCase(obj.getName()) &&
        obj.getActions() != null &&
        Arrays.asList(obj.getActions()).contains("Travel") &&
        obj.canReach()
    );

    RSObject rowboat = (RSObject) getUtils().getClosest(rowboats);
    if (rowboat == null) {
        log(getClass(), "No rowboat found!");
        return false;
    }

    if (!rowboat.interact("Travel")) {
        return false;
    }

    // Step 2: Wait for dialogue to appear
    boolean dialogueAppeared = pollFramesUntil(() -> {
        DialogueType type = getWidgetManager().getDialogue().getDialogueType();
        return type == DialogueType.TEXT_OPTION;
    }, random(6000, 10000));

    if (!dialogueAppeared) {
        log(getClass(), "Dialogue did not appear!");
        return false;
    }

    // Step 3: Select correct travel option
    boolean selected = getWidgetManager().getDialogue()
        .selectOption("Row out to sea, north of the island");

    if (!selected) {
        log(getClass(), "Failed to select dialogue option!");
        return false;
    }

    // Step 4: Wait for arrival
    RectangleArea destinationArea = new RectangleArea(2800, 2700, 2850, 2750, 0);
    boolean success = pollFramesUntil(() -> {
        WorldPosition current = getWorldPosition();
        return current != null && destinationArea.contains(current);
    }, random(14000, 17500));

    if (!success) {
        log(getClass(), "Travel timeout - may have failed!");
    }

    return success;
}
```

---

## Common Patterns

### Check if Player Can Reach Object
```java
RSObject tree = getObjectManager().getClosestObject(getWorldPosition(), "Tree");
if (tree != null && tree.canReach()) {
    tree.interact("Chop down");
} else {
    // Walk closer
    getWalker().walkTo(tree.getWorldPosition());
}
```

### Wait for Movement to Stop
```java
// After clicking an object, wait for player to reach it
WorldPosition startPos = getWorldPosition();
submitTask(() -> {
    WorldPosition current = getWorldPosition();
    return current != null && !current.equals(startPos);
}, 5000);
```

### Detect Stuck Player
```java
AtomicReference<WorldPosition> lastPos = new AtomicReference<>(null);
AtomicReference<Timer> stuckTimer = new AtomicReference<>(new Timer());

boolean isStuck = pollFramesUntil(() -> {
    WorldPosition current = getWorldPosition();
    if (current == null) return false;
    
    if (!current.equals(lastPos.get())) {
        lastPos.set(current);
        stuckTimer.get().reset();
    }
    
    // Player stuck for 5+ seconds
    return stuckTimer.get().timeElapsed() > 5000;
}, 10000);

if (isStuck) {
    // Handle stuck situation
    log("Player appears stuck, taking corrective action");
}
```

---

## Anti-Ban Walking Practices

### Don't Click Same Exact Spot
```java
// Use areas instead of fixed tiles for walking
RectangleArea area = new RectangleArea(x1, y1, x2, y2, 0);
WorldPosition randomInArea = area.getRandomPosition();
getWalker().walkTo(randomInArea);
```

### Add Reaction Delays
```java
// WRONG - No delay after reaching destination
if (MINE_AREA.contains(getWorldPosition())) {
    // Immediately start mining
}

// RIGHT - Add reaction delay
if (MINE_AREA.contains(getWorldPosition())) {
    submitTask(() -> false, RandomUtils.uniformRandom(200, 600));
    // Then start mining
}
```

---

## Best Practices

1. **Always null-check objects** before interacting
2. **Shrink polygons** when zoomed out to avoid misclicks
3. **Use tapGetResponse** to verify what was clicked
4. **Get NPC positions from minimap**, not ObjectManager
5. **Add break conditions** to walking for efficiency
6. **Randomize destinations** within areas for anti-ban
7. **Check object.canReach()** before interacting
8. **Verify visibility** of objects before clicking
