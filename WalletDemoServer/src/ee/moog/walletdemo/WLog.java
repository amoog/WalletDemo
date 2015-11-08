package ee.moog.walletdemo;

import ee.moog.walletdemo.parameters.ConstantParameters;
import ee.moog.walletdemo.parameters.HardcodedParameters;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class WLog {
    private static Logger logger;

    public static void init() throws FileNotFoundException {
        logger = new Logger();
        new Thread( logger ).start();
    }

    public static void stop() {
        if( !logger.logMessages.offer( new Object() ) ) // any object except LogMessage will do
            Panic.panic( "Could not stop logger" );
    }

    public static void d(String tag, String message ) {
        if( ConstantParameters.getEnableDebugLog() )
            log( tag, message, "D" );
    }

    public static void l(String tag, String message ) {
        log( tag, message, "L" );
    }
    private static void log( String tag, String message, String level ) {
        if( !logger.logMessages.offer( new LogMessage( tag, message, level ) ) )
            Panic.panic( "Log buffer overflow" );
    }

    private static class LogMessage {
        long threadId;
        String tag;
        String message;
        String level;

        public LogMessage(String tag, String message, String level) {
            threadId = Thread.currentThread().getId();
            this.tag = tag;
            this.message = message;
            this.level = level;
        }
    }

    private static class Logger implements Runnable {
        private LinkedBlockingQueue<Object> logMessages = new LinkedBlockingQueue<>();
        private OutputStreamWriter outputWriter;
        private SimpleDateFormat dateFormat;

        Logger() throws FileNotFoundException {
            dateFormat = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss.SSS");
            String timeString = ( new SimpleDateFormat("yyyyMMdd_HHmmss") ).format( new Date() );
            String logFileName = ConstantParameters.getLogDirectory();
            if( logFileName.length() > 0 )
                logFileName += File.separator;
            logFileName += "wlog_" + timeString + ".log";
            outputWriter = new OutputStreamWriter( new FileOutputStream( logFileName ));
        }

        void writeLogMessage( LogMessage message ) throws IOException {
            outputWriter.write( dateFormat.format( new Date() ));
            outputWriter.write( " " );
            outputWriter.write( Long.toHexString( message.threadId ) );
            outputWriter.write( " " );
            outputWriter.write( message.level );
            outputWriter.write( " " );
            outputWriter.write( message.tag );
            outputWriter.write( " " );
            outputWriter.write( message.message );
            outputWriter.write( System.lineSeparator() );
        }

        @Override
        public void run() {
            long lastFlush = System.currentTimeMillis();
            try {
                while (true) {
                    Object logmessage = logMessages.poll(HardcodedParameters.LOGGER_FLUSH_INTERVAL, TimeUnit.MILLISECONDS);
                    if( logmessage instanceof  LogMessage )
                        writeLogMessage( (LogMessage) logmessage );
                    else if( logmessage != null ){
                        outputWriter.close();
                        break;
                    }
                    if( lastFlush + HardcodedParameters.LOGGER_FLUSH_INTERVAL < System.currentTimeMillis() )
                        outputWriter.flush();
                }
            }
            catch( Throwable e ) {
                Panic.panic( "Logger terminated by unexpected error", e );
            }
        }
    }
}
