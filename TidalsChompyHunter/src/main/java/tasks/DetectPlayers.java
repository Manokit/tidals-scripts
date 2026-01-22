package tasks;

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
 * detects nearby players and tracks how long they linger in detection radius
 * sets crashDetected flag when player exceeds threshold (crashing behavior)
 */
public class DetectPlayers extends Task {

    // configurable settings (defaults match ScriptUI)
    public static int detectionRadius = 9;  // tiles
    public static long crashThresholdMs = 3000;  // 3 seconds

    // self-detection filter - our own dot can appear offset from reported position
    private static final int SELF_FILTER_DISTANCE = 5;

    // post-hop grace period - skip occupied check while OSMB stabilizes position
    private static final long POST_HOP_GRACE_MS = 5000;
    public static long lastHopTimestamp = 0;

    // state
    public static volatile boolean crashDetected = false;
    private static Map<String, DetectedPlayer> trackedPlayers = new HashMap<>();
    private static long lastLogTime = 0;

    public DetectPlayers(Script script) {
        super(script);
    }

    /**
     * convert WorldPosition to string key for HashMap lookup
     * avoids object identity issues with WorldPosition equality
     */
    private static String posKey(WorldPosition pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getPlane();
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
        if (!TidalsChompyHunter.setupComplete) {
            return false;
        }
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return false;
        }

        // clean up stale entries (players who left radius)
        cleanupStaleEntries(playerPos);

        // get current player positions from minimap
        UIResultList<WorldPosition> result = script.getWidgetManager().getMinimap().getPlayerPositions();
        if (result == null || result.isNotFound()) {
            // no players detected
            if (!trackedPlayers.isEmpty()) {
                script.log(getClass(), "all players left radius");
                trackedPlayers.clear();
            }
            return false;
        }

        List<WorldPosition> playerPositions = result.asList();
        if (playerPositions.isEmpty()) {
            // empty list - clear tracking
            if (!trackedPlayers.isEmpty()) {
                script.log(getClass(), "all players left radius");
                trackedPlayers.clear();
            }
            return false;
        }

        // track new players and check durations
        for (WorldPosition otherPlayer : playerPositions) {
            // filter same plane only
            if (otherPlayer.getPlane() != playerPos.getPlane()) {
                continue;
            }

            // calculate distance
            double distance = otherPlayer.distanceTo(playerPos);

            // skip self - our own dot can appear offset from reported position
            if (distance <= SELF_FILTER_DISTANCE) {
                continue;
            }

            // check if in detection radius
            if (distance <= detectionRadius) {
                String key = posKey(otherPlayer);

                // track if new player
                if (!trackedPlayers.containsKey(key)) {
                    trackedPlayers.put(key, new DetectedPlayer(otherPlayer));
                    script.log(getClass(), "player entered radius: " + key + " (distance: " + distance + ")");
                }

                // check if player is crashing (lingering)
                DetectedPlayer tracked = trackedPlayers.get(key);
                if (tracked.isCrashing(crashThresholdMs)) {
                    script.log(getClass(), "=== CRASH DETECTED ===");
                    script.log(getClass(), "player at " + key + " lingered for " + tracked.getDuration() + "ms");
                    script.log(getClass(), "threshold: " + crashThresholdMs + "ms, exceeded by: " + (tracked.getDuration() - crashThresholdMs) + "ms");
                    crashDetected = true;
                    TidalsChompyHunter.task = "crash detected!";
                    return true;
                }
            }
        }

        // update status if tracking players
        if (!trackedPlayers.isEmpty()) {
            TidalsChompyHunter.task = "monitoring " + trackedPlayers.size() + " nearby player(s)";

            // log player durations every 3 seconds to avoid spam
            long now = System.currentTimeMillis();
            if (now - lastLogTime >= 3000) {
                for (DetectedPlayer player : trackedPlayers.values()) {
                    script.log(getClass(), "tracking player at " + posKey(player.getPosition()) +
                               " for " + player.getDuration() + "ms / " + crashThresholdMs + "ms");
                }
                lastLogTime = now;
            }
        }

        return false;
    }

    /**
     * remove players who left the detection radius
     */
    private void cleanupStaleEntries(WorldPosition playerPos) {
        trackedPlayers.entrySet().removeIf(entry -> {
            WorldPosition trackedPos = entry.getValue().getPosition();
            double distance = trackedPos.distanceTo(playerPos);

            // remove if outside radius
            if (distance > detectionRadius) {
                script.log(getClass(), "player left radius at " + entry.getKey() + " after " +
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
     * quick check for any players on minimap (white dots)
     * used on startup/hop to immediately detect occupied world
     * returns true if ANY other player is visible on minimap within radius
     *
     * @param script the script instance
     * @param radius max distance to check (use large value like 50 for full minimap)
     */
    public static boolean hasPlayersOnMinimap(Script script, int radius) {
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

        script.log(DetectPlayers.class, "checking " + playerPositions.size() + " minimap dots, our pos: " + posKey(playerPos));

        // check if any player dot is within radius (excluding self)
        for (WorldPosition otherPlayer : playerPositions) {
            if (otherPlayer.getPlane() != playerPos.getPlane()) {
                continue;
            }

            double distance = otherPlayer.distanceTo(playerPos);

            // skip self - our own dot can appear offset from reported position
            if (distance <= SELF_FILTER_DISTANCE) {
                script.log(DetectPlayers.class, "  dot at " + posKey(otherPlayer) + " distance " + (int)distance + " - likely self, skipping");
                continue;
            }

            // any other player within specified radius = occupied world
            if (distance <= radius) {
                script.log(DetectPlayers.class, "  dot at " + posKey(otherPlayer) + " distance " + (int)distance + " - OTHER PLAYER");
                return true;
            }
        }

        return false;
    }

    /**
     * check for players using the configured detection radius
     * (convenience method for runtime crash detection)
     */
    public static boolean hasPlayersOnMinimap(Script script) {
        return hasPlayersOnMinimap(script, detectionRadius);
    }
}
