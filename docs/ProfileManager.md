# ProfileManager

**Package:** `com.osmb.api.profile`

Interface for managing bot profiles, including breaks, world hopping, and AFK behavior.

## Methods

### hasHopProfile()

```java
boolean hasHopProfile()
```

Checks if the user has a hop profile selected.

**Returns:** `true` if a hop profile is available, `false` otherwise.

---

### hasBreakProfile()

```java
boolean hasBreakProfile()
```

Checks if the user has a break profile selected.

**Returns:** `true` if a break profile is available, `false` otherwise.

---

### isAFKEnabled()

```java
boolean isAFKEnabled()
```

Checks if the user has AFK's selected.

**Returns:** `true` if AFK's are enabled, `false` otherwise.

---

### forceHop()

```java
boolean forceHop()
```

Will force the script to initiate a world hop if a hop profile is available. If no hop profile is available, this method will return `false`.

**Returns:** `true` if a profile is available, `false` otherwise.

---

### forceHop(WorldProvider)

```java
boolean forceHop(WorldProvider world)
```

Will force the script to initiate a world hop if a hop profile is available. If no hop profile is available, this method will return `false`.

**Parameters:**
- `world` - The world provider to use when hopping.

**Returns:** `true` if a profile is available, `false` otherwise.

---

### forceBreak()

```java
boolean forceBreak()
```

Will force the script to initiate a break if a break profile is available. If no break profile is available, this method will return `false`.

**Returns:** `true` if a profile is available, `false` otherwise.

---

### forceAFK()

```java
boolean forceAFK()
```

Will force the script to initiate an AFK if AFK's are enabled. If AFK option isn't enabled, this method will return `false`.

**Returns:** `true` if AFK's are enabled, `false` otherwise.

---

### isDueToHop()

```java
boolean isDueToHop()
```

Checks if the current script is due to hop.

**Returns:** `true` if the script is due to hop, `false` otherwise.

---

### isDueToBreak()

```java
boolean isDueToBreak()
```

Checks if the current script is due to break.

**Returns:** `true` if the script is due to break, `false` otherwise.

---

### isDueToAFK()

```java
boolean isDueToAFK()
```

Checks if the current script is due to AFK.

**Returns:** `true` if the script is due to AFK, `false` otherwise.
