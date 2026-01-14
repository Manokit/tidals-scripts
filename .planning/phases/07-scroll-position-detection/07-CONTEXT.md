# Phase 7: Scroll Position Detection - Context

**Gathered:** 2026-01-14
**Status:** Ready for planning

<vision>
## How This Should Work

Simple boolean methods — `isAtTop()` and `isAtBottom()` — that return true/false based on scroll bar sprite position. Clean and direct, matching the existing sprite detection pattern from Phase 6.

Detection uses fixed Y coordinates of specific scroll bar sprites:
- **At top:** Sprite ID 789 (top half of scroll bar) at Y = 334
- **At bottom:** Sprite ID 791 (bottom half of scroll bar) at Y = 507

The scroll bar has two halves that resize based on content. Checking the correct half for each position ensures consistent results regardless of bank content size.

</vision>

<essential>
## What Must Be Nailed

- **Reliability over speed** — Detection must be 100% accurate. False positives break the verified withdrawal flow in Phase 9.
- **Check the right sprite half** — Top detection uses sprite 789, bottom detection uses sprite 791. Using the wrong sprite gives inconsistent values.

</essential>

<specifics>
## Specific Ideas

- Match the existing sprite detection approach from openSearch() (Phase 6)
- Y coordinate 334 for top, 507 for bottom — these are fixed values discovered through testing
- Boolean return type keeps the API simple for callers

</specifics>

<notes>
## Additional Context

User tested scroll bar behavior and found the fixed Y coordinates. The key insight is that the scroll bar resizes, so each half reports different Y values when scrolled to the opposite extreme. By checking the correct half (789 for top, 791 for bottom), detection remains reliable regardless of scroll bar size.

</notes>

---

*Phase: 07-scroll-position-detection*
*Context gathered: 2026-01-14*
