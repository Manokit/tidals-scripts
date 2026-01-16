# TidalsSecondaryCollector - Fairy Ring Mode

## What This Is

TidalsSecondaryCollector with dual-mode Mort Myre fungus collection. Auto-detects equipment to seamlessly support both Ver Sinhaza (Drakan's Medallion) and Fairy Ring (Dramen staff) modes without user configuration.

## Core Value

Auto-detect equipment to seamlessly support both Ver Sinhaza (Drakan's Medallion) and Fairy Ring modes without requiring user configuration.

## Requirements

### Validated

- ✓ Ver Sinhaza mode with Drakan's Medallion — existing
- ✓ 4-log tile collection at 3667, 3255, 0 — existing
- ✓ Crafting cape and Ver Sinhaza banking — existing
- ✓ Ardy cloak prayer restoration — existing
- ✓ Lumbridge teleport fallback — existing
- ✓ Bloom casting from equipment — existing
- ✓ Auto-detect mode from equipment (Drakan's Medallion vs Dramen staff) — v1.0
- ✓ Fairy ring mode: 3-log tile collection at 3474, 3419, 0 — v1.0
- ✓ Cast bloom from inventory instead of equipment slot — v1.0
- ✓ Banking via fairy ring → Zanaris → walk to bank chest — v1.0
- ✓ Return via fairy ring "Last-destination (BKR)" with validation — v1.0
- ✓ Safety check: terminate if BKR not configured as last destination — v1.0

### Active

**Current Milestone: v1.1 Flexible Validation**

- [ ] Flexible equipment validation — check equipment AND inventory for required items
- [ ] Add region 13877 to regionsToPrioritise() — faster startup in fairy ring mode

**Backlog:**

- [ ] Quest cape support for return teleport (Legends Guild fairy ring)

### Out of Scope

- UI selection for mode — auto-detection is simpler and less error-prone
- Supporting other fairy ring destinations — only BKR (Mort Myre) needed
- House fairy ring support — adds complexity without significant benefit

## Context

**Existing Architecture:**
- `MortMyreFungusCollector.java` implements `SecondaryCollectorStrategy` interface
- Strategy pattern with `determineState()`, `collect()`, `bank()`, `restorePrayer()`, `returnToArea()`
- Ver Sinhaza mode uses Drakan's Medallion for both banking and return teleports

**Fairy Ring Mode Differences:**
- Collection tile: 3474, 3419, 0 (3-log tile vs 4-log tile)
- Log positions: (3473, 3418), (3473, 3420), (3475, 3420)
- Bloom: cast from inventory (not equipment)
- Banking: fairy ring at 3469, 3431, 0 → "Zanaris" → walk to bank chest
- Return: ardy cloak → monastery → walk/teleport to fairy ring → "Last-destination (BKR)"

**Key Positions:**
- 3-log standing tile: 3474, 3419, 0
- Mort Myre fairy ring: 3469, 3431, 0 (region 13877)
- Zanaris landing: 2412, 4434, 0
- Zanaris bank path: (2412,4441) → (2407,4444) → (2401,4447) → (2397,4447) → (2394,4450) → (2391,4453) → (2386,4457) → (2384,4459)
- Monastery fairy ring: ~2658, 3230, 0
- Monastery to fairy ring path: (2609,3221) → (2616,3222) → (2621,3222) → (2626,3221) → (2631,3222) → (2636,3221) → (2641,3224) → (2645,3226) → (2649,3229) → (2654,3230)
- Legends Guild fairy ring: 2740, 3351, 0

**Equipment Detection:**
- Dramen staff equipped + bloom tool in inventory + (ardy cloak OR quest cape) equipped → Fairy Ring mode
- Drakan's Medallion equipped + bloom tool equipped → Ver Sinhaza mode (existing)

**Fairy Ring Interaction (from dkTravel.java):**
- `ring.interact("zanaris")` — direct teleport to Zanaris
- `ring.interact("last-destination (bkr)")` — return via last destination
- Must validate menu contains "bkr" before interacting; stop script if misconfigured

## Constraints

- **API**: OSMB color bot — fairy ring interaction via menu text matching
- **Equipment**: Dramen staff required for fairy ring usage
- **Prayer**: Ardy cloak always required for monastery teleport (even with quest cape)
- **Safety**: Must validate BKR is configured before attempting return teleport

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Auto-detect mode from equipment | Simpler UX, no configuration needed | ✓ Good - works seamlessly |
| Mode flag instead of separate class | Single strategy file with mode branching | ✓ Good - keeps code maintainable |
| Bloom from inventory for fairy ring mode | Dramen staff occupies weapon slot | ✓ Good - required for fairy ring |
| Always use ardy cloak for prayer | Simplifies logic, quest cape doesn't restore prayer | ✓ Good - required for monastery teleport |
| BKR validation via tapGetResponse | Safety check before return teleport | ✓ Good - prevents stuck states |
| Script termination on BKR misconfiguration | Safety-first approach | ✓ Good - clear user guidance |

## Context

Shipped v1.0 with 2,010 lines changed in MortMyreFungusCollector.java.
Tech stack: OSMB color bot, Java.
Both modes fully functional with auto-detection at startup.
One low-priority tech debt item: region IDs not added to regionsToPrioritise() (performance only).

---
*Last updated: 2026-01-16 after v1.1 milestone start*
