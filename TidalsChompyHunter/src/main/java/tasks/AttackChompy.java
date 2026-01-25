package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.area.impl.PolyArea;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.walker.WalkConfig;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;
// MinimapArrowResult import removed - using pixel cluster detection only
import com.osmb.api.ui.overlay.HealthOverlay;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.visual.PixelCluster;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import main.TidalsChompyHunter;
import utils.SpawnedChompy;
import utils.Task;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * detects chompy spawns via arrow marker and initiates combat
 */
public class AttackChompy extends Task {

    // constants
    private static final int MAX_ATTACK_ATTEMPTS = 10;
    private static final int TILE_CUBE_HEIGHT = 70;
    private static final double SHRINK_FACTOR = 0.9;
    private static final int SCAN_RANGE = 15;
    private static final int MAX_TRACKED_CHOMPIES = 3;
    private static final int KILL_CONFIRMATION_TIMEOUT_MS = 20000;
    private static final int PLUCK_MAX_ATTEMPTS = 5;

    // pre-inflation constants
    private static final int BLOATED_TOAD = 2875;
    private static final int MAX_INVENTORY_TOADS = 3;
    private static final int OGRE_BELLOWS_3 = 2872;
    private static final int OGRE_BELLOWS_2 = 2873;
    private static final int OGRE_BELLOWS_1 = 2874;
    private static final int TILE_CUBE_HEIGHT_TOAD = 40;
    private static final int MONITORING_POLL_MS = 600;

    // bloated toad verification within tileCube bounds - high tolerance since area is constrained
    private static final SearchablePixel BLOATED_TOAD_BOUNDED = new SearchablePixel(
            -8346826,  // RGB int from testing
            new SingleThresholdComparator(20),  // high tolerance - searching small bounded area
            ColorModel.RGB
    );
    private static final int BLOATED_BOUNDED_MAX_DISTANCE = 20;
    private static final int BLOATED_BOUNDED_MIN_SIZE = 3;

    // dead chompy verification within tileCube bounds - for verifying corpses at ignored positions
    private static final SearchablePixel DEAD_CHOMPY_BOUNDED = new SearchablePixel(
            -2500192,  // RGB int from user testing
            new SingleThresholdComparator(10),  // tolerance 10
            ColorModel.RGB
    );
    private static final int DEAD_CHOMPY_BOUNDED_MAX_DISTANCE = 10;
    private static final int DEAD_CHOMPY_BOUNDED_MIN_SIZE = 3;

    // area where we can reliably verify tracked positions (toads/corpses visible on screen)
    // when outside this area (e.g., at swamp bubbles), UI/camera blocks the drop area
    // verification would incorrectly remove positions we can't see
    private static final RectangleArea VERIFICATION_AREA = new RectangleArea(2383, 3043, 14, 9, 0);

    // drop area where bloated toads are placed - chompies spawn here
    private static final PolyArea TOAD_DROP_AREA = new PolyArea(List.of(
        new WorldPosition(2391, 3044, 0),
        new WorldPosition(2389, 3043, 0),
        new WorldPosition(2385, 3046, 0),
        new WorldPosition(2388, 3047, 0),
        new WorldPosition(2390, 3046, 0),
        new WorldPosition(2391, 3046, 0),
        new WorldPosition(2392, 3046, 0),
        new WorldPosition(2393, 3045, 0),
        new WorldPosition(2393, 3044, 0),
        new WorldPosition(2392, 3044, 0),
        new WorldPosition(2390, 3044, 0),
        new WorldPosition(2388, 3043, 0),
        new WorldPosition(2387, 3043, 0),
        new WorldPosition(2386, 3043, 0),
        new WorldPosition(2385, 3044, 0),
        new WorldPosition(2385, 3045, 0),
        new WorldPosition(2386, 3044, 0)
    ));

    // chompy sprite detection (RGB pixel cluster fallback)
    private static final SearchablePixel CHOMPY_SPRITE = new SearchablePixel(
            -2566241,  // RGB int from user
            new SingleThresholdComparator(8),
            ColorModel.RGB
    );
    private static final int CHOMPY_CLUSTER_MAX_DISTANCE = 5;
    private static final int CHOMPY_CLUSTER_MIN_SIZE = 5;

    // shared state
    public static boolean inCombat = false;
    public static WorldPosition currentChompyPosition = null;

    // combat timeout - safety valve for stuck combat state
    private static final long COMBAT_TIMEOUT_MS = 30000; // 30 seconds max
    private static long combatStartTime = 0;

    // kill detection via chat message (set by main script onNewFrame)
    public static volatile boolean killDetected = false;

    // pluck detection via chat message (set by main script onNewFrame)
    public static volatile boolean pluckStarted = false;

    // tracked chompies for spawn-order priority
    private static List<SpawnedChompy> trackedChompies = new ArrayList<>();

    // positions to ignore (corpses we already checked) - uses integer keys for performance
    private static final long IGNORE_DURATION_MS = 30000; // ignore for 30s
    private static java.util.Map<Integer, Long> ignoredPositionTimestamps = new java.util.HashMap<>();

    // detection cooldown when only corpses found
    private static long lastNoChompyTime = 0;
    private static final long NO_CHOMPY_COOLDOWN_MS = 3000; // 3 second cooldown

    // pixel cluster cache - avoids repeated expensive full-screen scans
    private static final long CLUSTER_CACHE_TTL_MS = 300; // cache for 300ms
    private static List<PixelCluster> cachedChompyClusters = null;
    private static long cachedChompyClustersTime = 0;

    public AttackChompy(Script script) {
        super(script);
    }

    /**
     * convert WorldPosition to integer key for HashMap lookup
     * uses bit packing for performance - avoids string concatenation
     */
    public static int posKey(WorldPosition pos) {
        return (pos.getPlane() << 30) | ((pos.getY() & 0x7FFF) << 15) | (pos.getX() & 0x7FFF);
    }

    /**
     * check if a position is in the ignored list (known corpse)
     */
    public static boolean isPositionIgnored(int posKey) {
        return ignoredPositionTimestamps.containsKey(posKey);
    }

    /**
     * remove a position from the ignored list (corpse despawned)
     */
    public static void removeIgnoredPosition(int posKey) {
        ignoredPositionTimestamps.remove(posKey);
    }

    /**
     * add a position to the ignored list (called when we detect someone else's chompy)
     * prevents us from trying to attack or pluck a chompy that isn't ours
     */
    public static void addIgnoredPosition(WorldPosition pos) {
        if (pos != null) {
            ignoredPositionTimestamps.put(posKey(pos), System.currentTimeMillis());
        }
    }

    /**
     * get all currently ignored positions for paint overlay
     * converts integer keys back to WorldPositions
     */
    public static List<WorldPosition> getIgnoredPositions() {
        List<WorldPosition> positions = new ArrayList<>();
        for (Integer key : ignoredPositionTimestamps.keySet()) {
            // unpack: x = bits 0-14, y = bits 15-29, plane = bits 30-31
            int x = key & 0x7FFF;
            int y = (key >> 15) & 0x7FFF;
            int plane = (key >> 30) & 0x3;
            positions.add(new WorldPosition(x, y, plane));
        }
        return positions;
    }

    /**
     * format position for logging (only used in log statements, not hot paths)
     */
    private static String formatPos(WorldPosition pos) {
        return pos.getX() + "," + pos.getY();
    }

    @Override
    public boolean activate() {
        // CRITICAL: don't activate if crash detected - let HopWorld handle it
        if (DetectPlayers.crashDetected) {
            return false;
        }

        // only activate after setup is complete
        if (!TidalsChompyHunter.setupComplete) {
            return false;
        }

        // don't activate if already in combat (with timeout safety valve)
        if (inCombat) {
            // safety valve: if combat has lasted more than 30s, something is stuck
            long combatDuration = System.currentTimeMillis() - combatStartTime;
            if (combatDuration > COMBAT_TIMEOUT_MS) {
                script.log(getClass(), "[activate] STUCK STATE DETECTED - combat timeout exceeded (" + combatDuration + "ms) - resetting");
                inCombat = false;
                currentChompyPosition = null;
                // continue to normal activation
            } else {
                script.log(getClass(), "[activate] skipping - already in combat (" + (combatDuration / 1000) + "s)");
                return false;
            }
        }

        // cooldown after finding no chompies (all corpses)
        if (lastNoChompyTime > 0 && System.currentTimeMillis() - lastNoChompyTime < NO_CHOMPY_COOLDOWN_MS) {
            long remaining = NO_CHOMPY_COOLDOWN_MS - (System.currentTimeMillis() - lastNoChompyTime);
            script.log(getClass(), "[activate] in cooldown (" + remaining + "ms remaining)");
            return false; // still in cooldown
        }

        // clean up expired ignores
        long now = System.currentTimeMillis();
        int beforeClean = ignoredPositionTimestamps.size();
        ignoredPositionTimestamps.entrySet().removeIf(e -> now - e.getValue() > IGNORE_DURATION_MS);
        int afterClean = ignoredPositionTimestamps.size();
        if (beforeClean != afterClean) {
            script.log(getClass(), "[activate] cleaned " + (beforeClean - afterClean) + " expired ignores, " + afterClean + " remain");
        }

        // remove stale and ignored entries from tracking
        int beforeStale = trackedChompies.size();
        trackedChompies.removeIf(SpawnedChompy::isStale);
        trackedChompies.removeIf(c -> ignoredPositionTimestamps.containsKey(posKey(c.getPosition())));
        int afterStale = trackedChompies.size();
        if (beforeStale != afterStale) {
            script.log(getClass(), "[activate] removed " + (beforeStale - afterStale) + " stale/ignored tracked chompies");
        }

        // validate tracked corpses - remove positions where NPC despawned
        validateTrackedCorpses();

        // activate if we have valid tracked chompies
        if (!trackedChompies.isEmpty()) {
            lastNoChompyTime = 0; // reset cooldown on success
            script.log(getClass(), "[activate] have " + trackedChompies.size() + " tracked chompies - activating");
            return true;
        }

        // scan for chompy immediately - can detect from anywhere on screen
        script.log(getClass(), "[activate] scanning for chompy...");
        WorldPosition chompy = findChompyByPixelCluster();
        if (chompy != null) {
            lastNoChompyTime = 0; // reset cooldown on success
            script.log(getClass(), "[activate] chompy found at " + chompy.getX() + "," + chompy.getY() + " - activating");
            return true;
        }

        // no valid chompy found - set cooldown (ammo updated via BuffOverlay in main script)
        script.log(getClass(), "[activate] no chompy found, entering " + NO_CHOMPY_COOLDOWN_MS + "ms cooldown");
        lastNoChompyTime = System.currentTimeMillis();
        return false;
    }

    /**
     * walk back to drop area before scanning for chompies
     * chompies spawn where bloated toads are dropped
     */
    private boolean walkToDropArea() {
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) return false;

        // already in drop area - no need to walk
        if (TOAD_DROP_AREA.contains(playerPos)) {
            return true;
        }

        WorldPosition target = TOAD_DROP_AREA.getRandomPosition();
        if (target == null) return false;

        script.log(getClass(), "walking back to drop area");

        // use breakCondition to stop early when we reach the area
        WalkConfig config = new WalkConfig.Builder()
                .setWalkMethods(false, true)
                .breakDistance(2)
                .tileRandomisationRadius(1)
                .timeout(5000)
                .breakCondition(() -> {
                    WorldPosition pos = script.getWorldPosition();
                    return pos != null && TOAD_DROP_AREA.contains(pos);
                })
                .build();

        return script.getWalker().walkTo(target, config);
    }

    @Override
    public boolean execute() {
        // CRITICAL: abort immediately if crash detected
        if (DetectPlayers.crashDetected) {
            script.log(getClass(), "[execute] ABORTING - crash detected, yielding to HopWorld");
            return false;
        }

        TidalsChompyHunter.task = "hunting chompy";
        script.log(getClass(), "[execute] === CHOMPY HUNT CYCLE START ===");

        // detect chompy position
        script.log(getClass(), "[execute] running fresh detection...");
        WorldPosition chompyPos = detectChompy();
        if (chompyPos != null) {
            script.log(getClass(), "[execute] fresh detection found chompy at " + chompyPos.getX() + "," + chompyPos.getY());
            // track any newly detected chompy
            trackNewChompy(chompyPos);
        } else {
            script.log(getClass(), "[execute] fresh detection returned null");
        }

        // use fresh detection for targeting (not stale tracked positions)
        // fresh detection is always accurate - tracked positions become stale as chompy moves
        WorldPosition targetPos = chompyPos;

        // fall back to tracking only if no fresh detection
        if (targetPos == null) {
            script.log(getClass(), "[execute] falling back to tracked chompies...");
            targetPos = getNextChompyToAttack();
            if (targetPos != null) {
                script.log(getClass(), "[execute] using tracked position: " + targetPos.getX() + "," + targetPos.getY());
            }
        }

        if (targetPos == null) {
            // no chompy to attack - monitoring mode
            script.log(getClass(), "[execute] no target found - entering monitoring mode");
            TidalsChompyHunter.task = "monitoring for chompy";

            // pre-inflate during downtime
            if (tryPreInflate()) {
                script.log(getClass(), "[execute] pre-inflated toad during monitoring");
            }

            // brief monitoring wait (prevents tight loop)
            script.log(getClass(), "[execute] waiting " + MONITORING_POLL_MS + "ms before next scan");
            script.pollFramesUntil(() -> false, MONITORING_POLL_MS);
            return true;  // return true to keep monitoring
        }

        script.log(getClass(), "[execute] TARGET ACQUIRED: " + targetPos.getX() + "," + targetPos.getY());
        currentChompyPosition = targetPos;

        // attack once - no spam, just single tap
        script.log(getClass(), "[execute] initiating attack...");
        boolean attacked = attackChompy(targetPos);
        if (!attacked) {
            // attack verification failed - will retry detection on next poll
            script.log(getClass(), "[execute] attack failed - clearing target and retrying next cycle");
            currentChompyPosition = null;
            int delay = RandomUtils.weightedRandom(300, 1000, 0.002);
            script.log(getClass(), "[execute] waiting " + delay + "ms before retry");
            script.pollFramesUntil(() -> false, delay);
            return true;
        }

        // attack sent - enter combat state with guaranteed cleanup
        inCombat = true;
        combatStartTime = System.currentTimeMillis();

        try {
            TidalsChompyHunter.task = "engaging chompy";
            script.log(getClass(), "[execute] attack sent - waiting up to 3s for combat confirmation via health overlay...");

            HealthOverlay healthOverlay = new HealthOverlay(script);

            // wait for health overlay to appear (confirms combat started)
            long combatWaitStart = System.currentTimeMillis();
            boolean combatStarted = script.pollFramesUntil(() -> {
                boolean visible = healthOverlay.isVisible();
                Integer hp = getHealthOverlayHitpoints(healthOverlay);
                if (visible && hp != null && hp > 0) {
                    return true;
                }
                return false;
            }, 3000);
            long combatWaitTime = System.currentTimeMillis() - combatWaitStart;

            if (!combatStarted) {
                // attack failed to connect - don't count kill, reset and retry
                script.log(getClass(), "[execute] COMBAT NOT CONFIRMED after " + combatWaitTime + "ms - overlay never showed HP > 0");
                script.log(getClass(), "[execute] attack likely missed or target was invalid/moved");
                return true;
            }

            Integer initialHP = getHealthOverlayHitpoints(healthOverlay);
            script.log(getClass(), "[execute] COMBAT CONFIRMED after " + combatWaitTime + "ms - chompy HP: " + initialHP);
            TidalsChompyHunter.task = "killing chompy";
            script.log(getClass(), "[execute] waiting for kill (timeout: " + KILL_CONFIRMATION_TIMEOUT_MS + "ms)...");

            boolean killed = waitForKillConfirmation(healthOverlay);
            if (killed) {
                script.log(getClass(), "[execute] === KILL CONFIRMED ===");
            } else {
                script.log(getClass(), "[execute] kill timed out or failed - combat state will be reset");
            }

            // pluck tracked corpses when no live chompies remain
            if (TidalsChompyHunter.pluckingEnabled && !TidalsChompyHunter.corpsePositions.isEmpty()) {
                if (!hasLiveChompy(script)) {
                    script.log(getClass(), "[execute] no live chompies - plucking " + TidalsChompyHunter.corpsePositions.size() + " corpse(s)");
                    pluckAllTrackedCorpses();
                } else {
                    script.log(getClass(), "[execute] live chompy detected - deferring pluck");
                }
            }
        } finally {
            // GUARANTEED cleanup - prevents stuck inCombat state
            inCombat = false;
            currentChompyPosition = null;
        }

        script.log(getClass(), "[execute] === CHOMPY HUNT CYCLE END ===");
        return true;
    }

    /**
     * track a newly detected chompy position
     * also removes any nearby tracked toad position (chompy ate it to spawn)
     */
    private void trackNewChompy(WorldPosition pos) {
        // remove stale chompies first
        trackedChompies.removeIf(SpawnedChompy::isStale);

        // check if already tracking this position
        for (SpawnedChompy existing : trackedChompies) {
            if (existing.getPosition().equals(pos)) {
                return; // already tracked
            }
        }

        // add new chompy if under max
        if (trackedChompies.size() < MAX_TRACKED_CHOMPIES) {
            trackedChompies.add(new SpawnedChompy(pos));
            script.log(getClass(), "tracking chompy at " + pos + " (" + trackedChompies.size() + " total)");

            // verify all tracked toad positions - remove any that are gone
            verifyAllTrackedToads(script);
        }
    }

    /**
     * verify all tracked toad positions and remove any where toad is no longer visible
     * called before checking ground count to ensure accuracy
     * IMPORTANT: only verifies when player is in VERIFICATION_AREA - when at swamp bubbles,
     * UI/camera blocks the drop area and we'd incorrectly remove positions we can't see
     */
    public static void verifyAllTrackedToads(Script script) {
        if (TidalsChompyHunter.droppedToadPositions.isEmpty()) {
            return;
        }

        // skip verification if player is outside the viewable area
        // (e.g., at swamp bubbles on the east side - UI covers the drop area)
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null || !VERIFICATION_AREA.contains(playerPos)) {
            return; // can't reliably verify from here
        }

        int beforeCount = TidalsChompyHunter.droppedToadPositions.size();

        // check each position and collect ones to remove
        List<WorldPosition> toRemove = new ArrayList<>();
        for (WorldPosition toadPos : TidalsChompyHunter.droppedToadPositions.keySet()) {
            if (!isToadVisibleAt(script, toadPos)) {
                toRemove.add(toadPos);
                script.log(AttackChompy.class, "toad gone at " + toadPos.getX() + "," + toadPos.getY());
            }
        }

        // remove all gone positions
        for (WorldPosition pos : toRemove) {
            TidalsChompyHunter.droppedToadPositions.remove(pos);
        }

        if (!toRemove.isEmpty()) {
            script.log(AttackChompy.class, "verified toads: " + toRemove.size() + " removed, " +
                    TidalsChompyHunter.droppedToadPositions.size() + " remaining (was " + beforeCount + ")");
        }
    }

    /**
     * check if bloated toad sprite is visible at tracked position
     * searches for pixel clusters within the tileCube bounds
     */
    private static boolean isToadVisibleAt(Script script, WorldPosition toadPos) {
        Polygon tileCube = script.getSceneProjector().getTileCube(toadPos, 40);
        if (tileCube == null) {
            return false; // can't verify, assume gone
        }

        Rectangle bounds = tileCube.getBounds();
        if (bounds == null) {
            return false;
        }

        // search for bloated toad sprite within tileCube bounds only
        // high tolerance since we're searching a small constrained area (fewer false positives)
        PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                BLOATED_BOUNDED_MAX_DISTANCE,
                BLOATED_BOUNDED_MIN_SIZE,
                new SearchablePixel[]{BLOATED_TOAD_BOUNDED}
        );

        PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(bounds, query);
        if (result == null) {
            return false;
        }

        List<PixelCluster> clusters = result.getClusters();
        return clusters != null && !clusters.isEmpty();
    }

    /**
     * check if dead chompy sprite is visible at a known ignored position
     * uses bounded pixel cluster search within the tileCube (like toad verification)
     * @param script the script instance
     * @param position the ignored position to verify
     * @return true if corpse still present, false if despawned
     */
    public static boolean isCorpseVisibleAt(Script script, WorldPosition position) {
        Polygon tileCube = script.getSceneProjector().getTileCube(position, TILE_CUBE_HEIGHT);
        if (tileCube == null) {
            return false;
        }

        Rectangle bounds = tileCube.getBounds();
        if (bounds == null) {
            return false;
        }

        // search for dead chompy sprite within tileCube bounds only
        // high tolerance since we're searching a small constrained area
        PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                DEAD_CHOMPY_BOUNDED_MAX_DISTANCE,
                DEAD_CHOMPY_BOUNDED_MIN_SIZE,
                new SearchablePixel[]{DEAD_CHOMPY_BOUNDED}
        );

        PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(bounds, query);
        if (result == null) {
            return false;
        }

        List<PixelCluster> clusters = result.getClusters();
        return clusters != null && !clusters.isEmpty();
    }

    /**
     * get next chompy to attack (oldest first = spawn order priority)
     * filters out positions that are in ignoredPositionTimestamps (known corpses)
     */
    private WorldPosition getNextChompyToAttack() {
        // remove stale entries
        trackedChompies.removeIf(SpawnedChompy::isStale);

        // also remove entries whose positions are now ignored (corpses)
        trackedChompies.removeIf(c -> ignoredPositionTimestamps.containsKey(posKey(c.getPosition())));

        if (trackedChompies.isEmpty()) {
            return null;
        }

        // sort by spawn time (oldest first = longest age)
        trackedChompies.sort(Comparator.comparingLong(SpawnedChompy::getAge).reversed());

        // return oldest (first spawned) chompy
        return trackedChompies.get(0).getPosition();
    }

    /**
     * get current hitpoints from health overlay
     * returns null if overlay not visible or health result unavailable
     */
    private Integer getHealthOverlayHitpoints(HealthOverlay healthOverlay) {
        HealthOverlay.HealthResult healthResult = (HealthOverlay.HealthResult) healthOverlay.getValue(HealthOverlay.HEALTH);
        if (healthResult == null) return null;
        return healthResult.getCurrentHitpoints();
    }

    /**
     * wait for kill confirmation via HP tracking (primary) or chat message (backup)
     * uses try-finally to guarantee combat state reset even if interrupted by AFK/hop
     */
    private boolean waitForKillConfirmation(HealthOverlay healthOverlay) {
        // reset kill flag
        killDetected = false;
        boolean killed = false;

        // store position before finally block clears it - used for plucking
        WorldPosition killPosition = currentChompyPosition;

        // track combat state - must see overlay visible with HP > 0 before counting kills
        final boolean[] wasInCombat = {false};
        final Integer[] lastKnownHP = {null};

        try {
            // wait for kill confirmation via HP tracking
            killed = script.pollFramesUntil(() -> {
                // check chat message (set by main script onNewFrame) - most reliable
                if (killDetected) {
                    script.log(getClass(), "kill detected via chat message");
                    return true;
                }

                Integer currentHP = getHealthOverlayHitpoints(healthOverlay);
                boolean overlayVisible = healthOverlay.isVisible();

                // track if we've ever been in combat (overlay visible with HP > 0)
                if (overlayVisible && currentHP != null && currentHP > 0) {
                    wasInCombat[0] = true;
                    lastKnownHP[0] = currentHP;
                }

                // only count kill if we were actually in combat first
                if (!wasInCombat[0]) {
                    // not in combat yet - keep waiting
                    return false;
                }

                // primary: HP dropped to 0 (chompy dead)
                if (currentHP != null && currentHP == 0) {
                    script.log(getClass(), "kill detected: HP reached 0");
                    return true;
                }

                // secondary: overlay disappeared after being visible (chompy dead or despawned)
                if (!overlayVisible && currentHP == null) {
                    script.log(getClass(), "kill detected: overlay disappeared after combat");
                    return true;
                }

                return false;
            }, KILL_CONFIRMATION_TIMEOUT_MS);

            if (killed) {
                // wait briefly for chatbox total to appear (follows "scratch a notch" message)
                // must wait for value to INCREASE, not just be positive (otherwise subsequent kills pass immediately)
                int previousTotal = TidalsChompyHunter.gameReportedTotalKills;
                script.pollFramesUntil(() -> TidalsChompyHunter.gameReportedTotalKills > previousTotal, 2000);

                // sync kill count from game
                if (TidalsChompyHunter.gameReportedTotalKills > previousTotal) {
                    int sessionKills = TidalsChompyHunter.gameReportedTotalKills - TidalsChompyHunter.initialTotalKills;
                    TidalsChompyHunter.killCount = Math.max(TidalsChompyHunter.killCount, sessionKills);
                    script.log(getClass(), "synced kill count from game: session=" + sessionKills + " total=" + TidalsChompyHunter.gameReportedTotalKills);
                } else {
                    // fallback: increment manually
                    TidalsChompyHunter.killCount++;
                    script.log(getClass(), "kill count incremented manually: " + TidalsChompyHunter.killCount);
                }

                // decrement ground toad count (chompy consumed one toad to spawn)
                if (TidalsChompyHunter.groundToadCount > 0) {
                    TidalsChompyHunter.groundToadCount--;
                    script.log(getClass(), "ground toad count decremented to " + TidalsChompyHunter.groundToadCount);
                }

                // detect corpse position NOW via pixel cluster - chompy moved during combat
                // must scan immediately while sprite is still visible at death location
                // pass killPosition to find cluster nearest to where we were attacking
                WorldPosition corpsePos = findCorpseAtDeath(killPosition);
                if (corpsePos != null) {
                    // safety check: don't re-add already ignored positions
                    if (ignoredPositionTimestamps.containsKey(posKey(corpsePos))) {
                        script.log(getClass(), "corpse at " + corpsePos.getX() + "," + corpsePos.getY() +
                                " already ignored - skipping");
                    } else if (TidalsChompyHunter.corpsePositions.contains(corpsePos)) {
                        script.log(getClass(), "corpse at " + corpsePos.getX() + "," + corpsePos.getY() +
                                " already tracked - skipping duplicate");
                    } else {
                        TidalsChompyHunter.corpsePositions.add(corpsePos);
                        script.log(getClass(), "tracking corpse at death position " + corpsePos.getX() + "," + corpsePos.getY() +
                                " (" + TidalsChompyHunter.corpsePositions.size() + " total)");
                    }
                    // defer plucking - will pluck all corpses when no live chompies remain
                } else {
                    script.log(getClass(), "could not detect corpse position at death - sprite may have disappeared");
                }

                // remove killed chompy from tracking
                removeTrackedChompy(killPosition);
            }
        } finally {
            // GUARANTEED to run even if interrupted by AFK/hop PriorityTaskException
            inCombat = false;
            currentChompyPosition = null;
        }

        return killed;
    }

    /**
     * remove chompy from tracked list by position
     */
    private void removeTrackedChompy(WorldPosition pos) {
        if (pos == null) return;
        trackedChompies.removeIf(c -> c.getPosition().equals(pos));
    }

    /**
     * find corpse position at moment of death using pixel cluster detection
     * called immediately when HP hits 0 to capture actual death location
     * uses attackPosition to find the cluster closest to where we were attacking
     */
    private WorldPosition findCorpseAtDeath(WorldPosition attackPosition) {
        script.log(getClass(), "[corpseDetect] scanning for corpse sprite near attack position...");

        if (attackPosition == null) {
            script.log(getClass(), "[corpseDetect] no attack position provided");
            return null;
        }

        PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                CHOMPY_CLUSTER_MAX_DISTANCE,
                CHOMPY_CLUSTER_MIN_SIZE,
                new SearchablePixel[]{CHOMPY_SPRITE}
        );

        PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(null, query);
        if (result == null) {
            script.log(getClass(), "[corpseDetect] findClusters returned null");
            return null;
        }

        List<PixelCluster> clusters = result.getClusters();
        if (clusters == null || clusters.isEmpty()) {
            script.log(getClass(), "[corpseDetect] no chompy sprite clusters found");
            return null;
        }

        script.log(getClass(), "[corpseDetect] found " + clusters.size() + " sprite clusters");

        // get screen position of where we were attacking
        Polygon attackTileCube = script.getSceneProjector().getTileCube(attackPosition, TILE_CUBE_HEIGHT);
        if (attackTileCube == null) {
            script.log(getClass(), "[corpseDetect] could not project attack position to screen");
            return null;
        }
        Rectangle attackBounds = attackTileCube.getBounds();
        int attackScreenX = attackBounds.x + attackBounds.width / 2;
        int attackScreenY = attackBounds.y + attackBounds.height / 2;
        script.log(getClass(), "[corpseDetect] attack position screen(" + attackScreenX + "," + attackScreenY + ")");

        // get NPC positions from minimap first (need this to filter clusters)
        UIResultList<WorldPosition> npcPositions = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcPositions == null || npcPositions.isNotFound()) {
            script.log(getClass(), "[corpseDetect] no NPC positions from minimap");
            return null;
        }

        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            script.log(getClass(), "[corpseDetect] player position null");
            return null;
        }

        // find the cluster closest to attack position that matches a NON-IGNORED NPC
        // this prevents re-detecting already-plucked corpses
        WorldPosition bestMatch = null;
        double bestClusterDist = Double.MAX_VALUE;

        for (PixelCluster cluster : clusters) {
            Rectangle bounds = cluster.getBounds();
            int clusterX = bounds.x + bounds.width / 2;
            int clusterY = bounds.y + bounds.height / 2;
            double clusterDistToAttack = Math.sqrt(Math.pow(clusterX - attackScreenX, 2) + Math.pow(clusterY - attackScreenY, 2));

            // find NPC that matches this cluster
            WorldPosition matchedNpc = null;
            double matchedNpcDist = 50; // max screen distance for cluster-to-NPC match

            for (WorldPosition npcPos : npcPositions.asList()) {
                if (npcPos.distanceTo(playerPos) > SCAN_RANGE) continue;

                // CRITICAL: skip already-ignored positions (already plucked/checked)
                if (ignoredPositionTimestamps.containsKey(posKey(npcPos))) {
                    continue;
                }

                Polygon tileCube = script.getSceneProjector().getTileCube(npcPos, TILE_CUBE_HEIGHT);
                if (tileCube == null) continue;

                Rectangle npcBounds = tileCube.getBounds();
                int npcCenterX = npcBounds.x + npcBounds.width / 2;
                int npcCenterY = npcBounds.y + npcBounds.height / 2;

                double dist = Math.sqrt(Math.pow(clusterX - npcCenterX, 2) + Math.pow(clusterY - npcCenterY, 2));
                if (dist < matchedNpcDist) {
                    matchedNpcDist = dist;
                    matchedNpc = npcPos;
                }
            }

            // if this cluster matched a valid (non-ignored) NPC and is closer to attack position
            if (matchedNpc != null && clusterDistToAttack < bestClusterDist) {
                bestClusterDist = clusterDistToAttack;
                bestMatch = matchedNpc;
                script.log(getClass(), "[corpseDetect] candidate: cluster at screen(" + clusterX + "," + clusterY +
                        ") -> NPC at " + matchedNpc.getX() + "," + matchedNpc.getY() +
                        " (clusterDist=" + (int)clusterDistToAttack + ")");
            }
        }

        if (bestMatch != null) {
            script.log(getClass(), "[corpseDetect] matched corpse to NPC at " + bestMatch.getX() + "," + bestMatch.getY() +
                    " (clusterDist=" + (int)bestClusterDist + ")");
        } else {
            script.log(getClass(), "[corpseDetect] no valid NPC matched (all may be ignored)");
        }

        return bestMatch;
    }

    /**
     * pluck a tracked corpse at known position
     * uses simple tap(shape, "Pluck") - one menu, one click
     */
    private boolean pluckTrackedCorpse(WorldPosition corpsePos) {
        TidalsChompyHunter.task = "plucking corpse";

        if (corpsePos == null) {
            script.log(getClass(), "no corpse position provided - skipping pluck");
            return false;
        }

        script.log(getClass(), "plucking tracked corpse at: " + corpsePos.getX() + "," + corpsePos.getY());

        Polygon tileCube = script.getSceneProjector().getTileCube(corpsePos, TILE_CUBE_HEIGHT);
        if (tileCube == null) {
            script.log(getClass(), "cannot create tile cube for corpse - trying fallback NPC search");
            return pluckFallbackNearbyNpcs(corpsePos);
        }

        // simple direct pluck - one menu open, one click
        for (int attempt = 1; attempt <= PLUCK_MAX_ATTEMPTS; attempt++) {
            script.log(getClass(), "pluck attempt " + attempt + "/" + PLUCK_MAX_ATTEMPTS);

            // reset flag before attempt
            pluckStarted = false;

            // direct tap "Pluck" - opens menu and clicks in one action
            boolean plucked = script.getFinger().tap(tileCube, "Pluck");

            if (plucked) {
                script.log(getClass(), "pluck action sent - waiting for chat confirmation");

                // wait for "You start plucking" chat message (covers walking to corpse)
                boolean confirmed = script.pollFramesUntil(() -> pluckStarted, 6000);

                if (confirmed) {
                    script.log(getClass(), "pluck confirmed via chat - waiting for animation to complete");
                    waitForPluckAnimation();
                    TidalsChompyHunter.corpsePositions.remove(corpsePos);
                    ignoredPositionTimestamps.put(posKey(corpsePos), System.currentTimeMillis());
                    script.log(getClass(), "corpse plucked (" + TidalsChompyHunter.corpsePositions.size() + " remaining)");
                    return true;
                } else {
                    script.log(getClass(), "pluck not confirmed - may have failed to reach corpse");
                }
            }

            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(200, 400));
        }

        // primary position failed - try nearby NPCs
        script.log(getClass(), "primary pluck failed - trying nearby NPCs");
        if (pluckFallbackNearbyNpcs(corpsePos)) {
            return true;
        }

        TidalsChompyHunter.corpsePositions.remove(corpsePos);
        script.log(getClass(), "failed to pluck after " + PLUCK_MAX_ATTEMPTS + " attempts - removed from tracking");
        return false;
    }

    /**
     * fallback: try plucking nearby NPCs with simple tap
     */
    private boolean pluckFallbackNearbyNpcs(WorldPosition corpsePos) {
        UIResultList<WorldPosition> npcResult = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcResult == null || !npcResult.isFound()) {
            script.log(getClass(), "[fallback] no NPCs on minimap");
            TidalsChompyHunter.corpsePositions.remove(corpsePos);
            return false;
        }

        List<WorldPosition> nearbyNpcs = npcResult.asList().stream()
                .filter(pos -> pos.distanceTo(corpsePos) <= 3)
                .sorted(Comparator.comparingDouble(pos -> pos.distanceTo(corpsePos)))
                .collect(Collectors.toList());

        script.log(getClass(), "[fallback] trying " + nearbyNpcs.size() + " NPCs near tracked position");

        for (WorldPosition npcPos : nearbyNpcs) {
            Polygon npcTileCube = script.getSceneProjector().getTileCube(npcPos, TILE_CUBE_HEIGHT);
            if (npcTileCube == null) continue;

            // reset flag before attempt
            pluckStarted = false;

            // simple direct pluck
            boolean plucked = script.getFinger().tap(npcTileCube, "Pluck");
            if (plucked) {
                script.log(getClass(), "[fallback] pluck action sent at " + npcPos.getX() + "," + npcPos.getY());

                // wait for chat confirmation
                boolean confirmed = script.pollFramesUntil(() -> pluckStarted, 6000);

                if (confirmed) {
                    script.log(getClass(), "[fallback] pluck confirmed - waiting for animation to complete");
                    waitForPluckAnimation();
                    TidalsChompyHunter.corpsePositions.remove(corpsePos);
                    ignoredPositionTimestamps.put(posKey(npcPos), System.currentTimeMillis());
                    return true;
                }
            }
        }

        script.log(getClass(), "[fallback] no pluckable corpse found");
        TidalsChompyHunter.corpsePositions.remove(corpsePos);
        return false;
    }

    /**
     * validate tracked corpse positions against minimap NPC dots
     * removes positions where no NPC exists (corpse despawned)
     * IMPORTANT: only validates when player is in VERIFICATION_AREA - when at swamp bubbles,
     * we can't reliably see the drop area and would incorrectly remove positions
     */
    public void validateTrackedCorpses() {
        if (TidalsChompyHunter.corpsePositions.isEmpty()) {
            return;
        }

        // skip validation if player is outside the viewable area
        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null || !VERIFICATION_AREA.contains(playerPos)) {
            return; // can't reliably validate from here
        }

        UIResultList<WorldPosition> npcResult = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcResult == null || !npcResult.isFound()) {
            // can't validate without minimap data - keep positions for now
            return;
        }

        List<WorldPosition> npcPositions = npcResult.asList();
        int beforeCount = TidalsChompyHunter.corpsePositions.size();

        // remove corpse positions that have no NPC within 1 tile
        TidalsChompyHunter.corpsePositions.removeIf(corpsePos -> {
            boolean hasNpc = npcPositions.stream()
                    .anyMatch(npcPos -> npcPos.distanceTo(corpsePos) <= 1);
            if (!hasNpc) {
                script.log(getClass(), "removing stale corpse at " + corpsePos.getX() + "," + corpsePos.getY() + " - no NPC on minimap");
            }
            return !hasNpc;
        });

        int removed = beforeCount - TidalsChompyHunter.corpsePositions.size();
        if (removed > 0) {
            script.log(getClass(), "validated corpses: " + removed + " removed, " + TidalsChompyHunter.corpsePositions.size() + " remaining");
        }
    }

    /**
     * wait for pluck animation to complete
     * uses fixed delay after chat confirmation - pixel detection unreliable for this animation
     * pluck animation takes ~3 ticks (1800ms), adding variance for human-like timing
     */
    private void waitForPluckAnimation() {
        // gaussian delay centered around 2100ms
        int delay = RandomUtils.gaussianRandom(1800, 2400, 2100, 150);

        script.log(getClass(), "waiting " + delay + "ms for pluck animation");
        script.pollFramesUntil(() -> false, delay);
        script.log(getClass(), "pluck animation wait complete");
    }

    /**
     * pluck all tracked corpses that haven't been plucked yet
     * called when there's downtime to clean up missed corpses
     */
    public void pluckAllTrackedCorpses() {
        if (!TidalsChompyHunter.pluckingEnabled || TidalsChompyHunter.corpsePositions.isEmpty()) {
            return;
        }

        // validate first - remove positions where corpse despawned
        validateTrackedCorpses();

        if (TidalsChompyHunter.corpsePositions.isEmpty()) {
            return;
        }

        script.log(getClass(), "plucking " + TidalsChompyHunter.corpsePositions.size() + " tracked corpses");

        // copy list to avoid concurrent modification
        List<WorldPosition> toPluck = new ArrayList<>(TidalsChompyHunter.corpsePositions);
        for (WorldPosition corpsePos : toPluck) {
            // check for chompy interrupt
            if (hasLiveChompy(script)) {
                script.log(getClass(), "chompy detected - stopping corpse cleanup");
                break;
            }
            pluckTrackedCorpse(corpsePos);
        }
    }

    /**
     * detect chompy using pixel cluster detection only
     */
    private WorldPosition detectChompy() {
        WorldPosition chompyPos = findChompyByPixelCluster();
        if (chompyPos != null) {
            script.log(getClass(), "detected chompy via pixel cluster at " + chompyPos);
        }
        return chompyPos;
    }

    /**
     * get chompy pixel clusters, using cache if available and fresh
     * reduces expensive full-screen scans when called multiple times in quick succession
     */
    private List<PixelCluster> getChompyClustersCached() {
        long now = System.currentTimeMillis();

        // return cached if fresh
        if (cachedChompyClusters != null && now - cachedChompyClustersTime < CLUSTER_CACHE_TTL_MS) {
            return cachedChompyClusters;
        }

        // fresh scan
        PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                CHOMPY_CLUSTER_MAX_DISTANCE,
                CHOMPY_CLUSTER_MIN_SIZE,
                new SearchablePixel[]{CHOMPY_SPRITE}
        );

        PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(null, query);
        if (result == null) {
            cachedChompyClusters = null;
            cachedChompyClustersTime = now;
            return null;
        }

        cachedChompyClusters = result.getClusters();
        cachedChompyClustersTime = now;
        return cachedChompyClusters;
    }

    /**
     * find chompy using RGB pixel cluster detection
     * iterates through clusters (largest first) until finding a non-ignored NPC
     */
    private WorldPosition findChompyByPixelCluster() {
        script.log(getClass(), "[pixelScan] starting pixel cluster search...");

        List<PixelCluster> clusters = getChompyClustersCached();
        if (clusters == null || clusters.isEmpty()) {
            script.log(getClass(), "[pixelScan] no clusters found matching chompy color");
            return null;
        }

        script.log(getClass(), "[pixelScan] found " + clusters.size() + " chompy sprite clusters");

        // sort clusters by size (largest first - most likely to be live chompy)
        List<PixelCluster> sortedClusters = clusters.stream()
                .sorted(Comparator.comparingInt((PixelCluster c) -> c.getPoints().size()).reversed())
                .collect(Collectors.toList());

        // log cluster details
        for (int i = 0; i < sortedClusters.size(); i++) {
            PixelCluster c = sortedClusters.get(i);
            Rectangle b = c.getBounds();
            script.log(getClass(), "[pixelScan] cluster " + i + ": size=" + c.getPoints().size() +
                    " screen=(" + (b.x + b.width/2) + "," + (b.y + b.height/2) + ")");
        }

        // get NPC positions once (shared across all cluster checks)
        UIResultList<WorldPosition> npcPositions = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcPositions == null || npcPositions.isNotFound()) {
            script.log(getClass(), "[pixelScan] no NPC positions from minimap");
            return null;
        }

        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            script.log(getClass(), "[pixelScan] player position null");
            return null;
        }

        script.log(getClass(), "[pixelScan] player at " + playerPos.getX() + "," + playerPos.getY() +
                ", " + npcPositions.asList().size() + " NPCs on minimap");

        // log nearby NPCs
        List<WorldPosition> nearbyNpcs = npcPositions.asList().stream()
                .filter(pos -> pos.distanceTo(playerPos) <= SCAN_RANGE)
                .collect(Collectors.toList());
        script.log(getClass(), "[pixelScan] " + nearbyNpcs.size() + " NPCs within " + SCAN_RANGE + " tiles");
        for (WorldPosition npc : nearbyNpcs) {
            String ignored = ignoredPositionTimestamps.containsKey(posKey(npc)) ? " [IGNORED]" : "";
            script.log(getClass(), "[pixelScan]   NPC at " + formatPos(npc) +
                    " dist=" + (int)npc.distanceTo(playerPos) + ignored);
        }

        // clean up expired ignores once at start
        long now = System.currentTimeMillis();
        ignoredPositionTimestamps.entrySet().removeIf(e -> now - e.getValue() > IGNORE_DURATION_MS);

        // try each cluster until we find a non-ignored NPC
        for (int clusterIdx = 0; clusterIdx < sortedClusters.size(); clusterIdx++) {
            PixelCluster cluster = sortedClusters.get(clusterIdx);
            // get center of cluster
            Rectangle bounds = cluster.getBounds();
            Point screenCenter = new Point(
                    bounds.x + bounds.width / 2,
                    bounds.y + bounds.height / 2
            );

            script.log(getClass(), "[pixelScan] checking cluster " + clusterIdx +
                    " at screen(" + screenCenter.x + "," + screenCenter.y + ")");

            // find the NPC whose screen projection is closest to this cluster center
            WorldPosition closestNpc = null;
            double closestDistance = Double.MAX_VALUE;

            for (WorldPosition npcPos : npcPositions.asList()) {
                if (npcPos.distanceTo(playerPos) > SCAN_RANGE) {
                    continue;
                }

                Polygon tileCube = script.getSceneProjector().getTileCube(npcPos, TILE_CUBE_HEIGHT);
                if (tileCube == null) {
                    continue;
                }

                // get tile cube center from polygon bounds
                Rectangle npcBounds = tileCube.getBounds();
                int npcCenterX = npcBounds.x + npcBounds.width / 2;
                int npcCenterY = npcBounds.y + npcBounds.height / 2;

                // distance from cluster center to NPC screen position
                double dist = Math.sqrt(
                        Math.pow(screenCenter.x - npcCenterX, 2) +
                        Math.pow(screenCenter.y - npcCenterY, 2)
                );

                if (dist < closestDistance) {
                    closestDistance = dist;
                    closestNpc = npcPos;
                }
            }

            // skip if no NPC found within range for this cluster
            if (closestNpc == null || closestDistance >= 50) {
                script.log(getClass(), "[pixelScan] cluster " + clusterIdx + ": no nearby NPC (closest dist=" +
                        (closestNpc == null ? "none" : (int)closestDistance) + ")");
                continue;
            }

            // skip if this position is ignored (corpse) - try next cluster
            if (ignoredPositionTimestamps.containsKey(posKey(closestNpc))) {
                script.log(getClass(), "[pixelScan] cluster " + clusterIdx + ": matched NPC at " +
                        closestNpc.getX() + "," + closestNpc.getY() + " but IGNORED (corpse)");
                continue;
            }

            // found a valid non-ignored NPC
            script.log(getClass(), "[pixelScan] cluster " + clusterIdx + ": MATCHED valid NPC at " +
                    closestNpc.getX() + "," + closestNpc.getY() + " (screenDist=" + (int)closestDistance + ")");
            return closestNpc;
        }

        // all clusters mapped to ignored positions or no valid NPC found
        script.log(getClass(), "[pixelScan] FAILED - checked " + sortedClusters.size() +
                " clusters, no valid chompy found");
        return null;
    }

    /**
     * fast check for any chompy sprite on screen
     * used by other tasks to interrupt and yield to AttackChompy
     * does NOT do NPC matching or ignore checking - just raw pixel detection
     * uses cluster cache to avoid repeated expensive scans
     */
    public static boolean hasVisibleChompySprite(Script script) {
        List<PixelCluster> clusters = getChompyClustersCachedStatic(script);
        return clusters != null && !clusters.isEmpty();
    }

    /**
     * get chompy pixel clusters with caching (static version for interrupt checks)
     */
    private static List<PixelCluster> getChompyClustersCachedStatic(Script script) {
        long now = System.currentTimeMillis();

        // return cached if fresh
        if (cachedChompyClusters != null && now - cachedChompyClustersTime < CLUSTER_CACHE_TTL_MS) {
            return cachedChompyClusters;
        }

        // fresh scan
        PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                CHOMPY_CLUSTER_MAX_DISTANCE,
                CHOMPY_CLUSTER_MIN_SIZE,
                new SearchablePixel[]{CHOMPY_SPRITE}
        );

        PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(null, query);
        if (result == null) {
            cachedChompyClusters = null;
            cachedChompyClustersTime = now;
            return null;
        }

        cachedChompyClusters = result.getClusters();
        cachedChompyClustersTime = now;
        return cachedChompyClusters;
    }

    /**
     * check if there's a LIVE chompy on screen (filters out ignored/corpse positions)
     * this should be used for interrupt checks instead of hasVisibleChompySprite()
     * prevents infinite loop when only dead chompy corpses are visible
     */
    public static boolean hasLiveChompy(Script script) {
        // clean up expired ignores first
        long now = System.currentTimeMillis();
        ignoredPositionTimestamps.entrySet().removeIf(e -> now - e.getValue() > IGNORE_DURATION_MS);

        // use cached clusters
        List<PixelCluster> clusters = getChompyClustersCachedStatic(script);
        if (clusters == null || clusters.isEmpty()) return false;

        // get NPC positions to match clusters
        UIResultList<WorldPosition> npcResult = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcResult == null || !npcResult.isFound()) return false;

        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) return false;

        List<WorldPosition> nearbyNpcs = npcResult.asList().stream()
                .filter(pos -> pos.distanceTo(playerPos) <= 15)
                .collect(Collectors.toList());

        // check each cluster - if ANY matches a non-ignored NPC, return true
        for (PixelCluster cluster : clusters) {
            Rectangle bounds = cluster.getBounds();
            int clusterScreenX = bounds.x + bounds.width / 2;
            int clusterScreenY = bounds.y + bounds.height / 2;

            // find closest NPC to this cluster
            WorldPosition closestNpc = null;
            double closestDist = 50; // max screen distance

            for (WorldPosition npcPos : nearbyNpcs) {
                Polygon tileCube = script.getSceneProjector().getTileCube(npcPos, 70);
                if (tileCube == null) continue;

                Rectangle npcBounds = tileCube.getBounds();
                int npcScreenX = npcBounds.x + npcBounds.width / 2;
                int npcScreenY = npcBounds.y + npcBounds.height / 2;

                double dist = Math.sqrt(Math.pow(clusterScreenX - npcScreenX, 2) +
                                       Math.pow(clusterScreenY - npcScreenY, 2));
                if (dist < closestDist) {
                    closestDist = dist;
                    closestNpc = npcPos;
                }
            }

            // if we found a matching NPC that's NOT ignored, we have a live chompy
            if (closestNpc != null && !ignoredPositionTimestamps.containsKey(posKey(closestNpc))) {
                return true;
            }
        }

        return false; // all clusters matched to ignored positions (corpses)
    }

    // track if we plucked instead of attacked (set by attackChompy)
    private boolean lastActionWasPluck = false;

    // track if we found a corpse but couldn't pluck (plucking disabled)
    // used for fail-fast detection - don't retry 10 times on a corpse
    private boolean foundCorpseNoPluck = false;

    /**
     * attack chompy - retries up to MAX_ATTACK_ATTEMPTS times
     * uses tapGameScreen to handle Attack or Pluck in a single menu interaction
     * retries help when chompy is moving and clicks miss
     * FAIL-FAST: if we detect a corpse (Pluck option) but plucking is disabled, immediately ignore
     */
    private boolean attackChompy(WorldPosition chompyPos) {
        script.log(getClass(), "[attack] attempting attack at " + chompyPos.getX() + "," + chompyPos.getY() +
                " (max " + MAX_ATTACK_ATTEMPTS + " attempts)");
        lastActionWasPluck = false;
        foundCorpseNoPluck = false;

        for (int attempt = 1; attempt <= MAX_ATTACK_ATTEMPTS; attempt++) {
            script.log(getClass(), "[attack] attempt " + attempt + "/" + MAX_ATTACK_ATTEMPTS);

            // re-detect chompy position each attempt (chompy may have moved)
            WorldPosition currentPos = (attempt == 1) ? chompyPos : refreshChompyPosition(chompyPos);
            if (currentPos == null) {
                script.log(getClass(), "[attack] chompy no longer detected - giving up");
                return false;
            }

            Polygon tileCube = script.getSceneProjector().getTileCube(currentPos, TILE_CUBE_HEIGHT);
            if (tileCube == null) {
                script.log(getClass(), "[attack] getTileCube returned null - retrying");
                script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(300, 500));
                continue;
            }

            Polygon shrunk = tileCube.getResized(SHRINK_FACTOR);

            // open menu - try Attack first, fall back to Pluck
            boolean success = script.getFinger().tapGameScreen(shrunk, menuEntries -> {
                // first look for Attack (live chompy) - check raw text contains "attack"
                var attack = menuEntries.stream()
                        .filter(e -> {
                            String raw = e.getRawText();
                            return raw != null && raw.toLowerCase().contains("attack");
                        })
                        .findFirst()
                        .orElse(null);

                if (attack != null) {
                    script.log(getClass(), "[attack] found Attack option: " + attack.getRawText());
                    return attack;
                }

                // no Attack - look for Pluck (corpse)
                var pluck = menuEntries.stream()
                        .filter(e -> {
                            String raw = e.getRawText();
                            return raw != null && raw.toLowerCase().contains("pluck");
                        })
                        .findFirst()
                        .orElse(null);

                if (pluck != null) {
                    if (TidalsChompyHunter.pluckingEnabled) {
                        script.log(getClass(), "[attack] found Pluck option: " + pluck.getRawText());
                        lastActionWasPluck = true;
                        return pluck;
                    } else {
                        // FAIL-FAST: corpse found but plucking disabled - set flag to break retry loop
                        script.log(getClass(), "[attack] found Pluck (corpse) but plucking disabled - ignoring");
                        foundCorpseNoPluck = true;
                        return null;
                    }
                }

                return null; // nothing found
            });

            if (success) {
                if (lastActionWasPluck) {
                    // we plucked a corpse - wait for animation like the dedicated pluck method does
                    script.log(getClass(), "[attack] plucked corpse in one action - waiting for animation");
                    waitForPluckAnimation();
                    ignoredPositionTimestamps.put(posKey(currentPos), System.currentTimeMillis());
                    removeTrackedChompy(currentPos);
                    TidalsChompyHunter.corpsePositions.remove(currentPos);
                    return false; // didn't attack, return false
                } else {
                    // record that WE initiated this attack - for health bar crash detection
                    // this tells DetectPlayers that any health bar appearing soon is OURS
                    DetectPlayers.recordOurAttack();
                    script.log(getClass(), "[attack] SUCCESS - attack sent on attempt " + attempt);
                    return true;
                }
            }

            // FAIL-FAST: if we detected a corpse but plucking is disabled, immediately ignore
            // don't waste 10 retries on a dead chompy we can't interact with
            if (foundCorpseNoPluck) {
                script.log(getClass(), "[attack] FAIL-FAST: corpse detected, plucking disabled - adding to ignore");
                ignoredPositionTimestamps.put(posKey(currentPos), System.currentTimeMillis());
                removeTrackedChompy(currentPos);
                return false;
            }

            // tap failed - retry with delay
            script.log(getClass(), "[attack] attempt " + attempt + " failed - waiting before retry");
            script.pollFramesUntil(() -> false, RandomUtils.weightedRandom(300, 500));
        }

        // all attempts failed - likely a false positive or chompy despawned
        script.log(getClass(), "[attack] FAILED after " + MAX_ATTACK_ATTEMPTS + " attempts");
        ignoredPositionTimestamps.put(posKey(chompyPos), System.currentTimeMillis());
        removeTrackedChompy(chompyPos);
        return false;
    }

    /**
     * refresh chompy position by re-detecting via pixel cluster
     * returns updated position if chompy still visible, null if gone
     */
    private WorldPosition refreshChompyPosition(WorldPosition originalPos) {
        WorldPosition newPos = findChompyByPixelCluster();
        if (newPos != null) {
            // found a chompy - use it even if position changed (chompy moved)
            if (!newPos.equals(originalPos)) {
                script.log(getClass(), "[refresh] chompy moved from " +
                        originalPos.getX() + "," + originalPos.getY() + " to " +
                        newPos.getX() + "," + newPos.getY());
            }
            return newPos;
        }
        return null;
    }

    /**
     * reset combat state (called when kill confirmed or chompy lost)
     */
    public static void resetCombatState() {
        inCombat = false;
        currentChompyPosition = null;
        combatStartTime = 0;
    }

    /**
     * reset all state for world hop / fresh start
     * clears tracked chompies, ignored positions, combat state, cooldowns
     */
    public static void resetAllState() {
        // combat state
        inCombat = false;
        currentChompyPosition = null;
        combatStartTime = 0;
        killDetected = false;
        pluckStarted = false;

        // tracking lists
        trackedChompies.clear();
        ignoredPositionTimestamps.clear();

        // cooldown
        lastNoChompyTime = 0;

        // clear cluster cache
        cachedChompyClusters = null;
        cachedChompyClustersTime = 0;
    }

    /**
     * try to pre-inflate a toad during monitoring downtime
     * returns true if inflation succeeded
     */
    private boolean tryPreInflate() {
        // don't pre-inflate if we already have enough
        int inventoryToads = countBloatedToadsInv();
        if (inventoryToads >= MAX_INVENTORY_TOADS) {
            return false;
        }

        // don't pre-inflate if bellows are empty
        if (!anyBellowsHaveCharges()) {
            return false;
        }

        TidalsChompyHunter.task = "pre-inflating toad";

        // find and inflate one toad
        WorldPosition toadPos = findSwampToadForPreInflate();
        if (toadPos == null) {
            TidalsChompyHunter.task = "monitoring for chompy";
            return false;
        }

        boolean success = inflateOneToad(toadPos);
        TidalsChompyHunter.task = "monitoring for chompy";
        return success;
    }

    /**
     * check if any bellows have charges
     */
    private boolean anyBellowsHaveCharges() {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(
            Set.of(OGRE_BELLOWS_3, OGRE_BELLOWS_2, OGRE_BELLOWS_1)
        );
        if (inv == null) {
            return false;
        }
        int chargedCount = inv.getAmount(OGRE_BELLOWS_3)
                + inv.getAmount(OGRE_BELLOWS_2)
                + inv.getAmount(OGRE_BELLOWS_1);
        return chargedCount > 0;
    }

    /**
     * count bloated toads in inventory
     */
    private int countBloatedToadsInv() {
        ItemGroupResult inv = script.getWidgetManager().getInventory().search(Set.of(BLOATED_TOAD));
        if (inv == null) {
            return 0;
        }
        return inv.getAmount(BLOATED_TOAD);
    }

    /**
     * find a swamp toad nearby for pre-inflation
     */
    private WorldPosition findSwampToadForPreInflate() {
        UIResultList<WorldPosition> npcResult = script.getWidgetManager().getMinimap().getNPCPositions();
        if (npcResult == null || !npcResult.isFound()) {
            return null;
        }

        WorldPosition playerPos = script.getWorldPosition();
        if (playerPos == null) {
            return null;
        }

        // filter NPCs within close range (don't walk far for pre-inflate)
        List<WorldPosition> candidates = npcResult.asList().stream()
                .filter(pos -> pos.distanceTo(playerPos) <= 10)
                .sorted(Comparator.comparingDouble(pos -> pos.distanceTo(playerPos)))
                .collect(Collectors.toList());

        // try each candidate - tap "Inflate" to identify swamp toads
        for (WorldPosition npcPos : candidates) {
            Polygon tileCube = script.getSceneProjector().getTileCube(npcPos, TILE_CUBE_HEIGHT_TOAD);
            if (tileCube == null) {
                continue;
            }

            // try to tap "Inflate" - only swamp toads have this action
            boolean success = script.getFinger().tap(tileCube, "Inflate");
            if (success) {
                return npcPos;
            }
        }

        return null;
    }

    /**
     * inflate a single toad at position (simplified - no retry for pre-inflate)
     */
    private boolean inflateOneToad(WorldPosition toadPos) {
        int previousCount = countBloatedToadsInv();

        Polygon tileCube = script.getSceneProjector().getTileCube(toadPos, TILE_CUBE_HEIGHT_TOAD);
        if (tileCube == null) {
            return false;
        }

        // tap "Inflate" - game handles walking and using bellows
        boolean success = script.getFinger().tap(tileCube, "Inflate");
        if (!success) {
            return false;
        }

        // wait for inventory to gain toad (shorter timeout for pre-inflate)
        boolean gotToad = script.pollFramesUntil(() -> countBloatedToadsInv() > previousCount, 5000);
        return gotToad;
    }
}
