# OSMB Script Development Guide

> **CRITICAL: OSMB is a COLOR BOT** - Visual/pixel detection only. No memory access. All detection via screen analysis, color matching, and OCR.

> **NEVER ASSUME A METHOD EXISTS.** Always verify against `docs/`, `examples/`, or existing Tidals scripts.

---

## Core Principles

```
WHEN USER POSTS LOGS → THERE IS A BUG. INVESTIGATE.
READ THE STACK TRACE. FIX ROOT CAUSE, NOT SYMPTOM.
DON'T REINVENT. Read existing code before writing new.
DON'T OVERENGINEER. No abstractions before they're needed.
WHEN YOU'RE DONE MAKING CHANGES, ALWAYS BUILD THE SCRIPT.
ALWAYS MAKE A DISCORD_POST.MD (see examples/discord-post.md).
```
## If you need API Validation
You have access to an MCP docs server with full chunks of the API you can call on if needed

## Git Commits
Do not add Co-Authored-By trailers to commit messages.

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
- Use `RandomUtils.weightedRandom()` for short delays (weighted toward faster)
- Use `RandomUtils.gaussianRandom()` for animation waits (clusters around mean)
- Fixed delay: `pollFramesUntil(() -> false, ms)` - waits full timeout
- Humanized delay: `pollFramesHuman(() -> true, ms)` - instant + human variance

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

**Bank withdrawal pattern:**
```java
BankSearchUtils.searchAndWithdrawVerified(script, itemId, amount, true);
BankSearchUtils.clickSearchToReset(script);  // MUST reset after each withdrawal
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
5. **Screen edges cause crashes** - validate bounds (25px margin) before tapping
6. **Menu interactions fail** - always use RetryUtils (10 attempts)

---

## Documentation Index

**Must Read:**
- `docs/critical-concepts.md` - Color bot fundamentals
- `docs/common-mistakes.md` - Debugging guide, pitfalls
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

## Resources

- **API Docs**: https://doc.osmb.co.uk/documentation
- **Debug Tool**: Built into OSMB client

---

*Think visually. Verify interactions. Read existing scripts before writing new code.*
