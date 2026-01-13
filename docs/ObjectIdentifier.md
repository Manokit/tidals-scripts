# ObjectIdentifier

**Package:** `com.osmb.api.scene`  
**Type:** Class

Lightweight identifier object used to reference a scene object either by **ID** or **name**, paired with a fixed **WorldPosition**.

---

## Class Signature

```java
public class ObjectIdentifier
```
Extends `java.lang.Object`.

---

## Purpose

`ObjectIdentifier` is a small value object used when you need to target or describe a specific in‑game object at a known world position.  
It supports **two identification strategies**:

- Numeric object ID
- String-based object name

Exactly one of `id` or `name` is expected to be meaningful, depending on which constructor is used.

---

## Constructors

### `ObjectIdentifier(int id, WorldPosition position)`

Creates an identifier using a numeric object ID.

```java
ObjectIdentifier identifier =
    new ObjectIdentifier(12345, worldPosition);
```

**Parameters**
- `id` – Object ID
- `position` – World position of the object

---

### `ObjectIdentifier(String name, WorldPosition position)`

Creates an identifier using an object name.

```java
ObjectIdentifier identifier =
    new ObjectIdentifier("Bank booth", worldPosition);
```

**Parameters**
- `name` – Object name
- `position` – World position of the object

---

## Methods

### `int getId()`

```java
int getId()
```

Returns the numeric object ID.

**Notes**
- Likely returns `0` or an invalid value if this instance was created using the name constructor.

---

### `String getName()`

```java
String getName()
```

Returns the object name.

**Notes**
- Likely returns `null` if this instance was created using the ID constructor.

---

### `WorldPosition getPosition()`

```java
WorldPosition getPosition()
```

Returns the world position associated with this identifier.

---

## Usage Patterns

### ID-based targeting
```java
new ObjectIdentifier(11730, player.getWorldPosition());
```

### Name-based targeting
```java
new ObjectIdentifier("Chest", someTile);
```

This class is typically passed into higher-level scene or interaction APIs rather than used directly for logic.

---

## Inherited Methods

All standard `java.lang.Object` methods apply:
`equals`, `hashCode`, `toString`, `wait`, `notify`, etc.

---

## LLM Notes

- Treat this as an **immutable value object**
- Do not assume both `id` and `name` are set
- Always use `getPosition()` when resolving scene queries
