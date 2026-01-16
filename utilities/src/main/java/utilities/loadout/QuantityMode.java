package utilities.loadout;

/**
 * Defines how the quantity field should be interpreted during restock operations.
 *
 * <ul>
 *   <li>{@link #EXACT} - restock to exactly N items (e.g., 10k coins for ship fee)</li>
 *   <li>{@link #UNLIMITED} - take entire stack from bank (runes, bolts, arrows)</li>
 *   <li>{@link #MINIMUM} - must have at least N, fail restock check if not (e.g., >50 ruby bolts)</li>
 * </ul>
 */
public enum QuantityMode {

    /**
     * Restock to exactly N items.
     * Use for items where you need a specific count (e.g., 10k coins for a fee).
     */
    EXACT(""),

    /**
     * Take the entire stack from bank.
     * Use for consumables like runes, bolts, or arrows where you want all available.
     * Represented as "*" in UI.
     */
    UNLIMITED("*"),

    /**
     * Must have at least N items, fail restock check if bank has fewer.
     * Use when a minimum threshold is required (e.g., >50 ruby bolts).
     * Represented as ">" in UI.
     */
    MINIMUM(">");

    private final String symbol;

    QuantityMode(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Gets the UI symbol representation for this mode.
     *
     * @return the symbol: "" for EXACT, "*" for UNLIMITED, ">" for MINIMUM
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Parses a UI symbol to its corresponding QuantityMode.
     *
     * @param symbol the symbol to parse ("", "*", or ">")
     * @return the matching QuantityMode
     * @throws IllegalArgumentException if the symbol is not recognized
     */
    public static QuantityMode fromSymbol(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            return EXACT;
        }
        for (QuantityMode mode : values()) {
            if (mode.symbol.equals(symbol)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown quantity mode symbol: " + symbol);
    }
}
