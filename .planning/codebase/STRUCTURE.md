# Codebase Structure

**Analysis Date:** 2026-01-15

## Directory Layout

```
tidals-scripts/
├── API/                    # OSMB framework library (closed-source)
├── utilities/              # Shared utility library
├── TidalsCannonballThiever/# Thieving script (11 tasks)
├── TidalsGemCutter/        # Gem cutting script
├── TidalsGoldSuperheater/  # Gold bar superheating script
├── TidalsSecondaryCollector/# Herblore secondary collector
├── TidalsWithdrawer/       # Simple bank withdrawer
├── docs/                   # API reference documentation (49 files)
├── examples/               # Example scripts for reference
├── .planning/              # GSD planning documents
├── settings.gradle         # Gradle multi-project config
├── CLAUDE.md               # Development instructions
└── README.md               # Project overview
```

## Directory Purposes

**API/**
- Purpose: OSMB game automation framework
- Contains: `API.jar` (9.1MB closed-source)
- Key files: `API.jar` - Core game interaction API
- Subdirectories: None

**utilities/**
- Purpose: Shared code library compiled to JAR
- Contains: Utility classes for banking, retries, dialogues, tabs
- Key files:
  - `src/main/java/utilities/RetryUtils.java` - Menu interaction retries
  - `src/main/java/utilities/BankingUtils.java` - Banking operations
  - `src/main/java/utilities/BankSearchUtils.java` - Bank search control
  - `src/main/java/utilities/TabUtils.java` - Tab management
  - `src/main/java/utilities/DialogueUtils.java` - Dialogue handling
  - `jar/TidalsUtilities.jar` - Compiled output
- Subdirectories: `src/main/java/utilities/`

**TidalsGemCutter/**
- Purpose: Gem cutting and bolt tip crafting automation
- Contains: Main script, UI, tasks, XP tracking
- Key files:
  - `src/main/java/main/TidalsGemCutter.java` (722 lines)
  - `src/main/java/main/ScriptUI.java`
  - `src/main/java/tasks/Setup.java`, `Process.java`, `Bank.java`
  - `jar/TidalsGemCutter.jar`
- Subdirectories: `main/`, `tasks/`, `utils/`, `obf/`

**TidalsCannonballThiever/**
- Purpose: Thieving automation with guard tracking
- Contains: 11 task files for complex state machine
- Key files:
  - `src/main/java/main/TidalsCannonballThiever.java`
  - `src/main/java/utils/GuardTracker.java` (931 lines)
  - `src/main/java/tasks/*.java` (11 task files)
- Subdirectories: `main/`, `tasks/`, `utils/`, `obf/`

**TidalsSecondaryCollector/**
- Purpose: Herblore secondary ingredient collection
- Contains: Strategy pattern for different secondaries
- Key files:
  - `src/main/java/main/TidalsSecondaryCollector.java`
  - `src/main/java/strategies/MortMyreFungusCollector.java` (1116 lines)
  - `src/main/java/strategies/SecondaryCollectorStrategy.java`
- Subdirectories: `main/`, `strategies/`, `obf/`

**TidalsWithdrawer/**
- Purpose: Simple bank item withdrawal
- Contains: Minimal script without task pattern
- Key files:
  - `src/main/java/main/TidalsWithdrawer.java` (179 lines)
  - `src/main/java/main/ScriptUI.java`
- Subdirectories: `main/`, `obf/`

**docs/**
- Purpose: Comprehensive API reference and patterns
- Contains: 49 markdown documentation files
- Key files:
  - `api-reference.md` - Complete API methods
  - `critical-concepts.md` - Color bot fundamentals
  - `banking-patterns.md` - Banking guide
  - `Walker.md` - Pathfinding patterns
  - `Paint.md` - Overlay rendering standard
  - `Reporting-data.md` - Stats API integration
- Subdirectories: None

## Key File Locations

**Entry Points:**
- `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` - Gem cutter entry
- `TidalsCannonballThiever/src/main/java/main/TidalsCannonballThiever.java` - Thieving entry
- `TidalsSecondaryCollector/src/main/java/main/TidalsSecondaryCollector.java` - Collector entry

**Configuration:**
- `settings.gradle` - Gradle module discovery
- `TidalsGemCutter/build.gradle` - Script build configuration
- `utilities/build.gradle` - Utilities build configuration

**Core Logic:**
- `utilities/src/main/java/utilities/RetryUtils.java` - Retry wrapper
- `utilities/src/main/java/utilities/BankingUtils.java` - Banking operations
- `utilities/src/main/java/utilities/BankSearchUtils.java` - Search functionality

**Testing:**
- Not detected - Manual testing via OSMB client

**Documentation:**
- `CLAUDE.md` - Development instructions for Claude Code
- `docs/*.md` - API reference files

## Naming Conventions

**Files:**
- PascalCase.java for all Java classes (`TidalsGemCutter.java`, `RetryUtils.java`)
- kebab-case.md for documentation (`api-reference.md`, `banking-patterns.md`)
- PascalCase directories for scripts (`TidalsGemCutter/`, `TidalsWithdrawer/`)

**Directories:**
- lowercase for standard directories (`main/`, `tasks/`, `utils/`, `obf/`)
- PascalCase for script project directories (`TidalsGemCutter/`)
- lowercase for shared resources (`docs/`, `utilities/`, `examples/`)

**Special Patterns:**
- `<Script>/jar/<Script>.jar` - Build output location
- `<Script>/src/main/java/obf/Secrets.java` - API credentials (gitignored)
- `<Script>/src/main/resources/logo.png` - Script logo asset

## Where to Add New Code

**New Script:**
- Primary code: `<ScriptName>/src/main/java/main/<ScriptName>.java`
- UI: `<ScriptName>/src/main/java/main/ScriptUI.java`
- Tasks: `<ScriptName>/src/main/java/tasks/*.java`
- Utils: `<ScriptName>/src/main/java/utils/*.java`
- Build: `<ScriptName>/build.gradle`
- Secrets: `<ScriptName>/src/main/java/obf/Secrets.java`

**New Task:**
- Implementation: `<Script>/src/main/java/tasks/<TaskName>.java`
- Base class: Extend `utils.Task`
- Registration: Add to task list in main script

**New Utility:**
- Implementation: `utilities/src/main/java/utilities/<Name>Utils.java`
- Build: Run `gradle :utilities:build`
- Usage: Add JAR dependency to consuming scripts

**New Documentation:**
- Implementation: `docs/<topic>.md`
- Reference: Update `CLAUDE.md` if needed

## Special Directories

**obf/**
- Purpose: Secrets storage (API keys, URLs)
- Source: Created per-script with `Secrets.java`
- Committed: No (`.gitignore` excludes `*/obf/`)

**jar/**
- Purpose: Compiled JAR output
- Source: Generated by Gradle build
- Committed: Partial (main JARs tracked, build artifacts ignored)

**.planning/**
- Purpose: GSD project planning documents
- Source: Generated by GSD workflow
- Committed: Yes (tracked for project continuity)

---

*Structure analysis: 2026-01-15*
*Update when directory structure changes*
