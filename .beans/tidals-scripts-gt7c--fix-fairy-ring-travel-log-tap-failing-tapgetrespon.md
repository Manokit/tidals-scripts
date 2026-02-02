---
# tidals-scripts-gt7c
title: Fix fairy ring travel log tap failing - tapGetResponse used on UI overlay
status: completed
type: bug
priority: normal
created_at: 2026-02-02T09:15:48Z
updated_at: 2026-02-02T09:30:43Z
---

## Problem
`tapGetResponse(true, resultArea)` internally uses game-screen filtering that rejects UI overlay coordinates → `No tappable game screen points for shape: Rectangle`.

## Fix (two changes)
1. **Replaced `tapGetResponse` with `tap()`** — UI overlays need `tap()`, not `tapGameScreen`-family methods
2. **Changed `tap(resultArea)` to `tap(resultArea, "use code")`** — long-press to open context menu and explicitly select the 'use code' entry instead of unreliable plain left-click

## File
`utilities/src/main/java/utilities/FairyRingUtils.java` line ~292

## Status
Built and ready to test.