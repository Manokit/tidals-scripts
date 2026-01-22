package tasks;

import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.script.Script;
import com.osmb.api.utils.UIResultList;
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
    private static final long POST_HOP_GRACE_MS = 5000;
    public static long lastHopTimestamp = 0;

    // state
    public static volatile boolean crashDetected = false;
    private static Map<Integer, DetectedPlayer> trackedPlayers = new HashMap<>();
    private static long lastLogTime = 0;

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
            // empty list - clear tracking
            if (!trackedPlayers.isEmpty()) {
                script.log(getClass(), "all players left hunting area");
                trackedPlayers.clear();
            }
            return false;
        }

        // track players in the chompy hunting area
        for (WorldPosition otherPlayer : playerPositions) {
            // filter same plane only
            if (otherPlayer.getPlane() != playerPos.getPlane()) {
                continue;
            }

            // skip self - our own dot can appear offset from reported position
            double distance = otherPlayer.distanceTo(playerPos);
            if (distance <= SELF_FILTER_DISTANCE) {
                continue;
            }

            // check if player is in the chompy hunting area
            if (CHOMPY_HUNTING_AREA.contains(otherPlayer)) {
                int key = posKey(otherPlayer);

                // track if new player - set random threshold
                if (!trackedPlayers.containsKey(key)) {
                    // randomize threshold between 7-12 seconds for this player
                    crashThresholdMs = MIN_THRESHOLD_MS + (long)(Math.random() * (MAX_THRESHOLD_MS - MIN_THRESHOLD_MS));
                    trackedPlayers.put(key, new DetectedPlayer(otherPlayer));
                    script.log(getClass(), "player entered hunting area: " + formatPos(otherPlayer) + " (threshold: " + crashThresholdMs + "ms)");
                }

                // check if player is crashing (lingering in our area)
                DetectedPlayer tracked = trackedPlayers.get(key);
                if (tracked.isCrashing(crashThresholdMs)) {
                    script.log(getClass(), "=== CRASH DETECTED ===");
                    script.log(getClass(), "player at " + formatPos(otherPlayer) + " in hunting area for " + tracked.getDuration() + "ms");
                    script.log(getClass(), "threshold: " + crashThresholdMs + "ms, exceeded by: " + (tracked.getDuration() - crashThresholdMs) + "ms");
                    crashDetected = true;
                    TidalsChompyHunter.task = "crash detected!";
                    return true;
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
     * reset tracking state (called after world hop completes)
     */
    public static void resetTrackingState() {
        trackedPlayers.clear();
        crashDetected = false;
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
            script.log(DetectPlayers.class, "skipping occupied check - within grace period (" + timeSinceHop + "ms since hop)");
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

        script.log(DetectPlayers.class, "checking " + playerPositions.size() + " minimap dots for hunting area occupancy");

        // check if any player dot is in the hunting area (excluding self)
        for (WorldPosition otherPlayer : playerPositions) {
            if (otherPlayer.getPlane() != playerPos.getPlane()) {
                continue;
            }

            double distance = otherPlayer.distanceTo(playerPos);

            // skip self - our own dot can appear offset from reported position
            if (distance <= SELF_FILTER_DISTANCE) {
                script.log(DetectPlayers.class, "  dot at " + formatPos(otherPlayer) + " - likely self, skipping");
                continue;
            }

            // check if player is in the chompy hunting area
            if (CHOMPY_HUNTING_AREA.contains(otherPlayer)) {
                script.log(DetectPlayers.class, "  dot at " + formatPos(otherPlayer) + " - IN HUNTING AREA");
                return true;
            }
        }

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
