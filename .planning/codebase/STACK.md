# Technology Stack

**Analysis Date:** 2026-01-13

## Languages

**Primary:**
- Java 17 - OSMB automation scripts (`TidalsGemCutter/build.gradle`, `utilities/build.gradle`)
- TypeScript 5 - Dashboard application (`script-dashboard/tsconfig.json`)

**Secondary:**
- JavaScript - Build scripts, config files
- Groovy - Gradle build configuration (`settings.gradle`, `*/build.gradle`)

## Runtime

**Environment:**
- Java 17 (class version 61.0) - Required by OSMB API
- Node.js 16+ - Inferred from Next.js 16 requirements

**Package Manager:**
- Gradle 7+ - Java dependency management via wrapper (`gradlew`)
- npm - Node.js package management (`script-dashboard/package.json`)
- No lockfiles committed (npm uses package.json semver, Gradle uses resolution)

## Frameworks

**Core:**
- OSMB API - Color bot framework for automation scripts (`API/API.jar`)
- Next.js 16.1.1 - React web framework with App Router (`script-dashboard/package.json`)
- React 19.2.3 - UI component library

**Testing:**
- None - No formal test framework configured in either project

**Build/Dev:**
- Gradle - Java compilation and JAR packaging
- TypeScript 5 - Static type checking
- ESLint 9 - Linting for dashboard (`script-dashboard/eslint.config.mjs`)
- Tailwind CSS 4 - Utility-first styling (`@tailwindcss/postcss`)
- PostCSS - CSS processing

## Key Dependencies

**Critical (Dashboard):**
- `@prisma/client` ^6.19.1 - ORM for SQLite database access (`script-dashboard/src/lib/db.ts`)
- `prisma` ^6.19.1 - Database migrations and schema management
- `recharts` ^3.6.0 - Data visualization charts (`script-dashboard/src/components/StatsChart.tsx`)
- `date-fns` ^4.1.0 - Date formatting utilities

**Critical (Scripts):**
- OSMB API.jar - Core color bot framework (`API/API.jar`)
- TidalsUtilities.jar - Shared retry, banking, tab, dialogue utilities (`utilities/jar/`)

**Infrastructure:**
- SQLite - File-based database via Prisma (`prisma/schema.prisma`)
- JavaFX - UI configuration dialogs (`*/main/ScriptUI.java`)

## Configuration

**Environment (Dashboard):**
- `.env` files with DATABASE_URL and STATS_API_KEY (`script-dashboard/.env.example`)
- Required: `DATABASE_URL` (SQLite path), `STATS_API_KEY` (authentication)

**Environment (Scripts):**
- `obf/Secrets.java` - Hardcoded API URL and key (gitignored)
- Java Preferences API - User configuration storage for webhook URLs

**Build:**
- `settings.gradle` - Root project with auto-discovery of subdirectories
- `build.gradle` per script - Individual JAR configuration
- `tsconfig.json` - TypeScript strict mode, path aliases (`@/*` → `src/*`)
- `next.config.ts` - Next.js security headers

## Platform Requirements

**Development:**
- macOS/Linux/Windows with Java 17 JDK
- Node.js 16+ for dashboard
- OSMB client for script testing

**Production:**
- Scripts: Compiled JAR files run in OSMB client
- Dashboard: Vercel-compatible (Docker available via `Dockerfile`)
- Database: SQLite file storage

---

*Stack analysis: 2026-01-13*
*Update after major dependency changes*
