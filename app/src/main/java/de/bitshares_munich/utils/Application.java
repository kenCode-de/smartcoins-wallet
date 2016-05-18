package de.bitshares_munich.utils;

import android.content.Context;
import android.util.Log;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import de.bitshares_munich.Interfaces.IAccount;
import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by qasim on 5/9/16.
 */
public class Application extends android.app.Application {

    public static WebSocket webSocketG;
    public static int socketCounter;
    public static Context context;
    static IAccount iAccount;
    public static String blockHead="";

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.setDebug(true);
        context = getApplicationContext();
        blockHead="";
        webSocketConnection();


    }

    public void registerCallback(IAccount callbackClass) {
        iAccount = callbackClass;
    }

    public static void webSocketConnection() {
        iAccount = iAccount;
        AsyncHttpGet get = new AsyncHttpGet(context.getString(R.string.url_bitshares_openledger));
        get.setTimeout(1000000000);
        AsyncHttpClient.getDefaultInstance().websocket(get, null, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }

                Application.webSocketG = webSocket;
                sendInitialSocket(context);

            }

        });


    }

    public static void sendInitialSocket(final Context context) {
        socketCounter = 0;

        if (Application.webSocketG.isOpen()) {

            Application.webSocketG.send(context.getString(R.string.login_api));
            Application.webSocketG.setStringCallback(new WebSocket.StringCallback() {
                public void onStringAvailable(String s) {
                    if (s.contains("true")) {
                        Application.webSocketG.send(context.getString(R.string.database_indentifier));
                        Application.webSocketG.send(context.getString(R.string.network_broadcast_identifier));
                        Application.webSocketG.send(context.getString(R.string.history_identifier));

                        Application.webSocketG.send(context.getString(R.string.subscribe_callback));
                        socketCounter = 1;
                    } else if (socketCounter == 1) {
                        try {
                            //System.out.println("I got a string: " + s);
                            JSONObject jsonObject = new JSONObject(s);
                            int id = 0;
                            if (jsonObject.has("id")) {
                                id = jsonObject.getInt("id");
                            }
                            if (id == 2) {
                                Helper.storeIntSharePref(context, context.getString(R.string.sharePref_database), jsonObject.getInt("result"));
                            } else if (id == 3) {
                                Helper.storeIntSharePref(context, context.getString(R.string.sharePref_network_broadcast), jsonObject.getInt("result"));
                            } else if (id == 4) {
                                Helper.storeIntSharePref(context,
                                        context.getString(R.string.sharePref_history), jsonObject.getInt("result"));
                            } else if (s.contains(context.getString(R.string.head_block_number))) {
                                headBlockNumber(s);
                            } else if (id == 6) {
                                if (iAccount != null) {
                                    iAccount.checkAccount(jsonObject);
                                }
                            }
                        } catch (JSONException e) {

                        }

                    }

                }
            });

        }
    }

    private static String headBlockNumber(String json) {

        int start = json.lastIndexOf(context.getString(R.string.head_block_number));
        int length = 19;
        start = start + length;
        int end = start;
        for (; end < json.length(); end++) {
            if (json.substring(end, end + 1).equals(",")) {
                break;
            }
        }
        return blockHead = "block# "+json.substring(start, end);
    }

}
