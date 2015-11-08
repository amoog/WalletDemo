package ee.moog.walletdemo;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import ee.moog.walletdemo.dbaccess.DBBalanceReader;
import ee.moog.walletdemo.dbaccess.DBBalanceWriter;
import ee.moog.walletdemo.dbaccess.DBConnectionFactory;
import ee.moog.walletdemo.internalcommands.*;
import ee.moog.walletdemo.parameters.ConstantParameters;
import ee.moog.walletdemo.parameters.HardcodedParameters;
import ee.moog.walletdemo.pojo.BalanceInfo;
import ee.moog.walletdemo.pojo.BlackListItem;
import ee.moog.walletprotocol.Protocol;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Central class holding balance information.
 * It retrieves requests and commands via requestQueue and returns responses to queues provided in
 * BalanceQuery objects.
 * Main logic is in the run() method
 */
public class WalletInMemDb implements  Runnable {
    private static final String TAG = "WIMDB";
    // thread main response loop runs in
    private Thread memDbThread = null;

    private DBBalanceReader dbReader;
    private Thread dbReaderThread;

    private DBBalanceWriter dbWriter;
    private Thread dbWriterThread;

    private LinkedBlockingQueue<Command> requestQueue;

    private HashMap<String,BalanceInfo> balances;

    // key of these is request_id
    private HashMap<String,BalanceResponse> recentResponses;
    private HashMap<String,BalanceResponse> olderResponses;

    private HashMap<String, List<BalanceQuery>> pendingReads;

    private HashMap<String, BalanceInfo> pendingWrites;

    private HashMap<String,BlackListItem> blacklist = null;
    private int changeLimit = 0;


    private boolean stopping = false;
    private long lastWrite;

    private WalletInMemDb() {} // private constructor so only builder can build this

    @Override
    public void run() {
        lastWrite = System.currentTimeMillis();
        while( true ) {
            try {
                // first check for response from db reader
                // always read all responses, as we are the only one sending read requests
                // there can never be too many responses coming in
                while( true ) {
                    BalanceInfo readerResponse = dbReader.getResultQueue().poll();
                    if( readerResponse == null )
                        break;
                    if(ConstantParameters.getEnableDebugLog())
                        WLog.d( TAG, "DB response USER: " + readerResponse.getUsername() +
                                " VERSION: " + readerResponse.getBalance_version() +
                                " BALANCE: " + readerResponse.getBalance() );
                    handleReaderResponse( readerResponse );
                }

                Command command;
                command = requestQueue.poll(HardcodedParameters.REGULAR_ACTIONS_DELAY, TimeUnit.MILLISECONDS );

                doRegularActions();

                if( command != null ) {
                    if( command instanceof BalanceQuery) {
                        handleIncomingRequest((BalanceQuery) command, false );
                    }
                    else if( command instanceof UpdateBlacklist ) {
                            handleBlacklistChange((UpdateBlacklist) command);
                        }
                    else if( command instanceof UpdateChangeLimit )
                        handleChangeLimitChange( (UpdateChangeLimit) command );
                    else { // stop
                        handleStop();
                        break;
                    }

                }
            } catch ( Throwable e) {
                Panic.panic( "WalletInMemDb unexpected error", e );
            }
        }
    }

    public Thread start() {
        memDbThread = new Thread( this );
        memDbThread.start();

        dbReaderThread = new Thread( dbReader );
        dbWriterThread = new Thread( dbWriter );

        dbReaderThread.start();
        dbWriterThread.start();

        return memDbThread;
    }

    public void stop() {
        requestQueue.offer( new StopCommand() );  // this should always succeed
    }

    private void doRegularActions() {
        long currentTime = System.currentTimeMillis();
        if( lastWrite + HardcodedParameters.REGULAR_ACTIONS_DELAY < currentTime ) {
            doPendingWrites();
            lastWrite = currentTime;
        }
    }

    private void doPendingWrites() {
        dbWriter.saveChanges(pendingWrites);
        pendingWrites = new HashMap<>();
    }

    private void bufferWrite( BalanceInfo infoToBuffer ) {
        pendingWrites.put( infoToBuffer.getUsername(), infoToBuffer.copy() );
    }

    private void handleReaderResponse( BalanceInfo readerResponse ) {
        // add balance info
        balances.put( readerResponse.getUsername(), readerResponse );
        // now retry any pending queries
        List<BalanceQuery> pendingQueries = pendingReads.remove( readerResponse.getUsername() );
        if( pendingQueries != null )
            for( BalanceQuery query : pendingQueries )
                handleIncomingRequest( query, false );
    }

    // readnotpending = false means that we have already read this from db
    private void handleIncomingRequest( BalanceQuery incomingRequest, boolean readNotPending ) {
        if(ConstantParameters.getEnableDebugLog())
            WLog.d( TAG, "BalanceRequest USER: " + incomingRequest.getUsername() +
                    " ID: " + incomingRequest.getTransaction_id() +
                    " CHANGE: " + incomingRequest.getBalanceChange() );

        BalanceResponse response = null;
        // check if we have lately processed this request
        response = checkRecentResponses(incomingRequest);

        if (response == null)
            // check limit and blacklist
            response = checkRequest(incomingRequest); // this must be after recents check, as blacklist and limit can change

        if (response == null) {
            if( !readNotPending && waitForPendingRead(incomingRequest))
                return;  // nothing to send yet, will send on next try

            // get from memory
            BalanceInfo balanceInfo = balances.get(incomingRequest.getUsername());
            if (balanceInfo == null) { // not in memory yet
                readFromExternalDb(incomingRequest);
                return;
            }

            // check and change balance
            response = checkAndChangeBalance(balanceInfo, incomingRequest);

            // add to recents
            addToRecents( response );
        }

        // send response
        if(ConstantParameters.getEnableDebugLog())
            WLog.d( TAG, "Response CODE: " + response.getErrorCode() +
                    "USER: " + response.getUserName() +
                    " ID: " + response.getTransactionId() +
                    " VERSION: " + response.getBalanceVersion() +
                    " BALANCE: " + response.getBalanceAfter() );

        incomingRequest.getResponseQueue().add( response );
    }

    private BalanceResponse checkRecentResponses( BalanceQuery incomingRequest ) {
        BalanceResponse result = recentResponses.get( incomingRequest.getTransaction_id() );
        if( result == null )
            olderResponses.get( incomingRequest.getTransaction_id() );
        if( result == null )
            return null;
        // check if request and response match
        if( result.getUserName().equals( incomingRequest.getUsername()) &&
                result.getTransactionId().equals( incomingRequest.getTransaction_id() ) &&
                result.getBalanceChange() == incomingRequest.getBalanceChange() ) {
            return result;
        }
        else
            return new BalanceResponse( incomingRequest.getUsername(), incomingRequest.getTransaction_id(),
                    Protocol.RESULT_INVALID_DUPLICATE, 0, incomingRequest.getBalanceChange(), 0 );
    }

    private BalanceResponse checkRequest(BalanceQuery incomingRequest) {
        int errorCode;
        if( Math.abs( incomingRequest.getBalanceChange() ) > this.changeLimit )
            errorCode = Protocol.RESULT_OVER_LIMIT;
        else {
            BlackListItem blacklistItem = blacklist.get(incomingRequest.getUsername());
            if (blacklistItem != null)
                errorCode = Protocol.RESULT_BLACKLISTED;
            else
                return null;
        }
        return new BalanceResponse( incomingRequest.getUsername(), incomingRequest.getTransaction_id(),
                errorCode, 0, incomingRequest.getBalanceChange(), 0 );
    }

    private boolean waitForPendingRead(BalanceQuery incomingRequest) {
        List<BalanceQuery> pendingRequests = pendingReads.get( incomingRequest.getUsername() );
        if( pendingRequests == null )
            return false;

        pendingRequests.add( incomingRequest );
        return true;
    }

    private void readFromExternalDb(BalanceQuery incomingRequest) {
        dbReader.getRequestQueue().add( new BalanceDbQuery( incomingRequest.getUsername() ) );
        List<BalanceQuery> pendingRequests = new ArrayList<>();
        pendingRequests.add( incomingRequest );
        pendingReads.put( incomingRequest.getUsername(), pendingRequests );
    }

    private BalanceResponse checkAndChangeBalance(BalanceInfo balanceInfo, BalanceQuery incomingRequest) {
        if( balanceInfo.getBalance() + incomingRequest.getBalanceChange() < 0 )
            return new BalanceResponse( incomingRequest.getUsername(), incomingRequest.getTransaction_id(),
                    Protocol.RESULT_NOT_ENOUGH_BALANCE, balanceInfo.getBalance_version(),
                    incomingRequest.getBalanceChange(), balanceInfo.getBalance() );

        balanceInfo.setBalance_version( balanceInfo.getBalance_version() + 1);
        // long overflow is not handled here intentionally
        balanceInfo.setBalance( balanceInfo.getBalance() + incomingRequest.getBalanceChange() );

        bufferWrite( balanceInfo );

        return new BalanceResponse( incomingRequest.getUsername(), incomingRequest.getTransaction_id(),
                Protocol.RESULT_OK, balanceInfo.getBalance_version(),
                incomingRequest.getBalanceChange(), balanceInfo.getBalance() );
    }

    private void addToRecents(BalanceResponse response) {
        recentResponses.put( response.getTransactionId(), response );
        if( recentResponses.size() == HardcodedParameters.RECENTS_HISTORY_MIN_SIZE ) {
            // roll history over
            HashMap<String,BalanceResponse> temp = olderResponses;
            temp.clear();

            olderResponses = recentResponses;
            recentResponses = temp;
        }
    }

    private void handleBlacklistChange( UpdateBlacklist blaclistChange ) {
        blacklist = blaclistChange.getNewBlackList();
    }

    private void handleChangeLimitChange(UpdateChangeLimit updateChangeLimit) {
        changeLimit = updateChangeLimit.getChangeLimit();
    }

    private void handleStop() {
        stopping = true;
        dbReader.stop();

        doPendingWrites();

        dbWriter.stop();
        // we have to wait for dbWriter to finish writing all changes!
        try {
            dbWriterThread.join( HardcodedParameters.DBWRITER_JOIN_WAIT_TIMEOUT );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public LinkedBlockingQueue<Command> getRequestQueue() {
        return requestQueue;
    }

    public static class Builder {
        private DBConnectionFactory init_dbConnectionFactory;

        private HashMap<String,BlackListItem> init_blacklist = null;
        private int init_changeLimit = Integer.MAX_VALUE;

        public Builder setDbConfig(DBConnectionFactory config ) {
            init_dbConnectionFactory = config;
            return this;
        }

        public Builder setBlacklist(HashMap<String, BlackListItem> blacklist ) {
            init_blacklist = blacklist;
            return this;
        }
        public Builder setChangeLimit(int changeLimit ) {
            init_changeLimit = changeLimit;
            return this;
        }

        public WalletInMemDb build() throws SQLException, ClassNotFoundException {
            WalletInMemDb result = new WalletInMemDb();

            result.dbReader = new DBBalanceReader(init_dbConnectionFactory);
            result.dbWriter = new DBBalanceWriter(init_dbConnectionFactory);

            // this MUST be created internally because it can not be shared - memory db is also not shared!
            result.requestQueue = new LinkedBlockingQueue<>();

            result.balances = new HashMap<>();

            result.recentResponses = new HashMap<>( HardcodedParameters.RECENTS_HISTORY_MIN_SIZE );
            result.olderResponses = new HashMap<>( HardcodedParameters.RECENTS_HISTORY_MIN_SIZE );
            result.pendingReads = new HashMap<>();
            result.pendingWrites = new HashMap<>();

            if( init_blacklist == null )
                result.blacklist = new HashMap<>();
            else
                result.blacklist = init_blacklist;
            result.changeLimit = init_changeLimit;

            return result;
        }

    }
}
