# OSMB API - Widgets & Sprites

Widget management and sprite handling

---

## Classes in this Module

- [SpriteID](#spriteid) [class]
- [SpriteManager](#spritemanager) [class]
- [WidgetManager](#widgetmanager) [class]

---

## SpriteID

**Package:** `com.osmb.api.ui`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public static final int EXAPND_POPOUT_ICON`
- `public static final int CLOSE_POPOUT_ICON`
- `public static final int HOTKEY_ARROW_UP`
- `public static final int HOTKEY_ARROW_DOWN`
- `public static final int HOTKEY_CONTAINER_CLOSED`
- `public static final int HOTKEY_CONTAINER_TOP`
- `public static final int HOTKEY_CONTAINER_BOTTOM`
- `public static final int TAP_TO_DROP_ICON`
- `public static final int DISABLE_ENTITY_OPTIONS_ICON`
- `public static final int TILE_HIGHLIGHTS_ICON`
- `public static final int ENTITY_HIGHLIGHTS_ICON`
- `public static final int EDIT_GROUND_ITEMS_ICON`
- `public static final int STEEL_BORDER_NW`
- `public static final int STEEL_BORDER_NE`
- `public static final int STEEL_BORDER_SW`
- `public static final int STEEL_BORDER_SE`
- `public static final int ITEM_DIALOGUE_BORDER_NW`
- `public static final int ITEM_DIALOGUE_BORDER_NE`
- `public static final int ITEM_DIALOGUE_BORDER_SW`
- `public static final int ITEM_DIALOGUE_BORDER_SE`
- `public static final int ITEM_DIALOGUE_BORDER_HIGHLIGHTED_NW`
- `public static final int ITEM_DIALOGUE_BORDER_HIGHLIGHTED_NE`
- `public static final int ITEM_DIALOGUE_BORDER_HIGHLIGHTED_SW`
- `public static final int ITEM_DIALOGUE_BORDER_HIGHLIGHTED_SE`
- `public static final int STEEL_BORDER_TOP`
- `public static final int STEEL_BORDER_BOTTOM`
- `public static final int STEEL_BORDER_LEFT`
- `public static final int STEEL_BORDER_RIGHT`
- `public static final int STONE_BORDER_NW`
- `public static final int STONE_BORDER_NE`
- `public static final int STONE_BORDER_SW`
- `public static final int STONE_BORDER_SE`
- `public static final int STONE_BORDER_TOP`
- `public static final int STONE_BORDER_BOTTOM`
- `public static final int STONE_BORDER_LEFT`
- `public static final int STONE_BORDER_RIGHT`
- `public static final int MODERN_BORDER_NW`
- `public static final int MODERN_BORDER_NE`
- `public static final int MODERN_BORDER_SW`
- `public static final int MODERN_BORDER_SE`
- `public static final int MODERN_BORDER_TOP`
- `public static final int MODERN_BORDER_BOTTOM`
- `public static final int MODERN_BORDER_LEFT`
- `public static final int MODERN_BORDER_RIGHT`
- `public static final int TAB_NORMAL`
- `public static final int TAB_HIHGLIGHTED`
- `public static final int TAB_RED`
- `public static final int CHATBOX_FILTER_TAB_NORMAL`
- `public static final int CHATBOX_FILTER_TAB_HIGHLIGHTED`
- `public static final int CHATBOX_FILTER_TAB_RED`
- `public static final int CHATBOX_ICON`
- `public static final int KEYBOARD_ICON`
- `public static final int LOG_OUT_ICON`
- `public static final int COLLAPSE_TAB_ICON`
- `public static final int EXPAND_TAB_ICON`
- `public static final int INVENTORY_TAB_ICON`
- `public static final int SKILLS_TAB_ICON`
- `public static final int EQUIPMENT_TAB_ICON`
- `public static final int EMOTE_TAB_ICON`
- `public static final int PRAYER_TAB_ICON`
- `public static final int MUSIC_TAB_ICON`
- `public static final int NORMAL_SPELLBOOK_TAB_ICON`
- `public static final int ANCIENT_SPELLBOOK_TAB_ICON`
- `public static final int LUNAR_SPELLBOOK_TAB_ICON`
- `public static final int ARCEUUS_SPELLBOOK_TAB_ICON`
- `public static final int CLAN_TAB_ICON`
- `public static final int CLAN_TAB_BLUE_ICON`
- `public static final int CLAN_TAB_CYAN_ICON`
- `public static final int GROUPING_TAB_ICON`
- `public static final int COMBAT_TAB_ICON`
- `public static final int FRIENDS_TAB_ICON`
- `public static final int IGNORE_TAB_ICON`
- `public static final int QUEST_TAB_ICON`
- `public static final int ACCOUNT_SETTINGS_TAB_ICON`
- `public static final int SETTINGS_TAB_ICON`
- `public static final int SAILING_TAB_ICON`
- `public static final int SETTINGS_SUB_TAB_LEFT`
- `public static final int SETTINGS_SUB_TAB_LEFT_TRANSPARENT`
- `public static final int SETTINGS_SUB_TAB_CENTER`
- `public static final int SETTINGS_SUB_TAB_CENTER_TRANSPARENT`
- `public static final int SETTINGS_SUB_TAB_LEFT_SELECTED`
- `public static final int SETTINGS_SUB_TAB_LEFT_SELECTED_TRANSPARENT`
- `public static final int SETTINGS_SUB_TAB_CENTER_SELECTED`
- `public static final int SETTINGS_SUB_TAB_CENTER_SELECTED_TRANSPARENT`
- `public static final int SETTINGS_DISPLAY_ICON`
- `public static final int SETTINGS_CONTROLS_ICON`
- `public static final int SETTINGS_AUDIO_ICON`
- `public static final int CHATBOX_SIDE_TAB_NORMAL`
- `public static final int CHATBOX_SIDE_TAB_HIHGLIGHTED`
- `public static final int CHATBOX_SIDE_TAB_RED`
- `public static final int HOTKEYS_TAB_COLLAPSED`
- `public static final int HOTKEYS_TAB_OPEN_BOTTOM`
- `public static final int HOTKEYS_TAB_OPEN_TOP`
- `public static final int MINIMAP_ORB_NORMAL`
- `public static final int MINIMAP_ORB_HIGHLIGHTED`
- `public static final int MINIMAP_ORB_RED`
- `public static final int CIRCLE_ORB_NORMAL`
- `public static final int CIRCLE_ORB_HIGHLIGHTED`
- `public static final int CIRCLE_ORB_RED`
- `public static final int HEALTH_ORB_ICON`
- `public static final int PRAYER_ORB_ICON`
- `public static final int PRAYER_ORB_SELECTED_ICON`
- `public static final int RUN_ORB_ICON`
- `public static final int RUN_ORB_HIGHLIGHTED_ICON`
- `public static final int RUN_ORB_BOOSTED_ICON`
- `public static final int SPECIAL_ATTACK_ICON`
- `public static final int ORB_BLACK_UNDERLAY`
- `public static final int HEALTH_ORB_RED_UNDERLAY`
- `public static final int HEALTH_ORB_POISON_UNDERLAY`
- `public static final int HEALTH_ORB_DISEASED_UNDERLAY`
- `public static final int HEALTH_ORB_VENOM_UNDERLAY`
- `public static final int SPECIAL_ATTACK_ORB_UNDERLAY`
- `public static final int SPECIAL_ATTACK_ORB_ACTIVATED_UNDERLAY`
- `public static final int SPECIAL_ATTACK_ORB_DISABLED_UNDERLAY`
- `public static final int RUN_ORB_UNDERLAY`
- `public static final int RUN_ORB_ACTIVATED_UNDERLAY`
- `public static final int PRAYER_ORB_UNDERLAY`
- `public static final int PRAYER_ORB_ACTIVATED_UNDERLAY`
- `public static final int MAP_AND_COMPASS`
- `public static final int XP_DROPS_BUTTON`
- `public static final int XP_DROPS_BUTTON_HIGHLIGHTED`
- `public static final int XP_DROPS_BUTTON_ENABLED`
- `public static final int XP_DROPS_BUTTON_ENABLED_HIGHLIGHTED`
- `public static final int OLD_SCHOOL_BANNER`
- `public static final int WELCOME_SCREEN_BANNER_TOP`
- `public static final int SLIDER_BAR_LEFT`
- `public static final int SLIDER_BAR_RIGHT`
- `public static final int GREEN_SLIDER_THUMB`
- `public static final int BLUE_SLIDER_THUMB`
- `public static final int DIALOGUE_CONTAINER`
- `public static final int DIALOGUE_EXIT_BUTTON`
- `public static final int DIALOGUE_EXIT_BUTTON_HIGHLIGHTED`
- `public static final int SCROLL_BUTTON_UP`
- `public static final int LOGIN_CONTAINER`
- `public static final int LOGIN_CONTAINER_BUTTON`
- `public static final int LOGIN_CONTAINER_BUTTON_BIG`
- `public static final int WORLD_SELECTION_MENU_BUTTON`
- `public static final int TAP_HERE_TO_PLAY`
- `public static final int BANK_TAB_INFINITY_ICON`
- `public static final int BANK_TAB_PLUS_ICON`
- `public static final int BANK_DEPOSIT_INVENTORY_ICON`
- `public static final int BANK_DEPOSIT_EQUIPMENT_ICON`
- `public static final int BANK_SEARCH_ICON`
- `public static final int BANK_TAB_ACTIVE`
- `public static final int BANK_TAB_ACTIVE_HIGHLIGHTED`
- `public static final int BANK_TAB_ACTIVE_SELECTED`
- `public static final int BANK_TAB_NEW`
- `public static final int BANK_SQUARE_BUTTON`
- `public static final int BANK_SQUARE_BUTTON_RED`
- `public static final int BANK_TEXT_BUTTON_NW`
- `public static final int BANK_TEXT_BUTTON_NE`
- `public static final int BANK_TEXT_BUTTON_SW`
- `public static final int BANK_TEXT_BUTTON_SE`
- `public static final int BANK_TEXT_BUTTON_RED_NW`
- `public static final int BANK_TEXT_BUTTON_RED_NE`
- `public static final int BANK_TEXT_BUTTON_RED_SW`
- `public static final int BANK_TEXT_BUTTON_RED_SE`
- `public static final int BANK_CLOSE_BUTTON`
- `public static final int PRAYER_ACTIVE_UNDERLAY`


---

## SpriteManager

**Package:** `com.osmb.api.ui`

**Type:** Class

### Methods

#### `getEntityMapDot(EntityMapDot mapDot)`

**Returns:** `SearchableImage[]`

#### `getSprites()`

**Returns:** `Map<Integer,SpriteDefinition[]>`

#### `getSprite(int id, int frame)`

**Returns:** `SpriteDefinition`

#### `getSprite(int id)`

**Returns:** `SpriteDefinition`

#### `getSprites(int id)`

**Returns:** `SpriteDefinition[]`

#### `getResizedSprite(SpriteDefinition sprite, int newWidth, int newHeight)`

**Returns:** `Image`


---

## WidgetManager

**Package:** `com.osmb.api.ui`

**Type:** Class

### Methods

#### `getEquipment()`

**Returns:** `Equipment`

#### `getBank()`

**Returns:** `Bank`

#### `getDepositBox()`

**Returns:** `DepositBox`

#### `getMinimapOrbs()`

**Returns:** `MinimapOrbs`

#### `getHotkeys()`

**Returns:** `Hotkeys`

#### `getSettings()`

**Returns:** `Settings`

#### `getMinimap()`

**Returns:** `Minimap`

#### `getCompass()`

**Returns:** `Compass`

#### `getGameState()`

**Returns:** `GameState`

#### `getSkillTab()`

**Returns:** `Skill`

#### `getPrayerTab()`

**Returns:** `Prayer`

#### `isGameScreen(Point point)`

**Returns:** `boolean`

Checks if a given point is within the 3D game screen area & NOT inside the bounds of any active UI components.

**Parameters:**
- `point` - the point to check

**Returns:** true if the point is within the 3D game screen area and not inside any active UI components, false otherwise

#### `isInsideGameFrame(int x, int y)`

**Returns:** `boolean`

Checks if the given coordinates (x, y) are inside the game frame area. The method retrieves the screen bounds and the game frame boundaries, and checks if the coordinates fall within any of the game frame boundaries.

**Parameters:**
- `x` - the x-coordinate to check
- `y` - the y-coordinate to check

**Returns:** true if the coordinates are inside the game frame, false otherwise

#### `insideGameScreen(Shape shape, List<Class<? extends Component>> componentsToSkip)`

**Returns:** `boolean`

Determines if a given shape is sufficiently inside the game screen area, taking into account specific UI components to skip. By default, the ChatboxComponent is added to the list of components to skip as we can tap through it.

**Parameters:**
- `shape` - the shape to check
- `componentsToSkip` - a list of component classes to skip when calculating the game screen area

**Returns:** true if the shape is more than 20% inside the game screen, false otherwise

#### `insideGameScreenFactor(Shape shape, List<Class<? extends Component>> componentsToSkip)`

**Returns:** `double`

Calculates the factor of the given shape that is inside the 3D game screen area, excluding areas covered by active UI components (with option to skip certain components).

**Parameters:**
- `shape` - the shape to check
- `componentsToSkip` - list of component classes to skip when calculating the inside factor

**Returns:** a double value between 0.0 and 1.0 representing the proportion of the shape inside the game screen

#### `getGameFrameBoundaries()`

**Returns:** `List<Rectangle>`

Retrieves the boundaries of the visible game frame components.

**Returns:** a list of Rectangle objects representing the detected boundaries of the game frame

#### `getGameFrameBoundaries(List<Class<? extends Component>> componentsToSkip)`

**Returns:** `List<Rectangle>`

Retrieves the boundaries of the visible game frame components, excluding specified UI components.

**Parameters:**
- `componentsToSkip` - a list of component classes to exclude from boundary detection

**Returns:** a list of Rectangle objects representing the detected boundaries of the game frame

#### `addUIBoundary(UIBoundary uiBoundary)`

Adds a UIBoundary to be considered when calculating game frame boundaries. This can be used to add custom UI elements that should be treated as part of the game frame & will be considered when using methods like isGameScreen(Point), insideGameScreen(Shape, List) insideGameScreenFactor(Shape, List) getGameFrameBoundaries() getGameFrameBoundaries(List) Finger.tapGameScreen(Shape) Finger.tapGameScreen(boolean, Shape) Finger.tapGameScreen(Shape, MenuHook) Finger.tapGameScreen(String, Shape, String...) , Finger.tapGameScreen(Shape, String...) , PixelAnalyzer.findPixelsOnGameScreen(Shape, SearchablePixel...) , PixelAnalyzer.findPixelsOnGameScreenMulti(Shape, List), and others that rely on accurate game frame boundary detection.

**Parameters:**
- `uiBoundary` - the UIBoundary to add which would be either custom Component or OverlayBoundary

#### `getTabManager()`

**Returns:** `TabManager`

#### `getInventory()`

**Returns:** `Inventory`

#### `getChatbox()`

**Returns:** `Chatbox`

#### `getDialogue()`

**Returns:** `Dialogue`

#### `getLogoutTab()`

**Returns:** `Logout`

#### `getComponent(Class<? extends Component> component)`

**Returns:** `Component`

Retrieves a UI component instance of the specified type.

**Parameters:**
- `component` - The class type of the component to retrieve.

**Returns:** The active component of the specified type, or null if the component is not active.

#### `getDeadZoneBorder()`

**Returns:** `int[]`

Calculates and returns the borders of the "dead zone" area within the game canvas. The method retrieves the bounds of specific UI components (logout button, minimap, hotkey container, etc.) and uses them to determine the coordinates of the dead zone borders. If a component is not present or its bounds are unavailable, the method uses fallback logic or returns null if critical components are missing.

**Returns:** An integer array representing the dead zone borders, where: - index 0: Top Y-coordinate (vertical position of the top border). - index 1: Right X-coordinate (horizontal position of the right border). - index 2: Bottom Y-coordinate (vertical position of the bottom border). - index 3: Left X-coordinate (horizontal position of the left border). Returns null if critical components (logout button, minimap, or tab container) are missing or their bounds cannot be determined.

#### `getCenterComponentBounds()`

**Returns:** `Rectangle`

Gets the calculated bounds of the center component area of the game interface. Center components are UI elements that are typically positioned in the center of the game screen, such as bank interface, trade windows etc.

**Returns:** A Rectangle representing the bounds of the center component area, or null if no center component is active.

#### `getMiniMenu()`

**Returns:** `MiniMenu`

#### `addComponent(Component component)`

Add a custom Component to be detected by the WidgetManager.

**Parameters:**
- `component` - the custom component to add

#### `getActiveComponents()`

**Returns:** `Map<Class<? extends Component>,Component>`

Retrieves a map of all currently active/detected UI components managed by the WidgetManager.

**Returns:** A map where the keys are the classes of the active components and the values are the corresponding component instances.

#### `getSpellbook()`

**Returns:** `Spellbook`


---

