# Testing Patterns

**Analysis Date:** 2026-01-15

## Test Framework

**Runner:**
- Not detected - No automated test framework configured
- Manual integration testing via OSMB client

**Assertion Library:**
- Not applicable - No unit tests

**Run Commands:**
```bash
# No automated tests - manual testing workflow:
osmb build TidalsGemCutter    # Build the script
# Then load JAR in OSMB client and test manually
```

## Test File Organization

**Location:**
- No test files detected (`*.test.java`, `*Test.java`, `*Tests.java`)
- No `src/test/java/` directories in any script

**Naming:**
- Not applicable

**Structure:**
```
# No test structure exists
# Scripts are tested manually in OSMB client
```

## Test Structure

**Suite Organization:**
Not applicable - no automated tests.

**Patterns:**
- Manual QA in OSMB client with actual game
- Paint overlay displays live stats for verification
- Debug logging via `script.log()` for troubleshooting
- Discord webhook screenshots for remote monitoring

## Mocking

**Framework:**
- Not applicable - no unit tests

**What Would Need Mocking:**
- OSMB API calls (game interaction)
- Network requests (stats API, webhooks)
- UI components (JavaFX)

## Fixtures and Factories

**Test Data:**
- Not applicable - scripts run against live game

**Location:**
- None

## Coverage

**Requirements:**
- No coverage targets (no automated tests)
- Manual verification via paint overlay stats

**Configuration:**
- Not applicable

**View Coverage:**
- Not applicable

## Test Types

**Unit Tests:**
- Not implemented
- Would benefit utilities (RetryUtils, BankingUtils)

**Integration Tests:**
- Manual only
- Run script in OSMB client with actual game

**E2E Tests:**
- Manual only
- Full script execution in game environment

## Common Patterns

**Manual Testing Workflow:**
```bash
# 1. Build the script
osmb build TidalsGemCutter

# 2. Load JAR in OSMB client
# 3. Configure script settings via UI
# 4. Monitor paint overlay for:
#    - Runtime, XP/hour, items processed
#    - Current task state
#    - Error messages in log

# 5. Check script.log() output for errors
# 6. Discord webhook for remote monitoring
```

**Verification via Code:**
```java
// Defensive null checks throughout
if (inv == null) return false;

// Movement tracking for state verification
AtomicReference<Timer> lastMovement = new AtomicReference<>();
script.pollFramesUntil(() -> !lastMovement.get().timeRemaining(), timeout, true);

// Inventory counts for action verification
int beforeCount = inv.getAmount(ItemID.UNCUT_SAPPHIRE);
// ... perform action ...
int afterCount = inv.getAmount(ItemID.UNCUT_SAPPHIRE);
if (afterCount < beforeCount) { /* success */ }
```

**Debug Logging:**
```java
// Class-based logging
script.log(Setup.class, "checking requirements...");
script.log(Bank.class, "withdraw failed");
script.log(Process.class, "crafted " + count + " gems");

// Retry logging pattern
script.log(getClass(), description + " attempt " + attempt + "/" + maxAttempts);
```

## Recommended Test Additions

**Priority Areas:**
1. `utilities/src/main/java/utilities/RetryUtils.java` - Retry logic
2. `utilities/src/main/java/utilities/BankingUtils.java` - Banking operations
3. `utilities/src/main/java/utilities/BankSearchUtils.java` - Search functionality

**Testing Approach:**
- Mock OSMB API interfaces
- Verify retry behavior
- Test edge cases (null returns, timeouts)

---

*Testing analysis: 2026-01-15*
*Update when test patterns change*
