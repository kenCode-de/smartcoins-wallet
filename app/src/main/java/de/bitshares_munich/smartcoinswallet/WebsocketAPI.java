package de.bitshares_munich.smartcoinswallet;

import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by nelson on 11/10/16.
 */

public class WebsocketAPI {
    public static final String TAG = "WebsocketAPI";
    public static Thread thread;

    public static void sendData(final String payload) {
        thread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    asyncSendData(payload);
                } catch (IOException e) {
                    Log.e(TAG,"IOException. Msg: "+e.getMessage());
                }
            }
        });
        thread.start();
    }

    public static void asyncSendData(final String payload) throws IOException {
        String url = "wss://bitshares.openledger.info/ws";

        WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);

        // Create a WebSocket. The timeout value set above is used.
        WebSocket ws = factory.createSocket(url);
        ws.addListener(new com.neovisionaries.ws.client.WebSocketAdapter(){

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                Log.d(TAG, "onConnected. sending text");
//              websocket.sendText(payload);
                websocket.sendText("{\"id\":1, \"method\":\"call\", \"params\":[0,\"get_accounts\",[[\"1.2.0\"]]]}");
            }

            @Override
            public void onTextMessage(WebSocket websocket, String text) throws Exception {
                Log.d(TAG, "onTextMessage");
                Log.d(TAG, text);
            }

            @Override
            public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
                Log.e(TAG, "onMessageError");
            }

            @Override
            public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                Log.e(TAG, "onSendError");
            }

            @Override
            public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                Log.e(TAG, "onError");
            }

            @Override
            public void onDisconnected(WebSocket websocket, com.neovisionaries.ws.client.WebSocketFrame serverCloseFrame, com.neovisionaries.ws.client.WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                Log.d(TAG, "onDisconnected");
            }

            @Override
            public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.d(TAG,"onTextFrame");
                Log.d(TAG,"frame: "+frame.getPayloadText());
            }

            @Override
            public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.d(TAG,"onPingFrame");
            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                Log.d(TAG,"onBinaryMessage");
            }

            @Override
            public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
                Log.d(TAG,"onStateChanged. new state: "+newState.toString());
            }

            @Override
            public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
                Log.e(TAG,"onUnexpectedError");
                Log.e(TAG,"cause: "+cause.getMessage());
            }
        });
        try {
            // Connect to the server and perform an opening handshake.
            // This method blocks until the opening handshake is finished.
            ws.connect();
        } catch (com.neovisionaries.ws.client.OpeningHandshakeException e) {
            // A violation against the WebSocket protocol was detected
            // during the opening handshake.
            Log.d(TAG, "OpeningHandshakeException");
        } catch (com.neovisionaries.ws.client.WebSocketException e) {
            // Failed to establish a WebSocket connection.
            Log.d(TAG, "WebSocketException");
        }

    }
}
