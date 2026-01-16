package utilities.loadout;

import com.osmb.api.script.Script;
import javafx.scene.control.Button;
import utilities.items.ItemResolver;
import utilities.loadout.ui.LoadoutEditor;

import java.util.List;

/**
 * Facade for script-level loadout management.
 *
 * <p>Provides a unified API that ties together:
 * <ul>
 *   <li>LoadoutEditor - visual editing popup</li>
 *   <li>LoadoutPersistence - cross-session storage</li>
 *   <li>LoadoutRestocker - bank withdrawal with quantity modes</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // in ScriptUI or setup
 * LoadoutManager loadouts = new LoadoutManager(script, "MyScript");
 * loadouts.loadFromPreferences();  // restore last config
 * loadouts.showEditor();           // let user edit
 * loadouts.saveToPreferences();    // remember for next time
 *
 * // in banking task
 * RestockResult result = loadouts.restock();
 * </pre>
 */
public final class LoadoutManager {

    private final Script script;
    private final String scriptName;
    private Loadout loadout;

    // lazy-initialized components
    private ItemResolver itemResolver;
    private LoadoutRestocker restocker;

    /**
     * Creates a new LoadoutManager bound to a script instance.
     *
     * @param script     the script instance for API access
     * @param scriptName the script name (used for persistence key)
     */
    public LoadoutManager(Script script, String scriptName) {
        this.script = script;
        this.scriptName = scriptName;
        this.loadout = new Loadout(scriptName);
    }

    /**
     * Gets the current loadout (never null).
     *
     * @return the current loadout
     */
    public Loadout getLoadout() {
        return loadout;
    }

    /**
     * Sets the current loadout.
     *
     * @param loadout the loadout to set (must not be null)
     */
    public void setLoadout(Loadout loadout) {
        if (loadout == null) {
            throw new IllegalArgumentException("loadout must not be null");
        }
        this.loadout = loadout;
    }

    /**
     * Opens the LoadoutEditor popup for visual editing.
     *
     * @return true if the user saved changes, false if cancelled
     */
    public boolean showEditor() {
        return LoadoutEditor.show(script, loadout);
    }

    /**
     * Saves the current loadout as this script's default.
     *
     * @return true if saved successfully, false on error
     */
    public boolean saveToPreferences() {
        return LoadoutPersistence.saveDefault(scriptName, loadout);
    }

    /**
     * Loads this script's default loadout from preferences.
     *
     * @return true if a default was found and loaded, false if none exists or on error
     */
    public boolean loadFromPreferences() {
        Loadout loaded = LoadoutPersistence.loadDefault(scriptName);
        if (loaded != null) {
            this.loadout = loaded;
            return true;
        }
        return false;
    }

    /**
     * Full restock including equipment.
     *
     * <p>Use this for initial setup when first gearing up.
     * Requires the bank to be open before calling.
     *
     * @return result indicating success, partial, or failure
     */
    public RestockResult restock() {
        return getRestocker().restock(loadout);
    }

    /**
     * Restocks only inventory items (skips equipment).
     *
     * <p>Use this for subsequent restocks after equipment has been validated.
     * Much faster since it doesn't try to search for equipped items.
     * Requires the bank to be open before calling.
     *
     * <p>Typical flow:
     * <pre>
     * // initial setup
     * loadouts.restock();
     * if (!loadouts.isEquipmentValid()) { error... }
     *
     * // subsequent restocks (after kills, etc.)
     * loadouts.restockInventory();
     * </pre>
     *
     * @return result indicating success, partial, or failure
     */
    public RestockResult restockInventory() {
        return getRestocker().restockInventory(loadout);
    }

    /**
     * Gets the ItemResolver instance (lazy-initialized).
     *
     * <p>Exposed for scripts that need direct item lookups.
     *
     * @return the ItemResolver
     */
    public ItemResolver getItemResolver() {
        if (itemResolver == null) {
            itemResolver = new ItemResolver(script);
        }
        return itemResolver;
    }

    /**
     * Gets the LoadoutRestocker instance (lazy-initialized).
     *
     * <p>Exposed for advanced use cases.
     *
     * @return the LoadoutRestocker
     */
    public LoadoutRestocker getRestocker() {
        if (restocker == null) {
            restocker = new LoadoutRestocker(script, getItemResolver());
        }
        return restocker;
    }

    /**
     * Gets the script name used for persistence.
     *
     * @return the script name
     */
    public String getScriptName() {
        return scriptName;
    }

    // === helper methods ===

    /**
     * Returns true if the loadout has any items defined.
     *
     * @return true if at least one equipment or inventory slot is set
     */
    public boolean hasLoadout() {
        return !loadout.getAllItems().isEmpty();
    }

    /**
     * Compares current state to the loadout and returns true if anything is missing.
     *
     * <p>Checks both equipment and inventory. Requires bank to be closed
     * for accurate equipment checking.
     *
     * @return true if any items are missing from the loadout
     */
    public boolean needsRestock() {
        LoadoutComparator comparator = new LoadoutComparator(getItemResolver());
        List<MissingItem> missing = comparator.compare(script, loadout);
        return !missing.isEmpty();
    }

    /**
     * Checks if only inventory needs restocking (equipment assumed valid).
     *
     * <p>Works while bank is open since it only checks inventory.
     * Use this for subsequent restock decisions after equipment is validated.
     *
     * @return true if any inventory items are missing
     */
    public boolean needsInventoryRestock() {
        LoadoutComparator comparator = new LoadoutComparator(getItemResolver());
        List<MissingItem> missing = comparator.compareInventoryOnly(script, loadout);
        return !missing.isEmpty();
    }

    /**
     * Validates that all required equipment is currently worn.
     *
     * <p>Call this before opening the bank to verify gear is correct.
     * Requires equipment tab to be accessible (bank must be closed).
     *
     * <p>Typical flow:
     * <pre>
     * // before opening bank for restock
     * if (!loadouts.isEquipmentValid()) {
     *     // do full restock to fix equipment
     *     openBank();
     *     loadouts.restock();
     * } else {
     *     // just restock inventory
     *     openBank();
     *     loadouts.restockInventory();
     * }
     * </pre>
     *
     * @return true if all required equipment is currently worn
     */
    public boolean isEquipmentValid() {
        LoadoutComparator comparator = new LoadoutComparator(getItemResolver());
        List<MissingItem> missing = comparator.compare(script, loadout);

        // check if any missing items are equipment
        for (MissingItem item : missing) {
            if (item.getContainer() == MissingItem.ContainerType.EQUIPMENT) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a JavaFX Button that calls restock when clicked.
     *
     * <p>Convenience method for ScriptUI integration. The button displays
     * the provided label and triggers restock on click.
     *
     * <p>Note: restock() requires the bank to be open, so this button
     * is typically used in a banking context.
     *
     * @param label the button label text
     * @return a styled Button that triggers restock
     */
    public Button createRestockButton(String label) {
        Button button = new Button(label);
        button.setOnAction(e -> restock());
        return button;
    }
}
