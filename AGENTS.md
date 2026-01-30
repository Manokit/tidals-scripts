# OSMB Scripts Repository - Agent Guide

> **CRITICAL: OSMB is a COLOR BOT** - Visual/pixel detection only. No memory access. All detection via screen analysis, color matching, and OCR.

> **NEVER ASSUME A METHOD EXISTS.** Always verify against `docs/`, `examples/`, or existing Tidals scripts.

---

## Project Overview

This is a collection of automation scripts for OSMB (Old School Mobile Bot) - a color-based botting framework for Old School RuneScape. The repository uses a Gradle multi-project structure where each script is a self-contained module.

**Key Characteristics:**
- **Language:** Java 17
- **Build System:** Gradle with ProGuard shrinking
- **Architecture:** Poll-based task system (NOT linear/procedural)
- **Detection:** Visual/pixel based - no memory injection
- **API:** OSMB v2 API (local JAR at `API/API.jar`)

---

## Project Structure

```
tidals-scripts/
├── API/
│   └── API.jar                    # OSMB API dependency (checked in)
├── utilities/                     # Shared library module
│   ├── src/main/java/utilities/   # Shared utilities (RetryUtils, BankingUtils, etc.)
│   ├── src/main/java/utilities/loadout/   # Loadout system
│   └── jar/TidalsUtilities.jar    # Compiled utilities JAR
├── docs/                          # Extensive API documentation
│   ├── critical-concepts.md       # Color bot fundamentals
│   ├── common-mistakes.md         # 20+ documented pitfalls
│   ├── poll-based-architecture.md # Core architecture pattern
│   ├── interaction-patterns.md    # tap vs tapGameScreen, visibility
│   └── [70+ more .md files]       # Complete API reference
├── examples/                      # Sample scripts and patterns
│   ├── discord-post.md            # Discord announcement template
│   └── [Sample scripts]           # Reference implementations
├── TidalsScriptName/              # Each script is a top-level module
│   ├── build.gradle               # Module build config
│   ├── discord_post.md            # Discord announcement
│   ├── Changes/                   # Daily changelogs (gitignored)
│   ├── jar/                       # Output JAR directory
│   └── src/main/
│       ├── java/
│       │   ├── main/
│       │   │   ├── TidalsScriptName.java   # Main script class
│       │   │   └── ScriptUI.java           # JavaFX setup UI (optional)
│       │   ├── tasks/              # Task implementations
│       │   │   ├── Setup.java
│       │   │   └── [ActivityTasks].java
│       │   ├── utils/
│       │   │   └── Task.java       # Task interface
│       │   ├── data/               # Locations, configs, enums
│       │   └── obf/
│       │       └── Secrets.java    # API keys (gitignored)
│       └── resources/
│           └── logo.png            # 208x91px script logo
├── build.gradle                   # Root build - ProGuard shrinking
├── settings.gradle                # Multi-project settings
└── AGENTS.md                      # This file
```

---

## Build Commands

### Building Scripts

```bash
# Build and deploy a single script
osmb build TidalsGemMiner

# Build utilities when shared code changes
osmb build utilities

# List available scripts
osmb list
```

### Manual Gradle Build (when osmb CLI unavailable)

```bash
# Build utilities
JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle :utilities:build

# Build specific script
JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle :TidalsGemMiner:build

# Build all scripts
JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle build
```

**Output Location:** `<script-dir>/jar/<ScriptName>.jar`

**Build Process:**
1. Compiles Java source
2. Creates fat JAR with dependencies
3. Runs ProGuard to shrink (removes unused code)
4. Outputs final JAR to `jar/` directory

---

## Architecture

### Poll-Based Task System

OSMB scripts operate on a polling model. The framework calls `poll()` repeatedly, and each call should perform ONE logical action then return.

**Task Priority Flow:**
```
[poll() called]
    │
    ├── CrashRecovery.activate()? → Yes → execute(), return
    │                             └ No ──┐
    ├── Setup.activate()? ───────────────┼─→ Yes → execute(), return
    │                                    │   └ No ──┐
    ├── Bank.activate()? ──────────────────────────┼─→ Yes → execute(), return
    │                                               │   └ No ──┐
    └── Mine.activate()? ─────────────────────────────────────┼─→ Yes → execute()
                                                               └ No → idle
```

**Task Interface:**
```java
public abstract class Task {
    protected Script script;
    
    public Task(Script script) {
        this.script = script;
    }
    
    public abstract boolean activate();  // Can this task run right now?
    public abstract boolean execute();   // Do ONE action, then return
}
```

**Main Script Structure:**
```java
@ScriptDefinition(
    name = "TidalsScriptName",
    author = "Tidal",
    description = "Description",
    skillCategory = SkillCategory.MINING,
    version = 1.0
)
public class TidalsScriptName extends Script {
    private List<Task> tasks;
    
    @Override
    public int poll() {
        for (Task t : tasks) {
            if (t.activate()) {
                t.execute();
                return 0;  // Return sleep time in ms (0 = continue immediately)
            }
        }
        return 600;  // Idle sleep time
    }
}
```

### State Management

Share state across tasks using static fields in the main script:

```java
public class TidalsScriptName extends Script {
    // Cross-task state
    public static volatile boolean setupDone = false;
    public static volatile String task = "Starting";
    public static volatile int itemsProcessed = 0;
    
    // For flags set in onNewFrame (thread safety)
    public static volatile boolean levelUpDetected = false;
}
```

---

## Coding Standards

### Naming Conventions

- **Classes:** `PascalCase` (e.g., `TidalsGemMiner`, `BankTask`)
- **Methods:** `camelCase` (e.g., `findNearestBank()`, `isInventoryFull()`)
- **Constants:** `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_ATTEMPTS`, `BANK_POSITION`)
- **Static fields:** `camelCase` (e.g., `setupDone`, `gemsMined`)
- **Packages:** `main/`, `tasks/`, `utils/`, `data/`, `obf/`

### Indentation

- Use 4-space indentation
- Opening brace on same line

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

---

## Critical API Patterns

### Timing and Delays

**NEVER use `script.random()`** - produces uniform distribution (robotic).

**Use `RandomUtils` methods:**
```java
// FIXED DELAY (animation wait, exact timing)
script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(1800, 2400, 2100, 150));

// HUMANIZED DELAY (between actions, adds ~500-1000ms variance)
script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));

// CONDITIONAL WAIT
script.pollFramesUntil(() -> bank.isVisible(), 5000);
```

**CRITICAL: Lambda Return Values Matter:**
```java
// WRONG - Exits IMMEDIATELY (~38ms) - NO WAIT AT ALL
script.pollFramesUntil(() -> true, 2000);

// CORRECT - Waits full ~2000ms
script.pollFramesUntil(() -> false, 2000);
```

### Interaction Methods

| Method | Use Case |
|--------|----------|
| `tap(shape, "Action")` | UI elements only (inventory, bank, dialogue) |
| `tapGameScreen(shape, "Action")` | 3D world objects (rocks, trees, NPCs) |

```java
// UI interactions
ItemSearchResult item = script.getWidgetManager().getInventory().getItem(itemId);
script.getFinger().tap(item.getBounds(), "Use");

// 3D world interactions - MUST verify visibility first
RSObject rock = script.getObjectManager().getClosestObject(myPos, "Rocks");
Polygon hull = rock.getConvexHull();
if (hull == null || hull.numVertices() == 0) return false;

double visibility = script.getWidgetManager().insideGameScreenFactor(
    hull, List.of(ChatboxComponent.class)
);
if (visibility < 0.3) return false;  // Not visible enough

Polygon shrunk = hull.getResized(0.7);  // Shrink to avoid misclicks
script.getFinger().tapGameScreen(shrunk != null ? shrunk : hull, "Mine");
```

### Null Checking

**Always null-check API returns** (except Dialogue and UIResult which are guaranteed non-null):

```java
// These CAN return null - check before use
RSObject obj = script.getObjectManager().getClosestObject(pos, "Tree");
if (obj == null) return false;

ItemGroupResult items = script.getWidgetManager().getInventory().search(Set.of(itemId));
if (items == null || !items.contains(itemId)) return false;

// These are NEVER null - just check visibility/isFound
Dialogue dialogue = script.getWidgetManager().getDialogue();
if (!dialogue.isVisible()) return false;

UIResult<String> text = dialogue.getText();
if (!text.isFound()) return false;
```

### Exception Handling

**Catch `RuntimeException`, NOT `Exception`** - OSMB uses exceptions for control flow:

```java
// WRONG - Catches OSMB control flow exceptions
try { ... } catch (Exception e) { ... }

// CORRECT - Only catches actual runtime errors
try { ... } catch (RuntimeException e) { ... }
```

### Lambda Safety

Extract `getWorldPosition()` to local variable inside lambdas:

```java
// WRONG - getWorldPosition() can return null when lambda executes
script.pollFramesUntil(() -> {
    RSObject bank = script.getObjectManager().getClosestObject(
        script.getWorldPosition(), "Bank"  // Might be null!
    );
    return bank != null;
}, 5000);

// CORRECT - Null-check inside lambda
script.pollFramesUntil(() -> {
    WorldPosition myPos = script.getWorldPosition();
    if (myPos == null) return false;  // Gracefully handle
    RSObject bank = script.getObjectManager().getClosestObject(myPos, "Bank");
    return bank != null;
}, 5000);
```

---

## Shared Utilities

Located in `utilities/src/main/java/utilities/`. **Always use these** instead of custom logic:

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

---

## Required Script Elements

Every script MUST include:

1. **Version Checking in `onStart()`:**
```java
@Override
public void onStart() {
    if (checkForUpdates()) {
        stop();
        return;
    }
    // ... rest of initialization
}
```

2. **Region Priorities (banking scripts):**
```java
@Override
public int[] regionsToPrioritise() {
    return new int[]{12850, 12851};  // Your operating regions
}
```

3. **AFK Control:**
```java
@Override
public boolean canAFK() {
    return allowAFK;  // Toggle during critical actions
}
```

4. **Level Up Handling:**
```java
// Check for level up dialogue after XP-granting actions
Dialogue dialogue = script.getWidgetManager().getDialogue();
if (dialogue.getDialogueType() == DialogueType.TAP_HERE_TO_CONTINUE) {
    dialogue.continueChatDialogue();
}
```

---

## Testing Strategy

- **No automated test suite** - testing is manual in OSMB client
- Use purpose-built test scripts when touching utilities:
  - `TidalsLoadoutTester` - Test loadout/restocking flows
  - `TidalsBankTester` - Test banking patterns
  - `TidalsDelayTester` - Verify timing patterns
- Cross-check behavior against relevant guides in `docs/`

---

## Documentation

**Must Read BEFORE Writing Code:**
- `docs/critical-concepts.md` - Color bot fundamentals
- `docs/common-mistakes.md` - 20+ documented pitfalls with correct patterns
- `docs/poll-based-architecture.md` - One action per poll pattern
- `docs/interaction-patterns.md` - tap vs tapGameScreen, visibility, MovementChecker

**Reference:**
- `docs/api-reference.md` - Complete API methods
- `docs/banking-patterns.md` - Banking, inventory, deposits
- `docs/Walker.md` - Walking code pitfalls
- `docs/Paint.md` - Paint overlay & Setup UI standard

**Online API Docs:** https://doc.osmb.co.uk/documentation

---

## Changelog Management

Every script should have a `Changes/` directory for tracking modifications:

```
TidalsScriptName/
└── Changes/
    └── ScriptName-01-28-Changes.md
```

**Format:**
```markdown
# TidalsScriptName Changes - 2026-01-28

## Summary
Brief overview of changes.

---

## 1. Change Title

**File:** `path/to/file.java` (lines XX-YY)

**Problem:** What was broken.

**Fix:** What was changed.

```java
// Code snippet
```
```

---

## Discord Post

Every script should have a `discord_post.md` file for Discord announcements.

See `examples/discord-post.md` for template and `TidalsCannonballThiever/discord_post.md` for a real example.

---

## Security

- Put secrets in `obf/` package (gitignored)
- Never commit API keys to git
- Use `obf.Secrets.java` for webhook URLs, API keys

---

## Commit Guidelines

- Commit messages are short, descriptive sentences
- No Conventional Commit prefixes
- Do not add `Co-Authored-By` trailers
- Do not commit `Changes/` directories or planning files

**PR Requirements:**
- List affected scripts/modules
- Note jar builds performed
- Include manual test notes
- Add screenshots/logs when paint/UI changes

---

## Quick Reference

| Pattern | Correct Usage |
|---------|---------------|
| Fixed delay | `pollFramesUntil(() -> false, ms)` |
| Humanized delay | `pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400))` |
| 3D object click | `tapGameScreen()` + visibility check |
| UI click | `tap()` |
| Random timing | `RandomUtils.gaussianRandom()` or `weightedRandom()` |
| Menu retries | `RetryUtils.tapGameScreen()` |
| Bank withdrawal | `BankSearchUtils.searchAndWithdrawVerified()` |
| Movement timeout | `MovementChecker` |
| Exception catching | `catch (RuntimeException e)` |

---

*Think visually. Verify interactions. Read existing scripts before writing new code.*
