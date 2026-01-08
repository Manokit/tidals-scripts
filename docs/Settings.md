# Settings

**Package:** `com.osmb.api.ui.tabs`

**Type:** Interface

**Extends:** `Expandable`

**Implementing Classes:** `SettingsTabComponent`

Interface for interacting with the settings tab. Provides methods to open sub-tabs, set and get brightness and zoom levels, and manage frame rate settings.

## Methods

### setBrightnessLevel()

```java
boolean setBrightnessLevel()
```

Sets the brightness level. This method will handle opening the settings tab and the sub-tab if necessary.

**Returns:** `true` if the settings tab was successfully opened, `false` otherwise.

---

### getBrightnessLevel()

```java
UIResult<Integer> getBrightnessLevel()
```

Gets the current brightness level.

**Warning:** This method is read-only. You will have to handle visibility of the settings tab and sub-tab yourself.

**Returns:** `UIResult` containing the brightness level.

---

### setZoomLevel(int)

```java
boolean setZoomLevel(int level)
```

Sets the zoom level. This method will handle opening the settings tab and the sub-tab if necessary.

**Parameters:**
- `level` - The zoom level to set, typically between 0 and 96.

**Returns:** `true` if the zoom level was successfully set, `false` otherwise.

---

### getZoomLevel()

```java
UIResult<Integer> getZoomLevel()
```

Gets the current zoom level.

**Warning:** This method is read-only. You will have to handle visibility of the settings tab and sub-tab yourself.

**Returns:** `UIResult` containing the zoom level.

---

### openSubTab(SettingsSubTabType)

```java
boolean openSubTab(SettingsTabComponent.SettingsSubTabType subTab)
```

Opens the settings tab and the specified sub-tab.

**Parameters:**
- `subTab` - The sub-tab to open.

**Returns:** `true` if the sub-tab was successfully opened, `false` otherwise.

---

### getFrameRate()

```java
int getFrameRate()
```

**Placeholder:** This method is not implemented yet.

**Returns:** Frame rate value.

---

### setFrameRate(int)

```java
void setFrameRate(int rate)
```

**Placeholder:** This method is not implemented yet.

**Parameters:**
- `rate` - The frame rate to set.

---

## Inherited Methods

### From Expandable

- `close()` - Closes the settings tab
- `isOpen()` - Checks if the settings tab is open
- `open()` - Opens the settings tab

### From Viewable

- `getBounds()` - Gets the bounds of the settings tab
- `isVisible()` - Checks if the settings tab is visible
