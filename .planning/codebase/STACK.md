# Technology Stack

**Analysis Date:** 2026-01-15

## Languages

**Primary:**
- Java 17 - All OSMB automation scripts (`TidalsGemCutter/build.gradle`, `TidalsSecondaryCollector/build.gradle`)
- TypeScript 5 - Dashboard application (`script-dashboard/package.json`)

**Secondary:**
- JavaScript - Build scripts, config files (`script-dashboard/next.config.ts`)
- Gradle DSL - Build configuration (`settings.gradle`, `utilities/build.gradle`)

## Runtime

**Environment:**
- Java 17 (enforced via `sourceCompatibility = JavaVersion.VERSION_17`)
- Node.js - Dashboard runtime via Next.js 16.1.1
- OSMB Client - Game automation runtime (closed-source)

**Package Managers:**
- Gradle - Java build system (`settings.gradle`, per-script `build.gradle`)
- npm - Node.js packages (`script-dashboard/package-lock.json`)

## Frameworks

**Core:**
- OSMB API - Game automation framework (`API/API.jar`, 9.1MB closed-source)
- Next.js 16.1.1 - Dashboard web framework (`script-dashboard/package.json`)
- React 19.2.3 - UI library (`script-dashboard/package.json`)

**Testing:**
- Not detected - Manual integration testing via OSMB client

**Build/Dev:**
- Gradle 8.x - Java compilation and JAR packaging
- TypeScript 5 - Dashboard type checking (`script-dashboard/tsconfig.json`)
- Tailwind CSS 4 - Dashboard styling (`script-dashboard/package.json`)

## Key Dependencies

**Critical (Java):**
- OSMB API.jar - Game interaction, visual detection, pathfinding (`API/API.jar`)
- TidalsUtilities.jar - Shared retry, banking, dialogue utilities (`utilities/jar/TidalsUtilities.jar`)

**Critical (Dashboard):**
- Prisma 6.19.1 - Database ORM (`script-dashboard/package.json`)
- Recharts 3.6.0 - Stats visualization (`script-dashboard/package.json`)
- date-fns 4.1.0 - Date formatting (`script-dashboard/package.json`)

**Infrastructure:**
- SQLite - Dashboard database (`script-dashboard/prisma/schema.prisma`)
- JavaFX - Script configuration UI (bundled with OSMB API)

## Configuration

**Environment:**
- Dashboard: `.env` files with `DATABASE_URL`, `STATS_API_KEY` (`script-dashboard/.env.example`)
- Scripts: `obf/Secrets.java` (gitignored) for API credentials

**Build:**
- `settings.gradle` - Dynamic Gradle module discovery
- `tsconfig.json` - TypeScript compiler options (`script-dashboard/tsconfig.json`)
- `next.config.ts` - Next.js build config, security headers, remote image patterns

## Platform Requirements

**Development:**
- macOS/Linux/Windows with Java 17
- Node.js for dashboard development
- OSMB client for script testing

**Production:**
- Scripts: JAR files loaded by OSMB client
- Dashboard: Vercel or self-hosted Next.js

---

*Stack analysis: 2026-01-15*
*Update after major dependency changes*
