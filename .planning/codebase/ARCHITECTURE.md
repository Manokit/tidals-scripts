# Architecture

**Analysis Date:** 2026-01-13

## Pattern Overview

**Overall:** Monorepo with State Machine Scripts + Full-stack Dashboard

**Key Characteristics:**
- Independent Java scripts share utilities via compiled JAR
- Dashboard aggregates incremental stats from running scripts
- HTTP POST every 10 minutes for stats reporting
- Color bot paradigm: visual detection, not injection

## Layers

**Script Entry Point Layer:**
- Purpose: Initialize script, run main loop, render paint overlay
- Contains: `@ScriptDefinition`, `onStart()`, `poll()`, `onPaint()`
- Location: `*/src/main/java/main/*.java`
- Depends on: Task layer, utilities, OSMB API
- Used by: OSMB client loads and executes scripts

**Task Layer (State Machine):**
- Purpose: Encapsulate discrete actions with conditions
- Contains: `activate()` condition, `execute()` action
- Location: `*/src/main/java/tasks/*.java`
- Depends on: Script reference, utilities
- Used by: Main script's poll loop

**Utilities Layer:**
- Purpose: Shared reusable functionality across scripts
- Contains: RetryUtils, BankingUtils, TabUtils, DialogueUtils
- Location: `utilities/src/main/java/utilities/*.java`
- Depends on: OSMB API only
- Used by: All scripts via JAR dependency

**Configuration Layer:**
- Purpose: User preferences via JavaFX UI
- Contains: ScriptUI dialogs, Preferences storage
- Location: `*/src/main/java/main/ScriptUI.java`
- Used by: Main script's onStart()

**Dashboard API Layer:**
- Purpose: Receive and serve aggregated stats
- Contains: POST/GET handlers, authentication, rate limiting
- Location: `script-dashboard/src/app/api/stats/route.ts`
- Depends on: Prisma ORM, SQLite
- Used by: Scripts (POST), frontend (GET)

**Dashboard Page Layer:**
- Purpose: Render stats visualizations
- Contains: Server components with database queries
- Location: `script-dashboard/src/app/*/page.tsx`
- Depends on: Prisma, React components
- Used by: Web browsers

## Data Flow

**Script Execution Cycle:**

1. User starts script in OSMB client
2. `onStart()` shows ScriptUI, initializes task list
3. `poll()` iterates tasks, first `activate()` true runs `execute()`
4. Task returns, poll sleeps (0-600ms)
5. Every 10 minutes: `sendStats()` POSTs incremental data
6. `onPaint()` renders overlay each frame
7. `onStop()` cleanup on script end

**Stats Reporting Flow:**

1. Script tracks XP/GP/runtime increments
2. POST to `/api/stats` with X-Stats-Key header
3. Dashboard validates, rate-limits, sanitizes
4. Upsert ScriptSession (session tracking)
5. Create ScriptStat (individual report)
6. Update AggregatedStats (daily rollup)
7. Frontend fetches GET `/api/stats?days=7`
8. Render charts with Recharts

**State Management:**
- Scripts: Static fields + Preferences API (per-user)
- Dashboard: File-based SQLite via Prisma
- Sessions: UUID per script run, ephemeral

## Key Abstractions

**Task:**
- Purpose: Discrete state machine action
- Examples: `Setup.java`, `Process.java`, `Bank.java`
- Location: `*/src/main/java/tasks/*.java`
- Pattern: Abstract class with `activate()` + `execute()`

**Utility:**
- Purpose: Reusable interaction helpers
- Examples: `RetryUtils`, `BankingUtils`, `TabUtils`, `DialogueUtils`
- Location: `utilities/src/main/java/utilities/*.java`
- Pattern: Static methods with Script reference parameter

**Strategy (Alternative):**
- Purpose: Pluggable behavior implementations
- Examples: `MortMyreFungusCollector` implements `SecondaryCollectorStrategy`
- Location: `TidalsSecondaryCollector/src/main/java/strategies/`
- Pattern: Interface + concrete implementations

**XPTracking:**
- Purpose: Skill progress wrapper
- Location: `*/src/main/java/utils/XPTracking.java`
- Pattern: Wrapper around OSMB XPTracker API

## Entry Points

**Scripts:**
- Location: `Tidals*/src/main/java/main/Tidals*.java`
- Triggers: OSMB client starts script
- Responsibilities: Initialization, loop execution, stats, paint

**Dashboard Homepage:**
- Location: `script-dashboard/src/app/page.tsx`
- Triggers: Browser navigates to `/`
- Responsibilities: Hero section, script carousel

**Dashboard Stats:**
- Location: `script-dashboard/src/app/stats/page.tsx`
- Triggers: Browser navigates to `/stats`
- Responsibilities: Query DB, render charts/cards

**Stats API:**
- Location: `script-dashboard/src/app/api/stats/route.ts`
- Triggers: HTTP POST/GET requests
- Responsibilities: Auth, validation, database operations

## Error Handling

**Strategy (Scripts):** Log and continue
- Utilities log failures with attempt counts
- Scripts use `script.log(getClass(), message)` pattern
- Some empty catch blocks exist (tech debt)

**Strategy (Dashboard):** Return appropriate HTTP status
- 400 for validation errors
- 401 for authentication failures
- 429 for rate limiting
- 500 for unexpected errors
- All errors logged to console

## Cross-Cutting Concerns

**Logging:**
- Scripts: `script.log(Class, message)` to OSMB console
- Dashboard: `console.error()` / `console.log()`

**Authentication:**
- X-Stats-Key header with timing-safe comparison
- API key stored in Secrets.java (scripts) and .env (dashboard)

**Rate Limiting:**
- Dashboard: 30 requests/minute per IP
- In-memory Map-based tracking

**Validation:**
- Dashboard: Input sanitization, length limits, type checking
- Scripts: Minimal validation before API calls

---

*Architecture analysis: 2026-01-13*
*Update when major patterns change*
