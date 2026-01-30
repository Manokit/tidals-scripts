---
# tidals-scripts-te2i
title: Create poll-based architecture documentation
status: completed
type: task
priority: normal
created_at: 2026-01-28T07:09:56Z
updated_at: 2026-01-28T07:44:55Z
parent: tidals-scripts-8cpu
blocking:
    - tidals-scripts-3f9f
---

# Create Poll-Based Architecture Documentation

Create new documentation files that establish the poll-based paradigm.

## Checklist

- [x] Create `docs/poll-based-architecture.md` - Core polling philosophy and patterns
- [x] Create `docs/interaction-patterns.md` - tap vs tapGameScreen, visibility checks, MovementChecker

## docs/poll-based-architecture.md Content

Should cover:
- The "one action per poll" principle
- Why linear execution is problematic
- State machine pattern for script flow
- Before/after examples
- When to return true vs false from execute()

## docs/interaction-patterns.md Content

Should cover:
- `tap()` vs `tapGameScreen()` - when to use each
- Visibility checking with `insideGameScreenFactor()`
- The convex hull visibility trap (non-null != visible)
- MovementChecker pattern for timeout handling
- Recommended visibility thresholds