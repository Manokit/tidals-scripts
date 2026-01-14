# External Integrations

**Analysis Date:** 2026-01-14

## APIs & External Services

**Stats API (Internal):**
- Purpose: Scripts report game statistics to dashboard
- Endpoint: `POST /api/stats` (`script-dashboard/src/app/api/stats/route.ts`)
- Authentication: `X-Stats-Key` header with API key
- Rate Limiting: 30 requests per minute per IP (in-memory)
- Payload: JSON with `script`, `session`, `gp`, `xp`, `runtime`, and script-specific metadata

**OSMB API (Game Automation):**
- Purpose: Color-based bot for OSRS game automation
- SDK: `API/API.jar` (provided dependency)
- Core Managers:
  - `WidgetManager` - UI: Bank, Inventory, Dialogue, Tabs, Minimap
  - `ObjectManager` - RSObjects (trees, rocks, banks)
  - `SceneManager` - NPCs, ground items, tiles
  - `Walker` - Pathfinding and navigation
  - `PixelAnalyzer` - Color/pixel detection
  - `OCR` - Text recognition

**Discord Webhook (Optional):**
- Purpose: Script completion notifications
- Integration: `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java`
- Method: Multipart form-data with embedded canvas screenshot
- Rate limit handling: 429 response retry logic
- Configurable interval: 5-60 minutes

## Data Storage

**Databases:**
- SQLite - Primary data store for dashboard (`script-dashboard/prisma/schema.prisma`)
  - Connection: `DATABASE_URL` environment variable
  - Client: Prisma ORM 6.19.1
  - Migrations: `prisma/migrations/`
  - Tables:
    - `ScriptSession` - Session tracking with unique `sessionId`
    - `ScriptStat` - Individual stats entries (gp, xp, runtime, metadata)
    - `AggregatedStats` - Daily aggregated stats by script

**File Storage:**
- None - No external file storage service
- Local file system for script JARs and build artifacts

**Caching:**
- None - No Redis or external cache
- In-memory rate limiting map (single-instance only)

## Authentication & Identity

**Auth Provider:**
- API key authentication only (`X-Stats-Key` header)
- Timing-safe constant-time comparison (`script-dashboard/src/app/api/stats/route.ts:56-67`)
- No user authentication system

**OAuth Integrations:**
- None

## Monitoring & Observability

**Error Tracking:**
- None - No Sentry, Datadog, or similar
- Console logging only

**Analytics:**
- None - No Mixpanel, GA, or similar
- Internal stats dashboard provides script usage metrics

**Logs:**
- Console stdout/stderr
- OSMB client logs for script execution
- `script.log(Class, "message")` pattern in Java scripts

## CI/CD & Deployment

**Hosting:**
- Docker container - Dashboard deployment
- Local - Script JAR files deployed to OSMB client

**CI Pipeline:**
- None detected - No GitHub Actions, Jenkins, etc.
- Manual builds: `osmb build [ScriptName]`

## Environment Configuration

**Development:**
- Required env vars: `DATABASE_URL`, `STATS_API_KEY`
- Secrets location: `.env` (gitignored), `obf/Secrets.java` (gitignored)
- Mock services: None - uses real SQLite database

**Production:**
- Docker Compose with volume persistence (`docker-compose.yml`)
- Environment variables passed via Docker

## Webhooks & Callbacks

**Incoming:**
- `POST /api/stats` - Stats submission from scripts
  - Validation: API key, string sanitization, numeric bounds
  - Data: Incremental stats (not cumulative)

**Outgoing:**
- Discord webhook (optional from Java scripts)
  - Trigger: Script completion or interval
  - Payload: Stats summary with screenshot embed

## External Image Resources

**Old School RuneScape Wiki:**
- Configuration: `script-dashboard/next.config.ts`
- Source: `https://oldschool.runescape.wiki/images/**`
- Use: Item sprites and game assets in dashboard UI

---

*Integration audit: 2026-01-14*
*Update when adding/removing external services*
