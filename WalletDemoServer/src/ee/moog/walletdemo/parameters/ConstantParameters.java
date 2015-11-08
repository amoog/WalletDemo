package ee.moog.walletdemo.parameters;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public class ConstantParameters {
    private static String dburl;
    private static String dbUser;
    private static String dbPassword;

    private static String logDirectory;
    private static boolean enableDebugLog;

    private static int listenPort;;

    public static void init(String fileName) throws IOException {
        FileInputStream inputStream = new FileInputStream( fileName );
        Properties prop = new Properties();
        prop.load(inputStream);

        dburl = "jdbc:hsqldb:" + prop.getProperty( "db_url" );
        dbUser = prop.getProperty( "db_user", "" );
        dbPassword = prop.getProperty( "db_password", "" );
        logDirectory = prop.getProperty( "log_directory" );
        enableDebugLog = prop.getProperty( "enable_debug_log", "0" ) == "1";
        listenPort = Integer.parseInt( prop.getProperty( "listen_port" ));


    }

    public static String getDburl() {
        return dburl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static int getListenPort() {
        return listenPort;
    }

    public static String getLogDirectory() {
        return logDirectory;
    }

    public static boolean getEnableDebugLog() {
        return enableDebugLog;
    }
}
