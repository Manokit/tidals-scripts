---
# tidals-scripts-n8v4
title: Fix first toad dropping at wrong position - walk break condition too eager
status: completed
type: bug
priority: normal
created_at: 2026-01-30T06:55:11Z
updated_at: 2026-01-30T06:55:35Z
---

Walk to drop area uses breakCondition that fires when player enters the polygon edge, but target is deeper inside. First toad drops at the edge tile instead of the target. Fix: remove the breakCondition and let the walker reach the actual target tile (breakDistance 0 already handles this).