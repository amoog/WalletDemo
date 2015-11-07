package ee.moog.walletdemo.dbaccess;

import ee.moog.walletdemo.pojo.BlackListItem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 */
public class DBParametersReader {

    public DBParametersReader( DBConfig dbConfig ) {

    }

    public void close() {

    }
    public HashMap<String, BlackListItem> readBlackList( ) {
        HashMap<String, BlackListItem> result = new HashMap<>();
        return result;
    }

    public int readIntParam( String key ) {
        return 100000;
    }
}
