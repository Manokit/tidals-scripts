# ScriptCore

**Package:** `com.osmb.api`

**Type:** Interface

The central interface for OSMB scripts, providing access to all major API managers and core functionality.

## Manager Access Methods

### getAppManager()

```java
AppManager getAppManager()
```

Gets the application manager.

**Returns:** The `AppManager` instance.

---

### getFinger()

```java
Finger getFinger()
```

Gets the finger (mouse/touch input) interface.

**Returns:** The `Finger` instance.

---

### getImageAnalyzer()

```java
ImageAnalyzer getImageAnalyzer()
```

Gets the image analyzer for visual processing.

**Returns:** The `ImageAnalyzer` instance.

---

### getItemManager()

```java
ItemManager getItemManager()
```

Gets the item manager for item operations.

**Returns:** The `ItemManager` instance.

---

### getKeyboard()

```java
Keyboard getKeyboard()
```

Gets the keyboard input interface.

**Returns:** The `Keyboard` instance.

---

### getObjectManager()

```java
ObjectManager getObjectManager()
```

Gets the object manager for scene objects.

**Returns:** The `ObjectManager` instance.

---

### getOCR()

```java
OCR getOCR()
```

Gets the OCR (Optical Character Recognition) interface.

**Returns:** The `OCR` instance.

---

### getPixelAnalyzer()

```java
PixelAnalyzer getPixelAnalyzer()
```

Gets the pixel analyzer for color-based operations.

**Returns:** The `PixelAnalyzer` instance.

---

### getProfileManager()

```java
ProfileManager getProfileManager()
```

Gets the profile manager for break/hop profiles.

**Returns:** The `ProfileManager` instance.

---

### getSceneManager()

```java
SceneManager getSceneManager()
```

Gets the scene manager for game scene operations.

**Returns:** The `SceneManager` instance.

---

### getSceneProjector()

```java
SceneProjector getSceneProjector()
```

Gets the scene projector for 3D to 2D coordinate projection.

**Returns:** The `SceneProjector` instance.

---

### getScreen()

```java
Screen getScreen()
```

Gets the screen interface for display operations.

**Returns:** The `Screen` instance.

---

### getSpriteManager()

```java
SpriteManager getSpriteManager()
```

Gets the sprite manager for UI sprite operations.

**Returns:** The `SpriteManager` instance.

---

### getStageController()

```java
StageController getStageController()
```

Gets the stage controller for script workflow management.

**Returns:** The `StageController` instance.

---

### getUtils()

```java
Utils getUtils()
```

Gets the utilities class with helper methods.

**Returns:** The `Utils` instance.

---

### getWalker()

```java
Walker getWalker()
```

Gets the walker for automated navigation.

**Returns:** The `Walker` instance.

---

### getWidgetManager()

```java
WidgetManager getWidgetManager()
```

Gets the widget manager for UI components.

**Returns:** The `WidgetManager` instance.

---

## Position and Movement Methods

### getWorldPosition()

```java
WorldPosition getWorldPosition()
```

Gets the current `WorldPosition` of the player.

**Returns:** The player's current world position.

---

### getLocalPosition()

```java
LocalPosition getLocalPosition()
```

Gets the current `LocalPosition` of the player.

**Returns:** The player's current local position.

---

### getExpectedWorldPosition()

```java
WorldPosition getExpectedWorldPosition()
```

Calculates the expected `WorldPosition` based on the current position and walking direction.

**Returns:** The expected world position after the current step.

---

### getWalkingDirection()

```java
Direction getWalkingDirection()
```

Gets the current walking direction of the player.

**Returns:** The current walking `Direction`.

---

### getLastPositionChangeMillis()

```java
long getLastPositionChangeMillis()
```

Gets the timestamp of the last position change.

**Returns:** Milliseconds since last position change.

---

## World and Region Methods

### getCurrentWorld()

```java
Integer getCurrentWorld()
```

Gets the current world ID.

**Returns:** The current world number.

---

### getOSMBUsername()

```java
String getOSMBUsername()
```

Gets the OSMB username.

**Returns:** The OSMB username.

---

### addCustomMap(MapDefinition)

```java
void addCustomMap(MapDefinition mapDefinition)
```

Adds a custom map to the location service.

**Parameters:**
- `mapDefinition` - The custom map definition to add.

---

## XP Tracking Methods

### getXPTrackers()

```java
Map<SkillType, XPTracker> getXPTrackers()
```

Gets a map of all current `XPTracker` instances, keyed by their corresponding `SkillType`.

**Returns:** Map of skill types to XP trackers.

---

## Frame Polling Methods

### pollFramesUntil(BooleanSupplier, int)

```java
boolean pollFramesUntil(BooleanSupplier condition, int timeout)
```

Polls the given `condition` once per frame until it evaluates to `true` or the `timeout` (in milliseconds) expires.

**Parameters:**
- `condition` - The condition to check each frame.
- `timeout` - The timeout in milliseconds.

**Returns:** `true` if the condition was met, `false` if timeout occurred.

---

### pollFramesUntil(BooleanSupplier, int, boolean)

```java
boolean pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks)
```

Same as `pollFramesUntil(BooleanSupplier, int)` but allows control over whether higher-priority tasks (breaks, hops, AFK) are ignored.

**Parameters:**
- `condition` - The condition to check each frame.
- `timeout` - The timeout in milliseconds.
- `ignoreTasks` - Whether to ignore priority tasks during polling.

**Returns:** `true` if the condition was met, `false` if timeout occurred.

---

### pollFramesUntil(BooleanSupplier, int, boolean, boolean)

```java
boolean pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks, boolean humanisedDelayAfter)
```

Polls the given `condition` once per frame until it evaluates to `true` or the `timeout` expires, with options for priority handling and human-like delay.

**Parameters:**
- `condition` - The condition to check each frame.
- `timeout` - The timeout in milliseconds.
- `ignoreTasks` - Whether to ignore priority tasks.
- `humanisedDelayAfter` - Whether to add a human-like delay after condition is met.

**Returns:** `true` if the condition was met, `false` if timeout occurred.

---

### pollFramesUntil(BooleanSupplier, int, boolean, boolean, boolean, boolean)

```java
boolean pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreGameState, boolean ignoreTasks, boolean humanisedDelayAfter, boolean ignoreMenu)
```

Polls the given `condition` once per frame until it evaluates to `true` or the `timeout` expires, with full control over all options.

**Parameters:**
- `condition` - The condition to check each frame.
- `timeout` - The timeout in milliseconds.
- `ignoreGameState` - Whether to ignore game state checks.
- `ignoreTasks` - Whether to ignore priority tasks.
- `humanisedDelayAfter` - Whether to add a human-like delay after.
- `ignoreMenu` - Whether to ignore menu state.

**Returns:** `true` if the condition was met, `false` if timeout occurred.

---

### pollFramesHuman(BooleanSupplier, int)

```java
boolean pollFramesHuman(BooleanSupplier condition, int timeout)
```

Same as `pollFramesUntil(BooleanSupplier, int)` but with an additional *human-like delay* added after the condition is satisfied.

**Parameters:**
- `condition` - The condition to check each frame.
- `timeout` - The timeout in milliseconds.

**Returns:** `true` if the condition was met, `false` if timeout occurred.

---

### pollFramesHuman(BooleanSupplier, int, boolean)

```java
boolean pollFramesHuman(BooleanSupplier condition, int timeout, boolean ignoreTasks)
```

Same as `pollFramesHuman(BooleanSupplier, int)` but allows control over whether higher-priority tasks (breaks, hops, AFK) are ignored.

**Parameters:**
- `condition` - The condition to check each frame.
- `timeout` - The timeout in milliseconds.
- `ignoreTasks` - Whether to ignore priority tasks.

**Returns:** `true` if the condition was met, `false` if timeout occurred.

---

## Sleep and Timing Methods

### sleep(int)

```java
void sleep(int millis)
```

Sleeps for the specified number of milliseconds, handling interruptions appropriately.

**Parameters:**
- `millis` - The number of milliseconds to sleep.

---

### sleep(long)

```java
void sleep(long millis)
```

Sleeps for the specified number of milliseconds.

**Parameters:**
- `millis` - The number of milliseconds to sleep.

---

## Random Number Methods

### random(long, long)

```java
int random(long low, long high)
```

Generates a random integer between low and high (inclusive).

**Parameters:**
- `low` - The lower bound.
- `high` - The upper bound.

**Returns:** A random integer in the specified range.

---

### random(int) (Deprecated)

```java
@Deprecated
int random(int num)
```

**Deprecated:** Use `RandomUtils.uniformRandom(int)` instead.

**Parameters:**
- `num` - The upper bound.

**Returns:** A random integer from 0 to num-1.

---

### random(int, int) (Deprecated)

```java
@Deprecated
int random(int low, int high)
```

**Deprecated:** Use `RandomUtils.uniformRandom(int, int)` instead.

**Parameters:**
- `low` - The lower bound.
- `high` - The upper bound.

**Returns:** A random integer in the specified range.

---

## Script Control Methods

### paused()

```java
boolean paused()
```

Checks if the script is currently paused.

**Returns:** `true` if paused, `false` otherwise.

---

### setPause(boolean)

```java
void setPause(boolean pause)
```

Pauses or unpauses the script.

**Parameters:**
- `pause` - `true` to pause, `false` to unpause.

---

### stop()

```java
void stop()
```

Requests the script to stop.

---

### stopped()

```java
boolean stopped()
```

Checks if the script has been requested to stop.

**Returns:** `true` if stop was requested, `false` otherwise.

---

## Logging Methods

### log(String)

```java
void log(String message)
```

Logs a message to the script console.

**Parameters:**
- `message` - The message to log.

---

### log(String, String)

```java
void log(String tag, String message)
```

Logs a tagged message to the script console.

**Parameters:**
- `tag` - The log tag.
- `message` - The message to log.

---

### log(Class, String)

```java
void log(Class tag, String msg)
```

Logs a message with a class tag.

**Parameters:**
- `tag` - The class to use as a tag.
- `msg` - The message to log.

---

## Lifecycle Hook Methods (Default)

### onGameStateChanged(GameState)

```java
default void onGameStateChanged(GameState newGameState)
```

Called whenever the game state changes.

**Parameters:**
- `newGameState` - The new game state.

---

### onNewFrame()

```java
default void onNewFrame()
```

Called when a new frame is captured, right after updating the position, scene and widgets.

---

### onPaint(Canvas)

```java
default void onPaint(Canvas c)
```

Called to draw custom overlays on the game screen.

**Parameters:**
- `c` - The canvas to draw on.

---

### onRegionChange(int, int)

```java
default void onRegionChange(int newRegionId, int previousRegionId)
```

Called whenever the region ID changes.

**Parameters:**
- `newRegionId` - The new region ID.
- `previousRegionId` - The previous region ID.

---

## Deprecated Methods

### submitTask(BooleanSupplier, int) (Deprecated)

```java
@Deprecated
boolean submitTask(BooleanSupplier condition, int timeout)
```

**Deprecated:** Use `pollFramesUntil(BooleanSupplier, int)` instead.

---

### submitTask(BooleanSupplier, int, boolean, boolean) (Deprecated)

```java
@Deprecated
boolean submitTask(BooleanSupplier condition, int timeout, boolean ignoreGameState, boolean ignoreTasks)
```

**Deprecated:** Use `pollFramesUntil(BooleanSupplier, int, boolean)` instead.

---

### submitHumanTask(BooleanSupplier, int) (Deprecated)

```java
@Deprecated
boolean submitHumanTask(BooleanSupplier condition, int timeout)
```

**Deprecated:** Use `pollFramesHuman(BooleanSupplier, int)` instead.

---

### submitHumanTask(BooleanSupplier, int, boolean, boolean) (Deprecated)

```java
@Deprecated
boolean submitHumanTask(BooleanSupplier condition, int timeout, boolean ignoreGameState, boolean ignoreTasks)
```

**Deprecated:** Use `pollFramesHuman(BooleanSupplier, int, boolean)` instead.

---

### setExpectedRegionId(Integer) (Deprecated)

```java
@Deprecated
void setExpectedRegionId(Integer expectedRegionId)
```

**Deprecated:** This method is no longer recommended for use.
