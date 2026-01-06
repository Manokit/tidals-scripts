# Specialized Activity Patterns

Patterns for minigames, altars, processing activities, and other specialized scripts.

## Altar and Offering Patterns

### House Altar Pattern

```java
private boolean offerBonesOnAltar() {
    // Find altar in house
    RSObject altar = getObjectManager().getClosestObject(
        getWorldPosition(), "Altar", "Gilded altar"
    );
    
    if (altar == null) {
        log("Can't find altar");
        return false;
    }
    
    // Get bones from inventory
    Inventory inv = getWidgetManager().getInventory();
    ItemGroupResult invSnapshot = inv.search(Set.of(ItemID.DRAGON_BONES));
    
    if (invSnapshot == null || !invSnapshot.contains(ItemID.DRAGON_BONES)) {
        log("No bones in inventory");
        return false;
    }
    
    SearchableItem bones = invSnapshot.getItem(ItemID.DRAGON_BONES);
    if (bones != null) {
        // Use bones on altar
        Polygon altarPoly = altar.getConvexHull();
        bones.useOn(altarPoly);
        
        // Wait for offering dialogue or action
        pollFramesUntil(() -> {
            // Check if bones were used (inventory count decreased)
            ItemGroupResult check = inv.search(Set.of(ItemID.DRAGON_BONES));
            return check == null || check.getAmount(ItemID.DRAGON_BONES) < 
                   invSnapshot.getAmount(ItemID.DRAGON_BONES);
        }, 3000);
        
        return true;
    }
    
    return false;
}
```

### Burner Management

```java
private boolean lightBurner(String burnerName) {
    RSObject burner = getObjectManager().getClosestObject(
        getWorldPosition(), burnerName
    );
    
    if (burner == null) return false;
    
    // Check if burner is already lit via menu
    Polygon burnerPoly = burner.getConvexHull();
    MenuEntry response = getFinger().tapGetResponse(true, burnerPoly);
    
    if (response != null) {
        String action = response.getAction();
        
        if (action.equalsIgnoreCase("Light")) {
            // Burner is unlit, light it
            getFinger().tap(burnerPoly, "Light");
            pollFramesHuman(() -> false, random(600, 1000));
            return true;
        } else {
            // Already lit
            return true;
        }
    }
    
    return false;
}

private boolean ensureBurnersLit() {
    // Light both burners in POH
    String[] burners = {"Incense burner", "Incense burner"};
    
    for (String burner : burners) {
        if (!lightBurner(burner)) {
            log("Failed to light burner");
            return false;
        }
    }
    
    return true;
}
```

---

## Resource Processing Patterns

### Sawmill Pattern

```java
private enum ProcessingState {
    GETTING_LOGS,
    RUNNING_TO_SAWMILL,
    PROCESSING,
    BANKING_PLANKS
}

private int runSawmill() {
    ProcessingState state = getSawmillState();
    
    switch (state) {
        case GETTING_LOGS:
            return withdrawLogs();
        case RUNNING_TO_SAWMILL:
            return walkToSawmill();
        case PROCESSING:
            return processLogs();
        case BANKING_PLANKS:
            return bankPlanks();
        default:
            return 600;
    }
}

private boolean processLogs() {
    // Find sawmill operator NPC
    List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    
    for (WorldPosition npcPos : npcPositions) {
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
        if (tileCube == null) continue;
        
        MenuEntry response = getFinger().tapGetResponse(true, tileCube);
        if (response != null && response.getEntityName().contains("Sawmill operator")) {
            // Talk to operator
            getFinger().tap(tileCube, "Talk-to");
            
            // Wait for dialogue
            boolean dialogueAppeared = pollFramesUntil(() -> 
                getWidgetManager().getDialogue().isVisible(), 5000);
            
            if (dialogueAppeared) {
                // Handle dialogue for plank type
                handleSawmillDialogue();
                return true;
            }
        }
    }
    
    return false;
}

private void handleSawmillDialogue() {
    Dialogue dialogue = getWidgetManager().getDialogue();
    
    // Navigate through sawmill dialogue options
    if (dialogue.getDialogueType() == DialogueType.OPTION) {
        dialogue.selectOption("Buy planks"); // Or specific plank type
        pollFramesHuman(() -> false, random(600, 1200));
    }
}
```

### Winemaking Pattern

```java
private boolean makeWine() {
    Inventory inv = getWidgetManager().getInventory();
    ItemGroupResult invSnapshot = inv.search(Set.of(
        ItemID.JUG_OF_WATER, 
        ItemID.GRAPES
    ));
    
    if (invSnapshot == null) return false;
    
    SearchableItem jug = invSnapshot.getItem(ItemID.JUG_OF_WATER);
    SearchableItem grapes = invSnapshot.getItem(ItemID.GRAPES);
    
    if (jug != null && grapes != null) {
        // Use grapes on jug
        grapes.useOn(jug.getBounds());
        
        // Wine takes 12 seconds to ferment
        log("Wine fermenting, waiting...");
        pollFramesHuman(() -> false, random(12000, 13000));
        
        return true;
    }
    
    return false;
}

private boolean makeAllWine() {
    int winesMade = 0;
    
    while (hasIngredients()) {
        makeWine();
        winesMade++;
        
        // Small delay between makes
        pollFramesHuman(() -> false, random(300, 600));
    }
    
    log("Made " + winesMade + " wines, waiting for fermentation");
    
    // Wait for all wine to ferment
    return pollFramesUntil(() -> {
        ItemGroupResult inv = getWidgetManager().getInventory()
            .search(Set.of(ItemID.UNFERMENTED_WINE));
        return inv == null || !inv.contains(ItemID.UNFERMENTED_WINE);
    }, 15000);
}

private boolean hasIngredients() {
    ItemGroupResult inv = getWidgetManager().getInventory()
        .search(Set.of(ItemID.JUG_OF_WATER, ItemID.GRAPES));
    
    return inv != null && 
           inv.contains(ItemID.JUG_OF_WATER) && 
           inv.contains(ItemID.GRAPES);
}
```

---

## Minigame Patterns

### Castle Wars AFK Pattern

```java
private boolean isInCastleWars() {
    // Check if we're in Castle Wars area
    WorldPosition pos = getWorldPosition();
    RectangleArea castleWarsArea = new RectangleArea(2368, 3072, 2431, 3135, 0);
    return castleWarsArea.contains(pos);
}

private int afkCastleWars() {
    // Simple AFK - just stay in game
    if (!isInCastleWars()) {
        log("Not in Castle Wars area");
        stop();
        return 0;
    }
    
    // Check for game end dialogue
    Dialogue dialogue = getWidgetManager().getDialogue();
    if (dialogue.isVisible()) {
        dialogue.clickContinue();
        pollFramesHuman(() -> false, random(1000, 2000));
    }
    
    // Move randomly to avoid logout
    if (random(100) < 5) { // 5% chance per poll
        WorldPosition currentPos = getWorldPosition();
        WorldPosition randomNearby = new WorldPosition(
            currentPos.getX() + random(-3, 3),
            currentPos.getY() + random(-3, 3),
            currentPos.getPlane()
        );
        getWalker().walkTo(randomNearby);
    }
    
    return RandomUtils.uniformRandom(5000, 10000); // Long delays for AFK
}
```

### Pest Control Pattern

```java
private boolean isInPestControl() {
    // Pest Control is instanced - check via custom map
    WorldPosition pos = getWorldPosition();
    // Pest Control void knight area
    return pos.getX() >= 2624 && pos.getX() <= 2687;
}

private int doPestControl() {
    if (!isInPestControl()) {
        return waitForGame();
    }
    
    // Attack portals or void knight defense
    return attackNearestPortal();
}

private int attackNearestPortal() {
    // Portals appear as NPCs
    List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    
    SearchablePixel portalHighlight = new SearchablePixel(300, 100, 50, 10, 10, 10); // Purple
    
    for (WorldPosition npcPos : npcPositions) {
        if (npcPos.distanceTo(getWorldPosition()) > 20) continue;
        
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 100);
        if (tileCube == null) continue;
        
        Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, portalHighlight);
        if (bounds != null) {
            getFinger().tap(bounds, "Attack");
            pollFramesHuman(() -> false, random(600, 1200));
            return 0;
        }
    }
    
    return RandomUtils.uniformRandom(1000, 2000);
}
```

---

## Smelting and Furnace Patterns

### Blast Furnace Pattern

```java
private boolean useBlastFurnace() {
    // Find conveyor belt
    RSObject conveyor = getObjectManager().getClosestObject(
        getWorldPosition(), "Conveyor belt"
    );
    
    if (conveyor == null) return false;
    
    // Get ore from inventory
    Inventory inv = getWidgetManager().getInventory();
    ItemGroupResult invSnapshot = inv.search(Set.of(ItemID.IRON_ORE));
    
    if (invSnapshot == null || !invSnapshot.contains(ItemID.IRON_ORE)) {
        return false;
    }
    
    SearchableItem ore = invSnapshot.getItem(ItemID.IRON_ORE);
    if (ore != null) {
        // Use ore on conveyor
        Polygon conveyorPoly = conveyor.getConvexHull();
        ore.useOn(conveyorPoly);
        
        // Wait for ore to be deposited
        pollFramesHuman(() -> false, random(600, 1000));
        return true;
    }
    
    return false;
}

private boolean collectBars() {
    // Find bar dispenser
    RSObject dispenser = getObjectManager().getClosestObject(
        getWorldPosition(), "Bar dispenser"
    );
    
    if (dispenser == null) return false;
    
    Polygon dispenserPoly = dispenser.getConvexHull();
    if (dispenserPoly != null) {
        getFinger().tap(dispenserPoly, "Take");
        
        // Wait for bars to be collected
        return pollFramesUntil(() -> {
            ItemGroupResult inv = getWidgetManager().getInventory()
                .search(Set.of(ItemID.STEEL_BAR));
            return inv != null && inv.contains(ItemID.STEEL_BAR);
        }, 5000);
    }
    
    return false;
}
```

### Cannonball Smelting

```java
private boolean smeltCannonballs() {
    // Find furnace
    RSObject furnace = getObjectManager().getClosestObject(
        getWorldPosition(), "Furnace"
    );
    
    if (furnace == null) return false;
    
    // Check for steel bars in inventory
    Inventory inv = getWidgetManager().getInventory();
    ItemGroupResult invSnapshot = inv.search(Set.of(
        ItemID.STEEL_BAR, 
        ItemID.AMMO_MOULD
    ));
    
    if (invSnapshot == null || 
        !invSnapshot.contains(ItemID.STEEL_BAR) ||
        !invSnapshot.contains(ItemID.AMMO_MOULD)) {
        return false;
    }
    
    // Click furnace
    Polygon furnacePoly = furnace.getConvexHull();
    if (furnacePoly != null) {
        getFinger().tap(furnacePoly, "Smelt");
        
        // Wait for smelting dialogue
        boolean dialogueAppeared = pollFramesUntil(() -> 
            getWidgetManager().getDialogue().isVisible(), 3000);
        
        if (dialogueAppeared) {
            // Select cannonball option
            Dialogue dialogue = getWidgetManager().getDialogue();
            if (dialogue.getDialogueType() == DialogueType.ITEM_OPTION) {
                dialogue.selectItem(ItemID.CANNONBALL);
                
                // Wait for smelting to complete
                return waitForSmeltingComplete();
            }
        }
    }
    
    return false;
}

private boolean waitForSmeltingComplete() {
    // Cannonballs take ~3 minutes per inventory
    return pollFramesUntil(() -> {
        ItemGroupResult inv = getWidgetManager().getInventory()
            .search(Set.of(ItemID.STEEL_BAR));
        return inv == null || !inv.contains(ItemID.STEEL_BAR);
    }, 200000); // 200 second timeout
}
```

---

## Cooking Patterns

### Range/Fire Cooking

```java
private boolean cookFood(int rawFoodId, int cookedFoodId) {
    // Find cooking source
    RSObject cookingSource = getObjectManager().getClosestObject(
        getWorldPosition(), "Range", "Fire"
    );
    
    if (cookingSource == null) return false;
    
    // Get raw food from inventory
    Inventory inv = getWidgetManager().getInventory();
    ItemGroupResult invSnapshot = inv.search(Set.of(rawFoodId));
    
    if (invSnapshot == null || !invSnapshot.contains(rawFoodId)) {
        return false;
    }
    
    SearchableItem rawFood = invSnapshot.getItem(rawFoodId);
    if (rawFood != null) {
        // Use food on range
        Polygon rangePoly = cookingSource.getConvexHull();
        rawFood.useOn(rangePoly);
        
        // Wait for cooking to complete
        return pollFramesUntil(() -> {
            ItemGroupResult check = getWidgetManager().getInventory()
                .search(Set.of(rawFoodId));
            return check == null || !check.contains(rawFoodId);
        }, 60000); // 60 second timeout for full inventory
    }
    
    return false;
}
```

---

## Hunter Patterns

### Herbiboar Tracking

```java
private boolean followTrack() {
    // Find herbiboar tracks/tunnel
    RSObject track = getObjectManager().getClosestObject(
        getWorldPosition(), 
        "Herbiboar tracks",
        "Herbiboar tunnel entrance"
    );
    
    if (track == null) {
        log("No track found");
        return false;
    }
    
    // Interact with track
    Polygon trackPoly = track.getConvexHull();
    if (trackPoly != null) {
        MenuEntry response = getFinger().tapGetResponse(true, trackPoly);
        if (response != null) {
            getFinger().tap(trackPoly, response.getAction());
            
            // Wait for next track to appear
            pollFramesHuman(() -> false, random(2000, 3000));
            return true;
        }
    }
    
    return false;
}

private boolean setTrap() {
    // Use object on tunnel
    Inventory inv = getWidgetManager().getInventory();
    ItemGroupResult invSnapshot = inv.search(Set.of(ItemID.LOGS));
    
    if (invSnapshot == null) return false;
    
    SearchableItem logs = invSnapshot.getItem(ItemID.LOGS);
    RSObject tunnel = getObjectManager().getClosestObject(
        getWorldPosition(), "Herbiboar tunnel"
    );
    
    if (logs != null && tunnel != null) {
        logs.useOn(tunnel.getConvexHull());
        pollFramesHuman(() -> false, random(1000, 2000));
        return true;
    }
    
    return false;
}
```

---

## Best Practices for Specialized Activities

### Altar Scripts
1. **Light burners first** before offering bones
2. **Handle "yes/no" dialogues** for bone offering
3. **Track bones used** for paint overlay
4. **Use house teleport tabs** for speed

### Processing Scripts
1. **Bank near processing location** for efficiency
2. **Handle payment dialogues** (sawmill, tanning, etc.)
3. **Track processing costs** vs profit
4. **Wait for processing to complete** (wine fermentation, etc.)

### Minigames
1. **Check if in minigame area** before actions
2. **Handle end-game dialogues** automatically
3. **Add movement for AFK** to prevent logout
4. **Track points/rewards** gained

### Smelting
1. **Check for required tools** (ammo mould, etc.)
2. **Wait appropriately** for long smelt times
3. **Handle running out of materials** mid-smelt
4. **Bank efficiently** near furnace

---

*These specialized patterns handle unique activities not covered in core documentation.*
