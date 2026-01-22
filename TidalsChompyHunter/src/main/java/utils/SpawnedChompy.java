package utils;

import com.osmb.api.location.position.types.WorldPosition;

/**
 * tracks a detected chompy's position and spawn time
 */
public class SpawnedChompy {
    private final WorldPosition position;
    private final long spawnTime;

    // chompy considered lost if not engaged within 10 seconds
    private static final long STALE_TIMEOUT_MS = 10000;

    public SpawnedChompy(WorldPosition pos) {
        this.position = pos;
        this.spawnTime = System.currentTimeMillis();
    }

    public WorldPosition getPosition() {
        return position;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public long getAge() {
        return System.currentTimeMillis() - spawnTime;
    }

    /**
     * check if this chompy is stale (not engaged within timeout)
     */
    public boolean isStale() {
        return getAge() > STALE_TIMEOUT_MS;
    }
}
