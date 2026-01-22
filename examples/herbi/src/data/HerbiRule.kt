package data

enum class HerbiboarRule(
    private val to: HerbiboarSearchSpot.Group? = null,
    private val fromStart: HerbiboarStart? = null,
    private val fromGroup: HerbiboarSearchSpot.Group? = null,
) {

    A_MUSHROOM(to = HerbiboarSearchSpot.Group.A, fromStart = HerbiboarStart.MIDDLE),

    C_MUDDY_PATCH(to = HerbiboarSearchSpot.Group.C, fromStart = HerbiboarStart.MIDDLE),

    F_MUSHROOM_START(to = HerbiboarSearchSpot.Group.F, fromStart = HerbiboarStart.MIDDLE),

    D_MUDDY_PATCH(to = HerbiboarSearchSpot.Group.D, fromGroup = HerbiboarSearchSpot.Group.C),

    E_MUSHROOM(to = HerbiboarSearchSpot.Group.E, fromGroup = HerbiboarSearchSpot.Group.A),

    F_MUSHROOM(to = HerbiboarSearchSpot.Group.F, fromGroup = HerbiboarSearchSpot.Group.G),

    G_MUSHROOM(to = HerbiboarSearchSpot.Group.G, fromGroup = HerbiboarSearchSpot.Group.F),

    H_SEAWEED_NE(to = HerbiboarSearchSpot.Group.H, fromGroup = HerbiboarSearchSpot.Group.D),

    H_SEAWEED_SW(to = HerbiboarSearchSpot.Group.H, fromStart = HerbiboarStart.DRIFTWOOD),

    I_MUSHROOM_TWO(to = HerbiboarSearchSpot.Group.I, fromStart = HerbiboarStart.LEPRECHAUN),

    I_MUDDY_PATCH(to = HerbiboarSearchSpot.Group.I, fromStart = HerbiboarStart.CAMP_ENTRANCE),

    I_MUSHROOM(to = HerbiboarSearchSpot.Group.I, fromGroup = HerbiboarSearchSpot.Group.E);

    constructor(to: HerbiboarSearchSpot.Group, from: HerbiboarSearchSpot.Group) : this(to, null, from)

    constructor(to: HerbiboarSearchSpot.Group, fromStart: HerbiboarStart) : this(to, fromStart, null)

    fun matches(from: HerbiboarStart?, to: HerbiboarSearchSpot.Group): Boolean {
        return this.matches(from, null, to)
    }

    fun matches(from: HerbiboarSearchSpot.Group?, to: HerbiboarSearchSpot.Group): Boolean {
        return this.matches(null, from, to)
    }

    fun matches(
        fromStart: HerbiboarStart?,
        fromGroup: HerbiboarSearchSpot.Group?,
        to: HerbiboarSearchSpot.Group
    ): Boolean {
        return (this.to === to
                && (fromStart != null && this.fromStart === fromStart || fromGroup != null && this.fromGroup === fromGroup))
    }

    companion object {
        /**
         * Returns whether the next [HerbiboarSearchSpot] can be deterministically selected based on the starting
         * location and the path taken so far, based on the rules defined on the OSRS wiki.
         *
         * @param start Herbivore's starting spot where the tracking path begins
         * @param currentPath A list of [HerbiboarSearchSpot]s which have been searched thus far, and the next one to search
         * @return `true` if a rule can be applied, `false` otherwise
         */
        fun canApplyRule(start: HerbiboarStart?, currentPath: List<HerbiboarSearchSpot>): Boolean {
            if (start == null || currentPath.isEmpty()) {
                return false
            }
            val lastIndex = currentPath.size - 1
            val goingTo = currentPath[lastIndex].group
            for (rule in values()) {
                if (lastIndex > 0 && rule.matches(currentPath[lastIndex - 1].group, goingTo)
                    || lastIndex == 0 && rule.matches(start, goingTo)
                ) {
                    return true
                }
            }
            return false
        }
    }
}