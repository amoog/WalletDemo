package ee.moog.walletdemo;

import ee.moog.walletdemo.dbaccess.DBConfig;
import ee.moog.walletdemo.dbaccess.DBReader;
import ee.moog.walletdemo.parameters.ConstantParameters;

/**
 *
 */
public class WalletMain {
    public static void main(String [ ] args) {
        System.out.println("Wallet demo");

        ConstantParameters.init("");
        DBConfig dbConfig = new DBConfig();

        DBReader reader = new DBReader( dbConfig );
        reader.run();

    }
}
