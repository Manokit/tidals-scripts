package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.ui.chatbox.Chatbox;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.visual.PixelAnalyzer;
import com.osmb.api.walker.WalkConfig;
import com.osmb.api.ui.component.chatbox.ChatboxComponent;
import utils.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static main.TidalsGemMiner.*;

import main.TidalsGemMiner;

public class Mine extends Task {

    // ═══════════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════════
    private static final String TARGET_OBJECT_NAME = "Gem rocks";
    // stuck timeout range (4-6 minutes) - randomized on init and each success
    private static final long STUCK_TIMEOUT_MIN_MS = 4 * 60 * 1000;
    private static final long STUCK_TIMEOUT_MAX_MS = 6 * 60 * 1000;
    private static final String NO_ORE_MESSAGE = "there is currently no ore available in this rock";
    private static final String MINED_MESSAGE = "you just mined";
    private static final String SWING_PICK_MESSAGE = "you swing your pick at the rock";
    private static final String CLUE_SCROLL_MESSAGE = "you have a sneaking suspicion";

    // hardcoded underground mine clusters - rocks grouped by physical proximity
    private static final WorldPosition[][] UNDERGROUND_CLUSTERS = {
        { wp(2839,9381), wp(2838,9381), wp(2837,9380) },
        { wp(2833,9380), wp(2832,9381), wp(2831,9382), wp(2829,9384) },
        { wp(2831,9386), wp(2831,9387) },
        { wp(2827,9386), wp(2827,9388), wp(2826,9389), wp(2826,9390), wp(2827,9391), wp(2828,9392) },
        { wp(2833,9391), wp(2835,9392), wp(2836,9392) },
        { wp(2831,9396), wp(2832,9399), wp(2833,9398), wp(2835,9398), wp(2836,9398), wp(2837,9398), wp(2837,9397), wp(2840,9397) },
        { wp(2843,9392), wp(2844,9391), wp(2845,9391), wp(2846,9390), wp(2847,9390), wp(2848,9390) },
        { wp(2848,9386), wp(2849,9387) },
        { wp(2847,9381), wp(2847,9379), wp(2848,9379), wp(2850,9378), wp(2851,9377) },
        { wp(2855,9382), wp(2856,9382), wp(2857,9382), wp(2857,9385), wp(2857,9386) },
        { wp(2852,9390), wp(2853,9389), wp(2854,9388) },
    };

    private static WorldPosition wp(int x, int y) {
        return new WorldPosition(x, y, 0);
    }

    // rock scan interval range (3-5s) - randomized per check
    private static final long ROCK_SCAN_INTERVAL_MIN_MS = 3_000L;
    private static final long ROCK_SCAN_INTERVAL_MAX_MS = 5_000L;
    private static final int CHAT_LINES_TO_CHECK = 4;
    // chat repeat window range (1.5-2.5s) - randomized per check
    private static final long CHAT_REPEAT_WINDOW_MIN_MS = 1500L;
    private static final long CHAT_REPEAT_WINDOW_MAX_MS = 2500L;

    private static final int MAX_CONSECUTIVE_MISCLICKS = 5;
    // gem rock respawn is exactly 59.4 seconds (99 game ticks)
    private static final long ROCK_COOLDOWN_MS = 59_400;
    // stuck rock cooldown range (50-70s) - randomized when marking stuck rocks
    private static final long STUCK_ROCK_COOLDOWN_MIN_MS = 50_000;
    private static final long STUCK_ROCK_COOLDOWN_MAX_MS = 70_000;
    // stationary threshold range (200-300ms) - randomized per idle check
    private static final int STATIONARY_THRESHOLD_MIN_MS = 200;
    private static final int STATIONARY_THRESHOLD_MAX_MS = 300;

    // ═══════════════════════════════════════════════════════════════════════════
    // MINING STATE MACHINE
    // ═══════════════════════════════════════════════════════════════════════════
    private enum MiningState {
        IDLE,           // waiting for player to stop moving/animating
        FIND_TARGET,    // locate best rock to mine
        TAPPING,        // send tap action to rock
        APPROACHING,    // walking to rock
        MINING,         // waiting for mining completion (chat/respawn)
        PROCESSING      // handling result, updating counters
    }

    private MiningState miningState = MiningState.FIND_TARGET;
    private RockTarget currentTarget = null;
    private WorldPosition tapStartPos = null;
    private MiningResult pendingResult = null;

    // ═══════════════════════════════════════════════════════════════════════════
    // STATE FIELDS
    // ═══════════════════════════════════════════════════════════════════════════
    private final Map<String, Long> recentlyMinedRocks = new HashMap<>();
    private long lastSuccessfulAction = 0;
    private long stuckThreshold = 0;
    private int consecutiveMisclickCount = 0;
    private String lastMisclickPositionKey = null;
    private Set<String> emptyRockPositionKeys = new HashSet<>();
    private int consecutiveNoOreCount = 0;
    private final Set<WorldPosition> knownRockPositions = new HashSet<>();
    private Map<Integer, Integer> lastGemCounts = new HashMap<>();
    private long lastRockScanMs = 0;
    private boolean skipIdleOnNextFind = false;
    private int activeClusterIndex = -1; // sticky cluster for underground mine, -1 = pick new
    private boolean clusterFirstPick = false; // true only for the first rock pick when entering a new cluster
    private boolean wasActive = false; // tracks if Mine was active last poll (to detect post-bank reset)
    private String lastChatMatchLine = null;
    private long lastChatMatchMs = 0;
    private int lastChatSnapshotHash = 0;

    // visibility failure tracking - prevents infinite loop when cluster has only low-visibility rocks
    private int consecutiveVisibilityFailures = 0;
    private String lastVisibilityFailureKey = null;
    private static final int MAX_VISIBILITY_FAILURES = 3;
    // short cooldown for rocks that fail visibility checks (20-30s)
    private static final long VISIBILITY_FAILURE_COOLDOWN_MIN_MS = 20_000;
    private static final long VISIBILITY_FAILURE_COOLDOWN_MAX_MS = 30_000;

    // ═══════════════════════════════════════════════════════════════════════════
    // RECORDS
    // ═══════════════════════════════════════════════════════════════════════════
    private record RockTarget(WorldPosition position, Polygon clickArea) {}
    private record MiningResult(boolean mined, boolean noOre, boolean respawnSeen, boolean swingPickSeen) {}
    private record ChatSignal(boolean mined, boolean noOre, boolean swingPick, boolean clueScroll, String line) {}

    /** stable string key for WorldPosition - avoids equals/hashCode issues */
    private static String posKey(WorldPosition pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getPlane();
    }

    private boolean isVerbose() {
        return TidalsGemMiner.verboseLogging;
    }

    public Mine(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // don't activate if deposit box is open
        if (script.getWidgetManager().getDepositBox().isVisible()) {
            wasActive = false;
            return false;
        }
        boolean canActivate = setupDone && !isInventoryFull();
        if (!canActivate) {
            wasActive = false;
            return false;
        }
        // reset cluster after banking (wasActive was false while Bank ran)
        if (!wasActive) {
            activeClusterIndex = -1;
            logVerbose("[activate] reset active cluster (returning from bank/setup)");
        }
        wasActive = true;
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAIN EXECUTE - POLL-BASED STATE MACHINE
    // ═══════════════════════════════════════════════════════════════════════════
    @Override
    public boolean execute() {
        task = "Mining";
        logVerbose("[execute] state=" + miningState);

        // always check: stuck too long? stop script
        if (isStuckTooLong()) {
            handleStuck();
            return false;
        }

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) {
            logVerbose("[execute] abort: player position is null");
            return false;
        }

        boolean isUpperMine = selectedLocation.name().equals("upper");

        // always check: not in mining area? walk there and reset state
        if (!inMiningArea(myPos)) {
            resetMiningState();
            walkToMine();
            return false;
        }

        // refresh known rock positions from ObjectManager
        refreshKnownRockPositions(myPos);

        switch (miningState) {
            case IDLE:
                return executeIdle();
            case FIND_TARGET:
                return executeFindTarget(myPos, isUpperMine);
            case TAPPING:
                return executeTapping();
            case APPROACHING:
                return executeApproaching(myPos);
            case MINING:
                return executeMining();
            case PROCESSING:
                return executeProcessing(isUpperMine);
            default:
                resetMiningState();
                return false;
        }
    }

    private void resetMiningState() {
        miningState = MiningState.FIND_TARGET;
        currentTarget = null;
        tapStartPos = null;
        pendingResult = null;
        // don't reset skipIdleOnNextFind - it's set intentionally before reset
    }

    // ── state: IDLE - wait for player to stop ──
    private boolean executeIdle() {
        if (!waitForPlayerIdle()) {
            logVerbose("[IDLE] idle wait failed");
            return false;
        }
        miningState = MiningState.TAPPING;
        return false;
    }

    // ── state: FIND_TARGET - locate best rock ──
    private boolean executeFindTarget(WorldPosition myPos, boolean isUpperMine) {
        RockTarget target = findBestTarget(myPos, isUpperMine);
        if (target == null) {
            handleNoRocksAvailable(isUpperMine);
            return false;
        }
        currentTarget = target;
        logVerbose("[FIND_TARGET] selected: " + target.position().getX() + "," + target.position().getY());
        // skip idle wait if player is already stationary (just mined or got "no ore")
        if (skipIdleOnNextFind) {
            skipIdleOnNextFind = false;
            miningState = MiningState.TAPPING;
        } else {
            miningState = MiningState.IDLE;
        }
        return false;
    }

    // ── state: TAPPING - tap the rock ──
    private boolean executeTapping() {
        if (currentTarget == null) {
            resetMiningState();
            return false;
        }
        // snapshot inventory for GP tracking before mining
        snapshotGemCounts();

        if (!tapTarget(currentTarget)) {
            logVerbose("[TAPPING] tap failed");

            // track consecutive failures on same rock (visibility-based failures cause infinite loops)
            String targetKey = currentTarget.position() != null ? posKey(currentTarget.position()) : null;
            if (targetKey != null) {
                if (targetKey.equals(lastVisibilityFailureKey)) {
                    consecutiveVisibilityFailures++;
                } else {
                    consecutiveVisibilityFailures = 1;
                    lastVisibilityFailureKey = targetKey;
                }

                // after repeated failures on same rock, mark it unavailable and reset cluster
                if (consecutiveVisibilityFailures >= MAX_VISIBILITY_FAILURES) {
                    long cooldownMs = RandomUtils.gaussianRandom(
                        (int) VISIBILITY_FAILURE_COOLDOWN_MIN_MS,
                        (int) VISIBILITY_FAILURE_COOLDOWN_MAX_MS,
                        (VISIBILITY_FAILURE_COOLDOWN_MIN_MS + VISIBILITY_FAILURE_COOLDOWN_MAX_MS) / 2.0,
                        (VISIBILITY_FAILURE_COOLDOWN_MAX_MS - VISIBILITY_FAILURE_COOLDOWN_MIN_MS) / 4.0
                    );
                    recentlyMinedRocks.put(targetKey, System.currentTimeMillis() + cooldownMs);
                    script.log(getClass(), "[TAPPING] rock at " + currentTarget.position() +
                        " failed visibility " + consecutiveVisibilityFailures + " times, marking unavailable (" +
                        cooldownMs + "ms cooldown) and resetting cluster");

                    // reset cluster to force picking a new one with visible rocks
                    activeClusterIndex = -1;
                    consecutiveVisibilityFailures = 0;
                    lastVisibilityFailureKey = null;
                }
            }

            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(150, 800, 400, 150));
            resetMiningState();
            return false;
        }

        // successful tap - reset visibility failure tracking
        consecutiveVisibilityFailures = 0;
        lastVisibilityFailureKey = null;

        tapStartPos = script.getWorldPosition();
        miningState = MiningState.APPROACHING;
        return false;
    }

    // ── state: APPROACHING - wait until adjacent to rock ──
    private boolean executeApproaching(WorldPosition myPos) {
        if (currentTarget == null) {
            resetMiningState();
            return false;
        }
        WorldPosition rockPos = currentTarget.position();
        WorldPosition startPos = tapStartPos != null ? tapStartPos : myPos;
        if (!waitForApproachToRock(rockPos, startPos)) {
            script.log(getClass(), "failed to move toward rock " + rockPos);
            skipIdleOnNextFind = false;
            resetMiningState();
            return false;
        }
        // seed chat baseline right before mining wait
        seedChatBaseline();
        miningState = MiningState.MINING;
        return false;
    }

    // ── state: MINING - wait for chat signal / respawn / timeout ──
    private boolean executeMining() {
        if (currentTarget == null) {
            resetMiningState();
            return false;
        }
        MiningResult result = waitForMiningCompletion(currentTarget.position());
        logVerbose("[MINING] result: mined=" + result.mined() + ", noOre=" + result.noOre()
                + ", swingPick=" + result.swingPickSeen());
        pendingResult = result;
        miningState = MiningState.PROCESSING;
        return false;
    }

    // ── state: PROCESSING - handle mining result ──
    private boolean executeProcessing(boolean isUpperMine) {
        if (currentTarget == null || pendingResult == null) {
            resetMiningState();
            return false;
        }
        boolean retry = handleMiningResult(pendingResult, currentTarget.position(), isUpperMine);
        if (retry) {
            // misclick - retry same target from TAPPING
            miningState = MiningState.TAPPING;
        } else {
            resetMiningState();
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STATE CHECKS
    // ═══════════════════════════════════════════════════════════════════════════

    private boolean isStuckTooLong() {
        if (lastSuccessfulAction == 0) {
            lastSuccessfulAction = System.currentTimeMillis();
            stuckThreshold = RandomUtils.gaussianRandom((int) STUCK_TIMEOUT_MIN_MS, (int) STUCK_TIMEOUT_MAX_MS, (STUCK_TIMEOUT_MIN_MS + STUCK_TIMEOUT_MAX_MS) / 2.0, (STUCK_TIMEOUT_MAX_MS - STUCK_TIMEOUT_MIN_MS) / 4.0);
        }
        return System.currentTimeMillis() - lastSuccessfulAction > stuckThreshold;
    }

    private boolean inMiningArea(WorldPosition myPos) {
        return selectedLocation.miningArea().contains(myPos);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STATE HANDLERS
    // ═══════════════════════════════════════════════════════════════════════════

    private void handleStuck() {
        long timeSinceSuccess = System.currentTimeMillis() - lastSuccessfulAction;
        script.log(getClass(), "stuck for " + (timeSinceSuccess / 1000) + " seconds, stopping script");
        task = "STUCK - Stopping";
        script.stop();
    }

    private void walkToMine() {
        task = "Walking to mine";
        script.log(getClass(), "not in mining area, walking to " + selectedLocation.minePosition());
        script.getWalker().walkTo(selectedLocation.minePosition(), new WalkConfig.Builder()
                .breakCondition(() -> {
                    // stop walking if a mineable rock is already on screen
                    List<RSObject> available = script.getObjectManager().getObjects(obj ->
                            obj != null &&
                            obj.isInteractableOnScreen() &&
                            obj.getName() != null &&
                            obj.getName().equalsIgnoreCase(TARGET_OBJECT_NAME) &&
                            obj.getActions() != null &&
                            Arrays.asList(obj.getActions()).contains("Mine") &&
                            !recentlyMinedRocks.containsKey(posKey(obj.getWorldPosition()))
                    );
                    if (available != null && !available.isEmpty()) {
                        script.log(getClass(), "[walkToMine] rock found on screen, stopping walk");
                        return true;
                    }
                    return false;
                })
                .build());
    }

    private void handleNoRocksAvailable(boolean isUpperMine) {
        if (isUpperMine) {
            task = "Hopping worlds";
            script.log(getClass(), "upper mine depleted, hopping worlds");
            emptyRockPositionKeys.clear();
            consecutiveNoOreCount = 0;
            script.getProfileManager().forceHop();
        } else {
            task = "Waiting for respawn";
            script.log(getClass(), "no available gem rocks, waiting for respawn");
            // wait until a rock comes off cooldown instead of spinning
            script.pollFramesUntil(() -> {
                // clean expired cooldowns each frame
                long now = System.currentTimeMillis();
                recentlyMinedRocks.entrySet().removeIf(e -> now > e.getValue());

                List<RSObject> available = script.getObjectManager().getObjects(obj ->
                        obj != null &&
                        obj.isInteractableOnScreen() &&
                        obj.getName() != null &&
                        obj.getName().equalsIgnoreCase(TARGET_OBJECT_NAME) &&
                        obj.getActions() != null &&
                        Arrays.asList(obj.getActions()).contains("Mine") &&
                        !recentlyMinedRocks.containsKey(posKey(obj.getWorldPosition()))
                );
                return available != null && !available.isEmpty();
            }, RandomUtils.gaussianRandom(15000, 25000, 20000, 2500));
            skipIdleOnNextFind = true;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TARGET FINDING
    // ═══════════════════════════════════════════════════════════════════════════

    /** finds best rock target using ObjectManager */
    private RockTarget findBestTarget(WorldPosition myPos, boolean isUpperMine) {
        // clean expired cooldowns
        long now = System.currentTimeMillis();
        recentlyMinedRocks.entrySet().removeIf(e -> now > e.getValue());

        return findObjectManagerTarget(myPos, isUpperMine);
    }

    /** ObjectManager-based rock targeting - dispatches to clustered or greedy selection */
    private RockTarget findObjectManagerTarget(WorldPosition myPos, boolean isUpperMine) {
        List<RSObject> rocks = script.getObjectManager().getObjects(obj ->
                obj != null &&
                obj.isInteractableOnScreen() &&
                obj.getName() != null &&
                obj.getName().equalsIgnoreCase(TARGET_OBJECT_NAME) &&
                obj.getActions() != null &&
                Arrays.asList(obj.getActions()).contains("Mine") &&
                !recentlyMinedRocks.containsKey(posKey(obj.getWorldPosition())) &&
                (!isUpperMine || !emptyRockPositionKeys.contains(posKey(obj.getWorldPosition())))
        );

        if (rocks == null || rocks.isEmpty()) return null;

        // sort by distance for greedy fallback
        rocks = new ArrayList<>(rocks);
        rocks.sort(Comparator.comparingDouble(o -> {
            WorldPosition pos = o.getWorldPosition();
            return pos != null ? pos.distanceTo(myPos) : Double.MAX_VALUE;
        }));

        // underground mine - use sticky hardcoded clusters
        if (!isUpperMine) {
            RockTarget clustered = selectFromCluster(rocks, myPos);
            if (clustered != null) return clustered;
        }

        return selectGreedyNearest(rocks);
    }

    /** greedy nearest - picks first rock with valid hull (list must be pre-sorted by distance) */
    private RockTarget selectGreedyNearest(List<RSObject> rocks) {
        for (RSObject rock : rocks) {
            WorldPosition pos = rock.getWorldPosition();
            Polygon hull = script.getSceneProjector().getConvexHull(rock);
            if (hull == null || hull.numVertices() == 0) continue;

            Polygon shrunk = hull.getResized(0.7);
            logVerbose("[selectGreedy] selected: " + (pos != null ? pos.getX() + "," + pos.getY() : "null"));
            return new RockTarget(pos, shrunk != null ? shrunk : hull);
        }
        return null;
    }

    /**
     * Sticky cluster selection for underground mine. Stays in the active cluster until
     * all its rocks are on cooldown, then picks a new cluster (weighted toward closest,
     * with a chance at 2nd closest). Within a cluster, picks weighted toward nearest rock.
     */
    private RockTarget selectFromCluster(List<RSObject> availableRocks, WorldPosition myPos) {
        // build a set of available rock position keys for fast lookup
        Set<String> availableKeys = new HashSet<>();
        Map<String, RSObject> rockByKey = new HashMap<>();
        for (RSObject rock : availableRocks) {
            WorldPosition pos = rock.getWorldPosition();
            if (pos != null) {
                String key = posKey(pos);
                availableKeys.add(key);
                rockByKey.put(key, rock);
            }
        }

        // if we have a sticky cluster, check if it still has available rocks
        if (activeClusterIndex >= 0 && activeClusterIndex < UNDERGROUND_CLUSTERS.length) {
            List<RSObject> clusterRocks = getAvailableRocksInCluster(activeClusterIndex, availableKeys, rockByKey);
            if (!clusterRocks.isEmpty()) {
                // stay in this cluster - pick weighted toward nearest
                return selectWeightedRockFromCluster(clusterRocks, myPos, activeClusterIndex);
            }
            // cluster depleted - pick a new one
            logVerbose("[selectFromCluster] cluster " + (activeClusterIndex + 1) + " depleted, picking new");
            activeClusterIndex = -1;
        }

        // pick a new cluster: find clusters with available rocks, sort by distance
        List<int[]> candidateClusters = new ArrayList<>(); // [clusterIndex, rockCount]
        for (int i = 0; i < UNDERGROUND_CLUSTERS.length; i++) {
            List<RSObject> clusterRocks = getAvailableRocksInCluster(i, availableKeys, rockByKey);
            if (!clusterRocks.isEmpty()) {
                candidateClusters.add(new int[]{ i, clusterRocks.size() });
            }
        }

        if (candidateClusters.isEmpty()) return null;

        // sort candidates by centroid distance to player
        candidateClusters.sort(Comparator.comparingDouble(c -> clusterCentroidDistance(c[0], myPos)));

        // weighted pick between 2 closest clusters (only if 2nd is within 3 tiles of 1st)
        int picked;
        if (candidateClusters.size() == 1) {
            picked = 0;
        } else {
            double dist1 = clusterCentroidDistance(candidateClusters.get(0)[0], myPos);
            double dist2 = clusterCentroidDistance(candidateClusters.get(1)[0], myPos);
            if (dist2 - dist1 > 3.0) {
                // 2nd cluster is too far away, no human would run there
                picked = 0;
                logVerbose("[selectFromCluster] 2nd cluster too far (+" + String.format("%.1f", dist2 - dist1) + " tiles), using closest");
            } else {
                // 0.15 skew = ~85% chance of closest cluster
                picked = RandomUtils.weightedRandom(0, 1, 0.15);
            }
        }
        activeClusterIndex = candidateClusters.get(picked)[0];
        clusterFirstPick = true;
        script.log(getClass(), "[selectFromCluster] chose cluster " + (activeClusterIndex + 1)
                + "/" + UNDERGROUND_CLUSTERS.length + " (candidate " + (picked + 1)
                + "/" + Math.min(candidateClusters.size(), 2) + ")");

        List<RSObject> clusterRocks = getAvailableRocksInCluster(activeClusterIndex, availableKeys, rockByKey);
        if (clusterRocks.isEmpty()) return null;

        return selectWeightedRockFromCluster(clusterRocks, myPos, activeClusterIndex);
    }

    /** returns available (not on cooldown, on screen) rocks in the given hardcoded cluster */
    private List<RSObject> getAvailableRocksInCluster(int clusterIndex, Set<String> availableKeys, Map<String, RSObject> rockByKey) {
        List<RSObject> result = new ArrayList<>();
        for (WorldPosition pos : UNDERGROUND_CLUSTERS[clusterIndex]) {
            String key = posKey(pos);
            if (availableKeys.contains(key)) {
                result.add(rockByKey.get(key));
            }
        }
        return result;
    }

    /**
     * Picks a rock from a cluster. On first entry (clusterFirstPick), uses weighted random
     * to add variety to the starting rock. After that, always picks greedy nearest.
     */
    private RockTarget selectWeightedRockFromCluster(List<RSObject> clusterRocks, WorldPosition myPos, int clusterIndex) {
        clusterRocks.sort(Comparator.comparingDouble(o -> {
            WorldPosition p = o.getWorldPosition();
            return p != null ? p.distanceTo(myPos) : Double.MAX_VALUE;
        }));

        int rockIdx;
        if (clusterFirstPick) {
            // first rock in a new cluster - heavily weighted toward closest 2 rocks
            // 0.08 skew: ~90%+ chance of index 0 or 1, rocks 3+ are rare but possible
            int maxIdx = clusterRocks.size() - 1;
            rockIdx = RandomUtils.weightedRandom(0, maxIdx, 0.08);
            clusterFirstPick = false;
            logVerbose("[selectRock] first pick in cluster " + (clusterIndex + 1) + ", weighted idx=" + rockIdx);
        } else {
            // continuing in cluster - always greedy nearest
            rockIdx = 0;
        }
        RSObject pick = clusterRocks.get(rockIdx);

        WorldPosition pickPos = pick.getWorldPosition();
        Polygon hull = script.getSceneProjector().getConvexHull(pick);
        if (hull == null || hull.numVertices() == 0) {
            // fallback: try next closest in cluster
            for (int i = 0; i < clusterRocks.size(); i++) {
                if (i == rockIdx) continue;
                RSObject fallback = clusterRocks.get(i);
                Polygon fbHull = script.getSceneProjector().getConvexHull(fallback);
                if (fbHull != null && fbHull.numVertices() > 0) {
                    WorldPosition fbPos = fallback.getWorldPosition();
                    Polygon fbShrunk = fbHull.getResized(0.7);
                    logVerbose("[selectRock] cluster=" + (clusterIndex + 1) + " fallback rock=" + (i + 1)
                            + "/" + clusterRocks.size());
                    return new RockTarget(fbPos, fbShrunk != null ? fbShrunk : fbHull);
                }
            }
            return null;
        }

        Polygon shrunk = hull.getResized(0.7);
        logVerbose("[selectRock] cluster=" + (clusterIndex + 1) + " rock=" + (rockIdx + 1)
                + "/" + clusterRocks.size()
                + " pos=" + (pickPos != null ? pickPos.getX() + "," + pickPos.getY() : "null"));
        return new RockTarget(pickPos, shrunk != null ? shrunk : hull);
    }

    /** distance from a cluster's centroid to the player */
    private double clusterCentroidDistance(int clusterIndex, WorldPosition myPos) {
        WorldPosition[] positions = UNDERGROUND_CLUSTERS[clusterIndex];
        double sumX = 0, sumY = 0;
        for (WorldPosition p : positions) {
            sumX += p.getX();
            sumY += p.getY();
        }
        double cx = sumX / positions.length;
        double cy = sumY / positions.length;
        double dx = cx - myPos.getX();
        double dy = cy - myPos.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** taps a rock target - uses tapGameScreen for 3D world objects */
    private boolean tapTarget(RockTarget target) {
        if (target == null || target.clickArea() == null) return false;

        Polygon clickPoly = target.clickArea();

        // check visibility before clicking - rocks are 3D objects that may be obscured by UI
        double visibility = script.getWidgetManager().insideGameScreenFactor(
                clickPoly,
                List.of(ChatboxComponent.class)  // ignore chatbox (semi-transparent)
        );

        if (visibility < 0.3) {
            logVerbose("[tapTarget] visibility too low: " + String.format("%.2f", visibility) + " for " + target.position());
            return false;  // let caller find a different target
        }

        // shrink polygon for more reliable clicks (avoid edge misclicks)
        Polygon shrunk = clickPoly.getResized(0.7);
        Polygon finalPoly = shrunk != null ? shrunk : clickPoly;

        // use tapGameScreen for all 3D world interactions (rocks, trees, NPCs)
        // tap() can click through UI overlays; tapGameScreen only clicks visible game area
        return script.getFinger().tapGameScreen(finalPoly);
    }

    /** processes mining result - returns true if should retry same rock */
    private boolean handleMiningResult(MiningResult result, WorldPosition rockPos, boolean isUpperMine) {
        // handle "no ore" message - skip idle on next find since player is stationary
        if (result.noOre()) {
            markRockAsMined(rockPos);
            skipIdleOnNextFind = true;
            if (isUpperMine) {
                script.log(getClass(), "rock was empty, marking position: " + rockPos);
                emptyRockPositionKeys.add(posKey(rockPos));
            } else {
                script.log(getClass(), "rock was empty, on cooldown");
            }
            consecutiveNoOreCount++;
            return false;
        }

        // handle no swing pick - could be misclick or depleted rock
        if (!result.mined() && !result.swingPickSeen() && rockPos != null) {
            // if adjacent, the click landed but rock is depleted - treat as empty
            WorldPosition myPos = script.getWorldPosition();
            if (myPos != null && isAdjacent(myPos, rockPos)) {
                script.log(getClass(), "adjacent but no response - rock depleted, on cooldown: " + rockPos);
                markRockAsMined(rockPos);
                skipIdleOnNextFind = true;
                consecutiveMisclickCount = 0;
                lastMisclickPositionKey = null;
                return false;
            }

            // not adjacent = actual misclick (click didn't land on rock)
            String key = posKey(rockPos);
            if (key.equals(lastMisclickPositionKey)) {
                consecutiveMisclickCount++;
            } else {
                consecutiveMisclickCount = 1;
                lastMisclickPositionKey = key;
            }

            if (consecutiveMisclickCount >= MAX_CONSECUTIVE_MISCLICKS) {
                script.log(getClass(), "[STUCK] rock failed " + consecutiveMisclickCount + " times, relocating");
                recoverFromStuckRock(rockPos, isUpperMine);
                return false;
            }

            script.log(getClass(), "misclick " + consecutiveMisclickCount + "/" + MAX_CONSECUTIVE_MISCLICKS);
            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(200, 800, 400, 150));
            return true; // retry
        }

        // handle mining timeout (swing seen but no success)
        if (!result.mined() && rockPos != null) {
            script.log(getClass(), "mining failed/timeout, marking on cooldown: " + rockPos);
            markRockAsMined(rockPos);
            if (isUpperMine) {
                emptyRockPositionKeys.add(posKey(rockPos));
            }
            consecutiveNoOreCount++;
            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(300, 1800, 1000, 300));
            return false;
        }

        // handle success - skip idle on next find since player is stationary at rock
        if (result.mined()) {
            onMiningSuccess(rockPos, isUpperMine);
            skipIdleOnNextFind = true;
        }

        return false;
    }

    /** handles successful mine */
    private void onMiningSuccess(WorldPosition rockPos, boolean isUpperMine) {
        gemsMined++;
        lastSuccessfulAction = System.currentTimeMillis();
        stuckThreshold = RandomUtils.gaussianRandom((int) STUCK_TIMEOUT_MIN_MS, (int) STUCK_TIMEOUT_MAX_MS, (STUCK_TIMEOUT_MIN_MS + STUCK_TIMEOUT_MAX_MS) / 2.0, (STUCK_TIMEOUT_MAX_MS - STUCK_TIMEOUT_MIN_MS) / 4.0);
        consecutiveNoOreCount = 0;
        consecutiveMisclickCount = 0;
        lastMisclickPositionKey = null;

        calculateGpFromMine();
        markRockAsMined(rockPos);

        if (isUpperMine && !emptyRockPositionKeys.isEmpty()) {
            emptyRockPositionKeys.clear();
            script.log(getClass(), "cleared empty positions after successful mine");
        }

        if (TidalsGemMiner.xpTracking != null) {
            TidalsGemMiner.xpTracking.addMiningXp(65.0);
        }

        script.log(getClass(), "mined gem rock, total: " + gemsMined);

        // humanized delay before next rock
        int delay = RandomUtils.weightedRandom(80, 1200, 0.3);
        script.pollFramesUntil(() -> false, delay);
    }

    private boolean isInventoryFull() {
        var inventoryComponent = script.getWidgetManager().getInventory();
        if (inventoryComponent == null) {
            return false;
        }
        ItemGroupResult inventory = inventoryComponent.search(Collections.emptySet());
        return inventory != null && inventory.isFull();
    }

    private void refreshKnownRockPositions(WorldPosition myPos) {
        if (myPos == null || selectedLocation.miningArea() == null) {
            logVerbose("skip rock scan: missing player pos or mining area");
            return;
        }
        if (!selectedLocation.miningArea().contains(myPos)) {
            logVerbose("skip rock scan: player not in mining area");
            return;
        }
        long now = System.currentTimeMillis();
        // randomize scan interval each check to avoid patterns
        long scanInterval = RandomUtils.gaussianRandom((int) ROCK_SCAN_INTERVAL_MIN_MS, (int) ROCK_SCAN_INTERVAL_MAX_MS, (ROCK_SCAN_INTERVAL_MIN_MS + ROCK_SCAN_INTERVAL_MAX_MS) / 2.0, (ROCK_SCAN_INTERVAL_MAX_MS - ROCK_SCAN_INTERVAL_MIN_MS) / 4.0);
        if (now - lastRockScanMs < scanInterval) {
            logVerbose("skip rock scan: throttled (interval=" + scanInterval + "ms)");
            return;
        }
        lastRockScanMs = now;

        List<RSObject> rocks = script.getObjectManager().getObjects(obj ->
                obj != null &&
                obj.getName() != null &&
                obj.getName().equalsIgnoreCase(TARGET_OBJECT_NAME) &&
                obj.getWorldPosition() != null &&
                selectedLocation.miningArea().contains(obj.getWorldPosition())
        );

        if (rocks == null || rocks.isEmpty()) {
            logVerbose("rock scan: no rocks found");
            return;
        }

        int beforeCount = knownRockPositions.size();
        for (RSObject rock : rocks) {
            WorldPosition pos = rock.getWorldPosition();
            if (pos != null) {
                knownRockPositions.add(pos);
            }
        }
        logVerbose("rock scan: found=" + rocks.size() + ", knownRockPositions=" + knownRockPositions.size()
                + " (added " + (knownRockPositions.size() - beforeCount) + ")");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private boolean waitForApproachToRock(WorldPosition rockPos, WorldPosition startPos) {
        if (rockPos == null || startPos == null) {
            logVerbose("approach: missing positions, skip");
            return true;
        }
        if (isAdjacent(startPos, rockPos)) {
            logVerbose("approach: already adjacent");
            return true;
        }
        long startMs = System.currentTimeMillis();

        // wait until we are actually adjacent to the rock - no shortcuts
        boolean reached = script.pollFramesUntil(() -> {
            WorldPosition current = script.getWorldPosition();
            if (current == null) {
                return false;
            }
            return isAdjacent(current, rockPos);
        }, RandomUtils.weightedRandom(4000, 6000, 0.002));
        WorldPosition endPos = script.getWorldPosition();
        double endDistance = endPos != null ? endPos.distanceTo(rockPos) : -1;
        boolean adjacent = endPos != null && isAdjacent(endPos, rockPos);
        logVerbose("approach: endDist=" + String.format("%.2f", endDistance)
                + ", adjacent=" + adjacent + ", reached=" + reached + ", ms=" + (System.currentTimeMillis() - startMs));
        return adjacent;
    }

    private boolean isAdjacent(WorldPosition pos, WorldPosition target) {
        if (pos == null || target == null) {
            return false;
        }
        if (pos.getPlane() != target.getPlane()) {
            return false;
        }
        int dx = Math.abs(pos.getX() - target.getX());
        int dy = Math.abs(pos.getY() - target.getY());
        return Math.max(dx, dy) <= 1;
    }

    private void logVerbose(String message) {
        if (!isVerbose()) {
            return;
        }
        script.log(getClass(), "[DEBUG] " + message);
    }

    private ChatSignal readMiningChatSignal() {
        try {
            Chatbox chatbox = script.getWidgetManager().getChatbox();
            if (chatbox == null) {
                return null;
            }

            UIResultList<String> currentLines = chatbox.getText();
            if (currentLines == null || currentLines.isEmpty()) {
                return null;
            }

            int snapshotHash = computeChatSnapshotHash(currentLines);
            boolean snapshotChanged = snapshotHash != lastChatSnapshotHash;
            lastChatSnapshotHash = snapshotHash;
            if (!snapshotChanged) {
                return null;
            }

            int checked = 0;
            long now = System.currentTimeMillis();

            // first pass: check only the most recent line for primary signals
            for (String line : currentLines) {
                if (checked++ >= CHAT_LINES_TO_CHECK) {
                    break;
                }
                if (checked > 1) {
                    continue;  // only check first line for primary signals
                }
                if (line == null) {
                    continue;
                }
                String lower = line.toLowerCase();
                boolean mined = lower.contains(MINED_MESSAGE);
                boolean noOre = lower.contains(NO_ORE_MESSAGE);
                boolean swingPick = lower.contains(SWING_PICK_MESSAGE);
                boolean clueScroll = lower.contains(CLUE_SCROLL_MESSAGE);

                // any of these is a valid signal
                if (!mined && !noOre && !swingPick && !clueScroll) {
                    continue;
                }

                // randomize repeat window to avoid patterns
                long repeatWindow = RandomUtils.gaussianRandom((int) CHAT_REPEAT_WINDOW_MIN_MS, (int) CHAT_REPEAT_WINDOW_MAX_MS, (CHAT_REPEAT_WINDOW_MIN_MS + CHAT_REPEAT_WINDOW_MAX_MS) / 2.0, (CHAT_REPEAT_WINDOW_MAX_MS - CHAT_REPEAT_WINDOW_MIN_MS) / 4.0);
                if (line.equals(lastChatMatchLine) && now - lastChatMatchMs < repeatWindow) {
                    continue;
                }

                lastChatMatchLine = line;
                lastChatMatchMs = now;
                logVerbose("chat signal: " + line);
                return new ChatSignal(mined, noOre, swingPick, clueScroll, line);
            }

            // second pass: check lines 2-4 specifically for clue scroll (often appears after "you just mined")
            checked = 0;
            for (String line : currentLines) {
                checked++;
                if (checked == 1 || checked > CHAT_LINES_TO_CHECK) {
                    continue;  // skip first line (already checked) and beyond limit
                }
                if (line == null) {
                    continue;
                }
                String lower = line.toLowerCase();
                if (lower.contains(CLUE_SCROLL_MESSAGE)) {
                    // randomize repeat window for clue scroll check
                    long clueRepeatWindow = RandomUtils.gaussianRandom((int) CHAT_REPEAT_WINDOW_MIN_MS, (int) CHAT_REPEAT_WINDOW_MAX_MS, (CHAT_REPEAT_WINDOW_MIN_MS + CHAT_REPEAT_WINDOW_MAX_MS) / 2.0, (CHAT_REPEAT_WINDOW_MAX_MS - CHAT_REPEAT_WINDOW_MIN_MS) / 4.0);
                    if (line.equals(lastChatMatchLine) && now - lastChatMatchMs < clueRepeatWindow) {
                        continue;
                    }
                    lastChatMatchLine = line;
                    lastChatMatchMs = now;
                    logVerbose("chat signal (clue scroll secondary): " + line);
                    return new ChatSignal(false, false, false, true, line);
                }
            }
        } catch (RuntimeException e) {
            return null;
        }
        return null;
    }

    private void seedChatBaseline() {
        try {
            Chatbox chatbox = script.getWidgetManager().getChatbox();
            if (chatbox == null) {
                return;
            }
            UIResultList<String> currentLines = chatbox.getText();
            if (currentLines == null || currentLines.isEmpty()) {
                return;
            }
            lastChatSnapshotHash = computeChatSnapshotHash(currentLines);
            lastChatMatchLine = null;
            lastChatMatchMs = 0;
        } catch (RuntimeException e) {
            // ignore chat baseline failures
        }
    }

    private int computeChatSnapshotHash(UIResultList<String> lines) {
        int checked = 0;
        int hash = 1;
        for (String line : lines) {
            if (checked++ >= CHAT_LINES_TO_CHECK) {
                break;
            }
            hash = 31 * hash + (line == null ? 0 : line.hashCode());
        }
        return hash;
    }

    private boolean waitForPlayerIdle() {
        Timer stationaryTimer = new Timer();
        WorldPosition[] lastPosition = { script.getWorldPosition() };
        long startMs = System.currentTimeMillis();
        // randomize stationary threshold for this idle check
        final int stationaryThreshold = RandomUtils.gaussianRandom(STATIONARY_THRESHOLD_MIN_MS, STATIONARY_THRESHOLD_MAX_MS, (STATIONARY_THRESHOLD_MIN_MS + STATIONARY_THRESHOLD_MAX_MS) / 2.0, (STATIONARY_THRESHOLD_MAX_MS - STATIONARY_THRESHOLD_MIN_MS) / 4.0);

        boolean idle = script.pollFramesUntil(() -> {
            WorldPosition current = script.getWorldPosition();
            if (current == null) {
                return false;
            }

            // reset timer if player moved
            if (lastPosition[0] == null || !current.equals(lastPosition[0])) {
                lastPosition[0] = current;
                stationaryTimer.reset();
            }

            boolean stationary = stationaryTimer.timeElapsed() > stationaryThreshold;
            boolean animating = script.getPixelAnalyzer().isPlayerAnimating(0.4);
            return stationary && !animating;
        }, RandomUtils.weightedRandom(3500, 5000, 0.002));
        logVerbose("waitForPlayerIdle: idle=" + idle + ", ms=" + (System.currentTimeMillis() - startMs));
        return idle;
    }

    private MiningResult waitForMiningCompletion(WorldPosition targetPos) {
        RectangleArea respawnArea = null;
        if (targetPos != null) {
            respawnArea = new RectangleArea(
                    targetPos.getX(),
                    targetPos.getY(),
                    1,
                    1,
                    targetPos.getPlane()
            );
        }

        RectangleArea finalRespawnArea = respawnArea;
        final boolean[] minedByChat = { false };
        final boolean[] noOreSeen = { false };
        final boolean[] respawnSeen = { false };
        final boolean[] inventoryFull = { false };
        final boolean[] swingPickSeen = { false };
        final boolean[] clueScrollSeen = { false };
        final long[] swingPickSeenMs = { 0 };
        long startMs = System.currentTimeMillis();
        logVerbose("waitForMiningCompletion: target=" + targetPos);

        // timeout before seeing swing pick (misclick detection)
        final long swingPickTimeout = RandomUtils.gaussianRandom(2000, 3000, 2500, 250);
        // timeout after seeing swing pick - give mining plenty of time to complete
        final long miningTimeout = RandomUtils.gaussianRandom(18000, 22000, 20000, 1000);

        script.pollFramesUntil(() -> {
            ItemGroupResult inventory = script.getWidgetManager().getInventory().search(Collections.emptySet());
            if (inventory != null && inventory.isFull()) {
                inventoryFull[0] = true;
                return true;
            }

            if (finalRespawnArea != null) {
                PixelAnalyzer.RespawnCircle circle = script.getPixelAnalyzer().getRespawnCircle(
                        finalRespawnArea,
                        PixelAnalyzer.RespawnCircleDrawType.TOP_CENTER,
                        20,
                        6
                );
                if (circle != null) {
                    respawnSeen[0] = true;
                }
            }

            ChatSignal signal = readMiningChatSignal();
            if (signal != null) {
                logVerbose("chat signal detected: mined=" + signal.mined() + ", noOre=" + signal.noOre()
                        + ", swingPick=" + signal.swingPick() + ", clueScroll=" + signal.clueScroll()
                        + ", line=" + signal.line());
                if (signal.noOre()) {
                    noOreSeen[0] = true;
                    return true;
                }
                if (signal.mined()) {
                    minedByChat[0] = true;
                    return true;
                }
                if (signal.swingPick()) {
                    swingPickSeen[0] = true;
                    swingPickSeenMs[0] = System.currentTimeMillis();
                    logVerbose("swing pick detected - mining started, will wait up to " + miningTimeout + "ms");
                    // don't exit - keep polling for completion
                }
                if (signal.clueScroll()) {
                    clueScrollSeen[0] = true;
                    minedByChat[0] = true;
                    logVerbose("clue scroll message detected - counting as successful mine");
                    return true;  // successful mine
                }
            }

            long now = System.currentTimeMillis();

            // no swing pick yet? short timeout for misclick detection
            if (!swingPickSeen[0] && (now - startMs) > swingPickTimeout) {
                logVerbose("no swing pick within " + swingPickTimeout + "ms - likely misclick");
                return true;
            }

            // swing pick seen but mining taking too long? timeout
            if (swingPickSeen[0] && (now - swingPickSeenMs[0]) > miningTimeout) {
                logVerbose("mining timeout after swing pick (" + miningTimeout + "ms)");
                return true;
            }

            return false;
        }, (int) (swingPickTimeout + miningTimeout + 2000));

        boolean mined = minedByChat[0] || clueScrollSeen[0] ||
                (!noOreSeen[0] && (respawnSeen[0] || inventoryFull[0]));
        logVerbose("waitForMiningCompletion: mined=" + mined + ", noOre=" + noOreSeen[0]
                + ", respawnSeen=" + respawnSeen[0] + ", invFull=" + inventoryFull[0]
                + ", swingPickSeen=" + swingPickSeen[0] + ", clueScrollSeen=" + clueScrollSeen[0]
                + ", ms=" + (System.currentTimeMillis() - startMs));
        return new MiningResult(mined, noOreSeen[0], respawnSeen[0], swingPickSeen[0]);
    }


    /**
     * marks a rock as recently mined (adds to cooldown with randomized duration)
     */
    private void markRockAsMined(WorldPosition pos) {
        if (pos != null) {
            long expiryTime = System.currentTimeMillis() + ROCK_COOLDOWN_MS;
            recentlyMinedRocks.put(posKey(pos), expiryTime);
            logVerbose("marked rock on cooldown (" + ROCK_COOLDOWN_MS + "ms): " + pos);
        }
    }

    /**
     * recovers from being stuck on a depleted rock by marking it and walking to a different area
     */
    private void recoverFromStuckRock(WorldPosition stuckPos, boolean isUpperMine) {
        task = "Relocating";

        // mark stuck rock as empty with extended cooldown
        if (stuckPos != null) {
            String stuckKey = posKey(stuckPos);
            emptyRockPositionKeys.add(stuckKey);
            // use randomized extended cooldown for stuck rocks (50-70s)
            long stuckCooldownMs = RandomUtils.gaussianRandom((int) STUCK_ROCK_COOLDOWN_MIN_MS, (int) STUCK_ROCK_COOLDOWN_MAX_MS, (STUCK_ROCK_COOLDOWN_MIN_MS + STUCK_ROCK_COOLDOWN_MAX_MS) / 2.0, (STUCK_ROCK_COOLDOWN_MAX_MS - STUCK_ROCK_COOLDOWN_MIN_MS) / 4.0);
            recentlyMinedRocks.put(stuckKey, System.currentTimeMillis() + stuckCooldownMs);
            script.log(getClass(), "marked stuck rock as empty (" + stuckCooldownMs + "ms cooldown): " + stuckPos);
        }

        // reset misclick tracking
        consecutiveMisclickCount = 0;
        lastMisclickPositionKey = null;

        // find a random rock position at least 3 tiles away
        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) {
            script.log(getClass(), "cannot relocate - unknown position");
            return;
        }

        Set<WorldPosition> rockPositions = knownRockPositions;
        List<WorldPosition> validTargets = new ArrayList<>();

        int skippedStuck = 0, skippedCooldown = 0, skippedEmpty = 0, skippedTooClose = 0;
        for (WorldPosition pos : rockPositions) {
            if (pos == null) continue;
            String pk = posKey(pos);
            if (pos.equals(stuckPos)) { skippedStuck++; continue; }
            if (recentlyMinedRocks.containsKey(pk)) { skippedCooldown++; continue; }
            if (emptyRockPositionKeys.contains(pk)) { skippedEmpty++; continue; }
            if (pos.distanceTo(myPos) < 3.0) { skippedTooClose++; continue; }
            validTargets.add(pos);
        }

        logVerbose("[recover] from=" + myPos.getX() + "," + myPos.getY()
                + " stuck=" + stuckPos.getX() + "," + stuckPos.getY()
                + " validTargets=" + validTargets.size()
                + " skipped[stuck=" + skippedStuck + " cooldown=" + skippedCooldown
                + " empty=" + skippedEmpty + " tooClose=" + skippedTooClose + "]");

        if (validTargets.isEmpty()) {
            script.log(getClass(), "[recover] no valid targets found - will wait and retry");
            // just wait a bit and let normal flow handle it
            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(1000, 3000, 2000, 500));
            return;
        }

        // pick a random target
        WorldPosition target = validTargets.get(RandomUtils.uniformRandom(0, validTargets.size() - 1));
        double targetDist = target.distanceTo(myPos);
        script.log(getClass(), "[recover] walking to " + target.getX() + "," + target.getY()
                + " (dist=" + String.format("%.1f", targetDist) + ", " + validTargets.size() + " options)");

        script.getWalker().walkTo(target, new WalkConfig.Builder()
                .breakCondition(() -> {
                    // stop walking if any rock comes off cooldown and is interactable
                    List<RSObject> available = script.getObjectManager().getObjects(obj ->
                            obj != null &&
                            obj.isInteractableOnScreen() &&
                            obj.getName() != null &&
                            obj.getName().equalsIgnoreCase(TARGET_OBJECT_NAME) &&
                            obj.getActions() != null &&
                            Arrays.asList(obj.getActions()).contains("Mine") &&
                            !recentlyMinedRocks.containsKey(posKey(obj.getWorldPosition()))
                    );
                    if (available != null && !available.isEmpty()) {
                        script.log(getClass(), "[recover] rock available, stopping walk");
                        return true;
                    }
                    return false;
                })
                .build());
    }

    /**
     * snapshots current gem counts in inventory (call before mining)
     */
    private void snapshotGemCounts() {
        lastGemCounts.clear();
        var inventory = script.getWidgetManager().getInventory();
        if (inventory == null) return;

        // search for all gem types at once
        Set<Integer> allGemIds = new HashSet<>(TidalsGemMiner.GEM_ITEM_IDS.values());
        ItemGroupResult result = inventory.search(allGemIds);
        if (result == null) return;

        for (int itemId : allGemIds) {
            int count = result.getAmount(itemId);
            lastGemCounts.put(itemId, count);
        }
    }

    /**
     * compares inventory to snapshot and calculates GP + banked crafting XP (call after successful mine)
     */
    private void calculateGpFromMine() {
        if (lastGemCounts.isEmpty()) return;

        var inventory = script.getWidgetManager().getInventory();
        if (inventory == null) return;

        Set<Integer> allGemIds = new HashSet<>(TidalsGemMiner.GEM_ITEM_IDS.values());
        ItemGroupResult result = inventory.search(allGemIds);
        if (result == null) return;

        for (Map.Entry<String, Integer> entry : TidalsGemMiner.GEM_ITEM_IDS.entrySet()) {
            int itemId = entry.getValue();
            int currentCount = result.getAmount(itemId);
            int previousCount = lastGemCounts.getOrDefault(itemId, 0);

            if (currentCount > previousCount) {
                int gained = currentCount - previousCount;

                // track GP (only if prices loaded)
                if (TidalsGemMiner.pricesLoaded) {
                    int price = TidalsGemMiner.gemPrices.getOrDefault(itemId, 0);
                    long gpGained = (long) gained * price;
                    TidalsGemMiner.totalGpEarned += gpGained;
                }

                // track banked crafting XP
                double craftingXp = TidalsGemMiner.GEM_CRAFTING_XP.getOrDefault(itemId, 0.0);
                double xpGained = gained * craftingXp;
                TidalsGemMiner.bankedCraftingXp += xpGained;

                script.log(getClass(), "gained " + gained + "x " + entry.getKey() +
                    " (+" + TidalsGemMiner.gemPrices.getOrDefault(itemId, 0) * gained + " gp, +" + xpGained + " craft xp)");
                break; // only one gem per mine
            }
        }
    }

}
