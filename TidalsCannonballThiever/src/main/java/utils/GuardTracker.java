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

    // === SINGLE STALL MODE (original) ===
    // early warning tile - guard sits here for ~3 seconds
    private static final int EARLY_WARNING_X = 1865;
    private static final int PATROL_Y = 3295;

    // immediate danger tiles (single stall)
    private static final int DANGER_X_1 = 1866;
    private static final int DANGER_X_2 = 1867;

    // === TWO STALL MODE ===
    // cannonball stall position in two-stall mode: 1867, 3295
    // ore stall position: 1863, 3295
    // guard patrols from west to east and back
    
    // cannonball stall danger tile (two-stall mode)
    private static final int TWO_STALL_CANNONBALL_DANGER_X = 1866;
    
    // ore stall danger tile
    private static final int ORE_STALL_DANGER_Y = 3292;
    private static final int ORE_STALL_X = 1863;

    // early warning delay params (normal distribution)
    private static final double DELAY_MIN_SEC = 2.5;
    private static final double DELAY_MAX_SEC = 3.5;
    private static final double DELAY_MEAN_SEC = 3.2;  // center of distribution
    private static final double DELAY_STD_DEV = 0.25;  // standard deviation

    // track when we first saw guard at early warning tile
    private long earlyWarningStartTime = 0;
    private long currentDelayMs = 0;  // randomized delay for current encounter

    // store last known npc positions for paint/debugging
    private List<WorldPosition> lastNpcPositions = new ArrayList<>();

    // === HIGHLIGHT-BASED PIXEL DETECTION (10-20x faster than tile updates!) ===
    // Cyan highlight color for Market Guards
    // RGB cyan = 0x00FFFF (0, 255, 255), tolerance = 15 for each HSL channel
    private static final SearchablePixel GUARD_HIGHLIGHT = new SearchablePixel(
            0x00FFFF,  // Cyan RGB value
            new SingleThresholdComparator(15),  // Tolerance of 15 for all channels
            ColorModel.HSL  // Compare in HSL color space
    );
    
    // Movement detection threshold (pixels)
    // Idle animations can cause 5-15 pixel jitter, actual walking is 30+ pixels
    private static final int MOVEMENT_THRESHOLD = 20;
    
    // === SPECIFIC WATCH TILES ===
    // These are the exact tiles where we START watching for guard movement
    // Cannonball stall watch tile: (1865, 3295, 0) - guard stands here before approaching CB stall
    private static final int CB_WATCH_X = 1865;
    private static final int CB_WATCH_Y = 3295;
    
    // Ore stall watch tile: (1863, 3292, 0) - guard stands here before approaching ore stall
    private static final int ORE_WATCH_X = 1863;
    private static final int ORE_WATCH_Y = 3292;
    
    // Track if we're actively watching at each stall's danger tile
    private boolean watchingAtCBTile = false;
    private boolean watchingAtOreTile = false;
    private Point cbWatchStartCenter = null;
    private Point oreWatchStartCenter = null;
    
    // Track when we started watching (for settling delay)
    private long cbWatchStartTime = 0;
    private long oreWatchStartTime = 0;
    
    // Settling delay - wait this long after guard arrives before recording their position
    // This prevents detecting their walking-into-position as "movement"
    private static final long SETTLE_DELAY_MS = 300;
    
    // Track last known guard highlight center position
    private Point lastGuardCenter = null;
    private long lastGuardCheckTime = 0;
    
    // Track guard movement direction (true = moving east/away from cannonball stall)
    private boolean guardMovingEast = false;
    
    // Number of ore thieves done in current ore stall visit
    private int oreThiefCount = 0;
    private static final int MAX_ORE_THIEVES = 2;
    
    // Track if we've received an XP drop at the ore stall
    // Only start watching guard AFTER we get an XP drop (confirms we're settled)
    private long arrivedAtOreStallTime = 0;
    private boolean gotOreStallXpDrop = false;
    
    // Track if we've received an XP drop at the CB stall (same approach)
    private long arrivedAtCBStallTime = 0;
    private boolean gotCBStallXpDrop = false;
    
    // CB stall player position - must be here before we start watching guard
    private static final int CB_STALL_PLAYER_X = 1867;
    private static final int CB_STALL_PLAYER_Y = 3295;

    public GuardTracker(Script script) {
        this.script = script;
    }

    /**
     * Generate a random delay using normal distribution
     * centered around DELAY_MEAN_SEC, clamped to [DELAY_MIN_SEC, DELAY_MAX_SEC]
     */
    private long generateRandomDelay() {
        // normal distribution centered at mean
        double delay = DELAY_MEAN_SEC + random.nextGaussian() * DELAY_STD_DEV;
        
        // clamp to min/max
        delay = Math.max(DELAY_MIN_SEC, Math.min(DELAY_MAX_SEC, delay));
        
        return (long) (delay * 1000); // convert to ms
    }

    /**
     * find all npc positions from minimap (no tapping, just positions)
     * @return list of npc positions
     */
    public List<WorldPosition> findAllNPCPositions() {
        List<WorldPosition> npcPositions = new ArrayList<>();

        // get all npc positions from minimap
        UIResultList<WorldPosition> npcResult = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcResult == null || !npcResult.isFound()) {
            return npcPositions;
        }

        // create mutable copy since asList() returns unmodifiable list
        npcPositions = new ArrayList<>(npcResult.asList());

        // update cached positions
        lastNpcPositions = npcPositions;

        return npcPositions;
    }

    /**
     * check if any npc is at a danger tile
     * - immediate danger (1866, 1867): return true right away
     * - early warning (1865): return true after 2 seconds delay
     * @return true if danger detected
     */
    public boolean isAnyGuardInDangerZone() {
        List<WorldPosition> npcPositions = findAllNPCPositions();

        boolean guardAtEarlyWarning = false;
        boolean guardAtImmediateDanger = false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // check patrol row
            if (y != PATROL_Y) continue;

            // immediate danger - retreat NOW
            if (x == DANGER_X_1 || x == DANGER_X_2) {
                script.log("GUARD", "IMMEDIATE DANGER! NPC at x=" + x);
                earlyWarningStartTime = 0; // reset early warning timer
                return true;
            }

            // early warning - guard at 1865
            if (x == EARLY_WARNING_X) {
                guardAtEarlyWarning = true;
            }
        }

        // handle early warning with randomized delay
        if (guardAtEarlyWarning) {
            if (earlyWarningStartTime == 0) {
                // first time seeing guard at 1865, generate random delay
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
            // still waiting, don't retreat yet
        } else {
            // guard no longer at 1865, reset timer
            if (earlyWarningStartTime != 0) {
                earlyWarningStartTime = 0;
                currentDelayMs = 0;
            }
        }

        return false;
    }

    /**
     * check if safe to return to thieving
     * guard must have moved PAST the stall (x >= 1868) or be off the patrol row
     * @return true if safe to return
     */
    public boolean isSafeToReturn() {
        List<WorldPosition> npcPositions = findAllNPCPositions();

        // check if any npc is in the guard patrol area (x 1864-1867, y 3295)
        // ignore NPCs outside this range - they're not guards
        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // only check NPCs in the guard patrol zone (x 1864-1867 at y=3295)
            // NPCs outside this range are not the patrolling guard
            if (y == PATROL_Y && x >= 1864 && x <= DANGER_X_2) {
                script.log("GUARD", "Not safe yet - NPC at x=" + x + " in patrol zone");
                return false;
            }
        }

        // reset early warning timer when returning
        earlyWarningStartTime = 0;
        currentDelayMs = 0;

        script.log("GUARD", "Safe to return - patrol zone clear");
        return true;
    }

    /**
     * get last known npc positions (for paint overlay)
     */
    public List<WorldPosition> getLastNpcPositions() {
        return lastNpcPositions;
    }

    // === TWO STALL MODE METHODS ===
    // Guards patrol CLOCKWISE only!
    // - At cannonball stall: guard at x=1865 = danger (heading toward stall)
    // - Guard at x > 1868 = safe (has passed cannonball stall, heading to ore stall)
    // - At ore stall: guard at (1863, 3292) = danger (heading toward ore stall from south)
    // Priority: cannonball stall (more XP), ore stall is just a waiting spot

    // GLOBAL early warning timer - persists across stall switches!
    // This way if guard is at 1865 while we're at ore stall, timer is already counting
    private long twoStallEarlyWarningStart = 0;
    private long twoStallCurrentDelay = 0;

    /**
     * Update the global early warning timer based on guard position
     * Call this frequently (e.g. in MonitorThieving) regardless of which stall we're at
     */
    public void updateGlobalGuardTimer() {
        if (!twoStallMode) return;

        List<WorldPosition> npcPositions = findAllNPCPositions();
        boolean guardAtEarlyWarning = false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // guard on patrol row at early warning position (1865)
            if (y == PATROL_Y && x == EARLY_WARNING_X) {
                guardAtEarlyWarning = true;
            }
        }

        // keep timer running if guard at 1865
        if (guardAtEarlyWarning) {
            if (twoStallEarlyWarningStart == 0) {
                twoStallEarlyWarningStart = System.currentTimeMillis();
                twoStallCurrentDelay = generateRandomDelay();
                double delaySec = twoStallCurrentDelay / 1000.0;
                script.log("GUARD", String.format("GLOBAL: Guard at 1865, timer started (%.2fs)", delaySec));
            }
        } else {
            // guard moved away, reset timer
            if (twoStallEarlyWarningStart != 0) {
                script.log("GUARD", "GLOBAL: Guard moved from 1865, timer reset");
                twoStallEarlyWarningStart = 0;
                twoStallCurrentDelay = 0;
            }
        }
    }

    /**
     * check if guard is approaching the cannonball stall in two-stall mode
     * MORE AGGRESSIVE: switch immediately at 1865 (no timer) because travel to ore takes time
     * @return true if should switch to ore stall
     */
    public boolean isGuardNearCannonballStall() {
        if (!twoStallMode) return false;

        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // check guard on patrol row (y=3295)
            if (y != PATROL_Y) continue;

            // IMMEDIATE switch in two-stall mode when guard at 1865, 1866, or 1867
            // No timer delay - we need the extra time to travel to ore stall!
            if (x >= EARLY_WARNING_X && x <= DANGER_X_2) {
                script.log("GUARD", "Guard at x=" + x + " - SWITCH TO ORE NOW!");
                twoStallEarlyWarningStart = 0;
                twoStallCurrentDelay = 0;
                return true;
            }
        }

        return false;
    }

    /**
     * check if cannonball stall is safe to return to (guard has passed it)
     * REQUIRES seeing guard at x >= 1868 - won't return true if no guard visible
     * @return true if guard is CONFIRMED at x >= 1868 (at or past the cannonball stall)
     */
    public boolean isCannonballStallSafe() {
        if (!twoStallMode) return isSafeToReturn();

        List<WorldPosition> npcPositions = findAllNPCPositions();
        
        boolean foundGuardOnPatrol = false;
        boolean guardPastStall = false;

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // only check NPCs on patrol row
            if (y != PATROL_Y) continue;
            
            foundGuardOnPatrol = true;

            // guard still in danger zone (1865-1867) = NOT SAFE
            if (x >= EARLY_WARNING_X && x <= 1867) {
                script.log("GUARD", "Cannonball NOT safe - guard at x=" + x);
                return false;
            }
            
            // guard at or past the stall (x >= 1868) = SAFE to return
            if (x >= 1868) {
                guardPastStall = true;
            }
        }

        // ONLY safe if we actually SAW the guard past the stall
        // Don't return true just because we didn't see any guard (detection might have failed)
        if (guardPastStall) {
            // reset early warning timer when returning
            twoStallEarlyWarningStart = 0;
            twoStallCurrentDelay = 0;
            script.log("GUARD", "Cannonball SAFE - guard at x>=1868!");
            return true;
        }
        
        // if we found a guard but they weren't past, not safe
        // if we didn't find any guard, also not safe (detection might have missed them)
        if (!foundGuardOnPatrol) {
            // don't spam log, just return false silently
        }
        return false;
    }

    /**
     * check if guard is approaching the ore stall (from the south)
     * triggers when guard is at (1863, 3292)
     * @return true if guard is near ore stall
     */
    public boolean isGuardNearOreStall() {
        if (!twoStallMode) return false;

        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // guard at ore stall danger zone (south of stall)
            if (x == ORE_STALL_X && y == ORE_STALL_DANGER_Y) {
                script.log("GUARD", "Guard near ore stall at (" + x + ", " + y + ")");
                return true;
            }
        }
        return false;
    }

    // Keep old methods for backwards compatibility but they now delegate
    public boolean isSafeAtCannonballStall() {
        return isCannonballStallSafe();
    }

    public boolean isSafeAtOreStall() {
        // ore stall is "safe" as long as guard isn't right there
        return !isGuardNearOreStall();
    }

    // === HIGHLIGHT-BASED PIXEL DETECTION METHODS ===
    // These detect guard movement 10-20x faster than tile-based methods!

    /**
     * Find if any NPC is at the exact specified tile
     * @return WorldPosition if found, null otherwise
     */
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

    /**
     * Log all NPC positions visible from minimap (for debugging)
     */
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

    /**
     * Get the highlight center point for a guard at the given position
     * @return Point center of highlight bounds, or null if not found
     */
    private Point getGuardHighlightCenter(WorldPosition guardPos) {
        if (guardPos == null) return null;

        // Get tile cube for this NPC (100 height for guard)
        Polygon tileCube = script.getSceneProjector().getTileCube(guardPos, 100);
        if (tileCube == null) return null;

        // Get highlight bounds using cyan color
        Rectangle bounds = script.getPixelAnalyzer().getHighlightBounds(tileCube, GUARD_HIGHLIGHT);
        if (bounds == null) return null;

        return bounds.getCenter();
    }

    /**
     * MAIN METHOD FOR CANNONBALL STALL:
     * Check if guard is at CB watch tile (1865, 3295) and watch for movement.
     * Returns true the INSTANT they start moving away.
     * 
     * Call this frequently (every frame) when at cannonball stall.
     * 
     * @return true if guard just started moving = SWITCH TO ORE NOW!
     */
    public boolean shouldSwitchToOre() {
        if (!twoStallMode) return false;

        // CRITICAL: Don't start watching until we're actually at the CB stall!
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;
        
        int playerX = (int) myPos.getX();
        int playerY = (int) myPos.getY();
        
        if (playerX != CB_STALL_PLAYER_X || playerY != CB_STALL_PLAYER_Y) {
            // Not at CB stall yet - don't watch, reset any existing watch state
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
        
        // Track when we first arrived at the CB stall
        if (arrivedAtCBStallTime == 0) {
            arrivedAtCBStallTime = System.currentTimeMillis();
            gotCBStallXpDrop = false;
            script.log("GUARD-PIXEL", "Arrived at CB stall - waiting for XP drop before watching guard...");
            return false;
        }
        
        // CRITICAL: Wait for XP drop before starting guard watch!
        if (!gotCBStallXpDrop) {
            if (lastXpGain.timeElapsed() < (System.currentTimeMillis() - arrivedAtCBStallTime)) {
                gotCBStallXpDrop = true;
                script.log("GUARD-PIXEL", "Got XP drop at CB stall - NOW can start watching guard!");
            } else {
                return false;
            }
        }

        // Check if guard is at the CB watch tile
        WorldPosition guardPos = findNPCAtTile(CB_WATCH_X, CB_WATCH_Y);

        if (guardPos != null) {
            // Guard IS at the watch tile - track their highlight pixel center
            Point currentCenter = getGuardHighlightCenter(guardPos);

            if (currentCenter == null) {
                // Can't see highlight, maybe occluded - keep watching
                return false;
            }

            if (!watchingAtCBTile) {
                // Just started watching - record time and wait for settle
                watchingAtCBTile = true;
                cbWatchStartTime = System.currentTimeMillis();
                cbWatchStartCenter = null;
                script.log("GUARD-PIXEL", "Guard at CB watch tile (1865,3295) - waiting for settle...");
                return false;
            }

            // Check if settling delay has passed (short - XP drop confirms we're settled)
            long elapsed = System.currentTimeMillis() - cbWatchStartTime;
            if (elapsed < 300) {
                return false;
            }

            // Record position if not done yet
            if (cbWatchStartCenter == null) {
                cbWatchStartCenter = currentCenter;
                script.log("GUARD-PIXEL", "Guard settled at CB tile - NOW watching for movement...");
                return false;
            }

            // Already watching - check for ANY movement from settled position
            int dx = Math.abs(currentCenter.x - cbWatchStartCenter.x);
            int dy = Math.abs(currentCenter.y - cbWatchStartCenter.y);

            if (dx > MOVEMENT_THRESHOLD || dy > MOVEMENT_THRESHOLD) {
                script.log("GUARD-PIXEL", "GUARD MOVED! dx=" + dx + ", dy=" + dy + " pixels - SWITCH TO ORE!");
                return true;  // SWITCH NOW!
            }

            // Still watching, no movement yet
            return false;
        } else {
            // Guard NOT at watch tile
            if (watchingAtCBTile) {
                script.log("GUARD-PIXEL", "Guard left CB watch tile - stopping watch");
                watchingAtCBTile = false;
                cbWatchStartCenter = null;
                cbWatchStartTime = 0;
            }
            return false;
        }
    }

    // Ore stall player position - must be here before we start watching guard
    private static final int ORE_STALL_PLAYER_X = 1863;
    private static final int ORE_STALL_PLAYER_Y = 3295;

    /**
     * MAIN METHOD FOR ORE STALL:
     * Watch the guard at (1863, 3292) - the INSTANT they start moving, we must
     * click cannonball stall immediately because all 3 guards move at once!
     * 
     * IMPORTANT: Only starts watching once player is AT the ore stall (1863, 3295, 0).
     * This prevents false positives from camera movement during the walk.
     * 
     * @return true if guard started moving = CLICK CANNONBALL NOW!
     */
    public boolean shouldSwitchToCannonball() {
        if (!twoStallMode) return false;

        // CRITICAL: Don't start watching until we're actually at the ore stall!
        // Camera movement during walking causes false "guard movement" detection
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return false;
        
        int playerX = (int) myPos.getX();
        int playerY = (int) myPos.getY();
        
        if (playerX != ORE_STALL_PLAYER_X || playerY != ORE_STALL_PLAYER_Y) {
            // Not at ore stall yet - don't watch, reset any existing watch state
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
        
        // Track when we first arrived at the ore stall
        if (arrivedAtOreStallTime == 0) {
            arrivedAtOreStallTime = System.currentTimeMillis();
            gotOreStallXpDrop = false;
            script.log("GUARD-PIXEL", "Arrived at ore stall - waiting for XP drop before watching guard...");
            return false;
        }
        
        // CRITICAL: Wait for XP drop before starting guard watch!
        // XP drop confirms thieve completed and we're settled
        if (!gotOreStallXpDrop) {
            // Check if we got an XP drop since arriving (lastXpGain timer resets on XP)
            // If lastXpGain was reset AFTER we arrived, we got an XP drop
            if (lastXpGain.timeElapsed() < (System.currentTimeMillis() - arrivedAtOreStallTime)) {
                gotOreStallXpDrop = true;
                script.log("GUARD-PIXEL", "Got XP drop at ore stall - NOW can start watching guard!");
                // Log all visible NPCs for debugging
                logAllNPCPositions();
            } else {
                // Still waiting for XP drop
                return false;
            }
        }

        // Check if guard is at the ore watch tile
        WorldPosition guardPos = findNPCAtTile(ORE_WATCH_X, ORE_WATCH_Y);

        if (guardPos != null) {
            // Log actual tile position of the NPC we found
            int guardX = (int) guardPos.getX();
            int guardY = (int) guardPos.getY();
            
            // Guard IS at the watch tile - track their highlight pixel center
            Point currentCenter = getGuardHighlightCenter(guardPos);

            if (currentCenter == null) {
                // Can't see highlight - keep waiting
                script.log("GUARD-PIXEL", "NPC at tile (" + guardX + "," + guardY + ") but can't see highlight");
                return false;
            }

            if (!watchingAtOreTile) {
                // Just started watching - record time and wait for guard to settle
                watchingAtOreTile = true;
                oreWatchStartTime = System.currentTimeMillis();
                oreWatchStartCenter = null;
                script.log("GUARD-PIXEL", "NPC at tile (" + guardX + "," + guardY + ") - waiting for settle...");
                return false;
            }

            // Check if settling delay has passed (shorter now - XP drop already confirmed we're settled)
            long elapsed = System.currentTimeMillis() - oreWatchStartTime;
            if (elapsed < 300) {  // 300ms settle time (reduced since XP drop confirms player settled)
                return false;
            }

            // Settling complete - record position if not done yet
            if (oreWatchStartCenter == null) {
                oreWatchStartCenter = currentCenter;
                script.log("GUARD-PIXEL", "NPC at (" + guardX + "," + guardY + ") settled - pixel pos: (" + currentCenter.x + "," + currentCenter.y + ")");
                return false;
            }

            // Already watching - check for ANY movement from settled position
            int dx = Math.abs(currentCenter.x - oreWatchStartCenter.x);
            int dy = Math.abs(currentCenter.y - oreWatchStartCenter.y);

            if (dx > MOVEMENT_THRESHOLD || dy > MOVEMENT_THRESHOLD) {
                script.log("GUARD-PIXEL", "NPC at (" + guardX + "," + guardY + ") MOVED! start=(" + oreWatchStartCenter.x + "," + oreWatchStartCenter.y + ") now=(" + currentCenter.x + "," + currentCenter.y + ") dx=" + dx + " dy=" + dy);
                return true;  // DROP EVERYTHING AND CLICK CANNONBALL!
            }

            return false;
        } else {
            // Guard NOT at watch tile anymore
            if (watchingAtOreTile) {
                script.log("GUARD-PIXEL", "Guard left ore watch tile");
                watchingAtOreTile = false;
                oreWatchStartCenter = null;
                oreWatchStartTime = 0;
            }
            // Guard left - cannonball should be safe now
            return isCannonballStallSafe();
        }
    }

    /**
     * Check if we're currently watching the guard at CB tile
     * @return true if actively watching
     */
    public boolean isWatchingAtCBTile() {
        return watchingAtCBTile;
    }

    /**
     * Check if we're currently watching the guard at ore tile
     * @return true if actively watching
     */
    public boolean isWatchingAtOreTile() {
        return watchingAtOreTile;
    }

    /**
     * Check if guard has PASSED the CB watch tile (at x=1866 or 1867)
     * This is a fallback for when pixel detection might have missed them
     * @return true if guard is past the watch tile
     */
    public boolean isGuardPastWatchTile() {
        if (!twoStallMode) return false;

        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // Only check guard on patrol row (y=3295)
            if (y != PATROL_Y) continue;

            // Guard at x=1866 or 1867 = PAST the watch tile, need to switch NOW
            if (x == 1866 || x == 1867) {
                script.log("GUARD", "Guard PAST watch tile at x=" + x + " - SWITCH NOW!");
                return true;
            }
        }
        return false;
    }

    /**
     * Check if guard has PASSED the ore watch tile
     * This is a fallback for when pixel detection might have missed them
     * @return true if guard is past the ore watch tile (heading toward ore stall)
     */
    public boolean isGuardPastOreWatchTile() {
        if (!twoStallMode) return false;

        List<WorldPosition> npcPositions = findAllNPCPositions();

        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // Guard past ore watch tile - check if they're at ore stall area
            // Ore watch tile is (1863, 3292), past would be closer to stall
            if (x == ORE_STALL_X && y == 3293) {
                script.log("GUARD", "Guard PAST ore watch tile - heading to ore stall!");
                return true;
            }
        }
        return false;
    }

    /**
     * Update the guard's highlight pixel position (generic method).
     * Call this frequently (e.g., in onNewFrame) for instant movement detection.
     * Returns the current guard highlight bounds center, or null if not found.
     */
    public Point updateGuardHighlightPosition() {
        if (!twoStallMode) return null;

        UIResultList<WorldPosition> npcPositions = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcPositions == null || !npcPositions.isFound()) return null;

        // Look for guard near cannonball stall area (x 1863-1870, y 3291-3297)
        for (WorldPosition npcPos : npcPositions) {
            if (npcPos == null || npcPos.getPlane() != 0) continue;

            int x = (int) npcPos.getX();
            int y = (int) npcPos.getY();

            // Only check NPCs in the guard patrol zone
            if (y < 3291 || y > 3297) continue;
            if (x < 1863 || x > 1872) continue;

            Point currentCenter = getGuardHighlightCenter(npcPos);
            if (currentCenter == null) continue;

            // Track movement direction
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

    /**
     * Detects the MOMENT a guard starts moving via highlight pixel shift.
     * Returns true when guard's highlight center moves beyond threshold.
     * This is 10-20x faster than waiting for minimap tile update!
     * 
     * @return true if guard has started moving (pixel shift detected)
     */
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

    /**
     * Check if guard is moving AWAY from cannonball stall (east direction).
     * When true, it's safe to return to cannonball stall.
     * 
     * @return true if guard is moving east (away from cannonball, toward ore stall)
     */
    public boolean isGuardMovingAwayFromCannonball() {
        if (!twoStallMode) return false;
        updateGuardHighlightPosition();
        return guardMovingEast;
    }

    /**
     * Check if guard is moving TOWARD cannonball stall (west direction).
     * When true, should switch to ore stall.
     * 
     * @return true if guard is moving west (toward cannonball stall)
     */
    public boolean isGuardMovingTowardCannonball() {
        if (!twoStallMode) return false;
        updateGuardHighlightPosition();
        return !guardMovingEast && lastGuardCenter != null;
    }

    // === ORE THIEVE TRACKING ===

    /**
     * Reset ore thieve count (call when switching to ore stall)
     */
    public void resetOreThiefCount() {
        oreThiefCount = 0;
    }

    /**
     * Increment ore thieve count (call after each successful ore thieve)
     */
    public void incrementOreThiefCount() {
        oreThiefCount++;
        script.log("GUARD", "Ore thieve #" + oreThiefCount + " of " + MAX_ORE_THIEVES);
    }

    /**
     * Check if we can do more ore thieves
     * @return true if under max ore thieves limit
     */
    public boolean canDoMoreOreThieves() {
        return oreThiefCount < MAX_ORE_THIEVES;
    }

    /**
     * Get current ore thieve count
     */
    public int getOreThiefCount() {
        return oreThiefCount;
    }

    /**
     * Reset guard tracking state (call when switching stalls)
     */
    public void resetGuardTracking() {
        lastGuardCenter = null;
        guardMovingEast = false;
        lastGuardCheckTime = 0;
        // Also reset watch states
        watchingAtCBTile = false;
        watchingAtOreTile = false;
        cbWatchStartCenter = null;
        oreWatchStartCenter = null;
        cbWatchStartTime = 0;
        oreWatchStartTime = 0;
        // Reset XP tracking for both stalls
        arrivedAtOreStallTime = 0;
        gotOreStallXpDrop = false;
        arrivedAtCBStallTime = 0;
        gotCBStallXpDrop = false;
    }
}
