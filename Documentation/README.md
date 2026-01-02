# OSMB API Knowledge Base

> **Old School Mobile Bot (OSMB) API Reference**
> 
> This knowledge base provides comprehensive documentation for the OSMB scripting API.
> Designed for use with LLMs like Claude Code and Context7 to assist in writing OSMB scripts.

---

## 📚 Documentation Guides

| Guide | Description |
|-------|-------------|
| [CHEATSHEET.md](./CHEATSHEET.md) | Quick reference for all common API methods and patterns |
| [EXAMPLES.md](./EXAMPLES.md) | Complete working script examples for various use cases |
| [PATTERNS.md](./PATTERNS.md) | Design patterns, best practices, and anti-patterns to avoid |

---

## Quick Reference

| Module | Description | Classes |
|--------|-------------|---------|
| [core](./core.md) | Core interfaces and classes that form the foundation of OSMB... | 13 |
| [definition](./definition.md) | Game definitions for items, objects, NPCs, etc. | 3 |
| [input](./input.md) | Mouse (Finger) and keyboard input handling | 7 |
| [item](./item.md) | Items, item groups, and inventory interaction | 12 |
| [location](./location.md) | Position and area handling for world navigation | 7 |
| [profile](./profile.md) | Player profiles and AFK handling | 3 |
| [scene](./scene.md) | Scene management, NPCs, game objects, and ground items | 9 |
| [script](./script.md) | Script framework including Script base class, configurations... | 9 |
| [shape](./shape.md) | Geometric shapes for area definitions | 5 |
| [trackers](./trackers.md) | XP tracking, item tracking, and other monitoring systems | 2 |
| [ui-bank](./ui-bank.md) | Bank interface and related components | 5 |
| [ui-chatbox](./ui-chatbox.md) | Chatbox, dialogue handling, and chat components | 8 |
| [ui-inventory](./ui-inventory.md) | Inventory management and tab components | 2 |
| [ui-minimap](./ui-minimap.md) | Minimap, compass, and status orbs | 15 |
| [ui-other](./ui-other.md) | Other UI components | 21 |
| [ui-overlay](./ui-overlay.md) | Overlay and buff/status display components | 9 |
| [ui-spellbook](./ui-spellbook.md) | Spellbook interfaces for all magic types | 8 |
| [ui-tabs](./ui-tabs.md) | Tab management (Skills, Equipment, Prayer, etc.) | 34 |
| [ui-widgets](./ui-widgets.md) | Widget management and sprite handling | 3 |
| [utils](./utils.md) | Utility classes for timing, conditions, and common operation... | 14 |
| [visual-color](./visual-color.md) | Color detection and pixel analysis | 8 |
| [visual-image](./visual-image.md) | Image analysis and drawing utilities | 19 |
| [visual-ocr](./visual-ocr.md) | Optical character recognition for text reading | 5 |
| [walker](./walker.md) | Web walking and pathfinding systems | 10 |

---

## Getting Started

### Basic Script Structure

Every OSMB script extends the `Script` class and uses the `@ScriptManifest` annotation:

```java
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;

@ScriptManifest(
    name = "My Script",
    author = "Author Name", 
    version = 1.0,
    description = "What the script does"
)
public class MyScript extends Script {
    
    @Override
    public void onStart() {
        // Called once when script starts
        log("Script started!");
    }
    
    @Override
    public int poll() {
        // Main loop - called repeatedly
        // Return sleep time in milliseconds
        return 600;
    }
    
    @Override
    public void onStop() {
        // Called when script stops
        log("Script stopped!");
    }
}
```

### Accessing API Components

The `Script` class extends `ScriptCore`, giving access to all API managers:

```java
// Screen and visual
Screen screen = getScreen();
PixelAnalyzer pixels = getPixelAnalyzer();
OCR ocr = getOCR();

// Scene entities
ObjectManager objects = getObjectManager();
SceneManager scene = getSceneManager();

// UI components  
WidgetManager widgets = getWidgetManager();
Bank bank = widgets.getBank();
Inventory inventory = widgets.getInventory();

// Input
Finger finger = getFinger();
Keyboard keyboard = getKeyboard();

// Navigation
Walker walker = getWalker();
WorldPosition pos = getWorldPosition();

// Items
ItemManager items = getItemManager();
```

---

## Common Patterns

### Finding and Interacting with Game Objects

```java
// Find nearest tree
RS2Object tree = getObjectManager().getClosest(
    obj -> obj.getName().equals("Tree")
);

// Interact with object
if (tree != null && tree.interact("Chop down")) {
    submitTask(() -> !tree.exists(), 5000);
}
```

### Bank Operations

```java
Bank bank = getWidgetManager().getBank();

// Check if bank is open
if (bank.isVisible()) {
    // Deposit all items
    bank.depositAll(Set.of()); // Empty set = deposit everything
    
    // Withdraw items
    bank.withdraw(ITEM_ID, 10);
    
    // Close bank
    bank.close();
}
```

### Inventory Management

```java
Inventory inv = getWidgetManager().getInventory();

// Check inventory
if (inv.isFull()) {
    log("Inventory is full!");
}

// Find item
Item item = inv.getItem(ITEM_ID);
if (item != null) {
    item.interact("Use");
}

// Count items
int count = inv.getCount(ITEM_ID);
```

### Walking to Locations

```java
WorldPosition destination = new WorldPosition(3222, 3218, 0);

// Walk to destination
if (getWalker().walkTo(destination)) {
    submitTask(() -> getWorldPosition().distanceTo(destination) < 3, 30000);
}
```

### Conditional Waiting (submitTask)

```java
// Wait for condition with timeout
boolean success = submitTask(
    () -> getWidgetManager().getBank().isVisible(),  // Condition
    5000  // Timeout in ms
);

// With custom polling interval
submitTask(() -> someCondition(), 5000, 100);
```

### Reading Text with OCR

```java
OCR ocr = getOCR();

// Read text in area
Rectangle area = new Rectangle(100, 100, 200, 50);
String text = ocr.getText(area, FontType.SMALL);
```

### Color Detection

```java
PixelAnalyzer pixels = getPixelAnalyzer();

// Find color in area
Rectangle searchArea = new Rectangle(0, 0, 500, 500);
Point found = pixels.findColor(searchArea, new Color(255, 0, 0));
```

---

## Module Documentation

### [Core](./core.md)

Core interfaces and classes that form the foundation of OSMB scripts

**Key classes:** `ScriptCore`, `World`, `Screen`, `Class Location`, `JavaFXUtils`

### [Definition](./definition.md)

Game definitions for items, objects, NPCs, etc.

**Key classes:** `SpriteDefinition`, `MapDefinition`, `ItemDefinition`

### [Input](./input.md)

Mouse (Finger) and keyboard input handling

**Key classes:** `Finger`, `MenuEntry`, `Class PhysicalKey`, `Class TouchType`, `Class EntityType`

### [Item](./item.md)

Items, item groups, and inventory interaction

**Key classes:** `ItemGroupResult`, `ItemGroup`, `ItemManager`, `ItemSearchResult`, `ItemImage`

### [Location](./location.md)

Position and area handling for world navigation

**Key classes:** `Position`, `RectangleArea`, `Location3D`, `Area`, `PolyArea`

### [Profile](./profile.md)

Player profiles and AFK handling

**Key classes:** `AFKTime`, `ProfileManager`, `WorldProvider`

### [Scene](./scene.md)

Scene management, NPCs, game objects, and ground items

**Key classes:** `RSObject`, `SceneManager`, `RSTile`, `CollisionMap`, `Class ObjectType`

### [Script](./script.md)

Script framework including Script base class, configurations, and task system

**Key classes:** `Script`, `ScriptOptions`, `Class SkillCategory`, `TaskInterruptedException`, `ScriptHeader`

### [Shape](./shape.md)

Geometric shapes for area definitions

**Key classes:** `Rectangle`, `Polygon`, `Triangle`, `Shape`, `Line`

### [Trackers](./trackers.md)

XP tracking, item tracking, and other monitoring systems

**Key classes:** `XPTracker`, `XPDropsListener`

### [Ui Bank](./ui-bank.md)

Bank interface and related components

**Key classes:** `Bank`, `Class QuantityBankButtonType`, `BankButton`, `Class BankButtonType`, `BankButtonIdentityType`

### [Ui Chatbox](./ui-chatbox.md)

Chatbox, dialogue handling, and chat components

**Key classes:** `LogoutTabComponent`, `DialogueComponent`, `ChatboxComponent`, `Dialogue`, `ChatboxTab`

### [Ui Inventory](./ui-inventory.md)

Inventory management and tab components

**Key classes:** `InventoryTabComponent`, `Inventory`

### [Ui Minimap](./ui-minimap.md)

Minimap, compass, and status orbs

**Key classes:** `MinimapComponent`, `OrbManager`, `MinimapOrbs`, `MinimapOrb`, `Minimap`

### [Ui Other](./ui-other.md)

Other UI components

**Key classes:** `ComponentChild<T>`, `ComponentParent<T>`, `ComponentCentered`, `MiniMenu`, `DepositBox`

### [Ui Overlay](./ui-overlay.md)

Overlay and buff/status display components

**Key classes:** `OverlayBoundary`, `HealthOverlay`, `BuffOverlay`, `Class OverlayPosition`, `OverlayValueFinder<T>`

### [Ui Spellbook](./ui-spellbook.md)

Spellbook interfaces for all magic types

**Key classes:** `Class AncientSpellbook`, `Class LunarSpellbook`, `Class StandardSpellbook`, `Class ArceuusSpellbook`, `Class SpellbookType`

### [Ui Tabs](./ui-tabs.md)

Tab management (Skills, Equipment, Prayer, etc.)

**Key classes:** `SquareTabComponent`, `SettingsTabComponent`, `HotKeyTabComponent`, `SpellbookTabComponent`, `Tab`

### [Ui Widgets](./ui-widgets.md)

Widget management and sprite handling

**Key classes:** `WidgetManager`, `SpriteManager`, `SpriteID`

### [Utils](./utils.md)

Utility classes for timing, conditions, and common operations

**Key classes:** `Utils`, `UIResultList<T>`, `RandomUtils`, `UIResult<T>`, `Stopwatch`

### [Visual Color](./visual-color.md)

Color detection and pixel analysis

**Key classes:** `ColorUtils`, `ChannelThresholdComparator`, `HSLPalette`, `SingleThresholdComparator`, `ToleranceComparator`

### [Visual Image](./visual-image.md)

Image analysis and drawing utilities

**Key classes:** `Canvas`, `PixelAnalyzer`, `Image`, `SceneProjector`, `ImageAnalyzer`

### [Visual Ocr](./visual-ocr.md)

Optical character recognition for text reading

**Key classes:** `Class Font`, `RSFontChar`, `RSFont`, `OCR`, `Font.Loader`

### [Walker](./walker.md)

Web walking and pathfinding systems

**Key classes:** `WalkConfig`, `WalkConfig.Builder`, `Walker`, `Class Direction`, `Node`

---

## API Design Notes

- **Interfaces vs Classes**: Most API components are interfaces (e.g., `Bank`, `Inventory`) implemented internally
- **UIResult Pattern**: Many UI methods return `UIResult<T>` for safe null handling
- **Interactable Interface**: Game entities implement `Interactable` for common interaction methods
- **Position System**: Use `WorldPosition` for global coords, `LocalPosition` for scene-relative coords
- **Conditional Operations**: Use `submitTask()` for waiting on conditions with timeouts

---

*Generated from OSMB API v1.0*
