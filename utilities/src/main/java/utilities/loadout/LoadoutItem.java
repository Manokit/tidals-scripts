package utilities.loadout;

import java.util.Objects;

/**
 * Represents a single item in a loadout slot.
 *
 * <p>This is an immutable data class that maps to the RuneLite Inventory Setups export format:
 * <ul>
 *   <li>id -> itemId</li>
 *   <li>q -> quantity (default 1 when null)</li>
 *   <li>f -> fuzzy (default false when null)</li>
 *   <li>sc -> not stored (RuneLite highlighting only, not needed for restocking)</li>
 * </ul>
 *
 * <p>Use the builder pattern for fluent construction:
 * <pre>
 * LoadoutItem item = LoadoutItem.builder()
 *     .itemId(995)
 *     .quantity(10000)
 *     .mode(QuantityMode.EXACT)
 *     .build();
 * </pre>
 */
public final class LoadoutItem {

    private final int itemId;
    private final String name;
    private final int quantity;
    private final QuantityMode mode;
    private final boolean fuzzy;

    /**
     * Creates a new LoadoutItem with all fields.
     *
     * @param itemId the OSMB/RuneLite item ID (required)
     * @param name human-readable name for display (can be null, resolved later)
     * @param quantity amount required (default 1)
     * @param mode how to interpret quantity (default EXACT)
     * @param fuzzy if true, match potion doses, jewelry charges, degraded variants (default false)
     */
    public LoadoutItem(int itemId, String name, int quantity, QuantityMode mode, boolean fuzzy) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.mode = mode != null ? mode : QuantityMode.EXACT;
        this.fuzzy = fuzzy;
    }

    /**
     * Creates a builder for fluent LoadoutItem construction.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the OSMB/RuneLite item ID.
     *
     * @return the item ID
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the human-readable name for display.
     *
     * @return the item name, or null if not yet resolved
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the quantity required.
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets how the quantity should be interpreted.
     *
     * @return the quantity mode
     */
    public QuantityMode getMode() {
        return mode;
    }

    /**
     * Returns whether fuzzy matching is enabled.
     * When true, matches potion doses, jewelry charges, and degraded variants.
     *
     * @return true if fuzzy matching is enabled
     */
    public boolean isFuzzy() {
        return fuzzy;
    }

    /**
     * Items are equal if they have the same item ID.
     * Quantity, mode, and fuzzy settings don't affect equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoadoutItem that = (LoadoutItem) o;
        return itemId == that.itemId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    /**
     * Returns a string representation: "itemId:quantity" or "itemId:quantity*" for unlimited.
     */
    @Override
    public String toString() {
        String suffix = mode == QuantityMode.UNLIMITED ? "*" : "";
        return itemId + ":" + quantity + suffix;
    }

    /**
     * Builder for fluent LoadoutItem construction.
     */
    public static class Builder {
        private int itemId;
        private String name;
        private int quantity = 1;
        private QuantityMode mode = QuantityMode.EXACT;
        private boolean fuzzy = false;

        private Builder() {
        }

        /**
         * Sets the item ID (required).
         *
         * @param itemId the OSMB/RuneLite item ID
         * @return this builder
         */
        public Builder itemId(int itemId) {
            this.itemId = itemId;
            return this;
        }

        /**
         * Sets the display name.
         *
         * @param name human-readable name
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the quantity (default 1).
         *
         * @param quantity amount required
         * @return this builder
         */
        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * Sets the quantity mode (default EXACT).
         *
         * @param mode how to interpret quantity
         * @return this builder
         */
        public Builder mode(QuantityMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets fuzzy matching (default false).
         *
         * @param fuzzy if true, match variants
         * @return this builder
         */
        public Builder fuzzy(boolean fuzzy) {
            this.fuzzy = fuzzy;
            return this;
        }

        /**
         * Builds the LoadoutItem.
         *
         * @return a new immutable LoadoutItem
         */
        public LoadoutItem build() {
            return new LoadoutItem(itemId, name, quantity, mode, fuzzy);
        }
    }
}
