---
# tidals-scripts-htme
title: Add Debug Tab to TidalsCannonballThiever
status: completed
type: task
priority: normal
created_at: 2026-01-30T05:56:43Z
updated_at: 2026-01-30T06:07:23Z
parent: tidals-scripts-yphx
---

Add verbose logging and a Debug UI tab to TidalsCannonballThiever.

## Checklist
- [ ] Add `public static volatile boolean verboseLogging = false` to main script
- [ ] Convert ScriptUI from single VBox to TabPane with Main + Debug tabs
- [ ] Add debug checkbox with Preferences persistence (key: PREF_DEBUG_ENABLED)
- [ ] Add `isVerbose()` and `logVerbose()` helpers to all task classes
- [ ] Add meaningful debug log lines to each task's execute/activate methods
- [ ] Build and verify with `osmb build TidalsCannonballThiever`