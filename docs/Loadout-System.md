# Loadout System

The loadout system provides a complete solution for managing equipment and inventory configurations in PVM scripts. It handles visual editing, persistence, and bank restocking with quantity modes.

## Quick Start

```java
// in your script class
private LoadoutManager loadouts;

@Override
public void onStart() {
    loadouts = new LoadoutManager(this, "MyScriptName");
    loadouts.loadFromPreferences();  // restore last config
}

// in your ScriptUI setup
Button editButton = new Button("Edit Loadout");
editButton.setOnAction(e -> {
    loadouts.showEditor();
    loadouts.saveToPreferences();
});

// in your banking task
if (loadouts.needsRestock()) {
    BankingUtils.openBankAndWait(this, 15000);
    loadouts.restock();
}
```

## Core Components

### LoadoutManager

The main facade - use this in your scripts.

```java
LoadoutManager loadouts = new LoadoutManager(script, "ScriptName");

// persistence
loadouts.loadFromPreferences();   // load saved loadout
loadouts.saveToPreferences();     // save current loadout

// editor
loadouts.showEditor();            // open visual editor popup

// restocking (bank must be open)
loadouts.restock();               // full restock (equipment + inventory)
loadouts.restockInventory();      // inventory only (faster for subsequent restocks)

// state checking
loadouts.hasLoadout();            // true if any items defined
loadouts.needsRestock();          // true if anything missing (bank must be closed)
loadouts.needsInventoryRestock(); // true if inventory items missing (works with bank open)
loadouts.isEquipmentValid();      // true if all equipment is worn (bank must be closed)

// direct access
loadouts.getLoadout();            // get the Loadout object
loadouts.getRestocker();          // get LoadoutRestocker for advanced use
```

### Typical PVM Script Flow

```java
// === INITIAL SETUP (first bank trip) ===
public void handleInitialSetup() {
    if (!getWidgetManager().getBank().isVisible()) {
        BankingUtils.openBankAndWait(this, 15000);
    }

    // deposit everything for clean slate
    BankingUtils.depositAll(this);

    // full restock - withdraws and equips everything
    RestockResult result = loadouts.restock();

    if (!result.isSuccess()) {
        log("Failed to restock: " + result.getFailureReason());
        // handle error
    }

    // close bank and verify equipment
    BankingUtils.closeBankAndWait(this, 3000);

    if (!loadouts.isEquipmentValid()) {
        log("Equipment validation failed!");
    }
}

// === SUBSEQUENT RESTOCKS (after kills, etc.) ===
public void handleRestock() {
    // check if we need to restock before opening bank
    // (equipment check requires bank to be closed)
    boolean needsEquipment = !loadouts.isEquipmentValid();

    BankingUtils.openBankAndWait(this, 15000);

    if (needsEquipment) {
        // something happened to our gear, do full restock
        loadouts.restock();
    } else {
        // gear is fine, just restock inventory (much faster)
        loadouts.restockInventory();
    }

    BankingUtils.closeBankAndWait(this, 3000);
}
```

## Quantity Modes

Items can have different quantity modes:

| Mode | Badge | Behavior |
|------|-------|----------|
| EXACT | `5` | Withdraw exactly this amount |
| MINIMUM | `>5` | Must have at least this amount (hard requirement) |
| UNLIMITED | `*` | Withdraw entire stack from bank |

**Setting modes in the editor:**
- Right-click any item slot
- Select "Set to All (*)", "Set to Exact", or "Set to Minimum (>)"
- Use "Set Quantity..." to enter a custom amount (like 1234)

**Custom quantities (Withdraw-X):**
- Any amount not 1, 5, 10, or All uses Withdraw-X
- The system types the amount and presses Enter automatically

## RestockResult

The restock methods return a `RestockResult` with status info:

```java
RestockResult result = loadouts.restock();

result.isSuccess();        // true if all items restocked
result.isPartial();        // true if some items restocked
result.isFailed();         // true if restock failed
result.isNothingMissing(); // true if nothing needed restocking

result.getRestocked();     // list of items that were restocked
result.getUnfulfilled();   // list of items that couldn't be restocked
result.getTotalRestocked();// count of restocked items
result.getFailureReason(); // error message if failed
```

## ScriptUI Integration

Example setup UI with loadout editor:

```java
public class ScriptUI {
    private final Script script;
    private final LoadoutManager loadouts;

    public ScriptUI(Script script, LoadoutManager loadouts) {
        this.script = script;
        this.loadouts = loadouts;
    }

    public Scene buildScene(Script script) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // edit loadout button
        Button editButton = new Button("Edit Loadout");
        editButton.setOnAction(e -> {
            boolean saved = loadouts.showEditor();
            if (saved) {
                loadouts.saveToPreferences();
            }
        });

        // start button
        Button startButton = new Button("Start Script");
        startButton.setOnAction(e -> {
            if (!loadouts.hasLoadout()) {
                // show error - need to configure loadout first
                return;
            }
            loadouts.saveToPreferences();
            script.getStageController().close();
        });

        root.getChildren().addAll(editButton, startButton);
        return new Scene(root, 300, 200);
    }
}
```

## BankSearchUtils (Internal)

The loadout system uses `BankSearchUtils` internally for reliable withdrawals:

- Types item name in bank search box (not raw bank API)
- Uses sprite detection to find items visually
- Handles Withdraw-X for custom amounts
- Verifies inventory count after withdrawal
- Auto-retries on failure

**You don't need to use BankSearchUtils directly** - LoadoutManager handles everything.

## BankingUtils Helpers

```java
// deposit everything (worn + inventory)
BankingUtils.depositAll(script);

// deposit just worn items
BankingUtils.depositWornItems(script);

// open/close bank
BankingUtils.openBankAndWait(script, 15000);
BankingUtils.closeBankAndWait(script, 3000);
```

## Fuzzy Matching

For degradable items (barrows, crystal, etc.), enable fuzzy matching:

```java
LoadoutItem item = LoadoutItem.builder()
    .itemId(ItemID.AMULET_OF_FURY)
    .name("Amulet of fury")
    .quantity(1)
    .mode(QuantityMode.EXACT)
    .fuzzy(true)  // matches any variant of this item
    .build();
```

This uses `ItemResolver` to find all variants of the item.

## Item Filtering

The item search in the editor automatically filters out unbankable items:
- Last Man Standing items
- Animation items
- Broken/locked variants
- Placeholder items
- NPC-only items

## Build Dependency

Add to your script's `build.gradle`:

```gradle
dependencies {
    implementation files('../utilities/jar/TidalsUtilities.jar')
}
```

## Class Reference

| Class | Purpose |
|-------|---------|
| `LoadoutManager` | Main facade for scripts |
| `Loadout` | Data model for equipment + inventory |
| `LoadoutItem` | Single item with quantity and mode |
| `LoadoutEditor` | Visual editor popup |
| `LoadoutPersistence` | Save/load to preferences |
| `LoadoutRestocker` | Bank withdrawal logic |
| `LoadoutComparator` | Compare current state to loadout |
| `RestockResult` | Result of restock operation |
| `QuantityMode` | EXACT, MINIMUM, UNLIMITED |
| `MissingItem` | Details about a missing item |
