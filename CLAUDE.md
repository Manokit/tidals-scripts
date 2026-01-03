# OSMB Script Development Guide for Claude Code

> **CRITICAL: OSMB is a COLOR BOT** - It uses visual/pixel detection, NOT injection. You cannot access game memory directly. All detection is done through screen analysis, color matching, and OCR.

## Quick Reference

### Script Structure
```java
@ScriptManifest(name = "Name", author = "Author", version = 1.0, description = "Description")
public class MyScript extends Script {
    @Override
    public void onStart() { }           // Called once at startup
    
    @Override  
    public int poll() { return 600; }   // Main loop - return sleep ms
    
    @Override
    public void onStop() { }            // Called on stop
    
    // Optional overrides
    public boolean canAFK() { return true; }         // Control AFK behavior
    public boolean canBreak() { return true; }       // Control break behavior  
    public boolean canHopWorlds() { return true; }   // Control world hop behavior
    public void onNewFrame() { }                     // Called each frame (READ-ONLY ops only!)
}
```

### Core API Access (from Script/ScriptCore)
```java
getWidgetManager()      // UI: Bank, Inventory, Dialogue, Tabs, Minimap
getObjectManager()      // Find RSObjects (trees, rocks, banks, etc.)
getSceneManager()       // NPCs, ground items, tiles
getWalker()             // Pathfinding and walking
getFinger()             // Mouse/touch input
getKeyboard()           // Keyboard input
getPixelAnalyzer()      // Color/pixel detection
getOCR()                // Text recognition
getItemManager()        // Item definitions
getWorldPosition()      // Player's world coordinates
getLocalPosition()      // Player's local/scene coordinates
getScreen()             // Screen capture, dimensions
getSceneProjector()     // 3D to 2D projection (tile cubes, polygons)
```

---

## CRITICAL CONCEPTS FOR COLOR BOTTING

### 1. Items with Identical Sprites CANNOT Be Distinguished
```java
// WRONG - Waterskin(0) through (4) look identical!
inventory.search(Set.of(ItemID.WATERSKIN_4)); // Won't distinguish charges!

// CORRECT - Use BuffOverlay for items with charges
BuffOverlay waterskinBuff = new BuffOverlay(core, WATERSKIN_ID);
String charges = waterskinBuff.getText(); // Read the overlay text

// Seeds, jewelry charges, etc. - same problem
// Solution: Use right-click menu, overlays, or be creative
```

### 2. HSL vs RGB Color Detection
```java
// HSL comparators are superior for gamescreen detection
// RGB works better for minimap

// For gamescreen objects - use HSL
SearchablePixel pixel = new SearchablePixel(hue, saturation, lightness, tolerance);

// For minimap - RGB is acceptable
```

### 3. Visual Door Detection (Collision Map is Static!)
```java
// The collision map shows DEFAULT world state, NOT real-time
// You MUST visually check if doors are open/closed

// WRONG - Assuming collision map is accurate
if (collisionMap.isBlocked(doorTile)) { /* door is closed */ }

// CORRECT - Visual verification via menu
MenuEntry response = getFinger().tapGetResponse(true, doorPoly);
if (response != null && response.getAction().equalsIgnoreCase("Open")) {
    // Door is closed, open it
}
```

### 4. NPC Interaction Pattern (NPCs move!)
```java
// NPCs aren't in ObjectManager - get positions from minimap
List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

for (WorldPosition npcPos : npcPositions) {
    // Create tile cube at NPC position
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60); // 60 = height
    if (tileCube == null) continue;
    
    // For 2x2 NPCs, increase tile dimensions
    Polygon largeCube = getSceneProjector().getTileCube(
        npcPos.getX(), npcPos.getY(), 0,  // position
        2, 2,  // tile width, height (for 2x2 NPC)
        0, 60, // z offset, height
        false  // require all vertices on screen
    );
    
    // Get highlighted bounds for interaction
    Rectangle bounds = getPixelAnalyzer().getHighlightBounds(tileCube, highlightColors);
    if (bounds != null) {
        getFinger().tap(bounds, "Attack");
    }
}
```

### 5. Verify Interactions with tapGetResponse
```java
// BETTER than blind clicking - verify what was actually clicked
MenuEntry response = getFinger().tapGetResponse(true, objectPoly);

if (response != null) {
    String action = response.getAction();
    String entity = response.getEntityName();
    
    if (action.equalsIgnoreCase("Mine")) {
        log("Successfully clicked mine action on " + entity);
        // Now wait for mining to complete
    } else {
        log("Misclicked! Got action: " + action);
        // Retry
    }
}
```

### 6. Shrink Click Areas to Avoid Misclicks
```java
// When zoomed out, object polys can overlap - shrink them
RSObject rock = getObjectManager().getClosestObject(getWorldPosition(), "Rocks");
Polygon rockPoly = rock.getConvexHull();

if (rockPoly != null) {
    // Shrink to 70% to avoid clicking adjacent objects
    Polygon shrunkPoly = rockPoly.getResized(0.7);
    if (shrunkPoly != null) {
        getFinger().tap(shrunkPoly, "Mine");
    }
}
```

---

## REGION AND MAP HANDLING

### Prioritize Regions for Fast Startup
```java
@Override
public ScriptOptions getScriptOptions() {
    return new ScriptOptions() {
        @Override
        public int[] regionsToPrioritise() {
            // Return region IDs where your script operates
            return new int[]{12850, 12851, 12594}; // Example: Lumbridge area
        }
    };
}
```

### Custom Maps for Instanced Areas
```java
// Required for: Tempoross, Pest Control, Temple Trekking, etc.
@Override
public void onStart() {
    // Full region is 63x63 tiles (0-indexed)
    addCustomMap(new MapDefinition(
        2624,  // baseX
        2560,  // baseY  
        63,    // width (use 64 if 63 doesn't work)
        63,    // height
        0,     // plane
        0      // flags
    ));
}

// Some instances need compass check disabled
@Override
public boolean skipYawCheck() {
    return true; // For areas without compass (Temple Trekking)
}
```

---

## COMMON PATTERNS

### State Machine Pattern
```java
private enum State { IDLE, GATHERING, BANKING, WALKING }

@Override
public int poll() {
    State state = getState();
    switch (state) {
        case GATHERING: return gather();
        case BANKING: return bank();
        case WALKING: return walk();
        default: return 600;
    }
}

private State getState() {
    Bank bank = getWidgetManager().getBank();
    Inventory inv = getWidgetManager().getInventory();
    
    if (bank.isVisible()) return State.BANKING;
    if (inv.isFull()) return State.WALKING; // to bank
    if (atGatheringSpot()) return State.GATHERING;
    return State.WALKING; // to spot
}
```

### Safe Banking
```java
private boolean openBank() {
    Bank bank = getWidgetManager().getBank();
    if (bank.isVisible()) return true;
    
    RSObject bankBooth = getObjectManager().getClosestObject(
        getWorldPosition(), "Bank booth", "Bank chest"
    );
    
    if (bankBooth != null && bankBooth.interact("Bank")) {
        return submitTask(() -> bank.isVisible(), 5000);
    }
    return false;
}

private boolean depositAllExcept(Set<Integer> keepItems) {
    Bank bank = getWidgetManager().getBank();
    if (!bank.isVisible()) return false;
    return bank.depositAll(keepItems);
}

// Search inventory - pass empty set to get all items
ItemGroupResult snapshot = getWidgetManager().getInventory().search(Set.of());
if (snapshot != null && snapshot.isFull()) {
    // Inventory is full
}
int freeSlots = snapshot.getFreeSlots();
```

### Waiting Patterns
```java
// submitTask - waits for condition with timeout
boolean success = submitTask(() -> bank.isVisible(), 5000);

// pollFramesUntil - similar but with frame-based polling  
pollFramesUntil(() -> someCondition(), 5000);

// IMPORTANT: Inside pollFramesUntil, canAFK() is checked
// Override canAFK() to false during critical sections:
private boolean allowAFK = true;

@Override
public boolean canAFK() {
    return allowAFK;
}

// In your task:
allowAFK = false;
pollFramesUntil(() -> criticalOperation(), 10000);
allowAFK = true;
```

### Chatbox Message Reading
```java
// In onNewFrame() - only use read-only methods!
@Override
public void onNewFrame() {
    updateChatBoxLines();
}

private List<String> previousLines = new ArrayList<>();

private void updateChatBoxLines() {
    Chatbox chatbox = getWidgetManager().getChatbox();
    if (chatbox.getActiveFilterTab() != ChatboxFilterTab.GAME) return;
    
    List<String> currentLines = chatbox.getLines();
    List<String> newLines = getNewLines(currentLines, previousLines);
    
    for (String line : newLines) {
        if (line.contains("You catch a")) {
            // Handle catch message
        } else if (line.contains("You fail")) {
            // Handle fail message
        }
    }
    
    previousLines = new ArrayList<>(currentLines);
}
```

### XP Tracking Utility Pattern
```java
// Create reusable XPTracking utility class
// Place in utils/XPTracking.java

package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import java.util.Map;

public class XPTracking {
    private final ScriptCore core;

    public XPTracking(ScriptCore core) {
        this.core = core;
    }

    // internal helper to retrieve a specific tracker
    private XPTracker getTracker(SkillType skill) {
        Map<SkillType, XPTracker> trackers = core.getXPTrackers();
        if (trackers == null) return null;
        return trackers.get(skill);
    }

    // skill-specific methods (example: woodcutting)
    public XPTracker getWoodcuttingTracker() {
        return getTracker(SkillType.WOODCUTTING);
    }

    public double getWoodcuttingXpGained() {
        XPTracker tracker = getWoodcuttingTracker();
        if (tracker == null) return 0.0;
        return tracker.getXpGained();
    }

    public int getWoodcuttingXpPerHour() {
        XPTracker tracker = getWoodcuttingTracker();
        if (tracker == null) return 0;
        return tracker.getXpPerHour();
    }

    public int getWoodcuttingLevel() {
        XPTracker tracker = getWoodcuttingTracker();
        if (tracker == null) return 0;
        return tracker.getLevel();
    }

    public String getWoodcuttingTimeToNextLevel() {
        XPTracker tracker = getWoodcuttingTracker();
        if (tracker == null) return "-";
        return tracker.timeToNextLevelString();
    }

    // optional: track xp gains for timeout detection
    public void checkXP() {
        // xp trackers update automatically
        // use this for custom logic like idle detection
        XPTracker tracker = getWoodcuttingTracker();
        if (tracker == null) return;
        
        double currentXp = tracker.getXpGained();
        // add your custom logic here
    }
}
```

**Usage in main script:**
```java
public class MyScript extends Script {
    private XPTracking xpTracking;
    
    @Override
    public void onStart() {
        xpTracking = new XPTracking(this);
    }
    
    @Override
    public void onNewFrame() {
        // optional: call checkXP for custom logic
        if (xpTracking != null) {
            xpTracking.checkXP();
        }
    }
    
    @Override
    public int poll() {
        // use in your script
        log("XP Gained: " + xpTracking.getWoodcuttingXpGained());
        log("XP/hr: " + xpTracking.getWoodcuttingXpPerHour());
        return 600;
    }
}
```

**Adapt for other skills:**
```java
// For crafting:
public XPTracker getCraftingTracker() {
    return getTracker(SkillType.CRAFTING);
}

// For mining:
public XPTracker getMiningTracker() {
    return getTracker(SkillType.MINING);
}

// For fishing:
public XPTracker getFishingTracker() {
    return getTracker(SkillType.FISHING);
}
```

**IMPORTANT**: XPTracker does NOT have a `checkXP()` method - trackers update automatically. The `checkXP()` method in your utility is for custom logic only.

### Respawn Circle Detection (for mining, fishing spots)
```java
// Find respawn circles in an area
List<RespawnCircle> circles = getPixelAnalyzer().findRespawnCircleTypes(searchBounds);

// Or use with objects
List<RSObject> rocks = getObjectManager().getObjects(o -> o.getName().equals("Rocks"));
List<RespawnCircleResult> activeRocks = getPixelAnalyzer().getRespawnCircleObjects(
    rocks,
    RespawnCircleDrawType.DRAW_ON_ACTIVE, // or DRAW_ON_DEPLETED
    zHeight,
    distanceTolerance
);
```

### Widget Visibility Check
```java
// Check how much of a polygon is visible on gamescreen
// Accounts for UI obstructions (chatbox, inventory, etc.)
Polygon objectPoly = object.getConvexHull();
double visibleFactor = getWidgetManager().insideGameScreenFactor(
    objectPoly, 
    List.of(ChatboxComponent.class) // Ignore chatbox obstruction
);

if (visibleFactor < 0.3) {
    // Less than 30% visible, walk closer or rotate camera
    return false;
}
```

### Walking with Break Conditions
```java
WalkConfig config = new WalkConfig.Builder()
    .breakCondition(() -> {
        // Stop walking when we find what we're looking for
        return getObjectManager().getClosestObject(getWorldPosition(), "Tree") != null;
    })
    .breakDistance(5)         // Stop within 5 tiles
    .tileRandomisationRadius(2) // Randomize destination by 2 tiles
    .build();

getWalker().walkTo(destination, config);
```

---

## UI COMPONENTS

### Inventory
```java
Inventory inv = getWidgetManager().getInventory();

inv.isFull();
inv.getFreeSlots();
inv.contains(itemId);
inv.getItem(itemId);
inv.dropItems(itemId1, itemId2); // Drop specific items

// Search with item IDs
ItemGroupResult result = inv.search(Set.of(ItemID.LOGS, ItemID.OAK_LOGS));
if (result != null) {
    int logCount = result.getAmount(ItemID.LOGS);
    SearchableItem item = result.getItem(ItemID.LOGS);
    if (item != null) {
        item.interact("Drop");
    }
}
```

### Dialogue Handling
```java
Dialogue dialogue = getWidgetManager().getDialogue();

if (dialogue.isVisible()) {
    DialogueType type = dialogue.getDialogueType();
    
    switch (type) {
        case NPC_DIALOGUE:
        case PLAYER_DIALOGUE:
            dialogue.clickContinue();
            break;
        case OPTION:
            dialogue.selectOption("Yes"); // or by index
            break;
        case ITEM_OPTION:
            dialogue.selectItem(ItemID.COINS);
            break;
        case SKILL:
            dialogue.typeInput("1");
            break;
    }
}
```

### Equipment Tab
```java
EquipmentTab equipment = getWidgetManager().getEquipment();

// Check if item equipped
boolean hasWeapon = equipment.contains(ItemID.RUNE_SCIMITAR);

// Get equipped item
SearchableItem weapon = equipment.getItem(EquipmentSlot.WEAPON);

// Interact with equipped item (for teleport jewelry, etc.)
SearchableItem ring = equipment.getItem(EquipmentSlot.RING);
if (ring != null) {
    ring.interact("Rub");
}
```

### Minimap & Orbs
```java
Minimap minimap = getWidgetManager().getMinimap();

// Health/Prayer/Run orbs
int healthPercent = minimap.getHitpointsPercentage();
int prayerPoints = minimap.getPrayerOrb().getValue();
int runEnergy = minimap.getRunOrb().getValue();
boolean isRunEnabled = minimap.isRunEnabled();
minimap.toggleRun(true);

// NPC/Item positions from minimap
List<WorldPosition> npcPositions = minimap.getNPCPositions();
List<WorldPosition> itemPositions = minimap.getItemPositions();
```

### Buff Overlays
```java
// For tracking buffs/debuffs shown on screen
BuffOverlay poisonOverlay = new BuffOverlay(this, POISON_SPRITE_ID);
if (poisonOverlay.isVisible()) {
    String value = poisonOverlay.getText();
    // Handle poison
}

// Waterskin tracking (when user has overlay enabled)
BuffOverlay waterskinOverlay = new BuffOverlay(this, WATERSKIN_SPRITE_ID);
if (waterskinOverlay.isVisible()) {
    String remaining = waterskinOverlay.getText();
}
```

---

## OBJECT INTERACTION

### Finding Objects
```java
ObjectManager objects = getObjectManager();

// By name (closest)
RSObject tree = objects.getClosestObject(getWorldPosition(), "Tree", "Oak tree");

// By predicate
RSObject specific = objects.getRSObject(o -> 
    o.getName().equals("Bank booth") && 
    o.getId() == 12345
);

// All matching
List<RSObject> allTrees = objects.getObjects(o -> o.getName().contains("tree"));

// Optional version
Optional<RSObject> maybeTree = objects.getObject(o -> o.getName().equals("Tree"));
maybeTree.ifPresent(tree -> tree.interact("Chop down"));
```

### RSObject Methods
```java
RSObject object = ...;

object.getName();
object.getId();
object.getWorldPosition();
object.getActions();            // Available actions
object.getConvexHull();         // Polygon for clicking
object.exists();                // Still valid?
object.interact("Action");      // Click with action
object.interact(null, null, "Action"); // Alternative
```

---

## ANTI-BAN CONSIDERATIONS

### Random Delays
```java
// Use RandomUtils for human-like timing
int delay = RandomUtils.weightedRandom(100, 500); // Weighted toward middle
int gaussian = RandomUtils.gaussianRandom(300, 100); // Mean 300, stddev 100

// Between actions
return RandomUtils.uniformRandom(400, 800);
```

### Reaction Delays After Actions
```java
// WRONG - No delay after mining completes
if (!rock.exists()) {
    // Immediately click next rock
}

// RIGHT - Add reaction delay
if (!rock.exists()) {
    submitTask(() -> false, RandomUtils.uniformRandom(200, 600)); // Fake "reaction time"
    // Then click next rock
}
```

### Don't Click Same Exact Spot
```java
// Use areas instead of fixed tiles for walking
RectangleArea area = new RectangleArea(x1, y1, x2, y2, 0);
WorldPosition randomInArea = area.getRandomPosition();
getWalker().walkTo(randomInArea);
```

---

## DEBUG TOOL TIPS

1. **Find Item ID**: Debug tool > Item > Search by name or ID
2. **Custom Comparator**: If item not detected, adjust HSL tolerance
3. **Respawn Circles**: Adjust z-height and distance tolerance
4. **Create Poly Area**: Use built-in tool to define areas
5. **Scene Viewer**: Inspect objects, tiles, collision data
6. **Check Sprites**: Search all sprites on screen

### When Items Aren't Detected
```java
// Some items need custom comparators due to color variations
// Check debug tool with high HSL tolerance (25+ for all three)
// If still not found, may need cache update from OSMB
```

---

## COMMON MISTAKES TO AVOID

### 1. Not Null Checking
```java
// WRONG
RSObject tree = objects.getClosestObject(pos, "Tree");
tree.interact("Chop"); // NullPointerException!

// RIGHT
RSObject tree = objects.getClosestObject(pos, "Tree");
if (tree != null) {
    tree.interact("Chop");
}
```

### 2. Using while Loops Instead of submitTask
```java
// WRONG - Blocks everything
while (!bank.isVisible()) {
    Thread.sleep(100);
}

// RIGHT - Proper waiting
submitTask(() -> bank.isVisible(), 5000);
```

### 3. Modifying State in onNewFrame
```java
// WRONG - onNewFrame is for READ-ONLY operations
@Override
public void onNewFrame() {
    getFinger().tap(somePoint); // NO! Don't do actions here
}

// RIGHT - Only read data
@Override  
public void onNewFrame() {
    chatLines = getChatbox().getLines(); // OK - just reading
}
```

### 4. Assuming Collision Map is Real-Time
```java
// WRONG - Collision map is static default state
if (collisionMap.isBlocked(x, y)) {
    // Door might actually be open!
}

// RIGHT - Visual verification
// Check door state via menu entry or pixel detection
```

### 5. Not Using Region Priorities
```java
// Script startup takes 90+ seconds without this
@Override
public ScriptOptions getScriptOptions() {
    return new ScriptOptions() {
        @Override
        public int[] regionsToPrioritise() {
            return new int[]{12850}; // Your operating region
        }
    };
}
```

---

## IMPORTS REFERENCE

```java
// Core
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;
import com.osmb.api.script.ScriptCore;

// Location
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.location.position.types.LocalPosition;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.area.impl.PolyArea;

// Scene
import com.osmb.api.scene.RSObject;
import com.osmb.api.scene.RSNPC;
import com.osmb.api.scene.ObjectManager;
import com.osmb.api.scene.SceneManager;

// UI
import com.osmb.api.ui.bank.Bank;
import com.osmb.api.ui.tabs.Inventory;
import com.osmb.api.ui.chatbox.Dialogue;
import com.osmb.api.ui.chatbox.Chatbox;
import com.osmb.api.ui.minimap.Minimap;
import com.osmb.api.ui.widget.WidgetManager;

// Items
import com.osmb.api.item.ItemID;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.SearchableItem;

// Input
import com.osmb.api.input.Finger;
import com.osmb.api.input.MenuEntry;

// Visual
import com.osmb.api.visual.PixelAnalyzer;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.drawing.SceneProjector;
import com.osmb.api.visual.ocr.OCR;

// Walker
import com.osmb.api.walker.Walker;
import com.osmb.api.walker.WalkConfig;

// Shapes
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;

// Utils
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;

// Overlays
import com.osmb.api.ui.overlay.BuffOverlay;
```

---

## COMPLETE EXAMPLE: Mining Script

```java
package scripts;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;
import com.osmb.api.script.ScriptOptions;
import com.osmb.api.scene.RSObject;
import com.osmb.api.ui.bank.Bank;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.walker.WalkConfig;
import com.osmb.api.input.MenuEntry;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.RandomUtils;
import java.util.Set;

@ScriptManifest(
    name = "Example Miner",
    author = "Claude",
    version = 1.0,
    description = "Mines rocks and banks ore"
)
public class ExampleMiner extends Script {
    
    private static final int[] ORE_IDS = {ItemID.COPPER_ORE, ItemID.TIN_ORE};
    private static final int PICKAXE_ID = ItemID.BRONZE_PICKAXE;
    
    private static final WorldPosition MINE_CENTER = new WorldPosition(3285, 3365, 0);
    private static final WorldPosition BANK_POS = new WorldPosition(3253, 3420, 0);
    private static final RectangleArea MINE_AREA = new RectangleArea(3280, 3360, 3290, 3370, 0);
    
    private enum State { MINING, WALKING_TO_BANK, BANKING, WALKING_TO_MINE }
    
    @Override
    public ScriptOptions getScriptOptions() {
        return new ScriptOptions() {
            @Override
            public int[] regionsToPrioritise() {
                return new int[]{13107, 12850}; // Mine and bank regions
            }
        };
    }
    
    @Override
    public void onStart() {
        log("Miner started!");
    }
    
    @Override
    public int poll() {
        WorldPosition pos = getWorldPosition();
        if (pos == null) return 1000;
        
        State state = getState();
        log("State: " + state);
        
        switch (state) {
            case MINING: return mine();
            case WALKING_TO_BANK: return walkToBank();
            case BANKING: return bank();
            case WALKING_TO_MINE: return walkToMine();
            default: return 600;
        }
    }
    
    private State getState() {
        Bank bank = getWidgetManager().getBank();
        ItemGroupResult inv = getWidgetManager().getInventory().search(Set.of());
        WorldPosition pos = getWorldPosition();
        
        if (bank.isVisible()) return State.BANKING;
        if (inv != null && inv.isFull()) {
            return pos.distanceTo(BANK_POS) < 10 ? State.BANKING : State.WALKING_TO_BANK;
        }
        if (!MINE_AREA.contains(pos)) return State.WALKING_TO_MINE;
        return State.MINING;
    }
    
    private int mine() {
        RSObject rock = getObjectManager().getClosestObject(
            getWorldPosition(), "Copper rocks", "Tin rocks"
        );
        
        if (rock == null) {
            log("No rocks found, waiting...");
            return 1000;
        }
        
        // Shrink click area to avoid misclicks
        Polygon rockPoly = rock.getConvexHull();
        if (rockPoly != null) {
            Polygon shrunk = rockPoly.getResized(0.7);
            if (shrunk != null) {
                // Verify we clicked the right thing
                MenuEntry response = getFinger().tapGetResponse(true, shrunk);
                if (response != null && response.getAction().equalsIgnoreCase("Mine")) {
                    // Wait for rock to deplete or inventory full
                    submitTask(() -> !rock.exists() || 
                        getWidgetManager().getInventory().search(Set.of()).isFull(), 
                        30000);
                }
            }
        }
        
        return RandomUtils.uniformRandom(400, 800);
    }
    
    private int walkToBank() {
        WalkConfig config = new WalkConfig.Builder()
            .breakCondition(() -> getWidgetManager().getBank().isVisible())
            .build();
        getWalker().walkTo(BANK_POS, config);
        return 600;
    }
    
    private int bank() {
        Bank bank = getWidgetManager().getBank();
        
        if (!bank.isVisible()) {
            RSObject bankBooth = getObjectManager().getClosestObject(
                getWorldPosition(), "Bank booth"
            );
            if (bankBooth != null && bankBooth.interact("Bank")) {
                submitTask(() -> bank.isVisible(), 5000);
            }
            return 600;
        }
        
        // Deposit all except pickaxe
        bank.depositAll(Set.of(PICKAXE_ID));
        
        // Wait for deposit, then close
        if (submitTask(() -> {
            ItemGroupResult inv = getWidgetManager().getInventory().search(Set.of(ORE_IDS[0], ORE_IDS[1]));
            return inv == null || inv.getAmount(ORE_IDS[0]) + inv.getAmount(ORE_IDS[1]) == 0;
        }, 3000)) {
            bank.close();
        }
        
        return RandomUtils.uniformRandom(300, 600);
    }
    
    private int walkToMine() {
        WalkConfig config = new WalkConfig.Builder()
            .breakCondition(() -> MINE_AREA.contains(getWorldPosition()))
            .tileRandomisationRadius(3)
            .build();
        getWalker().walkTo(MINE_AREA.getRandomPosition(), config);
        return 600;
    }
    
    @Override
    public void onStop() {
        log("Miner stopped!");
    }
}
```

---

## GEM CUTTING SCRIPTS (Lessons from tGemCutter)

### Critical Differences from Other Crafting Scripts

Gem cutting has unique dialogue behavior that differs from other crafting activities:

#### 1. Dialogue Shows UNCUT Gem, Not Cut Gem

```java
// WRONG - This will fail!
DialogueType dialogueType = script.getWidgetManager().getDialogue().getDialogueType();
if (dialogueType == DialogueType.ITEM_OPTION) {
    boolean selected = script.getWidgetManager().getDialogue().selectItem(selectedCutGemID);
    // ^ Trying to select "Ruby" but dialogue shows "Uncut ruby"!
}

// CORRECT - Select the UNCUT gem
if (dialogueType == DialogueType.ITEM_OPTION) {
    boolean selected = script.getWidgetManager().getDialogue().selectItem(selectedUncutGemID);
    // ^ Dialogue shows "Uncut ruby", so select that!
}
```

**Why this happens**:
- Scripts like dAmethystCutter ask "Which item to make?" (bolt tips, arrowtips, javelin heads)
- Gem cutting asks "How many gems to cut?" with the UNCUT gem image shown
- The dialogue is confirming which gem to cut, not which product to make

#### 2. Bank Withdrawal Timing Issue

```java
// WRONG - Calculating empty slots before depositing!
ItemGroupResult inventorySnapshot = getWidgetManager().getInventory().search(Collections.emptySet());
int emptySlots = inventorySnapshot.getFreeSlots(); // = 0 (inventory full of cut gems!)

// Deposit cut gems
bank.depositAll(Set.of(ItemID.CHISEL));

// Try to withdraw with stale empty slots count
bank.withdraw(selectedUncutGemID, emptySlots); // Tries to withdraw 0 items!
```

```java
// CORRECT - Recalculate after depositing
// Deposit cut gems
bank.depositAll(Set.of(ItemID.CHISEL));

// Wait for deposit to complete
script.pollFramesHuman(() -> false, script.random(300, 600));

// Get FRESH inventory snapshot after deposit
ItemGroupResult inventorySnapshot = getWidgetManager().getInventory().search(Collections.emptySet());
int emptySlots = inventorySnapshot.getFreeSlots(); // = 27 (correct!)

// Now withdraw correct amount
bank.withdraw(selectedUncutGemID, emptySlots);
```

**Why this matters**: If you try to withdraw 0 items, OSRS shows a quantity selection menu which breaks the script flow.

#### 3. ItemID Constants Are Correct - Use Them!

```java
// GOOD - Use ItemID constants from the API
private static final Integer[] GEM_OPTIONS = {
    ItemID.UNCUT_SAPPHIRE,  // = 1607
    ItemID.UNCUT_EMERALD,   // = 1605
    ItemID.UNCUT_RUBY,      // = 1619
    ItemID.UNCUT_DIAMOND    // = 1617
};

private static final Map<Integer, Integer> UNCUT_TO_CUT = Map.of(
    ItemID.UNCUT_SAPPHIRE, ItemID.SAPPHIRE,
    ItemID.UNCUT_EMERALD, ItemID.EMERALD,
    ItemID.UNCUT_RUBY, ItemID.RUBY,
    ItemID.UNCUT_DIAMOND, ItemID.DIAMOND
);
```

**Reference**: https://osmb.co.uk/javadocs - All ItemID constants match their numeric values exactly.

---

## COMMON MISTAKES & SOLUTIONS

### 1. Level 99 Handling in onPaint()

```java
// WRONG - Shows negative XP and "99 (+98)" levels gained
public static int startLevel = 1;  // Initialized to 1

// In onPaint()
if (startLevel <= 0) startLevel = currentLevel;  // Never triggers because 1 > 0!
int levelsGained = Math.max(0, currentLevel - startLevel);  // 99 - 1 = 98!
```

```java
// CORRECT - Initialize to 0
public static int startLevel = 0;  // Initialized to 0

// In onPaint()
if (startLevel <= 0) startLevel = currentLevel;  // Triggers correctly!

// Handle max level specially
if (currentLevel >= 99) {
    ttlText = "MAXED";
    etl = 0;
    levelProgressFraction = 1.0;
    levelProgressText = "MAXED";
} else {
    // Normal level calculations
}

// ETL display
String etlText = (currentLevel >= 99) ? "MAXED" : intFmt.format(Math.round(etl));
```

### 2. Resources Folder Location

```java
// WRONG - Resources in wrong location
// tGemCutter/resources/logo.png  ❌ Won't be found!

// CORRECT - Resources must be in src/main/resources
// tGemCutter/src/main/resources/logo.png  ✅ Found by classloader!

// Loading code
try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
    // This looks for logo.png at the root of the classpath
    // which is src/main/resources/ after building
}
```

**Fix**: Always put resources in `src/main/resources/`, not just `resources/`

### 3. Bank Loading Delay

```java
// WRONG - Searching bank immediately after it opens
if (!script.getWidgetManager().getBank().isVisible()) {
    openBank();
    return false;
}

// Next poll - bank just opened, items not loaded yet!
ItemGroupResult bankSnapshot = script.getWidgetManager().getBank().search(Set.of(itemID));
// Returns null or doesn't find items!
```

```java
// CORRECT - Wait for bank to fully load
if (!script.getWidgetManager().getBank().isVisible()) {
    openBank();
    return false;
}

// Give bank a moment to fully load before searching
script.pollFramesHuman(() -> false, script.random(300, 500));

// Now search for items
ItemGroupResult bankSnapshot = script.getWidgetManager().getBank().search(Set.of(itemID));
```

---

## PAINT OVERLAY BEST PRACTICES

### User Preferences (Learned from tGemCutter development)

**✅ DO:**
- Keep it clean and minimal (reference: dSunbleakWCer style)
- Logo centered at the top
- Simple white border on dark background
- Muted, professional colors (turquoise, light blue, white)
- Clear hierarchy: labels on left, values on right
- Consistent spacing (16px line gap)
- Standard fonts (Arial, 12pt)

**❌ DON'T:**
- Loud accent colors (coral pink, bright orange)
- Decorative elements (wave lines, fancy titles)
- Multiple borders or gradients
- Oversized panels
- Inconsistent color schemes

### Modern Paint Template

```java
// Clean, professional colors
final Color bgColor = new Color(15, 52, 96);        // Deep blue background
final Color labelColor = new Color(64, 224, 208);   // Turquoise labels
final Color valueColor = Color.WHITE;               // White values
final Color accentColor = new Color(100, 149, 237); // Cornflower blue highlights
final Color progressColor = new Color(152, 251, 152); // Seafoam green progress

// Standard layout
final int x = 5;
final int baseY = 40;
final int width = 260;
final int borderThickness = 2;
final int paddingX = 10;
final int lineGap = 16;
final int logoBottomGap = 8;

// Simple panel (like dSunbleakWCer)
c.fillRect(innerX - borderThickness, innerY - borderThickness,
        innerWidth + (borderThickness * 2),
        innerHeight + (borderThickness * 2),
        Color.WHITE.getRGB(), 1);
c.fillRect(innerX, innerY, innerWidth, innerHeight, bgColor.getRGB(), 1);
c.drawRect(innerX, innerY, innerWidth, innerHeight, Color.WHITE.getRGB());

// Logo at top, centered
if (logoImage != null) {
    int logoX = innerX + (innerWidth - logoImage.width) / 2;
    c.drawAtOn(logoImage, logoX, curY);
    curY += logoImage.height + logoBottomGap;
}

// Clean stat lines (no decorations)
drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "Runtime", runtime, labelColor.getRGB(), valueColor.getRGB(),
        FONT_VALUE_BOLD, FONT_LABEL);
```

---

## DEBUG LOGGING BEST PRACTICES

When troubleshooting issues, add comprehensive debug logging:

```java
// Example from tGemCutter bank debugging
task = "Get bank snapshot";

String gemName = script.getItemManager().getItemName(selectedUncutGemID);
script.log(getClass(), "Searching for " + gemName + " (ID: " + selectedUncutGemID + ") in bank");

// Check if bank has any items at all
ItemGroupResult allBankItems = script.getWidgetManager().getBank().search(Collections.emptySet());
if (allBankItems != null) {
    script.log(getClass(), "Bank has items, checking for gem...");
} else {
    script.log(getClass(), "Bank search returned null! Bank might not be ready.");
    return false;
}

// Search for specific gem
ItemGroupResult bankSnapshot = script.getWidgetManager().getBank().search(Set.of(selectedUncutGemID));

// Log the result
if (bankSnapshot.contains(selectedUncutGemID)) {
    int gemCount = bankSnapshot.getAmount(selectedUncutGemID);
    script.log(getClass(), "Found " + gemCount + " " + gemName + " in bank");
} else {
    script.log(getClass(), "bankSnapshot.contains() returned false for ID " + selectedUncutGemID);
}
```

This logging helped quickly identify:
- Wrong gem ID being searched
- Bank not fully loaded
- Timing issues with deposits/withdrawals

---

## RESOURCES

- **API Documentation**: https://doc.osmb.co.uk
- **Javadocs**: https://osmb.co.uk/javadocs (for ItemID constants, etc.)
- **Debug Tool**: Built into OSMB client for testing
- **Community Scripts**: Check GitHub repos for reference implementations
- **Discord**: Developer knowledge base channel for Q&A

---

*Remember: OSMB is a COLOR BOT. Think visually, verify interactions, and be creative when the API can't distinguish items directly!*
