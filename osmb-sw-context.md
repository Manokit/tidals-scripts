# OSMB Script-Wide Fixes Context

> **Branch:** `osmb-sw-fixes`
> **Date:** 2026-01-24
> **Purpose:** Document timing/pattern changes for testing and debugging

This document tracks all changes made to address code reviewer feedback. If timing feels off during testing, reference this to identify potential culprits and how to tweak them.

---

## Summary of Changes

### 1. Lambda Fix: `() -> false` → `() -> true`

**What changed:**
```java
// BEFORE (wrong - condition never met, relies on timeout)
script.pollFramesUntil(() -> false, 500);
script.pollFramesHuman(() -> false, delay);
script.submitTask(() -> false, 300);

// AFTER (correct - condition already met, pure delay)
script.pollFramesUntil(() -> true, 500);
script.submitTask(() -> true, 300);
```

**Why:** The lambda is the "break condition" - `false` means "keep waiting", `true` means "condition met". For pure delays, we want the condition to be instantly true so it just waits the specified time.

**If timing feels off:** This change should have no functional impact - both versions wait the full timeout. If behavior differs, the original code may have been relying on side effects.

---

### 2. Deprecated `script.random()` → `RandomUtils`

**What changed:**
```java
// BEFORE
script.pollFramesHuman(() -> false, script.random(200, 400));
int delay = script.random(300, 500);

// AFTER (for delays - weighted distribution)
script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(200, 800, 0.002));

// AFTER (for conditionals/timeouts - uniform distribution)
if (RandomUtils.uniformRandom(0, 3) == 0) { ... }
int timeout = RandomUtils.uniformRandom(6000, 8000);
```

**Conversion patterns used:**

| Original | Replacement | When to use |
|----------|-------------|-------------|
| `script.random(min, max)` | `RandomUtils.weightedRandom(min, max*2, 0.002)` | Delays between actions |
| `script.random(min, max)` | `RandomUtils.uniformRandom(min, max)` | Conditionals, timeouts |
| `script.random(min, max)` | `RandomUtils.gaussianRandom(min, max, mean, stdev)` | When you need bell curve |

**If timing feels too fast:**
- Increase the max multiplier: `weightedRandom(200, 1200, 0.002)` instead of `(200, 800, 0.002)`
- Decrease lambda (3rd param): `weightedRandom(200, 800, 0.001)` makes higher values more likely

**If timing feels too slow:**
- Decrease the max: `weightedRandom(200, 600, 0.002)`
- Increase lambda: `weightedRandom(200, 800, 0.005)` makes lower values more likely

**Lambda parameter guide (0.002 is default):**
- `0.001` = more spread, longer average delays
- `0.002` = balanced (used throughout)
- `0.005` = tighter clustering near minimum
- `0.01` = very tight, almost always near minimum

---

### 3. `pollFramesHuman` → `pollFramesUntil`

**What changed:**
```java
// BEFORE
script.pollFramesHuman(() -> false, script.random(200, 400));

// AFTER
script.pollFramesUntil(() -> true, RandomUtils.weightedRandom(200, 800, 0.002));
```

**Why:** `pollFramesHuman` was being misused with `() -> false`. The humanization is now handled by `RandomUtils.weightedRandom()` which provides similar distribution characteristics.

**If humanization feels different:** `pollFramesHuman` had internal variance. You can add occasional longer pauses:
```java
int delay = RandomUtils.weightedRandom(200, 800, 0.002);
if (RandomUtils.uniformRandom(10) == 0) {
    delay += RandomUtils.uniformRandom(300, 600);  // 10% chance of extra pause
}
script.pollFramesUntil(() -> true, delay);
```

---

### 4. Exception Handling Narrowed

**What changed:**
```java
// BEFORE (swallows control exceptions!)
try {
    // HTTP call or any code
} catch (Exception e) {
    log("error: " + e.getMessage());
}

// AFTER (HTTP calls only)
try {
    // HTTP call
} catch (IOException e) {
    log("error: " + e.getMessage());
}

// AFTER (spell casting)
try {
    selectSpell(...);
} catch (SpellNotFoundException e) {
    log("spell not found");
}
```

**Why:** Broad `catch (Exception)` swallows `PriorityTaskException` and `HaltScriptException` which OSMB uses for:
- AFK handling
- Scheduled world hops
- Break handling
- Script stopping

**If script doesn't respond to AFK/hops:** Check for remaining broad exception catches.

---

## Files Changed By Script

### TidalsGemCutter
| File | Changes |
|------|---------|
| `tasks/Process.java` | RandomUtils, null checks on getRandomItem(), lambda fix |
| `tasks/Bank.java` | RandomUtils, lambda fix |
| `tasks/Setup.java` | Removed unnecessary openTab calls |
| `main/TidalsGemCutter.java` | IOException narrowing |

### TidalsGoldSuperheater
| File | Changes |
|------|---------|
| `tasks/Process.java` | RandomUtils, lambda fix |
| `tasks/Bank.java` | RandomUtils, lambda fix |
| `tasks/Setup.java` | Removed openTab, RandomUtils |
| `main/TidalsGoldSuperheater.java` | IOException narrowing |

### TidalsSecondaryCollector
| File | Changes |
|------|---------|
| `strategies/MortMyreFungusCollector.java` | RandomUtils, lambda fix, SpellNotFoundException |
| `main/TidalsSecondaryCollector.java` | IOException narrowing |

### TidalsChompyHunter
| File | Changes |
|------|---------|
| `tasks/AttackChompy.java` | RandomUtils, lambda fix |
| `tasks/Setup.java` | RandomUtils, lambda fix |
| `tasks/DropToads.java` | RandomUtils, lambda fix |
| `tasks/InflateToads.java` | RandomUtils, lambda fix |
| `tasks/HopWorld.java` | RandomUtils, lambda fix |
| `main/TidalsChompyHunter.java` | IOException narrowing |

### TidalsCannonballThiever
| File | Changes |
|------|---------|
| `tasks/EscapeJail.java` | RandomUtils, lambda fix |
| `tasks/StartThieving.java` | RandomUtils, lambda fix |
| `tasks/DepositOres.java` | RandomUtils, lambda fix, exception removal |
| `tasks/Setup.java` | RandomUtils, lambda fix |
| `tasks/WaitAtSafety.java` | RandomUtils, lambda fix |
| `tasks/DismissDialogue.java` | RandomUtils, lambda fix |
| `tasks/ReturnToThieving.java` | RandomUtils, lambda fix |
| `tasks/Retreat.java` | Exception narrowing |
| `tasks/MonitorThieving.java` | Exception narrowing |
| `tasks/PrepareForBreak.java` | Exception removal |
| `main/TidalsCannonballThiever.java` | IOException narrowing |
| `main/ScriptUI.java` | IOException narrowing |
| `utils/XPTracking.java` | Exception narrowing |

### TidalsGemMiner
| File | Changes |
|------|---------|
| `tasks/Cut.java` | RandomUtils, lambda fix |
| `tasks/Bank.java` | RandomUtils, lambda fix |
| `tasks/Mine.java` | RandomUtils, lambda fix, RuntimeException |
| `main/TidalsGemMiner.java` | IOException narrowing |
| `main/ScriptUI.java` | IOException narrowing |
| `utils/XPTracking.java` | RuntimeException |

---

## Quick Timing Adjustment Reference

If a script feels too fast/slow after these changes, here are the key values to tweak:

```java
// Current pattern (balanced):
RandomUtils.weightedRandom(200, 800, 0.002)

// Slower (more human-like):
RandomUtils.weightedRandom(200, 1200, 0.001)

// Faster (still randomized):
RandomUtils.weightedRandom(200, 600, 0.003)

// Bell curve around specific value:
RandomUtils.gaussianRandom(200, 800, 400, 100)  // clusters around 400ms
```

---

## Testing Checklist

When testing each script, verify:

- [ ] **Delays feel natural** - not too robotic, not too slow
- [ ] **AFK kicks in** when expected (broad exception fix)
- [ ] **Scheduled hops work** (broad exception fix)
- [ ] **Breaks work** (broad exception fix)
- [ ] **Script stops cleanly** when requested

If any of these fail, check the exception handling changes first.
