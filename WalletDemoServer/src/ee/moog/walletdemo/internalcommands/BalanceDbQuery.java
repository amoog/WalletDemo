package ee.moog.walletdemo.internalcommands;

/**
 *
 */
public class BalanceDbQuery extends Command {
    private String userName;

    public BalanceDbQuery(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
