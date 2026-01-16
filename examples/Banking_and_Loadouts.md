# Banking and Loadouts - Code Examples

Practical code snippets for using the loadout system in PVM scripts.

## Setup

```gradle
// build.gradle
dependencies {
    implementation files('../utilities/jar/TidalsUtilities.jar')
}
```

```java
// imports
import utilities.BankingUtils;
import utilities.BankSearchUtils;
import utilities.loadout.LoadoutManager;
import utilities.loadout.LoadoutItem;
import utilities.loadout.RestockResult;
import utilities.loadout.QuantityMode;
```

---

## LoadoutManager - Basic Usage

```java
public class MyScript extends Script {
    private LoadoutManager loadouts;

    @Override
    public void onStart() {
        // create manager with script name (used for persistence key)
        loadouts = new LoadoutManager(this, "MyScriptName");

        // load saved loadout from preferences
        if (loadouts.loadFromPreferences()) {
            log("Loaded saved loadout");
        }
    }
}
```

---

## ScriptUI - Edit Loadout Button

```java
public Scene buildScene(Script script) {
    VBox root = new VBox(10);
    root.setPadding(new Insets(15));

    Button editButton = new Button("Edit Loadout");
    editButton.setOnAction(e -> {
        boolean saved = loadouts.showEditor();
        if (saved) {
            loadouts.saveToPreferences();
            log("Loadout saved");
        }
    });

    Button startButton = new Button("Start");
    startButton.setOnAction(e -> {
        if (!loadouts.hasLoadout()) {
            log("Configure loadout first!");
            return;
        }
        loadouts.saveToPreferences();
        script.getStageController().close();
    });

    root.getChildren().addAll(editButton, startButton);
    return new Scene(root, 300, 150);
}
```

---

## Initial Setup Task (First Bank Trip)

```java
public class SetupTask implements Task {
    private final Script script;
    private final LoadoutManager loadouts;

    @Override
    public int execute() {
        // open bank
        if (!script.getWidgetManager().getBank().isVisible()) {
            if (!BankingUtils.openBankAndWait(script, 15000)) {
                script.log("Failed to open bank");
                return 1000;
            }
        }

        // deposit everything for clean slate
        BankingUtils.depositAll(script);

        // full restock - withdraws equipment and inventory, equips gear
        RestockResult result = loadouts.restock();

        if (!result.isSuccess()) {
            script.log("Restock failed: " + result.getFailureReason());
            return 1000;
        }

        // close bank to verify equipment
        BankingUtils.closeBankAndWait(script, 3000);

        // verify equipment is worn correctly
        if (!loadouts.isEquipmentValid()) {
            script.log("Equipment validation failed!");
            return 1000;
        }

        script.log("Setup complete");
        return 0;
    }
}
```

---

## Restock Task (After Kills)

```java
public class RestockTask implements Task {
    private final Script script;
    private final LoadoutManager loadouts;

    @Override
    public boolean shouldExecute() {
        // check if inventory needs restocking (works with bank closed)
        return loadouts.needsInventoryRestock();
    }

    @Override
    public int execute() {
        // check equipment BEFORE opening bank (can't check with bank open)
        boolean needsEquipmentFix = !loadouts.isEquipmentValid();

        // open bank
        if (!script.getWidgetManager().getBank().isVisible()) {
            if (!BankingUtils.openBankAndWait(script, 15000)) {
                return 1000;
            }
        }

        RestockResult result;
        if (needsEquipmentFix) {
            // something wrong with gear, do full restock
            script.log("Equipment issue detected, full restock");
            result = loadouts.restock();
        } else {
            // gear is fine, just restock inventory (much faster)
            result = loadouts.restockInventory();
        }

        if (!result.isSuccess() && !result.isPartial()) {
            script.log("Restock failed: " + result.getFailureReason());
        }

        BankingUtils.closeBankAndWait(script, 3000);
        return 0;
    }
}
```

---

## BankingUtils - Common Operations

```java
// open nearest bank and wait
BankingUtils.openBankAndWait(script, 15000);

// open specific bank object
RSObject bank = BankingUtils.findNearestBank(script);
BankingUtils.openBankAndWait(script, bank, 15000);

// close bank
BankingUtils.closeBankAndWait(script, 3000);

// deposit all worn equipment and inventory
BankingUtils.depositAll(script);

// deposit just worn equipment
BankingUtils.depositWornItems(script);

// deposit inventory except certain items
BankingUtils.depositAllExcept(script, Set.of(ItemID.RUNE_POUCH));
```

---

## BankSearchUtils - Direct Withdrawals

Use this when you need to withdraw items outside of the loadout system.

```java
// CRITICAL: always use BankSearchUtils, not raw bank.withdraw()
// raw API only sees visible items, doesn't use search box

// withdraw specific amount
BankSearchUtils.searchAndWithdrawVerified(script, ItemID.SHARK, 10, true);

// withdraw all of a stack
BankSearchUtils.searchAndWithdrawVerified(script, ItemID.DEATH_RUNE, 0, true);  // 0 = all

// IMPORTANT: reset search between withdrawals
BankSearchUtils.clickSearchToReset(script);

// clear search when done
BankSearchUtils.clearSearch(script);
```

### Multi-Item Withdrawal Pattern

```java
int[] itemsToWithdraw = {ItemID.SHARK, ItemID.PRAYER_POTION4, ItemID.SUPER_COMBAT_POTION4};
int[] amounts = {10, 4, 1};

for (int i = 0; i < itemsToWithdraw.length; i++) {
    boolean success = BankSearchUtils.searchAndWithdrawVerified(
        script,
        itemsToWithdraw[i],
        amounts[i],
        true  // keepSearchOpen
    );

    if (success) {
        // MUST reset search after each successful withdrawal
        BankSearchUtils.clickSearchToReset(script);
    }
}

// clear search when done with all withdrawals
BankSearchUtils.clearSearch(script);
```

---

## RestockResult Handling

```java
RestockResult result = loadouts.restock();

if (result.isSuccess()) {
    log("All items restocked");

} else if (result.isPartial()) {
    log("Partial restock - missing: " + result.getUnfulfilled().size() + " items");
    // might be okay to continue with what we got

} else if (result.isFailed()) {
    log("Restock failed: " + result.getFailureReason());
    // stop script or try again

} else if (result.isNothingMissing()) {
    log("Nothing to restock");
}

// check what was actually restocked
for (MissingItem item : result.getRestocked()) {
    log("Restocked: " + item.getLoadoutItem().getName());
}
```

---

## Quantity Modes

```java
// EXACT - withdraw exactly this amount
LoadoutItem sharks = LoadoutItem.builder()
    .itemId(ItemID.SHARK)
    .name("Shark")
    .quantity(10)
    .mode(QuantityMode.EXACT)
    .build();

// MINIMUM - must have at least this (hard requirement, fails if unmet)
LoadoutItem prayer = LoadoutItem.builder()
    .itemId(ItemID.PRAYER_POTION4)
    .name("Prayer potion(4)")
    .quantity(4)
    .mode(QuantityMode.MINIMUM)
    .build();

// UNLIMITED - withdraw entire stack
LoadoutItem runes = LoadoutItem.builder()
    .itemId(ItemID.DEATH_RUNE)
    .name("Death rune")
    .quantity(1)  // ignored for UNLIMITED
    .mode(QuantityMode.UNLIMITED)
    .build();
```

In the editor UI:
- Right-click item -> "Set Quantity..." for custom amounts (1234, etc.)
- Right-click -> "Set to All (*)" for UNLIMITED
- Right-click -> "Set to Minimum (>)" for MINIMUM
- Right-click -> "Set to Exact" for EXACT

---

## State Checking Methods

```java
// check if loadout has any items configured
if (!loadouts.hasLoadout()) {
    log("No loadout configured!");
}

// check if anything is missing (BANK MUST BE CLOSED)
if (loadouts.needsRestock()) {
    // need to bank
}

// check only inventory (works with bank open or closed)
if (loadouts.needsInventoryRestock()) {
    // need to restock inventory
}

// check if equipment is correct (BANK MUST BE CLOSED)
if (!loadouts.isEquipmentValid()) {
    // equipment is wrong, need full restock
}
```

---

## Complete PVM Script Example

```java
@ScriptDefinition(name = "MyBossKiller", ...)
public class MyBossKiller extends Script {
    private LoadoutManager loadouts;
    private boolean initialSetupDone = false;

    @Override
    public void onStart() {
        loadouts = new LoadoutManager(this, "MyBossKiller");
        loadouts.loadFromPreferences();

        // show setup UI
        ScriptUI ui = new ScriptUI(this, loadouts);
        getStageController().show(ui.buildScene(this), "Setup", false);
    }

    @Override
    public int poll() {
        // initial setup
        if (!initialSetupDone) {
            return doInitialSetup();
        }

        // check if we need to restock
        if (loadouts.needsInventoryRestock()) {
            return doRestock();
        }

        // main activity
        return doActivity();
    }

    private int doInitialSetup() {
        BankingUtils.openBankAndWait(this, 15000);
        BankingUtils.depositAll(this);

        RestockResult result = loadouts.restock();
        if (!result.isSuccess()) {
            log("Setup failed: " + result.getFailureReason());
            return 5000;
        }

        BankingUtils.closeBankAndWait(this, 3000);

        if (!loadouts.isEquipmentValid()) {
            log("Equipment check failed");
            return 5000;
        }

        initialSetupDone = true;
        return 0;
    }

    private int doRestock() {
        boolean needsGearFix = !loadouts.isEquipmentValid();

        BankingUtils.openBankAndWait(this, 15000);

        if (needsGearFix) {
            loadouts.restock();
        } else {
            loadouts.restockInventory();
        }

        BankingUtils.closeBankAndWait(this, 3000);
        return 0;
    }

    private int doActivity() {
        // kill boss, etc.
        return 600;
    }
}
```
