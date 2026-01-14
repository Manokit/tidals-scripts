# Dialogue

**Package:** `com.osmb.api.ui.chatbox.dialogue`  
**Type:** Interface + Enum Overview  
**Primary Implementation:** `DialogueComponent`

Unified reference for **chatbox dialogues** and their **types**.  
This file is designed for LLM ingestion and scripting correctness.

---

## Purpose

`Dialogue` represents an **active chatbox interaction**.  
Correct usage requires:

1. Detecting if a dialogue is visible
2. Determining the `DialogueType`
3. Performing the *only* valid action for that type
4. Waiting for the dialogue to change or disappear

Failure to branch on type is the #1 cause of broken dialogue scripts.

---

## Dialogue Lifecycle (Required Pattern)

```java
if (dialogue.isVisible()) {
    switch (dialogue.getDialogueType()) {
        case CHAT_DIALOGUE, TAP_HERE_TO_CONTINUE ->
            dialogue.continueChatDialogue();

        case TEXT_OPTION ->
            dialogue.selectOption("Yes");

        case ITEM_OPTION ->
            dialogue.selectItem(ItemID.COINS_995);
    }
}
```

---

## Core Dialogue Methods

### `boolean isVisible()`
Returns `true` if a dialogue is currently present.

Use this as your entry guard.

---

### `DialogueType getDialogueType()`
Returns the detected dialogue type.

All logic **must branch** on this value.

---

### `UIResult<String> getText()`
Returns dialogue text for:
- `CHAT_DIALOGUE`
- `TAP_HERE_TO_CONTINUE`

---

### `UIResult<String> getDialogueTitle()`
Returns the red header text (NPC or speaker name).

---

### `List<String> getOptions()`
Returns options for `TEXT_OPTION` dialogues.

- Ordered top → bottom
- Returns `null` if not applicable

---

### `boolean selectOption(String option)`
Selects a text option from a `TEXT_OPTION` dialogue.

Text must match exactly.

---

### `boolean selectItem(int... itemID)`
Selects an item from an `ITEM_OPTION` dialogue.

Succeeds if *any* ID is found.

---

### `boolean continueChatDialogue()`
Safely advances:
- `CHAT_DIALOGUE`
- `TAP_HERE_TO_CONTINUE`

Automatically waits for the dialogue to update or close.

This is the **preferred** continuation method.

---

### `Rectangle getBounds()`
Returns screen bounds of the dialogue.

Rarely needed directly.

---

## DialogueType (Inline Reference)

`DialogueType` defines **what actions are valid** for a dialogue.

### Common Types (Handle These)

#### `CHAT_DIALOGUE`
Standard NPC dialogue text.

- Read with `getText()`
- Advance with `continueChatDialogue()`

---

#### `TAP_HERE_TO_CONTINUE`
Continuation prompt without options.

- Treated the same as `CHAT_DIALOGUE`
- Use `continueChatDialogue()`

---

#### `TEXT_OPTION`
Dialogue presenting selectable text options.

- Read options via `getOptions()`
- Select using `selectOption(String)`

---

#### `ITEM_OPTION`
Dialogue prompting item selection.

- Select using `selectItem(...)`
- Used in trade, quest, and confirmation flows

---

### Less Common Types (Edge Cases)

#### `TEXT_SEARCH`
Search-based text input or filtering.

#### `ITEM_SEARCH`
Search-based item selection.

#### `ENTER_AMOUNT`
Numeric input prompt.

Usually requires keyboard input handling.

#### `REPORT`
Reporting / moderation dialogue.

Rarely relevant for automation.

---

## LLM Rules (Critical)

- **Always** check `getDialogueType()` before interacting
- Never guess which options or items exist
- Use `continueChatDialogue()` instead of manual clicks
- Dialogue interactions are blocking, wait for state change

---

## Summary

> Dialogue handling is type-driven.

If you don’t know the `DialogueType`, you don’t know what action is safe.
