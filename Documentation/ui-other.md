# OSMB API - Other UI Components

Other UI components

---

## Classes in this Module

- [Class ComponentButtonStatus](#class-componentbuttonstatus) [class]
- [Class ComponentContainerStatus](#class-componentcontainerstatus) [class]
- [Class ComponentParent.ScreenArea](#class-componentparent.screenarea) [class]
- [Class GameState](#class-gamestate) [class]
- [Component<T>](#componentt) [class]
- [ComponentCentered](#componentcentered) [class]
- [ComponentChild<T>](#componentchildt) [class]
- [ComponentGlobal<T>](#componentglobalt) [class]
- [ComponentImage<T>](#componentimaget) [class]
- [ComponentParent<T>](#componentparentt) [class]
- [ComponentSearchResult<T>](#componentsearchresultt) [class]
- [DepositBox](#depositbox) [class]
- [Expandable](#expandable) [class]
- [HotKeyManager](#hotkeymanager) [class]
- [HotkeyContainer](#hotkeycontainer) [class]
- [Hotkeys](#hotkeys) [class]
- [MiniMenu](#minimenu) [class]
- [PopoutPanelContainer](#popoutpanelcontainer) [class]
- [TapToDrop](#taptodrop) [class]
- [UIBoundary](#uiboundary) [class]
- [Viewable](#viewable) [class]

---

## Class ComponentButtonStatus

**Package:** `com.osmb.api.ui.component`

**Type:** Class

**Extends/Implements:** extends Enum<ComponentButtonStatus>

### Methods

#### `values()`

**Returns:** `ComponentButtonStatus[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `ComponentButtonStatus`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Class ComponentContainerStatus

**Package:** `com.osmb.api.ui.component`

**Type:** Class

**Extends/Implements:** extends Enum<ComponentContainerStatus>

### Methods

#### `values()`

**Returns:** `ComponentContainerStatus[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `ComponentContainerStatus`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Class ComponentParent.ScreenArea

**Package:** `com.osmb.api.ui.component`

**Type:** Class

**Extends/Implements:** extends Enum<ComponentParent.ScreenArea>

### Methods

#### `values()`

**Returns:** `ComponentParent.ScreenArea[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `ComponentParent.ScreenArea`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `isValid(int width, int height, int x, int y)`

**Returns:** `boolean`


---

## Class GameState

**Package:** `com.osmb.api.ui`

**Type:** Class

**Extends/Implements:** extends Enum<GameState>

### Methods

#### `values()`

**Returns:** `GameState[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `GameState`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Component<T>

**Package:** `com.osmb.api.ui.component`

**Type:** Class

**Extends/Implements:** extends UIBoundary

**Interfaces:** UIBoundary

### Fields

- `static final Class<InventoryTabComponent> INVENTORY`
- `static final Class<EquipmentTabComponent> EQUIPMENT`
- `static final Class<Container> CONTAINER`

### Methods

#### `getResult()`

**Returns:** `ComponentSearchResult<T>`

#### `getComponentGameState()`

**Returns:** `GameState`

#### `isVisible()`

**Returns:** `boolean`

#### `updateSearchResult(ComponentSearchResult<T> result)`

#### `getScriptCore()`

**Returns:** `ScriptCore`

#### `getBounds()`

**Returns:** `Rectangle`


---

## ComponentCentered

**Package:** `com.osmb.api.ui.component`

**Type:** Class

**Extends/Implements:** extends Object implements Component

### Fields

- `protected final ScriptCore core`
- `protected final ComponentImage backgroundImage`
- `protected ComponentSearchResult result`

### Methods

#### `getBackgroundImage()`

**Returns:** `ComponentImage`

#### `buildBackgroundImage()`

**Returns:** `ComponentImage`

#### `getResult()`

**Returns:** `ComponentSearchResult`

#### `getComponentGameState()`

**Returns:** `GameState`

#### `updateSearchResult(ComponentSearchResult result)`

#### `getScriptCore()`

**Returns:** `ScriptCore`

#### `find()`

**Returns:** `ComponentSearchResult`

#### `getBounds()`

**Returns:** `Rectangle`


---

## ComponentChild<T>

**Package:** `com.osmb.api.ui.component`

**Type:** Class

**Extends/Implements:** extends Object implements ComponentGlobal<T>

### Fields

- `protected final ScriptCore core`
- `protected final List<ComponentImage<T>> componentImages`
- `protected final Map<Integer,SearchableImage> componentIcons`
- `protected ComponentParent parent`
- `protected ComponentSearchResult<T> result`

### Methods

#### `getParentOffsets()`

**Returns:** `Map<T,Point>`

#### `insideParent()`

**Returns:** `boolean`

#### `getVisibilityCondition(ComponentSearchResult<T> parentResult)`

**Returns:** `UIResult<BooleanSupplier>`

#### `getParent()`

**Returns:** `ComponentParent`

#### `setParent(ComponentParent parent)`

#### `updateSearchResult(ComponentSearchResult result)`

#### `getScriptCore()`

**Returns:** `ScriptCore`

#### `getComponentImages()`

**Returns:** `List<ComponentImage<T>>`

#### `getResult()`

**Returns:** `ComponentSearchResult`

#### `getComponentGameState()`

**Returns:** `GameState`


---

## ComponentGlobal<T>

**Package:** `com.osmb.api.ui.component`

**Type:** Class

**Extends/Implements:** extends Component<T>

**Interfaces:** Component, UIBoundary

### Methods

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<T>>`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `getComponentImages()`

**Returns:** `List<ComponentImage<T>>`

#### `findIcon(Rectangle containerBounds)`

**Returns:** `UIResult<Integer>`


---

## ComponentImage<T>

**Package:** `com.osmb.api.ui.component`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getSearchableImage()`

**Returns:** `SearchableImage`

#### `getBackgroundID()`

**Returns:** `int`

#### `getGameFrameStatusType()`

**Returns:** `T`


---

## ComponentParent<T>

**Package:** `com.osmb.api.ui.component`

**Type:** Class

**Extends/Implements:** extends Object implements ComponentGlobal<T>

### Fields

- `protected final ScriptCore core`
- `protected final Map<Class<? extends Component>,ComponentChild> childrenComponents`
- `protected final List<ComponentImage<T>> componentImages`
- `protected final Map<Integer,SearchableImage> componentIcons`
- `protected ComponentSearchResult<T> result`

### Methods

#### `getChildrenComponents()`

**Returns:** `Map<Class<? extends Component>,ComponentChild>`

#### `addChild(Class<? extends Component> type, ComponentChild child)`

#### `findActiveChildren()`

**Returns:** `Map<Class<? extends Component>,ComponentChild>`

#### `getScreenArea()`

**Returns:** `ComponentParent.ScreenArea`

#### `getResult()`

**Returns:** `ComponentSearchResult<T>`

#### `onFound(ImageSearchResult result, int foundIconID, ComponentImage foundImage)`

**Returns:** `ComponentSearchResult`

#### `getScriptCore()`

**Returns:** `ScriptCore`

#### `getComponentImages()`

**Returns:** `List<ComponentImage<T>>`

#### `updateSearchResult(ComponentSearchResult<T> result)`


---

## ComponentSearchResult<T>

**Package:** `com.osmb.api.ui.component`

**Type:** Class

**Extends/Implements:** extends ImageSearchResult

### Methods

#### `getIconID()`

**Returns:** `int`

#### `getComponentImage()`

**Returns:** `ComponentImage<T>`

#### `getBounds()`

**Returns:** `Rectangle`


---

## DepositBox

**Package:** `com.osmb.api.ui.depositbox`

**Type:** Class

**Extends/Implements:** extends Viewable, ItemGroup

**Interfaces:** ItemGroup, Viewable

### Methods

#### `drawComponents(Canvas canvas)`

#### `deposit(int itemID, int amount)`

**Returns:** `boolean`

#### `depositAllIgnoreSlots(Set<Integer> slotsToIgnore)`

**Returns:** `boolean`

#### `depositAll(Set<Integer> itemIDsToIgnore)`

**Returns:** `boolean`

#### `depositAll(Set<Integer> itemsIDsToIgnore, Set<Integer> slotsToIgnore)`

**Returns:** `boolean`

#### `close()`

**Returns:** `boolean`


---

## Expandable

**Package:** `com.osmb.api.ui`

**Type:** Class

**Extends/Implements:** extends Viewable

**Interfaces:** Viewable

### Methods

#### `isOpen()`

**Returns:** `boolean`

#### `open()`

**Returns:** `boolean`

#### `close()`

**Returns:** `boolean`


---

## HotKeyManager

**Package:** `com.osmb.api.ui.component.hotkeys`

**Type:** Class

**Extends/Implements:** extends Object implements Hotkeys

### Methods

#### `isTapToDropEnabled()`

**Returns:** `UIResult<Boolean>`

#### `setTapToDropEnabled(boolean enabled)`

**Returns:** `boolean`

Description copied from interface: Hotkeys

**Parameters:**
- `enabled` - true to enable 'Tap to drop', false to disable it.

**Returns:** false if the button is not visible, if the method timed out, or if the action could not be completed. true if the function was successfully enabled or disabled.


---

## HotkeyContainer

**Package:** `com.osmb.api.ui.component.hotkeys`

**Type:** Class

**Extends/Implements:** extends ComponentParent<ComponentContainerStatus>

### Methods

#### `getScreenArea()`

**Returns:** `ComponentParent.ScreenArea`

#### `onFound(ImageSearchResult result, int iconID, ComponentImage foundImage)`

**Returns:** `ComponentSearchResult`

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<ComponentContainerStatus>>`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `getComponentGameState()`

**Returns:** `GameState`


---

## Hotkeys

**Package:** `com.osmb.api.ui.hotkeys`

**Type:** Class

### Methods

#### `isTapToDropEnabled()`

**Returns:** `UIResult<Boolean>`

#### `setTapToDropEnabled(boolean enabled)`

**Returns:** `boolean`

Enables or disables the 'Tap to drop' function in the hotkey container.

**Parameters:**
- `enabled` - true to enable 'Tap to drop', false to disable it.

**Returns:** false if the button is not visible, if the method timed out, or if the action could not be completed. true if the function was successfully enabled or disabled.


---

## MiniMenu

**Package:** `com.osmb.api.ui`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public static final int BLACK_COLOR`
- `public static final int SCROLL_BAR_UNDERLAY_COLOR`
- `public static final int SCROLL_BAR_THUMB_COLOR`

### Methods

#### `tapOutside()`

**Returns:** `boolean`

#### `getPointOutsideMenu()`

**Returns:** `Point`

#### `getMenuEntries()`

**Returns:** `MenuEntry[]`

#### `getMenuBounds()`

**Returns:** `CachedObject<Rectangle>`

#### `findMenu(int x, int y)`

**Returns:** `boolean`

#### `isVisible()`

**Returns:** `boolean`


---

## PopoutPanelContainer

**Package:** `com.osmb.api.ui.component.popout`

**Type:** Class

**Extends/Implements:** extends ComponentParent<ComponentContainerStatus>

### Methods

#### `getScreenArea()`

**Returns:** `ComponentParent.ScreenArea`

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<ComponentContainerStatus>>`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `getComponentGameState()`

**Returns:** `GameState`


---

## TapToDrop

**Package:** `com.osmb.api.ui.component.hotkeys.functions`

**Type:** Class

**Extends/Implements:** extends HotKeyTabComponent

### Methods

#### `getIconXOffset()`

**Returns:** `int`

#### `getIconYOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`


---

## UIBoundary

**Package:** `com.osmb.api.ui.component`

**Type:** Class

### Methods

#### `getBounds()`

**Returns:** `Rectangle`


---

## Viewable

**Package:** `com.osmb.api.ui`

**Type:** Class

### Methods

#### `isVisible()`

**Returns:** `boolean`

#### `getBounds()`

**Returns:** `Rectangle`


---

