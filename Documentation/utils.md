# OSMB API - Utilities

Utility classes for timing, conditions, and common operations

---

## Classes in this Module

- [AppManager](#appmanager) [class]
- [CachedObject<T>](#cachedobjectt) [class]
- [Class TileEdge](#class-tileedge) [class]
- [Class UIResultList.State](#class-uiresultlist.state) [class]
- [DrawableAwt](#drawableawt) [class]
- [ImagePanel](#imagepanel) [class]
- [RandomUtils](#randomutils) [class]
- [Result<T>](#resultt) [class]
- [StageController](#stagecontroller) [class]
- [Stopwatch](#stopwatch) [class]
- [Timer](#timer) [class]
- [UIResult<T>](#uiresultt) [class]
- [UIResultList<T>](#uiresultlistt) [class]
- [Utils](#utils) [class]

---

## AppManager

**Package:** `com.osmb.api.utils`

**Type:** Class

### Methods

#### `restart()`

**Returns:** `boolean`

#### `kill()`

#### `open()`

**Returns:** `boolean`


---

## CachedObject<T>

**Package:** `com.osmb.api.utils`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `getObject()`

**Returns:** `T`

#### `getScreenUUID()`

**Returns:** `UUID`

#### `toString()`

**Returns:** `String`


---

## Class TileEdge

**Package:** `com.osmb.api.utils`

**Type:** Class

**Extends/Implements:** extends Enum<TileEdge>

### Methods

#### `values()`

**Returns:** `TileEdge[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `TileEdge`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## Class UIResultList.State

**Package:** `com.osmb.api.utils`

**Type:** Class

Enum representing possible states of the UIResultList.

**Extends/Implements:** extends Enum<UIResultList.State>

### Methods

#### `values()`

**Returns:** `UIResultList.State[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `UIResultList.State`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## DrawableAwt

**Package:** `com.osmb.api.utils`

**Type:** Class

### Methods

#### `draw(Graphics g)`


---

## ImagePanel

**Package:** `com.osmb.api.utils`

**Type:** Class

**Extends/Implements:** extends JPanel

### Methods

#### `setImage(BufferedImage image)`

#### `paintComponent(Graphics g)`

#### `showInFrame(String frameTitle)`


---

## RandomUtils

**Package:** `com.osmb.api.utils`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `generateRandomPoint(Rectangle rectangle, double stdDev)`

**Returns:** `Point`

Generates a random point around the center of a rectangle.

**Parameters:**
- `rectangle` - The rectangle boundaries we want to generate a point inside.
- `stdDev` - The standard deviation for the normal distribution (controls spread).

**Returns:** A random point within the rectangle, clustered around the center.

#### `generateRandomPoint(Rectangle rectangle, Point target, double stdDev)`

**Returns:** `Point`

Generates a random point around a specified target point within a rectangle.

**Parameters:**
- `rectangle` - The rectangle boundaries we want to generate a point inside.
- `target` - The point around which the random point will be clustered.
- `stdDev` - The standard deviation for the normal distribution (controls spread).

**Returns:** A random point within the rectangle, clustered around the target point.

#### `generateRandomPointScaled(Rectangle rectangle, Point target, double spread)`

**Returns:** `Point`

Generates a random point around a specified target point within a rectangle, scaling the spread by the rectangle's dimensions for more natural taps.

**Parameters:**
- `rectangle` - The rectangle boundaries we want to generate a point inside.
- `target` - The point around which the random point will be clustered.
- `spread` - The spread factor (e.g., 0.15 for 15% of width/height).

**Returns:** A random point within the rectangle, clustered around the target point.

#### `generateRandomPointSporadic(Rectangle rectangle, Point target, double spread, double sporadicChance)`

**Returns:** `Point`

Generates a random point around a specified target point within a rectangle, with occasional points falling outside the standard deviation for added sporadicness.

**Parameters:**
- `rectangle` - The rectangle boundaries we want to generate a point inside.
- `target` - The point around which the random point will be clustered.
- `spread` - The spread factor (e.g., 0.15 for 15% of width/height).
- `sporadicChance` - The probability (0.0-1.0) that the point will be more sporadic.

**Returns:** A random point within the rectangle, sometimes more sporadic.

#### `exponentialRandom(int mean, int min, int max)`

**Returns:** `int`

Generates a random delay time that follows human-like behavior (exponential distribution). This is useful for simulating realistic pauses between actions The distribution ensures most values cluster near the mean, with occasional longer delays, while respecting the specified min/max bounds.

**Parameters:**
- `mean` - The average delay time (most results will be near this value)
- `min` - The minimum possible delay (inclusive)
- `max` - The maximum possible delay (inclusive)

**Returns:** A random delay time between min and max, exponentially distributed around mean

**Throws:**
- IllegalArgumentException - if mean â‰¤ 0 or min > max

#### `weightedRandom(int min, int max, double lambda)`

**Returns:** `int`

Generates a random number between min and max, with a higher probability of generating lower numbers.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).

**Returns:** A weighted random number.

**Throws:**
- IllegalArgumentException - if mean â‰¤ 0 or min > max

#### `weightedRandom(int min, int max)`

**Returns:** `int`

Generates a random number between min and max, with a higher probability of generating lower numbers.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).

**Returns:** A weighted random number.

**Throws:**
- IllegalArgumentException - if mean â‰¤ 0 or min > max

#### `inverseWeightedRandom(int min, int max)`

**Returns:** `int`

Generates a random number between min and max, with a higher probability of generating higher numbers.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).

**Returns:** An inverse weighted random number.

#### `inverseWeightedRandom(int min, int max, double lambda)`

**Returns:** `int`

Generates a random number between min and max, with a higher probability of generating higher numbers.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).
- `lambda` - The lambda. Decrease lambda to make higher numbers more likely. Increase lambda to make lower numbers more likely.

**Returns:** An inverse weighted random number.

#### `gaussianRandom(int min, int max, double mean, double stdev)`

**Returns:** `int`

Generates a random number between min and max using a Gaussian (normal) distribution.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).
- `mean` - The mean (center) of the distribution.
- `stdev` - The standard deviation (spread) of the distribution.

**Returns:** A Gaussian-distributed random number.

#### `triangularRandom(int min, int max, double midpoint)`

**Returns:** `int`

Generates a random number between min and max using a triangular distribution.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).
- `midpoint` - The peak of the distribution (most likely value).

**Returns:** A triangular-distributed random number.

#### `uniformRandom(int min, int max)`

**Returns:** `int`

Generates a random number between min and max using a uniform distribution.

**Parameters:**
- `min` - The minimum value (inclusive).
- `max` - The maximum value (inclusive).

**Returns:** A uniformly distributed random number.

#### `uniformRandom(int max)`

**Returns:** `int`

Generates a random number between 0 and max using a uniform distribution.

**Parameters:**
- `max` - The maximum value (inclusive).

**Returns:** A uniformly distributed random number.

#### `generateRandomProbability(double minProbability, double maxProbability)`

**Returns:** `boolean`

Generates a random misclick probability within a specified range.

**Parameters:**
- `minProbability` - The minimum probability (e.g., 0.05 for 5%).
- `maxProbability` - The maximum probability (e.g., 0.2 for 20%).

**Returns:** A random probability between minProbability and maxProbability.


---

## Result<T>

**Package:** `com.osmb.api.utils`

**Type:** Class

### Methods

#### `isNotVisible()`

**Returns:** `boolean`

#### `isNotFound()`

**Returns:** `boolean`

#### `isFound()`

**Returns:** `boolean`


---

## StageController

**Package:** `com.osmb.api.utils`

**Type:** Class

### Methods

#### `show(javafx.scene.Scene scene, String windowTitle, boolean resizable)`

Displays a modal stage (popup) centered relative to the main application stage. This method blocks the calling thread until the modal stage is closed by the user. The method ensures thread-safety by running the UI changes on the JavaFX Application Thread. It uses a FutureTask to synchronize the calling thread with the modal stage's lifecycle.

**Parameters:**
- `scene` - The Scene to be displayed in the modal stage. Represents the content of the window.
- `windowTitle` - The title of the modal stage's window.
- `resizable` - A boolean indicating whether the modal stage can be resized. Key Features: The modal stage is centered relative to the main application stage. The modal stage is displayed as an application-modal window, ensuring user focus. The calling thread is paused until the modal stage is closed, enabling synchronous control flow. Usage: ScriptStageController controller = new ScriptStageController(mainStage); Scene myScene = new Scene(new VBox(), 400, 300); controller.show(myScene, "Popup Title", true);

**Throws:**
- RuntimeException - if an error occurs while displaying the modal stage.


---

## Stopwatch

**Package:** `com.osmb.api.utils.timing`

**Type:** Class

A simple stopwatch utility for measuring elapsed time. Usage: Instantiate with a duration in milliseconds to start the stopwatch. Call reset(long) to restart the stopwatch with a new duration. Use timeLeft() to get the remaining time in milliseconds. Use hasFinished() to check if the stopwatch has finished. Use getRemainingTimeFormatted() to get the remaining time as a formatted string (HH:mm:ss).

**Extends/Implements:** extends Object

### Fields

- `public long stopTime` - The timestamp (in ms) when the stopwatch will finish.

### Methods

#### `reset(long time)`

Resets the stopwatch to finish after the specified duration.

**Parameters:**
- `time` - Duration in milliseconds.

#### `timeLeft()`

**Returns:** `long`

Returns the remaining time in milliseconds.

**Returns:** Remaining time in milliseconds.

#### `hasFinished()`

**Returns:** `boolean`

Checks if the stopwatch has finished.

**Returns:** true if finished, false otherwise.

#### `getRemainingTimeFormatted()`

**Returns:** `String`

Returns the remaining time formatted as HH:mm:ss.

**Returns:** Remaining time as a formatted string.


---

## Timer

**Package:** `com.osmb.api.utils.timing`

**Type:** Class

A simple timer utility class for measuring elapsed time in milliseconds. The timer can be initialized with a specific start time or with the current system time. It provides methods to reset the timer, retrieve the elapsed time in milliseconds, and get the elapsed time as a formatted string.

**Extends/Implements:** extends Object

### Fields

- `public long startTime` - The start time of the timer in milliseconds.

### Methods

#### `reset()`

Resets the start time of the timer to the current system time.

#### `timeElapsed()`

**Returns:** `long`

Returns the time difference between the start time and the current system time in milliseconds.

**Returns:** the time difference in milliseconds

#### `getTimeElapsedFormatted()`

**Returns:** `String`

Returns the time difference between the start time and the current system time as a formatted string in the format HH:mm:ss.SSS.

**Returns:** the formatted time difference


---

## UIResult<T>

**Package:** `com.osmb.api.utils`

**Type:** Class

A result container that tracks both search capability and results for UI components, providing clear distinction between: 1. Component not visible (couldn't attempt search) 2. Component visible with two sub-outcomes: a) Target found (with value) b) Target not found (null) This solves the ambiguity of knowing whether: You should retry (component wasn't visible) The search genuinely failed (component was visible but target missing) The search succeeded While technically having two states (NOT_VISIBLE/FOUND), the FOUND state combines both successful and unsuccessful searches when the component was visible.

**Extends/Implements:** extends Object implements Result<T>

### Methods

#### `notVisible()`

**Returns:** `UIResult<T>`

Creates a result indicating the search couldn't be performed because the component wasn't visible.

**Returns:** A UIResult with NOT_VISIBLE state

#### `of(T value)`

**Returns:** `UIResult<T>`

Creates a result from a search attempt (component was visible).

**Parameters:**
- `value` - The found value (null if not found)

**Returns:** A UIResult with FOUND state

#### `getIfFound()`

**Returns:** `T`

Gets the value if found, otherwise returns null.

**Returns:** The found value or null

#### `isNotVisible()`

**Returns:** `boolean`

Checks if the component wasn't visible during search.

**Returns:** true if search couldn't be attempted

#### `isNotFound()`

**Returns:** `boolean`

Checks if the search was performed but found nothing (visible component, null value).

**Returns:** true if component was visible but target wasn't found

#### `isFound()`

**Returns:** `boolean`

Checks if the target was successfully found.

**Returns:** true if component was visible AND target was found

#### `get()`

**Returns:** `T`

Gets the found value.

**Returns:** The result value

**Throws:**
- IllegalStateException - if component wasn't visible

#### `orElse(T other)`

**Returns:** `T`

Gets the found value or a default if not available.

**Parameters:**
- `other` - The fallback value

**Returns:** The found value or fallback

#### `orElseGet(Function<com.osmb.api.utils.UIResult.State,T> stateFallback)`

**Returns:** `T`

Gets the found value or computes a fallback based on state.

**Parameters:**
- `stateFallback` - Function that provides fallback value

**Returns:** The found value or computed fallback

#### `ifFound(Consumer<T> consumer)`

Performs an action if the target was found.

**Parameters:**
- `consumer` - The action to execute

#### `toString()`

**Returns:** `String`

Returns a string representation of the result.

**Returns:** Formatted string showing state and value


---

## UIResultList<T>

**Package:** `com.osmb.api.utils`

**Type:** Class

A two-state result container for UI component searches so we can distinguish a failed search due to the component being visible or not.

**Extends/Implements:** extends Object implements Result<T>, Iterable<T>

### Fields

- `protected final UIResultList.State state` - Enum representing possible states of the result.
- `protected final List<T> values` - The list of result values when the state is FOUND.

### Methods

#### `notVisible()`

**Returns:** `UIResultList<T>`

Creates an instance representing a result where the component is not visible.

**Returns:** A UIResultList instance with state NOT_VISIBLE.

#### `of(List<T> values)`

**Returns:** `UIResultList<T>`

Creates an instance representing a result where UI components were found.

**Parameters:**
- `values` - The list of found elements.

**Returns:** A UIResultList instance with state FOUND and the provided values.

#### `isNotVisible()`

**Returns:** `boolean`

Checks whether the state of the result is NOT_VISIBLE.

**Returns:** true if the state is NOT_VISIBLE, false otherwise.

#### `isNotFound()`

**Returns:** `boolean`

Checks whether if the result is empty

**Returns:** true if there are no results in the list

#### `isFound()`

**Returns:** `boolean`

Checks whether the state of the result is FOUND & if the list is empty.

**Returns:** true if the state is FOUND & the list is not empty, false otherwise.

#### `get(int index)`

**Returns:** `T`

Returns the element at the specified position in the list.

**Parameters:**
- `index` - The index of the element to return.

**Returns:** The element at the specified position.

**Throws:**
- IllegalStateException - if the state is NOT_VISIBLE.

#### `getRandom()`

**Returns:** `T`

Returns a random element from the list.

**Returns:** A randomly selected element from the list.

**Throws:**
- IllegalStateException - if the state is NOT_VISIBLE.

#### `size()`

**Returns:** `int`

Returns the number of elements in the result list.

**Returns:** The size of the list.

#### `isEmpty()`

**Returns:** `boolean`

Checks if the result list is empty.

**Returns:** true if the list is empty, false otherwise.

#### `asList()`

**Returns:** `List<T>`

Returns an unmodifiable view of the result list.

**Returns:** An unmodifiable list containing the result values.

#### `shuffleResults()`

Custom shuffle implementation (Fisher-Yates shuffle).

#### `getOrDefault(int index, T defaultValue)`

**Returns:** `T`

Returns the element at the specified index, or a default value if the index is out of bounds or the state is NOT_VISIBLE.

**Parameters:**
- `index` - The index of the element to return.
- `defaultValue` - The default value to return if the index is invalid or state is NOT_VISIBLE.

**Returns:** The element at the specified index, or the default value.

#### `ifFound(Consumer<List<T>> consumer)`

Performs the given action if the state is FOUND, passing the list of values to the consumer.

**Parameters:**
- `consumer` - The action to perform with the list of values.

#### `forEach(Consumer<? super T> action)`

Performs the given action for each element in the result list if the state is FOUND.

**Parameters:**
- `action` - The action to perform for each element.

#### `toString()`

**Returns:** `String`

Returns a string representation of the result, including the state and values.

**Returns:** A string describing the result.

#### `iterator()`

**Returns:** `Iterator<T>`

Returns an iterator over the elements in the result list.

**Returns:** An iterator over the elements, or an empty iterator if state is NOT_VISIBLE.


---

## Utils

**Package:** `com.osmb.api.utils`

**Type:** Class

**Extends/Implements:** extends Object

### Methods

#### `random(int num)`

**Returns:** `int`

#### `random(int low, int high)`

**Returns:** `int`

#### `lineIntersectsRectangle(Line line, Rectangle rectangle)`

**Returns:** `boolean`

#### `stringToTimestamp(String input)`

**Returns:** `LocalDateTime`

#### `randomisePointByRadius(Point point, int radius)`

**Returns:** `Point`

#### `convertImageToBytes(BufferedImage image)`

**Returns:** `byte[]`

**Throws:**
- IOException

#### `combineArray(T[] array1, T[] array2)`

**Returns:** `T[]`

#### `getClosestPosition(Position mainPosition, Position... positions)`

**Returns:** `Position`

#### `getClosestPosition(Position mainPosition, int maxDistance, Position... positions)`

**Returns:** `Position`

#### `getClosestPosition(WorldPosition mainPosition, int maxDistance, List<WorldPosition> positions)`

**Returns:** `WorldPosition`

#### `getClosestPoint(Point mainPosition, int maxDistance, List<Point> positions)`

**Returns:** `Point`

#### `generateHumanLikePath(Rectangle screenBounds, Point start, Point end)`

**Returns:** `List<Point>`

#### `generateStraightPath(Rectangle screenBounds, Point start, Point end)`

**Returns:** `List<Point>`

#### `createBoundingRectangle(List<Point> points)`

**Returns:** `Rectangle`

Creates the smallest rectangle (bounding rectangle) that completely encompasses a given list of points.

**Parameters:**
- `points` - the list of Point objects representing the points to be enclosed within the rectangle. Each point must have valid integer x and y coordinates. The list must not be null or empty.

**Returns:** a Rectangle object representing the smallest rectangle that can fully enclose all the given points. - The top-left corner of the rectangle is determined by the smallest x and y values among the points. - The width is calculated as (maxX - minX). - The height is calculated as (maxY - minY).

**Throws:**
- IllegalArgumentException - if the points list is null or empty.

#### `getPositionsWithinRadius(LocalPosition position, int radius)`

**Returns:** `List<LocalPosition>`

#### `getPositionsWithinRadius(WorldPosition position, int radius)`

**Returns:** `List<WorldPosition>`

#### `getRandomPointOutside(Rectangle rectangle, Rectangle parent, int maxDistance)`

**Returns:** `Point`

#### `imageToImageView(Image image)`

**Returns:** `javafx.scene.image.ImageView`

#### `getImageResource(String name)`

**Returns:** `BufferedImage`

**Throws:**
- IOException

#### `calculateCentroid(List<Point> points)`

**Returns:** `Point`

Returns the center point of the list of points, calculated as the average of all points' coordinates.

**Returns:** A Point object representing the center of the list of points.

#### `calculateBoundingBox(List<Point> points)`

**Returns:** `Rectangle`

Returns the bounding rectangle that contains all points. The rectangle is defined by its top-left corner (minX, minY) and its width and height.

**Returns:** A Rectangle object representing the bounding box of the list of points.

#### `getClosest(List<? extends Location3D> objects)`

**Returns:** `Location3D`

#### `getTextBounds(Rectangle bounds, int maxHeight, int textColor)`

**Returns:** `Rectangle[]`

Retrieves the rectangular bounds of the text displayed in a given area.

**Parameters:**
- `bounds` - The rectangle area on the screen where the text is expected. It specifies the starting position (x, y) and the limit for width and height.
- `maxHeight` - The maximum height each line of text can have. This is useful when lines of text are close together, with no whitespace in between. This height can be calculated based on the distance from the top of ascending characters to the bottom of descending characters.
- `textColor` - The color of the text as an integer. The bounds will be calculated for the text pixels that match this color.

**Returns:** An array of Rectangle objects where each rectangle corresponds to a line of text. The rectangle provides the bounds of the line of text on the screen (x, y, width, height). If no text matching the color is found within the bounds, an empty array is returned.

#### `getWorldPositionForRespawnCircles(List<Rectangle> circleBounds, int respawnCircleHeight)`

**Returns:** `List<WorldPosition>`

This method matches detected respawn circles (in screen coordinates) to their corresponding world positions by: Calculating center points of all detected circles Projecting each game tile to screen space Finding the closest matching circle for each projected tile point Converting matching tile coordinates to world positions Note: The matching uses a proximity threshold of 6 pixels between tile projections and circle centers.

**Parameters:**
- `circleBounds` - List of rectangles representing detected respawn circles. Each rectangle should contain a valid respawn circle.
- `respawnCircleHeight` - The height/z-offset (in pixels) at which respawn circles are drawn relative to their base tile position. This affects the projection.

**Returns:** List of WorldPosition objects corresponding to the detected respawn circles. The list may be shorter than the input if some circles couldn't be matched to tiles.

**Throws:**
- RuntimeException - if the current world position cannot be determined
- NullPointerException - if circleBounds is null

#### `getWorldPositionForRespawnCircles(List<PixelAnalyzer.RespawnCircle> circles, int objectWidth, int objectHeight, int circleZHeight, boolean debug)`

**Returns:** `List<WorldPosition>`

This method matches detected respawn circles (in screen coordinates) to their corresponding world positions by: Calculating center points of all detected circles Projecting each game tile to screen space Finding the closest matching circle for each projected tile point Converting matching tile coordinates to world positions Note: The matching uses a proximity threshold of 6 pixels between tile projections and circle centers.

**Parameters:**
- `circles` - List of Respawn circle objects representing detected respawn circles.
- `circleZHeight` - The height/z-offset (in pixels) at which respawn circles are drawn (center of the circle) relative to their base tile position.
- `debug` - If true, draws the respawn circle center points on screen for visual debugging.

**Returns:** List of WorldPosition objects corresponding to the detected respawn circles. The list may be shorter than the input if some circles couldn't be matched to tiles.

**Throws:**
- RuntimeException - if the current world position cannot be determined
- NullPointerException - if circles is null


---

