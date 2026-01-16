# Architecture

**Analysis Date:** 2026-01-15

## Pattern Overview

**Overall:** Multi-script automation monorepo with state machine core

**Key Characteristics:**
- 5 independent scripts building to separate JARs
- Shared utilities library (`TidalsUtilities.jar`)
- Task/state machine pattern for complex scripts
- Strategy pattern for extensible collection types
- Stats reporting to external dashboard via HTTP

## Layers

**Entry/Main Layer:**
- Purpose: Script entry point, state machine orchestration, stats reporting
- Contains: Main script class extending `Script`, paint overlay, UI initialization
- Location: `<Script>/src/main/java/main/<ScriptName>.java`
- Examples: `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (722 lines)
- Depends on: Task layer, utilities layer, OSMB API
- Used by: OSMB client runtime

**Configuration/UI Layer:**
- Purpose: JavaFX setup dialogs for user configuration
- Contains: Scene definitions, input controls, configuration storage
- Location: `<Script>/src/main/java/main/ScriptUI.java`
- Depends on: JavaFX (bundled with OSMB)
- Used by: Main script at startup

**Task/State Machine Layer:**
- Purpose: Encapsulate discrete states and their logic
- Contains: Task subclasses with `activate()` and `execute()` methods
- Location: `<Script>/src/main/java/tasks/*.java`
- Base class: `<Script>/src/main/java/utils/Task.java`
- Examples: `Setup.java`, `Process.java`, `Bank.java`
- Depends on: Utilities layer, OSMB API
- Used by: Main script's `poll()` method

**Utilities Layer:**
- Purpose: Shared code across all scripts
- Contains: RetryUtils, BankingUtils, TabUtils, DialogueUtils, BankSearchUtils
- Location: `utilities/src/main/java/utilities/*.java`
- Depends on: OSMB API only
- Used by: All scripts via JAR dependency

**Strategy Layer (SecondaryCollector):**
- Purpose: Pluggable collection strategies
- Contains: Abstract strategy base, concrete implementations
- Location: `TidalsSecondaryCollector/src/main/java/strategies/*.java`
- Examples: `MortMyreFungusCollector.java` (1116 lines)
- Depends on: Utilities layer, OSMB API
- Used by: Main script's poll logic

## Data Flow

**Script Execution Lifecycle:**

1. OSMB client loads script JAR
2. `onStart()` called - initializes UI, shows configuration dialog
3. User sets configuration (gem type, item name, etc.)
4. UI closes, static config variables populated
5. `poll()` called repeatedly by OSMB runtime
6. Task iteration: `activate()` checks state, `execute()` performs action
7. OSMB API used for game interaction (tap, walk, detect)
8. `onPaint()` renders live stats overlay
9. Every 10 minutes: HTTP POST incremental stats to dashboard
10. `onStop()` called on script termination

**State Management:**
- Static fields on main script class (e.g., `setupDone`, `craftCount`)
- Tasks coordinate via shared static state
- No persistent state between script runs

## Key Abstractions

**Task:**
- Purpose: Encapsulate a discrete state and its behavior
- Examples: `Setup`, `Process`, `Bank`, `MonitorThieving`, `EscapeJail`
- Pattern: Template method with `activate()` and `execute()`
- Location: `<Script>/src/main/java/utils/Task.java`

**RetryUtils:**
- Purpose: Wrap menu interactions with automatic retry logic
- Examples: `equipmentInteract()`, `objectInteract()`, `tap()`
- Pattern: Retry decorator (10 attempts, 300-500ms delay)
- Location: `utilities/src/main/java/utilities/RetryUtils.java`

**BankingUtils:**
- Purpose: Encapsulate banking operations
- Examples: `findNearestBank()`, `openBankAndWait()`, `depositAllExcept()`
- Pattern: Utility class with static methods
- Location: `utilities/src/main/java/utilities/BankingUtils.java`

**Strategy (SecondaryCollector):**
- Purpose: Support different collection strategies
- Examples: `MortMyreFungusCollector` implements `SecondaryCollectorStrategy`
- Pattern: Strategy pattern with abstract base
- Location: `TidalsSecondaryCollector/src/main/java/strategies/`

## Entry Points

**Script Entry:**
- Location: `<Script>/src/main/java/main/<ScriptName>.java`
- Triggers: OSMB client loads JAR
- Responsibilities: Register with OSMB, initialize tasks, run poll loop

**Build Entry:**
- Location: `<Script>/build.gradle`
- Triggers: `osmb build <ScriptName>`
- Responsibilities: Compile Java, package JAR with resources

**Utilities Entry:**
- Location: `utilities/build.gradle`
- Triggers: Manual `gradle :utilities:build`
- Responsibilities: Compile shared utilities to JAR

## Error Handling

**Strategy:** Log and continue, retry menu interactions

**Patterns:**
- Null checks before all API calls (`if (inv == null) return false`)
- RetryUtils wraps interactions with 10-attempt retry
- Broad exception catch for non-critical operations (stats, webhook)
- `script.log()` for debug output

## Cross-Cutting Concerns

**Logging:**
- `script.log(Class, message)` for class-based logging
- Paint overlay shows runtime stats visually
- Discord webhook for periodic screenshots

**Validation:**
- `Setup` task validates requirements before execution
- Inventory checks before banking decisions
- Skill level checks before processing

**Stats Reporting:**
- HTTP POST to dashboard every 10 minutes
- Incremental values (delta since last report)
- Session ID for tracking across restarts

---

*Architecture analysis: 2026-01-15*
*Update when major patterns change*
