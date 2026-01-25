# UIResult<T>

**Type:** Class

**Type Parameters:** `T` - Type of the value when found

**Extends:** Object

**Implements:** Result<T>

A result container that tracks both search capability and results for UI components, providing clear distinction between:
1. Component not visible (couldn't attempt search)
2. Component visible with two sub-outcomes:
   - a) Target found (with value)
   - b) Target not found (null)

This solves the ambiguity of knowing whether:
- You should retry (component wasn't visible)
- The search genuinely failed (component was visible but target missing)
- The search succeeded

While technically having two states (NOT_VISIBLE/FOUND), the FOUND state combines both successful and unsuccessful searches when the component was visible.

## Methods

| Return Type | Method |
|------------|--------|
| `T` | `get()` |
| `T` | `getIfFound()` |
| `void` | `ifFound(Consumer<T> consumer)` |
| `boolean` | `isFound()` |
| `boolean` | `isNotFound()` |
| `boolean` | `isNotVisible()` |
| `static <T> UIResult<T>` | `notVisible()` |
| `static <T> UIResult<T>` | `of(T value)` |
| `T` | `orElse(T other)` |
| `T` | `orElseGet(Function<UIResult.State, T> stateFallback)` |
| `String` | `toString()` |

## Method Details

### notVisible
```java
public static <T> UIResult<T> notVisible()
```

Creates a result indicating the search couldn't be performed because the component wasn't visible.

**Type Parameters:**
- `T` - The type parameter for the result value

**Returns:** A UIResult with NOT_VISIBLE state

### of
```java
public static <T> UIResult<T> of(T value)
```

Creates a result from a search attempt (component was visible).

**Type Parameters:**
- `T` - The type parameter for the result value

**Parameters:**
- `value` - The found value (null if not found)

**Returns:** A UIResult with FOUND state

### getIfFound
```java
public T getIfFound()
```

Gets the value if found, otherwise returns null.

**Returns:** The found value or null

### isNotVisible
```java
public boolean isNotVisible()
```

Checks if the component wasn't visible during search.

**Specified by:** isNotVisible in interface Result<T>

**Returns:** true if search couldn't be attempted

### isNotFound
```java
public boolean isNotFound()
```

Checks if the search was performed but found nothing (visible component, null value).

**Specified by:** isNotFound in interface Result<T>

**Returns:** true if component was visible but target wasn't found

### isFound
```java
public boolean isFound()
```

Checks if the target was successfully found.

**Specified by:** isFound in interface Result<T>

**Returns:** true if component was visible AND target was found

### get
```java
public T get()
```

Gets the found value.

**Returns:** The result value

**Throws:** `IllegalStateException` - if component wasn't visible

### orElse
```java
public T orElse(T other)
```

Gets the found value or a default if not available.

**Parameters:**
- `other` - The fallback value

**Returns:** The found value or fallback

### orElseGet
```java
public T orElseGet(Function<UIResult.State, T> stateFallback)
```

Gets the found value or computes a fallback based on state.

**Parameters:**
- `stateFallback` - Function that provides fallback value

**Returns:** The found value or computed fallback

### ifFound
```java
public void ifFound(Consumer<T> consumer)
```

Performs an action if the target was found.

**Parameters:**
- `consumer` - The action to execute

### toString
```java
public String toString()
```

Returns a string representation of the result.

**Overrides:** toString in class Object

**Returns:** Formatted string showing state and value
