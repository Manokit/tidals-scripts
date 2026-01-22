package ui

import com.osmb.api.item.ItemGroupResult
import com.osmb.api.item.ItemID

data class HerbGains(
    val ranarrs: Int = 0,
    val toadflax: Int = 0,
    val avantoe: Int = 0,
    val kwuarm: Int = 0,
    val cadantine: Int = 0,
    val lantadyme: Int = 0,
    val dwarfweed: Int = 0,
    val torstol: Int = 0,
    val guam: Int = 0,
    val marrentill: Int = 0,
    val tarromin: Int = 0,
    val harralander: Int = 0,
    val rareFossil: Int = 0,
    val smallFossil: Int = 0,
    val largeFossil: Int = 0,
    val mediumFossil: Int = 0,
    val numulite: Int = 0,
) {
    fun isEmpty() =
        ranarrs == 0 && toadflax == 0 && avantoe == 0 && kwuarm == 0 &&
        cadantine == 0 && lantadyme == 0 && dwarfweed == 0 && torstol == 0 &&
        guam == 0 && marrentill == 0 && tarromin == 0 && harralander == 0 &&
        rareFossil == 0 && smallFossil == 0 && largeFossil == 0 && mediumFossil == 0 && numulite == 0

    fun updateHerbCount(currentItems: ItemGroupResult, previousItems: ItemGroupResult): HerbGains {
        return HerbGains(
            ranarrs = ranarrs + getDifference(currentItems, previousItems, ItemID.GRIMY_RANARR_WEED),
            toadflax = toadflax + getDifference(currentItems, previousItems, ItemID.GRIMY_TOADFLAX),
            avantoe = avantoe + getDifference(currentItems, previousItems, ItemID.GRIMY_AVANTOE),
            kwuarm = kwuarm + getDifference(currentItems, previousItems, ItemID.GRIMY_KWUARM),
            cadantine = cadantine + getDifference(currentItems, previousItems, ItemID.GRIMY_CADANTINE),
            lantadyme = lantadyme + getDifference(currentItems, previousItems, ItemID.GRIMY_LANTADYME),
            dwarfweed = dwarfweed + getDifference(currentItems, previousItems, ItemID.GRIMY_DWARF_WEED),
            torstol = torstol + getDifference(currentItems, previousItems, ItemID.GRIMY_TORSTOL),
            guam = guam + getDifference(currentItems, previousItems, ItemID.GRIMY_GUAM_LEAF),
            marrentill = marrentill + getDifference(currentItems, previousItems, ItemID.GRIMY_MARRENTILL),
            tarromin = tarromin + getDifference(currentItems, previousItems, ItemID.GRIMY_TARROMIN),
            harralander = harralander + getDifference(currentItems, previousItems, ItemID.GRIMY_HARRALANDER),
            rareFossil = rareFossil + getDifference(currentItems, previousItems, ItemID.UNIDENTIFIED_RARE_FOSSIL),
            smallFossil = smallFossil + getDifference(currentItems, previousItems, ItemID.UNIDENTIFIED_SMALL_FOSSIL),
            largeFossil = largeFossil + getDifference(currentItems, previousItems, ItemID.UNIDENTIFIED_LARGE_FOSSIL),
            mediumFossil = mediumFossil + getDifference(currentItems, previousItems, ItemID.UNIDENTIFIED_MEDIUM_FOSSIL),
            numulite = numulite + getDifference(currentItems, previousItems, ItemID.NUMULITE),

        )
    }
    
    private fun getDifference(currentItems: ItemGroupResult, previousItems: ItemGroupResult, itemId: Int): Int {
        val currentAmount = currentItems.getItem(itemId)?.stackAmount ?: 0
        val previousAmount = previousItems.getItem(itemId)?.stackAmount ?: 0
        return currentAmount - previousAmount
    }
}