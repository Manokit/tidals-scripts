---
# tidals-scripts-yphx
title: Add Debug Mode UI Tab to All Scripts
status: completed
type: epic
priority: normal
created_at: 2026-01-30T05:56:30Z
updated_at: 2026-01-30T06:07:23Z
---

Port the VERBOSE_LOGGING debug mode pattern from TidalsChompyHunter to TidalsCannonballThiever, TidalsGoldSuperheater, and TidalsGemCutter. Also convert TidalsGemMiner's existing hardcoded flag to use the UI toggle.

Instead of a compile-time `public static final boolean VERBOSE_LOGGING = true` flag, each script should get a **Debug tab** in its ScriptUI (like the existing Webhooks/Discord tabs) with a checkbox to enable/disable debug mode at runtime.

## Pattern

### Main Script
- Change `public static final boolean VERBOSE_LOGGING = true` to `public static volatile boolean verboseLogging = false` (runtime-toggleable, default off)
- Remove `final` so ScriptUI can set it

### ScriptUI Changes
- Add a "Debug" tab to the TabPane (create TabPane if script only has a single VBox)
- Add a checkbox: "Enable verbose logging"
- Persist with Preferences key `PREF_DEBUG_ENABLED`
- On save, sync to main script's `verboseLogging` static field

### Task Pattern (already exists in Chompy/GemMiner)
Each task that uses debug logging has:
```java
private boolean isVerbose() {
    return MainScript.verboseLogging;
}
private void logVerbose(String message) {
    if (!isVerbose()) return;
    script.log(getClass(), "[DEBUG] " + message);
}
```

## Scripts to Update

1. **TidalsCannonballThiever** - Add debug mode + UI tab (currently has no debug logging or tabs)
2. **TidalsGoldSuperheater** - Add debug mode + UI tab (currently has no debug logging, single VBox UI)
3. **TidalsGemCutter** - Add debug mode + UI tab (already has TabPane with Main + Webhooks tabs)
4. **TidalsGemMiner** - Convert hardcoded VERBOSE_LOGGING to UI-toggled + add Debug tab (currently single VBox UI)