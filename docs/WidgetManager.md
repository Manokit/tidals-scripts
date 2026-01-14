# WidgetManager

**Type:** Interface

## Methods

| Return Type | Method |
|------------|--------|
| `void` | `addComponent(Component component)` |
| `void` | `addUIBoundary(UIBoundary uiBoundary)` |
| `List<Component>` | `getActiveComponents()` |
| `Bank` | `getBank()` |
| `Rectangle` | `getCenterComponentBounds()` |
| `Chatbox` | `getChatbox()` |
| `Compass` | `getCompass()` |
| `Component` | `getComponent(Class<T>)` |
| `int[]` | `getDeadZoneBorder()` |
| `DepositBox` | `getDepositBox()` |
| `Dialogue` | `getDialogue()` |
| `Equipment` | `getEquipment()` |
| `List<Rectangle>` | `getGameFrameBoundaries()` |
| `List<Rectangle>` | `getGameFrameBoundaries(List)` |
| `GameState` | `getGameState()` |
| `Hotkeys` | `getHotkeys()` |
| `Inventory` | `getInventory()` |
| `LogoutTab` | `getLogoutTab()` |
| `Minimap` | `getMinimap()` |
| `MinimapOrbs` | `getMinimapOrbs()` |
| `PrayerTab` | `getPrayerTab()` |
| `Settings` | `getSettings()` |
| `SkillTab` | `getSkillTab()` |
| `Spellbook` | `getSpellbook()` |
| `TabManager` | `getTabManager()` |
| `boolean` | `insideGameScreen(Shape, List)` |
| `double` | `insideGameScreenFactor(Shape shape, List<Class<? extends Component>> componentsToSkip)` |
| `boolean` | `isGameScreen(Point point)` |
| `boolean` | `isInsideGameFrame(int x, int y)` |

## Method Details

### getEquipment
```java
Equipment getEquipment()
```

### getBank
```java
Bank getBank()
```

### getDepositBox
```java
DepositBox getDepositBox()
```

### getMinimapOrbs
```java
MinimapOrbs getMinimapOrbs()
```

### getHotkeys
```java
Hotkeys getHotkeys()
```

### getSettings
```java
Settings getSettings()
```

### getMinimap
```java
Minimap getMinimap()
```

### getCompass
```java
Compass getCompass()
```

### getGameState
```java
GameState getGameState()
```

### getSkillTab
```java
SkillTab getSkillTab()
```

### getPrayerTab
```java
PrayerTab getPrayerTab()
```

### isGameScreen
```java
boolean isGameScreen(Point point)
```

Checks if a given point is within the 3D game screen area & NOT inside the bounds of any active UI components.

**Parameters:**
- `point` - the point to check

**Returns:** true if the point is within the 3D game screen area and not inside any active UI components, false otherwise

### isInsideGameFrame
```java
boolean isInsideGameFrame(int x, int y)
```

### insideGameScreen
```java
boolean insideGameScreen(Shape, List)
```

### insideGameScreenFactor
```java
double insideGameScreenFactor(Shape shape, List<Class<? extends Component>> componentsToSkip)
```

### getGameFrameBoundaries
```java
List<Rectangle> getGameFrameBoundaries()
```

```java
List<Rectangle> getGameFrameBoundaries(List)
```

### addUIBoundary
```java
void addUIBoundary(UIBoundary uiBoundary)
```

Adds a UIBoundary to be considered when calculating game frame boundaries. This can be used to add custom UI elements that should be treated as part of the game frame & will be considered when using methods like `isGameScreen(Point)`, `insideGameScreen(Shape, List)` `insideGameScreenFactor(Shape, List)` `getGameFrameBoundaries()` `getGameFrameBoundaries(List)` `Finger.tapGameScreen(Shape)` `Finger.tapGameScreen(boolean, Shape)` `Finger.tapGameScreen(Shape, MenuHook)` `Finger.tapGameScreen(String, Shape, String...)` , `Finger.tapGameScreen(Shape, String...)` , `PixelAnalyzer.findPixelsOnGameScreen(Shape, SearchablePixel...)` , `PixelAnalyzer.findPixelsOnGameScreenMulti(Shape, List)`,

and others that rely on accurate game frame boundary detection.

**Parameters:**
- `uiBoundary` - the UIBoundary to add which would be either custom `Component` or `OverlayBoundary`

### getTabManager
```java
TabManager getTabManager()
```

### getInventory
```java
Inventory getInventory()
```

### getChatbox
```java
Chatbox getChatbox()
```

### getDialogue
```java
Dialogue getDialogue()
```

### getLogoutTab
```java
LogoutTab getLogoutTab()
```

### getComponent
```java
<T extends Component> T getComponent(Class<T> component)
```

Retrieves a UI component instance of the specified type.

**Parameters:**
- `component` - The class type of the component to retrieve.

**Returns:** The active component of the specified type, or `null` if the component is not active.

### getDeadZoneBorder
```java
int[] getDeadZoneBorder()
```

Calculates and returns the borders of the "dead zone" area within the game canvas.

The method retrieves the bounds of specific UI components (logout button, minimap, hotkey container, etc.) and uses them to determine the coordinates of the dead zone borders. If a component is not present or its bounds are unavailable, the method uses fallback logic or returns `null` if critical components are missing.

**Returns:** An integer array representing the dead zone borders, where:
- index 0: Top Y-coordinate (vertical position of the top border).
- index 1: Right X-coordinate (horizontal position of the right border).
- index 2: Bottom Y-coordinate (vertical position of the bottom border).
- index 3: Left X-coordinate (horizontal position of the left border).

Returns `null` if critical components (logout button, minimap, or tab container) are missing or their bounds cannot be determined.

### getCenterComponentBounds
```java
Rectangle getCenterComponentBounds()
```

### addComponent
```java
void addComponent(Component component)
```

Add a custom `Component` to be detected by the WidgetManager.

**Parameters:**
- `component` - the custom component to add

### getActiveComponents
```java
List<Component> getActiveComponents()
```

### getSpellbook
```java
Spellbook getSpellbook()
```
