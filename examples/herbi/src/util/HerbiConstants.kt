package util

import com.osmb.api.item.ItemID
import com.osmb.api.location.area.impl.PolyArea
import com.osmb.api.location.position.types.WorldPosition
import util.HerbiUtils.grow

object HerbiConstants {
    val grimyHerbs = setOf(
        ItemID.GRIMY_AVANTOE, ItemID.GRIMY_CADANTINE, ItemID.GRIMY_DWARF_WEED,
        ItemID.GRIMY_GUAM_LEAF, ItemID.GRIMY_HARRALANDER, ItemID.GRIMY_KWUARM,
        ItemID.GRIMY_LANTADYME, ItemID.GRIMY_MARRENTILL, ItemID.GRIMY_RANARR_WEED,
        ItemID.GRIMY_TARROMIN, ItemID.GRIMY_TOADFLAX, ItemID.GRIMY_TORSTOL
    )
    const val HERBI_PIXEL = -1123301
    const val ANOTHER_HERBY_PIXEL = -2372837
    const val ANOTHER_HERBY_PIXEL_THREE = -13477846

    val HERBI_PIXELS = setOf(
        HERBI_PIXEL,
        ANOTHER_HERBY_PIXEL,
        ANOTHER_HERBY_PIXEL_THREE,
        -12553421,
        -14344439,
        -15055859,
    ).toTypedArray()

    val ISLAND_AREA = WorldPosition(3765, 3899,0).grow(5, 5)
    val CENTER_STUCK_AREA = WorldPosition(3675, 3862, 0).grow(6, 3)
    val PIXEL_FOOTSTEPS = listOf(-14344439, -14212855, -12501977, -14278647, -14344183, -14278391)
    val D_STUCK_TILE = WorldPosition(3708, 3877, 0)
    val F_STUCK_TILE = WorldPosition(3681, 3861, 0)
    val I_STUCK_TILE = WorldPosition(3680, 3837, 0)
    val BOAT_WALK_TO_TILE = WorldPosition(3732, 3894, 0)

    val CRAB_AREA_SOUTH = PolyArea(
        listOf(
            WorldPosition(3706, 3856, 0),
            WorldPosition(3716, 3856, 0),
            WorldPosition(3761, 3855, 0),
            WorldPosition(3761, 3849, 0),
            WorldPosition(3738, 3834, 0),
            WorldPosition(3694, 3841, 0)
        )
    )
    val CRAB_AREA_NORTH = PolyArea(
        listOf(
            WorldPosition(3710, 3898, 0),
            WorldPosition(3717, 3902, 0),
            WorldPosition(3730, 3902, 0),
            WorldPosition(3738, 3891, 0),
            WorldPosition(3741, 3885, 0),
            WorldPosition(3735, 3885, 0),
            WorldPosition(3727, 3886, 0),
            WorldPosition(3724, 3866, 0),
            WorldPosition(3716, 3856, 0),
            WorldPosition(3707, 3856, 0)
        )
    )
}