# ComponentChild

**Package:** `com.osmb.api.ui.component`
**Type:** Abstract Class
**Type Parameter:** `<T>` - the type of component status

Base class for UI components that are children of a parent container. Provides foundational methods for component state management and search result retrieval.

---

## Class Signature

```java
public abstract class ComponentChild<T>
```

---

## Core Responsibilities

- Serve as base class for tab components and other UI children
- Provide access to component search results
- Manage component game state

---

## Methods

| Return Type | Method |
|------------|--------|
| `ComponentSearchResult` | `getOriginalResult()` |
| `GameState` | `getComponentGameState()` |

---

## Method Details

### getOriginalResult

```java
public ComponentSearchResult getOriginalResult()
```

Returns the original ComponentSearchResult from the component search. This provides access to the underlying search result data.

---

### getComponentGameState

```java
public GameState getComponentGameState()
```

Returns the game state associated with this component.

---

## Known Subclasses

- `SquareTabComponent` - Base class for square tab UI components
- `Container` - UI container components

---

## Usage Example

```java
// accessing the original search result from a component
ComponentSearchResult result = component.getOriginalResult();
if (result != null && result.isFound()) {
    Rectangle bounds = result.getBounds();
    // use bounds for interaction
}
```

---

## LLM Notes

- `getOriginalResult()` returns the raw search result, useful for accessing underlying component data
- Subclasses may override `getComponentGameState()` for specific game state requirements
- This is typically not used directly - use the specific subclass (e.g., `SquareTabComponent`)

---

## Summary

`ComponentChild` is the abstract base class for UI component hierarchy, providing core search result and game state methods that subclasses build upon.
