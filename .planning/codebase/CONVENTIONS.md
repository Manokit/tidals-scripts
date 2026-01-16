# Coding Conventions

**Analysis Date:** 2026-01-15

## Naming Patterns

**Files:**
- PascalCase for all Java classes (`TidalsGemCutter.java`, `RetryUtils.java`, `BankingUtils.java`)
- kebab-case for documentation (`api-reference.md`, `banking-patterns.md`)
- File names match class names (standard Java convention)

**Functions:**
- camelCase for all methods (`activate()`, `execute()`, `openBankAndWait()`)
- Action verbs preferred (`interact()`, `withdraw()`, `deposit()`, `findNearestBank()`)
- No special prefix for async methods
- Handler pattern: `handleEventName` not widely used (scripts are synchronous)

**Variables:**
- camelCase for instance/local variables (`selectedUncutGemID`, `craftCount`, `lastStatsSent`)
- CONSTANT_CASE for constants (`STATS_INTERVAL_MS`, `DEFAULT_MAX_ATTEMPTS`, `FONT_LABEL`)
- Boolean prefixes: `is/has/should/done` (`setupDone`, `makeBoltTips`, `shouldBank`, `hasReqs`)
- Underscore for large numbers (`600_000L`, `3_600_000.0`)

**Types:**
- PascalCase for interfaces, no I prefix (`Task`, `SecondaryCollectorStrategy`)
- PascalCase for classes (`TidalsGemCutter`, `RetryUtils`)
- PascalCase for enums, UPPER_CASE for values (`State.IDLE`, `SecondaryType.MORT_MYRE_FUNGUS`)

## Code Style

**Formatting:**
- 4 spaces indentation (standard Java)
- 120 character line length (generally followed)
- Double quotes for strings (Java standard)
- Semicolons required (Java syntax)

**Linting:**
- No linting tools configured (no Checkstyle, SpotBugs)
- Manual code review pattern
- IDE formatting (IntelliJ/VS Code defaults)

## Import Organization

**Order:**
1. External packages (`com.osmb.api.*`)
2. Java stdlib (`java.util.*`, `java.awt.*`)
3. Project packages (`main.*`, `tasks.*`, `utilities.*`)

**Grouping:**
- Wildcard imports accepted (`java.util.*`)
- Static imports for script constants (`import static main.TidalsGemCutter.*`)
- No strict alphabetical ordering

**Path Aliases:**
- Not applicable (Java doesn't use path aliases)

## Error Handling

**Patterns:**
- Null checks before all API calls (`if (inv == null) return false`)
- RetryUtils wraps menu interactions with automatic retry
- Broad exception catch for non-critical operations (`catch (Exception ignored) {}`)

**Error Types:**
- Return `false` for task failures (activate/execute pattern)
- Continue execution on non-critical errors (stats, webhook)
- Stop script for critical failures (no chisel, wrong level)

**Logging:**
- `script.log(Class, message)` for class-based logging
- Lowercase, blunt comment style as per CLAUDE.md
- Examples: `"no chisel, stopping"`, `"withdraw failed"`

## Logging

**Framework:**
- Built-in OSMB `script.log()` method
- Class-based logging: `script.log(BankingUtils.class, "...")`
- No external logging framework

**Patterns:**
- Log state transitions (`"starting bank task"`, `"setup complete"`)
- Log retry attempts (`"description attempt X/10"`)
- Minimal logging in hot paths (poll loop)

**Where:**
- Log in task activation/execution
- Log in utility method failures
- Paint overlay for user-visible stats

## Comments

**When to Comment:**
- Lowercase, blunt style (per CLAUDE.md)
- Explain complex game mechanics
- Document hardcoded values (coordinates, colors)
- Avoid obvious comments

**JSDoc/TSDoc:**
- Javadoc for public utility methods (`@param`, `@return`)
- Class-level documentation for utilities
- Minimal documentation in task classes

**TODO Comments:**
- Format: `// TODO: description`
- No username prefix (use git blame)
- Examples: `// TODO: Not 100% sure these are correct`

## Function Design

**Size:**
- Keep under 50 lines where practical
- Extract complex logic to private helper methods
- Large strategy implementations accepted (1000+ lines for complex game logic)

**Parameters:**
- Max 3-4 parameters typically
- First parameter often `Script script` for utility methods
- Use descriptive names (`maxAttempts`, `description`)

**Return Values:**
- Boolean returns for success/failure patterns
- Return early for guard clauses
- Explicit returns preferred

## Module Design

**Exports:**
- Public classes for external use
- Package-private for internal helpers
- Static fields on main script for shared state (not ideal but consistent)

**Barrel Files:**
- Not applicable (Java packages don't use barrel files)
- Each class in its own file (Java convention)

---

*Convention analysis: 2026-01-15*
*Update when patterns change*
