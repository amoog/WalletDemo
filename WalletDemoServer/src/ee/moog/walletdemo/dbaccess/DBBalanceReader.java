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
    private DBConnectionFactory dbConnectionFactory;
    private Connection conn;

    private PreparedStatement readStatement;

    private LinkedBlockingQueue<Command> requestQueue;
    private ConcurrentLinkedQueue<BalanceInfo> resultQueue;

    public DBBalanceReader(DBConnectionFactory config) throws SQLException, ClassNotFoundException {
        dbConnectionFactory = config;

        requestQueue = new LinkedBlockingQueue<>();
        resultQueue = new ConcurrentLinkedQueue<>();

        prepareStatements();
    }

    private void prepareStatements() throws SQLException, ClassNotFoundException {
        conn = dbConnectionFactory.getConnection();
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
                return new BalanceInfo(username, 0, 0);
            int balance_version = rs.getInt(1);
            long balance = rs.getLong(2);
            return new BalanceInfo( username, balance_version, balance );
        }
        finally {
            rs.close();
        }
    }

    @Override
    public void run() {
        try {
            try {
                while (true) {
                    Command command = requestQueue.take();
                    if (command instanceof BalanceDbQuery) {
                        BalanceInfo result = readBalanceInfo(((BalanceDbQuery) command).getUserName());
                        resultQueue.offer(result);
                    } else {// assume stop
                        break;
                    }
                }
            }
            finally {
                conn.close();
            }
        } catch ( Throwable e ) {
            // there are probably ways to handle db failures better, but for now just panic
            Panic.panic( "DBBalanceReader unexpected error", e );
        }
    }

    public void stop() {
        requestQueue.offer( new StopCommand() );
    }
}
