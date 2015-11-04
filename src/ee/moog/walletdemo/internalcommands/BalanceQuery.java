package ee.moog.walletdemo.internalcommands;

/**
 * .
 */
public class BalanceQuery extends Command {
    private String username;
    private String transaction_id;
    private int balanceChange;

    public BalanceQuery(String username, String transaction_id, int balanceChange) {
        this.username = username;
        this.transaction_id = transaction_id;
        this.balanceChange = balanceChange;
    }

    public String getUsername() {
        return username;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public int getBalanceChange() {
        return balanceChange;
    }
}
