# SquareTabComponent

**Type:** Abstract Class

**Implements:** Component<ComponentButtonStatus>, ComponentGlobal<ComponentButtonStatus>, UIBoundary, Tab

**Direct Known Subclasses:** AccountTabComponent, ClanTabComponent, CombatTabComponent, EmoteTabComponent, EquipmentTabComponent, ExpandCollapseTabComponent, FriendsTabComponent, InventoryTabComponent, MusicTabComponent, PrayerTabComponent, QuestTabComponent, SailingTabComponent, SettingsTabComponent, SkillsTabComponent, SpellbookTabComponent

## Nested Classes

**Nested classes/interfaces inherited from Tab:**
- `Tab.Type`

## Fields

| Type | Field |
|------|-------|
| `ToleranceComparator` | `TOLERANCE_COMPARATOR` |

## Constructors

| Constructor |
|-------------|
| `SquareTabComponent()` |

## Methods

| Return Type | Method |
|------------|--------|
| `abstract SearchableImage[]` | `buildBackgrounds()` |
| `abstract SearchableImage[]` | `buildIcons()` |
| `boolean` | `close()` |
| `boolean` | `close(boolean humanDelay)` |
| `abstract ComponentSearchResult` | `findIcon(Rectangle)` |
| `Rectangle` | `getBounds()` |
| `Component<?>` | `getComponent()` |
| `GameState` | `getComponentGameState()` |
| `Container` | `getContainer()` |
| `protected int` | `getIconXOffset()` |
| `protected int` | `getIconYOffset()` |
| `abstract int[]` | `getIcons()` |
| `ComponentSearchResult` | `getResult()` |
| `abstract boolean` | `hiddenWhenTabContainerCollapsed()` |
| `boolean` | `isOpen()` |
| `boolean` | `isTabActive()` |
| `boolean` | `isVisible()` |
| `static void` | `main(String[] args)` |
| `boolean` | `open()` |
| `boolean` | `open(boolean humanDelay)` |
| `Rectangle` | `transformCroppedBounds(Rectangle)` |

### Inherited Methods from Tab

`getType()`

### Inherited Methods from Object

clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait

## Method Details

### main
```java
public static void main(String[] args)
```

### hiddenWhenTabContainerCollapsed
```java
public abstract boolean hiddenWhenTabContainerCollapsed()
```

### getComponent
```java
public Component<?> getComponent()
```

**Specified by:** getComponent in interface Tab

### transformCroppedBounds
```java
public Rectangle transformCroppedBounds(Rectangle)
```

### getBounds
```java
public Rectangle getBounds()
```

**Specified by:** getBounds in interface Component<ComponentButtonStatus>

**Specified by:** getBounds in interface UIBoundary

### getComponentGameState
```java
public GameState getComponentGameState()
```

**Specified by:** getComponentGameState in interface Component<ComponentButtonStatus>

**Overrides:** getComponentGameState in class ComponentChild<ComponentButtonStatus>

### getContainer
```java
public Container getContainer()
```

**Specified by:** getContainer in interface Tab

### getResult
```java
public ComponentSearchResult getResult()
```

**Overrides:** getResult in class ComponentChild<ComponentButtonStatus>

Returns the ComponentSearchResult for this tab component.

### buildIcons
```java
public abstract SearchableImage[] buildIcons()
```

**Specified by:** buildIcons in interface ComponentGlobal<ComponentButtonStatus>

### findIcon
```java
public abstract ComponentSearchResult findIcon(Rectangle)
```

**Specified by:** findIcon in interface ComponentGlobal<ComponentButtonStatus>

### getIconXOffset
```java
protected int getIconXOffset()
```

### getIconYOffset
```java
protected int getIconYOffset()
```

### getIcons
```java
public abstract int[] getIcons()
```

### buildBackgrounds
```java
public abstract SearchableImage[] buildBackgrounds()
```

**Specified by:** buildBackgrounds in interface ComponentGlobal<ComponentButtonStatus>

### isOpen
```java
public boolean isOpen()
```

**Specified by:** isOpen in interface Tab

### isTabActive
```java
public boolean isTabActive()
```

### open
```java
public boolean open()
```

**Specified by:** open in interface Tab

---

```java
public boolean open(boolean humanDelay)
```

**Specified by:** open in interface Tab

### close
```java
public boolean close()
```

**Specified by:** close in interface Tab

---

```java
public boolean close(boolean humanDelay)
```

**Specified by:** close in interface Tab

### isVisible
```java
public boolean isVisible()
```

**Specified by:** isVisible in interface Component<ComponentButtonStatus>

**Specified by:** isVisible in interface Tab
