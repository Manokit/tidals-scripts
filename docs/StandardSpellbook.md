# StandardSpellbook Enum

**Package:** `com.osmb.api.ui.spellbook`

**Type:** Enum

**Implements:** `Spell`, `Serializable`, `Comparable<StandardSpellbook>`, `Constable`

## Overview

The `StandardSpellbook` enum represents all spells available in the standard spellbook in Old School RuneScape. Each enum constant implements the `Spell` interface.

## Methods

### `values()`
Returns an array containing all spell constants in declaration order.

**Returns:** `StandardSpellbook[]`

### `valueOf(String name)`
Returns the spell constant with the specified name.

**Parameters:** 
- `name` - The name of the spell constant (must match exactly)

**Returns:** `StandardSpellbook`

**Throws:**
- `IllegalArgumentException` - if no constant with the specified name exists
- `NullPointerException` - if the argument is null

### `getSpriteIDs()`
Returns the sprite IDs associated with this spell.

**Returns:** `int[]`

**Specified by:** `Spell.getSpriteIDs()`

### `getName()`
Returns the display name of the spell.

**Returns:** `String`

**Specified by:** `Spell.getName()`

## Spell Constants

### Combat Spells

#### Strike Spells
- `WIND_STRIKE`
- `WATER_STRIKE`
- `EARTH_STRIKE`
- `FIRE_STRIKE`

#### Bolt Spells
- `WIND_BOLT`
- `WATER_BOLT`
- `EARTH_BOLT`
- `FIRE_BOLT`

#### Blast Spells
- `WIND_BLAST`
- `WATER_BLAST`
- `EARTH_BLAST`
- `FIRE_BLAST`

#### Wave Spells
- `WIND_WAVE`
- `WATER_WAVE`
- `EARTH_WAVE`
- `FIRE_WAVE`

#### Surge Spells
- `WIND_SURGE`
- `WATER_SURGE`
- `EARTH_SURGE`
- `FIRE_SURGE`

#### Special Combat Spells
- `CRUMBLE_UNDEAD`
- `IBAN_BLAST`
- `MAGIC_DART`
- `SARADOMIN_STRIKE`
- `CLAWS_OF_GUTHIX`
- `FLAMES_OF_ZAMORAK`

### Binding & Curse Spells
- `BIND`
- `SNARE`
- `ENTANGLE`
- `CONFUSE`
- `WEAKEN`
- `CURSE`
- `VULNERABILITY`
- `ENFEEBLE`
- `STUN`

### Teleport Spells

#### Standard Teleports
- `VARROCK_TELEPORT`
- `LUMBRIDGE_TELEPORT`
- `FALADOR_TELEPORT`
- `CAMELOT_TELEPORT`
- `ARDOUGNE_TELEPORT`
- `WATCHTOWER_TELEPORT`
- `TROLLHEIM_TELEPORT`
- `APE_ATOLL_TELEPORT`
- `KOUREND_TELEPORT`
- `CIVITAS_ILLA_FORTIS_TELEPORT`

#### Special Teleports
- `TELEPORT_TO_HOUSE`
- `TELEPORT_TO_TARGET`

#### Teleother Spells
- `TELEOTHER_LUMBRIDGE`
- `TELEOTHER_FALADOR`
- `TELEOTHER_CAMELOT`

#### Blocking Spells
- `TELE_BLOCK`

### Utility Spells

#### Alchemy
- `LOW_ALCHEMY`
- `HIGH_LEVEL_ALCHEMY`

#### Item Transformation
- `BONES_TO_BANANAS`
- `BONES_TO_PEACHES`
- `SUPERHEAT_ITEM`

#### Orb Charging
- `CHARGE_WATER_ORB`
- `CHARGE_EARTH_ORB`
- `CHARGE_FIRE_ORB`
- `CHARGE_AIR_ORB`

#### Other Utility
- `TELEKINETIC_GRAB`
- `CHARGE`

### Enchantment Categories
- `JEWELLERY_ENCHANTMENTS`
- `CROSSBOW_BOLT_ENCHANTMENTS`

## Usage Example

```java
// Get a specific spell
StandardSpellbook spell = StandardSpellbook.HIGH_LEVEL_ALCHEMY;

// Get the spell name
String name = spell.getName();

// Get sprite IDs
int[] spriteIds = spell.getSpriteIDs();

// Iterate through all spells
for (StandardSpellbook s : StandardSpellbook.values()) {
    System.out.println(s.getName());
}

// Get spell by name
StandardSpellbook varrock = StandardSpellbook.valueOf("VARROCK_TELEPORT");
```

## Notes

- The `JEWELLERY_ENCHANTMENTS` and `CROSSBOW_BOLT_ENCHANTMENTS` constants represent categories of enchantment spells rather than individual spells
- All spell constants follow SCREAMING_SNAKE_CASE naming convention
- Spell names in the constant match their in-game counterparts with underscores replacing spaces
