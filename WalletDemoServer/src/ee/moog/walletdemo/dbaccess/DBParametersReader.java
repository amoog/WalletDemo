package ee.moog.walletdemo.dbaccess;

import ee.moog.walletdemo.pojo.BlackListItem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 *
 */
public class DBParametersReader {
    private DBConnectionFactory dbConnectionFactory;

    public DBParametersReader( DBConnectionFactory dbConnectionFactory) {
        this.dbConnectionFactory = dbConnectionFactory;
    }

    public HashMap<String, BlackListItem> readBlackList() throws SQLException, ClassNotFoundException {
        HashMap<String, BlackListItem> result = new HashMap<>();
        Statement statement = null;
        Connection conn = dbConnectionFactory.getConnection();
        try {
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery( "SELECT USERNAME FROM BLACKLIST" );
            while( rs.next() ) {
                String userName = rs.getString( 1 );
                BlackListItem newItem = new BlackListItem( userName );

                result.put( userName, newItem );
            }
        }
        finally {
            if( statement != null )
                statement.close();
            conn.close();
        }
        return result;
    }

    public int readIntParam( String key ) throws Exception {
        Statement statement = null;
        Connection conn = dbConnectionFactory.getConnection();
        try {
            statement = conn.createStatement();
            String sqlQuery = "SELECT PARAM_VALUE FROM INT_PARAMS WHERE PARAM_NAME=" + "'" + key + "'";
            ResultSet rs = statement.executeQuery( sqlQuery );
            if( rs.next() )
                return rs.getInt( 1 );
            else
                return -1;
        }
        finally {
            if( statement != null )
                statement.close();
            conn.close();
        }
    }
}
