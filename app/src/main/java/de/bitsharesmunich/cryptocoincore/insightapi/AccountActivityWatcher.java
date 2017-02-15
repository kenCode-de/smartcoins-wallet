package de.bitsharesmunich.cryptocoincore.insightapi;

import android.content.Context;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;

/**
 * Created by henry on 12/02/2017.
 */

public class AccountActivityWatcher {

    private final GeneralCoinAccount account;
    private List<String> watchAddress = new ArrayList();
    private Socket socket;
    private final Context context;

    private final Emitter.Listener onAddressTransaction = new Emitter.Listener() {
        @Override
        public void call(Object... os) {
            try {
                System.out.println("New addr transaction received: " + ((JSONObject) os[0]).toString());
                String txi = ((JSONObject) os[0]).getString(InsightApiConstants.txTag);
                new GetTransactionData(txi, account,context).start();
            } catch (JSONException ex) {
                Logger.getLogger(AccountActivityWatcher.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    };

    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... os) {
            socket.emit(InsightApiConstants.subscribeEmmit, InsightApiConstants.changeAddressRoom, watchAddress.toArray());
        }
    };

    private final Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... os) {
        }
    };

    public AccountActivityWatcher(GeneralCoinAccount account, Context context) throws URISyntaxException {
        this.socket = IO.socket(InsightApiConstants.protocol + "://" + InsightApiConstants.getAddress(account.getCoin()) + ":" + InsightApiConstants.getPort(account.getCoin())+"/");
        this.socket.on(Socket.EVENT_CONNECT, onConnect);
        this.socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        this.socket.on(InsightApiConstants.changeAddressRoom, onAddressTransaction);
        this.account = account;
        this.context = context;
    }

    public void addAddress(String address) {
        watchAddress.add(address);
        if (this.socket.connected()) {
            socket.emit(InsightApiConstants.subscribeEmmit, InsightApiConstants.changeAddressRoom, new String[]{address});
        }
    }

    public void connect() {
        this.socket.connect();
    }

    public void disconnect() {
        this.socket.disconnect();
    }
}
