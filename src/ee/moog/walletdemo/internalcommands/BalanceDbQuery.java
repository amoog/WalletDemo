package ee.moog.walletdemo.internalcommands;

/**
 * Created by Andu on 3.11.2015.
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
