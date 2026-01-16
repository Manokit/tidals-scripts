# Architecture

**Analysis Date:** 2026-01-16

## Pattern Overview

**Overall:** Task-based State Machine with Strategy Pattern (for multi-mode scripts)

**Key Characteristics:**
- Scripts extend `com.osmb.api.script.Script` from OSMB API
- Main loop in `poll()` iterates through Task list, executing first active task
- Tasks implement `activate()` (condition) and `execute()` (action) pattern
- Strategy pattern used for scripts with multiple collection/activity modes
- Shared utilities library (`TidalsUtilities.jar`) for cross-script code reuse

## Layers

**Script Layer (Main Class):**
- Purpose: Entry point, lifecycle management, UI display, stats reporting
- Location: `{ScriptName}/src/main/java/main/{ScriptName}.java`
- Contains: `@ScriptDefinition` annotation, `onStart()`, `poll()`, `onPaint()`, task list initialization
- Depends on: Task layer, OSMB API (`com.osmb.api.*`)
- Used by: OSMB client runtime

**Task Layer:**
- Purpose: Discrete units of behavior representing script states
- Location: `{ScriptName}/src/main/java/tasks/*.java`
- Contains: Individual tasks (Setup, Process, Bank, etc.) implementing Task interface
- Depends on: Utilities layer, OSMB API
- Used by: Script layer (via task list iteration)

**Strategy Layer (Optional):**
- Purpose: Encapsulate different activity modes (e.g., different collection methods)
- Location: `{ScriptName}/src/main/java/strategies/*.java`
- Contains: Strategy interface + concrete implementations
- Depends on: Utilities layer, OSMB API
- Used by: Script layer delegates state determination and actions

**Utilities Layer:**
- Purpose: Shared, reusable code across all scripts
- Location: `utilities/src/main/java/utilities/*.java`
- Contains: RetryUtils, BankingUtils, BankSearchUtils, DialogueUtils, TabUtils, Loadout system
- Depends on: OSMB API only
- Used by: All scripts via `TidalsUtilities.jar` dependency

**UI Layer:**
- Purpose: JavaFX configuration dialogs shown before script starts
- Location: `{ScriptName}/src/main/java/main/ScriptUI.java`
- Contains: JavaFX Scene building, user preferences, settings persistence
- Depends on: OSMB JavaFX utilities, Java Preferences API
- Used by: Script `onStart()` method

## Data Flow

**Main Execution Loop:**

1. `onStart()` shows ScriptUI dialog, initializes task list
2. `poll()` iterates tasks, calling `task.activate()` on each
3. First task where `activate()` returns true gets `execute()` called
4. `execute()` performs action, returns false (to re-evaluate next poll)
5. Return value from `poll()` is sleep time in ms (typically 0 or 600)

**Strategy-Based Flow (TidalsSecondaryCollector pattern):**

1. `poll()` calls `strategy.determineState()` to get current state enum
2. Switch on state calls appropriate strategy method (collect, bank, restore, return)
3. Strategy method performs action and returns sleep time

**State Management:**
- Global state via static fields on main script class (e.g., `setupDone`, `currentState`, `craftCount`)
- Task-specific state via instance fields on task classes
- Cross-session persistence via Java Preferences API in ScriptUI

## Key Abstractions

**Task Interface:**
- Purpose: Defines contract for executable script behaviors
- Examples: `{ScriptName}/src/main/java/utils/Task.java`
- Pattern: Abstract class with `activate()` and `execute()` methods

```java
public abstract class Task {
    protected Script script;
    public Task(Script script) { this.script = script; }
    public abstract boolean activate();
    public abstract boolean execute();
}
```

**SecondaryCollectorStrategy Interface:**
- Purpose: Abstraction for different collection activities
- Examples: `TidalsSecondaryCollector/src/main/java/strategies/SecondaryCollectorStrategy.java`
- Pattern: Interface with state determination and action methods

```java
public interface SecondaryCollectorStrategy {
    State determineState();
    boolean verifyRequirements();
    int collect();
    int bank();
    int restorePrayer();
    int returnToArea();
}
```

**RetryUtils:**
- Purpose: Wrap menu interactions with retry logic (10 attempts default)
- Examples: `utilities/src/main/java/utilities/RetryUtils.java`
- Pattern: Static utility methods for equipment, object, polygon, inventory interactions

**LoadoutManager:**
- Purpose: Manage equipment/inventory configurations with visual editor
- Examples: `utilities/src/main/java/utilities/loadout/LoadoutManager.java`
- Pattern: Facade over LoadoutEditor, LoadoutPersistence, LoadoutRestocker

## Entry Points

**Script Entry (`onStart`):**
- Location: `{ScriptName}/src/main/java/main/{ScriptName}.java`
- Triggers: OSMB client loads and starts script
- Responsibilities: Show UI, configure settings, initialize task list

**Main Loop (`poll`):**
- Location: Same as above
- Triggers: Called repeatedly by OSMB runtime
- Responsibilities: Execute tasks, send stats, return sleep duration

**Frame Callback (`onNewFrame`):**
- Location: Same as above
- Triggers: Every game frame render
- Responsibilities: XP tracking, real-time state monitoring

**Paint Callback (`onPaint`):**
- Location: Same as above
- Triggers: Every UI render
- Responsibilities: Draw stats overlay on game screen

## Error Handling

**Strategy:** Fail-safe with logging, retry on transient failures

**Patterns:**
- RetryUtils wraps interactions with 10-attempt retry loops
- `pollFramesUntil()` used for timed conditions with timeout fallback
- Methods return boolean success/failure, caller decides next action
- `script.stop()` called for unrecoverable errors (missing requirements)
- Null checks on API results (inventory, bank, position can return null)

## Cross-Cutting Concerns

**Logging:** `script.log(category, message)` or `script.log(Class, message)` - outputs to OSMB client console

**Validation:** Requirements checked in Setup task or `verifyRequirements()` strategy method before main loop

**Authentication:** Stats API uses `obf.Secrets.STATS_API` key from gitignored `obf/Secrets.java`

**AFK/Break Control:** Override `canAFK()`, `canBreak()`, `canHopWorlds()` to control OSMB built-in behaviors based on script state

---

*Architecture analysis: 2026-01-16*
