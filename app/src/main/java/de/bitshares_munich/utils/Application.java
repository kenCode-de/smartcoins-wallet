package de.bitshares_munich.utils;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import butterknife.ButterKnife;

/**
 * Created by qasim on 5/9/16.
 */
public class Application extends android.app.Application {

    public static WebSocket webSocketG;

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.setDebug(true);
        webSocketConnection();
    }

    public static void webSocketConnection() {

        AsyncHttpClient.getDefaultInstance().websocket("wss://bitshares.openledger.info/ws", "wss", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                webSocketG = webSocket;
            }

        });
    }
}
