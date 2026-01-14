package utilities;

/**
 * Represents a request to withdraw an item from the bank.
 *
 * Used with BankSearchUtils.withdrawBatch() for batch withdrawal operations.
 *
 * Usage:
 *   // create individual requests
 *   WithdrawalRequest request = new WithdrawalRequest(ItemID.SHARK, 10);
 *
 *   // or use the fluent factory method
 *   WithdrawalRequest request = WithdrawalRequest.of(ItemID.SHARK, 10);
 */
public class WithdrawalRequest {

    private final int itemId;
    private final int amount;

    /**
     * Creates a new withdrawal request.
     *
     * @param itemId the item ID to withdraw
     * @param amount the amount to withdraw
     */
    public WithdrawalRequest(int itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    /**
     * Factory method for fluent API.
     *
     * @param itemId the item ID to withdraw
     * @param amount the amount to withdraw
     * @return a new WithdrawalRequest
     */
    public static WithdrawalRequest of(int itemId, int amount) {
        return new WithdrawalRequest(itemId, amount);
    }

    /**
     * Gets the item ID to withdraw.
     *
     * @return the item ID
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the amount to withdraw.
     *
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }
}
