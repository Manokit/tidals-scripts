# Script

**Type:** Abstract Class

**Implements:** ScriptOptions, ScriptCore

## Fields

| Type | Field |
|------|-------|
| `protected long` | `startTime` |

## Constructors

| Constructor |
|-------------|
| `Script(Object scriptCore)` |

## Methods

| Return Type | Method |
|------------|--------|
| `void` | `addCustomMap(MapDefinition mapDefinition)` |
| `AppManager` | `getAppManager()` |
| `Integer` | `getCurrentWorld()` |
| `WorldPosition` | `getExpectedWorldPosition()` |
| `Finger` | `getFinger()` |
| `Map<String, List<SearchableImage>>` | `getImportedImages()` |
| `ImageAnalyzer` | `getImageAnalyzer()` |
| `ItemManager` | `getItemManager()` |
| `Keyboard` | `getKeyboard()` |
| `long` | `getLastPositionChangeMillis()` |
| `LocalPosition` | `getLocalPosition()` |
| `ObjectManager` | `getObjectManager()` |
| `OCR` | `getOCR()` |
| `String` | `getOSMBUsername()` |
| `PixelAnalyzer` | `getPixelAnalyzer()` |
| `ProfileManager` | `getProfileManager()` |
| `SceneManager` | `getSceneManager()` |
| `SceneProjector` | `getSceneProjector()` |
| `Screen` | `getScreen()` |
| `SpriteManager` | `getSpriteManager()` |
| `StageController` | `getStageController()` |
| `long` | `getStartTime()` |
| `Utils` | `getUtils()` |
| `Walker` | `getWalker()` |
| `Direction` | `getWalkingDirection()` |
| `WidgetManager` | `getWidgetManager()` |
| `WorldPosition` | `getWorldPosition()` |
| `Map<SkillType, XPTracker>` | `getXPTrackers()` |
| `void` | `log(String message)` |
| `void` | `log(Class, String)` |
| `void` | `log(String, String)` |
| `void` | `onGameStateChanged(GameState newGameState)` |
| `boolean` | `paused()` |
| `abstract int` | `poll()` |
| `boolean` | `pollFramesHuman(BooleanSupplier condition, int timeout)` |
| `boolean` | `pollFramesHuman(BooleanSupplier condition, int timeout, boolean ignoreTasks)` |
| `boolean` | `pollFramesUntil(BooleanSupplier condition, int timeout)` |
| `boolean` | `pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks)` |
| `boolean` | `pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks, boolean humanisedDelayAfter)` |
| `boolean` | `pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreGameState, boolean ignoreTasks, boolean humanisedDelayAfter, boolean ignoreMenu)` |
| `int` | `random(int num)` |
| `int` | `random(int low, int high)` |
| `int` | `random(long low, long high)` |
| `void` | `setExpectedRegionId(Integer expectedRegionId)` |
| `void` | `setPause(boolean pause)` |
| `void` | `sleep(int millis)` |
| `void` | `sleep(long millis)` |
| `void` | `stop()` |
| `boolean` | `stopped()` |
| `boolean` | `submitHumanTask(BooleanSupplier condition, int timeout)` |
| `boolean` | `submitHumanTask(BooleanSupplier condition, int timeout, boolean ignoreGamestate, boolean ignoreTasks)` |
| `boolean` | `submitTask(BooleanSupplier condition, int timeout)` |
| `boolean` | `submitTask(BooleanSupplier condition, int timeout, boolean ignoreGamestate, boolean ignoreTasks)` |

## Method Details

### setExpectedRegionId
```java
public void setExpectedRegionId(Integer expectedRegionId)
```

Sets the expected region ID for the script. The region set will be searched immediately after failing to search the region where we previously found our position. Once the expected region is found, the expected region ID will be set to null.

**Parameters:**
- `expectedRegionId` - The expected region ID. If null, the expected region ID will be cleared.

### getImportedImages
```java
public Map<String, List<SearchableImage>> getImportedImages()
```

### poll
```java
public abstract int poll()
```

### getStartTime
```java
public long getStartTime()
```

### getSceneManager
```java
public SceneManager getSceneManager()
```

### getFinger
```java
public Finger getFinger()
```

### log
```java
public void log(String message)
```

```java
public void log(Class, String)
```

```java
public void log(String, String)
```

### getScreen
```java
public Screen getScreen()
```

### getLocalPosition
```java
public LocalPosition getLocalPosition()
```

**Returns:** The current LocalPosition of the player, or null if the position is unknown.

### getExpectedWorldPosition
```java
public WorldPosition getExpectedWorldPosition()
```

Calculates the expected WorldPosition based on the current position and walking direction.

This method adjusts the current world position to the next tile boundary in the direction of movement, simulating the expected position after a walking step. If the walking direction is `null`, the current position is returned.

- If moving NORTH, rounds Y up to the next integer if not already on a tile boundary.
- If moving SOUTH, rounds Y down.
- If moving EAST, rounds X up.
- If moving WEST, rounds X down.
- Diagonal directions adjust both X and Y accordingly.

**Returns:** the expected WorldPosition after moving in the current walking direction, or the current position if direction is `null`, or `null` if position is unknown

### getWalkingDirection
```java
public Direction getWalkingDirection()
```

Gets the current walking direction of the player. This is worked out by comparing to the last known position.

**Returns:** The current walking direction of the player, or null if the direction cannot be determined.

### onGameStateChanged
```java
public void onGameStateChanged(GameState newGameState)
```

Called whenever the game state changes.

**Parameters:**
- `newGameState` - The new game state.

### getWidgetManager
```java
public WidgetManager getWidgetManager()
```

### getSpriteManager
```java
public SpriteManager getSpriteManager()
```

### getObjectManager
```java
public ObjectManager getObjectManager()
```

### getOCR
```java
public OCR getOCR()
```

### getImageAnalyzer
```java
public ImageAnalyzer getImageAnalyzer()
```

### getAppManager
```java
public AppManager getAppManager()
```

### getKeyboard
```java
public Keyboard getKeyboard()
```

### getPixelAnalyzer
```java
public PixelAnalyzer getPixelAnalyzer()
```

### getSceneProjector
```java
public SceneProjector getSceneProjector()
```

### getStageController
```java
public StageController getStageController()
```

### getCurrentWorld
```java
public Integer getCurrentWorld()
```

Gets the current world ID.

**Returns:** The current world ID, or null if not set.

### getWalker
```java
public Walker getWalker()
```

### getUtils
```java
public Utils getUtils()
```

### setPause
```java
public void setPause(boolean pause)
```

Pauses or unpauses the script.

**Parameters:**
- `pause` - true to pause, false to unpause

### getWorldPosition
```java
public WorldPosition getWorldPosition()
```

**Returns:** The current WorldPosition of the player, or null if the position is unknown.

### getLastPositionChangeMillis
```java
public long getLastPositionChangeMillis()
```

### submitTask
```java
public boolean submitTask(BooleanSupplier condition, int timeout)
```

```java
public boolean submitTask(BooleanSupplier condition, int timeout, boolean ignoreGamestate, boolean ignoreTasks)
```

### submitHumanTask
```java
public boolean submitHumanTask(BooleanSupplier condition, int timeout, boolean ignoreGamestate, boolean ignoreTasks)
```

```java
public boolean submitHumanTask(BooleanSupplier condition, int timeout)
```

### getProfileManager
```java
public ProfileManager getProfileManager()
```

### stopped
```java
public boolean stopped()
```

Checks if the script has been requested to stop.

**Returns:** true if a stop has been requested, false otherwise

### paused
```java
public boolean paused()
```

### stop
```java
public void stop()
```

Requests the script to stop. The script will stop as soon as possible. This method is non-blocking and returns immediately.

### sleep
```java
public void sleep(int millis)
```

Sleeps for the specified number of milliseconds, handling interruptions appropriately.

This method pauses the current thread for the given duration. If the sleep is interrupted, it catches the InterruptedException and re-interrupts the thread to preserve the interrupt status.

**Parameters:**
- `millis` - The number of milliseconds to sleep.

---

```java
public void sleep(long millis)
```

### random
```java
public int random(int num)
```

```java
public int random(int low, int high)
```

```java
public int random(long low, long high)
```

### getItemManager
```java
public ItemManager getItemManager()
```

### pollFramesHuman
```java
public boolean pollFramesHuman(BooleanSupplier condition, int timeout)
```

Same as `pollFramesUntil(BooleanSupplier, int)` but with an additional _human-like delay_ added after the condition is satisfied.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

**Interruptions**

If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll.

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** TaskInterruptedException - if interrupted by permitted higher-priority behaviour

---

```java
public boolean pollFramesHuman(BooleanSupplier condition, int timeout, boolean ignoreTasks)
```

Same as `pollFramesHuman(BooleanSupplier, int)` but allows control over whether higher-priority tasks (breaks, hops, AFK) are ignored.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

- If `ignoreTasks` is `true`, interruptions are suppressed.
- If `ignoreTasks` is `false`, interruptions may occur as normal.

**Interruptions**

If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll.

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** TaskInterruptedException - if interrupted and `ignoreTasks` is `false`

### pollFramesUntil
```java
public boolean pollFramesUntil(BooleanSupplier condition, int timeout)
```

Polls the given `condition` once per frame until it evaluates to `true` or the `timeout` (in milliseconds) expires.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

**Interruptions**

If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll.

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** TaskInterruptedException - if interrupted by permitted higher-priority behaviour

---

```java
public boolean pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks)
```

Same as `pollFramesUntil(BooleanSupplier, int)` but allows control over whether higher-priority tasks (breaks, hops, AFK) are ignored.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

- If `ignoreTasks` is `true`, interruptions are suppressed.
- If `ignoreTasks` is `false`, interruptions may occur as normal.

**Interruptions**

If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll.

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** TaskInterruptedException - if interrupted and `ignoreTasks` is `false`

---

```java
public boolean pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks, boolean humanisedDelayAfter)
```

Polls the given `condition` once per frame until it evaluates to `true` or the `timeout` expires, with options for priority handling and human-like delay.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

- If `ignoreTasks` is `true`, the poll will continue uninterrupted even if higher-priority events (breaks, hops, AFK) occur.
- If `humanisedDelayAfter` is `true`, a short, natural pause is added after the condition is satisfied to mimic human reaction time.

**Interruptions**

If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll.

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts (breaks/hops/AFK)
- `humanisedDelayAfter` - whether to add a natural pause after completion

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** TaskInterruptedException - if interrupted and `ignoreTasks` is `false`

---

```java
public boolean pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreGameState, boolean ignoreTasks, boolean humanisedDelayAfter, boolean ignoreMenu)
```

Polls the given `condition` once per frame until it evaluates to `true` or the `timeout` expires, with options for priority handling and human-like delay.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

- If `ignoreTasks` is `true`, the poll will continue uninterrupted even if higher-priority events (breaks, hops, AFK) occur.
- If `humanisedDelayAfter` is `true`, a short, natural pause is added after the condition is satisfied to mimic human reaction time.

**Interruptions**

If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll (defaults to `#canBreak()`).

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts (breaks/hops/AFK)
- `humanisedDelayAfter` - whether to add a natural pause after completion
- `ignoreMenu` - whether to ignore an open minimenu when polling

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** TaskInterruptedException - if interrupted and `ignoreTasks` is `false`

### getOSMBUsername
```java
public String getOSMBUsername()
```

### addCustomMap
```java
public void addCustomMap(MapDefinition mapDefinition)
```

Adds a custom map to the location service. This map will be treated as a prioritised region.

**Parameters:**
- `mapDefinition` - The map definition to add.

### getXPTrackers
```java
public Map<SkillType, XPTracker> getXPTrackers()
```

Gets a map of all current XPTracker instances, keyed by their corresponding SkillType.

Each XPTracker in the map tracks experience gained for its associated skill using the XPDropsComponent. If no experience has been tracked for a skill, it will not appear in the map.

**Returns:** A map of SkillType to XPTracker for all skills with tracked experience.
