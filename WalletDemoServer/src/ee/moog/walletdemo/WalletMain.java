package ee.moog.walletdemo;

import ee.moog.walletdemo.dbaccess.DBConnectionFactory;
import ee.moog.walletdemo.dbaccess.DBParametersReader;
import ee.moog.walletdemo.internalcommands.UpdateBlacklist;
import ee.moog.walletdemo.internalcommands.UpdateChangeLimit;
import ee.moog.walletdemo.parameters.ConstantParameters;
import ee.moog.walletdemo.parameters.HardcodedParameters;
import ee.moog.walletdemo.pojo.BlackListItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 */
public class WalletMain {
    private static final String TAG = "WalletMain";
    public static void main(String [ ] args) {
        out("Wallet memorydb demo");
        try {
            String propFileName = "walletserver.properties";
            if( args.length > 0 )
                propFileName = args[ 0 ];
            out("Reading parameters from " + propFileName );
            ConstantParameters.init( propFileName );
            out("Starting log");
            WLog.init();

            DBConnectionFactory dbConnectionFactory = new DBConnectionFactory();

            out("Initialising blacklist");
            DBParametersReader dbParametersReader = new DBParametersReader(dbConnectionFactory);
            HashMap<String, BlackListItem> blacklist = dbParametersReader.readBlackList();

            out("Initialising change limit");
            int changeLimit = dbParametersReader.readIntParam( "LIMIT" );
            if( changeLimit == -1 ) {
                out( "Could not read limit. Limit disabled.");
                WLog.l( TAG, "Could not read limit. Limit.disabled." );
                changeLimit = Integer.MAX_VALUE;
            }

            out( "Initialising in-memory wallet");
            WalletInMemDb walletdb = (new WalletInMemDb.Builder())
                    .setDbConfig(dbConnectionFactory)
                    .setChangeLimit( changeLimit )
                    .setBlacklist( blacklist )
                    .build();

            out( "Initialising tcp server" );
            WalletServer server = new WalletServer();
            server.setPort(ConstantParameters.getListenPort());
            server.setInmemBb(walletdb);

            Thread walletThread = walletdb.start();

            out( "Starting tcp server on port " + ConstantParameters.getListenPort() );
            server.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            WLog.l( "WalletMain", "Server started" );
            out("Server running");
            out("Type \"help\" for commands");
            while( true ) {
                String cmd = br.readLine();
                switch( cmd ) {
                    case "blupdate":
                        out( "Reading blacklist" );
                        WLog.l( TAG, "Blacklist update requested. Reading blacklist" );
                        try {
                            HashMap<String, BlackListItem> new_blacklist = dbParametersReader.readBlackList();
                            out( "Updating blacklist" );
                            WLog.d( TAG, "Updating blacklist" );
                            walletdb.getRequestQueue().put( new UpdateBlacklist( new_blacklist ));
                        }
                        catch( SQLException | ClassNotFoundException e ) {
                            WLog.l( "WalletMain", "Error reading new blacklist" + e.getMessage() );
                        }

                        break;
                    case "limitupdate":
                        out( "Reading change limit" );
                        WLog.l( TAG, "Change limit update requested. Reading new change limit" );
                        int new_limit = dbParametersReader.readIntParam( "LIMIT" );
                        if( new_limit == -1 ) {
                            out( "Could not read limit" );
                            WLog.l( TAG, "Could not read change limit" );
                        }
                        else
                            walletdb.getRequestQueue().put( new UpdateChangeLimit( new_limit ));

                        break;
                    case "help":
                        out( "Commands are:" );
                        out( "    \"blupdate\" - update blacklist" );
                        out( "    \"limitupdate\" - update change limit" );
                        out( "    \"exit\" - terminate program" );
                        out( "    \"help\" - display this so called help" );
                        break;
                    case "exit":
                        WLog.d( "WalletMain", "Server shutdown started" );
                        walletdb.stop();
                        server.stop();
                        walletThread.join(HardcodedParameters.MEMDB_JOIN_WAIT_TIMEOUT );
                        WLog.l( "WalletMain", "Server shutdown completed" );
                        WLog.stop();
                        return;

                    default:
                        out( "Unknown command, type \"help\" for command list");
                }
            }
        }
        catch (Throwable e) {
            Panic.panic( "Main loop terminated unexpectedly", e );
        }
    }

    public static void out( String o ) {
        System.out.println( o );
    }
}
