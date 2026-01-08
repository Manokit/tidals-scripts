package utils;

import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static main.TidalsCannonballThiever.twoStallMode;
import static main.TidalsCannonballThiever.lastXpGain;

public class GuardTracker {

    private final Script script;
    private final Random random = new Random();

    // single stall mode
    private static final int EARLY_WARNING_X = 1865;
    private static final int PATROL_Y = 3295;

    private static final int DANGER_X_1 = 1866;
    private static final int DANGER_X_2 = 1867;

    // two stall mode
    private static final int ORE_STALL_DANGER_Y = 3292;
    private static final int ORE_STALL_X = 1863;

    // early warning delay params
    private static final double DELAY_MIN_SEC = 2.5;
    private static final double DELAY_MAX_SEC = 3.5;
    private static final double DELAY_MEAN_SEC = 3.2;
    private static final double DELAY_STD_DEV = 0.25;

    private long earlyWarningStartTime = 0;
    private long currentDelayMs = 0;

    private List<WorldPosition> lastNpcPositions = new ArrayList<>();

    // highlight-based pixel detection (cyan guard highlight)
    private static final SearchablePixel GUARD_HIGHLIGHT = new SearchablePixel(
            0x00FFFF,
            new SingleThresholdComparator(15),
            ColorModel.HSL
    );
    
    private static final int MOVEMENT_THRESHOLD = 20;
    
    // watch tiles
    private static final int CB_WATCH_X = 1865;
    private static final int CB_WATCH_Y = 3295;
    private static final int ORE_WATCH_X = 1863;
    private static final int ORE_WATCH_Y = 3292;
    
    private boolean watchingAtCBTile = false;
    private boolean watchingAtOreTile = false;
    private Point cbWatchStartCenter = null;
    private Point oreWatchStartCenter = null;
    private long cbWatchStartTime = 0;
    private long oreWatchStartTime = 0;
    private Point lastGuardCenter = null;
    private long lastGuardCheckTime = 0;
    private boolean guardMovingEast = false;
    
    private int oreThiefCount = 0;
    private static final int MAX_ORE_THIEVES = 2;
    
    private long arrivedAtOreStallTime = 0;
    private boolean gotOreStallXpDrop = false;
    private long arrivedAtCBStallTime = 0;
    private boolean gotCBStallXpDrop = false;
    
    // xp-based cycle tracking (4 cb -> 2 ore -> repeat)
    private int cbXpDropCount = 0;
    private int oreXpDropCount = 0;
    private static final int CB_THIEVES_PER_CYCLE = 4;
    private static final int ORE_THIEVES_PER_CYCLE = 2;
    private double lastKnownXpForCycle = -1;
    
    private long lastXpBasedSwitchTime = 0;
    private static final long XP_SWITCH_COOLDOWN_MS = 5000;
    
    private static final int CB_STALL_PLAYER_X = 1867;
    private static final int CB_STALL_PLAYER_Y = 3295;

    public GuardTracker(Script script) {
        this.script = script;
    }

    private long generateRandomDelay() {
        double delay = DELAY_MEAN_SEC + random.nextGaussian() * DELAY_STD_DEV;
        delay = Math.max(DELAY_MIN_SEC, Math.min(DELAY_MAX_SEC, delay));
        return (long) (delay * 1000);
    }

    public List<WorldPosition> findAllNPCPositions() {
        List<WorldPosition> npcPositions = new ArrayList<>();

        UIResultList<WorldPosition> npcResult = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcResult == null || !npcResult.isFound()) {
            return npcPositions;
        }

        npcPositions = new ArrayList<>(npcResult.asList());
        lastNpcPositions = npcPositions;

        return npcPositions;
    }

    public boolean isAnyGuardInDangerZone() {
        List<WorldPosition> npcPositions = findAllNPCPositions();

        boolean guardAtEarlyWarning = false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (y != PATROL_Y) continue;

            if (x == DANGER_X_1 || x == DANGER_X_2) {
                script.log("GUARD", "IMMEDIATE DANGER! NPC at x=" + x);
                earlyWarningStartTime = 0;
                return true;
            }

            if (x == EARLY_WARNING_X) {
                guardAtEarlyWarning = true;
            }
        }

        if (guardAtEarlyWarning) {
            if (earlyWarningStartTime == 0) {
                earlyWarningStartTime = System.currentTimeMillis();
                currentDelayMs = generateRandomDelay();
                double delaySec = currentDelayMs / 1000.0;
                script.log("GUARD", String.format("Early warning - guard at 1865, waiting %.2fs before retreat", delaySec));
            }

            long elapsed = System.currentTimeMillis() - earlyWarningStartTime;
            if (elapsed >= currentDelayMs) {
                double actualSec = elapsed / 1000.0;
                script.log("GUARD", String.format("Early warning expired after %.2fs - retreating!", actualSec));
                return true;
            }
        } else {
            if (earlyWarningStartTime != 0) {
                earlyWarningStartTime = 0;
                currentDelayMs = 0;
            }
        }

        return false;
    }

    public boolean isSafeToReturn() {
        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (y == PATROL_Y && x >= 1864 && x <= DANGER_X_2) {
                script.log("GUARD", "Not safe yet - NPC at x=" + x + " in patrol zone");
                return false;
            }
        }

        earlyWarningStartTime = 0;
        currentDelayMs = 0;

        script.log("GUARD", "Safe to return - patrol zone clear");
        return true;
    }

    public List<WorldPosition> getLastNpcPositions() {
        return lastNpcPositions;
    }

    private long twoStallEarlyWarningStart = 0;
    private long twoStallCurrentDelay = 0;

    public void updateGlobalGuardTimer() {
        if (!twoStallMode) return;

        List<WorldPosition> npcPositions = findAllNPCPositions();
        boolean guardAtEarlyWarning = false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (y == PATROL_Y && x == EARLY_WARNING_X) {
                guardAtEarlyWarning = true;
            }
        }

        if (guardAtEarlyWarning) {
            if (twoStallEarlyWarningStart == 0) {
                twoStallEarlyWarningStart = System.currentTimeMillis();
                twoStallCurrentDelay = generateRandomDelay();
                double delaySec = twoStallCurrentDelay / 1000.0;
                script.log("GUARD", String.format("GLOBAL: Guard at 1865, timer started (%.2fs)", delaySec));
            }
        } else {
            if (twoStallEarlyWarningStart != 0) {
                script.log("GUARD", "GLOBAL: Guard moved from 1865, timer reset");
                twoStallEarlyWarningStart = 0;
                twoStallCurrentDelay = 0;
            }
        }
    }

    public boolean isGuardNearCannonballStall() {
        if (!twoStallMode) return false;

        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (y != PATROL_Y) continue;

            if (x >= EARLY_WARNING_X && x <= DANGER_X_2) {
                script.log("GUARD", "Guard at x=" + x + " - SWITCH TO ORE NOW!");
                twoStallEarlyWarningStart = 0;
                twoStallCurrentDelay = 0;
                return true;
            }
        }

        return false;
    }

    public boolean isCannonballStallSafe() {
        if (!twoStallMode) return isSafeToReturn();

        List<WorldPosition> npcPositions = findAllNPCPositions();
        
        boolean foundGuardOnPatrol = false;
        boolean guardPastStall = false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (y != PATROL_Y) continue;
            foundGuardOnPatrol = true;

            if (x >= EARLY_WARNING_X && x <= 1867) {
                script.log("GUARD", "Cannonball NOT safe - guard at x=" + x);
                return false;
            }
            
            if (x >= 1868) {
                guardPastStall = true;
            }
        }

        if (guardPastStall) {
            twoStallEarlyWarningStart = 0;
            twoStallCurrentDelay = 0;
            script.log("GUARD", "Cannonball SAFE - guard at x>=1868!");
            return true;
        }
        
        return false;
    }

    public boolean isGuardNearOreStall() {
        if (!twoStallMode) return false;

        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (x == ORE_STALL_X && y == ORE_STALL_DANGER_Y) {
                script.log("GUARD", "Guard near ore stall at (" + x + ", " + y + ")");
                return true;
            }
        }
        return false;
    }

    private WorldPosition findNPCAtTile(int targetX, int targetY) {
        UIResultList<WorldPosition> npcPositions = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcPositions == null || !npcPositions.isFound()) return null;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (x == targetX && y == targetY) {
                return npcPos;
            }
        }
        return null;
    }

    private void logAllNPCPositions() {
        UIResultList<WorldPosition> npcPositions = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcPositions == null || !npcPositions.isFound()) {
            script.log("GUARD-DEBUG", "No NPCs visible on minimap");
            return;
        }
        
        StringBuilder sb = new StringBuilder("All NPCs: ");
        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null) continue;
            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();
            int plane = npcPos.getPlane();
            sb.append("(").append(x).append(",").append(y).append(",").append(plane).append(") ");
        }
        script.log("GUARD-DEBUG", sb.toString());
    }

    private Point getGuardHighlightCenter(WorldPosition guardPos) {
        if (guardPos == null) return null;

        Polygon tileCube = script.getSceneProjector().getTileCube(guardPos, 100);
        if (tileCube == null) return null;

        Rectangle bounds = script.getPixelAnalyzer().getHighlightBounds(tileCube, GUARD_HIGHLIGHT);
        if (bounds == null) return null;

        return bounds.getCenter();
    }

    public boolean shouldSwitchToOre() {
        if (!twoStallMode) return false;

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;
        
        int playerX = (int) myPos.getX();
        int playerY = (int) myPos.getY();
        
        if (playerX != CB_STALL_PLAYER_X || playerY != CB_STALL_PLAYER_Y) {
            if (watchingAtCBTile || arrivedAtCBStallTime != 0) {
                script.log("GUARD-PIXEL", "Not at CB stall yet - resetting watch state");
                watchingAtCBTile = false;
                cbWatchStartCenter = null;
                cbWatchStartTime = 0;
                arrivedAtCBStallTime = 0;
                gotCBStallXpDrop = false;
            }
            return false;
        }
        
        if (arrivedAtCBStallTime == 0) {
            arrivedAtCBStallTime = System.currentTimeMillis();
            gotCBStallXpDrop = false;
            script.log("GUARD-PIXEL", "Arrived at CB stall - waiting for XP drop before watching guard...");
            return false;
        }
        
        if (!gotCBStallXpDrop) {
            if (lastXpGain.timeElapsed() < (System.currentTimeMillis() - arrivedAtCBStallTime)) {
                gotCBStallXpDrop = true;
                script.log("GUARD-PIXEL", "Got XP drop at CB stall - NOW can start watching guard!");
            } else {
                return false;
            }
        }

        WorldPosition guardPos = findNPCAtTile(CB_WATCH_X, CB_WATCH_Y);

        if (guardPos != null) {
            Point currentCenter = getGuardHighlightCenter(guardPos);

            if (currentCenter == null) return false;

            if (!watchingAtCBTile) {
                watchingAtCBTile = true;
                cbWatchStartTime = System.currentTimeMillis();
                cbWatchStartCenter = null;
                script.log("GUARD-PIXEL", "Guard at CB watch tile (1865,3295) - waiting for settle...");
                return false;
            }

            long elapsed = System.currentTimeMillis() - cbWatchStartTime;
            if (elapsed < 300) return false;

            if (cbWatchStartCenter == null) {
                cbWatchStartCenter = currentCenter;
                script.log("GUARD-PIXEL", "Guard settled at CB tile - NOW watching for movement...");
                return false;
            }

            int dx = Math.abs(currentCenter.x - cbWatchStartCenter.x);
            int dy = Math.abs(currentCenter.y - cbWatchStartCenter.y);

            if (dx > MOVEMENT_THRESHOLD || dy > MOVEMENT_THRESHOLD) {
                script.log("GUARD-PIXEL", "GUARD MOVED! dx=" + dx + ", dy=" + dy + " pixels - SWITCH TO ORE!");
                return true;
            }

            return false;
        } else {
            if (watchingAtCBTile) {
                script.log("GUARD-PIXEL", "Guard left CB watch tile - stopping watch");
                watchingAtCBTile = false;
                cbWatchStartCenter = null;
                cbWatchStartTime = 0;
            }
            return false;
        }
    }

    private static final int ORE_STALL_PLAYER_X = 1863;
    private static final int ORE_STALL_PLAYER_Y = 3295;

    public boolean shouldSwitchToCannonball() {
        if (!twoStallMode) return false;

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;
        
        int playerX = (int) myPos.getX();
        int playerY = (int) myPos.getY();
        
        if (playerX != ORE_STALL_PLAYER_X || playerY != ORE_STALL_PLAYER_Y) {
            if (watchingAtOreTile || arrivedAtOreStallTime != 0) {
                script.log("GUARD-PIXEL", "Not at ore stall yet - resetting watch state");
                watchingAtOreTile = false;
                oreWatchStartCenter = null;
                oreWatchStartTime = 0;
                arrivedAtOreStallTime = 0;
                gotOreStallXpDrop = false;
            }
            return false;
        }
        
        if (arrivedAtOreStallTime == 0) {
            arrivedAtOreStallTime = System.currentTimeMillis();
            gotOreStallXpDrop = false;
            script.log("GUARD-PIXEL", "Arrived at ore stall - waiting for XP drop before watching guard...");
            return false;
        }
        
        if (!gotOreStallXpDrop) {
            if (lastXpGain.timeElapsed() < (System.currentTimeMillis() - arrivedAtOreStallTime)) {
                gotOreStallXpDrop = true;
                script.log("GUARD-PIXEL", "Got XP drop at ore stall - NOW can start watching guard!");
                logAllNPCPositions();
            } else {
                return false;
            }
        }

        WorldPosition guardPos = findNPCAtTile(ORE_WATCH_X, ORE_WATCH_Y);

        if (guardPos != null) {
            int guardX = (int) guardPos.getX();
            int guardY = (int) guardPos.getY();
            Point currentCenter = getGuardHighlightCenter(guardPos);

            if (currentCenter == null) return false;

            if (!watchingAtOreTile) {
                watchingAtOreTile = true;
                oreWatchStartTime = System.currentTimeMillis();
                oreWatchStartCenter = null;
                script.log("GUARD-PIXEL", "NPC at tile (" + guardX + "," + guardY + ") - waiting for settle...");
                return false;
            }

            long elapsed = System.currentTimeMillis() - oreWatchStartTime;
            if (elapsed < 300) return false;

            if (oreWatchStartCenter == null) {
                oreWatchStartCenter = currentCenter;
                script.log("GUARD-PIXEL", "NPC at (" + guardX + "," + guardY + ") settled");
                return false;
            }

            int dx = Math.abs(currentCenter.x - oreWatchStartCenter.x);
            int dy = Math.abs(currentCenter.y - oreWatchStartCenter.y);

            if (dx > MOVEMENT_THRESHOLD || dy > MOVEMENT_THRESHOLD) {
                script.log("GUARD-PIXEL", "NPC MOVED! dx=" + dx + " dy=" + dy);
                return true;
            }

            return false;
        } else {
            if (watchingAtOreTile) {
                script.log("GUARD-PIXEL", "Guard left ore watch tile");
                watchingAtOreTile = false;
                oreWatchStartCenter = null;
                oreWatchStartTime = 0;
            }
            return isCannonballStallSafe();
        }
    }

    public boolean isWatchingAtCBTile() {
        return watchingAtCBTile;
    }

    public boolean isWatchingAtOreTile() {
        return watchingAtOreTile;
    }

    public boolean isGuardPastWatchTile() {
        if (!twoStallMode) return false;

        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (y != PATROL_Y) continue;

            if (x == 1866 || x == 1867) {
                script.log("GUARD", "Guard PAST watch tile at x=" + x + " - SWITCH NOW!");
                return true;
            }
        }
        return false;
    }

    public boolean isGuardPastOreWatchTile() {
        if (!twoStallMode) return false;

        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (x == ORE_STALL_X && y == 3293) {
                script.log("GUARD", "Guard PAST ore watch tile - heading to ore stall!");
                return true;
            }
        }
        return false;
    }

    public Point updateGuardHighlightPosition() {
        if (!twoStallMode) return null;

        UIResultList<WorldPosition> npcPositions = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcPositions == null || !npcPositions.isFound()) return null;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (y < 3291 || y > 3297) continue;
            if (x < 1863 || x > 1872) continue;

            Point currentCenter = getGuardHighlightCenter(npcPos);
            if (currentCenter == null) continue;

            if (lastGuardCenter != null) {
                int dx = currentCenter.x - lastGuardCenter.x;
                if (Math.abs(dx) > MOVEMENT_THRESHOLD) {
                    guardMovingEast = dx > 0;
                }
            }

            lastGuardCenter = currentCenter;
            lastGuardCheckTime = System.currentTimeMillis();
            return currentCenter;
        }

        return null;
    }

    public boolean hasGuardStartedMoving() {
        if (!twoStallMode) return false;
        if (lastGuardCenter == null) return false;

        Point previousCenter = lastGuardCenter;
        Point currentCenter = updateGuardHighlightPosition();

        if (currentCenter == null || previousCenter == null) return false;

        int dx = Math.abs(currentCenter.x - previousCenter.x);
        int dy = Math.abs(currentCenter.y - previousCenter.y);

        if (dx > MOVEMENT_THRESHOLD || dy > MOVEMENT_THRESHOLD) {
            script.log("GUARD-PIXEL", "Guard MOVED! Distance: " + dx + "x, " + dy + "y pixels");
            return true;
        }

        return false;
    }

    public boolean isGuardMovingAwayFromCannonball() {
        if (!twoStallMode) return false;
        updateGuardHighlightPosition();
        return guardMovingEast;
    }

    public boolean isGuardMovingTowardCannonball() {
        if (!twoStallMode) return false;
        updateGuardHighlightPosition();
        return !guardMovingEast && lastGuardCenter != null;
    }

    public void resetOreThiefCount() {
        oreThiefCount = 0;
    }

    public void incrementOreThiefCount() {
        oreThiefCount++;
        script.log("GUARD", "Ore thieve #" + oreThiefCount + " of " + MAX_ORE_THIEVES);
    }

    public boolean canDoMoreOreThieves() {
        return oreThiefCount < MAX_ORE_THIEVES;
    }

    public int getOreThiefCount() {
        return oreThiefCount;
    }

    public void resetGuardTracking() {
        lastGuardCenter = null;
        guardMovingEast = false;
        lastGuardCheckTime = 0;
        watchingAtCBTile = false;
        watchingAtOreTile = false;
        cbWatchStartCenter = null;
        oreWatchStartCenter = null;
        cbWatchStartTime = 0;
        oreWatchStartTime = 0;
        arrivedAtOreStallTime = 0;
        gotOreStallXpDrop = false;
        arrivedAtCBStallTime = 0;
        gotCBStallXpDrop = false;
    }

    public boolean checkCbXpDrop(double currentXp) {
        if (lastKnownXpForCycle < 0) {
            lastKnownXpForCycle = currentXp;
            script.log("CYCLE", "CB XP tracking auto-initialized with: " + currentXp);
            return false;
        }
        
        if (currentXp > lastKnownXpForCycle) {
            double xpGained = currentXp - lastKnownXpForCycle;
            lastKnownXpForCycle = currentXp;
            cbXpDropCount++;
            script.log("CYCLE", "CB steal #" + cbXpDropCount + "/" + CB_THIEVES_PER_CYCLE + " (+" + String.format("%.0f", xpGained) + " XP)");
            return true;
        }
        return false;
    }
    
    public boolean checkOreXpDrop(double currentXp) {
        if (lastKnownXpForCycle < 0) {
            lastKnownXpForCycle = currentXp;
            return false;
        }
        
        if (currentXp > lastKnownXpForCycle) {
            lastKnownXpForCycle = currentXp;
            oreXpDropCount++;
            script.log("CYCLE", "Extra stall (Ore) " + oreXpDropCount + "/" + ORE_THIEVES_PER_CYCLE);
            return true;
        }
        return false;
    }
    
    public boolean shouldSwitchToOreByXp() {
        return cbXpDropCount >= CB_THIEVES_PER_CYCLE;
    }
    
    public boolean shouldSwitchToCbByXp() {
        return oreXpDropCount >= ORE_THIEVES_PER_CYCLE;
    }
    
    public void resetCbCycle() {
        cbXpDropCount = 0;
        script.log("CYCLE", "CB cycle reset - starting fresh");
    }
    
    public void resetOreCycle() {
        oreXpDropCount = 0;
        script.log("CYCLE", "Ore cycle reset - starting fresh");
    }
    
    public int getCbXpDropCount() {
        return cbXpDropCount;
    }
    
    public int getOreXpDropCount() {
        return oreXpDropCount;
    }
    
    public void initXpTracking(double currentXp) {
        if (currentXp >= 0) {
            lastKnownXpForCycle = currentXp;
            script.log("GUARD", "XP cycle tracking initialized with baseline: " + currentXp);
        } else {
            script.log("GUARD", "WARNING: Invalid XP value for initialization: " + currentXp);
        }
    }
    
    public boolean isXpTrackingInitialized() {
        return lastKnownXpForCycle >= 0;
    }
    
    public void markXpBasedSwitch() {
        lastXpBasedSwitchTime = System.currentTimeMillis();
        script.log("CYCLE", "XP-based switch - guard backup disabled for " + (XP_SWITCH_COOLDOWN_MS/1000) + "s");
    }
    
    public boolean isInXpSwitchCooldown() {
        if (lastXpBasedSwitchTime == 0) return false;
        long elapsed = System.currentTimeMillis() - lastXpBasedSwitchTime;
        return elapsed < XP_SWITCH_COOLDOWN_MS;
    }
    
    public void clearXpSwitchCooldown() {
        lastXpBasedSwitchTime = 0;
    }
}
