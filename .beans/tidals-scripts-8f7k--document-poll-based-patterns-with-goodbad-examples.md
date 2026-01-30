---
# tidals-scripts-8f7k
title: Document poll-based patterns with good/bad examples
status: completed
type: task
priority: normal
created_at: 2026-01-28T08:45:38Z
updated_at: 2026-01-28T09:37:29Z
parent: apzr
---

# Document Poll-Based Patterns

Update documentation with concrete examples of good and bad patterns.

## Deliverables

### docs/poll-based-architecture.md
- [x] Add "Good Example" section with DepositOres.java pattern
- [x] Add "Bad Example" section showing monolithic anti-pattern
- [x] Add state diagram showing check → handle → return flow

### docs/common-mistakes.md
- [x] Add "Monolithic Execute Methods" as a documented pitfall
- [x] Include before/after code snippets
- [x] Link to poll-based-architecture.md for full explanation

## Good Example to Document

```java
// state 1: dismiss dialogue if present
if (hasDialogue()) {
    dismissDialogue();
    return true;  // re-poll
}

// state 2: interface open? handle it
if (isInterfaceOpen()) {
    handleInterface();
    return true;  // re-poll
}

// state 3: need to walk? walk
if (!atDestination()) {
    walkTo(destination);
    return true;  // re-poll
}
```
