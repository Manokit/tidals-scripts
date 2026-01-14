# Bank

**Package:** `com.osmb.api.ui.bank`  
**Type:** Interface + Enum Overview

Unified reference for **bank interactions** and **bank UI buttons**, written for LLM ingestion and safe scripting.

---

## Purpose

The `Bank` interface represents an **open bank interface**.

Correct usage requires:
1. Ensuring the bank is visible
2. Performing deposits / withdrawals
3. Managing tabs or quantities if needed
4. Closing the bank when finished

`BankButtonType` describes the UI buttons present in the bank.

---

## Bank Interface

### Visibility

`Bank` extends `Viewable`.

```java
if (!bank.isVisible()) {
    // open bank first
}
```

---

## Core Bank Actions

### `boolean withdraw(int itemID, int amount)`
Withdraws an item into the inventory.

```java
bank.withdraw(ItemID.COINS_995, 1000);
```

---

### `boolean deposit(int itemID, int amount)`
Deposits an inventory item into the bank.

---

### `boolean depositAll(Set<Integer> itemIDsToIgnore)`
Deposits all items except specific item IDs.

---

### `boolean depositAll(Set<Integer> itemIDsToIgnore, Set<Integer> slotsToIgnore)`
Deposits all items except specific item IDs and slots.

---

### `boolean depositAllIgnoreSlots(Set<Integer> slotsToIgnore)`
Deposits all items except those in specified slots.

---

### `int getFreeBankSlots()`
Returns remaining free bank slots.

---

### `boolean close()`
Closes the bank interface.

---

## Bank Tabs & Quantities

### `UIResult<Integer> getSelectedTabIndex()`
Returns the currently selected bank tab.

---

### `boolean setSelectedTabIndex(int index)`
Selects a bank tab.

---

### `int getxQuantity()`
Returns the cached X quantity, or `-1` if not set.

---

## BankButtonType (UI Buttons)

`BankButtonType` identifies clickable UI buttons inside the bank.

### Buttons

- `CLOSE`
- `DEPOSIT_INVENTORY`
- `DEPOSIT_EQUIPMENT`
- `SEARCH`
- `WITHDRAW_AS_ITEM`
- `WITHDRAW_AS_NOTE`

---

## Usage Guidance

- Prefer **Bank methods** over clicking buttons
- Buttons are metadata, not workflow
- Button logic is handled internally by the UI system

---

## LLM Rules

- Never assume the bank is open
- Never mix inventory logic with bank logic
- Use high-level bank methods whenever possible

---

## Summary

Use `Bank` for all bank actions.  
Use `BankButtonType` only when reasoning about UI state.
