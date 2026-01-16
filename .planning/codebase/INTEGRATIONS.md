# External Integrations

**Analysis Date:** 2026-01-15

## APIs & External Services

**Stats Dashboard API (Internal):**
- Script → Dashboard stats reporting
  - Endpoint: Configured in `obf/Secrets.STATS_URL`
  - Method: HTTP POST with JSON body
  - Auth: `X-Stats-Key` header with API key
  - Interval: 10 minutes (`STATS_INTERVAL_MS = 600_000L`)
  - Implementation: `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (lines 678-721)

**Discord Webhook (Optional):**
- Script → Discord screenshot notifications
  - Endpoint: User-configured webhook URL
  - Method: Multipart form-data POST with PNG screenshot
  - Rate limiting: Respects Discord `Retry-After` header
  - Implementation: `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java` (lines 590-655)

**OSRS Wiki Images:**
- Dashboard → Image assets
  - Endpoint: `https://oldschool.runescape.wiki/images/**`
  - Method: GET (via Next.js Image optimization)
  - Config: `script-dashboard/next.config.ts` (remotePatterns)

## Data Storage

**Databases:**
- SQLite - Dashboard data storage
  - Connection: `DATABASE_URL` env var (`file:./dev.db` or `file:/app/data/prod.db`)
  - Client: Prisma ORM 6.19.1
  - Schema: `script-dashboard/prisma/schema.prisma`
  - Migrations: `script-dashboard/prisma/migrations/`

**File Storage:**
- Not used - All data in SQLite

**Caching:**
- Not detected - Direct database queries

## Authentication & Identity

**Auth Provider:**
- API Key authentication (scripts → dashboard)
  - Implementation: Timing-safe comparison
  - Location: `script-dashboard/src/app/api/stats/route.ts`
  - Header: `X-Stats-Key`

**OAuth Integrations:**
- None

## Monitoring & Observability

**Error Tracking:**
- Not detected - Console logging only

**Analytics:**
- Script stats stored in SQLite
- Aggregated daily via `AggregatedStats` table

**Logs:**
- Scripts: `script.log()` to OSMB client console
- Dashboard: Next.js server logs (stdout)

## CI/CD & Deployment

**Hosting:**
- Scripts: Built locally, loaded by OSMB client
- Dashboard: Vercel or self-hosted

**CI Pipeline:**
- Not detected - Manual builds

## Environment Configuration

**Development:**
- Required env vars (Dashboard):
  - `DATABASE_URL` - SQLite path
  - `STATS_API_KEY` - API authentication
- Secrets location: `script-dashboard/.env.local` (gitignored)
- Template: `script-dashboard/.env.example`

**Scripts:**
- Required secrets:
  - `STATS_URL` - Dashboard API endpoint
  - `STATS_API` - API key
- Location: `<Script>/src/main/java/obf/Secrets.java` (gitignored)

**Production:**
- Dashboard: Vercel environment variables
- Scripts: Same Secrets.java with production values

## Webhooks & Callbacks

**Incoming (Dashboard):**
- POST `/api/stats` - Stats ingestion from scripts
  - Verification: API key header check (timing-safe)
  - Events: Incremental stats (XP, GP, runtime, items)
  - Implementation: `script-dashboard/src/app/api/stats/route.ts`

**Outgoing (Scripts):**
- Discord webhook (optional)
  - Trigger: Configurable interval or script events
  - Payload: PNG screenshot + stats text
  - Implementation: `TidalsGemCutter/src/main/java/main/TidalsGemCutter.java`

## Data Schema (Prisma)

**ScriptSession:**
- Tracks individual script execution sessions
- Fields: script name, session ID, created timestamp

**ScriptStat:**
- Incremental stats per session
- Fields: XP, GP, runtime, metadata (JSON), createdAt

**AggregatedStats:**
- Daily rollup of script activity
- Fields: date, script, total XP/GP/runtime, session count

## Stats Payload Format

**Script → Dashboard:**
```json
{
  "script": "TidalsGemCutter",
  "sessionId": "uuid-here",
  "xp": 12500,       // incremental since last report
  "gp": 0,           // incremental
  "runtime": 600,    // seconds since last report
  "metadata": {      // script-specific
    "gemsCut": 150,
    "boltTipsMade": 0
  }
}
```

**Security Features:**
- Rate limiting: 30 req/min per IP
- Input validation: Max lengths, null byte removal
- Numeric bounds: Clamped to safe ranges
- Request size: 10KB limit

---

*Integration audit: 2026-01-15*
*Update when adding/removing external services*
