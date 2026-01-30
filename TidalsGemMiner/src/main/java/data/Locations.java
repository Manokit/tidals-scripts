package data;

import com.osmb.api.location.area.Area;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;

public final class Locations {

    private Locations() {
    }

    private static final WorldPosition UPPER_MINE_POSITION = new WorldPosition(2823, 2999, 0);
    private static final WorldPosition UPPER_BANK_POSITION = new WorldPosition(2852, 2953, 0);
    private static final WorldPosition UNDERGROUND_MINE_POSITION = new WorldPosition(2838, 9388, 0);
    private static final WorldPosition UNDERGROUND_BANK_POSITION = new WorldPosition(2842, 9383, 0);

    // mining area bounds - roughly 10 tile radius around gem rocks
    // upper mine: centered around (2823, 2999) - small area with few rocks
    private static final Area UPPER_MINING_AREA = new RectangleArea(2818, 2994, 12, 12, 0);
    // underground mine: larger area with many rocks
    // expanded to include all hardcoded rock positions (X: 2826-2857, Y: 9377-9399)
    private static final Area UNDERGROUND_MINING_AREA = new RectangleArea(2825, 9377, 34, 24, 0);

    // approach area for underground deposit chest - walk to this area before interacting
    // covers tiles where deposit chest is visible and reachable
    private static final Area UNDERGROUND_APPROACH_AREA = new RectangleArea(2839, 9383, 6, 5, 0);

    public record MiningLocation(
            String name,
            String displayName,
            WorldPosition minePosition,
            WorldPosition bankPosition,
            Area approachArea,  // area to walk to before banking (null = use bankPosition directly)
            String depositObjectName,
            String depositAction,
            int[] priorityRegions,
            Area miningArea
    ) {
        public static MiningLocation fromDisplay(String displayName) {
            if (displayName != null) {
                if (UPPER.displayName().equalsIgnoreCase(displayName.trim())) {
                    return UPPER;
                }
                if (UNDERGROUND.displayName().equalsIgnoreCase(displayName.trim())) {
                    return UNDERGROUND;
                }
            }
            return UPPER; // default
        }
    }

    public static final MiningLocation UPPER = new MiningLocation(
            "upper",
            "Upper Mine",
            UPPER_MINE_POSITION,
            UPPER_BANK_POSITION,
            null,  // no approach area - use distance-based logic
            "Bank Deposit Box",
            "Deposit",
            new int[]{11310},
            UPPER_MINING_AREA
    );

    public static final MiningLocation UNDERGROUND = new MiningLocation(
            "underground",
            "Underground Mine",
            UNDERGROUND_MINE_POSITION,
            UNDERGROUND_BANK_POSITION,
            UNDERGROUND_APPROACH_AREA,  // walk to general area before depositing
            "Bank Deposit Chest",
            "Deposit",
            new int[]{11410},
            UNDERGROUND_MINING_AREA
    );

    public static final MiningLocation[] ALL_LOCATIONS = {UPPER, UNDERGROUND};
}
