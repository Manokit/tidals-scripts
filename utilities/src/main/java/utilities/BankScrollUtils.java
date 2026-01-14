package utilities;

import com.osmb.api.script.Script;
import com.osmb.api.shape.Rectangle;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.color.ColorModel;
import com.osmb.api.visual.color.tolerance.impl.SingleThresholdComparator;

import java.awt.*;
import java.util.List;

/**
 * Bank scroll utilities for scrolling the bank interface via pixel color detection.
 *
 * Detects the bank scroll buttons by finding clusters of their characteristic
 * brown/golden colors in the right portion of the screen where the scrollbar lives.
 *
 * Usage:
 *   // scroll down to find more items
 *   if (BankScrollUtils.scrollDown(script)) {
 *       // bank scrolled successfully
 *   }
 *
 * Note: Bank must be visible before calling these methods.
 */
public class BankScrollUtils {

    private static final int COLOR_TOLERANCE = 20;

    // scroll button colors - brown/golden tones from OSRS UI
    // these are the prominent colors in the scroll arrow buttons
    private static final int SCROLL_BUTTON_COLOR_1 = 0x655230; // dark brown
    private static final int SCROLL_BUTTON_COLOR_2 = 0x7a6239; // medium brown
    private static final int SCROLL_BUTTON_COLOR_3 = 0x544426; // darker brown

    // approximate screen regions for scroll buttons (right side of bank interface)
    // these will be dynamically adjusted based on screen size
    private static final double SCROLL_REGION_X_START = 0.85; // start at 85% of screen width
    private static final double SCROLL_REGION_X_END = 0.99;   // end at 99% of screen width
    private static final double SCROLL_UP_Y_START = 0.15;     // scroll up in upper portion
    private static final double SCROLL_UP_Y_END = 0.35;
    private static final double SCROLL_DOWN_Y_START = 0.65;   // scroll down in lower portion
    private static final double SCROLL_DOWN_Y_END = 0.85;

    /**
     * Scrolls the bank down by detecting and tapping the scroll down button.
     *
     * @param script the script instance
     * @return true if scroll button was found and tapped
     */
    public static boolean scrollDown(Script script) {
        // pre-condition: bank must be visible
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankScrollUtils.class, "bank not visible - cannot scroll down");
            return false;
        }

        // get scroll down search region
        Rectangle searchRegion = getScrollDownRegion(script);
        if (searchRegion == null) {
            script.log(BankScrollUtils.class, "could not determine scroll region");
            return false;
        }

        // find scroll button pixels
        Point buttonCenter = findScrollButton(script, searchRegion);
        if (buttonCenter == null) {
            script.log(BankScrollUtils.class, "scroll down button not found");
            return false;
        }

        // tap the button
        script.log(BankScrollUtils.class, "tapping scroll down at: " + buttonCenter.x + "," + buttonCenter.y);
        boolean tapped = script.getFinger().tap(buttonCenter);

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
        // pre-condition: bank must be visible
        if (!script.getWidgetManager().getBank().isVisible()) {
            script.log(BankScrollUtils.class, "bank not visible - cannot scroll up");
            return false;
        }

        // get scroll up search region
        Rectangle searchRegion = getScrollUpRegion(script);
        if (searchRegion == null) {
            script.log(BankScrollUtils.class, "could not determine scroll region");
            return false;
        }

        // find scroll button pixels
        Point buttonCenter = findScrollButton(script, searchRegion);
        if (buttonCenter == null) {
            script.log(BankScrollUtils.class, "scroll up button not found");
            return false;
        }

        // tap the button
        script.log(BankScrollUtils.class, "tapping scroll up at: " + buttonCenter.x + "," + buttonCenter.y);
        boolean tapped = script.getFinger().tap(buttonCenter);

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
     * Checks if the scroll down button is visible.
     *
     * @param script the script instance
     * @return true if scroll down button is visible
     */
    public static boolean canScrollDown(Script script) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return false;
        }

        Rectangle searchRegion = getScrollDownRegion(script);
        if (searchRegion == null) {
            return false;
        }

        return findScrollButton(script, searchRegion) != null;
    }

    /**
     * Checks if the scroll up button is visible.
     *
     * @param script the script instance
     * @return true if scroll up button is visible
     */
    public static boolean canScrollUp(Script script) {
        if (!script.getWidgetManager().getBank().isVisible()) {
            return false;
        }

        Rectangle searchRegion = getScrollUpRegion(script);
        if (searchRegion == null) {
            return false;
        }

        return findScrollButton(script, searchRegion) != null;
    }

    /**
     * Gets the search region for the scroll down button.
     */
    private static Rectangle getScrollDownRegion(Script script) {
        Rectangle screenBounds = script.getScreen().getBounds();
        if (screenBounds == null) {
            return null;
        }

        int x = (int) (screenBounds.width * SCROLL_REGION_X_START);
        int y = (int) (screenBounds.height * SCROLL_DOWN_Y_START);
        int width = (int) (screenBounds.width * (SCROLL_REGION_X_END - SCROLL_REGION_X_START));
        int height = (int) (screenBounds.height * (SCROLL_DOWN_Y_END - SCROLL_DOWN_Y_START));

        return new Rectangle(x, y, width, height);
    }

    /**
     * Gets the search region for the scroll up button.
     */
    private static Rectangle getScrollUpRegion(Script script) {
        Rectangle screenBounds = script.getScreen().getBounds();
        if (screenBounds == null) {
            return null;
        }

        int x = (int) (screenBounds.width * SCROLL_REGION_X_START);
        int y = (int) (screenBounds.height * SCROLL_UP_Y_START);
        int width = (int) (screenBounds.width * (SCROLL_REGION_X_END - SCROLL_REGION_X_START));
        int height = (int) (screenBounds.height * (SCROLL_UP_Y_END - SCROLL_UP_Y_START));

        return new Rectangle(x, y, width, height);
    }

    /**
     * Finds the scroll button in the given region by detecting characteristic colors.
     *
     * @param script the script instance
     * @param searchRegion the region to search within
     * @return center point of button if found, null otherwise
     */
    private static Point findScrollButton(Script script, Rectangle searchRegion) {
        SingleThresholdComparator tolerance = new SingleThresholdComparator(COLOR_TOLERANCE);

        SearchablePixel[] buttonColors = {
            new SearchablePixel(SCROLL_BUTTON_COLOR_1, tolerance, ColorModel.RGB),
            new SearchablePixel(SCROLL_BUTTON_COLOR_2, tolerance, ColorModel.RGB),
            new SearchablePixel(SCROLL_BUTTON_COLOR_3, tolerance, ColorModel.RGB)
        };

        // find pixels matching scroll button colors
        List<Point> pixels = script.getPixelAnalyzer().findPixels(searchRegion, buttonColors);

        if (pixels == null || pixels.isEmpty()) {
            return null;
        }

        // calculate centroid of found pixels as button center
        int sumX = 0;
        int sumY = 0;
        for (Point p : pixels) {
            sumX += p.x;
            sumY += p.y;
        }

        return new Point(sumX / pixels.size(), sumY / pixels.size());
    }
}
