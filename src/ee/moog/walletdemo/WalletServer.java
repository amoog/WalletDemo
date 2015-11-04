package ee.moog.walletdemo;

import ee.moog.walletdemo.dbaccess.DBBalanceReader;
import ee.moog.walletdemo.dbaccess.DBBalanceWriter;
import ee.moog.walletdemo.internalcommands.BalanceQuery;
import ee.moog.walletdemo.internalcommands.BalanceResponse;
import ee.moog.walletdemo.internalcommands.Command;
import ee.moog.walletdemo.internalcommands.UpdateBlacklist;
import ee.moog.walletdemo.parameters.HardcodedParameters;
import ee.moog.walletdemo.pojo.BalanceInfo;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class WalletServer implements  Runnable {
    private DBBalanceReader reader;
    private DBBalanceWriter writer;
    private Logger l;

    private LinkedBlockingQueue<Command> requestQueue;

    private HashMap<String,BalanceInfo> balances;

    // key of these is username+request_id
    private HashMap<String,BalanceInfo> recentResponses;
    private HashMap<String,BalanceInfo> olderResponses;

    private HashMap<String, BalanceInfo> pendingReads;


    // private TCPWriter tcpwriter;
    private WalletServer() {} // only builder can build this

    @Override
    public void run() {
        while( true ) {
            try {
                // first check for response from reader
                while( true ) {
                    BalanceInfo readerResponse = reader.getResultQueue().poll();
                    if( readerResponse == null )
                        break;
                    handleReaderResponse( readerResponse );
                }

                Command command = requestQueue.poll(HardcodedParameters.REGULAR_ACTIONS_DELAY, TimeUnit.MILLISECONDS );

                checkRegularActions();

                if( command != null ) {
                    if( command instanceof BalanceQuery) {
                        handleIncomingRequest((BalanceQuery) command);
                    }
                    else if( command instanceof UpdateBlacklist) {
                            handleBlacklistChange((UpdateBlacklist) command);
                        }
                    else { // stop
                        handleStop();
                        break;
                    }

                }
            } catch (InterruptedException e) {
                Panic.panic( e );
            }
        }
    }

    private void checkRegularActions() {

    }

    private void handleReaderResponse( BalanceInfo readerResponse ) {

    }

    private void handleIncomingRequest( BalanceQuery incomingRequest ) {
        BalanceResponse response = null;
        // check if we have lately processed this request

        // check if database read is in progress for this user

        // get info from internal database

        // query from db
        // or
        // send response
    }

    private void handleBlacklistChange( UpdateBlacklist blaclistChange ) {

    }

    private void handleStop() {

    }

    public class WalletServerBuilder {

    }
}
