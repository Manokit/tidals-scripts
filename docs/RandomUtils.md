# RandomUtils

**Type:** Class

**Extends:** Object

## Constructors

| Constructor |
|-------------|
| `RandomUtils()` |

## Methods

| Return Type | Method |
|------------|--------|
| `static int` | `exponentialRandom(int mean, int min, int max)` |
| `static int` | `gaussianRandom(int min, int max, double mean, double stdev)` |
| `static Point` | `generateRandomPoint(Rectangle rectangle, double stdDev)` |
| `static Point` | `generateRandomPoint(Rectangle rectangle, Point target, double stdDev)` |
| `static Point` | `generateRandomPointScaled(Rectangle rectangle, Point target, double spread)` |
| `static Point` | `generateRandomPointSporadic(Rectangle rectangle, Point target, double spread, double sporadicChance)` |
| `static boolean` | `generateRandomProbability(double minProbability, double maxProbability)` |
| `static int` | `inverseWeightedRandom(int min, int max)` |
| `static int` | `inverseWeightedRandom(int min, int max, double lambda)` |
| `static int` | `triangularRandom(int min, int max, double midpoint)` |
| `static int` | `uniformRandom(int max)` |
| `static int` | `uniformRandom(int min, int max)` |
| `static int` | `weightedRandom(int min, int max)` |
| `static int` | `weightedRandom(int min, int max, double lambda)` |

### Inherited Methods from Object

clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait, wait, wait

## Constructor Details

### RandomUtils
```java
public RandomUtils()
```

## Method Details

### generateRandomPoint
```java
public static Point generateRandomPoint(Rectangle rectangle, double stdDev)
```

Generates a random point around the center of a rectangle.

**Parameters:**
- `rectangle` - The rectangle boundaries we want to generate a point inside.
- `stdDev` - The standard deviation for the normal distribution (controls spread).

**Returns:** A random point within the rectangle, clustered around the center.

---

```java
public static Point generateRandomPoint(Rectangle rectangle, Point target, double stdDev)
```

Generates a random point around a specified target point within a rectangle.

**Parameters:**
- `rectangle` - The rectangle boundaries we want to generate a point inside.
- `target` - The point around which the random point will be clustered.
- `stdDev` - The standard deviation for the normal distribution (controls spread).

**Returns:** A random point within the rectangle, clustered around the target point.

### generateRandomPointScaled
```java
public static Point generateRandomPointScaled(Rectangle rectangle, Point target, double spread)
```

Generates a random point around a specified target point within a rectangle, scaling the spread by the rectangle's dimensions for more natural taps.

**Parameters:**
- `rectangle` - The rectangle boundaries we want to generate a point inside.
- `target` - The point around which the random point will be clustered.
- `spread` - The spread factor (e.g., 0.15 for 15% of width/height).

**Returns:** A random point within the rectangle, clustered around the target point.

### generateRandomPointSporadic
```java
public static Point generateRandomPointSporadic(Rectangle rectangle, Point target, double spread, double sporadicChance)
```

Generates a random point around a specified target point within a rectangle, with occasional points falling outside the standard deviation for added sporadicness.

**Parameters:**
- `rectangle` - The rectangle boundaries we want to generate a point inside.
- `target` - The point around which the random point will be clustered.
- `spread` - The spread factor (e.g., 0.15 for 15% of width/height).
- `sporadicChance` - The probability (0.0-1.0) that the point will be more sporadic.

**Returns:** A random point within the rectangle, sometimes more sporadic.

### exponentialRandom
```java
public static int exponentialRandom(int mean, int min, int max)
```

Generates a random delay time that follows human-like behavior (exponential distribution). This is useful for simulating realistic pauses between actions

The distribution ensures most values cluster near the mean, with occasional longer delays, while respecting the specified min/max bounds.

**Parameters:**
- `mean` - The average delay time (most results will be near this value)
- `min` - The minimum possible delay (inclusive)
- `max` - The maximum possible delay (inclusive)

**Returns:** A random delay time between min and max, exponentially distributed around mean

**Throws:** `IllegalArgumentException` - if mean ≤ 0 or min > max

### weightedRandom
```java
public static int weightedRandom(int min, int max, double lambda)
```

Generates a random number between min and max, with a higher probability of generating lower numbers.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).

**Returns:** A weighted random number.

**Throws:** `IllegalArgumentException` - if mean ≤ 0 or min > max

---

```java
public static int weightedRandom(int min, int max)
```

Generates a random number between min and max, with a higher probability of generating lower numbers.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).

**Returns:** A weighted random number.

**Throws:** `IllegalArgumentException` - if mean ≤ 0 or min > max

### inverseWeightedRandom
```java
public static int inverseWeightedRandom(int min, int max)
```

Generates a random number between min and max, with a higher probability of generating higher numbers.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).

**Returns:** An inverse weighted random number.

---

```java
public static int inverseWeightedRandom(int min, int max, double lambda)
```

Generates a random number between min and max, with a higher probability of generating higher numbers.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).
- `lambda` - The lambda. Decrease lambda to make higher numbers more likely. Increase lambda to make lower numbers more likely.

**Returns:** An inverse weighted random number.

### gaussianRandom
```java
public static int gaussianRandom(int min, int max, double mean, double stdev)
```

Generates a random number between min and max using a Gaussian (normal) distribution.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).
- `mean` - The mean (center) of the distribution.
- `stdev` - The standard deviation (spread) of the distribution.

**Returns:** A Gaussian-distributed random number.

### triangularRandom
```java
public static int triangularRandom(int min, int max, double midpoint)
```

Generates a random number between min and max using a triangular distribution.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).
- `midpoint` - The peak of the distribution (most likely value).

**Returns:** A triangular-distributed random number.

### uniformRandom
```java
public static int uniformRandom(int min, int max)
```

Generates a random number between min and max using a uniform distribution.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).

**Returns:** A uniformly distributed random number.

---

```java
public static int uniformRandom(int max)
```

Generates a random number between 0 and max using a uniform distribution.

**Parameters:**
- `max` - The maximum value (inclusive).

**Returns:** A uniformly distributed random number.

### generateRandomProbability
```java
public static boolean generateRandomProbability(double minProbability, double maxProbability)
```

Generates a random misclick probability within a specified range.

**Parameters:**
- `minProbability` - The minimum probability (e.g., 0.05 for 5%).
- `maxProbability` - The maximum probability (e.g., 0.2 for 20%).

**Returns:** A random probability between minProbability and maxProbability.
