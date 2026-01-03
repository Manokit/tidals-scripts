# OSMB Script Development Guide

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

## ADVANCED PATTERNS FROM DAVY'S SCRIPTS

These battle-tested patterns come from production scripts and demonstrate real-world solutions to common scripting challenges.

### Advanced Banking with Visibility Checks

**Problem**: Objects can be partially off-screen or obstructed by UI elements, leading to failed interactions.

**Solution** (from dSunbleakWCer): Check polygon visibility and walk closer if needed.

```java
private boolean openBank() {
    RSObject chest = getObjectManager().getClosestObject(
        getWorldPosition(), "Bank chest"
    );

    if (chest == null) return false;

    Polygon hull = chest.getConvexHull();
    if (hull == null) return false;

    // Check how visible the bank chest is (ignoring chatbox obstruction)
    double insideFactor = getWidgetManager().insideGameScreenFactor(
        hull, List.of(ChatboxComponent.class)
    );

    if (insideFactor < 1.0) {
        // Walk closer if not fully visible
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

    // Verify all polygon points are within screen bounds before tapping
    if (!isPolygonTapSafe(hull)) {
        // Move closer
        return false;
    }

    // Now safe to tap
    getFinger().tap(hull, "Bank");
    return true;
}

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

**Idle Detection During Walking**:
```java
// Track position changes to detect if player got stuck
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

### Deposit Box (Alternative to Banks)

**Use Case**: Mining areas, certain quests, activities without full bank access.

**Example** (from dAmethystMiner):
```java
private boolean handleDepositBox() {
    DepositBox depositBox = getWidgetManager().getDepositBox();

    if (!depositBox.isVisible()) {
        RSObject box = getObjectManager().getClosestObject(
            getWorldPosition(), "Bank deposit box"
        );
        if (box != null && box.interact("Deposit")) {
            submitTask(() -> depositBox.isVisible(), 5000);
        }
        return false;
    }

    // Deposit all items except tools
    Set<Integer> keepItems = Set.of(ItemID.RUNE_PICKAXE);
    if (!depositBox.depositAll(keepItems)) {
        log(getClass(), "Failed to deposit items.");
        return false;
    }

    depositBox.close();
    return true;
}
```

### Spellbook Teleportation

**Use Case**: High-frequency teleport scripts, offering training, altar running.

**Example** (from dTeleporter):
```java
private static final int TELEPORT_COOLDOWN_MS = 1800; // Base cooldown
private long lastTeleportTime = 0;
private int teleportCount = 0;

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
    boolean success;
    try {
        success = getWidgetManager().getSpellbook().selectSpell(
            Spell.VARROCK_TELEPORT,  // Or your spell
            null  // null = auto-cast at current location
        );
    } catch (SpellNotFoundException e) {
        log(getClass(), "Spell sprite not found: " + e.getMessage());
        stop();
        return false;
    }

    if (success) {
        lastTeleportTime = System.currentTimeMillis();
        teleportCount++;
    }

    return success;
}

// Randomized cooldown (anti-ban)
private long getCooldownForSpell() {
    int roll = random(100);

    if (roll < 50) {
        return random(1800, 1901);  // ~1.8-1.9s
    } else if (roll < 90) {
        return random(1850, 2001);  // ~1.85-2.0s
    } else {
        return random(1900, 2301);  // ~1.9-2.3s
    }
}
```

### Custom Interface with OCR Detection

**Use Case**: Teleport interfaces, minigame interfaces, custom dialogues.

**Example** (from dBirdhouseRunner - Mushroom Transport):
```java
public class MushroomTransportInterface extends CustomInterface {
    private static final String TITLE_TEXT = "Bittercap Mushrooms";
    private static final Rectangle TITLE_BOUNDS = new Rectangle(10, 5, 200, 20);
    private static final SearchablePixel ORANGE_TEXT_COLOR =
        new SearchablePixel(25, 100, 50, 10, 10, 10); // HSL

    private Map<ButtonType, Rectangle> buttons = null;

    public enum ButtonType {
        VERDANT_VALLEY("Verdant Valley"),
        MUSHROOM_MEADOW("Mushroom Meadow"),
        LUMBER_CLEARING("Lumber Clearing");

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
            ORANGE_TEXT_COLOR
        );

        boolean visible = text.equalsIgnoreCase(TITLE_TEXT);

        // Register buttons dynamically if visible
        if (visible && buttons == null) {
            this.buttons = registerButtons(bounds);
        }
        return visible;
    }

    private Map<ButtonType, Rectangle> registerButtons(Rectangle bounds) {
        Map<ButtonType, Rectangle> buttons = new HashMap<>();

        // Find containers by corner sprite IDs (use debug tool to find these)
        List<Rectangle> containers = core.getImageAnalyzer()
            .findContainers(bounds, 913, 914, 915, 916);

        for (Rectangle container : containers) {
            String rawText = core.getOCR().getText(
                Font.SMALL_FONT,
                container,
                ORANGE_BUTTON_TEXT_COLOR
            ).trim();

            // Normalize OCR errors (I -> l is common)
            String normalizedText = rawText.replace('I', 'l').toLowerCase();

            for (ButtonType buttonType : ButtonType.values()) {
                if (normalizedText.equals(buttonType.getText().toLowerCase())) {
                    buttons.put(buttonType, new Rectangle(
                        container.x - bounds.x,
                        container.y - bounds.y,
                        container.width,
                        container.height
                    ));
                    break;
                }
            }
        }
        return buttons;
    }

    public boolean selectOption(ButtonType buttonType) {
        Rectangle buttonScreenBounds = getButtonScreenBounds(buttonType);
        if (buttonScreenBounds == null) return false;

        if (core.getFinger().tap(buttonScreenBounds)) {
            return core.pollFramesUntil(() -> !isVisible(), 5000);
        }
        return false;
    }
}

// Usage:
MushroomTransportInterface transport = new MushroomTransportInterface(this);
if (transport.isVisible()) {
    transport.selectOption(ButtonType.VERDANT_VALLEY);
}
```

### Multi-Step Travel Sequence

**Use Case**: Complex travel requiring boats, NPCs, dialogue choices.

**Example** (from dBirdhouseRunner):
```java
private boolean travelToIsland() {
    // Step 1: Find and interact with boat
    List<RSObject> rowboats = getObjectManager().getObjects(obj ->
        "Rowboat".equalsIgnoreCase(obj.getName()) &&
        obj.getActions() != null &&
        Arrays.asList(obj.getActions()).contains("Travel") &&
        obj.canReach()
    );

    RSObject rowboat = (RSObject) getUtils().getClosest(rowboats);
    if (rowboat == null) {
        log(getClass(), "No rowboat found!");
        return false;
    }

    if (!rowboat.interact("Travel")) {
        return false;
    }

    // Step 2: Wait for dialogue to appear
    boolean dialogueAppeared = pollFramesUntil(() -> {
        DialogueType type = getWidgetManager().getDialogue().getDialogueType();
        return type == DialogueType.TEXT_OPTION;
    }, random(6000, 10000));

    if (!dialogueAppeared) {
        log(getClass(), "Dialogue did not appear!");
        return false;
    }

    // Step 3: Select correct travel option
    boolean selected = getWidgetManager().getDialogue()
        .selectOption("Row out to sea, north of the island");

    if (!selected) {
        log(getClass(), "Failed to select dialogue option!");
        return false;
    }

    // Step 4: Wait for arrival
    RectangleArea destinationArea = new RectangleArea(2800, 2700, 2850, 2750, 0);
    boolean success = pollFramesUntil(() -> {
        WorldPosition current = getWorldPosition();
        return current != null && destinationArea.contains(current);
    }, random(14000, 17500));

    if (!success) {
        log(getClass(), "Travel timeout - may have failed!");
    }

    return success;
}
```

### Complex Multi-Step Dialogue Chains

**Problem**: Some interfaces require TEXT_OPTION → ITEM_OPTION sequences (Construction, Crafting).

**Example** (from dConstructioneer):
```java
private boolean buildItem() {
    RSObject workbench = getObjectManager().getClosestObject(
        getWorldPosition(), "Workbench"
    );

    if (workbench == null) return false;

    // Step 1: Interact with workbench
    if (!workbench.interact("Build")) {
        return false;
    }

    // Step 2: Wait for first dialogue (TEXT_OPTION for category)
    BooleanSupplier waitForTextOption = () -> {
        DialogueType type = getWidgetManager().getDialogue().getDialogueType();
        return type == DialogueType.TEXT_OPTION;
    };

    if (!pollFramesHuman(waitForTextOption, random(4000, 6000))) {
        log(getClass(), "TEXT_OPTION dialogue did not appear.");
        return false;
    }

    // Step 3: Select category
    DialogueType dialogueType = getWidgetManager().getDialogue().getDialogueType();
    if (dialogueType != DialogueType.TEXT_OPTION) {
        log(getClass(), "Expected TEXT_OPTION but got: " + dialogueType);
        return false;
    }

    boolean selectedCategory = getWidgetManager().getDialogue()
        .selectOption("Repair kits");  // Your category

    if (!selectedCategory) {
        log(getClass(), "Failed to select construction category.");
        return false;
    }

    // Step 4: Wait for second dialogue (ITEM_OPTION for specific item)
    BooleanSupplier waitForItemOption = () ->
        getWidgetManager().getDialogue().getDialogueType() == DialogueType.ITEM_OPTION;

    if (!pollFramesHuman(waitForItemOption, random(4000, 6000))) {
        log(getClass(), "ITEM_OPTION dialogue did not appear.");
        return false;
    }

    // Step 5: Select specific item
    DialogueType afterCategory = getWidgetManager().getDialogue().getDialogueType();
    if (afterCategory != DialogueType.ITEM_OPTION) {
        log(getClass(), "Expected ITEM_OPTION, but got: " + afterCategory);
        return false;
    }

    boolean selectedItem = getWidgetManager().getDialogue()
        .selectItem(ItemID.BASIC_REPAIR_KIT);

    if (!selectedItem) {
        log(getClass(), "Failed to select item.");
        return false;
    }

    // Step 6: Wait for crafting to complete (with level-up detection)
    return waitUntilFinishedCrafting();
}

private boolean waitUntilFinishedCrafting() {
    Timer amountChangeTimer = new Timer();

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
        if (amountChangeTimer.timeElapsed() > random(70000, 78000)) {
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

### Task Manager State Machine Pattern

**Best Practice**: Organize scripts using Task classes for clean separation of concerns.

**Implementation** (from dSunbleakWCer):

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

// Example: BankTask
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

// Main script
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

### Professional Paint Overlay Implementation

**Reference Standard**: dSunbleakWCer style - clean, minimal, professional.

**Complete Implementation**:
```java
// Paint fields
private Image logoImage = null;
private long startTime;
private int startLevel = 0;  // IMPORTANT: Initialize to 0, not 1!
private int currentLevel = 1;

private static final Font FONT_LABEL = new Font("Arial", Font.PLAIN, 12);
private static final Font FONT_VALUE_BOLD = new Font("Arial", Font.BOLD, 12);

@Override
public void onStart() {
    startTime = System.currentTimeMillis();
    ensureLogoLoaded();  // Load logo from resources
}

@Override
public void onPaint(Canvas c) {
    long elapsed = System.currentTimeMillis() - startTime;
    double hours = Math.max(1e-9, elapsed / 3_600_000.0);
    String runtime = formatRuntime(elapsed);

    // Get live XP data (if using XP tracker)
    String ttlText = "-";
    double etl = 0.0;
    double xpGainedLive = 0.0;
    double currentXp = 0.0;
    double levelProgressFraction = 0.0;

    if (xpTracking != null) {
        XPTracker tracker = xpTracking.getWoodcuttingTracker();
        if (tracker != null) {
            xpGainedLive = tracker.getXpGained();
            currentXp = tracker.getXp();

            // Sync level (only increases)
            final int MAX_LEVEL = 99;
            int guard = 0;
            while (currentLevel < MAX_LEVEL
                    && currentXp >= tracker.getExperienceForLevel(currentLevel + 1)
                    && guard++ < 10) {
                currentLevel++;
            }

            // Handle max level
            if (currentLevel >= 99) {
                ttlText = "MAXED";
                etl = 0;
                levelProgressFraction = 1.0;
            } else {
                ttlText = tracker.timeToNextLevelString();

                // Calculate progress to next level
                int curLevelXpStart = tracker.getExperienceForLevel(currentLevel);
                int nextLevelXpTarget = tracker.getExperienceForLevel(
                    Math.min(MAX_LEVEL, currentLevel + 1));
                int span = Math.max(1, nextLevelXpTarget - curLevelXpStart);

                etl = Math.max(0, nextLevelXpTarget - currentXp);
                levelProgressFraction = Math.max(0.0, Math.min(1.0,
                    (currentXp - curLevelXpStart) / (double) span));
            }
        }
    }

    // Calculate rates
    int xpPerHour = (int) Math.round(xpGainedLive / hours);

    // Handle level gain display
    if (startLevel <= 0) startLevel = currentLevel;
    int levelsGained = Math.max(0, currentLevel - startLevel);
    String currentLevelText = (levelsGained > 0)
        ? (currentLevel + " (+" + levelsGained + ")")
        : String.valueOf(currentLevel);

    // === Panel Layout ===
    final int x = 5;
    final int baseY = 40;
    final int width = 225;
    final int borderThickness = 2;
    final int paddingX = 10;
    final int lineGap = 16;

    // Clean, professional colors
    final int labelGray = new Color(180, 180, 180).getRGB();
    final int valueWhite = Color.WHITE.getRGB();
    final int valueGreen = new Color(80, 220, 120).getRGB();
    final Color bgColor = Color.decode("#01031C");  // Deep blue/black

    int innerX = x;
    int innerY = baseY;
    int innerWidth = width;

    // Calculate panel height
    int totalLines = 8;
    int y = innerY + 6;
    if (logoImage != null) y += logoImage.height + 8;
    y += totalLines * lineGap + 16;
    int innerHeight = Math.max(200, y - innerY);

    // Draw panel border and background
    c.fillRect(innerX - borderThickness, innerY - borderThickness,
        innerWidth + (borderThickness * 2),
        innerHeight + (borderThickness * 2),
        Color.WHITE.getRGB(), 1);
    c.fillRect(innerX, innerY, innerWidth, innerHeight,
        bgColor.getRGB(), 1);
    c.drawRect(innerX, innerY, innerWidth, innerHeight,
        Color.WHITE.getRGB());

    int curY = innerY + 6;

    // Draw logo if loaded
    if (logoImage != null) {
        int imgX = innerX + (innerWidth - logoImage.width) / 2;
        c.drawAtOn(logoImage, imgX, curY);
        curY += logoImage.height + 8;
    }

    // Draw stat lines (clean, no decorations)
    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "Runtime", runtime, labelGray, valueWhite,
        FONT_VALUE_BOLD, FONT_LABEL);

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "XP/hr", formatInt(xpPerHour), labelGray, valueWhite,
        FONT_VALUE_BOLD, FONT_LABEL);

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "Level", currentLevelText, labelGray, valueGreen,
        FONT_VALUE_BOLD, FONT_LABEL);

    curY += lineGap;
    String etlText = (currentLevel >= 99) ? "MAXED" : formatInt((int) Math.round(etl));
    drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "XP to level", etlText, labelGray, valueWhite,
        FONT_VALUE_BOLD, FONT_LABEL);

    curY += lineGap;
    drawStatLine(c, innerX, innerWidth, paddingX, curY,
        "Time to level", ttlText, labelGray, valueWhite,
        FONT_VALUE_BOLD, FONT_LABEL);
}

private void drawStatLine(Canvas c, int innerX, int innerWidth, int paddingX, int y,
                          String label, String value, int labelColor, int valueColor,
                          Font valueFont, Font labelFont) {
    c.drawText(label, innerX + paddingX, y, labelColor, labelFont);
    int valW = c.getFontMetrics(valueFont).stringWidth(value);
    int valX = innerX + innerWidth - paddingX - valW;
    c.drawText(value, valX, y, valueColor, valueFont);
}

private void ensureLogoLoaded() {
    if (logoImage != null) return;

    try (InputStream in = getClass().getResourceAsStream("/logo.png")) {
        if (in == null) {
            log(getClass(), "Logo '/logo.png' not found on classpath.");
            return;
        }

        BufferedImage src = ImageIO.read(in);
        if (src == null) return;

        // Convert to ARGB
        BufferedImage argb = new BufferedImage(
            src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = argb.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(src, 0, 0, null);
        g.dispose();

        int w = argb.getWidth();
        int h = argb.getHeight();
        int[] px = new int[w * h];
        argb.getRGB(0, 0, w, h, px, 0, w);

        // Premultiply alpha for correct rendering
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            int a = (p >>> 24) & 0xFF;
            if (a == 0) {
                px[i] = 0x00000000;
            } else {
                int r = (p >>> 16) & 0xFF;
                int g_val = (p >>> 8) & 0xFF;
                int b = p & 0xFF;
                r = (r * a + 127) / 255;
                g_val = (g_val * a + 127) / 255;
                b = (b * a + 127) / 255;
                px[i] = (a << 24) | (r << 16) | (g_val << 8) | b;
            }
        }

        logoImage = new Image(px, w, h);
    } catch (Exception e) {
        log(getClass(), "Error loading logo: " + e.getMessage());
    }
}

private String formatRuntime(long ms) {
    long seconds = ms / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;

    return String.format("%02d:%02d:%02d",
        hours, minutes % 60, seconds % 60);
}

private String formatInt(int value) {
    return String.format("%,d", value);
}
```

**IMPORTANT**: Place logo in `src/main/resources/logo.png`, NOT just `resources/logo.png`!

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


# OSMB Script Build Guide for AI Agents


> Complete guide for building, updating, and managing OSMB script JARs using Gradle

## Quick Start

To build any script JAR (e.g., dSunbleakWCer):

```bash
cd /Users/zaffre/Documents/Engineering/Projects/tidals-scripts/dSunbleakWCer
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
```

The JAR will be output to: `dSunbleakWCer/jar/dSunbleakWCer.jar`

---

## Prerequisites

### Required Software
- **Gradle**: Installed via Homebrew (`/opt/homebrew/bin/gradle`)
- **Java 17+**: API.jar requires Java 17 (class version 61.0)
  - Available JDK: `/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home`
  - Check all installed: `/usr/libexec/java_home -V`

### Project Structure
```
tidals-scripts/
├── API/
│   └── API.jar              # OSMB API dependency (Java 17)
├── dScriptName/
│   ├── build.gradle         # Build configuration
│   ├── settings.gradle      # Project name
│   ├── jar/
│   │   └── dScriptName.jar  # Output JAR
│   └── src/
│       └── main/
│           ├── java/        # Source code
│           └── resources/   # logo.png, etc.
└── settings.gradle          # Root multi-project config
```

---

## Build Configuration File

### Create `build.gradle` in Script Directory

Every script needs a `build.gradle` file in its root directory (e.g., `dSunbleakWCer/build.gradle`):

```groovy
plugins {
    id 'java'
}

group = 'com.osmb.scripts'
version = '1.3'  // Update this when incrementing version

repositories {
    mavenCentral()
}

dependencies {
    // osmb api jar dependency
    compileOnly files('../API/API.jar')
}

java {
    // api.jar requires java 17 (class version 61.0)
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

jar {
    archiveFileName = "${project.name}.jar"
    destinationDirectory = file("${projectDir}/jar")
    
    // prevent duplicate file errors when bundling resources
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    
    // include resources like logo.png
    from sourceSets.main.resources
}

// clean task to remove old jars
clean {
    delete "${projectDir}/jar"
}
```

### Key Configuration Fields

| Field | Purpose | Example |
|-------|---------|---------|
| `version` | JAR build version (metadata) | `'1.3'` |
| `group` | Maven-style group ID | `'com.osmb.scripts'` |
| `archiveFileName` | Output JAR filename | `"${project.name}.jar"` |
| `destinationDirectory` | Where to put the JAR | `file("${projectDir}/jar")` |
| `duplicatesStrategy` | Handle duplicate files | `DuplicatesStrategy.EXCLUDE` |

---

## Building Scripts

### Standard Build Commands

```bash
# navigate to script directory
cd /Users/zaffre/Documents/Engineering/Projects/tidals-scripts/dScriptName

# set java 17 and build
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build

# alternative: just build (no clean)
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle build

# clean only (removes jar/ directory)
gradle clean
```

### One-Liner from Root

```bash
cd /Users/zaffre/Documents/Engineering/Projects/tidals-scripts/dScriptName && JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
```

### Set JAVA_HOME Permanently (Optional)

Add to `~/.zshrc`:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

Then just run:
```bash
gradle clean build
```

---

## Updating Script Versions

When updating a script version (e.g., 1.2 → 1.3):

### 1. Update `build.gradle`

```groovy
version = '1.3'  // <-- update this
```

### 2. Update `@ScriptDefinition` in Java Source

Find the main script file (e.g., `dSunbleakWCer.java`):

```java
@ScriptDefinition(
        name = "dSunbleakWCer",
        description = "Chops and optionally banks Ironwood logs on Sunbleak island",
        skillCategory = SkillCategory.WOODCUTTING,
        version = 1.3,  // <-- update this
        author = "Tidal"
)
public class dSunbleakWCer extends Script {
    // ...
}
```

### 3. Rebuild

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
```

---

## Common Issues & Solutions

### Issue: "permission denied: ./gradlew"

**Cause**: Gradle wrapper isn't executable or is missing files

**Solution**: Use system Gradle instead:
```bash
gradle clean build
```

Or fix permissions:
```bash
chmod +x gradlew
```

### Issue: "class file has wrong version 61.0, should be 55.0"

**Cause**: Trying to build with Java 11 instead of Java 17

**Solution**: Set JAVA_HOME to Java 17:
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
```

### Issue: "package obf does not exist"

**Cause**: Code references `obf.Secrets` class (for stats tracking) which isn't available

**Solution**: Comment out the `sendStats()` method or stats-related code:

```java
private void sendStats(long gpEarned, long xpGained, long runtimeMs) {
    // stats tracking disabled - missing obf.Secrets dependency
    // to enable: add obf.Secrets class with STATS_URL and STATS_API fields
    /*
    try {
        // ... stats code here ...
    } catch (Exception e) {
        log("STATS", "❌ Error sending stats: " + e.getMessage());
    }
    */
}
```

### Issue: "Entry logo.png is a duplicate but no duplicate handling strategy has been set"

**Cause**: Resource files being included multiple times

**Solution**: Add to `build.gradle` jar task:
```groovy
jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // ...
}
```

### Issue: "Could not find or load main class org.gradle.wrapper.GradleWrapperMain"

**Cause**: Gradle wrapper files are corrupted or missing

**Solution**: Use system Gradle instead of `./gradlew`:
```bash
gradle clean build
```

---

## Creating New Scripts from Boilerplate

### 1. Create Directory Structure

```bash
cd /Users/zaffre/Documents/Engineering/Projects/tidals-scripts
mkdir -p dNewScript/src/main/{java,resources}
mkdir -p dNewScript/jar
```

### 2. Create `settings.gradle`

```groovy
/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/8.1.1/userguide/multi_project_builds.html
 */

rootProject.name = 'dNewScript'
```

### 3. Create `build.gradle`

Copy the template from section "Build Configuration File" above, updating:
- `version = '1.0'` for new script
- Everything else can stay the same

### 4. Add Java Source Files

Create your script classes in `src/main/java/`

### 5. Add Resources

Add `logo.png` and other resources to `src/main/resources/`

### 6. Build

```bash
cd dNewScript
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
```

---

## Build Process Breakdown

### What Happens During Build

1. **`:clean`** - Deletes the `jar/` directory
2. **`:compileJava`** - Compiles `.java` files to `.class` files
   - Uses API.jar as compile-only dependency
   - Targets Java 17 bytecode
3. **`:processResources`** - Copies resources (logo.png, etc.)
4. **`:classes`** - Combines compiled classes and resources
5. **`:jar`** - Packages everything into a JAR file
   - Output: `jar/dScriptName.jar`
6. **`:assemble`** - Finalizes all outputs
7. **`:build`** - Runs all tasks including tests (if present)

### Output Files

After successful build:
```
dScriptName/
├── build/
│   ├── classes/java/main/     # Compiled .class files
│   ├── resources/main/         # Processed resources
│   └── libs/                   # (not used, we use jar/)
└── jar/
    └── dScriptName.jar         # ✅ Final output JAR
```

---

## Batch Building Multiple Scripts

### Build All Scripts Script

Create `build-all.sh` in project root:

```bash
#!/bin/bash

# set java 17
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home

# root directory
ROOT="/Users/zaffre/Documents/Engineering/Projects/tidals-scripts"
cd "$ROOT"

# find all directories with build.gradle
for dir in */; do
    # skip API and other non-script directories
    if [[ "$dir" == "API/" || "$dir" == "Documentation/" ]]; then
        continue
    fi
    
    # check if build.gradle exists
    if [[ -f "${dir}build.gradle" ]]; then
        echo "📦 Building ${dir%/}..."
        cd "$dir"
        gradle clean build
        if [[ $? -eq 0 ]]; then
            echo "✅ ${dir%/} built successfully"
        else
            echo "❌ ${dir%/} failed to build"
        fi
        cd "$ROOT"
        echo ""
    fi
done

echo "🎉 All builds complete!"
```

Make it executable:
```bash
chmod +x build-all.sh
```

Run it:
```bash
./build-all.sh
```

---

## Verification

### Check JAR Was Created

```bash
ls -lh jar/
# should show: dScriptName.jar with size ~40-50KB

# check contents
unzip -l jar/dScriptName.jar | head -20
```

### Verify Manifest

```bash
unzip -p jar/dScriptName.jar META-INF/MANIFEST.MF
```

### Test JAR in OSMB

1. Copy JAR to OSMB scripts directory
2. Launch OSMB client
3. Script should appear in script selector
4. Check version matches your updated version

---

## AI Agent Workflow

When asked to build or update a script JAR:

1. **Check if `build.gradle` exists** in script directory
   - If not, create it using the template
2. **Update version** if requested
   - In `build.gradle`: `version = 'X.Y'`
   - In Java source: `@ScriptDefinition(version = X.Y, ...)`
3. **Check for `obf.Secrets` references**
   - If present and causing errors, comment out stats code
4. **Run build command**:
   ```bash
   cd /path/to/script && JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
   ```
5. **Verify output**:
   ```bash
   ls -lh jar/
   ```
6. **Report success** with JAR location and size

---

## Reference: Java Versions

```bash
# check current java version
java -version

# list all installed java versions
/usr/libexec/java_home -V

# available java 17 installations on this system
/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home

# api.jar requires java 17 (class version 61.0)
```

---

## Troubleshooting Checklist

- [ ] Java 17+ is installed and JAVA_HOME is set correctly
- [ ] `build.gradle` exists in script directory
- [ ] `settings.gradle` has correct `rootProject.name`
- [ ] `../API/API.jar` path is correct (one level up from script dir)
- [ ] Source files are in `src/main/java/`
- [ ] Resources are in `src/main/resources/`
- [ ] No references to `obf.Secrets` (or they're commented out)
- [ ] `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` is set in jar task
- [ ] Gradle daemon isn't stuck (try `gradle --stop` then rebuild)

---

## Additional Notes

### Why compileOnly for API.jar?

The API.jar is already present in the OSMB runtime environment. Using `compileOnly` means:
- We can compile against it
- It won't be bundled into our JAR (keeps size small)
- OSMB loads it at runtime

### Why Java 17?

The API.jar is compiled with Java 17 (class version 61.0). Our scripts must be compiled with Java 17+ to be compatible with the API.

### Gradle vs ./gradlew

- `gradle` - System-wide Gradle installation
- `./gradlew` - Project-specific Gradle wrapper (often broken in this repo)
- **Use system `gradle`** for reliable builds

---

## Quick Command Reference

```bash
# build single script
cd dScriptName && JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build

# clean only
gradle clean

# build without clean
gradle build

# stop gradle daemon (if stuck)
gradle --stop

# check java version
java -version

# list all java installations
/usr/libexec/java_home -V

# check if jar exists
ls -lh jar/*.jar

# inspect jar contents
unzip -l jar/dScriptName.jar

# check jar manifest
unzip -p jar/dScriptName.jar META-INF/MANIFEST.MF
```

---

**Last Updated**: January 2, 2026  
**OSMB API Version**: Requires Java 17+ (class version 61.0)  
**Gradle Version**: 8.13
