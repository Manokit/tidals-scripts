package data

import com.osmb.api.location.position.types.WorldPosition

enum class HerbiboarStart(val position: WorldPosition) {
    MIDDLE(WorldPosition(3686, 3870, 0)),
    LEPRECHAUN(WorldPosition(3705, 3830, 0)),
    CAMP_ENTRANCE(WorldPosition(3704, 3810, 0)),
    DRIFTWOOD(WorldPosition(3751, 3850, 0));
//    GHOST_MUSHROOM(WorldPosition(3695, 3800, 0)),
}