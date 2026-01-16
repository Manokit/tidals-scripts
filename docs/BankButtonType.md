# BankButtonType

**Type:** Enum

**Implements:** BankButtonIdentityType, Serializable, Comparable<BankButtonType>, Constable

## Enum Constants

| Constant |
|----------|
| `CLOSE` |
| `DEPOSIT_EQUIPMENT` |
| `DEPOSIT_INVENTORY` |
| `SEARCH` |
| `WITHDRAW_AS_ITEM` |
| `WITHDRAW_AS_NOTE` |

## Methods

| Return Type | Method |
|------------|--------|
| `Object` | `getIdentifier()` |
| `static BankButtonType` | `valueOf(String name)` |
| `static BankButtonType[]` | `values()` |

### Inherited Methods from Enum

clone, compareTo, describeConstable, equals, finalize, getDeclaringClass, hashCode, name, ordinal, toString, valueOf

## Enum Constant Details

### CLOSE
```java
public static final BankButtonType CLOSE
```

### DEPOSIT_EQUIPMENT
```java
public static final BankButtonType DEPOSIT_EQUIPMENT
```

### DEPOSIT_INVENTORY
```java
public static final BankButtonType DEPOSIT_INVENTORY
```

### SEARCH
```java
public static final BankButtonType SEARCH
```

### WITHDRAW_AS_ITEM
```java
public static final BankButtonType WITHDRAW_AS_ITEM
```

### WITHDRAW_AS_NOTE
```java
public static final BankButtonType WITHDRAW_AS_NOTE
```

## Method Details

### values
```java
public static BankButtonType[] values()
```

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

### valueOf
```java
public static BankButtonType valueOf(String name)
```

Returns the enum constant of this class with the specified name. The string must match _exactly_ an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- `IllegalArgumentException` - if this enum class has no constant with the specified name
- `NullPointerException` - if the argument is null

### getIdentifier
```java
public Object getIdentifier()
```

**Specified by:** getIdentifier in interface BankButtonIdentityType
