# Coding Conventions

**Analysis Date:** 2026-01-13

## Naming Patterns

**Files (Java):**
- PascalCase for all class files: `TidalsGemCutter.java`, `Setup.java`, `RetryUtils.java`
- Package names lowercase: `main`, `tasks`, `utils`, `obf`, `utilities`

**Files (TypeScript):**
- PascalCase for components: `StatsChart.tsx`, `ScriptCard.tsx`
- lowercase for utilities: `db.ts`
- Special: `route.ts` for API endpoints

**Functions (Java):**
- camelCase for all methods: `openBankAndWait()`, `equipmentInteract()`, `depositAllExcept()`
- Boolean getters: `isLevelUp()`, `hasDialogue()`, `needsGuardSync()`
- Action methods: `sendStats()`, `handleLevelUp()`, `resetCbCycle()`

**Functions (TypeScript):**
- camelCase: `formatRuntime()`, `sanitizeString()`, `mergeMetadata()`
- React components: PascalCase: `StatsChart`, `ScriptCard`

**Variables (Java):**
- camelCase for fields: `selectedUncutGemID`, `lastStatsSent`, `webhookEnabled`
- UPPER_SNAKE_CASE for constants: `STATS_INTERVAL_MS`, `DEFAULT_MAX_ATTEMPTS`, `BANK_NAMES`
- Static fields: camelCase with static modifier

**Variables (TypeScript):**
- camelCase: `accentColor`, `isRateLimited`
- UPPER_SNAKE_CASE for constants: `RATE_LIMIT_WINDOW`, `MAX_SCRIPT_NAME_LENGTH`

**Types (TypeScript):**
- PascalCase for interfaces: `StatCardProps`, `ScriptStat`, `ChartDataPoint`
- Props suffix for component props: `ScriptCardProps`, `StatCardProps`

## Code Style

**Formatting (Java):**
- Indentation: 4 spaces
- Braces: Same line opening (K&R style)
- Line length: Generally under 120 characters
- Import organization: Grouped by package origin

**Formatting (TypeScript):**
- Indentation: 2 spaces
- Braces: Same line opening
- Line length: Generally under 100 characters
- ESLint: `eslint-config-next` with core-web-vitals

**Linting:**
- TypeScript: ESLint 9 with Next.js config (`script-dashboard/eslint.config.mjs`)
- Java: No formal linter (IDE-based)
- Run: `npm run lint` in script-dashboard

## Import Organization

**Order (Java):**
1. Java standard library (java.*, javax.*)
2. Third-party packages (com.osmb.*)
3. Local packages (tasks.*, utils.*, obf.*)

**Order (TypeScript):**
1. React/framework imports
2. Third-party packages
3. Internal modules (@/lib, @/components)
4. Type imports

**Path Aliases:**
- TypeScript: `@/*` maps to `src/*` (`script-dashboard/tsconfig.json`)

## Error Handling

**Patterns (Java):**
- Utilities log and return boolean: `if (!success) { script.log(...); return false; }`
- Null checks before API calls: `if (inv == null) return false;`
- Atomic types for thread safety: `AtomicBoolean`, `AtomicReference`

**Patterns (TypeScript):**
- Try/catch at API boundaries with appropriate HTTP status
- Timing-safe comparison for authentication
- Input sanitization with helper functions

**Error Types:**
- Java: Log to OSMB console, continue or return
- TypeScript: Return HTTP error responses with status codes

## Logging

**Framework (Java):**
- OSMB logging: `script.log(getClass(), message)` or `script.log("CATEGORY", message)`
- Categories: "GUARD", "CYCLE", "SYNC" for specialized tracking

**Framework (TypeScript):**
- Console: `console.error()` for errors, `console.log()` for info
- No structured logging framework

**Patterns:**
- Java: Log each retry attempt with count: "description attempt X/10"
- TypeScript: Log validation failures with context

## Comments

**When to Comment:**
- Explain why, not what: `// timeout` or `// level up`
- Document business rules and edge cases
- Blunt, lowercase style per CLAUDE.md: `// track crafted items`

**JSDoc/JavaDoc:**
- Java: Document utility methods with usage examples (`BankingUtils.java`)
- TypeScript: JSDoc for public API functions

**TODO Comments:**
- Format: `// TODO: description`
- Example: `// TODO: Not 100% sure these are correct` (`BlisterwoodChopper.java`)

## Function Design

**Size:**
- Java: Most methods under 50 lines, utilities are compact
- TypeScript: Components modular, logic extracted to helpers

**Parameters:**
- Java: Script reference as first parameter for utilities
- TypeScript: Destructured props for React components
- Use object parameter for 4+ arguments

**Return Values:**
- Java: Boolean for success/failure, void for actions
- TypeScript: Typed returns, explicit null handling

## Module Design

**Exports (TypeScript):**
- Named exports for utilities: `export function`
- Named exports for components: `export function ComponentName`
- No default exports

**Barrel Files:**
- Not used in this codebase
- Direct imports preferred

**Java Packages:**
- `main/` - Entry point and UI
- `tasks/` - State machine tasks
- `utils/` - Local utilities
- `obf/` - Secrets (gitignored)
- `utilities/` - Shared JAR

## Script-Specific Patterns

**Task Pattern:**
```java
public abstract class Task {
    protected Script script;
    public Task(Script script) { this.script = script; }
    public abstract boolean activate();
    public abstract boolean execute();
}
```

**Retry Pattern:**
```java
for (int attempt = 1; attempt <= maxAttempts; attempt++) {
    script.log(getClass(), description + " attempt " + attempt + "/" + maxAttempts);
    if (success) return true;
    script.pollFramesUntil(() -> false, script.random(300, 500), true);
}
```

**Static State:**
- Scripts use `public static` fields for configuration
- Set during `onStart()` from ScriptUI selections
- Accessed throughout task execution

---

*Convention analysis: 2026-01-13*
*Update when patterns change*
