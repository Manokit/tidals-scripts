# OSMB Script Development Guide

> **CRITICAL: OSMB is a COLOR BOT** - Visual/pixel detection only. No memory access. All detection via screen analysis, color matching, and OCR.

> **NO VARPS/VARBITS** - You cannot read varps, varbits, or any game memory values. Quest progress, skill levels, and game state must be detected visually (quest journal tab, skill tab OCR, dialogue text, inventory contents, etc.). Never write code that calls getVarp(), getVarbit(), or similar memory-access methods.

> **NEVER ASSUME A METHOD EXISTS.** Always verify against `docs/`, `examples/`, or existing Tidals scripts.

**IMPORTANT**: before you do anything else, run the `beans prime` command and heed its output.

After a change with a successful build, always add your changes to bean you're working on, or make a new bean so we can know of recent changes if it breaks anything

When you mark a Bean complete, let me know what bean is best to go next

---

## Core Principles

```
WHEN USER POSTS LOGS -> THERE IS A BUG. INVESTIGATE.
READ THE STACK TRACE. FIX ROOT CAUSE, NOT SYMPTOM.
DON'T REINVENT. Read existing code before writing new.
DON'T OVERENGINEER. No abstractions before they're needed.
POLL-BASED: One action per poll. Check condition, handle it, return. Don't chain actions.
WHEN YOU'RE DONE MAKING CHANGES, ALWAYS BUILD THE SCRIPT.
ALWAYS MAKE A DISCORD_POST.MD (see examples/discord-post.md).
BEFORE NEW FEATURES OR BIG CHANGES -> READ docs/common-mistakes.md FIRST.
```
This environment has both ripgrep and ast-grep for searching for docs, feel free to use it

## Git Commits
- **ONLY commit when explicitly asked.** Do not auto-commit after making changes.
- Do not add Co-Authored-By trailers to commit messages.
- Do not commit `Changes/` directories, `docs/plans/`, or other planning files - these are gitignored and local-only.

## Changelog Management

**Every script should have a `Changes/` directory** for tracking modifications.

When making changes to a script:
1. Check if `<ScriptName>/Changes/` directory exists - create it if not
2. Check if today's changelog exists: `<ScriptName>-MM-DD-Changes.md`
3. If it doesn't exist, create it with the header format below
4. Append new changes to the existing file if it already exists

**Changelog format:**
```markdown
# TidalsScriptName Changes - YYYY-MM-DD

## Summary
Brief 1-2 sentence overview of all changes made today.

---

## 1. Change Title

**File:** `path/to/file.java` (lines XX-YY)

**Problem:** What was broken or missing.

**Fix:** What was changed and why.

```java
// relevant code snippet
```

---

## 2. Next Change...
```

**Key points:**
- Include file paths and line numbers for easy navigation
- Explain the problem AND the fix
- Add code snippets for complex changes
- Number each change sequentially
- End with version bump reminder if applicable

See `TidalsChompyHunter/Changes/` or `TidalsGemMiner/Changes/` for examples.

---

## Script Architecture

### Folder Structure
```
TidalsScriptName/
├── build.gradle
├── discord_post.md
├── Changes/                          # daily changelogs
│   └── ScriptName-MM-DD-Changes.md
├── src/main/java/
│   ├── main/
│   │   ├── TidalsScriptName.java    # main script, state, paint
│   │   └── ScriptUI.java            # javafx setup ui (optional)
│   ├── tasks/
│   │   ├── Setup.java               # initial validation
│   │   └── [ActivityTasks].java     # one task per activity
│   ├── utils/
│   │   └── Task.java                # task interface
│   └── obf/
│       └── Secrets.java             # api keys (gitignored)
└── src/main/resources/
    └── logo.png                     # 208x91 png
```

### Priority-Based Task System

Tasks are evaluated in priority order. First task whose `activate()` returns true runs exclusively.

**Task ordering matters:**
1. High-priority interrupts (crash detection, world hop)
2. Setup/validation
3. Combat/time-sensitive actions
4. Resource management (banking, restocking)
5. Main activity (lowest priority)

**Key patterns:**
- `activate()` - Can this task run right now? Check preconditions only.
- `execute()` - Do the work. Include interrupt checks in long loops.
- Use global static fields in main script for cross-task state sharing.
- Use `volatile` boolean flags for event signaling between `onNewFrame()` and `poll()`.

### State Management

Main script holds shared state as static fields:
- `setupComplete`, `task` (current status), `startTime`
- Activity-specific counters and trackers
- Settings from UI (pluckingEnabled, webhookEnabled, etc.)
- Volatile flags for chat events (`killDetected`, `bellowsEmpty`)

Tasks read/write these directly - no parameter passing through method chains.

---

## Coding Standards

### Comments
Blunt, lowercase, explain "why" not "what":
```java
// block hop while toads drain - prevents invisible ground items
// cache clusters to avoid repeated expensive scans
// verify visually not via collision map
```

### Logging
Use prefix-based logging for flow tracking:
```java
script.log(getClass(), "[activate] skipping - already in combat");
script.log(getClass(), "[execute] starting fresh detection...");
script.log(getClass(), "[attack] attempt " + attempt + "/" + MAX_ATTEMPTS);
```

### Naming
- Constants: `MAX_ATTACK_ATTEMPTS`, `SCREEN_EDGE_MARGIN`
- Static fields: `setupComplete`, `killCount`, `droppedToadPositions`
- Methods: `findChompyByPixelCluster()`, `waitForKillConfirmation()`

### Null Checking
**Every** API call that can return null must be checked:
```java
if (result == null || result.isNotFound()) return;
```

### Delays & Timing
- **NEVER** use `script.random()` - produces uniform distribution (robotic)
- **NEVER** use `submitTask()` - deprecated, may not block properly
- **NEVER** use `pollFramesUntil(() -> true, ms)` - exits instantly with NO delay!
- Use `RandomUtils.weightedRandom()` for short delays (weighted toward faster)
- Use `RandomUtils.gaussianRandom()` for animation waits (clusters around mean)
- Fixed delay: `pollFramesUntil(() -> false, ms)` - waits full timeout
- Humanized delay: `pollFramesHuman(() -> true, ms)` - adds ~500-1000ms human variance

### Error Handling
- Catch `RuntimeException`, not `Exception` - let OSMB control flow bubble up
- Use try-finally for guaranteed state cleanup in long operations
- Fail fast with sensible defaults, don't swallow errors

### Interrupt Handling
In long loops, check for interrupts periodically:
```java
if (CrashDetection.crashDetected) return false;  // yield to higher priority
if (hasHigherPriorityWork()) return true;        // switch tasks
```

### Performance
- Cache expensive operations (pixel scans) with TTL
- Use bit-packed position keys instead of string concatenation
- Validate screen bounds before tapping (25px margin from edges)
- Use `RectangleArea.contains()` for zone checks

---

## Required Elements

### Bank Regions
Every banking script MUST override `regionsToPrioritise()` with the standard 60+ region list. Copy from an existing script like TidalsGemCutter.

### Version Checking
Every script MUST include version checking in `onStart()`. See existing scripts for the `getLatestVersion()`, `compareVersions()`, and `checkForUpdates()` methods.

### AFK Control
Override `canAFK()` and toggle `allowAFK` static field during critical actions.

### Level Up Handling
Check for `DialogueType.TAP_HERE_TO_CONTINUE` after XP-granting actions.

---

## Shared Utilities (TidalsUtilities.jar)

**Always use these** instead of custom logic:

| Utility | Purpose |
|---------|---------|
| `RetryUtils` | Menu interactions with 10 retry attempts |
| `BankingUtils` | Bank opening, deposits, standard bank query |
| `BankSearchUtils` | **CRITICAL for withdrawals** - types in search box, verifies inventory |
| `TabUtils` | Tab opening with verification |
| `DialogueUtils` | Level ups, item selection, chat handling |
| `MovementChecker` | Detects when player stops moving (misclicks, interrupts) |

**Bank withdrawal pattern:**
```java
BankSearchUtils.searchAndWithdrawVerified(script, itemId, amount, true);
BankSearchUtils.clickSearchToReset(script);  // MUST reset after each withdrawal
```

**Movement timeout pattern:**
```java
// detect if player stopped moving (misclick, block, interrupt)
MovementChecker checker = new MovementChecker(script.getLocalPlayer().getWorldPosition());
while (walking) {
    if (checker.hasTimedOut(script.getLocalPlayer().getWorldPosition())) {
        break;  // player stalled - retry or take action
    }
}
```

Build utilities: `cd tidals-scripts && JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle :utilities:build`

---

## Building Scripts

```bash
osmb build TidalsScriptName    # build specific script
osmb build all                  # build all scripts
osmb list                       # see available scripts
```

Output: `<script-dir>/jar/<ScriptName>.jar`

---

## Critical Concepts

1. **NPCs use Minimap** - `getMinimap().getNPCPositions()`, not ObjectManager
2. **Identical sprites can't be distinguished** - use BuffOverlay for charges
3. **Collision map is static** - verify doors visually via menu response
4. **Direct tap() preferred** - `tap(shape, "Action")` is safer than tapGetResponse chains
5. **Screen edges cause crashes** - validate bounds (25px margin from edges)
6. **Menu interactions fail** - always use RetryUtils (10 attempts)
7. **Use tapGameScreen() for 3D objects** - `tap()` can click through UI overlays; use `tapGameScreen()` for rocks, trees, NPCs
8. **Visibility check required** - `getConvexHull() != null` does NOT mean visible; use `insideGameScreenFactor()` before clicking
9. **Randomize ALL timeouts** - No static delay values; re-randomize each use with `RandomUtils.weightedRandom(min, max)`
10. **pollFramesUntil(() -> true) exits immediately** - Returns in ~38ms with no delay! Use `() -> false` for fixed delays

---

## Production Patterns

These patterns are used across all production Tidals scripts. Follow them when writing new scripts.

### onNewFrame() Throttling

Every `onNewFrame()` that does expensive work (OCR, chat parsing, player detection) MUST be throttled:
```java
private static final long CHAT_PARSE_INTERVAL_MS = 500;
private long lastChatParseTime = 0;

@Override
public void onNewFrame() {
    long now = System.currentTimeMillis();
    if (now - lastChatParseTime >= CHAT_PARSE_INTERVAL_MS) {
        updateChatBoxLines();
        lastChatParseTime = now;
    }
}
```

### Volatile Guards for onNewFrame/Poll Race Conditions

When `onNewFrame()` detects events that your own actions could trigger, guard with `volatile` + try/finally:
```java
public static volatile boolean attackInProgress = false;

// In execute():
attackInProgress = true;
try {
    performAttack();
} finally {
    attackInProgress = false;
}

// In onNewFrame():
if (attackInProgress) return;  // skip detection during our action
```

### Chat Line Diff Pattern (Game Event Detection)

Detect game messages by diffing chat lines between frames:
```java
private List<String> previousChatLines = new ArrayList<>();

private void updateChatBoxLines() {
    UIResultList<String> chatResult = getWidgetManager().getChatbox().getText();
    if (chatResult == null || chatResult.isNotFound()) return;

    List<String> currentLines = chatResult.asList();
    List<String> newLines = new ArrayList<>();
    for (String line : currentLines) {
        if (!previousChatLines.contains(line)) newLines.add(line);
    }

    for (String line : newLines) {
        if (line.contains("scratch a notch")) { killDetected = true; }
        if (line.contains("air seems too thin")) { bellowsEmpty = true; }
    }
    previousChatLines = new ArrayList<>(currentLines);
}
```
Call this from throttled `onNewFrame()`, signal to tasks via `volatile` flags.

### Resumable State Flag (Multi-Step Sequences)

When a task has a multi-step sequence (e.g., deposit run), use a static boolean to keep `activate()` returning true mid-sequence:
```java
public static boolean doingDepositRun = false;

@Override
public boolean activate() {
    if (doingDepositRun) return true;  // resume interrupted sequence
    return inventory.isFull();         // normal activation
}

@Override
public boolean execute() {
    doingDepositRun = true;
    // ... multi-state logic ...
    if (sequenceComplete) { doingDepositRun = false; }
    return true;
}
```

### canHopWorlds() / canBreak() / canAFK() Overrides

Every script with multi-phase gameplay MUST override these. Block hops during critical actions, allow during safe states:
```java
@Override
public boolean canHopWorlds() {
    if (doingDepositRun) return true;     // safe during deposit
    if (!currentlyThieving) return true;  // safe when idle
    if (isAtSafetyTile()) return true;    // safe at safety tile
    return false;                          // block during active gameplay
}
```

### WalkConfig Patterns

Use `breakCondition` to stop walking when the target becomes visible/reachable:
```java
// Approach walk: stop when target is on screen
script.getWalker().walkTo(target, new WalkConfig.Builder()
    .breakCondition(() -> {
        RSObject obj = script.getObjectManager().getClosestObject(myPos, "Bank booth");
        return obj != null && obj.isInteractableOnScreen();
    })
    .build());
```

### Verbose Logging Toggle

Every script should have a debug toggle (set via ScriptUI):
```java
public static volatile boolean verboseLogging = false;

// Gate expensive debug logging:
if (verboseLogging) {
    script.log(getClass(), "[DEBUG] state: " + state + " pos: " + pos);
}
```

### Session ID and Incremental Stats

Stats are sent incrementally using "last sent" tracking:
```java
private String sessionId = UUID.randomUUID().toString();
private int lastSentXp = 0;

// In stats reporting (every 10 min):
int xpIncrement = xpGained - lastSentXp;
sendStats(sessionId, xpIncrement);
lastSentXp = xpGained;
```

---

## Documentation Index

**Must Read BEFORE Writing Code:**
- `docs/critical-concepts.md` - Color bot fundamentals
- `docs/common-mistakes.md` - **READ THIS FIRST** for new features/major changes. Contains 27 documented pitfalls with correct patterns.
- `docs/poll-based-architecture.md` - One action per poll pattern; state machine design
- `docs/interaction-patterns.md` - tap vs tapGameScreen, visibility checking, MovementChecker
- `docs/Walker.md` - Walking code pitfalls
- `docs/Paint.md` - Paint overlay & Setup UI standard

**Reference:**
- `docs/api-reference.md` - Complete API methods
- `docs/Common-menu-entries.md` - Exact menu action strings
- `docs/banking-patterns.md` - Banking, inventory, deposits
- `docs/walking-npcs.md` - Walking, NPC interaction, objects
- `docs/ui-widgets.md` - Dialogue, equipment, minimap
- `docs/Reporting-data.md` - Stats reporting to dashboard

**Advanced:**
- `docs/advanced-patterns.md` - Production patterns
- `docs/advanced-techniques.md` - Ground items, combat, health
- `docs/specialized-patterns.md` - Altars, minigames, processing

**Examples:**
- `examples/discord-post.md` - Discord post template
- Existing scripts (TidalsChompyHunter, TidalsGemCutter, etc.)

---

## MCP Map Data Tools

The OSRS MCP server includes map/cache data tools for looking up spawn locations, menu actions, teleports, and transports. **Use these instead of guessing or asking the user.**

### When to Use

| Need | Tool |
|------|------|
| Region ID for `regionsToPrioritise()` | `osrs_coords_to_region(x, y)` |
| Exact menu action string for RetryUtils | `osrs_object_actions(name)` or `osrs_npc_actions(name)` |
| Item inventory/ground actions (Eat, Wield, Break, etc.) | `osrs_item_actions(name)` |
| Where an NPC/object spawns in the world | `osrs_npc_spawns(name)` or `osrs_object_spawns(name)` |
| Finding nearby stairs, ladders, portals | `osrs_transports(near_x, near_y, radius)` |
| Teleport destination coordinates | `osrs_teleports(query)` |

### Tool Reference

```
osrs_coords_to_region(x, y)
  -> { regionId, regionX, regionY }
  Formula: (x >> 6) << 8 | (y >> 6)

osrs_item_actions(name OR id)
  -> [{ id, name, inventoryActions: ["Eat", "Drop"], groundActions: ["Take"] }]
  Data from OSRS cache (16k+ items). Refresh: node mcp-osrs/scripts/dump-item-actions.mjs

osrs_object_spawns(name OR id, limit=50)
  -> [{ id, name, x, y, plane, regionId }]

osrs_npc_spawns(name OR id, limit=50)
  -> [{ id, name, x, y, plane, regionId, combatLevel, actions }]

osrs_object_actions(name OR id)
  -> [{ id, name, actions: ["Bank", "Collect", ...] }]

osrs_npc_actions(name OR id)
  -> [{ id, name, actions: ["Talk-to", "Bank", ...], combatLevel }]

osrs_teleports(query?, near_x?, near_y?, radius=50)
  -> [{ id, category, menuOption, menuTarget, destinations, destinationCount }]

osrs_transports(query?, near_x?, near_y?, radius=50)
  -> [{ id, category, menuOption, menuTarget, start, destinations, destinationCount }]
```

### Keeping Data Fresh

After a game update, run these MCP tools to refresh cached data:

| Tool | What it refreshes | When to run |
|------|-------------------|-------------|
| `update_item_actions` | Item inventory/ground actions (16k+ items) | After OSRS game update (RuneLite auto-downloads new cache) |
| `update_osrsbox_data` | Monster drop tables | After osrsreboxed-db GitHub repo updates |
| `update_soundids_from_wiki` | Sound effect IDs | After wiki editors update |
| `check_map_data_updates` | Checks if map data fork is behind upstream | Periodically - if behind, user must `git pull` in map-mcp |

### Examples

```
# Get region ID for Lumbridge to add to regionsToPrioritise()
osrs_coords_to_region(3222, 3218) -> regionId: 12850

# Verify the exact menu action string for a bank booth
osrs_object_actions("Bank booth") -> actions: ["Bank", "Collect"]

# Check if a teleport tab uses "Break" or "Teleport"
osrs_item_actions("Varrock teleport") -> inventoryActions: ["Break", "Varrock", "Grand Exchange", "Toggle", "Drop"]

# Find where Rock Crabs spawn
osrs_npc_spawns("Rock Crab") -> spawns at (2707, 3712) region 10554, combat 13

# Find stairs/ladders near a location
osrs_transports(near_x=3222, near_y=3218, radius=50) -> Lumbridge castle stairs, cellar trapdoor, etc.
```

---

## Examples Directory Warning

The `examples/` directory contains older reference scripts from various authors. These may contain anti-patterns that should **NOT** be copied:

- **Broad `catch (Exception e)` blocks** - Swallows OSMB control flow exceptions (`HaltScriptException`, `PriorityTaskException`). Use `catch (RuntimeException e)` and rethrow OSMB exceptions, or catch specific exceptions like `IOException`.
- **`pollFramesHuman(() -> false, ...)`** for pure delays - When lambda returns `false`, OSMB adds the timeout to the humanization delay. For pure delays (no condition to check), return `true` for instant completion + human variance.
- **Missing null checks on `ItemSearchResult`** before `.interact()` - `getItem()` and `getRandomItem()` can return null. Always check before calling methods.
- **Manual `openTab()` before `search()` calls** - `ItemGroup::search()` automatically opens the required tab. Explicit `openTab()` is redundant.
- **`Math.random()` or `script.random()`** - Produces uniform distribution (robotic). Use `RandomUtils.weightedRandom()` or `RandomUtils.gaussianRandom()` instead.

**Always prefer patterns from production Tidals scripts** (TidalsChompyHunter, TidalsGemCutter, TidalsGemMiner, etc.) or the patterns documented above.

---

## Resources

- **API Docs**: https://doc.osmb.co.uk/documentation
- **Debug Tool**: Built into OSMB client

---

*Think visually. Verify interactions. Read existing scripts before writing new code.*

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Structure

This is a monorepo containing two projects:

- **tidals-scripts/** - OSMB automation scripts (Java/Gradle)
- **script-dashboard/** - Statistics dashboard (Next.js/TypeScript)

> **Full documentation**: See `tidals-scripts/CLAUDE.md` for comprehensive script development guide with templates, patterns, and examples.

## Development Mindset

```
WHEN USER POSTS LOGS -> THERE IS A BUG. INVESTIGATE.
READ THE STACK TRACE. FIX ROOT CAUSE, NOT SYMPTOM.
DON'T REINVENT. Read existing code before writing new.
WHEN YOU'RE DONE MAKING CHANGES, ALWAYS BUILD THE SCRIPT.
```

## tidals-scripts (OSMB Scripts)

> **CRITICAL: OSMB is a COLOR BOT** - Uses visual/pixel detection, NOT injection. All detection is done through screen analysis, color matching, and OCR. You cannot access game memory directly.

**CRITICAL: Never assume a method exists. Always refer to `docs/` files or existing scripts for API verification.**

### Building Scripts

```bash
# Build a specific script
osmb build TidalsGemCutter

# Build all scripts
osmb build all

# Build utilities jar
cd tidals-scripts && JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle :utilities:build
```

Build output: `<script-dir>/jar/<ScriptName>.jar`

### Shared Utilities (TidalsUtilities.jar)

Always use utility classes instead of custom retry/banking logic:

```java
// Add to build.gradle
implementation files('../utilities/jar/TidalsUtilities.jar')

// RetryUtils - menu interactions with 10 retry attempts
RetryUtils.equipmentInteract(script, itemId, "Teleport", "crafting cape");
RetryUtils.objectInteract(script, bankChest, "Use", "bank chest");
RetryUtils.tap(script, polygon, "Pick", "fungus");
RetryUtils.inventoryInteract(script, item, "Eat", "food");

// BankSearchUtils - CRITICAL for withdrawals (raw bank APIs don't work reliably)
BankSearchUtils.searchAndWithdrawVerified(script, ItemID.SHARK, 5, true);
BankSearchUtils.clickSearchToReset(script);  // MUST reset after each withdrawal
BankSearchUtils.clearSearch(script);          // clear when done

// BankingUtils - bank operations
BankingUtils.openBankAndWait(script, 15000);
BankingUtils.depositAllExcept(script, Set.of(ItemID.CHISEL));

// TabUtils, DialogueUtils - see tidals-scripts/CLAUDE.md for full API
```

### Key Documentation

- `docs/api-reference.md` - Complete API methods
- `docs/critical-concepts.md` - Color bot fundamentals (MUST READ)
- `docs/Common-menu-entries.md` - Exact menu action strings
- `docs/Walker.md` - Walking code (MUST READ before any walking code)
- `docs/Paint.md` - Paint overlay & Setup UI standard
- `docs/Reporting-data.md` - Stats reporting to dashboard

### Script Architecture

Scripts use state machine pattern with Task classes:
```
TidalsGemCutter/
├── src/main/java/
│   ├── main/
│   │   ├── TidalsGemCutter.java  # Main script (extends Script)
│   │   └── ScriptUI.java         # JavaFX configuration UI
│   ├── tasks/
│   │   ├── Setup.java            # Initial state validation
│   │   ├── Process.java          # Main activity logic
│   │   └── Bank.java             # Banking logic
│   ├── utils/
│   │   └── Task.java             # Task interface
│   └── obf/
│       └── Secrets.java          # API keys (gitignored)
└── discord_post.md               # Release post (use examples/discord-post.md template)
```

### Core API Access

```java
getWidgetManager()      // UI: Bank, Inventory, Dialogue, Tabs, Minimap
getObjectManager()      // RSObjects (trees, rocks, banks)
getSceneManager()       // NPCs, ground items, tiles
getWalker()             // Pathfinding
getFinger()             // Mouse/touch input
getPixelAnalyzer()      // Color/pixel detection
getOCR()                // Text recognition
```

### Critical Patterns

1. **NPCs use Minimap detection**, not ObjectManager
2. **Items with identical sprites cannot be distinguished** - use BuffOverlay for charges
3. **Collision map is static** - verify doors visually via menu response
4. **Direct tap() preferred** - `getFinger().tap(bounds, "Action")` is safer than tapGetResponse
5. **Always retry menu interactions** - use RetryUtils (10 attempts default)
6. **Humanize delays** - use `script.random(200, 400)` between actions, occasional longer pauses

### Standard Bank Regions (Required for Banking Scripts)

Every script using banking MUST override `regionsToPrioritise()` with the standard 60+ region list. See `tidals-scripts/CLAUDE.md` for the complete `BANK_REGIONS` array.

## script-dashboard (Next.js)

### Commands

```bash
cd script-dashboard

# Development
npm install
npx prisma migrate dev
npm run dev                    # http://localhost:3000

# Production
npm run build
npm run lint

# Database
npx prisma studio              # GUI for database
npx prisma db push             # Sync schema without migration

# Docker deployment
docker compose up -d
```

### Tech Stack

- Next.js 16 with App Router + React 19
- TypeScript 5
- Prisma + SQLite
- Tailwind CSS 4
- Recharts for charts

### Architecture

**Server Components**: Stats page uses async server components with direct Prisma queries.

**Client Components**: Interactive components in `src/components/` use `'use client'` directive.

**Prisma Singleton**: `src/lib/db.ts` prevents connection issues in development.

### Stats API

**POST /api/stats** - Receives incremental stats from scripts
- Requires `X-Stats-Key` header
- All numeric values must be incremental (not cumulative)
- Body: `{ script, session, gp, xp, runtime, metadata? }`

**GET /api/stats** - Fetch aggregated stats
- Query params: `days` (default: 7), `script` (optional filter)

### Environment Variables

| Variable | Description |
|----------|-------------|
| DATABASE_URL | SQLite path (e.g., `file:./dev.db`) |
| STATS_API_KEY | API key for script authentication |

### Deployment

**Coolify**: Push to GitHub, set `DATABASE_URL=file:/app/data/prod.db` and `STATS_API_KEY`.

**Docker**: `docker compose up -d` with `STATS_API_KEY` env var set.

## Integration

Scripts report stats to dashboard via HTTP POST every 10 minutes. Stats are incremental - each report contains only the values gained since the last report, which the dashboard aggregates.

Scripts use `obf/Secrets.java` (gitignored) for dashboard credentials:
```java
public class Secrets {
    public static final String STATS_URL = "https://your-dashboard.com/api/stats";
    public static final String STATS_API = "your-api-key";
}
```
