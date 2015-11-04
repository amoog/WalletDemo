package ee.moog.walletdemo.pojo;

/**
 *
 */
public class BalanceInfo {
    private String username;
    private int balance_version;
    private int balance;

    public BalanceInfo(String username, int balance_version, int balance) {
        this.username = username;
        this.balance_version = balance_version;
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public int getBalance_version() {
        return balance_version;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance_version(int balance_version) {
        this.balance_version = balance_version;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
