# TabManager

**Type:** Interface

**All Known Implementing Classes:** TabManagerService

Manages operations related to tabs and their linked containers. Provides methods to query the active tab, close the tab container, and switch between tabs with optional behavior to mimic user interactions.

## Methods

| Return Type | Method |
|------------|--------|
| `boolean` | `closeContainer()` |
| `Tab.Type` | `getActiveTab()` |
| `Tab` | `getActiveTabComponent()` |
| `Tab` | `getTabComponent(Tab.Type)` |
| `boolean` | `openTab(Tab.Type type)` |

## Method Details

### getActiveTabComponent
```java
Tab getActiveTabComponent()
```

Retrieves the currently active tab component.

**Returns:** the active `Tab`, or `null` if no tab is active.

### getActiveTab
```java
Tab.Type getActiveTab()
```

Retrieves the currently active tab.

**Returns:** the active `Tab.Type`, or `null` if no tab is active.

### getTabComponent
```java
Tab getTabComponent(Tab.Type)
```

**Returns:** the `Tab` equivalent for the provided `Tab.Type`.

### closeContainer
```java
boolean closeContainer()
```

Closes the container linked to the active tab.

If no tabs are linked to the container, a random tab is selected, then deselected to ensure the container becomes inactive.

**Returns:** `true` if the container was closed successfully, `false` otherwise.

### openTab
```java
boolean openTab(Tab.Type type)
```

Opens the specified `Tab.Type`.

This method may select random tabs before switching to the target tab as an anti-ban mechanism.

**Parameters:**
- `type` - the tab to open; must not be `null`.

**Returns:** `true` if the tab was successfully opened or was already active, `false` if the operation failed.

**Throws:** `IllegalArgumentException` - if `tab` is `null`.
