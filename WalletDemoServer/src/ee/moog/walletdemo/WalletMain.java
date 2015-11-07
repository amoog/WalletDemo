package ee.moog.walletdemo;

import ee.moog.walletdemo.dbaccess.DBConfig;
import ee.moog.walletdemo.dbaccess.DBBalanceReader;
import ee.moog.walletdemo.dbaccess.DBParametersReader;
import ee.moog.walletdemo.parameters.ConstantParameters;
import ee.moog.walletdemo.pojo.BlackListItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;


/**
 *
 */
public class WalletMain {
    public static void main(String [ ] args) {
        out("Wallet memorydb demo");
        try {
            DBParametersReader dbParametersReader = null;
            try {
                out("Reading parameters");
                ConstantParameters.init("");


                DBConfig dbConfig = new DBConfig();

                out("Reading blacklist");
                dbParametersReader = new DBParametersReader( dbConfig );
                HashMap<String, BlackListItem> blacklist = dbParametersReader.readBlackList();

                out("Reading change limit");
                int changeLimit = dbParametersReader.readIntParam( "LIMIT");

                out( "Initialising in memory wallet");
                WalletInMemDb walletdb = (new WalletInMemDb.Builder())
                        .setDbConfig(dbConfig)
                        .setChangeLimit( changeLimit )
                        .setBlacklist( blacklist )
                        .build();

                out( "Initialising tcp server" );
                WalletServer server = new WalletServer();
                server.setPort(ConstantParameters.getListenPort());
                server.setInmemBb(walletdb);

                walletdb.start();

                out( "Starting tcp server on port " + ConstantParameters.getListenPort() );
                server.start();

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                out("Server running");
                out("Type \"help\" for commands");
                while( true ) {
                    String cmd = br.readLine();
                    switch( cmd ) {
                        case "blupdate":
                            break;
                        case "limitupdate":
                            break;
                        case "help":
                            out( "Commands are:" );
                            out( "    \"blupdate\" - update blacklist" );
                            out( "    \"limitupdate\" - update change limit" );
                            out( "    \"exit\" - terminate program" );
                            out( "    \"help\" - display this so called help" );
                            break;
                        case "exit":
                            walletdb.stop();
                            server.stop();
                            return;

                        default:
                            out( "Unknown command, type \"help\" for command list");
                    }
                }

            } finally {
                if( dbParametersReader != null )
                    dbParametersReader.close();
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void out( String o ) {
        System.out.println( o );
    }
}
