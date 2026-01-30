---
# tidals-scripts-dmxq
title: Refactor GemCutter Process.waitUntilFinishedCrafting() to poll-based
status: completed
type: task
priority: normal
created_at: 2026-01-30T09:49:00Z
updated_at: 2026-01-30T10:02:46Z
---

Process.java uses an internal polling loop in waitUntilFinishedCrafting() rather than yielding back to the executor between checks. Should return to the executor and re-poll instead of looping internally, matching the poll-based architecture pattern.