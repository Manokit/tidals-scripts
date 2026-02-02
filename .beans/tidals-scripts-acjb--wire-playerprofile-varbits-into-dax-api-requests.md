---
# tidals-scripts-acjb
title: Wire PlayerProfile varbits into Dax API requests
status: in-progress
type: feature
priority: normal
created_at: 2026-02-02T07:44:23Z
updated_at: 2026-02-02T07:54:45Z
---

## Problem

TidalsWalker sends empty varbit/setting arrays to the Dax API, causing NO_WEB_PATH for Varlamore and other quest-gated areas. TribotRS sends 50+ varbits read from game memory.

## Solution

Use PlayerProfile (WikiSync-backed) to map quest/diary completion → varbit values, then send them with Dax API requests.

## Checklist

- [x] Add varbit/setting setters to PlayerDetails.java
- [x] Create DaxVarbitMapper utility that maps PlayerProfile → varbit list
- [x] Update PlayerDetailsBuilder to accept optional PlayerProfile
- [x] Update TidalsWalker to accept and pass through PlayerProfile
- [x] Build utilities jar and verify compilation
- [x] Wire PlayerProfile into TidalsWalkerTest onStart()
- [x] Delete old TidalsDaxWalkerPOC script
- [ ] Live test: fairy ring route from Varlamore

## Files Changed

- `utilities/.../webwalker/models/PlayerDetails.java` — added varbit()/setting() setters
- `utilities/.../webwalker/api/DaxVarbitMapper.java` — NEW: maps 25+ quest/diary completions → Dax varbits
- `utilities/.../webwalker/api/PlayerDetailsBuilder.java` — new build(script, profile) overload
- `utilities/.../webwalker/TidalsWalker.java` — added setProfile(PlayerProfile)
- `TidalsWalkerTest/.../TidalsWalkerTest.java` — loads profile on start, passes to walker
- `TidalsDaxWalkerPOC/` — DELETED (obsolete)