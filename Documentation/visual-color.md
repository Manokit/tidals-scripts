# OSMB API - Color & Pixel Analysis

Color detection and pixel analysis

---

## Classes in this Module

- [ChannelThresholdComparator](#channelthresholdcomparator) [class]
- [Class ColorModel](#class-colormodel) [class]
- [ColorUtils](#colorutils) [class]
- [HSLPalette](#hslpalette) [class]
- [MultiChannelThresholdComparator](#multichannelthresholdcomparator) [class]
- [Pixel](#pixel) [class]
- [SingleThresholdComparator](#singlethresholdcomparator) [class]
- [ToleranceComparator](#tolerancecomparator) [class]

---

## ChannelThresholdComparator

**Package:** `com.osmb.api.visual.color.tolerance.impl`

**Type:** Class

The ChannelThresholdComparator class implements the ToleranceComparator interface and is used to compare RGB or HSL values to determine whether they are within a specified tolerance range for each of the three channels (Red/Green/Blue or Hue/Saturation/Lightness). This comparator works by comparing the differences between the channels of two colors and checks whether each difference is within a defined threshold.

**Extends/Implements:** extends Object implements ToleranceComparator

### Methods

#### `getChannel2()`

**Returns:** `int`

#### `getChannel1()`

**Returns:** `int`

#### `getChannel3()`

**Returns:** `int`

#### `isWithinTolerance(double[] diff)`

**Returns:** `boolean`

Determines if the differences between two colors are within the specified tolerances for each channel. This method compares the differences in three channels (e.g., RGB or HSL) against their respective thresholds.

**Parameters:**
- `diff` - an array containing the differences between the three color channels (e.g., diff[0] for Red/Hue, diff[1] for Green/Saturation, diff[2] for Blue/Lightness)

**Returns:** true if the differences for all three channels are within their respective thresholds; false otherwise

#### `isWithinTolerance(int[] diff)`

**Returns:** `boolean`

Description copied from interface: ToleranceComparator

**Parameters:**
- `diff` - an array containing the differences between the three color channels (e.g., diff[0] for Red/Hue, diff[1] for Green/Saturation, diff[2] for Blue/Lightness)

**Returns:** true if the differences for all three channels are within their respective thresholds; false otherwise

#### `isZero()`

**Returns:** `boolean`


---

## Class ColorModel

**Package:** `com.osmb.api.visual.color`

**Type:** Class

**Extends/Implements:** extends Enum<ColorModel>

### Methods

#### `values()`

**Returns:** `ColorModel[]`

Returns an array containing the constants of this enum class, in the order they are declared.

**Returns:** an array containing the constants of this enum class, in the order they are declared

#### `valueOf(String name)`

**Returns:** `ColorModel`

Returns the enum constant of this class with the specified name. The string must match exactly an identifier used to declare an enum constant in this class. (Extraneous whitespace characters are not permitted.)

**Parameters:**
- `name` - the name of the enum constant to be returned.

**Returns:** the enum constant with the specified name

**Throws:**
- IllegalArgumentException - if this enum class has no constant with the specified name
- NullPointerException - if the argument is null


---

## ColorUtils

**Package:** `com.osmb.api.visual.color`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public static final int ORANGE_UI_TEXT`
- `public static final int TRANSPARENT_PIXEL`
- `public static final int DIALOGUE_COLOR`
- `public static final int BLACK_ITEM_BORDER_COLOR`
- `public static final int WHITE_ITEM_BORDER_COLOR`
- `public static final int BLACK_TEXT_COLOR`
- `public static final int YELLOW_TEXT_COLOR`
- `public static final int COMPONENT_TITLE_COLOR`

### Methods

#### `setAlpha(int[] pixels, int alpha)`

Sets the alpha value for an array of pixel colors, modifying the original array.

**Parameters:**
- `pixels` - the array of pixel colors in ARGB format.
- `alpha` - the desired alpha value (0-255) to set for all pixels.

#### `getRGBDifferences(int rgb0, int rgb1)`

**Returns:** `int[]`

Computes the absolute differences between the red, green, and blue components of two RGB colors.

**Parameters:**
- `rgb0` - the first color in RGB/ARGB format.
- `rgb1` - the second color in RGB/ARGB format.

**Returns:** an array of three integers representing the absolute differences in the red, green, and blue channels.

#### `isRGBinsideThreshold(ToleranceComparator toleranceComparator, int rgb0, int rgb1, double[] differencesHolder)`

**Returns:** `boolean`

Checks if the differences between two RGB colors are within a specified tolerance using a ToleranceComparator.

**Parameters:**
- `toleranceComparator` - the comparator defining the acceptable threshold for the RGB differences.
- `rgb0` - the first color in RGB/ARGB format.
- `rgb1` - the second color in RGB/ARGB format.
- `differencesHolder` - the output parameter for acquiring differences

**Returns:** true if the differences are within the tolerance, otherwise false.

#### `getHSLDifferences(int rgb0, int rgb1)`

**Returns:** `double[]`

Calculates the absolute differences between two colors in the HSL color space.

**Parameters:**
- `rgb0` - the first color in RGB/ARGB format
- `rgb1` - the second color in RGB/ARGB format

**Returns:** an array of three doubles representing the absolute differences in the hue, saturation, and lightness components.

#### `isTransparent(int rgb)`

**Returns:** `boolean`

#### `isHSLinsideThreshold(ToleranceComparator toleranceComparator, int rgb0, int rgb1, double[] differencesHolder)`

**Returns:** `boolean`

#### `isHSLInDistanceThreshold(int maxDistance, int rgb0, int rgb1)`

**Returns:** `boolean`

#### `calculateHSLDistance(double[] hslColor1, double[] hslColor2)`

**Returns:** `double`

Calculates the Euclidean distance between two colors in the HSL (Hue, Saturation, Lightness) color space. This method determines how visually different two colors are based on their HSL values, where the hue component is handled circularly to account for the wrap-around of hues (i.e., 0Â° is adjacent to 360Â°). **Note:** This method involves the use of the Math.sqrt() function, which can be computationally expensive. It is recommended not to call this method repeatedly in tight loops or performance-critical sections of code. Consider using calculateSquaredDistance() to avoid the square root.

**Parameters:**
- `hslColor1` - the first HSL color represented as a double array where: hslColor1[0] - hue (in degrees, 0-360) hslColor1[1] - saturation (percentage, 0-100) hslColor1[2] - lightness (percentage, 0-100)
- `hslColor2` - the second HSL color, formatted similarly to hslColor1.

**Returns:** the Euclidean distance between the two HSL colors, accounting for circular hue distance.

#### `calculateHSLSquaredDistance(double[] hslColor1, double[] hslColor2)`

**Returns:** `double`

Calculates the squared distance between two colors in the HSL (Hue, Saturation, Lightness) color space. This method avoids the use of Math.sqrt() by returning the squared distance instead, which can be useful for comparing relative distances without needing to compute the actual Euclidean distance. **Note:** Squared distance is sufficient when you are only comparing distances, as it avoids the overhead of the square root computation. The actual distance values will be larger, but comparisons (i.e., which color is closer) will remain valid.

**Parameters:**
- `hslColor1` - the first HSL color represented as an integer array where: hslColor1[0] - hue (in degrees, 0-360) hslColor1[1] - saturation (percentage, 0-100) hslColor1[2] - lightness (percentage, 0-100)
- `hslColor2` - the second HSL color, formatted similarly to hslColor1.

**Returns:** the squared Euclidean distance between the two HSL colors, accounting for circular hue distance.

#### `removeAlphaChannel(int color)`

**Returns:** `int`


---

## HSLPalette

**Package:** `com.osmb.api.visual.color`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `public static final double MAX`
- `public static final double HIGH`
- `public static final double MID`
- `public static final double LOW`
- `public static final double MIN`
- `public static final int MAX_RGB_VALUE`
- `public static final int LOOKUP_TABLE_SIZE`
- `public static long[] rgbToHSLLookupTable`

### Methods

#### `initializeLookupTable()`

#### `create(double brightness)`

**Returns:** `int[]`

#### `rgbToHsl(int rgb)`

**Returns:** `double[]`

#### `packHsl(double h, double s, double l)`

**Returns:** `long`

#### `unpackHsl(long packedValue)`

**Returns:** `double[]`


---

## MultiChannelThresholdComparator

**Package:** `com.osmb.api.visual.color.tolerance.impl`

**Type:** Class

The ChannelThresholdComparator class implements the ToleranceComparator interface and is used to compare RGB or HSL values to determine whether they are within a specified tolerance range for each of the three channels (Red/Green/Blue or Hue/Saturation/Lightness). This comparator works by comparing the differences between the channels of two colors and checks whether each difference is within a defined threshold.

**Extends/Implements:** extends Object implements ToleranceComparator

### Methods

#### `isWithinTolerance(double[] diff)`

**Returns:** `boolean`

Determines if the differences between two colors are within the specified tolerances for each channel. This method compares the differences in three channels (e.g., RGB or HSL) against their respective thresholds.

**Parameters:**
- `diff` - an array containing the differences between the three color channels (e.g., diff[0] for Red/Hue, diff[1] for Green/Saturation, diff[2] for Blue/Lightness)

**Returns:** true if the differences for all three channels are within their respective thresholds; false otherwise

#### `isWithinTolerance(int[] diff)`

**Returns:** `boolean`

Description copied from interface: ToleranceComparator

**Parameters:**
- `diff` - an array containing the differences between the three color channels (e.g., diff[0] for Red/Hue, diff[1] for Green/Saturation, diff[2] for Blue/Lightness)

**Returns:** true if the differences for all three channels are within their respective thresholds; false otherwise

#### `isZero()`

**Returns:** `boolean`


---

## Pixel

**Package:** `com.osmb.api.visual.color`

**Type:** Class

**Extends/Implements:** extends Object

### Fields

- `protected final int rgb`

### Methods

#### `getRgb()`

**Returns:** `int`

#### `getColor()`

**Returns:** `Color`


---

## SingleThresholdComparator

**Package:** `com.osmb.api.visual.color.tolerance.impl`

**Type:** Class

The SingleThresholdComparator class implements the ToleranceComparator interface and is used to compare the difference between the channels of two colors (e.g., RGB or HSL) against a single, unified tolerance threshold. This comparator checks whether the difference in each color channel is less than or equal to a specified threshold value.

**Extends/Implements:** extends Object implements ToleranceComparator

### Methods

#### `getThreshold()`

**Returns:** `int`

#### `isWithinTolerance(int[] diff)`

**Returns:** `boolean`

Determines if the differences between two colors are within the specified tolerance for all channels. This method compares the differences in three channels (e.g., RGB or HSL) against a single threshold value.

**Parameters:**
- `diff` - an array containing the differences between the three color channels (e.g., diff[0] for Red/Hue, diff[1] for Green/Saturation, diff[2] for Blue/Lightness)

**Returns:** true if the differences for all three channels are less than or equal to the threshold; false otherwise

#### `isWithinTolerance(double[] diff)`

**Returns:** `boolean`

Determines if the differences between two colors are within the specified tolerance for all channels. This method compares the differences in three channels (e.g., RGB or HSL) against a single threshold value.

**Parameters:**
- `diff` - an array containing the differences between the three color channels (e.g., diff[0] for Red/Hue, diff[1] for Green/Saturation, diff[2] for Blue/Lightness)

**Returns:** true if the differences for all three channels are less than or equal to the threshold; false otherwise

#### `isZero()`

**Returns:** `boolean`


---

## ToleranceComparator

**Package:** `com.osmb.api.visual.color.tolerance`

**Type:** Class

### Fields

- `static final SingleThresholdComparator ZERO_TOLERANCE`

### Methods

#### `isWithinTolerance(double[] diff)`

**Returns:** `boolean`

Determines if the differences between two colors are within the specified tolerances for each channel. This method compares the differences in three channels (e.g., RGB or HSL) against their respective thresholds.

**Parameters:**
- `diff` - an array containing the differences between the three color channels (e.g., diff[0] for Red/Hue, diff[1] for Green/Saturation, diff[2] for Blue/Lightness)

**Returns:** true if the differences for all three channels are within their respective thresholds; false otherwise

#### `isWithinTolerance(int[] diff)`

**Returns:** `boolean`

Determines if the differences between two colors are within the specified tolerances for each channel. This method compares the differences in three channels (e.g., RGB or HSL) against their respective thresholds.

**Parameters:**
- `diff` - an array containing the differences between the three color channels (e.g., diff[0] for Red/Hue, diff[1] for Green/Saturation, diff[2] for Blue/Lightness)

**Returns:** true if the differences for all three channels are within their respective thresholds; false otherwise

#### `isZero()`

**Returns:** `boolean`


---

