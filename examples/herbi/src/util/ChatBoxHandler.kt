package util

import com.osmb.api.script.Script
import com.osmb.api.shape.Rectangle
import com.osmb.api.ui.chatbox.ChatboxFilterTab
import com.osmb.api.utils.CachedObject
import com.osmb.api.utils.UIResultList
import java.util.Locale


class ChatBoxHandler(
    private val script: Script,
    private val onChatboxEvent: (Event) -> Unit,
) {

    private var lastChatBoxChange: Long = 0
    private var lastChatBoxRead: Long = 0

    private fun cooldownActive(): Boolean {
        return System.currentTimeMillis() - lastChatBoxRead < 2000
    }

    companion object {
        private const val CAPTURED_MESSAGE = "you harvest herbs from the herbiboar"
        private const val ESCAPED_MESSAGE = "the creature has successfully confused you with its tracks"
    }

    sealed class Event {
        object HerbiCaptured : Event()
        object HerbiEscaped : Event()
    }

    private var currentChatboxLines: UIResultList<String>? = null

    private var previousChatboxLines: MutableList<String> = mutableListOf()

    fun updateChatBoxLines() = with(script) {
        if (widgetManager.chatbox.activeFilterTab !== ChatboxFilterTab.GAME || cooldownActive()) {
            return
        }
        val chatboxBounds: Rectangle = widgetManager.chatbox.bounds ?: return
        // check if minimenu overlaps chatbox
        val minimenuBounds: CachedObject<Rectangle?>? = widgetManager.miniMenu?.menuBounds
        if (minimenuBounds?.screenUUID != null && minimenuBounds.screenUUID == screen.uuid) {
            if (minimenuBounds.getObject()?.intersects(chatboxBounds) == true) {
                log("Minimenu intersects chatbox")
                return
            }
        }

        // check is we recently tapped over the chatbox (this causes issues with text reading and gives false positives for new lines)
        val chatboxBounds2: Rectangle = chatboxBounds.getPadding(0, 0, 12, 0)
        val lastTapMillis: Long = finger.lastTapMillis

        if (chatboxBounds2.contains(finger.lastTapX, finger.lastTapY) && System.currentTimeMillis() - lastTapMillis < 1500) {
            return
        }

        currentChatboxLines = widgetManager.chatbox.text
        if (currentChatboxLines?.isNotVisible == true) {
            log("Chatbox not visible")
            return
        }
        val currentLines: List<String> = currentChatboxLines?.asList() ?: emptyList()
        if (currentLines.isEmpty()) {
            return
        }
        val newLines: List<String> = getNewLines(currentLines, previousChatboxLines) ?: emptyList()
        previousChatboxLines.clear()
        previousChatboxLines.addAll(currentLines)
        if (newLines.isNotEmpty()) {
            onNewChatBoxMessage(newLines)
        }
    }

    private fun getNewLines(currentLines: List<String>, previousLines: MutableList<String>): List<String>? {
        lastChatBoxRead = System.currentTimeMillis()
        if (currentLines.isEmpty()) {
            return emptyList()
        }
        var firstDifference = 0
        if (previousLines.isNotEmpty()) {
            if (currentLines == previousLines) {
                return emptyList()
            }
            val currSize = currentLines.size
            val prevSize = previousLines.size
            for (i in 0 until currSize) {
                val suffixLen = currSize - i
                if (suffixLen > prevSize) continue
                var match = true
                for (j in 0 until suffixLen) {
                    if (currentLines[i + j] != previousLines[j]) {
                        match = false
                        break
                    }
                }
                if (match) {
                    firstDifference = i
                    break
                }
            }
        } else {
            previousLines.addAll(currentLines)
        }
        val newLines =
            if (firstDifference == 0) java.util.List.copyOf(currentLines) else currentLines.subList(0, firstDifference)
        lastChatBoxChange = System.currentTimeMillis()
        return newLines
    }

    private fun Script.onNewChatBoxMessage(newLines: List<String>) {
        for (line in newLines) {
            val lowerLine = line.lowercase(Locale.getDefault())
            log("New line: $lowerLine")
            when {
                lowerLine.contains(ESCAPED_MESSAGE) -> onChatboxEvent(Event.HerbiEscaped)
                lowerLine.contains(CAPTURED_MESSAGE) -> onChatboxEvent(Event.HerbiCaptured)
            }
        }
    }
}