# OSMB API - Input (Mouse & Keyboard)

Mouse (Finger) and keyboard input handling

---

## Classes in this Module

- [Class EntityType](#class-entitytype) [class]
- [Class PhysicalKey](#class-physicalkey) [class]
- [Class TouchType](#class-touchtype) [class]
- [Finger](#finger) [class]
- [Keyboard](#keyboard) [class]
- [MenuEntry](#menuentry) [class]
- [MenuHook](#menuhook) [class]

---

## Class EntityType

**Package:** `com.osmb.api.input`

**Type:** Class

**Extends/Implements:** extends Enum<EntityType>

### Methods

#### `values()`

**Returns:** `EntityType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `EntityType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Class PhysicalKey

**Package:** `com.osmb.api.input`

**Type:** Class

**Extends/Implements:** extends Enum<PhysicalKey>

### Methods

#### `values()`

**Returns:** `PhysicalKey[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `PhysicalKey`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getLinuxKeyCode()`

**Returns:** `Integer`

Returns the Linux evdev key code (KEY_...) integer value, required for binary input injection.

#### `getAndroidKeyCode()`

**Returns:** `String`

#### `getWindowsKeyCode()`

**Returns:** `Integer`


---

## Class TouchType

**Package:** `com.osmb.api.input`

**Type:** Class

**Extends/Implements:** extends Enum<TouchType>

### Methods

#### `values()`

**Returns:** `TouchType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `TouchType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getMonkeyIdentifier()`

**Returns:** `String`


---

## Finger

**Package:** `com.osmb.api.input`

**Type:** Class

### Methods

#### `getLastTouchType()`

**Returns:** `TouchType`

#### `getLastTapMillis()`

**Returns:** `long`

#### `getLastTapX()`

**Returns:** `int`

Gets the x-coordinate of the last tap performed

**Returns:** x coordinate of last tap

#### `getLastTapY()`

**Returns:** `int`

Gets the y-coordinate of the last tap performed

**Returns:** y coordinate of last tap

#### `tap(int x, int y)`

**Returns:** `boolean`

Performs a tap at the specified coordinates with default human-like delay

**Parameters:**
- `x` - The x coordinate to tap
- `y` - The y coordinate to tap

**Returns:** true if the tap was successful, false otherwise

#### `tap(Point point)`

**Returns:** `boolean`

Performs a tap at the specified point with default human-like delay

**Parameters:**
- `point` - The point to tap

**Returns:** true if the tap was successful, false otherwise

#### `tap(boolean humanDelay, ItemSearchResult itemSearchResult, MenuHook menuHook)`

**Returns:** `boolean`

#### `tap(Shape shape)`

**Returns:** `boolean`

Performs a tap at a random point within the specified shape with default human-like delay

**Parameters:**
- `shape` - The shape to tap within

**Returns:** true if the tap was successful, false otherwise

#### `tap(boolean humanDelay, int x, int y)`

**Returns:** `boolean`

Performs a tap with configurable delay settings

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `x` - The x coordinate to tap
- `y` - The y coordinate to tap

**Returns:** true if the tap was successful, false otherwise

#### `tap(int x, int y, String... entryText)`

**Returns:** `boolean`

Performs a tap and attempts to select a menu entry matching the provided text

**Parameters:**
- `x` - The x coordinate to tap
- `y` - The y coordinate to tap
- `entryText` - One or more menu entry texts to match (checks if entry starts with text)

**Returns:** true if the menu entry was found and selected, false otherwise

#### `tap(Point point, String... entryText)`

**Returns:** `boolean`

Performs a tap and attempts to select a menu entry matching the provided text

**Parameters:**
- `point` - The point coordinate to tap
- `entryText` - One or more menu entry texts to match (checks if entry starts with text)

**Returns:** true if the menu entry was found and selected, false otherwise

#### `tap(Shape shape, String... entryText)`

**Returns:** `boolean`

Performs a tap and attempts to select a menu entry matching the provided text

**Parameters:**
- `shape` - The area to tap, a random point inside the shape will be generated
- `entryText` - One or more menu entry texts to match (checks if entry starts with text)

**Returns:** true if the menu entry was found and selected, false otherwise

#### `tap(Shape shape, MenuHook menuHook)`

**Returns:** `boolean`

Performs a tap with a custom menu selection handler

**Parameters:**
- `shape` - The area to tap, a random point inside the shape will be generated
- `menuHook` - Custom handler for selecting menu entries

**Returns:** true if the menu interaction was successful, false otherwise

#### `tapGetResponse(boolean humanDelay, Shape shape)`

**Returns:** `MenuEntry`

#### `tap(boolean humanDelay, int x, int y, MenuHook menuHook)`

**Returns:** `boolean`

Performs a tap with a custom menu selection hook and configurable delay

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `x` - The x coordinate to tap
- `y` - The y coordinate to tap
- `menuHook` - Custom logic for selecting a menu entry

**Returns:** true if the menu interaction was successful, false otherwise

#### `tap(boolean humanDelay, Point point, MenuHook menuHook)`

**Returns:** `boolean`

Performs a tap with a custom menu selection hook and configurable delay

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `point` - The point to tap
- `menuHook` - Custom logic for selecting a menu entry

**Returns:** true if the menu interaction was successful, false otherwise

#### `tap(boolean humanDelay, Shape shape)`

**Returns:** `boolean`

Performs a tap with a custom menu selection hook and configurable delay

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `shape` - The shape to tap within

**Returns:** true if the menu interaction was successful, false otherwise

#### `tap(boolean humanDelay, Shape shape, MenuHook menuHook)`

**Returns:** `boolean`

Performs a tap on a specific shape with a custom menu selection hook

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `shape` - The shape to tap within
- `menuHook` - Custom logic for selecting a menu entry

**Returns:** true if the menu interaction was successful, false otherwise

#### `tap(boolean humanDelay, ItemSearchResult itemSearchResult)`

**Returns:** `boolean`

Performs a tap on an item search result with configurable delay

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `itemSearchResult` - The item to tap

**Returns:** true if the tap was successful, false if the item is not visible or tap fails

#### `tap(boolean humanDelay, ItemSearchResult itemSearchResult, String... actions)`

**Returns:** `boolean`

Performs a tap on an item and attempts to select a specific action from its menu

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `itemSearchResult` - The item to tap
- `actions` - One or more menu actions to attempt (exact match required)

**Returns:** true if the action was found and selected, false otherwise

#### `touch(int x, int y, TouchType touchType)`

**Returns:** `boolean`

#### `tapGameScreen(String entityNameOverride, Shape shape, String... entryText)`

**Returns:** `boolean`

Performs a tap on a specific game entity with optional actions

**Parameters:**
- `entityNameOverride` - Overrides the entity name. Used in situations where the name from the object definition is null.
- `shape` - The shape containing the entity
- `entryText` - Optional menu entries to select

**Returns:** true if the interaction was successful, false if entity not found or interaction fails

#### `tapGameScreen(Shape shape, MenuHook menuHook)`

**Returns:** `boolean`

#### `tapGameScreen(Shape shape)`

**Returns:** `boolean`

#### `tap(int x, int y, String entityNameOverride, String... actions)`

**Returns:** `boolean`

Performs a tap on specific coordinates targeting a named entity with optional actions

**Parameters:**
- `x` - The x coordinate to tap
- `y` - The y coordinate to tap
- `entityNameOverride` - Overrides the entity name. Used in situations where the name from the object definition is null.
- `actions` - Optional menu actions to select (exact match required)

**Returns:** true if the interaction was successful, false otherwise

#### `tapGameScreen(boolean humanDelay, Shape shape)`

**Returns:** `boolean`

#### `tapGameScreen(Shape shape, String... entryText)`

**Returns:** `boolean`

Performs a tap on the game screen (outside UI boundaries) within a shape

**Parameters:**
- `shape` - The shape to tap within
- `entryText` - Optional menu entries to select

**Returns:** true if the tap was successful, false if the coordinates aren't on the game screen (covered by a UI component) or if the tap fails in general


---

## Keyboard

**Package:** `com.osmb.api.input`

**Type:** Class

### Methods

#### `type(String message)`

#### `pressKey(TouchType touchType, PhysicalKey key)`


---

## MenuEntry

**Package:** `com.osmb.api.input`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getRawText()`

**Returns:** `String`

Returns the raw text of the menu entry.

**Returns:** the raw text of the menu entry

#### `getEntryBounds()`

**Returns:** `Rectangle`

#### `getEntityType()`

**Returns:** `EntityType`

#### `getAction()`

**Returns:** `String`

Returns the action associated with this menu entry. The action is determined by the white text in the menu entry.

**Returns:** the action as a String

#### `getEntityName()`

**Returns:** `String`

#### `toString()`

**Returns:** `String`


---

## MenuHook

**Package:** `com.osmb.api.input`

**Type:** Class

Functional interface for custom menu entry selection logic. Used to programmatically select menu entries after performing a tap. Implementations should examine the provided menu entries and return: The desired MenuEntry to interact with null if no matching entry is found When used with Finger methods: A tap is performed at the specified coordinates The menu appears (if applicable) The handle() method is called with available menu entries If non-null, the returned entry is automatically interacted with If null, the menu is closed and the interaction fails

### Methods

#### `handle(List<MenuEntry> menuEntries)`

**Returns:** `MenuEntry`

Processes available menu entries and returns the desired one to interact with.

**Parameters:**
- `menuEntries` - List of available menu entries (never null, may be empty)

**Returns:** The MenuEntry to select, or null if no matching entry is found


---

