---
# tidals-scripts-3f9f
title: Update CLAUDE.md with poll-based patterns
status: completed
type: task
priority: normal
created_at: 2026-01-28T07:09:59Z
updated_at: 2026-01-28T07:50:13Z
parent: tidals-scripts-8cpu
---

# Update CLAUDE.md for Future Scripts

Ensure CLAUDE.md instructs future development to follow these patterns.

## Checklist

- [x] Add poll-based architecture to Core Principles section
- [x] Add tapGameScreen vs tap distinction to Critical Concepts
- [x] Add visibility checking requirements
- [x] Reference new documentation files
- [x] Add timeout randomization requirement

## Key Additions

### Core Principles Addition
```
POLL-BASED: One action per poll. Check condition, handle it, return. Don't chain actions.
```

### Critical Concepts Additions
```
7. **Use tapGameScreen() for 3D objects** - tap() can click through UI overlays
8. **Visibility check required** - getConvexHull() != null does NOT mean visible
9. **Randomize ALL timeouts** - No static delay values, re-randomize each use
10. **pollFramesUntil with true exits immediately** - Return false for delays
```

### Documentation Index Updates
Add references to:
- `docs/poll-based-architecture.md`
- `docs/interaction-patterns.md`