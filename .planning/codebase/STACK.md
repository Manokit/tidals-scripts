# Technology Stack

**Analysis Date:** 2026-01-14

## Languages

**Primary:**
- Java 17 - All OSMB script application code (`TidalsGemCutter/build.gradle`, `utilities/build.gradle`)
- TypeScript 5.x - Dashboard application code (`script-dashboard/package.json`)

**Secondary:**
- JavaScript - Dashboard build scripts, config files
- Gradle DSL - Build configuration for Java scripts

## Runtime

**Environment:**
- Java 17 (class version 61.0) - Required for OSMB API compatibility
- Node.js 20.x - Dashboard runtime (`script-dashboard/Dockerfile` uses node:20-alpine)

**Package Manager:**
- npm 10.x - Dashboard dependencies (`script-dashboard/package-lock.json`)
- Gradle - Java script builds (multi-project setup via `settings.gradle`)

## Frameworks

**Core:**
- OSMB API - Game automation framework (`API/API.jar` - compileOnly dependency)
- Next.js 16.1.1 - Full-stack React framework with App Router (`script-dashboard/package.json`)
- React 19.2.3 - UI component library (`script-dashboard/package.json`)

**Testing:**
- No formal testing frameworks detected in either project
- Manual/integration testing through OSMB client execution

**Build/Dev:**
- Gradle - Java build system (auto-discovers modules via `settings.gradle`)
- TypeScript 5.x - Type checking for dashboard (`script-dashboard/tsconfig.json`)
- PostCSS 4 - CSS transformation (`script-dashboard/postcss.config.mjs`)

## Key Dependencies

**Critical (Dashboard):**
- Prisma 6.19.1 - ORM for SQLite database access (`@prisma/client`)
- Recharts 3.6.0 - React charting for stats visualization
- date-fns 4.1.0 - Date utility library
- Tailwind CSS 4 - Utility-first CSS framework

**Critical (Java Scripts):**
- OSMB API (`API/API.jar`) - Core game automation (WidgetManager, ObjectManager, SceneManager)
- TidalsUtilities.jar - Shared utilities (RetryUtils, BankingUtils, DialogueUtils, TabUtils)

**Infrastructure:**
- SQLite - Lightweight database for stats storage
- Docker - Containerization for dashboard deployment

## Configuration

**Environment:**
- `DATABASE_URL` - SQLite database path (`file:./dev.db` or `file:/app/data/prod.db`)
- `STATS_API_KEY` - API key for script authentication
- `obf/Secrets.java` - Java scripts credentials (gitignored)

**Build:**
- `script-dashboard/next.config.ts` - Next.js with security headers, remote image patterns
- `script-dashboard/tsconfig.json` - TypeScript ES2017 target, bundler module resolution
- `prisma/schema.prisma` - Database schema (SQLite provider)
- `script-dashboard/eslint.config.mjs` - ESLint extending next/core-web-vitals

## Platform Requirements

**Development:**
- macOS/Linux/Windows (any platform with Java 17 and Node.js 20)
- OSMB client for script testing
- No external database required (SQLite embedded)

**Production:**
- Docker 20.10+ - Multi-stage build for dashboard
- Docker Compose 3.8 - Orchestration with volume persistence
- OSMB client - Script execution environment
- Java 17 runtime for scripts

---

*Stack analysis: 2026-01-14*
*Update after major dependency changes*
