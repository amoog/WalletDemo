package ee.moog.walletdemo.internalcommands;

import java.util.Queue;

/**
 * .
 */
public class BalanceQuery extends Command {
    private String username;
    private String transaction_id;
    private int balanceChange;

    private Queue responseQueue;

    public BalanceQuery(String username, String transaction_id, int balanceChange, Queue<Command> responseQueue ) {
        this.username = username;
        this.transaction_id = transaction_id;
        this.balanceChange = balanceChange;
        this.responseQueue = responseQueue;
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

    public Queue getResponseQueue() {
        return responseQueue;
    }
}
