# OSMB API - Script Framework

Script framework including Script base class, configurations, and task system

---

## Classes in this Module

- [Class SkillCategory](#class-skillcategory) [class]
- [GameStateChangedException](#gamestatechangedexception) [class]
- [HaltScriptException](#haltscriptexception) [class]
- [PriorityTaskException](#prioritytaskexception) [class]
- [Script](#script) [class]
- [ScriptDefinition](#scriptdefinition) [class]
- [ScriptHeader](#scriptheader) [class]
- [ScriptOptions](#scriptoptions) [class]
- [TaskInterruptedException](#taskinterruptedexception) [class]

---

## Class SkillCategory

**Package:** `com.osmb.api.script`

**Type:** Class

**Extends/Implements:** extends Enum<SkillCategory>

### Methods

#### `values()`

**Returns:** `SkillCategory[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `SkillCategory`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getFileName()`

**Returns:** `String`

#### `getName()`

**Returns:** `String`

#### `getById(int id)`

**Returns:** `SkillCategory`

#### `getNames()`

**Returns:** `String[]`

#### `getScriptTypeForString(String categoryName)`

**Returns:** `SkillCategory`

#### `toString()`

**Returns:** `String`


---

## GameStateChangedException

**Package:** `com.osmb.api.script.task.exception`

**Type:** Class

**Extends/Implements:** extends TaskInterruptedException


---

## HaltScriptException

**Package:** `com.osmb.api.script.task.exception`

**Type:** Class

**Extends/Implements:** extends TaskInterruptedException


---

## PriorityTaskException

**Package:** `com.osmb.api.script.task.exception`

**Type:** Class

**Extends/Implements:** extends TaskInterruptedException


---

## Script

**Package:** `com.osmb.api.script`

**Type:** Class

**Extends/Implements:** extends Object implements ScriptCore, ScriptOptions

### Fields

- `protected long startTime`

### Methods

#### `setExpectedRegionId(Integer expectedRegionId)`

Description copied from interface: ScriptCore

**Parameters:**
- `expectedRegionId` - The expected region ID. If null, the expected region ID will be cleared.

#### `getImportedImages()`

**Returns:** `Map<String,SearchableImage>`

#### `poll()`

**Returns:** `int`

#### `getStartTime()`

**Returns:** `long`

#### `getSceneManager()`

**Returns:** `SceneManager`

#### `getFinger()`

**Returns:** `Finger`

#### `log(String message)`

#### `getScreen()`

**Returns:** `Screen`

#### `getLocalPosition()`

**Returns:** `LocalPosition`

Description copied from interface: ScriptCore

**Returns:** The current LocalPosition of the player, or null if the position is unknown.

#### `getExpectedWorldPosition()`

**Returns:** `WorldPosition`

Description copied from interface: ScriptCore

**Returns:** the expected WorldPosition after moving in the current walking direction, or the current position if direction is null, or null if position is unknown

#### `getWalkingDirection()`

**Returns:** `Direction`

Description copied from interface: ScriptCore

**Returns:** The current walking direction of the player, or null if the direction cannot be determined.

#### `onGameStateChanged(GameState newGameState)`

Description copied from interface: ScriptCore

**Parameters:**
- `newGameState` - The new game state.

#### `getWidgetManager()`

**Returns:** `WidgetManager`

#### `getSpriteManager()`

**Returns:** `SpriteManager`

#### `getObjectManager()`

**Returns:** `ObjectManager`

#### `getOCR()`

**Returns:** `OCR`

#### `getImageAnalyzer()`

**Returns:** `ImageAnalyzer`

#### `getAppManager()`

**Returns:** `AppManager`

#### `getKeyboard()`

**Returns:** `Keyboard`

#### `getPixelAnalyzer()`

**Returns:** `PixelAnalyzer`

#### `getSceneProjector()`

**Returns:** `SceneProjector`

#### `getStageController()`

**Returns:** `StageController`

#### `getCurrentWorld()`

**Returns:** `Integer`

Description copied from interface: ScriptCore

**Returns:** The current world ID, or null if not set.

#### `getWalker()`

**Returns:** `Walker`

#### `getUtils()`

**Returns:** `Utils`

#### `setPause(boolean pause)`

Description copied from interface: ScriptCore

**Parameters:**
- `pause` - true to pause, false to unpause

#### `log(Class tag, String msg)`

#### `log(String tag, String message)`

#### `getWorldPosition()`

**Returns:** `WorldPosition`

Description copied from interface: ScriptCore

**Returns:** The current WorldPosition of the player, or null if the position is unknown.

#### `getLastPositionChangeMillis()`

**Returns:** `long`

#### `submitTask(BooleanSupplier task, int timeout)`

**Returns:** `boolean`

#### `submitTask(BooleanSupplier condition, int timeout, boolean ignoreGamestate, boolean ignoreTasks)`

**Returns:** `boolean`

#### `submitHumanTask(BooleanSupplier condition, int timeout, boolean ignoreGamestate, boolean ignoreTasks)`

**Returns:** `boolean`

#### `submitHumanTask(BooleanSupplier condition, int timeout)`

**Returns:** `boolean`

#### `getProfileManager()`

**Returns:** `ProfileManager`

#### `stopped()`

**Returns:** `boolean`

Description copied from interface: ScriptCore

**Returns:** true if a stop has been requested, false otherwise

#### `paused()`

**Returns:** `boolean`

#### `stop()`

Description copied from interface: ScriptCore

#### `sleep(int millis)`

Description copied from interface: ScriptCore

**Parameters:**
- `millis` - The number of milliseconds to sleep.

#### `sleep(long millis)`

#### `random(int num)`

**Returns:** `int`

#### `random(int low, int high)`

**Returns:** `int`

#### `random(long low, long high)`

**Returns:** `int`

#### `getItemManager()`

**Returns:** `ItemManager`

#### `pollFramesHuman(BooleanSupplier condition, int timeout)`

**Returns:** `boolean`

Description copied from interface: ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted by permitted higher-priority behaviour

#### `pollFramesHuman(BooleanSupplier condition, int timeout, boolean ignoreTasks)`

**Returns:** `boolean`

Description copied from interface: ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted and ignoreTasks is false

#### `pollFramesUntil(BooleanSupplier condition, int timeout)`

**Returns:** `boolean`

Description copied from interface: ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted by permitted higher-priority behaviour

#### `pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks)`

**Returns:** `boolean`

Description copied from interface: ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted and ignoreTasks is false

#### `pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks, boolean humanisedDelayAfter)`

**Returns:** `boolean`

Description copied from interface: ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts (breaks/hops/AFK)
- `humanisedDelayAfter` - whether to add a natural pause after completion

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted and ignoreTasks is false

#### `pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreGameState, boolean ignoreTasks, boolean humanisedDelayAfter, boolean ignoreMenu)`

**Returns:** `boolean`

Description copied from interface: ScriptCore

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts (breaks/hops/AFK)
- `humanisedDelayAfter` - whether to add a natural pause after completion
- `ignoreMenu` - whether to ignore an open minimenu when polling

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted and ignoreTasks is false

#### `getOSMBUsername()`

**Returns:** `String`

#### `getDiscordUsername()`

**Returns:** `String`

#### `addCustomMap(MapDefinition mapDefinition)`

Description copied from interface: ScriptCore

**Parameters:**
- `mapDefinition` - The map definition to add.

#### `getXPTrackers()`

**Returns:** `Map<SkillType,XPTracker>`

Description copied from interface: ScriptCore

**Returns:** A map of SkillType to XPTracker for all skills with tracked experience.


---

## ScriptDefinition

**Package:** `com.osmb.api.script`

**Type:** Class


---

## ScriptHeader

**Package:** `com.osmb.api.script`

**Type:** Class


---

## ScriptOptions

**Package:** `com.osmb.api.script.configuration`

**Type:** Class

### Methods

#### `canBreak()`

**Returns:** `boolean`

#### `canHopWorlds()`

**Returns:** `boolean`

#### `canAFK()`

**Returns:** `boolean`

#### `promptBankTabDialogue()`

**Returns:** `boolean`

#### `onRelog()`

#### `onStart()`

#### `importImages(Set<ImageImport> imageImportSet)`

#### `trackXP()`

**Returns:** `boolean`

#### `afkTimers()`

**Returns:** `List<AFKTime>`

#### `enableCameraOffsetSync()`

**Returns:** `boolean`

On script start up, the script will sync the camera offset by walking diagonally. WARNING: If disabled and the camera offset may be wrong (unless the user reloaded the app and hasn't moved since starting the script) This can cause miss-clicking issues or detection issues if you rely on precise shapes in the 3d world.

**Returns:** true to enable camera offset sync, false to disable it.

#### `regionsToPrioritise()`

**Returns:** `int[]`

#### `skipCompassYawCheck()`

**Returns:** `boolean`

Skips the compass yaw check when finding position. This can be useful for certain instances where the compass is not visible.

**Returns:** true to skip the compass yaw check, false to perform the check.


---

## TaskInterruptedException

**Package:** `com.osmb.api.script.task.exception`

**Type:** Class

**Extends/Implements:** extends RuntimeException

### Methods

#### `getReason()`

**Returns:** `String`


---

