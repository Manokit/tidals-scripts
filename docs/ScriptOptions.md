# ScriptOptions

**Type:** Interface

**All Known Implementing Classes:** Script

## Methods

| Return Type | Method |
|------------|--------|
| `List<AFKTimer>` | `afkTimers()` |
| `default boolean` | `canAFK()` |
| `default boolean` | `canBreak()` |
| `default boolean` | `canHopWorlds()` |
| `default boolean` | `enableCameraOffsetSync()` |
| `default void` | `importImages(Set<ImageImport> imageImportSet)` |
| `default void` | `onRelog()` |
| `default void` | `onStart()` |
| `default boolean` | `promptBankTabDialogue()` |
| `default int[]` | `regionsToPrioritise()` |
| `default boolean` | `skipCompassYawCheck()` |
| `default boolean` | `trackXP()` |

## Method Details

### canBreak
```java
default boolean canBreak()
```

### canHopWorlds
```java
default boolean canHopWorlds()
```

### canAFK
```java
default boolean canAFK()
```

### promptBankTabDialogue
```java
default boolean promptBankTabDialogue()
```

### onRelog
```java
default void onRelog()
```

### onStart
```java
default void onStart()
```

### importImages
```java
default void importImages(Set<ImageImport> imageImportSet)
```

### trackXP
```java
default boolean trackXP()
```

### afkTimers
```java
List<AFKTimer> afkTimers()
```

### regionsToPrioritise
```java
default int[] regionsToPrioritise()
```

### enableCameraOffsetSync
```java
default boolean enableCameraOffsetSync()
```

On script start up, the script will sync the camera offset by walking diagonally. WARNING: If disabled and the camera offset may be wrong (unless the user reloaded the app and hasn't moved since starting the script) This can cause miss-clicking issues or detection issues if you rely on precise shapes in the 3d world.

**Returns:** true to enable camera offset sync, false to disable it.

### skipCompassYawCheck
```java
default boolean skipCompassYawCheck()
```

Skips the compass yaw check when finding position. This can be useful for certain instances where the compass is not visible.

**Returns:** true to skip the compass yaw check, false to perform the check.
