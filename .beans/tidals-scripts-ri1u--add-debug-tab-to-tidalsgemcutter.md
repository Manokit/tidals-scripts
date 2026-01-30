---
# tidals-scripts-ri1u
title: Add Debug Tab to TidalsGemCutter
status: completed
type: task
priority: normal
created_at: 2026-01-30T05:56:43Z
updated_at: 2026-01-30T06:07:23Z
parent: tidals-scripts-yphx
---

Add verbose logging and a Debug UI tab to TidalsGemCutter. Already has TabPane with Main + Webhooks tabs.

## Checklist
- [ ] Add `public static volatile boolean verboseLogging = false` to main script
- [ ] Add a third 'Debug' tab to existing TabPane
- [ ] Add debug checkbox with Preferences persistence (key: PREF_DEBUG_ENABLED)
- [ ] Add `isVerbose()` and `logVerbose()` helpers to all task classes
- [ ] Add meaningful debug log lines to each task's execute/activate methods
- [ ] Build and verify with `osmb build TidalsGemCutter`