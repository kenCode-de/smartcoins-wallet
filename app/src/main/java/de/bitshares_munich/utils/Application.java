package de.bitshares_munich.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;

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
import de.bitshares_munich.Interfaces.IRelativeHistory;
import de.bitshares_munich.Interfaces.ITransactionObject;
import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by qasim on 5/9/16.
 */
public class Application extends android.app.Application {

    public static WebSocket webSocketG;
    public static Context context;
    static IAccount iAccount;
    static IExchangeRate iExchangeRate;
    static BalancesDelegate iBalancesDelegate;
    static AssetDelegate iAssetDelegate;
    static ITransactionObject iTransactionObject;
    static IAccountObject iAccountObject;
    static IAssetObject iAssetObject;
    static IRelativeHistory iRelativeHistory;
    public static String blockHead = "";
    private static Activity currentActivity;

    public static String urlsSocketConnection[] = {"https://bitshares.openledger.info/ws",
            "https://bitshares.dacplay.org:8089/ws",
            "https://dele-puppy.com/ws",
            "https://valen-tin.fr:8090"};

    public static int counter = 0;

    public static String monitorAccountId;

    private static Handler warningHandler = new Handler();


    public static void setCurrentActivity(Activity _activity) {
        Application.currentActivity = _activity;
    }

    public static Activity getCurrentActivity() {
        return Application.currentActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.setDebug(true);
        context = getApplicationContext();
        //showDialogPin();
        blockHead = "";
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

    public void registerRelativeHistoryCallback(IRelativeHistory callbackClass) {
        iRelativeHistory = callbackClass;
    }

    private static void showWarningMessage(final String myEx) {
        if (getCurrentActivity() != null) {
            Log.d("exception websocket", "inside again");
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Your system does not supports new SSL ciphering. Error : " + myEx, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public static void webSocketConnection() {
        iAccount = iAccount;
        final AsyncHttpGet get = new AsyncHttpGet(urlsSocketConnection[counter]);
        get.setTimeout(5 * 1000);//000000);
        if (getCurrentActivity() != null) {
            Log.d("exception websocket", "inside again");
            getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Connecting to url " + urlsSocketConnection[counter], Toast.LENGTH_LONG).show();
                }
            });
        }
        AsyncHttpClient.getDefaultInstance().websocket(get, null, new AsyncHttpClient.WebSocketConnectCallback() {

            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (!webSocket.isOpen()) {
                    counter++;
                    if (counter > 3) {
                        counter = 0;
                    }
                }
                if (ex != null) {
                    Log.d("exception websocket", ex.toString());
                    final Exception myEx = ex;
                    Log.d("exception websocket", myEx.toString());
                    try {
                        final String exMessage = ex.getMessage();
                        if (exMessage != null && !exMessage.isEmpty() && exMessage.contains("handshake_failure")) {
                            Log.d("exception3 websocket", "inside");

                            Runnable updateTask = new Runnable() {
                                @Override
                                public void run() {
                                    showWarningMessage(exMessage);
                                }
                            };
                            warningHandler.postDelayed(updateTask, 1000);
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
                    } catch (Exception e) {
                        Log.d("exception websocket", e.getMessage());
                    }
                    //ex.printStackTrace();
                    return;
                }
                webSocket.setClosedCallback(new CompletedCallback() {
                    public void onCompleted(Exception ex) {
                        counter++;
                        if (counter > 3) {
                            counter = 0;
                        }
                        // webSocketConnection();
                    }
                });

                webSocket.setEndCallback(new CompletedCallback() {
                    public void onCompleted(Exception ex) {

                    }
                });
                Log.d("exception websocket", "before making application.websocketg");
                Application.webSocketG = webSocket;
                Log.d("exception websocket", "sending initial socket");
                sendInitialSocket(context);
                Log.d("exception websocket", "completed");
            }


        });

    }

    public static void sendInitialSocket(final Context context) {

        if (Application.webSocketG.isOpen()) {

            Application.webSocketG.send(context.getString(R.string.login_api));
            Application.webSocketG.setStringCallback(new WebSocket.StringCallback() {
                public void onStringAvailable(String s) {

                    try {
                        //System.out.println("I got a string: " + s);
                        JSONObject jsonObject = new JSONObject(s);

                        if (jsonObject.has("id")) {
                            int id = jsonObject.getInt("id");

                            if (id == 1) {
                                if (s.contains("true")) {
                                    Application.webSocketG.send(context.getString(R.string.database_indentifier));
                                } else {
                                    Application.webSocketG.send(context.getString(R.string.login_api));
                                }
                            } else if (id == 2) {
                                Helper.storeIntSharePref(context, context.getString(R.string.sharePref_database), jsonObject.getInt("result"));
                                Application.webSocketG.send(context.getString(R.string.network_broadcast_identifier));
                            } else if (id == 3) {
                                Helper.storeIntSharePref(context, context.getString(R.string.sharePref_network_broadcast), jsonObject.getInt("result"));
                                Application.webSocketG.send(context.getString(R.string.history_identifier));
                            } else if (id == 4) {
                                Helper.storeIntSharePref(context, context.getString(R.string.sharePref_history), jsonObject.getInt("result"));
                                Application.webSocketG.send(context.getString(R.string.subscribe_callback));
                                //Application.webSocketG.send(context.getString(R.string.subscribe_callback_full_account));
                            } else if (id == 6) {
                                if (iAccount != null) {
                                    iAccount.checkAccount(jsonObject);
                                }
                            } else if (id == 100 || id == 200) {
                                JSONArray jsonArray = (JSONArray) jsonObject.get("result");
                                JSONObject obj = new JSONObject();
                                if (jsonArray.length() != 0) {
                                    obj = (JSONObject) jsonArray.get(1);
                                }
                                iExchangeRate.callback_exchange_rate(obj, id);
                            } else if (id == 8) {
                                if (iBalancesDelegate != null) {
                                    iBalancesDelegate.OnUpdate(s, id);
                                }
                            } else if (id == 12) {
                                if (iTransactionObject != null) {
                                    iTransactionObject.checkTransactionObject(jsonObject);
                                }
                            } else if (id == 13) {
                                if (iAccountObject != null) {
                                    iAccountObject.accountObjectCallback(jsonObject);
                                }
                            } else if (id == 14) {
                                if (iAssetObject != null) {
                                    iAssetObject.assetObjectCallback(jsonObject);
                                }
                            } else if (id == 9) {
                                if (iBalancesDelegate != null) {
                                    iBalancesDelegate.OnUpdate(s, id);
                                }
                            } else if (id == 10) {
                                if (iBalancesDelegate != null) {
                                    iBalancesDelegate.OnUpdate(s, id);
                                }
                            } else if (id == 11) {
                                if (iBalancesDelegate != null) {
                                    iBalancesDelegate.OnUpdate(s, id);
                                }
                            } else if (id == 99) {
                                if (iBalancesDelegate != null) {
                                    SupportMethods.testing("assests", 99, "account_name");
                                    iBalancesDelegate.OnUpdate(s, id);
                                }
                            } else if (id == 999) {
                                if (iBalancesDelegate != null) {
                                    SupportMethods.testing("assests", 999, "account_name");
                                    iBalancesDelegate.OnUpdate(s, id);
                                }
                            } else if (id == 15) {
                                if (iAssetDelegate != null) {
                                    iAssetDelegate.getLifetime(s, id);
                                }
                            } else if (id == 160 || id == 161) {
                                if (iRelativeHistory != null) {
                                    iRelativeHistory.relativeHistoryCallback(jsonObject);
                                }
                            } else if (id == 17) {
                                Log.d("account_update", jsonObject.toString());
                            }
                        } else if (jsonObject.has("method")) {
                            if (jsonObject.getString("method").equals("notice")) {
                                if (jsonObject.has("params")) {
                                    int id = jsonObject.getJSONArray("params").getInt(0);
                                    JSONArray values = jsonObject.getJSONArray("params").getJSONArray(1);

                                    if (id == 7) {
                                        headBlockNumber(values.toString());
                                        if (monitorAccountId != null && !monitorAccountId.isEmpty() && values.toString().contains(monitorAccountId)) {
                                            if (iAssetDelegate != null) {
                                                iAssetDelegate.loadAll();
                                            }
                                            Log.d("Notice Update", values.toString());
                                        }
                                        //headBlockNumber(s);
                                    } else {
                                        Log.d("other notice", values.toString());
                                    }

                                }
                            }
                        }


                    } catch (JSONException e) {

                    }
                }
            });


        }
    }

    private static String headBlockNumber(String json) {

        String result = "";
        if (json.contains(context.getString(R.string.head_block_number))) {
            int start = json.lastIndexOf(context.getString(R.string.head_block_number));
            int length = 19;
            start = start + length;
            int end = start;
            for (; end < json.length(); end++) {
                if (json.substring(end, end + 1).equals(",")) {
                    break;
                }
            }
            result = "block# " + json.substring(start, end);
        } else {
            result = Application.blockHead;
        }

        return blockHead = result;
    }
}

