package utilities.loadout;

/**
 * Represents an item that is missing or needs restocking.
 *
 * <p>Tracks the loadout requirement alongside current state to enable
 * accurate deficit calculation and restock decisions.
 */
public final class MissingItem {

    /**
     * Identifies which container an item belongs to.
     */
    public enum ContainerType {
        EQUIPMENT,
        INVENTORY,
        RUNE_POUCH,
        BOLT_POUCH,
        QUIVER
    }

    private final LoadoutItem loadoutItem;
    private final int currentQuantity;
    private final int neededQuantity;
    private final ContainerType container;

    /**
     * Creates a new MissingItem.
     *
     * @param loadoutItem     the loadout specification for this item
     * @param currentQuantity how many the player currently has
     * @param neededQuantity  how many are required
     * @param container       which container this item belongs to
     */
    public MissingItem(LoadoutItem loadoutItem, int currentQuantity, int neededQuantity, ContainerType container) {
        this.loadoutItem = loadoutItem;
        this.currentQuantity = currentQuantity;
        this.neededQuantity = neededQuantity;
        this.container = container;
    }

    /**
     * Gets the loadout item specification.
     *
     * @return the loadout item
     */
    public LoadoutItem getLoadoutItem() {
        return loadoutItem;
    }

    /**
     * Gets the current quantity the player has.
     *
     * @return current quantity
     */
    public int getCurrentQuantity() {
        return currentQuantity;
    }

    /**
     * Gets the quantity required by the loadout.
     *
     * @return needed quantity
     */
    public int getNeededQuantity() {
        return neededQuantity;
    }

    /**
     * Gets which container this item belongs to.
     *
     * @return the container type
     */
    public ContainerType getContainer() {
        return container;
    }

    /**
     * Calculates the deficit (how many more are needed).
     *
     * @return neededQuantity - currentQuantity (minimum 0)
     */
    public int getDeficit() {
        return Math.max(0, neededQuantity - currentQuantity);
    }

    /**
     * Returns a string representation for logging.
     */
    @Override
    public String toString() {
        String itemName = loadoutItem.getName() != null ? loadoutItem.getName() : "item#" + loadoutItem.getItemId();
        return String.format("MissingItem{%s in %s: have %d, need %d (deficit: %d)}",
                itemName, container, currentQuantity, neededQuantity, getDeficit());
    }
}
