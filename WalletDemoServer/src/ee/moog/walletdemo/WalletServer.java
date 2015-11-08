package ee.moog.walletdemo;

import org.hsqldb.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class WalletServer implements Runnable {
    private WalletInMemDb walletInMemDb;
    private int listen_port;

    private Thread thread;
    private ServerSocket listening_socket;

    private AtomicBoolean stopped = new AtomicBoolean( false );

    private HashSet<TcpReaderWriter> readerWriters = new HashSet<>();

    public WalletServer setInmemBb( WalletInMemDb db ) {
        walletInMemDb = db;
        return this;
    }

    public WalletServer setPort( int port ) {
        listen_port = port;
        return this;
    }

    private void addReaderWriter( TcpReaderWriter readerWriter ) {
        synchronized ( readerWriters ) {
            readerWriters.add( readerWriter );
        }
    }

    public void removeReaderWriter( TcpReaderWriter readerWriter ) {
        synchronized ( readerWriters ) {
            readerWriters.remove( readerWriter );
        }
    }

    private void stopReaderWriters() {
        synchronized ( readerWriters ) {
            Iterator<TcpReaderWriter> it = readerWriters.iterator();
            while( it.hasNext() ) {
                TcpReaderWriter current = it.next();
                current.stop( false ); // removing from set is no longer necessary
            }
        }
    }

    @Override
    public void run() {
        try {
            while( true ) {
                Socket conn = listening_socket.accept();
                TcpReaderWriter readerWriter = new TcpReaderWriter( conn, walletInMemDb.getRequestQueue(), this );
                addReaderWriter( readerWriter );
                readerWriter.start();
            }
        } catch (Throwable e) {
            stop();
        }

    }

    public void start() throws IOException {
        listening_socket = new ServerSocket( listen_port );
        thread = new Thread( this );
        thread.start();
    }

    public void stop() {
        if( stopped.compareAndSet( false, true ) ) {
            if( !listening_socket.isClosed() )
                try {
                    listening_socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            stopReaderWriters();
        }

    }
}
