package ee.moog.walletdemo;

import ee.moog.walletdemo.dbaccess.DBConfig;
import ee.moog.walletdemo.dbaccess.DBBalanceReader;
import ee.moog.walletdemo.parameters.ConstantParameters;


/**
 *
 */
public class WalletMain {
    public static void main(String [ ] args) {
        System.out.println("Wallet demo");

        ConstantParameters.init("");
        DBConfig dbConfig = new DBConfig();

        DBBalanceReader reader = new DBBalanceReader( dbConfig );
        reader.run();


    }
}
