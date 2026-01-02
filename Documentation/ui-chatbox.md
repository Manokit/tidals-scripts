# OSMB API - Chatbox & Dialogue

Chatbox, dialogue handling, and chat components

---

## Classes in this Module

- [Chatbox](#chatbox) [class]
- [ChatboxComponent](#chatboxcomponent) [class]
- [ChatboxTab](#chatboxtab) [class]
- [Class ChatboxFilterTab](#class-chatboxfiltertab) [class]
- [Class DialogueType](#class-dialoguetype) [class]
- [Dialogue](#dialogue) [class]
- [DialogueComponent](#dialoguecomponent) [class]
- [LogoutTabComponent](#logouttabcomponent) [class]

---

## Chatbox

**Package:** `com.osmb.api.ui.chatbox`

**Type:** Class

**Extends/Implements:** extends Expandable

**Interfaces:** Expandable, Viewable

### Methods

#### `getActiveFilterTab()`

**Returns:** `ChatboxFilterTab`

Gets the currently active filter tab in the chatbox. Note: This is a read-only method and does not update game frames.

**Returns:** The active ChatboxFilterTab.

#### `openFilterTab(ChatboxFilterTab chatboxFilterTab)`

**Returns:** `boolean`

Opens the specified filter tab in the chatbox. Note: This method updates game frames while running and waits until the UI reflects the requested state (or an internal timeout elapses). Returns false if the control is not visible, cannot be tapped, or the state did not confirm in time.

**Parameters:**
- `chatboxFilterTab` - The ChatboxFilterTab to open.

**Returns:** true if the tab was successfully opened, false otherwise.

#### `getUsername()`

**Returns:** `String`

Gets the username of the player currently logged into the game. Note: This is a read-only method and does not update game frames.

**Returns:** The player's username as a String.

#### `getUsernameBounds()`

**Returns:** `Rectangle`

Gets the bounding rectangle of the username display area in the chatbox. Note: This is a read-only method and does not update game frames.

**Returns:** A Rectangle representing the bounds of the username area.

#### `getText()`

**Returns:** `UIResultList<String>`

Retrieves the text messages currently displayed in the chatbox. Note: This is a read-only method and does not update game frames.

**Returns:** A UIResultList containing the chatbox messages as Strings.

#### `addFontColor(int color)`

Adds a font color to the chatbox's font color list, allowing messages with this color to be recognized by getText().

**Parameters:**
- `color` - The color value to add.


---

## ChatboxComponent

**Package:** `com.osmb.api.ui.component.chatbox`

**Type:** Class

**Extends/Implements:** extends ComponentChild<Integer> implements Chatbox

### Fields

- `public static final int CHATBOX_WIDTH`
- `public static final int CHATBOX_HEIGHT`
- `public static final int BUTTON_Y_OFFSET`
- `public static final int LINE_HEIGHT`
- `public static final int MAX_LINES`
- `public static final Set<Integer> CHATBOX_TEXT_COLORS` - Add all the colors used to be recognised in the chatbox text to this set.
- `public static final int WHITE_TEXT_PIXEL`

### Methods

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<Integer>>`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `isVisible()`

**Returns:** `boolean`

#### `getVisibilityCondition(ComponentSearchResult parentResult)`

**Returns:** `UIResult<BooleanSupplier>`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getActiveFilterTab()`

**Returns:** `ChatboxFilterTab`

Description copied from interface: Chatbox

**Returns:** The active ChatboxFilterTab.

#### `openFilterTab(ChatboxFilterTab chatboxFilterTab)`

**Returns:** `boolean`

Description copied from interface: Chatbox

**Parameters:**
- `chatboxFilterTab` - The ChatboxFilterTab to open.

**Returns:** true if the tab was successfully opened, false otherwise.

#### `getUsername()`

**Returns:** `String`

Description copied from interface: Chatbox

**Returns:** The player's username as a String.

#### `getUsernameBounds()`

**Returns:** `Rectangle`

Description copied from interface: Chatbox

**Returns:** A Rectangle representing the bounds of the username area.

#### `isOpen()`

**Returns:** `boolean`

#### `open()`

**Returns:** `boolean`

#### `close()`

**Returns:** `boolean`

#### `getText()`

**Returns:** `UIResultList<String>`

Description copied from interface: Chatbox

**Returns:** A UIResultList containing the chatbox messages as Strings.

#### `addFontColor(int color)`

Description copied from interface: Chatbox

**Parameters:**
- `color` - The color value to add.

#### `getTextLines()`

**Returns:** `UIResultList<Rectangle>`


---

## ChatboxTab

**Package:** `com.osmb.api.ui.component.chatbox`

**Type:** Class

**Extends/Implements:** extends ComponentParent<ComponentButtonStatus>

### Methods

#### `getScreenArea()`

**Returns:** `ComponentParent.ScreenArea`

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<ComponentButtonStatus>>`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `findIcon(Rectangle containerBounds)`

**Returns:** `UIResult<Integer>`

#### `getComponentGameState()`

**Returns:** `GameState`

#### `getIcons()`

**Returns:** `int[]`


---

## Class ChatboxFilterTab

**Package:** `com.osmb.api.ui.chatbox`

**Type:** Class

**Extends/Implements:** extends Enum<ChatboxFilterTab>

### Methods

#### `values()`

**Returns:** `ChatboxFilterTab[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `ChatboxFilterTab`

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

## Class DialogueType

**Package:** `com.osmb.api.ui.chatbox.dialogue`

**Type:** Class

**Extends/Implements:** extends Enum<DialogueType>

### Methods

#### `values()`

**Returns:** `DialogueType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `DialogueType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Dialogue

**Package:** `com.osmb.api.ui.chatbox.dialogue`

**Type:** Class

### Methods

#### `getDialogueType()`

**Returns:** `DialogueType`

**Returns:** The type of dialogue detected.

#### `getBounds()`

**Returns:** `Rectangle`

Gets the boundaries of the dialogue.

**Returns:** Dialogue bounds

#### `getOptions()`

**Returns:** `List<String>`

Gets the available options of a DialogueType.TEXT_OPTION

**Returns:** The options available inside a DialogueType.TEXT_OPTION, the list is in order of the options shown (Top to bottom) If there is no dialogue visible or the dialogue type is not DialogueType.TEXT_OPTION then this will return null.

#### `addFontColor(int color)`

#### `getText()`

**Returns:** `UIResult<String>`

Gets the text from DialogueType.CHAT_DIALOGUE or DialogueType.TAP_HERE_TO_CONTINUE.

#### `getTextLines(int textColor)`

**Returns:** `UIResult<Rectangle[]>`

#### `selectOption(String option)`

**Returns:** `boolean`

Selects an option for DialogueType.TEXT_OPTION.

**Parameters:**
- `option` - - The option to select.

**Returns:** If the dialogue interaction was successful

#### `selectItem(int... itemID)`

**Returns:** `boolean`

Selects an item from a DialogueType.ITEM_OPTION dialogue.

**Parameters:**
- `itemID` - The item ID to search for & interact with inside the dialogue.

**Returns:** If the dialogue interaction was successful

#### `selectItem(Collection<Integer> itemID)`

**Returns:** `boolean`

#### `getDialogueTitle()`

**Returns:** `UIResult<String>`

Gets the header for text dialogues. Indicates NPC or player's name

**Returns:** The red text at the top of a dialogue.

#### `isVisible()`

**Returns:** `boolean`

Checks to see if a dialogue is present.

**Returns:** True if a dialogue is present on the screen, false otherwise.

#### `continueChatDialogue()`

**Returns:** `boolean`

Checks if the dialogue is a DialogueType.CHAT_DIALOGUE or DialogueType.TAP_HERE_TO_CONTINUE & then interacts - continuing the dialogue waiting until the dialogue changes or disappears.

**Returns:** True if the dialogue is a chat dialogue, false otherwise.


---

## DialogueComponent

**Package:** `com.osmb.api.ui.component.chatbox.dialogue`

**Type:** Class

**Extends/Implements:** extends ComponentParent<Integer> implements Dialogue

### Fields

- `public static final int OPTION_SWORD_ID`
- `public static final int RED_TEXT_COLOR`

### Methods

#### `getScreenArea()`

**Returns:** `ComponentParent.ScreenArea`

#### `getItemImages(int... ids)`

**Returns:** `List<SearchableItem>`

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<Integer>>`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `getComponentGameState()`

**Returns:** `GameState`

#### `isVisible()`

**Returns:** `boolean`

Description copied from interface: Dialogue

**Returns:** True if a dialogue is present on the screen, false otherwise.

#### `getBounds()`

**Returns:** `Rectangle`

Description copied from interface: Dialogue

**Returns:** Dialogue bounds

#### `getOptions()`

**Returns:** `List<String>`

Description copied from interface: Dialogue

**Returns:** The options available inside a DialogueType.TEXT_OPTION, the list is in order of the options shown (Top to bottom) If there is no dialogue visible or the dialogue type is not DialogueType.TEXT_OPTION then this will return null.

#### `addFontColor(int color)`

#### `getText()`

**Returns:** `UIResult<String>`

Description copied from interface: Dialogue

#### `getTextLines(int textColor)`

**Returns:** `UIResult<Rectangle[]>`

#### `selectOption(String option)`

**Returns:** `boolean`

Description copied from interface: Dialogue

**Parameters:**
- `option` - - The option to select.

**Returns:** If the dialogue interaction was successful

#### `findItem(int... itemID)`

**Returns:** `boolean`

#### `selectItem(Collection<Integer> itemID)`

**Returns:** `boolean`

#### `selectItem(int... itemIDs)`

**Returns:** `boolean`

Description copied from interface: Dialogue

**Parameters:**
- `itemIDs` - The item ID to search for & interact with inside the dialogue.

**Returns:** If the dialogue interaction was successful

#### `getDialogueTitle()`

**Returns:** `UIResult<String>`

Description copied from interface: Dialogue

**Returns:** The red text at the top of a dialogue.

#### `continueChatDialogue()`

**Returns:** `boolean`

Description copied from interface: Dialogue

**Returns:** True if the dialogue is a chat dialogue, false otherwise.

#### `getDialogueType()`

**Returns:** `DialogueType`

**Returns:** The type of dialogue detected.


---

## LogoutTabComponent

**Package:** `com.osmb.api.ui.component.tabs.chatbox`

**Type:** Class

**Extends/Implements:** extends ComponentChild<ComponentButtonStatus> implements Logout, Tab

### Methods

#### `getComponentGameState()`

**Returns:** `GameState`

#### `isVisible()`

**Returns:** `boolean`

#### `getType()`

**Returns:** `Tab.Type`

#### `getComponent()`

**Returns:** `Component`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getParentOffsets()`

**Returns:** `Map<ComponentButtonStatus,Point>`

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<ComponentButtonStatus>>`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `findIcon(Rectangle containerBounds)`

**Returns:** `UIResult<Integer>`

#### `getXOffset()`

**Returns:** `int`

#### `getIcons()`

**Returns:** `int[]`

#### `logout()`

**Returns:** `boolean`

#### `getContainer()`

**Returns:** `Component`

#### `isOpen()`

**Returns:** `boolean`

#### `open()`

**Returns:** `boolean`

#### `open(boolean humanDelay)`

**Returns:** `boolean`

#### `close()`

**Returns:** `boolean`

#### `close(boolean humanDelay)`

**Returns:** `boolean`


---

