# Codebase Concerns

**Analysis Date:** 2026-01-13

## Tech Debt

**Duplicate Bank Query Definitions:**
- Issue: BANK_NAMES, BANK_ACTIONS, and bankQuery predicate copied across scripts
- Files: `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (lines 108-114), `TidalsGoldSuperheater/src/main/java/main/TidalsGoldSuperheater.java` (lines 98-104)
- Why: Each script added independently before BankingUtils existed
- Impact: Changes must be made in multiple places, risk of inconsistency
- Fix approach: Remove duplicates and use `BankingUtils.BANK_QUERY` from shared utilities

**Duplicate Webhook Implementation:**
- Issue: ~120 lines of webhook code duplicated across scripts
- Files: `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (lines 542-662), `TidalsGoldSuperheater/src/main/java/main/TidalsGoldSuperheater.java`
- Why: Copy-paste when adding webhook feature to multiple scripts
- Impact: Bug fixes must be applied multiple times
- Fix approach: Extract to shared `WebhookUtils` in utilities JAR

**Static State Management:**
- Issue: Heavy use of static fields for script state (14+ static fields per script)
- Files: `TidalsGemCutter.java`, `TidalsCannonballThiever.java`, `TidalsGoldSuperheater.java`
- Why: Quick initial development approach
- Impact: State persists unpredictably, testing difficult, accidental mutations possible
- Fix approach: Convert to instance fields or explicit state objects

## Known Bugs

**Silent Exception Swallowing:**
- Symptoms: Inventory tracking failures are silently ignored
- Trigger: Exception during inventory snapshot creation
- Files: `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java` (lines 374-375)
- Workaround: None - failures are hidden
- Root cause: Empty catch block with no logging
- Fix: Add proper logging: `script.log("ERROR", "Inventory tracking failed: " + e.getMessage())`

**Additional empty catch blocks:**
- Files: `examples/dAmethystMiner/src/main/java/tasks/MineTask.java`, `examples/dLooter/src/main/java/main/dLooter.java`, `examples/dSunbleakWCer/src/main/java/main/dSunbleakWCer.java`

## Security Considerations

**Secrets Management (Properly Handled):**
- Risk: API keys could be exposed
- Current mitigation: All secrets in gitignored `obf/Secrets.java` files
- Files: `*/obf/Secrets.java` (all scripts)
- Recommendations: Current approach is adequate; document pattern for new scripts

**Input Validation (Dashboard - Good):**
- Risk: Malformed data from scripts
- Current mitigation: Comprehensive validation in `script-dashboard/src/app/api/stats/route.ts` (lines 29-35, 81-102)
- Includes: Length limits, type checking, sanitization, rate limiting

**Input Validation (Scripts - Weak):**
- Risk: Scripts send unvalidated data to API
- Files: `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (lines 678-721)
- Recommendations: Add validation before API transmission

## Performance Bottlenecks

**Memory Allocation in Tight Loops:**
- Problem: New ArrayList created every frame in guard tracking
- Files: `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java` (line 45)
- Measurement: Not measured - potential GC pressure
- Cause: `new ArrayList<>()` in frequently-called `getNpcPositions()`
- Improvement path: Reuse collection or use array

**Inefficient Collection Creation:**
- Problem: HashSet created every poll cycle
- Files: `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java` (lines 308-312)
- Improvement path: Reuse collections, clear instead of recreate

## Fragile Areas

**GuardTracker (931 lines):**
- Files: `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java`
- Why fragile: Complex XP tracking, cycle management, guard position monitoring in single class
- Common failures: XP baseline reset, cycle count mismatch
- Safe modification: Understand XP cycle logic before changes
- Test coverage: None - manual testing only

**Webhook Threading:**
- Files: `TidalsGemCutter.java` (lines 76-78)
- Why fragile: Uses AtomicBoolean/AtomicReference but no explicit synchronization
- Common failures: Race conditions if multiple threads access state
- Safe modification: Review thread safety before changes

## Scaling Limits

**SQLite Database:**
- Current capacity: Suitable for single-user stats aggregation
- Limit: Concurrent writes limited by SQLite file locking
- Symptoms at limit: Database locked errors under high concurrency
- Scaling path: Migrate to PostgreSQL if multi-user needed

**Rate Limiting:**
- Current: 30 requests/minute per IP (in-memory Map)
- Limit: Memory grows with unique IPs, no cleanup
- Scaling path: Add periodic cleanup or use external rate limiter

## Dependencies at Risk

**None Detected:**
- Dashboard dependencies are actively maintained (Next.js 16, React 19, Prisma 6)
- OSMB API is external but required for functionality

## Missing Critical Features

**No Automated Testing:**
- Problem: Zero test coverage in both projects
- Current workaround: Manual testing in OSMB client
- Blocks: Confident refactoring, regression detection
- Implementation complexity: Medium - need to mock OSMB API for scripts

**No Error Alerting:**
- Problem: Errors only visible in console logs
- Current workaround: Check logs manually
- Blocks: Proactive issue detection
- Implementation complexity: Low - add Sentry or similar

## Test Coverage Gaps

**Entire Codebase:**
- What's not tested: All production code
- Risk: Regressions go unnoticed until manual testing
- Priority: High for utilities (shared across scripts)
- Difficulty to test: Requires OSMB API mocking

## Inconsistencies

**Stats Reporting Intervals:**
- `TidalsGemCutter.java`: 600,000ms (10 minutes)
- `TidalsGoldSuperheater.java`: 500,000ms (8.33 minutes)
- `TidalsCannonballThiever.java`: 600,000ms (10 minutes)
- Issue: Inconsistent makes aggregation harder to interpret
- Fix: Standardize to 600,000ms (10 minutes)

**Version Numbering:**
- Scripts use string versions (`"1.3"`, `"1.6"`) that don't match `@ScriptDefinition version = 1.0`
- Files: All main script files
- Fix: Sync version fields or remove redundancy

---

*Concerns audit: 2026-01-13*
*Update as issues are fixed or new ones discovered*
