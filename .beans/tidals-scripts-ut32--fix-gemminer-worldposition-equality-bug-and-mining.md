---
# tidals-scripts-ut32
title: Fix GemMiner WorldPosition equality bug and mining flow improvements
status: completed
type: bug
created_at: 2026-01-30T02:32:39Z
updated_at: 2026-01-30T02:32:39Z
---

## Root Cause
WorldPosition does not override equals()/hashCode(), so all HashMap/HashSet lookups using WorldPosition keys silently failed. This meant:
- recentlyMinedRocks cooldown never filtered rocks (containsKey always false)
- emptyRockPositions never matched (contains always false)
- misclick position tracking never matched

## Fixes Applied
1. **WorldPosition → String keys**: Added posKey() helper returning "x,y,plane" string. Converted recentlyMinedRocks (Map<String,Long>), emptyRockPositionKeys (Set<String>), lastMisclickPositionKey (String)
2. **Removed unreliable respawn circle pre-scanning**: getRespawnCircleObjects() returned empty maps. Now relies on cooldown map + fast 'no ore' chat detection
3. **Cooldown updated to 56-62s**: Matches actual 59.4s gem rock respawn (was 18-22s)
4. **Adjacent + no swing pick = depleted**: When player is adjacent and gets no response, rock is put on cooldown instead of retrying as misclick
5. **skipIdleOnNextFind**: After 'no ore' or successful mine, skips IDLE wait and goes straight to TAPPING next rock
6. **Walk break conditions**: recoverFromStuckRock and walkToMine stop walking when a non-cooldown rock becomes interactable on screen
7. **Underground 'waiting for respawn'**: Proper pollFramesUntil that cleans expired cooldowns and breaks when a rock is available

## Files Changed
- TidalsGemMiner/src/main/java/tasks/Mine.java