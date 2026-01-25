# Timer & Stopwatch

**Package:** `com.osmb.api.utils.timing`

Utility classes for measuring elapsed time in scripts. Useful for runtime tracking, action cooldowns, and paint overlays.

---

## Timer

A simple timer for measuring elapsed time from a start point.

### Constructors

```java
// start timer from current time
Timer timer = new Timer();

// start timer from specific time
Timer timer = new Timer(System.currentTimeMillis() - 60000);  // started 1 min ago
```

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `startTime` | `long` | The start time in milliseconds |

### Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `reset()` | `void` | Reset to current time |
| `timeElapsed()` | `long` | Milliseconds since start |
| `getTimeElapsedFormatted()` | `String` | Formatted as "HH:mm:ss.SSS" |

### Example: Runtime Tracking

```java
public class MyScript extends Script {
    private Timer runtime;

    @Override
    public void onStart() {
        runtime = new Timer();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // draw runtime on paint
        canvas.drawText("Runtime: " + runtime.getTimeElapsedFormatted(), 10, 50);
    }
}
```

### Example: Action Cooldown

```java
private Timer lastAction = new Timer();
private static final long COOLDOWN_MS = 5000;  // 5 seconds

public boolean canAct() {
    return lastAction.timeElapsed() >= COOLDOWN_MS;
}

public void doAction() {
    if (canAct()) {
        // perform action
        lastAction.reset();  // restart cooldown
    }
}
```

---

## Stopwatch

Similar to Timer but with additional pause/resume capabilities.

### Constructor

```java
Stopwatch stopwatch = new Stopwatch();
```

### Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `start()` | `void` | Start the stopwatch |
| `stop()` | `void` | Stop (pause) the stopwatch |
| `reset()` | `void` | Reset to zero |
| `getElapsedTime()` | `long` | Get elapsed milliseconds |
| `isRunning()` | `boolean` | Check if currently running |

### Example: Track Active Time

```java
private Stopwatch activeTime = new Stopwatch();

@Override
public int poll() {
    if (isDoingWork()) {
        if (!activeTime.isRunning()) {
            activeTime.start();
        }
        // do work
    } else {
        if (activeTime.isRunning()) {
            activeTime.stop();  // pause when idle
        }
    }
    return 600;
}

public long getActiveTimeMs() {
    return activeTime.getElapsedTime();
}
```

---

## Common Patterns

### Simple Runtime String

```java
// for paint overlay
private long startTime = System.currentTimeMillis();

public String getRuntime() {
    long elapsed = System.currentTimeMillis() - startTime;
    long hours = elapsed / 3600000;
    long mins = (elapsed % 3600000) / 60000;
    long secs = (elapsed % 60000) / 1000;
    return String.format("%02d:%02d:%02d", hours, mins, secs);
}
```

### Stats Per Hour Calculation

```java
private Timer runtime = new Timer();
private int itemsProcessed = 0;

public int getItemsPerHour() {
    long elapsed = runtime.timeElapsed();
    if (elapsed < 1000) return 0;  // avoid division by very small numbers

    double hours = elapsed / 3600000.0;
    return (int)(itemsProcessed / hours);
}
```

### Periodic Actions

```java
private Timer lastReport = new Timer();
private static final long REPORT_INTERVAL = 600000;  // 10 minutes

@Override
public int poll() {
    // report stats every 10 minutes
    if (lastReport.timeElapsed() >= REPORT_INTERVAL) {
        reportStats();
        lastReport.reset();
    }

    // normal script logic
    return processItems();
}
```

### Timeout Waiting

```java
public boolean waitForCondition(BooleanSupplier condition, long timeoutMs) {
    Timer timeout = new Timer();
    while (timeout.timeElapsed() < timeoutMs) {
        if (condition.getAsBoolean()) {
            return true;
        }
        script.pollFramesUntil(() -> false, 100);  // brief wait
    }
    return false;  // timed out
}

// usage
boolean bankOpened = waitForCondition(
    () -> script.getWidgetManager().getBank().isOpen(),
    5000  // 5 second timeout
);
```

---

## Timer vs System.currentTimeMillis()

For simple elapsed time tracking, you can use either:

```java
// Using Timer
Timer timer = new Timer();
// ... later ...
long elapsed = timer.timeElapsed();

// Using raw System time
long startTime = System.currentTimeMillis();
// ... later ...
long elapsed = System.currentTimeMillis() - startTime;
```

**Use Timer when:**
- You need the formatted string output
- You want `reset()` functionality
- Code readability is important

**Use raw System time when:**
- Minimal overhead is critical
- You only need basic elapsed time
- You're storing the start time in a field anyway

---

## See Also

- [Paint.md](Paint.md) - Using timers in paint overlay
- [Reporting-data.md](Reporting-data.md) - Stats reporting intervals
