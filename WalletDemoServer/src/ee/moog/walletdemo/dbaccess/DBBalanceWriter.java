package ee.moog.walletdemo.dbaccess;

import ee.moog.walletdemo.pojo.BalanceInfo;
import ee.moog.walletdemo.internalcommands.StopCommand;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public class DBBalanceWriter implements Runnable {
    private DBConfig dbConfig;
    private Connection conn;


    private PreparedStatement updateStatement;
    private PreparedStatement readStatement;
    private PreparedStatement insertStatement;
    private LinkedBlockingQueue<Object> requestQueue;

    public DBBalanceWriter(DBConfig config ) throws SQLException, ClassNotFoundException {
        dbConfig = config;

        requestQueue = new LinkedBlockingQueue<>();

        prepareStatements();
    }

    private void prepareStatements() throws SQLException, ClassNotFoundException {
        conn = dbConfig.getConnection();

        updateStatement = conn.prepareStatement("UPDATE PLAYER SET BALANCE_VERSION=?, BALANCE=? WHERE USERNAME=? AND BALANCE_VERSION < ?");
        readStatement = conn.prepareStatement("SELECT BALANCE_VERSION FROM PLAYER WHERE USERNAME=?");
        insertStatement = conn.prepareStatement("INSERT INTO PLAYER (USERNAME,BALANCE_VERSION,BALANCE) VALUES(?,?,?)");
    }

    private boolean updateBalanceInfo( BalanceInfo balanceInfo ) throws SQLException {
        // balance version check is needed in case there are multiple writers - it is possible that another writer
        // has already written older data
        updateStatement.setInt( 1, balanceInfo.getBalance_version() );
        updateStatement.setLong( 2, balanceInfo.getBalance() );
        updateStatement.setString( 3, balanceInfo.getUsername() );
        updateStatement.setInt( 4, balanceInfo.getBalance_version() );
        updateStatement.execute();
        return updateStatement.getUpdateCount() == 1;
    }

    private boolean balanceExists( String username ) throws SQLException {
        readStatement.setString( 1, username );
        ResultSet rs = readStatement.executeQuery();
        try {
            return rs.next();
        }
        finally {
            rs.close();
        }
    }

    private boolean addBalanceInfo( BalanceInfo balanceInfo ) throws SQLException {
        insertStatement.setString( 1, balanceInfo.getUsername() );
        insertStatement.setInt( 2, balanceInfo.getBalance_version() );
        insertStatement.setLong( 3, balanceInfo.getBalance() );
        try {
            insertStatement.execute();
        }
        catch ( SQLIntegrityConstraintViolationException e ) {
            return false;
        }
        return true;
    }

    private boolean saveBalanceInfo( BalanceInfo balanceInfo ) throws SQLException {
        // lets first try to update - this should be most probable path
        if( !updateBalanceInfo( balanceInfo ) )
            if( !balanceExists( balanceInfo.getUsername() )) // if new username then add
                return addBalanceInfo( balanceInfo );
        return true;
    }

    private void saveBalanceChanges( HashMap<String, BalanceInfo> balanceInfoList ) throws SQLException {
        HashMap<String,BalanceInfo> aggregate = new HashMap<>();
        for( BalanceInfo balanceInfo : balanceInfoList.values() ) {
            BalanceInfo lastInfo = aggregate.get(balanceInfo.getUsername());
            if( lastInfo == null || lastInfo.getBalance_version() < balanceInfo.getBalance_version() )
                aggregate.put( balanceInfo.getUsername(), balanceInfo );
        }
        while( true ) {
            for( BalanceInfo savingInfo : aggregate.values() ) {
                if( !saveBalanceInfo( savingInfo ) ) {
                    conn.rollback();
                    continue; // try again
                }
            }
            break; // if were here all succeeded
        }
    }

    public void saveChanges( HashMap<String, BalanceInfo> balanceInfos ) {
        requestQueue.offer( balanceInfos );
    }

    public void stop() {
        requestQueue.offer( new StopCommand() );
    }

    @Override
    public void run() {
        while (true) {
            try {
                Object command = requestQueue.take();
                if (command instanceof HashMap) {
                    saveBalanceChanges((HashMap<String,BalanceInfo>) command);
                } else {// must be stop
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
