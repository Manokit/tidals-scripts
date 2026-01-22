package util

import com.osmb.api.script.Script
import com.osmb.api.visual.color.ColorModel
import com.osmb.api.visual.color.ColorUtils
import com.osmb.api.visual.color.tolerance.ToleranceComparator
import com.osmb.api.visual.drawing.Canvas
import com.osmb.api.visual.image.SearchableImage

class CombatHelper(
    private val script: Script,
) {

    private val images: MutableList<SearchableImage> = mutableListOf()
    private var lastAttackTime: Long = 0
    private var lastNpcCombatUpdateTime: Long = 0

    init {
        for (hitsplatID in hitsplats) {
            val canvas = Canvas(hitsplatID, script)
            canvas.fillRect(0, 7, canvas.canvasWidth, 9, ColorUtils.TRANSPARENT_PIXEL)
            val searchableImage: SearchableImage =
                canvas.toSearchableImage(ToleranceComparator.ZERO_TOLERANCE, ColorModel.RGB)
            images.add(searchableImage)
        }
    }

    fun isInCombat(): Boolean = with(script) {
        worldPosition ?: return false

        val tileCube = sceneProjector?.getTileCube(worldPosition, 150) ?: return false
        imageAnalyzer.findLocation(tileCube, *images.toTypedArray()) ?: return false
        lastAttackTime = System.currentTimeMillis()
        return true
    }

    fun isBeingAttacked(): Boolean {
        val timeSinceLastAttack = getTimeSinceLastAttack()
        return timeSinceLastAttack in 0..13_000
    }

    private fun getTimeSinceLastAttack(): Long {
        return if (lastAttackTime == 0L) {
            -1L // No attack detected yet
        } else {
            System.currentTimeMillis() - lastAttackTime
        }
    }

    companion object {
        private val hitsplats = listOf(
            1358,
            1359,
            1360,
            1361
        )
    }
}