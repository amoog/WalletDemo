package ee.moog.walletdemo;

import ee.moog.walletdemo.internalcommands.BalanceResponse;
import ee.moog.walletdemo.internalcommands.Command;
import ee.moog.walletdemo.parameters.HardcodedParameters;

import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Andu on 4.11.2015.
 */
public class TcpReaderWriter {
    private Socket socket;
    private BlockingQueue<Command> commandQueue;

    private LinkedBlockingQueue<Command> responseQueue;

    private Semaphore requestLimiter;

    private TcpReader reader;
    private TcpReaderWriter writer;

    public TcpReaderWriter(Socket socket, BlockingQueue<Command> commandQueue ) {
        this.socket = socket;
        this.commandQueue = commandQueue;

        responseQueue = new LinkedBlockingQueue<>();
        requestLimiter = new Semaphore( HardcodedParameters.MAX_OPEN_TCP_REQUESTS_PER_SOCKET );
    }

    private class TcpReader implements Runnable {

        @Override
        public void run() {
            while( true ) {
                // read data
                try {
                    commandQueue.put( null );
                    requestLimiter.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private class TcpWriter implements Runnable {
        private byte[] responseBuffer = new byte[ 300 ];

        @Override
        public void run() {
            try {
                socket.setTcpNoDelay( true );
                while( true ) {
                    Command response = responseQueue.take();
                    if( response instanceof BalanceResponse ) {
                        requestLimiter.release();
                        // write to socket
                    }

                    else { // assume it is stop
                        break;
                    }

                }
            } catch (SocketException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
