# OSMB API - Core API

Core interfaces and classes that form the foundation of OSMB scripts

---

## Classes in this Module

- [CanvasDrawable](#canvasdrawable) [class]
- [Class Location](#class-location) [class]
- [Class SkillTotal](#class-skilltotal) [class]
- [Class WorldType](#class-worldtype) [class]
- [ColorPickerPanel](#colorpickerpanel) [class]
- [ItemSearchDialogue](#itemsearchdialogue) [class]
- [JavaFXUtils](#javafxutils) [class]
- [ProgressBar](#progressbar) [class]
- [Screen](#screen) [class]
- [ScriptCore](#scriptcore) [class]
- [TilePickerPanel](#tilepickerpanel) [class]
- [World](#world) [class]
- [WorldNoneExistentException](#worldnoneexistentexception) [class]

---

## CanvasDrawable

**Package:** `com.osmb.api.screen`

**Type:** Class

### Methods

#### `draw(Canvas canvas)`


---

## Class Location

**Package:** `com.osmb.api.world`

**Type:** Class

**Extends/Implements:** extends Enum<Location>

### Methods

#### `values()`

**Returns:** `Location[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `Location`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getLocationId()`

**Returns:** `int`

#### `getName()`

**Returns:** `String`

#### `getImageName()`

**Returns:** `String`

#### `getByName(String name)`

**Returns:** `Location`

#### `getLocationByLocationId(int locationId)`

**Returns:** `Location`


---

## Class SkillTotal

**Package:** `com.osmb.api.world`

**Type:** Class

**Extends/Implements:** extends Enum<SkillTotal>

### Methods

#### `values()`

**Returns:** `SkillTotal[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `SkillTotal`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getTotal()`

**Returns:** `int`

#### `getByInt(int ttl)`

**Returns:** `SkillTotal`


---

## Class WorldType

**Package:** `com.osmb.api.world`

**Type:** Class

**Extends/Implements:** extends Enum<WorldType>

### Methods

#### `values()`

**Returns:** `WorldType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `WorldType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getText()`

**Returns:** `String`


---

## ColorPickerPanel

**Package:** `com.osmb.api.javafx`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `show(ScriptCore core)`

**Returns:** `int`

#### `show(ScriptCore core, String title)`

**Returns:** `int`


---

## ItemSearchDialogue

**Package:** `com.osmb.api.javafx`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `show(ScriptCore core, javafx.stage.Stage primaryStage)`

**Returns:** `int`


---

## JavaFXUtils

**Package:** `com.osmb.api.javafx`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `createItemCombobox(ScriptCore core, int[] values)`

**Returns:** `javafx.scene.control.ComboBox<Integer>`

#### `createItemCombobox(ScriptCore core, boolean noneOption, int[] values)`

**Returns:** `javafx.scene.control.ComboBox<Integer>`

#### `getItemImageView(ScriptCore core, int itemID)`

**Returns:** `javafx.scene.image.ImageView`

#### `centerPopupStage(javafx.stage.Stage mainStage, javafx.stage.Stage popupStage)`


---

## ProgressBar

**Package:** `com.osmb.api.screen.progressbar`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public static final Color XP_PROGRESS_COLOR`
- `public static final Font ARIAL`

### Methods

#### `drawProgressBar(Canvas canvas, int x, int y, SkillType skillType, XPTracker tracker)`

#### `drawProgressBar(int xPos, int yPos, int width, int height, float currentValue, float maxValue, Color borderColor, Color fillColor, Canvas canvas)`


---

## Screen

**Package:** `com.osmb.api.screen`

**Type:** Class

### Methods

#### `getUUID()`

**Returns:** `UUID`

Gets the unique identifier of the current screen image.

**Returns:** The UUID of the current screen being processed.

#### `queueCanvasDrawable(String key, CanvasDrawable canvasDrawable)`

Queues a CanvasDrawable to be processed when the screen is being finalised. Note: All CanvasDrawable's are cleared each time the executor is polled. So canvas drawables are great for visually debugging when using ScriptCore.submitTask(BooleanSupplier, int)

**Parameters:**
- `key` - - The identifier of the CanvasDrawable. This can be used to manually remove/override the CanvasDrawable
- `canvasDrawable` - - The CanvasDrawable to execute.

#### `getDrawableCanvas()`

**Returns:** `Canvas`

Gets the canvas for the display screen. This can be used for directly visual debugging. The canvas is converted to an Image when finalising and then drawn to the canvas.

**Returns:** The drawable canvas

#### `removeCanvasDrawable(String key)`

Removes a CanvasDrawable from the queue.

**Parameters:**
- `key` - The identifier of the CanvasDrawable you want to remove.

#### `getBounds()`

**Returns:** `Rectangle`

Retrieves the boundaries of the current screen being processed.

#### `getImage()`

**Returns:** `Image`

Returns the screen image being currently processed.

**Returns:** The screen image which is currently being processed.

#### `getPreviousImage()`

**Returns:** `Image`

Retrieves the previous screen image.

**Returns:** The previous screen image.

#### `setSceneUpdating(boolean updateScene)`

Disables scene updating completely. WARNING: only do this if you know what you are doing. Disabling scene update will void our known Position and should only be used in situations where we cover or potentially cover the minimap.

**Parameters:**
- `updateScene` - 


---

## ScriptCore

**Package:** `com.osmb.api`

**Type:** Class

### Methods

#### `getOSMBUsername()`

**Returns:** `String`

#### `getDiscordUsername()`

**Returns:** `String`

#### `getAppManager()`

**Returns:** `AppManager`

#### `getWidgetManager()`

**Returns:** `WidgetManager`

#### `getFinger()`

**Returns:** `Finger`

#### `getKeyboard()`

**Returns:** `Keyboard`

#### `getScreen()`

**Returns:** `Screen`

#### `getXPTrackers()`

**Returns:** `Map<SkillType,XPTracker>`

Gets a map of all current XPTracker instances, keyed by their corresponding SkillType. Each XPTracker in the map tracks experience gained for its associated skill using the XPDropsComponent. If no experience has been tracked for a skill, it will not appear in the map.

**Returns:** A map of SkillType to XPTracker for all skills with tracked experience.

#### `getWorldPosition()`

**Returns:** `WorldPosition`

Gets the current WorldPosition of the player.

**Returns:** The current WorldPosition of the player, or null if the position is unknown.

#### `getExpectedWorldPosition()`

**Returns:** `WorldPosition`

Calculates the expected WorldPosition based on the current position and walking direction. This method adjusts the current world position to the next tile boundary in the direction of movement, simulating the expected position after a walking step. If the walking direction is null, the current position is returned. If moving NORTH, rounds Y up to the next integer if not already on a tile boundary. If moving SOUTH, rounds Y down. If moving EAST, rounds X up. If moving WEST, rounds X down. Diagonal directions adjust both X and Y accordingly.

**Returns:** the expected WorldPosition after moving in the current walking direction, or the current position if direction is null, or null if position is unknown

#### `getLocalPosition()`

**Returns:** `LocalPosition`

Gets the current LocalPosition of the player.

**Returns:** The current LocalPosition of the player, or null if the position is unknown.

#### `getWalkingDirection()`

**Returns:** `Direction`

Gets the current walking direction of the player. This is worked out by comparing to the last known position.

**Returns:** The current walking direction of the player, or null if the direction cannot be determined.

#### `getSceneManager()`

**Returns:** `SceneManager`

#### `getImageAnalyzer()`

**Returns:** `ImageAnalyzer`

#### `getPixelAnalyzer()`

**Returns:** `PixelAnalyzer`

#### `getSceneProjector()`

**Returns:** `SceneProjector`

#### `getSpriteManager()`

**Returns:** `SpriteManager`

#### `getObjectManager()`

**Returns:** `ObjectManager`

#### `getItemManager()`

**Returns:** `ItemManager`

#### `getProfileManager()`

**Returns:** `ProfileManager`

#### `getWalker()`

**Returns:** `Walker`

#### `getUtils()`

**Returns:** `Utils`

#### `random(long low, long high)`

**Returns:** `int`

#### `getOCR()`

**Returns:** `OCR`

#### `getStageController()`

**Returns:** `StageController`

#### `log(String message)`

#### `log(String tag, String message)`

#### `log(Class tag, String msg)`

#### `getLastPositionChangeMillis()`

**Returns:** `long`

#### `getCurrentWorld()`

**Returns:** `Integer`

Gets the current world ID.

**Returns:** The current world ID, or null if not set.

#### `setExpectedRegionId(Integer expectedRegionId)`

Sets the expected region ID for the script. The region set will be searched immediately after failing to search the region where we previously found our position. Once the expected region is found, the expected region ID will be set to null.

**Parameters:**
- `expectedRegionId` - The expected region ID. If null, the expected region ID will be cleared.

#### `submitHumanTask(BooleanSupplier condition, int timeout)`

**Returns:** `boolean`

#### `submitHumanTask(BooleanSupplier condition, int timeout, boolean ignoreGameState, boolean ignoreTasks)`

**Returns:** `boolean`

#### `submitTask(BooleanSupplier condition, int timeout)`

**Returns:** `boolean`

#### `submitTask(BooleanSupplier condition, int timeout, boolean ignoreGameState, boolean ignoreTasks)`

**Returns:** `boolean`

#### `sleep(long millis)`

#### `random(int num)`

**Returns:** `int`

#### `random(int low, int high)`

**Returns:** `int`

#### `pollFramesHuman(BooleanSupplier condition, int timeout)`

**Returns:** `boolean`

Same as pollFramesUntil(BooleanSupplier, int) but with an additional human-like delay added after the condition is satisfied. Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data. Interruptions If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed: ScriptOptions.canBreak() - if true, break timers may interrupt this poll. ScriptOptions.canHopWorlds() - if true, world-hop tasks may interrupt this poll. ScriptOptions.canAFK() - if true, AFK timers may interrupt this poll. Override these methods to return false if you want to prevent interruptions from the corresponding task type. Warning: Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted by permitted higher-priority behaviour

#### `pollFramesHuman(BooleanSupplier condition, int timeout, boolean ignoreTasks)`

**Returns:** `boolean`

Same as pollFramesHuman(BooleanSupplier, int) but allows control over whether higher-priority tasks (breaks, hops, AFK) are ignored. Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data. If ignoreTasks is true, interruptions are suppressed. If ignoreTasks is false, interruptions may occur as normal. Interruptions If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed: ScriptOptions.canBreak() - if true, break timers may interrupt this poll. ScriptOptions.canHopWorlds() - if true, world-hop tasks may interrupt this poll. ScriptOptions.canAFK() - if true, AFK timers may interrupt this poll. Override these methods to return false if you want to prevent interruptions from the corresponding task type. Warning: Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted and ignoreTasks is false

#### `pollFramesUntil(BooleanSupplier condition, int timeout)`

**Returns:** `boolean`

Polls the given condition once per frame until it evaluates to true or the timeout (in milliseconds) expires. Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data. Interruptions If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed: ScriptOptions.canBreak() - if true, break timers may interrupt this poll. ScriptOptions.canHopWorlds() - if true, world-hop tasks may interrupt this poll. ScriptOptions.canAFK() - if true, AFK timers may interrupt this poll. Override these methods to return false if you want to prevent interruptions from the corresponding task type. Warning: Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted by permitted higher-priority behaviour

#### `pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks)`

**Returns:** `boolean`

Same as pollFramesUntil(BooleanSupplier, int) but allows control over whether higher-priority tasks (breaks, hops, AFK) are ignored. Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data. If ignoreTasks is true, interruptions are suppressed. If ignoreTasks is false, interruptions may occur as normal. Interruptions If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed: ScriptOptions.canBreak() - if true, break timers may interrupt this poll. ScriptOptions.canHopWorlds() - if true, world-hop tasks may interrupt this poll. ScriptOptions.canAFK() - if true, AFK timers may interrupt this poll. Override these methods to return false if you want to prevent interruptions from the corresponding task type. Warning: Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted and ignoreTasks is false

#### `pollFramesUntil(BooleanSupplier condition, int timeout, boolean ignoreTasks, boolean humanisedDelayAfter)`

**Returns:** `boolean`

Polls the given condition once per frame until it evaluates to true or the timeout expires, with options for priority handling and human-like delay. Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data. If ignoreTasks is true, the poll will continue uninterrupted even if higher-priority events (breaks, hops, AFK) occur. If humanisedDelayAfter is true, a short, natural pause is added after the condition is satisfied to mimic human reaction time. Interruptions If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed: ScriptOptions.canBreak() - if true, break timers may interrupt this poll. ScriptOptions.canHopWorlds() - if true, world-hop tasks may interrupt this poll. ScriptOptions.canAFK() - if true, AFK timers may interrupt this poll. Override these methods to return false if you want to prevent interruptions from the corresponding task type. Warning: Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

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

Polls the given condition once per frame until it evaluates to true or the timeout expires, with options for priority handling and human-like delay. Each frame update refreshes the player's position, the scene state, and the widget manager, ensuring that the condition is evaluated against the most recent game data. If ignoreTasks is true, the poll will continue uninterrupted even if higher-priority events (breaks, hops, AFK) occur. If humanisedDelayAfter is true, a short, natural pause is added after the condition is satisfied to mimic human reaction time. Interruptions If a break, world-hop, or AFK action is scheduled and permitted by your script's overrides, a TaskInterruptedException will be thrown. The following methods control whether these interruptions are allowed: ScriptOptions.canBreak() - if true, break timers may interrupt this poll. ScriptOptions.canHopWorlds() - if true, world-hop tasks may interrupt this poll. ScriptOptions.canAFK() - if true, AFK timers may interrupt this poll (defaults to #canBreak()). Override these methods to return false if you want to prevent interruptions from the corresponding task type. Warning: Do not catch or suppress TaskInterruptedException in your script. It is a control signal for the framework's outer executor, which will handle interruptions safely.

**Parameters:**
- `condition` - a check that is evaluated once per frame
- `timeout` - maximum time to wait, in milliseconds
- `ignoreTasks` - whether to suppress higher-priority task interrupts (breaks/hops/AFK)
- `humanisedDelayAfter` - whether to add a natural pause after completion
- `ignoreMenu` - whether to ignore an open minimenu when polling

**Returns:** true if the condition passed before the timeout, otherwise false

**Throws:**
- TaskInterruptedException - if interrupted and ignoreTasks is false

#### `sleep(int millis)`

Sleeps for the specified number of milliseconds, handling interruptions appropriately. This method pauses the current thread for the given duration. If the sleep is interrupted, it catches the InterruptedException and re-interrupts the thread to preserve the interrupt status.

**Parameters:**
- `millis` - The number of milliseconds to sleep.

#### `stop()`

Requests the script to stop. The script will stop as soon as possible. This method is non-blocking and returns immediately.

#### `stopped()`

**Returns:** `boolean`

Checks if the script has been requested to stop.

**Returns:** true if a stop has been requested, false otherwise

#### `setPause(boolean pause)`

Pauses or unpauses the script.

**Parameters:**
- `pause` - true to pause, false to unpause

#### `paused()`

**Returns:** `boolean`

#### `onGameStateChanged(GameState newGameState)`

Called whenever the game state changes.

**Parameters:**
- `newGameState` - The new game state.

#### `onPaint(Canvas c)`

#### `onNewFrame()`

Called when a new frame is captured, right after updating the position, scene and widgets. This method is called on the main script thread, so be careful not to block it WARNING: You must only call read-only methods inside the scope of this method. This method is intended for passive monitoring and analysis only.

#### `onRegionChange(int newRegionId, int previousRegionId)`

Called whenever the region ID changes.

**Parameters:**
- `newRegionId` - The new region ID.
- `previousRegionId` - The previous region ID.

#### `addCustomMap(MapDefinition mapDefinition)`

Adds a custom map to the location service. This map will be treated as a prioritised region.

**Parameters:**
- `mapDefinition` - The map definition to add.


---

## TilePickerPanel

**Package:** `com.osmb.api.javafx`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `show(ScriptCore core)`

**Returns:** `WorldPosition`

Displays a tile picker panel that allows the user to select a tile on the screen. The selected tile is highlighted in green, and the user can confirm their selection. The method returns the world position of the selected tile. WARNING: This method should only be called when we have established our position in the world.

**Parameters:**
- `core` - the ScriptCore instance to interact with the scene environment

**Returns:** the world position of the selected tile


---

## World

**Package:** `com.osmb.api.world`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public static final String[] WORLDS_TO_IGNORE`

### Methods

#### `init()`

**Throws:**
- Exception

#### `load()`

**Throws:**
- Exception

#### `getWorlds()`

**Returns:** `List<World>`

#### `getTotalWorlds()`

**Returns:** `int`

#### `getId()`

**Returns:** `int`

#### `getFlag()`

**Returns:** `int`

#### `getHost()`

**Returns:** `String`

#### `getActivity()`

**Returns:** `String`

#### `getServerLocation()`

**Returns:** `Location`

#### `getPlayerCount()`

**Returns:** `int`

#### `getSkillTotal()`

**Returns:** `SkillTotal`

#### `isMembers()`

**Returns:** `boolean`

#### `getCategory()`

**Returns:** `WorldType`

#### `isIgnore()`

**Returns:** `boolean`

#### `toString()`

**Returns:** `String`


---

## WorldNoneExistentException

**Package:** `com.osmb.api.world`

**Type:** Class

**Extends/Implements:** extends Exception


---

