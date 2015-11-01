package ee.moog.walletdemo.parameters;

/**
 *
 */
public class ConstantParameters {
    private static String dburl = "jdbc:hsqldb:file:C:/Users/Andu/IdeaProjects/WalletDemo/hdb/walletdb";;

    public static void init(String fileName) {
        // dburl =
    }

    public static String getDburl() {
        return dburl;
    }
}
