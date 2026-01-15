# Phase 12: Fix Withdraw - Context

**Gathered:** 2026-01-14
**Status:** Ready for planning

<vision>
## How This Should Work

The current withdraw functionality types search terms correctly and items appear visually in the bank, but the detection doesn't "see" them — it scrolls past matching items to the bottom until failure.

The fix uses **sprite-based detection**: get the item's sprite from ItemID (like ScriptUI does for the "Craft Into" menu where each item shows its sprite), then use ImageSearchResult to find that sprite on screen. Verify via menu hook that the found image is the correct item before withdrawing.

Search types correctly → sprite detected on screen → menu hook confirms item → withdraw succeeds.

</vision>

<essential>
## What Must Be Nailed

- **Reliable item detection** — Finding items in bank results must work 100% of the time when they exist. This is the core problem: items are there visually but not being detected.

</essential>

<specifics>
## Specific Ideas

- Reference implementation: `examples/dAmethystMiner/src/main/java/main/ScriptUI.java` shows how to get item sprites from ItemID
- Use `ImageSearchResult` to locate sprites on screen (see `docs/ImageSearchResult.md`)
- Menu hook verification to confirm the found image matches the expected item
- Blue wizard hat and multiple other items are failing with current approach

</specifics>

<notes>
## Additional Context

This is a systemic issue affecting multiple items, not edge cases. The current detection method fundamentally doesn't work — sprite-based detection is the proposed replacement.

</notes>

---

*Phase: 12-fix-withdraw*
*Context gathered: 2026-01-14*
