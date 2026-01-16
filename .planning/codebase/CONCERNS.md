# Codebase Concerns

**Analysis Date:** 2026-01-15

## Tech Debt

**Swallowed Exception Handling:**
- Issue: Multiple files silently catch and ignore exceptions with `catch (Exception ignored) {}`
- Files:
  - `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (line 482)
  - `TidalsGoldSuperheater/src/main/java/main/TidalsGoldSuperheater.java` (lines 417, 557)
  - `TidalsSecondaryCollector/src/main/java/main/TidalsSecondaryCollector.java` (lines 370, 427)
  - `examples/dAmethystMiner/src/main/java/main/dAmethystMiner.java` (lines 425, 597)
- Why: Quick error suppression during rapid development
- Impact: Debugging difficult when failures occur silently
- Fix approach: Add `script.log()` calls to catch blocks at minimum

**Static Mutable State in Main Scripts:**
- Issue: Scripts use static fields modified during execution
- Files:
  - `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java`
  - `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java`
- Why: Simpler coordination between tasks than dependency injection
- Impact: Thread-safety issues if scripts could run concurrently
- Fix approach: Refactor to instance fields with constructor injection

**Large Files with Limited Documentation:**
- Issue: Complex files exceed 700+ lines with minimal JavaDoc
- Files:
  - `TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java` (1116 lines)
  - `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java` (931 lines)
  - `utilities/src/main/java/utilities/BankSearchUtils.java` (1072 lines)
  - `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java` (738 lines)
  - `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (722 lines)
- Why: Complex game logic requires significant code
- Impact: Maintenance difficulty, onboarding friction
- Fix approach: Add class-level JavaDoc, document complex algorithms

## Known Bugs

**None detected** - Codebase appears stable based on analysis.

## Security Considerations

**API Credentials Properly Handled:**
- Risk: API keys could be exposed
- Files: `<Script>/src/main/java/obf/Secrets.java`
- Current mitigation: `obf/` directories are `.gitignore`d
- Recommendations: Continue current pattern, verify .gitignore coverage

**Dashboard API Security (Good):**
- Files: `script-dashboard/src/app/api/stats/route.ts`
- Current mitigation:
  - Timing-safe API key comparison
  - Rate limiting (30 req/min per IP)
  - Input validation with sanitization
  - Request body size limits (10KB)
- Recommendations: Consider adding IP-based key expiration for rate limit map

## Performance Bottlenecks

**None detected** - Scripts are I/O bound by game interaction timing.

## Fragile Areas

**Hardcoded Magic Values:**
- Files:
  - `MortMyreFungusCollector.java` (lines 72-80): RGB color values (-7453660, -10933482, etc.)
  - `GuardTracker.java` (lines 26-40): Guard positions and timing delays
- Why fragile: Values are game-specific and may break with game updates
- Common failures: Detection fails if game colors shift
- Safe modification: Document values with game version, add fallback detection
- Test coverage: None - manual testing only

**Guard Tracking Algorithm:**
- File: `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java` (931 lines)
- Why fragile: Complex state machine tracking NPC positions and XP timing
- Common failures: Guard position misdetection, timing desync
- Safe modification: Add comprehensive inline documentation before changes
- Test coverage: None

## Scaling Limits

**Rate Limit Map Memory:**
- File: `script-dashboard/src/app/api/stats/route.ts`
- Current capacity: In-memory Map grows indefinitely
- Limit: Server memory
- Symptoms at limit: Memory exhaustion if many IPs attack
- Scaling path: Add time-based expiration or switch to Redis

## Dependencies at Risk

**OSMB API (External Dependency):**
- Risk: Closed-source, updates not in our control
- File: `API/API.jar` (9.1MB)
- Impact: All scripts depend on this; breaking changes break everything
- Migration plan: None possible - locked to OSMB ecosystem

## Missing Critical Features

**Automated Test Suite:**
- Problem: No unit tests for utilities or script logic
- Files affected: All `utilities/src/main/java/utilities/*.java`
- Current workaround: Manual testing in OSMB client
- Blocks: Confident refactoring, regression detection
- Implementation complexity: Medium - need to mock OSMB API

## Test Coverage Gaps

**Utilities (100% Untested):**
- Files:
  - `utilities/src/main/java/utilities/RetryUtils.java`
  - `utilities/src/main/java/utilities/BankingUtils.java`
  - `utilities/src/main/java/utilities/BankSearchUtils.java`
  - `utilities/src/main/java/utilities/TabUtils.java`
  - `utilities/src/main/java/utilities/DialogueUtils.java`
- Risk: Retry logic bugs, banking failures, tab switching errors
- Priority: High - these are shared across all scripts
- Difficulty to test: Need to mock OSMB Script interface

**Task Logic (100% Untested):**
- Files: All `<Script>/src/main/java/tasks/*.java`
- Risk: State machine bugs could cause script failures
- Priority: Medium - tasks are script-specific
- Difficulty to test: Need to mock OSMB API extensively

## Documentation Gaps

**Algorithm Documentation:**
- Issue: Complex sprite detection and guard tracking lack explanation
- Files:
  - `MortMyreFungusCollector.java` - RGB color matching algorithm
  - `GuardTracker.java` - XP-based timing detection
  - `BankSearchUtils.java` - Sprite detection logic
- Risk: Future maintainers cannot understand or safely modify
- Fix approach: Add comprehensive inline comments explaining game mechanics

---

*Concerns audit: 2026-01-15*
*Update as issues are fixed or new ones discovered*
