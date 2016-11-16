package de.bitshares_munich.smartcoinswallet;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;

/**
 * Created by nelson on 11/10/16.
 */

public class WebsocketAPI {
    public static final String TAG = "WebsocketAPI";
    public static Thread thread;

    public static void sendData(final WebSocket webSocket) {
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    webSocket.connect();
                } catch (WebSocketException e) {
                    Log.e(TAG,"WebSocketException. Msg: "+e.getMessage());
                }
            }
        });
        thread.start();
    }
}
