---
# tidals-scripts-jgd2
title: Add fairy ring test destinations to TidalsWalkerTest
status: completed
type: task
priority: normal
created_at: 2026-02-02T06:34:20Z
updated_at: 2026-02-02T07:00:53Z
---

Added fairy ring test destinations to ScriptUI. Fixed FairyRingUtils to skip the last-destination shortcut (obj.interact retries internally, wasting 12+ seconds when code doesn't match). Now goes straight to travel log Configure flow. Deployed and ready for testing.