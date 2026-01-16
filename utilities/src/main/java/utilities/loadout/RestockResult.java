package utilities.loadout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a restock operation, tracking what was missing, restocked, and unfulfilled.
 *
 * <p>Use static factory methods to create instances:
 * <ul>
 *   <li>{@link #success(List)} - all items were restocked</li>
 *   <li>{@link #partial(List, List)} - some items restocked, some unfulfilled</li>
 *   <li>{@link #failed(List, String)} - restock failed entirely</li>
 * </ul>
 */
public final class RestockResult {

    private final List<MissingItem> missing;
    private final List<MissingItem> restocked;
    private final List<MissingItem> unfulfilled;
    private final boolean success;
    private final String failureReason;

    private RestockResult(List<MissingItem> missing, List<MissingItem> restocked,
                          List<MissingItem> unfulfilled, boolean success, String failureReason) {
        // defensive copies
        this.missing = missing != null ? new ArrayList<>(missing) : Collections.emptyList();
        this.restocked = restocked != null ? new ArrayList<>(restocked) : Collections.emptyList();
        this.unfulfilled = unfulfilled != null ? new ArrayList<>(unfulfilled) : Collections.emptyList();
        this.success = success;
        this.failureReason = failureReason;
    }

    /**
     * Creates a successful result where all items were restocked.
     *
     * @param restocked list of items that were successfully restocked
     * @return a successful RestockResult
     */
    public static RestockResult success(List<MissingItem> restocked) {
        return new RestockResult(restocked, restocked, Collections.emptyList(), true, null);
    }

    /**
     * Creates a partial result where some items were restocked but others failed.
     *
     * @param restocked   items that were successfully restocked
     * @param unfulfilled items that could not be restocked
     * @return a partial RestockResult (success=false)
     */
    public static RestockResult partial(List<MissingItem> restocked, List<MissingItem> unfulfilled) {
        List<MissingItem> allMissing = new ArrayList<>();
        if (restocked != null) allMissing.addAll(restocked);
        if (unfulfilled != null) allMissing.addAll(unfulfilled);
        return new RestockResult(allMissing, restocked, unfulfilled, false, null);
    }

    /**
     * Creates a failed result where the restock could not be completed.
     *
     * @param missing list of items that were identified as missing
     * @param reason  human-readable explanation of failure
     * @return a failed RestockResult
     */
    public static RestockResult failed(List<MissingItem> missing, String reason) {
        return new RestockResult(missing, Collections.emptyList(), missing, false, reason);
    }

    /**
     * Creates an empty successful result (nothing was missing).
     *
     * @return a successful RestockResult with no items
     */
    public static RestockResult nothingMissing() {
        return new RestockResult(Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), true, null);
    }

    /**
     * Gets all items that were originally identified as missing.
     *
     * @return unmodifiable list of missing items
     */
    public List<MissingItem> getMissing() {
        return Collections.unmodifiableList(missing);
    }

    /**
     * Gets items that were successfully restocked.
     *
     * @return unmodifiable list of restocked items
     */
    public List<MissingItem> getRestocked() {
        return Collections.unmodifiableList(restocked);
    }

    /**
     * Gets items that could not be restocked.
     *
     * @return unmodifiable list of unfulfilled items
     */
    public List<MissingItem> getUnfulfilled() {
        return Collections.unmodifiableList(unfulfilled);
    }

    /**
     * Returns whether the restock was fully successful.
     *
     * @return true if all items were restocked or nothing was missing
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the failure reason (if any).
     *
     * @return failure reason, or null if success or no specific reason
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Gets the total count of items that were missing.
     *
     * @return count of missing items
     */
    public int getTotalMissing() {
        return missing.size();
    }

    /**
     * Gets the total count of items that were restocked.
     *
     * @return count of restocked items
     */
    public int getTotalRestocked() {
        return restocked.size();
    }

    /**
     * Gets the total count of items that could not be fulfilled.
     *
     * @return count of unfulfilled items
     */
    public int getTotalUnfulfilled() {
        return unfulfilled.size();
    }

    /**
     * Returns a summary string for logging.
     */
    @Override
    public String toString() {
        if (success && missing.isEmpty()) {
            return "RestockResult{success, nothing missing}";
        }

        StringBuilder sb = new StringBuilder("RestockResult{");
        sb.append(success ? "success" : "failed");
        sb.append(", missing=").append(missing.size());
        sb.append(", restocked=").append(restocked.size());
        sb.append(", unfulfilled=").append(unfulfilled.size());
        if (failureReason != null) {
            sb.append(", reason=\"").append(failureReason).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
}
