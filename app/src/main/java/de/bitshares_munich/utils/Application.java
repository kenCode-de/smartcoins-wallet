package de.bitshares_munich.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import de.bitshares_munich.autobahn.WebSocketConnection;
import de.bitshares_munich.autobahn.WebSocketException;
import de.bitshares_munich.interfaces.AssetDelegate;
import de.bitshares_munich.interfaces.IAccount;
import de.bitshares_munich.interfaces.IAccountID;
import de.bitshares_munich.interfaces.IAccountObject;
import de.bitshares_munich.interfaces.IAssetObject;
import de.bitshares_munich.interfaces.IBalancesDelegate;
import de.bitshares_munich.interfaces.IExchangeRate;
import de.bitshares_munich.interfaces.IRelativeHistory;
import de.bitshares_munich.interfaces.ITransactionObject;
import de.bitshares_munich.smartcoinswallet.Constants;
import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by qasim on 5/9/16.
 */
public class Application extends android.app.Application implements de.bitshares_munich.autobahn.WebSocket.WebSocketConnectionObserver,
        android.app.Application.ActivityLifecycleCallbacks {
    public static Context context;
    public static String blockHead = "";
    public static int refBlockNum;
    public static long refBlockPrefix;
    public static long blockTime;
    public static String urlsSocketConnection[] =
            {
// i think we need to set the 7 regions here via the user country selection. see job doc
// example: public_nodes_asia, public_nodes_africa, public_nodes_europe, etc

//                    "ws://api.devling.xyz:8088",
                      "wss://bitshares.openledger.info/ws",     // Openledger node
                      "ws://128.0.69.157:8090",                 // Henry node
                      "wss://de.blockpay.ch/node",              // German node
//                    "wss://bit.btsabc.org/ws",
//                    "wss://bts.transwiser.com/ws",
//                    "wss://freedom.bts123.cc:15138",
//                    "wss://okbtc.org:8089/ws",
//                    "wss://ratebts.com:8089",
//                    "wss://webber.tech:8089/ws",
//                    "wss://bitshares.dacplay.org:8089/ws",
//                    "https://dele-puppy.com/ws",
//                    "https://valen-tin.fr:8090"
            };
    public static String monitorAccountId;
    public static int nodeIndex = 0;
    public static Boolean isReady = false;
    static IAccount iAccount;
    static IExchangeRate iExchangeRate;
    static IBalancesDelegate iBalancesDelegate_transactionActivity;
    static IBalancesDelegate iBalancesDelegate_ereceiptActivity;
    static IBalancesDelegate iBalancesDelegate_assetsActivity;
    static AssetDelegate iAssetDelegate;
    static IAccountID iAccountID;
    static ITransactionObject iTransactionObject;
    static IAccountObject iAccountObject;
    static IAssetObject iAssetObject;
    static IRelativeHistory iRelativeHistory;
    static String connectedSocket;
    private static String TAG = "Application";
    private static Activity currentActivity;
    private static Handler warningHandler = new Handler();
    private static boolean mIsConnected = false;
    private static WebSocketConnection mConnection;
    private static URI mServerURI;
    /**
     * Constant used to specify how long will the app wait for another activity to go through its starting life
     * cycle events before create the lock pin screen.
     * <p>
     * This is used as a means to detect whether or not the user has left the app.
     */
    private final int LOCK_DELAY = 10000;
    Handler connectionHandler = new Handler();
    /* Attribute used to indicate that funds must be updated (primarilly used at SendScreen and BalanceFragments */
    private boolean mUpdateFunds = false;
    /* Internal attribute used to keep track of the application state */
    private boolean mAppLock = true;
    /**
     * Handler instance used to schedule tasks back to the main thread
     */
    private Handler mHandler;
    /**
     * Runnable that will set to lock the app with the PIN.
     */
    private Runnable lockApp = new Runnable() {
        @Override
        public void run() {
            mAppLock = true;
            Log.i(TAG, "App Locked");
        }
    };

    public static Activity getCurrentActivity() {
        return Application.currentActivity;
    }

    public static void setCurrentActivity(Activity _activity) {
        Application.currentActivity = _activity;
    }

    public static void registerCallback(IAccount callbackClass) {
        iAccount = callbackClass;
    }

    public static void registerCallbackIAccountID(IAccountID callbackClass) {
        iAccountID = callbackClass;
    }

    public static void registerExchangeRateCallback(IExchangeRate callbackClass) {
        iExchangeRate = callbackClass;
    }

    public static void registerBalancesDelegateTransaction(IBalancesDelegate callbackClass) {
        iBalancesDelegate_transactionActivity = callbackClass;
    }

    public static void registerBalancesDelegateEReceipt(IBalancesDelegate callbackClass) {
        iBalancesDelegate_ereceiptActivity = callbackClass;
    }

    public static void registerBalancesDelegateAssets(IBalancesDelegate callbackClass) {
        iBalancesDelegate_assetsActivity = callbackClass;
    }

    public static void registerAssetDelegate(AssetDelegate callbackClass) {
        iAssetDelegate = callbackClass;
    }

    public static void registerTransactionObject(ITransactionObject callbackClass) {
        iTransactionObject = callbackClass;
    }

    public static void registerAccountObjectCallback(IAccountObject callbackClass) {
        iAccountObject = callbackClass;
    }

    public static void registerAssetObjectCallback(IAssetObject callbackClass) {
        iAssetObject = callbackClass;
    }

    public static void registerRelativeHistoryCallback(IRelativeHistory callbackClass) {
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

    public static void stringTextRecievedWs(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);

            if (jsonObject.has("id")) {
                int id = jsonObject.getInt("id");
                Log.d(TAG, "Got response. id: " + id);
                if (id == 1) {
                    if (s.contains("true")) {
                        Application.send(context.getString(R.string.database_indentifier));
                    } else {
                        Application.send(context.getString(R.string.login_api));
                    }
                } else if (id == 2) {
                    Helper.storeIntSharePref(context, context.getString(R.string.sharePref_database), jsonObject.getInt("result"));
                    Application.send(context.getString(R.string.network_broadcast_identifier));
                } else if (id == 3) {
                    Helper.storeIntSharePref(context, context.getString(R.string.sharePref_network_broadcast), jsonObject.getInt("result"));
                    Application.send(context.getString(R.string.history_identifier));
                } else if (id == 4) {
                    Helper.storeIntSharePref(context, context.getString(R.string.sharePref_history), jsonObject.getInt("result"));
                    Application.send(context.getString(R.string.subscribe_callback));
                    isReady = true;
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
                    if (iBalancesDelegate_transactionActivity != null) {
                        iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                    }
                } else if (id == 20) {
                    if (iBalancesDelegate_transactionActivity != null) {
                        iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                    }
                } else if (id == 21) {
                    if (iBalancesDelegate_transactionActivity != null) {
                        iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                    }
                } else if (id == 22) {
                    if (iBalancesDelegate_transactionActivity != null) {
                        iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                    }
                } else if (id == 23) {
                    if (iBalancesDelegate_transactionActivity != null) {
                        iBalancesDelegate_transactionActivity.OnUpdate(s, id);
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
                    if (iBalancesDelegate_transactionActivity != null) {
                        iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                    }
                } else if (id == 10) {
                    if (iBalancesDelegate_transactionActivity != null) {
                        iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                    }
                } else if (id == 11) {
                    if (iBalancesDelegate_transactionActivity != null) {
                        iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                    }
                } else if (id == 99) {
                    if (iBalancesDelegate_assetsActivity != null) {
                        iBalancesDelegate_assetsActivity.OnUpdate(s, id);
                    }
                } else if (id == 999) {
                    if (iBalancesDelegate_assetsActivity != null) {
                        iBalancesDelegate_assetsActivity.OnUpdate(s, id);
                    }
                } else if (id == 15) {
                    if (iAssetDelegate != null) {
                        iAssetDelegate.getLifetime(s, id);
                    }
                } else if (id == 151) {
                    if (iAccountID != null) {
                        iAccountID.accountId(s);
                    }
                } else if (id == 160 || id == 161) {
                    if (iRelativeHistory != null) {
                        iRelativeHistory.relativeHistoryCallback(jsonObject);
                    }
                } else if (id == 17) {
                } else if (id == 18) {
                    if (iBalancesDelegate_ereceiptActivity != null) {
                        iBalancesDelegate_ereceiptActivity.OnUpdate(s, id);
                    }
                } else if (id == 19) {
                    if (iBalancesDelegate_ereceiptActivity != null) {
                        iBalancesDelegate_ereceiptActivity.OnUpdate(s, id);
                    }
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
                        } else {
                            Log.d("other notice", values.toString());
                        }

                    }
                }
            }


        } catch (JSONException e) {

        }
    }

    private static void sendInitialSocket(final Context context) {

        if (Application.mIsConnected) {
            Application.send(context.getString(R.string.login_api));
        }
    }

    private static String headBlockNumber(String json) {
        final String BLOCK_REFERENCE_ID = "head_block_id";
        final String HEAD_BLOCK_NUMBER = "head_block_number";
        final String TIME = "time";
        try {
            JSONArray array = new JSONArray(json);
            String rawBlockId = "";
            String blockNumber = "";
            if (array.length() == 1) {
                JSONArray subArray = array.getJSONArray(0);
                for (int i = 0; i < subArray.length(); i++) {
                    if (subArray.get(i) instanceof JSONObject) {
                        JSONObject element = (JSONObject) subArray.get(i);
                        if (element.has(BLOCK_REFERENCE_ID)) {
                            rawBlockId = element.getString(BLOCK_REFERENCE_ID);
                        }
                        if (element.has(HEAD_BLOCK_NUMBER)) {
                            blockNumber = element.getString(HEAD_BLOCK_NUMBER);
                        }
                        if (element.has(TIME)) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            try {
                                Date date = simpleDateFormat.parse(element.getString(TIME));
                                blockTime = date.getTime() / 1000;
                            } catch (ParseException e) {
                                Log.e(TAG, "ParseException while trying to parse time. time string: " + element.getString(TIME));
                            }
                        }
                    } else {
                        String element = (String) subArray.get(i);
                        Log.d(TAG, "Could not cast string: " + element);
                    }
                }
                if (rawBlockId.equals("")) {
                    return blockHead;
                }
                // Setting block number
                blockHead = "block# " + blockNumber;

                // Setting reference block number (lower 16 bits of block number)
                refBlockNum = Integer.valueOf(blockNumber) & 0xffff;

                // Setting block prefix
                String hashData = rawBlockId.substring(8, 16);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 8; i = i + 2) {
                    builder.append(hashData.substring(6 - i, 8 - i));
                }
                refBlockPrefix = Long.parseLong(builder.toString(), 16);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException at headBlockNumber");
        }
        return blockHead;
    }


    //WebSocketConnection

    public static void send(String message) {
        if (mIsConnected) {
            mConnection.sendTextMessage(message);
        }
    }

    public static void disconnect() {
        Log.i("internetBlockpay", "Disconnect");
        if (mConnection != null) {
            mConnection.disconnect();
        }
    }

    @NonNull
    public static Boolean isConnected() {
        return mConnection != null && (mConnection.isConnected());
    }

    public static void timeStamp() {

        Helper.storeBoolianSharePref(context, "account_can_create", false);
        setTimeStamp();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Helper.storeBoolianSharePref(context, "account_can_create", true);

            }
        }, 10 * 60000);
    }

    @NonNull
    public static Boolean accountCanCreate() {
        return Helper.fetchBoolianSharePref(context, "account_can_create");
    }

    static void setTimeStamp() {
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis();
        Helper.storeLongSharePref(context, "account_create_timestamp", time);
    }

    static void getTimeStamp() {
        try {
            Calendar c = Calendar.getInstance();
            long currentTime = c.getTimeInMillis();
            ;
            long oldTime = Helper.fetchLongSharePref(context, "account_create_timestamp");
            long diff = currentTime - oldTime;
            if (diff < TimeUnit.MINUTES.toMillis(10)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Helper.storeBoolianSharePref(context, "account_can_create", true);
                    }
                }, TimeUnit.MINUTES.toMillis(10) - diff);
            } else {
                Helper.storeBoolianSharePref(context, "account_can_create", true);
            }
        } catch (Exception e) {
            Helper.storeBoolianSharePref(context, "account_can_create", true);
        }
    }

    /*
     * Return the state of the application(If locked or not).
     */
    public Boolean getLock() {
        return mAppLock;
    }

    /*
     * Set the state of the application(If locked or not).
     */
    public void setLock(Boolean value) {
        mAppLock = value;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        ButterKnife.setDebug(true);
        context = getApplicationContext();
        blockHead = "";

        mHandler = new Handler();
        /*
        * Registering this class as a listener to all activitie's callback cycle events, in order to
        * better estimate when the user has left the app and it is safe to lock the app or not
        */
        registerActivityLifecycleCallbacks(this);


        //SETUP LOCALE AND DEFAULT PREFERENCES

        //Setup Country
        String country = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.pref_country), "");
        //If not at preferences yet it will setup device country or constant default
        //(It is expected that this "if" will be executed only one time, at first start up)
        if (country.equals("")) {
            Log.w(TAG, "Could not resolve country information, trying with the telephony manager");
            //If the locale mechanism fails to give us a country, we try
            //to get it from the TelephonyManager.
            country = Helper.getDeviceCountry(getApplicationContext());
            //If device don't respond with any country set it to the default (app constant)
            if (country == null || country.equals("")) {
                Log.w(TAG, "Could not resolve country information again, falling back to the default");
                country = Constants.DEFAULT_COUNTRY_CODE;
            }
        }
        Helper.setCountry(getApplicationContext(), country);

        //Setup Language
        String language = Helper.fetchStringSharePref(getApplicationContext(), getString(R.string.pref_language));
        //If app language preferences aren't set, set default as device/os language (telephony language)
        //(It is expected that this "if" will be executed only one time, at first start up)
        if (language.equals("")) {
            language = Locale.getDefault().getLanguage();
            //Just checking if we still don't have a language setup in the locale, in which
            //case we fallback to english as the default (the app constant .
            if (language.equals("")) {
                Log.w(TAG, "Could not resolve language information, falling back to english");
                language = Constants.DEFAULT_LANGUAGE_CODE;
            }
        }
        Helper.setLanguage(getApplicationContext(), language);


        //Check automatically close app behavior (after 3 min) is set and if not, put true by default
        Boolean closeAppPref = Helper.checkSharedPref(getApplicationContext(), "close_bitshare");
        if (!closeAppPref) {
            Helper.storeBoolianSharePref(getApplicationContext(), "close_bitshare", true);
        }


        init();
        accountCreateInit();
    }

    public void init() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mConnection == null) {
                    mConnection = new WebSocketConnection();
                    checkConnection();
                }
            }
        }, 1000);

    }

    private void webSocketConnection() {
        isReady = false;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                nodeIndex = nodeIndex % urlsSocketConnection.length;

                Log.i(TAG, "preparing to connect to:" + urlsSocketConnection[nodeIndex]);

                connect(urlsSocketConnection[nodeIndex]);
                nodeIndex++;

            }
        }, 500);

    }

    @Override
    public void onOpen() {
        Log.i("internetBlockpay", "open internet");
        mIsConnected = true;
//        Toast.makeText(context, getResources().getString(R.string.connected_to)+ ": "+connectedSocket,Toast.LENGTH_SHORT).show();
        sendInitialSocket(context);
    }

    public void checkConnection() {
        connectionHandler.removeCallbacksAndMessages(null);
        connectionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (checkInternetConnection()) {
                    {
                        webSocketConnection();
                    }
                } else {
                    connectionHandler.postDelayed(this, 500);
                }

            }
        }, 500);
    }

    @Override
    public void onClose(WebSocketCloseNotification code, String reason) {
        Log.i("internetBlockpay", "close internet");
        mIsConnected = false;
        checkConnection();
    }

    @Override
    public void onTextMessage(String payload) {
        stringTextRecievedWs(payload);
    }

    @Override
    public void onRawTextMessage(byte[] payload) {
    }

    @Override
    public void onBinaryMessage(byte[] payload) {
    }

    private void connect(String node) {
        Log.i("internetBlockpay", "connecting to internet");
        Log.i("Connect", "connect(String node) called");
        try {
            if (!mIsConnected) {
                Log.i("Connect", "Inside when not mIsConnected");
                mServerURI = new URI(node);
                connectedSocket = node;
                mConnection.connect(mServerURI, this);
            }
        } catch (URISyntaxException e) {
            String message = e.getLocalizedMessage();
            Log.i("Connect", "Inside catch block when not mIsConnected, got exception, msg is:" + message);
            checkConnection();
        } catch (WebSocketException e) {
            String message = e.getLocalizedMessage();
            Log.i("Connect", "Inside catch block when not mIsConnected, got exception, msg is:" + message);
            if (!mIsConnected) {
                checkConnection();
            }
        }
    }

    Boolean checkInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    void accountCreateInit() {
        if (Helper.containKeySharePref(context, "account_can_create")) {
            if (!accountCanCreate()) {
                getTimeStamp();
            }
        } else {
            Helper.storeBoolianSharePref(context, "account_can_create", true);
        }
    }

    /*
     * Get attribute used to indicate if UI should update the funds or not after an update (Send funds primarily)
     *
     * @return The boolean value of update UI state
     */
    public boolean getUpdateFunds() {
        return this.mUpdateFunds;
    }

    /*
     * Set attribute used to indicate if UI should update the funds or not after an update (Send funds primarily)
     *
     * @param update The boolean value of update UI state
     */
    public void setUpdateFunds(boolean update) {
        this.mUpdateFunds = update;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mHandler.removeCallbacks(this.lockApp);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mHandler.removeCallbacks(this.lockApp);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mHandler.removeCallbacks(this.lockApp);
        Application.setCurrentActivity(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        //Call the handler only if app is not already locked
        if (!mAppLock) {
            mHandler.postDelayed(this.lockApp, LOCK_DELAY);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}

