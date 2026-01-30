---
# tidals-scripts-zanw
title: Convert TidalsGemMiner Debug to UI Toggle
status: completed
type: task
priority: normal
created_at: 2026-01-30T05:56:43Z
updated_at: 2026-01-30T06:07:23Z
parent: tidals-scripts-yphx
---

TidalsGemMiner already has VERBOSE_LOGGING as a hardcoded final boolean. Convert it to a runtime UI toggle.

## Checklist
- [ ] Change `public static final boolean VERBOSE_LOGGING = true` to `public static volatile boolean verboseLogging = false`
- [ ] Convert ScriptUI from single VBox to TabPane with Main + Debug tabs
- [ ] Add debug checkbox with Preferences persistence (key: PREF_DEBUG_ENABLED)
- [ ] Update all task references from `VERBOSE_LOGGING` to `verboseLogging`
- [ ] Build and verify with `osmb build TidalsGemMiner`