package data

import com.osmb.api.location.position.types.WorldPosition
import data.HerbiboarSearchSpot.Group

enum class PixelTile(val position: WorldPosition, val source: Group, val destination: Group) {

    // =========================
    // START → destinations
    // =========================
    START_TO_A(WorldPosition(3685, 3872, 0), Group.START, Group.A),
    START_TO_C(WorldPosition(3689, 3872, 0), Group.START, Group.C),
    START_TO_G(WorldPosition(3702, 3829, 0), Group.START, Group.G),
    START_TO_H(WorldPosition(3748, 3847, 0), Group.START, Group.H), // driftwood
    START_TO_I(WorldPosition(3701, 3812, 0), Group.START, Group.I), // muddy patch
    START_TO_F(WorldPosition(3701, 3832, 0), Group.START, Group.F),
    START_TO_F_FROM_MIDDLE(WorldPosition(3683, 3867, 0), Group.START, Group.F),
    START_TO_J_THROUGH_CAVE(WorldPosition(3708, 3811, 0), Group.START, Group.J),
    TREES_START_TO_G(WorldPosition(3702, 3829, 0), Group.START, Group.G),

    // =========================
    // A → destinations
    // =========================
    A_TO_B(WorldPosition(3675, 3893, 0), Group.A, Group.B),
    A_TO_B_ALTERNATIVE(WorldPosition(3672, 3892, 0), Group.A, Group.B),
    A_TO_E(WorldPosition(3665, 3888, 0), Group.A, Group.E),
    A_TO_END(WorldPosition(3673, 3884, 0), Group.A, Group.END_G),

    // =========================
    // B → destinations
    // =========================
    B_TO_A(WorldPosition(3719, 3898, 0), Group.B, Group.A), // update needed
    B_TO_END_BY_C(WorldPosition(3723, 3894, 0), Group.B, Group.END_E), // END BY C
    B_TO_END_BY_D(WorldPosition(3726, 3888, 0), Group.B, Group.END_D),
    B_TO_C(WorldPosition(3724, 3891, 0), Group.B, Group.C),

    // =========================
    // C → destinations
    // =========================
    C_TO_B(WorldPosition(3700, 3872, 0), Group.C, Group.B), // might need update
    C_TO_D(WorldPosition(3701, 3876, 0), Group.C, Group.D),
    C_TO_END(WorldPosition(3699, 3873, 0), Group.C, Group.END_G),

    // =========================
    // D → destinations
    // =========================
    D_TO_C(WorldPosition(3708, 3879, 0), Group.D, Group.C),
    D_TO_H(WorldPosition(3711, 3878, 0), Group.D, Group.H),
    D_TO_B(WorldPosition(3715, 3883, 0), Group.D, Group.B),
    D_TO_G(WorldPosition(3709, 3872, 0), Group.D, Group.G),
    // =========================
    // E → destinations
    // =========================
    E_TO_A(WorldPosition(3666, 3866, 0), Group.E, Group.A),
    E_TO_I(WorldPosition(3664, 3861, 0), Group.E, Group.I),
    E_TO_END_I(WorldPosition(3672, 3862, 0), Group.E, Group.END_I),

    // =========================
    // F → destinations
    // =========================
    F_TO_C(WorldPosition(3726, 3892, 0), Group.F, Group.C),
    F_TO_E(WorldPosition(3675, 3862, 0), Group.F, Group.E),
    F_TO_G(WorldPosition(3683, 3860, 0), Group.F, Group.G),
    F_TO_I(WorldPosition(3680, 3854, 0), Group.F, Group.I),
    F_TO_END(WorldPosition(3683, 3863, 0), Group.F, Group.END_G), // END_F
    // =========================
    // G → destinations
    // =========================
    G_TO_D(WorldPosition(3697, 3844, 0), Group.G, Group.D),
    G_TO_F(WorldPosition(3697, 3850, 0), Group.G, Group.F),
    G_TO_END(WorldPosition(3694, 3842, 0), Group.G, Group.END_C), // END_C

    // =========================
    // H → destinations
    // =========================
    H_TO_F(WorldPosition(3696, 3848, 0), Group.H, Group.F),
    H_TO_G(WorldPosition(3710, 3851, 0), Group.H, Group.G),
    H_TO_END_BY_J(WorldPosition(3714, 3847, 0), Group.H, Group.END_F),
    H_TO_END(WorldPosition(3719, 3847, 0), Group.H, Group.END_H), // END_H
    H_TO_D(WorldPosition(3715, 3856, 0), Group.H, Group.D),

    // =========================
    // I → destinations
    // =========================
    I_TO_E(WorldPosition(3673, 3839, 0), Group.I, Group.E),
    I_TO_F(WorldPosition(3682, 3842, 0), Group.I, Group.F),
    I_TO_H(WorldPosition(3714, 3841, 0), Group.I, Group.H),
    I_TO_END_TWO(WorldPosition(3682, 3836, 0), Group.I, Group.END_B), // END_B
    I_TO_END(WorldPosition(3682, 3836, 0), Group.I, Group.END_C), // END_C eed tp cjamge thois pme
    I_TO_END_A(WorldPosition(3682, 3831, 0), Group.I, Group.END_A), // END_A all the way south
    // I_TO_J(WorldPosition(3682, 3836, 0), Group.I, Group.J), // needs change

    // =========================
    // J → destinations
    // =========================
    J_TO_D(WorldPosition(3711, 3842, 0), Group.J, Group.D),
    J_TO_H(WorldPosition(3714, 3844, 0), Group.J, Group.H);

    companion object {
        fun noEnds() = values().filter { !it.destination.name.startsWith("END") }

        fun toSearchSpot(pixelTile: PixelTile?): HerbiboarSearchSpot? {
            return pixelTile?.let { tile ->
                // Find a data.HerbiboarSearchSpot that matches the destination group
                HerbiboarSearchSpot.values().firstOrNull { it.group == tile.destination }
            }
        }
    }
}