# Phase 5: Restock Logic - Context

**Gathered:** 2026-01-16
**Status:** Ready for planning

<vision>
## How This Should Work

Smart diff approach — compare what the player currently has equipped and in inventory against the loadout baseline, then withdraw only what's missing. No wasteful "deposit all and re-withdraw everything" cycles.

The restock happens in a logical order: equipment first (check/equip gear), then inventory. Equipment is more critical for combat scripts, so getting that right before filling inventory slots makes sense.

Quantity modes affect behavior:
- **Exact**: Take up to N items (soft requirement — take what's available)
- **Unlimited**: Take entire stack from bank (soft requirement)
- **Minimum**: Must have at least N (hard requirement — fail if unmet)

This means minimum mode is the "I can't proceed without this" flag, while exact/unlimited are "take what you can" modes.

</vision>

<essential>
## What Must Be Nailed

- **Accuracy above all** — Never withdraw wrong items, never miss something that's needed. The core promise is reliable restocking. If it gets items wrong, the whole utility breaks down.
- **Equipment-first order** — Check and equip gear before handling inventory items
- **Mode-dependent failures** — Minimum mode fails the restock if unmet; exact/unlimited take what's available and continue

</essential>

<specifics>
## Specific Ideas

- Equipment checked/equipped before inventory is filled
- Degradable items (Barrows, crystal) should match any charge variant when checking "do I have this?"
- Clear feedback to the script about what happened — what was missing, what was restocked, what couldn't be fulfilled

</specifics>

<notes>
## Additional Context

This phase builds on the existing BankingUtils patterns. The loadout data model from Phase 1 already has quantity modes and fuzzy flags. Phase 2's item resolution handles degradable matching.

The output of this phase is the comparison/withdrawal logic — script integration (actually using this in banking tasks) comes in Phase 6.

</notes>

---

*Phase: 05-restock-logic*
*Context gathered: 2026-01-16*
