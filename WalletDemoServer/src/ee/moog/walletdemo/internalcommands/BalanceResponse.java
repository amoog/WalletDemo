package ee.moog.walletdemo.internalcommands;

/**
 *
 */
public class BalanceResponse extends Command {
    String userName;
    String transactionId;
    int errorCode;
    int balanceVersion;
    int balanceChange;
    long balanceAfter;

    public BalanceResponse(String userName, String transactionId, int errorCode, int balanceVersion, int balanceChange,
                           long balanceAfter) {
        this.userName = userName;
        this.transactionId = transactionId;
        this.errorCode = errorCode;
        this.balanceVersion = balanceVersion;
        this.balanceChange = balanceChange;
        this.balanceAfter = balanceAfter;
    }


    public String getUserName() {
        return userName;
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
