package ee.moog.walletdemo.internalcommands;

/**
 *
 */
public class UpdateChangeLimit extends Command {
    int changeLimit;

    public UpdateChangeLimit(int changeLimit) {
        this.changeLimit = changeLimit;
    }

    public int getChangeLimit() {
        return changeLimit;
    }
}
