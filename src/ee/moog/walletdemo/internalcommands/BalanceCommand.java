package ee.moog.walletdemo.internalcommands;

/**
 * .
 */
public class BalanceCommand extends Command {
    private String username;

    public BalanceCommand(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
