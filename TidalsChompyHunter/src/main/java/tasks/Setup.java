package tasks;

import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.ItemSearchResult;
import com.osmb.api.script.Script;
import com.osmb.api.ui.chatbox.dialogue.DialogueType;
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.utils.UIResult;
import com.osmb.api.utils.UIResultList;
import main.TidalsChompyHunter;
import utils.Task;

import static main.TidalsChompyHunter.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Setup extends Task {

    // item ids - ogre bows
    private static final int OGRE_BOW = 2883;
    private static final int COMP_OGRE_BOW = 4827;
    private static final int[] OGRE_BOWS = {OGRE_BOW, COMP_OGRE_BOW};

    // item ids - arrows (ammo slot)
    private static final int OGRE_ARROW = 2866;
    private static final int BRONZE_BRUTAL = 4773;
    private static final int IRON_BRUTAL = 4778;
    private static final int STEEL_BRUTAL = 4783;
    private static final int BLACK_BRUTAL = 4788;
    private static final int MITHRIL_BRUTAL = 4793;
    private static final int ADAMANT_BRUTAL = 4798;
    private static final int RUNE_BRUTAL = 4803;
    private static final int[] VALID_ARROWS = {
        OGRE_ARROW, BRONZE_BRUTAL, IRON_BRUTAL, STEEL_BRUTAL,
        BLACK_BRUTAL, MITHRIL_BRUTAL, ADAMANT_BRUTAL, RUNE_BRUTAL
    };

    // item ids - ogre bellows (all charge states)
    private static final int OGRE_BELLOWS_EMPTY = 2871;
    private static final int OGRE_BELLOWS_3 = 2872;
    private static final int OGRE_BELLOWS_2 = 2873;
    private static final int OGRE_BELLOWS_1 = 2874;
    private static final int[] OGRE_BELLOWS = {OGRE_BELLOWS_EMPTY, OGRE_BELLOWS_3, OGRE_BELLOWS_2, OGRE_BELLOWS_1};

    // chompy hunting region
    private static final int CHOMPY_REGION = 9519;

    // minimum requirements
    private static final int MIN_BELLOWS = 2;
    private static final int MIN_FREE_SLOTS = 3;

    // zoom level
    private static final int TARGET_ZOOM_LEVEL = 3;

    public Setup(Script script) {
        super(script);
    }

    @Override
    public boolean activate() {
        return !TidalsChompyHunter.setupComplete;
    }

    @Override
    public boolean execute() {
        script.log(getClass(), "verifying setup requirements...");

        // set login timestamp on first setup run - gives OSMB time to identify our position
        // prevents false positive crash detection from our own white dot
        if (DetectPlayers.lastLoginTimestamp == 0) {
            DetectPlayers.lastLoginTimestamp = System.currentTimeMillis();
            script.log(getClass(), "login grace period started (10s)");
        }

        List<String> errors = new ArrayList<>();

        // open inventory tab first
        script.getWidgetManager().getTabManager().openTab(Tab.Type.INVENTORY);
        boolean invAccessible = script.pollFramesUntil(() ->
            script.getWidgetManager().getInventory().search(Set.of()) != null,
            3000);

        if (!invAccessible) {
            script.log(getClass(), "ERROR: inventory not accessible");
            script.stop();
            return false;
        }

        // SETUP-04: check bellows count (need 2+)
        ItemGroupResult bellowsSearch = script.getWidgetManager().getInventory().search(
            toIntegerSet(OGRE_BELLOWS)
        );

        int bellowsCount = 0;
        if (bellowsSearch != null) {
            bellowsCount = bellowsSearch.getAmount(OGRE_BELLOWS_EMPTY)
                         + bellowsSearch.getAmount(OGRE_BELLOWS_3)
                         + bellowsSearch.getAmount(OGRE_BELLOWS_2)
                         + bellowsSearch.getAmount(OGRE_BELLOWS_1);
        }

        if (bellowsCount < MIN_BELLOWS) {
            errors.add("2+ ogre bellows (have " + bellowsCount + ")");
        }

        // SETUP-05: check free inventory slots (need 3+)
        ItemGroupResult allInv = script.getWidgetManager().getInventory().search(Set.of());
        int freeSlots = 0;
        if (allInv != null) {
            freeSlots = allInv.getFreeSlots();
        }

        if (freeSlots < MIN_FREE_SLOTS) {
            errors.add("3+ free inventory slots (have " + freeSlots + ")");
        }

        // switch to equipment tab
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> false, script.random(300, 500));

        // SETUP-02: check ogre bow equipped
        UIResult<ItemSearchResult> bowCheck = script.getWidgetManager().getEquipment().findItem(OGRE_BOWS);
        if (!bowCheck.isFound()) {
            errors.add("ogre bow or comp ogre bow equipped");
        }

        // SETUP-03: check arrows equipped
        UIResult<ItemSearchResult> arrowCheck = script.getWidgetManager().getEquipment().findItem(VALID_ARROWS);
        if (!arrowCheck.isFound()) {
            errors.add("brutal arrows or ogre arrows equipped");
        } else {
            // capture initial arrow count and item ID for BuffOverlay tracking
            ItemSearchResult arrowResult = arrowCheck.get();
            if (arrowResult != null) {
                TidalsChompyHunter.initialArrowCount = arrowResult.getStackAmount();
                TidalsChompyHunter.equippedArrowId = arrowResult.getId();
                script.log(getClass(), "initial arrow count: " + TidalsChompyHunter.initialArrowCount +
                           ", item ID: " + TidalsChompyHunter.equippedArrowId);
            }
        }

        // SETUP-01: check region
        int regionID = script.getWorldPosition().getRegionID();
        if (regionID != CHOMPY_REGION) {
            errors.add("Castle Wars chompy area (region 9519, current: " + regionID + ")");
        }

        // report all errors at once
        if (!errors.isEmpty()) {
            script.log(getClass(), "Missing: " + String.join(", ", errors));
            script.stop();
            return false;
        }

        // check and set zoom level
        checkZoomLevel();

        // check bow for total kills (nice-to-have, don't stop if fails)
        try {
            checkBowForTotalKills();
        } catch (Exception e) {
            script.log(getClass(), "warning: failed to get total kills from bow - " + e.getMessage());
        }

        // enable arrow detection for chompy spawn detection
        script.getWidgetManager().getMinimap().arrowDetectionEnabled(true);
        script.log(getClass(), "arrow detection enabled");

        // reset ground toad counter for clean state
        TidalsChompyHunter.groundToadCount = 0;
        script.log(getClass(), "ground toad counter reset");

        // clear any tracked toad/corpse positions from previous session
        TidalsChompyHunter.droppedToadPositions.clear();
        TidalsChompyHunter.corpsePositions.clear();
        AttackChompy.resetAllState();  // clear tracked chompies, ignored positions, combat state
        script.log(getClass(), "cleared tracked positions and attack state");

        // wait for login grace period before checking for players
        // this gives OSMB time to identify our position (prevents false positive from own dot)
        long timeSinceLogin = System.currentTimeMillis() - DetectPlayers.lastLoginTimestamp;
        long loginGraceMs = 10000;
        if (DetectPlayers.lastLoginTimestamp > 0 && timeSinceLogin < loginGraceMs) {
            long waitTime = loginGraceMs - timeSinceLogin;
            script.log(getClass(), "waiting " + (waitTime / 1000) + "s for position stabilization...");
            task = "stabilizing position...";
            script.submitTask(() -> false, (int) waitTime);
            script.log(getClass(), "stabilization complete - checking for players");
        }

        // check for ANY players on minimap BEFORE starting (immediate hop if occupied)
        // use large radius (50 tiles) to scan entire visible minimap, not just crash radius
        task = "checking for players...";
        if (DetectPlayers.hasPlayersOnMinimap(script, 50)) {
            script.log(getClass(), "=== WORLD OCCUPIED ===");
            script.log(getClass(), "player detected on minimap - triggering hop");
            DetectPlayers.crashDetected = true;
            TidalsChompyHunter.task = "world occupied - hopping";
            // don't mark setupComplete - HopWorld will handle it and Setup will re-run
            return true;
        }
        script.log(getClass(), "no players on minimap - world is clear");

        // check for existing chompies (live or dead) - if we haven't dropped toads yet,
        // any chompies must be from another player who was just here
        task = "checking for chompies...";
        if (TidalsChompyHunter.droppedToadPositions.isEmpty() && AttackChompy.hasVisibleChompySprite(script)) {
            script.log(getClass(), "=== WORLD OCCUPIED ===");
            script.log(getClass(), "chompy sprite detected but we haven't dropped toads - another player was hunting here");
            DetectPlayers.crashDetected = true;
            TidalsChompyHunter.task = "chompies present - hopping";
            // don't mark setupComplete - HopWorld will handle it and Setup will re-run
            return true;
        }
        script.log(getClass(), "no stray chompies - world is clean");

        // all validations passed
        script.log(getClass(), "Setup complete - all requirements verified");
        TidalsChompyHunter.setupComplete = true;

        return false;
    }

    /**
     * check ogre bow for total kills via "Check" action
     * parses dialogue for "total of (X) chompy" pattern
     */
    private void checkBowForTotalKills() {
        script.log(getClass(), "checking bow for total kills...");

        // open equipment tab if not already
        script.getWidgetManager().getTabManager().openTab(Tab.Type.EQUIPMENT);
        script.pollFramesHuman(() -> false, script.random(300, 500));

        // try each bow id until one succeeds (we already validated one is equipped)
        boolean success = false;
        for (int bowId : OGRE_BOWS) {
            success = script.getWidgetManager().getEquipment().interact(bowId, "Check");
            if (success) break;
        }
        if (!success) {
            script.log(getClass(), "failed to check bow");
            return;
        }

        // wait for TAP_HERE_TO_CONTINUE dialogue
        boolean dialogueAppeared = script.pollFramesUntil(() ->
            script.getWidgetManager().getDialogue().getDialogueType() == DialogueType.TAP_HERE_TO_CONTINUE,
            3000);

        if (!dialogueAppeared) {
            script.log(getClass(), "bow check dialogue did not appear");
            return;
        }

        // get dialogue text from chatbox
        UIResultList<String> chatResult = script.getWidgetManager().getChatbox().getText();
        if (chatResult == null || chatResult.isNotFound()) {
            script.log(getClass(), "could not read chatbox for total kills");
            dismissDialogue();
            return;
        }

        // parse for total kills pattern
        // format: "You've killed a total of (x) chompy birds so far!"
        Pattern pattern = Pattern.compile("total of (\\d+) chompy");
        for (String line : chatResult.asList()) {
            Matcher matcher = pattern.matcher(line.toLowerCase());
            if (matcher.find()) {
                int totalKills = Integer.parseInt(matcher.group(1));
                TidalsChompyHunter.initialTotalKills = totalKills;
                script.log(getClass(), "total chompy kills: " + totalKills);

                // set milestone tracking so we don't re-notify existing progress
                int[] milestones = {30, 125, 300, 1000};
                for (int m : milestones) {
                    if (totalKills >= m) {
                        TidalsChompyHunter.lastMilestoneReached = m;
                    }
                }
                break;
            }
        }

        dismissDialogue();
    }

    /**
     * dismiss TAP_HERE_TO_CONTINUE dialogue
     */
    private void dismissDialogue() {
        script.getWidgetManager().getDialogue().continueChatDialogue();
        script.pollFramesUntil(() ->
            script.getWidgetManager().getDialogue().getDialogueType() != DialogueType.TAP_HERE_TO_CONTINUE,
            2000);
    }

    // helper to convert int array to Set<Integer>
    private Set<Integer> toIntegerSet(int[] arr) {
        Set<Integer> set = new java.util.HashSet<>();
        for (int id : arr) {
            set.add(id);
        }
        return set;
    }

    /**
     * check and set zoom level to optimal for chompy detection
     */
    private void checkZoomLevel() {
        try {
            task = "checking zoom";
            boolean opened = script.getWidgetManager().getSettings().open();
            if (!opened) {
                script.log(getClass(), "could not open settings tab to check zoom");
                return;
            }

            script.pollFramesHuman(() -> false, script.random(200, 400));
            UIResult<Integer> zoomResult = script.getWidgetManager().getSettings().getZoomLevel();
            if (zoomResult != null && zoomResult.isFound()) {
                int currentZoom = zoomResult.get();
                script.log(getClass(), "current zoom level: " + currentZoom);

                if (currentZoom != TARGET_ZOOM_LEVEL) {
                    script.log(getClass(), "setting zoom level to " + TARGET_ZOOM_LEVEL + "...");
                    boolean set = script.getWidgetManager().getSettings().setZoomLevel(TARGET_ZOOM_LEVEL);
                    if (set) {
                        script.log(getClass(), "zoom level set to " + TARGET_ZOOM_LEVEL);
                    } else {
                        script.log(getClass(), "failed to set zoom level");
                    }
                } else {
                    script.log(getClass(), "zoom level already optimal");
                }
            } else {
                script.log(getClass(), "could not read zoom level, attempting to set anyway...");
                script.getWidgetManager().getSettings().setZoomLevel(TARGET_ZOOM_LEVEL);
            }

            script.getWidgetManager().getSettings().close();
            script.pollFramesHuman(() -> false, script.random(200, 400));

        } catch (Exception e) {
            script.log(getClass(), "error checking zoom level: " + e.getMessage());
        }
    }
}
