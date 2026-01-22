package ui

import com.osmb.api.trackers.experience.XPTracker
import com.osmb.api.visual.drawing.Canvas
import java.awt.Color
import java.awt.Font
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.roundToInt

/**
 * Delegate class for handling paint operations in the Herbi script
 */
class HerbiPaintDelegate {
    
    private val FONT_LABEL = Font("Arial", Font.PLAIN, 12)
    private val FONT_VALUE_BOLD = Font("Arial", Font.BOLD, 12)
    
    // Paint data interface
    interface PaintData {
        val version: String
        val startTime: Long
        val harvestCount: Int
        val prevState: String?
        val startedObject: String?
        val catchStartTime: Long
        val averageCatchTime: Long
        val useStaminas: Boolean
        val selectedPotionDisplayName: String
        val successRate: Double
        val herbGains: HerbGains
        val canHop: Boolean
        val herbloreExpTracker: XPTracker?
        val hunterExpTracker: XPTracker?
    }
    
    /**
     * Main paint method that handles all the drawing logic
     */
    fun onPaint(c: Canvas, data: PaintData) {
        val elapsed = System.currentTimeMillis() - data.startTime
        val hours = 1e-9.coerceAtLeast(elapsed / 3600000.0)
        val herbiHr = (data.harvestCount / hours).roundToInt()

        val x = 5
        val baseY = 40
        val width = 290
        val borderThickness = 2
        val paddingX = 10
        val topGap = 6
        val lineGap = 16

        val labelGray: Int = Color.WHITE.rgb
        val valueWhite: Int = Color.WHITE.rgb
        val valueGreen: Int = Color.decode("#306844").rgb

        val innerX = x
        val innerY = baseY
        val innerWidth = width

        var curY = innerY + topGap

        curY += lineGap

        val runtime: String = formatRuntime(elapsed)

        val intFmt = DecimalFormat("#,###")
        val sym = DecimalFormatSymbols()
        sym.groupingSeparator = ','
        intFmt.decimalFormatSymbols = sym
        val y = innerY + topGap

        val innerHeight = Math.max(240, y - innerY)

        c.fillRect(innerX - borderThickness, innerY - borderThickness,
            innerWidth + (borderThickness * 2),
            innerHeight + (borderThickness * 2),
            Color.WHITE.rgb, 1.0)
        c.fillRect(innerX, innerY, innerWidth, innerHeight, Color.decode("#319608").rgb, 0.8)
        c.drawRect(innerX, innerY, innerWidth, innerHeight, Color.WHITE.rgb)

        drawStatLine(c, innerX, innerWidth, paddingX, curY,
            "Rats Herbiboar (v${data.version})", "", labelGray, valueWhite,
            FONT_VALUE_BOLD, FONT_LABEL)
        curY += lineGap
        curY += lineGap
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
            data.prevState ?: "Initializing...", "", labelGray, valueWhite,
            FONT_VALUE_BOLD, FONT_LABEL)
        curY += lineGap
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
            "Runtime", runtime, labelGray, valueWhite,
            FONT_VALUE_BOLD, FONT_LABEL)
        curY += lineGap

        drawStatLine(c, innerX, innerWidth, paddingX, curY,
            "Herbiboars caught", "${intFmt.format(data.harvestCount)} (${intFmt.format(herbiHr)} hr)",
            labelGray, valueWhite, FONT_VALUE_BOLD, FONT_LABEL)
        curY += lineGap

        data.hunterExpTracker?.let {
            drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Hunter exp gained", "${intFmt.format(it.xpGained)} (${intFmt.format(it.xpPerHour)} hr)",
                labelGray, valueWhite, FONT_VALUE_BOLD, FONT_LABEL)
            curY += lineGap
        }

        data.herbloreExpTracker?.let {
            drawStatLine(c, innerX, innerWidth, paddingX, curY,
                "Herblore exp gained", "${intFmt.format(it.xpGained)} (${intFmt.format(it.xpPerHour)} hr)",
                labelGray, valueWhite, FONT_VALUE_BOLD, FONT_LABEL)
            curY += lineGap
        }

        drawStatLine(c, innerX, innerWidth, paddingX, curY,
            "Current start", data.startedObject ?: "None",
            labelGray, valueWhite, FONT_VALUE_BOLD, FONT_LABEL)
        curY += lineGap

        val catchTime = if (data.catchStartTime == 0L) 0 else System.currentTimeMillis() - data.catchStartTime
        val color = if (catchTime <= data.averageCatchTime || data.averageCatchTime == 0L) valueGreen else Color.ORANGE.rgb
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
            "Current catch time", formatTime(catchTime),
            labelGray, color, FONT_VALUE_BOLD, FONT_LABEL)
        curY += lineGap

        drawStatLine(c, innerX, innerWidth, paddingX, curY,
            "Average catch time", formatTime(data.averageCatchTime),
            labelGray, valueWhite, FONT_VALUE_BOLD, FONT_LABEL)
        curY += lineGap
        
        val successColor = if (data.successRate == 0.0) valueWhite else if (data.successRate >= 70.0) valueGreen else if (data.successRate >= 50.0) Color.ORANGE.rgb else Color.RED.rgb
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
            "Success rate", String.format("%.1f%%", data.successRate),
            labelGray, successColor, FONT_VALUE_BOLD, FONT_LABEL)
        curY += lineGap

        drawStatLine(c, innerX, innerWidth, paddingX, curY,
            "Energy restoration", if (data.useStaminas) data.selectedPotionDisplayName else "Disabled",
            labelGray, valueWhite, FONT_VALUE_BOLD, FONT_LABEL)

        curY += lineGap

        val newColor = if (data.canHop) valueGreen else Color.RED.rgb
        drawStatLine(c, innerX, innerWidth, paddingX, curY,
            "Can Hop", if (data.canHop) "YES" else "NO",
            labelGray, newColor, FONT_VALUE_BOLD, FONT_LABEL)

        if (!data.herbGains.isEmpty()) {
            drawHerbGains(c, data.herbGains)
        }
    }

    private fun formatRuntime(millis: Long): String {
        val seconds = millis / 1000
        val days = seconds / 86400
        val hours = seconds % 86400 / 3600
        val minutes = seconds % 3600 / 60
        val secs = seconds % 60
        return if (days > 0) {
            String.format("%dd %02d:%02d:%02d", days, hours, minutes, secs)
        } else {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        }
    }
    
    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val secs = seconds % 60
        return if (minutes > 0) {
            String.format("%dm %02ds", minutes, secs)
        } else {
            String.format("%ds", secs)
        }
    }

    private fun drawStatLine(
        c: Canvas, innerX: Int, innerWidth: Int, paddingX: Int, y: Int,
        label: String, value: String, labelColor: Int, valueColor: Int,
        labelFont: Font, valueFont: Font
    ) {
        c.drawText(label, innerX + paddingX, y, labelColor, labelFont)
        val valW = c.getFontMetrics(valueFont).stringWidth(value)
        val valX = innerX + innerWidth - paddingX - valW
        c.drawText(value, valX, y, valueColor, valueFont)
    }
    
    private fun drawHerbGains(c: Canvas, herbGains: HerbGains) {
        val x = 5
        val y = 540
        val width = 750  // Full width rectangle
        val height = 50  // Single line height
        val padding = 10
        
        // Draw translucent white background rectangle
        c.fillRect(x, y, width, height, Color.decode("#319608").rgb, 0.35)
        
        // Draw border
        c.drawRect(x, y, width, height, Color.WHITE.rgb)
        
        // Prepare herb gains text as two lines
        val firstLine = buildString {
            if (herbGains.ranarrs > 0) append("Ranarr: +${herbGains.ranarrs}, ")
            if (herbGains.toadflax > 0) append("Toadflax: +${herbGains.toadflax}, ")
            if (herbGains.avantoe > 0) append("Avantoe: +${herbGains.avantoe}, ")
            if (herbGains.kwuarm > 0) append("Kwuarm: +${herbGains.kwuarm}, ")
            if (herbGains.smallFossil > 0) append("Small Fossil: +${herbGains.smallFossil}, ")
            if (herbGains.mediumFossil > 0) append("Medium Fossil: +${herbGains.mediumFossil}, ")
            if (herbGains.largeFossil > 0) append("Large Fossil: +${herbGains.largeFossil}, ")
            if (herbGains.rareFossil > 0) append("Rare Fossil: +${herbGains.rareFossil}, ")
            if (herbGains.numulite > 0) append("Numulite: +${herbGains.numulite}, ")
        }

        val secondLine = buildString {
            if (herbGains.cadantine > 0) append("Cadantine: +${herbGains.cadantine}, ")
            if (herbGains.lantadyme > 0) append("Lantadyme: +${herbGains.lantadyme}, ")
            if (herbGains.dwarfweed > 0) append("Dwarfweed: +${herbGains.dwarfweed}, ")
            if (herbGains.torstol > 0) append("Torstol: +${herbGains.torstol}, ")
            if (herbGains.guam > 0) append("Guam: +${herbGains.guam}, ")
            if (herbGains.marrentill > 0) append("Marrentill: +${herbGains.marrentill}, ")
            if (herbGains.tarromin > 0) append("Tarromin: +${herbGains.tarromin}, ")
            if (herbGains.harralander > 0) append("Harralander: +${herbGains.harralander}")
        }

        
        // Draw two lines of text
        val font = Font("Arial", Font.BOLD, 12)
        val lineHeight = 16
        
        // First line
        c.drawText(firstLine.trim(), x + padding, y + padding + 12, Color.WHITE.rgb, font)
        
        // Second line
        c.drawText(secondLine.trim(), x + padding, y + padding + 12 + lineHeight, Color.WHITE.rgb, font)
    }
}
