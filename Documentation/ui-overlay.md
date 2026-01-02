# OSMB API - Overlays & Buffs

Overlay and buff/status display components

---

## Classes in this Module

- [BuffOverlay](#buffoverlay) [class]
- [Class OverlayPosition](#class-overlayposition) [class]
- [Class OverlayPosition.HorizontalEdge](#class-overlayposition.horizontaledge) [class]
- [Class OverlayPosition.VerticalEdge](#class-overlayposition.verticaledge) [class]
- [Class OverlayShiftDirection](#class-overlayshiftdirection) [class]
- [HealthOverlay](#healthoverlay) [class]
- [HealthOverlay.HealthResult](#healthoverlay.healthresult) [class]
- [OverlayBoundary](#overlayboundary) [class]
- [OverlayValueFinder<T>](#overlayvaluefindert) [class]

---

## BuffOverlay

**Package:** `com.osmb.api.ui.overlay`

**Type:** Class

**Extends/Implements:** extends OverlayBoundary

### Fields

- `public static final SearchablePixel BLACK_PIXEL`
- `public static final int[] BORDER_PIXELS`
- `public static final int BASE_COLOR`
- `public static final ToleranceComparator COLOR_TOLERANCE`
- `public static final String TEXT`

### Methods

#### `getWidth()`

**Returns:** `int`

#### `getHeight()`

**Returns:** `int`

#### `checkVisibility(Rectangle bounds)`

**Returns:** `boolean`

#### `getOverlayPosition()`

**Returns:** `OverlayPosition`

#### `getOverlayOffset()`

**Returns:** `Point`

#### `applyValueFinders()`

**Returns:** `List<OverlayValueFinder>`

#### `getBuffText()`

**Returns:** `String`

#### `onOverlayFound(Rectangle overlayBounds)`

#### `onOverlayNotFound()`


---

## Class OverlayPosition

**Package:** `com.osmb.api.ui.overlay`

**Type:** Class

**Extends/Implements:** extends Enum<OverlayPosition>

### Methods

#### `values()`

**Returns:** `OverlayPosition[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `OverlayPosition`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getHorizontalEdge()`

**Returns:** `OverlayPosition.HorizontalEdge`

#### `getVerticalEdge()`

**Returns:** `OverlayPosition.VerticalEdge`

#### `getOverlayShiftDirection()`

**Returns:** `OverlayShiftDirection`


---

## Class OverlayPosition.HorizontalEdge

**Package:** `com.osmb.api.ui.overlay`

**Type:** Class

**Extends/Implements:** extends Enum<OverlayPosition.HorizontalEdge>

### Methods

#### `values()`

**Returns:** `OverlayPosition.HorizontalEdge[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `OverlayPosition.HorizontalEdge`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Class OverlayPosition.VerticalEdge

**Package:** `com.osmb.api.ui.overlay`

**Type:** Class

**Extends/Implements:** extends Enum<OverlayPosition.VerticalEdge>

### Methods

#### `values()`

**Returns:** `OverlayPosition.VerticalEdge[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `OverlayPosition.VerticalEdge`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Class OverlayShiftDirection

**Package:** `com.osmb.api.ui.overlay`

**Type:** Class

**Extends/Implements:** extends Enum<OverlayShiftDirection>

### Methods

#### `values()`

**Returns:** `OverlayShiftDirection[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `OverlayShiftDirection`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## HealthOverlay

**Package:** `com.osmb.api.ui.overlay`

**Type:** Class

**Extends/Implements:** extends OverlayBoundary

### Fields

- `public static final String NPC_NAME`
- `public static final String HEALTH`

### Methods

#### `getWidth()`

**Returns:** `int`

#### `getHeight()`

**Returns:** `int`

#### `checkVisibility(Rectangle bounds)`

**Returns:** `boolean`

#### `getOverlayPosition()`

**Returns:** `OverlayPosition`

#### `getOverlayOffset()`

**Returns:** `Point`

#### `applyValueFinders()`

**Returns:** `List<OverlayValueFinder>`

#### `getHealthResult()`

**Returns:** `HealthOverlay.HealthResult`

This method retrieves the health information of the NPC, including current and maximum hitpoints. If the health overlay is not visible, it returns null.

**Returns:** The health result containing current and maximum hitpoints, or null if not available.

#### `getNPCName()`

**Returns:** `String`

This method retrieves the name of the NPC from the health overlay. If the health overlay is not visible, it returns null.

**Returns:** The name of the NPC, or null if not available.

#### `onOverlayFound(Rectangle overlayBounds)`

#### `onOverlayNotFound()`


---

## HealthOverlay.HealthResult

**Package:** `com.osmb.api.ui.overlay`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getCurrentHitpoints()`

**Returns:** `int`

#### `getMaxHitpoints()`

**Returns:** `int`


---

## OverlayBoundary

**Package:** `com.osmb.api.ui.overlay`

**Type:** Class

**Extends/Implements:** extends Object implements UIBoundary

### Fields

- `protected ScriptCore core`

### Methods

#### `transformOverlayPosition(ScriptCore core, OverlayPosition overlayPosition, Rectangle rectangle)`

**Returns:** `Rectangle`

#### `getWidth()`

**Returns:** `int`

#### `getHeight()`

**Returns:** `int`

#### `checkVisibility(Rectangle bounds)`

**Returns:** `boolean`

#### `getOverlayPosition()`

**Returns:** `OverlayPosition`

#### `getOverlayOffset()`

**Returns:** `Point`

#### `applyValueFinders()`

**Returns:** `List<OverlayValueFinder>`

#### `getValue(String key)`

**Returns:** `Object`

#### `onOverlayFound(Rectangle overlayBounds)`

#### `onOverlayNotFound()`

#### `isVisible()`

**Returns:** `boolean`

#### `getBounds()`

**Returns:** `Rectangle`


---

## OverlayValueFinder<T>

**Package:** `com.osmb.api.ui.overlay`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `key()`

**Returns:** `String`

#### `findValue(Rectangle overlayBounds)`

**Returns:** `T`

#### `toString()`

**Returns:** `String`


---

