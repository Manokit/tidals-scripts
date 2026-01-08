# OSMB Script Development Guide

> **CRITICAL: OSMB is a COLOR BOT** - It uses visual/pixel detection, NOT injection. You cannot access game memory directly. All detection is done through screen analysis, color matching, and OCR.

---

## 🚀 Getting Started

**New to OSMB? Start here:**

1. **`docs/critical-concepts.md`** - ⭐ **MUST READ FIRST** - Color bot fundamentals
2. **`docs/api-reference.md`** - Core API overview and imports
3. **`docs/common-mistakes.md`** - Avoid these pitfalls
---

## 📖 Documentation by Category

### 🎯 Core Patterns & Techniques

**Essential Patterns:**
- **`docs/highlight-npc-detection.md`** - ⚡ Instant NPC movement detection (guards, combat)
- **`docs/walking-npcs.md`** - Walking, NPC interaction, pathfinding
- **`docs/banking-patterns.md`** - Safe banking, deposits, withdrawals
- **`docs/ui-widgets.md`** - Dialogues, equipment, minimap, interfaces
- **`docs/paint-overlay.md`** - Professional paint implementation

**Advanced Techniques:**
- **`docs/advanced-patterns.md`** - Production patterns from real scripts
- **`docs/advanced-techniques.md`** - Ground items, agility, combat, health
- **`docs/specialized-patterns.md`** - Minigames, altars, processing, smelting

### 🔧 API References

**Core APIs:**
- **`docs/ItemManager.md`** - Item management and searching (NEW)
- **`docs/PixelAnalyzer_API.md`** - Visual analysis and color detection (NEW)
- **`docs/Deposit-box.md`** - Deposit box API (NEW)
- **`docs/ProfileManager.md`** - Breaks, world hopping, AFK (NEW)
- **`docs/XPDropsListener.md`** - XP drop tracking (NEW)
- **`docs/EntityMapDot.md`** - Minimap entity types (NEW)

**JSON API References:**
- **`docs/Finger.json`** - Complete Finger API methods
- **`docs/ScriptCore.json`** - ScriptCore interface documentation
- **`docs/Minimap.json`** - Minimap API reference
- **`docs/com.osmb.api.walker.json`** - Walker API
- **`docs/com.osmb.api.walker.WalkConfig.json`** - WalkConfig options
- **`docs/Interface Chatbox.json`** - Chatbox interface API
- **`docs/Interface TabManager.json`** - Tab management API
- **`docs/Interface ScriptOptions.json`** - Script options API
- **`docs/rectangle area.json`** - Rectangle area utilities

### 📝 Working Examples

- **`examples/mining-script.md`** - Complete mining bot with state machine
- **`examples/gem-cutting.md`** - Gem cutting with dialogue handling
- **`examples/BlisterwoodChopper.java`** - Woodcutting with inventory checking and dropping

---

## ⚡ Quick Reference by Use Case

### Finding & Interacting with NPCs

**Combat / Attacking:**
→ **`docs/highlight-npc-detection.md`** ⚡ (Fastest method)
- Sub-tile movement detection (3-5 pixels)
- Frame-by-frame tracking (50-150ms detection)
- Combat targeting patterns
- Multi-NPC tracking

**General NPC Interaction:**
→ **`docs/walking-npcs.md`** (TOP SECTION)
- Minimap positions + tile cubes (3 methods)
- Finding NPCs by name
- Large NPC handling (2x2, 3x3)
- Shop keepers, dialogue NPCs

**Thieving with Guards:**
→ **`docs/highlight-npc-detection.md`** ⚡ (INSTANT detection)
- 10-20x faster than minimap
- Complete thieving state machine
- Guard alert system

### Banking & Inventory

**Safe Banking:**
→ **`docs/banking-patterns.md`**
- Bank loading delays (300-500ms)
- Withdrawal timing
- Deposit box usage
- Free slot calculations

**Deposit Box:**
→ **`docs/Deposit-box.md`** (NEW)
- Deposit box API reference
- Direct API usage

**Item Management:**
→ **`docs/ItemManager.md`** (NEW)
- Item searching and filtering
- Item group management
- Drop actions

**Inventory Checking:**
→ **`examples/BlisterwoodChopper.java`**
- Check if inventory is full
- Get item counts from snapshots
- Efficient inventory state tracking

### Visual Detection

**Pixel & Color Detection:**
→ **`docs/PixelAnalyzer_API.md`** (NEW)
- Highlight bounds detection
- Pixel clusters
- Color matching
- Shape analysis

**Identical Items:**
→ **`docs/critical-concepts.md`**
- BuffOverlay for charges
- External tracking
- Menu detection

### UI & Interfaces

**Dialogues:**
→ **`docs/ui-widgets.md`**
- Multi-step sequences
- Item selection dialogs
- Level-up handling

**Minimap:**
→ **`docs/Minimap.json`** & **`docs/EntityMapDot.md`** (NEW)
- NPC positions
- Entity types (dots)
- Minimap API

**Tabs & Equipment:**
→ **`docs/ui-widgets.md`** & **`docs/Interface TabManager.json`**
- Equipment tab
- Spellbook teleports
- Tab switching

### Movement & Pathfinding

**Walking:**
→ **`docs/walking-npcs.md`** & **`docs/com.osmb.api.walker.json`**
- Basic walking
- Break conditions
- Multi-step travel
- Area definitions

**WalkConfig:**
→ **`docs/com.osmb.api.walker.WalkConfig.json`** (NEW)
- Configuration options
- Custom walk behavior

### Looting & Ground Items

→ **`docs/advanced-techniques.md`**
- Ground item detection
- Loot tracking
- Distance filtering
- Value-based looting

### Combat & Health

→ **`docs/advanced-techniques.md`**
- Health monitoring
- Food eating patterns
- Prayer restoration
- Animation detection

### Agility

→ **`docs/advanced-techniques.md`**
- Obstacle detection
- Course completion tracking
- Lap counting
- Animation waiting

### Minigames & Altars

→ **`docs/specialized-patterns.md`**
- Castle Wars AFK
- Altar offering (POH)
- Pest Control
- Burner management

### Processing Activities

→ **`docs/specialized-patterns.md`**
- Sawmill running
- Winemaking
- Blast furnace
- Cannonball smelting
- Cooking patterns

### Script Features

**XP Tracking:**
→ **`docs/XPDropsListener.md`** (NEW)
- XP drop tracking
- Skill-specific listeners

**Breaks & Anti-Ban:**
→ **`docs/ProfileManager.md`** (NEW)
- Break scheduling
- World hopping
- AFK behavior

**Paint Overlays:**
→ **`docs/paint-overlay.md`**
- Professional design
- XP tracking integration
- Level 99 handling

---

## 🎓 Learning Path

### Beginner (Start Here)
1. Read **`docs/critical-concepts.md`** - Understand color bot limitations
2. Study **`examples/mining-script.md`** - See complete working example
3. Review **`docs/common-mistakes.md`** - Avoid common pitfalls

### Intermediate
4. Learn **`docs/banking-patterns.md`** - Master safe banking
5. Study **`docs/walking-npcs.md`** - NPC interaction
6. Practice **`docs/ui-widgets.md`** - Dialogue handling

### Advanced
7. Master **`docs/highlight-npc-detection.md`** - Instant NPC detection
8. Study **`docs/advanced-patterns.md`** - Production-ready patterns
9. Reference API docs as needed for specific features

---

## 🔥 Critical Concepts (Quick Reference)

### 1. NPCs Aren't in ObjectManager
```java
// Get NPC positions from minimap
UIResultList<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

for (WorldPosition npcPos : npcPositions) {
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
    if (tileCube == null) continue;
    
    MenuEntry response = getFinger().tapGetResponse(true, tileCube);
    if (response != null && response.getEntityName().contains("Guard")) {
        getFinger().tap(tileCube, "Attack");
    }
}
```
→ See **`docs/walking-npcs.md`** for complete guide

### 2. Identical Sprites Cannot Be Distinguished
```java
// WRONG - Waterskin(0) through (4) look identical!
inventory.search(Set.of(ItemID.WATERSKIN_4)); 

// CORRECT - Use BuffOverlay for items with charges
BuffOverlay waterskinBuff = new BuffOverlay(core, WATERSKIN_ID);
String charges = waterskinBuff.getText();
```
→ See **`docs/critical-concepts.md`** for solutions

### 3. Collision Map is Static
```java
// WRONG - Assuming collision map updates in real-time
if (collisionMap.isBlocked(doorTile)) { /* door is closed */ }

// CORRECT - Visual verification via menu
MenuEntry response = getFinger().tapGetResponse(true, doorPoly);
if (response != null && response.getAction().equalsIgnoreCase("Open")) {
    // Door is closed, open it
}
```
→ See **`docs/critical-concepts.md`**

### 4. Always Verify Interactions
```java
// Verify what you're clicking
MenuEntry response = getFinger().tapGetResponse(true, objectPoly);
if (response != null && response.getAction().equalsIgnoreCase("Mine")) {
    getFinger().tap(objectPoly, "Mine");
}
```

### 5. Use State Machines
```java
private enum State { IDLE, GATHERING, BANKING, WALKING }

@Override
public int poll() {
    switch (getState()) {
        case GATHERING: return gather();
        case BANKING: return bank();
        case WALKING: return walk();
        default: return 600;
    }
}
```

### 6. Prioritize Regions
```java
@Override
public ScriptOptions getScriptOptions() {
    return new ScriptOptions() {
        @Override
        public int[] regionsToPrioritise() {
            return new int[]{12850, 12851}; // Avoid 90+ second startup
        }
    };
}
```

### 7. Inventory State Checking
```java
// Create inventory snapshot (returns ItemGroupResult)
ItemGroupResult inventorySnapshot = getWidgetManager().getInventory().search(Set.of(ITEM_ID));

// Null check is essential
if (inventorySnapshot == null) return 0;

// Check if inventory is full
if (inventorySnapshot.isFull()) {
    log("Inventory is full!");
    // Handle full inventory (drop, bank, etc.)
}

// Get count of specific item
int itemCount = inventorySnapshot.getAmount(ITEM_ID);
log("Found " + itemCount + " items");
```
→ See **`examples/BlisterwoodChopper.java`** for complete example

---

## 🛠️ Basic Script Structure

```java
@ScriptManifest(name = "Name", author = "Author", version = 1.0, description = "Description")
public class MyScript extends Script {
    
    @Override
    public void onStart() {
        // Called once at startup
        log("Script started!");
    }
    
    @Override  
    public int poll() {
        // Main loop - return sleep time in milliseconds
        // This is your script's "brain"
        return doTask();
    }
    
    @Override
    public void onNewFrame() {
        // Called every game frame (READ-ONLY)
        // Never modify state here, only read/monitor
    }
    
    @Override
    public void onStop() {
        // Called when script stops
        log("Script stopped!");
    }
}
```

---

## 🔑 Core API Access

```java
// UI Components
getWidgetManager()      // Bank, Inventory, Dialogue, Tabs, Minimap

// Game Objects
getObjectManager()      // Find RSObjects (trees, rocks, banks, etc.)
getSceneManager()       // NPCs, ground items, scene tiles

// Movement
getWalker()             // Pathfinding and walking

// Input
getFinger()             // Mouse/touch input, clicking, tapping

// Visual Analysis
getPixelAnalyzer()      // Color detection, highlight bounds, pixel clusters
getOCR()                // Text recognition

// Items
getItemManager()        // Item searching, management (See ItemManager.md)

// Utilities
getWorldPosition()      // Get player position
getSceneProjector()     // Convert positions to polygons
```

---

## 🐛 Troubleshooting

**Script won't start?** → Check region priorities (`docs/common-mistakes.md`)
**Can't find NPCs?** → Read `docs/walking-npcs.md` TOP SECTION
**Bank withdrawing 0?** → Check `examples/gem-cutting.md` for timing
**NPCs not detected?** → Use `docs/highlight-npc-detection.md`
**Items not found?** → See `docs/ItemManager.md` and `docs/critical-concepts.md`
**Inventory checking issues?** → See `examples/BlisterwoodChopper.java` for isFull() pattern
**Dialogue not working?** → Check `docs/ui-widgets.md`
**Paint broken?** → See `docs/paint-overlay.md`
**General debugging?** → Read `docs/common-mistakes.md`

---

## 📚 External Resources

- **API Documentation**: https://doc.osmb.co.uk
- **Javadocs**: https://osmb.co.uk/javadocs (ItemID constants)
- **Debug Tool**: Built into OSMB client (get exact HSL/RGB values)
- **Discord**: Community support and knowledge base

---

## 💡 Pro Tips

1. **Think visually** - You're a color bot, not an injection bot
2. **Verify everything** - Use `tapGetResponse()` before clicking
3. **Handle nulls** - Everything can be null, check before using
4. **Use Debug Tool** - Get exact color values for detection
5. **Read critical-concepts.md** - Understand fundamental limitations
6. **Check examples** - See working patterns before writing your own
7. **Start simple** - Build complexity gradually

---

*Remember: OSMB gives you eyes, not memory access. Be creative with visual detection!*
