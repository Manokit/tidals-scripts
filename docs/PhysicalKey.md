# PhysicalKey

**Type:** Enum

**Implements:** Serializable, Comparable<PhysicalKey>, Constable

## Enum Constants

| Constant |
|----------|
| `BACK` |
| `BACKSPACE` |
| `CTRL_LEFT` |
| `CTRL_RIGHT` |
| `DPAD_CENTER` |
| `DPAD_DOWN` |
| `DPAD_LEFT` |
| `DPAD_RIGHT` |
| `DPAD_UP` |
| `ENTER` |
| `F1` |
| `F2` |
| `F3` |
| `F4` |
| `F5` |
| `F6` |
| `F7` |
| `F8` |
| `F9` |
| `F10` |
| `F11` |
| `F12` |
| `HOME` |
| `MEDIA_PREVIOUS` |
| `SHIFT_LEFT` |
| `SHIFT_RIGHT` |
| `SPACE` |
| `TAB` |

## Methods

| Return Type | Method |
|------------|--------|
| `String` | `getAndroidKeyCode()` |
| `Integer` | `getLinuxKeyCode()` |
| `Integer` | `getWindowsKeyCode()` |
| `static PhysicalKey` | `valueOf(String name)` |
| `static PhysicalKey[]` | `values()` |

### Inherited Methods from Enum

clone, compareTo, describeConstable, equals, finalize, getDeclaringClass, hashCode, name, ordinal, toString, valueOf

## Enum Constant Details

### HOME
```java
public static final PhysicalKey HOME
```

### BACK
```java
public static final PhysicalKey BACK
```

### MEDIA_PREVIOUS
```java
public static final PhysicalKey MEDIA_PREVIOUS
```

### DPAD_UP
```java
public static final PhysicalKey DPAD_UP
```

### DPAD_DOWN
```java
public static final PhysicalKey DPAD_DOWN
```

### DPAD_LEFT
```java
public static final PhysicalKey DPAD_LEFT
```

### DPAD_RIGHT
```java
public static final PhysicalKey DPAD_RIGHT
```

### DPAD_CENTER
```java
public static final PhysicalKey DPAD_CENTER
```

### ENTER
```java
public static final PhysicalKey ENTER
```

### TAB
```java
public static final PhysicalKey TAB
```

### BACKSPACE
```java
public static final PhysicalKey BACKSPACE
```

### SPACE
```java
public static final PhysicalKey SPACE
```

### F1
```java
public static final PhysicalKey F1
```

### F2
```java
public static final PhysicalKey F2
```

### F3
```java
public static final PhysicalKey F3
```

### F4
```java
public static final PhysicalKey F4
```

### F5
```java
public static final PhysicalKey F5
```

### F6
```java
public static final PhysicalKey F6
```

### F7
```java
public static final PhysicalKey F7
```

### F8
```java
public static final PhysicalKey F8
```

### F9
```java
public static final PhysicalKey F9
```

### F10
```java
public static final PhysicalKey F10
```

### F11
```java
public static final PhysicalKey F11
```

### F12
```java
public static final PhysicalKey F12
```

### CTRL_LEFT
```java
public static final PhysicalKey CTRL_LEFT
```

### CTRL_RIGHT
```java
public static final PhysicalKey CTRL_RIGHT
```

### SHIFT_LEFT
```java
public static final PhysicalKey SHIFT_LEFT
```

### SHIFT_RIGHT
```java
public static final PhysicalKey SHIFT_RIGHT
```

## Method Details

### values
```java
public static PhysicalKey[] values()
```

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

### valueOf
```java
public static PhysicalKey valueOf(String name)
```

Returns the enum constant of this class with the specified name. The string must match _exactly_ an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- `IllegalArgumentException` - if this enum class has no constant with the specified name
- `NullPointerException` - if the argument is null

### getLinuxKeyCode
```java
public Integer getLinuxKeyCode()
```

Returns the Linux evdev key code (KEY_...) integer value, required for binary input injection.

### getAndroidKeyCode
```java
public String getAndroidKeyCode()
```

### getWindowsKeyCode
```java
public Integer getWindowsKeyCode()
```
