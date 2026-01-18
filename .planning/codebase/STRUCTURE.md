# Codebase Structure

**Analysis Date:** 2026-01-16

## Directory Layout

```
TidalsSecondaryCollector-WT/
├── API/                              # OSMB API jar (external dependency)
│   └── API.jar
├── docs/                             # Documentation for OSMB scripting
├── examples/                         # Reference scripts from other authors
│   ├── dAmethystMiner/
│   ├── dLooter/
│   ├── dSunbleakWCer/
│   └── BlisterwoodChopper.java
├── utilities/                        # Shared utility library (TidalsUtilities.jar)
│   ├── src/main/java/utilities/
│   └── jar/TidalsUtilities.jar
├── TidalsGemCutter/                  # Gem cutting script
├── TidalsGoldSuperheater/            # Gold superheating script
├── TidalsCannonballThiever/          # Port Roberts thieving script
├── TidalsSecondaryCollector/         # Herblore secondary collector script
├── TidalsBankTester/                 # Bank utility test script
├── TidalsLoadoutTester/              # Loadout system test script
├── settings.gradle                   # Multi-project gradle config
├── gradlew                           # Gradle wrapper
├── CLAUDE.md                         # Development guide for Claude
└── .planning/                        # GSD planning documents
```

## Directory Purposes

**Script Directories (Tidal*):**
- Purpose: Individual OSMB automation scripts
- Contains: Gradle project with src/main/java structure
- Key files: `main/{ScriptName}.java`, `main/ScriptUI.java`, `tasks/*.java`, `utils/Task.java`

**utilities/:**
- Purpose: Shared code library used by all scripts
- Contains: RetryUtils, BankingUtils, BankSearchUtils, DialogueUtils, TabUtils, Loadout system
- Key files: `src/main/java/utilities/*.java`, `jar/TidalsUtilities.jar`

**docs/:**
- Purpose: OSMB API documentation and scripting guides
- Contains: Markdown files covering API reference, patterns, common mistakes
- Key files: `api-reference.md`, `critical-concepts.md`, `banking-patterns.md`, `Walker.md`

**examples/:**
- Purpose: Reference implementations from other script authors
- Contains: Complete script projects showing different patterns
- Key files: Various example scripts demonstrating OSMB patterns

**API/:**
- Purpose: OSMB framework dependency
- Contains: Pre-built API jar
- Key files: `API.jar` - the OSMB scripting framework

## Key File Locations

**Entry Points:**
- `{ScriptName}/src/main/java/main/{ScriptName}.java`: Main script class with @ScriptDefinition
- `{ScriptName}/src/main/java/main/ScriptUI.java`: JavaFX configuration dialog

**Configuration:**
- `settings.gradle`: Root gradle config that auto-discovers script subprojects
- `CLAUDE.md`: Development conventions and API guidance

**Core Logic:**
- `{ScriptName}/src/main/java/tasks/*.java`: Task implementations
- `{ScriptName}/src/main/java/strategies/*.java`: Strategy implementations (if applicable)
- `{ScriptName}/src/main/java/utils/Task.java`: Task interface/base class

**Shared Utilities:**
- `utilities/src/main/java/utilities/RetryUtils.java`: Menu interaction retries
- `utilities/src/main/java/utilities/BankingUtils.java`: Bank operations
- `utilities/src/main/java/utilities/BankSearchUtils.java`: Bank search and withdraw
- `utilities/src/main/java/utilities/TabUtils.java`: Tab management
- `utilities/src/main/java/utilities/DialogueUtils.java`: Dialogue handling
- `utilities/src/main/java/utilities/loadout/*.java`: Loadout management system

**Resources:**
- `{ScriptName}/src/main/resources/logo.png`: Script overlay logo
- `{ScriptName}/src/main/resources/style.css`: JavaFX styling (if present)

**Testing:**
- No formal test framework - use `TidalsBankTester`, `TidalsLoadoutTester` for manual testing

## Naming Conventions

**Files:**
- Main script: `Tidals{Feature}.java` (e.g., `TidalsGemCutter.java`)
- UI class: `ScriptUI.java` (consistent across all scripts)
- Tasks: `{Action}.java` (e.g., `Bank.java`, `Process.java`, `Setup.java`)
- Utilities: `{Domain}Utils.java` (e.g., `RetryUtils.java`, `BankingUtils.java`)

**Directories:**
- Scripts: `Tidals{Feature}/` (PascalCase with Tidals prefix)
- Java packages: lowercase (`main`, `tasks`, `utils`, `strategies`)
- Source layout: `src/main/java/` (standard Gradle)

**Classes:**
- Main script: Same as directory name
- Tasks: Action-based naming (Setup, Process, Bank, Retreat, etc.)
- Strategies: `{Activity}Collector.java` or `{Activity}Strategy.java`

## Where to Add New Code

**New Script:**
1. Create directory: `Tidals{ScriptName}/`
2. Create structure:
   ```
   Tidals{ScriptName}/
   ├── src/main/java/
   │   ├── main/
   │   │   ├── Tidals{ScriptName}.java
   │   │   └── ScriptUI.java
   │   ├── tasks/
   │   │   ├── Setup.java
   │   │   └── {OtherTasks}.java
   │   └── utils/
   │       └── Task.java
   └── src/main/resources/
       └── logo.png
   ```
3. Add to settings.gradle (auto-discovered if build.gradle exists)

**New Task:**
- Location: `{ScriptName}/src/main/java/tasks/{TaskName}.java`
- Extend Task base class from `utils/Task.java`
- Add to task list in main script `onStart()`

**New Strategy:**
- Location: `{ScriptName}/src/main/java/strategies/{StrategyName}.java`
- Implement strategy interface (e.g., `SecondaryCollectorStrategy`)
- Add selection logic in main script

**New Utility:**
- Location: `utilities/src/main/java/utilities/{UtilName}.java`
- Add static methods following existing patterns
- Rebuild utilities jar: `JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle :utilities:build`

**New Resource:**
- Script-specific: `{ScriptName}/src/main/resources/`
- Shared (loadout): `utilities/src/main/resources/loadout/`

## Special Directories

**.planning/:**
- Purpose: GSD planning and codebase analysis documents
- Generated: Yes (by GSD commands)
- Committed: Yes

**{ScriptName}/jar/:**
- Purpose: Built script jars for deployment
- Generated: Yes (by `osmb build`)
- Committed: No (gitignored)

**API/:**
- Purpose: External OSMB framework dependency
- Generated: No (downloaded/provided)
- Committed: Yes

**utilities/jar/:**
- Purpose: Built utilities jar for script dependencies
- Generated: Yes (by gradle build)
- Committed: Yes (needed by scripts)

**obf/:**
- Purpose: Secrets and API keys (per-script)
- Generated: No (manually created)
- Committed: No (gitignored)
- Template:
  ```java
  package obf;
  public class Secrets {
      public static String STATS_URL = "https://your-dashboard/api/stats";
      public static String STATS_API = "your-api-key";
  }
  ```

---

*Structure analysis: 2026-01-16*
