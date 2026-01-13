# Walker – Best Practices (with Smooth Long‑Distance Walking)

This document consolidates **recommended Walker usage patterns**, including a proven approach for **smooth 10+ tile walking** inspired by Rats herbi behavior.

The goal is continuous, human‑like movement that:
- avoids idle ticks
- recovers quickly from small path issues
- keeps pushing forward while already walking

---

## Core Principles

1. **Do not click once and wait** for long distances  
2. **Re‑issue walk intents while moving**, on a human‑like cadence  
3. Prefer **minimap clicks for distance**, screen clicks for precision  
4. Keep **tile randomisation low** for long walks  
5. Always include **stall detection**

---

## Movement Safety: Stall Detection

Long walks can silently fail. Always track whether the player is *actually moving*.

### PlayerMovementTracker

```kotlin
class PlayerMovementTracker(private val script: Script) {
    private var currentPlayerLocation: WorldPosition? = null
    private var lastMovementTime: Long = System.currentTimeMillis()
    private val MOVEMENT_TIMEOUT_MS = 5 * 60 * 1000L

    fun updateLocation() {
        val newLocation = script.worldPosition

        if (currentPlayerLocation != null && currentPlayerLocation != newLocation) {
            lastMovementTime = System.currentTimeMillis()
        }

        currentPlayerLocation = newLocation

        if (timeSinceMovement() >= MOVEMENT_TIMEOUT_MS) {
            script.log("Player stalled, stopping script.")
            script.stop()
        }
    }

    fun timeSinceMovement(): Long =
        System.currentTimeMillis() - lastMovementTime

    fun reset() {
        lastMovementTime = System.currentTimeMillis()
        currentPlayerLocation = script.worldPosition
    }
}
```

Use this inside `doWhileWalking` so stalled paths never soft‑lock a script.

---

## Standard Walker Helpers

These are simple wrappers around `walker.walkTo` that add:
- break conditions
- UI hygiene during walking

```kotlin
fun Script.walkToPosition(
    position: WorldPosition,
    config: WalkConfig.Builder = MINIMAL_MINIMAP_CONFIG
): Boolean {
    return walker.walkTo(
        position,
        config
            .breakCondition { worldPosition.distanceTo(position) < 5 }
            .doWhileWalking {
                widgetManager.tabManager.closeContainer()
                config.build()
            }
            .build()
    )
}
```

This pattern is fine for **short or medium** walks.

---

## Smooth Long‑Distance Walking (10+ tiles)

### Why this is needed

For long walks, a single click can:
- end early
- stall on obstacles
- cause idle frames

Instead, we **nudge the walk forward while already moving**.

---

## Smooth Walk Strategy

For distances greater than ~10 tiles:

- Minimap‑only clicks
- Low or zero tile randomisation
- Re‑click every **700–1200 ms**
- Short timeout per “nudge”
- Stop when within `breakDistance`

---

## walkToPositionSmooth (Drop‑in)

```kotlin
fun Script.walkToPositionSmooth(
    target: WorldPosition,
    baseConfig: WalkConfig.Builder = MINIMAL_MINIMAP_CONFIG,
    breakDistance: Int = 5,
    reclickMinMs: Long = 700,
    reclickMaxMs: Long = 1200,
): Boolean {

    var lastReclickAt = 0L
    var nextInterval = random(reclickMinMs, reclickMaxMs)

    val tracker = PlayerMovementTracker(this).also { it.reset() }

    fun shouldReclick(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastReclickAt < nextInterval) return false
        lastReclickAt = now
        nextInterval = random(reclickMinMs, reclickMaxMs)
        return true
    }

    val cfg = baseConfig
        .breakCondition { worldPosition.distanceTo(target) < breakDistance }
        .doWhileWalking {
            widgetManager.tabManager.closeContainer()
            tracker.updateLocation()

            val me = worldPosition
            if (me != null && me.distanceTo(target) > 10 && shouldReclick()) {
                val stepCfg = WalkConfig.Builder()
                    .timeout(8000)
                    .breakDistance(breakDistance)
                    .setWalkMethods(false, true) // minimap only
                    .tileRandomisationRadius(0)
                    .minimapTapDelay(120, 260)
                    .allowInterrupt(true)
                    .build()

                walker.walkTo(target, stepCfg)
            }

            baseConfig.build()
        }
        .build()

    return walker.walkTo(target, cfg)
}
```

### Why this works

- The walker is **continuously reinforced**
- Small path errors self‑correct early
- Movement looks deliberate and uninterrupted
- Human‑like timing avoids spammy behavior

This is the missing piece that makes long walks feel “Rats smooth”.

---

## Optional Upgrade: A* Lookahead

Best possible behavior:

1. Build A* path
2. Select a **lookahead tile** (10–15 tiles ahead)
3. Nudge‑walk to that tile instead of the final destination
4. Recalculate periodically

This reduces over‑clicking the same destination and adapts better to bends.

---

## Recommended Defaults

| Setting | Value |
|------|------|
| Long‑walk threshold | 10 tiles |
| Re‑click interval | 700–1200 ms |
| Tile randomisation | 0–1 |
| Step timeout | ~8000 ms |
| Walk method | Minimap first |

---

## Summary

If you remember one rule:

> **For 10+ tiles, never rely on a single click. Re‑click while moving.**

This pattern composes cleanly with:
- `doWhileWalking`
- UI management
- stall detection
- A* pathing

It should be treated as the default for all long‑distance traversal.
