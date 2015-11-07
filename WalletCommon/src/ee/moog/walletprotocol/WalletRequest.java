package ee.moog.walletprotocol;

/**
 *
 */
public class WalletRequest {
    private String userName;
    private String transactionId;
    private int balanceChange;

    public WalletRequest(String userName, String transactionId, int balanceChange) {
        this.userName = userName;
        this.transactionId = transactionId;
        this.balanceChange = balanceChange;
    }

    public String getUserName() {
        return userName;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getBalanceChange() {
        return balanceChange;
    }
}
