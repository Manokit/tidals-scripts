# Codebase Concerns

**Analysis Date:** 2026-01-16

## Tech Debt

**Static Mutable State in Scripts:**
- Issue: Multiple scripts use `public static` mutable fields for runtime state (counters, flags, level tracking)
- Files:
  - `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java` (lines 49-95)
  - `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (lines 54-83)
  - `TidalsSecondaryCollector/src/main/java/main/TidalsSecondaryCollector.java` (lines 40-66)
- Impact: State persists across script restarts within the same JVM session. Can cause incorrect counts, stale data, or unexpected behavior if script is stopped and restarted without client restart.
- Fix approach: Move mutable state to instance fields, reset in `onStart()`, or use a dedicated state holder class that is explicitly reset.

**Duplicate Code Patterns Across Scripts:**
- Issue: Logo loading, paint overlay rendering, runtime formatting, and stats reporting code is nearly identical across all main script files
- Files:
  - `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java` (lines 630-680, 696-737)
  - `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (lines 494-540, 664-721)
  - `TidalsSecondaryCollector/src/main/java/main/TidalsSecondaryCollector.java` (lines 358-379, 401-430)
- Impact: Bug fixes or improvements must be applied to multiple files. Inconsistency risk.
- Fix approach: Extract common utilities to shared classes in `utilities/`: `LogoLoader`, `PaintUtils`, `StatsReporter`.

**BankSearchUtils is Oversized:**
- Issue: Single utility class at 1179 lines with multiple responsibilities (search, withdraw, scroll, verify, batch operations)
- Files: `utilities/src/main/java/utilities/BankSearchUtils.java`
- Impact: Difficult to maintain, test, or extend. High cognitive load when making changes.
- Fix approach: Split into focused classes: `BankSearcher`, `BankWithdrawer`, `BankSlotFinder`. Keep `BankSearchUtils` as a facade if backward compatibility needed.

**GuardTracker Complexity:**
- Issue: 931-line class with multiple tracking systems (position, pixel, XP-based, preemptive, sync) interleaved
- Files: `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java`
- Impact: High coupling, difficult to understand state transitions, fragile when modifying one tracking system
- Fix approach: Extract separate classes: `PositionTracker`, `PixelMovementDetector`, `XPCycleTracker`, `GuardSyncManager`. Have `GuardTracker` orchestrate them.

**XPTracking Duplicated Across Scripts:**
- Issue: Each script has its own copy of `XPTracking.java` with minor variations
- Files:
  - `TidalsCannonballThiever/src/main/java/utils/XPTracking.java`
  - `TidalsGemCutter/src/main/java/utils/XPTracking.java`
  - `TidalsGoldSuperheater/src/main/java/utils/XPTracking.java`
- Impact: Inconsistent implementations, duplicate maintenance
- Fix approach: Consolidate into `utilities/src/main/java/utilities/XPTracking.java`

## Known Bugs

**MortMyreFungusCollector - Unused Cluster Detection:**
- Symptoms: `findFungusClusters()` method (lines 591-636) is defined but never called
- Files: `TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java`
- Trigger: N/A - dead code
- Workaround: Currently using `detectFungusPositions()` which works via pixel detection per known log position

## Security Considerations

**API Keys in Obfuscated But Unprotected Files:**
- Risk: `obf/Secrets.java` contains STATS_URL and STATS_API credentials. While gitignored, the pattern relies on developer discipline.
- Files: Referenced in all main script files (e.g., `TidalsCannonballThiever.java` line 698, `TidalsGemCutter.java` line 681)
- Current mitigation: Files are gitignored
- Recommendations: Consider environment variables or encrypted config file with runtime decryption

**HTTP Stats Reporting Without HTTPS Verification:**
- Risk: Stats are sent over HTTP with API key in header. Man-in-middle possible if not using HTTPS.
- Files:
  - `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java` (lines 696-737)
  - `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (lines 678-721)
- Current mitigation: Timeout limits exposure
- Recommendations: Ensure STATS_URL uses HTTPS; add certificate validation if self-signed

## Performance Bottlenecks

**Inventory Checking Every Frame:**
- Problem: `TidalsCannonballThiever.onNewFrame()` calls `checkInventoryForChanges()` which searches inventory
- Files: `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java` (lines 249-282)
- Cause: Frame callbacks fire at 60fps; inventory search involves sprite detection
- Improvement path: Throttle to check every N frames (e.g., every 10 frames = 6 checks/sec still sufficient for thieving detection)

**BankSearchUtils Sprite Initialization:**
- Problem: First search call initializes sprite via `initSprite()` which can cause lag spike
- Files: `utilities/src/main/java/utilities/BankSearchUtils.java` (lines 75-99)
- Cause: Sprite loading from game assets is synchronous
- Improvement path: Add `preloadSprites()` method to call during script startup

## Fragile Areas

**MortMyreFungusCollector - Cached Inventory Count:**
- Files: `TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java` (lines 126-129, 146-159)
- Why fragile: `cachedInventoryCount` is incremented on each pickup without verification. If a pickup fails silently (game lag, misclick), count drifts from reality.
- Safe modification: Add periodic sync with actual inventory count (e.g., every N blooms or on state change)
- Test coverage: None detected

**GuardTracker State Reset:**
- Files: `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java` (lines 732-754)
- Why fragile: `resetGuardTracking()` must reset all tracking fields. Easy to forget new fields when adding features.
- Safe modification: Consider builder pattern or state object that can be wholesale replaced
- Test coverage: None detected

**Two-Stall Mode Coordination:**
- Files:
  - `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java` (static flags at lines 57-59)
  - `TidalsCannonballThiever/src/main/java/utils/GuardTracker.java` (XP cycle tracking)
  - `TidalsCannonballThiever/src/main/java/tasks/SwitchToOreStall.java`, `SwitchToCannonballStall.java`
- Why fragile: Coordination relies on multiple static flags (`twoStallMode`, `atOreStall`, `doingDepositRun`) and XP-based cycle detection. Race conditions possible if state changes while tasks execute.
- Safe modification: Introduce state machine with explicit transitions rather than flag checks

## Scaling Limits

**Single-Threaded Stats Reporting:**
- Current capacity: Stats sent synchronously in `poll()` loop
- Limit: If stats server slow/unresponsive, blocks script execution for up to 5 second timeout
- Scaling path: Move stats reporting to background thread (like webhook sending in `TidalsGemCutter`)

## Dependencies at Risk

**None Critical:**
- All dependencies are on OSMB framework APIs which are maintained by the platform
- No external Maven/Gradle dependencies beyond the framework

## Missing Critical Features

**No Graceful Shutdown Handling:**
- Problem: Scripts call `script.stop()` on fatal errors but don't always clean up state or report final stats
- Files: Multiple locations using `script.stop()` without prior cleanup
- Blocks: Stats may be lost if script stops unexpectedly before next reporting interval

**No Error Recovery in Banking:**
- Problem: Bank operations that fail (timeout, UI lag) often just return error codes without retry at the task level
- Files: `utilities/src/main/java/utilities/BankingUtils.java`, individual script Bank tasks
- Blocks: Scripts may get stuck if bank interaction fails due to transient game issues

## Test Coverage Gaps

**No Automated Tests:**
- What's not tested: Entire codebase lacks unit/integration tests
- Files: No test directories found (`src/test/` missing in all script modules)
- Risk: Regression risk high when modifying shared utilities. Logic changes in `BankSearchUtils`, `GuardTracker`, or `LoadoutRestocker` could break multiple scripts undetected.
- Priority: High - utilities affect all scripts

**Strategy Pattern Untested:**
- What's not tested: `SecondaryCollectorStrategy` interface and `MortMyreFungusCollector` implementation
- Files: `TidalsSecondaryCollector/src/main/java/strategies/`
- Risk: Adding new collectors (e.g., other herblore secondaries) may introduce bugs in shared state handling
- Priority: Medium

**Loadout System Complex But Untested:**
- What's not tested: `LoadoutManager`, `LoadoutComparator`, `LoadoutRestocker`, `LoadoutImporter`, `LoadoutExporter`
- Files: `utilities/src/main/java/utilities/loadout/`
- Risk: Fuzzy matching, quantity modes, equipment slot mapping all have edge cases
- Priority: High - loadout system is reusable across many scripts

---

*Concerns audit: 2026-01-16*
