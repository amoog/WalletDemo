package ee.moog.walletdemoclient;

import ee.moog.walletprotocol.Protocol;
import ee.moog.walletprotocol.WalletRequest;
import ee.moog.walletprotocol.WalletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class WalletClientTcp {
    RandomRequest randomRequest;
    private Socket socket;

    public AtomicInteger sent = new AtomicInteger();
    public AtomicInteger received = new AtomicInteger();
    public AtomicBoolean close = new AtomicBoolean( false );

    public WalletClientTcp( Socket socket, RandomRequest randomRequest ) {
        this.socket = socket;
        this.randomRequest = randomRequest;
    }

    public void start() {
        new Thread( new TcpReader() ).start();
        new Thread( new TcpWriter() ).start();
    }

    public void stop() {
        if( close.compareAndSet( false, true ))
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private class TcpReader implements Runnable {
        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] inputBuffer = Protocol.createResponseBuffer();
                while( true ) {
                    int bytesRead = 0;
                    int lastRead;
                    while( bytesRead < Protocol.RESPONSE_BUFFER_SIZE ) {
                        lastRead = inputStream.read( inputBuffer, bytesRead, Protocol.RESPONSE_BUFFER_SIZE - bytesRead );
                        if( lastRead == -1 ) {
                            throw new IOException( "Inputstream returned eof" );
                        }
                        bytesRead += lastRead;
                    }

                    WalletResponse response = Protocol.parseResponse( inputBuffer );
                    randomRequest.checkResponse( response );
                    received.getAndIncrement();
                }
            } catch ( Throwable e) {
                if( !socket.isClosed() )
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                WalletClient.out( "TcpReader - reading terminated by exception:" + e.getClass().getName() + ':' +
                            e.getMessage());
            }
            if( !close.get() )
                System.exit( 1 );
        }
    }

    private class TcpWriter implements Runnable {
        @Override
        public void run() {
            try {
                byte[] requestBuffer = Protocol.createRequestBuffer();
                OutputStream outputStream = socket.getOutputStream();
                while( true ) {
                    WalletRequest request = randomRequest.generateRequest();

                    Protocol.writeRequest( requestBuffer, request );
                    outputStream.write( requestBuffer );

                    sent.getAndIncrement();

                    //Thread.sleep( 300 );
                }
            } catch ( Throwable e ) {
                if( !socket.isClosed() )
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                WalletClient.out( "TcpWriter - writing terminated by exception:" + e.getClass().getName() + ':' +
                            e.getMessage());
            }
        }
    }
}
