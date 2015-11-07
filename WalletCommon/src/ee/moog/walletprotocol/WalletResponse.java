package ee.moog.walletprotocol;

/**
 *
 */
public class WalletResponse {
    private String transactionId;
    private int errorCode;
    private int balanceVersion;
    private int balanceChange;
    private long balanceAfter;

    public WalletResponse(String transactionId, int errorCode, int balanceVersion, int balanceChange, long balanceAfter) {
        this.transactionId = transactionId;
        this.errorCode = errorCode;
        this.balanceVersion = balanceVersion;
        this.balanceChange = balanceChange;
        this.balanceAfter = balanceAfter;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getBalanceVersion() {
        return balanceVersion;
    }

    public int getBalanceChange() {
        return balanceChange;
    }

    public long getBalanceAfter() {
        return balanceAfter;
    }
}

