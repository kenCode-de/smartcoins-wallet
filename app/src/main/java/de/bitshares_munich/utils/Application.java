package de.bitshares_munich.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServerRequestImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.Interfaces.BalancesDelegate;
import de.bitshares_munich.Interfaces.IAccount;
import de.bitshares_munich.Interfaces.IAccountObject;
import de.bitshares_munich.Interfaces.IAssetObject;
import de.bitshares_munich.Interfaces.IExchangeRate;
import de.bitshares_munich.Interfaces.ITransactionObject;
import de.bitshares_munich.smartcoinswallet.BalancesLoad;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.SendScreen;

/**
 * Created by qasim on 5/9/16.
 */
public class Application extends android.app.Application {

    public static WebSocket webSocketG;
    public static int socketCounter;
    public static Context context;
    static IAccount iAccount;
    static IExchangeRate iExchangeRate;
    static BalancesDelegate iBalancesDelegate;
    static AssetDelegate iAssetDelegate;
    static ITransactionObject iTransactionObject;
    static IAccountObject iAccountObject;
    static IAssetObject iAssetObject;
    public static String blockHead="";
    private static Activity currentActivity;

    public static void setCurrentActivity(Activity _activity)
    {
        Application.currentActivity = _activity;
    }

    public static Activity getCurrentActivity()
    {
        return Application.currentActivity;
    }

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
    public void registerExchangeRateCallback(IExchangeRate callbackClass) {
        iExchangeRate = callbackClass;
    }
    public void registerBalancesDelegate(BalancesDelegate callbackClass) {
        iBalancesDelegate = callbackClass;
    }
    public void registerAssetDelegate(AssetDelegate callbackClass) {
        iAssetDelegate = callbackClass;
    }
    public void registerTransactionObject(ITransactionObject callbackClass) {
        iTransactionObject = callbackClass;
    }
    public void registerAccountObjectCallback(IAccountObject callbackClass) {
        iAccountObject = callbackClass;
    }
    public void registerAssetObjectCallback(IAssetObject callbackClass) {
        iAssetObject = callbackClass;
    }

    public static void webSocketConnection() {
        iAccount = iAccount;
        final AsyncHttpGet get = new AsyncHttpGet(context.getString(R.string.url_bitshares_openledger));
        get.setTimeout(1*60*60*1000);//000000);

        //System.setProperty("https.protocols", "TLSv1");
        AsyncHttpClient.getDefaultInstance().websocket(get, null, new AsyncHttpClient.WebSocketConnectCallback() {


            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    Log.d("exception websocket",ex.toString());
                    final Exception myEx = ex;
                    Log.d("exception websocket",myEx.toString());
                    try {
                        if (ex.getMessage().contains("handshake_failure")) {
                            Log.d("exception3 websocket", "inside");
                            if (currentActivity != null) {
                                Log.d("exception websocket", "inside again");
                                currentActivity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(context, "Your system does not supports new SSL ciphering. Error : " + myEx.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            //webSocketConnection();
                            Log.d("exception websocket", "getting out");
                        } else {
                            Log.d("exception websocket", "webbb socket");
                            if (webSocket != null) {
                                Log.d("exception websocket", "inside webbb socket");
                                if (webSocket.isOpen()) {
                                    Log.d("exception websocket", "is open");
                                    webSocket.close();
                                    Log.d("exception websocket", "closed");
                                }
                            }
                            webSocketConnection();
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d("exception websocket", e.getMessage());
                    }
                    //ex.printStackTrace();
                    return;
                }
                Log.d("exception websocket","before making application.websocketg");
                Application.webSocketG = webSocket;
                Log.d("exception websocket","sending initial socket");
                sendInitialSocket(context);
                Log.d("exception websocket","completed");
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
                            } else if (id == 7) {
                                JSONArray jsonArray = (JSONArray) jsonObject.get("result");
                                JSONObject obj = new JSONObject();
                                if (jsonArray.length() != 0) {
                                    obj = (JSONObject) jsonArray.get(1);
                                }
                                iExchangeRate.callback_exchange_rate(obj);
                            }else if (id == 8) {
                                if (iBalancesDelegate != null) {
                                    iBalancesDelegate.OnUpdate(s,id);
                                }
                            }else if (id == 12) {
                                if (iTransactionObject != null) {
                                    iTransactionObject.checkTransactionObject(jsonObject);
                                }
                            }else if (id == 13) {
                                if (iAccountObject != null) {
                                    iAccountObject.accountObjectCallback(jsonObject);
                                }
                            }else if (id == 14) {
                                if (iAssetObject != null) {
                                    iAssetObject.assetObjectCallback(jsonObject);
                                }
                            }
                            else if (id == 9) {
                                if (iBalancesDelegate != null) {
                                    iBalancesDelegate.OnUpdate(s,id);
                                }
                            } else if (id == 10) {
                                if (iBalancesDelegate != null) {
                                    iBalancesDelegate.OnUpdate(s,id);
                                }
                            }
                            else if (id == 11) {
                                if (iBalancesDelegate != null) {
                                    iBalancesDelegate.OnUpdate(s,id);
                                }
                            }
                            else if (id == 15) {
                                if (iAssetDelegate != null) {
                                    iAssetDelegate.getLifetime(s,id);
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
