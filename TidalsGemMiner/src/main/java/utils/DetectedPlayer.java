package utils;

import com.osmb.api.location.position.types.WorldPosition;

import java.util.Objects;

/**
 * tracks a detected player's position and first-seen time
 * used by DetectPlayers task to determine if player is crashing vs passing through
 */
public class DetectedPlayer {
    private final WorldPosition position;
    private final long firstSeenTime;

    public DetectedPlayer(WorldPosition pos) {
        this.position = Objects.requireNonNull(pos, "position required for player tracking");
        this.firstSeenTime = System.currentTimeMillis();
    }

    public WorldPosition getPosition() {
        return position;
    }

    public long getFirstSeenTime() {
        return firstSeenTime;
    }

    /**
     * get how long this player has been tracked (milliseconds)
     */
    public long getDuration() {
        return System.currentTimeMillis() - firstSeenTime;
    }

    /**
     * check if this player has lingered long enough to be considered crashing
     * threshold is configurable via DetectPlayers task
     */
    public boolean isCrashing(long thresholdMs) {
        return getDuration() > thresholdMs;
    }
}
