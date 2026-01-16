# Chatbox

**Type:** Interface

**Extends:** Expandable

**All Superinterfaces:** Expandable, Viewable

**All Known Implementing Classes:** ChatboxComponent

## Methods

| Return Type | Method |
|------------|--------|
| `void` | `addFontColor(int color)` |
| `ChatboxFilterTab` | `getActiveFilterTab()` |
| `UIResultList<String>` | `getText()` |
| `String` | `getUsername()` |
| `Rectangle` | `getUsernameBounds()` |
| `boolean` | `openFilterTab(ChatboxFilterTab chatboxFilterTab)` |

## Method Details

### getActiveFilterTab
```java
ChatboxFilterTab getActiveFilterTab()
```

Gets the currently active filter tab in the chatbox.

**Note:** This is a read-only method and does not update game frames.

**Returns:** The active `ChatboxFilterTab`.

### openFilterTab
```java
boolean openFilterTab(ChatboxFilterTab chatboxFilterTab)
```

Opens the specified filter tab in the chatbox.

**Note:** This method updates game frames while running and waits until the UI reflects the requested state (or an internal timeout elapses). Returns `false` if the control is not visible, cannot be tapped, or the state did not confirm in time.

**Parameters:**
- `chatboxFilterTab` - The `ChatboxFilterTab` to open.

**Returns:** true if the tab was successfully opened, false otherwise.

### getUsername
```java
String getUsername()
```

Gets the username of the player currently logged into the game.

**Note:** This is a read-only method and does not update game frames.

**Returns:** The player's username as a String.

### getUsernameBounds
```java
Rectangle getUsernameBounds()
```

Gets the bounding rectangle of the username display area in the chatbox.

**Note:** This is a read-only method and does not update game frames.

**Returns:** A `Rectangle` representing the bounds of the username area.

### getText
```java
UIResultList<String> getText()
```

Retrieves the text messages currently displayed in the chatbox.

**Note:** This is a read-only method and does not update game frames.

**Returns:** A `UIResultList` containing the chatbox messages as Strings.

### addFontColor
```java
void addFontColor(int color)
```

Adds a font color to the chatbox's font color list, allowing messages with this color to be recognized by `getText()`.

**Parameters:**
- `color` - The color value to add.
