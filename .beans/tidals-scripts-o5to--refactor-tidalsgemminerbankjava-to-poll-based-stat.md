---
# tidals-scripts-o5to
title: Refactor TidalsGemMiner/Bank.java to poll-based states
status: completed
type: feature
priority: normal
created_at: 2026-01-28T08:45:34Z
updated_at: 2026-01-28T09:25:25Z
parent: apzr
---

# Refactor GemMiner Bank.java to Poll-Based States

Review and refactor banking logic to clean state machine pattern.

## Checklist

- [x] Read current Bank.java
- [x] Identify if refactoring needed (may already be clean)
- [x] If needed: separate walk/open/deposit/withdraw/close states → **NOT NEEDED**
- [x] Test banking flow → **N/A - no changes made**

## Analysis Results

**Conclusion: No refactoring needed.** The current Bank.java already follows the poll-based pattern correctly.

### Current Structure (Already Clean)

The `execute()` method properly checks state at each step:

1. **If deposit box visible** → calls `handleDepositing()` → returns
2. **If deposit object NOT in scene** → walks to bank → `return false` ✅
3. **If deposit object IS in scene** → opens deposit box → `return false` ✅

This matches the pattern used in TidalsGemCutter Bank.java (the reference implementation).

### Why `handleDepositing()` is Acceptable

The method bundles deposit + close operations, but this is the same pattern used in TidalsGemCutter:
- Once a banking UI is open, completing the transaction in one execute() is acceptable
- Breaking it into multiple polls could cause the UI to timeout/close
- The key anti-pattern (linear execution *before* opening UI) is avoided

### Key Patterns Present

- ✅ State check at top of `execute()`
- ✅ Returns `false` after walking action
- ✅ Returns `false` after opening action
- ✅ Proper use of `pollFramesUntil(() -> false, ...)` for fixed delays
- ✅ Proper null checking throughout
- ✅ Uses `RandomUtils.weightedRandom()` instead of `script.random()`