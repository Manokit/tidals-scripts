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
import java.util.Objects;
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

    // movement detection thresholds
    private static final int MOVEMENT_THRESHOLD = 4; // lowered from 10 for instant detection
    private static final int VELOCITY_THRESHOLD = 2; // consecutive frames with rightward movement triggers switch
    private static final int SETTLE_TIME_MS = 0; // immediate detection - no settle delay

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

    // velocity tracking for instant detection
    private Point cbLastFrameCenter = null;
    private int consecutiveRightwardFrames = 0;

    // logging state to prevent spam
    private boolean lastCbSafeState = true; // only log on state change

    // oreThiefCount removed - now using XP-based tracking (oreXpDropCount) only
    
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

    // flag to prevent double-counting when assume + tracker both fire (CB only)
    private boolean firstCbDropAssumed = false;
    private long cbAssumeTimestamp = 0;
    private static final long ASSUME_WINDOW_MS = 1500; // only skip if within 1.5 sec of assume
    
    private long lastXpBasedSwitchTime = 0;
    private static final long XP_SWITCH_COOLDOWN_MS = 5000;

    // preemptive switch timer: detect guard at 1865 early and switch if low theft count
    private long guardAt1865StartTime = 0;
    private long preemptiveSwitchDelayMs = 0;
    private static final int PREEMPTIVE_MIN_TICKS = 5;
    private static final int PREEMPTIVE_MAX_TICKS = 8;
    private static final int MS_PER_TICK = 600;

    // guard sync: after reset, wait to see guard leave CB stall before starting
    private boolean needsGuardSync = false;
    private boolean sawGuardAtCbStall = false;

    private static final int CB_STALL_PLAYER_X = 1867;
    private static final int CB_STALL_PLAYER_Y = 3295;

    public GuardTracker(Script script) {
        this.script = Objects.requireNonNull(script, "script required");
    }

    private long generateRandomDelay() {
        double delay = DELAY_MEAN_SEC + random.nextGaussian() * DELAY_STD_DEV;
        delay = Math.max(DELAY_MIN_SEC, Math.min(DELAY_MAX_SEC, delay));
        return (long) (delay * 1000);
    }

    private long generatePreemptiveSwitchDelay() {
        int ticks = PREEMPTIVE_MIN_TICKS + random.nextInt(PREEMPTIVE_MAX_TICKS - PREEMPTIVE_MIN_TICKS + 1);
        return ticks * MS_PER_TICK;
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

        boolean guardPastStall = false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (y != PATROL_Y) continue;

            if (x >= EARLY_WARNING_X && x <= 1867) {
                // only log on state change to avoid spam
                if (lastCbSafeState) {
                    script.log("GUARD", "Cannonball NOT safe - guard at x=" + x);
                    lastCbSafeState = false;
                }
                return false;
            }

            if (x >= 1868) {
                guardPastStall = true;
            }
        }

        if (guardPastStall) {
            twoStallEarlyWarningStart = 0;
            twoStallCurrentDelay = 0;
            // only log on state change
            if (!lastCbSafeState) {
                script.log("GUARD", "Cannonball SAFE - guard at x>=1868!");
                lastCbSafeState = true;
            }
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

        // reset if not at CB stall
        if (playerX != CB_STALL_PLAYER_X || playerY != CB_STALL_PLAYER_Y) {
            if (watchingAtCBTile) {
                watchingAtCBTile = false;
                cbWatchStartCenter = null;
                cbWatchStartTime = 0;
                cbLastFrameCenter = null;
                consecutiveRightwardFrames = 0;
            }
            return false;
        }

        // find guard at watch tile
        WorldPosition guardPos = findNPCAtTile(CB_WATCH_X, CB_WATCH_Y);

        if (guardPos != null) {
            Point currentCenter = getGuardHighlightCenter(guardPos);
            if (currentCenter == null) return false;

            // first time seeing guard at watch tile - capture baseline IMMEDIATELY
            if (!watchingAtCBTile || cbWatchStartCenter == null) {
                watchingAtCBTile = true;
                cbWatchStartTime = System.currentTimeMillis();
                cbWatchStartCenter = currentCenter;
                cbLastFrameCenter = currentCenter;
                consecutiveRightwardFrames = 0;
                script.log("GUARD-PIXEL", "Guard at watch tile! Baseline x=" + currentCenter.x + " - watching for rightward movement...");
                return false;
            }

            // check total displacement from baseline
            int dxFromBaseline = currentCenter.x - cbWatchStartCenter.x;

            // instant detection: total displacement exceeds threshold
            if (dxFromBaseline > MOVEMENT_THRESHOLD) {
                script.log("GUARD-PIXEL", "GUARD MOVING RIGHT! dx=" + dxFromBaseline + "px - SWITCH NOW!");
                return true;
            }

            // velocity detection: track frame-to-frame movement
            if (cbLastFrameCenter != null) {
                int frameDx = currentCenter.x - cbLastFrameCenter.x;

                if (frameDx > 0) {
                    // guard moved right this frame
                    consecutiveRightwardFrames++;
                    if (consecutiveRightwardFrames >= VELOCITY_THRESHOLD) {
                        script.log("GUARD-PIXEL", "GUARD VELOCITY DETECTED! " + consecutiveRightwardFrames + " frames right - SWITCH NOW!");
                        return true;
                    }
                } else if (frameDx < 0) {
                    // guard moved left - reset velocity counter
                    consecutiveRightwardFrames = 0;
                }
                // frameDx == 0 means no change, keep counter as-is
            }
            cbLastFrameCenter = currentCenter;

            // if guard moved left significantly from baseline, update baseline
            if (dxFromBaseline < -MOVEMENT_THRESHOLD) {
                cbWatchStartCenter = currentCenter;
                consecutiveRightwardFrames = 0;
            }

            return false;
        } else {
            // guard not at watch tile
            if (watchingAtCBTile) {
                watchingAtCBTile = false;
                cbWatchStartCenter = null;
                cbWatchStartTime = 0;
                cbLastFrameCenter = null;
                consecutiveRightwardFrames = 0;
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
                // logAllNPCPositions();  // commented out to reduce log clutter
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
                script.log("GUARD-PIXEL", "NPC at tile (" + guardX + "," + guardY + ") - watching for movement...");
                return false;
            }

            long elapsed = System.currentTimeMillis() - oreWatchStartTime;
            if (elapsed < SETTLE_TIME_MS) return false;

            if (oreWatchStartCenter == null) {
                oreWatchStartCenter = currentCenter;
                script.log("GUARD-PIXEL", "NPC baseline captured at (" + guardX + "," + guardY + ")");
                return false;
            }

            // guard still at ore watch tile - don't switch yet
            // the guard walks EAST from here towards CB, so any movement here is dangerous
            // only switch when guard has LEFT this tile (handled in else branch below)
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

    /**
     * checks if we should preemptively switch to ore due to low theft count
     * and guard position. only triggers when:
     * 1. guard is at (1865, 3295)
     * 2. cbXpDropCount is 0, 1, or 2
     * 3. timer has elapsed (5-8 ticks / 3000-4800ms)
     */
    public boolean shouldPreemptiveSwitchToOre() {
        if (!twoStallMode) return false;

        // only applies to low theft counts (0-2)
        if (cbXpDropCount > 2) return false;

        // check if guard is at early warning position (1865, 3295)
        List<WorldPosition> npcPositions = findAllNPCPositions();
        boolean guardAt1865 = false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (x == EARLY_WARNING_X && y == PATROL_Y) {
                guardAt1865 = true;
                break;
            }
        }

        if (guardAt1865) {
            // start timer if not already running
            if (guardAt1865StartTime == 0) {
                guardAt1865StartTime = System.currentTimeMillis();
                preemptiveSwitchDelayMs = generatePreemptiveSwitchDelay();
                double delaySec = preemptiveSwitchDelayMs / 1000.0;
                script.log("GUARD", String.format("Guard at 1865 with low count (%d/4) - preemptive timer started (%.1fs)",
                        cbXpDropCount, delaySec));
            }

            // check if timer has elapsed
            long elapsed = System.currentTimeMillis() - guardAt1865StartTime;
            if (elapsed >= preemptiveSwitchDelayMs) {
                script.log("GUARD", String.format("Preemptive switch triggered! Only %d/4 thefts, guard at 1865 for %.1fs",
                        cbXpDropCount, elapsed / 1000.0));
                return true;
            }
        } else {
            // guard not at 1865 - reset timer
            if (guardAt1865StartTime != 0) {
                script.log("GUARD", "Guard left 1865 - preemptive timer reset");
                guardAt1865StartTime = 0;
                preemptiveSwitchDelayMs = 0;
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

    // old oreThiefCount methods removed - now using XP-based tracking only

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
        // reset velocity tracking
        cbLastFrameCenter = null;
        consecutiveRightwardFrames = 0;
        // reset logging state
        lastCbSafeState = true;
        // reset preemptive switch timer
        guardAt1865StartTime = 0;
        preemptiveSwitchDelayMs = 0;
    }

    public boolean checkCbXpDrop(double currentXp) {
        if (lastKnownXpForCycle < 0) {
            lastKnownXpForCycle = currentXp;
            script.log("CYCLE", "CB XP tracking auto-initialized with: " + currentXp);
            return false;
        }

        // handle tracker reset on level-up: if XP suddenly dropped, re-baseline
        if (currentXp < lastKnownXpForCycle) {
            script.log("CYCLE", "Tracker reset detected (level up?) - re-baselining from " + lastKnownXpForCycle + " to " + currentXp);
            lastKnownXpForCycle = currentXp;
            return false;
        }

        if (currentXp > lastKnownXpForCycle) {
            double xpGained = currentXp - lastKnownXpForCycle;
            lastKnownXpForCycle = currentXp;

            // if we assumed the first drop and tracker also caught it within the window, skip incrementing
            if (firstCbDropAssumed && cbXpDropCount == 1) {
                long elapsed = System.currentTimeMillis() - cbAssumeTimestamp;
                firstCbDropAssumed = false;  // clear flag either way
                if (elapsed < ASSUME_WINDOW_MS) {
                    script.log("CYCLE", "CB steal confirmed (already assumed 1/" + CB_THIEVES_PER_CYCLE + ")");
                    return true;
                }
                // outside window - this is a new steal, count it
                script.log("CYCLE", "XP drop outside assume window (" + elapsed + "ms) - counting as new steal");
            }

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

        // handle tracker reset on level-up: if XP suddenly dropped, re-baseline
        if (currentXp < lastKnownXpForCycle) {
            script.log("CYCLE", "Tracker reset detected (level up?) - re-baselining from " + lastKnownXpForCycle + " to " + currentXp);
            lastKnownXpForCycle = currentXp;
            return false;
        }

        if (currentXp > lastKnownXpForCycle) {
            lastKnownXpForCycle = currentXp;
            oreXpDropCount++;
            script.log("CYCLE", "Ore steal #" + oreXpDropCount + "/" + ORE_THIEVES_PER_CYCLE);
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
        firstCbDropAssumed = false;
        script.log("CYCLE", "CB cycle reset - starting fresh");
    }

    // used when assuming first xp drop occurred (tracker might not catch it)
    public void assumeFirstCbDrop() {
        // only assume if tracker hasn't already caught it
        if (cbXpDropCount > 0) {
            script.log("CYCLE", "Skip assume - tracker already caught first CB steal (" + cbXpDropCount + "/" + CB_THIEVES_PER_CYCLE + ")");
            return;
        }
        cbXpDropCount = 1;
        firstCbDropAssumed = true;
        cbAssumeTimestamp = System.currentTimeMillis();
        script.log("CYCLE", "Assumed first CB steal (1/" + CB_THIEVES_PER_CYCLE + ")");
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

    // guard sync: wait to see guard leave CB stall (1867 → 1868+) before starting
    public void enableGuardSync() {
        needsGuardSync = true;
        sawGuardAtCbStall = false;
        script.log("SYNC", "Guard sync enabled - waiting to see guard leave CB stall");
    }

    public void disableGuardSync() {
        needsGuardSync = false;
        sawGuardAtCbStall = false;
    }

    public boolean needsGuardSync() {
        return needsGuardSync;
    }

    // returns true when guard has been seen at CB stall (1867) and then moved away (1868+)
    public boolean isGuardSyncComplete() {
        if (!needsGuardSync) return true; // no sync needed

        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            if (y != PATROL_Y) continue;

            // step 1: see guard at CB stall (x=1867)
            if (x == 1867 && !sawGuardAtCbStall) {
                sawGuardAtCbStall = true;
                script.log("SYNC", "Guard at CB stall (x=1867) - watching for departure...");
                return false;
            }

            // step 2: after seeing guard at stall, wait for them to leave (x >= 1868)
            if (sawGuardAtCbStall && x >= 1868) {
                script.log("SYNC", "Guard left CB stall (x=" + x + ") - sync complete, GO!");
                needsGuardSync = false;
                sawGuardAtCbStall = false;
                return true;
            }
        }

        return false;
    }
}
