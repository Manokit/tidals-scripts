package utilities.loadout;

/**
 * Exports loadouts to RuneLite-compatible JSON format.
 *
 * <p>Our format extends RuneLite Inventory Setups format with:
 * <ul>
 *   <li>{@code mode} field on items ("exact", "unlimited", "minimum") - our addition for quantity modes</li>
 * </ul>
 *
 * <p>Equipment is exported in RuneLite's 14-slot order for compatibility:
 * <pre>
 * Our Index -> RuneLite Index
 * 0  Head   -> 0  Head
 * 1  Cape   -> 1  Cape
 * 2  Amulet -> 2  Amulet
 * 3  Ammo   -> 10 Ammo
 * 4  Weapon -> 3  Weapon
 * 5  Body   -> 4  Body
 * 6  Shield -> 5  Shield
 * 7  Legs   -> 6  Legs
 * 8  Gloves -> 7  Gloves
 * 9  Boots  -> 8  Boots
 * 10 Ring   -> 9  Ring
 * (11-13 are null filler)
 * </pre>
 *
 * <p>Export structure:
 * <pre>
 * {
 *   "setup": {
 *     "name": "loadout name",
 *     "inv": [{item}, null, ...],  // 28 slots
 *     "eq": [{item}, null, ...],   // 14 slots (RuneLite format)
 *     "rp": [{item}, ...] or null, // rune pouch
 *     "bp": [{item}, ...] or null, // bolt pouch
 *     "qv": [{item}] or null       // quiver
 *   }
 * }
 *
 * Item structure:
 * {
 *   "id": 995,
 *   "q": 10000,        // omit if 1
 *   "f": true,         // omit if false
 *   "mode": "exact"    // omit if "exact" (our extension)
 * }
 * </pre>
 *
 * @see LoadoutImporter for importing loadouts
 */
public class LoadoutExporter {

    /**
     * Maps our Loadout equipment indices to RuneLite equipment indices.
     *
     * Our order (11 slots): head(0), cape(1), amulet(2), ammo(3), weapon(4), body(5), shield(6), legs(7), gloves(8), boots(9), ring(10)
     * RuneLite (14 slots): head(0), cape(1), amulet(2), weapon(3), body(4), shield(5), ARMS(6), legs(7), HAIR(8), gloves(9), boots(10), JAW(11), ring(12), ammo(13)
     *
     * RuneLite indices 6 (arms), 8 (hair), 11 (jaw) are unused slots - we leave them null.
     *
     * Index is our slot, value is RuneLite slot.
     */
    private static final int[] LOADOUT_TO_RUNELITE_SLOT = {
            0,  // our 0 (head) -> RuneLite 0 (head)
            1,  // our 1 (cape) -> RuneLite 1 (cape)
            2,  // our 2 (amulet) -> RuneLite 2 (amulet)
            13, // our 3 (ammo) -> RuneLite 13 (ammo)
            3,  // our 4 (weapon) -> RuneLite 3 (weapon)
            4,  // our 5 (body) -> RuneLite 4 (body)
            5,  // our 6 (shield) -> RuneLite 5 (shield)
            7,  // our 7 (legs) -> RuneLite 7 (legs)
            9,  // our 8 (gloves) -> RuneLite 9 (gloves)
            10, // our 9 (boots) -> RuneLite 10 (boots)
            12  // our 10 (ring) -> RuneLite 12 (ring)
    };

    private LoadoutExporter() {
        // utility class
    }

    /**
     * Exports a Loadout to JSON string.
     *
     * @param loadout the loadout to export
     * @return compact JSON string
     * @throws IllegalArgumentException if loadout is null
     */
    public static String toJson(Loadout loadout) {
        if (loadout == null) {
            throw new IllegalArgumentException("Loadout cannot be null");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"setup\":{");

        // name - required (escape special chars)
        sb.append("\"name\":\"").append(escapeJson(loadout.getName())).append("\"");

        // inventory - 28 slots
        sb.append(",\"inv\":");
        appendItemArray(sb, loadout.getInventory());

        // equipment - convert our 11 slots to RuneLite's 14-slot format
        sb.append(",\"eq\":");
        appendEquipmentArray(sb, loadout.getEquipment());

        // optional containers - only include if present

        // rune pouch
        if (loadout.hasRunePouch()) {
            sb.append(",\"rp\":");
            appendItemArray(sb, loadout.getRunePouch());
        }

        // bolt pouch
        if (loadout.hasBoltPouch()) {
            sb.append(",\"bp\":");
            appendItemArray(sb, loadout.getBoltPouch());
        }

        // quiver
        if (loadout.hasQuiver()) {
            sb.append(",\"qv\":");
            appendItemArray(sb, loadout.getQuiver());
        }

        sb.append("}}");
        return sb.toString();
    }

    /**
     * Exports a Loadout to formatted JSON string with indentation.
     *
     * @param loadout the loadout to export
     * @return formatted JSON string with 2-space indentation
     * @throws IllegalArgumentException if loadout is null
     */
    public static String toJsonPretty(Loadout loadout) {
        if (loadout == null) {
            throw new IllegalArgumentException("Loadout cannot be null");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"setup\": {\n");

        // name - required
        sb.append("    \"name\": \"").append(escapeJson(loadout.getName())).append("\"");

        // inventory - 28 slots
        sb.append(",\n    \"inv\": ");
        appendItemArrayPretty(sb, loadout.getInventory(), "    ");

        // equipment - convert our 11 slots to RuneLite's 14-slot format
        sb.append(",\n    \"eq\": ");
        appendEquipmentArrayPretty(sb, loadout.getEquipment(), "    ");

        // optional containers
        if (loadout.hasRunePouch()) {
            sb.append(",\n    \"rp\": ");
            appendItemArrayPretty(sb, loadout.getRunePouch(), "    ");
        }

        if (loadout.hasBoltPouch()) {
            sb.append(",\n    \"bp\": ");
            appendItemArrayPretty(sb, loadout.getBoltPouch(), "    ");
        }

        if (loadout.hasQuiver()) {
            sb.append(",\n    \"qv\": ");
            appendItemArrayPretty(sb, loadout.getQuiver(), "    ");
        }

        sb.append("\n  }\n}");
        return sb.toString();
    }

    /**
     * Appends an array of items to the StringBuilder in compact format.
     */
    private static void appendItemArray(StringBuilder sb, LoadoutItem[] items) {
        sb.append("[");
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            if (items[i] == null) {
                sb.append("null");
            } else {
                appendItem(sb, items[i]);
            }
        }
        sb.append("]");
    }

    /**
     * Appends an array of items to the StringBuilder with pretty formatting.
     */
    private static void appendItemArrayPretty(StringBuilder sb, LoadoutItem[] items, String indent) {
        sb.append("[\n");
        for (int i = 0; i < items.length; i++) {
            sb.append(indent).append("  ");
            if (items[i] == null) {
                sb.append("null");
            } else {
                appendItem(sb, items[i]);
            }
            if (i < items.length - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append(indent).append("]");
    }

    /**
     * Appends equipment array in RuneLite's 14-slot format (compact).
     * Maps our 11-slot order to RuneLite's order for compatibility.
     */
    private static void appendEquipmentArray(StringBuilder sb, LoadoutItem[] ourEquipment) {
        // build RuneLite's 14-slot array
        LoadoutItem[] runeliteEquipment = new LoadoutItem[14];
        for (int ourIdx = 0; ourIdx < ourEquipment.length; ourIdx++) {
            int runeliteIdx = LOADOUT_TO_RUNELITE_SLOT[ourIdx];
            runeliteEquipment[runeliteIdx] = ourEquipment[ourIdx];
        }
        // slots 11-13 remain null (filler)
        appendItemArray(sb, runeliteEquipment);
    }

    /**
     * Appends equipment array in RuneLite's 14-slot format (pretty).
     * Maps our 11-slot order to RuneLite's order for compatibility.
     */
    private static void appendEquipmentArrayPretty(StringBuilder sb, LoadoutItem[] ourEquipment, String indent) {
        // build RuneLite's 14-slot array
        LoadoutItem[] runeliteEquipment = new LoadoutItem[14];
        for (int ourIdx = 0; ourIdx < ourEquipment.length; ourIdx++) {
            int runeliteIdx = LOADOUT_TO_RUNELITE_SLOT[ourIdx];
            runeliteEquipment[runeliteIdx] = ourEquipment[ourIdx];
        }
        // slots 11-13 remain null (filler)
        appendItemArrayPretty(sb, runeliteEquipment, indent);
    }

    /**
     * Appends a single item to the StringBuilder, omitting default values.
     *
     * <p>Optimization rules (match RuneLite style):
     * <ul>
     *   <li>Omit {@code q} if quantity == 1</li>
     *   <li>Omit {@code f} if fuzzy == false</li>
     *   <li>Omit {@code mode} if mode == EXACT (default)</li>
     * </ul>
     */
    private static void appendItem(StringBuilder sb, LoadoutItem item) {
        sb.append("{\"id\":").append(item.getItemId());

        // omit q if quantity == 1
        if (item.getQuantity() != 1) {
            sb.append(",\"q\":").append(item.getQuantity());
        }

        // omit f if fuzzy == false
        if (item.isFuzzy()) {
            sb.append(",\"f\":true");
        }

        // omit mode if EXACT (default)
        if (item.getMode() != QuantityMode.EXACT) {
            sb.append(",\"mode\":\"").append(item.getMode().name().toLowerCase()).append("\"");
        }

        sb.append("}");
    }

    /**
     * Escapes special characters in a string for JSON output.
     */
    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        // control character - encode as unicode
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
