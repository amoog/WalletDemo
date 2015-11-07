package ee.moog.walletprotocol;

public class Protocol {
    public static final int REQUEST_BUFFER_SIZE = 24; // username 10 + transaction_id 10 + balancechange 4
    public static final int RESPONSE_BUFFER_SIZE = 30;
    // transaction_id 10 + error_code 4 +  balance_version 4 + balancechange 4 + balance_after_change 8

    // result (error) codes
    public static final int RESULT_NONE = 0;
    public static final int RESULT_OK = 1;

    public static final int RESULT_OVER_LIMIT = 2;
    public static final int RESULT_BLACKLISTED = 3;
    public static final int RESULT_NOT_ENOUGH_BALANCE = 4;
    public static final int RESULT_INVALID_DUPLICATE = 5;

    public static byte[] createRequestBuffer() {
        return new byte[ REQUEST_BUFFER_SIZE ];
    }

    public static WalletRequest parseRequest( byte[] requestBuffer ) {
        String userName = new String( requestBuffer, 0, 10 );
        userName = userName.trim();
        String transactionId = new String( requestBuffer, 10, 10 );
        transactionId = transactionId.trim();
        int balanceChange = readInt( requestBuffer, 20 );

        return new WalletRequest( userName, transactionId, balanceChange );
     }

    public static void writeRequest( byte[] requestBuffer, WalletRequest request ) {
        putString( requestBuffer, 0, request.getUserName(), 10 );
        putString( requestBuffer, 10, request.getTransactionId(), 10 );
        putInt( requestBuffer, 20, request.getBalanceChange() );
    }

    public static WalletResponse parseResponse( byte[] responseBuffer ) {
        String transactionId = new String( responseBuffer, 0, 10 );
        transactionId.trim();
        int errorCode = readInt( responseBuffer, 10 );
        int balanceVersion = readInt( responseBuffer, 14 );
        int balanceChange = readInt( responseBuffer, 18 );
        long balance = readLong( responseBuffer, 22 );

        return new WalletResponse( transactionId, errorCode, balanceVersion, balanceChange, balance );
    }

    public static void writeResponse( byte[] responseBuffer, WalletResponse response ) {
        putString( responseBuffer, 0, response.getTransactionId(), 10 );
        putInt( responseBuffer, 10, response.getErrorCode() );
        putInt( responseBuffer, 14, response.getBalanceVersion() );
        putInt( responseBuffer, 18, response.getBalanceChange() );
        putLong( responseBuffer, 22, response.getBalanceAfter() );
    }

    public static byte[] createResponseBuffer() {
        return new byte[ RESPONSE_BUFFER_SIZE ];
    }

    private static long readLong(byte[] bytes, int offset) {
        long res = 0;
        for(int i = offset; i < offset + 8; i++) {
            res <<= 8;
            res ^= bytes[i] & 0xFF;
        }
        return res;
    }

    private static int readInt(byte[] bytes, int offset) {
        int res = 0;
        for(int i = offset; i < offset + 4; i++) {
            res <<= 8;
            res ^= bytes[i] & 0xFF;
        }
        return res;
    }

    private static void putString( byte[] bytes, int offset, String value, int length ) {
        byte[] strbytes = value.getBytes();
        if( strbytes.length > length )
            throw new IllegalArgumentException( "String too long!" );
        for( int i = 0; i < strbytes.length; i++ )
            bytes[ offset + i ] = strbytes[ i ];
        for( int i = strbytes.length; i < length; i++ )
            bytes[ i ] = ' '; // this will be stripped in parse*
    }

    private static void putLong(byte[] bytes, int offset, long value) {
        for (int i = offset + 7; i > offset; i--) {
            bytes[i] = (byte) value;
            value >>>= 8;
        }
        bytes[offset] = (byte) value;
    }

    private static void putInt(byte[] bytes, int offset, int value) {
        for (int i = offset + 3; i > offset; i--) {
            bytes[i] = (byte) value;
            value >>>= 8;
        }
        bytes[offset] = (byte) value;
    }

}
