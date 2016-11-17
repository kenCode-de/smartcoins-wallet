package de.bitshares_munich.smartcoinswallet;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketListener;

import java.io.IOException;

import de.bitshares_munich.utils.Application;

/**
 * Created by nelson on 11/17/16.
 */
public class WebsocketWorkerThread extends Thread {
    private final String TAG = this.getClass().getName();
    private WebSocket mWebSocket;
    private WebSocketListener mWebSocketListner;

    public WebsocketWorkerThread(WebSocketListener webSocketListener){
        mWebSocketListner = webSocketListener;

        WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
        try {
            mWebSocket = factory.createSocket(Application.urlsSocketConnection[0]);
            mWebSocket.addListener(webSocketListener);
        } catch (IOException e) {
            Log.e(TAG, "IOException. Msg: "+e.getMessage());
        }
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        try {
            mWebSocket.connect();
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }
}
