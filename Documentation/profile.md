# OSMB API - Profile & AFK

Player profiles and AFK handling

---

## Classes in this Module

- [AFKTime](#afktime) [class]
- [ProfileManager](#profilemanager) [class]
- [WorldProvider](#worldprovider) [class]

---

## AFKTime

**Package:** `com.osmb.api.profile.afk`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getAfkForMillis()`

**Returns:** `long`

#### `setAfkForMillis(long afkForMillis)`

#### `getAfkForRandomisationMillis()`

**Returns:** `long`

#### `setAfkForRandomisationMillis(long afkForRandomisationMillis)`

#### `getWeight()`

**Returns:** `int`

#### `setWeight(int weight)`

#### `getAfkAfterMillis()`

**Returns:** `long`

#### `setAfkAfterMillis(long afkAfterMillis)`

#### `getAfkAfterRandomisationMillis()`

**Returns:** `long`

#### `setAfkAfterRandomisationMillis(long afkAfterRandomisationMillis)`

#### `getLikelihood(int totalWeight)`

**Returns:** `double`

#### `toString()`

**Returns:** `String`


---

## ProfileManager

**Package:** `com.osmb.api.profile`

**Type:** Class

### Methods

#### `hasHopProfile()`

**Returns:** `boolean`

Checks if the user has a hop profile selected.

**Returns:** true if a hop profile is available, false otherwise.

#### `hasBreakProfile()`

**Returns:** `boolean`

Checks if the user has a break profile selected.

**Returns:** true if a break profile is available, false otherwise.

#### `isAFKEnabled()`

**Returns:** `boolean`

Checks if the user has an AFK's selected.

**Returns:** true if an AFK's are enabled, false otherwise.

#### `forceHop()`

**Returns:** `boolean`

Will force the script to initiate a world hop if a hop profile is available. If no hop profile is available, this method will return false.

**Returns:** true if a profile is available, false otherwise.

#### `forceHop(WorldProvider world)`

**Returns:** `boolean`

Will force the script to initiate a world hop if a hop profile is available. If no hop profile is available, this method will return false.

**Parameters:**
- `world` - The world provider to use when hopping.

**Returns:** true if a profile is available, false otherwise.

#### `forceBreak()`

**Returns:** `boolean`

Will force the script to initiate a break if a break profile is available. If no break profile is available, this method will return false.

**Returns:** true if a profile is available, false otherwise.

#### `forceAFK()`

**Returns:** `boolean`

Will force the script to initiate an AFK if AFK's are enabled. If AFK option isn't enabled, this method will return false.

**Returns:** true if AFK's are enabled, false otherwise.

#### `isDueToHop()`

**Returns:** `boolean`

Checks if the current script is due to hop.

**Returns:** true if the script is due to hop, false otherwise.

#### `isDueToBreak()`

**Returns:** `boolean`

Checks if the current script is due to break.

**Returns:** true if the script is due to break, false otherwise.

#### `isDueToAFK()`

**Returns:** `boolean`

Checks if the current script is due to AFK.

**Returns:** true if the script is due to AFK, false otherwise.


---

## WorldProvider

**Package:** `com.osmb.api.profile`

**Type:** Class

### Methods

#### `getWorld(List<World> worlds)`

**Returns:** `World`

Selects a world from the provided list of worlds based on custom criteria. WARNING: Allow for different worlds to be returned, if not the script will get stuck in the case that the provided world is full.

**Parameters:**
- `worlds` - the list of available worlds to choose from

**Returns:** the selected world for the TitleScreenHandler to choose in the World selection menu.


---

