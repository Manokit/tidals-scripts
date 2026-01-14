# Testing Patterns

**Analysis Date:** 2026-01-14

## Test Framework

**Runner:**
- No formal unit testing framework detected
- No JUnit, TestNG, Mockito, or similar in dependencies
- No `testImplementation` dependencies in `build.gradle` files

**Assertion Library:**
- Not applicable - no test framework

**Run Commands:**
```bash
# No test commands available
# Scripts are tested manually through OSMB client execution
```

## Test File Organization

**Location:**
- No `src/test/` directories found
- No `__tests__/` directories
- No `*.test.java` or `*.spec.java` files

**Testing Philosophy:**
- Manual/integration-focused rather than unit-tested
- Scripts tested by running in OSMB client
- Visual verification through paint overlay statistics

## Implicit Testing Approach

**Setup Validation:**
- `Setup.java` tasks validate prerequisites before execution
- Checks performed:
  - Skill levels (`TidalsGemCutter/src/main/java/tasks/Setup.java`)
  - Equipment presence
  - Inventory state
  - Location verification

**Runtime Validation Pattern:**
```java
@Override
public boolean activate() {
    ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(selectedUncutGemID));
    if (inv == null) return false;
    return !inv.contains(selectedUncutGemID);
}
```

**Manual Testing Approach:**
- Scripts output detailed logs for monitoring
- Canvas overlay displays runtime statistics
- XP tracking compared against in-game stats

## Test Structure (Inferred)

**Condition Checks:**
- All task `activate()` methods contain conditional logic
- Example: `Bank.java` lines 21-51 - Complex conditions for banking
- Example: `Setup.java` - Prerequisites validation

**State Verification:**
- `XPTracking` class monitors skill progression
- `GuardTracker` monitors NPC positions and movement
- Paint overlay displays live statistics

## Coverage Approach

**Areas Without Tests:**
- `RetryUtils.java` - Tested implicitly through script execution
- `BankingUtils.java` - Validated during runtime
- `DialogueUtils.java` - Tested when scripts encounter dialogues
- `BankSearchUtils.java` - Validated through bank operations

**Validation Through Execution:**
- State machine transitions - Validated through task activation/execution
- Webhook integration - Tested with Discord in production
- XP tracking - Compared against in-game stats in logs

## Build Verification

**Gradle Build:**
```bash
# Build specific script
osmb build TidalsGemCutter

# Build all scripts
osmb build all

# Build utilities JAR
cd tidals-scripts && JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle :utilities:build
```

**Build Output Verification:**
- JAR files generated in `[ScriptName]/jar/`
- No automated build tests
- Manual load test in OSMB client

## Linting & Static Analysis

**Java:**
- No checkstyle.xml configuration
- No PMD or FindBugs setup
- IDE-based style enforcement only

**TypeScript (Dashboard):**
- ESLint configured (`script-dashboard/eslint.config.mjs`)
- Extends `next/core-web-vitals` and `next/typescript`
- Run: `npm run lint`

## Dashboard Testing

**Framework:**
- No test framework detected in `script-dashboard/package.json`
- No Jest, Vitest, or Playwright configuration

**API Testing:**
- Manual testing via curl/Postman
- Scripts serve as integration test clients

## Test Gaps (Known Issues)

**Critical Gaps:**
- No unit tests for utility classes
- No integration tests for API endpoints
- No component tests for React dashboard
- Complex business logic (GuardTracker) untested

**Recommended Additions:**
- JUnit 5 for Java utility testing
- Vitest for dashboard component testing
- API integration tests with mock scripts

## Debugging Approach

**Logging:**
- `script.log(Class, "message")` throughout codebase
- Paint overlay for visual debugging
- Task name displayed in overlay

**OSMB Debug Tools:**
- Built-in OSMB client debugger
- Pixel analyzer for color detection debugging
- Object inspector for RSObject verification

---

*Testing analysis: 2026-01-14*
*Update when test patterns change*
