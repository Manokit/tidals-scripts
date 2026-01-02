# OSMB API - XP & Item Trackers

XP tracking, item tracking, and other monitoring systems

---

## Classes in this Module

- [XPDropsListener](#xpdropslistener) [class]
- [XPTracker](#xptracker) [class]

---

## XPDropsListener

**Package:** `com.osmb.api.trackers.xpdrops`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getXPTrackers()`

**Returns:** `Map<SkillType,XPTracker>`

#### `checkXP()`


---

## XPTracker

**Package:** `com.osmb.api.trackers.experience`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getXp()`

**Returns:** `double`

#### `setXp(int xp)`

#### `incrementXp(double exp)`

#### `getXpGained()`

**Returns:** `double`

#### `getXpPerHour()`

**Returns:** `int`

#### `getXpForNextLevel()`

**Returns:** `double`

#### `getLevelProgressPercentage()`

**Returns:** `int`

#### `getLevel()`

**Returns:** `int`

#### `getLevelForXp(double exp)`

**Returns:** `int`

#### `timeToNextLevelMillis()`

**Returns:** `long`

#### `timeToNextLevelMillis(double xpPH)`

**Returns:** `long`

#### `timeToNextLevelString()`

**Returns:** `String`

#### `timeToNextLevelString(double xpPH)`

**Returns:** `String`

#### `getExperienceForLevel(int level)`

**Returns:** `int`

#### `getStartXp()`

**Returns:** `int`


---

