---
# tidals-scripts-0b0t
title: Refactor TidalsCannonballThiever/EscapeJail.java to poll-based states
status: completed
type: feature
priority: high
created_at: 2026-01-28T08:45:33Z
updated_at: 2026-01-28T08:58:05Z
parent: apzr
---

# Refactor EscapeJail.java to Poll-Based States

Break up jail escape logic into clean check → handle → return states.

## Checklist

- [x] Read current EscapeJail.java and identify linear patterns
- [x] Identify all distinct states (in jail, at door, picking lock, escaped, etc.)
- [x] Refactor to state machine pattern
- [x] Test jail escape flow (build passes, logic verified)

## Changes Made

**Linear patterns removed:**
- Removed try-finally block with `isEscaping` flag
- Removed chained method calls: teleport → walk, picklock → walk
- Removed `walkMinimapPath()` while loop with custom waypoint logic

**States identified and implemented:**
1. IN_CELL - pick lock or teleport
2. WAITING_FOR_TELEPORT - poll for teleport completion
3. WALKING_BACK - minimap/screen walk to stall
4. AT_STALL - reset and complete

**Poll-based structure:**
- `execute()` checks position and does ONE action per call
- `handleInCell()` - attempts escape method
- `handleWalkingBack()` - walks using walker with breakCondition
- `finishEscape()` - resets state when at stall

**Simplified walking:**
- Replaced custom `walkMinimapPath()` with `walker.walkTo()` + breakCondition
- Two-phase walk: minimap when far, screen walk for final approach