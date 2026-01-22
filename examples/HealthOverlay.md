# HealthOverlay

**Type:** Class

**Extends:** OverlayBoundary

**Implements:** UIBoundary

## Nested Classes

| Type | Class |
|------|-------|
| `static class` | `HealthOverlay.HealthResult` |

## Fields

| Type | Field |
|------|-------|
| `static final String` | `HEALTH` |
| `static final String` | `NPC_NAME` |

## Constructors

| Constructor |
|-------------|
| `HealthOverlay(ScriptCore)` |

## Methods

| Return Type | Method |
|------------|--------|
| `void` | `applyValueFinders()` |
| `protected boolean` | `checkVisibility(Rectangle bounds)` |
| `int` | `getHeight()` |
| `HealthOverlay.HealthResult` | `getHealthResult()` |
| `String` | `getNPCName()` |
| `Point` | `getOverlayOffset()` |
| `OverlayPosition` | `getOverlayPosition()` |
| `int` | `getWidth()` |
| `void` | `onOverlayFound(Rectangle overlayBounds)` |
| `void` | `onOverlayNotFound()` |

### Inherited Methods from Object

clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait

## Field Details

### NPC_NAME
```java
public static final String NPC_NAME
```

### HEALTH
```java
public static final String HEALTH
```

## Constructor Details

### HealthOverlay
```java
public HealthOverlay(ScriptCore)
```

## Method Details

### getWidth
```java
public int getWidth()
```

**Specified by:** getWidth in class OverlayBoundary

### getHeight
```java
public int getHeight()
```

**Specified by:** getHeight in class OverlayBoundary

### checkVisibility
```java
protected boolean checkVisibility(Rectangle bounds)
```

**Specified by:** checkVisibility in class OverlayBoundary

### getOverlayPosition
```java
public OverlayPosition getOverlayPosition()
```

**Specified by:** getOverlayPosition in class OverlayBoundary

### getOverlayOffset
```java
public Point getOverlayOffset()
```

**Specified by:** getOverlayOffset in class OverlayBoundary

### applyValueFinders
```java
public void applyValueFinders()
```

**Specified by:** applyValueFinders in class OverlayBoundary

### getHealthResult
```java
public HealthOverlay.HealthResult getHealthResult()
```

This method retrieves the health information of the NPC, including current and maximum hitpoints. If the health overlay is not visible, it returns null.

**Returns:** The health result containing current and maximum hitpoints, or null if not available.

### getNPCName
```java
public String getNPCName()
```

This method retrieves the name of the NPC from the health overlay. If the health overlay is not visible, it returns null.

**Returns:** The name of the NPC, or null if not available.

### onOverlayFound
```java
public void onOverlayFound(Rectangle overlayBounds)
```

**Specified by:** onOverlayFound in class OverlayBoundary

### onOverlayNotFound
```java
public void onOverlayNotFound()
```

**Specified by:** onOverlayNotFound in class OverlayBoundary
