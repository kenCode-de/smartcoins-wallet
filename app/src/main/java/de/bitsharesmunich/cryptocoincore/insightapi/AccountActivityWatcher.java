package de.bitsharesmunich.cryptocoincore.insightapi;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Handles all the calls for the Socket.IO of the insight api
 *
 * Only gets new transaction in real time for each address of an account
 *
 */

public class AccountActivityWatcher {

    /**
     * The account to be monitor
     */
    private final GeneralCoinAccount account;
    /**
     * The list of address to monitor
     */
    private List<String> watchAddress = new ArrayList<>();
    /**
     * the Socket.IO
     */
    private Socket socket;
    /**
     * This app context, used to save on the DB
     */
    private final Context context;

    /**
     * Handles the address/transaction notification.
     * Then calls the GetTransactionData to get the info of the new transaction
     */
    private final Emitter.Listener onAddressTransaction = new Emitter.Listener() {
        @Override
        public void call(Object... os) {
            try {
                System.out.println("Receive accountActivtyWatcher " + os[0].toString() );
                String txid = ((JSONObject) os[0]).getString(InsightApiConstants.txTag);
                new GetTransactionData(txid, account, context).start();
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
            for(String addr : watchAddress) {
                array.put(addr);
            }
            socket.emit(InsightApiConstants.subscribeEmmit, InsightApiConstants.changeAddressRoom, array);
        }
    };

    /**
     * Handles the disconnect of the Socket.Io
     * Reconcects the socket
     */
    private final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... os) {
            System.out.println("Disconnected to accountActivityWatcher");
            socket.connect();
        }
    };

    /**
     * Error handler, doesn't need reconnect, the socket.io do that by default
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
     * @param account The account to be monitor
     * @param context This app context
     */
    public AccountActivityWatcher(GeneralCoinAccount account, Context context) {
        //String serverUrl = InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(account.getCoin()) + ":" + InsightApiConstants.getPort(account.getCoin()) + "/"+InsightApiConstants.getRawPath(account.getCoin())+"/socket.io/";
        String serverUrl = InsightApiConstants.protocolSocketIO + "://" + InsightApiConstants.getAddress(account.getCoin()) + ":" + InsightApiConstants.getPort(account.getCoin()) + "/";
        this.account = account;
        this.context = context;
        System.out.println("accountActivityWatcher " + serverUrl);
        try {
            this.socket = IO.socket(serverUrl);
            this.socket.on(Socket.EVENT_CONNECT, onConnect);
            this.socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            this.socket.on(Socket.EVENT_ERROR, onError);
            this.socket.on(Socket.EVENT_CONNECT_ERROR, onError);
            this.socket.on(Socket.EVENT_CONNECT_TIMEOUT, onError);
            this.socket.on(InsightApiConstants.changeAddressRoom, onAddressTransaction);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add an address to be monitored, it can be used after the connect
     * @param address The String address to monitor
     */
    public void addAddress(String address) {
        watchAddress.add(address);
        if (this.socket.connected()) {
            socket.emit(InsightApiConstants.subscribeEmmit, InsightApiConstants.changeAddressRoom, new String[]{address});
        }
    }

    /**
     * Connects the Socket
     */
    public void connect() {
        System.out.println("accountActivityWatcher connecting");
        try{
            this.socket.connect();
        }catch(Exception e){
            System.out.println("accountActivityWatcher exception " + e.getMessage());
        }
    }

    /**
     * Disconnects the Socket
     */
    public void disconnect() {this.socket.disconnect();}
}
