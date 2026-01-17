# Prayer

## PrayerTabComponent

**Type:** Class

**Extends:** SquareTabComponent

**Implements:** Component<ComponentButtonStatus>, ComponentGlobal<ComponentButtonStatus>, UIBoundary, Prayer, Tab

### Nested Classes

**Inherited from Prayer:**
- `Prayer.Type`

**Inherited from Tab:**
- `Tab.Type`

### Fields

| Type | Field |
|------|-------|
| `Map<Prayer.Type, SearchableImage>` | `activePrayerImages` |
| `Map<Prayer.Type, SearchableImage>` | `prayerImages` |

### Constructors

| Constructor |
|-------------|
| `PrayerTabComponent()` |

### Methods

| Return Type | Method |
|------------|--------|
| `List<Prayer.Type>` | `getActivePrayers()` |
| `int[]` | `getIcons()` |
| `Integer` | `getPrayerPoints()` |
| `Tab.Type` | `getType()` |
| `boolean` | `hiddenWhenTabContainerCollapsed()` |
| `boolean` | `setActive(Prayer.Type prayer, boolean active)` |

### Inherited Methods from SquareTabComponent

buildBackgrounds, buildIcons, close, close, findIcon, getBounds, getComponent, getComponentGameState, getContainer, getIconXOffset, getIconYOffset, isOpen, isTabActive, isVisible, main, open, open, transformCroppedBounds

### Inherited Methods from Object

clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait

### Method Details

#### hiddenWhenTabContainerCollapsed
```java
public boolean hiddenWhenTabContainerCollapsed()
```

**Specified by:** hiddenWhenTabContainerCollapsed in class SquareTabComponent

#### getIcons
```java
public int[] getIcons()
```

**Specified by:** getIcons in class SquareTabComponent

#### getActivePrayers
```java
public List<Prayer.Type> getActivePrayers()
```

**Specified by:** getActivePrayers in interface Prayer

#### setActive
```java
public boolean setActive(Prayer.Type prayer, boolean active)
```

**Specified by:** setActive in interface Prayer

#### getPrayerPoints
```java
public Integer getPrayerPoints()
```

**Specified by:** getPrayerPoints in interface Prayer

#### getType
```java
public Tab.Type getType()
```

**Specified by:** getType in interface Tab

---

## Prayer.Type

**Type:** Enum

**Implements:** Serializable, Comparable<Prayer.Type>, Constable

**Enclosing interface:** Prayer

### Enum Constants

| Constant |
|----------|
| `THICK_SKIN` |
| `BURST_OF_STRENGTH` |
| `CLARITY_OF_THOUGHT` |
| `SHARP_EYE` |
| `MYSTIC_WILL` |
| `ROCK_SKIN` |
| `SUPERHUMAN_STRENGTH` |
| `IMPROVED_REFLEXES` |
| `RAPID_RESTORE` |
| `RAPID_HEAL` |
| `PROTECT_ITEM` |
| `HAWK_EYE` |
| `MYSTIC_LORE` |
| `STEEL_SKIN` |
| `ULTIMATE_STRENGTH` |
| `INCREDIBLE_REFLEXES` |
| `PROTECT_FROM_MAGIC` |
| `PROTECT_FROM_MISSILES` |
| `PROTECT_FROM_MELEE` |
| `EAGLE_EYE` |
| `MYSTIC_MIGHT` |
| `RETRIBUTION` |
| `REDEMPTION` |
| `SMITE` |
| `PRESERVE` |
| `CHIVALRY` |
| `PIETY` |
| `RIGOUR` |
| `AUGURY` |

### Methods

| Return Type | Method |
|------------|--------|
| `int` | `getSpriteID()` |
| `static Prayer.Type` | `valueOf(String name)` |
| `static Prayer.Type[]` | `values()` |

### Inherited Methods from Enum

clone, compareTo, describeConstable, equals, finalize, getDeclaringClass, hashCode, name, ordinal, toString, valueOf

### Enum Constant Details

#### THICK_SKIN
```java
public static final Prayer.Type THICK_SKIN
```

#### BURST_OF_STRENGTH
```java
public static final Prayer.Type BURST_OF_STRENGTH
```

#### CLARITY_OF_THOUGHT
```java
public static final Prayer.Type CLARITY_OF_THOUGHT
```

#### SHARP_EYE
```java
public static final Prayer.Type SHARP_EYE
```

#### MYSTIC_WILL
```java
public static final Prayer.Type MYSTIC_WILL
```

#### ROCK_SKIN
```java
public static final Prayer.Type ROCK_SKIN
```

#### SUPERHUMAN_STRENGTH
```java
public static final Prayer.Type SUPERHUMAN_STRENGTH
```

#### IMPROVED_REFLEXES
```java
public static final Prayer.Type IMPROVED_REFLEXES
```

#### RAPID_RESTORE
```java
public static final Prayer.Type RAPID_RESTORE
```

#### RAPID_HEAL
```java
public static final Prayer.Type RAPID_HEAL
```

#### PROTECT_ITEM
```java
public static final Prayer.Type PROTECT_ITEM
```

#### HAWK_EYE
```java
public static final Prayer.Type HAWK_EYE
```

#### MYSTIC_LORE
```java
public static final Prayer.Type MYSTIC_LORE
```

#### STEEL_SKIN
```java
public static final Prayer.Type STEEL_SKIN
```

#### ULTIMATE_STRENGTH
```java
public static final Prayer.Type ULTIMATE_STRENGTH
```

#### INCREDIBLE_REFLEXES
```java
public static final Prayer.Type INCREDIBLE_REFLEXES
```

#### PROTECT_FROM_MAGIC
```java
public static final Prayer.Type PROTECT_FROM_MAGIC
```

#### PROTECT_FROM_MISSILES
```java
public static final Prayer.Type PROTECT_FROM_MISSILES
```

#### PROTECT_FROM_MELEE
```java
public static final Prayer.Type PROTECT_FROM_MELEE
```

#### EAGLE_EYE
```java
public static final Prayer.Type EAGLE_EYE
```

#### MYSTIC_MIGHT
```java
public static final Prayer.Type MYSTIC_MIGHT
```

#### RETRIBUTION
```java
public static final Prayer.Type RETRIBUTION
```

#### REDEMPTION
```java
public static final Prayer.Type REDEMPTION
```

#### SMITE
```java
public static final Prayer.Type SMITE
```

#### PRESERVE
```java
public static final Prayer.Type PRESERVE
```

#### CHIVALRY
```java
public static final Prayer.Type CHIVALRY
```

#### PIETY
```java
public static final Prayer.Type PIETY
```

#### RIGOUR
```java
public static final Prayer.Type RIGOUR
```

#### AUGURY
```java
public static final Prayer.Type AUGURY
```

### Method Details

#### values
```java
public static Prayer.Type[] values()
```

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### valueOf
```java
public static Prayer.Type valueOf(String name)
```

Returns the enum constant of this class with the specified name. The string must match _exactly_ an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- `IllegalArgumentException` - if this enum class has no constant with the specified name
- `NullPointerException` - if the argument is null

#### getSpriteID
```java
public int getSpriteID()
```
