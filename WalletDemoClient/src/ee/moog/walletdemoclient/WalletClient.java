package ee.moog.walletdemoclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 */
public class WalletClient {
    private static String serverAddress = "127.0.0.1";
    private static int serverPort = 6789;

    public static void main(String [ ] args) {
        out("Wallet memorydb demo client");

        try {
            RandomRequest randomRequest = new RandomRequest();

            out( "Connecting to " + serverAddress + ":" + serverPort );
            Socket socket = new Socket( serverAddress, serverPort );

            WalletClientTcp client = new WalletClientTcp( socket, randomRequest );
            client.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            out("Client running");
            out("Type \"help\" for commands");
            while( true ) {
                String cmd = br.readLine();
                switch( cmd ) {
                    case "s":
                        int sent = client.sent.get();
                        int received = client.received.get();
                        out( "Sent: " + sent + ", Received: " + received );
                        break;
                    case "help":
                        out( "Commands are:" );
                        out( "    \"s\" - display client statistics" );
                        out( "    \"exit\" - terminate program" );
                        out( "    \"help\" - display this so called help" );
                        break;
                    case "exit":
                        client.stop();
                        return;
                    default:
                        out( "Unknown command, type \"help\" for command list");
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void out( String o ) {
        System.out.println( o );
    }
}
