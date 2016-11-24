package de.bitshares_munich.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import de.bitshares_munich.Interfaces.AssetDelegate;
import de.bitshares_munich.Interfaces.IAccount;
import de.bitshares_munich.Interfaces.IAccountID;
import de.bitshares_munich.Interfaces.IAccountObject;
import de.bitshares_munich.Interfaces.IAssetObject;
import de.bitshares_munich.Interfaces.IBalancesDelegate;
import de.bitshares_munich.Interfaces.IExchangeRate;
import de.bitshares_munich.Interfaces.IRelativeHistory;
import de.bitshares_munich.Interfaces.ITransactionObject;
import de.bitshares_munich.autobahn.WebSocketConnection;
import de.bitshares_munich.autobahn.WebSocketException;
import de.bitshares_munich.smartcoinswallet.R;

/**
 * Created by qasim on 5/9/16.
 */
public class Application extends android.app.Application implements de.bitshares_munich.autobahn.WebSocket.WebSocketConnectionObserver {
    private static String TAG = "Application";
    public static Context context;
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
    public static String blockHead = "";
    public static int refBlockNum;
    public static long refBlockPrefix;
    public static long blockTime;
    private static Activity currentActivity;

    public static String urlsSocketConnection[] =
            {
//                    "ws://api.devling.xyz:8088",
                    "wss://de.blockpay.ch:8089",                // German node
                    "wss://fr.blockpay.ch:8089",               // France node
                    "wss://bitshares.openledger.info/ws",      // Openledger node

//                    "wss://bit.btsabc.org/ws",
//                    "wss://bts.transwiser.com/ws",
//                    "wss://freedom.bts123.cc:15138",
//                    "wss://okbtc.org:8089/ws",
//                    "wss://ratebts.com:8089",
//                    "wss://webber.tech:8089/ws",
                    //"wss://bitshares.dacplay.org:8089/ws"
                    //,"https://dele-puppy.com/ws"
                    //,"https://valen-tin.fr:8090"
            };

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
        MultiDex.install(this);
        ButterKnife.setDebug(true);
        context = getApplicationContext();
        blockHead = "";
        init();
        accountCreateInit();
    }

    public void init()
    {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if ( mConnection == null )
                {
                    mConnection = new WebSocketConnection();
                   checkConnection();
                }
            }
        },1000);

    }


    public static void  registerCallback(IAccount callbackClass) {
        iAccount = callbackClass;
    }

    public static void  registerCallbackIAccountID(IAccountID callbackClass) {
        iAccountID = callbackClass;
    }

    public static void registerExchangeRateCallback(IExchangeRate callbackClass) {
        iExchangeRate = callbackClass;
    }

    public static void registerBalancesDelegateTransaction(IBalancesDelegate callbackClass) {
        iBalancesDelegate_transactionActivity = callbackClass;
    }

    public static void  registerBalancesDelegateEReceipt(IBalancesDelegate callbackClass) {
        iBalancesDelegate_ereceiptActivity = callbackClass;
    }

    public static void  registerBalancesDelegateAssets(IBalancesDelegate callbackClass) {
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

public static int nodeIndex = 0;

    private void webSocketConnection()
    {
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

    public static Boolean isReady = false;

    public static void stringTextRecievedWs(String s) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);

                        if (jsonObject.has("id")) {
                            int id = jsonObject.getInt("id");

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
                            }
                            else if (id == 8)
                            {
                                if (iBalancesDelegate_transactionActivity != null)
                                {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 20)
                            {
                                if (iBalancesDelegate_transactionActivity != null)
                                {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 21)
                            {
                                if (iBalancesDelegate_transactionActivity != null)
                                {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 22)
                            {
                                if (iBalancesDelegate_transactionActivity != null)
                                {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 23)
                            {
                                if (iBalancesDelegate_transactionActivity != null)
                                {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 12)
                            {
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
                            } else if (id == 9)
                            {
                                if (iBalancesDelegate_transactionActivity != null) {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            } else if (id == 10) {
                                if (iBalancesDelegate_transactionActivity != null) {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            } else if (id == 11)
                            {
                                if (iBalancesDelegate_transactionActivity != null) {
                                    iBalancesDelegate_transactionActivity.OnUpdate(s, id);
                                }
                            } else if (id == 99) {
                                if (iBalancesDelegate_assetsActivity != null) {
                                    SupportMethods.testing("assests", 99, "account_name");
                                    iBalancesDelegate_assetsActivity.OnUpdate(s, id);
                                }
                            } else if (id == 999) {
                                if (iBalancesDelegate_assetsActivity != null) {
                                    SupportMethods.testing("assests", 999, "account_name");
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
                            }
                            else if (id == 18) {
                                if (iBalancesDelegate_ereceiptActivity != null) {
                                    iBalancesDelegate_ereceiptActivity.OnUpdate(s, id);
                                }
                            }
                            else if (id == 19) {
                                if (iBalancesDelegate_ereceiptActivity != null) {
                                    iBalancesDelegate_ereceiptActivity.OnUpdate(s, id);
                                }
                            }
                        } else if (jsonObject.has("method")) {
                            if (jsonObject.getString("method").equals("notice")) {
                                if (jsonObject.has("params")) {
                                    int id = jsonObject.getJSONArray("params").getInt(0);
                                    JSONArray values = jsonObject.getJSONArray("params").getJSONArray(1);

                                    if (id == 7)
                                    {
                                        headBlockNumber(values.toString());

                                        if (monitorAccountId != null && !monitorAccountId.isEmpty() && values.toString().contains(monitorAccountId))
                                        {
                                            if (iAssetDelegate != null)
                                            {
                                                iAssetDelegate.loadAll();
                                            }
                                            Log.d("Notice Update", values.toString());
                                        }
                                    }
                                    else {
                                        Log.d("other notice", values.toString());
                                    }

                                }
                            }
                        }


                    } catch (JSONException e) {

                    }
                }

    private static void sendInitialSocket(final Context context)
    {

        if (Application.mIsConnected)
        {
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
            if(array.length() == 1){
                JSONArray subArray = array.getJSONArray(0);
                for(int i = 0; i < subArray.length(); i++){
                    if(subArray.get(i) instanceof JSONObject){
                        JSONObject element = (JSONObject) subArray.get(i);
                        if(element.has(BLOCK_REFERENCE_ID)){
                            rawBlockId = element.getString(BLOCK_REFERENCE_ID);
                        }
                        if(element.has(HEAD_BLOCK_NUMBER)){
                            blockNumber = element.getString(HEAD_BLOCK_NUMBER);
                        }
                        if(element.has(TIME)){
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            try {
                                Date date = simpleDateFormat.parse(element.getString(TIME));
                                blockTime = date.getTime() / 1000;
                            } catch (ParseException e) {
                                Log.e(TAG, "ParseException while trying to parse time. time string: "+element.getString(TIME));
                            }
                        }
                    }else{
                        String element = (String) subArray.get(i);
                        Log.d(TAG, "Could not cast string: "+element);
                    }
                }
                if(rawBlockId.equals("")) {
                    Log.w(TAG,"Could not process data");
                    return blockHead;
                }
                // Setting block number
                blockHead = "block# " + blockNumber;

                // Setting reference block number (lower 16 bits of block number)
                refBlockNum = Integer.valueOf(blockNumber) & 0xffff;

                // Setting block prefix
                String hashData = rawBlockId.substring(8, 16);
                StringBuilder builder = new StringBuilder();
                for(int i = 0; i < 8; i = i + 2){
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

    public static void send(String message)
    {
        if (mIsConnected)
        {
            mConnection.sendTextMessage(message);
        }
    }

    private static boolean mIsConnected = false;
    static String connectedSocket;

    public static void disconnect() {
        Log.i("internetBlockpay", "Disconnect");
        if(mConnection!=null) {
            mConnection.disconnect();
        }
    }

    @Override
    public void onOpen()
    {
        Log.i("internetBlockpay", "open internet");
        mIsConnected = true;
//        Toast.makeText(context, getResources().getString(R.string.connected_to)+ ": "+connectedSocket,Toast.LENGTH_SHORT).show();
        sendInitialSocket(context);
    }

    Handler connectionHandler = new Handler();
    public void checkConnection(){
        connectionHandler.removeCallbacksAndMessages(null);
        connectionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if(checkInternetConnection()) {{
                        webSocketConnection();
                    }
                }else {
                    connectionHandler.postDelayed(this,500);
                }

            }
        }, 500);
    }


    @Override
    public void onClose(WebSocketCloseNotification code, String reason)
    {
        Log.i("internetBlockpay", "close internet");
        mIsConnected = false;
        checkConnection();
    }

    @Override
    public void onTextMessage(String payload)
    {
      stringTextRecievedWs(payload);
    }

    @Override
    public void onRawTextMessage(byte[] payload) {
    }

    @Override
    public void onBinaryMessage(byte[] payload) {
    }

    private static WebSocketConnection mConnection;
    private static URI mServerURI;

    @NonNull
    public static Boolean isConnected(){
        return mConnection!=null && (mConnection.isConnected());
    }

    private void connect(String node)
    {
        Log.i("internetBlockpay", "connecting to internet");
        Log.i("Connect", "connect(String node) called");
        try
        {
            if ( !mIsConnected )
            {
                Log.i("Connect", "Inside when not mIsConnected");
                mServerURI = new URI(node);
                connectedSocket = node;
                mConnection.connect(mServerURI, this);
            }
        }
        catch (URISyntaxException e)
        {
            String message = e.getLocalizedMessage();
            Log.i("Connect", "Inside catch block when not mIsConnected, got exception, msg is:" + message);
            checkConnection();
        }
        catch (WebSocketException e)
        {
            String message = e.getLocalizedMessage();
            Log.i("Connect", "Inside catch block when not mIsConnected, got exception, msg is:" + message);
            if ( !mIsConnected ) {
                checkConnection();
            }
        }
    }

    Boolean checkInternetConnection(){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static void timeStamp(){

        Helper.storeBoolianSharePref(context,"account_can_create",false);
        setTimeStamp();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Helper.storeBoolianSharePref(context,"account_can_create",true);

                }
            }, 10 * 60000);
    }
    @NonNull
    public static Boolean accountCanCreate(){
        return Helper.fetchBoolianSharePref(context,"account_can_create");
    }
    void accountCreateInit(){
        if(Helper.containKeySharePref(context,"account_can_create")){
           if(!accountCanCreate()){
              getTimeStamp();
           }
        }else {
            Helper.storeBoolianSharePref(context,"account_can_create",true);
        }
    }
    static void setTimeStamp(){
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis();
        Helper.storeLongSharePref(context,"account_create_timestamp",time);
    }
    static void getTimeStamp(){
        try {
            Calendar c = Calendar.getInstance();
            long currentTime = c.getTimeInMillis();;
            long oldTime = Helper.fetchLongSharePref(context, "account_create_timestamp");
            long diff = currentTime-oldTime;
            if(diff < TimeUnit.MINUTES.toMillis(10)){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Helper.storeBoolianSharePref(context,"account_can_create",true);
                    }
                }, TimeUnit.MINUTES.toMillis(10)-diff);
            }else {
                Helper.storeBoolianSharePref(context,"account_can_create",true);
            }
        }catch (Exception e){
            Helper.storeBoolianSharePref(context,"account_can_create",true);
        }
    }
}

