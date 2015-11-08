package ee.moog.walletdemo.internalcommands;

/**
 *  Sent from memory db to DBBalanceReader
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
