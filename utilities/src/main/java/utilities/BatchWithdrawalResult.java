package utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a batch withdrawal operation.
 *
 * Tracks which withdrawal requests succeeded and which failed,
 * allowing callers to handle partial failures gracefully.
 *
 * Usage:
 *   BatchWithdrawalResult result = BankSearchUtils.withdrawBatch(script, requests);
 *   if (result.isAllSuccessful()) {
 *       log("all items withdrawn");
 *   } else {
 *       log("failed to withdraw: " + result.getFailCount() + " items");
 *       for (WithdrawalRequest failed : result.getFailed()) {
 *           log("  - item id: " + failed.getItemId());
 *       }
 *   }
 */
public class BatchWithdrawalResult {

    private final List<WithdrawalRequest> successful;
    private final List<WithdrawalRequest> failed;

    /**
     * Creates a new empty batch withdrawal result.
     */
    public BatchWithdrawalResult() {
        this.successful = new ArrayList<>();
        this.failed = new ArrayList<>();
    }

    /**
     * Adds a request to the successful list.
     *
     * @param request the successful withdrawal request
     */
    public void addSuccessful(WithdrawalRequest request) {
        successful.add(request);
    }

    /**
     * Adds a request to the failed list.
     *
     * @param request the failed withdrawal request
     */
    public void addFailed(WithdrawalRequest request) {
        failed.add(request);
    }

    /**
     * Gets the list of successful withdrawal requests.
     *
     * @return unmodifiable list of successful requests
     */
    public List<WithdrawalRequest> getSuccessful() {
        return Collections.unmodifiableList(successful);
    }

    /**
     * Gets the list of failed withdrawal requests.
     *
     * @return unmodifiable list of failed requests
     */
    public List<WithdrawalRequest> getFailed() {
        return Collections.unmodifiableList(failed);
    }

    /**
     * Checks if all requests succeeded.
     *
     * @return true if no failures occurred
     */
    public boolean isAllSuccessful() {
        return failed.isEmpty();
    }

    /**
     * Gets the total number of requests processed.
     *
     * @return total count (successful + failed)
     */
    public int getTotalRequested() {
        return successful.size() + failed.size();
    }

    /**
     * Gets the number of successful requests.
     *
     * @return count of successful withdrawals
     */
    public int getSuccessCount() {
        return successful.size();
    }

    /**
     * Gets the number of failed requests.
     *
     * @return count of failed withdrawals
     */
    public int getFailCount() {
        return failed.size();
    }
}
