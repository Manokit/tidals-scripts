# Walker

**Type:** Interface

## Methods

| Return Type | Method |
|------------|--------|
| `AStar` | `getaStar()` |
| `CollisionManager` | `getCollisionManager()` |
| `WalkSettings` | `getDefaultSettings()` |
| `boolean` | `walkPath(List<WorldPosition>)` |
| `boolean` | `walkPath(List<WorldPosition>, WalkConfig)` |
| `boolean` | `walkTo(Position position)` |
| `boolean` | `walkTo(Position position, WalkConfig config)` |
| `boolean` | `walkTo(int worldX, int worldY)` |
| `boolean` | `walkTo(int worldX, int worldY, WalkConfig config)` |
| `boolean` | `walkTo(RSObject object)` |
| `boolean` | `walkTo(RSObject object, WalkConfig walkConfig)` |
| `boolean` | `walkTo(RSObject object, int interactDistance, WalkConfig walkConfig)` |

## Method Details

### getaStar
```java
AStar getaStar()
```

### getDefaultSettings
```java
WalkSettings getDefaultSettings()
```

### walkTo
```java
boolean walkTo(Position position)
```

Walks to the specified coordinates

**Parameters:**
- `position` - The Position to navigate to. Either LocalPosition or WorldPosition can be passed.

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(Position position, WalkConfig config)
```

Walks to the specified coordinates

**Parameters:**
- `position` - The Position to navigate to. Either LocalPosition or WorldPosition can be passed.
- `config` - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(int worldX, int worldY)
```

Walks to the specified coordinates

**Parameters:**
- `worldX` - The world X coordinate to navigate to
- `worldY` - The world Y coordinate to navigate to

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(int worldX, int worldY, WalkConfig config)
```

Walks to the specified coordinates

**Parameters:**
- `worldX` - The world X coordinate to navigate to
- `worldY` - The world Y coordinate to navigate to
- `config` - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(RSObject object)
```

**Parameters:**
- `object` - The RSObject to navigate to

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(RSObject object, WalkConfig walkConfig)
```

**Parameters:**
- `object` - The RSObject to navigate to
- `walkConfig` - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

---

```java
boolean walkTo(RSObject object, int interactDistance, WalkConfig walkConfig)
```

**Parameters:**
- `object` - The RSObject to navigate to
- `interactDistance` - The tile distance from the object to where you can interact from.
- `walkConfig` - The walk configurations to be used

**Returns:** true if reached the destination, false otherwise

### walkPath
```java
boolean walkPath(List<WorldPosition>)
```

```java
boolean walkPath(List<WorldPosition>, WalkConfig)
```

### getCollisionManager
```java
CollisionManager getCollisionManager()
```

---

## WalkConfig and Break Conditions

`WalkConfig` allows you to customize walking behavior, including break conditions that stop walking early when a condition is met.

### Creating WalkConfig

```java
WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> /* condition */)
    .enableRun(true)
    .disableWalkScreen(true)  // don't click on game screen during walk
    .build();

getWalker().walkTo(destination, config);
```

### Break Condition Patterns

Break conditions are lambdas that run continuously during walking. When they return `true`, walking stops immediately.

#### Pattern 1: Wait for Object to Load (Query-Based)

Objects may not be loaded until you get close enough. Use break condition to stop walking once the target appears. **Use this when you don't have an RSObject reference yet.**

```java
// Stop walking when bank booth becomes visible (query each check)
WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

        RSObject bank = script.getObjectManager().getClosestObject(myPos, "Bank booth");
        return bank != null;
    })
    .build();

script.getWalker().walkTo(bankArea.getRandomPosition(), config);
```

**Per OSMB**: If using `RSObject::interact`, call this when RSObject is null to walk until it's loaded into scene. The interact method handles the rest.

#### Pattern 2: Wait for Hull Visibility (Existing Reference)

**Use this when you already have an RSObject reference** (e.g., handling interaction yourself instead of using `RSObject::interact`). Poll the hull visibility while walking:

```java
// Already have reference from earlier query
RSObject booth = script.getObjectManager().getClosestObject(myPos, "Bank booth");

WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> {
        Polygon hull = booth.getConvexHull();
        if (hull == null) return false;

        // ensure visible and not overlapped by UI (ignore chatbox - can tap through it)
        double visibility = script.getWidgetManager().insideGameScreenFactor(
            hull, List.of(ChatboxComponent.class)
        );
        return visibility >= 0.3;  // 30% visible is enough to interact
    })
    .enableRun(true)
    .build();

script.getWalker().walkTo(booth.getWorldPosition(), config);
// After walk completes, hull should be visible - interact immediately
```

**Per OSMB**: If structured correctly, after re-poll it should instantly interact with the bank since the visibility check already passed.

#### Pattern 3: Distance-Based Break

Stop walking when within interaction range:

```java
WorldPosition target = new WorldPosition(3253, 3420, 0);

WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;
        return myPos.distanceTo(target) <= 5;
    })
    .build();

script.getWalker().walkTo(target, config);
```

#### Pattern 4: Multiple Conditions

Combine conditions using logical operators:

```java
WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> {
        // Stop if: arrived at bank, OR bank interface opened, OR interrupted
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;

        boolean nearBank = myPos.distanceTo(BANK_POSITION) <= 3;
        boolean bankOpen = script.getWidgetManager().getBank().isVisible();
        boolean interrupted = CrashDetection.detected;

        return nearBank || bankOpen || interrupted;
    })
    .build();
```

### Lambda Null Safety (Critical!)

Break condition lambdas execute repeatedly during walking. Always null-check `getWorldPosition()`:

```java
// WRONG - getWorldPosition() might return null during loading
WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> {
        RSObject altar = script.getObjectManager().getClosestObject(
            script.getWorldPosition(), "Altar"  // NPE risk!
        );
        return altar != null;
    })
    .build();

// CORRECT - Extract position, null check, then use
WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> {
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;  // graceful handling

        RSObject altar = script.getObjectManager().getClosestObject(myPos, "Altar");
        return altar != null;
    })
    .build();
```

See `docs/common-mistakes.md` section 15 for detailed explanation.

---

## MovementChecker Integration

Use `MovementChecker` to detect when walking has stalled:

```java
import utilities.MovementChecker;

private boolean walkToBank() {
    WorldPosition target = BANK_POSITION;
    WorldPosition start = script.getWorldPosition();
    if (start == null) return false;

    // Start walking
    script.getWalker().walkTo(target, new WalkConfig.Builder().build());

    // Monitor for stalls
    MovementChecker checker = new MovementChecker(start);

    boolean arrived = script.pollFramesUntil(() -> {
        WorldPosition current = script.getWorldPosition();
        if (current == null) return false;

        // Stall detection - player hasn't moved for 800-2000ms
        if (checker.hasTimedOut(current)) {
            script.log(getClass(), "walk stalled, will retry");
            return true;  // exit poll
        }

        return current.distanceTo(target) < 3;
    }, 30000);

    // Verify actual arrival (not just stall exit)
    WorldPosition finalPos = script.getWorldPosition();
    return finalPos != null && finalPos.distanceTo(target) < 3;
}
```

See `docs/interaction-patterns.md` for full MovementChecker documentation.

---

## Common Walking Mistakes

### 1. Not Using Break Conditions

```java
// WRONG - Walks all the way to destination even if target appears earlier
script.getWalker().walkTo(bankArea.getRandomPosition());

// CORRECT - Stop early when target loads
WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> bankBooth != null)
    .build();
script.getWalker().walkTo(bankArea.getRandomPosition(), config);
```

### 2. Static Positions Instead of Areas

```java
// WRONG - Same exact tile every time (detectable)
script.getWalker().walkTo(new WorldPosition(3253, 3420, 0));

// CORRECT - Random position within area (human-like)
RectangleArea area = new RectangleArea(3250, 3418, 3256, 3422, 0);
script.getWalker().walkTo(area.getRandomPosition());
```

### 3. No Stall Detection

```java
// WRONG - Gets stuck if walk fails silently
script.getWalker().walkTo(destination);
// ...assumes we arrived

// CORRECT - Use MovementChecker to detect stalls
MovementChecker checker = new MovementChecker(currentPos);
// ...poll and check hasTimedOut()
```
