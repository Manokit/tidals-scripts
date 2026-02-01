---
# tidals-scripts-94rk
title: Reintegrate MortMyreFungusCollector fairy ring mode from Secondary-collector branch
status: completed
type: feature
priority: high
created_at: 2026-01-31T01:29:01Z
updated_at: 2026-01-31T02:03:33Z
---

## Background

The `Secondary-collector` branch has ~46 commits of work on `MortMyreFungusCollector.java` implementing **fairy ring mode** — a complete alternative collection path. That branch diverged early (pre-osmb-poll-changes merge) so it also has stale parallel copies of changes to other scripts that are already on main. We only want the SecondaryCollector-specific changes brought to main.

The branch also has utility changes but those are pre-OSMB-feedback and main's utility code is newer/better — skip those.

## What to bring over

**Files to port (from Secondary-collector branch):**

1. **\`TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java\`** — the big one (~1050 lines of changes). Must be manually adapted to current main's code style (main uses \`RandomUtils.weightedRandom\`, branch uses \`script.random\`; main uses \`pollFramesHuman(() -> true, ...)\`, etc.)

2. **\`TidalsSecondaryCollector/src/main/java/main/TidalsSecondaryCollector.java\`** — minor changes: new regions (fairy ring 13877, zanaris 9541), inventory tab opening fix, teal paint theme, broader exception catches

## Feature summary (what fairy ring mode adds)

### Mode Detection
- \`Mode\` enum: \`VER_SINHAZA\` vs \`FAIRY_RING\`
- Auto-detected during \`verifyRequirements()\` based on equipped/inventory items
- Dramen staff auto-equip from inventory

### Collection (fairy ring mode)
- New 3-log tile location at (3474, 3419, 0) with \`THREE_LOG_AREA\`
- 3 log positions instead of 4
- Inventory bloom casting (bloom tool in inventory, not equipped)
- Mode-aware \`detectFungusPositions()\` and \`collect()\`
- Inventory full dialogue detection to correct cached count

### Mory Hard Diary Detection
- Dynamic \`fungusPerLog\` (1 or 2) with verification on first "full" inventory
- \`verifyDiaryStatus()\` checks actual vs cached inventory count
- No-diary mode: forces full prayer restoration between trips

### Banking (fairy ring mode)
- Zanaris banking via fairy ring: monastery→zanaris or mort myre→zanaris
- \`useZanarisBanking()\`, \`walkToZanarisBank()\`
- \`useMonasteryFairyRingToZanaris()\`, \`useMortMyreFairyRingToZanaris()\`
- Keeps bloom tools in inventory during deposit

### Return to Area (fairy ring mode)
- \`useFairyRingReturn()\` — routes through ardy cloak → monastery → fairy ring → BKR
- \`useZanarisFairyRingReturn()\` — direct from zanaris after banking
- \`interactWithMonasteryFairyRing()\` — "last-destination (bkr)" interaction
- \`walkToFairyRingLogTile()\`

### Other Changes
- Quest cape constant (alternative to ardy cloak for fairy ring teleport)
- \`getSpecificObjectAt()\` helper for reliable fairy ring detection at exact coords
- State machine changes: mode-aware \`determineState()\` and \`returnToArea()\`
- Broader exception catches (\`Exception\` instead of \`SpellNotFoundException\`/\`IOException\`)
- Various \`pollFramesHuman\` lambda fixes (\`() -> true\` → \`() -> false\`)

## Approach

Since the branch diverged pre-feedback, do NOT cherry-pick or merge. Instead:

1. Read the current main version of MortMyreFungusCollector
2. Read the branch version for reference (at \`/Users/zaffre/Documents/Engineering/Projects/Scripts-Project/TidalsSecondaryCollector-WT/\`)
3. Manually port each feature group, adapting to main's current coding conventions
4. Apply pollFramesHuman/delay fixes from common-mistakes.md
5. Build and verify

## Checklist

- [x] Add Mode enum and detection field to MortMyreFungusCollector
- [x] Port verifyRequirements() mode detection + dramen staff auto-equip
- [x] Add fairy ring constants (3-log tile, zanaris areas, monastery fairy ring)
- [x] Port mode-aware determineState() (diary detection, prayer logic)
- [x] Port verifyDiaryStatus() method
- [x] Port mode-aware collect() (3-log positions, inventory bloom, full-dialogue detection)
- [x] Port mode-aware detectFungusPositions()
- [x] Port zanaris banking methods (useZanarisBanking, walkToZanarisBank, fairy ring methods)
- [x] Port fairy ring return methods (useFairyRingReturn, useZanarisFairyRingReturn, etc.)
- [x] Port getSpecificObjectAt() helper
- [x] Update TidalsSecondaryCollector.java (regions, paint theme, inventory tab fix)
- [x] Quest cape constant
- [x] Fix all pollFramesHuman/delay patterns to match current main conventions
- [x] Build TidalsSecondaryCollector successfully
- [x] Create changelog entry