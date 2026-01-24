# Container

**Package:** `com.osmb.api.ui.component.tabs.container`
**Type:** Class
**Extends:** `ComponentChild`

UI container that holds and manages tab components. Containers represent the visual groupings of tabs in the game interface (e.g., the control panel container that holds inventory, equipment, prayer tabs, etc.).

---

## Class Signature

```java
public class Container extends ComponentChild
```

---

## Core Responsibilities

- Group and manage related tab components
- Provide container-level bounds and visibility
- Access to container search results

---

## Methods

| Return Type | Method |
|------------|--------|
| `ComponentSearchResult` | `getResult()` |

---

## Method Details

### getResult

```java
public ComponentSearchResult getResult()
```

**Overrides:** getResult in class ComponentChild

Returns the ComponentSearchResult for this container. This provides access to the container's detected bounds and state.

---

## Usage Context

Containers are typically accessed through tab components:

```java
// get the container from a tab component
Container container = script.getWidgetManager().getInventory().getContainer();

// access the container's search result
ComponentSearchResult result = container.getResult();
if (result != null && result.isFound()) {
    Rectangle containerBounds = result.getBounds();
}
```

---

## Related Classes

- `SquareTabComponent` - Tab components that live within containers
- `TabManager` - Manages tabs and containers via `closeContainer()`
- `EquipmentTabComponent` - Example of a tab that uses Container in its constructor

---

## LLM Notes

- Containers group related tabs together in the UI
- Use `getResult()` to access the container's detected bounds
- `TabManager.closeContainer()` can close an open container
- Most scripts interact with tabs directly rather than containers

---

## Summary

`Container` represents a UI container for tab groupings, providing access to container-level search results and bounds.
