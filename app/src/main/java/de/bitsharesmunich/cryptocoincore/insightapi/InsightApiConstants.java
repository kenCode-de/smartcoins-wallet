package de.bitsharesmunich.cryptocoincore.insightapi;

import de.bitsharesmunich.cryptocoincore.base.Coin;

import java.util.HashMap;

/**
 * Class holds all constant related to the Insight Api
 *
 */

abstract class InsightApiConstants {
    /**
     * Protocol of the insight api calls
     */
    static final String sProtocol = "https";
    /**
     * Protocol of the insigiht api Socket.IO connection
     */
    static final String sProtocolSocketIO = "http";
    /**
     * Contains each url information for each coin
     */
    private static final HashMap<Coin,AddressPort> sServerAddressPort = new HashMap<>();
    /**
     * Insight api Socket.IO new transaction by address notification
     */
    static final String sChangeAddressRoom = "bitcoind/addresstxid";
    /**
     * Socket.io subscribe command
     */
    static final String sSubscribeEmmit = "subscribe";
    /**
     * Tag used in the response of the address transaction notification
     */
    static final String sTxTag = "txid";

    /**
     * Wait time to check for confirmations
     */
    static long sWaitTime = (30 * 1000); //wait 1 minute

    //Filled the serverAddressPort maps with static data
     static{
        //serverAddressPort.put(Coin.BITCOIN,new AddressPort("fr.blockpay.ch",3002,"node/btc/testnet","insight-api"));
        sServerAddressPort.put(Coin.BITCOIN,new AddressPort("fr.blockpay.ch",3003,"node/btc/testnet","insight-api"));
        //serverAddressPort.put(Coin.BITCOIN_TEST,new AddressPort("fr.blockpay.ch",3003,"node/btc/testnet","insight-api"));
        sServerAddressPort.put(Coin.LITECOIN,new AddressPort("fr.blockpay.ch",3009,"node/ltc","insight-lite-api"));
        sServerAddressPort.put(Coin.DASH,new AddressPort("fr.blockpay.ch",3005,"node/dash","insight-api-dash"));
        sServerAddressPort.put(Coin.DOGECOIN,new AddressPort("fr.blockpay.ch",3006,"node/dogecoin","insight-api"));
    }

    /**
     * Get the insight api server address
     * @param coin The coin of the API to find
     * @return The String address of the server, can be a name or the IP
     */
    static String getAddress(Coin coin){
        return sServerAddressPort.get(coin).mServerAddress;
    }

    /**
     * Get the port of the server Insight API
     * @param coin The coin of the API to find
     * @return The server number port
     */
    static int getPort(Coin coin){
        return sServerAddressPort.get(coin).mPort;
    }

    /**
     * Get the url path of the server Insight API
     * @param coin The coin of the API to find
     * @return The path of the Insight API
     */
    static String getPath(Coin coin){
        return sServerAddressPort.get(coin).mPath + "/" + sServerAddressPort.get(coin).mInsightPath;
    }

    /**
     * Contains all the url info neccessary to connects to the insight api
     */
    private static class AddressPort{
        /**
         * The server address
         */
        final String mServerAddress;
        /**
         * The port used in the Socket.io
         */
        final int mPort;
        /**
         * The path of the coin server
         */
        final String mPath;
        /**
         * The path of the insight api
         */
        final String mInsightPath;


        /**
         * Constructor
         * @param serverAddress The server address of the Insight API
         * @param port the port number of the Insight API
         * @param path the path to the Insight API before the last /
         * @param insightPath the path after the last / of the Insight API
         */
        AddressPort(String serverAddress, int port, String path, String insightPath) {
            this.mServerAddress = serverAddress;
            this.mPort = port;
            this.mPath = path;
            this.mInsightPath = insightPath;
        }
    }
}
