package ee.moog.walletdemo;

import ee.moog.walletdemo.dbaccess.DBReader;
import ee.moog.walletdemo.dbaccess.DBWriter;
import ee.moog.walletdemo.internalcommands.BalanceCommand;
import ee.moog.walletdemo.internalcommands.Command;
import ee.moog.walletdemo.internalcommands.ConfigChangeCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class WalletServer implements  Runnable {
    private DBReader reader;
    private DBWriter writer;
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

                Command command = requestQueue.poll( 100, TimeUnit.MILLISECONDS );

                checkRegularActions();

                if( command != null ) {
                    if( command instanceof BalanceCommand ) {
                        handleIncomingRequest((BalanceCommand) command);
                    }
                    else if( command instanceof ConfigChangeCommand ) {
                            handleConfigurationChange((ConfigChangeCommand) command);
                        }
                    else { // stop
                        handleStop();
                        break;
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void checkRegularActions() {

    }

    private void handleReaderResponse( BalanceInfo readerResponse ) {

    }

    private void handleIncomingRequest( BalanceCommand incomingRequest ) {

    }

    private void handleConfigurationChange( ConfigChangeCommand configChange ) {

    }

    private void handleStop() {

    }

    public class WalletServerBuilder {

    }
}
