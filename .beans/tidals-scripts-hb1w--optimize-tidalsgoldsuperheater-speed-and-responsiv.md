---
# tidals-scripts-hb1w
title: Optimize TidalsGoldSuperheater speed and responsiveness
status: completed
type: bug
priority: normal
created_at: 2026-01-28T10:35:18Z
updated_at: 2026-01-28T10:37:19Z
---

## Problem

The Gold Superheater script was running slowly with excessive delays. Looking at the logs:
- Lots of empty poll cycles with no visible activity
- Long gaps between casts (5-6 seconds between poll markers)
- No logging showed what was actually happening during superheating

## Root Cause Analysis

1. **Excessive delays between casts**: `Process.java:114-121` used 1200-3500ms delays - superheat animation only takes ~600ms
2. **No logging during normal operation**: `superheatOneOre()` only logged failures, not successes
3. **No cast verification**: Script assumed tap succeeded without checking

## Checklist

- [x] Reduce superheat delay to appropriate range (400-600ms verification + 200-500ms post-cast)
- [x] Add logging for successful superheat casts
- [x] Add cast verification (ore count decreased)
- [x] Review and optimize banking delays (reduced from 2500-4000ms to 800-1500ms for bank close, etc)
- [x] Build and test the script

## Changes Made

See `TidalsGoldSuperheater/Changes/TidalsGoldSuperheater-01-28-Changes.md` for full details.

### Summary
- Reduced superheat delays from 1200-3500ms to 600-1100ms total
- Added cast verification using ore count before/after
- Added logging for all operations
- Reduced banking delays significantly