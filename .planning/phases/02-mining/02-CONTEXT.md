# Phase 2: Mining - Context

**Gathered:** 2026-01-16
**Status:** Ready for planning

<vision>
## How This Should Work

The script mines gem rocks at either location (upper or underground) with smart rock selection — always targeting the nearest available rock that isn't depleted.

The two locations behave differently:
- **Underground:** Plenty of rocks, never runs out. Just keep mining the nearest available.
- **Upper (surface):** Limited rocks. When all are depleted, world hop until finding available rocks.

The script should feel reliable and autonomous — it handles whatever situation arises without getting stuck.

</vision>

<essential>
## What Must Be Nailed

- **Never gets stuck** — Failsafes that recover from any weird state. This is the top priority.
- **Location-aware behavior** — Underground keeps mining, upper level hops worlds when depleted
- **Nearest available rock** — Always mine the closest rock that isn't currently depleted

</essential>

<specifics>
## Specific Ideas

- World hopping on upper level when no rocks available
- Underground doesn't need world hop logic (always has rocks)
- Smart enough to detect depleted vs available rocks

</specifics>

<notes>
## Additional Context

No additional notes

</notes>

---

*Phase: 02-mining*
*Context gathered: 2026-01-16*
