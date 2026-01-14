# Codebase Structure

**Analysis Date:** 2026-01-14

## Directory Layout

```
tidals-scripts/
├── utilities/              # Shared utilities JAR
├── TidalsGemCutter/        # Gem cutting script
├── TidalsCannonballThiever/# Cannonball smelting script
├── TidalsGoldSuperheater/  # Gold superheating script
├── TidalsSecondaryCollector/# Secondary collecting script
├── TidalsWithdrawer/       # Simple test script
├── examples/               # Reference implementations
├── docs/                   # API documentation (48 files)
├── API/                    # OSMB SDK (provided)
├── .planning/              # Project planning documents
└── script-dashboard/       # (sibling project - stats dashboard)
```

## Directory Purposes

**utilities/**
- Purpose: Shared utility library compiled to JAR
- Contains: RetryUtils, BankingUtils, DialogueUtils, TabUtils, BankSearchUtils
- Key files:
  - `src/main/java/utilities/RetryUtils.java` - Menu interaction retry wrapper
  - `src/main/java/utilities/BankingUtils.java` - Banking operations
  - `src/main/java/utilities/BankSearchUtils.java` - Bank search by name
  - `jar/TidalsUtilities.jar` - Output consumed by scripts
- Subdirectories: `src/main/java/utilities/` (8 Java files)

**TidalsGemCutter/** (example script structure)
- Purpose: Gem cutting automation script
- Contains: Main script, tasks, utilities, configuration
- Key files:
  - `src/main/java/main/TidalsGemCutter.java` - Main script (722 lines)
  - `src/main/java/main/ScriptUI.java` - JavaFX setup UI
  - `src/main/java/tasks/Setup.java` - Prerequisite validation
  - `src/main/java/tasks/Process.java` - Gem cutting logic
  - `src/main/java/tasks/Bank.java` - Banking logic
  - `src/main/java/utils/Task.java` - Abstract task base
  - `src/main/java/obf/Secrets.java` - API credentials (gitignored)
  - `jar/TidalsGemCutter.jar` - Output deployable
- Subdirectories: `main/`, `tasks/`, `utils/`, `obf/`, `resources/`

**TidalsCannonballThiever/**
- Purpose: Cannonball smelting with guard avoidance
- Key files:
  - `src/main/java/main/TidalsCannonballThiever.java` - Main script (738 lines)
  - `src/main/java/utils/GuardTracker.java` - Guard AI detection (931 lines)
  - 14 task files for complex state machine

**examples/**
- Purpose: Reference implementations from other developers
- Contains: dAmethystMiner, dLooter, dSunbleakWCer
- Key files: Various example patterns for learning

**docs/**
- Purpose: API reference and development guidance
- Contains: 48 markdown files documenting OSMB API
- Key files:
  - `api-reference.md` - Complete API methods
  - `critical-concepts.md` - Color bot fundamentals
  - `Walker.md` - Walking/NPC interaction
  - `Paint.md` - Paint overlay standard
  - `Common-menu-entries.md` - Exact menu action strings
  - `Reporting-data.md` - Stats dashboard integration

**API/**
- Purpose: OSMB SDK (read-only provided dependency)
- Contains: `API.jar` compiled against by all scripts
- Key files: `API.jar`

## Key File Locations

**Entry Points:**
- Script main: `[ScriptName]/src/main/java/main/[ScriptName].java`
- Script UI: `[ScriptName]/src/main/java/main/ScriptUI.java`
- Build root: `settings.gradle`

**Configuration:**
- Script build: `[ScriptName]/build.gradle`
- Secrets: `[ScriptName]/src/main/java/obf/Secrets.java` (gitignored)
- Gradle wrapper: `gradlew`, `gradlew.bat`

**Core Logic:**
- Tasks: `[ScriptName]/src/main/java/tasks/*.java`
- Utilities: `utilities/src/main/java/utilities/*.java`
- Task base: `[ScriptName]/src/main/java/utils/Task.java`

**Output:**
- Utilities JAR: `utilities/jar/TidalsUtilities.jar`
- Script JARs: `[ScriptName]/jar/[ScriptName].jar`

**Documentation:**
- API docs: `docs/*.md`
- Project guide: `CLAUDE.md`

## Naming Conventions

**Files:**
- PascalCase for Java classes: `TidalsGemCutter.java`, `BankingUtils.java`
- PascalCase for task files: `Setup.java`, `Process.java`, `Bank.java`
- kebab-case for markdown: `api-reference.md`, `Common-menu-entries.md`
- UPPERCASE for important docs: `CLAUDE.md`, `README.md`

**Directories:**
- camelCase for script modules: `TidalsGemCutter/`, `TidalsCannonballThiever/`
- lowercase for packages: `main/`, `tasks/`, `utils/`, `obf/`, `utilities/`
- lowercase plural for collections: `examples/`, `docs/`

**Special Patterns:**
- `Tidals` prefix for all production scripts
- `obf/Secrets.java` for credentials (gitignored)
- `jar/` subdirectory for compiled output
- `resources/logo.png` for script branding

## Where to Add New Code

**New Script:**
- Create: `Tidals[Name]/` directory
- Implementation: `src/main/java/main/Tidals[Name].java`
- UI: `src/main/java/main/ScriptUI.java`
- Tasks: `src/main/java/tasks/*.java`
- Build config: `build.gradle` (copy from existing script)

**New Task (existing script):**
- Implementation: `[ScriptName]/src/main/java/tasks/[TaskName].java`
- Registration: Add to task list in main script `onStart()`

**New Utility (shared):**
- Implementation: `utilities/src/main/java/utilities/[Name]Utils.java`
- Rebuild: `gradle :utilities:build`
- Rebuild dependent scripts

**New Documentation:**
- API doc: `docs/[topic].md`
- Planning doc: `.planning/[document].md`

## Special Directories

**utilities/jar/**
- Purpose: Compiled shared utilities
- Source: Built from `utilities/src/`
- Committed: Yes (JAR is version controlled)

**[ScriptName]/jar/**
- Purpose: Compiled script output
- Source: Built from script sources
- Committed: Yes (for deployment convenience)

**.planning/**
- Purpose: Project planning and codebase analysis
- Source: Generated by planning tools
- Committed: Yes

**examples/**
- Purpose: Reference implementations (not maintained)
- Source: Copied from other developers
- Committed: Yes (read-only reference)

---

*Structure analysis: 2026-01-14*
*Update when directory structure changes*
