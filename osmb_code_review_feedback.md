# OSMB Code Review Feedback - Action Items

## Core Principle: Poll-Based Script Structure

**Don't do many consecutive things in a single poll.** Make scripts more dynamic by performing minimal actions per poll.

### Current Problem (Linear Approach)
```
- Handle dialogues
- Walk to deposit box
- Open it
- Deposit items
- Close it
- Walk back
All potentially in a single poll
```

### Desired Approach (Poll-Based)
```java
if(dialogueVisible) {
    handle it
    // re-poll
}

if(depositInterfaceOpen){
    handle it
    // re-poll
}
// etc.
```

**Pattern:** Check for a condition, handle it, and re-poll.

---

## Gem Miner Issues

### 1. Color Detection Fallback is Pointless
```java
// if no rocks from ObjectManager, try color detection as fallback
if (gemRocks == null || gemRocks.isEmpty()) {
```

**Issue:** Object manager will always return the objects if you are in the mine, so this fallback is unnecessary.

**Action:** Remove color detection fallback.

---

### 2. Area Validation Should Be Pre-Check
```java
// no rocks visible at all - check if we should walk to mining area or hop
if (!selectedLocation.miningArea().contains(myPos)) {
    task = "Walking to mine";
    script.log(getClass(), "no rocks visible, walking to mining area");
    script.getWalker().walkTo(selectedLocation.minePosition(), new WalkConfig.Builder().build());
    logVerbose("walkTo minePosition=" + selectedLocation.minePosition());
    return false;
}
```

**Issue:** This is backwards. You should always validate the area BEFORE executing any search for rocks. Doesn't make sense to search and then check when you can validate location initially at no cost.

**Action:** Move area validation to the beginning, before searching for rocks.

---

### 3. Randomize Timeouts to Avoid Patterns
```java
// early exit for misclick: if no swing pick seen within timeout, click missed
if (!swingPickSeen[0] && (System.currentTimeMillis() - startMs) > SWING_PICK_TIMEOUT_MS) {
    logVerbose("no swing pick within " + SWING_PICK_TIMEOUT_MS + "ms - likely misclick");
    return true;  // exit early
}
```

**Issue:** `SWING_PICK_TIMEOUT_MS` should be randomized each time, as it has potential to cause patterns with a precise 2.5 second interval.

**Action:** Randomize `SWING_PICK_TIMEOUT_MS` on each use.

---

### 4. Static Timeout Should Be Randomized
```java
// done when deposit box visible or idle for 2+ seconds
return script.getWidgetManager().getDepositBox().isVisible() ||
        posTimer.get().timeElapsed() > 2000;
}, RandomUtils.weightedRandom(15000, 30000, 0.002));
```

**Issue:** Static timeout (2000ms) should be randomized and re-randomized each time it is executed.

**Action:** Replace `2000` with a randomized value that changes on each execution.

---

## Cannonball Thiever Issues

### 1. Incorrect pollFramesUntil Usage
```java
script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(300, 1000, 0.002));
```

**Issue:** When using `pollFramesUntil`, it does NOT generate a delay on completion (unlike `pollFramesHuman`). You would have to return `false` for your custom delay.

**Action:** Return `false` instead of `true` when using `pollFramesUntil` with a custom delay.

---

### 2. execute() Method is Too Linear

**Issue:** The execute method performs too many consecutive actions in a single poll:
1. Handle dialogues
2. Walk to deposit box
3. Open it
4. Deposit items
5. Close it
6. Walk back

**Action:** Break up into separate poll-based checks. See example below.

---

### 3. Don't Handle Walking Back in Deposit Method
```java
// I wouldn't handle walking back to the stall in this method
// Just have it solely for walking to & depositing
// It's a much cleaner way to structure it
if (!atStall) {
    script.log("DEPOSIT", "Returning to cannonball stall...");
    script.getWalker().walkTo(CANNONBALL_STALL_TILE, exactTileConfig);
    // ...
}
```

**Action:** Remove walking back to stall from deposit method. Keep deposit method focused only on walking to and depositing.

---

## openDepositBoxWithMenu() Issues

### Current Implementation Problems

```java
private boolean openDepositBoxWithMenu() {
    WorldPosition myPos = script.getWorldPosition();
    if (myPos == null) return false;

    RSObject depositBox = script.getObjectManager().getClosestObject(myPos, "Bank deposit box");
    if (depositBox == null) {
        script.log("DEPOSIT", "Can't find deposit box!");
        return false;
    }

    Polygon boxPoly = depositBox.getConvexHull();
    if (boxPoly == null) {
        script.log("DEPOSIT", "Deposit box hull null");
        return false;
    }

    script.log("DEPOSIT", "Opening deposit box via menu...");
    boolean tapped = script.getFinger().tap(boxPoly, "Deposit");
    
    if (tapped) {
        script.log("DEPOSIT", "Deposit action sent!");
        return true;
    }
    
    script.log("DEPOSIT", "Failed to send Deposit action");
    return false;
}
```

### Issues:
1. **Not checking if deposit box hull is visible on screen without UI overlap**
   - The convex hull will be considered valid as long as 3D coordinates project within 2D screen bounds
   - Does NOT account for UI components
   - Hull may be non-null even if object is overlapped by interface (e.g., inventory)

2. **Using wrong tap method**
   - Do NOT use `Finger::tap` for 3D game screen interactions
   - MUST use `Finger::tapGameScreen`
   - If shape is half on game screen & half overlapping UI, `tapGameScreen` ensures it only taps the game screen section

### Relevant Methods:
- `WidgetManager.insideGameScreen(Shape, List)` - Check if shape is inside game screen
- `WidgetManager.insideGameScreenFactor(Shape, List)` - Check visibility factor (e.g., 0.5 = half visible)
  - **Recommended:** Especially when checking pixels inside shape
- `RSObject.isInteractableOnScreen()` - Only checks factor of 0.2
- `Finger.tapGameScreen(boolean, Shape)` - Use this for game screen interactions

**Documentation:**
- https://www.osmb.co.uk/documentation/com/osmb/api/ui/WidgetManager.html#insideGameScreen(com.osmb.api.shape.Shape,java.util.List)
- https://www.osmb.co.uk/documentation/com/osmb/api/ui/WidgetManager.html#insideGameScreenFactor(com.osmb.api.shape.Shape,java.util.List)
- https://www.osmb.co.uk/documentation/com/osmb/api/input/Finger.html#tapGameScreen(boolean,com.osmb.api.shape.Shape)

---

## Recommended Structure: executeReview() Example

```java
// removed dialogue check, as you can walk and it will remove the dialogue anyways
public boolean executeReview() {
    // always check for interfaces first
    if (getWidgetManager().getDepositBox().isVisible()) {
        // handle depositing logic
        return true;
    }
    
    // At this point, deposit box is not open, so open the deposit box
    RSObject depositBox = getObjectManager().getClosestObject("Bank deposit box");
    if (depositBox == null) {
        // if deposit box is null, that means its not loaded into our scene, so we need to walk to it
        // walk to deposit box
        return true;
    }
    
    // at this point, the deposit box object is non-null, so lets move on.
    // get the hull of the deposit box
    Polygon boxPoly = depositBox.getConvexHull();
    if (boxPoly == null || !depositBox.isInteractableOnScreen()) {
        // convex hull is null, meaning that the projected polygon is not on screen, so we need to walk closer
        // walk to deposit box
        return true;
    }
    
    // get the initial world position BEFORE interacting, to compare for movement.
    WorldPosition worldPosition = script.getWorldPosition();
    if (!getFinger().tapGameScreen(boxPoly)) {
        // failed to tap deposit box, retry
        return false;
    }
    
    // successfully tapped deposit box, wait for interface to open
    // definitely add a movement timeout here also in case you get interrupted while walking 
    // or missclick so you aren't stood until the timeout
    // pollFramesHuman for a humanised delay
    // note: the timeout here isn't really important as long as it is randomised also it isn't 
    // too long, but long enough to open the interface in time on a normal occasion.
    // the gaussian distribution is good for things you interact with often as it mimics 
    // human behaviour well with the right values, but banks etc. are interacted with much less often.
    MovementChecker movementChecker = new MovementChecker(worldPosition);
    return pollFramesHuman(() -> {
        if(getWidgetManager().getDepositBox().isVisible()) {
            return true;
        }
        WorldPosition currentWorldPosition = script.getWorldPosition();
        if (currentWorldPosition == null) {
            return false;
        }
        return movementChecker.hasTimedOut(currentWorldPosition);
    }, RandomUtils.uniformRandom(10000, 15000));
}

public static class MovementChecker {
    private final long timeout;
    private WorldPosition initialPosition;
    private long lastMovementTime;

    public MovementChecker(WorldPosition initialPosition) {
        this.initialPosition = initialPosition;
        this.timeout = RandomUtils.uniformRandom(800, 2000);
        this.lastMovementTime = System.currentTimeMillis();
    }

    public boolean hasTimedOut(WorldPosition currentPosition) {
        if (!currentPosition.equalsPrecisely(this.initialPosition)) {
            lastMovementTime = System.currentTimeMillis();
            initialPosition = currentPosition;
            return false;
        }
        return System.currentTimeMillis() - lastMovementTime > timeout;
    }
}
```

**Key Points:**
- Check conditions in order
- Handle one thing at a time
- Re-poll after each action
- Use `tapGameScreen` for 3D interactions
- Add movement timeout to prevent getting stuck
- Use `pollFramesHuman` for humanized delays

---

## Walker Best Practices

### Walking to Bank Until Objects Are Loaded

```java
// if handling via RSObject::interact, you would call this if the RSObject 
// is null (or list is empty if querying for multiple) to walk to it until its loaded into scene. 
// Here we constantly poll for the objects being found
private void walkToBankArea(Area bankArea) {
    // walk to bank area
    WalkConfig.Builder walkConfig = new WalkConfig.Builder();
    walkConfig.breakCondition(() -> {
        // ---- If querying a single object
        RSObject bank = getObjectManager().getRSObject(BANK_QUERY);
        return bank != null;

        // ---- If querying a list
        List<RSObject> banksFound = getObjectManager().getObjects(BANK_QUERY);
        // break out once bank objects are found & let the RSObject::interact method handle the rest
        return !banksFound.isEmpty();
    });
    getWalker().walkTo(bankArea.getRandomPosition(), walkConfig.build());
}
```

### Walking Until Hull is Visible (Custom Interaction)

```java
// If you handle object interaction yourself instead of interact method
private void walkToBankArea(RSObject bank) {
    // walk to bank area
    WalkConfig.Builder walkConfig = new WalkConfig.Builder();
    walkConfig.breakCondition(() -> {
        Polygon bankPoly = bank.getConvexHull();
        // -- check for bank poly being on screen
        if(bankPoly == null) {
            return false;        
        }
        // -- ensure the bank poly is visible and not overlapped by a UI component
        // ignore chatbox as we can tap through it, dialogue is a separate component 
        // so if that is visible it will consider it
        if (getWidgetManager().insideGameScreenFactor(bankPoly, List.of(ChatboxComponent.class)) < 0.3) {
            // only return true if the bank poly is more than 2/3 on screen
            return false;
        }
        // the above could be inlined but just keeping it like this for readability sake
        return true;
    });
    getWalker().walkTo(bankArea.getRandomPosition(), walkConfig.build());
}
```

**Result:** After re-poll, it should instantly interact with the bank.

---

## Summary of Action Items

1. **Remove unnecessary fallbacks** (color detection in gem miner)
2. **Validate area/location FIRST** before searching for objects
3. **Randomize ALL timeouts** - no static values, re-randomize on each use
4. **Fix pollFramesUntil usage** - return `false` for custom delays
5. **Break up linear execute() methods** - one action per poll
6. **Use `tapGameScreen`** instead of `tap` for 3D interactions
7. **Check object visibility properly** - use `insideGameScreenFactor()` 
8. **Separate concerns** - don't handle walking back in deposit methods
9. **Use walker break conditions** - wait for objects to load or be visible
10. **Add movement timeouts** - prevent getting stuck on misclicks

---

## Next Steps

1. Implement poll-based structure in all scripts
2. Fix walker usage with proper break conditions
3. Update all object interactions to use `tapGameScreen` with visibility checks
4. Randomize all timeout values
5. Test and verify improvements
