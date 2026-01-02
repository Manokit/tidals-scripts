# OSMB API - Tab Components

Tab management (Skills, Equipment, Prayer, etc.)

---

## Classes in this Module

- [AccountTabComponent](#accounttabcomponent) [class]
- [ClanTabComponent](#clantabcomponent) [class]
- [Class EquipmentTabComponent.Slot](#class-equipmenttabcomponent.slot) [class]
- [Class Prayer.Type](#class-prayer.type) [class]
- [Class SettingsTabComponent.SettingsSubTabType](#class-settingstabcomponent.settingssubtabtype) [class]
- [Class SkillType](#class-skilltype) [class]
- [Class Spellbook.ResultType](#class-spellbook.resulttype) [class]
- [Class Tab.Type](#class-tab.type) [class]
- [CombatTabComponent](#combattabcomponent) [class]
- [Container](#container) [class]
- [EmoteTabComponent](#emotetabcomponent) [class]
- [Equipment](#equipment) [class]
- [EquipmentTabComponent](#equipmenttabcomponent) [class]
- [ExpandCollapseTabComponent](#expandcollapsetabcomponent) [class]
- [FriendsTabComponent](#friendstabcomponent) [class]
- [HotKeyTabComponent](#hotkeytabcomponent) [class]
- [Logout](#logout) [class]
- [MusicTabComponent](#musictabcomponent) [class]
- [Prayer](#prayer) [class]
- [PrayerTabComponent](#prayertabcomponent) [class]
- [QuestTabComponent](#questtabcomponent) [class]
- [SailingTabComponent](#sailingtabcomponent) [class]
- [Settings](#settings) [class]
- [SettingsTabComponent](#settingstabcomponent) [class]
- [Skill](#skill) [class]
- [SkillsTabComponent](#skillstabcomponent) [class]
- [SkillsTabComponent.SkillLevel](#skillstabcomponent.skilllevel) [class]
- [Spellbook](#spellbook) [class]
- [SpellbookTabComponent](#spellbooktabcomponent) [class]
- [SquareTabComponent](#squaretabcomponent) [class]
- [Tab](#tab) [class]
- [TabContainer](#tabcontainer) [class]
- [TabManager](#tabmanager) [class]
- [TabManagerService](#tabmanagerservice) [class]

---

## AccountTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconYOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `getType()`

**Returns:** `Tab.Type`


---

## ClanTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `getType()`

**Returns:** `Tab.Type`


---

## Class EquipmentTabComponent.Slot

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends Enum<EquipmentTabComponent.Slot>

### Methods

#### `values()`

**Returns:** `EquipmentTabComponent.Slot[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `EquipmentTabComponent.Slot`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getItemPositionY()`

**Returns:** `int`

#### `getItemPositionX()`

**Returns:** `int`


---

## Class Prayer.Type

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

**Extends/Implements:** extends Enum<Prayer.Type>

### Methods

#### `values()`

**Returns:** `Prayer.Type[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `Prayer.Type`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getSpriteID()`

**Returns:** `int`


---

## Class SettingsTabComponent.SettingsSubTabType

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends Enum<SettingsTabComponent.SettingsSubTabType>

### Methods

#### `values()`

**Returns:** `SettingsTabComponent.SettingsSubTabType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `SettingsTabComponent.SettingsSubTabType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getIconID()`

**Returns:** `int`


---

## Class SkillType

**Package:** `com.osmb.api.ui.component.tabs.skill`

**Type:** Class

**Extends/Implements:** extends Enum<SkillType>

### Fields

- `public static final Map<SkillType,Image> SKILL_SPRITES`

### Methods

#### `values()`

**Returns:** `SkillType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `SkillType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getSpriteId()`

**Returns:** `int`

#### `getName()`

**Returns:** `String`


---

## Class Spellbook.ResultType

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

Defines the types of result verification available for spell selection.

**Extends/Implements:** extends Enum<Spellbook.ResultType>

### Methods

#### `values()`

**Returns:** `Spellbook.ResultType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `Spellbook.ResultType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Class Tab.Type

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

**Extends/Implements:** extends Enum<Tab.Type>

### Methods

#### `values()`

**Returns:** `Tab.Type[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `Tab.Type`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## CombatTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIcons()`

**Returns:** `int[]`

#### `getType()`

**Returns:** `Tab.Type`


---

## Container

**Package:** `com.osmb.api.ui.component.tabs.container`

**Type:** Class

**Extends/Implements:** extends ComponentChild<Integer>

### Fields

- `public static final int STONE_BORDER`
- `public static final int STEEL_BORDER`
- `public static final int STONE_BORDER_COVERED`
- `public static final int STEEL_BORDER_COVERED`

### Methods

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<Integer>>`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `getComponentGameState()`

**Returns:** `GameState`

#### `getParentOffsets()`

**Returns:** `Map<Integer,Point>`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getInnerBounds()`

**Returns:** `Rectangle`


---

## EmoteTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `getType()`

**Returns:** `Tab.Type`


---

## Equipment

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

### Methods

#### `findItem(int itemID)`

**Returns:** `UIResult<ItemSearchResult>`

#### `findItem(int... itemIDs)`

**Returns:** `UIResult<ItemSearchResult>`

#### `isEquipped(int... itemIDs)`

**Returns:** `UIResult<Boolean>`

#### `interact(int itemID, String menuOption)`

**Returns:** `boolean`


---

## EquipmentTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent implements Equipment

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `getType()`

**Returns:** `Tab.Type`

#### `findItem(int itemID)`

**Returns:** `UIResult<ItemSearchResult>`

#### `findItem(int[] itemIDs)`

**Returns:** `UIResult<ItemSearchResult>`

#### `isEquipped(int... itemIDs)`

**Returns:** `UIResult<Boolean>`

#### `interact(int itemID, String menuOption)`

**Returns:** `boolean`


---

## ExpandCollapseTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `getType()`

**Returns:** `Tab.Type`


---

## FriendsTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `getType()`

**Returns:** `Tab.Type`


---

## HotKeyTabComponent

**Package:** `com.osmb.api.ui.component.hotkeys.functions`

**Type:** Class

**Extends/Implements:** extends ComponentChild<ComponentButtonStatus>

### Fields

- `protected static final ToleranceComparator TOLERANCE_COMPARATOR`

### Methods

#### `getBounds()`

**Returns:** `Rectangle`

#### `getComponentGameState()`

**Returns:** `GameState`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `findIcon(Rectangle containerBounds)`

**Returns:** `UIResult<Integer>`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIconYOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<ComponentButtonStatus>>`

#### `isOpen()`

**Returns:** `boolean`

#### `open()`

**Returns:** `boolean`

#### `isVisible()`

**Returns:** `boolean`


---

## Logout

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

**Extends/Implements:** extends Expandable

**Interfaces:** Expandable, Viewable

### Methods

#### `logout()`

**Returns:** `boolean`


---

## MusicTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `getType()`

**Returns:** `Tab.Type`


---

## Prayer

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

### Methods

#### `getActivePrayers()`

**Returns:** `Prayer.Type[]`

#### `setActive(Prayer.Type prayer, boolean active)`

**Returns:** `boolean`

#### `getPrayerPoints()`

**Returns:** `Integer`


---

## PrayerTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent implements Prayer

### Fields

- `public final Map<Prayer.Type,SearchableImage> activePrayerImages`
- `public final Map<Prayer.Type,SearchableImage> prayerImages`

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIcons()`

**Returns:** `int[]`

#### `getActivePrayers()`

**Returns:** `Prayer.Type[]`

#### `setActive(Prayer.Type prayer, boolean active)`

**Returns:** `boolean`

#### `getPrayerPoints()`

**Returns:** `Integer`

#### `getType()`

**Returns:** `Tab.Type`


---

## QuestTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `getType()`

**Returns:** `Tab.Type`


---

## SailingTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `getType()`

**Returns:** `Tab.Type`


---

## Settings

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

Interface for interacting with the settings tab. Provides methods to open sub-tabs, set and get brightness and zoom levels, and manage frame rate settings.

**Extends/Implements:** extends Expandable

**Interfaces:** Expandable, Viewable

### Methods

#### `setBrightnessLevel()`

**Returns:** `boolean`

Sets the brightness level, this method will handle opening the settings tab & the sub-tab if necessary.

**Returns:** true if the settings tab was successfully opened, false otherwise.

#### `getBrightnessLevel()`

**Returns:** `UIResult<Integer>`

Gets the current brightness level - WARNING: This method is read-only, you will have to handle visibility of the settings tab & sub-tab yourself.

**Returns:** UIResult containing the brightness level.

#### `getZoomLevel()`

**Returns:** `UIResult<Integer>`

Gets the current zoom level - WARNING: This method is read-only, you will have to handle visibility of the settings tab & sub-tab yourself.

**Returns:** UIResult containing the zoom level.

#### `openSubTab(SettingsTabComponent.SettingsSubTabType subTab)`

**Returns:** `boolean`

Opens the settings tab and the specified sub-tab.

**Parameters:**
- `subTab` - The sub-tab to open.

**Returns:** true if the sub-tab was successfully opened, false otherwise.

#### `setZoomLevel(int level)`

**Returns:** `boolean`

Sets the zoom level, this method will handle opening the settings tab & the sub-tab if necessary.

**Parameters:**
- `level` - The zoom level to set, typically between 0 and 96.

**Returns:** true if the zoom level was successfully set, false otherwise.

#### `getFrameRate()`

**Returns:** `int`

Placeholder - This method is not implemented yet.

#### `setFrameRate(int rate)`

Placeholder - This method is not implemented yet.

**Parameters:**
- `rate` - 


---

## SettingsTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent implements Settings

### Fields

- `public static final int THUMB_HEIGHT`
- `public static final int DEFAULT_BRIGHTNESS_LEVEL`

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIcons()`

**Returns:** `int[]`

#### `getFrameRate()`

**Returns:** `int`

Description copied from interface: Settings

#### `setFrameRate(int rate)`

Description copied from interface: Settings

#### `getActiveSubTab()`

**Returns:** `SettingsTabComponent.SettingsSubTabType`

#### `openSubTab(SettingsTabComponent.SettingsSubTabType subTab)`

**Returns:** `boolean`

Description copied from interface: Settings

**Parameters:**
- `subTab` - The sub-tab to open.

**Returns:** true if the sub-tab was successfully opened, false otherwise.

#### `setZoomLevel(int zoom)`

**Returns:** `boolean`

Description copied from interface: Settings

**Parameters:**
- `zoom` - The zoom level to set, typically between 0 and 96.

**Returns:** true if the zoom level was successfully set, false otherwise.

#### `setBrightnessLevel()`

**Returns:** `boolean`

Description copied from interface: Settings

**Returns:** true if the settings tab was successfully opened, false otherwise.

#### `getBrightnessLevel()`

**Returns:** `UIResult<Integer>`

Description copied from interface: Settings

**Returns:** UIResult containing the brightness level.

#### `getZoomLevel()`

**Returns:** `UIResult<Integer>`

Description copied from interface: Settings

**Returns:** UIResult containing the zoom level.

#### `getType()`

**Returns:** `Tab.Type`


---

## Skill

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

### Methods

#### `getSkillLevel(SkillType skillType)`

**Returns:** `SkillsTabComponent.SkillLevel`

Retrieves the skill level for the specified skill. If the skills tab is not open it will try to open it - failing to do so will return null.

**Parameters:**
- `skillType` - The skill to get the level for.

**Returns:** SkillLevel object containing the level and boosted level, or null if not found.


---

## SkillsTabComponent

**Package:** `com.osmb.api.ui.component.tabs.skill`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent implements Skill

### Fields

- `public static final Font ARIAL`
- `public static final int YELLOW_PIXEL_COLOR`

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `isOpen()`

**Returns:** `boolean`

#### `debugInterface(Rectangle containerBounds, Canvas canvas)`

Debug method to visualize the skill bounds on the screen.

**Parameters:**
- `containerBounds` - 
- `canvas` - 

#### `getType()`

**Returns:** `Tab.Type`

#### `open()`

**Returns:** `boolean`

Opens the skills tab and ensures that the popup is closed if it is active by tapping on a random skill component. If the popup is not active, it simply opens the tab.

**Returns:** true if the skills tab was opened successfully & pop removed (if it was active), false otherwise.

#### `getSkillLevel(SkillType skillType)`

**Returns:** `SkillsTabComponent.SkillLevel`

Retrieves the skill level for the specified skill. If the skills tab is not open it will try to open it - failing to do so will return null.

**Parameters:**
- `skillType` - The skill to get the level for.

**Returns:** SkillLevel object containing the level and boosted level, or null if not found.


---

## SkillsTabComponent.SkillLevel

**Package:** `com.osmb.api.ui.component.tabs.skill`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getLevel()`

**Returns:** `int`

#### `getBoostedLevel()`

**Returns:** `int`


---

## Spellbook

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

**Extends/Implements:** extends Expandable

**Interfaces:** Expandable, Viewable

### Methods

#### `selectSpell(Spell spell, Spellbook.ResultType resultType)`

**Returns:** `boolean`

Attempts to select the specified spell in the spellbook. This method automatically handles: Opening the spellbook if it's not already open Verifying the correct spellbook type is active Selecting the spell if not already selected When resultType is null, the method will return true after attempting to click the spell, allowing the caller to handle verification themselves.

**Parameters:**
- `spell` - the spell to select (must belong to current spellbook type, cannot be null)
- `resultType` - the type of verification to perform (null for no verification)

**Returns:** true if spell was successfully selected (or if no verification requested), false if: Spellbook cannot be opened Wrong spellbook type is active Spell cannot be found/selected Verification fails when resultType is specified

**Throws:**
- SpellNotFoundException - if the spell is not found/sprite is disabled

#### `selectSpell(Spell spell, String menuOption, Spellbook.ResultType resultType)`

**Returns:** `boolean`

Selects a spell from the spellbook with a specific menu option and result type. This method automatically handles: Opening the spellbook if it's not already open Verifying the correct spellbook type is active Selecting the spell if not already selected When resultType is null, the method will return true after attempting to click the spell, allowing the caller to handle verification themselves.

**Parameters:**
- `spell` - the spell to select (must belong to current spellbook type, cannot be null)
- `menuOption` - the menu option to select the spell (e.g., "Cast", "Use")
- `resultType` - the type of verification to perform (null for no verification)

**Returns:** true if spell was successfully selected (or if no verification requested), false if: Spellbook cannot be opened Wrong spellbook type is active Spell cannot be found/selected Verification fails when resultType is specified

**Throws:**
- SpellNotFoundException
- InvalidSpellbookTypeException

#### `castSpell(Spell spell, Shape castTargetArea, String entityName)`

**Returns:** `boolean`

Selects and casts a spell on an entity. This method automatically handles: Opening the spellbook if needed Verifying the correct spellbook type Selecting the spell Casting on the target The action will be logged in format: "Cast [SpellName] -> [entityName]"

**Parameters:**
- `spell` - the spell to cast (must belong to current spellbook type, cannot be null)
- `castTargetArea` - the target area or entity shape to cast on (cannot be null)
- `entityName` - the name that appears in the action log/menu (e.g., "Goblin" produces log: "Cast Water Strike -> Goblin")

**Returns:** true if both spell selection and casting succeeded, false if: Spellbook cannot be opened Wrong spellbook type Spell selection fails Target tapping fails

**Throws:**
- NullPointerException - if any parameter is null
- SpellNotFoundException - if the spell is not found/sprite is disabled

#### `getSpellbookType()`

**Returns:** `SpellbookType`

Gets the type of the currently active spellbook.

**Returns:** the current SpellbookType, or null if undetermined


---

## SpellbookTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends SquareTabComponent implements Spellbook

### Fields

- `public static final SearchablePixel SELECTED_PIXEL`

### Methods

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `selectSpell(Spell spell, Spellbook.ResultType resultType)`

**Returns:** `boolean`

Description copied from interface: Spellbook

**Parameters:**
- `spell` - the spell to select (must belong to current spellbook type, cannot be null)
- `resultType` - the type of verification to perform (null for no verification)

**Returns:** true if spell was successfully selected (or if no verification requested), false if: Spellbook cannot be opened Wrong spellbook type is active Spell cannot be found/selected Verification fails when resultType is specified

**Throws:**
- SpellNotFoundException - if the spell is not found/sprite is disabled
- InvalidSpellbookTypeException

#### `selectSpell(Spell spell, String menuOption, Spellbook.ResultType resultType)`

**Returns:** `boolean`

Description copied from interface: Spellbook

**Parameters:**
- `spell` - the spell to select (must belong to current spellbook type, cannot be null)
- `menuOption` - the menu option to select the spell (e.g., "Cast", "Use")
- `resultType` - the type of verification to perform (null for no verification)

**Returns:** true if spell was successfully selected (or if no verification requested), false if: Spellbook cannot be opened Wrong spellbook type is active Spell cannot be found/selected Verification fails when resultType is specified

**Throws:**
- SpellNotFoundException
- InvalidSpellbookTypeException

#### `castSpell(Spell spell, Shape castTargetArea, String entityName)`

**Returns:** `boolean`

Description copied from interface: Spellbook

**Parameters:**
- `spell` - the spell to cast (must belong to current spellbook type, cannot be null)
- `castTargetArea` - the target area or entity shape to cast on (cannot be null)
- `entityName` - the name that appears in the action log/menu (e.g., "Goblin" produces log: "Cast Water Strike -> Goblin")

**Returns:** true if both spell selection and casting succeeded, false if: Spellbook cannot be opened Wrong spellbook type Spell selection fails Target tapping fails

**Throws:**
- SpellNotFoundException - if the spell is not found/sprite is disabled

#### `getSelectedSpell()`

**Returns:** `UIResult<Spell>`

Gets the currently selected spell in the spellbook.

**Returns:** UIResult containing the selected spell if found, UIResult.notVisible() if spellbook isn't open, UIResult.of(null) if no spell is selected

#### `getSpellbookType()`

**Returns:** `SpellbookType`

Description copied from interface: Spellbook

**Returns:** the current SpellbookType, or null if undetermined

#### `getComponentGameState()`

**Returns:** `GameState`

#### `getType()`

**Returns:** `Tab.Type`


---

## SquareTabComponent

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends ComponentChild<ComponentButtonStatus> implements Tab

### Fields

- `protected static final ToleranceComparator TOLERANCE_COMPARATOR`

### Methods

#### `main(String[] args)`

#### `hiddenWhenTabContainerCollapsed()`

**Returns:** `boolean`

#### `getComponent()`

**Returns:** `Component`

#### `transformCroppedBounds(Rectangle bounds)`

**Returns:** `Rectangle`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getComponentGameState()`

**Returns:** `GameState`

#### `getContainer()`

**Returns:** `Container`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `findIcon(Rectangle containerBounds)`

**Returns:** `UIResult<Integer>`

#### `getIconXOffset()`

**Returns:** `int`

#### `getIconYOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<ComponentButtonStatus>>`

#### `isOpen()`

**Returns:** `boolean`

#### `isTabActive()`

**Returns:** `boolean`

#### `open()`

**Returns:** `boolean`

#### `open(boolean humanDelay)`

**Returns:** `boolean`

#### `close()`

**Returns:** `boolean`

#### `close(boolean humanDelay)`

**Returns:** `boolean`

#### `isVisible()`

**Returns:** `boolean`


---

## Tab

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

### Methods

#### `getContainer()`

**Returns:** `Component`

#### `isOpen()`

**Returns:** `boolean`

#### `isVisible()`

**Returns:** `boolean`

#### `getType()`

**Returns:** `Tab.Type`

#### `getComponent()`

**Returns:** `Component`

#### `open()`

**Returns:** `boolean`

#### `open(boolean humanDelay)`

**Returns:** `boolean`

#### `close()`

**Returns:** `boolean`

#### `close(boolean humanDelay)`

**Returns:** `boolean`


---

## TabContainer

**Package:** `com.osmb.api.ui.component.tabs`

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

## TabManager

**Package:** `com.osmb.api.ui.tabs`

**Type:** Class

Manages operations related to tabs and their linked containers. Provides methods to query the active tab, close the tab container, and switch between tabs with optional behavior to mimic user interactions.

### Methods

#### `getActiveTabComponent()`

**Returns:** `Tab`

Retrieves the currently active tab component.

**Returns:** the active Tab, or null if no tab is active.

#### `getActiveTab()`

**Returns:** `Tab.Type`

Retrieves the currently active tab.

**Returns:** the active Tab.Type, or null if no tab is active.

#### `getTabComponent(Tab.Type type)`

**Returns:** `Tab`

Retrieves the Tab for the provided Tab.Type.

**Returns:** the Tab equivalent for the provided Tab.Type.

#### `closeContainer()`

**Returns:** `boolean`

Closes the container linked to the active tab. If no tabs are linked to the container, a random tab is selected, then deselected to ensure the container becomes inactive.

**Returns:** true if the container was closed successfully, false otherwise.

#### `openTab(Tab.Type type)`

**Returns:** `boolean`

Opens the specified Tab.Type. This method may select random tabs before switching to the target tab as an anti-ban mechanism.

**Parameters:**
- `type` - the tab to open; must not be null.

**Returns:** true if the tab was successfully opened or was already active, false if the operation failed.

**Throws:**
- IllegalArgumentException - if tab is null.


---

## TabManagerService

**Package:** `com.osmb.api.ui.component.tabs`

**Type:** Class

**Extends/Implements:** extends Object implements TabManager

### Methods

#### `getActiveTab()`

**Returns:** `Tab.Type`

Description copied from interface: TabManager

**Returns:** the active Tab.Type, or null if no tab is active.

#### `getActiveTabComponent()`

**Returns:** `Tab`

Description copied from interface: TabManager

**Returns:** the active Tab, or null if no tab is active.

#### `closeContainer()`

**Returns:** `boolean`

Description copied from interface: TabManager

**Returns:** true if the container was closed successfully, false otherwise.

#### `closeContainer(boolean humanDelay)`

**Returns:** `boolean`

#### `getTabComponent(Tab.Type type)`

**Returns:** `Tab`

Description copied from interface: TabManager

**Returns:** the Tab equivalent for the provided Tab.Type.

#### `openTab(Tab.Type type)`

**Returns:** `boolean`

Description copied from interface: TabManager

**Parameters:**
- `type` - the tab to open; must not be null.

**Returns:** true if the tab was successfully opened or was already active, false if the operation failed.


---

