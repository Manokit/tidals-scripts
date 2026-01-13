# Finger

**Package:** `com.osmb.api.input`  
**Type:** Interface

Primary abstraction for **touch input simulation**.  
`Finger` represents a virtual finger capable of tapping, touching, and interacting with the game screen, UI elements, items, and shapes using human-like delays and menu selection logic.

---

## Interface Signature

```java
public interface Finger
```

---

## Core Concepts

- **Tap vs Touch**
  - `tap(...)` performs a full press-and-release interaction.
  - `touch(...)` allows specifying a `TouchType` (press, release, move, etc).

- **Human Delay**
  - Many methods accept `humanDelay` to simulate realistic timing.
  - Default overloads usually enable human-like delay automatically.

- **Targeting Modes**
  - Absolute coordinates (`x`, `y`)
  - `Point`
  - `Shape` (random point inside)
  - `ItemSearchResult`
  - Game-screen only interactions (ignores UI overlays)

- **Menu Handling**
  - Automatic text matching
  - Explicit action selection
  - Custom `MenuHook` logic
  - Ability to retrieve the selected `MenuEntry`

---

## State / Telemetry

These methods expose information about the most recent interaction.

### `long getLastTapMillis()`
Returns the timestamp of the last tap in milliseconds.

### `int getLastTapX()`
Returns the x-coordinate of the last tap.

### `int getLastTapY()`
Returns the y-coordinate of the last tap.

### `TouchType getLastTouchType()`
Returns the type of the last touch event.

---

## Basic Taps

### Coordinate-based
```java
finger.tap(320, 450);
finger.tap(new Point(320, 450));
```

### With menu entry matching
```java
finger.tap(320, 450, "Use");
finger.tap(point, "Walk here");
```

Menu matching checks if the menu entry **starts with** the provided text.

---

## Shape-based Taps

Used when an area is known but exact coordinates are not.

```java
finger.tap(shape);
finger.tap(shape, "Open");
finger.tap(shape, menuHook);
```

A random point inside the shape is selected automatically.

---

## Item Interactions

```java
finger.tap(true, itemResult);
finger.tap(true, itemResult, "Drop");
finger.tap(true, itemResult, menuHook);
```

Notes:
- Item must be visible and tappable
- Action matching requires exact matches

---

## Human Delay Control

Most overloads support explicit delay control.

```java
finger.tap(true, x, y);
finger.tap(false, shape);
```

Use `false` only when deterministic timing is required.

---

## Menu Hooks

`MenuHook` allows full control over which menu entry is selected.

```java
finger.tap(true, shape, menuHook);
```

Useful for:
- Conditional logic
- Dynamic menus
- Debugging menu contents

---

## Game Screen Taps

Game screen taps ignore UI overlays and only target the playable area.

```java
finger.tapGameScreen(shape);
finger.tapGameScreen(shape, "Chop");
finger.tapGameScreen("Tree", shape, "Chop down");
```

Fails if:
- Coordinates are covered by UI
- Entity cannot be resolved

---

## Getting Menu Responses

```java
MenuEntry entry =
    finger.tapGetResponse(true, shape);
```

Returns the selected menu entry for inspection or branching logic.

---

## Low-Level Touch

```java
finger.touch(x, y, TouchType.DOWN);
finger.touch(x, y, TouchType.UP);
```

Used for:
- Custom gestures
- Dragging
- Advanced input simulation

---

## LLM Notes

- Prefer `Shape`-based taps when possible
- Use `humanDelay = true` by default
- Menu text matching is prefix-based unless using `MenuHook`
- Game screen taps bypass UI safety checks
- Treat this as the **primary input surface** for automation
