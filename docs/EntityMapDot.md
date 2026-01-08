# EntityMapDot

**Package:** `com.osmb.api.ui.minimap`

**Type:** Enum Class

Enum representing different types of entities that can appear as dots on the minimap.

## Enum Constants

### PLAYER

```java
public static final EntityMapDot PLAYER
```

Represents a player dot on the minimap.

---

### ITEM

```java
public static final EntityMapDot ITEM
```

Represents a ground item dot on the minimap.

---

### NPC

```java
public static final EntityMapDot NPC
```

Represents an NPC dot on the minimap.

---

## Methods

### values()

```java
public static EntityMapDot[] values()
```

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** An array containing the constants of this enum class, in the order they are declared.

---

### valueOf(String)

```java
public static EntityMapDot valueOf(String name)
```

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - The name of the enum constant to be returned.

**Returns:** The enum constant with the specified name.

**Throws:**
- `IllegalArgumentException` - If this enum class has no constant with the specified name.
- `NullPointerException` - If the argument is null.
