# OSMB API - Minimap & Orbs

Minimap, compass, and status orbs

---

## Classes in this Module

- [Class Compass.Direction](#class-compass.direction) [class]
- [Class EntityMapDot](#class-entitymapdot) [class]
- [Class HealthStatus](#class-healthstatus) [class]
- [Compass](#compass) [class]
- [HealthOrb](#healthorb) [class]
- [Minimap](#minimap) [class]
- [MinimapArrowResult](#minimaparrowresult) [class]
- [MinimapComponent](#minimapcomponent) [class]
- [MinimapOrb](#minimaporb) [class]
- [MinimapOrbs](#minimaporbs) [class]
- [OrbManager](#orbmanager) [class]
- [PrayerOrb](#prayerorb) [class]
- [RunOrb](#runorb) [class]
- [SpecialAttackOrb](#specialattackorb) [class]
- [XPDropsComponent](#xpdropscomponent) [class]

---

## Class Compass.Direction

**Package:** `com.osmb.api.ui.minimap`

**Type:** Class

**Extends/Implements:** extends Enum<Compass.Direction>

### Methods

#### `values()`

**Returns:** `Compass.Direction[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `Compass.Direction`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Class EntityMapDot

**Package:** `com.osmb.api.ui.minimap`

**Type:** Class

**Extends/Implements:** extends Enum<EntityMapDot>

### Methods

#### `values()`

**Returns:** `EntityMapDot[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `EntityMapDot`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Class HealthStatus

**Package:** `com.osmb.api.ui.minimap.status`

**Type:** Class

**Extends/Implements:** extends Enum<HealthStatus>

### Methods

#### `values()`

**Returns:** `HealthStatus[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `HealthStatus`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getSpriteUnderlayID()`

**Returns:** `int`


---

## Compass

**Package:** `com.osmb.api.ui.minimap`

**Type:** Class

### Methods

#### `getRotation()`

**Returns:** `UIResult<Integer>`

#### `setDirection(Compass.Direction direction)`

#### `getCompassCenter()`

**Returns:** `UIResult<Point>`


---

## HealthOrb

**Package:** `com.osmb.api.ui.component.minimap.orbs`

**Type:** Class

**Extends/Implements:** extends MinimapOrb

### Methods

#### `getIcons()`

**Returns:** `int[]`

#### `getActivatedUnderlay()`

**Returns:** `int`

#### `getMinimapOffset()`

**Returns:** `Point`


---

## Minimap

**Package:** `com.osmb.api.ui.minimap`

**Type:** Class

### Fields

- `static final int RADIUS`

### Methods

#### `getLastArrowResult()`

**Returns:** `MinimapArrowResult`

#### `arrowDetectionEnabled(boolean enabled)`

#### `isArrowDetectionEnabled()`

**Returns:** `boolean`

#### `positionToMinimapClamped(WorldPosition playerPosition, WorldPosition targetPos)`

**Returns:** `Point`

#### `positionToMinimap(WorldPosition playerPosition, WorldPosition targetPos)`

**Returns:** `Rectangle`

#### `getCenter()`

**Returns:** `UIResult<Point>`

#### `clampToMinimap(int x, int y)`

**Returns:** `Point`

#### `insideMinimap(int x, int y)`

**Returns:** `boolean`

#### `getMinimapImage(boolean cleanDynamicEntities)`

**Returns:** `Image`

#### `getPlayerPositions()`

**Returns:** `UIResultList<WorldPosition>`

Returns a list containing the coordinates of each player(white) dot seen on the minimap.

**Returns:** List

#### `getNPCPositions()`

**Returns:** `UIResultList<WorldPosition>`

Returns a list containing the coordinates of each NPC(yellow) dot seen on the minimap.

**Returns:** List

#### `getItemPositions()`

**Returns:** `UIResultList<WorldPosition>`

Returns a list containing the coordinates of each item(red) dot seen on the minimap.

**Returns:** List


---

## MinimapArrowResult

**Package:** `com.osmb.api.ui.minimap`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getFoundTime()`

**Returns:** `Timer`

#### `getPosition()`

**Returns:** `WorldPosition`


---

## MinimapComponent

**Package:** `com.osmb.api.ui.component.minimap`

**Type:** Class

**Extends/Implements:** extends ComponentParent<Integer> implements Minimap, Compass

### Fields

- `public static final int[] FLAG_PALLET`
- `public static final int[] ARROW_PALLET`
- `public static final int[] SHIP_PALLET`
- `public static final int[][] PALLETS`
- `public static final int[][] ARROW_PIXELS`

### Methods

#### `getLastArrowResult()`

**Returns:** `MinimapArrowResult`

#### `setLastArrowResult(MinimapArrowResult lastArrowResult)`

#### `arrowDetectionEnabled(boolean enabled)`

#### `isArrowDetectionEnabled()`

**Returns:** `boolean`

#### `getScreenArea()`

**Returns:** `ComponentParent.ScreenArea`

#### `positionToMinimapClamped(WorldPosition playerPosition, WorldPosition targetPos)`

**Returns:** `Point`

#### `positionToMinimap(WorldPosition playerPosition, WorldPosition position)`

**Returns:** `Rectangle`

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<Integer>>`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `getComponentGameState()`

**Returns:** `GameState`

#### `onFound(ImageSearchResult result, int iconID, ComponentImage foundImage)`

**Returns:** `ComponentSearchResult`

#### `getCenter()`

**Returns:** `UIResult<Point>`

#### `clampToMinimap(int x, int y)`

**Returns:** `Point`

#### `insideMinimap(int x, int y)`

**Returns:** `boolean`

#### `insideMinimap(Point center, int x, int y)`

**Returns:** `boolean`

#### `getMinimapImage(boolean cleanDynamicEntities)`

**Returns:** `Image`

#### `isBlackMap(Image image)`

**Returns:** `boolean`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getMinimapBounds()`

**Returns:** `Rectangle`

#### `getPlayerPositions()`

**Returns:** `UIResultList<WorldPosition>`

Description copied from interface: Minimap

**Returns:** List

#### `getNPCPositions()`

**Returns:** `UIResultList<WorldPosition>`

Description copied from interface: Minimap

**Returns:** List

#### `getItemPositions()`

**Returns:** `UIResultList<WorldPosition>`

Description copied from interface: Minimap

**Returns:** List

#### `getRotation()`

**Returns:** `UIResult<Integer>`

**Returns:** the rotation angle in degrees of the compass. Will return empty if the compass is not visible.

#### `setDirection(Compass.Direction direction)`

#### `findArrowPosition()`

**Returns:** `UIResult<WorldPosition>`

#### `getCompassImage(int angle)`

**Returns:** `Image`

#### `getCompassCenter()`

**Returns:** `UIResult<Point>`


---

## MinimapOrb

**Package:** `com.osmb.api.ui.component.minimap.orbs`

**Type:** Class

**Extends/Implements:** extends ComponentChild<ComponentButtonStatus>

### Methods

#### `main(String[] args)`

#### `findIcon(Rectangle containerBounds)`

**Returns:** `UIResult<Integer>`

#### `getIcons()`

**Returns:** `int[]`

#### `getActivatedUnderlay()`

**Returns:** `int`

#### `getMinimapOffset()`

**Returns:** `Point`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<ComponentButtonStatus>>`

#### `getValue()`

**Returns:** `Integer`

#### `getPercentage()`

**Returns:** `Integer`

#### `isActivated()`

**Returns:** `Boolean`

#### `setActivated(boolean activated, boolean bypassHumanDelay)`

**Returns:** `boolean`

#### `getParentOffsets()`

**Returns:** `Map<ComponentButtonStatus,Point>`

#### `getTappableBounds()`

**Returns:** `Rectangle`

#### `getComponentGameState()`

**Returns:** `GameState`


---

## MinimapOrbs

**Package:** `com.osmb.api.ui.minimap`

**Type:** Class

### Methods

#### `getHitpoints()`

**Returns:** `Integer`

Gets the player's current hitpoints, reading directly from the health orb. Note: This is a read-only method and does not update game frames.

**Returns:** the player's current hitpoints, or null if the health orb is not visible

#### `getHitpointsPercentage()`

**Returns:** `Integer`

Gets the player's current hitpoints percentage, reading directly from the health orb. Note: This is a read-only method and does not update game frames.

**Returns:** the player's current hitpoints percentage, or null if the health orb is not visible

#### `getPrayerPointsPercentage()`

**Returns:** `Integer`

Gets the player's current prayer points percentage, reading directly from the prayer orb. Note: This is a read-only method and does not update game frames.

**Returns:** the player's current prayer points percentage, or null if the prayer orb is not visible

#### `getPrayerPoints()`

**Returns:** `Integer`

Gets the player's current prayer points, reading directly from the prayer orb. Note: This is a read-only method and does not update game frames.

**Returns:** the player's current prayer points, or null if the prayer orb is not visible

#### `getRunEnergy()`

**Returns:** `Integer`

Gets the player's current run energy, reading directly from the run orb. Note: This is a read-only method and does not update game frames.

**Returns:** the player's current run energy, or null if the run orb is not visible

#### `hasStaminaEffect()`

**Returns:** `Boolean`

Checks if the player currently has a stamina effect active, reading directly from the run orb. Note: This is a read-only method and does not update game frames.

**Returns:** true if a stamina effect is active, false if not, or null if the run orb is not visible

#### `isRunEnabled()`

**Returns:** `Boolean`

Checks if running is currently enabled, reading directly from the run orb. Note: This is a read-only method and does not update game frames.

**Returns:** true if running is enabled, false if not, or null if the run orb is not visible

#### `getSpecialAttackPercentage()`

**Returns:** `Integer`

Gets the player's current special attack energy percentage, reading directly from the special attack orb. Note: This is a read-only method and does not update game frames.

**Returns:** the special attack energy percentage, or null if the special attack orb is not visible

#### `isSpecialAttackActivated()`

**Returns:** `Boolean`

Checks if the special attack is currently activated, reading directly from the special attack orb. Note: This is a read-only method and does not update game frames.

**Returns:** true if special attack is activated, false if not, or null if the special attack orb is not visible

#### `isQuickPrayersActivated()`

**Returns:** `Boolean`

Checks if quick prayers are currently activated, reading directly from the prayer orb. Note: This is a read-only method and does not update game frames.

**Returns:** true if quick prayers are activated, false if not, or null if the prayer orb is not visible

#### `setQuickPrayers(boolean enabled)`

**Returns:** `boolean`

Toggles quick prayers and waits for confirmation. Note: This method updates game frames while running and waits until the UI reflects the requested state (or an internal timeout elapses). Returns false if the control is not visible, cannot be tapped, or the state did not confirm in time.

**Parameters:**
- `enabled` - true to enable, false to disable

**Returns:** true if the state was confirmed, otherwise false

#### `setQuickPrayers(boolean activated, boolean bypassHumanDelay)`

**Returns:** `boolean`

Toggles quick prayers and waits for confirmation, with optional human-like delay before tapping. Note: This method updates game frames while running and waits until the UI reflects the requested state (or an internal timeout elapses). Returns false if the control is not visible, cannot be tapped, or the state did not confirm in time.

**Parameters:**
- `activated` - true to enable, false to disable
- `bypassHumanDelay` - true to skip the human-like pre-tap delay

**Returns:** true if the state was confirmed, otherwise false

#### `setSpecialAttack(boolean enabled)`

**Returns:** `boolean`

Toggles special attack and waits for confirmation. Note: This method updates game frames while running and waits until the UI reflects the requested state (or an internal timeout elapses). Returns false if the control is not visible, cannot be tapped, or the state did not confirm in time.

**Parameters:**
- `enabled` - true to enable, false to disable

**Returns:** true if the state was confirmed, otherwise false

#### `setSpecialAttack(boolean activated, boolean bypassHumanDelay)`

**Returns:** `boolean`

Toggles special attack and waits for confirmation, with optional human-like delay before tapping. Note: This method updates game frames while running and waits until the UI reflects the requested state (or an internal timeout elapses). Returns false if the control is not visible, cannot be tapped, or the state did not confirm in time.

**Parameters:**
- `activated` - true to enable, false to disable
- `bypassHumanDelay` - true to skip the human-like pre-tap delay

**Returns:** true if the state was confirmed, otherwise false

#### `setRun(boolean enabled)`

**Returns:** `boolean`

Toggles run and waits for confirmation. Note: This method updates game frames while running and waits until the UI reflects the requested state (or an internal timeout elapses). Returns false if the control is not visible, cannot be tapped, or the state did not confirm in time.

**Parameters:**
- `enabled` - true to enable running, false to disable

**Returns:** true if the state was confirmed, otherwise false

#### `setRun(boolean enabled, boolean bypassHumanDelay)`

**Returns:** `boolean`

Toggles run and waits for confirmation, with optional human-like delay before tapping. Note: This method updates game frames while running and waits until the UI reflects the requested state (or an internal timeout elapses). Returns false if the control is not visible, cannot be tapped, or the state did not confirm in time.

**Parameters:**
- `enabled` - true to enable running, false to disable
- `bypassHumanDelay` - true to skip the human-like pre-tap delay

**Returns:** true if the state was confirmed, otherwise false


---

## OrbManager

**Package:** `com.osmb.api.ui.component.minimap.orbs`

**Type:** Class

**Extends/Implements:** extends Object implements MinimapOrbs

### Methods

#### `getHitpoints()`

**Returns:** `Integer`

Description copied from interface: MinimapOrbs

**Returns:** the player's current hitpoints, or null if the health orb is not visible

#### `getHitpointsPercentage()`

**Returns:** `Integer`

Description copied from interface: MinimapOrbs

**Returns:** the player's current hitpoints percentage, or null if the health orb is not visible

#### `getPrayerPointsPercentage()`

**Returns:** `Integer`

Description copied from interface: MinimapOrbs

**Returns:** the player's current prayer points percentage, or null if the prayer orb is not visible

#### `getPrayerPoints()`

**Returns:** `Integer`

Description copied from interface: MinimapOrbs

**Returns:** the player's current prayer points, or null if the prayer orb is not visible

#### `getRunEnergy()`

**Returns:** `Integer`

Description copied from interface: MinimapOrbs

**Returns:** the player's current run energy, or null if the run orb is not visible

#### `hasStaminaEffect()`

**Returns:** `Boolean`

Description copied from interface: MinimapOrbs

**Returns:** true if a stamina effect is active, false if not, or null if the run orb is not visible

#### `isRunEnabled()`

**Returns:** `Boolean`

Description copied from interface: MinimapOrbs

**Returns:** true if running is enabled, false if not, or null if the run orb is not visible

#### `getSpecialAttackPercentage()`

**Returns:** `Integer`

Description copied from interface: MinimapOrbs

**Returns:** the special attack energy percentage, or null if the special attack orb is not visible

#### `isSpecialAttackActivated()`

**Returns:** `Boolean`

Description copied from interface: MinimapOrbs

**Returns:** true if special attack is activated, false if not, or null if the special attack orb is not visible

#### `isQuickPrayersActivated()`

**Returns:** `Boolean`

Description copied from interface: MinimapOrbs

**Returns:** true if quick prayers are activated, false if not, or null if the prayer orb is not visible

#### `setQuickPrayers(boolean activated)`

**Returns:** `boolean`

Description copied from interface: MinimapOrbs

**Parameters:**
- `activated` - true to enable, false to disable

**Returns:** true if the state was confirmed, otherwise false

#### `setQuickPrayers(boolean activated, boolean bypassHumanDelay)`

**Returns:** `boolean`

Description copied from interface: MinimapOrbs

**Parameters:**
- `activated` - true to enable, false to disable
- `bypassHumanDelay` - true to skip the human-like pre-tap delay

**Returns:** true if the state was confirmed, otherwise false

#### `setSpecialAttack(boolean activated)`

**Returns:** `boolean`

Description copied from interface: MinimapOrbs

**Parameters:**
- `activated` - true to enable, false to disable

**Returns:** true if the state was confirmed, otherwise false

#### `setSpecialAttack(boolean activated, boolean bypassHumanDelay)`

**Returns:** `boolean`

Description copied from interface: MinimapOrbs

**Parameters:**
- `activated` - true to enable, false to disable
- `bypassHumanDelay` - true to skip the human-like pre-tap delay

**Returns:** true if the state was confirmed, otherwise false

#### `setRun(boolean enabled)`

**Returns:** `boolean`

Description copied from interface: MinimapOrbs

**Parameters:**
- `enabled` - true to enable running, false to disable

**Returns:** true if the state was confirmed, otherwise false

#### `setRun(boolean enabled, boolean bypassHumanDelay)`

**Returns:** `boolean`

Description copied from interface: MinimapOrbs

**Parameters:**
- `enabled` - true to enable running, false to disable
- `bypassHumanDelay` - true to skip the human-like pre-tap delay

**Returns:** true if the state was confirmed, otherwise false


---

## PrayerOrb

**Package:** `com.osmb.api.ui.component.minimap.orbs`

**Type:** Class

**Extends/Implements:** extends MinimapOrb

### Methods

#### `getIcons()`

**Returns:** `int[]`

#### `getActivatedUnderlay()`

**Returns:** `int`

#### `getMinimapOffset()`

**Returns:** `Point`

#### `isActivated()`

**Returns:** `Boolean`


---

## RunOrb

**Package:** `com.osmb.api.ui.component.minimap.orbs`

**Type:** Class

**Extends/Implements:** extends MinimapOrb

### Methods

#### `getIcons()`

**Returns:** `int[]`

#### `getActivatedUnderlay()`

**Returns:** `int`

#### `getMinimapOffset()`

**Returns:** `Point`

#### `hasStaminaEffect()`

**Returns:** `Boolean`


---

## SpecialAttackOrb

**Package:** `com.osmb.api.ui.component.minimap.orbs`

**Type:** Class

**Extends/Implements:** extends MinimapOrb

### Methods

#### `getIcons()`

**Returns:** `int[]`

#### `getActivatedUnderlay()`

**Returns:** `int`

#### `getMinimapOffset()`

**Returns:** `Point`


---

## XPDropsComponent

**Package:** `com.osmb.api.ui.component.minimap.xpcounter`

**Type:** Class

**Extends/Implements:** extends ComponentChild<Integer> implements Expandable

### Methods

#### `getParentOffsets()`

**Returns:** `Map<Integer,Point>`

#### `buildBackgrounds()`

**Returns:** `List<ComponentImage<Integer>>`

#### `getPopUpBounds()`

**Returns:** `Rectangle`

#### `buildIcons()`

**Returns:** `Map<Integer,SearchableImage>`

#### `isOpen()`

**Returns:** `boolean`

#### `open()`

**Returns:** `boolean`

#### `close()`

**Returns:** `boolean`

#### `isVisible()`

**Returns:** `boolean`

#### `getBounds()`

**Returns:** `Rectangle`


---

