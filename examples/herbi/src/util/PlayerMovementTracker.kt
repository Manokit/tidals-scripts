package util

import com.osmb.api.location.position.types.WorldPosition
import com.osmb.api.script.Script

class PlayerMovementTracker(private val script: Script) {
    private var currentPlayerLocation: WorldPosition? = null
    private var lastMovementTime: Long = System.currentTimeMillis()
    private val MOVEMENT_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes in milliseconds

    fun updateLocation() {
        val newLocation = script.worldPosition
        
        if (currentPlayerLocation != null && currentPlayerLocation != newLocation) {
            lastMovementTime = System.currentTimeMillis()
            script.log("Player moved from $currentPlayerLocation to $newLocation")
        }
        
        currentPlayerLocation = newLocation
        
        val timeSinceLastMovement = timeSinceMovement()
        
        if (timeSinceLastMovement >= MOVEMENT_TIMEOUT_MS) {
            script.log("Player has been stationary for ${timeSinceLastMovement / 1000} seconds. Stopping script.")
            script.stop()
        }
    }

    fun timeSinceMovement(): Long {
        return System.currentTimeMillis() - lastMovementTime
    }
    
    fun reset() {
        lastMovementTime = System.currentTimeMillis()
        currentPlayerLocation = script.worldPosition
    }
}