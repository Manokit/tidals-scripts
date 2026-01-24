# ScriptFacade

**Type:** Abstract Class

**Extends:** Object

**Implements:** ScriptCore

Facade for script tasks to access ScriptCore methods without referencing ScriptCore directly.

## Constructors

| Constructor |
|-------------|
| `ScriptFacade(ScriptCore scriptCore)` |

## Methods

| Return Type | Method |
|------------|--------|
| `void` | `addCustomMap(MapDefinition mapDefinition)` |
| `AppManager` | `getAppManager()` |
| `Integer` | `getCurrentWorld()` |
| `String` | `getDiscordUsername()` |
| `WorldPosition` | `getExpectedWorldPosition()` |
| `Finger` | `getFinger()` |
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

### Inherited Methods from Object

clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait

## Constructor Details

### ScriptFacade
```java
public ScriptFacade(ScriptCore scriptCore)
```

## Method Details

### setExpectedRegionId
```java
public void setExpectedRegionId(Integer expectedRegionId)
```

Sets the expected region ID for the script. The region set will be searched immediately after failing to search the region where we previously found our position. Once the expected region is found, the expected region ID will be set to null.

**Specified by:** setExpectedRegionId in interface ScriptCore

**Parameters:**
- `expectedRegionId` - The expected region ID. If null, the expected region ID will be cleared.

### getSceneManager
```java
public SceneManager getSceneManager()
```

**Specified by:** getSceneManager in interface ScriptCore

### getFinger
```java
public Finger getFinger()
```

**Specified by:** getFinger in interface ScriptCore

### log
```java
public void log(String message)
```

**Specified by:** log in interface ScriptCore

---

```java
public void log(Class, String)
```

**Specified by:** log in interface ScriptCore

---

```java
public void log(String, String)
```

**Specified by:** log in interface ScriptCore

### getScreen
```java
public Screen getScreen()
```

**Specified by:** getScreen in interface ScriptCore

### getLocalPosition
```java
public LocalPosition getLocalPosition()
```

**Specified by:** getLocalPosition in interface ScriptCore

**Returns:** The current `LocalPosition` of the player, or null if the position is unknown.

### getExpectedWorldPosition
```java
public WorldPosition getExpectedWorldPosition()
```

Calculates the expected `WorldPosition` based on the current position and walking direction.

This method adjusts the current world position to the next tile boundary in the direction of movement, simulating the expected position after a walking step. If the walking direction is `null`, the current position is returned.

- If moving NORTH, rounds Y up to the next integer if not already on a tile boundary.
- If moving SOUTH, rounds Y down.
- If moving EAST, rounds X up.
- If moving WEST, rounds X down.
- Diagonal directions adjust both X and Y accordingly.

**Specified by:** getExpectedWorldPosition in interface ScriptCore

**Returns:** the expected `WorldPosition` after moving in the current walking direction, or the current position if direction is `null`, or `null` if position is unknown

### getWalkingDirection
```java
public Direction getWalkingDirection()
```

Gets the current walking direction of the player. This is worked out by comparing to the last known position.

**Specified by:** getWalkingDirection in interface ScriptCore

**Returns:** The current walking direction of the player, or null if the direction cannot be determined.

### onGameStateChanged
```java
public void onGameStateChanged(GameState newGameState)
```

Called whenever the game state changes.

**Specified by:** onGameStateChanged in interface ScriptCore

**Parameters:**
- `newGameState` - The new game state.

### getWidgetManager
```java
public WidgetManager getWidgetManager()
```

**Specified by:** getWidgetManager in interface ScriptCore

### getSpriteManager
```java
public SpriteManager getSpriteManager()
```

**Specified by:** getSpriteManager in interface ScriptCore

### getObjectManager
```java
public ObjectManager getObjectManager()
```

**Specified by:** getObjectManager in interface ScriptCore

### getOCR
```java
public OCR getOCR()
```

**Specified by:** getOCR in interface ScriptCore

### getImageAnalyzer
```java
public ImageAnalyzer getImageAnalyzer()
```

**Specified by:** getImageAnalyzer in interface ScriptCore

### getAppManager
```java
public AppManager getAppManager()
```

**Specified by:** getAppManager in interface ScriptCore

### getKeyboard
```java
public Keyboard getKeyboard()
```

**Specified by:** getKeyboard in interface ScriptCore

### getPixelAnalyzer
```java
public PixelAnalyzer getPixelAnalyzer()
```

**Specified by:** getPixelAnalyzer in interface ScriptCore

### getSceneProjector
```java
public SceneProjector getSceneProjector()
```

**Specified by:** getSceneProjector in interface ScriptCore

### getStageController
```java
public StageController getStageController()
```

**Specified by:** getStageController in interface ScriptCore

### getCurrentWorld
```java
public Integer getCurrentWorld()
```

Gets the current world ID.

**Specified by:** getCurrentWorld in interface ScriptCore

**Returns:** The current world ID, or null if not set.

### getWalker
```java
public Walker getWalker()
```

**Specified by:** getWalker in interface ScriptCore

### getUtils
```java
public Utils getUtils()
```

**Specified by:** getUtils in interface ScriptCore

### setPause
```java
public void setPause(boolean pause)
```

Pauses or unpauses the script.

**Specified by:** setPause in interface ScriptCore

**Parameters:**
- `pause` - true to pause, false to unpause

### getWorldPosition
```java
public WorldPosition getWorldPosition()
```

**Specified by:** getWorldPosition in interface ScriptCore

**Returns:** The current `WorldPosition` of the player, or null if the position is unknown.

### getLastPositionChangeMillis
```java
public long getLastPositionChangeMillis()
```

**Specified by:** getLastPositionChangeMillis in interface ScriptCore

### submitTask
```java
public boolean submitTask(BooleanSupplier condition, int timeout)
```

**Specified by:** submitTask in interface ScriptCore

---

```java
public boolean submitTask(BooleanSupplier condition, int timeout, boolean ignoreGamestate, boolean ignoreTasks)
```

**Specified by:** submitTask in interface ScriptCore

### submitHumanTask
```java
public boolean submitHumanTask(BooleanSupplier condition, int timeout, boolean ignoreGamestate, boolean ignoreTasks)
```

**Specified by:** submitHumanTask in interface ScriptCore

---

```java
public boolean submitHumanTask(BooleanSupplier condition, int timeout)
```

**Specified by:** submitHumanTask in interface ScriptCore

### getDiscordUsername
```java
public String getDiscordUsername()
```

**Specified by:** getDiscordUsername in interface ScriptCore

### getProfileManager
```java
public ProfileManager getProfileManager()
```

**Specified by:** getProfileManager in interface ScriptCore

### stopped
```java
public boolean stopped()
```

Checks if the script has been requested to stop.

**Specified by:** stopped in interface ScriptCore

**Returns:** true if a stop has been requested, false otherwise

### paused
```java
public boolean paused()
```

**Specified by:** paused in interface ScriptCore

### stop
```java
public void stop()
```

Requests the script to stop. The script will stop as soon as possible. This method is non-blocking and returns immediately.

**Specified by:** stop in interface ScriptCore

### sleep
```java
public void sleep(int millis)
```

Sleeps for the specified number of milliseconds, handling interruptions appropriately.

This method pauses the current thread for the given duration. If the sleep is interrupted, it catches the `InterruptedException` and re-interrupts the thread to preserve the interrupt status.

**Specified by:** sleep in interface ScriptCore

**Parameters:**
- `millis` - The number of milliseconds to sleep.

---

```java
public void sleep(long millis)
```

**Specified by:** sleep in interface ScriptCore

### random
```java
public int random(int num)
```

**Specified by:** random in interface ScriptCore

---

```java
public int random(int low, int high)
```

**Specified by:** random in interface ScriptCore

---

```java
public int random(long low, long high)
```

**Specified by:** random in interface ScriptCore

### getItemManager
```java
public ItemManager getItemManager()
```

**Specified by:** getItemManager in interface ScriptCore

### pollFramesHuman
```java
public boolean pollFramesHuman(BooleanSupplier condition, int timeout)
```

Same as `ScriptCore.pollFramesUntil(BooleanSupplier, int)` but with an additional _human-like delay_ added after the condition is satisfied.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

**Interruptions**  
If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a `TaskInterruptedException` will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll.

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress `TaskInterruptedException` in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Specified by:** pollFramesHuman in interface ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** `TaskInterruptedException` - if interrupted by permitted higher-priority behaviour

---

```java
public boolean pollFramesHuman(BooleanSupplier condition, int timeout, boolean ignoreTasks)
```

Same as `ScriptCore.pollFramesHuman(BooleanSupplier, int)` but allows control over whether higher-priority tasks (breaks, hops, AFK) are ignored.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

- If `ignoreTasks` is `true`, interruptions are suppressed.
- If `ignoreTasks` is `false`, interruptions may occur as normal.

**Interruptions**  
If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a `TaskInterruptedException` will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll.

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress `TaskInterruptedException` in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Specified by:** pollFramesHuman in interface ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** `TaskInterruptedException` - if interrupted and `ignoreTasks` is `false`

### pollFramesUntil
```java
public boolean pollFramesUntil(BooleanSupplier condition, int timeout)
```

Polls the given `condition` once per frame until it evaluates to `true` or the `timeout` (in milliseconds) expires.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

**Interruptions**  
If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a `TaskInterruptedException` will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll.

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress `TaskInterruptedException` in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Specified by:** pollFramesUntil in interface ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** `TaskInterruptedException` - if interrupted by permitted higher-priority behaviour

---

```java
public boolean pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks)
```

Same as `ScriptCore.pollFramesUntil(BooleanSupplier, int)` but allows control over whether higher-priority tasks (breaks, hops, AFK) are ignored.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

- If `ignoreTasks` is `true`, interruptions are suppressed.
- If `ignoreTasks` is `false`, interruptions may occur as normal.

**Interruptions**  
If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a `TaskInterruptedException` will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll.

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress `TaskInterruptedException` in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Specified by:** pollFramesUntil in interface ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** `TaskInterruptedException` - if interrupted and `ignoreTasks` is `false`

---

```java
public boolean pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks, boolean humanisedDelayAfter)
```

Polls the given `condition` once per frame until it evaluates to `true` or the `timeout` expires, with options for priority handling and human-like delay.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

- If `ignoreTasks` is `true`, the poll will continue uninterrupted even if higher-priority events (breaks, hops, AFK) occur.
- If `humanisedDelayAfter` is `true`, a short, natural pause is added after the condition is satisfied to mimic human reaction time.

**Interruptions**  
If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a `TaskInterruptedException` will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll.

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress `TaskInterruptedException` in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Specified by:** pollFramesUntil in interface ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts (breaks/hops/AFK)
- `humanisedDelayAfter` - whether to add a natural pause after completion

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** `TaskInterruptedException` - if interrupted and `ignoreTasks` is `false`

---

```java
public boolean pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreGameState, boolean ignoreTasks, boolean humanisedDelayAfter, boolean ignoreMenu)
```

Polls the given `condition` once per frame until it evaluates to `true` or the `timeout` expires, with options for priority handling and human-like delay.

Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data.

- If `ignoreTasks` is `true`, the poll will continue uninterrupted even if higher-priority events (breaks, hops, AFK) occur.
- If `humanisedDelayAfter` is `true`, a short, natural pause is added after the condition is satisfied to mimic human reaction time.

**Interruptions**  
If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a `TaskInterruptedException` will be thrown. The following methods control whether these interruptions are allowed:

- `ScriptOptions.canBreak()` - if `true`, break timers may interrupt this poll.
- `ScriptOptions.canHopWorlds()` - if `true`, world-hop tasks may interrupt this poll.
- `ScriptOptions.canAFK()` - if `true`, AFK timers may interrupt this poll (defaults to `#canBreak()`).

Override these methods to return `false` if you want to prevent interruptions from the corresponding task type.

**Warning:** Do not catch or suppress `TaskInterruptedException` in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Specified by:** pollFramesUntil in interface ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts (breaks/hops/AFK)
- `humanisedDelayAfter` - whether to add a natural pause after completion
- `ignoreMenu` - whether to ignore an open minimenu when polling

**Returns:** `true` if the condition passed before the timeout, otherwise `false`

**Throws:** `TaskInterruptedException` - if interrupted and `ignoreTasks` is `false`

### getOSMBUsername
```java
public String getOSMBUsername()
```

**Specified by:** getOSMBUsername in interface ScriptCore

### addCustomMap
```java
public void addCustomMap(MapDefinition mapDefinition)
```

Adds a custom map to the location service. This map will be treated as a prioritised region.

**Specified by:** addCustomMap in interface ScriptCore

**Parameters:**
- `mapDefinition` - The map definition to add.

### getXPTrackers
```java
public Map<SkillType, XPTracker> getXPTrackers()
```

Gets a map of all current `XPTracker` instances, keyed by their corresponding `SkillType`.

Each `XPTracker` in the map tracks experience gained for its associated skill using the `XPDropsComponent`. If no experience has been tracked for a skill, it will not appear in the map.

**Specified by:** getXPTrackers in interface ScriptCore

**Returns:** A map of `SkillType` to `XPTracker` for all skills with tracked experience.
