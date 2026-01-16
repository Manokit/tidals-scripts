# Finger

**Type:** Interface

## Methods

| Return Type | Method |
|------------|--------|
| `long` | `getLastTapMillis()` |
| `int` | `getLastTapX()` |
| `int` | `getLastTapY()` |
| `TouchType` | `getLastTouchType()` |
| `boolean` | `tap(int x, int y)` |
| `boolean` | `tap(boolean humanDelay, int x, int y)` |
| `boolean` | `tap(boolean humanDelay, int x, int y, MenuHook menuHook)` |
| `boolean` | `tap(boolean humanDelay, ItemSearchResult itemSearchResult)` |
| `boolean` | `tap(boolean humanDelay, ItemSearchResult itemSearchResult, String... actions)` |
| `boolean` | `tap(boolean humanDelay, Shape shape)` |
| `boolean` | `tap(boolean humanDelay, Shape shape, MenuHook menuHook)` |
| `boolean` | `tap(boolean humanDelay, Shape shape, String... entryText)` |
| `boolean` | `tap(int x, int y, String... entryText)` |
| `boolean` | `tap(int x, int y, String entityNameOverride, String... actions)` |
| `boolean` | `tap(Point point)` |
| `boolean` | `tap(Point point, String... entryText)` |
| `boolean` | `tap(Shape shape)` |
| `boolean` | `tap(Shape shape, MenuHook menuHook)` |
| `boolean` | `tap(Shape shape, String... entryText)` |
| `default boolean` | `tapGameScreen(Shape shape)` |
| `boolean` | `tapGameScreen(boolean humanDelay, Shape shape)` |
| `boolean` | `tapGameScreen(Shape shape, MenuHook menuHook)` |
| `boolean` | `tapGameScreen(Shape shape, String... entryText)` |
| `boolean` | `tapGameScreen(String entityNameOverride, Shape shape, String... entryText)` |
| `MenuEntry` | `tapGetResponse(boolean humanDelay, Shape shape)` |
| `boolean` | `touch(int x, int y, TouchType touchType)` |

## Method Details

### getLastTouchType
```java
TouchType getLastTouchType()
```

### getLastTapMillis
```java
long getLastTapMillis()
```

### getLastTapX
```java
int getLastTapX()
```

Gets the x-coordinate of the last tap performed

**Returns:** x coordinate of last tap

### getLastTapY
```java
int getLastTapY()
```

Gets the y-coordinate of the last tap performed

**Returns:** y coordinate of last tap

### tap
```java
boolean tap(int x, int y)
```

Performs a tap at the specified coordinates with default human-like delay

**Parameters:**
- `x` - The x coordinate to tap
- `y` - The y coordinate to tap

**Returns:** true if the tap was successful, false otherwise

---

```java
boolean tap(Point point)
```

Performs a tap at the specified point with default human-like delay

**Parameters:**
- `point` - The point to tap

**Returns:** true if the tap was successful, false otherwise

---

```java
boolean tap(Shape shape)
```

Performs a tap at a random point within the specified shape with default human-like delay

**Parameters:**
- `shape` - The shape to tap within

**Returns:** true if the tap was successful, false otherwise

---

```java
boolean tap(boolean humanDelay, int x, int y)
```

Performs a tap with configurable delay settings

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `x` - The x coordinate to tap
- `y` - The y coordinate to tap

**Returns:** true if the tap was successful, false otherwise

---

```java
boolean tap(int x, int y, String... entryText)
```

Performs a tap and attempts to select a menu entry matching the provided text

**Parameters:**
- `x` - The x coordinate to tap
- `y` - The y coordinate to tap
- `entryText` - One or more menu entry texts to match (checks if entry starts with text)

**Returns:** true if the menu entry was found and selected, false otherwise

---

```java
boolean tap(Point point, String... entryText)
```

Performs a tap and attempts to select a menu entry matching the provided text

**Parameters:**
- `point` - The point coordinate to tap
- `entryText` - One or more menu entry texts to match (checks if entry starts with text)

**Returns:** true if the menu entry was found and selected, false otherwise

---

```java
boolean tap(Shape shape, String... entryText)
```

Performs a tap and attempts to select a menu entry matching the provided text

**Parameters:**
- `shape` - The area to tap, a random point inside the shape will be generated
- `entryText` - One or more menu entry texts to match (checks if entry starts with text)

**Returns:** true if the menu entry was found and selected, false otherwise

---

```java
boolean tap(boolean humanDelay, Shape shape)
```

Performs a tap with a custom menu selection hook and configurable delay

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `shape` - The shape to tap within

**Returns:** true if the menu interaction was successful, false otherwise

---

```java
boolean tap(boolean humanDelay, int x, int y, MenuHook menuHook)
```

Performs a tap with a custom menu selection hook and configurable delay

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `x` - The x coordinate to tap
- `y` - The y coordinate to tap
- `menuHook` - Custom menu selection hook

**Returns:** true if the menu interaction was successful, false otherwise

---

```java
boolean tap(boolean humanDelay, Shape shape, MenuHook menuHook)
```

Performs a tap on a specific shape with a custom menu selection hook

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `shape` - The shape to tap within
- `menuHook` - Custom menu selection hook

**Returns:** true if the menu interaction was successful, false otherwise

---

```java
boolean tap(boolean humanDelay, Shape shape, String... entryText)
```

Performs a tap with a custom menu selection hook and configurable delay

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `shape` - The shape to tap within
- `entryText` - One or more menu entry texts to match

**Returns:** true if the menu interaction was successful, false otherwise

---

```java
boolean tap(boolean humanDelay, ItemSearchResult itemSearchResult)
```

Performs a tap on an item search result with configurable delay

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `itemSearchResult` - The item to tap

**Returns:** true if the tap was successful, false if the item is not visible or tap fails

---

```java
boolean tap(boolean humanDelay, ItemSearchResult itemSearchResult, String... actions)
```

Performs a tap on an item and attempts to select a specific action from its menu

**Parameters:**
- `humanDelay` - Whether to use human-like delay patterns
- `itemSearchResult` - The item to tap
- `actions` - One or more menu actions to attempt (exact match required)

**Returns:** true if the action was found and selected, false otherwise

---

```java
boolean tap(int x, int y, String entityNameOverride, String... actions)
```

Performs a tap on specific coordinates targeting a named entity with optional actions

**Parameters:**
- `x` - The x coordinate to tap
- `y` - The y coordinate to tap
- `entityNameOverride` - Overrides the entity name. Used in situations where the name from the object definition is null.
- `actions` - Optional menu actions to select (exact match required)

**Returns:** true if the interaction was successful, false otherwise

---

```java
boolean tap(Shape shape, MenuHook menuHook)
```

Performs a tap with a custom menu selection handler

**Parameters:**
- `shape` - The shape to tap within
- `menuHook` - Custom menu selection hook

**Returns:** true if the menu interaction was successful, false otherwise

### tapGetResponse
```java
MenuEntry tapGetResponse(boolean humanDelay, Shape shape)
```

### tapGameScreen
```java
default boolean tapGameScreen(Shape shape)
```

---

```java
boolean tapGameScreen(boolean humanDelay, Shape shape)
```

---

```java
boolean tapGameScreen(Shape shape, String... entryText)
```

Performs a tap on the game screen (outside UI boundaries) within a shape

**Parameters:**
- `shape` - The shape to tap within
- `entryText` - Optional menu entries to select

**Returns:** true if the tap was successful, false if the coordinates aren't on the game screen (covered by a UI component) or if the tap fails in general

---

```java
boolean tapGameScreen(String entityNameOverride, Shape shape, String... entryText)
```

Performs a tap on a specific game entity with optional actions

**Parameters:**
- `entityNameOverride` - Overrides the entity name. Used in situations where the name from the object definition is null.
- `shape` - The shape containing the entity
- `entryText` - Optional menu entries to select

**Returns:** true if the interaction was successful, false if entity not found or interaction fails

---

```java
boolean tapGameScreen(Shape shape, MenuHook menuHook)
```

### touch
```java
boolean touch(int x, int y, TouchType touchType)
```
