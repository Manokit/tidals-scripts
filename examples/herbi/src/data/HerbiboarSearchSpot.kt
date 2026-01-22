package data

import com.osmb.api.location.position.types.WorldPosition

enum class HerbiboarSearchSpot(
    val group: Group,
    val location: WorldPosition,
    val type: SpotType,
) {
    A_MUSHROOM(
        Group.A, WorldPosition(3670, 3889, 0), SpotType.MUSHROOM,
    ),
    A_PATCH(
        Group.A, WorldPosition(3672, 3890, 0), SpotType.PATCH,
    ),
    // Wiki B location
    B_SEAWEED(
        Group.B, WorldPosition(3728, 3893, 0), SpotType.SEAWEED,
    ),
    // Wiki C location
    C_MUSHROOM(
        Group.C, WorldPosition(3697, 3875, 0), SpotType.MUSHROOM,
    ),
    C_PATCH(
        Group.C, WorldPosition(3699, 3875, 0), SpotType.PATCH,
    ),
    // Wiki D location
    D_PATCH(
        Group.D, WorldPosition(3708, 3876, 0), SpotType.PATCH,
    ),
    D_SEAWEED(
        Group.D, WorldPosition(3710, 3877, 0), SpotType.SEAWEED,
    ),
    // Wiki E location
    E_MUSHROOM(
        Group.E, WorldPosition(3668, 3865, 0), SpotType.MUSHROOM,
    ),
    E_PATCH(
        Group.E, WorldPosition(3667, 3862, 0), SpotType.PATCH,
    ),
    // Wiki F location
    F_MUSHROOM(
        Group.F, WorldPosition(3681, 3860, 0), SpotType.MUSHROOM,
    ),
    F_PATCH(
        Group.F, WorldPosition(3681, 3859, 0), SpotType.PATCH,
    ),  // Wiki G location
    G_MUSHROOM(
        Group.G, WorldPosition(3694, 3847, 0), SpotType.MUSHROOM,
    ),
    G_PATCH(
        Group.G, WorldPosition(3698, 3847, 0), SpotType.PATCH,
    ),  // Wiki H location
    H_SEAWEED_EAST(
        Group.H, WorldPosition(3715, 3851, 0), SpotType.SEAWEED,
    ),
    H_SEAWEED_WEST(
        Group.H, WorldPosition(3713, 3850, 0), SpotType.SEAWEED,
    ),
    // Wiki I location
    I_MUSHROOM(
        Group.I, WorldPosition(3680, 3838, 0), SpotType.MUSHROOM,
    ),
    I_PATCH(
        Group.I, WorldPosition(3680, 3836, 0), SpotType.PATCH,
    ),
    // Wiki J location
    J_PATCH(
        Group.J, WorldPosition(3713, 3840, 0), SpotType.PATCH,
    ),
    // Wiki K location
    K_PATCH(
        Group.K, WorldPosition(3706, 3811, 0), SpotType.PATCH,
    ),
    END_A(
        Group.END_A, WorldPosition(3693, 3798, 0), SpotType.TUNNEL, // Waaaay south
    ),
    END_B(
        Group.END_B, WorldPosition(3702, 3808, 0), SpotType.TUNNEL, // south east by the sand transition
    ),
    END_C(
        Group.END_C, WorldPosition(3703, 3826, 0), SpotType.TUNNEL, // SW of trees
    ),
    END_D(
        Group.END_D, WorldPosition(3710, 3881, 0), SpotType.TUNNEL, // right by D
    ),
    END_E(
        Group.END_E, WorldPosition(3700, 3877, 0), SpotType.TUNNEL, // right by c
    ),
    END_F(
        Group.END_F, WorldPosition(3715, 3840, 0), SpotType.TUNNEL, // Right by J, north of trees
    ),
    END_G(
        Group.END_G, WorldPosition(3685, 3869, 0), SpotType.TUNNEL, // right by the middle start
    ),
    END_H(
        Group.END_H, WorldPosition(3751, 3849, 0), SpotType.TUNNEL, // FAR OUT south east
    ),
    END_I(
        Group.END_I, WorldPosition(3681, 3863, 0), SpotType.TUNNEL, // south west of middle
    );

    companion object {
        private val GROUPS: Map<Group, List<HerbiboarSearchSpot>>
        private val SPOTS: Set<WorldPosition>
        private val TRAILS: Set<Int>

        init {
            val groupBuilder = mutableMapOf<Group, MutableList<HerbiboarSearchSpot>>()
            val spotBuilder = mutableSetOf<WorldPosition>()
            val trailBuilder = mutableSetOf<Int>()

            for (spot in values()) {
                groupBuilder.getOrPut(spot.group) { mutableListOf() }.add(spot)
                spotBuilder.add(spot.location)
            }

            GROUPS = groupBuilder.mapValues { it.value.toList() }
            SPOTS = spotBuilder.toSet()
            TRAILS = trailBuilder.toSet()
        }

        fun noEnds(): List<HerbiboarSearchSpot> =
            values().filter { !it.group.name.startsWith("END") }

        fun isTrail(id: Int): Boolean = TRAILS.contains(id)

        fun isSearchSpot(location: WorldPosition): Boolean = SPOTS.contains(location)

        fun getGroupLocations(group: Group): List<WorldPosition> =
            GROUPS[group]?.map { it.location } ?: emptyList()
    }

    enum class Group {
        A, B, C, D, E, F, G, H, I, J, K, START, END, END_A, END_B, END_C, END_D, END_E, END_F, END_G, END_H, END_I
    }
    enum class SpotType {
        MUSHROOM, PATCH, SEAWEED, TUNNEL
    }
}