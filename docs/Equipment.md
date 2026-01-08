# Equipment Interface

**Package:** `com.osmb.api.ui.tabs`

**Type:** Interface

**Implementing Classes:** `EquipmentTabComponent`

## Overview

The `Equipment` interface provides methods for interacting with the equipment tab in Old School RuneScape. It allows you to find items, check if items are equipped, and interact with equipped items.

## Methods

### `findItem(int itemID)`
Finds an item in the equipment tab by its item ID.

**Parameters:**
- `itemID` - The ID of the item to find

**Returns:** `UIResult<ItemSearchResult>` - A UI result containing the item search result if found

---

### `findItem(int... itemIDs)`
Finds any of the specified items in the equipment tab.

**Parameters:**
- `itemIDs` - Variable number of item IDs to search for

**Returns:** `UIResult<ItemSearchResult>` - A UI result containing the first matching item found

---

### `isEquipped(int... itemIDs)`
Checks if any of the specified items are currently equipped.

**Parameters:**
- `itemIDs` - Variable number of item IDs to check

**Returns:** `UIResult<Boolean>` - A UI result containing `true` if any of the items are equipped, `false` otherwise

---

### `interact(int itemID, String menuOption)`
Interacts with an equipped item using the specified menu option.

**Parameters:**
- `itemID` - The ID of the equipped item to interact with
- `menuOption` - The menu action to perform (e.g., "Remove", "Wield", "Operate")

**Returns:** `boolean` - `true` if the interaction was successful, `false` otherwise

## Usage Examples

```java
// Find a specific item in equipment
UIResult<ItemSearchResult> result = equipment.findItem(1234);
if (result.isSuccess()) {
    ItemSearchResult item = result.getValue();
    // Process the item
}

// Find any of several items
UIResult<ItemSearchResult> weaponResult = equipment.findItem(4151, 4153, 11694); // Dragon weapons
if (weaponResult.isSuccess()) {
    // One of the weapons is equipped
}

// Check if an item is equipped
UIResult<Boolean> isWearing = equipment.isEquipped(1163); // Rune full helm
if (isWearing.isSuccess() && isWearing.getValue()) {
    // Player is wearing the helm
}

// Check if any of several items are equipped
UIResult<Boolean> hasWeapon = equipment.isEquipped(4151, 4153, 11694);
if (hasWeapon.isSuccess() && hasWeapon.getValue()) {
    // Player has one of these weapons equipped
}

// Interact with an equipped item
boolean success = equipment.interact(1163, "Remove"); // Remove rune full helm
if (success) {
    // Item was successfully removed
}

// Operate an item
equipment.interact(11283, "Operate"); // Operate dragonfire shield
```

## Equipment Slots

The equipment tab typically contains the following slots:
- Head
- Cape
- Neck
- Ammunition
- Weapon
- Body
- Shield
- Legs
- Hands
- Feet
- Ring

## Notes

- The `findItem()` methods with varargs will return the first matching item found
- `isEquipped()` returns `true` if ANY of the provided item IDs are currently equipped
- `interact()` requires the exact menu option text as it appears in-game
- All methods returning `UIResult` should have their success state checked before accessing the value
- Item IDs can be found in the OSRS Wiki or through the game's API
- Menu options are case-sensitive and must match exactly

## Related Classes

- `EquipmentTabComponent` - The implementing class for this interface
- `UIResult<T>` - Wrapper class for UI operation results
- `ItemSearchResult` - Contains information about found items
