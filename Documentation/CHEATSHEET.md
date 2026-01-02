# OSMB API Quick Reference Cheat Sheet

> Quick lookup for the most commonly used OSMB API methods and patterns

---

## Script Lifecycle

```java
@ScriptManifest(name = "Name", author = "Author", version = 1.0, description = "Desc")
public class MyScript extends Script {
    
    public void onStart() { }        // Called once at start
    public int poll() { return 600; } // Main loop, return sleep ms
    public void onStop() { }          // Called on stop
    
    // Optional overrides
    public void onGameStateChanged(GameState newState) { }
    public void onLevelUp(SkillType skill, int newLevel) { }
}
```

---

## Core Getters (from ScriptCore)

| Method | Returns | Description |
|--------|---------|-------------|
| `getScreen()` | `Screen` | Screen capture and dimensions |
| `getPixelAnalyzer()` | `PixelAnalyzer` | Color/pixel search |
| `getOCR()` | `OCR` | Text recognition |
| `getObjectManager()` | `ObjectManager` | Find game objects |
| `getSceneManager()` | `SceneManager` | NPCs, ground items, tiles |
| `getWidgetManager()` | `WidgetManager` | UI components |
| `getWalker()` | `Walker` | Pathfinding/walking |
| `getFinger()` | `Finger` | Mouse/touch input |
| `getKeyboard()` | `Keyboard` | Keyboard input |
| `getItemManager()` | `ItemManager` | Item definitions |
| `getProfileManager()` | `ProfileManager` | AFK handling |
| `getWorldPosition()` | `WorldPosition` | Player world coords |
| `getLocalPosition()` | `LocalPosition` | Player local coords |

---

## Widget Manager Quick Access

```java
WidgetManager widgets = getWidgetManager();

// UI Components
Bank bank = widgets.getBank();
Inventory inventory = widgets.getInventory();
Equipment equipment = widgets.getEquipment();
Prayer prayer = widgets.getPrayer();
Dialogue dialogue = widgets.getDialogue();
Chatbox chatbox = widgets.getChatbox();
TabManager tabs = widgets.getTabManager();
Minimap minimap = widgets.getMinimap();
```

---

## Finding Objects

```java
ObjectManager objects = getObjectManager();

// By predicate (recommended)
RSObject obj = objects.getRSObject(o -> o.getName().equals("Bank booth"));
Optional<RSObject> opt = objects.getObject(o -> o.getId() == 12345);
List<RSObject> all = objects.getObjects(o -> o.getName().contains("Tree"));

// Closest by name
RSObject closest = objects.getClosestObject(getWorldPosition(), "Tree", "Oak tree");
```

---

## Finding NPCs

```java
SceneManager scene = getSceneManager();

// By predicate
RSNPC npc = scene.getNpc(n -> n.getName().equals("Guard"));
List<RSNPC> npcs = scene.getNpcs(n -> n.getCombatLevel() > 10);
```

---

## Interacting with Entities

```java
// Basic interaction
if (object.interact("Chop down")) {
    submitTask(() -> !object.exists(), 5000);
}

// With verification
if (npc.interact(null, null, "Talk-to")) {
    submitTask(() -> dialogue.isVisible(), 5000);
}

// Get available actions
String[] actions = object.getActions(); // ["Open", "Examine", etc.]
```

---

## Bank Operations

```java
Bank bank = getWidgetManager().getBank();

// Check state
bank.isVisible();              // Is bank open?
bank.getFreeBankSlots();       // Available slots

// Deposits
bank.deposit(itemId, amount);  // Deposit specific amount
bank.depositAll(Set.of());     // Deposit everything
bank.depositAll(Set.of(995));  // Deposit all except coins
bank.depositAllIgnoreSlots(Set.of(0, 1)); // Keep slots 0,1

// Withdrawals  
bank.withdraw(itemId, amount);

// Tab management
bank.getSelectedTabIndex();
bank.setSelectedTabIndex(0);   // Main tab

// Close
bank.close();
```

---

## Inventory Operations

```java
Inventory inv = getWidgetManager().getInventory();

// Check state
inv.isVisible();
inv.isFull();
inv.getFreeSlots();

// Find items
Item item = inv.getItem(itemId);
List<Item> items = inv.getItems(id -> id == 995);
int count = inv.getCount(itemId);
boolean has = inv.contains(itemId);

// Item interaction
item.interact("Use");
item.interact("Drop");
```

---

## Item Groups (Common interface for Bank/Inventory)

```java
// Both Bank and Inventory implement ItemGroup
ItemGroup container = bank; // or inventory

// Getting items
Item item = container.getItem(itemId);
List<Item> items = container.getItems();
boolean has = container.contains(itemId);
int count = container.getCount(itemId);

// Item operations
ItemGroupResult result = container.getTappableItem(item);
```

---

## Walking & Navigation

```java
Walker walker = getWalker();

// Walk to position
WorldPosition dest = new WorldPosition(3222, 3218, 0);
walker.walkTo(dest);

// With config
WalkConfig config = WalkConfig.builder()
    .breakCondition(() -> someCondition())
    .tileRandomisation(2)
    .build();
walker.walkTo(dest, config);

// Current position
WorldPosition pos = getWorldPosition();
int distance = pos.distanceTo(dest);
```

---

## Conditional Waiting (submitTask)

```java
// Wait for condition (returns true if condition met, false if timeout)
boolean success = submitTask(
    () -> bank.isVisible(),  // Condition to check
    5000                      // Timeout in ms
);

// With custom poll interval
submitTask(() -> condition(), 5000, 100);

// Example usage pattern
if (object.interact("Open")) {
    if (submitTask(() -> bank.isVisible(), 5000)) {
        // Bank opened successfully
    }
}
```

---

## Dialogue Handling

```java
Dialogue dialogue = getWidgetManager().getDialogue();

// Check state
dialogue.isVisible();
DialogueType type = dialogue.getDialogueType();

// Handle different types
dialogue.clickContinue();           // NPC_DIALOGUE, PLAYER_DIALOGUE
dialogue.selectOption("Yes");       // OPTION
dialogue.typeInput("123");          // ITEM_OPTION, SKILL
```

---

## Prayer & Skills

```java
Prayer prayer = getWidgetManager().getPrayer();

// Toggle prayer
prayer.setPrayer(Prayer.Type.PROTECT_MELEE, true);
prayer.isPrayerActive(Prayer.Type.PROTECT_MELEE);

// Check points
int points = widgets.getMinimap().getPrayerOrb().getValue();

// Skills
Skill skill = widgets.getSkills();
int level = skill.getLevel(SkillType.ATTACK);
int xp = skill.getExperience(SkillType.ATTACK);
```

---

## OCR Text Reading

```java
OCR ocr = getOCR();

// Read text in region
Rectangle area = new Rectangle(100, 100, 200, 30);
String text = ocr.getText(area);

// With specific font
String text = ocr.getText(area, FontType.SMALL);
String text = ocr.getText(area, FontType.BOLD);

// Parse numbers
int value = ocr.readNumber(area);
```

---

## Color & Pixel Analysis

```java
PixelAnalyzer pixels = getPixelAnalyzer();

// Find color
Rectangle area = new Rectangle(0, 0, 500, 500);
Point found = pixels.findColor(area, new Color(255, 0, 0));

// With tolerance
Point found = pixels.findColor(area, new Color(255, 0, 0), 10);

// Check if color exists
boolean exists = pixels.colorExists(area, targetColor);
```

---

## Screen Capture

```java
Screen screen = getScreen();

// Get dimensions
int width = screen.getWidth();
int height = screen.getHeight();

// Get screenshot
BufferedImage img = screen.getScreenshot();
BufferedImage region = screen.getScreenshot(rectangle);
```

---

## Input (Finger/Mouse)

```java
Finger finger = getFinger();

// Tap at point
finger.tap(new Point(100, 200));
finger.tap(rectangle);  // Random point in rectangle

// Tap with type
finger.tap(point, TouchType.TAP);
finger.tap(point, TouchType.LONG_TAP);

// Swipe
finger.swipe(startPoint, endPoint);
```

---

## Keyboard

```java
Keyboard keyboard = getKeyboard();

// Type text
keyboard.type("Hello world");

// Press key
keyboard.press(KeyEvent.VK_ENTER);
keyboard.press(KeyEvent.VK_ESCAPE);

// Hold key
keyboard.hold(KeyEvent.VK_SHIFT);
keyboard.release(KeyEvent.VK_SHIFT);
```

---

## Positions & Areas

```java
// World Position (global coordinates)
WorldPosition pos = new WorldPosition(3222, 3218, 0);
WorldPosition current = getWorldPosition();
int distance = current.distanceTo(pos);

// Local Position (scene-relative)
LocalPosition local = getLocalPosition();

// Areas
RectangleArea rect = new RectangleArea(3220, 3215, 3225, 3220, 0);
PolyArea poly = new PolyArea(new WorldPosition[]{...});
boolean inside = area.contains(getWorldPosition());
```

---

## Timing Utilities

```java
// Random delays
Utils utils = getUtils();
int delay = utils.random(100, 500);
utils.sleep(100, 300);  // Random sleep between range

// Stopwatch
Stopwatch timer = new Stopwatch();
timer.start();
long elapsed = timer.getElapsedTime();
timer.reset();
```

---

## GameState Checking

```java
// Check current game state
GameState state = getWidgetManager().getGameState();

if (state == GameState.LOGGED_IN) {
    // Do stuff
}

// Handle state changes
@Override
public void onGameStateChanged(GameState newState) {
    if (newState == GameState.LOGIN_SCREEN) {
        // Handle disconnect
    }
}
```

---

## XP Tracking

```java
// Get XP trackers
Map<SkillType, XPTracker> trackers = getXPTrackers();
XPTracker tracker = trackers.get(SkillType.WOODCUTTING);

// Track gains
int gained = tracker.getGainedXP();
int xpPerHour = tracker.getXPPerHour();
```

---

## Common Patterns

### Safe Interaction Pattern
```java
if (object != null && object.interact("Use")) {
    submitTask(() -> expectedResult(), 5000);
}
```

### Null-Safe Widget Check
```java
Bank bank = getWidgetManager().getBank();
if (bank != null && bank.isVisible()) {
    // Safe to use bank
}
```

### Wait After Action
```java
object.interact("Chop down");
submitTask(() -> inventory.isFull() || !object.exists(), 60000);
```

### Distance Check
```java
if (getWorldPosition().distanceTo(destination) > 10) {
    getWalker().walkTo(destination);
}
```

---

## UIResult Pattern

Many UI methods return `UIResult<T>` for safe handling:

```java
UIResult<Integer> result = bank.getSelectedTabIndex();

if (result.isFound()) {
    int tabIndex = result.get();
}

// Or check state
if (result == UIResult.NOT_VISIBLE) {
    // Bank not open
}
```

---

*Use this cheat sheet as a quick reference while developing OSMB scripts.*
