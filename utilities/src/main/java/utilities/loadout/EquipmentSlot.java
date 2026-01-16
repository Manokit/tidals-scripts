package utilities.loadout;

/**
 * Defines the 11 equipment slots in OSRS with their grid positions.
 *
 * <p>Layout matches the OSRS Worn Equipment tab:
 * <pre>
 *            [Head]
 *   [Cape]  [Amulet]  [Ammo]
 *   [Weapon] [Body]  [Shield]
 *            [Legs]
 *   [Gloves] [Boots]  [Ring]
 * </pre>
 *
 * <p>Grid uses a 3-column layout where columns are:
 * 0 = left, 1 = center, 2 = right
 */
public enum EquipmentSlot {

    HEAD(0, "Head", "head.png", 1, 0),
    CAPE(1, "Cape", "cape.png", 0, 1),
    AMULET(2, "Amulet", "amulet.png", 1, 1),
    AMMO(3, "Ammo", "ammo.png", 2, 1),
    WEAPON(4, "Weapon", "weapon.png", 0, 2),
    BODY(5, "Body", "body.png", 1, 2),
    SHIELD(6, "Shield", "shield.png", 2, 2),
    LEGS(7, "Legs", "legs.png", 1, 3),
    GLOVES(8, "Gloves", "gloves.png", 0, 4),
    BOOTS(9, "Boots", "boots.png", 1, 4),
    RING(10, "Ring", "ring.png", 2, 4);

    private final int index;
    private final String displayName;
    private final String placeholderImage;
    private final int gridColumn;
    private final int gridRow;

    EquipmentSlot(int index, String displayName, String placeholderImage, int gridColumn, int gridRow) {
        this.index = index;
        this.displayName = displayName;
        this.placeholderImage = placeholderImage;
        this.gridColumn = gridColumn;
        this.gridRow = gridRow;
    }

    /**
     * Gets the slot index (0-10) matching Loadout array indices.
     *
     * @return the slot index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the display name for this slot.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the placeholder image filename.
     *
     * @return the image filename (e.g., "head.png")
     */
    public String getPlaceholderImage() {
        return placeholderImage;
    }

    /**
     * Gets the grid column (0=left, 1=center, 2=right).
     *
     * @return the column index
     */
    public int getGridColumn() {
        return gridColumn;
    }

    /**
     * Gets the grid row (0-5 from top to bottom).
     *
     * @return the row index
     */
    public int getGridRow() {
        return gridRow;
    }

    /**
     * Gets an EquipmentSlot by its index.
     *
     * @param index the slot index (0-10)
     * @return the EquipmentSlot, or null if invalid index
     */
    public static EquipmentSlot fromIndex(int index) {
        for (EquipmentSlot slot : values()) {
            if (slot.index == index) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Total number of equipment slots.
     */
    public static final int COUNT = 11;
}
