package ee.moog.walletdemo.parameters;

/**
 *
 */
public class ConstantParameters {
    private static String dburl = "jdbc:hsqldb:file:C:/Users/Andu/IdeaProjects/WalletDemo/hdb/walletdb";;

    private static int listenPort = 6789;

    public static void init(String fileName) {
        // dburl =
    }

    public static String getDburl() {
        return dburl;
    }

    public static int getListenPort() {
        return listenPort;
    }
}
