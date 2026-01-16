# Testing Patterns

**Analysis Date:** 2026-01-16

## Test Framework

**Runner:**
- Not detected - no dedicated test framework configured

**Assertion Library:**
- Not detected

**Run Commands:**
```bash
# no test commands available
# testing is done manually via OSMB client
```

## Test File Organization

**Location:**
- No test files detected (`**/*.test.*`, `**/*.spec.*` returned no results)
- No `test/` or `tests/` directories present

**Naming:**
- Not applicable

**Structure:**
```
# no test directory structure
```

## Test Structure

**Suite Organization:**
- Not applicable - no automated tests

**Patterns:**
- Manual testing via OSMB client execution
- Visual verification of paint overlay output
- Log output analysis for debugging

## Mocking

**Framework:** Not applicable

**Patterns:**
- No mocking infrastructure

**What to Mock:**
- Not applicable

**What NOT to Mock:**
- Not applicable

## Fixtures and Factories

**Test Data:**
- Not applicable

**Location:**
- Not applicable

## Coverage

**Requirements:** None enforced

**View Coverage:**
```bash
# no coverage tooling
```

## Test Types

**Unit Tests:**
- None present

**Integration Tests:**
- None present
- Integration testing done by running full scripts in OSMB client

**E2E Tests:**
- Manual execution of full script workflows in OSMB client
- Verify via paint overlay stats and log output

## Common Patterns

**Async Testing:**
- Not applicable

**Error Testing:**
- Not applicable

## Manual Testing Approach

This codebase relies on manual testing through the OSMB client. The testing process involves:

**Build and Deploy:**
```bash
# build a specific script
osmb build TidalsGemCutter

# build all scripts
osmb build all
```

**Verification Methods:**

1. **Paint Overlay** - Scripts display real-time statistics via `onPaint()`:
   - Runtime, XP gained, items processed
   - Current task/state
   - Visual confirmation of script operation

2. **Log Output** - Scripts log state transitions and errors:
   ```java
   script.log(getClass(), "finding bank");
   script.log("THIEVE", "Starting to steal...");
   ```

3. **State Machine Verification** - Check task activation order:
   ```java
   for (Task task : tasks) {
       if (task.activate()) {
           task.execute();
           return 0;
       }
   }
   ```

4. **Requirement Checks** - Scripts verify requirements on startup:
   ```java
   if (!activeStrategy.verifyRequirements()) {
       log(getClass(), "requirements not met, stopping");
       stop();
       return 0;
   }
   ```

## Debugging Patterns

**Log-based Debugging:**
```java
// category-based logging for filtering
script.log("LOOT", "+" + gained + " " + type);
script.log("INVENTORY", "Initialized inventory snapshot");

// class-based logging for task identification
script.log(getClass(), "bank interact failed");
```

**State Inspection:**
```java
// log current position
WorldPosition pos = script.getWorldPosition();
script.log(getClass(), "current position: " + pos.getX() + ", " + pos.getY());

// log inventory state
ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of());
script.log(getClass(), "free slots: " + inv.getFreeSlots());
```

**Condition Verification:**
```java
// verify widget state
if (!script.getWidgetManager().getBank().isVisible()) {
    script.log(getClass(), "bank not visible");
    return false;
}
```

## Validation Patterns in Code

Scripts implement runtime validation as a form of testing:

**Setup Validation (`verifyRequirements`):**
```java
@Override
public boolean verifyRequirements() {
    // check bloom tool equipped
    UIResult<ItemSearchResult> bloomTool = script.getWidgetManager()
        .getEquipment().findItem(BLOOM_TOOLS);
    if (!bloomTool.isFound()) {
        script.log(getClass(), "ERROR: no bloom tool equipped");
        return false;
    }

    // check drakan's medallion
    UIResult<ItemSearchResult> medallion = script.getWidgetManager()
        .getEquipment().findItem(DRAKANS_MEDALLION);
    if (!medallion.isFound()) {
        script.log(getClass(), "ERROR: no drakan's medallion equipped");
        return false;
    }

    return true;
}
```

**Activation Guards:**
```java
@Override
public boolean activate() {
    ItemGroupResult inv = script.getWidgetManager()
        .getInventory().search(Set.of(selectedUncutGemID));
    if (inv == null) return false;
    return !inv.contains(selectedUncutGemID);
}
```

**Boundary Checks:**
```java
private boolean isPolygonTapSafe(Polygon poly) {
    if (poly == null || poly.numVertices() == 0) {
        return false;
    }

    int[] xs = poly.getXPoints();
    int[] ys = poly.getYPoints();

    for (int i = 0; i < xs.length; i++) {
        if (xs[i] < 0 || ys[i] < 0 ||
            xs[i] >= screenWidth || ys[i] >= screenHeight) {
            return false;
        }
    }
    return true;
}
```

## Recommendations for Future Testing

If automated testing were to be added:

**Unit Tests:**
- Test utility classes (`RetryUtils`, `BankingUtils`, etc.) with mocked Script
- Test loadout serialization/deserialization
- Test item ID mappings and constants

**Integration Tests:**
- Test task activation conditions with mock game state
- Test state machine transitions

**Tools to Consider:**
- JUnit 5 for unit testing
- Mockito for mocking OSMB API calls
- AssertJ for fluent assertions

---

*Testing analysis: 2026-01-16*
