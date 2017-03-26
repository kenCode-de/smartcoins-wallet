package de.bitsharesmunich.cryptocoincore.insightapi;

import java.util.HashMap;

import de.bitsharesmunich.cryptocoincore.base.Coin;

/**
 * Class holds all constant related to the Insight Api
 *
 */

abstract class InsightApiConstants {
    /**
     * Protocol of the insight api calls
     */
    static final String protocol = "https";
    /**
     * Protocol of the insigiht api Socket.IO connection
     */
    static final String protocolSocketIO = "http";
    /**
     * Contains each url information for each coin
     */
    private static final HashMap<Coin,AddressPort> serverAddressPort = new HashMap<>();
    /**
     * Insight api Socket.IO new transaction by address notification
     */
    static final String changeAddressRoom = "bitcoind/addresstxid";
    /**
     * Socket.io subscribe command
     */
    static final String subscribeEmmit = "subscribe";
    /**
     * Tag used in the response of the address transaction notification
     */
    static final String txTag = "txid";

    /**
     * Wait time to check for confirmations
     */
    static long WAIT_TIME = (30 * 1000); //wait 1 minute

    /**
     * Filled the serverAddressPort maps with static data
     */
    static{
        //serverAddressPort.put(Coin.BITCOIN,new AddressPort("fr.blockpay.ch",3002,"node/btc/testnet","insight-api"));
        serverAddressPort.put(Coin.BITCOIN,new AddressPort("fr.blockpay.ch",3003,"node/btc/testnet","insight-api"));
        //serverAddressPort.put(Coin.BITCOIN_TEST,new AddressPort("fr.blockpay.ch",3003,"node/btc/testnet","insight-api"));
        serverAddressPort.put(Coin.LITECOIN,new AddressPort("fr.blockpay.ch",3009,"node/ltc","insight-lite-api"));
        serverAddressPort.put(Coin.DASH,new AddressPort("fr.blockpay.ch",3005,"node/dash","insight-api-dash"));
        serverAddressPort.put(Coin.DOGECOIN,new AddressPort("fr.blockpay.ch",3006,"node/dogecoin","insight-api"));
    }

    static String getAddress(Coin coin){
        return serverAddressPort.get(coin).serverAddress;
    }

    static int getPort(Coin coin){
        return serverAddressPort.get(coin).port;
    }

    static String getPath(Coin coin){
        return serverAddressPort.get(coin).path + "/" + serverAddressPort.get(coin).insightPath;
    }

    /**
     * Contains all the url info neccessary to connects to the insight api
     */
    private static class AddressPort{
        /**
         * The server address
         */
        final String serverAddress;
        /**
         * The port used in the Socket.io
         */
        final int port;
        /**
         * The path of the coin server
         */
        final String path;
        /**
         * The path of the insight api
         */
        final String insightPath;

        AddressPort(String serverAddress, int port, String path, String insightPath) {
            this.serverAddress = serverAddress;
            this.port = port;
            this.path = path;
            this.insightPath = insightPath;
        }
    }
}
