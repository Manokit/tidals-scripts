# OSMB Script Development Guide

> **CRITICAL: OSMB is a COLOR BOT** - It uses visual/pixel detection, NOT injection. You cannot access game memory directly. All detection is done through screen analysis, color matching, and OCR.

**CRITICAL: NEVER ASSUME A METHOD EXISTS. ALWAYS REFER TO ONE OF THE CORE REFERENCES, OR ANYTHING IN THE DOCS DIR, EXAMPLES DIR, OR OTHER TIDALS SCRIPTS FOR DIRECTION AND CLARIFICATION**

---


WHEN USER POSTS LOGS → THERE IS A BUG. INVESTIGATE.
WHEN TESTS FAIL → Test is source of truth unless PROVEN otherwise.
TEST BEHAVIOR, NOT IMPLEMENTATION. TESTS FIND BUGS, NOT PASS.
READ THE STACK TRACE. FIX ROOT CAUSE, NOT SYMPTOM.
FAIL FAST. Silent failures are worst. Errors > Warnings.
VERIFY ASSUMPTIONS. Don't assume - check.
BUILD WHAT WAS ASKED. Not what you think should be built.
DON'T OVERENGINEER. No abstractions before they're needed.
DON'T REINVENT. Read existing code before writing new.
DON'T SWALLOW ERRORS. Try/catch that ignores = hidden bugs.
IS THIS HOW A SENIOR STAFF ARCHITECT WOULD DO IT? ACT LIKE ONE.
WHEN YOU'RE DONE MAKING CHANGES, ALWAYS BUILD THE SCRIPT.
ALWAYS MAKE A DISCORD_POST.MD FOLLOWING THE EXAMPLE IN THE EXAMPLES DIR, UPDATE IT AS YOU GO.


## 🆕 New Script Guidelines

When creating a new script, follow this structure and include ALL of these elements.

### Folder Structure
```
TidalsScriptName/
├── build.gradle
├── src/main/java/
│   ├── main/
│   │   ├── TidalsScriptName.java    # main script class
│   │   └── ScriptUI.java            # javafx setup ui (optional)
│   ├── tasks/
│   │   ├── Setup.java               # initial validation task
│   │   ├── Process.java             # main activity task
│   │   └── Bank.java                # banking task
│   ├── utils/
│   │   └── Task.java                # task interface
│   └── obf/
│       └── Secrets.java             # api keys (gitignored)
└── src/main/resources/
    └── logo.png                     # 208x91 png
```

### build.gradle Template
```gradle
plugins {
    id 'java'
}

group = 'com.osmb.scripts'
version = '1.0'

repositories {
    mavenCentral()
}

dependencies {
    compileOnly files('../API/API.jar')
    implementation files('../utilities/jar/TidalsUtilities.jar')
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

jar {
    archiveFileName = "${project.name}.jar"
    destinationDirectory = file("${projectDir}/jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    from sourceSets.main.resources
}

clean {
    delete "${projectDir}/jar"
}
```

### Main Script Template
```java
package main;

import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.scene.RSObject;
// ... other imports

@ScriptDefinition(
        name = "TidalsScriptName",
        description = "Does something useful",
        skillCategory = SkillCategory.PROCESSING,  // or COMBAT, GATHERING, etc.
        version = 1.0,
        author = "Tidaleus"
)
public class TidalsScriptName extends Script {
    public static final String SCRIPT_VERSION = "1.0";
    private static final String SCRIPT_NAME = "ScriptName";  // short name for stats

    // state
    public static boolean setupComplete = false;
    public static String task = "starting...";
    public static long startTime = System.currentTimeMillis();

    // stats
    public static int itemsProcessed = 0;

    private List<Task> tasks;

    public TidalsScriptName(Object scriptCore) {
        super(scriptCore);
    }

    // CRITICAL: always override this with the standard bank list
    @Override
    public int[] regionsToPrioritise() {
        return BANK_REGIONS;
    }

    @Override
    public void onStart() {
        log(getClass(), "starting " + SCRIPT_NAME + " v" + SCRIPT_VERSION);
        tasks = Arrays.asList(
                new Setup(this),
                new Process(this),
                new Bank(this)
        );
    }

    @Override
    public int poll() {
        for (Task task : tasks) {
            if (task.activate()) {
                task.execute();
                return 0;
            }
        }
        return 600;
    }

    // ... paint, stats, etc.
}
```

### CRITICAL: Standard Bank Regions (ALWAYS INCLUDE)
Every script that uses banking MUST include this regions list. Copy it exactly.

```java
// standard bank regions - covers 99% of bank locations
public static final int[] BANK_REGIONS = {
    13104, // shantay pass
    13105, // al kharid
    13363, // duel arena / pvp arena
    12850, // lumbridge castle
    12338, // draynor
    12853, // varrock east
    12597, // varrock west + cooks guild
    12598, // grand exchange
    12342, // edgeville
    12084, // falador east + mining guild
    11828, // falador west
    11571, // crafting guild
    11319, // warriors guild
    11061, // catherby
    10806, // seers
    11310, // shilo
    10284, // corsair cove
    9772,  // myths guild
    10288, // yanille
    10545, // port khazard
    10547, // ardougne east/south
    10292, // ardougne east/north
    10293, // fishing guild
    10039, // barbarian assault
    9782,  // grand tree
    9781,  // tree gnome stronghold
    9776,  // castle wars
    9265,  // lletya
    8748,  // soul wars
    8253,  // lunar isle
    9275,  // neitiznot
    9531,  // jatiszo
    6461,  // wintertodt
    7227,  // port piscarilius
    6458,  // arceeus
    6457,  // kourend castle
    6968,  // hosidius
    7223,  // vinery
    6710,  // sand crabs chest
    6198,  // woodcutting guild
    5941,  // land's end
    5944,  // shayzien
    5946,  // lovakengj south
    5691,  // lovekengj north
    4922,  // farming guild
    4919,  // chambers of xeric
    5938,  // quetzacalli
    6448,  // varlamore west
    6960,  // varlamore east
    6191,  // hunter guild
    5421,  // aldarin
    5420,  // mistrock
    14638, // mos'le harmless
    14642, // tob + ver sinhaza
    14646, // port phasmatys
    12344, // ferox enclave
    12895, // priff north
    13150, // priff south
    13907, // museum camp
    14908, // fossil bank chest island
    10290, // kandarin monastery (ardy cloak)
};

@Override
public int[] regionsToPrioritise() {
    return BANK_REGIONS;
}
```

### Standard Bank Query (ALWAYS USE)
```java
public static final String[] BANK_NAMES = {"Bank", "Chest", "Bank booth", "Bank chest", "Grand Exchange booth", "Bank counter", "Bank table"};
public static final String[] BANK_ACTIONS = {"bank", "open", "use"};
public static final Predicate<RSObject> BANK_QUERY = obj ->
        obj.getName() != null && obj.getActions() != null &&
        Arrays.stream(BANK_NAMES).anyMatch(name -> name.equalsIgnoreCase(obj.getName())) &&
        Arrays.stream(obj.getActions()).anyMatch(action ->
            Arrays.stream(BANK_ACTIONS).anyMatch(bankAction -> bankAction.equalsIgnoreCase(action))) &&
        obj.canReach();
```

### Task Interface Template
```java
package utils;

import com.osmb.api.script.Script;

public abstract class Task {
    protected Script script;

    public Task(Script script) {
        this.script = script;
    }

    public abstract boolean activate();
    public abstract boolean execute();
}
```

### Humanization Patterns (ALWAYS USE)

**Random delays between actions:**
```java
// standard delay (200-400ms) - use between most actions
script.submitTask(() -> false, script.random(200, 400));

// longer delay (300-600ms) - use after bank operations
script.submitTask(() -> false, script.random(300, 600));

// human-style delay with variance logging
script.pollFramesHuman(() -> false, script.random(200, 400));
```

**Variable delays with occasional longer pauses:**
```java
// normal 2-3 tick delay, 10% chance of extra pause
int delay = script.random(1200, 1800);
if (script.random(10) == 0) {
    delay += script.random(600, 1200);  // occasional longer pause
}

// ~20% chance to use human delay (logs the ⏳ message)
if (script.random(5) == 0) {
    script.pollFramesHuman(() -> false, delay);
} else {
    script.submitTask(() -> false, delay);
}
```

### Caching Patterns (USE WHEN POSSIBLE)

**Cache inventory slots to avoid re-scanning:**
```java
// cache slots once, process all without re-scanning
ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(ItemID.GOLD_ORE));
if (inv == null || !inv.contains(ItemID.GOLD_ORE)) return false;

List<Integer> slots = new ArrayList<>(inv.getSlotsForItem(ItemID.GOLD_ORE));
script.log(getClass(), "cached " + slots.size() + " slots");

for (int slot : slots) {
    var boundsResult = script.getWidgetManager().getInventory().getBoundsForSlot(slot);
    if (boundsResult == null || !boundsResult.isFound()) continue;

    Rectangle bounds = boundsResult.get();
    script.getFinger().tap(bounds);
    // ... process
}
```

**Cache bank search result:**
```java
// search once, use multiple times
ItemGroupResult bank = script.getWidgetManager().getBank().search(Set.of(ItemID.GOLD_ORE, ItemID.NATURE_RUNE));
if (bank == null) return false;

int oreCount = bank.getAmount(ItemID.GOLD_ORE);
int runeCount = bank.getAmount(ItemID.NATURE_RUNE);
```

### AFK Control Pattern
```java
// in main script class
public static boolean allowAFK = true;

@Override
public boolean canAFK() {
    return allowAFK;
}

// in task when doing critical actions
TidalsScriptName.allowAFK = false;  // prevent afk during bloom/cast
// ... do action ...
TidalsScriptName.allowAFK = true;   // re-enable after
```

### Level Up Handling (ALWAYS CHECK)
```java
// check for level up dialogue after xp-granting actions
DialogueType type = script.getWidgetManager().getDialogue().getDialogueType();
if (type == DialogueType.TAP_HERE_TO_CONTINUE) {
    script.log(getClass(), "level up");
    script.getWidgetManager().getDialogue().continueChatDialogue();
    script.submitTask(() ->
        script.getWidgetManager().getDialogue().getDialogueType() != DialogueType.TAP_HERE_TO_CONTINUE,
        2000);
}
```

### Secrets.java Template (gitignored)
```java
package obf;

public class Secrets {
    public static final String STATS_URL = "https://your-dashboard.com/api/stats";
    public static final String STATS_API = "your-api-key";
}
```

### Version Checking (ALWAYS INCLUDE)
Every script MUST include automatic version checking that runs on startup. This fetches the script's source from GitHub and compares versions, notifying users if they're running an outdated version.

**GitHub URL Pattern:**
```
https://raw.githubusercontent.com/Manokit/tidals-scripts/main/{ScriptName}/src/main/java/main/{ScriptName}.java
```

**CRITICAL:** The `@ScriptDefinition` annotation `version` and the `SCRIPT_VERSION` constant MUST match. The version check parses the annotation from GitHub.

**Add these three methods to your main script class:**
```java
// version checking
public String getLatestVersion(String urlString) {
    try {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("version")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        return parts[1].replace(",", "").trim();
                    }
                }
            }
        }
    } catch (Exception e) {
        log("VERSIONCHECK", "Exception occurred while fetching version from GitHub.");
    }
    return null;
}

public static int compareVersions(String v1, String v2) {
    String[] parts1 = v1.split("\\.");
    String[] parts2 = v2.split("\\.");

    int length = Math.max(parts1.length, parts2.length);
    for (int i = 0; i < length; i++) {
        int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
        int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
        if (num1 < num2) return -1;
        if (num1 > num2) return 1;
    }
    return 0;
}

private boolean checkForUpdates() {
    String latest = getLatestVersion("https://raw.githubusercontent.com/Manokit/tidals-scripts/main/TidalsScriptName/src/main/java/main/TidalsScriptName.java");

    if (latest == null) {
        log("VERSION", "Could not fetch latest version info.");
        return false;
    }

    if (compareVersions(SCRIPT_VERSION, latest) < 0) {
        for (int i = 0; i < 10; i++) {
            log("VERSION", "New version v" + latest + " found! Please update the script before running it again.");
        }
        return true;
    }

    log("VERSION", "You are running the latest version (v" + SCRIPT_VERSION + ").");
    return false;
}
```

**Call in onStart() immediately after the startup log:**
```java
@Override
public void onStart() {
    log(getClass(), "Starting " + SCRIPT_NAME + " v" + SCRIPT_VERSION);

    if (checkForUpdates()) {
        stop();
        return;
    }

    // ... rest of onStart
}
```

**Required imports:**
```java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
```

**Behavior:**
- If GitHub is unreachable: logs warning, script continues (doesn't block users)
- If outdated: logs 10 warning lines, stops script
- If current: logs confirmation, script continues

---

## 🛠️ Shared Utilities (TidalsUtilities.jar)

**ALWAYS USE THESE** instead of writing custom retry logic. Located in `utilities/` directory.

### Setup
Add to your script's `build.gradle`:
```gradle
dependencies {
    implementation files('../utilities/jar/TidalsUtilities.jar')
}
```

### RetryUtils - Menu Interaction Retries
```java
import utilities.RetryUtils;

// equipment interactions (teleports, etc.)
RetryUtils.equipmentInteract(script, ItemID.CRAFTING_CAPE, "Teleport", "crafting cape teleport");

// object interactions (banks, altars, etc.)
RetryUtils.objectInteract(script, bankChest, "Use", "bank chest");

// polygon tap interactions (ground items, tile objects, etc.)
RetryUtils.tap(script, fungusPolygon, "Pick", "fungus");

// inventory item interactions (eating, using items, etc.)
RetryUtils.inventoryInteract(script, item, "Eat", "food");

// custom attempt count (default is 10)
RetryUtils.equipmentInteract(script, itemId, "Teleport", "ring teleport", 5);
```

**Benefits**:
- 10 retry attempts by default (configurable)
- Logs each attempt as "description attempt X/10"
- 300-500ms random delay between attempts
- Consistent error handling across all scripts

### BankingUtils - Banking Operations
```java
import utilities.BankingUtils;

// find nearest bank using standard query
RSObject bank = BankingUtils.findNearestBank(script);

// open bank and wait (with movement tracking)
BankingUtils.openBankAndWait(script, 15000);  // finds nearest bank
BankingUtils.openBankAndWait(script, bank, 15000);  // specific bank

// deposit all except specified items
BankingUtils.depositAllExcept(script, Set.of(ItemID.CHISEL));

// close bank and wait
BankingUtils.closeBankAndWait(script, 5000);

// use the shared bank query predicate
List<RSObject> banks = script.getObjectManager().getObjects(BankingUtils.BANK_QUERY);
```

**For withdrawals, use BankSearchUtils instead** - see BankSearchUtils section below.

**Benefits**:
- Shared `BANK_QUERY` predicate (no more duplicate code)
- Movement tracking - waits until player stops moving
- Human-like delays built into deposit/withdraw
- Null-safe with proper logging

### TabUtils - Tab Opening with Verification
```java
import utilities.TabUtils;

// open inventory and verify accessible
TabUtils.openAndVerifyInventory(script, 3000);  // with timeout
TabUtils.openAndVerifyInventory(script);  // default 3000ms

// open equipment tab (uses delay since no isVisible check)
TabUtils.openAndWaitEquipment(script);  // default 200-400ms
TabUtils.openAndWaitEquipment(script, 500);  // custom delay

// open equipment and verify specific item equipped
TabUtils.openAndVerifyEquipment(script, new int[]{ItemID.CRAFTING_CAPE}, 3000);

// open skills tab
TabUtils.openAndVerifySkills(script, 3000);
TabUtils.openAndVerifySkills(script);  // default 3000ms

// generic tab opening with wait
TabUtils.openTabAndWait(script, Tab.Type.COMBAT, 300);
```

**Benefits**:
- Verifies tab contents are accessible before returning
- Eliminates duplicate tab opening patterns
- Human-like delays

### DialogueUtils - Dialogue Handling
```java
import utilities.DialogueUtils;

// level up handling
if (DialogueUtils.isLevelUp(script)) {
    DialogueUtils.handleLevelUp(script);  // clicks continue, waits for close
}

// wait for dialogues
DialogueUtils.waitForDialogue(script, DialogueType.ITEM_OPTION, 5000);
DialogueUtils.waitForItemDialogue(script, 5000);  // shorthand

// item selection dialogue
DialogueUtils.selectItem(script, ItemID.GOLD_BAR);
DialogueUtils.selectItemWithRetry(script, ItemID.GOLD_BAR, 10);  // with retries

// continue chat dialogues
DialogueUtils.continueChatDialogue(script);
DialogueUtils.dismissContinueDialogue(script, 2000);  // click and wait

// check dialogue state
if (DialogueUtils.hasDialogue(script)) {
    DialogueType type = DialogueUtils.getDialogueType(script);
}
```

**Benefits**:
- Handles level ups cleanly
- Retry support for item selection
- Consistent dialogue state checking

### BankSearchUtils - Bank Search & Withdraw (CRITICAL)
**ALWAYS USE THIS** for withdrawing items from the bank. The raw `bank.search()` and `bank.withdraw()` APIs only see currently visible items and DO NOT work reliably.

```java
import utilities.BankSearchUtils;

// withdraw single item (types name in search box, finds visually, verifies)
BankSearchUtils.searchAndWithdrawVerified(script, ItemID.SHARK, 5, true);

// withdraw all of a stack
BankSearchUtils.searchAndWithdrawVerified(script, ItemID.DEATH_RUNE, 0, true);  // 0 = all

// CRITICAL: after EVERY successful withdrawal, reset the search for the next item
BankSearchUtils.clickSearchToReset(script);

// when done with all withdrawals, clear the search completely
BankSearchUtils.clearSearch(script);
```

**CRITICAL PATTERN - Multi-item withdrawal loop:**
```java
// correct pattern for withdrawing multiple items
for (int itemId : itemsToWithdraw) {
    boolean success = BankSearchUtils.searchAndWithdrawVerified(script, itemId, amount, true);
    if (success) {
        // MUST reset search after each successful withdrawal
        BankSearchUtils.clickSearchToReset(script);
    }
}
// clear search when done
BankSearchUtils.clearSearch(script);
```

**Why this matters:**
- `bank.search(Set.of(itemId))` only searches VISIBLE items (no scrolling, no search box)
- `BankSearchUtils` types the item name in the search box, uses sprite detection, verifies inventory
- After withdrawal, the search box stays open with old text - MUST click search button to reset
- `clickSearchToReset()` clears current search and reopens fresh search input
- `clearSearch()` closes search entirely (use when done with all withdrawals)

**Benefits**:
- Types item name in bank search box
- Uses sprite detection to find items visually
- Scrolls through bank if needed
- Verifies withdrawal succeeded via inventory count
- Works regardless of bank tab or scroll position

### Building Utilities
```bash
cd tidals-scripts && JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle :utilities:build
```

---

## 🔨 Building Scripts

Use the `osmb build` command to build scripts. This command handles Java environment setup and deployment automatically.

### Build Commands
```bash
# build a specific script by name (recommended for Claude Code)
osmb build TidalsGemCutter
osmb build TidalsSecondaryCollector

# build all scripts at once
osmb build all

# interactive menu (for manual use)
osmb build
```

### Available Scripts
Run `osmb list` to see all buildable scripts:
- TidalsCannonballThiever
- TidalsGemCutter
- TidalsGoldSuperheater
- TidalsSecondaryCollector

**When to build:**
- After making changes to a script's source code
- After updating TidalsUtilities.jar (rebuild dependent scripts)
- Before testing changes in the OSMB client

**Build output location:** `<script-dir>/jar/<ScriptName>.jar`

---

## 📚 Documentation Index

**Core References:**
- `docs/api-reference.md` - Complete API methods and imports
- `docs/critical-concepts.md` - Color bot fundamentals (MUST READ)
- `docs/Common-menu-entries.md` - Exact menu action strings for .interact() calls
- `docs/banking-patterns.md` - Banking, inventory, deposits
- `docs/walking-npcs.md` - Walking, NPC interaction, objects
- `docs/ui-widgets.md` - Dialogue, equipment, minimap, overlays
- `docs/Paint.md` - **Paint overlay & Setup UI standard (MUST READ for any UI work)**
- `docs/paint-overlay.md` - Legacy paint reference (see Paint.md for current standard)
- `docs/common-mistakes.md` - Debugging guide, pitfalls to avoid
- `docs/advanced-patterns.md` - Production patterns from Davy's scripts
- `docs/advanced-techniques.md` - Ground items, agility, combat, health
- `docs/specialized-patterns.md` - Altars, minigames, processing, smelting
- `docs/Walker.md` - Getting to where you need to be - **CRITICAL TO READ BEFORE WRITING ANY WALKING CODE, ESPECIALLY PITFALLS**
- `docs/Reporting-data.md` - Stats reporting to dashboard (API format, implementation guide)

**Examples:**
- `examples/gem-cutting.md` - Gem cutting specifics
- `examples/discord-post.md` - Discord post template for script releases

## Discord Posts

Every script should have a `discord_post.md` in its root directory for sharing on Discord.

**Guidelines:**
- Create the main post on initial release with all standard sections
- Update the main post when new features are added
- Version updates (v1.1, v1.2, etc.) are added ONLY when explicitly requested by the user
- See `examples/discord-post.md` for the full template

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

## Critical Concepts

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

### 3. Visual Door Detection (Collision Map is Static!)
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

### 4. Use Direct tap() for Interactions - Avoid Double-Tap Bug
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

### 5. Screen Edge Bounds - tap() Fails Near Edges
```java
// WRONG - pixel cluster at y=693 with 30x30 click area goes off 700px screen
Point clickPoint = new Point(x, 693);
Rectangle clickArea = new Rectangle(clickPoint.x - 15, clickPoint.y - 15, 30, 30);
script.getFinger().tap(clickArea, "Inflate");  // ArrayIndexOutOfBoundsException!

// CORRECT - filter out points too close to screen edges
private static final int SCREEN_EDGE_MARGIN = 25;

if (clickPoint.x < SCREEN_EDGE_MARGIN || clickPoint.y < SCREEN_EDGE_MARGIN ||
    clickPoint.x > 750 - SCREEN_EDGE_MARGIN || clickPoint.y > 700 - SCREEN_EDGE_MARGIN) {
    continue;  // skip this target
}
```
**Rule: When using pixel clusters or screen coordinates, always validate bounds before tapping. Screen is approximately 750x700.**

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

### 8. Menu Interaction Retry Pattern (ALWAYS USE THIS)
**CRITICAL**: Menu interactions can fail due to timing, camera angle, or misclicks. ALWAYS retry menu interactions up to 10 times unless speed is explicitly critical.

```java
// standard menu interaction with retries
private boolean interactWithRetry(Polygon poly, String action, String description) {
    int maxAttempts = 10;
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
        script.log(getClass(), description + " attempt " + attempt + "/" + maxAttempts);

        boolean success = script.getFinger().tap(poly, action);
        if (success) {
            return true;
        }

        script.pollFramesUntil(() -> false, script.random(300, 500), true);
    }
    script.log(getClass(), description + " failed after " + maxAttempts + " attempts");
    return false;
}

// for equipment interactions
private boolean equipmentInteractWithRetry(int itemId, String action, String description) {
    int maxAttempts = 10;
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
        script.log(getClass(), description + " attempt " + attempt + "/" + maxAttempts);

        boolean success = script.getWidgetManager().getEquipment().interact(itemId, action);
        if (success) {
            return true;
        }

        script.pollFramesUntil(() -> false, script.random(300, 500), true);
    }
    script.log(getClass(), description + " failed after " + maxAttempts + " attempts");
    return false;
}

// for object interactions
private boolean objectInteractWithRetry(RSObject obj, String action, String description) {
    int maxAttempts = 10;
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
        script.log(getClass(), description + " attempt " + attempt + "/" + maxAttempts);

        boolean success = obj.interact(action);
        if (success) {
            return true;
        }

        script.pollFramesUntil(() -> false, script.random(300, 500), true);
    }
    script.log(getClass(), description + " failed after " + maxAttempts + " attempts");
    return false;
}
```

**Rules**:
- Default to 10 retry attempts for all menu interactions
- Log each attempt as "description attempt X/10"
- Only reduce retries if speed is explicitly mentioned as critical
- Add brief delay between attempts (300-500ms)

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

**Paint Overlay / Setup UI?** → `docs/Paint.md`
- Standard color palette and layout
- Paint overlay template
- Setup UI (JavaFX) template
- Logo loading and rendering

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
- **Paint looks amateur** → See `docs/Paint.md` for standard template
- **Bank withdrawing 0 items** → See `examples/gem-cutting.md` for timing fix
- **Items not detected** → See `docs/critical-concepts.md` for HSL/RGB
- **Complex travel** → See `docs/advanced-patterns.md` for multi-step sequences
- **Verify action succeeded** → See `docs/advanced-patterns.md` for inventory count verification
- **Tap objects from distance** → See `docs/advanced-patterns.md` for direct tile interaction
- **Prevent AFK during cycle** → See `docs/advanced-patterns.md` for full cycle AFK prevention

## Resources

- **API Documentation**: https://doc.osmb.co.uk
- **Javadocs**: https://osmb.co.uk/javadocs (ItemID constants)
- **Debug Tool**: Built into OSMB client
- **Discord**: Developer knowledge base

---

*Remember: Think visually, verify interactions, and be creative when the API can't distinguish items directly!*
