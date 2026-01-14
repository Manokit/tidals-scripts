# Codebase Concerns

**Analysis Date:** 2026-01-14

## Tech Debt

**Large Complex Files:**
- Issue: Multiple files exceed 600+ lines, increasing cognitive load
- Files:
  - `TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java` (1116 lines)
  - `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java` (931 lines)
  - `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java` (738 lines)
  - `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (722 lines)
  - `utilities/src/main/java/utilities/BankSearchUtils.java` (648 lines)
- Why: Organic growth during development, complex state machines
- Impact: Difficult to test, maintain, and understand
- Fix approach: Extract state machine logic into separate strategy classes

**In-Memory Rate Limiting:**
- Issue: Rate limiting uses in-memory Map, won't work with multiple instances
- File: `script-dashboard/src/app/api/stats/route.ts:4-20`
- Why: Simple implementation for single-instance deployment
- Impact: Rate limiting ineffective in scaled deployments; potential memory leak
- Fix approach: Use Redis or database-backed rate limiting

## Known Bugs

**No critical bugs identified**

## Security Considerations

**Custom Timing-Safe Comparison:**
- Risk: Proprietary implementation instead of crypto.timingSafeEqual()
- Files: `script-dashboard/src/app/api/stats/route.ts:56-67, 203-214`
- Current mitigation: Implementation follows correct pattern (XOR accumulation)
- Recommendations: Replace with `crypto.timingSafeEqual()` from Node.js standard library

**Missing Environment Documentation:**
- Risk: Developers may commit secrets or miss required configuration
- Files: No `obf/Secrets.example.java` template exists
- Current mitigation: `.gitignore` excludes `Secrets.java`
- Recommendations: Create `Secrets.example.java` with placeholder values

## Performance Bottlenecks

**No significant bottlenecks identified**

Scripts run in single-threaded OSMB client; dashboard handles low volume.

## Fragile Areas

**GuardTracker State Machine:**
- File: `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java`
- Why fragile: Multiple timestamp fields without synchronization, complex state transitions
- Common failures: Guard detection could malfunction without clear logging
- Safe modification: Add comprehensive logging for state transitions
- Test coverage: None - critical logic untested

**Bank Search Fallback Logic:**
- File: `utilities/src/main/java/utilities/BankSearchUtils.java:403`
- Why fragile: Hardcoded scroll iterations (30), magic pixel offsets (90px, 110px)
- Common failures: Could fail silently if OSRS changes bank search behavior
- Safe modification: Extract constants with documentation
- Test coverage: None

**Static Inter-Task Communication:**
- Files: All `main/[ScriptName].java` with `public static` fields
- Why fragile: Shared mutable state across tasks without synchronization
- Common failures: State corruption if tasks execute unexpectedly
- Safe modification: Use proper state container class
- Test coverage: None

## Scaling Limits

**SQLite Database:**
- Current capacity: Single-file database, sufficient for current scale
- Limit: Concurrent write performance, file locking
- Symptoms at limit: Slow writes, potential lock contention
- Scaling path: Migrate to PostgreSQL if needed

**In-Memory Rate Limit Map:**
- Current capacity: Single instance only
- Limit: Map grows unbounded without cleanup
- Symptoms at limit: Memory growth, ineffective limiting
- Scaling path: Redis-backed rate limiting

## Dependencies at Risk

**No deprecated or unmaintained dependencies detected**

Dashboard dependencies are current (Next.js 16, React 19, Prisma 6).

## Missing Critical Features

**Test Coverage:**
- Problem: No unit tests for utilities, no integration tests for API
- Current workaround: Manual testing through OSMB client
- Blocks: Safe refactoring, regression detection
- Implementation complexity: Medium (add JUnit 5, Vitest)

**Secrets Template:**
- Problem: No example file for `obf/Secrets.java`
- Current workaround: Developers must discover required fields
- Blocks: New developer onboarding
- Implementation complexity: Low (create example file)

## Test Coverage Gaps

**Utility Classes:**
- What's not tested: RetryUtils, BankingUtils, DialogueUtils, TabUtils, BankSearchUtils
- Risk: Regressions undetected when modifying shared code
- Priority: High
- Difficulty to test: Medium (need OSMB API mocking)

**Guard Tracking Logic:**
- What's not tested: GuardTracker state machine (931 lines)
- File: `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java`
- Risk: Guard avoidance could break without detection
- Priority: High
- Difficulty to test: High (complex game state simulation needed)

**API Endpoints:**
- What's not tested: Stats submission and aggregation
- File: `script-dashboard/src/app/api/stats/route.ts`
- Risk: Stats reporting could break silently
- Priority: Medium
- Difficulty to test: Low (standard API testing)

## Documentation Gaps

**Complex Code Without Comments:**
- File: `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java`
- What's missing: Explanation of timing constants, state transition model
- Impact: Difficult to understand guard detection logic

**Magic Numbers:**
- Files: Throughout Java scripts
- Examples:
  - `BankSearchUtils.java:580-581` - Bank UI offsets (90px, 110px) unexplained
  - `GuardTracker.java:26-63` - Patrol position constants without source
- Impact: When OSRS updates, difficult to know what values need changing

---

*Concerns audit: 2026-01-14*
*Update as issues are fixed or new ones discovered*
