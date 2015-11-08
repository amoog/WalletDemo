package ee.moog.walletdemoclient;

import ee.moog.walletprotocol.WalletRequest;
import ee.moog.walletprotocol.WalletResponse;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 */
public class RandomRequest {
    public int clientCount = 10000;
    public int maxChange = 10000;

    private int reqNo = 0;
    public WalletRequest generateRequest() {
        int current = ThreadLocalRandom.current().nextInt( clientCount );
        String userName = Integer.toString( current );
        int amount = ThreadLocalRandom.current().nextInt( maxChange ) - maxChange / 2;
        WalletRequest request = new WalletRequest( userName, Integer.toString( reqNo++ ), amount );

        return request;
    }

    public void checkResponse(WalletResponse response ) {

    }
}
