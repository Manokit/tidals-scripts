# Point

**Package:** java.awt

**Type:** Class

**Extends:** Point2D

**Implements:** Serializable, Cloneable

A point representing a location in `(x,y)` coordinate space, specified in integer precision.

**Since:** 1.0

## Fields

| Type | Field |
|------|-------|
| `int` | `x` - The X coordinate of this Point |
| `int` | `y` - The Y coordinate of this Point |

## Constructors

| Constructor |
|-------------|
| `Point()` - Constructs and initializes a point at the origin (0, 0) |
| `Point(int x, int y)` - Constructs and initializes a point at the specified (x,y) location |
| `Point(Point p)` - Constructs and initializes a point with the same location as the specified Point object |

## Methods

| Return Type | Method |
|------------|--------|
| `boolean` | `equals(Object obj)` |
| `Point` | `getLocation()` |
| `double` | `getX()` |
| `double` | `getY()` |
| `void` | `move(int x, int y)` |
| `void` | `setLocation(double x, double y)` |
| `void` | `setLocation(int x, int y)` |
| `void` | `setLocation(Point p)` |
| `String` | `toString()` |
| `void` | `translate(int dx, int dy)` |

## Field Details

### x
```java
public int x
```

The X coordinate of this `Point`. If no X coordinate is set it will default to 0.

**Since:** 1.0

**See Also:** getLocation(), move(int, int)

### y
```java
public int y
```

The Y coordinate of this `Point`. If no Y coordinate is set it will default to 0.

**Since:** 1.0

**See Also:** getLocation(), move(int, int)

## Constructor Details

### Point
```java
public Point()
```

Constructs and initializes a point at the origin (0, 0) of the coordinate space.

**Since:** 1.1

---

```java
public Point(Point p)
```

Constructs and initializes a point with the same location as the specified `Point` object.

**Parameters:**
- `p` - a point

**Since:** 1.1

---

```java
public Point(int x, int y)
```

Constructs and initializes a point at the specified `(x,y)` location in the coordinate space.

**Parameters:**
- `x` - the X coordinate of the newly constructed Point
- `y` - the Y coordinate of the newly constructed Point

**Since:** 1.0

## Method Details

### getX
```java
public double getX()
```

Returns the X coordinate of this `Point2D` in `double` precision.

**Specified by:** getX in class Point2D

**Returns:** the X coordinate of this Point2D.

**Since:** 1.2

### getY
```java
public double getY()
```

Returns the Y coordinate of this `Point2D` in `double` precision.

**Specified by:** getY in class Point2D

**Returns:** the Y coordinate of this Point2D.

**Since:** 1.2

### getLocation
```java
public Point getLocation()
```

Returns the location of this point. This method is included for completeness, to parallel the `getLocation` method of `Component`.

**Returns:** a copy of this point, at the same location

**Since:** 1.1

**See Also:** Component.getLocation(), setLocation(Point), setLocation(int, int)

### setLocation
```java
public void setLocation(Point p)
```

Sets the location of the point to the specified location. This method is included for completeness, to parallel the `setLocation` method of `Component`.

**Parameters:**
- `p` - a point, the new location for this point

**Since:** 1.1

**See Also:** Component.setLocation(Point), getLocation()

---

```java
public void setLocation(int x, int y)
```

Changes the point to have the specified location.

This method is included for completeness, to parallel the `setLocation` method of `Component`. Its behavior is identical with `move(int, int)`.

**Parameters:**
- `x` - the X coordinate of the new location
- `y` - the Y coordinate of the new location

**Since:** 1.1

**See Also:** Component.setLocation(int, int), getLocation(), move(int, int)

---

```java
public void setLocation(double x, double y)
```

Sets the location of this point to the specified double coordinates. The double values will be rounded to integer values. Any number smaller than `Integer.MIN_VALUE` will be reset to `MIN_VALUE`, and any number larger than `Integer.MAX_VALUE` will be reset to `MAX_VALUE`.

**Specified by:** setLocation in class Point2D

**Parameters:**
- `x` - the X coordinate of the new location
- `y` - the Y coordinate of the new location

**See Also:** getLocation()

### move
```java
public void move(int x, int y)
```

Moves this point to the specified location in the `(x,y)` coordinate plane. This method is identical with `setLocation(int, int)`.

**Parameters:**
- `x` - the X coordinate of the new location
- `y` - the Y coordinate of the new location

**See Also:** Component.setLocation(int, int)

### translate
```java
public void translate(int dx, int dy)
```

Translates this point, at location `(x,y)`, by `dx` along the `x` axis and `dy` along the `y` axis so that it now represents the point `(x+dx,y+dy)`.

**Parameters:**
- `dx` - the distance to move this point along the X axis
- `dy` - the distance to move this point along the Y axis

### equals
```java
public boolean equals(Object obj)
```

Determines whether or not two points are equal. Two instances of `Point2D` are equal if the values of their `x` and `y` member fields, representing their position in the coordinate space, are the same.

**Overrides:** equals in class Point2D

**Parameters:**
- `obj` - an object to be compared with this Point2D

**Returns:** `true` if the object to be compared is an instance of Point2D and has the same values; `false` otherwise.

**See Also:** Object.hashCode(), HashMap

### toString
```java
public String toString()
```

Returns a string representation of this point and its location in the `(x,y)` coordinate space. This method is intended to be used only for debugging purposes, and the content and format of the returned string may vary between implementations. The returned string may be empty but may not be `null`.

**Overrides:** toString in class Object

**Returns:** a string representation of this point
