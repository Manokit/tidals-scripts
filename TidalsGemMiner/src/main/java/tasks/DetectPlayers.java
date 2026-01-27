package tasks;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.utils.RandomUtils;
import main.TidalsGemMiner;
import utils.DetectedPlayer;
import utils.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * detects players in the gem mining area and triggers hop when occupied
 * uses zone-based timeouts: shorter for mining area, longer for exit zones (ladder/deposit)
 * players near exits are likely leaving, players in mining area are potential crashers
 */
public class DetectPlayers extends Task {

    // zone definitions for underground mine
    // exit zones get longer timeouts (players passing through)
    private static final RectangleArea LADDER_ZONE = new RectangleArea(2838, 9387, 2, 2, 0);
    private static final RectangleArea DEPOSIT_ZONE = new RectangleArea(2841, 9382, 3, 3, 0);
    // full mining area (threat zone) - players lingering here are crashers
    private static final RectangleArea MINING_AREA = new RectangleArea(2825, 9377, 26, 24, 0);

    // zone timeout thresholds
    private static final long EXIT_ZONE_MIN_MS = 15000;   // 15s min for ladder/deposit zones
    private static final long EXIT_ZONE_MAX_MS = 25000;   // 25s max for ladder/deposit zones
    private static final long MINING_ZONE_MIN_MS = 10000; // 10s min for mining area
    private static final long MINING_ZONE_MAX_MS = 16000; // 16s max for mining area

    // current crash threshold (randomized per detection start)
    public static long crashThresholdMs = 12000;

    // self-detection filter - our own dot can appear offset from reported position
    private static final int SELF_FILTER_DISTANCE = 3;

    // post-hop grace period - skip detection while OSMB stabilizes position
    private static final long POST_HOP_GRACE_MS = 10000;
    public static long lastHopTimestamp = 0;

    // post-login grace period - skip detection while OSMB identifies our position
    private static final long POST_LOGIN_GRACE_MS = 10000;
    public static long lastLoginTimestamp = 0;

    // state
    public static volatile boolean crashDetected = false;
    private static Map<Integer, DetectedPlayer> trackedPlayers = new HashMap<>();
    private static long lastLogTime = 0;

    // anti-jitter: require multiple consecutive "it's us" readings before clearing
    private static int consecutiveSelfReadings = 0;
    private static final int SELF_READINGS_TO_CLEAR = 5;

    // persistent area occupied tracking - doesn't reset when dot position jitters
    private static long areaOccupiedSince = 0;
    private static int consecutiveClearReadings = 0;
    private static final int CLEAR_READINGS_TO_RESET = 10;

    // track which zone type triggered detection (for timeout selection)
    private static boolean inExitZone = false;

    public DetectPlayers(Script script) {
        super(script);
    }

    /**
     * convert WorldPosition to integer key for HashMap lookup
     * uses bit packing: plane(2 bits) | y(15 bits) | x(15 bits) = 32 bits
     */
    private static int posKey(WorldPosition pos) {
        return (pos.getPlane() << 30) | ((pos.getY() & 0x7FFF) << 15) | (pos.getX() & 0x7FFF);
    }

    /**
     * format position for logging
     */
    private static String formatPos(WorldPosition pos) {
        return pos.getX() + "," + pos.getY();
    }

    /**
     * classify which zone a position is in
     * priority: LADDER -> DEPOSIT -> MINING -> OUTSIDE
     */
    private static ZoneType classifyZone(WorldPosition pos) {
        if (LADDER_ZONE.contains(pos)) return ZoneType.LADDER;
        if (DEPOSIT_ZONE.contains(pos)) return ZoneType.DEPOSIT;
        if (MINING_AREA.contains(pos)) return ZoneType.MINING;
        return ZoneType.OUTSIDE;
    }

    private enum ZoneType {
        LADDER,   // exit zone - 10-15s timeout
        DEPOSIT,  // exit zone - 10-15s timeout
        MINING,   // threat zone - 6-10s timeout
        OUTSIDE   // ignored
    }

    @Override
    public boolean activate() {
        // never activate as a task - detection runs via runDetection() from poll()
        return false;
    }

    @Override
    public boolean execute() {
        // unused - detection runs via runDetection()
        return false;
    }

    /**
     * run player detection - call from main script's poll() loop
     * returns true if crash detected this frame
     */
    public boolean runDetection() {
        // skip if anti-crash disabled
        if (!TidalsGemMiner.antiCrashEnabled) {
            return false;
        }

        if (!TidalsGemMiner.setupDone) {
            return false;
        }

        // skip during grace periods
        long now = System.currentTimeMillis();
        if (lastHopTimestamp > 0 && (now - lastHopTimestamp) < POST_HOP_GRACE_MS) {
            return false;
        }
        if (lastLoginTimestamp > 0 && (now - lastLoginTimestamp) < POST_LOGIN_GRACE_MS) {
            return false;
        }

        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return false;
        }

        // clean up stale entries
        cleanupStaleEntries();

        // get player positions from minimap
        UIResultList<WorldPosition> result = script.getWidgetManager().getMinimap().getPlayerPositions();
        if (result == null || result.isNotFound()) {
            handleClearReading("no minimap result");
            return false;
        }

        List<WorldPosition> playerPositions = result.asList();
        if (playerPositions.isEmpty()) {
            handleClearReading("empty player list");
            return false;
        }

        // verbose logging of all dots
        long currentTime = System.currentTimeMillis();
        if (TidalsGemMiner.VERBOSE_LOGGING && currentTime - lastLogTime >= 3000) {
            script.log(getClass(), "[verbose] " + playerPositions.size() + " dot(s), our pos: " + formatPos(playerPos) +
                       ", timer: " + (areaOccupiedSince > 0 ? (currentTime - areaOccupiedSince) + "ms" : "inactive") +
                       ", clearCount: " + consecutiveClearReadings);
            for (int i = 0; i < playerPositions.size(); i++) {
                WorldPosition dot = playerPositions.get(i);
                double dist = dot.distanceTo(playerPos);
                ZoneType zone = classifyZone(dot);
                script.log(getClass(), "  dot[" + i + "]: " + formatPos(dot) +
                           " dist=" + String.format("%.1f", dist) + " zone=" + zone);
            }
            lastLogTime = currentTime;
        }

        // handle single dot case
        if (playerPositions.size() == 1) {
            return handleSingleDot(playerPositions.get(0), playerPos, currentTime);
        }

        // multiple dots - find and filter our own
        return handleMultipleDots(playerPositions, playerPos, currentTime);
    }

    /**
     * handle clear reading (no threats detected)
     * increments clear counter and resets timer after enough consecutive clears
     */
    private void handleClearReading(String reason) {
        consecutiveClearReadings++;
        consecutiveSelfReadings = 0;  // reset self counter on any clear

        if (areaOccupiedSince > 0) {
            // timer is running - check if we should reset it
            if (consecutiveClearReadings >= CLEAR_READINGS_TO_RESET) {
                long duration = System.currentTimeMillis() - areaOccupiedSince;
                script.log(getClass(), "TIMER RESET - area clear for " + consecutiveClearReadings +
                           " readings (" + reason + "), was running for " + duration + "ms");
                areaOccupiedSince = 0;
                trackedPlayers.clear();
                inExitZone = false;
                consecutiveClearReadings = 0;
            } else if (TidalsGemMiner.VERBOSE_LOGGING) {
                script.log(getClass(), "[verbose] clear reading (" + reason + "), count: " +
                           consecutiveClearReadings + "/" + CLEAR_READINGS_TO_RESET);
            }
        }
    }

    /**
     * handle single dot detection - is it us or another player?
     */
    private boolean handleSingleDot(WorldPosition singleDot, WorldPosition playerPos, long currentTime) {
        if (singleDot.getPlane() != playerPos.getPlane()) {
            handleClearReading("single dot different plane");
            return false;
        }

        double distFromUs = singleDot.distanceTo(playerPos);

        // if dot is close, it's us - area is clear
        if (distFromUs <= SELF_FILTER_DISTANCE) {
            consecutiveSelfReadings++;
            handleClearReading("single dot is us (dist=" + String.format("%.1f", distFromUs) + ")");

            // extra reset after sustained self readings
            if (consecutiveSelfReadings >= SELF_READINGS_TO_CLEAR && areaOccupiedSince > 0) {
                long duration = System.currentTimeMillis() - areaOccupiedSince;
                script.log(getClass(), "TIMER RESET - confirmed just us after " + consecutiveSelfReadings +
                           " readings, timer was " + duration + "ms");
                areaOccupiedSince = 0;
                trackedPlayers.clear();
                inExitZone = false;
                consecutiveSelfReadings = 0;
                consecutiveClearReadings = 0;
            }
            return false;
        }

        // dot is far from us - another player
        consecutiveSelfReadings = 0;
        consecutiveClearReadings = 0;
        return processOtherPlayer(singleDot, currentTime);
    }

    /**
     * handle multiple dot detection
     */
    private boolean handleMultipleDots(List<WorldPosition> playerPositions, WorldPosition playerPos, long currentTime) {
        // find our dot (closest to our position)
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

        // process other players and track if any are threats
        boolean anyThreatFound = false;
        boolean crashTriggered = false;

        for (WorldPosition otherPlayer : playerPositions) {
            if (otherPlayer.getPlane() != playerPos.getPlane()) continue;
            if (otherPlayer.equals(ourDot)) continue;

            ZoneType zone = classifyZone(otherPlayer);
            if (zone != ZoneType.OUTSIDE) {
                anyThreatFound = true;
                if (processOtherPlayer(otherPlayer, currentTime)) {
                    crashTriggered = true;
                }
            }
        }

        // if no threats found among other dots, area is clear
        if (!anyThreatFound) {
            handleClearReading("no other dots in mining area");
        }

        // log tracked players periodically (always, not just verbose)
        if (!trackedPlayers.isEmpty() && areaOccupiedSince > 0) {
            long timerDuration = currentTime - areaOccupiedSince;
            if (currentTime - lastLogTime >= 3000) {
                TidalsGemMiner.task = "player in area...";
                script.log(getClass(), "TRACKING: timer=" + timerDuration + "ms / " + crashThresholdMs + "ms threshold, " +
                           trackedPlayers.size() + " player(s) in area");
                lastLogTime = currentTime;
            }
        }

        return crashTriggered;
    }

    /**
     * process detection of another player (not us)
     * returns true if crash threshold exceeded
     */
    private boolean processOtherPlayer(WorldPosition otherPos, long currentTime) {
        ZoneType zone = classifyZone(otherPos);

        // ignore players outside mining area
        if (zone == ZoneType.OUTSIDE) {
            return false;
        }

        consecutiveClearReadings = 0;

        // start tracking if not already
        if (areaOccupiedSince == 0) {
            areaOccupiedSince = currentTime;
            inExitZone = (zone == ZoneType.LADDER || zone == ZoneType.DEPOSIT);

            // set threshold based on zone
            if (inExitZone) {
                crashThresholdMs = RandomUtils.gaussianRandom(
                    (int) EXIT_ZONE_MIN_MS, (int) EXIT_ZONE_MAX_MS,
                    (int) ((EXIT_ZONE_MIN_MS + EXIT_ZONE_MAX_MS) / 2), 1500);
                script.log(getClass(), "TIMER START - player at " + formatPos(otherPos) +
                           " in " + zone + " zone (exit timeout: " + crashThresholdMs + "ms)");
            } else {
                crashThresholdMs = RandomUtils.gaussianRandom(
                    (int) MINING_ZONE_MIN_MS, (int) MINING_ZONE_MAX_MS,
                    (int) ((MINING_ZONE_MIN_MS + MINING_ZONE_MAX_MS) / 2), 1000);
                script.log(getClass(), "TIMER START - player at " + formatPos(otherPos) +
                           " in MINING zone (threat timeout: " + crashThresholdMs + "ms)");
            }
        } else {
            // player moved from exit zone to mining zone - use shorter timeout
            if (inExitZone && zone == ZoneType.MINING) {
                inExitZone = false;
                long newThreshold = RandomUtils.gaussianRandom(
                    (int) MINING_ZONE_MIN_MS, (int) MINING_ZONE_MAX_MS,
                    (int) ((MINING_ZONE_MIN_MS + MINING_ZONE_MAX_MS) / 2), 1000);
                // only shorten if new threshold is actually shorter
                if (newThreshold < crashThresholdMs) {
                    script.log(getClass(), "player moved to MINING zone - shortening timeout from " +
                               crashThresholdMs + "ms to " + newThreshold + "ms");
                    crashThresholdMs = newThreshold;
                }
            }
        }

        // check if threshold exceeded
        long occupiedDuration = currentTime - areaOccupiedSince;
        if (occupiedDuration >= crashThresholdMs) {
            script.log(getClass(), "=== CRASH DETECTED === timer=" + occupiedDuration + "ms >= threshold=" + crashThresholdMs + "ms");
            crashDetected = true;
            TidalsGemMiner.task = "crash detected!";
            return true;
        }

        // track in map for logging
        int key = posKey(otherPos);
        if (!trackedPlayers.containsKey(key)) {
            trackedPlayers.put(key, new DetectedPlayer(otherPos));
        }

        return false;
    }

    /**
     * remove players who left the mining area
     */
    private void cleanupStaleEntries() {
        trackedPlayers.entrySet().removeIf(entry -> {
            WorldPosition trackedPos = entry.getValue().getPosition();
            if (!MINING_AREA.contains(trackedPos)) {
                script.log(getClass(), "player left mining area at " + formatPos(trackedPos) +
                           " after " + entry.getValue().getDuration() + "ms");
                return true;
            }
            return false;
        });
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
        inExitZone = false;
    }

    /**
     * check for players in the mining area at startup
     * uses immediate check without waiting for timeout
     * returns true if other players are in the mining area
     */
    public static boolean hasPlayersInMine(Script script) {
        // skip if anti-crash disabled
        if (!TidalsGemMiner.antiCrashEnabled) {
            return false;
        }

        // skip during grace periods
        long now = System.currentTimeMillis();
        if (lastHopTimestamp > 0 && (now - lastHopTimestamp) < POST_HOP_GRACE_MS) {
            script.log(DetectPlayers.class, "skipping occupied check - within hop grace period");
            return false;
        }
        if (lastLoginTimestamp > 0 && (now - lastLoginTimestamp) < POST_LOGIN_GRACE_MS) {
            script.log(DetectPlayers.class, "skipping occupied check - within login grace period");
            return false;
        }

        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return false;
        }

        UIResultList<WorldPosition> result = script.getWidgetManager().getMinimap().getPlayerPositions();
        if (result == null || result.isNotFound()) {
            return false;
        }

        List<WorldPosition> playerPositions = result.asList();
        if (playerPositions.isEmpty()) {
            return false;
        }

        script.log(DetectPlayers.class, "checking " + playerPositions.size() + " minimap dots at startup");

        // single dot case - check if it's us
        if (playerPositions.size() == 1) {
            WorldPosition singleDot = playerPositions.get(0);
            if (singleDot.getPlane() != playerPos.getPlane()) {
                return false;
            }

            double distFromUs = singleDot.distanceTo(playerPos);
            if (distFromUs <= SELF_FILTER_DISTANCE) {
                script.log(DetectPlayers.class, "single dot is us - world clear");
                return false;
            }

            // dot is far - another player
            ZoneType zone = classifyZone(singleDot);
            if (zone != ZoneType.OUTSIDE) {
                script.log(DetectPlayers.class, "OTHER PLAYER at " + formatPos(singleDot) + " in " + zone + " zone - world occupied!");
                return true;
            }
            return false;
        }

        // multiple dots - find our dot and check others
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

        for (WorldPosition otherPlayer : playerPositions) {
            if (otherPlayer.getPlane() != playerPos.getPlane()) continue;
            if (otherPlayer.equals(ourDot)) continue;

            ZoneType zone = classifyZone(otherPlayer);
            if (zone != ZoneType.OUTSIDE) {
                script.log(DetectPlayers.class, "OTHER PLAYER at " + formatPos(otherPlayer) + " in " + zone + " zone - world occupied!");
                return true;
            }
        }

        script.log(DetectPlayers.class, "no other players in mining area - world clear");
        return false;
    }
}
