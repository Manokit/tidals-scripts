# Architecture

**Analysis Date:** 2026-01-14

## Pattern Overview

**Overall:** Modular Monorepo with Shared Utilities

**Key Characteristics:**
- Multiple independent script applications sharing a utility JAR
- Task-based state machine pattern for script execution
- Separate dashboard application for statistics aggregation
- Single-threaded execution model within OSMB client

## Layers

**Presentation Layer:**
- Purpose: User interface and visual feedback
- Contains: Paint overlays, JavaFX setup UIs, React dashboard components
- Files: `**/main/ScriptUI.java`, `**/onPaint()` methods, `script-dashboard/src/app/`
- Depends on: Script Core Layer, API Layer

**Script Core Layer:**
- Purpose: State machine orchestration and task execution
- Contains: Main script classes, task implementations, state management
- Files: `**/main/[ScriptName].java`, `**/tasks/*.java`
- Depends on: Service Layer, API Layer
- Used by: Presentation Layer

**Service/Utility Layer:**
- Purpose: Reusable interaction patterns and helpers
- Contains: RetryUtils, BankingUtils, DialogueUtils, TabUtils, BankSearchUtils
- Files: `utilities/src/main/java/utilities/*.java`
- Depends on: API Layer only
- Used by: Script Core Layer

**API Layer (OSMB):**
- Purpose: Game client abstraction and detection
- Contains: Managers (Widget, Object, Scene), Pixel detection, OCR
- Files: `API/API.jar` (provided)
- Depends on: Platform
- Used by: All layers above

## Data Flow

**Script Execution Lifecycle:**

1. User runs `osmb build [ScriptName]` - Gradle compiles to JAR
2. User loads script in OSMB client
3. `Script.onStart()` - Initialize UI, load config, create task list
4. `Script.poll()` loop at each frame:
   - Check webhook send timing
   - Iterate tasks, check `task.activate()`
   - First activated task's `execute()` runs
   - Return sleep duration (ms)
5. `Script.onPaint()` - Render overlay asynchronously
6. `Script.onStop()` - Cleanup on stop

**Stats Reporting Flow:**

1. Script collects incremental stats (XP, items, runtime)
2. Every 10 minutes: `sendStats()` constructs JSON payload
3. HTTP POST to dashboard `/api/stats` endpoint
4. Dashboard validates, stores to SQLite, aggregates daily
5. React dashboard fetches and displays charts

**State Management:**
- Inter-task: Shared `public static` fields in main script class
- Example: `TidalsGemCutter.setupDone`, `TidalsGemCutter.task`, `TidalsGemCutter.craftCount`
- Cross-session: SQLite database for historical stats

## Key Abstractions

**Task (State Machine Node):**
- Purpose: Encapsulate activation condition and execution logic
- Interface: `activate()` returns boolean, `execute()` performs action
- Examples: `Setup.java`, `Process.java`, `Bank.java`
- Files: `**/utils/Task.java` (abstract base)
- Pattern: Strategy with condition-based selection

**Utility Services:**
- Purpose: Encapsulate common interaction patterns with retry logic
- Examples: `RetryUtils`, `BankingUtils`, `DialogueUtils`, `TabUtils`
- Files: `utilities/src/main/java/utilities/*.java`
- Pattern: Static utility methods with script reference

**RSObject Query Predicate:**
- Purpose: Reusable object finding criteria
- Examples: `BankingUtils.BANK_QUERY`, custom predicates per script
- Pattern: Predicate<RSObject> composition

**XP Tracking:**
- Purpose: Monitor skill progression during execution
- Files: `**/utils/XPTracking.java`
- Pattern: Observer on XP changes

## Entry Points

**Script Entry:**
- Location: `[ScriptName]/src/main/java/main/[ScriptName].java`
- Triggers: OSMB client loads JAR
- Responsibilities: Extend `Script`, implement `onStart()`, `poll()`, `onStop()`
- Examples:
  - `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java`
  - `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java`
  - `TidalsGoldSuperheater/src/main/java/main/TidalsGoldSuperheater.java`

**Dashboard API Entry:**
- Location: `script-dashboard/src/app/api/stats/route.ts`
- Triggers: HTTP POST from scripts
- Responsibilities: Validate, store, aggregate stats

**Build Entry:**
- Location: `settings.gradle`
- Triggers: `gradle build` or `osmb build`
- Responsibilities: Auto-discover and compile all modules

## Error Handling

**Strategy:** Graceful degradation with logging

**Patterns:**
- Null checks before method calls, return false/null for failures
- `script.log(Class, "message")` for context-aware logging
- Try-catch only for truly exceptional cases (IO, network)
- Retry logic in utilities (10 attempts, 300-500ms delays)

## Cross-Cutting Concerns

**Logging:**
- Java: `script.log(ClassName.class, "message")` - OSMB client console
- Dashboard: Console stdout/stderr

**Validation:**
- API: String sanitization, numeric bounds, size limits
- Scripts: `Setup.java` task validates prerequisites before execution

**Security:**
- API key authentication with timing-safe comparison
- Security headers in Next.js config (X-Frame-Options, CSP)
- Rate limiting (30 req/min per IP)

---

*Architecture analysis: 2026-01-14*
*Update when major patterns change*
