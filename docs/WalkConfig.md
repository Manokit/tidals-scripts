# WalkConfig

**Type:** Class

**Extends:** Object

Configuration class for path walking behavior in the walking API. Provides customizable settings for character movement, timing, and execution control.

## Nested Classes

| Type | Class |
|------|-------|
| `static class` | `WalkConfig.Builder` - Builder pattern implementation for creating WalkConfig instances with fluent API. |

## Constructors

| Constructor |
|-------------|
| `WalkConfig()` |

## Methods

| Return Type | Method |
|------------|--------|
| `int` | `getBreakDistance()` |
| `BooleanSupplier` | `getBreakCondition()` |
| `Supplier<WalkConfig>` | `getDoWhileWalking()` |
| `long` | `getMinimapTapDelayMax()` |
| `long` | `getMinimapTapDelayMin()` |
| `int` | `getRunEnergyMaxThreshold()` |
| `int` | `getRunEnergyMinThreshold()` |
| `long` | `getScreenTapDelayMax()` |
| `long` | `getScreenTapDelayMin()` |
| `int` | `getTileRandomisationRadius()` |
| `int` | `getTimeout()` |
| `boolean` | `isAllowInterrupt()` |
| `boolean` | `isEnableRun()` |
| `boolean` | `isHandleContainerObstruction()` |
| `boolean` | `isWalkMinimap()` |
| `boolean` | `isWalkScreen()` |
| `String` | `toString()` - overrides Object.toString() |

## Method Details

### WalkConfig
```java
public WalkConfig()
```

### getRunEnergyMaxThreshold
```java
public int getRunEnergyMaxThreshold()
```

### getRunEnergyMinThreshold
```java
public int getRunEnergyMinThreshold()
```

### isHandleContainerObstruction
```java
public boolean isHandleContainerObstruction()
```

**Returns:** Whether to handle container obstructions during walking (e.g., inventory, skills tab)

### getBreakCondition
```java
public BooleanSupplier getBreakCondition()
```

**Returns:** The break condition supplier that can interrupt path walking

### getDoWhileWalking
```java
public Supplier<WalkConfig> getDoWhileWalking()
```

**Returns:** Supplier for dynamic configuration updates during execution

### isWalkScreen
```java
public boolean isWalkScreen()
```

**Returns:** Whether walking via screen clicks is enabled

### isWalkMinimap
```java
public boolean isWalkMinimap()
```

**Returns:** Whether walking via minimap clicks is enabled

### isAllowInterrupt
```java
public boolean isAllowInterrupt()
```

**Returns:** Whether the walking task can be interrupted by break, afk, or hopping tasks

### getTileRandomisationRadius
```java
public int getTileRandomisationRadius()
```

**Returns:** Radius for randomizing target tile positions (in tiles)

### isEnableRun
```java
public boolean isEnableRun()
```

**Returns:** Whether run energy should be automatically managed

### getTimeout
```java
public int getTimeout()
```

**Returns:** Maximum time in milliseconds to attempt walking before timing out

### getBreakDistance
```java
public int getBreakDistance()
```

**Returns:** Distance from destination at which walking is considered complete (in tiles)

### getMinimapTapDelayMin
```java
public long getMinimapTapDelayMin()
```

**Returns:** Minimum delay between minimap clicks (in milliseconds)

### getMinimapTapDelayMax
```java
public long getMinimapTapDelayMax()
```

**Returns:** Maximum delay between minimap clicks (in milliseconds)

### getScreenTapDelayMin
```java
public long getScreenTapDelayMin()
```

**Returns:** Minimum delay between screen clicks (in milliseconds)

### getScreenTapDelayMax
```java
public long getScreenTapDelayMax()
```

**Returns:** Maximum delay between screen clicks (in milliseconds)

### toString
```java
public String toString()
```

**Overrides:** toString in class Object
