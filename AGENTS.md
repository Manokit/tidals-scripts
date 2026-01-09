# OSMB Script Development Guide

> **CRITICAL: OSMB is a COLOR BOT** - It uses visual/pixel detection, NOT injection. You cannot access game memory directly. All detection is done through screen analysis, color matching, and OCR.

**CRITICAL: NEVER ASSUSME A METHOD EXISTS. ALWAYS REFER TO ONE OF THE CORE REFERENCES, OR ANYTHING IN THE DOCS DIR, EXAMPLES DIR, OR OTHER TIDALS SCRIPTS FOR DIRECTION AND CLARIFICATION**

## 📚 Documentation Index

**Core References:**
- `docs/api-reference.md` - Complete API methods and imports
- `docs/critical-concepts.md` - Color bot fundamentals (MUST READ)
- `docs/Common-menu-entries.md` - Exact menu action strings for .interact() calls
- `docs/banking-patterns.md` - Banking, inventory, deposits
- `docs/walking-npcs.md` - Walking, NPC interaction, objects
- `docs/ui-widgets.md` - Dialogue, equipment, minimap, overlays
- `docs/paint-overlay.md` - Professional paint implementation
- `docs/common-mistakes.md` - Debugging guide, pitfalls to avoid
- `docs/advanced-patterns.md` - Production patterns from Davy's scripts
- `docs/advanced-techniques.md` - Ground items, agility, combat, health
- `docs/specialized-patterns.md` - Altars, minigames, processing, smelting

**Examples:**
- `examples/gem-cutting.md` - Gem cutting specifics

## Quick Reference

// when writing comments, make them blunt and lowercase

### Basic Script Structure
```java
@ScriptManifest(name = "Name", author = "Author", version = 1.0, description = "Description")
public class MyScript extends Script {
    @Override
    public void onStart() { }           // Called once at startup
    
    @Override  
    public int poll() { return 600; }   // Main loop - return sleep ms
    
    @Override
    public void onStop() { }            // Called on stop
}
```

### Core API Access
```java
getWidgetManager()      // UI: Bank, Inventory, Dialogue, Tabs, Minimap
getObjectManager()      // Find RSObjects (trees, rocks, banks, etc.)
getSceneManager()       // NPCs, ground items, tiles
getWalker()             // Pathfinding and walking
getFinger()             // Mouse/touch input
getPixelAnalyzer()      // Color/pixel detection
getOCR()                // Text recognition
```

## Top 5 Critical Concepts

### 1. NPCs Aren't in ObjectManager - Use Minimap!
```java
// Get NPC positions from minimap
List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

for (WorldPosition npcPos : npcPositions) {
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 60);
    if (tileCube == null) continue;
    
    // Verify what's at this position
    MenuEntry response = getFinger().tapGetResponse(true, tileCube);
    if (response != null && response.getEntityName().contains("Guard")) {
        getFinger().tap(tileCube, "Attack");
    }
}
```
→ **See `docs/walking-npcs.md` for complete NPC finding guide (highlights, clusters, etc.)**

### 2. Items with Identical Sprites CANNOT Be Distinguished
```java
// WRONG - Waterskin(0) through (4) look identical!
inventory.search(Set.of(ItemID.WATERSKIN_4)); 

// CORRECT - Use BuffOverlay for items with charges
BuffOverlay waterskinBuff = new BuffOverlay(core, WATERSKIN_ID);
String charges = waterskinBuff.getText();
```
→ **See `docs/critical-concepts.md` for solutions**

### 3. Items with Identical Sprites CANNOT Be Distinguished
```java
// WRONG - Waterskin(0) through (4) look identical!
inventory.search(Set.of(ItemID.WATERSKIN_4)); 

// CORRECT - Use BuffOverlay for items with charges
BuffOverlay waterskinBuff = new BuffOverlay(core, WATERSKIN_ID);
String charges = waterskinBuff.getText();
```
→ **See `docs/critical-concepts.md` for solutions**

### 4. Visual Door Detection (Collision Map is Static!)
```java
// WRONG - Assuming collision map is accurate
if (collisionMap.isBlocked(doorTile)) { /* door is closed */ }

// CORRECT - Visual verification via menu
MenuEntry response = getFinger().tapGetResponse(true, doorPoly);
if (response != null && response.getAction().equalsIgnoreCase("Open")) {
    // Door is closed, open it
}
```
→ **See `docs/critical-concepts.md` for details**

### 5. Use Direct tap() for Interactions - Avoid Double-Tap Bug
```java
// WRONG - causes double interaction (tap then menu open)
MenuEntry response = getFinger().tapGetResponse(true, bounds);
if (response != null && response.getAction().contains("Pick")) {
    getFinger().tap(bounds, response.getAction()); // BUG: taps again!
}

// CORRECT - when you know the action, just tap directly
getFinger().tap(bounds, "Pick");  // opens menu and selects in one step

// CORRECT - when you need to verify first, don't tap after
MenuEntry response = getFinger().tapGetResponse(true, bounds);
if (response != null && response.getAction().contains("Pick")) {
    // menu is already open from tapGetResponse, action was selected
    log("Picked item");
}
```
**Rule: When speed isn't critical, prefer direct `tap(shape, "Action")` - it's safer and cleaner.**

→ **See `docs/walking-npcs.md` for interaction patterns**

### 6. State Machine Pattern
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
```

### 7. Prioritize Regions for Fast Startup
```java
@Override
public ScriptOptions getScriptOptions() {
    return new ScriptOptions() {
        @Override
        public int[] regionsToPrioritise() {
            return new int[]{12850, 12851}; // Your operating regions
        }
    };
}
```

## Common Tasks - Quick Links

**Finding NPCs?** → `docs/walking-npcs.md` (TOP SECTION)
- Minimap positions + tile cubes
- Highlight detection for combat
- Pixel clusters for precision
- Large NPC handling (2x2, 3x3)
- Finding NPCs by name

**Banking Issues?** → `docs/banking-patterns.md`
- Safe banking pattern
- Deposit box usage
- Bank loading delays
- Withdrawal timing

**NPC/Object Interaction?** → `docs/walking-npcs.md`
- Finding objects
- NPC position detection
- Shrinking click areas
- Walking with break conditions

**Dialogue Not Working?** → `docs/ui-widgets.md`
- Dialogue handling
- Multi-step sequences
- Item selection dialogs

**Paint Overlay?** → `docs/paint-overlay.md`
- Professional clean design
- XP tracking integration
- Level 99 handling

**Script Broken?** → `docs/common-mistakes.md`
- Null checking
- onNewFrame misuse
- Timing issues
- Debug logging

**Ground Items / Looting?** → `docs/advanced-techniques.md`
- Finding ground items
- Loot tracking
- Distance filtering
- Multi-item looting

**Agility Course?** → `docs/advanced-techniques.md`
- Obstacle detection
- Course completion tracking
- Animation waiting
- Lap counting

**Combat / Health?** → `docs/advanced-techniques.md`
- Health monitoring
- Food eating
- Prayer restoration
- Animation detection

**Minigames / Altars?** → `docs/specialized-patterns.md`
- Castle Wars AFK
- Altar offering
- Pest Control
- Burner management

**Processing Activities?** → `docs/specialized-patterns.md`
- Sawmill running
- Winemaking
- Blast furnace
- Cannonball smelting

## When to Check Detailed Docs

- **Can't find NPCs** → See `docs/walking-npcs.md` TOP SECTION (minimap positions, highlights, clusters)
- **NPCs not clickable** → See `docs/walking-npcs.md` for tapGetResponse verification
- **Paint looks amateur** → See `docs/paint-overlay.md` for modern template
- **Bank withdrawing 0 items** → See `examples/gem-cutting.md` for timing fix
- **Items not detected** → See `docs/critical-concepts.md` for HSL/RGB
- **Complex travel** → See `docs/advanced-patterns.md` for multi-step sequences

## Resources

- **API Documentation**: https://doc.osmb.co.uk
- **Javadocs**: https://osmb.co.uk/javadocs (ItemID constants)
- **Debug Tool**: Built into OSMB client
- **Discord**: Developer knowledge base

---

*Remember: Think visually, verify interactions, and be creative when the API can't distinguish items directly!*
