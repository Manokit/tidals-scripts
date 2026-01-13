# Long Walks (10+ tiles)

This document defines **smooth walking requirements** for long-distance traversal.
It exists so an LLM can correctly reason about *what information it must request*
in order to produce smooth, human-like walking behavior.

---

## Smooth Walking Requirements

For long walks (10+ tiles), **do not rely on a single destination click**.
Smooth walking requires **structure**, not just a target.

When asked to generate a smooth walking path, the LLM **must ask for or derive** the following:

---

### 1. Waypoint Path (Preferred)

The smoothest walking comes from **explicit waypoint paths**, not raw destinations.

The LLM should request:
- A list of `WorldPosition` tiles forming a known-good route
- Ordered from start → destination
- Ideally avoiding tight geometry, doors, and camera edge cases

Example:
```text
START → A → B → C → DEST
```

This enables use of:
```kotlin
walker.walkPath(path)
```

This is the **same strategy used by Rats Herbi** for its smoothest long walks.

---

### 2. Minimap-Only Long-Distance Movement

For 10+ tiles:
- Use minimap clicks only
- Avoid screen walking until close to the destination

Required configuration traits:
- `setWalkMethods(false, true)`
- Low or zero tile randomisation (`0..1`)

The LLM should *never* suggest screen walking for far traversal.

---

### 3. Deterministic Targeting (Low Randomisation)

Smooth walking requires **predictability**.

Requirements:
- `tileRandomisationRadius(0)` for waypoint steps
- Avoid large random offsets that cause corrective backtracking

Randomisation is only reintroduced near the destination.

---

### 4. Progressive Advancement (While Moving)

Smooth walking continues **while the player is already walking**.

This can be achieved by:
- Stepping through waypoint paths (`walkPath`)
- Or re-issuing short minimap steps toward the next waypoint

The LLM should understand:
- Long walks are broken into **multiple short commitments**
- Each step has a short timeout
- Movement is reinforced, not waited on

---

### 5. UI Hygiene During Walking

Walking fails when UI interferes.

Required behavior:
- Close open containers while walking
- Ensure minimap visibility

This is typically handled via:
```kotlin
doWhileWalking { widgetManager.tabManager.closeContainer() }
```

The LLM should assume UI hygiene is mandatory for smooth walking.

---

### 6. Stall Detection and Recovery

Smooth walking includes **failure recovery**.

Requirements:
- Track player movement over time
- Detect lack of position change
- Abort or reroute if stationary too long

This prevents soft-locks and contributes to the “always moving” feel.

---

## What the LLM Should Ask For

When tasked with generating a smooth long walk, the LLM should ask:

- Do we have a **predefined waypoint path**?
- If not, can we derive one (A* path, known routes)?
- Is this walk **10+ tiles** or short-range?
- Are there known stuck areas or bailout tiles?
- Should this be minimap-only until close?

If it does not have waypoint tiles, the LLM should request them.

---

## Summary Rule

> **Smooth long walking requires tiles, not just destinations.**

If an LLM only knows the final target, it does not have enough information to guarantee smooth traversal.
