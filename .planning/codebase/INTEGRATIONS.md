# External Integrations

**Analysis Date:** 2026-01-13

## APIs & External Services

**Stats Reporting API (Internal):**
- Purpose: Scripts report XP/GP/runtime to dashboard
- Endpoint: `POST /api/stats` (`script-dashboard/src/app/api/stats/route.ts`)
- Auth: `X-Stats-Key` header with timing-safe comparison
- Rate Limit: 30 requests/minute per IP
- Payload:
  ```json
  {
    "script": "TidalsGemCutter",
    "session": "<UUID>",
    "gp": 0,
    "xp": 50000,
    "runtime": 600,
    "gemsCut": 150
  }
  ```

**Discord Webhooks (Optional):**
- Purpose: Send progress screenshots to Discord
- Configuration: User-provided webhook URL via ScriptUI
- Implementation: `TidalsGemCutter/src/main/java/main/ScriptUI.java`, `TidalsGoldSuperheater/src/main/java/main/ScriptUI.java`
- Features: Configurable interval (1-60 minutes), optional username inclusion
- Uses `java.net.HttpURLConnection` for POST requests

**OSMB API (External):**
- Purpose: Core color bot framework
- SDK: `API/API.jar` (compileOnly dependency)
- Provides: Script base class, widget managers, pixel analysis, OCR

## Data Storage

**Databases:**
- SQLite - Primary data store for dashboard
- Connection: `DATABASE_URL` environment variable
- Client: Prisma ORM v6.19 (`script-dashboard/src/lib/db.ts`)
- Migrations: `prisma/migrations/` (Prisma migrate)
- Schema: `script-dashboard/prisma/schema.prisma`

**Models:**
- `ScriptSession` - Unique session tracking (sessionId, script, timestamps)
- `ScriptStat` - Individual stat reports (xp, gp, runtime, metadata)
- `AggregatedStats` - Daily rollups per script (totals, session counts)

**File Storage:**
- None - No file upload functionality

**Caching:**
- None - Direct database queries

## Authentication & Identity

**Auth Provider:**
- API key authentication (not user auth)
- Key stored in `STATS_API_KEY` env var (dashboard)
- Key stored in `obf/Secrets.java` (scripts)

**Implementation:**
- Timing-safe string comparison to prevent timing attacks
- Single API key shared across all scripts
- No user sessions or tokens

## Monitoring & Observability

**Error Tracking:**
- None - Console logging only

**Analytics:**
- Built-in: Stats dashboard tracks XP, GP, runtime, sessions
- No external analytics service

**Logs:**
- Scripts: OSMB client console (`script.log()`)
- Dashboard: Node.js console (stdout/stderr)

## CI/CD & Deployment

**Hosting (Dashboard):**
- Docker-ready: `script-dashboard/Dockerfile`
- Can deploy to Vercel (Next.js native)
- Environment vars configured in host platform

**CI Pipeline:**
- None configured
- No GitHub Actions workflows

**Build:**
- Dashboard: `npm run build` (Next.js)
- Scripts: `osmb build <name>` or Gradle

## Environment Configuration

**Development:**
- Dashboard: `.env` with `DATABASE_URL=file:./dev.db`
- Scripts: `obf/Secrets.java` with local/test API endpoint
- Template: `.env.example` provided

**Production:**
- Dashboard: Environment variables via hosting platform
- Database: `file:/app/data/prod.db` (Docker volume)
- Scripts: Compiled with production API URL in Secrets.java

**Required Variables (Dashboard):**
- `DATABASE_URL` - SQLite file path
- `STATS_API_KEY` - API authentication key

**Required (Scripts):**
- `obf/Secrets.java` with `STATS_URL` and `STATS_API` constants

## Webhooks & Callbacks

**Incoming:**
- `POST /api/stats` - Receives stats from scripts
  - Validation: Script name, session ID, numeric bounds
  - Authentication: X-Stats-Key header
  - Rate limiting: 30/min per IP

**Outgoing:**
- Discord webhooks (optional, user-configured)
  - Triggers: Configured interval (1-60 minutes)
  - Payload: JSON with script name, stats, optional screenshot
  - No retry logic implemented

## External References

**Asset URLs:**
- OSRS Wiki images: `https://oldschool.runescape.wiki/images/*`
- Used in dashboard for item icons

**Stats Reporting URL:**
- Configured per-script in `obf/Secrets.java`
- Example: `https://scripts.tidale.us/api/stats`

---

*Integration audit: 2026-01-13*
*Update when adding/removing external services*
