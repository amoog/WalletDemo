package ee.moog.walletdemo;

import ee.moog.walletdemo.internalcommands.BalanceQuery;
import ee.moog.walletdemo.internalcommands.BalanceResponse;
import ee.moog.walletdemo.internalcommands.Command;
import ee.moog.walletdemo.parameters.HardcodedParameters;
import ee.moog.walletprotocol.Protocol;
import ee.moog.walletprotocol.WalletRequest;
import ee.moog.walletprotocol.WalletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TcpReaderWriter {
    private Socket socket;
    private BlockingQueue<Command> commandQueue;

    private LinkedBlockingQueue<Command> responseQueue;

    private Semaphore requestLimiter;

    private TcpReader reader;
    private TcpWriter writer;

    private Thread readerThread;
    private Thread writerThread;

    private AtomicBoolean finished;
    public TcpReaderWriter(Socket socket, BlockingQueue<Command> commandQueue ) {
        this.socket = socket;
        this.commandQueue = commandQueue;

        finished = new AtomicBoolean( false );

        responseQueue = new LinkedBlockingQueue<>();
        requestLimiter = new Semaphore( HardcodedParameters.MAX_OPEN_REQUESTS_PER_SOCKET );

        reader = new TcpReader();
        writer = new TcpWriter();

        readerThread = new Thread( reader );
        writerThread = new Thread( writer );

    }

    public void start() {
        readerThread.start();
        writerThread.start();
    }

    private void finishReaderWriter() {
        if( finished.compareAndSet( false, true ) ) { // finish only once
            if (!socket.isClosed())
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private class TcpReader implements Runnable {
        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                byte[] inputBuffer = Protocol.createRequestBuffer();
                while( true ) {
                    int bytesRead = 0;
                    int lastRead;
                    while( bytesRead < Protocol.REQUEST_BUFFER_SIZE ) {
                        lastRead = inputStream.read( inputBuffer, bytesRead, Protocol.REQUEST_BUFFER_SIZE - bytesRead );
                        if( lastRead == -1 ) {
                            throw new IOException( "Inputstream returned eof" );
                        }
                        bytesRead += lastRead;
                    }

                    WalletRequest request = Protocol.parseRequest( inputBuffer );

                    BalanceQuery query = new BalanceQuery( request.getUserName(), request.getTransactionId(),
                            request.getBalanceChange(), responseQueue );

                    commandQueue.put( query );

                    requestLimiter.acquire();
                }
            } catch ( Throwable e) {
                if( !socket.isClosed() )
                    Logger.l( "TcpReader", "Reading terminated by exception:" + e.getClass().getName() + ':' +
                        e.getMessage());
            }
            finishReaderWriter();
        }
    }

    private class TcpWriter implements Runnable {
        @Override
        public void run() {
            try {
                byte[] responseBuffer = Protocol.createResponseBuffer();
                // socket.setTcpNoDelay( true );
                OutputStream outputStream = socket.getOutputStream();
                while( true ) {
                    Command response = responseQueue.take();
                    if( response instanceof BalanceResponse ) {
                        BalanceResponse balanceResponse = (BalanceResponse) response;
                        WalletResponse walletResponse = new WalletResponse( balanceResponse.getTransactionId(),
                                balanceResponse.getErrorCode(), balanceResponse.getBalanceVersion(),
                                balanceResponse.getBalanceChange(), balanceResponse.getBalanceAfter() );

                        Protocol.writeResponse( responseBuffer, walletResponse );

                        outputStream.write( responseBuffer );

                        requestLimiter.release();
                    }
                    else { // assume it is stop
                        finishReaderWriter();
                        return;
                    }

                }
            } catch ( Throwable e ) {
                if( !socket.isClosed() )
                    Logger.l( "TcpWriter", "Writing terminated by exception:" + e.getClass().getName() + ':' +
                        e.getMessage());
            }
            finishReaderWriter();
        }
    }

}
