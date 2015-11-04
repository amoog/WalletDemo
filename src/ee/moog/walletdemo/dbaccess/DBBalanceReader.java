package ee.moog.walletdemo.dbaccess;

import ee.moog.walletdemo.pojo.BalanceInfo;
import ee.moog.walletdemo.Panic;
import ee.moog.walletdemo.internalcommands.BalanceDbQuery;
import ee.moog.walletdemo.internalcommands.Command;
import ee.moog.walletdemo.internalcommands.StopCommand;

import java.sql.*;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public class DBBalanceReader implements Runnable {
    private DBConfig dbConfig;
    private Connection conn;

    private PreparedStatement readStatement;


    private LinkedBlockingQueue<Command> requestQueue;
    private ConcurrentLinkedQueue<BalanceInfo> resultQueue;

    public DBBalanceReader(DBConfig config) {
        dbConfig = config;

        requestQueue = new LinkedBlockingQueue<>();
        resultQueue = new ConcurrentLinkedQueue<>();
    }

    private void prepareStatements() throws SQLException {
        conn = dbConfig.getConnection();
        readStatement = conn.prepareStatement("SELECT BALANCE_VERSION, BALANCE FROM PLAYER WHERE USERNAME=?");
    }

    public Queue<BalanceInfo> getResultQueue() {
        return resultQueue;
    }

    public BlockingQueue<Command> getRequestQueue() {
        return requestQueue;
    }

    private BalanceInfo readBalanceInfo( String username ) throws SQLException {
        readStatement.setString( 1, username );
        ResultSet rs = readStatement.executeQuery();
        try {
            if (!rs.next())
                return new BalanceInfo(username, -1, -1);
            int balance_version = rs.getInt(1);
            int balance = rs.getInt(2);
            return new BalanceInfo( username, balance_version, balance );
        }
        finally {
            rs.close();
        }
    }

    @Override
    public void run() {
        while( true ) {
            try {
                Command command = requestQueue.take();
                if( command instanceof BalanceDbQuery) {
                    BalanceInfo result = readBalanceInfo( ((BalanceDbQuery) command).getUserName() );
                    resultQueue.offer(result);
                }
                else
                    if( command instanceof StopCommand )
                        break;
                    else
                        Panic.panic( null );

            } catch (InterruptedException | SQLException e) {
                Panic.panic( e );
            }
        }
    }
}
