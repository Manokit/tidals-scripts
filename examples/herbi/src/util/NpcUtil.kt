package util

import com.osmb.api.script.Script
import com.osmb.api.visual.PixelCluster
import com.osmb.api.visual.SearchablePixel
import com.osmb.api.visual.color.ColorModel
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator

class NpcUtil(
    private val script: Script,
) {
    fun interactWithNpc(action: String, forceClick: Boolean, vararg pixelColor: Int): Boolean {
        val minimapNpcs = script.widgetManager.minimap.npcPositions
        val searchablePixels = pixelColor.map { color -> SearchablePixel(color, SingleThresholdComparator(2), ColorModel.HSL) }
        minimapNpcs.forEach {
            val npcTileCube = script.sceneProjector.getTileCube(it, 130, true)
            if (npcTileCube != null) {
                val highlightBounds = script.pixelAnalyzer.getHighlightBounds(npcTileCube, *searchablePixels.toTypedArray())
                if (highlightBounds != null) {

                    if (forceClick) {
                        val result = script.finger.tapGetResponse(false, highlightBounds)

                        return if (result != null && result.action.contains(action, ignoreCase = true)) {
                            true
                        } else {
                            script.finger.tap(false, highlightBounds) { menus ->
                                menus.firstOrNull { entry -> entry.action.lowercase().contains(action.lowercase()) }
                            }
                        }
                    }
                    return script.finger.tap(false, highlightBounds) { menus ->
                        menus.firstOrNull { entry -> entry.action.lowercase().contains(action.lowercase()) }
                    }
                }
            }
        }
        return false
    }

    fun getNpc(vararg pixelColor: Int) = script.widgetManager.minimap.npcPositions.firstOrNull {
        val npcTileCube = script.sceneProjector.getTileCube(it, 130)
        if (npcTileCube != null) {
            val searchablePixel = pixelColor.map { color -> SearchablePixel(color, SingleThresholdComparator(2), ColorModel.HSL) }
            val clusterQuery = PixelCluster.ClusterQuery(5, 3, searchablePixel.toTypedArray())
            val clusters = script.pixelAnalyzer.findClusters(npcTileCube, clusterQuery)?.getClusters() ?: return@firstOrNull false

            clusters.isNotEmpty()
        } else {
            false
        }
    }
}