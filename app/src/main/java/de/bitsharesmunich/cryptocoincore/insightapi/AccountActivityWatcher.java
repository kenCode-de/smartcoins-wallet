package de.bitsharesmunich.cryptocoincore.insightapi;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles all the calls for the Socket.IO of the insight api
 *
 * Only gets new transaction in real time for each address of an Account
 *
 */

public class AccountActivityWatcher {

    /**
     * The mAccount to be monitor
     */
    private final GeneralCoinAccount mAccount;
    /**
     * The list of address to monitor
     */
    private List<String> mWatchAddress = new ArrayList<>();
    /**
     * the Socket.IO
     */
    private Socket mSocket;
    /**
     * This app mContext, used to save on the DB
     */
    private final Context mContext;

    /**
     * Handles the address/transaction notification.
     * Then calls the GetTransactionData to get the info of the new transaction
     */
    private final Emitter.Listener onAddressTransaction = new Emitter.Listener() {
        @Override
        public void call(Object... os) {
            try {
                System.out.println("Receive accountActivtyWatcher " + os[0].toString() );
                String txid = ((JSONObject) os[0]).getString(InsightApiConstants.sTxTag);
                new GetTransactionData(txid, mAccount, mContext).start();
            } catch (JSONException ex) {
                Logger.getLogger(AccountActivityWatcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    /**
     * Handles the connect of the Socket.IO
     */
    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... os) {
            System.out.println("Connected to accountActivityWatcher");
            JSONArray array = new JSONArray();
            for(String addr : mWatchAddress) {
                array.put(addr);
            }
            mSocket.emit(InsightApiConstants.sSubscribeEmmit, InsightApiConstants.sChangeAddressRoom, array);
        }
    };

    /**
     * Handles the disconnect of the Socket.Io
     * Reconcects the mSocket
     */
    private final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... os) {
            System.out.println("Disconnected to accountActivityWatcher");
            mSocket.connect();
        }
    };

    /**
     * Error handler, doesn't need reconnect, the mSocket.io do that by default
     */
    private final Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(Object... os) {
            System.out.println("Error to accountActivityWatcher ");
            for(Object ob : os) {
                System.out.println("accountActivityWatcher " + ob.toString());
            }
        }
    };

    /**
     * Basic constructor
     *
     * @param mAccount The mAccount to be monitor
     * @param mContext This app mContext
     */
    public AccountActivityWatcher(GeneralCoinAccount mAccount, Context mContext) {
        //String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(mAccount.getCoin()) + ":" + InsightApiConstants.getPort(mAccount.getCoin()) + "/"+InsightApiConstants.getRawPath(mAccount.getCoin())+"/mSocket.io/";
        String serverUrl = InsightApiConstants.sProtocolSocketIO + "://" + InsightApiConstants.getAddress(mAccount.getCoin()) + ":" + InsightApiConstants.getPort(mAccount.getCoin()) + "/";
        this.mAccount = mAccount;
        this.mContext = mContext;
        System.out.println("accountActivityWatcher " + serverUrl);
        try {
            IO.Options opts = new IO.Options();
            System.out.println("accountActivityWatcher default path " + opts.path);
            this.mSocket = IO.socket(serverUrl);
            this.mSocket.on(Socket.EVENT_CONNECT, onConnect);
            this.mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            this.mSocket.on(Socket.EVENT_ERROR, onError);
            this.mSocket.on(Socket.EVENT_CONNECT_ERROR, onError);
            this.mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onError);
            this.mSocket.on(InsightApiConstants.sChangeAddressRoom, onAddressTransaction);
        } catch (URISyntaxException e) {
            //TODO change exception handler
            e.printStackTrace();
        }
    }

    /**
     * Add an address to be monitored, it can be used after the connect
     * @param address The String address to monitor
     */
    public void addAddress(String address) {
        mWatchAddress.add(address);
        if (this.mSocket.connected()) {
            mSocket.emit(InsightApiConstants.sSubscribeEmmit, InsightApiConstants.sChangeAddressRoom, new String[]{address});
        }
    }

    /**
     * Connects the Socket
     */
    public void connect() {
        //TODO change to use log
        System.out.println("accountActivityWatcher connecting");
        try{
            this.mSocket.connect();
        }catch(Exception e){
            //TODO change exception handler
            System.out.println("accountActivityWatcher exception " + e.getMessage());
        }
    }

    /**
     * Disconnects the Socket
     */
    public void disconnect() {this.mSocket.disconnect();}
}
