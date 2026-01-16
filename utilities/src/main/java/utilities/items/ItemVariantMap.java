package utilities.items;

import java.util.HashMap;
import java.util.Map;

/**
 * Static mappings for degradable item variants (Barrows, Crystal, etc.).
 *
 * <p>Enables fuzzy matching on items where each charge state has a unique ID.
 * The loadout's fuzzy flag uses this to match any variant of the same base item.
 *
 * <p>All methods are static for easy use without instantiation.
 */
public final class ItemVariantMap {

    // map from any variant ID to base ID
    private static final Map<Integer, Integer> VARIANT_TO_BASE = new HashMap<>();

    // map from base ID to all variant IDs (ordered highest charge first)
    private static final Map<Integer, int[]> BASE_TO_VARIANTS = new HashMap<>();

    static {
        // ========================================
        // DHAROK'S EQUIPMENT
        // ========================================

        // Dharok's helm: base 4716, degraded 100->0
        registerVariants(4716, 4716, 4880, 4881, 4882, 4883, 4884);

        // Dharok's greataxe: base 4718, degraded 100->0
        registerVariants(4718, 4718, 4886, 4887, 4888, 4889, 4890);

        // Dharok's platebody: base 4720, degraded 100->0
        registerVariants(4720, 4720, 4892, 4893, 4894, 4895, 4896);

        // Dharok's platelegs: base 4722, degraded 100->0
        registerVariants(4722, 4722, 4898, 4899, 4900, 4901, 4902);

        // ========================================
        // AHRIM'S EQUIPMENT
        // ========================================

        // Ahrim's hood: base 4708, degraded 100->0
        registerVariants(4708, 4708, 4856, 4857, 4858, 4859, 4860);

        // Ahrim's staff: base 4710, degraded 100->0
        registerVariants(4710, 4710, 4862, 4863, 4864, 4865, 4866);

        // Ahrim's robetop: base 4712, degraded 100->0
        registerVariants(4712, 4712, 4868, 4869, 4870, 4871, 4872);

        // Ahrim's robeskirt: base 4714, degraded 100->0
        registerVariants(4714, 4714, 4874, 4875, 4876, 4877, 4878);

        // ========================================
        // KARIL'S EQUIPMENT
        // ========================================

        // Karil's coif: base 4732, degraded 100->0
        registerVariants(4732, 4732, 4928, 4929, 4930, 4931, 4932);

        // Karil's crossbow: base 4734, degraded 100->0
        registerVariants(4734, 4734, 4934, 4935, 4936, 4937, 4938);

        // Karil's leathertop: base 4736, degraded 100->0
        registerVariants(4736, 4736, 4940, 4941, 4942, 4943, 4944);

        // Karil's leatherskirt: base 4738, degraded 100->0
        registerVariants(4738, 4738, 4946, 4947, 4948, 4949, 4950);

        // ========================================
        // TORAG'S EQUIPMENT
        // ========================================

        // Torag's helm: base 4745, degraded 100->0
        registerVariants(4745, 4745, 4952, 4953, 4954, 4955, 4956);

        // Torag's hammers: base 4747, degraded 100->0
        registerVariants(4747, 4747, 4958, 4959, 4960, 4961, 4962);

        // Torag's platebody: base 4749, degraded 100->0
        registerVariants(4749, 4749, 4964, 4965, 4966, 4967, 4968);

        // Torag's platelegs: base 4751, degraded 100->0
        registerVariants(4751, 4751, 4970, 4971, 4972, 4973, 4974);

        // ========================================
        // GUTHAN'S EQUIPMENT
        // ========================================

        // Guthan's helm: base 4724, degraded 100->0
        registerVariants(4724, 4724, 4904, 4905, 4906, 4907, 4908);

        // Guthan's warspear: base 4726, degraded 100->0
        registerVariants(4726, 4726, 4910, 4911, 4912, 4913, 4914);

        // Guthan's platebody: base 4728, degraded 100->0
        registerVariants(4728, 4728, 4916, 4917, 4918, 4919, 4920);

        // Guthan's chainskirt: base 4730, degraded 100->0
        registerVariants(4730, 4730, 4922, 4923, 4924, 4925, 4926);

        // ========================================
        // VERAC'S EQUIPMENT
        // ========================================

        // Verac's helm: base 4753, degraded 100->0
        registerVariants(4753, 4753, 4976, 4977, 4978, 4979, 4980);

        // Verac's flail: base 4755, degraded 100->0
        registerVariants(4755, 4755, 4982, 4983, 4984, 4985, 4986);

        // Verac's brassard: base 4757, degraded 100->0
        registerVariants(4757, 4757, 4988, 4989, 4990, 4991, 4992);

        // Verac's plateskirt: base 4759, degraded 100->0
        registerVariants(4759, 4759, 4994, 4995, 4996, 4997, 4998);

        // ========================================
        // CRYSTAL EQUIPMENT
        // ========================================

        // Crystal bow: new 4212, full 4214, then 9/10->1/10 (4215-4223)
        registerVariants(4212, 4212, 4214, 4215, 4216, 4217, 4218, 4219, 4220, 4221, 4222, 4223);

        // Crystal shield: new 4224, full 4225, then 9/10->1/10 (4226-4234)
        registerVariants(4224, 4224, 4225, 4226, 4227, 4228, 4229, 4230, 4231, 4232, 4233, 4234);

        // Crystal halberd: only base form in OSRS (no degradation states in inventory)
        // ID 32219 is the standard crystal halberd
        registerVariants(32219, 32219);

        // ========================================
        // SERPENTINE HELM
        // ========================================

        // Serpentine helm: uncharged 12929, charged 12931
        registerVariants(12929, 12929, 12931);

        // Magma helm: uncharged 13198, charged 13199
        registerVariants(13198, 13198, 13199);

        // Tanzanite helm: uncharged 13196, charged 13197
        registerVariants(13196, 13196, 13197);

        // ========================================
        // TOXIC BLOWPIPE
        // ========================================

        // Toxic blowpipe: empty 12924, charged 12926
        registerVariants(12924, 12924, 12926);
    }

    /**
     * Registers a variant group with a base ID and all its variants.
     *
     * @param baseId   the canonical base item ID
     * @param variants all variant IDs including base, ordered highest charge first
     */
    private static void registerVariants(int baseId, int... variants) {
        BASE_TO_VARIANTS.put(baseId, variants);
        for (int variant : variants) {
            VARIANT_TO_BASE.put(variant, baseId);
        }
    }

    // private constructor to prevent instantiation
    private ItemVariantMap() {
    }

    /**
     * Gets the base item ID for a given item ID.
     * If the item is not a known variant, returns the original ID.
     *
     * @param itemId the item ID to look up
     * @return the base ID, or the original ID if not a variant
     */
    public static int getBaseId(int itemId) {
        return VARIANT_TO_BASE.getOrDefault(itemId, itemId);
    }

    /**
     * Gets all variant IDs for an item's base.
     * Variants are ordered highest charge first.
     * If the item is not a known variant, returns a single-element array with the original ID.
     *
     * @param itemId any variant ID (or base ID) of the item
     * @return array of all variant IDs for this item's base
     */
    public static int[] getAllVariants(int itemId) {
        int baseId = getBaseId(itemId);
        int[] variants = BASE_TO_VARIANTS.get(baseId);
        if (variants == null) {
            return new int[]{itemId};
        }
        // defensive copy to prevent external mutation
        return variants.clone();
    }

    /**
     * Gets the preferred (highest charge) variant for an item.
     * This is the first element in the variants array, typically the base/new/full state.
     *
     * @param itemId any variant ID of the item
     * @return the preferred variant ID (highest charge)
     */
    public static int getPreferredVariant(int itemId) {
        int baseId = getBaseId(itemId);
        int[] variants = BASE_TO_VARIANTS.get(baseId);
        if (variants == null || variants.length == 0) {
            return itemId;
        }
        return variants[0];
    }

    /**
     * Checks if an item ID is a known variant in the variant map.
     *
     * @param itemId the item ID to check
     * @return true if this ID is registered as a variant
     */
    public static boolean isVariant(int itemId) {
        return VARIANT_TO_BASE.containsKey(itemId);
    }

    /**
     * Checks if two item IDs are variants of the same base item.
     * Returns true if both map to the same base ID.
     *
     * @param id1 first item ID
     * @param id2 second item ID
     * @return true if both IDs map to the same base item
     */
    public static boolean areVariants(int id1, int id2) {
        return getBaseId(id1) == getBaseId(id2);
    }
}
