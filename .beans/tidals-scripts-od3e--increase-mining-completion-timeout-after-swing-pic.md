---
# tidals-scripts-od3e
title: Increase mining completion timeout after swing pick
status: completed
type: bug
priority: normal
created_at: 2026-01-30T02:38:44Z
updated_at: 2026-01-30T02:39:26Z
---

waitForMiningCompletion has a 6-8s total timeout. Once swing pick is detected, the remaining time is too short for slow gem rock mines. Should use ~20s timeout after swing pick confirmation. The rock was actively being mined but the bot gave up and moved to the next rock.