package ee.moog.walletdemo.dbaccess;

import ee.moog.walletdemo.pojo.BlackListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andu on 4.11.2015.
 */
public class DBConfigReader {
    public static List<BlackListItem> readBlackList( DBConfig dbConfig ) {
        ArrayList<BlackListItem> result = new ArrayList<>();
        return result;
    }

    public static int readIntParam( DBConfig dbConfig ) {
        return 100000;
    }
}
