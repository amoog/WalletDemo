package ee.moog.walletdemo.internalcommands;

import ee.moog.walletdemo.pojo.BlackListItem;

import java.util.HashMap;

/**
 *
 */
public class UpdateBlacklist extends Command {
    HashMap<String,BlackListItem> newBlackList;

    public UpdateBlacklist(HashMap<String, BlackListItem> newBlackList) {
        this.newBlackList = newBlackList;
    }

    public HashMap<String, BlackListItem> getNewBlackList() {
        return newBlackList;
    }
}
