package utilities.loadout;

/**
 * Imports loadouts from RuneLite Inventory Setups JSON format.
 *
 * <p>Accepts two formats:
 * <ul>
 *   <li><b>Wrapped format</b> (RuneLite portable export): {@code {"setup": {...}, "layout": [...]}}</li>
 *   <li><b>Direct format</b> (just the setup object): {@code {"name": "...", "inv": [...], "eq": [...]}}</li>
 * </ul>
 *
 * <p>Setup object structure:
 * <pre>
 * {
 *   "name": "loadout name",
 *   "inv": [...],  // 28 inventory slots
 *   "eq": [...],   // 14 equipment slots (we use 11)
 *   "rp": [...],   // rune pouch (optional)
 *   "bp": [...],   // bolt pouch (optional)
 *   "qv": [...]    // quiver (optional)
 * }
 * </pre>
 *
 * <p>Equipment slot mapping (RuneLite 14 -> our 11):
 * <pre>
 * RuneLite Index -> Our Index
 * 0  Head        -> 0  Head
 * 1  Cape        -> 1  Cape
 * 2  Amulet      -> 2  Amulet
 * 3  Weapon      -> 4  Weapon
 * 4  Body        -> 5  Body
 * 5  Shield      -> 6  Shield
 * 6  Arms        -> ignored (unused)
 * 7  Legs        -> 7  Legs
 * 8  Hair        -> ignored (unused)
 * 9  Gloves      -> 8  Gloves
 * 10 Boots       -> 9  Boots
 * 11 Jaw         -> ignored (unused)
 * 12 Ring        -> 10 Ring
 * 13 Ammo        -> 3  Ammo
 * </pre>
 *
 * @see LoadoutExporter for exporting loadouts
 */
public class LoadoutImporter {

    /**
     * Maps RuneLite equipment indices to our Loadout equipment indices.
     *
     * RuneLite (14 slots): head(0), cape(1), amulet(2), weapon(3), body(4), shield(5), ARMS(6), legs(7), HAIR(8), gloves(9), boots(10), JAW(11), ring(12), ammo(13)
     * Our order (11 slots): head(0), cape(1), amulet(2), ammo(3), weapon(4), body(5), shield(6), legs(7), gloves(8), boots(9), ring(10)
     *
     * RuneLite indices 6 (arms), 8 (hair), 11 (jaw) are unused slots - always null.
     *
     * Index is RuneLite slot, value is our slot. -1 means ignored.
     */
    private static final int[] RUNELITE_TO_LOADOUT_SLOT = {
            0,  // RuneLite 0 (head) -> our 0 (head)
            1,  // RuneLite 1 (cape) -> our 1 (cape)
            2,  // RuneLite 2 (amulet) -> our 2 (amulet)
            4,  // RuneLite 3 (weapon) -> our 4 (weapon)
            5,  // RuneLite 4 (body) -> our 5 (body)
            6,  // RuneLite 5 (shield) -> our 6 (shield)
            -1, // RuneLite 6 (arms) -> ignored (unused slot)
            7,  // RuneLite 7 (legs) -> our 7 (legs)
            -1, // RuneLite 8 (hair) -> ignored (unused slot)
            8,  // RuneLite 9 (gloves) -> our 8 (gloves)
            9,  // RuneLite 10 (boots) -> our 9 (boots)
            -1, // RuneLite 11 (jaw) -> ignored (unused slot)
            10, // RuneLite 12 (ring) -> our 10 (ring)
            3   // RuneLite 13 (ammo) -> our 3 (ammo)
    };

    private LoadoutImporter() {
        // utility class
    }

    /**
     * Parses a RuneLite Inventory Setups JSON string into a Loadout.
     *
     * <p>Accepts two formats:
     * <ul>
     *   <li>Wrapped format: {@code {"setup": {...}, "layout": [...]}}</li>
     *   <li>Direct format: {@code {"name": "...", "inv": [...], "eq": [...]}}</li>
     * </ul>
     *
     * @param json the JSON string in RuneLite portable format
     * @return the parsed Loadout
     * @throws IllegalArgumentException if JSON is invalid or missing required fields
     */
    public static Loadout fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }

        try {
            String trimmed = json.trim();

            // find the setup object - either wrapped or direct
            String setupJson = extractSetupObject(trimmed);

            // parse name - required
            String name = extractStringField(setupJson, "name");
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Missing required 'name' field in setup");
            }

            Loadout loadout = new Loadout(name);

            // parse inventory (28 slots)
            String invArray = extractArrayField(setupJson, "inv");
            if (invArray != null) {
                LoadoutItem[] inventory = parseItemArray(invArray, Loadout.INVENTORY_SIZE);
                for (int i = 0; i < inventory.length; i++) {
                    loadout.setInventorySlot(i, inventory[i]);
                }
            }

            // parse equipment (14 RuneLite slots -> 11 our slots with mapping)
            String eqArray = extractArrayField(setupJson, "eq");
            if (eqArray != null) {
                // parse all 14 RuneLite slots
                LoadoutItem[] runeliteEquipment = parseItemArray(eqArray, 14);
                // map to our slot order
                for (int runeliteIdx = 0; runeliteIdx < runeliteEquipment.length && runeliteIdx < RUNELITE_TO_LOADOUT_SLOT.length; runeliteIdx++) {
                    int ourIdx = RUNELITE_TO_LOADOUT_SLOT[runeliteIdx];
                    if (ourIdx >= 0) {
                        loadout.setEquipment(ourIdx, runeliteEquipment[runeliteIdx]);
                    }
                }
            }

            // parse optional containers

            // rune pouch (4 slots)
            String rpArray = extractArrayField(setupJson, "rp");
            if (rpArray != null) {
                loadout.setRunePouch(parseItemArray(rpArray, Loadout.RUNE_POUCH_SIZE));
            }

            // bolt pouch (4 slots)
            String bpArray = extractArrayField(setupJson, "bp");
            if (bpArray != null) {
                loadout.setBoltPouch(parseItemArray(bpArray, Loadout.BOLT_POUCH_SIZE));
            }

            // quiver (1 slot)
            String qvArray = extractArrayField(setupJson, "qv");
            if (qvArray != null) {
                loadout.setQuiver(parseItemArray(qvArray, Loadout.QUIVER_SIZE));
            }

            return loadout;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON format: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the setup object JSON string from the root.
     * Handles both wrapped format {"setup": {...}} and direct format.
     */
    private static String extractSetupObject(String json) {
        // look for "setup" key
        int setupIdx = json.indexOf("\"setup\"");
        if (setupIdx >= 0) {
            // find the opening brace after "setup":
            int colonIdx = json.indexOf(':', setupIdx);
            if (colonIdx >= 0) {
                int braceStart = json.indexOf('{', colonIdx);
                if (braceStart >= 0) {
                    return extractObject(json, braceStart);
                }
            }
        }

        // no setup wrapper - check if this is direct format with name and inv/eq
        if (json.contains("\"name\"") && (json.contains("\"inv\"") || json.contains("\"eq\""))) {
            // direct format - use the whole object
            int braceStart = json.indexOf('{');
            if (braceStart >= 0) {
                return extractObject(json, braceStart);
            }
        }

        throw new IllegalArgumentException("Missing required 'setup' object or direct format fields");
    }

    /**
     * Extracts a complete JSON object starting at the given brace position.
     * Handles nested braces and quoted strings.
     */
    private static String extractObject(String json, int start) {
        if (json.charAt(start) != '{') {
            throw new IllegalArgumentException("Expected '{' at position " + start);
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, i + 1);
                    }
                }
            }
        }

        throw new IllegalArgumentException("Unterminated JSON object");
    }

    /**
     * Extracts a string field value from a JSON object.
     */
    private static String extractStringField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) {
            return null;
        }

        // find the colon after the field name
        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx < 0) {
            return null;
        }

        // find the opening quote
        int quoteStart = json.indexOf('"', colonIdx);
        if (quoteStart < 0) {
            return null;
        }

        // find the closing quote (handling escapes)
        int quoteEnd = findStringEnd(json, quoteStart + 1);
        if (quoteEnd < 0) {
            return null;
        }

        return unescapeJson(json.substring(quoteStart + 1, quoteEnd));
    }

    /**
     * Finds the end of a JSON string (position of closing quote).
     */
    private static int findStringEnd(String json, int start) {
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Extracts an array field from a JSON object.
     * Returns the array contents including brackets.
     */
    private static String extractArrayField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) {
            return null;
        }

        // find the colon after the field name
        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx < 0) {
            return null;
        }

        // skip whitespace
        int bracketStart = colonIdx + 1;
        while (bracketStart < json.length() && Character.isWhitespace(json.charAt(bracketStart))) {
            bracketStart++;
        }

        if (bracketStart >= json.length() || json.charAt(bracketStart) != '[') {
            return null;
        }

        // find matching closing bracket
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = bracketStart; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == '[') {
                    depth++;
                } else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        return json.substring(bracketStart, i + 1);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Parses an array of items from JSON array string.
     */
    private static LoadoutItem[] parseItemArray(String arrayJson, int expectedSize) {
        LoadoutItem[] items = new LoadoutItem[expectedSize];

        // remove outer brackets
        String content = arrayJson.substring(1, arrayJson.length() - 1).trim();
        if (content.isEmpty()) {
            return items;
        }

        // split into elements (handling nested objects)
        int slotIndex = 0;
        int start = 0;
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i <= content.length() && slotIndex < expectedSize; i++) {
            boolean atEnd = (i == content.length());
            char c = atEnd ? ',' : content.charAt(i);

            if (!atEnd) {
                if (escaped) {
                    escaped = false;
                    continue;
                }

                if (c == '\\') {
                    escaped = true;
                    continue;
                }

                if (c == '"') {
                    inString = !inString;
                    continue;
                }

                if (!inString) {
                    if (c == '{' || c == '[') {
                        depth++;
                        continue;
                    }
                    if (c == '}' || c == ']') {
                        depth--;
                        continue;
                    }
                }
            }

            if ((c == ',' && depth == 0 && !inString) || atEnd) {
                String element = content.substring(start, i).trim();
                if (!element.isEmpty()) {
                    if (element.equals("null")) {
                        items[slotIndex] = null;
                    } else if (element.startsWith("{")) {
                        items[slotIndex] = parseItem(element);
                    }
                }
                slotIndex++;
                start = i + 1;
            }
        }

        return items;
    }

    /**
     * Parses a single item from JSON object string.
     */
    private static LoadoutItem parseItem(String itemJson) {
        // id is required
        int id = extractIntField(itemJson, "id", -1);
        if (id < 0) {
            return null;
        }

        // quantity defaults to 1 when null/omitted
        int quantity = extractIntField(itemJson, "q", 1);

        // fuzzy defaults to false when null/omitted
        boolean fuzzy = extractBooleanField(itemJson, "f", false);

        // mode defaults to EXACT when null/omitted
        String modeStr = extractStringField(itemJson, "mode");
        QuantityMode mode = QuantityMode.EXACT;
        if (modeStr != null && !modeStr.isEmpty()) {
            try {
                mode = QuantityMode.valueOf(modeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore invalid mode, use default
            }
        }

        return LoadoutItem.builder()
                .itemId(id)
                .quantity(quantity)
                .fuzzy(fuzzy)
                .mode(mode)
                .build();
    }

    /**
     * Extracts an integer field value from a JSON object.
     */
    private static int extractIntField(String json, String fieldName, int defaultValue) {
        String pattern = "\"" + fieldName + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) {
            return defaultValue;
        }

        // find the colon after the field name
        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx < 0) {
            return defaultValue;
        }

        // skip whitespace
        int start = colonIdx + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        // read digits (and optional minus)
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '-' && sb.length() == 0) {
                sb.append(c);
            } else if (Character.isDigit(c)) {
                sb.append(c);
            } else {
                break;
            }
        }

        if (sb.length() == 0) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Extracts a boolean field value from a JSON object.
     */
    private static boolean extractBooleanField(String json, String fieldName, boolean defaultValue) {
        String pattern = "\"" + fieldName + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) {
            return defaultValue;
        }

        // find the colon after the field name
        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx < 0) {
            return defaultValue;
        }

        // skip whitespace
        int start = colonIdx + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        // check for true/false
        if (json.regionMatches(start, "true", 0, 4)) {
            return true;
        }
        if (json.regionMatches(start, "false", 0, 5)) {
            return false;
        }

        return defaultValue;
    }

    /**
     * Unescapes a JSON string value.
     */
    private static String unescapeJson(String s) {
        if (s == null || !s.contains("\\")) {
            return s;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"':
                        sb.append('"');
                        i++;
                        break;
                    case '\\':
                        sb.append('\\');
                        i++;
                        break;
                    case 'b':
                        sb.append('\b');
                        i++;
                        break;
                    case 'f':
                        sb.append('\f');
                        i++;
                        break;
                    case 'n':
                        sb.append('\n');
                        i++;
                        break;
                    case 'r':
                        sb.append('\r');
                        i++;
                        break;
                    case 't':
                        sb.append('\t');
                        i++;
                        break;
                    case 'u':
                        // unicode escape (backslash u followed by 4 hex digits)
                        if (i + 5 < s.length()) {
                            try {
                                int codePoint = Integer.parseInt(s.substring(i + 2, i + 6), 16);
                                sb.append((char) codePoint);
                                i += 5;
                            } catch (NumberFormatException e) {
                                sb.append(c);
                            }
                        } else {
                            sb.append(c);
                        }
                        break;
                    default:
                        sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
