package tasks;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.visual.PixelCluster;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import main.TidalsChompyHunter;
import utils.DetectedPlayer;
import utils.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * detects players in the chompy hunting area and triggers hop when occupied
 * uses area-based detection instead of radius for more accurate crash detection
 */
public class DetectPlayers extends Task {

    // chompy hunting area - covers the swamp where chompies spawn
    private static final RectangleArea CHOMPY_HUNTING_AREA = new RectangleArea(2379, 3039, 25, 19, 0);

    // crash threshold - random between 7-12 seconds (set on each new detection)
    public static long crashThresholdMs = 7000;
    private static final long MIN_THRESHOLD_MS = 7000;
    private static final long MAX_THRESHOLD_MS = 12000;

    // legacy setting kept for UI compatibility (not used in area-based detection)
    public static int detectionRadius = 9;

    // self-detection filter - our own dot can appear offset from reported position
    private static final int SELF_FILTER_DISTANCE = 3;

    // post-hop grace period - skip occupied check while OSMB stabilizes position
    private static final long POST_HOP_GRACE_MS = 10000;
    public static long lastHopTimestamp = 0;

    // post-login grace period - skip occupied check while OSMB identifies our position
    // prevents false positive from seeing our own white dot as another player
    private static final long POST_LOGIN_GRACE_MS = 10000;
    public static long lastLoginTimestamp = 0;

    // state
    public static volatile boolean crashDetected = false;
    private static Map<Integer, DetectedPlayer> trackedPlayers = new HashMap<>();
    private static long lastLogTime = 0;

    // anti-jitter: require multiple consecutive "it's us" readings before clearing
    private static int consecutiveSelfReadings = 0;
    private static final int SELF_READINGS_TO_CLEAR = 5;  // need 5 frames of "it's us" to clear

    // persistent area occupied tracking - doesn't reset when dot position jitters
    private static long areaOccupiedSince = 0;  // timestamp when we first detected a player
    private static int consecutiveClearReadings = 0;
    private static final int CLEAR_READINGS_TO_RESET = 10;  // need 10 frames of "clear" to reset

    // === HEALTH BAR CRASH DETECTION ===
    // detects when a chompy's health bar appears but we didn't attack it
    // this means someone else is killing our chompies = direct crash evidence

    // health bar pixel detection (user-provided values)
    private static final SearchablePixel HEALTH_BAR_GREEN = new SearchablePixel(
            -16711936,  // green portion of health bar
            new SingleThresholdComparator(2),
            ColorModel.RGB
    );
    private static final SearchablePixel HEALTH_BAR_RED = new SearchablePixel(
            -65536,  // red portion of health bar (damage taken)
            new SingleThresholdComparator(2),
            ColorModel.RGB
    );
    private static final int HEALTH_CLUSTER_MAX_DISTANCE = 3;
    private static final int HEALTH_CLUSTER_MIN_SIZE = 5;

    // track when WE initiated an attack - set by AttackChompy.recordOurAttack()
    private static long lastAttackTime = 0;
    // grace period after our attack before we consider a health bar "not ours"
    // health bar appears within ~1 second of attack, so 1.5s gives buffer
    private static final long ATTACK_GRACE_PERIOD_MS = 1500;

    // cache health bar detection to avoid repeated scans
    private static long lastHealthBarCheckTime = 0;
    private static final long HEALTH_BAR_CHECK_INTERVAL_MS = 300;  // check every 300ms
    private static boolean lastHealthBarVisible = false;

    public DetectPlayers(Script script) {
        super(script);
    }

    /**
     * convert WorldPosition to integer key for HashMap lookup
     * uses bit packing: plane(2 bits) | y(15 bits) | x(15 bits) = 32 bits
     * avoids string concatenation overhead while maintaining uniqueness
     */
    private static int posKey(WorldPosition pos) {
        // pack position into single int: x uses bits 0-14, y uses bits 15-29, plane uses bits 30-31
        return (pos.getPlane() << 30) | ((pos.getY() & 0x7FFF) << 15) | (pos.getX() & 0x7FFF);
    }

    /**
     * format position for logging (only used in log statements, not hot paths)
     */
    private static String formatPos(WorldPosition pos) {
        return pos.getX() + "," + pos.getY();
    }

    @Override
    public boolean activate() {
        // never activate as a task - detection runs via runDetection() from onNewFrame()
        return false;
    }

    @Override
    public boolean execute() {
        // unused - detection runs via runDetection()
        return false;
    }

    /**
     * run player detection - call from main script's onNewFrame()
     * returns true if crash detected this frame
     */
    public boolean runDetection() {
        // skip if anti-crash disabled in settings
        if (!TidalsChompyHunter.antiCrashEnabled) {
            return false;
        }

        if (!TidalsChompyHunter.setupComplete) {
            return false;
        }

        // === HEALTH BAR CRASH DETECTION (PRIMARY) ===
        // this is the most reliable crash signal - someone attacked a chompy we didn't
        WorldPosition stolenChompy = detectHealthBarCrash();
        if (stolenChompy != null) {
            // add to AttackChompy's ignore list so we don't try to attack/pluck it
            // this is critical - prevents trying to pluck someone else's kill
            AttackChompy.addIgnoredPosition(stolenChompy);

            // trigger crash - someone is stealing our chompies
            crashDetected = true;
            TidalsChompyHunter.task = "chompy stolen - hopping!";
            return true;
        }

        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return false;
        }

        // clean up stale entries (players who left the hunting area)
        cleanupStaleEntries();

        // get current player positions from minimap
        UIResultList<WorldPosition> result = script.getWidgetManager().getMinimap().getPlayerPositions();
        if (result == null || result.isNotFound()) {
            // no players detected
            if (!trackedPlayers.isEmpty()) {
                script.log(getClass(), "all players left hunting area");
                trackedPlayers.clear();
            }
            return false;
        }

        List<WorldPosition> playerPositions = result.asList();
        if (playerPositions.isEmpty()) {
            // empty list - increment clear counter
            consecutiveClearReadings++;
            if (consecutiveClearReadings >= CLEAR_READINGS_TO_RESET && areaOccupiedSince > 0) {
                script.log(getClass(), "area clear for " + consecutiveClearReadings + " readings - resetting occupied timer");
                areaOccupiedSince = 0;
                trackedPlayers.clear();
            }
            return false;
        }

        // log all detected dots (only every 3 seconds to reduce spam)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime >= 3000) {
            script.log(getClass(), "detected " + playerPositions.size() + " player dot(s), our pos: " + formatPos(playerPos));
            for (int i = 0; i < playerPositions.size(); i++) {
                WorldPosition dot = playerPositions.get(i);
                double dist = dot.distanceTo(playerPos);
                boolean inArea = CHOMPY_HUNTING_AREA.contains(dot);
                script.log(getClass(), "  dot[" + i + "]: " + formatPos(dot) +
                           " dist=" + (int)dist + (inArea ? " [IN AREA]" : " [outside]"));
            }
        }

        // handle single dot case - check if it's us or someone else
        if (playerPositions.size() == 1) {
            WorldPosition singleDot = playerPositions.get(0);
            if (singleDot.getPlane() != playerPos.getPlane()) {
                if (!trackedPlayers.isEmpty()) {
                    trackedPlayers.clear();
                }
                return false;
            }

            double distFromUs = singleDot.distanceTo(playerPos);

            // if dot is close to us, it's our dot - but require multiple consecutive readings
            // to prevent jitter from clearing valid player detection
            if (distFromUs <= SELF_FILTER_DISTANCE) {
                consecutiveSelfReadings++;
                consecutiveClearReadings++;  // also counts as "area clear"

                if (consecutiveSelfReadings >= SELF_READINGS_TO_CLEAR) {
                    if (!trackedPlayers.isEmpty()) {
                        script.log(getClass(), "single dot is us (dist " + (int)distFromUs + ", " + consecutiveSelfReadings + " consecutive) - clearing tracked");
                        trackedPlayers.clear();
                    }
                    if (areaOccupiedSince > 0) {
                        script.log(getClass(), "area confirmed clear - resetting occupied timer");
                        areaOccupiedSince = 0;
                    }
                    consecutiveSelfReadings = 0;
                }
                return false;
            }

            // dot is far from us - reset the consecutive self counter
            consecutiveSelfReadings = 0;

            // dot is far from us - this is another player, check if in hunting area
            if (CHOMPY_HUNTING_AREA.contains(singleDot)) {
                consecutiveClearReadings = 0;  // reset clear counter

                // start persistent area tracking if not already
                if (areaOccupiedSince == 0) {
                    areaOccupiedSince = System.currentTimeMillis();
                    crashThresholdMs = MIN_THRESHOLD_MS + (long)(Math.random() * (MAX_THRESHOLD_MS - MIN_THRESHOLD_MS));
                    script.log(getClass(), "AREA OCCUPIED - player at " + formatPos(singleDot) +
                               " dist=" + (int)distFromUs + " (threshold: " + crashThresholdMs + "ms)");
                }

                // check if threshold exceeded using persistent timer
                long occupiedDuration = System.currentTimeMillis() - areaOccupiedSince;
                if (occupiedDuration >= crashThresholdMs) {
                    script.log(getClass(), "=== CRASH DETECTED === area occupied for " + occupiedDuration + "ms");
                    crashDetected = true;
                    TidalsChompyHunter.task = "crash detected!";
                    return true;
                }

                // also track in map for logging
                int key = posKey(singleDot);
                if (!trackedPlayers.containsKey(key)) {
                    trackedPlayers.put(key, new DetectedPlayer(singleDot));
                }
            }
            return false;
        }

        // find our dot (closest to our reported position)
        WorldPosition ourDot = null;
        double ourDotDistance = Double.MAX_VALUE;
        for (WorldPosition dot : playerPositions) {
            if (dot.getPlane() != playerPos.getPlane()) continue;
            double dist = dot.distanceTo(playerPos);
            if (dist < ourDotDistance) {
                ourDotDistance = dist;
                ourDot = dot;
            }
        }

        // track OTHER players in the chompy hunting area
        for (WorldPosition otherPlayer : playerPositions) {
            // filter same plane only
            if (otherPlayer.getPlane() != playerPos.getPlane()) {
                continue;
            }

            // skip our own dot
            if (otherPlayer.equals(ourDot)) {
                continue;
            }

            // check if this other player is in the chompy hunting area
            if (CHOMPY_HUNTING_AREA.contains(otherPlayer)) {
                consecutiveClearReadings = 0;  // reset clear counter

                // start persistent area tracking if not already
                if (areaOccupiedSince == 0) {
                    areaOccupiedSince = System.currentTimeMillis();
                    crashThresholdMs = MIN_THRESHOLD_MS + (long)(Math.random() * (MAX_THRESHOLD_MS - MIN_THRESHOLD_MS));
                    script.log(getClass(), "AREA OCCUPIED - player at " + formatPos(otherPlayer) + " (threshold: " + crashThresholdMs + "ms)");
                }

                // check if threshold exceeded using persistent timer
                long occupiedDuration = System.currentTimeMillis() - areaOccupiedSince;
                if (occupiedDuration >= crashThresholdMs) {
                    script.log(getClass(), "=== CRASH DETECTED === area occupied for " + occupiedDuration + "ms");
                    crashDetected = true;
                    TidalsChompyHunter.task = "crash detected!";
                    return true;
                }

                // also track in map for logging
                int key = posKey(otherPlayer);
                if (!trackedPlayers.containsKey(key)) {
                    trackedPlayers.put(key, new DetectedPlayer(otherPlayer));
                }
            }
        }

        // update status if tracking players
        if (!trackedPlayers.isEmpty()) {
            TidalsChompyHunter.task = "player in area...";

            // log player durations every 3 seconds to avoid spam
            long now = System.currentTimeMillis();
            if (now - lastLogTime >= 3000) {
                for (DetectedPlayer player : trackedPlayers.values()) {
                    script.log(getClass(), "tracking player at " + formatPos(player.getPosition()) +
                               " for " + player.getDuration() + "ms / " + crashThresholdMs + "ms");
                }
                lastLogTime = now;
            }
        }

        return false;
    }

    /**
     * remove players who left the chompy hunting area
     */
    private void cleanupStaleEntries() {
        trackedPlayers.entrySet().removeIf(entry -> {
            WorldPosition trackedPos = entry.getValue().getPosition();

            // remove if outside hunting area
            if (!CHOMPY_HUNTING_AREA.contains(trackedPos)) {
                script.log(getClass(), "player left hunting area at " + formatPos(trackedPos) + " after " +
                           entry.getValue().getDuration() + "ms (no crash triggered)");
                return true;
            }
            return false;
        });
    }

    /**
     * record that WE just sent an attack - called from AttackChompy before attacking
     * used to distinguish our health bars from someone else's
     */
    public static void recordOurAttack() {
        lastAttackTime = System.currentTimeMillis();
    }

    /**
     * detect health bar crash - a health bar appeared but we didn't attack
     * this is DIRECT EVIDENCE of being crashed (someone else attacking our chompy)
     *
     * returns the approximate world position of the stolen chompy if detected, null otherwise
     */
    public WorldPosition detectHealthBarCrash() {
        // skip if anti-crash disabled
        if (!TidalsChompyHunter.antiCrashEnabled) {
            return null;
        }

        // throttle checks to avoid expensive pixel scans every frame
        long now = System.currentTimeMillis();
        if (now - lastHealthBarCheckTime < HEALTH_BAR_CHECK_INTERVAL_MS) {
            return null;
        }
        lastHealthBarCheckTime = now;

        // skip if WE are in combat - the health bar is ours
        if (AttackChompy.inCombat) {
            lastHealthBarVisible = false;
            return null;
        }

        // skip if we just attacked - health bar is loading for our target
        long timeSinceAttack = now - lastAttackTime;
        if (timeSinceAttack < ATTACK_GRACE_PERIOD_MS) {
            lastHealthBarVisible = false;
            return null;
        }

        // detect any health bar on screen
        PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                HEALTH_CLUSTER_MAX_DISTANCE,
                HEALTH_CLUSTER_MIN_SIZE,
                new SearchablePixel[]{HEALTH_BAR_GREEN, HEALTH_BAR_RED}
        );

        PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(null, query);
        if (result == null || result.getClusters() == null || result.getClusters().isEmpty()) {
            lastHealthBarVisible = false;
            return null;
        }

        // health bar detected but we didn't attack!
        // this only triggers once per appearance (not spam)
        if (!lastHealthBarVisible) {
            lastHealthBarVisible = true;

            script.log(getClass(), "=== HEALTH BAR CRASH === health bar visible but we didn't attack!");

            // try to find the world position of this stolen chompy
            // use the health bar screen position to match to an NPC
            WorldPosition stolenChompyPos = findStolenChompyPosition(result.getClusters());

            if (stolenChompyPos != null) {
                script.log(getClass(), "stolen chompy at " + stolenChompyPos.getX() + "," + stolenChompyPos.getY() +
                        " - adding to ignore list");
                // add to ignore so we don't try to attack/pluck it
                int posKey = AttackChompy.posKey(stolenChompyPos);
                // we can't directly access ignoredPositionTimestamps, so we'll return the position
                // and let the caller (runDetection) add it
            }

            return stolenChompyPos;
        }

        return null;
    }

    /**
     * find the world position of a chompy being attacked by someone else
     * uses the health bar screen position to match against NPC positions
     */
    private WorldPosition findStolenChompyPosition(List<PixelCluster> healthBarClusters) {
        if (healthBarClusters.isEmpty()) {
            return null;
        }

        // health bars appear above NPCs, so get the first cluster's position
        PixelCluster healthBar = healthBarClusters.get(0);
        Rectangle bounds = healthBar.getBounds();
        int healthBarX = bounds.x + bounds.width / 2;
        int healthBarY = bounds.y + bounds.height / 2;

        script.log(getClass(), "[healthBarCrash] health bar at screen(" + healthBarX + "," + healthBarY + ")");

        // get NPC positions from minimap
        UIResultList<WorldPosition> npcResult = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcResult == null || npcResult.isNotFound()) {
            return null;
        }

        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return null;
        }

        // find the NPC whose screen position is closest to the health bar
        // health bar appears ABOVE the NPC, so we look for NPC slightly below
        WorldPosition closestNpc = null;
        double closestDist = 100;  // max screen distance

        for (WorldPosition npcPos : npcResult.asList()) {
            if (npcPos.distanceTo(playerPos) > 15) continue;

            Polygon tileCube = script.getSceneProjector().getTileCube(npcPos, 70);
            if (tileCube == null) continue;

            Rectangle npcBounds = tileCube.getBounds();
            int npcCenterX = npcBounds.x + npcBounds.width / 2;
            // health bar is above NPC, so compare to top of tile cube
            int npcTopY = npcBounds.y;

            // health bar should be slightly above the NPC
            double dist = Math.sqrt(Math.pow(healthBarX - npcCenterX, 2) + Math.pow(healthBarY - npcTopY, 2));

            if (dist < closestDist) {
                closestDist = dist;
                closestNpc = npcPos;
            }
        }

        if (closestNpc != null) {
            script.log(getClass(), "[healthBarCrash] matched to NPC at " + closestNpc.getX() + "," + closestNpc.getY() +
                    " (screenDist=" + (int)closestDist + ")");
        }

        return closestNpc;
    }

    /**
     * reset tracking state (called after world hop completes)
     */
    public static void resetTrackingState() {
        trackedPlayers.clear();
        crashDetected = false;
        areaOccupiedSince = 0;
        consecutiveSelfReadings = 0;
        consecutiveClearReadings = 0;
        lastHealthBarVisible = false;
        lastHealthBarCheckTime = 0;
        // don't reset lastAttackTime - it's managed by AttackChompy
        // don't reset lastLoginTimestamp here - it's set once at script start
        // don't reset lastHopTimestamp here - it's managed by HopWorld
    }

    /**
     * quick check for any players in the chompy hunting area
     * used on startup/hop to immediately detect occupied world
     * returns true if ANY other player is in the hunting area
     *
     * @param script the script instance
     * @param radius ignored - kept for compatibility, uses area-based detection
     */
    public static boolean hasPlayersOnMinimap(Script script, int radius) {
        // skip if anti-crash disabled in settings
        if (!TidalsChompyHunter.antiCrashEnabled) {
            return false;
        }

        // skip check during post-hop grace period - position may not be stable yet
        long timeSinceHop = System.currentTimeMillis() - lastHopTimestamp;
        if (lastHopTimestamp > 0 && timeSinceHop < POST_HOP_GRACE_MS) {
            script.log(DetectPlayers.class, "skipping occupied check - within hop grace period (" + timeSinceHop + "ms since hop)");
            return false;
        }

        // skip check during post-login grace period - OSMB needs time to identify our position
        // prevents false positive from seeing our own white dot as another player
        long timeSinceLogin = System.currentTimeMillis() - lastLoginTimestamp;
        if (lastLoginTimestamp > 0 && timeSinceLogin < POST_LOGIN_GRACE_MS) {
            script.log(DetectPlayers.class, "skipping occupied check - within login grace period (" + timeSinceLogin + "ms since login)");
            return false;
        }

        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return false;
        }

        // get player (white) dots from minimap
        UIResultList<WorldPosition> result = script.getWidgetManager().getMinimap().getPlayerPositions();
        if (result == null || result.isNotFound()) {
            return false;
        }

        List<WorldPosition> playerPositions = result.asList();
        if (playerPositions.isEmpty()) {
            return false;
        }

        script.log(DetectPlayers.class, "checking " + playerPositions.size() + " minimap dots, our pos: " + formatPos(playerPos));

        // log all detected dots for debugging
        for (int i = 0; i < playerPositions.size(); i++) {
            WorldPosition dot = playerPositions.get(i);
            double dist = dot.distanceTo(playerPos);
            boolean inArea = CHOMPY_HUNTING_AREA.contains(dot);
            script.log(DetectPlayers.class, "  dot[" + i + "]: " + formatPos(dot) +
                       " dist=" + (int)dist + (inArea ? " [IN AREA]" : " [outside]"));
        }

        // handle single dot case - check if it's us or someone else
        if (playerPositions.size() == 1) {
            WorldPosition singleDot = playerPositions.get(0);
            if (singleDot.getPlane() != playerPos.getPlane()) {
                script.log(DetectPlayers.class, "  single dot on different plane - assuming clear");
                return false;
            }

            double distFromUs = singleDot.distanceTo(playerPos);
            script.log(DetectPlayers.class, "  single dot at " + formatPos(singleDot) + ", dist from us: " + (int)distFromUs);

            // if dot is close to our position, it's us - world is clear
            // if dot is far from us, it's another player (OSMB missed our dot) - occupied!
            if (distFromUs <= SELF_FILTER_DISTANCE) {
                script.log(DetectPlayers.class, "  dot is close to us (within " + SELF_FILTER_DISTANCE + " tiles) - that's us, world clear");
                return false;
            } else {
                // dot is far from us - this is another player, OSMB didn't detect our dot
                if (CHOMPY_HUNTING_AREA.contains(singleDot)) {
                    script.log(DetectPlayers.class, "  dot is FAR from us (" + (int)distFromUs + " tiles) and IN HUNTING AREA - OTHER PLAYER!");
                    return true;
                } else {
                    script.log(DetectPlayers.class, "  dot is far from us but outside hunting area - ignoring");
                    return false;
                }
            }
        }

        // 2+ dots detected - find our dot (closest to our reported position)
        WorldPosition ourDot = null;
        double ourDotDistance = Double.MAX_VALUE;
        for (WorldPosition dot : playerPositions) {
            if (dot.getPlane() != playerPos.getPlane()) continue;
            double dist = dot.distanceTo(playerPos);
            if (dist < ourDotDistance) {
                ourDotDistance = dist;
                ourDot = dot;
            }
        }
        script.log(DetectPlayers.class, "  our dot: " + (ourDot != null ? formatPos(ourDot) : "none") + " (dist: " + (int)ourDotDistance + ")");

        // check remaining dots (excluding ours) for hunting area occupancy
        for (WorldPosition otherPlayer : playerPositions) {
            if (otherPlayer.getPlane() != playerPos.getPlane()) {
                continue;
            }

            // skip our own dot
            if (otherPlayer.equals(ourDot)) {
                continue;
            }

            // this is another player - check if they're in our hunting area
            double distFromUs = otherPlayer.distanceTo(playerPos);
            if (CHOMPY_HUNTING_AREA.contains(otherPlayer)) {
                script.log(DetectPlayers.class, "  OTHER PLAYER at " + formatPos(otherPlayer) + " (dist: " + (int)distFromUs + ") - IN HUNTING AREA");
                return true;
            } else {
                script.log(DetectPlayers.class, "  other player at " + formatPos(otherPlayer) + " (dist: " + (int)distFromUs + ") - outside hunting area");
            }
        }

        script.log(DetectPlayers.class, "  no other players in hunting area");
        return false;
    }

    /**
     * check for players in the hunting area
     * (convenience method for runtime crash detection)
     */
    public static boolean hasPlayersOnMinimap(Script script) {
        return hasPlayersOnMinimap(script, 0);  // radius ignored, uses area-based
    }
}
