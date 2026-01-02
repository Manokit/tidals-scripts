# OSMB API - Spellbooks & Magic

Spellbook interfaces for all magic types

---

## Classes in this Module

- [Class AncientSpellbook](#class-ancientspellbook) [class]
- [Class ArceuusSpellbook](#class-arceuusspellbook) [class]
- [Class LunarSpellbook](#class-lunarspellbook) [class]
- [Class SpellbookType](#class-spellbooktype) [class]
- [Class StandardSpellbook](#class-standardspellbook) [class]
- [InvalidSpellbookTypeException](#invalidspellbooktypeexception) [class]
- [Spell](#spell) [class]
- [SpellNotFoundException](#spellnotfoundexception) [class]

---

## Class AncientSpellbook

**Package:** `com.osmb.api.ui.spellbook`

**Type:** Class

**Extends/Implements:** extends Enum<AncientSpellbook> implements Spell

### Methods

#### `values()`

**Returns:** `AncientSpellbook[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `AncientSpellbook`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getSpriteIDs()`

**Returns:** `int[]`

#### `getName()`

**Returns:** `String`


---

## Class ArceuusSpellbook

**Package:** `com.osmb.api.ui.spellbook`

**Type:** Class

**Extends/Implements:** extends Enum<ArceuusSpellbook> implements Spell

### Methods

#### `values()`

**Returns:** `ArceuusSpellbook[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `ArceuusSpellbook`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getSpriteIDs()`

**Returns:** `int[]`

#### `getName()`

**Returns:** `String`


---

## Class LunarSpellbook

**Package:** `com.osmb.api.ui.spellbook`

**Type:** Class

**Extends/Implements:** extends Enum<LunarSpellbook> implements Spell

### Methods

#### `values()`

**Returns:** `LunarSpellbook[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `LunarSpellbook`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getName()`

**Returns:** `String`

#### `getSpriteIDs()`

**Returns:** `int[]`


---

## Class SpellbookType

**Package:** `com.osmb.api.ui.spellbook`

**Type:** Class

**Extends/Implements:** extends Enum<SpellbookType>

### Methods

#### `values()`

**Returns:** `SpellbookType[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `SpellbookType`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getSpells()`

**Returns:** `Spell[]`


---

## Class StandardSpellbook

**Package:** `com.osmb.api.ui.spellbook`

**Type:** Class

**Extends/Implements:** extends Enum<StandardSpellbook> implements Spell

### Methods

#### `values()`

**Returns:** `StandardSpellbook[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `StandardSpellbook`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null

#### `getSpriteIDs()`

**Returns:** `int[]`

#### `getName()`

**Returns:** `String`


---

## InvalidSpellbookTypeException

**Package:** `com.osmb.api.ui.spellbook`

**Type:** Class

**Extends/Implements:** extends RuntimeException


---

## Spell

**Package:** `com.osmb.api.ui.spellbook`

**Type:** Class

### Methods

#### `getSpriteIDs()`

**Returns:** `int[]`

#### `getName()`

**Returns:** `String`


---

## SpellNotFoundException

**Package:** `com.osmb.api.ui.spellbook`

**Type:** Class

**Extends/Implements:** extends Exception


---

