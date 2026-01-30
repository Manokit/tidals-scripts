---
# tidals-scripts-apzr
title: 'De-monolith Production Scripts: Poll-Based Refactoring'
status: completed
type: epic
priority: normal
created_at: 2026-01-28T08:40:31Z
updated_at: 2026-01-28T09:39:32Z
---

# De-monolith Production Scripts: Poll-Based Refactoring

Following OSMB's feedback on proper poll-based architecture, refactor all production scripts to use clean **check → handle → return** patterns instead of linear execution.

## Problem Statement

Current scripts have "do everything" execute() methods that perform multiple operations in a single poll:
- Walk to location
- Open interface
- Perform action
- Handle result
- Walk back

This is a **linear mindset** that blocks the framework from re-evaluating state.

## Target Pattern

From OSMB's feedback:

```java
if (dialogueVisible) {
    handleDialogue();
    return true;  // re-poll
}

if (depositInterfaceOpen) {
    handleDeposit();
    return true;  // re-poll
}

// etc.
```

Each poll should:
1. Check current state
2. Handle ONE condition
3. Return to let framework re-evaluate

## Good Example: DepositOres.java (refactored)

```java
// state 1: dismiss dialogue if present
if (hasInventoryFullDialogue()) {
    script.log("DEPOSIT", "Dismissing dialogue...");
    script.getWidgetManager().getDialogue().continueChatDialogue();
    script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(300, 1000));
    return true;  // re-poll to check next state
}

// state 2a: deposit interface open, has items
if (isDepositInterfaceOpen() && !isDepositBoxEmpty()) {
    depositAll();
    script.pollFramesHuman(() -> isDepositBoxEmpty(), 3000);
    return true;
}

// state 2b: deposit interface open, items deposited
if (isDepositInterfaceOpen() && isDepositBoxEmpty()) {
    closeDepositInterface();
    return true;
}

// state 3: walk to deposit box
if (!isNearDepositBox()) {
    script.getWalker().walkTo(DEPOSIT_BOX_TILE, config);
    return true;
}

// state 4: open deposit box
if (!isInventoryEmpty()) {
    openDepositBoxWithMenu();
    script.pollFramesHuman(() -> isDepositInterfaceOpen(), 5000);
    return true;
}

// state 5: done
finishDepositRun();
return true;
```

## Scripts to Refactor

### Highest Priority (Complex execute methods)
- [ ] **TidalsGemMiner/Mine.java** - 350+ line execute(), does targeting, movement, mining, GP tracking in one method
- [ ] **TidalsCannonballThiever/StartThieving.java** - Multiple operations in linear sequence
- [ ] **TidalsCannonballThiever/EscapeJail.java** - Likely has chained operations

### Medium Priority
- [ ] **TidalsGemMiner/Bank.java** - Banking tasks are often monolithic
- [ ] **TidalsGemMiner/Cut.java** - Processing tasks
- [ ] **TidalsGoldSuperheater/Process.java** - Processing logic
- [ ] **TidalsGoldSuperheater/Bank.java** - Banking

### Review Only (Already clean per audit)
- [ ] TidalsChompyHunter - Verify still follows patterns
- [ ] TidalsGemCutter - Verify still follows patterns

## Documentation Deliverables

- [ ] Add "Good Example" section to `docs/poll-based-architecture.md` with DepositOres pattern
- [ ] Add anti-pattern examples showing what NOT to do
- [ ] Update `docs/common-mistakes.md` with monolithic execute warning

## Success Criteria

- Each execute() method reads as a series of state checks with early returns
- No sequential operations without state re-evaluation between them
- Long methods broken into helper methods or separate tasks
- Framework can interrupt at any point between operations

## OSMB Tips

### Combine wait + humanization
Instead of separate calls:
```java
// DON'T do this:
script.pollFramesUntil(() -> isDepositBoxEmpty(), 3000);
script.pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 500));

// DO this instead:
script.pollFramesHuman(() -> isDepositBoxEmpty(), 3000);
```

### No retry loops in execute()
Let the framework handle retries by returning and re-polling. Don't loop internally.

## Reference

OSMB feedback:
> "The goal of a clean poll-based script isn't to finish a whole set of tasks in one call... you want the approach of: check for a condition, handle it and re-poll"

> "I'd say you're nearly there, but I think first and foremost is getting you to understand how to properly structure a poll based script."