# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Build Commands

**Build this script:**
```bash
cd /Users/zaffre/Documents/Engineering/Projects/Scripts-Project/TidalsChompyHunter-WT
osmb build TidalsChompyHunter
```

**Alternative: Using gradlew directly** (from parent directory):
```bash
cd /Users/zaffre/Documents/Engineering/Projects/Scripts-Project/TidalsChompyHunter-WT
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :TidalsChompyHunter:clean :TidalsChompyHunter:build
```

**Output location:** `jar/TidalsChompyHunter.jar`

**Build the utilities dependency** (if modified):
```bash
cd /Users/zaffre/Documents/Engineering/Projects/Scripts-Project/TidalsChompyHunter-WT
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :utilities:build
```

**CRITICAL:** Always build after making code changes and before testing in the OSMB client.

## Architecture Overview

### Task-Based Execution Model
The script uses a priority-based task execution system where tasks activate based on game state conditions. Tasks are evaluated in order each poll cycle, and the first task to activate gets executed.

**Task Priority Order:**
1. `Setup` - Initial validation (runs once, enables arrow detection)
2. `AttackChompy` - Highest priority after setup - chompies interrupt everything
3. `FillBellows` - Refills ogre bellows when empty
4. `InflateToads` - Finds and inflates swamp toads (defers if chompy arrow present)
5. `DropToads` - Drops bloated toads as bait

**Key Design Principle:** `AttackChompy` is second in order because when a chompy spawns (indicated by minimap arrow), all other activities must stop immediately to attack it. This prevents the script from wasting time inflating toads when a chompy is already active.

### Task Interface
All tasks implement the `Task` abstract class (`utils/Task.java`):
- `activate()` - Returns true if task should run based on current game state
- `execute()` - Performs the task's actions, returns true if successful

### Visual Detection Architecture
**CRITICAL CONCEPT:** OSMB is a color bot that uses visual/pixel detection, NOT memory injection. All game state must be inferred from screen analysis.

**Three Detection Methods (in priority order):**

1. **Minimap Arrow Detection** (fastest, most reliable)
   - Used for chompy spawns
   - Requires `arrowDetectionEnabled(true)` in Setup
   - `getWidgetManager().getMinimap().getLastArrowResult()`

2. **RGB Pixel Cluster Detection** (primary method for entities)
   - Used for: Swamp toads, Bloated toads (ground bait), Chompies
   - Finds sprite colors on screen, groups nearby pixels into clusters
   - Each cluster center = clickable entity position
   - Tuned using Debug Tool (RGB color + threshold + cluster params)

3. **Minimap NPC Position Detection** (fallback)
   - `getWidgetManager().getMinimap().getNPCPositions()`
   - Less precise than pixel clusters
   - Used when arrow detection repeatedly fails

### Critical Detection Constants
These RGB color values and cluster parameters were tuned via Debug Tool:

```java
// Swamp toad sprite (live, inflatable NPCs)
SWAMP_TOAD_SPRITE = -14286849  // RGB int, threshold=5
CLUSTER_MAX_DISTANCE = 10
CLUSTER_MIN_SIZE = 10

// Bloated toad sprite (dropped bait on ground)
BLOATED_TOAD_SPRITE = -9071822  // RGB int, threshold=5

// Chompy sprite (fallback detection)
CHOMPY_SPRITE = -2237013  // RGB int, threshold=20
CHOMPY_CLUSTER_MAX_DISTANCE = 15
CHOMPY_CLUSTER_MIN_SIZE = 10
```

**Screen Edge Safety:** When using pixel clusters, always filter out points within 25px of screen edges (approx 750x700 screen) to prevent `ArrayIndexOutOfBoundsException` from tap operations.

### State Management
The main script (`TidalsChompyHunter.java`) maintains global state flags:
- `setupComplete` - Initial validation passed
- `bellowsEmpty` - Set via chat parsing ("air seems too thin"), triggers FillBellows
- `groundToadCount` - Logical counter (tracks drops/kills, not unreliable sprite detection)

**Chat Parsing:** The script monitors chat messages to detect empty bellows and kill counts. This is more reliable than attempting to parse UI elements.

## Development Patterns

### Menu Interaction Retry Pattern (REQUIRED)
Menu interactions can fail due to timing, camera angle, or misclicks. **Always use RetryUtils from TidalsUtilities.jar:**

```java
import utilities.RetryUtils;

// Equipment interactions (10 retries, 300-500ms delays)
RetryUtils.equipmentInteract(script, ItemID.OGRE_BELLOWS, "Fill", "fill bellows");

// Object interactions
RetryUtils.objectInteract(script, bankChest, "Use", "bank chest");

// Polygon/tile interactions
RetryUtils.tap(script, toadPolygon, "Inflate", "inflate toad");
```

Never write custom retry logic - use the shared utilities to maintain consistency across the codebase.

### Animation Waiting Pattern
When performing actions that trigger animations (inflating toads, attacking), always check if player is animating before attempting next action:

```java
// Wait for previous animation to complete
if (script.isPlayerAnimating(0.25)) {
    return true;  // try again next poll
}

// Perform action
getFinger().tap(bounds, "Inflate");

// Wait for animation to start and complete
script.pollFramesUntil(() -> !script.isPlayerAnimating(0.25), 5000);
```

**Tolerance value:** `0.25` is the standard animation tolerance used throughout the script.

### Banking Pattern
Use shared utilities from TidalsUtilities.jar:

```java
import utilities.BankingUtils;

// Open bank
RSObject bank = BankingUtils.findNearestBank(script);
BankingUtils.openBankAndWait(script, bank, 15000);

// Deposit all except specific items
BankingUtils.depositAllExcept(script, Set.of(ItemID.OGRE_BELLOWS));

// Close bank
BankingUtils.closeBankAndWait(script, 5000);
```

### Humanization
Add random delays between actions:

```java
// Standard delay (200-400ms) between most actions
script.submitTask(() -> false, script.random(200, 400));

// Longer delay (300-600ms) after bank operations
script.submitTask(() -> false, script.random(300, 600));
```

## File Structure

```
TidalsChompyHunter/
├── build.gradle              # Gradle build config (Java 17, API.jar, TidalsUtilities.jar)
├── Context.md                # Session changes and debugging history
├── jar/                      # Build output
│   └── TidalsChompyHunter.jar
└── src/main/java/
    ├── main/
    │   ├── TidalsChompyHunter.java  # Main script, task order, state management, paint
    │   └── ScriptUI.java            # JavaFX setup UI for user configuration
    ├── tasks/
    │   ├── Setup.java               # Initial validation, enables arrow detection
    │   ├── AttackChompy.java        # Arrow + pixel cluster detection, combat
    │   ├── FillBellows.java         # Refills ogre bellows at swamp bubbles
    │   ├── InflateToads.java        # Swamp toad detection (pixel clusters), inflation logic
    │   └── DropToads.java           # Drops bloated toads as bait
    └── utils/
        ├── Task.java                # Abstract task interface
        └── SpawnedChompy.java       # Tracks chompy spawn positions for monitoring
```

## Dependencies

**API.jar** - Located at `../API/API.jar` (OSMB client API)
- Core imports: `com.osmb.api.script.Script`, `com.osmb.api.scene.RSObject`, etc.
- All game interaction goes through this API

**TidalsUtilities.jar** - Located at `../utilities/jar/TidalsUtilities.jar`
- `RetryUtils` - Menu interaction retries
- `BankingUtils` - Banking operations
- `TabUtils` - Tab opening with verification
- `DialogueUtils` - Dialogue handling

## Important Documentation References

When working on this codebase, refer to these parent directory docs:

**Core References:**
- `../CLAUDE.md` - **CRITICAL:** Complete OSMB development guide, patterns, and rules
- `../docs/api-reference.md` - Complete API methods and imports
- `../docs/critical-concepts.md` - Color bot fundamentals (MUST READ)
- `../docs/Common-menu-entries.md` - Exact menu action strings
- `../docs/banking-patterns.md` - Banking, inventory, deposits
- `../docs/walking-npcs.md` - Walking, NPC interaction, objects
- `../docs/ui-widgets.md` - Dialogue, equipment, minimap, overlays
- `../docs/Paint.md` - Paint overlay & Setup UI standards
- `../docs/common-mistakes.md` - Debugging guide, pitfalls to avoid
- `../docs/Walker.md` - Pathfinding (CRITICAL: read before writing walking code)

**TidalsUtilities Reference:**
- `../utilities/` - Source code for shared utilities
- See CLAUDE.md section "Shared Utilities" for usage patterns

## Key Debugging Patterns

**Use Debug Tool** (built into OSMB client) to tune pixel detection:
- Find RGB color values for sprites
- Test threshold values (how strict color matching is)
- Tune cluster parameters (max distance, min size)
- Export tuned values to code

**Log strategically:**
```java
script.log(getClass(), "found " + clusters.size() + " swamp toad sprite clusters");
script.log(getClass(), "inflate attempt " + attempt + "/5");
```

**Common issues:**
- **Double-tap bug:** Don't call `tap()` after `tapGetResponse()` - the menu is already open
- **Items not detected:** Use BuffOverlay for items with charges, not inventory search
- **NPCs not found:** Check if arrow detection is enabled, verify sprite RGB colors
- **Screen edge crashes:** Filter cluster points within 25px of edges

## Testing Notes

**After code changes:**
1. Build the script: `osmb build TidalsChompyHunter`
2. Copy jar to OSMB scripts directory if needed
3. Test in OSMB client with Debug Tool open
4. Monitor logs for detection success/failure
5. Update Context.md with session changes

**Key validation checks:**
- Minimap arrow appears when chompy spawns
- Swamp toad sprite clusters detected (logged count)
- Bellows empty flag triggers correctly via chat
- Task priority: chompies always interrupt toad inflation

## Critical Rules from CLAUDE.md

**NEVER ASSUME A METHOD EXISTS** - Always refer to documentation, examples, or existing Tidal scripts

**Test behavior, not implementation** - Tests find bugs, not pass

**Read the stack trace** - Fix root cause, not symptom

**Don't overengineer** - No abstractions before needed

**Don't reinvent** - Read existing code before writing new

**Always build after changes** - Test in client with changes applied

**Update Context.md** - Document session changes for future sessions
