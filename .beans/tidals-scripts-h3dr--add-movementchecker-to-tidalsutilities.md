---
# tidals-scripts-h3dr
title: Add MovementChecker to TidalsUtilities
status: completed
type: task
priority: normal
created_at: 2026-01-28T07:09:58Z
updated_at: 2026-01-28T07:40:02Z
parent: tidals-scripts-8cpu
---

# Add MovementChecker Utility Class

Create a reusable MovementChecker class for detecting when player has stopped moving.

## Purpose

Prevents scripts from getting stuck when:
- Misclicks occur during walking
- Player is interrupted while moving
- Interface doesn't open after interaction

## Implementation

```java
package utils;

import com.osmb.api.position.WorldPosition;
import com.osmb.api.utils.RandomUtils;

public class MovementChecker {
    private final long timeout;
    private WorldPosition initialPosition;
    private long lastMovementTime;

    public MovementChecker(WorldPosition initialPosition) {
        this.initialPosition = initialPosition;
        this.timeout = RandomUtils.uniformRandom(800, 2000);
        this.lastMovementTime = System.currentTimeMillis();
    }
    
    public MovementChecker(WorldPosition initialPosition, long minTimeout, long maxTimeout) {
        this.initialPosition = initialPosition;
        this.timeout = RandomUtils.uniformRandom(minTimeout, maxTimeout);
        this.lastMovementTime = System.currentTimeMillis();
    }

    public boolean hasTimedOut(WorldPosition currentPosition) {
        if (!currentPosition.equalsPrecisely(this.initialPosition)) {
            lastMovementTime = System.currentTimeMillis();
            initialPosition = currentPosition;
            return false;
        }
        return System.currentTimeMillis() - lastMovementTime > timeout;
    }
}
```

## Checklist

- [x] Add MovementChecker.java to utilities/src/main/java/utilities/
- [x] Rebuild utilities jar
- [x] Add usage example to docs (CLAUDE.md)