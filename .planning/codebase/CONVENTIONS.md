# Coding Conventions

**Analysis Date:** 2026-01-16

## Naming Patterns

**Files:**
- Main script classes: `PascalCase` matching script name (e.g., `TidalsGemCutter.java`, `TidalsSecondaryCollector.java`)
- Task classes: `PascalCase` action verbs (e.g., `StartThieving.java`, `DepositOres.java`, `MonitorThieving.java`)
- Utility classes: `PascalCase` with `Utils` suffix (e.g., `RetryUtils.java`, `BankingUtils.java`, `DialogueUtils.java`)
- ScriptUI: Always `ScriptUI.java` in `main/` package

**Functions:**
- `camelCase` for all methods
- Getters: `getX()` pattern (e.g., `getThievingTile()`, `getInventoryCount()`)
- Boolean checks: `isX()` or `hasX()` pattern (e.g., `isAtSafetyTile()`, `hasRunePouch()`)
- Retry methods: verb + `WithRetry` suffix (e.g., `selectItemWithRetry()`)

**Variables:**
- `camelCase` for local variables and parameters
- `UPPER_SNAKE_CASE` for constants (e.g., `STATS_INTERVAL_MS`, `DEFAULT_MAX_ATTEMPTS`)
- Item IDs: `UPPER_SNAKE_CASE` with descriptive names (e.g., `CRAFTING_CAPE`, `MORT_MYRE_FUNGUS`)

**Types:**
- Enums: `PascalCase` names, `UPPER_SNAKE_CASE` values (e.g., `SecondaryType.MORT_MYRE_FUNGUS`, `State.COLLECTING`)
- Interfaces: `PascalCase` with descriptive names (e.g., `SecondaryCollectorStrategy`)

**Packages:**
- `main/` - Main script class and ScriptUI
- `tasks/` - Task classes for state machine
- `utils/` - Script-local utilities (Task interface, XPTracking)
- `strategies/` - Strategy pattern implementations
- `utilities/` - Shared utilities jar (separate module)

## Code Style

**Formatting:**
- No dedicated formatter tool detected
- 4-space indentation
- Opening braces on same line
- Single blank line between methods

**Linting:**
- No dedicated linter configured
- Relies on IDE defaults

**Line Length:**
- No strict limit, but generally under 120 characters
- Long method chains split across lines with `.` at start of continuation

## Import Organization

**Order:**
1. `com.osmb.api.*` - OSMB framework classes
2. `javafx.*` - JavaFX for UI (ScriptUI only)
3. `javax.*` - Standard extensions (ImageIO, etc.)
4. `java.*` - Standard library
5. Local packages (`main.*`, `tasks.*`, `utils.*`, `strategies.*`, `utilities.*`)

**Path Aliases:**
- None - uses fully qualified package paths
- Static imports for main script fields: `import static main.TidalsGemCutter.*;`

## Error Handling

**Patterns:**
- Return `false` on failure, `true` on success for boolean methods
- Return `null` on failure for object-returning methods, check for null before use
- Return `-1` on failure for int-returning methods when 0 is a valid value
- Log errors with `script.log(getClass(), "error description")` or `script.log("CATEGORY", "message")`
- Use `script.stop()` for fatal errors that require script termination

**Null Safety:**
```java
// always check widget/manager returns
ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
if (inv == null) {
    script.log(getClass(), "inventory not visible");
    return false;
}

// check position before use
WorldPosition pos = script.getWorldPosition();
if (pos == null) {
    return State.IDLE;
}
```

**Retry Pattern:**
- Use `RetryUtils` from shared utilities for all menu interactions
- Default: 10 retry attempts with 300-500ms delay between attempts
- Log each attempt as "description attempt X/Y"

## Logging

**Framework:** `script.log()` method

**Patterns:**
- Category logging: `script.log("CATEGORY", "message")` for grouped output
- Class logging: `script.log(getClass(), "message")` for task-specific output
- Lowercase, blunt comments in code
- Log important state transitions and errors

**Categories Used:**
- `INFO` - startup, configuration
- `UI` - user interface selections
- `THIEVE` - thieving-specific actions
- `LOOT` - item gains
- `INVENTORY` - inventory state changes
- `STATS` - stats reporting
- `WEBHOOK` - webhook operations
- `JAIL` - jail detection/escape

## Comments

**When to Comment:**
- Complex game mechanics that aren't obvious
- Timing-critical sections explaining delays
- Magic numbers (item IDs, coordinates, colors)
- Strategy decisions and workarounds

**Style:**
- Lowercase, blunt tone
- No period at end of single-line comments
- Explain "why" not "what"

```java
// wait for bloom animation (~3 ticks = 1800ms), ignoreTasks to prevent random tab opens
script.pollFramesUntil(() -> false, script.random(1800, 2000), true);

// skip delays in two-stall mode - timing critical
if (!twoStallMode) {
    script.pollFramesHuman(() -> false, script.random(200, 400));
}
```

**JSDoc/Javadoc:**
- Used for shared utility methods in `utilities/` package
- Include `@param`, `@return`, `@throws` tags
- Describe preconditions and usage patterns

## Function Design

**Size:**
- Keep under 50 lines when possible
- Extract helper methods for complex logic

**Parameters:**
- Script instance always first parameter in utility methods
- Use primitives for IDs/amounts, objects for complex data
- Use varargs for optional actions: `String... actions`

**Return Values:**
- `boolean` for success/failure operations
- `int` for counts (-1 for error, 0 for empty/none)
- Objects for complex results, null for not found
- Dedicated result classes for batch operations (`BatchWithdrawalResult`)

## Module Design

**Exports:**
- Public classes only, no package-private access
- Utility classes use static methods only
- Strategy pattern with interface + implementations

**Barrel Files:**
- Not applicable (Java)

## Static Fields Pattern

Scripts use static fields for shared state across tasks:

```java
// in main script class
public static boolean setupDone = false;
public static int craftCount = 0;
public static String task = "Initialize";
public static long startTime = System.currentTimeMillis();

// accessed from tasks via static import
import static main.TidalsGemCutter.*;

// in task
task = "Banking";
craftCount += gained;
```

## Task Interface Pattern

All tasks extend the abstract `Task` class:

```java
// utils/Task.java
public abstract class Task {
    protected Script script;

    public Task(Script script) {
        this.script = script;
    }

    public abstract boolean activate();  // return true if this task should run
    public abstract boolean execute();   // perform the task
}
```

Tasks checked in order in `poll()`:
```java
for (Task task : tasks) {
    if (task.activate()) {
        task.execute();
        return 0;
    }
}
```

## Strategy Pattern

For scripts with multiple modes/strategies:

```java
// interface
public interface SecondaryCollectorStrategy {
    State determineState();
    boolean verifyRequirements();
    int collect();
    int bank();
    int restorePrayer();
    int returnToArea();
}

// implementation
public class MortMyreFungusCollector implements SecondaryCollectorStrategy {
    private final Script script;

    public MortMyreFungusCollector(Script script) {
        this.script = script;
    }
    // implement methods...
}
```

## Paint Overlay Pattern

Use Canvas API with themed colors:

```java
@Override
public void onPaint(Canvas c) {
    // define colors matching script theme
    final Color bgColor = new Color(47, 27, 16);
    final Color textLight = new Color(238, 237, 233);

    // draw background
    c.fillRect(x, y, width, height, bgColor.getRGB(), 1);

    // draw stat lines
    drawStatLine(c, x, width, padding, curY, "Label", "Value", labelColor, valueColor);
}

private void drawStatLine(Canvas c, int innerX, int innerWidth, int paddingX, int y,
                          String label, String value, int labelColor, int valueColor) {
    c.drawText(label, innerX + paddingX, y, labelColor, FONT_LABEL);
    int valW = c.getFontMetrics(FONT_VALUE).stringWidth(value);
    int valX = innerX + innerWidth - paddingX - valW;
    c.drawText(value, valX, y, valueColor, FONT_VALUE);
}
```

## Human-like Delays

Use `pollFramesHuman` and `pollFramesUntil` with random ranges:

```java
// short action delay
script.pollFramesHuman(() -> false, script.random(200, 400));

// wait for condition with timeout
script.pollFramesUntil(() -> script.getWidgetManager().getBank().isVisible(), 5000);

// wait with ignoreTasks flag to prevent interruptions
script.pollFramesUntil(() -> false, script.random(1800, 2000), true);
```

---

*Convention analysis: 2026-01-16*
