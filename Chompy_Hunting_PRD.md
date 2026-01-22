# Chompy Hunting PRD for OSMB

## Overview

Chompy bird hunting is a Ranged activity where players kill chompy birds for Western Provinces Diary completion, cosmetic hats, and the chompy chick pet. This script is designed for **efficient, quick kills** to help players achieve the 300 kills required for Elite Void (Hard Western Diary).

The script assumes the player is already equipped and positioned near the hunting location, in a "Ready" state.

---

## Requirements

### Quest Requirements
- **Big Chompy Bird Hunting** (REQUIRED) - Unlocks the ability to hunt chompy birds

### Recommended Quests
- **Zogre Flesh Eaters** (STRONGLY RECOMMENDED) - Unlocks comp ogre bow and brutal arrows for faster kills

### Skill Requirements
- **30 Ranged minimum** (to equip ogre bow/comp ogre bow)
- Higher Ranged level recommended for faster kills

### Western Provinces Diary Benefits
- **Easy Diary (30 kills)**: 25% chance for 2 chompies to spawn
- **Medium Diary (125 kills)**: 50% chance for 2 chompies to spawn  
- **Hard Diary (300 kills)**: Unlocks Elite Void  
- **Elite Diary (1000 kills)**: 100% chance for 2 chompies to spawn + chompy chick pet unlock (1/500 drop rate)

---

## Equipment & Items

### Required Equipment

| Item | Item ID | Notes |
|------|---------|-------|
| **Ogre bow** | `1427` | Basic bow, slower attack speed |
| **Comp ogre bow** | `2302` | PREFERRED - Faster attack speed, tradeable |

**Equipment Check**: Script must verify player has EITHER ogre bow OR comp ogre bow equipped before starting.

### Required Ammunition

The script should check for ANY of the following arrow types (in order of preference):

| Arrow Type | Item ID | Notes | Preferred For |
|------------|---------|-------|---------------|
| **Rune brutal** | `4803` | Highest damage, one-shots most chompies | Fastest kills |
| **Adamant brutal** | `4798` | High damage, cost-effective | Good balance |
| **Mithril brutal** | `4793` | Moderate damage | Budget option |
| **Ogre arrow** | `2866` | Basic arrows, lowest damage | Not recommended for speed |

**Note**: Ogre bow can ONLY fire ogre arrows and up to mithril brutal. Comp ogre bow can fire ALL brutal arrow types. Script should warn if using ogre bow with adamant/rune brutals.

### Required Tools

| Item | Item ID | Quantity | Notes |
|------|---------|----------|-------|
| **Ogre bellows (empty)** | `1420` | 2-24 | More bellows = more efficiency |
| **Ogre bellows (1 load)** | `1421` | Variant | Script detects by base sprite |
| **Ogre bellows (2 loads)** | Variant | Variant | Script detects by base sprite |
| **Ogre bellows (3 loads)** | Variant | Variant | Script detects by base sprite |

**CRITICAL**: Ogre bellows with different gas levels (0, 1, 2, 3 loads) have **identical sprites** and cannot be distinguished visually in OSMB. The script should:
- Count total bellows in inventory (regardless of charge level)
- Track bellows usage by counting bloated toads created
- Refill bellows when needed (every 3 toads uses 1 bellow's worth of gas)

### Bait Items

| Item | Item ID | Quantity | Notes |
|------|---------|----------|-------|
| **Bloated toad** | `1422` | Max 3 at once | Created by using filled bellows on swamp toads |

**Note**: Cannot carry more than 3 bloated toads in inventory. Script should drop them immediately to free space.

### Optional Equipment

| Item | Purpose | Notes |
|------|---------|-------|
| **Ava's device** | Auto-retrieve arrows | Strongly recommended |
| **Bonecrusher** | Passive Prayer XP | Optional, equip in pocket slot |
| **Weight-reducing gear** | Movement speed | Graceful, or high-tier d'hide |

**Accuracy Note**: Above 60 Ranged, accuracy has negligible effect on kill times due to chompies' low defense. Prioritize Ranged Strength bonuses (ammunition) over accuracy.

---

## Location

### Recommended Location
**South of Castle Wars** - Primary hunting spot

**Region ID**: `9519`

**Key Coordinates**:
- **Toad Drop Area**: RectangleArea(2390, 3044, 3, 2, 0)
- **Swamp Bubble 1**: WorldPosition(2293, 3049, 0)
- **Swamp Bubble 2**: WorldPosition(2395, 3046, 0)

**Access Methods**:
- Fairy ring code: `BKP` (fastest)
- Castle Wars teleport/minigame teleport
- Walk from Castle Wars

**Advantages**:
- No aggressive monsters
- Close proximity to swamp bubbles (2 static locations)
- Open space for chompy spawns (21x21 area per toad)
- Safe AFK environment
- All resources within 20 tiles

**Region Verification**:
```java
int regionId = getWidgetManager().getMinimap().getRegionId();
if (regionId != 9519) {
    log("ERROR: Not in chompy hunting area (current region: " + regionId + ")");
    // walk to castle wars or stop script
    return false;
}
```

### Alternative Location
**Feldip Hills** - NOT RECOMMENDED for script

**Region ID**: Different (near Rantz's area)

**Disadvantages**:
- Aggressive wolves (level 64) that attack player
- Rantz's personal hunting ground (small restricted area south of Rantz)
- More obstacles/terrain
- Farther from swamp bubbles

**Script Location**: This PRD is designed specifically for Region 9519 (Castle Wars area) only.

---

## Chompy Hunting Flow

### Phase 1: Initialization & Equipment Check

```
1. Verify location - Check region ID 9519 (Castle Wars chompy area)
   - If not in region 9519 → Walk to area or ERROR
2. Check equipment slot for ogre bow (1427) OR comp ogre bow (2302)
   - If neither equipped → ERROR: "Ogre bow required" → STOP
3. Check ammo slot for arrows:
   - Search for: 4803, 4798, 4793, 2866 (in that order)
   - If no arrows → ERROR: "Arrows required" → STOP
   - If < 10 arrows → WARNING: "Low arrow count"
4. Count ogre bellows in inventory
   - Each bellow = 3 charges = 3 toads potential
   - Calculate: totalToadsPerCycle = bellowCount * 3
   - If < 2 bellows → WARNING: "Low bellow count, recommend 2-24"
5. Check inventory space
   - Need at least 3 free slots for bloated toads
   - If < 3 free slots → deposit/drop items
6. Initialize ground toad tracking
   - Create empty list to track dropped toad positions
   - Will be used to verify toads are still on ground
```

**Equipment Validation Example**:
```java
// check region ID
int regionId = getWidgetManager().getMinimap().getRegionId();
if (regionId != 9519) {
    log("ERROR: Not in chompy hunting area (Region 9519)");
    // walk to castle wars area or stop
    return -1;
}

// check for ogre bow or comp ogre bow
boolean hasOgreBow = getWidgetManager().getEquipment()
    .findItem(1427, 2302).isFound();

if (!hasOgreBow) {
    log("ERROR: Ogre bow or comp ogre bow required!");
    return -1; // stop script
}

// count bellows for cycle planning
ItemGroupResult bellows = getWidgetManager().getInventory()
    .search(Set.of(1420)); // searches for bellow sprite
int bellowCount = (bellows != null) ? bellows.getTotalQuantity() : 0;
int toadsPerCycle = bellowCount * 3;
log("Bellows found: " + bellowCount + " (can create " + toadsPerCycle + " toads per cycle)");
```

### Phase 2: Fill Ogre Bellows with Swamp Gas

**Known Swamp Bubble Locations**:
- **Primary**: WorldPosition(2293, 3049, 0) - Near swamp pond
- **Secondary**: WorldPosition(2395, 3046, 0) - Alternate location

**Swamp Bubble Detection**:
```java
// static bubble locations
private static final WorldPosition BUBBLE_LOCATION_1 = new WorldPosition(2293, 3049, 0);
private static final WorldPosition BUBBLE_LOCATION_2 = new WorldPosition(2395, 3046, 0);

// find closest bubble location
WorldPosition playerPos = getWorldPosition();
WorldPosition bubbleLocation = (playerPos.distanceTo(BUBBLE_LOCATION_1) < 
                                 playerPos.distanceTo(BUBBLE_LOCATION_2)) 
    ? BUBBLE_LOCATION_1 
    : BUBBLE_LOCATION_2;

// walk to bubble if not close enough
if (playerPos.distanceTo(bubbleLocation) > 3) {
    getWalker().walkTo(bubbleLocation);
    submitTask(() -> getWorldPosition().distanceTo(bubbleLocation) <= 3, 5000);
}

// get the tile cube for the bubble location
Polygon bubblePoly = getSceneProjector().getTileCube(bubbleLocation, 40);
if (bubblePoly == null) {
    log("ERROR: Cannot see swamp bubbles");
    return false;
}

// use menuHook to find "Suck" action on swamp bubbles
boolean suckedBubbles = getFinger().tapGameScreen(bubblePoly, menuEntries -> 
    menuEntries.stream()
        .filter(entry -> "Suck".equalsIgnoreCase(entry.getAction()) && 
                        entry.getEntityName().contains("bubbles"))
        .findFirst()
        .orElse(null)
);

if (!suckedBubbles) {
    log("ERROR: Failed to suck swamp bubbles");
    return false;
}

// wait for interaction to complete (each bellow gets filled)
submitTask(() -> {
    // check if bellows are filled (can be detected by game message or state change)
    return true; // simplified for example
}, 3000);
```

**Process**:
```
1. Determine closest bubble location (2293,3049,0 or 2395,3046,0)
2. Walk to bubble location if not within 3 tiles
3. Create tile cube polygon at bubble position
4. Use menuHook pattern to find "Suck Swamp bubbles" action
5. Click bubbles to fill bellows (fills ALL bellows in inventory automatically)
6. Wait for fill animation/confirmation
7. Move to toad collection phase
```

**Implementation Notes**:
- Swamp bubbles are static objects at known locations
- The "Suck" action fills ALL bellows in inventory at once
- Each bellow gets 3 charges from one "Suck" action
- No need to click multiple times - game handles batch filling

### Phase 3: Inflate Swamp Toads (Hybrid Detection)

**Swamp Toad Detection Strategy**:
Use **BOTH** minimap NPC positions AND NPC highlighting to reliably identify swamp toads.

**Why Hybrid Approach?**
- Minimap shows all NPC positions (yellow dots)
- Highlighting confirms visual presence of NPC
- MenuHook verifies it's actually a swamp toad
- Reduces false positives from other nearby NPCs

**Swamp Toad Highlighting**:
```java
// swamp toads typically have yellow/green NPC highlight
// use Debug Tool to sample exact color in your game
private static final SearchablePixel TOAD_HIGHLIGHT = new SearchablePixel(
    60,    // Hue (yellow)
    80,    // Saturation
    50,    // Lightness
    15, 15, 15  // Tolerances
);
```

**Process**:
```
1. Get NPC positions from minimap (yellow dots)
2. Filter NPCs within 10 tiles of player
3. For each NPC position:
   a. Create tile cube at NPC location
   b. Check for highlight bounds within tile cube
   c. If highlighted → Use menuHook to verify "Inflate Swamp toad"
   d. If verified → Interact with toad
4. Repeat until 3 bloated toads created
5. Move to toad dropping phase
```

**Implementation**:
```java
private boolean inflateToad() {
    WorldPosition playerPos = getWorldPosition();
    List<WorldPosition> npcPositions = getWidgetManager().getMinimap()
        .getNPCPositions().asList();
    
    for (WorldPosition npcPos : npcPositions) {
        // only check nearby NPCs
        if (npcPos.distanceTo(playerPos) > 10) continue;
        
        // create tile cube for NPC
        Polygon tileCube = getSceneProjector().getTileCube(npcPos, 40);
        if (tileCube == null) continue;
        
        // check for NPC highlighting (confirms visual presence)
        Rectangle highlightBounds = getPixelAnalyzer()
            .getHighlightBounds(tileCube, TOAD_HIGHLIGHT);
        
        if (highlightBounds == null) {
            // no highlight = not a visible NPC or wrong type
            continue;
        }
        
        // use menuHook to verify it's a swamp toad with "Inflate" action
        boolean inflated = getFinger().tapGameScreen(tileCube, menuEntries ->
            menuEntries.stream()
                .filter(entry -> "Inflate".equalsIgnoreCase(entry.getAction()) &&
                                entry.getEntityName().contains("Swamp toad"))
                .findFirst()
                .orElse(null)
        );
        
        if (inflated) {
            log("Inflated swamp toad at " + npcPos);
            
            // wait for toad to be added to inventory
            submitTask(() -> {
                ItemGroupResult toads = getWidgetManager().getInventory()
                    .search(Set.of(1422)); // bloated toad
                return toads != null && toads.getTotalQuantity() > 0;
            }, 2000);
            
            return true;
        }
    }
    
    log("ERROR: No swamp toads found to inflate");
    return false;
}

// main loop to get 3 toads
private boolean getThreeToads() {
    for (int i = 0; i < 3; i++) {
        if (!inflateToad()) {
            return false;
        }
        // small delay between inflations
        submitTask(() -> false, random(400, 600));
    }
    
    // verify we have exactly 3 bloated toads
    ItemGroupResult toads = getWidgetManager().getInventory()
        .search(Set.of(1422));
    int toadCount = (toads != null) ? toads.getTotalQuantity() : 0;
    
    if (toadCount != 3) {
        log("ERROR: Expected 3 toads, have " + toadCount);
        return false;
    }
    
    log("Successfully created 3 bloated toads");
    return true;
}
```

**CRITICAL**: 
- Maximum 3 bloated toads can be held at once
- Inflating a 4th toad will replace an existing one
- Must verify toad added to inventory before continuing
- Swamp toads are NPCs, not objects - use minimap positions

### Phase 4: Drop Bloated Toads as Bait (with Ground Tracking)

**Designated Drop Area**:
```java
private static final RectangleArea TOAD_DROP_AREA = new RectangleArea(2390, 3044, 3, 2, 0);
```

**Why This Location?**
- Open area with good line-of-sight
- No obstacles or terrain blocking chompy spawns
- Close to swamp bubbles for efficient cycling
- Safe from aggressive NPCs

**Ground Toad Tracking Setup**:
```java
// data structure to track dropped toads
private class DroppedToad {
    WorldPosition position;
    long droppedTime;
    
    DroppedToad(WorldPosition pos) {
        this.position = pos;
        this.droppedTime = System.currentTimeMillis();
    }
    
    boolean isExpired() {
        // toads explode after 60 seconds
        return System.currentTimeMillis() - droppedTime > 60000;
    }
}

// list to track all active ground toads
private List<DroppedToad> activeGroundToads = new ArrayList<>();
```

**Bait Placement Strategy**:
```
1. Walk to TOAD_DROP_AREA if not already there
2. Enable NPC highlighting for bloated toads (tag-all)
3. For each bloated toad in inventory:
   a. Get current WorldPosition (where toad will drop)
   b. Drop toad using menu action "Drop bloated toad"
   c. Record position and timestamp in activeGroundToads list
   d. Move 1 tile away for next toad drop
4. Step back 3-5 tiles from drop area
5. Begin chompy detection phase
```

**Implementation**:
```java
private boolean dropThreeToads() {
    WorldPosition playerPos = getWorldPosition();
    
    // walk to drop area if not there
    if (!TOAD_DROP_AREA.contains(playerPos)) {
        WorldPosition dropCenter = TOAD_DROP_AREA.getCenter();
        getWalker().walkTo(dropCenter);
        submitTask(() -> TOAD_DROP_AREA.contains(getWorldPosition()), 5000);
    }
    
    // clear expired toads from tracking list
    activeGroundToads.removeIf(DroppedToad::isExpired);
    
    // get bloated toads from inventory
    ItemGroupResult toads = getWidgetManager().getInventory()
        .search(Set.of(1422));
    
    if (toads == null || toads.getTotalQuantity() == 0) {
        log("ERROR: No bloated toads to drop");
        return false;
    }
    
    int toadsToDropCount = toads.getTotalQuantity();
    log("Dropping " + toadsToDropCount + " bloated toads");
    
    // drop each toad and record position
    for (int i = 0; i < toadsToDropCount; i++) {
        WorldPosition currentPos = getWorldPosition();
        
        // find bloated toad in inventory
        ItemSearchResult toad = toads.getItem(1422);
        if (toad == null) break;
        
        // drop the toad
        boolean dropped = toad.interact("Drop");
        if (!dropped) {
            log("ERROR: Failed to drop toad");
            continue;
        }
        
        // record the dropped toad's position for tracking
        DroppedToad droppedToad = new DroppedToad(currentPos);
        activeGroundToads.add(droppedToad);
        log("Dropped toad at " + currentPos);
        
        // small delay and move slightly for next toad
        submitTask(() -> false, random(300, 500));
        
        // move 1 tile to spread toads out
        if (i < toadsToDropCount - 1) {
            WorldPosition nextPos = currentPos.translate(random(-1, 1), random(-1, 1));
            if (TOAD_DROP_AREA.contains(nextPos)) {
                getWalker().walkTo(nextPos);
                submitTask(() -> getWorldPosition().equals(nextPos), 1000);
            }
        }
    }
    
    // step back from drop area (3-5 tiles)
    WorldPosition retreatPos = getWorldPosition().translate(random(-4, 4), random(-4, 4));
    getWalker().walkTo(retreatPos);
    submitTask(() -> getWorldPosition().distanceTo(TOAD_DROP_AREA.getCenter()) >= 3, 2000);
    
    log("Successfully dropped " + activeGroundToads.size() + " toads. Waiting for chompies...");
    return true;
}
```

**Ground Toad Verification**:
```java
// check if toads are still on ground using highlighting
private int countActiveGroundToads() {
    int activeCount = 0;
    
    // bloated toad NPC highlight color (use Debug Tool to verify)
    SearchablePixel BLOATED_TOAD_HIGHLIGHT = new SearchablePixel(
        30,    // Hue (brownish for bloated toad)
        50,    // Saturation
        40,    // Lightness
        15, 15, 15  // Tolerances
    );
    
    for (DroppedToad toad : new ArrayList<>(activeGroundToads)) {
        // check if toad expired
        if (toad.isExpired()) {
            activeGroundToads.remove(toad);
            continue;
        }
        
        // create tile cube at known drop position
        Polygon tileCube = getSceneProjector().getTileCube(toad.position, 30);
        if (tileCube == null) continue;
        
        // check for NPC highlight at this position
        Rectangle highlightBounds = getPixelAnalyzer()
            .getHighlightBounds(tileCube, BLOATED_TOAD_HIGHLIGHT);
        
        if (highlightBounds != null) {
            // toad is still on ground (highlighted NPC present)
            activeCount++;
        } else {
            // toad is gone (exploded or eaten by chompy)
            activeGroundToads.remove(toad);
        }
    }
    
    return activeCount;
}
```

**Toad Mechanics**:
- Each toad rolls every 25 ticks (15 seconds) for 1/5 chance to spawn chompy
- Up to 4 rolls per toad before it explodes
- Toad explodes after 1 minute, dealing 1-2 damage (cannot kill player)
- ~59% chance per toad to successfully spawn a chompy
- Chompies spawn within 10 tile radius (21x21 square) from toad

### Phase 5: Detect & Kill Chompy Birds (Arrow Marker + Hybrid Detection)

**CRITICAL ADVANTAGE**: Chompies spawn with an **arrow marker** on the minimap! This is the FASTEST and most reliable detection method.

**Arrow Marker Detection (Primary Method)**:

Chompies that spawn will have a yellow arrow marker pointing to them on the minimap, similar to quest objectives. We can use `MinimapArrowResult` to detect this.

```java
private WorldPosition findChompyByArrowMarker() {
    // get arrow marker from minimap (if present)
    ArrowResult arrowResult = getWidgetManager().getMinimap().getLastArrowResult();
    
    if (arrowResult != null) {
        WorldPosition arrowPos = arrowResult.getPosition();
        
        // verify arrow is pointing at an NPC (chompy)
        List<WorldPosition> npcPositions = getWidgetManager().getMinimap()
            .getNPCPositions().asList();
        
        for (WorldPosition npcPos : npcPositions) {
            // arrow should be within 2-3 tiles of actual NPC position
            if (npcPos.distanceTo(arrowPos) <= 3) {
                log("Found chompy via arrow marker at " + npcPos);
                return npcPos;
            }
        }
    }
    
    return null;
}
```

**Chompy Detection - Multi-Method Approach**:

#### Method 1: Arrow Marker Detection (PRIMARY - FASTEST)
```java
// PRIORITY 1: Check for arrow marker
WorldPosition chompyPos = findChompyByArrowMarker();
if (chompyPos != null) {
    return attackChompy(chompyPos);
}
```

#### Method 2: Minimap Yellow Dots (Secondary)
```java
// PRIORITY 2: Check minimap for NPC positions near toad drop area
List<WorldPosition> npcPositions = getWidgetManager().getMinimap()
    .getNPCPositions().asList();

WorldPosition playerPos = getWorldPosition();

for (WorldPosition npcPos : npcPositions) {
    // filter by distance (chompies spawn within 12 tiles of drop area)
    if (!TOAD_DROP_AREA.contains(npcPos) && 
        npcPos.distanceTo(TOAD_DROP_AREA.getCenter()) > 12) continue;
    
    // create tile cube for the NPC position
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 70);
    if (tileCube == null) continue;
    
    // verify it's a chompy bird using menuHook
    boolean isChompy = getFinger().tapGetResponse(true, tileCube, menuEntries ->
        menuEntries.stream()
            .filter(entry -> "Attack".equalsIgnoreCase(entry.getAction()) &&
                            entry.getEntityName().contains("Chompy"))
            .findFirst()
            .orElse(null)
    );
    
    if (isChompy) {
        log("Found chompy via minimap at " + npcPos);
        return attackChompy(npcPos);
    }
}
```

**Attack & Kill Chompy (Using KillGoblinTask Pattern)**:

```java
private boolean attackChompy(WorldPosition chompyPos) {
    // wait for player to be idle before attacking
    WorldPosition playerPos = getWorldPosition();
    submitTask(() -> getWorldPosition().equals(playerPos), 1000);
    
    // re-verify chompy is still at position (NPCs move)
    List<WorldPosition> currentNpcs = getWidgetManager().getMinimap()
        .getNPCPositions().asList();
    
    boolean stillThere = false;
    for (WorldPosition npc : currentNpcs) {
        if (npc.equals(chompyPos)) {
            stillThere = true;
            break;
        }
    }
    
    if (!stillThere) {
        log("Chompy moved or despawned before attack");
        return false;
    }
    
    // create tile cube and attack
    Polygon tileCube = getSceneProjector().getTileCube(chompyPos, 70);
    if (tileCube == null) {
        log("ERROR: Cannot see chompy tile cube");
        return false;
    }
    
    // shrink polygon for more precise clicking
    Shape shrunkPoly = tileCube.getResized(0.7);
    
    // use menuHook to attack chompy
    boolean attacked = getFinger().tapGameScreen(shrunkPoly, menuEntries ->
        menuEntries.stream()
            .filter(entry -> "Attack".equalsIgnoreCase(entry.getAction()) &&
                            entry.getEntityName().contains("Chompy bird"))
            .findFirst()
            .orElse(null)
    );
    
    if (!attacked) {
        log("ERROR: Failed to attack chompy");
        return false;
    }
    
    log("Attacked chompy at " + chompyPos);
    
    // wait for player to reach chompy (if not adjacent)
    int distance = (int) playerPos.distanceTo(chompyPos);
    if (distance > 1) {
        submitTask(() -> 
            getWorldPosition().distanceTo(chompyPos) <= distance, 
            distance * 1000
        );
    }
    
    // initialize health overlay to track chompy HP
    HealthOverlay healthOverlay = new HealthOverlay(this);
    
    // wait for health overlay to appear
    boolean healthVisible = submitTask(() -> healthOverlay.isVisible(), 2000);
    if (!healthVisible) {
        log("WARNING: Health overlay not visible, chompy may already be dead");
        return true; // assume success
    }
    
    // wait for chompy to die (health overlay disappears or HP = 0)
    boolean killed = submitTask(() -> {
        if (!healthOverlay.isVisible()) {
            log("Chompy killed - health overlay disappeared");
            return true;
        }
        
        HealthOverlay.HealthResult health = 
            (HealthOverlay.HealthResult) healthOverlay.getValue(HealthOverlay.HEALTH);
        
        if (health == null || health.getCurrentHitpoints() == 0) {
            log("Chompy killed - HP reached 0");
            return true;
        }
        
        return false;
    }, 16000); // max 16 seconds to kill chompy
    
    if (!killed) {
        log("ERROR: Failed to kill chompy within timeout");
        return false;
    }
    
    log("Successfully killed chompy!");
    
    // increment kill counter
    killCount++;
    
    // check for kill message
    // "You scratch a notch on your bow for the chompy bird kill."
    
    return true;
}
```

**CONTINUOUS Chompy Monitoring**:

This is CRITICAL - chompies despawn quickly if not engaged!

```java
// main detection loop - runs CONSTANTLY while toads are on ground
private boolean monitorForChompies() {
    while (countActiveGroundToads() > 0) {
        // PRIORITY 1: Check arrow marker (fastest)
        WorldPosition chompyPos = findChompyByArrowMarker();
        
        if (chompyPos != null) {
            attackChompy(chompyPos);
            continue; // check for more chompies immediately
        }
        
        // PRIORITY 2: Check minimap NPC positions
        chompyPos = findChompyByMinimapScan();
        
        if (chompyPos != null) {
            attackChompy(chompyPos);
            continue;
        }
        
        // No chompies detected - check if we should get more toads
        int activeToadCount = countActiveGroundToads();
        
        if (activeToadCount < 3) {
            // less than 3 toads on ground, go get more
            log("Only " + activeToadCount + " toads active, getting more...");
            break; // exit monitoring to get more toads
        }
        
        // small delay before next scan
        submitTask(() -> false, 600);
    }
    
    // all toads gone and no more chompies
    log("All toads expired or consumed. Refilling cycle...");
    return true;
}
```

**Multi-Chompy Handling**:

If multiple chompies spawn simultaneously (from multiple toads), prioritize closest one but tag all:

```java
// if multiple chompies detected, tag each one first
List<WorldPosition> chompyPositions = findAllChompies();

if (chompyPositions.size() > 1) {
    log("Multiple chompies detected: " + chompyPositions.size());
    
    // hit each chompy once to prevent despawn
    for (WorldPosition pos : chompyPositions) {
        Polygon tileCube = getSceneProjector().getTileCube(pos, 70);
        if (tileCube != null) {
            getFinger().tap(tileCube);
            submitTask(() -> false, 300); // quick hit
        }
    }
}

// now focus on killing closest chompy
chompyPositions.sort(Comparator.comparingInt(
    pos -> pos.distanceTo(getWorldPosition())
));

for (WorldPosition pos : chompyPositions) {
    attackChompy(pos);
}
```

**Attack Priority**:
1. Check arrow marker (instant detection)
2. Scan minimap NPC positions
3. Verify with menuHook
4. Attack closest chompy first
5. If multiple chompies, tag all then focus kills

### Phase 6: Kill Confirmation & Plucking

**Kill Detection**:
```
1. Wait for attack animation to complete
2. Verify chompy death:
   - NPC disappears from minimap
   - Kill count message: "You scratch a notch on your bow for the chompy bird kill"
   - Chompy corpse appears on ground (optional to pluck)
```

**Plucking (OPTIONAL)**:
- **Purpose**: Required for pet chance (1/500 after Elite Diary)
- **Items from plucking**: Raw chompy, feathers, bones
- **Detection**: Ground item "Chompy bird" (corpse)
- **Time limit**: 2 minutes before corpse despawns

**If tracking pet/loot**:
```java
// find chompy corpse
List<RSGroundItem> corpses = getSceneManager().getGroundItems().stream()
    .filter(item -> item.getName().contains("Chompy bird"))
    .collect(Collectors.toList());

if (!corpses.isEmpty()) {
    RSGroundItem corpse = corpses.get(0);
    Polygon corpsePoly = corpse.getConvexHull();
    getFinger().tap(corpsePoly, "Pluck");
}
```

**Script Decision**: This PRD recommends **NOT plucking** for maximum efficiency unless specifically enabled by user.

### Phase 7: Loop & Refill Cycle (Smart Cycling)

**Cycle Decision Logic**:

```java
// main script loop
@Override
public int poll() {
    switch (currentState) {
        case INITIALIZING:
            return handleInitialization();
            
        case FILLING_BELLOWS:
            return handleFillingBellows();
            
        case INFLATING_TOADS:
            return handleInflatingToads();
            
        case DROPPING_TOADS:
            return handleDroppingToads();
            
        case MONITORING_CHOMPIES:
            return handleChompyMonitoring();
            
        case REFILLING_CYCLE:
            return handleRefillCycle();
    }
    
    return 600;
}
```

**Chompy Monitoring with Smart Toad Refill**:

```java
private int handleChompyMonitoring() {
    // check for chompies via arrow marker (PRIORITY 1)
    WorldPosition chompyPos = findChompyByArrowMarker();
    
    if (chompyPos != null) {
        attackChompy(chompyPos);
        return 0; // immediately check for more chompies
    }
    
    // check for chompies via minimap scan (PRIORITY 2)
    chompyPos = findChompyByMinimapScan();
    
    if (chompyPos != null) {
        attackChompy(chompyPos);
        return 0; // immediately check for more chompies
    }
    
    // no chompies detected - check ground toad status
    int activeToadCount = countActiveGroundToads();
    
    if (activeToadCount == 0) {
        // all toads gone, need to refill completely
        log("No toads on ground. Starting refill cycle...");
        currentState = ChompyState.REFILLING_CYCLE;
        return 0;
    }
    
    // we have some toads but < 3, can opportunistically get more
    if (activeToadCount < 3) {
        // check if we have bloated toads in inventory already
        ItemGroupResult invToads = getWidgetManager().getInventory()
            .search(Set.of(1422));
        int invToadCount = (invToads != null) ? invToads.getTotalQuantity() : 0;
        
        if (invToadCount > 0) {
            // we have toads ready to drop!
            log("Have " + invToadCount + " toads ready. Dropping...");
            currentState = ChompyState.DROPPING_TOADS;
            return 0;
        }
        
        // check if we have charged bellows to make more toads
        ItemGroupResult bellows = getWidgetManager().getInventory()
            .search(Set.of(1420)); // checks for any bellows (charged or not)
        
        if (bellows != null && bellows.getTotalQuantity() > 0) {
            // assume bellows have charges, go inflate more toads
            log("Opportunistically inflating more toads while waiting...");
            currentState = ChompyState.INFLATING_TOADS;
            return 0;
        }
        
        // no bellows or charges left, need full refill
        log("Need to refill bellows. Starting cycle...");
        currentState = ChompyState.REFILLING_CYCLE;
        return 0;
    }
    
    // we have 3 toads, keep monitoring
    return 600; // check again in 600ms
}
```

**Refill Cycle Logic**:

```java
private int handleRefillCycle() {
    // Step 1: Go fill bellows with swamp gas
    if (!fillAllBellows()) {
        log("ERROR: Failed to fill bellows");
        return 5000;
    }
    
    // Step 2: Inflate 3 toads
    if (!getThreeToads()) {
        log("ERROR: Failed to get 3 toads");
        return 5000;
    }
    
    // Step 3: Drop toads as bait
    if (!dropThreeToads()) {
        log("ERROR: Failed to drop toads");
        return 5000;
    }
    
    // Step 4: Return to monitoring
    log("Refill cycle complete. Resuming chompy monitoring...");
    currentState = ChompyState.MONITORING_CHOMPIES;
    return 0;
}
```

**Efficiency Optimization Strategy**:

1. **Continuous Monitoring**: NEVER stop checking for chompies while toads are active
2. **Opportunistic Refilling**: If < 3 toads on ground, inflate more WHILE monitoring
3. **Pre-emptive Toads**: Keep some toads in inventory ready to drop quickly
4. **Minimize Downtime**: Drop new toads BEFORE old ones fully expire

**Timing Breakdown**:
```
Optimal Cycle (with 5 bellows):
- Fill bellows: ~5-10 seconds
- Inflate 3 toads: ~5-10 seconds  
- Drop toads: ~2-3 seconds
- Monitor & kill chompies: ~10-40 seconds per chompy
- TOTAL CYCLE: ~25-60 seconds

With Elite Diary (100% spawn rate):
- 3 toads → 3-6 chompies guaranteed
- Average 4-5 chompies per cycle
- 200-350 kills per hour achievable
```

**Cycle Overlap Strategy**:

```java
// ADVANCED: Pre-fill next batch while current toads active
private void overlappingCycleStrategy() {
    // monitor chompies in main thread
    // WHILE chompy detection is running:
    
    int activeToadCount = countActiveGroundToads();
    
    if (activeToadCount >= 2 && !isFighting()) {
        // we have 2+ toads active and not in combat
        // can safely go get more toads ready
        
        ItemGroupResult invToads = getWidgetManager().getInventory()
            .search(Set.of(1422));
        int invToadCount = (invToads != null) ? invToads.getTotalQuantity() : 0;
        
        if (invToadCount == 0) {
            // no toads in inventory, go make some
            log("[OVERLAP] Pre-filling toads while monitoring...");
            
            // quickly inflate 1-3 toads
            for (int i = 0; i < 3; i++) {
                if (inflateToad()) {
                    log("[OVERLAP] Got toad " + (i+1) + "/3");
                }
            }
            
            // return to monitoring position
            WorldPosition monitorPos = TOAD_DROP_AREA.getCenter()
                .translate(random(-4, 4), random(-4, 4));
            getWalker().walkTo(monitorPos);
        }
    }
}
```

**Error Recovery**:
- **No chompies after 2 minutes** → Drop new toads, old ones expired
- **All bellows depleted** → Walk to swamp bubbles, refill all
- **Inventory full** → Drop non-essential items or stop
- **Stuck in combat** → Wait for combat to end, resume monitoring

---

## State Machine

### States

```java
enum ChompyState {
    INITIALIZING,           // Check equipment, inventory, location, region ID
    FILLING_BELLOWS,        // Walk to swamp bubbles (known positions), fill bellows
    INFLATING_TOADS,        // Use bellows on swamp toads (hybrid detection)
    DROPPING_TOADS,         // Drop bloated toads in designated area, track positions
    MONITORING_CHOMPIES,    // CONTINUOUSLY check for arrow marker + minimap NPCs, attack on sight
    REFILLING_CYCLE         // Return to fill bellows when needed
}
```

### State Transitions

```
INITIALIZING → FILLING_BELLOWS (if all checks pass)
INITIALIZING → ERROR (if equipment/location check fails)

FILLING_BELLOWS → INFLATING_TOADS (when bellows filled)
FILLING_BELLOWS → ERROR (if cannot find swamp bubbles)

INFLATING_TOADS → DROPPING_TOADS (when 3 toads created)
INFLATING_TOADS → ERROR (if cannot find swamp toads)

DROPPING_TOADS → MONITORING_CHOMPIES (after dropping toads)
DROPPING_TOADS → ERROR (if drop fails or not in drop area)

MONITORING_CHOMPIES → MONITORING_CHOMPIES (continuous loop - check arrow marker/minimap)
MONITORING_CHOMPIES → INFLATING_TOADS (if < 3 ground toads and have charged bellows)
MONITORING_CHOMPIES → DROPPING_TOADS (if have toads in inventory to drop)
MONITORING_CHOMPIES → REFILLING_CYCLE (if no ground toads and no bellows/charges)

REFILLING_CYCLE → FILLING_BELLOWS (start new cycle)
```

### Core Loop Flow

```
[START]
    ↓
[INITIALIZING] ← Equipment + Region + Bellows check
    ↓
[FILLING_BELLOWS] ← Fill at static bubble locations
    ↓
[INFLATING_TOADS] ← Get 3 toads (minimap + highlight hybrid)
    ↓
[DROPPING_TOADS] ← Drop in designated area, record positions
    ↓
[MONITORING_CHOMPIES] ← CONTINUOUS LOOP
    │
    ├─→ Arrow marker detected? → ATTACK CHOMPY → [MONITORING_CHOMPIES]
    │
    ├─→ Minimap NPC detected? → ATTACK CHOMPY → [MONITORING_CHOMPIES]
    │
    ├─→ < 3 ground toads? 
    │   ├─→ Have inv toads? → [DROPPING_TOADS]
    │   ├─→ Have charged bellows? → [INFLATING_TOADS]
    │   └─→ Need refill? → [REFILLING_CYCLE]
    │
    └─→ 0 ground toads? → [REFILLING_CYCLE]
```

### Error States

```
ERROR_NO_BOW → Stop script, display message
ERROR_NO_ARROWS → Stop script, display message  
ERROR_NO_BELLOWS → Stop script, display message
ERROR_WRONG_REGION → Walk to region 9519 or stop
ERROR_INVENTORY_FULL → Drop items or stop
ERROR_CANNOT_FIND_BUBBLES → Walk to known bubble position
ERROR_CANNOT_FIND_TOADS → Move to different area, retry
ERROR_ATTACK_FAILED → Log warning, continue monitoring
```

### State Decision Tree

```
Is player in region 9519?
├─ NO → Walk to Castle Wars / ERROR
└─ YES → Continue

Has ogre bow equipped?
├─ NO → ERROR_NO_BOW
└─ YES → Continue

Has arrows equipped?
├─ NO → ERROR_NO_ARROWS
└─ YES → Continue

Has bellows in inventory?
├─ NO → ERROR_NO_BELLOWS
└─ YES → START CYCLE

[IN MONITORING STATE]
Arrow marker visible?
├─ YES → Attack chompy at marker position
└─ NO → Continue

NPC on minimap in drop area?
├─ YES → Verify it's a chompy → Attack
└─ NO → Continue

How many toads on ground?
├─ 0 → REFILLING_CYCLE
├─ 1-2 → Get more toads (if have charges) OR REFILLING_CYCLE
└─ 3 → Keep monitoring

Are we in combat?
├─ YES → Wait for kill, then continue monitoring
└─ NO → Keep scanning for chompies
```

---

## Key Implementation Patterns (from KillGoblinTask.java)

### Pattern 1: Wait for Idle Before Attacking

Always ensure player is stationary before initiating attack:

```java
// wait for player to stop moving
if (!waitHelper.waitForNoChange(
    "Position",
    script::getWorldPosition,
    1_000,  // no change for 1 second
    3_000   // max wait 3 seconds
)) {
    log("Failed to stop moving");
    return false;
}
```

### Pattern 2: Re-verify NPC Position Before Attack

NPCs move! Always double-check the NPC is still at the position:

```java
// before attacking, verify NPC still at position
List<WorldPosition> currentNpcs = getWidgetManager().getMinimap()
    .getNPCPositions().asList();

boolean stillThere = false;
for (WorldPosition npc : currentNpcs) {
    if (npc.equals(targetPos)) {
        stillThere = true;
        break;
    }
}

if (!stillThere) {
    log("NPC moved or despawned");
    return false;
}
```

### Pattern 3: Shrink Polygons for Precise Clicking

Reduce tile cubes to avoid misclicks:

```java
// create tile cube and shrink it
Polygon tilePoly = getSceneProjector().getTileCube(npcPos, 50);
Shape shrunkPoly = tilePoly.getResized(0.7); // 70% of original size

// use shrunk polygon for more precise clicking
getFinger().tapGameScreen(shrunkPoly, menuEntries -> ...);
```

### Pattern 4: Use MenuHook for Reliable Interaction

Filter menu entries to ensure correct action:

```java
boolean attacked = getFinger().tapGameScreen(tilePoly, menuEntries ->
    menuEntries.stream()
        .filter(entry -> 
            "Attack".equalsIgnoreCase(entry.getAction()) &&
            entry.getEntityName().contains("Chompy bird"))
        .findFirst()
        .orElse(null)
);
```

### Pattern 5: Health Overlay for Combat Tracking

Track NPC health to confirm kill:

```java
HealthOverlay healthOverlay = new HealthOverlay(script);

// wait for overlay to appear
if (!script.submitTask(() -> healthOverlay.isVisible(), 2000)) {
    log("Health overlay not visible");
    return false;
}

// monitor until dead
boolean killed = script.submitTask(() -> {
    if (!healthOverlay.isVisible()) {
        return true; // overlay disappeared = NPC dead
    }
    
    HealthOverlay.HealthResult health = 
        (HealthOverlay.HealthResult) healthOverlay.getValue(HealthOverlay.HEALTH);
    
    return health == null || health.getCurrentHitpoints() == 0;
}, 16000); // max 16 seconds
```

### Pattern 6: Check for Existing Combat Before New Attack

Don't attack if already in combat:

```java
private boolean isFighting(HealthOverlay healthOverlay) {
    if (healthOverlay == null || !healthOverlay.isVisible()) {
        return false;
    }
    
    HealthOverlay.HealthResult health = 
        (HealthOverlay.HealthResult) healthOverlay.getValue(HealthOverlay.HEALTH);
    
    return health != null && health.getCurrentHitpoints() > 0;
}

// in main loop
if (!isFighting(healthOverlay)) {
    attackChompy(chompyPos);
}
```

### Pattern 7: Distance-Based Timeout Calculation

Calculate wait time based on distance to target:

```java
int distance = (int) playerPos.distanceTo(targetPos);

// wait for player to reach target
if (!submitTask(() -> 
    getWorldPosition().distanceTo(targetPos) <= 1,
    distance * 1000  // 1 second per tile distance
)) {
    log("Failed to reach target");
    return false;
}
```

### Pattern 8: Avoid Occupied NPCs (Optional for Chompy)

For multi-player environments, check if other players are already attacking:

```java
List<WorldPosition> players = getWidgetManager().getMinimap()
    .getPlayerPositions().asList();

boolean occupied = false;
for (WorldPosition player : players) {
    if (CollisionManager.isCardinallyAdjacent(npcPos, player)) {
        // another player is near this NPC
        occupied = true;
        break;
    }
}

if (occupied) {
    // skip this NPC, find another
    continue;
}
```

**Application to Chompy Script**:
- Use health overlay to track chompy kills
- Wait for idle before attacking each chompy
- Re-verify chompy position via minimap before each attack
- Shrink tile cubes for precise clicking
- Use menuHook to filter "Attack Chompy bird" action
- Check if already in combat before attacking new chompy

---

## Arrow Marker Detection (MinimapArrowResult)

### Overview

**CRITICAL ADVANTAGE**: When chompies spawn, they appear with a **yellow arrow marker** on the minimap, similar to quest objectives. This is the **fastest and most reliable** detection method available.

**Why Arrow Marker Detection is Superior**:
- **Instant identification**: No need to scan all NPC positions
- **Guaranteed accuracy**: Arrow only appears for chompies, not other NPCs
- **Faster reaction time**: Can attack immediately upon spawn
- **No false positives**: Unlike minimap yellow dots (which could be any NPC)

### Arrow Marker API

**MinimapArrowResult** provides:
- `getPosition()`: Returns WorldPosition where arrow is pointing
- `getFoundTime()`: Returns Timer indicating when arrow was detected

### Implementation

```java
/**
 * Checks for arrow marker on minimap indicating chompy spawn.
 * This should be called CONSTANTLY in the monitoring loop.
 * 
 * @return WorldPosition of chompy if arrow found, null otherwise
 */
private WorldPosition findChompyByArrowMarker() {
    // get the last arrow result from minimap
    ArrowResult arrowResult = getWidgetManager().getMinimap().getLastArrowResult();
    
    if (arrowResult == null) {
        // no arrow marker present
        return null;
    }
    
    WorldPosition arrowPos = arrowResult.getPosition();
    
    // verify arrow is pointing at an actual NPC (chompy)
    // arrow position may be slightly offset from exact NPC position
    List<WorldPosition> npcPositions = getWidgetManager().getMinimap()
        .getNPCPositions().asList();
    
    for (WorldPosition npcPos : npcPositions) {
        // arrow should be within 2-3 tiles of actual NPC
        if (npcPos.distanceTo(arrowPos) <= 3) {
            log("✓ ARROW MARKER: Found chompy at " + npcPos);
            return npcPos;
        }
    }
    
    // arrow exists but no NPC nearby (rare edge case)
    log("WARNING: Arrow marker at " + arrowPos + " but no NPC nearby");
    return null;
}
```

### Monitoring Loop with Arrow Priority

```java
@Override
public int poll() {
    if (currentState != ChompyState.MONITORING_CHOMPIES) {
        return handleOtherStates();
    }
    
    // PRIORITY 1: Check arrow marker FIRST (fastest detection)
    WorldPosition chompyPos = findChompyByArrowMarker();
    
    if (chompyPos != null) {
        log("ARROW DETECTED! Attacking chompy immediately...");
        attackChompy(chompyPos);
        return 0; // check immediately for more chompies
    }
    
    // PRIORITY 2: Fallback to minimap scan (in case arrow isn't showing)
    chompyPos = scanMinimapForChompy();
    
    if (chompyPos != null) {
        attackChompy(chompyPos);
        return 0;
    }
    
    // No chompies detected, check toad status
    return handleToadManagement();
}
```

### Edge Cases

**Arrow Marker Lag**:
- Arrow may appear 1-2 ticks after chompy spawns
- Still faster than manual minimap scanning

**Multiple Chompies**:
- Arrow points to most recent spawn
- Use minimap scan to detect additional chompies
- Attack arrow-marked chompy first (highest priority)

**Arrow Disappears**:
- Arrow may disappear after ~10-15 seconds
- Fallback to minimap scanning if arrow no longer present
- Always cache last-known chompy position

**False Arrow**:
- Very rare, but arrow might point to invalid location
- Always verify NPC exists at or near arrow position
- Use 3-tile distance threshold for verification

### Benefits for Chompy Hunting

1. **Faster Kills**: Immediate detection = less despawn risk
2. **Higher Kill Rate**: Attack within 1-2 seconds of spawn
3. **Less CPU Usage**: No need for continuous pixel scanning
4. **Simpler Logic**: Arrow = chompy, very straightforward
5. **Reliable in Lag**: Arrow persists even with network latency

### Testing Arrow Detection

```java
// debug helper to test arrow detection
private void debugArrowMarker() {
    ArrowResult arrow = getWidgetManager().getMinimap().getLastArrowResult();
    
    if (arrow != null) {
        WorldPosition pos = arrow.getPosition();
        Timer foundTime = arrow.getFoundTime();
        
        log("DEBUG: Arrow at " + pos + ", found " + 
            foundTime.getElapsedTime() + "ms ago");
        
        // draw arrow position on screen for visual confirmation
        Polygon tileCube = getSceneProjector().getTileCube(pos, 100);
        if (tileCube != null) {
            getScreen().addCanvasDrawable("arrow_debug", () -> {
                Graphics2D g = getScreen().getGraphics();
                g.setColor(Color.CYAN);
                g.draw(tileCube);
            });
        }
    } else {
        log("DEBUG: No arrow marker present");
    }
}
```

**Recommendation**: Always prioritize arrow marker detection over all other methods for maximum efficiency.

---

## Detection Implementation Details

### Swamp Bubble Detection
```java
// METHOD 1: Object Manager (Preferred)
RSObject swampBubbles = getObjectManager()
    .findObject("Swamp bubbles", getWorldPosition(), 10);

if (swampBubbles != null) {
    Polygon bubblePoly = swampBubbles.getConvexHull();
    getFinger().tap(bubblePoly);
}

// METHOD 2: Ground Item (Alternative)
// Some implementations may detect bubbles as ground items
List<RSGroundItem> bubbles = getSceneManager().getGroundItems().stream()
    .filter(item -> item.getName().contains("bubbles"))
    .collect(Collectors.toList());
```

### Swamp Toad Detection (NPCs)
```java
// swamp toads are NPCs, use minimap positions
List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

for (WorldPosition npcPos : npcPositions) {
    // only check nearby NPCs
    if (npcPos.distanceTo(getWorldPosition()) > 5) continue;
    
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 40);
    if (tileCube == null) continue;
    
    MenuEntry response = getFinger().tapGetResponse(true, tileCube);
    if (response != null && response.getEntityName().contains("Swamp toad")) {
        // found a swamp toad, use bellows on it
        // (implementation would use inventory interaction + clicking toad)
        return npcPos;
    }
}
```

### Chompy Bird Detection (NPCs)
```java
// Primary Method: Minimap + Verification
List<WorldPosition> npcPositions = getWidgetManager().getMinimap().getNPCPositions();

for (WorldPosition npcPos : npcPositions) {
    int distance = npcPos.distanceTo(getWorldPosition());
    if (distance > 12) continue; // chompies spawn within 10 tiles of toads
    
    Polygon tileCube = getSceneProjector().getTileCube(npcPos, 70);
    if (tileCube == null) continue;
    
    // verify NPC identity
    MenuEntry response = getFinger().tapGetResponse(true, tileCube);
    if (response != null && response.getEntityName().equalsIgnoreCase("Chompy bird")) {
        // confirmed chompy bird
        getFinger().tap(tileCube, "Attack");
        
        // wait for attack animation
        submitTask(() -> {
            return !getPixelAnalyzer().isPlayerAnimating(0.10);
        }, 3000);
        
        return true;
    }
}
```

---

## Performance Metrics

### Expected Rates (with Optimization)

**Without Western Diaries**:
- 80-120 kills/hour (base spawn rates)

**With Easy Diary (25% double spawn)**:
- 100-140 kills/hour

**With Medium Diary (50% double spawn)**:
- 140-180 kills/hour

**With Hard Diary (50% double spawn)**:
- 140-180 kills/hour

**With Elite Diary (100% double spawn)**:
- 200-350 kills/hour

**Factors Affecting Rates**:
- Number of bellows (2-24 recommended)
- Arrow type (brutal arrows = one-shots)
- Ranged level (higher = faster kills)
- Script efficiency (detection speed, movement)

### Time to Goals

| Goal | Kills Required | Time (Elite Diary) | Time (No Diary) |
|------|----------------|-------------------|-----------------|
| Easy Diary | 30 | ~10 minutes | ~20 minutes |
| Medium Diary | 125 | ~40 minutes | ~75 minutes |
| Hard Diary | 300 | ~90 minutes | ~3 hours |
| Elite Diary | 1000 | ~4-5 hours | ~10-12 hours |

---

## Optional Features

### 1. Plucking for Pet/Loot
- **Setting**: Enable/disable plucking
- **Items**: Raw chompy (2,151 gp), feathers, bones
- **Pet**: 1/500 chance per pluck (Elite Diary required)

### 2. Kill Count Tracking
- **Method 1**: Right-click ogre bow → "Check kills"
- **Method 2**: Track kills via script counter
- **Method 3**: Parse game message "You scratch a notch..."

### 3. Western Diary Notifications
- **Alert at 30 kills** → "Easy Diary complete! Claim reward for spawn boost"
- **Alert at 125 kills** → "Medium Diary complete!"
- **Alert at 300 kills** → "Hard Diary complete! Elite Void unlocked!"

### 4. Arrow Management
- **Track arrows remaining** (check equipped ammunition)
- **Warning at < 50 arrows** → "Low arrow count"
- **Stop script at 0 arrows** → "Out of arrows"

### 5. Bellow Management
- **Count bellows used** (3 toads per bellow fill)
- **Suggest ideal bellow count** based on session length
- **Warn when < 2 bellows** → "Efficiency will drop"

---

## Safety & Anti-Pattern Features

### 1. Avoid Rantz's Area
- **Location**: Small area directly south of Rantz
- **Message**: "Rantz doesn't like it when you chompy hunt on his turf"
- **Script**: Stay south of Castle Wars, avoid Feldip Hills near Rantz

### 2. Health Monitoring
- **Toad explosions**: Deal 1-2 damage (cannot kill)
- **Recommendation**: Eat food if health < 10 (paranoia)

### 3. Aggressive NPC Check
- **Castle Wars area**: No aggressive NPCs
- **Feldip Hills**: Wolves (level 64) may attack
- **Script**: Detect combat, eat food, or run to safety

### 4. Stuck Detection
- **No chompies after 2 minutes** → Return to refill cycle
- **No swamp bubbles found** → Walk to known bubble location
- **No toads inflated in 30 seconds** → Reset to finding toads

---

## Configuration Options

### User Settings

```
[Equipment]
- Preferred bow: Ogre bow / Comp ogre bow (auto-detect)
- Pluck corpses: Yes / No
- Arrow type preference: Rune > Adamant > Mithril > Ogre

[Inventory]
- Bellow count: 2-24 (recommend 5-10 for balance)
- Keep inventory slots free: 3 (for toads)

[Performance]
- Detection method: Minimap + Pixel / Minimap Only
- Attack delay: 600ms (randomized)
- Toad placement spacing: 3 tiles (default)

[Goals]
- Target kill count: 30 / 125 / 300 / 1000
- Stop at goal: Yes / No
- Notify at milestones: Yes / No

[Safety]
- Min health before eating: 10 HP
- Detect aggressive NPCs: Yes (if Feldip Hills)
- Location restriction: Castle Wars only
```

---

## Testing Checklist

### Pre-Script Validation
- [ ] Big Chompy Bird Hunting quest completed
- [ ] Ogre bow OR comp ogre bow equipped
- [ ] Arrows in ammo slot (check arrow type compatibility)
- [ ] 2+ ogre bellows in inventory
- [ ] 3+ free inventory slots
- [ ] Player at Castle Wars hunting area

### Script Flow Testing
- [ ] Detects swamp bubbles correctly
- [ ] Fills all bellows with gas
- [ ] Detects swamp toads (NPCs) via minimap
- [ ] Creates 3 bloated toads successfully
- [ ] Drops bloated toads in good spawn area
- [ ] Detects chompy birds via minimap yellow dots
- [ ] Verifies chompy identity with tapGetResponse
- [ ] Attacks and kills chompies efficiently
- [ ] Tracks kill count accurately
- [ ] Refills bellows when depleted
- [ ] Loops continuously without errors

### Edge Case Testing
- [ ] Out of arrows → Script stops gracefully
- [ ] No bellows → Error message
- [ ] Inventory full → Drops items or warns
- [ ] No chompies spawn after 2 minutes → Refills cycle
- [ ] Toads explode early → Script adapts

---

## Notes for LLM Implementation

### Critical Reminders
1. **NPCs (chompies and toads) do NOT appear in ObjectManager** → Use `getMinimap().getNPCPositions()`
2. **Ogre bellows with different gas levels (0-3) are visually identical** → Count by sprite, not by ID
3. **Chompies spawn in 21x21 area around toads** → Use distance filtering on minimap positions
4. **Bloated toads have 59% spawn chance** → Script should handle "no spawn" scenarios
5. **Maximum 3 bloated toads in inventory** → Drop immediately after creation

### Item ID Reference (Quick)
```
EQUIPMENT:
- Ogre bow: 1427
- Comp ogre bow: 2302

AMMUNITION:
- Ogre arrow: 2866
- Mithril brutal: 4793
- Adamant brutal: 4798
- Rune brutal: 4803

TOOLS:
- Ogre bellows (any charge): 1420 (use sprite detection for variants)
- Bloated toad: 1422
```

### Example Script Patterns to Reference
- **NPC Detection**: See `docs/highlight-npc-detection.md` and `docs/critical-concepts.md` (NPCs + minimap)
- **Ground Items**: See `docs/advanced-techniques.md` (ground item detection)
- **Equipment Validation**: See `docs/Loadout-System.md` (equipment checking)
- **Pixel Clusters**: See `TidalsCannonballThiever/README.md` (guard detection example)

---

## Version History

**v1.0** - Initial PRD
- Complete flow documentation
- Item IDs verified from OSRS Wiki
- Detection methods based on OSMB best practices
- Optimized for Elite Void grinding (300 kills)

---

**END OF PRD**
