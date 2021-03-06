package ee.moog.walletdemo.pojo;

/**
 *
 */
public class BalanceInfo {
    private String username;
    private int balance_version;
    private long balance;

    public BalanceInfo(String username, int balance_version, long balance) {
        this.username = username;
        this.balance_version = balance_version;
        this.balance = balance;
    }

    public BalanceInfo copy() {
        return new BalanceInfo( username, balance_version, balance );
    }

    public String getUsername() {
        return username;
    }

    public int getBalance_version() {
        return balance_version;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance_version(int balance_version) {
        this.balance_version = balance_version;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
