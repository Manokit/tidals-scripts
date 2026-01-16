package utilities.items;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility for determining if items stack in the inventory.
 *
 * <p>In OSRS, most items do NOT stack - only specific categories like:
 * <ul>
 *   <li>Coins and currency</li>
 *   <li>Runes</li>
 *   <li>Ammunition (arrows, bolts, darts, etc.)</li>
 *   <li>Notes (banknoted items)</li>
 *   <li>Some quest/minigame items</li>
 * </ul>
 *
 * <p>Uses a combination of ID ranges and known stackable IDs.
 * When in doubt, defaults to non-stackable (safer for inventory planning).
 */
public final class StackabilityUtil {

    // known stackable item IDs - coins, runes, common ammo
    private static final Set<Integer> STACKABLE_IDS = new HashSet<>();

    static {
        // coins
        STACKABLE_IDS.add(995);    // coins

        // runes (standard runes, 554-566)
        for (int id = 554; id <= 566; id++) {
            STACKABLE_IDS.add(id);
        }
        STACKABLE_IDS.add(9075);   // astral rune
        STACKABLE_IDS.add(4695);   // soul rune
        STACKABLE_IDS.add(21880);  // wrath rune

        // arrows (standard arrows range from ~882-892)
        for (int id = 882; id <= 892; id++) {
            STACKABLE_IDS.add(id);
        }
        STACKABLE_IDS.add(11212);  // dragon arrow
        STACKABLE_IDS.add(21326);  // amethyst arrow

        // bolts - common bolt IDs
        STACKABLE_IDS.add(877);    // bronze bolts
        STACKABLE_IDS.add(9140);   // iron bolts
        STACKABLE_IDS.add(9141);   // steel bolts
        STACKABLE_IDS.add(9142);   // mithril bolts
        STACKABLE_IDS.add(9143);   // adamant bolts
        STACKABLE_IDS.add(9144);   // runite bolts
        STACKABLE_IDS.add(21905);  // dragon bolts
        STACKABLE_IDS.add(21316);  // amethyst bolts

        // enchanted gem bolts
        STACKABLE_IDS.add(9236);   // opal bolts (e)
        STACKABLE_IDS.add(9237);   // jade bolts (e)
        STACKABLE_IDS.add(9238);   // pearl bolts (e)
        STACKABLE_IDS.add(9239);   // topaz bolts (e)
        STACKABLE_IDS.add(9240);   // sapphire bolts (e)
        STACKABLE_IDS.add(9241);   // emerald bolts (e)
        STACKABLE_IDS.add(9242);   // ruby bolts (e)
        STACKABLE_IDS.add(9243);   // diamond bolts (e)
        STACKABLE_IDS.add(9244);   // dragonstone bolts (e)
        STACKABLE_IDS.add(9245);   // onyx bolts (e)

        // darts
        for (int id = 806; id <= 811; id++) {
            STACKABLE_IDS.add(id);
        }
        STACKABLE_IDS.add(11230);  // dragon dart

        // throwing knives
        for (int id = 864; id <= 869; id++) {
            STACKABLE_IDS.add(id);
        }

        // chinchompas
        STACKABLE_IDS.add(10033);  // chinchompa
        STACKABLE_IDS.add(10034);  // red chinchompa
        STACKABLE_IDS.add(11959);  // black chinchompa

        // cannonballs
        STACKABLE_IDS.add(2);      // cannonball
    }

    // private constructor
    private StackabilityUtil() {
    }

    /**
     * Checks if an item stacks in inventory.
     *
     * <p>Uses known stackable IDs and common patterns:
     * <ul>
     *   <li>Items in STACKABLE_IDS set</li>
     *   <li>Noted items (placeholder detection would need item definitions)</li>
     * </ul>
     *
     * @param itemId the item ID to check
     * @return true if the item stacks, false otherwise
     */
    public static boolean isStackable(int itemId) {
        // check known stackables
        if (STACKABLE_IDS.contains(itemId)) {
            return true;
        }

        // noted items generally have IDs that are +1 from the unnoted version
        // but we can't reliably detect this without item definitions
        // default to non-stackable for safety

        return false;
    }

    /**
     * Registers an additional stackable item ID.
     * Useful for scripts that know their items are stackable.
     *
     * @param itemId the stackable item ID
     */
    public static void registerStackable(int itemId) {
        STACKABLE_IDS.add(itemId);
    }

    /**
     * Checks if the item is in the known stackable set.
     *
     * @param itemId the item ID
     * @return true if explicitly registered as stackable
     */
    public static boolean isKnownStackable(int itemId) {
        return STACKABLE_IDS.contains(itemId);
    }
}
