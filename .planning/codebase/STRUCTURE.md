# Codebase Structure

**Analysis Date:** 2026-01-13

## Directory Layout

```
tidals-scripts/
├── utilities/                    # Shared compiled utilities JAR
│   ├── src/main/java/utilities/  # RetryUtils, BankingUtils, etc.
│   ├── build.gradle
│   └── jar/TidalsUtilities.jar
│
├── TidalsGemCutter/              # Gem cutting/fletching script
│   ├── src/main/java/
│   │   ├── main/                 # Entry point + UI
│   │   ├── tasks/                # State machine tasks
│   │   ├── utils/                # XPTracking, Task base
│   │   └── obf/                  # Secrets (gitignored)
│   ├── build.gradle
│   └── jar/TidalsGemCutter.jar
│
├── TidalsCannonballThiever/      # Guard-avoidance thieving
│   └── (same structure)
│
├── TidalsGoldSuperheater/        # Smelting automation
│   └── (same structure)
│
├── TidalsSecondaryCollector/     # Multi-strategy collection
│   ├── strategies/               # Alternative: Strategy pattern
│   └── (otherwise same)
│
├── API/API.jar                   # OSMB core library
├── docs/                         # Development guides (37 files)
├── examples/                     # Reference implementations
├── settings.gradle               # Root project config
├── gradlew / gradlew.bat         # Gradle wrapper
└── CLAUDE.md                     # AI development guide
```

```
script-dashboard/
├── src/
│   ├── app/
│   │   ├── page.tsx              # Homepage
│   │   ├── stats/page.tsx        # Stats dashboard
│   │   ├── api/stats/route.ts    # API endpoint
│   │   ├── layout.tsx            # Root layout
│   │   └── globals.css           # Styles
│   ├── components/               # React components
│   └── lib/db.ts                 # Prisma singleton
├── prisma/schema.prisma          # Database schema
├── package.json
├── tsconfig.json
├── Dockerfile
└── .env.example
```

## Directory Purposes

**utilities/**
- Purpose: Shared compiled library for all scripts
- Contains: RetryUtils, BankingUtils, TabUtils, DialogueUtils
- Key files: `src/main/java/utilities/*.java`, `jar/TidalsUtilities.jar`
- Build: `gradle :utilities:build`

**Tidals{Name}/**
- Purpose: Individual automation script
- Contains: Main script, tasks, utils, secrets
- Subdirectories:
  - `main/` - Entry point (Tidals*.java) + UI (ScriptUI.java)
  - `tasks/` - Setup.java, Process.java, Bank.java
  - `utils/` - Task.java, XPTracking.java
  - `obf/` - Secrets.java (gitignored)
- Build output: `jar/{ScriptName}.jar`

**docs/**
- Purpose: Comprehensive development guides
- Contains: 37 markdown files covering API, patterns, concepts
- Key files: `api-reference.md`, `critical-concepts.md`, `Paint.md`, `Walker.md`, `Bank.md`, `Keyboard.md`

**examples/**
- Purpose: Reference implementations and learning material
- Contains: BlisterwoodChopper.java, other example scripts

**script-dashboard/src/app/**
- Purpose: Next.js App Router pages and API
- Key files: `page.tsx` (home), `stats/page.tsx`, `api/stats/route.ts`

**script-dashboard/src/components/**
- Purpose: Reusable React components
- Key files: `StatsChart.tsx`, `ScriptCard.tsx`, `StatCard.tsx`, `Navbar.tsx`

## Key File Locations

**Entry Points:**
- `Tidals*/src/main/java/main/Tidals*.java` - Script main class
- `script-dashboard/src/app/page.tsx` - Dashboard homepage
- `script-dashboard/src/app/api/stats/route.ts` - Stats API

**Configuration:**
- `settings.gradle` - Gradle root project
- `*/build.gradle` - Per-script build config
- `script-dashboard/tsconfig.json` - TypeScript config
- `script-dashboard/.env` - Environment variables

**Core Logic:**
- `utilities/src/main/java/utilities/` - Shared utilities
- `*/src/main/java/tasks/` - Script state machines
- `script-dashboard/src/app/api/stats/route.ts` - Stats aggregation

**Testing:**
- None - No formal test infrastructure

**Documentation:**
- `docs/` - 42 development guides
- `CLAUDE.md` - AI assistant instructions
- `script-dashboard/README.md` - Dashboard setup

## Naming Conventions

**Files (Java):**
- PascalCase.java for all classes: `TidalsGemCutter.java`, `Setup.java`
- Package-based organization: `main/`, `tasks/`, `utils/`, `obf/`

**Files (TypeScript):**
- PascalCase.tsx for components: `StatsChart.tsx`, `ScriptCard.tsx`
- lowercase.ts for utilities: `db.ts`
- route.ts for API routes

**Directories:**
- lowercase for packages: `main`, `tasks`, `utils`, `obf`
- PascalCase for script projects: `TidalsGemCutter`
- kebab-case for documentation: `api-reference.md`

**Special Patterns:**
- `Tidals*` prefix for all production scripts
- `obf/Secrets.java` for API credentials (gitignored)
- `jar/*.jar` for compiled output

## Where to Add New Code

**New Script:**
- Create: `Tidals{Name}/src/main/java/` with `main/`, `tasks/`, `utils/`, `obf/`
- Add: `build.gradle` based on existing script
- Entry: `main/Tidals{Name}.java` extends Script
- Tasks: `tasks/Setup.java`, `tasks/Process.java`, `tasks/Bank.java`

**New Utility:**
- Implementation: `utilities/src/main/java/utilities/{Name}Utils.java`
- Rebuild: `gradle :utilities:build`
- Update: Scripts using utility must be rebuilt

**New Dashboard Component:**
- Component: `script-dashboard/src/components/{Name}.tsx`
- Page: `script-dashboard/src/app/{path}/page.tsx`
- API: `script-dashboard/src/app/api/{path}/route.ts`

**New Task:**
- File: `{Script}/src/main/java/tasks/{Name}.java`
- Extend: `utils.Task` abstract class
- Register: Add to task list in main script

## Special Directories

**jar/**
- Purpose: Compiled script output
- Source: Generated by Gradle build
- Committed: Yes (distribution artifacts)

**obf/**
- Purpose: Secrets storage (API keys)
- Source: Manually created per script
- Committed: No (gitignored)

**.planning/**
- Purpose: GSD planning documents
- Source: Generated by planning system
- Committed: Yes (planning artifacts)

---

*Structure analysis: 2026-01-13*
*Update when directory structure changes*
