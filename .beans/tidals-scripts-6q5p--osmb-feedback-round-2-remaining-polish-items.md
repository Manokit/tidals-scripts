---
# tidals-scripts-6q5p
title: 'OSMB Feedback Round 2: Remaining Polish Items'
status: completed
type: epic
priority: normal
created_at: 2026-01-28T10:47:33Z
updated_at: 2026-01-30T03:57:45Z
---

Second pass through OSMB code review feedback. These are items that weren't fully addressed in the first round.

## Overview

The original feedback focused on:
1. Poll-based architecture (one action per poll)
2. Using `tapGameScreen()` instead of raw `tap()` for 3D interactions
3. Proper visibility checks with `insideGameScreenFactor()`
4. Randomizing ALL timeout values
5. Adding MovementChecker for misclick detection
6. Walking with breakCondition when objects aren't in scene

## Scripts Affected
- TidalsGemMiner
- TidalsCannonballThiever  
- TidalsChompyHunter
- TidalsGemCutter
- TidalsGoldSuperheater

## Reference
- `osmb-feedback-todo.md` - Remaining items list
- `osmb_code_review_feedback.md` - Original detailed feedback
- `docs/poll-based-architecture.md` - Pattern documentation
- `docs/interaction-patterns.md` - tap vs tapGameScreen guidance