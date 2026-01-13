# EquipmentTabComponent

**Package:** `com.osmb.api.ui.component.tabs`  
**Type:** Class  
**Extends:** `SquareTabComponent`  
**Implements:** `Equipment`, `Tab`, `Component`, `ComponentGlobal`, `UIBoundary`

UI abstraction for interacting with the **Equipment tab**.  
Provides methods to query equipped items, locate them by ID, and interact with them via menu actions.

---

## Class Signature

```java
public class EquipmentTabComponent
    extends SquareTabComponent
    implements Equipment
```

---

## Core Responsibilities

- Represent the Equipment tab UI
- Query equipped items by ID
- Interact with equipped items via context menu
- Slot-level awareness via `Slot` enum

---

## Constructor

### `EquipmentTabComponent(ScriptCore core, Container container)`

```java
EquipmentTabComponent equipment =
    new EquipmentTabComponent(core, container);
```

---

## Item Queries

### `UIResult<ItemSearchResult> findItem(int itemID)`

Searches for a specific equipped item.

```java
equipment.findItem(ItemID.DRAGON_SCIMITAR);
```

---

### `UIResult<ItemSearchResult> findItem(int[] itemIDs)`

Searches for the first equipped item matching any ID.

---

### `UIResult<Boolean> isEquipped(int... itemIDs)`

Checks whether any of the provided item IDs are equipped.

```java
equipment.isEquipped(ItemID.AMULET_OF_GLORY);
```

---

## Interactions

### `boolean interact(int itemID, String menuOption)`

```java
equipment.interact(ItemID.AMULET_OF_GLORY, "Remove");
```

Menu option must match exactly.

---

## Tab Metadata

### `Tab.Type getType()`

Returns `Tab.Type.EQUIPMENT`.

---

### `boolean hiddenWhenTabContainerCollapsed()`

Whether the tab hides when the container is collapsed.

---

## Layout / Icons

### `int[] getIcons()`
Returns icon IDs for the Equipment tab.

### `protected int getIconXOffset()`
X-offset override for icon placement.

---

## Nested Types

### `EquipmentTabComponent.Slot`
Enum representing equipment slots.

---

## Usage Patterns

### Check equipment

```java
if (equipment.isEquipped(ItemID.FIRE_CAPE).get()) {
    // ok
}
```

### Remove item

```java
equipment.interact(ItemID.RING_OF_DUELING, "Remove");
```

---

## LLM Notes

- Prefer `isEquipped` over `findItem` for simple checks
- Equipment != Inventory
- Menu text must match exactly
- Safe to poll frequently

---

## Summary

Use `EquipmentTabComponent` for all worn-item logic.
