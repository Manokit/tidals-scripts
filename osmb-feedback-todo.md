# OSMB Feedback TODO (Remaining)

## TidalsGemMiner
- Remove tile-based color detection path and fallback; ObjectManager should be sufficient in-mine (`TidalsGemMiner/src/main/java/tasks/Mine.java`).
- Break up the major multi-function poll in mining: `Mine.execute()`/`mineTarget()` currently does idle wait → tap → approach wait → completion wait → result handling in one poll; refactor into poll-based states (`TidalsGemMiner/src/main/java/tasks/Mine.java`).
- Use `tapGameScreen` + `insideGameScreenFactor` for mining clicks; tile path uses `tap(point)` and object path uses hulls without visibility checks (`TidalsGemMiner/src/main/java/tasks/Mine.java`).
- Randomize remaining fixed timeouts/cooldowns in Gem Miner tasks (examples: `STUCK_TIMEOUT_MS`, `ROCK_SCAN_INTERVAL_MS`, `CHAT_REPEAT_WINDOW_MS`, `ROCK_COOLDOWN_MS`, `stationaryTimer.timeElapsed() > 250` in `Mine`; `HOP_COOLDOWN_MS`/`POST_HOP_STABILIZATION_MS` in `HopWorld`; `POST_HOP_GRACE_MS`/`POST_LOGIN_GRACE_MS`/zone min-maxs in `DetectPlayers`).

## TidalsCannonballThiever
- Break up the major multi-function poll in `PrepareForBreak.execute()` (tap safety tile → wait → trigger hop/break/AFK) into poll-based states (`TidalsCannonballThiever/src/main/java/tasks/PrepareForBreak.java`).
- Randomize remaining fixed timeouts/cooldowns (examples: `pollFramesUntil(..., 3000)` in `PrepareForBreak`/`Retreat`; `MIN_TIME_BETWEEN_CHECKS_MS = 2000`; `lastXpGain.timeElapsed() < 1500`; `WalkConfig.timeout(10000/15000)` in `StartThieving`/`ReturnToThieving`).
- Add movement timeout when waiting for deposit box to open after tap (currently only waits for interface open) (`TidalsCannonballThiever/src/main/java/tasks/DepositOres.java`).
- Use a walker breakCondition when walking to the deposit box (custom interaction) so walking stops once the box hull is visible/insideGameScreenFactor (`TidalsCannonballThiever/src/main/java/tasks/DepositOres.java`).

## TidalsChompyHunter
- Break up major multi-function polls into poll-based state machines:
  - `TidalsChompyHunter/src/main/java/tasks/AttackChompy.java` (full detection → attack → combat wait → pluck cycle in one execute).
  - `TidalsChompyHunter/src/main/java/tasks/DropToads.java` (walk + multi-drop loop in one execute).
  - `TidalsChompyHunter/src/main/java/tasks/FillBellows.java` (iterates bubbles + walk + interact in one execute).
  - `TidalsChompyHunter/src/main/java/tasks/InflateToads.java` (multi-toad loop + multi-attempt inflate loop in one execute).
- Replace 3D taps with `tapGameScreen` and add `insideGameScreenFactor` visibility checks:
  - `TidalsChompyHunter/src/main/java/tasks/FillBellows.java` uses `RetryUtils.tap` on a tile cube.
  - `TidalsChompyHunter/src/main/java/tasks/InflateToads.java` uses `script.getFinger().tap(clickPoint, "Inflate")`.
- Randomize remaining fixed timeouts/cooldowns (examples: `MONITORING_POLL_MS = 600`, `KILL_CONFIRMATION_TIMEOUT_MS = 20000`, `pollFramesUntil(..., 3000/2000/6000/5000/8000)` in `AttackChompy`/`InflateToads`; `WalkConfig.timeout(3000)` and `waitForPlayerToStop(..., 3000)` in `DropToads`; `POST_HOP_STABILIZATION_MS = 10000` in `HopWorld`).

## TidalsGemCutter
- Add walk + breakCondition to load bank objects when not in scene (currently logs "no bank found" and returns) (`TidalsGemCutter/src/main/java/tasks/Bank.java`).

## TidalsGoldSuperheater
- Add walk + breakCondition to load bank objects when not in scene (currently logs "no bank found" and returns) (`TidalsGoldSuperheater/src/main/java/tasks/Bank.java`).
- Add movement timeout while waiting for bank to open after interact (currently only visibility timeout) (`TidalsGoldSuperheater/src/main/java/tasks/Bank.java`).
