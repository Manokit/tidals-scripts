---
# tidals-scripts-4owu
title: Refactor collectGroundFungus to poll-based
status: completed
type: bug
priority: critical
created_at: 2026-01-31T02:09:40Z
updated_at: 2026-01-31T02:11:37Z
---

collectGroundFungus() contains a for-loop that blocks the framework for 3+ seconds per bloom cycle. Convert to indexed state machine where each poll picks one log.