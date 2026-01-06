# Advanced Techniques - Ground Items, Agility & More

Patterns for specialized activities not covered in the main documentation.

## Ground Item Detection and Looting

### Finding Ground Items

Ground items appear in the SceneManager, not ObjectManager.

```java
// Get ground items in current scene
List<RSGroundItem> groundItems = getSceneManager().getGroundItems();

// Filter by item ID
List<RSGroundItem> coins = groundItems.stream()
    .filter(item -> item.getId() == ItemID.COINS)
    .collect(Collectors.toList());

// Get closest ground item
RSGroundItem closest = groundItems.stream()
    .min(Comparator.comparingInt(item -> 
        item.getWorldPosition().distanceTo(getWorldPosition())
    ))
    .orElse(null);
```

### Interacting with Ground Items

```java
private boolean lootItem(int itemId) {
    List<RSGroundItem> items = getSceneManager().getGroundItems();
    
    RSGroundItem target = items.stream()
        .filter(item -> item.getId() == itemId)
        .filter(item -> item.getWorldPosition().distanceTo(getWorldPosition()) <= 10)
        .findFirst()
        .orElse(null);
    
    if (target == null) return false;
    
    // Get the polygon for the ground item
    Polygon itemPoly = target.getConvexHull();
    if (itemPoly == null) return false;
    
    // Verify what we're clicking
    MenuEntry response = getFinger().tapGetResponse(true, itemPoly);
    if (response != null && response.getAction().equalsIgnoreCase("Take")) {
        getFinger().tap(itemPoly, "Take");
        
        // Wait for item to be picked up
        return submitTask(() -> {
            ItemGroupResult inv = getWidgetManager().getInventory().search(Set.of(itemId));
            return inv != null && inv.contains(itemId);
        }, 3000);
    }
    
    return false;
}
```

### Looting Multiple Items

```java
private int lootValuableItems() {
    // Define valuable items to loot
    Set<Integer> valuableItems = Set.of(
        ItemID.COINS,
        ItemID.RUNE_PLATEBODY,
        ItemID.RUNE_SCIMITAR,
        ItemID.DRAGON_BONES
    );
    
    List<RSGroundItem> groundItems = getSceneManager().getGroundItems();
    WorldPosition playerPos = getWorldPosition();
    
    // Filter and sort by distance
    List<RSGroundItem> nearbyLoot = groundItems.stream()
        .filter(item -> valuableItems.contains(item.getId()))
        .filter(item -> item.getWorldPosition().distanceTo(playerPos) <= 15)
        .sorted(Comparator.comparingInt(item -> 
            item.getWorldPosition().distanceTo(playerPos)))
        .collect(Collectors.toList());
    
    for (RSGroundItem loot : nearbyLoot) {
        // Check if inventory has space
        ItemGroupResult inv = getWidgetManager().getInventory().search(Set.of());
        if (inv != null && inv.isFull()) {
            log("Inventory full, stopping loot");
            break;
        }
        
        Polygon itemPoly = loot.getConvexHull();
        if (itemPoly != null) {
            getFinger().tap(itemPoly, "Take");
            pollFramesHuman(() -> false, random(300, 600));
        }
    }
    
    return RandomUtils.uniformRandom(400, 800);
}
```

### Loot Tracking Pattern

```java
private Set<WorldPosition> lootedPositions = new HashSet<>();

private boolean shouldLootItem(RSGroundItem item) {
    WorldPosition itemPos = item.getWorldPosition();
    
    // Don't loot same position twice
    if (lootedPositions.contains(itemPos)) {
        return false;
    }
    
    // Check if item is valuable enough
    int value = getItemManager().getItemValue(item.getId());
    if (value < minLootValue) {
        return false;
    }
    
    // Mark as looted
    lootedPositions.add(itemPos);
    return true;
}

// Clean up old positions periodically
private void cleanupLootedPositions() {
    WorldPosition playerPos = getWorldPosition();
    lootedPositions.removeIf(pos -> pos.distanceTo(playerPos) > 30);
}
```

---

## Agility Obstacle Detection

### Finding Agility Obstacles

```java
private RSObject findNextObstacle(String... obstacleNames) {
    // Get all objects matching obstacle names
    List<RSObject> obstacles = getObjectManager().getObjects(obj -> 
        Arrays.asList(obstacleNames).contains(obj.getName())
    );
    
    if (obstacles.isEmpty()) return null;
    
    // Return closest obstacle
    WorldPosition playerPos = getWorldPosition();
    return obstacles.stream()
        .min(Comparator.comparingInt(obj -> 
            obj.getWorldPosition().distanceTo(playerPos)))
        .orElse(null);
}
```

### Course Completion Detection

```java
private int startXp = 0;
private boolean lapComplete = false;

@Override
public void onStart() {
    // Track starting XP
    XPTracker agilityTracker = getXPTrackers().get(SkillType.AGILITY);
    if (agilityTracker != null) {
        startXp = (int) agilityTracker.getXp();
    }
}

@Override
public void onNewFrame() {
    // Detect lap completion via XP gain
    XPTracker agilityTracker = getXPTrackers().get(SkillType.AGILITY);
    if (agilityTracker != null) {
        int currentXp = (int) agilityTracker.getXp();
        if (currentXp > startXp) {
            lapComplete = true;
            startXp = currentXp;
        }
    }
}
```

### Agility Course Pattern

```java
private enum Obstacle {
    ROUGH_WALL("Rough wall"),
    TIGHTROPE("Tightrope"),
    HAND_HOLDS("Hand holds"),
    GAP("Gap"),
    LEDGE("Ledge");
    
    private final String name;
    Obstacle(String name) { this.name = name; }
    public String getName() { return name; }
}

private Obstacle currentObstacle = Obstacle.ROUGH_WALL;

private int doAgilityLap() {
    RSObject obstacle = findNextObstacle(currentObstacle.getName());
    
    if (obstacle == null) {
        log("Can't find obstacle: " + currentObstacle.getName());
        return 1000;
    }
    
    // Click obstacle
    Polygon obstaclePoly = obstacle.getConvexHull();
    if (obstaclePoly != null) {
        MenuEntry response = getFinger().tapGetResponse(true, obstaclePoly);
        if (response != null) {
            log("Clicking: " + response.getAction());
            getFinger().tap(obstaclePoly, response.getAction());
            
            // Wait for animation to complete
            boolean completed = pollFramesUntil(() -> {
                // Check if moved to next obstacle area
                return playerMovedSignificantly();
            }, 10000);
            
            if (completed) {
                // Move to next obstacle
                currentObstacle = getNextObstacle(currentObstacle);
            }
        }
    }
    
    return RandomUtils.uniformRandom(400, 800);
}

private Obstacle getNextObstacle(Obstacle current) {
    Obstacle[] obstacles = Obstacle.values();
    int currentIndex = current.ordinal();
    return obstacles[(currentIndex + 1) % obstacles.length];
}

private boolean playerMovedSignificantly() {
    // Implement based on position tracking
    return true; // Placeholder
}
```

---

## Combat and Health Management

### Health Monitoring

```java
private boolean needsFood() {
    Minimap minimap = getWidgetManager().getMinimap();
    int healthPercent = minimap.getHitpointsPercentage();
    
    return healthPercent < 50; // Eat at 50% health
}

private boolean eatFood() {
    // Food item IDs
    int[] foodIds = {
        ItemID.SHARK,
        ItemID.LOBSTER,
        ItemID.SWORDFISH
    };
    
    Inventory inv = getWidgetManager().getInventory();
    ItemGroupResult invSnapshot = inv.search(Set.of(foodIds));
    
    if (invSnapshot == null) {
        log("No food in inventory!");
        return false;
    }
    
    for (int foodId : foodIds) {
        SearchableItem food = invSnapshot.getItem(foodId);
        if (food != null) {
            food.interact("Eat");
            pollFramesHuman(() -> false, random(300, 600));
            return true;
        }
    }
    
    return false;
}
```

### Prayer Monitoring

```java
private boolean needsPrayerRestore() {
    Minimap minimap = getWidgetManager().getMinimap();
    int prayerPoints = minimap.getPrayerOrb().getValue();
    int maxPrayer = 50; // Set your max prayer level
    
    return prayerPoints < (maxPrayer * 0.3); // Restore at 30%
}

private boolean drinkPrayerPotion() {
    int[] prayerPotionIds = {
        ItemID.PRAYER_POTION_4,
        ItemID.PRAYER_POTION_3,
        ItemID.PRAYER_POTION_2,
        ItemID.PRAYER_POTION_1
    };
    
    Inventory inv = getWidgetManager().getInventory();
    ItemGroupResult invSnapshot = inv.search(Set.of(prayerPotionIds));
    
    if (invSnapshot == null) return false;
    
    for (int potionId : prayerPotionIds) {
        SearchableItem potion = invSnapshot.getItem(potionId);
        if (potion != null) {
            potion.interact("Drink");
            return true;
        }
    }
    
    return false;
}
```

---

## Animation and Movement Detection

### Detecting Player Animation

```java
// Check if player is animating (busy)
private boolean isPlayerAnimating() {
    // This is a common pattern - implement based on available API
    // Usually involves checking player state or movement
    WorldPosition currentPos = getWorldPosition();
    
    pollFramesHuman(() -> false, 100);
    
    WorldPosition newPos = getWorldPosition();
    return !currentPos.equals(newPos);
}

// Wait for animation to complete
private boolean waitForAnimationComplete(int timeoutMs) {
    return pollFramesUntil(() -> !isPlayerAnimating(), timeoutMs);
}
```

### Idle Detection

```java
private WorldPosition lastPosition = null;
private long lastMovementTime = System.currentTimeMillis();

private boolean isPlayerIdle() {
    WorldPosition currentPos = getWorldPosition();
    
    if (currentPos == null) return true;
    
    if (!currentPos.equals(lastPosition)) {
        lastPosition = currentPos;
        lastMovementTime = System.currentTimeMillis();
        return false;
    }
    
    // Consider idle if not moved for 5+ seconds
    return (System.currentTimeMillis() - lastMovementTime) > 5000;
}
```

---

## Special Cases and Edge Cases

### Handling Level-Up During Activity

```java
@Override
public void onNewFrame() {
    Dialogue dialogue = getWidgetManager().getDialogue();
    
    if (dialogue.isVisible() && 
        dialogue.getDialogueType() == DialogueType.TAP_HERE_TO_CONTINUE) {
        // Level up dialogue appeared
        log("Level up detected, continuing dialogue");
        dialogue.continueChatDialogue();
        pollFramesHuman(() -> false, random(1000, 2000));
    }
}
```

### Handling Random Events

```java
private boolean handleRandomEvent() {
    // Check for common random event NPCs nearby
    List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();
    
    for (WorldPosition npcPos : npcPositions) {
        if (npcPos.distanceTo(getWorldPosition()) <= 2) {
            Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
            if (tileCube == null) continue;
            
            MenuEntry response = getFinger().tapGetResponse(true, tileCube);
            if (response != null) {
                String npcName = response.getEntityName();
                
                // Common random event NPCs
                if (npcName.contains("Strange plant") || 
                    npcName.contains("Genie") ||
                    npcName.contains("Dr Jekyll")) {
                    log("Random event detected: " + npcName);
                    // Dismiss or interact
                    getFinger().tap(tileCube, "Dismiss");
                    return true;
                }
            }
        }
    }
    
    return false;
}
```

---

## Best Practices

### Ground Items
1. **Check distance** before looting (don't run across the map)
2. **Track looted positions** to avoid re-clicking
3. **Sort by value** when multiple items available
4. **Check inventory space** before looting

### Agility
1. **Track obstacle order** in a state machine
2. **Detect lap completion** via XP gain
3. **Handle failed obstacles** (retry pattern)
4. **Add random delays** between obstacles

### Combat
1. **Monitor health constantly** in onNewFrame
2. **Keep food/potions in specific slots** for reliable access
3. **Eat between combat** not during
4. **Track potion doses** externally if needed

### General
1. **Use onNewFrame for monitoring** (health, XP, dialogues)
2. **Never block in onNewFrame** (read-only operations)
3. **Handle level-ups gracefully** (continue dialogue)
4. **Add reaction delays** after major events

---

*These patterns are commonly needed but not covered in the core documentation. They represent real-world script requirements.*
