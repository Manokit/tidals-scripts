package utilities;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.utils.RandomUtils;

/**
 * Detects when player movement has stalled.
 *
 * Use this to prevent scripts getting stuck when:
 * - Misclicks occur during walking
 * - Player is interrupted while moving
 * - Interface doesn't open after interaction
 *
 * Usage:
 *   MovementChecker checker = new MovementChecker(script.getLocalPlayer().getWorldPosition());
 *   while (walking) {
 *       if (checker.hasTimedOut(script.getLocalPlayer().getWorldPosition())) {
 *           // player stopped moving, take corrective action
 *           break;
 *       }
 *   }
 */
public class MovementChecker {
    private WorldPosition initialPosition;
    private final int timeout;
    private long lastMovementTime;

    /**
     * Creates checker with randomized timeout between 800-2000ms.
     */
    public MovementChecker(WorldPosition initialPosition) {
        this.initialPosition = initialPosition;
        this.timeout = RandomUtils.gaussianRandom(800, 2000, 1400, 300);
        this.lastMovementTime = System.currentTimeMillis();
    }

    /**
     * Creates checker with custom timeout range.
     */
    public MovementChecker(WorldPosition initialPosition, int minTimeout, int maxTimeout) {
        this.initialPosition = initialPosition;
        this.timeout = RandomUtils.gaussianRandom(minTimeout, maxTimeout, (minTimeout + maxTimeout) / 2.0, (maxTimeout - minTimeout) / 4.0);
        this.lastMovementTime = System.currentTimeMillis();
    }

    /**
     * Checks if player has stopped moving for longer than timeout.
     * Automatically resets timer when movement is detected.
     *
     * @param currentPosition player's current position
     * @return true if no movement detected for timeout duration
     */
    public boolean hasTimedOut(WorldPosition currentPosition) {
        if (!currentPosition.equalsPrecisely(this.initialPosition)) {
            // player moved - reset timer
            lastMovementTime = System.currentTimeMillis();
            initialPosition = currentPosition;
            return false;
        }
        return System.currentTimeMillis() - lastMovementTime > timeout;
    }

    /**
     * Gets the randomized timeout value for this checker.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Resets the movement timer without requiring position change.
     * Useful when you know player will start moving soon.
     */
    public void reset(WorldPosition currentPosition) {
        this.initialPosition = currentPosition;
        this.lastMovementTime = System.currentTimeMillis();
    }
}
