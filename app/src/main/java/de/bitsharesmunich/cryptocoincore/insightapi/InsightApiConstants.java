package de.bitsharesmunich.cryptocoincore.insightapi;

import java.util.HashMap;

import de.bitsharesmunich.cryptocoincore.base.Coin;

/**
 * Created by henry on 13/02/2017.
 */

abstract class InsightApiConstants {
    static final String protocol = "http";
    static final long amountMultiplier = 100000000;
    private static final HashMap<Coin,AddressPort> serverAddressPort = new HashMap();
    static final String changeAddressRoom = "bitcoind/addresstxid";
    static final String subscribeEmmit = "subscribe";
    static final String txTag = "txi";

    static int MIN_CONFIRM = 6; //minimun number of confirmations of a transaction
    static long WAIT_TIME = (5 *60 *1000); //wait 5 minutes

    static{
        serverAddressPort.put(Coin.BITCOIN,new AddressPort("fr.blockpay.ch",3003));
        serverAddressPort.put(Coin.BITCOIN_TEST,new AddressPort("fr.blockpay.ch",3003));
    }

    static String getAddress(Coin coin){
        return serverAddressPort.get(coin).serverAddress;
    }

    static int getPort(Coin coin){
        return serverAddressPort.get(coin).port;
    }

    private static class AddressPort{
        final String serverAddress;;
        final int port;

        AddressPort(String serverAddress, int port) {
            this.serverAddress = serverAddress;
            this.port = port;
        }
    }
}
