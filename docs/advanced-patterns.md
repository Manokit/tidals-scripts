# Advanced Patterns from Production Scripts

Battle-tested patterns from real-world OSMB scripts (dSunbleakWCer, dBirdhouseRunner, etc.).

## Task Manager State Machine Pattern

**Best Practice**: Organize scripts using Task classes for clean separation of concerns.

### Base Task Class
```java
// Base Task class
public abstract class Task {
    protected Script script;

    public Task(Script script) {
        this.script = script;
    }

    public abstract boolean activate();    // Should this task run?
    public abstract boolean execute();     // Run the task logic
}
```

### Example Task Implementation
```java
// BankTask
public class BankTask extends Task {
    public BankTask(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // Activate when inventory is full
        ItemGroupResult inv = script.getWidgetManager()
            .getInventory().search(Set.of());
        return inv != null && inv.isFull();
    }

    @Override
    public boolean execute() {
        Bank bank = script.getWidgetManager().getBank();

        if (!bank.isVisible()) {
            return openBank();
        }

        // Deposit all except tools
        bank.depositAll(Set.of(ItemID.BRONZE_AXE));
        script.pollFramesHuman(() -> false, script.random(300, 600));
        bank.close();

        return false; // Return to main loop
    }

    private boolean openBank() {
        // Bank opening logic...
        return false;
    }
}
```

### Main Script with Task Manager
```java
public class MyScript extends Script {
    private List<Task> tasks;

    @Override
    public void onStart() {
        tasks = Arrays.asList(
            new BankTask(this),
            new ChopTask(this),
            new WalkTask(this)
        );
    }

    @Override
    public int poll() {
        // Execute first activated task
        for (Task task : tasks) {
            if (task.activate()) {
                task.execute();
                return 0;  // Task handles its own timing
            }
        }
        return 600; // Default sleep
    }
}
```

**Benefits**:
- Clean separation of concerns
- Easy to add new tasks
- Tasks are self-contained and testable
- Priority ordering (first matching task wins)

---

## Advanced Walking with Idle Detection

Track position changes to detect if player got stuck during walking.

```java
AtomicReference<Timer> positionChangeTimer = new AtomicReference<>(new Timer());
AtomicReference<WorldPosition> pos = new AtomicReference<>(null);

pollFramesHuman(() -> {
    WorldPosition current = getWorldPosition();
    if (current == null) return false;

    if (!current.equals(pos.get())) {
        pos.set(current);
        positionChangeTimer.get().reset(); // Reset if moving
    }

    // Fail if idle for 4+ seconds
    return getWidgetManager().getBank().isVisible()
            || positionChangeTimer.get().timeElapsed() > 4000;
}, 20000);
```

**Use Case**: Detect when player gets stuck on obstacles or fails to path correctly.

---

## Polygon Safety Check Before Tapping

Ensure all polygon points are within screen bounds before clicking.

```java
private boolean isPolygonTapSafe(Polygon poly) {
    int screenWidth = getScreen().getWidth();
    int screenHeight = getScreen().getHeight();

    for (int i = 0; i < poly.size(); i++) {
        int x = poly.xpoints[i];
        int y = poly.ypoints[i];

        if (x < 0 || x > screenWidth || y < 0 || y > screenHeight) {
            return false;
        }
    }
    return true;
}
```

**Usage**:
```java
Polygon hull = object.getConvexHull();
if (hull != null && isPolygonTapSafe(hull)) {
    getFinger().tap(hull, "Mine");
} else {
    // Walk closer
}
```

---

## Multi-Step Dialogue with Level-Up Handling

Handle crafting activities that can be interrupted by level-up dialogues.

```java
private boolean waitUntilFinishedCrafting() {
    Timer craftingTimer = new Timer();

    BooleanSupplier condition = () -> {
        // Check for level-up dialogue
        DialogueType type = getWidgetManager().getDialogue().getDialogueType();
        if (type == DialogueType.TAP_HERE_TO_CONTINUE) {
            log(getClass(), "Level up detected!");
            getWidgetManager().getDialogue().continueChatDialogue();
            pollFramesHuman(() -> false, random(1000, 3000));
            return true;
        }

        // Timeout after random duration
        if (craftingTimer.timeElapsed() > random(70000, 78000)) {
            return true;
        }

        // Check if we ran out of materials
        ItemGroupResult inv = getWidgetManager().getInventory()
            .search(Set.of(ItemID.MAHOGANY_PLANK));
        if (inv == null) return false;
        return !inv.contains(ItemID.MAHOGANY_PLANK);
    };

    return pollFramesHuman(condition, random(70000, 78000));
}
```

**Key Points**:
- Detects level-up mid-crafting
- Times out if materials run out
- Adds randomization to timeout duration

---

## Randomized Teleport Cooldown (Anti-Ban)

Add variance to spell cooldowns to appear more human-like.

```java
private static final int TELEPORT_COOLDOWN_MS = 1800;
private long lastTeleportTime = 0;

private boolean castTeleportSpell() {
    // Wait for cooldown
    long remaining = getCooldownForSpell() -
        (System.currentTimeMillis() - lastTeleportTime);

    if (remaining > 0) {
        pollFramesUntil(
            () -> (System.currentTimeMillis() - lastTeleportTime) >= getCooldownForSpell(),
            (int) remaining + 100
        );
        return false;
    }

    // Cast the spell
    boolean success = getWidgetManager().getSpellbook().selectSpell(
        Spell.VARROCK_TELEPORT, null
    );

    if (success) {
        lastTeleportTime = System.currentTimeMillis();
    }

    return success;
}

// Weighted random cooldown distribution
private long getCooldownForSpell() {
    int roll = random(100);

    if (roll < 50) {
        return random(1800, 1901);  // ~1.8-1.9s (50% chance)
    } else if (roll < 90) {
        return random(1850, 2001);  // ~1.85-2.0s (40% chance)
    } else {
        return random(1900, 2301);  // ~1.9-2.3s (10% chance)
    }
}
```

**Benefits**:
- Mimics human reaction time variance
- Prevents perfectly timed casts
- Weighted distribution feels natural

---

## Custom Interface Detection with OCR

For minigames or special interfaces not covered by standard API.

```java
public class MushroomTransportInterface extends CustomInterface {
    private static final String TITLE_TEXT = "Bittercap Mushrooms";
    private static final Rectangle TITLE_BOUNDS = new Rectangle(10, 5, 200, 20);
    private static final SearchablePixel TITLE_COLOR =
        new SearchablePixel(25, 100, 50, 10, 10, 10);

    private Map<ButtonType, Rectangle> buttons = null;

    public enum ButtonType {
        VERDANT_VALLEY("Verdant Valley"),
        MUSHROOM_MEADOW("Mushroom Meadow");

        private final String text;
        ButtonType(String text) { this.text = text; }
        public String getText() { return text; }
    }

    @Override
    public boolean isVisible() {
        Rectangle bounds = getBounds();
        if (bounds == null) return false;

        Rectangle titleArea = bounds.getSubRectangle(TITLE_BOUNDS);
        String text = core.getOCR().getText(
            Font.STANDARD_FONT_BOLD,
            titleArea,
            TITLE_COLOR
        );

        boolean visible = text.equalsIgnoreCase(TITLE_TEXT);

        if (visible && buttons == null) {
            this.buttons = registerButtons(bounds);
        }
        return visible;
    }

    private Map<ButtonType, Rectangle> registerButtons(Rectangle bounds) {
        Map<ButtonType, Rectangle> found = new HashMap<>();

        // Find containers by corner sprites
        List<Rectangle> containers = core.getImageAnalyzer()
            .findContainers(bounds, 913, 914, 915, 916);

        for (Rectangle container : containers) {
            String rawText = core.getOCR().getText(
                Font.SMALL_FONT, container, BUTTON_TEXT_COLOR
            ).trim();

            // Normalize OCR errors (I -> l is common)
            String normalized = rawText.replace('I', 'l').toLowerCase();

            for (ButtonType btn : ButtonType.values()) {
                if (normalized.equals(btn.getText().toLowerCase())) {
                    found.put(btn, new Rectangle(
                        container.x - bounds.x,
                        container.y - bounds.y,
                        container.width,
                        container.height
                    ));
                    break;
                }
            }
        }
        return found;
    }

    public boolean selectOption(ButtonType btn) {
        Rectangle screenBounds = getButtonScreenBounds(btn);
        if (screenBounds == null) return false;

        if (core.getFinger().tap(screenBounds)) {
            return core.pollFramesUntil(() -> !isVisible(), 5000);
        }
        return false;
    }
}
```

**Use Cases**:
- Minigame interfaces
- Quest dialogues
- Special teleport menus
- Any interface without standard API support

---

## Waiting Patterns with AFK Control

Prevent AFK detection during critical operations.

```java
private boolean allowAFK = true;

@Override
public boolean canAFK() {
    return allowAFK;
}

// In critical section:
allowAFK = false;
pollFramesUntil(() -> criticalOperation(), 10000);
allowAFK = true;
```

**Why**: During sensitive operations (dialogue sequences, banking), you don't want the AFK handler to interrupt.

---

## Multi-Object Search with Filtering

Find the best object from multiple options based on custom criteria.

```java
// Find closest reachable bank
List<RSObject> allBanks = getObjectManager().getObjects(obj ->
    obj.getName().contains("Bank") &&
    obj.canReach() &&
    obj.getActions() != null &&
    Arrays.asList(obj.getActions()).contains("Bank")
);

// Sort by distance
RSObject closestBank = allBanks.stream()
    .min(Comparator.comparingInt(obj ->
        obj.getWorldPosition().distanceTo(getWorldPosition())
    ))
    .orElse(null);
```

**Benefits**:
- Finds best option, not just first
- Applies multiple filters
- Handles edge cases (unreachable, wrong actions)

---

## Visibility-Based Banking

Only interact with bank when fully visible, walk closer if needed.

```java
private boolean openBankSafely() {
    RSObject chest = getObjectManager().getClosestObject(
        getWorldPosition(), "Bank chest"
    );

    if (chest == null) return false;

    Polygon hull = chest.getConvexHull();
    if (hull == null) return false;

    // Check visibility (ignoring chatbox)
    double visibility = getWidgetManager().insideGameScreenFactor(
        hull, List.of(ChatboxComponent.class)
    );

    if (visibility < 1.0) {
        // Not fully visible - walk closer
        WalkConfig config = new WalkConfig.Builder()
            .disableWalkScreen(true)
            .breakCondition(() -> {
                Polygon h = chest.getConvexHull();
                return h != null &&
                    getWidgetManager().insideGameScreenFactor(
                        h, List.of(ChatboxComponent.class)) >= 1.0;
            })
            .enableRun(true)
            .build();
        getWalker().walkTo(chest.getWorldPosition(), config);
        return false;
    }

    // Verify polygon is safe to tap
    if (!isPolygonTapSafe(hull)) {
        return false;
    }

    getFinger().tap(hull, "Bank");
    return true;
}
```

**Prevents**: Clicking off-screen polygons, misclicks on adjacent objects.

---

## Best Practices from Production Scripts

1. **Use Task pattern** for complex scripts (3+ states)
2. **Always check visibility** before clicking objects
3. **Track position changes** to detect stuck player
4. **Handle level-ups** in crafting/training loops
5. **Randomize cooldowns** for repetitive actions
6. **Use OCR for custom interfaces** when needed
7. **Control AFK behavior** during critical sections
8. **Filter objects** by multiple criteria, not just name
9. **Verify polygon safety** before tapping
10. **Add idle detection** to walking sequences

---

*These patterns are proven in production scripts with thousands of hours runtime.*
