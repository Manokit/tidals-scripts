# Walker API Documentation

**Package:** `com.osmb.api.walker`

## Overview

The Walker API provides pathfinding and navigation functionality for moving around the game world. It includes the `Walker` interface for executing walks and the `WalkConfig.Builder` for customizing walking behavior.

---

# Walker Interface

**Type:** Interface

## Description

The Walker interface provides methods for navigating to positions, objects, and along paths using automatic pathfinding.

## Methods

### `getaStar()`
Returns the A* pathfinding instance used by the walker.

**Returns:** `AStarPathFinder` - The A* pathfinding algorithm implementation

---

### `getDefaultSettings()`
Returns the default walking configuration settings.

**Returns:** `WalkConfig` - Default walk configuration

---

### `getCollisionManager()`
Returns the collision manager for handling collision detection.

**Returns:** `CollisionManager` - The collision manager instance

---

### Walking to Positions

#### `walkTo(Position position)`
Walks to the specified coordinates.

**Parameters:**
- `position` - The Position to navigate to. Either `LocalPosition` or `WorldPosition` can be passed

**Returns:** `boolean` - `true` if reached the destination, `false` otherwise

---

#### `walkTo(Position position, WalkConfig config)`
Walks to the specified coordinates with custom configuration.

**Parameters:**
- `position` - The Position to navigate to. Either `LocalPosition` or `WorldPosition` can be passed
- `config` - The walk configurations to be used

**Returns:** `boolean` - `true` if reached the destination, `false` otherwise

---

#### `walkTo(int worldX, int worldY)`
Walks to the specified world coordinates.

**Parameters:**
- `worldX` - The world X coordinate to navigate to
- `worldY` - The world Y coordinate to navigate to

**Returns:** `boolean` - `true` if reached the destination, `false` otherwise

---

#### `walkTo(int worldX, int worldY, WalkConfig config)`
Walks to the specified world coordinates with custom configuration.

**Parameters:**
- `worldX` - The world X coordinate to navigate to
- `worldY` - The world Y coordinate to navigate to
- `config` - The walk configurations to be used

**Returns:** `boolean` - `true` if reached the destination, `false` otherwise

---

### Walking to Objects

#### `walkTo(RSObject object)`
Walks to the specified RSObject.

**Parameters:**
- `object` - The RSObject to navigate to

**Returns:** `boolean` - `true` if reached the destination, `false` otherwise

---

#### `walkTo(RSObject object, WalkConfig walkConfig)`
Walks to the specified RSObject with custom configuration.

**Parameters:**
- `object` - The RSObject to navigate to
- `walkConfig` - The walk configurations to be used

**Returns:** `boolean` - `true` if reached the destination, `false` otherwise

---

#### `walkTo(RSObject object, int interactDistance, WalkConfig walkConfig)`
Walks to the specified RSObject with custom interaction distance and configuration.

**Parameters:**
- `object` - The RSObject to navigate to
- `interactDistance` - The tile distance from the object to where you can interact from
- `walkConfig` - The walk configurations to be used

**Returns:** `boolean` - `true` if reached the destination, `false` otherwise

---

### Walking Paths

#### `walkPath(List<WorldPosition> path)`
Walks along a predefined path.

**Parameters:**
- `path` - List of WorldPosition waypoints defining the path

**Returns:** `boolean` - `true` if completed the path, `false` otherwise

---

#### `walkPath(List<WorldPosition> path, WalkConfig config)`
Walks along a predefined path with custom configuration.

**Parameters:**
- `path` - List of WorldPosition waypoints defining the path
- `config` - The walk configurations to be used

**Returns:** `boolean` - `true` if completed the path, `false` otherwise

---

# WalkConfig.Builder Class

**Type:** Static Nested Class

**Enclosing Class:** `WalkConfig`

## Description

Builder pattern implementation for creating WalkConfig instances with fluent API. Allows customizing all aspects of walking behavior including run energy management, delays, break conditions, and more.

## Constructor

### `Builder()`
Creates a new WalkConfig Builder with default settings.

---

## Builder Methods

All builder methods return `WalkConfig.Builder` for method chaining.

### Run Energy Management

#### `setRunEnergyThreshold(int minThreshold, int maxThreshold)`
Set the run energy thresholds for automatic run management.

**Parameters:**
- `minThreshold` - Minimum run energy percentage to enable running
- `maxThreshold` - Maximum run energy percentage to enable running

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

**Example:** `.setRunEnergyThreshold(20, 80)` - Enable run between 20-80% energy

---

#### `enableRun(boolean enable)`
Enable or disable automatic run energy management.

**Parameters:**
- `enable` - Whether to enable run energy management

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

---

### Timeout and Distance

#### `timeout(int timeout)`
Set the operation timeout.

**Parameters:**
- `timeout` - Maximum execution time in milliseconds

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

**Example:** `.timeout(30000)` - 30 second timeout

---

#### `breakDistance(int breakDistance)`
Set the completion distance threshold.

**Parameters:**
- `breakDistance` - Distance in tiles to consider destination reached

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

**Example:** `.breakDistance(3)` - Stop when within 3 tiles of destination

---

### Click Delays

#### `minimapTapDelay(long min, long max)`
Set minimap click delay range.

**Parameters:**
- `min` - Minimum delay in milliseconds
- `max` - Maximum delay in milliseconds

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

**Example:** `.minimapTapDelay(100, 300)` - Random 100-300ms delay between minimap clicks

---

#### `screenTapDelay(long min, long max)`
Set screen click delay range.

**Parameters:**
- `min` - Minimum delay in milliseconds
- `max` - Maximum delay in milliseconds

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

**Example:** `.screenTapDelay(200, 500)` - Random 200-500ms delay between screen clicks

---

### Click Randomization

#### `tileRandomisationRadius(int tileRadius)`
Set tile randomization radius.

**Parameters:**
- `tileRadius` - Radius in tiles for randomizing click positions

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

**Example:** `.tileRandomisationRadius(2)` - Randomize clicks within 2 tiles

---

### Interruption Control

#### `allowInterrupt(boolean allow)`
Set whether the walking task can be interrupted by break, afk or hopping tasks.

**Parameters:**
- `allow` - Whether to allow interruption

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

---

#### `breakCondition(BooleanSupplier breakCondition)`
Set the break condition supplier.

**Parameters:**
- `breakCondition` - Supplier that returns `true` to interrupt walking

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

**Example:** `.breakCondition(() -> ctx.inventory.isFull())` - Stop walking if inventory is full

---

### Dynamic Behavior

#### `doWhileWalking(Supplier<WalkConfig> doWhileWalking)`
Supplier to be executed while walking. To update the WalkConfig dynamically, you can return a different WalkConfig and the configs will be updated (apart from `timeout` & `allowInterrupt` which is initially set when called). If you don't want to update the current WalkConfig then return null.

**Parameters:**
- `doWhileWalking` - Supplier that provides updated config during execution

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

**Example:** 
```java
.doWhileWalking(() -> {
    if (ctx.players.getLocal().isInCombat()) {
        // Disable walking if in combat
        return new WalkConfig.Builder()
            .timeout(5000)
            .build();
    }
    return null; // Keep current config
})
```

---

### Walking Methods

#### `disableWalkScreen(boolean disable)`
Enable or disable walking via screen clicks.

**Parameters:**
- `disable` - Whether to disable screen walking

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

---

#### `disableWalkMinimap(boolean disable)`
Enable or disable walking via minimap clicks.

**Parameters:**
- `disable` - Whether to disable minimap walking

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

---

#### `setWalkMethods(boolean screen, boolean minimap)`
Set both screen and minimap walking options.

**Parameters:**
- `screen` - Whether to enable screen walking
- `minimap` - Whether to enable minimap walking

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

**Example:** `.setWalkMethods(true, false)` - Only use screen clicks, not minimap

---

### Container Handling

#### `setHandleContainerObstruction(boolean handleContainerObstruction)`
Set whether to handle container obstructions (e.g., inventory, skills tab) during walking.

**Parameters:**
- `handleContainerObstruction` - Whether to handle container obstructions

**Returns:** `WalkConfig.Builder` - Builder instance for method chaining

**Default:** `true` - Automatically closes obstructing interfaces

---

### Build

#### `build()`
Build the WalkConfig instance with current settings.

**Returns:** `WalkConfig` - Configured WalkConfig instance

---

## Usage Examples

### Basic Walking

```java
Walker walker = ctx.getWalker();

// Walk to coordinates with default settings
walker.walkTo(3000, 3000);

// Walk to a Position
WorldPosition destination = new WorldPosition(3000, 3000, 0);
walker.walkTo(destination);

// Walk to an object
RSObject tree = ctx.objects.getNearest("Tree");
walker.walkTo(tree);

// Walk along a predefined path
List<WorldPosition> path = Arrays.asList(
    new WorldPosition(3000, 3000, 0),
    new WorldPosition(3010, 3010, 0),
    new WorldPosition(3020, 3020, 0)
);
walker.walkPath(path);
```

### Custom Walk Configuration

```java
// Create custom walk config
WalkConfig config = new WalkConfig.Builder()
    .timeout(30000)                      // 30 second timeout
    .breakDistance(5)                    // Stop within 5 tiles
    .enableRun(true)                     // Enable run management
    .setRunEnergyThreshold(30, 90)       // Run between 30-90% energy
    .minimapTapDelay(150, 400)           // Minimap delay 150-400ms
    .screenTapDelay(300, 600)            // Screen delay 300-600ms
    .tileRandomisationRadius(1)          // Randomize within 1 tile
    .build();

// Walk with custom config
walker.walkTo(3000, 3000, config);
```

### Walking with Break Conditions

```java
// Stop walking if inventory is full
WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> ctx.inventory.isFull())
    .timeout(60000)
    .build();

walker.walkTo(bankLocation, config);

// Stop walking if in combat
WalkConfig combatConfig = new WalkConfig.Builder()
    .breakCondition(() -> ctx.players.getLocal().isInCombat())
    .build();

walker.walkTo(safeSpot, combatConfig);

// Multiple break conditions
WalkConfig multiConfig = new WalkConfig.Builder()
    .breakCondition(() -> 
        ctx.inventory.isFull() || 
        ctx.players.getLocal().getHealthPercent() < 30
    )
    .build();
```

### Dynamic Walking Behavior

```java
// Change walking behavior while walking
WalkConfig dynamicConfig = new WalkConfig.Builder()
    .doWhileWalking(() -> {
        // If being attacked, change to faster walking
        if (ctx.players.getLocal().isInCombat()) {
            return new WalkConfig.Builder()
                .minimapTapDelay(50, 100)  // Faster clicks
                .screenTapDelay(50, 100)
                .setRunEnergyThreshold(10, 100)  // Always run
                .build();
        }
        
        // If HP is low, stop walking
        if (ctx.players.getLocal().getHealthPercent() < 20) {
            return new WalkConfig.Builder()
                .breakDistance(0)  // Stop immediately
                .build();
        }
        
        return null;  // Keep current config
    })
    .build();

walker.walkTo(destination, dynamicConfig);
```

### Walking to Objects with Interaction Distance

```java
// Walk to tree and stop 2 tiles away (for woodcutting)
RSObject tree = ctx.objects.getNearest("Oak tree");
WalkConfig config = new WalkConfig.Builder()
    .breakDistance(2)
    .build();

walker.walkTo(tree, 2, config);

// Walk to bank chest (can interact from 1 tile away)
RSObject bankChest = ctx.objects.getNearest("Bank chest");
walker.walkTo(bankChest, 1, new WalkConfig.Builder().build());
```

### Minimap vs Screen Walking

```java
// Only use minimap (useful for long distance)
WalkConfig minimapOnly = new WalkConfig.Builder()
    .setWalkMethods(false, true)  // screen=false, minimap=true
    .build();

walker.walkTo(farLocation, minimapOnly);

// Only use screen clicks (useful for precise short walks)
WalkConfig screenOnly = new WalkConfig.Builder()
    .setWalkMethods(true, false)  // screen=true, minimap=false
    .build();

walker.walkTo(nearbyObject, screenOnly);

// Use both methods (default)
WalkConfig both = new WalkConfig.Builder()
    .setWalkMethods(true, true)
    .build();
```

### Advanced Walking Patterns

```java
// Stealth walking (slower, more randomization)
WalkConfig stealthWalk = new WalkConfig.Builder()
    .minimapTapDelay(300, 800)
    .screenTapDelay(400, 900)
    .tileRandomisationRadius(3)
    .enableRun(false)
    .build();

// Speed walking (faster, less delay)
WalkConfig speedWalk = new WalkConfig.Builder()
    .minimapTapDelay(50, 150)
    .screenTapDelay(50, 150)
    .tileRandomisationRadius(0)
    .setRunEnergyThreshold(1, 100)
    .build();

// Walking to multiple locations in sequence
List<WorldPosition> locations = Arrays.asList(
    new WorldPosition(3000, 3000, 0),
    new WorldPosition(3100, 3100, 0),
    new WorldPosition(3200, 3200, 0)
);

for (WorldPosition loc : locations) {
    boolean success = walker.walkTo(loc, config);
    if (!success) {
        System.out.println("Failed to reach " + loc);
        break;
    }
    // Do something at each location
    Time.sleep(1000);
}
```

### Walking with Container Obstruction Handling

```java
// Automatically close inventory/interfaces that block walking
WalkConfig autoCloseConfig = new WalkConfig.Builder()
    .setHandleContainerObstruction(true)  // Default behavior
    .build();

walker.walkTo(destination, autoCloseConfig);

// Disable auto-close (keep interfaces open)
WalkConfig keepOpenConfig = new WalkConfig.Builder()
    .setHandleContainerObstruction(false)
    .build();

walker.walkTo(destination, keepOpenConfig);
```

### Combining with Pathfinding

```java
// Get A* pathfinder instance
AStarPathFinder aStar = walker.getaStar();

// Generate custom path using A*
List<WorldPosition> customPath = aStar.findPath(startPos, endPos);

// Walk the custom path
WalkConfig config = new WalkConfig.Builder()
    .timeout(60000)
    .build();

walker.walkPath(customPath, config);
```

## Important Notes

### Walking Behavior
- The walker automatically uses both minimap and screen clicks by default
- Pathfinding avoids obstacles using collision detection
- Walking continues until destination is reached or break condition is met
- Returns `true` only if successfully reached the destination

### Run Energy Management
- When `enableRun(true)`, walker automatically manages run state
- Run is enabled when energy is between minThreshold and maxThreshold
- Run is disabled outside this range
- Set thresholds appropriately to avoid constant toggling

### Break Conditions
- Break conditions are checked continuously during walking
- When break condition returns `true`, walking stops immediately
- Use break conditions for inventory full, HP low, combat, etc.
- Multiple conditions can be combined with `||` or `&&`

### Delays and Randomization
- Tap delays add human-like pauses between clicks
- Randomization makes walking appear more natural
- Lower delays = faster but more bot-like
- Higher delays = slower but more human-like

### Timeouts
- Walking will abort if timeout is exceeded
- Returns `false` if timeout occurs
- Set appropriate timeouts for expected travel distance
- Very long walks may need higher timeouts

### Performance Considerations
- `doWhileWalking` is called frequently - keep logic lightweight
- Break conditions should be fast to evaluate
- Avoid expensive operations in these callbacks
- Consider caching values that don't change often

## Common Patterns

### Walking to Bank
```java
WalkConfig bankConfig = new WalkConfig.Builder()
    .breakDistance(2)  // Stop near bank
    .breakCondition(() -> ctx.inventory.isEmpty())  // Stop if already emptied
    .timeout(30000)
    .build();

WorldPosition bankLocation = new WorldPosition(3269, 3167, 0); // Lumbridge bank
walker.walkTo(bankLocation, bankConfig);
```

### Walking to Training Spot
```java
WalkConfig trainingConfig = new WalkConfig.Builder()
    .breakCondition(() -> 
        ctx.inventory.isFull() || 
        ctx.players.getLocal().getHealthPercent() < 30
    )
    .setRunEnergyThreshold(40, 80)
    .timeout(45000)
    .build();

walker.walkTo(trainingSpot, trainingConfig);
```

### Escaping Combat
```java
WalkConfig escapeConfig = new WalkConfig.Builder()
    .minimapTapDelay(25, 50)  // Fast clicks
    .screenTapDelay(25, 50)
    .setRunEnergyThreshold(1, 100)  // Always run
    .breakCondition(() -> !ctx.players.getLocal().isInCombat())
    .build();

walker.walkTo(safeSpot, escapeConfig);
```

## Best Practices

1. **Always set timeouts** - Prevents infinite walking if stuck
2. **Use appropriate break distances** - Consider interaction ranges
3. **Test break conditions** - Ensure they trigger correctly
4. **Randomize delays** - Makes walking appear more human
5. **Handle failures** - Check return value and have fallback logic
6. **Use run energy management** - Faster travel with automatic toggling
7. **Consider pathing** - Some destinations may require custom paths
8. **Monitor performance** - Keep callbacks lightweight
9. **Test different configs** - Find what works best for your script
10. **Cache configs** - Reuse configs instead of rebuilding every time

## Related Classes

- `WalkConfig` - The immutable configuration object built by this builder
- `Position` / `WorldPosition` / `LocalPosition` - Position types for navigation
- `RSObject` - Objects in the game world
- `AStarPathFinder` - Pathfinding algorithm
- `CollisionManager` - Collision detection for pathfinding
