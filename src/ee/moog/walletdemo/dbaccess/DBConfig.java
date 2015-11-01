package ee.moog.walletdemo.dbaccess;

import ee.moog.walletdemo.parameters.ConstantParameters;
import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 */
public class DBConfig {

    public Connection getConnection() {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            // panic here
            e.printStackTrace();
        }

        JDBCDataSource dataSource = new JDBCDataSource();
        try {
            dataSource.setUrl(ConstantParameters.getDburl() );
            JDBCConnection connection = (JDBCConnection) dataSource.getConnection();
            // connection.setTransactionIsolation();
            return connection;
        } catch (SQLException e) {
            // panic here
            e.printStackTrace();
            return null;
        }
    }
}
