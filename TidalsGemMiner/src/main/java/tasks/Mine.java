package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.ui.chatbox.Chatbox;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResultList;
import com.osmb.api.utils.timing.Timer;
import com.osmb.api.visual.PixelAnalyzer;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.PixelCluster;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import com.osmb.api.walker.WalkConfig;
import utils.Task;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static main.TidalsGemMiner.*;

// gem rock mining XP
import main.TidalsGemMiner;

public class Mine extends Task {

    private static final String TARGET_OBJECT_NAME = "Gem rocks";
    private static final long STUCK_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes
    private static final String NO_ORE_MESSAGE = "there is currently no ore available in this rock";
    private static final String MINED_MESSAGE = "you just mined";
    private static final String SWING_PICK_MESSAGE = "you swing your pick at the rock";
    private static final String CLUE_SCROLL_MESSAGE = "you have a sneaking suspicion";

    // gem rock color detection (different colors per mine)
    private static final int GEM_ROCK_COLOR_UPPER = -7990908;
    private static final int GEM_ROCK_COLOR_UNDERGROUND = -9105036;
    private static final int COLOR_TOLERANCE = 10;
    private static final int CLUSTER_MAX_DISTANCE = 5;
    private static final int CLUSTER_MIN_SIZE = 20;
    private static final int TILE_GEM_ROCK_COLOR = -9039499;
    private static final int TILE_COLOR_TOLERANCE = 5;
    private static final int TILE_CUBE_HEIGHT = 150;
    private static final int TILE_PIXEL_SKIP = 2;
    private static final long ROCK_SCAN_INTERVAL_MS = 4_000L;
    private static final int CHAT_LINES_TO_CHECK = 4;
    private static final long CHAT_REPEAT_WINDOW_MS = 2000L;
    private static final SearchablePixel TILE_GEM_PIXEL = new SearchablePixel(
            TILE_GEM_ROCK_COLOR,
            new SingleThresholdComparator(TILE_COLOR_TOLERANCE),
            ColorModel.RGB
    );
    private static final boolean VERBOSE_LOGGING = true;

    // hardcoded underground mine rock positions (plane 0, region 11410)
    private static final Set<WorldPosition> UNDERGROUND_ROCK_POSITIONS = Set.of(
        new WorldPosition(2844, 9391, 0), new WorldPosition(2845, 9391, 0),
        new WorldPosition(2832, 9381, 0), new WorldPosition(2840, 9397, 0),
        new WorldPosition(2831, 9387, 0), new WorldPosition(2833, 9391, 0),
        new WorldPosition(2848, 9379, 0), new WorldPosition(2831, 9396, 0),
        new WorldPosition(2828, 9392, 0), new WorldPosition(2847, 9379, 0),
        new WorldPosition(2847, 9381, 0), new WorldPosition(2838, 9381, 0),
        new WorldPosition(2839, 9381, 0), new WorldPosition(2846, 9390, 0),
        new WorldPosition(2833, 9380, 0), new WorldPosition(2843, 9392, 0),
        new WorldPosition(2831, 9382, 0), new WorldPosition(2829, 9384, 0),
        new WorldPosition(2831, 9386, 0), new WorldPosition(2835, 9392, 0),
        new WorldPosition(2836, 9392, 0), new WorldPosition(2837, 9397, 0),
        new WorldPosition(2848, 9386, 0), new WorldPosition(2837, 9380, 0),
        new WorldPosition(2847, 9390, 0), new WorldPosition(2848, 9390, 0),
        // added from user feedback
        new WorldPosition(2849, 9387, 0), new WorldPosition(2827, 9391, 0),
        new WorldPosition(2826, 9390, 0), new WorldPosition(2826, 9389, 0),
        new WorldPosition(2827, 9388, 0), new WorldPosition(2832, 9399, 0),
        new WorldPosition(2837, 9398, 0), new WorldPosition(2827, 9386, 0),
        new WorldPosition(2833, 9398, 0), new WorldPosition(2835, 9398, 0),
        new WorldPosition(2836, 9398, 0)
    );
    private static final long SWING_PICK_TIMEOUT_MS = 2500; // exit early if no swing pick within 2.5s

    // cooldown tracking - prevents re-mining same rock
    private static final long ROCK_COOLDOWN_MS = 10_000; // 10 seconds
    private final Map<WorldPosition, Long> recentlyMinedRocks = new HashMap<>();

    // stuck detection
    private long lastSuccessfulAction = 0;

    // track empty rocks in upper mine (positions that gave "no ore" message)
    private Set<WorldPosition> emptyRockPositions = new HashSet<>();
    private int consecutiveNoOreCount = 0;
    private final Set<WorldPosition> knownRockPositions = new HashSet<>();

    // inventory snapshot for GP tracking
    private Map<Integer, Integer> lastGemCounts = new HashMap<>();
    private long lastRockScanMs = 0;
    private String lastChatMatchLine = null;
    private long lastChatMatchMs = 0;
    private int lastChatSnapshotHash = 0;

    private record RockCandidate(WorldPosition position, Polygon tileCube, double distance) {}
    private record MiningResult(boolean mined, boolean noOre, boolean respawnSeen, boolean swingPickSeen) {}
    private record ChatSignal(boolean mined, boolean noOre, boolean swingPick, boolean clueScroll, String line) {}

    public Mine(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        // don't activate if deposit box is open
        if (script.getWidgetManager().getDepositBox().isVisible()) {
            return false;
        }
        // only activate when setup is done and inventory is not full
        return setupDone && !isInventoryFull();
    }

    @Override
    public boolean execute() {
        task = "Mining";
        logVerbose("execute start");

        // initialize stuck timer on first run
        if (lastSuccessfulAction == 0) {
            lastSuccessfulAction = System.currentTimeMillis();
        }

        // stuck detection - stop if no successful action for too long
        long timeSinceSuccess = System.currentTimeMillis() - lastSuccessfulAction;
        if (timeSinceSuccess > STUCK_TIMEOUT_MS) {
            script.log(getClass(), "stuck for " + (timeSinceSuccess / 1000) + " seconds, stopping script");
            task = "STUCK - Stopping";
            script.stop();
            return false;
        }

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) {
            logVerbose("abort: player position is null");
            return false;
        }

        boolean isUpperMine = selectedLocation.name().equals("upper");
        logVerbose("pos=" + myPos + ", upperMine=" + isUpperMine + ", invFull=" + isInventoryFull());

        // only refresh rock positions for upper mine (underground uses hardcoded)
        if (isUpperMine) {
            refreshKnownRockPositions(myPos);
        }

        RockCandidate tileTarget = findTileCubeTarget(myPos, isUpperMine);
        if (tileTarget != null) {
            task = "Mining";
            logVerbose("tile target: pos=" + tileTarget.position() + ", dist=" + String.format("%.2f", tileTarget.distance()));
            if (!waitForPlayerIdle()) {
                logVerbose("idle wait failed before tile target");
                return false;
            }

            // snapshot inventory before mining for GP tracking
            snapshotGemCounts();

            if (!tapRockCandidate(tileTarget)) {
                logVerbose("tap tile target failed");
                // brief pause before retry - gaussian centered around 400ms
                script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(150, 800, 400, 150));
                return false;
            }

            WorldPosition tapStartPos = script.getWorldPosition();
            if (!waitForApproachToRock(tileTarget.position(), tapStartPos != null ? tapStartPos : myPos)) {
                script.log(getClass(), "failed to move toward rock " + tileTarget.position());
                logVerbose("approach failed for tile target");
                return false;
            }

            WorldPosition rockPos = tileTarget.position();
            MiningResult miningResult = waitForMiningCompletion(rockPos);
            boolean mined = miningResult.mined();

            if (miningResult.noOre()) {
                // rock was empty - mark on cooldown so we don't retry immediately
                markRockAsMined(rockPos);
                if (isUpperMine) {
                    script.log(getClass(), "rock was empty (no ore message), marking position: " + rockPos);
                    emptyRockPositions.add(rockPos);
                } else {
                    script.log(getClass(), "rock was empty, on cooldown for " + (ROCK_COOLDOWN_MS / 1000) + "s");
                }
                consecutiveNoOreCount++;
                return false; // immediately try next rock
            }

            // misclick detection: no swing pick message = click didn't land on rock
            if (!mined && !miningResult.noOre() && !miningResult.swingPickSeen() && rockPos != null) {
                script.log(getClass(), "misclick detected - no swing pick message, retrying same rock: " + rockPos);
                // short pause before retry - don't mark on cooldown
                script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(200, 800, 400, 150));
                return true;  // retry in next poll cycle (same rock still valid)
            }

            // mining started but failed (swing seen, no success)
            if (!mined && rockPos != null) {
                script.log(getClass(), "mining failed/timeout, marking on cooldown: " + rockPos);
                markRockAsMined(rockPos);
                if (isUpperMine) {
                    emptyRockPositions.add(rockPos);
                }
                consecutiveNoOreCount++;
                // confused pause - gaussian centered around 1000ms
                script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(300, 1800, 1000, 300));
                return false;
            }

            if (mined) {
                gemsMined++;
                lastSuccessfulAction = System.currentTimeMillis();
                consecutiveNoOreCount = 0;

                // calculate GP from inventory change
                calculateGpFromMine();

                // mark rock on cooldown so we don't re-target it
                markRockAsMined(rockPos);

                if (isUpperMine && !emptyRockPositions.isEmpty()) {
                    emptyRockPositions.clear();
                    script.log(getClass(), "cleared empty rock positions after successful mine");
                }
                if (TidalsGemMiner.xpTracking != null) {
                    TidalsGemMiner.xpTracking.addMiningXp(65.0);
                }
                script.log(getClass(), "mined gem rock, total: " + gemsMined);

                // humanized delay before clicking next rock
                // gaussian distribution: mean=350ms, stdDev=200ms, range 80-1200ms
                // wider stdDev gives occasional longer pauses for more human-like behavior
                int delay = RandomUtils.gaussianRandom(80, 1200, 350, 200);
                script.pollFramesUntil(() -> false, delay);
            }

            return false;
        }

        logVerbose("no tile target found, falling back to ObjectManager");
        // find available gem rocks that are interactable and not depleted
        List<RSObject> gemRocks = script.getObjectManager().getObjects(obj ->
                obj != null &&
                obj.isInteractableOnScreen() &&
                obj.getName() != null &&
                obj.getName().equalsIgnoreCase(TARGET_OBJECT_NAME) &&
                obj.getActions() != null &&
                Arrays.asList(obj.getActions()).contains("Mine") &&
                // for upper mine, also filter out rocks we know are empty (distance-based check)
                (!isUpperMine || !isPositionMarkedEmpty(obj.getWorldPosition()))
        );

        if (gemRocks != null && !gemRocks.isEmpty()) {
            gemRocks = new ArrayList<>(gemRocks);
            Set<WorldPosition> respawnCirclePositions = getRespawnCirclePositions(gemRocks);
            if (!respawnCirclePositions.isEmpty()) {
                gemRocks.removeIf(obj -> respawnCirclePositions.contains(obj.getWorldPosition()));
            }
            logVerbose("object manager rocks=" + gemRocks.size() + ", respawnFiltered=" + respawnCirclePositions.size());
        }

        // if no rocks from ObjectManager, try color detection as fallback
        if (gemRocks == null || gemRocks.isEmpty()) {
            script.log(getClass(), "no available gem rocks from object manager, trying color detection");

            List<Point> gemRockPixels = findGemRocksByColor();
            if (!gemRockPixels.isEmpty()) {
                script.log(getClass(), "found " + gemRockPixels.size() + " gem rock clusters by color");
                // click the nearest cluster center
                Point nearest = findNearestPoint(gemRockPixels, myPos);
                if (nearest != null) {
                    task = "Mining (color)";
                    script.log(getClass(), "clicking gem rock at " + nearest.x + "," + nearest.y);
                    boolean clicked = script.getFinger().tap(new Rectangle(nearest.x - 5, nearest.y - 5, 10, 10));
                    if (clicked) {
                        MiningResult miningResult = waitForMiningCompletion(null);
                        if (miningResult.noOre()) {
                            script.log(getClass(), "color-detected rock was empty");
                            consecutiveNoOreCount++;
                            // "oops wrong rock" pause - gaussian centered around 1200ms
                            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(400, 2000, 1200, 350));
                        } else if (miningResult.mined()) {
                            gemsMined++;
                            lastSuccessfulAction = System.currentTimeMillis();
                            consecutiveNoOreCount = 0;
                            if (TidalsGemMiner.xpTracking != null) {
                                TidalsGemMiner.xpTracking.addMiningXp(65.0);
                            }
                            script.log(getClass(), "mined gem rock (color), total: " + gemsMined);
                        }
                    }
                    return false;
                }
            }

            // no rocks visible at all - check if we should walk to mining area or hop
            if (!selectedLocation.miningArea().contains(myPos)) {
                task = "Walking to mine";
                script.log(getClass(), "no rocks visible, walking to mining area");
                script.getWalker().walkTo(selectedLocation.minePosition(), new WalkConfig.Builder().build());
                logVerbose("walkTo minePosition=" + selectedLocation.minePosition());
                return false;
            }

            // in mining area but no rocks - wait or hop
            if (isUpperMine) {
                task = "Hopping worlds";
                script.log(getClass(), "upper mine depleted, hopping worlds");
                emptyRockPositions.clear(); // reset for new world
                consecutiveNoOreCount = 0;
                script.getProfileManager().forceHop();
            } else {
                // underground has many rocks - just wait for respawn
                task = "Waiting for respawn";
            }
            return false;
        }

        task = "Mining";

        // sort by distance to player (nearest first)
        gemRocks.sort(Comparator.comparingDouble(o -> {
            WorldPosition pos = o.getWorldPosition();
            return pos != null ? pos.distanceTo(myPos) : Double.MAX_VALUE;
        }));

        RSObject targetRock = gemRocks.get(0);
        if (targetRock == null) {
            return false;
        }

        // wait for player to be idle before interacting
        if (!waitForPlayerIdle()) {
            return false;
        }

        WorldPosition rockPos = targetRock.getWorldPosition();
        logVerbose("target rock from ObjectManager=" + rockPos);

        // tap the rock to mine it
        if (!tapGemRock(targetRock)) {
            logVerbose("tap gem rock failed");
            // brief pause before retry - gaussian centered around 400ms
            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(150, 800, 400, 150));
            return false;
        }

        WorldPosition tapStartPos = script.getWorldPosition();
        if (!waitForApproachToRock(rockPos, tapStartPos != null ? tapStartPos : myPos)) {
            script.log(getClass(), "failed to move toward rock " + rockPos);
            logVerbose("approach failed for object rock");
            return false;
        }

        // wait for mining to complete
        MiningResult miningResult = waitForMiningCompletion(rockPos);
        boolean mined = miningResult.mined();
        logVerbose("mining completion result mined=" + mined + ", noOre=" + miningResult.noOre()
                + ", respawnSeen=" + miningResult.respawnSeen() + ", swingPickSeen=" + miningResult.swingPickSeen());

        if (miningResult.noOre()) {
            if (isUpperMine) {
                script.log(getClass(), "rock was empty (no ore message), marking position: " + rockPos);
                if (rockPos != null) {
                    emptyRockPositions.add(rockPos);
                    script.log(getClass(), "empty positions now: " + emptyRockPositions.size());
                }
            } else {
                script.log(getClass(), "rock was empty (no ore message), trying another rock");
            }
            consecutiveNoOreCount++;
            script.log(getClass(), "consecutive no ore count: " + consecutiveNoOreCount);
            // "oops wrong rock" pause - gaussian centered around 1200ms
            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(400, 2000, 1200, 350));
            return false;
        }

        // misclick detection: no swing pick message = click didn't land on rock
        if (!mined && !miningResult.noOre() && !miningResult.swingPickSeen() && rockPos != null) {
            script.log(getClass(), "misclick detected (ObjectManager) - no swing pick message, retrying: " + rockPos);
            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(200, 800, 400, 150));
            return true;  // retry in next poll cycle
        }

        // upper mine: if mining failed with swing pick (started but didn't complete), mark as suspicious
        // this handles cases where the rock is visually empty but ObjectManager reports it as valid
        if (isUpperMine && !mined && rockPos != null) {
            script.log(getClass(), "mining failed without message, marking position suspicious: " + rockPos);
            emptyRockPositions.add(rockPos);
            consecutiveNoOreCount++;
            // confused pause - gaussian centered around 1000ms
            script.pollFramesUntil(() -> false, RandomUtils.gaussianRandom(300, 1800, 1000, 300));
            return false;
        }

        if (mined) {
            gemsMined++;
            lastSuccessfulAction = System.currentTimeMillis();
            consecutiveNoOreCount = 0;
            // clear empty positions on successful mine (rocks may have respawned)
            if (isUpperMine && !emptyRockPositions.isEmpty()) {
                emptyRockPositions.clear();
                script.log(getClass(), "cleared empty rock positions after successful mine");
            }
            // add mining XP (65 XP per gem rock)
            if (TidalsGemMiner.xpTracking != null) {
                TidalsGemMiner.xpTracking.addMiningXp(65.0);
            }
            script.log(getClass(), "mined gem rock, total: " + gemsMined);
        }

        return false; // re-evaluate state
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
        if (now - lastRockScanMs < ROCK_SCAN_INTERVAL_MS) {
            logVerbose("skip rock scan: throttled");
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

    private RockCandidate findTileCubeTarget(WorldPosition myPos, boolean isUpperMine) {
        if (myPos == null) {
            logVerbose("tile target: no player position");
            return null;
        }

        // use hardcoded positions for underground, dynamic for upper
        Set<WorldPosition> rockPositions = isUpperMine ? knownRockPositions : UNDERGROUND_ROCK_POSITIONS;
        if (rockPositions.isEmpty()) {
            logVerbose("tile target: no known positions");
            return null;
        }

        // clean up expired cooldowns
        long now = System.currentTimeMillis();
        recentlyMinedRocks.entrySet().removeIf(e -> now - e.getValue() > ROCK_COOLDOWN_MS);

        logVerbose("tile target scan: known=" + rockPositions.size() + ", onCooldown=" + recentlyMinedRocks.size());
        lastRockStates.clear();  // reset debug states each scan
        List<RockCandidate> candidates = new ArrayList<>();
        for (WorldPosition pos : rockPositions) {
            if (pos == null || pos.getPlane() != myPos.getPlane()) {
                continue;
            }

            // skip rocks on cooldown (recently mined)
            if (recentlyMinedRocks.containsKey(pos)) {
                logVerbose("tile target: skipping cooldown rock at " + pos);
                updateDebugRockState(pos, "cooldown");
                continue;
            }

            if (isUpperMine && isPositionMarkedEmpty(pos)) {
                updateDebugRockState(pos, "cooldown");
                continue;
            }
            if (selectedLocation.miningArea() != null && !selectedLocation.miningArea().contains(pos)) {
                continue;
            }

            Polygon tileCube = script.getSceneProjector().getTileCube(pos, TILE_CUBE_HEIGHT, true);
            if (tileCube == null || tileCube.numVertices() == 0) {
                logVerbose("tile target: cube not visible for " + pos);
                continue;
            }
            if (!hasGemColorInCube(tileCube)) {
                logVerbose("tile target: no gem color at " + pos);
                updateDebugRockState(pos, "no_gem");
                continue;
            }

            // check for visible respawn circle (rock is depleted but color check passed)
            RectangleArea respawnCheckArea = new RectangleArea(pos.getX(), pos.getY(), 1, 1, pos.getPlane());
            PixelAnalyzer.RespawnCircle respawnCircle = script.getPixelAnalyzer().getRespawnCircle(
                    respawnCheckArea,
                    PixelAnalyzer.RespawnCircleDrawType.TOP_CENTER,
                    20,
                    6
            );
            if (respawnCircle != null) {
                logVerbose("tile target: respawn circle visible at " + pos + ", skipping");
                updateDebugRockState(pos, "cooldown");
                continue;
            }

            updateDebugRockState(pos, "valid");
            candidates.add(new RockCandidate(pos, tileCube, pos.distanceTo(myPos)));
        }

        if (candidates.isEmpty()) {
            logVerbose("tile target scan: no candidates after color check");
            return null;
        }

        candidates.sort(Comparator.comparingDouble(RockCandidate::distance));

        // log top 3 candidates with distances for debugging
        StringBuilder sb = new StringBuilder("tile target scan: candidates=" + candidates.size() + " [");
        for (int i = 0; i < Math.min(3, candidates.size()); i++) {
            RockCandidate c = candidates.get(i);
            sb.append(String.format("(%d,%d dist=%.1f)", c.position().getX(), c.position().getY(), c.distance()));
            if (i < Math.min(3, candidates.size()) - 1) sb.append(", ");
        }
        sb.append("]");
        logVerbose(sb.toString());

        RockCandidate selected = candidates.get(0);
        setDebugSelectedRock(selected.position());
        script.log(getClass(), "selected rock: " + selected.position().getX() + "," + selected.position().getY()
                + " dist=" + String.format("%.1f", selected.distance()));
        return selected;
    }

    private boolean hasGemColorInCube(Polygon tileCube) {
        if (tileCube == null) {
            return false;
        }
        List<Point> matches = script.getPixelAnalyzer().findPixelsOnGameScreen(
                TILE_PIXEL_SKIP,
                tileCube,
                TILE_GEM_PIXEL
        );
        boolean hasMatch = matches != null && !matches.isEmpty();
        logVerbose("tile color check: matches=" + (matches == null ? 0 : matches.size()) + ", hit=" + hasMatch);
        return hasMatch;
    }

    private boolean tapRockCandidate(RockCandidate target) {
        if (target == null || target.tileCube() == null) {
            logVerbose("tap candidate: missing tile cube");
            return false;
        }
        Point clickPoint = getGaussianPointInPolygon(target.tileCube());
        if (clickPoint != null) {
            logVerbose("tap candidate: point=" + clickPoint);
            return script.getFinger().tap(clickPoint);
        }
        logVerbose("tap candidate: fallback to polygon tap");
        return script.getFinger().tapGameScreen(target.tileCube());
    }

    private Point getGaussianPointInPolygon(Polygon polygon) {
        if (polygon == null) {
            return null;
        }
        Rectangle bounds = polygon.getBounds();
        if (bounds.width <= 0 || bounds.height <= 0) {
            return null;
        }

        int centerX = bounds.x + bounds.width / 2;
        int centerY = bounds.y + bounds.height / 2;
        int halfWidth = Math.max(1, bounds.width / 2);
        int halfHeight = Math.max(1, bounds.height / 2);
        double xStdDev = Math.max(1.0, halfWidth / 3.0);
        double yStdDev = Math.max(1.0, halfHeight / 3.0);

        for (int i = 0; i < 6; i++) {
            int offsetX = RandomUtils.gaussianRandom(-halfWidth, halfWidth, 0, xStdDev);
            int offsetY = RandomUtils.gaussianRandom(-halfHeight, halfHeight, 0, yStdDev);
            int x = Math.max(bounds.x, Math.min(bounds.x + bounds.width - 1, centerX + offsetX));
            int y = Math.max(bounds.y, Math.min(bounds.y + bounds.height - 1, centerY + offsetY));
            Point point = new Point(x, y);
            if (polygon.contains(point)) {
                return point;
            }
        }

        return null;
    }

    private boolean waitForApproachToRock(WorldPosition rockPos, WorldPosition startPos) {
        if (rockPos == null || startPos == null) {
            logVerbose("approach: missing positions, skip");
            return true;
        }
        if (isAdjacent(startPos, rockPos)) {
            logVerbose("approach: already adjacent");
            return true;
        }
        double startDistance = startPos.distanceTo(rockPos);
        long startMs = System.currentTimeMillis();

        boolean reached = script.pollFramesUntil(() -> {
            WorldPosition current = script.getWorldPosition();
            if (current == null) {
                return false;
            }
            if (isAdjacent(current, rockPos)) {
                return true;
            }
            WorldPosition expected = script.getExpectedWorldPosition();
            if (expected != null && expected.distanceTo(rockPos) < startDistance) {
                return true;
            }
            return current.distanceTo(rockPos) < startDistance;
        }, RandomUtils.weightedRandom(1200, 2400, 0.002));
        WorldPosition endPos = script.getWorldPosition();
        double endDistance = endPos != null ? endPos.distanceTo(rockPos) : -1;
        logVerbose("approach: startDist=" + String.format("%.2f", startDistance)
                + ", endDist=" + String.format("%.2f", endDistance)
                + ", reached=" + reached + ", ms=" + (System.currentTimeMillis() - startMs));
        return reached;
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
        if (!VERBOSE_LOGGING) {
            return;
        }
        script.log(getClass(), "[VERBOSE] " + message);
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

                if (line.equals(lastChatMatchLine) && now - lastChatMatchMs < CHAT_REPEAT_WINDOW_MS) {
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
                    if (line.equals(lastChatMatchLine) && now - lastChatMatchMs < CHAT_REPEAT_WINDOW_MS) {
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

    private Set<WorldPosition> getRespawnCirclePositions(List<RSObject> objects) {
        try {
            if (objects == null || objects.isEmpty()) {
                return Collections.emptySet();
            }

            Map<RSObject, PixelAnalyzer.RespawnCircle> respawnCircleObjects =
                    script.getPixelAnalyzer().getRespawnCircleObjects(
                            objects,
                            PixelAnalyzer.RespawnCircleDrawType.TOP_CENTER,
                            20,
                            6
                    );

            if (respawnCircleObjects == null || respawnCircleObjects.isEmpty()) {
                logVerbose("respawn circles: none detected for objects");
                return Collections.emptySet();
            }

            Set<WorldPosition> positions = new HashSet<>();
            for (RSObject obj : respawnCircleObjects.keySet()) {
                WorldPosition pos = obj.getWorldPosition();
                if (pos != null) {
                    positions.add(pos);
                }
            }

            return positions;
        } catch (RuntimeException e) {
            script.log(getClass(), "error getting respawn circles: " + e.getMessage());
            return Collections.emptySet();
        }
    }

    private boolean waitForPlayerIdle() {
        Timer stationaryTimer = new Timer();
        WorldPosition[] lastPosition = { script.getWorldPosition() };
        long startMs = System.currentTimeMillis();

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

            boolean stationary = stationaryTimer.timeElapsed() > 250;
            boolean animating = script.getPixelAnalyzer().isPlayerAnimating(0.4);
            return stationary && !animating;
        }, 4_000);
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
        long startMs = System.currentTimeMillis();
        logVerbose("waitForMiningCompletion: target=" + targetPos);
        seedChatBaseline();

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
                    logVerbose("swing pick detected - mining started");
                    // don't exit - keep polling for completion
                }
                if (signal.clueScroll()) {
                    clueScrollSeen[0] = true;
                    minedByChat[0] = true;
                    logVerbose("clue scroll message detected - counting as successful mine");
                    return true;  // successful mine
                }
            }

            // early exit for misclick: if no swing pick seen within timeout, click missed
            if (!swingPickSeen[0] && (System.currentTimeMillis() - startMs) > SWING_PICK_TIMEOUT_MS) {
                logVerbose("no swing pick within " + SWING_PICK_TIMEOUT_MS + "ms - likely misclick");
                return true;  // exit early
            }

            return false;
        }, RandomUtils.uniformRandom(6000, 8000));

        boolean mined = minedByChat[0] || clueScrollSeen[0] ||
                (!noOreSeen[0] && (respawnSeen[0] || inventoryFull[0]));
        logVerbose("waitForMiningCompletion: mined=" + mined + ", noOre=" + noOreSeen[0]
                + ", respawnSeen=" + respawnSeen[0] + ", invFull=" + inventoryFull[0]
                + ", swingPickSeen=" + swingPickSeen[0] + ", clueScrollSeen=" + clueScrollSeen[0]
                + ", ms=" + (System.currentTimeMillis() - startMs));
        return new MiningResult(mined, noOreSeen[0], respawnSeen[0], swingPickSeen[0]);
    }

    private boolean tapGemRock(RSObject rock) {
        if (rock == null) {
            logVerbose("tapGemRock: rock is null");
            return false;
        }
        Polygon hull = script.getSceneProjector().getConvexHull(rock);
        if (hull == null || hull.numVertices() == 0) {
            logVerbose("tapGemRock: hull missing");
            return false;
        }
        // shrink hull to 0.7 for more reliable clicks
        Polygon shrunk = hull.getResized(0.7);
        Polygon targetHull = shrunk != null ? shrunk : hull;
        logVerbose("tapGemRock: tapping hull");

        // use direct tap instead of tapGetResponse to actually click the rock
        return script.getFinger().tapGameScreen(targetHull);
    }

    /**
     * checks if a position is near any marked empty position (within 1 tile)
     */
    private boolean isPositionMarkedEmpty(WorldPosition pos) {
        if (pos == null || emptyRockPositions.isEmpty()) {
            return false;
        }
        for (WorldPosition emptyPos : emptyRockPositions) {
            if (emptyPos != null && pos.distanceTo(emptyPos) <= 1.5) {
                return true;
            }
        }
        return false;
    }

    /**
     * finds gem rocks by color detection (fallback when ObjectManager fails)
     */
    private List<Point> findGemRocksByColor() {
        try {
            // use different color based on mine location
            boolean isUpperMine = selectedLocation.name().equals("upper");
            int rockColor = isUpperMine ? GEM_ROCK_COLOR_UPPER : GEM_ROCK_COLOR_UNDERGROUND;

            SingleThresholdComparator tolerance = new SingleThresholdComparator(COLOR_TOLERANCE);
            SearchablePixel gemColor = new SearchablePixel(rockColor, tolerance, ColorModel.RGB);

            PixelCluster.ClusterQuery query = new PixelCluster.ClusterQuery(
                    CLUSTER_MAX_DISTANCE,
                    CLUSTER_MIN_SIZE,
                    new SearchablePixel[]{gemColor}
            );

            // search within mining area bounds on screen
            PixelCluster.ClusterSearchResult result = script.getPixelAnalyzer().findClusters(null, query);

            if (result == null) {
                return Collections.emptyList();
            }

            List<PixelCluster> clusters = result.getClusters();
            if (clusters == null || clusters.isEmpty()) {
                return Collections.emptyList();
            }

            // get center points of valid clusters
            List<Point> centers = new ArrayList<>();
            for (PixelCluster cluster : clusters) {
                if (cluster.getPoints().size() >= CLUSTER_MIN_SIZE) {
                    centers.add(cluster.getCenter());
                }
            }

            return centers;
        } catch (RuntimeException e) {
            script.log(getClass(), "error in color detection: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * finds the nearest screen point to player position
     */
    private Point findNearestPoint(List<Point> points, WorldPosition playerPos) {
        if (points == null || points.isEmpty()) {
            return null;
        }

        // get player screen position
        Polygon playerPoly = script.getSceneProjector().getTileCube(playerPos, 0);
        if (playerPoly == null) {
            return points.get(0); // fallback to first point
        }

        Rectangle playerBounds = playerPoly.getBounds();
        int playerScreenX = playerBounds.x + playerBounds.width / 2;
        int playerScreenY = playerBounds.y + playerBounds.height / 2;

        Point nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Point p : points) {
            double dist = Math.sqrt(Math.pow(p.x - playerScreenX, 2) + Math.pow(p.y - playerScreenY, 2));
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }

        return nearest;
    }

    /**
     * marks a rock as recently mined (adds to cooldown)
     */
    private void markRockAsMined(WorldPosition pos) {
        if (pos != null) {
            recentlyMinedRocks.put(pos, System.currentTimeMillis());
            logVerbose("marked rock on cooldown: " + pos);
        }
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

    // store last scan results for debug paint
    private static final Map<WorldPosition, String> lastRockStates = new ConcurrentHashMap<>();
    private static volatile WorldPosition lastSelectedRock = null;

    /**
     * DEBUG: draws markers on all known rock positions
     * GREEN = valid candidate, CYAN = selected, RED = cooldown, ORANGE = no gem color
     */
    public static void drawDebugRockMarkers(Script script, com.osmb.api.visual.drawing.Canvas c) {
        if (script == null || c == null) return;

        WorldPosition myPos = script.getWorldPosition();
        if (myPos == null) return;

        // only draw for underground mine (plane 0)
        if (myPos.getPlane() != 0) return;

        int colorValid = new Color(0, 255, 0).getRGB();      // green = valid
        int colorSelected = new Color(0, 255, 255).getRGB(); // cyan = selected
        int colorCooldown = new Color(255, 0, 0).getRGB();   // red = cooldown
        int colorNoGem = new Color(255, 165, 0).getRGB();    // orange = no gem color
        int textColor = Color.WHITE.getRGB();

        for (WorldPosition rockPos : UNDERGROUND_ROCK_POSITIONS) {
            if (rockPos == null) continue;
            if (rockPos.distanceTo(myPos) > 15) continue;

            Polygon tileCube = script.getSceneProjector().getTileCube(rockPos, 50, true);
            if (tileCube == null || tileCube.numVertices() == 0) continue;

            // determine state and color
            String state = lastRockStates.getOrDefault(rockPos, "unknown");
            int markerColor;
            if (rockPos.equals(lastSelectedRock)) {
                markerColor = colorSelected;
                state = "SEL";
            } else if (state.equals("cooldown")) {
                markerColor = colorCooldown;
                state = "CD";
            } else if (state.equals("no_gem")) {
                markerColor = colorNoGem;
                state = "NO";
            } else if (state.equals("valid")) {
                markerColor = colorValid;
                state = "OK";
            } else {
                markerColor = new Color(128, 128, 128).getRGB(); // gray = unknown
                state = "?";
            }

            c.fillPolygon(tileCube, markerColor, 0.4);

            // draw distance and state
            Rectangle bounds = tileCube.getBounds();
            int centerX = bounds.x + bounds.width / 2;
            int centerY = bounds.y + bounds.height / 2;
            double dist = rockPos.distanceTo(myPos);
            String label = String.format("%.1f %s", dist, state);
            c.drawText(label, centerX - 20, centerY, textColor, new java.awt.Font("Arial", java.awt.Font.BOLD, 9));
        }
    }

    /** call from findTileCubeTarget to update debug state */
    private void updateDebugRockState(WorldPosition pos, String state) {
        if (pos != null) {
            lastRockStates.put(pos, state);
        }
    }

    /** call when a rock is selected */
    private void setDebugSelectedRock(WorldPosition pos) {
        lastSelectedRock = pos;
    }

}
