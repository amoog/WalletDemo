package ee.moog.walletdemo.dbaccess;

import ee.moog.walletdemo.Panic;
import ee.moog.walletdemo.parameters.ConstantParameters;
import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 */
public class DBConfig {

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");

        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl(ConstantParameters.getDburl() );
        JDBCConnection connection = (JDBCConnection) dataSource.getConnection();
        return connection;
    }
}
