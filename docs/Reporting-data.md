# Stats Reporting

All Tidal scripts report anonymous usage statistics to the dashboard at `scripts.tidale.us`. This helps track script performance and usage patterns.

## API Endpoint

**URL**: `https://scripts.tidale.us/api/stats`
**Method**: `POST`
**Content-Type**: `application/json`
**Authentication**: `X-Stats-Key` header with API key

## Rate Limiting

- **30 requests per minute** per IP address
- Exceeding this returns `429 Too Many Requests`
- With 10-minute reporting intervals, you won't hit this limit

## Request Validation

| Constraint | Limit |
|------------|-------|
| Script name length | 100 characters max |
| Session ID length | 100 characters max |
| Request body size | 10 KB max |
| Numeric values | 0 to Number.MAX_SAFE_INTEGER |

## HTTP Response Codes

| Code | Meaning |
|------|---------|
| `200` | Success |
| `400` | Invalid JSON or missing required fields |
| `401` | Invalid or missing API key |
| `413` | Request body too large |
| `429` | Rate limited |
| `500` | Server error |

## Required Fields

| Field | Type | Description |
|-------|------|-------------|
| `script` | string | Script identifier (e.g., "GemCutter", "SecondaryCollector") |
| `session` | string | UUID generated once per script session |
| `gp` | int | GP earned this interval (incremental, not cumulative) |
| `xp` | int | XP gained this interval (incremental, not cumulative) |
| `runtime` | long | Runtime in seconds this interval (incremental) |

## CRITICAL: Incremental Reporting

**The API aggregates values by ADDING them together.** You must send INCREMENTAL values (the change since last report), NOT cumulative totals.

### Wrong - Sending cumulative totals:
```
Report 1: xp=100  (total so far)
Report 2: xp=200  (total so far)
Report 3: xp=300  (total so far)
Dashboard shows: 600 XP (100+200+300) - INCORRECT!
```

### Correct - Sending incremental values:
```
Report 1: xp=100  (gained since start)
Report 2: xp=100  (gained since report 1)
Report 3: xp=100  (gained since report 2)
Dashboard shows: 300 XP - CORRECT!
```

## Script-Specific Metadata Fields

Any additional fields beyond the required ones are stored as metadata and aggregated automatically. Numeric metadata values are summed across reports.

### TidalsSecondaryCollector
| Field | Type | Description |
|-------|------|-------------|
| `bloomCasts` | int | Bloom spell casts this interval |
| `itemsBanked` | int | Items banked this interval |
| `bankTrips` | int | Bank trips this interval |

### TidalsCannonballThiever
| Field | Type | Description |
|-------|------|-------------|
| `cannonballsStolen` | int | Cannonballs stolen this interval |
| `oresStolen` | int | Ores stolen this interval |

### TidalsGemCutter
| Field | Type | Description |
|-------|------|-------------|
| `gemsCut` | int | Gems cut this interval |

### TidalsGoldSuperheater
| Field | Type | Description |
|-------|------|-------------|
| `barsCreated` | int | Gold bars created this interval |
| `magicXp` | int | Magic XP gained this interval |
| `smithingXp` | int | Smithing XP gained this interval |

### TidalsChompyHunter
| Field | Type | Description |
|-------|------|-------------|
| `chompyKills` | int | Chompy birds killed this interval |

## Complete Implementation Example

### 1. Create Secrets.java

Create `src/main/java/obf/Secrets.java`:

```java
package obf;

// stats API credentials for dashboard reporting
public class Secrets {
    public static final String STATS_URL = "https://scripts.tidale.us/api/stats";
    public static final String STATS_API = "your-api-key-here";
}
```

**Important**: The `obf` package is gitignored. Never commit API keys.

### 2. Add Required Imports

```java
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
```

### 3. Add Static Fields

```java
// script identification
private static final String SCRIPT_NAME = "YourScriptName";  // no "Tidals" prefix
private static final String SESSION_ID = UUID.randomUUID().toString();

// stats timing
private static long lastStatsSent = 0;
private static final long STATS_INTERVAL_MS = 600_000L;  // 10 minutes

// track last sent values for incremental reporting
private static int lastSentXp = 0;
private static int lastSentGp = 0;
private static int lastSentCustomStat = 0;  // your script-specific stat
private static long lastSentRuntime = 0;

// current cumulative values (updated during script execution)
public static int totalXpGained = 0;
public static int totalGpEarned = 0;
public static int totalCustomStat = 0;
public static long startTime = System.currentTimeMillis();
```

### 4. Add Stats Reporting in poll()

```java
@Override
public int poll() {
    long nowMs = System.currentTimeMillis();
    if (nowMs - lastStatsSent >= STATS_INTERVAL_MS) {
        long elapsed = nowMs - startTime;

        // calculate INCREMENTS since last send
        int xpIncrement = totalXpGained - lastSentXp;
        int gpIncrement = totalGpEarned - lastSentGp;
        int customStatIncrement = totalCustomStat - lastSentCustomStat;
        long runtimeIncrement = (elapsed / 1000) - lastSentRuntime;

        sendStats(gpIncrement, xpIncrement, customStatIncrement, runtimeIncrement);

        // update last sent values AFTER sending
        lastSentXp = totalXpGained;
        lastSentGp = totalGpEarned;
        lastSentCustomStat = totalCustomStat;
        lastSentRuntime = elapsed / 1000;
        lastStatsSent = nowMs;
    }

    // ... rest of poll logic
    return 0;
}
```

### 5. Implement sendStats Method

```java
private void sendStats(int gpIncrement, int xpIncrement, int customStatIncrement, long runtimeSecs) {
    try {
        // check if stats reporting is configured
        if (obf.Secrets.STATS_URL == null || obf.Secrets.STATS_URL.isEmpty()) {
            return;
        }

        // skip if nothing to report
        if (gpIncrement == 0 && xpIncrement == 0 && customStatIncrement == 0 && runtimeSecs == 0) {
            return;
        }

        // build JSON payload with incremental values
        String json = String.format(
                "{\"script\":\"%s\",\"session\":\"%s\",\"gp\":%d,\"xp\":%d,\"runtime\":%d,\"customStat\":%d}",
                SCRIPT_NAME,
                SESSION_ID,
                gpIncrement,
                xpIncrement,
                runtimeSecs,
                customStatIncrement
        );

        URL url = new URL(obf.Secrets.STATS_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-Stats-Key", obf.Secrets.STATS_API);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code == 200) {
            log("STATS", "Stats reported: xp=" + xpIncrement + ", gp=" + gpIncrement +
                ", customStat=" + customStatIncrement + ", runtime=" + runtimeSecs + "s");
        } else {
            log("STATS", "Failed to report stats, HTTP " + code);
        }
    } catch (Exception e) {
        log("STATS", "Error sending stats: " + e.getClass().getSimpleName());
    }
}
```

## Example JSON Payloads

### Basic (GP/XP script)
```json
{
  "script": "MyScript",
  "session": "550e8400-e29b-41d4-a716-446655440000",
  "gp": 15000,
  "xp": 2500,
  "runtime": 600
}
```

### With custom metadata (GemCutter)
```json
{
  "script": "GemCutter",
  "session": "550e8400-e29b-41d4-a716-446655440000",
  "gp": 0,
  "xp": 4200,
  "runtime": 600,
  "gemsCut": 156
}
```

### Multiple custom fields (GoldSuperheater)
```json
{
  "script": "Superheater",
  "session": "550e8400-e29b-41d4-a716-446655440000",
  "gp": 0,
  "xp": 8500,
  "runtime": 600,
  "barsCreated": 250,
  "magicXp": 5100,
  "smithingXp": 3400
}
```

## Local Development

For local testing, use your machine's network IP (not localhost - OSMB runs in a sandbox):

```java
// for testing only
public static final String STATS_URL = "http://192.168.x.x:3000/api/stats";
```

Run the dashboard locally:
```bash
cd script-dashboard
npm run dev -- -H 0.0.0.0
```

## Testing Interval

For faster testing, reduce the interval to 10 seconds:
```java
private static final long STATS_INTERVAL_MS = 10_000L;  // 10 seconds for testing
```

Remember to change it back to `600_000L` (10 minutes) before release.

## Error Handling

Stats failures are silently logged to avoid interrupting script execution. The incremental reporting model means missed reports don't cause data loss - the next successful report will include all stats gained since the last successful send.
