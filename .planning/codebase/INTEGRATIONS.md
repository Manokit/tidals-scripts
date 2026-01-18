# External Integrations

**Analysis Date:** 2026-01-16

## APIs & External Services

**Stats Dashboard:**
- Service: Custom stats dashboard at `scripts.tidale.us`
- Purpose: Aggregate usage statistics across all script sessions
- SDK/Client: Native `java.net.HttpURLConnection`
- Auth: API key via `X-Stats-Key` header
- Env vars: `obf.Secrets.STATS_URL`, `obf.Secrets.STATS_API`

**Discord Webhooks:**
- Service: Discord webhook API
- Purpose: Send progress reports with screenshots to Discord channels
- SDK/Client: Native `java.net.HttpURLConnection` with multipart/form-data
- Auth: Webhook URL (user-provided via ScriptUI)
- Used in: `TidalsGemCutter` (webhook tab in ScriptUI)

**OSMB Platform:**
- Service: OSMB color bot client (closed-source)
- Purpose: Game interaction, visual detection, walking, input simulation
- SDK/Client: `API/API.jar` (~9.5MB)
- Auth: None (runs within OSMB client)

## Data Storage

**Databases:**
- None - Scripts are stateless beyond session

**File Storage:**
- Local JAR resources for logos (`src/main/resources/logo.png`)
- Build output JARs (`<script>/jar/<ScriptName>.jar`)

**Caching:**
- None - All state held in memory during script execution

**User Preferences:**
- `java.util.prefs.Preferences` - Persists UI settings between sessions
- Location: System-dependent (macOS: `~/Library/Preferences/`)
- Keys: `tgemcutter_selected_gem`, `tgemcutter_webhook_url`, etc.

## Authentication & Identity

**Auth Provider:**
- Custom API key authentication for stats dashboard
- Implementation: Static key in `obf/Secrets.java` (gitignored)

**Session Management:**
- `UUID.randomUUID().toString()` generates unique session ID per script start
- Session ID sent with every stats report for aggregation

## Monitoring & Observability

**Error Tracking:**
- None - Errors logged to OSMB client console only

**Logs:**
- `script.log(tag, message)` - OSMB built-in logging
- Format: `[TAG] message`
- No external log aggregation

**Metrics:**
- Stats sent to dashboard every 10 minutes (600,000ms)
- Incremental reporting (deltas, not cumulative)

## CI/CD & Deployment

**Hosting:**
- Scripts run locally within OSMB client
- Stats dashboard hosted at `scripts.tidale.us`

**CI Pipeline:**
- None - Manual build via `osmb build` command

**Deployment:**
- Build JAR, copy to OSMB scripts directory
- No automated deployment pipeline

## Environment Configuration

**Required env vars:**
- None (secrets in gitignored Java file, not env vars)

**Secrets location:**
- `src/main/java/obf/Secrets.java` (gitignored)
- Contains: `STATS_URL`, `STATS_API`

**Example Secrets.java:**
```java
package obf;

public class Secrets {
    public static final String STATS_URL = "https://scripts.tidale.us/api/stats";
    public static final String STATS_API = "your-api-key-here";
}
```

## Stats API Integration

**Endpoint:** `POST https://scripts.tidale.us/api/stats`

**Headers:**
- `Content-Type: application/json`
- `X-Stats-Key: <api-key>`

**Required Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `script` | string | Script identifier (e.g., "GemCutter") |
| `session` | string | UUID per script session |
| `gp` | int | GP earned (incremental) |
| `xp` | int | XP gained (incremental) |
| `runtime` | long | Runtime in seconds (incremental) |

**Script-Specific Fields:**
| Script | Fields |
|--------|--------|
| TidalsGemCutter | `gemsCut` |
| TidalsCannonballThiever | `cannonballsStolen`, `oresStolen` |
| TidalsSecondaryCollector | `bloomCasts`, `itemsBanked`, `bankTrips` |
| TidalsGoldSuperheater | `barsCreated`, `magicXp`, `smithingXp` |

**Rate Limits:**
- 30 requests/minute per IP
- Scripts report every 10 minutes (well under limit)

**Response Codes:**
| Code | Meaning |
|------|---------|
| 200 | Success |
| 400 | Invalid JSON |
| 401 | Invalid API key |
| 429 | Rate limited |

## Discord Webhook Integration

**Format:** Multipart form-data with embedded JSON

**Payload Structure:**
```json
{
  "embeds": [{
    "title": "Script run summary - <username>",
    "color": 5189303,
    "author": {
      "name": "Tidal's <scriptName>",
      "icon_url": "<author-icon-url>"
    },
    "description": "Progress report after running for **<runtime>**",
    "image": { "url": "attachment://canvas.png" },
    "footer": { "text": "Next update at: <time>" }
  }]
}
```

**Image Attachment:**
- PNG screenshot of paint overlay
- Captured via `Canvas.toImageCopy()`
- Sent as `file` part in multipart request

**Rate Limit Handling:**
- Checks `Retry-After` header on 429
- Backs off and retries after specified duration

## Webhooks & Callbacks

**Incoming:**
- None - Scripts are clients, not servers

**Outgoing:**
- Stats reports to dashboard (every 10 minutes)
- Discord webhooks (user-configurable interval, 1-60 minutes)

## Integration Pattern (Stats Reporting)

```java
// in main script class
private static final String SESSION_ID = UUID.randomUUID().toString();
private static long lastStatsSent = 0;
private static final long STATS_INTERVAL_MS = 600_000L;

// track last sent for incremental reporting
private static int lastSentXp = 0;

@Override
public int poll() {
    long nowMs = System.currentTimeMillis();
    if (nowMs - lastStatsSent >= STATS_INTERVAL_MS) {
        int xpIncrement = totalXp - lastSentXp;
        sendStats(xpIncrement, ...);
        lastSentXp = totalXp;
        lastStatsSent = nowMs;
    }
    // ... rest of poll
}

private void sendStats(int xpIncrement, ...) {
    if (obf.Secrets.STATS_URL == null) return;

    String json = String.format(
        "{\"script\":\"%s\",\"session\":\"%s\",\"xp\":%d,...}",
        SCRIPT_NAME, SESSION_ID, xpIncrement);

    HttpURLConnection conn = (HttpURLConnection) new URL(obf.Secrets.STATS_URL).openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("X-Stats-Key", obf.Secrets.STATS_API);
    // ... send request
}
```

---

*Integration audit: 2026-01-16*
