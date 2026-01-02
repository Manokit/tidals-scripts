# OSMB API - Bank Interface

Bank interface and related components

---

## Classes in this Module

- [Bank](#bank) [class]
- [BankButton](#bankbutton) [class]
- [BankButtonIdentityType](#bankbuttonidentitytype) [class]
- [Class BankButtonType](#class-bankbuttontype) [class]
- [Class QuantityBankButtonType](#class-quantitybankbuttontype) [class]

---

## Bank

**Package:** `com.osmb.api.ui.bank`

**Type:** Class

**Extends/Implements:** extends Viewable, ItemGroup

**Interfaces:** ItemGroup, Viewable

### Methods

#### `withdraw(int itemID, int amount)`

**Returns:** `boolean`

Withdraws an item from the bank into the inventory.

**Parameters:**
- `itemID` - the ID of the item to withdraw.
- `amount` - the amount of the item to withdraw.

**Returns:** true if the withdrawal was successful, false otherwise.

#### `getFreeBankSlots()`

**Returns:** `int`

Gets the available spaces inside the bank.

**Returns:** the number of free bank slots remaining.

#### `deposit(int itemID, int amount)`

**Returns:** `boolean`

Deposits an item from the inventory into the bank.

**Parameters:**
- `itemID` - the ID of the item to deposit.
- `amount` - the amount of the item to deposit.

**Returns:** true if the deposit was successful, false otherwise.

#### `depositAllIgnoreSlots(Set<Integer> slotsToIgnore)`

**Returns:** `boolean`

Deposits all items from the inventory into the bank, ignoring the specified slots.

**Parameters:**
- `slotsToIgnore` - a set of slot indices to ignore during the deposit.

**Returns:** true if all items were successfully deposited, false otherwise.

#### `depositAll(Set<Integer> itemIDsToIgnore)`

**Returns:** `boolean`

Deposits all items from the inventory into the bank, ignoring the specified item IDs.

**Parameters:**
- `itemIDsToIgnore` - a set of item IDs to ignore during the deposit.

**Returns:** true if all items were successfully deposited, false otherwise.

#### `depositAll(Set<Integer> itemsIDsToIgnore, Set<Integer> slotsToIgnore)`

**Returns:** `boolean`

Deposits all items from the inventory into the bank, ignoring both specified item IDs and slots.

**Parameters:**
- `itemsIDsToIgnore` - a set of item IDs to ignore during the deposit.
- `slotsToIgnore` - a set of slot indices to ignore during the deposit.

**Returns:** true if all items were successfully deposited, false otherwise.

#### `getSelectedTabIndex()`

**Returns:** `UIResult<Integer>`

Retrieves the currently selected tab index in the bank interface.

**Returns:** UIResult containing the selected tab index If called & the bank interface is not visible, UIResult.NOT_VISIBLE will be returned.

#### `setSelectedTabIndex(int index)`

**Returns:** `boolean`

Sets the selected tab index in the bank interface.

**Parameters:**
- `index` - the index of the tab to select.

**Returns:** true if the tab was successfully selected, false otherwise.

#### `close()`

**Returns:** `boolean`

Closes the bank interface.

**Returns:** true if the bank was successfully closed, false otherwise.

#### `getxQuantity()`

**Returns:** `int`

Gets the cached x quantity selected. Will return -1 if the x quantity hasn't been set yet.

**Returns:** the cached x quantity, or -1 if not set.


---

## BankButton

**Package:** `com.osmb.api.ui.bank`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getSelectedImage()`

**Returns:** `SearchableImage`

#### `getUnselectedImage()`

**Returns:** `SearchableImage`

#### `getBounds()`

**Returns:** `Rectangle`

#### `getScreenBounds(Rectangle bankBounds)`

**Returns:** `Rectangle`


---

## BankButtonIdentityType

**Package:** `com.osmb.api.ui.bank`

**Type:** Class

### Methods

#### `getIdentifier()`

**Returns:** `Object`


---

## Class BankButtonType

**Package:** `com.osmb.api.ui.bank`

**Type:** Class

**Extends/Implements:** extends Enum<BankButtonType> implements BankButtonIdentityType

### Methods

#### `values()`

**Returns:** `BankButtonType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `BankButtonType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getIdentifier()`

**Returns:** `Object`


---

## Class QuantityBankButtonType

**Package:** `com.osmb.api.ui.bank`

**Type:** Class

**Extends/Implements:** extends Enum<QuantityBankButtonType> implements BankButtonIdentityType

### Methods

#### `values()`

**Returns:** `QuantityBankButtonType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `QuantityBankButtonType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getWithdrawlAmount(int amount)`

**Returns:** `QuantityBankButtonType`

#### `getAmount()`

**Returns:** `int`

#### `getIdentifier()`

**Returns:** `String`


---

