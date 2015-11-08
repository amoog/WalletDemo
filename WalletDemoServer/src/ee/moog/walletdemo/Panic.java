package ee.moog.walletdemo;

/**
 *
 */
public class Panic {
    public static void panic( String message ) {
        panic( message, null, 1 );
    }
    public static void panic( String message, Throwable e ) {
        panic( message, e, 1 );
    }

    public static void panic( String message, Throwable e, int exitCode ) {
        System.err.println( "Abnormal termination" );
        if( message != null )
            System.err.println( message );
        if( e != null )
            e.printStackTrace();
        System.exit( exitCode );
    }
}
