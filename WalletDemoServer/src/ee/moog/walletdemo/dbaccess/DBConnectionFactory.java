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
public class DBConnectionFactory {
    public DBConnectionFactory() throws ClassNotFoundException {
        // make sure driver is available
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
    }

    public Connection getConnection() throws SQLException {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl(ConstantParameters.getDburl() );
        if( ConstantParameters.getDbUser().length() > 0 )
            dataSource.setUser( ConstantParameters.getDbUser() );
        if( ConstantParameters.getDbPassword().length() > 0 )
            dataSource.setPassword( ConstantParameters.getDbPassword() );
        JDBCConnection connection = (JDBCConnection) dataSource.getConnection();
        return connection;
    }
}
