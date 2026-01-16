# Keyboard

**Package:** `com.osmb.api.input`  
**Type:** Interface

Unified reference for **keyboard input simulation**.
Designed for LLM ingestion and safe scripting around text entry and key presses.

---

## Purpose

`Keyboard` provides low-level keyboard input.
It is primarily used for:
- typing text into prompts (search, amounts, chat)
- pressing specific physical keys when required

Most scripts should prefer **higher-level UI flows** first.
Use `Keyboard` only when the game explicitly requires keyboard input.

---

## Core Methods

### `void type(String message)`

Types a full string as keyboard input.

```java
keyboard.type("1000");
keyboard.type("yes");
```

Typical uses:
- bank X-quantity prompts
- search fields
- chat input
- numeric entry dialogues

Notes:
- Sends characters sequentially
- Assumes a text input field is already focused
- Does not submit automatically unless the UI handles it

---

### `void pressKey(TouchType touchType, PhysicalKey key)`

Simulates a physical key press or release.

```java
keyboard.pressKey(TouchType.DOWN, PhysicalKey.ENTER);
keyboard.pressKey(TouchType.UP, PhysicalKey.ENTER);
```

Used for:
- confirming inputs (ENTER)
- cancelling (ESC)
- special key-only interactions

Both `DOWN` and `UP` events may be required for a full key press.

---

## Required Context Before Use

Before invoking `Keyboard`, the script or LLM must know:

- Is a **text input currently active**?
- Is this an `ENTER_AMOUNT`, `TEXT_SEARCH`, or similar dialogue?
- Does the UI require ENTER to submit, or auto-submit on type?

Keyboard input without correct context will fail silently.

---

## Usage Patterns

### Enter an amount

```java
keyboard.type("500");
keyboard.pressKey(TouchType.DOWN, PhysicalKey.ENTER);
keyboard.pressKey(TouchType.UP, PhysicalKey.ENTER);
```

---

### Bank search

```java
keyboard.type("coal");
```

---

## LLM Rules (Critical)

- Never type unless a text field is visible
- Prefer UI-specific helpers over raw keyboard input
- Always consider whether ENTER is required
- Do not spam key presses

---

## Summary

> `Keyboard` is a precision tool.

Use it only when the UI explicitly demands keyboard input.
