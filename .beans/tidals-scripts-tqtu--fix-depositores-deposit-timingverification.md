---
# tidals-scripts-tqtu
title: Fix DepositOres deposit timing/verification
status: completed
type: bug
priority: high
created_at: 2026-01-28T07:54:19Z
updated_at: 2026-01-28T07:59:50Z
---

## Problem
OSMB reports: 'opens the deposit box but then closes it without actually hitting the deposit button'

## Root Cause Analysis
Looking at execute() state 2:
```java
if (isDepositInterfaceOpen()) {
    if (!isInventoryEmpty()) {
        if (!depositAll()) { ... }
        script.pollFramesUntil(() -> isInventoryEmpty(), 3000);
        script.pollFramesUntil(() -> false, ...);
    }
    closeDepositInterface();  // <-- closes regardless of success!
}
```

Two issues:
1. **No delay before interaction** - when interface opens, we call depositAll() immediately. Interface may be visible but not interactive yet.
2. **No verification before closing** - we close the interface without checking if inventory actually emptied

## Checklist
- [x] Add small delay after detecting interface is open before calling depositAll()
- [x] Verify inventory is actually empty before calling closeDepositInterface()
- [x] If inventory still has items after timeout, log warning and retry (don't close)

## Also Clean Up (per OSMB feedback)
- [x] Simplify null checks: Dialogue is never null, just check `.isVisible()`
- [x] Simplify UIResult checks: never null, just check `.isFound()`

## Reference Documentation
These patterns are now documented:
- `docs/common-mistakes.md` - Sections 19 (null guarantees), 20 (interface timing)
- `docs/interaction-patterns.md` - "Interface Operation Verification" section