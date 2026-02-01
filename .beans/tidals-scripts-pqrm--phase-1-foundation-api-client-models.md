---
# tidals-scripts-pqrm
title: 'Phase 1: Foundation - API Client & Models'
status: completed
type: feature
priority: normal
created_at: 2026-01-31T02:24:49Z
updated_at: 2026-01-31T02:34:31Z
parent: tidals-scripts-6gj8
blocking:
    - tidals-scripts-inzv
    - tidals-scripts-dyni
    - tidals-scripts-9eyc
---

Build the foundation layer replacing POC's regex parsing with proper structured code.

## Checklist

- [x] Create `PathStatus` enum (SUCCESS, BLOCKED, NO_WEB_PATH, RATE_LIMIT, ERROR)
- [x] Create `PathResult` model (status, path as List<WorldPosition>, cost)
- [x] Create `PlayerDetails` model / `PlayerDetailsBuilder` — extracts skills, equipment, inventory from script
- [x] Create `DaxApiClient` — HTTP POST to api.dax.cloud, JSON parsing (use Java stdlib or minimal parsing), rate limit handling
- [x] Add path caching (`PathCache`) — keyed by start→end, TTL-based expiry
- [x] Migrate from POC regex parsing to structured response parsing
- [x] Verify API connectivity with existing POC test script

## Notes
- NO external JSON dependencies — use Java stdlib (or lightweight manual parsing)
- Free tier creds: key=sub_DPjXXzL5DeSiPf, secret=PUBLIC-KEY
- Endpoints: /walker/generatePaths, /walker/generateBankPaths
- OSMB has no varbits — send empty arrays for settings/varbits
- SkillType.RANGED is actually SkillType.RANGE in OSMB API