# Coding Conventions

**Analysis Date:** 2026-01-14

## Naming Patterns

**Files:**
- PascalCase for Java files: `TidalsGemCutter.java`, `BankingUtils.java`
- Task files: `Setup.java`, `Process.java`, `Bank.java`
- Utility files: `*Utils.java` suffix (RetryUtils, BankingUtils, DialogueUtils)

**Functions:**
- camelCase for all methods: `openBankAndWait()`, `equipmentInteract()`, `searchAndWithdraw()`
- No special prefix for async methods
- Handler pattern: verb-first descriptive names

**Variables:**
- camelCase for instance/local: `setupDone`, `selectedUncutGemID`, `webhookEnabled`
- UPPER_SNAKE_CASE for constants: `DEFAULT_MAX_ATTEMPTS`, `STATS_INTERVAL_MS`, `MAX_SCROLL_ITERATIONS`
- No underscore prefix for private members

**Types:**
- PascalCase for classes/interfaces: `Task`, `WithdrawalRequest`, `BatchWithdrawalResult`
- No `I` prefix for interfaces
- Descriptive names: `GuardTracker`, `XPTracking`

## Code Style

**Formatting:**
- 4-space indentation (Java standard)
- Opening brace on same line
- Consistent blank lines between method definitions
- No trailing whitespace

**Quotes & Strings:**
- Double quotes for all string literals: `"Opening inventory"`
- String.format() for complex string construction

**Semicolons:**
- Required at end of all statements

**Linting:**
- No explicit linting configuration (checkstyle, PMD)
- Style enforced manually/via IDE
- Guidelines documented in `CLAUDE.md`

## Import Organization

**Order:**
1. `com.osmb.api.*` - OSMB API imports
2. `java.*` - Standard Java imports
3. `main.*` - Local main package
4. `utils.*` - Local utility imports
5. `static main.[ScriptName].*` - Static field imports

**Grouping:**
- Blank line between OSMB and Java imports
- No wildcard imports (explicit imports only)
- Static imports at end with `static` keyword

**Example from Bank.java:**
```java
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemID;
import com.osmb.api.script.Script;

import java.util.Collections;
import java.util.Set;

import main.TidalsGemCutter;
import utils.Task;

import static main.TidalsGemCutter.*;
```

## Error Handling

**Patterns:**
- Null checks before method calls, return false/null for failures
- Graceful degradation rather than exceptions
- Try-catch only for IO/network operations

**Example:**
```java
if (bank == null) {
    script.log(BankingUtils.class, "bank object is null");
    return false;
}
```

**Logging:**
- Context-aware: `script.log(ClassName.class, "message")`
- Lowercase, blunt messages per project guidelines
- Log each retry attempt: `"description attempt X/10"`

## Logging

**Framework:**
- OSMB built-in: `script.log(Class, String)` method
- Outputs to OSMB client console

**Patterns:**
- Log state transitions and important actions
- Include context (item names, counts, task names)
- Keep messages concise and lowercase

**Example:**
```java
script.log(getClass(), "looking for " + itemName);
script.log(BankSearchUtils.class, "searching for: " + itemName + " (id: " + itemId + ")");
```

## Comments

**When to Comment:**
- Explain why, not what: `// banked cut gems mode - bank when no cut gems`
- Document business logic and mode differences
- Avoid obvious comments

**JavaDoc:**
- Required for all public utility methods
- Format: `/** ... */` block style
- Use `@param`, `@return` tags

**Example from RetryUtils.java:**
```java
/**
 * Retry an equipment interaction up to maxAttempts times.
 *
 * @param script the script instance
 * @param itemId the equipment item ID to interact with
 * @param action the action to perform (e.g., "Teleport")
 * @param description logging description for the interaction
 * @param maxAttempts maximum retry attempts
 * @return true if interaction succeeded
 */
```

**Inline Comments:**
- Blunt, lowercase style: `// track last sent values for incremental reporting`
- Comments above the code they describe
- Branch explanation in conditionals

## Function Design

**Size:**
- Keep under 50 lines where practical
- Complex scripts may have longer methods (tracked as concern)

**Parameters:**
- `script` reference always first parameter in utilities
- Use options objects for 4+ parameters (see `WithdrawalRequest`)
- Descriptive parameter names

**Return Values:**
- Boolean for success/failure operations
- Null for "not found" cases
- Explicit returns, no implicit undefined

## Module Design

**Exports:**
- Public methods in utility classes
- Package-private for internal helpers
- Static methods for stateless utilities

**Task Pattern:**
- Abstract base class with `activate()` and `execute()` methods
- Concrete implementations per state
- Tasks communicate via static fields in main script

**Static State:**
- Inter-task communication: `public static` fields in main script
- Example: `TidalsGemCutter.setupDone`, `TidalsGemCutter.task`
- Reset in `onStart()`, cleaned in `onStop()`

## Retry Pattern (Critical)

**Standard pattern for all menu interactions:**
```java
private boolean interactWithRetry(Polygon poly, String action, String description) {
    int maxAttempts = 10;
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
        script.log(getClass(), description + " attempt " + attempt + "/" + maxAttempts);
        boolean success = script.getFinger().tap(poly, action);
        if (success) return true;
        script.pollFramesUntil(() -> false, script.random(300, 500), true);
    }
    script.log(getClass(), description + " failed after " + maxAttempts + " attempts");
    return false;
}
```

**Rules:**
- Default 10 retry attempts
- Log each attempt as "description attempt X/10"
- 300-500ms random delay between attempts
- Use `RetryUtils` from shared utilities instead of custom implementations

---

*Convention analysis: 2026-01-14*
*Update when patterns change*
