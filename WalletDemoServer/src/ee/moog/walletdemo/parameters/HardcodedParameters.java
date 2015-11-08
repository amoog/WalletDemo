package ee.moog.walletdemo.parameters;

/**
 *
 */
public class HardcodedParameters {
    public static final int RECENTS_HISTORY_MIN_SIZE = 1000; // minimum number of recent responses to check for duplicate

    public static final int MINIMUM_POLL_WAIT = 3; // minimum time in milliseconds to wait for commands in case db read is pending
    public static final int REGULAR_ACTIONS_DELAY=1000; // in milliseconds

    public static final int MAX_OPEN_REQUESTS_PER_SOCKET = 1000;

    public static final int LOGGER_FLUSH_INTERVAL = 200; // in millseconds

    public static final int DBWRITER_JOIN_WAIT_TIMEOUT = 10000; // time to wait for db writer thread to terminate
    // memdb wait should be larger that dbwriter wait
    public static final int MEMDB_JOIN_WAIT_TIMEOUT = 15000; // time to wait for memdb thread to terminate
}
