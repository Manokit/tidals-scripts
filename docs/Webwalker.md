# Dax WebWalker Integration for OSMB

> **Status:** Research Complete | **Feasibility:** Achievable with moderate effort 

This document captures research findings for integrating [dax.cloud](https://api.dax.cloud) web walking into OSMB via TidalsUtilities.

---

## Executive Summary

The Dax WebWalker is a server-client pathfinding system that can navigate anywhere in the OSRS world, including automatic teleportation selection. The pathfinding computation happens server-side at `api.dax.cloud`, and the client is responsible for:

1. Sending player state (position, skills, equipment, inventory)
2. Receiving waypoint paths
3. Executing movement (including teleports when optimal)

**Key insight:** The hard part (pathfinding across 10M+ nodes) is handled by the server. Our job is building the OSMB adapter layer.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        OSMB Script                               │
│                                                                  │
│   DaxWebWalker.walkTo(new WorldPosition(2440, 3089, 0));        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     DaxWebWalker (TidalsUtilities)              │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ PlayerDetails│  │  API Client  │  │  TeleportExecutor    │  │
│  │   Builder    │  │              │  │                      │  │
│  │              │  │  POST to     │  │  - Ring of Dueling   │  │
│  │  - Skills    │  │  dax.cloud   │  │  - Games Necklace    │  │
│  │  - Equipment │  │              │  │  - Glory             │  │
│  │  - Inventory │  │  Returns:    │  │  - Spells            │  │
│  │  - Varbits?  │  │  List<Point> │  │  - 30+ more...       │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    PathExecutor                           │   │
│  │                                                           │   │
│  │  1. Check if path[0] is teleport destination              │   │
│  │  2. Execute teleport if needed                            │   │
│  │  3. Walk remaining path with OSMB walker                  │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      OSMB Walker API                             │
│                                                                  │
│   script.getWalker().walkPath(waypoints, config);               │
└─────────────────────────────────────────────────────────────────┘
```

---

## Dax API Specification

### Endpoint

```
POST https://api.dax.cloud/walker/generatePaths
POST https://api.dax.cloud/walker/generateBankPaths
```

### Authentication

Free-tier key

Api Key: sub_DPjXXzL5DeSiPf
Secret Key: PUBLIC-KEY

Headers:
```
key: <your-api-key>
secret: <your-secret>  (use "PUBLIC-KEY" for free tier)
Content-Type: application/json
```

### Request Format (BulkPathRequest)

```json
{
  "player": {
    "attack": 75,
    "defence": 70,
    "strength": 80,
    "hitpoints": 75,
    "ranged": 70,
    "prayer": 55,
    "magic": 80,
    "cooking": 60,
    "woodcutting": 70,
    "fletching": 65,
    "fishing": 60,
    "firemaking": 55,
    "crafting": 70,
    "smithing": 60,
    "mining": 65,
    "herblore": 50,
    "agility": 60,
    "thieving": 55,
    "slayer": 70,
    "farming": 45,
    "runecrafting": 50,
    "hunter": 55,
    "construction": 60,
    "equipment": [
      {"id": 11978, "amount": 1},
      {"id": 2552, "amount": 1}
    ],
    "inventory": [
      {"id": 995, "amount": 50000},
      {"id": 385, "amount": 10}
    ],
    "settings": [
      {"id": 176, "value": 1},
      {"id": 32, "value": 100}
    ],
    "varbit": [
      {"id": 5087, "value": 1},
      {"id": 4895, "value": 50}
    ],
    "member": true
  },
  "requests": [
    {
      "start": {"x": 3200, "y": 3200, "z": 0},
      "end": {"x": 2440, "y": 3089, "z": 0}
    }
  ]
}
```

### Response Format (PathResult)

```json
{
  "pathStatus": "SUCCESS",
  "path": [
    {"x": 3200, "y": 3200, "z": 0},
    {"x": 3150, "y": 3180, "z": 0},
    {"x": 2897, "y": 3551, "z": 0},
    {"x": 2500, "y": 3200, "z": 0},
    {"x": 2440, "y": 3089, "z": 0}
  ],
  "cost": 245
}
```

### Path Status Values

- `SUCCESS` - Path found
- `BLOCKED` - No valid path exists
- `NO_WEB_PATH` - Area not mapped
- `RATE_LIMIT` - Too many requests (429)
- `ERROR` - Server error

### How Teleports Work in the Response

The server returns a path where:
1. If you're already at the first waypoint → pure walking path
2. If you're NOT at the first waypoint → first waypoint is a teleport destination

**Example:** Standing at GE (3165, 3485), want to go to Burthorpe (2897, 3551)

Response path might start at (2897, 3551) - the Games Necklace teleport destination. The client must:
1. Detect that current position ≠ path[0]
2. Look up which teleport goes to (2897, 3551)
3. Execute that teleport
4. Then walk the remaining path

---

## OSMB API Mapping

### What OSMB Provides

| Requirement | OSMB API | Notes |
|-------------|----------|-------|
| Current position | `script.getWorldPosition()` | Returns WorldPosition with x, y, plane |
| Skills | `script.getXPTrackers().get(SkillType.X).getLevel()` | All 23 skills available |
| Equipment | `script.getWidgetManager().getEquipment().search(Set.of(...))` | Returns ItemGroupResult |
| Inventory | `script.getWidgetManager().getInventory().search(Set.of(...))` | Returns ItemGroupResult |
| Local walking | `script.getWalker().walkPath(List<WorldPosition>, WalkConfig)` | A* within loaded region |
| Teleport execution | `RetryUtils.equipmentInteract(script, itemId, action, desc)` | From TidalsUtilities |
| HTTP requests | Java `HttpClient` or OkHttp | Standard library |
| JSON parsing | Gson (already in TidalsUtilities dependencies) | Need to verify |

### What OSMB Does NOT Provide

| Requirement | Status | Impact |
|-------------|--------|--------|
| Varbits/Settings | **Confirmed NOT available** | Server won't know quest completion states |
| Client settings | **Not available** | Minor - used for detecting members world |

**Varbit Impact:** Without varbits, the server can't know:
- Quest completions (fairy rings, spirit trees, shortcuts)
- Achievement diary completions (elite shortcuts)
- Specific unlocks (ectophial, royal seed pod)

**MVP Decision:** Quest-dependent teleports and shortcuts are **out of scope for MVP**. The server will fall back to paths that don't require quests. Common teleports (jewelry, spells) work without varbits.

**Future Work:** See [Future Work: QuestChecker Utility](#future-work-questchecker-utility) for planned solution.

---

## Teleport Definitions

The client must maintain a mapping of teleport destinations to execution logic.

### Teleport Data Structure

```java
public class TeleportDefinition {
    private final int moveCost;           // typically 35 game ticks
    private final WorldPosition destination;
    private final Supplier<Boolean> canUse;    // requirement check
    private final Supplier<Boolean> trigger;    // execution logic

    // constructor, getters...
}
```

### Common Teleports to Implement

#### Jewelry Teleports (Priority 1 - Most Common)

| Teleport | Destination | Item IDs | Action |
|----------|-------------|----------|--------|
| Ring of Dueling → Duel Arena | (3316, 3233, 0) | 2552-2566 | "Duel Arena" |
| Ring of Dueling → Castle Wars | (2440, 3089, 0) | 2552-2566 | "Castle Wars" |
| Ring of Dueling → Ferox Enclave | (3150, 3635, 0) | 2552-2566 | "Ferox Enclave" |
| Games Necklace → Burthorpe | (2897, 3551, 0) | 3853-3867 | "Burthorpe" |
| Games Necklace → Barbarian Outpost | (2520, 3571, 0) | 3853-3867 | "Barbarian Outpost" |
| Games Necklace → Corporeal Beast | (2965, 4382, 2) | 3853-3867 | "Corporeal Beast" |
| Games Necklace → Wintertodt | (1623, 3937, 0) | 3853-3867 | "Wintertodt Camp" |
| Amulet of Glory → Edgeville | (3087, 3496, 0) | 1704-1712, 11976-11978 | "Edgeville" |
| Amulet of Glory → Karamja | (2918, 3176, 0) | 1704-1712, 11976-11978 | "Karamja" |
| Amulet of Glory → Draynor Village | (3105, 3251, 0) | 1704-1712, 11976-11978 | "Draynor Village" |
| Amulet of Glory → Al Kharid | (3293, 3163, 0) | 1704-1712, 11976-11978 | "Al Kharid" |
| Combat Bracelet → Warriors Guild | (2882, 3548, 0) | 11118-11126 | "Warriors' Guild" |
| Combat Bracelet → Champions Guild | (3191, 3366, 0) | 11118-11126 | "Champions' Guild" |
| Combat Bracelet → Monastery | (3053, 3486, 0) | 11118-11126 | "Monastery" |
| Combat Bracelet → Ranging Guild | (2656, 3442, 0) | 11118-11126 | "Ranging Guild" |
| Skills Necklace → Fishing Guild | (2611, 3393, 0) | 11105-11113 | "Fishing Guild" |
| Skills Necklace → Mining Guild | (3050, 9763, 0) | 11105-11113 | "Mining Guild" |
| Skills Necklace → Crafting Guild | (2933, 3293, 0) | 11105-11113 | "Crafting Guild" |
| Skills Necklace → Cooking Guild | (3143, 3442, 0) | 11105-11113 | "Cooking Guild" |
| Skills Necklace → Woodcutting Guild | (1662, 3505, 0) | 11105-11113 | "Woodcutting Guild" |
| Skills Necklace → Farming Guild | (1248, 3719, 0) | 11105-11113 | "Farming Guild" |
| Ring of Wealth → Grand Exchange | (3164, 3461, 0) | 11980-11988 | "Grand Exchange" |
| Ring of Wealth → Falador | (2996, 3377, 0) | 11980-11988 | "Falador" |
| Necklace of Passage → Wizards Tower | (3113, 3179, 0) | 21146-21158 | "Wizards' Tower" |
| Necklace of Passage → The Outpost | (2430, 3347, 0) | 21146-21158 | "The Outpost" |
| Necklace of Passage → Eagle's Eyrie | (3406, 3156, 0) | 21146-21158 | "Eagle's Eyrie" |
| Digsite Pendant → Digsite | (3346, 3445, 0) | 11190-11194 | "Digsite" |
| Slayer Ring → Slayer Tower | (3422, 3537, 0) | 11866-11873 | "Slayer Tower" |
| Slayer Ring → Fremennik Slayer Dungeon | (2794, 3615, 0) | 11866-11873 | "Fremennik Slayer Dungeon" |
| Slayer Ring → Stronghold Slayer Cave | (2432, 3423, 0) | 11866-11873 | "Stronghold Slayer Cave" |
| Slayer Ring → Dark Beasts | (2028, 4636, 0) | 11866-11873 | "Dark Beasts" |

#### Magic Teleports (Priority 2)

| Teleport | Destination | Rune Requirements | Spell |
|----------|-------------|-------------------|-------|
| Varrock Teleport | (3212, 3424, 0) | 1 Law, 3 Air, 1 Fire | Standard |
| Lumbridge Teleport | (3222, 3218, 0) | 1 Law, 3 Air, 1 Earth | Standard |
| Falador Teleport | (2965, 3379, 0) | 1 Law, 3 Air, 1 Water | Standard |
| Camelot Teleport | (2757, 3479, 0) | 1 Law, 5 Air | Standard |
| Ardougne Teleport | (2661, 3301, 0) | 2 Law, 2 Water | Standard |

#### Tablet Teleports (Priority 3)

Same destinations as spells but use inventory items instead.

| Teleport | Item ID |
|----------|---------|
| Varrock Teleport Tab | 8007 |
| Lumbridge Teleport Tab | 8008 |
| Falador Teleport Tab | 8009 |
| Camelot Teleport Tab | 8010 |
| Ardougne Teleport Tab | 8011 |

#### Equipment Teleports (Priority 4 - Quest/Diary Dependent)

| Teleport | Destination | Item | Requirement |
|----------|-------------|------|-------------|
| Crafting Cape | (2933, 3293, 0) | 9780, 9781 | 99 Crafting |
| Ardougne Cloak → Monastery | (2606, 3222, 0) | 13121-13124 | Ardougne Diary |
| Explorer's Ring → Cabbage Patch | (3053, 3291, 0) | 13125-13128 | Lumbridge Diary |
| Karamja Gloves → Gem Mine | (2825, 2997, 0) | 13129-13132 | Karamja Diary |

### Teleport Implementation Pattern

```java
public class TeleportRegistry {
    private static final Map<WorldPosition, TeleportDefinition> teleportMap = new HashMap<>();

    static {
        // Ring of Dueling - Duel Arena
        register(new TeleportDefinition(
            35,  // cost
            new WorldPosition(3316, 3233, 0),
            (script) -> hasEquippedAny(script, RING_OF_DUELING_IDS),
            (script) -> RetryUtils.equipmentInteract(
                script,
                findEquipped(script, RING_OF_DUELING_IDS),
                "Duel Arena",
                "ring of dueling teleport"
            )
        ));

        // ... more teleports
    }

    public static TeleportDefinition getTeleportTo(WorldPosition destination) {
        // Allow some tolerance (within 15 tiles)
        return teleportMap.entrySet().stream()
            .filter(e -> e.getKey().distanceTo(destination) <= 15)
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }
}
```

---

## Implementation Plan

### Phase 1: Core Infrastructure (10-15 hours)

1. **HTTP Client Wrapper**
   - POST requests to api.dax.cloud
   - JSON serialization/deserialization
   - Error handling (rate limits, timeouts)
   - Response parsing

2. **PlayerDetails Builder**
   - Skill level extraction from XPTrackers
   - Equipment scanning
   - Inventory scanning
   - Members world detection

3. **Position Utilities**
   - WorldPosition ↔ Point3D conversion
   - Distance calculations
   - Tolerance matching for teleport destinations

### Phase 2: Teleport System (20-30 hours)

1. **TeleportDefinition Class**
   - Cost, destination, requirements, trigger

2. **TeleportRegistry**
   - Static map of all teleports
   - Lookup by destination position
   - Requirement checking

3. **TeleportExecutor**
   - Item detection (find ring of dueling in equipment)
   - Action execution via RetryUtils
   - Verification (did we teleport?)

4. **Implement 35+ Teleports**
   - Jewelry (highest priority)
   - Spells
   - Tablets
   - Equipment/capes

### Phase 3: Path Execution (15-20 hours)

1. **PathExecutor**
   - Receive path from API
   - Detect if teleport needed (current pos ≠ path[0])
   - Execute teleport if needed
   - Convert remaining path to WorldPositions
   - Walk with OSMB walker

2. **WalkConfig Integration**
   - Break conditions
   - Timeout handling
   - Humanization

3. **Error Recovery**
   - Teleport failed → retry
   - Walking stuck → recalculate
   - API error → fallback behavior

### Phase 4: Polish (10-15 hours)

1. **Caching**
   - Cache paths for repeated routes
   - Invalidate on teleport availability change

2. **Testing**
   - Test common routes
   - Test edge cases (wilderness, instances)
   - Test teleport detection

3. **Documentation**
   - API usage guide
   - Teleport extension guide

---

## Estimated Work Breakdown

| Component | Hours | Difficulty |
|-----------|-------|------------|
| HTTP client + JSON | 4-6 | Easy |
| PlayerDetails builder | 6-10 | Medium |
| Position utilities | 2-3 | Easy |
| TeleportDefinition class | 2-3 | Easy |
| TeleportRegistry | 4-6 | Medium |
| Teleport definitions (35+) | 12-20 | Tedious |
| Teleport execution logic | 10-16 | Medium |
| PathExecutor | 6-10 | Medium |
| Error handling | 6-10 | Medium |
| Testing | 10-15 | Variable |
| **Total** | **60-100** | |

---

## Risks and Mitigations

### Risk 1: No Varbit Access in OSMB (Confirmed)

**Impact:** Server won't know quest states, may not use fairy rings, spirit trees, etc.

**Status:** Confirmed - OSMB does not expose varbits.

**Mitigation (MVP):**
- Common teleports (jewelry, spells) don't need varbits
- Server falls back to non-quest paths
- Quest-dependent features deferred to post-MVP

**Mitigation (Post-MVP):**
- Build QuestChecker utility in TidalsUtilities
- Visually detect quest completion (quest tab, NPC dialogues, item possession)
- Map quest states to varbit equivalents for API requests
- See [Future Work: QuestChecker Utility](#future-work-questchecker-utility)

### Risk 2: API Rate Limits

**Impact:** Free tier may have request limits

**Mitigation:**
- Cache paths for common routes
- Batch requests where possible
- Implement exponential backoff on 429

### Risk 3: API Changes/Downtime

**Impact:** WebWalker stops working

**Mitigation:**
- Graceful fallback to manual paths
- Monitor API status
- Keep local backup paths for critical routes

### Risk 4: Teleport Detection Edge Cases

**Impact:** Script has teleport but can't detect/use it

**Mitigation:**
- Comprehensive item ID lists (all charge variants)
- Equipment AND inventory scanning
- Retry logic on failures

---

## Usage Example (Target API)

```java
// Simple usage - walk anywhere
DaxWebWalker.walkTo(script, new WorldPosition(2440, 3089, 0));

// With options
DaxWebWalker.walkTo(script, new WorldPosition(2440, 3089, 0),
    new WalkOptions()
        .allowTeleports(true)
        .timeout(60000)
        .breakCondition(() -> someCondition())
);

// Walk to nearest bank
DaxWebWalker.walkToBank(script);

// Walk to specific bank
DaxWebWalker.walkToBank(script, Bank.VARROCK_EAST);
```

---

## File Structure (Proposed)

```
utilities/src/main/java/utilities/webwalker/
├── DaxWebWalker.java           # Main public API
├── DaxApiClient.java           # HTTP client for dax.cloud
├── PlayerDetailsBuilder.java   # Builds request payload
├── PathExecutor.java           # Executes returned path
├── TeleportRegistry.java       # Maps positions to teleports
├── TeleportExecutor.java       # Executes individual teleports
├── models/
│   ├── PathRequest.java
│   ├── PathResult.java
│   ├── PlayerDetails.java
│   └── Point3D.java
└── teleports/
    ├── TeleportDefinition.java
    ├── JewelryTeleports.java   # Ring of dueling, glory, etc.
    ├── SpellTeleports.java     # Magic teleports
    └── EquipmentTeleports.java # Capes, diary items
```

---

## References

- [Dax Walker GitHub (TriBot)](https://github.com/itsdax/Runescape-Web-Walker-Engine)
- [Dax Walker GitHub (RSPeer)](https://github.com/itsdax/RSPeer-Webwalker)
- [dax.cloud Admin](https://admin.dax.cloud/)
- [OSMB Walker Documentation](./Walker.md)
- [OSMB API Reference](./api-reference.md)

---

## Open Questions

1. ~~**Varbit Access:** Does OSMB expose varbits/settings?~~ **RESOLVED:** Confirmed NOT available. See Future Work for QuestChecker plans.

2. **API Key:** What are the rate limits on the free tier? Need to test.

3. **Instanced Areas:** How does dax.cloud handle instances (Tempoross, ToB, etc.)? Likely doesn't - need manual paths.

4. **Wilderness:** Does the API handle wilderness paths? Need to verify.

5. **Members Detection:** Best way to detect if on members world in OSMB?

---

## Proof of Concept Scope

Before full implementation, build a minimal PoC:

1. Call API with hardcoded player data
2. Implement 3 teleports (glory, ring of dueling, games necklace)
3. Execute a cross-map path (GE → Castle Wars)
4. Verify it works end-to-end

**Estimated PoC time:** 8-12 hours

If PoC succeeds, proceed with full implementation.

---

## Test Script for Debugging

A standalone test script to validate each component of the WebWalker integration.

### Setup Requirements

**Player Position:** Grand Exchange (3165, 3485, 0)
- Central location with good region loading
- Easy to bank and re-gear between tests

**Required Equipment:**
- Ring of dueling (any charge)
- Amulet of glory (any charge)
- Games necklace (any charge)

**Inventory:**
- None required for basic tests

### Test Script Structure

```java
package main;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.item.ItemID;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import com.osmb.api.trackers.experience.XPTracker;

import java.util.*;

@ScriptDefinition(
    name = "WebWalkerTest",
    description = "Debug script for DaxWebWalker integration",
    skillCategory = SkillCategory.OTHER,
    version = 1.0,
    author = "Tidaleus"
)
public class WebWalkerTest extends Script {

    private enum TestPhase {
        API_CONNECTION,
        PLAYER_DETAILS,
        PATH_REQUEST,
        TELEPORT_DETECTION,
        TELEPORT_EXECUTION,
        PATH_WALKING,
        FULL_INTEGRATION
    }

    private TestPhase currentPhase = TestPhase.API_CONNECTION;
    private boolean phaseComplete = false;

    public WebWalkerTest(Object scriptCore) {
        super(scriptCore);
    }

    @Override
    public void onStart() {
        log(getClass(), "===========================================");
        log(getClass(), "WebWalker Test Script Started");
        log(getClass(), "===========================================");
        logPlayerState();
    }

    @Override
    public int poll() {
        switch (currentPhase) {
            case API_CONNECTION:
                testApiConnection();
                break;
            case PLAYER_DETAILS:
                testPlayerDetailsBuilder();
                break;
            case PATH_REQUEST:
                testPathRequest();
                break;
            case TELEPORT_DETECTION:
                testTeleportDetection();
                break;
            case TELEPORT_EXECUTION:
                testTeleportExecution();
                break;
            case PATH_WALKING:
                testPathWalking();
                break;
            case FULL_INTEGRATION:
                testFullIntegration();
                break;
        }

        if (phaseComplete) {
            advancePhase();
        }

        return 1000;
    }

    // ==================== LOGGING HELPERS ====================

    private void logPlayerState() {
        WorldPosition pos = getWorldPosition();
        log(getClass(), "--- Player State ---");
        log(getClass(), "Position: (" + pos.getX() + ", " + pos.getY() + ", " + pos.getPlane() + ")");
        log(getClass(), "Region: " + ((pos.getX() >> 6) << 8 | (pos.getY() >> 6)));

        // log skills
        logSkillLevels();

        // log equipment
        logEquipment();

        // log inventory
        logInventory();
    }

    private void logSkillLevels() {
        log(getClass(), "--- Skill Levels ---");
        Map<SkillType, XPTracker> trackers = getXPTrackers();
        if (trackers == null || trackers.isEmpty()) {
            log(getClass(), "ERROR: No XP trackers available!");
            return;
        }

        for (SkillType skill : SkillType.values()) {
            XPTracker tracker = trackers.get(skill);
            if (tracker != null) {
                log(getClass(), skill.name() + ": " + tracker.getLevel());
            }
        }
    }

    private void logEquipment() {
        log(getClass(), "--- Equipment Check ---");

        // check for teleport jewelry
        int[] ringOfDueling = {2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566};
        int[] gloryAmulet = {1704, 1706, 1708, 1710, 1712, 11976, 11978};
        int[] gamesNecklace = {3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867};

        boolean hasRod = checkEquipped(ringOfDueling, "Ring of Dueling");
        boolean hasGlory = checkEquipped(gloryAmulet, "Amulet of Glory");
        boolean hasGames = checkEquipped(gamesNecklace, "Games Necklace");

        log(getClass(), "Teleport jewelry equipped: ROD=" + hasRod +
            ", Glory=" + hasGlory + ", Games=" + hasGames);
    }

    private boolean checkEquipped(int[] itemIds, String name) {
        var result = getWidgetManager().getEquipment().search(toSet(itemIds));
        boolean found = result != null && !result.isEmpty();
        log(getClass(), "  " + name + ": " + (found ? "EQUIPPED" : "not found"));
        return found;
    }

    private void logInventory() {
        log(getClass(), "--- Inventory Check ---");
        var inv = getWidgetManager().getInventory().search(Set.of());
        if (inv == null) {
            log(getClass(), "ERROR: Could not read inventory!");
            return;
        }
        log(getClass(), "Inventory slots used: " + inv.getSize() + "/28");
    }

    private Set<Integer> toSet(int[] arr) {
        Set<Integer> set = new HashSet<>();
        for (int i : arr) set.add(i);
        return set;
    }

    // ==================== TEST PHASES ====================

    private void testApiConnection() {
        log(getClass(), "");
        log(getClass(), "========== TEST: API CONNECTION ==========");

        // test 1: can we reach the endpoint?
        log(getClass(), "Testing connection to api.dax.cloud...");

        // TODO: implement actual HTTP call
        // DaxApiClient client = new DaxApiClient(API_KEY, SECRET);
        // boolean connected = client.testConnection();

        // for now, log what we WOULD test
        log(getClass(), "Endpoint: https://api.dax.cloud/walker/generatePaths");
        log(getClass(), "Headers required:");
        log(getClass(), "  key: sub_DPjXXzL5DeSiPf");
        log(getClass(), "  secret: PUBLIC-KEY");
        log(getClass(), "  Content-Type: application/json");

        log(getClass(), "TODO: Implement HTTP client and test connection");
        log(getClass(), "Expected: 200 OK or 401 if bad credentials");

        phaseComplete = true;
    }

    private void testPlayerDetailsBuilder() {
        log(getClass(), "");
        log(getClass(), "========== TEST: PLAYER DETAILS BUILDER ==========");

        // build what we would send to the API
        log(getClass(), "Building PlayerDetails from OSMB state...");

        WorldPosition pos = getWorldPosition();
        log(getClass(), "Start position: {\"x\":" + pos.getX() +
            ",\"y\":" + pos.getY() + ",\"z\":" + pos.getPlane() + "}");

        // skills
        log(getClass(), "Skills JSON would include:");
        Map<SkillType, XPTracker> trackers = getXPTrackers();
        if (trackers != null) {
            StringBuilder skills = new StringBuilder();
            for (SkillType skill : new SkillType[]{
                SkillType.ATTACK, SkillType.STRENGTH, SkillType.DEFENCE,
                SkillType.AGILITY, SkillType.MAGIC
            }) {
                XPTracker t = trackers.get(skill);
                if (t != null) {
                    skills.append("  \"").append(skill.name().toLowerCase())
                          .append("\": ").append(t.getLevel()).append(",\n");
                }
            }
            log(getClass(), skills.toString());
        }

        // equipment IDs
        log(getClass(), "Equipment detection:");
        int[] ringOfDueling = {2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566};
        var rodResult = getWidgetManager().getEquipment().search(toSet(ringOfDueling));
        if (rodResult != null && !rodResult.isEmpty()) {
            log(getClass(), "  Found ring of dueling - would include in equipment array");
        }

        log(getClass(), "TODO: Build complete JSON payload");

        phaseComplete = true;
    }

    private void testPathRequest() {
        log(getClass(), "");
        log(getClass(), "========== TEST: PATH REQUEST ==========");

        WorldPosition start = getWorldPosition();
        WorldPosition end = new WorldPosition(2440, 3089, 0); // castle wars

        log(getClass(), "Test route: Current position -> Castle Wars");
        log(getClass(), "Start: (" + start.getX() + ", " + start.getY() + ", " + start.getPlane() + ")");
        log(getClass(), "End: (2440, 3089, 0)");

        log(getClass(), "");
        log(getClass(), "Request JSON would be:");
        log(getClass(), "{");
        log(getClass(), "  \"player\": { ... },");
        log(getClass(), "  \"requests\": [");
        log(getClass(), "    {");
        log(getClass(), "      \"start\": {\"x\":" + start.getX() + ",\"y\":" + start.getY() + ",\"z\":" + start.getPlane() + "},");
        log(getClass(), "      \"end\": {\"x\":2440,\"y\":3089,\"z\":0}");
        log(getClass(), "    }");
        log(getClass(), "  ]");
        log(getClass(), "}");

        log(getClass(), "");
        log(getClass(), "Expected response:");
        log(getClass(), "  pathStatus: SUCCESS");
        log(getClass(), "  path: [{x:..., y:..., z:...}, ...]");
        log(getClass(), "  cost: <number>");

        log(getClass(), "");
        log(getClass(), "IMPORTANT: If path[0] != current position, first point is teleport destination");

        log(getClass(), "TODO: Make actual API call and log response");

        phaseComplete = true;
    }

    private void testTeleportDetection() {
        log(getClass(), "");
        log(getClass(), "========== TEST: TELEPORT DETECTION ==========");

        log(getClass(), "Testing teleport destination matching...");

        // known teleport destinations
        Map<String, WorldPosition> teleports = new LinkedHashMap<>();
        teleports.put("Ring of Dueling -> Castle Wars", new WorldPosition(2440, 3089, 0));
        teleports.put("Ring of Dueling -> Duel Arena", new WorldPosition(3316, 3233, 0));
        teleports.put("Ring of Dueling -> Ferox Enclave", new WorldPosition(3150, 3635, 0));
        teleports.put("Glory -> Edgeville", new WorldPosition(3087, 3496, 0));
        teleports.put("Glory -> Draynor", new WorldPosition(3105, 3251, 0));
        teleports.put("Games Necklace -> Burthorpe", new WorldPosition(2897, 3551, 0));
        teleports.put("Games Necklace -> Wintertodt", new WorldPosition(1623, 3937, 0));

        log(getClass(), "Registered teleport destinations:");
        for (Map.Entry<String, WorldPosition> entry : teleports.entrySet()) {
            WorldPosition pos = entry.getValue();
            log(getClass(), "  " + entry.getKey() + " -> (" +
                pos.getX() + ", " + pos.getY() + ", " + pos.getPlane() + ")");
        }

        log(getClass(), "");
        log(getClass(), "Test: If API returns path starting at (2440, 3089, 0):");
        log(getClass(), "  -> Should detect: Ring of Dueling -> Castle Wars");
        log(getClass(), "  -> Should check if ring equipped");
        log(getClass(), "  -> Should execute teleport before walking");

        // check what we have equipped
        log(getClass(), "");
        log(getClass(), "Available teleports based on equipment:");
        int[] ringOfDueling = {2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566};
        int[] gloryAmulet = {1704, 1706, 1708, 1710, 1712, 11976, 11978};
        int[] gamesNecklace = {3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867};

        if (checkEquippedSilent(ringOfDueling)) {
            log(getClass(), "  [OK] Ring of Dueling - Castle Wars, Duel Arena, Ferox");
        }
        if (checkEquippedSilent(gloryAmulet)) {
            log(getClass(), "  [OK] Amulet of Glory - Edgeville, Draynor, Karamja, Al Kharid");
        }
        if (checkEquippedSilent(gamesNecklace)) {
            log(getClass(), "  [OK] Games Necklace - Burthorpe, Wintertodt, Corp, Barb Outpost");
        }

        phaseComplete = true;
    }

    private boolean checkEquippedSilent(int[] itemIds) {
        var result = getWidgetManager().getEquipment().search(toSet(itemIds));
        return result != null && !result.isEmpty();
    }

    private void testTeleportExecution() {
        log(getClass(), "");
        log(getClass(), "========== TEST: TELEPORT EXECUTION ==========");

        log(getClass(), "This test will attempt to use Ring of Dueling -> Castle Wars");
        log(getClass(), "");

        int[] ringOfDueling = {2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566};

        if (!checkEquippedSilent(ringOfDueling)) {
            log(getClass(), "ERROR: No ring of dueling equipped!");
            log(getClass(), "Please equip a ring of dueling and restart test");
            phaseComplete = true;
            return;
        }

        WorldPosition before = getWorldPosition();
        log(getClass(), "Position before teleport: (" + before.getX() + ", " + before.getY() + ")");

        log(getClass(), "Attempting teleport via equipment interact...");
        log(getClass(), "Action: 'Castle Wars' on ring slot");

        // TODO: actual teleport
        // boolean success = RetryUtils.equipmentInteract(this,
        //     findEquippedId(ringOfDueling), "Castle Wars", "ring of dueling teleport");

        log(getClass(), "TODO: Execute RetryUtils.equipmentInteract()");
        log(getClass(), "TODO: Wait for position change");
        log(getClass(), "TODO: Verify arrived at Castle Wars area");

        log(getClass(), "");
        log(getClass(), "Expected result:");
        log(getClass(), "  - Menu opens on ring");
        log(getClass(), "  - 'Castle Wars' option selected");
        log(getClass(), "  - Teleport animation plays");
        log(getClass(), "  - Position changes to ~(2440, 3089, 0)");

        log(getClass(), "");
        log(getClass(), "Failure modes to check:");
        log(getClass(), "  - Ring not found in equipment slot");
        log(getClass(), "  - Menu action not matching exactly");
        log(getClass(), "  - Teleport interrupted (combat, etc)");
        log(getClass(), "  - Position not changing after teleport");

        phaseComplete = true;
    }

    private void testPathWalking() {
        log(getClass(), "");
        log(getClass(), "========== TEST: PATH WALKING ==========");

        log(getClass(), "Testing OSMB walker with a short path...");

        WorldPosition current = getWorldPosition();
        log(getClass(), "Current position: (" + current.getX() + ", " + current.getY() + ")");

        // create a short test path (10 tiles east)
        List<WorldPosition> testPath = new ArrayList<>();
        testPath.add(new WorldPosition(current.getX() + 3, current.getY(), current.getPlane()));
        testPath.add(new WorldPosition(current.getX() + 6, current.getY(), current.getPlane()));
        testPath.add(new WorldPosition(current.getX() + 10, current.getY(), current.getPlane()));

        log(getClass(), "Test path (10 tiles east):");
        for (int i = 0; i < testPath.size(); i++) {
            WorldPosition wp = testPath.get(i);
            log(getClass(), "  [" + i + "] (" + wp.getX() + ", " + wp.getY() + ")");
        }

        log(getClass(), "");
        log(getClass(), "TODO: Execute walker.walkPath(testPath, config)");
        log(getClass(), "TODO: Log each waypoint reached");
        log(getClass(), "TODO: Verify final position");

        log(getClass(), "");
        log(getClass(), "WalkConfig options to test:");
        log(getClass(), "  - breakDistance(0) for exact tile");
        log(getClass(), "  - timeout(30000) for safety");
        log(getClass(), "  - breakCondition() for early exit");

        phaseComplete = true;
    }

    private void testFullIntegration() {
        log(getClass(), "");
        log(getClass(), "========== TEST: FULL INTEGRATION ==========");

        log(getClass(), "Full test: GE -> Castle Wars using WebWalker");
        log(getClass(), "");

        log(getClass(), "Expected flow:");
        log(getClass(), "  1. Build PlayerDetails from current state");
        log(getClass(), "  2. Send path request to api.dax.cloud");
        log(getClass(), "  3. Receive path response");
        log(getClass(), "  4. Check if path[0] requires teleport");
        log(getClass(), "  5. Execute teleport if needed");
        log(getClass(), "  6. Walk remaining path segments");
        log(getClass(), "  7. Verify arrival at destination");

        log(getClass(), "");
        log(getClass(), "TODO: DaxWebWalker.walkTo(this, new WorldPosition(2440, 3089, 0))");

        log(getClass(), "");
        log(getClass(), "===========================================");
        log(getClass(), "All test phases complete!");
        log(getClass(), "===========================================");

        // stop the script
        phaseComplete = false; // don't advance, we're done
    }

    private void advancePhase() {
        phaseComplete = false;
        TestPhase[] phases = TestPhase.values();
        int nextOrdinal = currentPhase.ordinal() + 1;
        if (nextOrdinal < phases.length) {
            currentPhase = phases[nextOrdinal];
            log(getClass(), "");
            log(getClass(), ">>> Advancing to phase: " + currentPhase.name());
        }
    }
}
```

### Test Phases Explained

| Phase | Purpose | What to Check in Logs |
|-------|---------|----------------------|
| `API_CONNECTION` | Verify we can reach dax.cloud | HTTP status, timeout errors |
| `PLAYER_DETAILS` | Test building request payload | Skills populated, equipment detected |
| `PATH_REQUEST` | Send actual path request | Response status, path array, cost |
| `TELEPORT_DETECTION` | Match path[0] to known teleport | Correct teleport identified |
| `TELEPORT_EXECUTION` | Execute a teleport | Menu interaction, position change |
| `PATH_WALKING` | Walk a short path | Waypoints reached, final position |
| `FULL_INTEGRATION` | End-to-end test | Complete journey succeeds |

### What to Look For in Logs

**API Issues:**
```
[WebWalkerTest] ERROR: HTTP 429 - Rate limited
[WebWalkerTest] ERROR: HTTP 401 - Invalid credentials
[WebWalkerTest] ERROR: Connection timeout after 5000ms
[WebWalkerTest] ERROR: JSON parse error: <details>
```

**PlayerDetails Issues:**
```
[WebWalkerTest] ERROR: No XP trackers available!
[WebWalkerTest] ERROR: Could not read inventory!
[WebWalkerTest] WARNING: Skill level returned 0 for AGILITY
```

**Path Issues:**
```
[WebWalkerTest] pathStatus: BLOCKED - no valid path
[WebWalkerTest] pathStatus: NO_WEB_PATH - area not mapped
[WebWalkerTest] WARNING: Empty path returned
[WebWalkerTest] WARNING: Path has only 1 waypoint
```

**Teleport Issues:**
```
[WebWalkerTest] ERROR: No teleport found for destination (x, y, z)
[WebWalkerTest] ERROR: Ring of dueling not equipped
[WebWalkerTest] WARNING: Teleport failed, position unchanged
[WebWalkerTest] ERROR: Menu action 'Castle Wars' not found
```

**Walking Issues:**
```
[WebWalkerTest] WARNING: Walker stuck at (x, y) for 10 seconds
[WebWalkerTest] ERROR: Walker timeout after 30000ms
[WebWalkerTest] WARNING: Position not changing during walk
```

### Debug Logging to Add in Implementation

```java
// in DaxApiClient
public PathResult getPath(WorldPosition start, WorldPosition end) {
    log("API REQUEST: " + start + " -> " + end);
    log("API URL: " + ENDPOINT);
    log("API PAYLOAD: " + jsonPayload);

    // ... http call ...

    log("API RESPONSE CODE: " + responseCode);
    log("API RESPONSE BODY: " + responseBody);
    log("API PATH STATUS: " + result.getPathStatus());
    log("API PATH LENGTH: " + result.getPath().size() + " waypoints");
    log("API PATH COST: " + result.getCost());

    return result;
}

// in TeleportRegistry
public TeleportDefinition getTeleportTo(WorldPosition destination) {
    log("TELEPORT LOOKUP: searching for destination " + destination);

    for (TeleportDefinition def : teleports) {
        double dist = def.getDestination().distanceTo(destination);
        log("  checking " + def.getName() + " (dist=" + dist + ")");
        if (dist <= 15) {
            log("TELEPORT FOUND: " + def.getName());
            return def;
        }
    }

    log("TELEPORT NOT FOUND for " + destination);
    return null;
}

// in PathExecutor
public boolean execute(PathResult path) {
    log("EXECUTING PATH: " + path.getPath().size() + " waypoints");

    WorldPosition first = path.getPath().get(0);
    WorldPosition current = script.getWorldPosition();

    log("PATH[0]: " + first);
    log("CURRENT: " + current);
    log("DISTANCE TO PATH[0]: " + current.distanceTo(first));

    if (current.distanceTo(first) > 20) {
        log("TELEPORT REQUIRED: current position too far from path start");
        TeleportDefinition teleport = TeleportRegistry.getTeleportTo(first);
        if (teleport == null) {
            log("ERROR: No teleport available to reach path start!");
            return false;
        }
        log("EXECUTING TELEPORT: " + teleport.getName());
        // ... teleport ...
    }

    for (int i = 0; i < path.getPath().size(); i++) {
        WorldPosition waypoint = path.getPath().get(i);
        log("WALKING TO WAYPOINT [" + i + "]: " + waypoint);
        // ... walk ...
        log("REACHED WAYPOINT [" + i + "]");
    }

    log("PATH COMPLETE");
    return true;
}
```

### Common Test Scenarios

| Scenario | Start | End | Tests |
|----------|-------|-----|-------|
| Short walk | GE | Varrock West Bank | Pure walking, no teleport |
| Single teleport | GE | Castle Wars | Ring of dueling teleport |
| Teleport + walk | GE | Edgeville furnace | Glory to Edgeville + short walk |
| Long journey | GE | Wintertodt | Games necklace + extended walk |
| Cross-continent | Lumbridge | Ardougne | Multiple teleport options |

---

## Future Work: QuestChecker Utility

> **Priority:** Post-MVP | **Location:** TidalsUtilities

Since OSMB does not expose varbits, we cannot directly tell the dax.cloud API which quests are complete. This limits the paths the server will generate (no fairy rings, spirit trees, quest shortcuts, etc.).

### Planned Solution

Build a `QuestChecker` utility in TidalsUtilities that can determine quest completion through visual/behavioral checks:

### Detection Methods

| Method | Use Case | Reliability |
|--------|----------|-------------|
| Item possession | Ectophial, Dramen staff, Royal seed pod | High |
| Equipment check | Quest cape, skill capes with quest req | High |
| Location access | Can enter area = quest done | Medium |
| Quest tab OCR | Read quest list status | Medium-Low |
| NPC dialogue | Check for post-quest dialogue | Low |

### Example Implementation

```java
public class QuestChecker {

    // high confidence - item proves quest completion
    public static boolean hasFairyTalePartII(Script script) {
        // player has dramen/lunar staff OR can use fairy ring without it
        return hasItem(script, ItemID.DRAMEN_STAFF, ItemID.LUNAR_STAFF)
            || hasEquipped(script, ItemID.DRAMEN_STAFF, ItemID.LUNAR_STAFF);
    }

    public static boolean hasGhostsAhoy(Script script) {
        // ectophial proves completion
        return hasItem(script, ItemID.ECTOPHIAL);
    }

    public static boolean hasMonkeyMadnessII(Script script) {
        // royal seed pod proves completion
        return hasItem(script, ItemID.ROYAL_SEED_POD);
    }

    public static boolean hasTreeGnomeVillage(Script script) {
        // spirit tree access - could test by trying to use one
        // or check for gnome amulet
        return hasItem(script, ItemID.GNOME_AMULET);
    }

    // build varbit-equivalent map for API
    public static Map<Integer, Integer> buildQuestVarbits(Script script) {
        Map<Integer, Integer> varbits = new HashMap<>();

        // fairy tale pt 2 varbit
        if (hasFairyTalePartII(script)) {
            varbits.put(5087, 1);  // or whatever the actual varbit is
        }

        // ... more quest checks

        return varbits;
    }
}
```

### Mapping to Dax API

Once QuestChecker can determine quest states, we map them to the varbit IDs that dax.cloud expects:

| Quest | Unlock | Varbit ID | Detection Method |
|-------|--------|-----------|------------------|
| Fairy Tale Part II | Fairy rings | 5087? | Dramen staff possession |
| Tree Gnome Village | Spirit trees | TBD | Gnome amulet / location test |
| Ghosts Ahoy | Ectophial | TBD | Ectophial possession |
| Monkey Madness II | Royal seed pod | TBD | Royal seed pod possession |
| Plague City | Ardougne teleport | TBD | Spell availability |

### Implementation Priority

1. **High value, easy detection:**
   - Ectophial (Ghosts Ahoy)
   - Royal seed pod (MM2)
   - Dramen staff (Fairy Tale)

2. **Medium value:**
   - Spirit trees (Tree Gnome Village)
   - Gnome gliders

3. **Lower priority:**
   - Agility shortcuts (diary-based)
   - Obscure quest teleports

### Integration with WebWalker

```java
// in PlayerDetailsBuilder
public PlayerDetails build(Script script) {
    PlayerDetails details = new PlayerDetails();

    // ... skills, equipment, inventory ...

    // add quest-derived varbits if QuestChecker available
    if (QuestChecker.isAvailable()) {
        details.setVarbits(QuestChecker.buildQuestVarbits(script));
    }

    return details;
}
```

This allows the WebWalker to progressively improve path quality as more quest checks are added, without requiring changes to the core walking logic.
