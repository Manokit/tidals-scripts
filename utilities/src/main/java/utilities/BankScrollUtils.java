package utilities;

import com.osmb.api.definition.SpriteDefinition;
import com.osmb.api.script.Script;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.visual.image.ImageSearchResult;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;
import com.osmb.api.visual.image.Image;
import com.osmb.api.visual.image.SearchableImage;

import java.awt.*;
import java.util.List;

/**
 * Bank scroll utilities for scrolling the bank interface via sprite-based detection.
 *
 * Uses game sprites to find scroll arrow buttons on screen and tap them.
 * Also tracks scrollbar position to determine if more scrolling is possible.
 *
 * Usage:
 *   // scroll down to find more items
 *   if (BankScrollUtils.scrollDown(script)) {
 *       // bank scrolled successfully
 *   }
 *
 *   // check if we've scrolled to the bottom
 *   if (!BankScrollUtils.canScrollDown(script)) {
 *       // at bottom of bank
 *   }
 *
 * Note: Bank must be visible before calling these methods.
 */
public class BankScrollUtils {

    private static final int COLOR_TOLERANCE = 15;

    // sprite IDs for bank scroll elements
    private static final int SCROLL_UP_SPRITE_ID = 773;
    private static final int SCROLL_DOWN_SPRITE_ID = 788;
    private static final int SCROLLBAR_TOP_SPRITE_ID = 789;
    private static final int SCROLLBAR_BOTTOM_SPRITE_ID = 791;

    private static SearchableImage scrollUpImage;
    private static SearchableImage scrollDownImage;
    private static SearchableImage scrollbarTopImage;
    private static SearchableImage scrollbarBottomImage;
    private static boolean initialized = false;

    /**
     * Initializes scroll button sprites. Called lazily on first use.
     *
     * @param script the script instance
     * @return true if sprites loaded successfully
     */
    public static boolean init(Script script) {
        if (initialized) {
            return true;
        }

        try {
            SingleThresholdComparator tolerance = new SingleThresholdComparator(COLOR_TOLERANCE);

            // load scroll up sprite
            SpriteDefinition upSprite = script.getSpriteManager().getSprite(SCROLL_UP_SPRITE_ID, 0);
            if (upSprite == null) {
                script.log(BankScrollUtils.class, "scroll up sprite not found: " + SCROLL_UP_SPRITE_ID);
                return false;
            }
            Image upImage = new Image(upSprite);
            scrollUpImage = upImage.toSearchableImage(tolerance, ColorModel.RGB);
            script.log(BankScrollUtils.class, "loaded scroll up sprite: " + upSprite.width + "x" + upSprite.height);

            // load scroll down sprite
            SpriteDefinition downSprite = script.getSpriteManager().getSprite(SCROLL_DOWN_SPRITE_ID, 0);
            if (downSprite == null) {
                script.log(BankScrollUtils.class, "scroll down sprite not found: " + SCROLL_DOWN_SPRITE_ID);
                return false;
            }
            Image downImage = new Image(downSprite);
            scrollDownImage = downImage.toSearchableImage(tolerance, ColorModel.RGB);
            script.log(BankScrollUtils.class, "loaded scroll down sprite: " + downSprite.width + "x" + downSprite.height);

            // load scrollbar top sprite (for position tracking)
            SpriteDefinition topSprite = script.getSpriteManager().getSprite(SCROLLBAR_TOP_SPRITE_ID, 0);
            if (topSprite != null) {
                Image topImage = new Image(topSprite);
                scrollbarTopImage = topImage.toSearchableImage(tolerance, ColorModel.RGB);
                script.log(BankScrollUtils.class, "loaded scrollbar top sprite: " + topSprite.width + "x" + topSprite.height);
            }

            // load scrollbar bottom sprite (for position tracking)
            SpriteDefinition bottomSprite = script.getSpriteManager().getSprite(SCROLLBAR_BOTTOM_SPRITE_ID, 0);
            if (bottomSprite != null) {
                Image bottomImage = new Image(bottomSprite);
                scrollbarBottomImage = bottomImage.toSearchableImage(tolerance, ColorModel.RGB);
                script.log(BankScrollUtils.class, "loaded scrollbar bottom sprite: " + bottomSprite.width + "x" + bottomSprite.height);
            }

            initialized = true;
            return true;

        } catch (Exception e) {
            script.log(BankScrollUtils.class, "failed to load scroll sprites: " + e.getMessage());
            return false;
        }
    }

    /**
     * Scrolls the bank down by detecting and tapping the scroll down button.
     *
     * @param script the script instance
     * @return true if scroll button was found and tapped
     */
    public static boolean scrollDown(Script script) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankScrollUtils.class, "bank not visible - cannot scroll down");
            return false;
        }

        if (!init(script)) {
            return false;
        }

        // find scroll down button on screen
        List<ImageSearchResult> matches = script.getImageAnalyzer().findLocations(scrollDownImage);
        if (matches == null || matches.isEmpty()) {
            script.log(BankScrollUtils.class, "scroll down button not found");
            return false;
        }

        // tap the button
        ImageSearchResult result = matches.get(0);
        Point location = result.getAsPoint();
        script.log(BankScrollUtils.class, "tapping scroll down at: " + location.x + "," + location.y);
        boolean tapped = script.getFinger().tap(location);

        if (tapped) {
            // human-like delay after tapping
            script.pollFramesHuman(() -> false, script.random(200, 400));
            script.log(BankScrollUtils.class, "scroll down tapped successfully");
        } else {
            script.log(BankScrollUtils.class, "failed to tap scroll down button");
        }

        return tapped;
    }

    /**
     * Scrolls the bank up by detecting and tapping the scroll up button.
     *
     * @param script the script instance
     * @return true if scroll button was found and tapped
     */
    public static boolean scrollUp(Script script) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankScrollUtils.class, "bank not visible - cannot scroll up");
            return false;
        }

        if (!init(script)) {
            return false;
        }

        // find scroll up button on screen
        List<ImageSearchResult> matches = script.getImageAnalyzer().findLocations(scrollUpImage);
        if (matches == null || matches.isEmpty()) {
            script.log(BankScrollUtils.class, "scroll up button not found");
            return false;
        }

        // tap the button
        ImageSearchResult result = matches.get(0);
        Point location = result.getAsPoint();
        script.log(BankScrollUtils.class, "tapping scroll up at: " + location.x + "," + location.y);
        boolean tapped = script.getFinger().tap(location);

        if (tapped) {
            // human-like delay after tapping
            script.pollFramesHuman(() -> false, script.random(200, 400));
            script.log(BankScrollUtils.class, "scroll up tapped successfully");
        } else {
            script.log(BankScrollUtils.class, "failed to tap scroll up button");
        }

        return tapped;
    }

    /**
     * Checks if the scroll down button is visible (can scroll further down).
     *
     * @param script the script instance
     * @return true if scroll down button is visible
     */
    public static boolean canScrollDown(Script script) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return false;
        }

        if (!init(script)) {
            return false;
        }

        List<ImageSearchResult> matches = script.getImageAnalyzer().findLocations(scrollDownImage);
        return matches != null && !matches.isEmpty();
    }

    /**
     * Checks if the scroll up button is visible (can scroll further up).
     *
     * @param script the script instance
     * @return true if scroll up button is visible
     */
    public static boolean canScrollUp(Script script) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return false;
        }

        if (!init(script)) {
            return false;
        }

        List<ImageSearchResult> matches = script.getImageAnalyzer().findLocations(scrollUpImage);
        return matches != null && !matches.isEmpty();
    }

    /**
     * Gets the current scrollbar position as a Rectangle.
     * Useful for determining scroll progress.
     *
     * @param script the script instance
     * @return Rectangle of scrollbar position, or null if not found
     */
    public static Rectangle getScrollbarPosition(Script script) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return null;
        }

        if (!init(script)) {
            return null;
        }

        // find scrollbar top and bottom to get full scrollbar bounds
        List<ImageSearchResult> topMatches = scrollbarTopImage != null
            ? script.getImageAnalyzer().findLocations(scrollbarTopImage)
            : null;
        List<ImageSearchResult> bottomMatches = scrollbarBottomImage != null
            ? script.getImageAnalyzer().findLocations(scrollbarBottomImage)
            : null;

        if (topMatches != null && !topMatches.isEmpty() &&
            bottomMatches != null && !bottomMatches.isEmpty()) {
            Point top = topMatches.get(0).getAsPoint();
            Point bottom = bottomMatches.get(0).getAsPoint();

            // combine into full scrollbar bounds (estimate width as 15px)
            int x = Math.min(top.x, bottom.x);
            int y = top.y;
            int width = 15;
            int height = bottom.y - top.y + 15;

            return new Rectangle(x, y, width, height);
        }

        return null;
    }

    /**
     * Scrolls to the top of the bank.
     *
     * @param script the script instance
     * @param maxScrolls maximum number of scroll attempts
     * @return true if reached top or already at top
     */
    public static boolean scrollToTop(Script script, int maxScrolls) {
        for (int i = 0; i < maxScrolls; i++) {
            if (!canScrollUp(script)) {
                script.log(BankScrollUtils.class, "reached top of bank");
                return true;
            }
            if (!scrollUp(script)) {
                return false;
            }
        }
        return !canScrollUp(script);
    }

    /**
     * Scrolls to the bottom of the bank.
     *
     * @param script the script instance
     * @param maxScrolls maximum number of scroll attempts
     * @return true if reached bottom or already at bottom
     */
    public static boolean scrollToBottom(Script script, int maxScrolls) {
        for (int i = 0; i < maxScrolls; i++) {
            if (!canScrollDown(script)) {
                script.log(BankScrollUtils.class, "reached bottom of bank");
                return true;
            }
            if (!scrollDown(script)) {
                return false;
            }
        }
        return !canScrollDown(script);
    }
}
