package de.bitshares_munich.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import de.bitshares_munich.interfaces.AssetDelegate;
import de.bitshares_munich.interfaces.IBalancesDelegate;
import de.bitshares_munich.models.EquivalentFiatStorage;
import de.bitshares_munich.models.TransactionDetails;
import de.bitshares_munich.smartcoinswallet.R;
import de.bitshares_munich.smartcoinswallet.WebsocketWorkerThread;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.PublicKey;
import de.bitsharesmunich.graphenej.api.GetAssets;
import de.bitsharesmunich.graphenej.api.GetLimitOrders;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.Market;
import de.bitsharesmunich.graphenej.models.WitnessResponse;


/**
 * Created by Syed Muhammad Muzzammil on 5/20/16.
 */
public class TransactionsHelper implements IBalancesDelegate {
    final int time = 5000;
    public Context context;
    public boolean finalBlockReceived = false;
    AssetDelegate assetDelegate;
    String accountid;
    String wifkey;
    long numberOfTransactionsLoaded = 0;
    long numberOfTransactionsToLoad = 0;

    ArrayList<TransactionDetails> alreadyLoadedTransactions;
    Handler handleHourlyTransactions;
    boolean callInProgressForHourlyTransactions = false;
    boolean callReceivedForHourlyTransactions = true;
    Date transactionsTimeSpan;
    long hourlyNumberOfTransactionsLoaded = 0;
    String hourlyTransactionsAccountId;
    String firstTransactionId = "";
    // block number -> ids -> operation
    HashMap<String, HashMap<String, JSONObject>> transactionsReceivedHm;
    HashMap<String, JSONObject> assetsReceivedHm;
    HashMap<String, JSONObject> namesToResolveHm;
    HashMap<JSONObject, String> memosToDecodeHm;
    List<String> headersTimeToFetch;
    int indexHeadersTimeToFetch;
    HashMap<String, Date> headerTimings;
    JSONObject[] encryptedMemos;
    int indexEncryptedMemos;
    HashMap<String, String> equivalentRatesHm;
    int retryGetEquivalentRates = 0;
    int retryGetIndirectEquivalentRates = 0;
    private String TAG = this.getClass().getName();
    public TransactionsHelper(Context c, final String account_id, AssetDelegate instance, String wif_key, final long _numberOfTransactionsLoaded, final long _numberOfTransactionsToLoad, final ArrayList<TransactionDetails> _alreadyLoadedTransactions) {
        context = c;
        assetDelegate = instance;
        Application.registerBalancesDelegateTransaction(this);
        accountid = account_id;

        handleHourlyTransactions = new Handler(Looper.getMainLooper());
        transactionsReceivedHm = new HashMap<>();
        assetsReceivedHm = new HashMap<>();
        headerTimings = new HashMap<>();
        namesToResolveHm = new HashMap<>();
        memosToDecodeHm = new HashMap<>();
        finalBlockReceived = false;

        numberOfTransactionsLoaded = _numberOfTransactionsLoaded;
        numberOfTransactionsToLoad = _numberOfTransactionsToLoad;

        alreadyLoadedTransactions = _alreadyLoadedTransactions;

        try {
            wifkey = Crypt.getInstance().decrypt_string(wif_key);
        } catch (Exception e) {
            wifkey = "";
        }

        if (account_id != null) {
            get_relative_account_history(account_id, "8", _numberOfTransactionsLoaded, _numberOfTransactionsToLoad);
        }
    }
    public TransactionsHelper(Context c, final String account_id, AssetDelegate instance, String wif_key, final Date _transactionsTimeSpan) {
        context = c;
        assetDelegate = instance;
        Application.registerBalancesDelegateTransaction(this);
        accountid = account_id;

        try {
            wifkey = Crypt.getInstance().decrypt_string(wif_key);
        } catch (Exception e) {
            wifkey = "";
        }

        handleHourlyTransactions = new Handler(Looper.getMainLooper());
        transactionsTimeSpan = _transactionsTimeSpan;
        transactionsReceivedHm = new HashMap<>();
        assetsReceivedHm = new HashMap<>();
        headerTimings = new HashMap<>();
        namesToResolveHm = new HashMap<>();
        memosToDecodeHm = new HashMap<>();
        hourlyNumberOfTransactionsLoaded = 0;
        finalBlockReceived = false;

        if (account_id != null) {
            get_relative_account_history(account_id, "20", hourlyNumberOfTransactionsLoaded);
        }
    }

    private void make_websocket_call(final String call_string_before_identifier, final String call_string_after_identifier, final api_identifier identifer) {
        handleHourlyTransactions.removeCallbacksAndMessages(null);

        final Runnable checkifTransactionReceived = new Runnable() {
            @Override
            public void run() {
                if (callInProgressForHourlyTransactions && !callReceivedForHourlyTransactions && Application.isReady) // if balances are not returned in one second
                {
                    Application.disconnect();
                    make_websocket_call(call_string_before_identifier, call_string_after_identifier, identifer);
                } else if (callInProgressForHourlyTransactions && !callReceivedForHourlyTransactions) {
                    make_websocket_call(call_string_before_identifier, call_string_after_identifier, identifer);
                }
            }
        };


        final Runnable initiateTransactionsFetch = new Runnable() {
            @Override
            public void run() {
                if (Application.isReady) {
                    String call = "";

                    if (identifer == api_identifier.database) {
                        if (context == null) return;
                        int database_id = Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_database));
                        call = call_string_before_identifier + database_id + call_string_after_identifier;
                    } else if (identifer == api_identifier.history) {
                        if (context == null) return;
                        int history_id = Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_history));
                        call = call_string_before_identifier + history_id + call_string_after_identifier;
                    } else if (identifer == api_identifier.network) {
                        if (context == null) return;
                        int nw_id = Helper.fetchIntSharePref(context, context.getString(R.string.sharePref_network_broadcast));
                        call = call_string_before_identifier + nw_id + call_string_after_identifier;
                    } else {
                        call = call_string_before_identifier;
                    }

                    Application.send(call);
                    callInProgressForHourlyTransactions = true;
                    callReceivedForHourlyTransactions = false;
                    handleHourlyTransactions.postDelayed(checkifTransactionReceived, time);
                } else {
                    make_websocket_call(call_string_before_identifier, call_string_after_identifier, identifer);
                }
            }
        };

        handleHourlyTransactions.postDelayed(initiateTransactionsFetch, 5);
    }

    void get_relative_account_history(final String account_id, final String id, final long _numberOfTransactionsLoaded) {
        handleHourlyTransactions.removeCallbacksAndMessages(null);
        hourlyTransactionsAccountId = account_id;

        String getDetails_before = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails_after = ",\"get_relative_account_history\",[\"" + account_id + "\",0," + Integer.toString(100) + "," + Long.toString(_numberOfTransactionsLoaded) + "]]}";

        make_websocket_call(getDetails_before, getDetails_after, api_identifier.history);
    }

    void get_relative_account_history(final String account_id, final String id, final long _numberOfTransactionsLoaded, final long _numberOfTransactionsToLoad) {
        Log.d(TAG, "get_relative_account_history. account id: " + account_id + ", id: " + id + ", _numberOfTransactionsLoaded: " + _numberOfTransactionsLoaded + ", _numberOfTransactionsToLoad: " + _numberOfTransactionsToLoad);
        handleHourlyTransactions.removeCallbacksAndMessages(null);
        hourlyTransactionsAccountId = account_id;

        String getDetails_before = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails_after = ",\"get_relative_account_history\",[\"" + account_id + "\",0," + Long.toString(_numberOfTransactionsToLoad) + "," + Long.toString(_numberOfTransactionsLoaded) + "]]}";

        make_websocket_call(getDetails_before, getDetails_after, api_identifier.history);
    }

    void get_block_header(final String id, final String block_num) {
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails2 = ",\"get_block_header\",[" + block_num + "]]}";
        make_websocket_call(getDetails, getDetails2, api_identifier.database);
    }

    void get_assets(final String asset, final String id) {
        String getDetails = "{\"id\":" + id + ",\"method\":\"get_assets\",\"params\":[[" + asset + "]]}";
        make_websocket_call(getDetails, "", api_identifier.none);
    }

    void get_names_transactions_recieved(final String names, final String id) {
        String getDetails = "{\"id\":" + id + ",\"method\":\"call\",\"params\":[";
        String getDetails2 = ",\"get_accounts\",[[" + names + "]]]}";
        make_websocket_call(getDetails, getDetails2, api_identifier.database);
    }

    @Override
    public void OnUpdate(String s, int id) {
        if (id == 8)
            Log.d(TAG, "OnUpdate. id: " + id + ", s: " + s);
        callInProgressForHourlyTransactions = false;
        callReceivedForHourlyTransactions = true;
        handleHourlyTransactions.removeCallbacksAndMessages(null);

        if (id == 8) {
            transactionsReceived(s);
        } else if (id == 20) {
            hourlyTransactionsReceived(s);
        } else if (id == 21) {
            if (numberOfTransactionsToLoad > 0) {
                blockNumberTimeReceived(s);
            } else {
                blockNumberTimeReceivedHourly(s);
            }
        } else if (id == 22) {
            assetsReceived(s);
        } else if (id == 23) {
            namesReceived(s);
        }
    }

    void hourlyTransactionsReceived(String result) {
        try {
            JSONArray myJson = new JSONObject(result).getJSONArray("result");

            // extract first transaction to detect when all the transactions are loaded
            if (hourlyNumberOfTransactionsLoaded == 0) {
                for (int i = 0; i < myJson.length(); i++) {
                    firstTransactionId = "";

                    JSONObject firstTx = myJson.getJSONObject(i);

                    if (firstTx.has("id")) {
                        firstTransactionId = firstTx.get("id").toString();
                        break;
                    }
                }

                if (firstTransactionId.isEmpty()) {
                    if (context == null) return;
                    assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_transactions));
                    return;
                }
            }

            // initialize few HashMaps and else
            if (transactionsReceivedHm == null) {
                transactionsReceivedHm = new HashMap<>();
                finalBlockReceived = false;
                assetsReceivedHm = new HashMap<>();
                namesToResolveHm = new HashMap<>();
                memosToDecodeHm = new HashMap<>();
            }

            headersTimeToFetch = new ArrayList<>();

            for (int i = 0; i < myJson.length(); i++) {
                // check if last transaction recieved
                if (!((hourlyNumberOfTransactionsLoaded == 0) && (i == 0))
                        && myJson.getJSONObject(i).has("id")
                        && myJson.getJSONObject(i).get("id").toString().equals(firstTransactionId)
                        ) {
                    // first transaction occurred again in history
                    // meaning last transaction is already received
                    finalBlockReceived = true;
                } else {
                    // Fetch only transactions wrt to block number and transaction ids 1.11.0002320
                    if (myJson.getJSONObject(i).has("block_num") && myJson.getJSONObject(i).has("id")) {
                        if (transactionsReceivedHm.containsKey(myJson.getJSONObject(i).get("block_num").toString())) {
                            if (transactionsReceivedHm.get(myJson.getJSONObject(i).get("block_num").toString()).containsKey(myJson.getJSONObject(i).get("id").toString())) {
                                // id already exists
                            } else {
                                transactionsReceivedHm.get(myJson.getJSONObject(i).get("block_num").toString()).put(myJson.getJSONObject(i).get("id").toString(), myJson.getJSONObject(i));
                            }
                        } else {
                            HashMap<String, JSONObject> newENtry = new HashMap<>();
                            newENtry.put(myJson.getJSONObject(i).get("id").toString(), myJson.getJSONObject(i));
                            transactionsReceivedHm.put(myJson.getJSONObject(i).get("block_num").toString(), newENtry);

                            headersTimeToFetch.add(myJson.getJSONObject(i).get("block_num").toString());
                        }
                    } else {
                        Log.d("Loading Transactions", "duplication");
                    }
                }

                try {
                    if (myJson.getJSONObject(i).has("op")) {
                        for (int j = 0; j < myJson.getJSONObject(i).getJSONArray("op").length(); j++) {
                            if (myJson.getJSONObject(i).getJSONArray("op").get(j) instanceof JSONObject) {
                                // get fee asset
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("fee")) {
                                    String asset_id = myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("fee").get("asset_id").toString();

                                    if (!assetsReceivedHm.containsKey(asset_id)) {
                                        assetsReceivedHm.put(asset_id, null);
                                    }
                                }

                                // get amount asset
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("amount")) {
                                    String asset_id = myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("amount").get("asset_id").toString();

                                    if (!assetsReceivedHm.containsKey(asset_id)) {
                                        assetsReceivedHm.put(asset_id, null);
                                    }
                                }

                                // get names of person from which money is recieved
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("from")) {
                                    if (!namesToResolveHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("from"))) {
                                        namesToResolveHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("from"), null);
                                    }
                                }

                                // get names of person to whom money is sent
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("to")) {
                                    if (!namesToResolveHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("to"))) {
                                        namesToResolveHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("to"), null);
                                    }
                                }

                                // get memos to decode
                                if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("memo") && myJson.getJSONObject(i).has("block_num")) {
                                    if (!memosToDecodeHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("memo"))) {
                                        memosToDecodeHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("memo"), myJson.getJSONObject(i).getString("block_num"));
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }

            hourlyNumberOfTransactionsLoaded += 100;

            if (headersTimeToFetch.size() == 0) {
                getAssetsInTransactionsReceived();
                return;
            }

            Collections.sort(headersTimeToFetch);
            Collections.reverse(headersTimeToFetch);

            indexHeadersTimeToFetch = 0;

            if (headerTimings == null) {
                headerTimings = new HashMap<>();
            }

            getTimeForTransactionsReceived();
        } catch (Exception e) {
            if (context == null) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
            Log.e(TAG, "Caught exception at hourlyTransactionsReceived. Msg: " + e.getMessage());
        }
    }

    void transactionsReceived(String result) {
        Log.d(TAG, "transactionsReceived");
        try {
            JSONArray myJson = new JSONObject(result).getJSONArray("result");

            // extract first transaction to detect when all the transactions are loaded
            if (numberOfTransactionsLoaded == 0) {
                for (int i = 0; i < myJson.length(); i++) {
                    firstTransactionId = "";

                    JSONObject firstTx = myJson.getJSONObject(i);

                    if (firstTx.has("id")) {
                        firstTransactionId = firstTx.get("id").toString();
                        break;
                    }
                }

                if (firstTransactionId.isEmpty()) {
                    if (context == null) return;
                    assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_transactions));
                    return;
                }
            }

            // initialize few HashMaps and else
            if (transactionsReceivedHm == null) {
                transactionsReceivedHm = new HashMap<>();
                finalBlockReceived = false;
                assetsReceivedHm = new HashMap<>();
                namesToResolveHm = new HashMap<>();
                memosToDecodeHm = new HashMap<>();
            }

            headersTimeToFetch = new ArrayList<>();

            HashMap<String, String> alreadyLoadedIds = new HashMap<>();
            for (TransactionDetails td : alreadyLoadedTransactions) {
                alreadyLoadedIds.put(td.id, td.blockNumber);
            }

            Boolean isNew = false;

            for (int i = 0; i < myJson.length(); i++) {
                if (!((hourlyNumberOfTransactionsLoaded == 0) && (i == 0))
                        && myJson.getJSONObject(i).has("id")
                        && myJson.getJSONObject(i).get("id").toString().equals(firstTransactionId)
                        ) {
                    // first transaction occurred again in history
                    // meaning last transaction is already received
                    finalBlockReceived = true;
                }

                if (
                        myJson.getJSONObject(i).has("id")
                                && alreadyLoadedIds.containsKey(myJson.getJSONObject(i).getString("id"))
                        ) {

                    Log.d("trx", "continue");
                    continue;

                }

                isNew = true;

                if (myJson.getJSONObject(i).has("block_num") && myJson.getJSONObject(i).has("id") && myJson.getJSONObject(i).has("op")) {

                    if (transactionsReceivedHm.containsKey(myJson.getJSONObject(i).get("block_num").toString())) {
                        if (transactionsReceivedHm.get(myJson.getJSONObject(i).get("block_num").toString()).containsKey(myJson.getJSONObject(i).get("id").toString())) {
                            // id already exists
                        } else {
                            transactionsReceivedHm.get(myJson.getJSONObject(i).get("block_num").toString()).put(myJson.getJSONObject(i).get("id").toString(), myJson.getJSONObject(i));
                        }
                    } else {
                        HashMap<String, JSONObject> newENtry = new HashMap<>();
                        newENtry.put(myJson.getJSONObject(i).get("id").toString(), myJson.getJSONObject(i));
                        transactionsReceivedHm.put(myJson.getJSONObject(i).get("block_num").toString(), newENtry);

                        headersTimeToFetch.add(myJson.getJSONObject(i).get("block_num").toString());
                    }


                    for (int j = 0; j < myJson.getJSONObject(i).getJSONArray("op").length(); j++) {
                        if (myJson.getJSONObject(i).getJSONArray("op").get(j) instanceof JSONObject) {
                            // get fee asset
                            if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("fee")) {
                                String asset_id = myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("fee").get("asset_id").toString();

                                if (!assetsReceivedHm.containsKey(asset_id)) {
                                    assetsReceivedHm.put(asset_id, null);
                                }
                            }

                            // get amount asset
                            if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("amount")) {
                                String asset_id = myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("amount").get("asset_id").toString();

                                if (!assetsReceivedHm.containsKey(asset_id)) {
                                    assetsReceivedHm.put(asset_id, null);
                                }
                            }

                            // get names of person from which money is recieved
                            if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("from")) {
                                if (!namesToResolveHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("from"))) {
                                    namesToResolveHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("from"), null);
                                }
                            }

                            // get names of person to whom money is sent
                            if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("to")) {
                                if (!namesToResolveHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("to"))) {
                                    namesToResolveHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getString("to"), null);
                                }
                            }

                            // get memos to decode
                            if (myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).has("memo") && myJson.getJSONObject(i).has("block_num")) {
                                if (!memosToDecodeHm.containsKey(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("memo"))) {
                                    memosToDecodeHm.put(myJson.getJSONObject(i).getJSONArray("op").getJSONObject(j).getJSONObject("memo"), myJson.getJSONObject(i).getString("block_num"));
                                }
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "duplication");
                }
            }

            //  if(isNew) {
            Log.d(TAG, "found");

            numberOfTransactionsLoaded += numberOfTransactionsToLoad;


            if (headersTimeToFetch.size() == 0) {
                Log.d("LogTransactions", "headersTimeToFetch.size()");
                getAssetsInTransactionsReceived();
                return;
            }

            Collections.sort(headersTimeToFetch);
            Collections.reverse(headersTimeToFetch);

            indexHeadersTimeToFetch = 0;

            if (headerTimings == null) {
                headerTimings = new HashMap<>();
            }

            getTimeForTransactionsReceived();
            // }
//            }else {
//                Log.d("LogTransactions","not found");
//                callInProgressForHourlyTransactions = false;
//                callReceivedForHourlyTransactions = true;
//                handleHourlyTransactions.removeCallbacksAndMessages(null);
//                assetDelegate.loadAgain();
//            }
        } catch (Exception e) {
            if (context == null) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
            Log.d("Loading Transactions", e.getMessage());
        }


    }

    void getTimeForTransactionsReceived() {
        try {
            get_block_header("21", headersTimeToFetch.get(indexHeadersTimeToFetch));
        } catch (Exception e) {
            if (context == null) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
            Log.d("Transactions Time", e.getMessage());
        }
    }

    void blockNumberTimeReceivedHourly(String result) {
        Log.d(TAG, "blockNumberTimeReceivedHourly. result: " + result);
        try {
            // get time from result
            JSONObject resultJson = new JSONObject(result);

            if (!resultJson.has("result")) {
                if (context == null) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_block_header));
                return;
            }

            JSONObject header = resultJson.getJSONObject("result");

            if (!header.has("timestamp")) {
                if (context == null) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_block_header));
                return;
            }

            String time = header.getString("timestamp");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date formattedDate = formatter.parse(time);

            // compare if less than or more than required Hours To load
            // if loaded transactions are enough then get asset objects
            // else get next block of transactions
            if (formattedDate.before(transactionsTimeSpan)) {
                getAssetsInTransactionsReceived();
            } else {
                String msg = String.format("%d / %d", indexHeadersTimeToFetch + 1 + hourlyNumberOfTransactionsLoaded - 100, headersTimeToFetch.size() + hourlyNumberOfTransactionsLoaded - 100);

                if (context == null) return;
                assetDelegate.transactionsLoadMessageStatus(msg);

                if (++indexHeadersTimeToFetch < headersTimeToFetch.size()) {
                    headerTimings.put(headersTimeToFetch.get(indexHeadersTimeToFetch - 1), formattedDate);
                    getTimeForTransactionsReceived();
                } else if (finalBlockReceived) {
                    getAssetsInTransactionsReceived();
                } else {
                    get_relative_account_history(hourlyTransactionsAccountId, "20", hourlyNumberOfTransactionsLoaded);
                }
            }
        } catch (Exception e) {
            if (context == null) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
            Log.e(TAG, "Exception caught in blockNumberTimeReceivedHourly. Msg: " + e.getMessage());
        }
    }

    void blockNumberTimeReceived(String result) {
        try {
            // get time from result
            JSONObject resultJson = new JSONObject(result);

            if (!resultJson.has("result")) {
                if (context == null) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_block_header));
                return;
            }

            JSONObject header = resultJson.getJSONObject("result");

            if (!header.has("timestamp")) {
                if (context == null) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_block_header));
                return;
            }

            String time = header.getString("timestamp");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date formattedDate = formatter.parse(time);

            String msg = String.format(Locale.ENGLISH, "%d / %d", indexHeadersTimeToFetch + 1 + numberOfTransactionsLoaded - numberOfTransactionsToLoad, headersTimeToFetch.size() + numberOfTransactionsLoaded - numberOfTransactionsToLoad);

            if (context == null) return;
            assetDelegate.transactionsLoadMessageStatus(msg);

            if (indexHeadersTimeToFetch < headersTimeToFetch.size()) {
                headerTimings.put(headersTimeToFetch.get(indexHeadersTimeToFetch), formattedDate);
            }
            if (++indexHeadersTimeToFetch < headersTimeToFetch.size()) {
                getTimeForTransactionsReceived();
            } else if (finalBlockReceived) {
                getAssetsInTransactionsReceived();
            } else {
                getAssetsInTransactionsReceived();
                //get_relative_account_history(hourlyTransactionsAccountId, "20", hourlyNumberOfTransactionsLoaded);
            }
        } catch (Exception e) {
            if (context == null) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
            Log.d("Transactions Time", e.getMessage());
        }
    }

    void getAssetsInTransactionsReceived() {
        try {
            // fetch assets
            // usables :
            // transactionsReceivedHm
            // headerTimings
            // assetsReceivedHm

            if (context == null) return;
            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.fetching_assets_in_transactions));

            if (assetsReceivedHm.size() > 0) {
                String values = "";

                for (String assets : assetsReceivedHm.keySet()) {
                    values += "\"" + assets + "\"" + ",";
                }

                values = values.substring(0, values.length() - 1);

                get_assets(values, "22");
            } else {
                if (context == null) return;
                getNamesInTransactionsReceived();
            }
        } catch (Exception e) {
            if (context == null) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
        }
    }

    void assetsReceived(String result) {
        try {
            // get time from result
            JSONObject resultJson = new JSONObject(result);

            if (!resultJson.has("result")) {
                if (context == null) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_assets));
                return;
            }

            JSONArray assets = resultJson.getJSONArray("result");

            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);

                if (asset.has("id") && assetsReceivedHm.containsKey(asset.getString("id"))) {
                    assetsReceivedHm.put(asset.getString("id"), asset);
                }
            }

            if (context == null) return;
            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.assets_retrieved) + assetsReceivedHm.size());

            getNamesInTransactionsReceived();
        } catch (Exception e) {
            if (context == null) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
        }
    }

    void getNamesInTransactionsReceived() {
        try {
            if (context == null) return;
            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.resolving_account_names_in_transactions));
            if (namesToResolveHm.size() > 0) {
                String values = "";

                for (String assets : namesToResolveHm.keySet()) {
                    values += "\"" + assets + "\"" + ",";
                }

                values = values.substring(0, values.length() - 1);

                get_names_transactions_recieved(values, "23");
            } else {
                if (context == null) return;
                String fiatCurrency = Helper.getFadeCurrency(context);
                getEquivalentFiatRates(fiatCurrency);
            }
        } catch (Exception e) {
            if (context == null) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
        }
    }

    void namesReceived(String result) {
        try {
            // get time from result
            JSONObject resultJson = new JSONObject(result);

            if (!resultJson.has("result")) {
                if (context == null) return;
                assetDelegate.transactionsLoadFailure(context.getString(R.string.invalid_accounts_name));
                return;
            }

            JSONArray names = resultJson.getJSONArray("result");

            for (int i = 0; i < names.length(); i++) {
                JSONObject asset = names.getJSONObject(i);

                if (asset.has("id") && namesToResolveHm.containsKey(asset.getString("id"))) {
                    namesToResolveHm.put(asset.getString("id"), asset);
                }
            }

            if (context == null) return;
            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.account_names_retrieved) + namesToResolveHm.size());

            if (context == null) return;
            String fiatCurrency = Helper.getFadeCurrency(context);
            getEquivalentFiatRates(fiatCurrency);
        } catch (Exception e) {
            if (context == null) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
        }
    }

    private void decodeMemoTransactionsReceived(final JSONObject memoObject) {
        ECKey toKey;
        toKey = ECKey.fromPrivate(DumpedPrivateKey.fromBase58(null, wifkey).getKey().getPrivKeyBytes());
        if (context == null) return;
        try {
            String message = memoObject.get("message").toString();
            PublicKey fromKey = new Address(memoObject.get("from").toString()).getPublicKey();
            String nonce = memoObject.get("nonce").toString();

//            if (memosToDecodeHm.containsKey(memoObject)) {
//                memosToDecodeHm.put(memoObject, Memo.decodeMessage(fromKey, toKey, message, nonce));
//            }
            loadMemoSListForDecoding();

        } catch (JSONException | MalformedAddressException e) {
            e.printStackTrace();
        }
    }

    private void decodeAllMemosInTransactionsReceived(final List<JSONObject> memosArray) {
        ECKey toKey;
        toKey = ECKey.fromPrivate(DumpedPrivateKey.fromBase58(null, wifkey).getKey().getPrivKeyBytes());

        for (JSONObject memoObject : memosArray) {
            try {
                String message = memoObject.get("message").toString();
                PublicKey fromKey = new Address(memoObject.get("from").toString()).getPublicKey();
                String nonce = memoObject.get("nonce").toString();
//                if (memosToDecodeHm.containsKey(memoObject)) {
//                    memosToDecodeHm.put(memoObject, Memo.decodeMessage(fromKey, toKey, message, nonce));
//                }
                decodingMemosComplete();

            } catch (JSONException | MalformedAddressException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMemoSListForDecoding() {
        try {
            if (indexEncryptedMemos < memosToDecodeHm.size()) {
                decodeMemoTransactionsReceived(encryptedMemos[indexEncryptedMemos]);
                indexEncryptedMemos++;
                if (context == null) return;
                assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.decrypting_memos) + indexEncryptedMemos + " of " + memosToDecodeHm.size());
            } else {
                generateTransactionsDetailArray();
            }

        } catch (Exception e) {
            generateTransactionsDetailArray();
        }
    }

    private void decodingMemosComplete() {
        try {
            /*
            if ( indexEncryptedMemos < memosToDecodeHm.size() )
            {
                decodeMemoTransactionsReceived(encryptedMemos[indexEncryptedMemos]);
                indexEncryptedMemos++;
                if ( context == null ) return;
                assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.decrypting_memos) + indexEncryptedMemos + " of " + memosToDecodeHm.size());
            }
            else
            {
                generateTransactionsDetailArray();
            }
            */
            generateTransactionsDetailArray();
        } catch (Exception e) {
            generateTransactionsDetailArray();
        }
    }

    private void decodeReceivedMemos() {
        try {

            // trim down extra memos
            JSONObject[] keys = memosToDecodeHm.keySet().toArray(new JSONObject[memosToDecodeHm.size()]);
            for (JSONObject key : keys) {
                if (!headerTimings.containsKey(memosToDecodeHm.get(key))) {
                    memosToDecodeHm.remove(key);
                } else {
                    memosToDecodeHm.put(key, null);
                }
            }


            if (memosToDecodeHm.size() > 0) {
                encryptedMemos = memosToDecodeHm.keySet().toArray(new JSONObject[memosToDecodeHm.size()]);
                List<JSONObject> memosList = new ArrayList<>();
                for (JSONObject memoObj : Arrays.asList(encryptedMemos)) {
                    memosList.add(memoObj);
                }
                decodeAllMemosInTransactionsReceived(memosList);
            } else {
                generateTransactionsDetailArray();
            }
        } catch (Exception e) {
            generateTransactionsDetailArray();
        }
    }

    private void generateTransactionsDetailArray() {
        Log.d(TAG, "generateTransactionsDetailArray");
        try {
            String[] blocks = headerTimings.keySet().toArray(new String[headerTimings.size()]);
            List<String> listOfBlocks = Arrays.asList(blocks);

            Collections.sort(listOfBlocks);
            Collections.reverse(listOfBlocks);

            List<TransactionDetails> myTransactions = new ArrayList<>();

            for (String blockNum : listOfBlocks) {
                HashMap<String, JSONObject> transactions = transactionsReceivedHm.get(blockNum);
                String[] transactionsIds = transactions.keySet().toArray(new String[transactions.size()]);
                List<String> transactionIdsList = Arrays.asList(transactionsIds);
                Collections.sort(transactionIdsList);
                Collections.reverse(transactionIdsList);

                for (String id : transactionIdsList) {
                    JSONObject transaction = transactions.get(id);

                    if (transaction.has("op")) {
                        for (int j = 0; j < transaction.getJSONArray("op").length(); j++) {
                            if (transaction.getJSONArray("op").get(j) instanceof JSONObject) {
                                TransactionDetails myTransactionDetails = new TransactionDetails();
                                myTransactionDetails.id = id;
                                myTransactionDetails.blockNumber = blockNum;
                                myTransactionDetails.Date = headerTimings.get(blockNum);

                                // get amount asset
                                if (transaction.getJSONArray("op").getJSONObject(j).has("amount")) {
                                    // get asset symbol
                                    myTransactionDetails.assetSymbol = assetsReceivedHm.get(transaction.getJSONArray("op").getJSONObject(j).getJSONObject("amount").getString("asset_id")).getString("symbol");

                                    // get asset amount
                                    String precision = assetsReceivedHm.get(transaction.getJSONArray("op").getJSONObject(j).getJSONObject("amount").getString("asset_id")).getString("precision");
                                    String number = transaction.getJSONArray("op").getJSONObject(j).getJSONObject("amount").getString("amount");
                                    myTransactionDetails.Amount = SupportMethods.convertAssetAmountToDouble(precision, number);

                                    // get fiat amount and symbol
                                    if (equivalentRatesHm.get(myTransactionDetails.assetSymbol) != null) {
                                        myTransactionDetails.fiatAmount = SupportMethods.convertAssetAmountToFiat(myTransactionDetails.Amount, equivalentRatesHm.get(myTransactionDetails.assetSymbol));
                                        if (context == null) return;
                                        myTransactionDetails.fiatAssetSymbol = Helper.getFadeCurrencySymbol(context);
                                    } else {
                                        myTransactionDetails.fiatAmount = 0;
                                        myTransactionDetails.fiatAssetSymbol = "";
                                    }
                                }

                                // get names of person from which money is recieved
                                if (transaction.getJSONArray("op").getJSONObject(j).has("from")) {
                                    myTransactionDetails.From = namesToResolveHm.get(transaction.getJSONArray("op").getJSONObject(j).getString("from")).getString("name");
                                } else {
                                    myTransactionDetails.From = "";
                                }

                                // get names of person to whom money is sent
                                if (transaction.getJSONArray("op").getJSONObject(j).has("to")) {
                                    myTransactionDetails.To = namesToResolveHm.get(transaction.getJSONArray("op").getJSONObject(j).getString("to")).getString("name");
                                } else {
                                    myTransactionDetails.To = "";
                                }

                                // get if sent or received
                                if (transaction.getJSONArray("op").getJSONObject(j).has("to")) {
                                    if (accountid.equals(transaction.getJSONArray("op").getJSONObject(j).getString("to"))) {
                                        myTransactionDetails.Sent = false;
                                    } else {
                                        myTransactionDetails.Sent = true;
                                    }
                                } else {
                                    continue;
                                }

                                // get memo message
                                Log.d(TAG, "Memo: " + memosToDecodeHm.get(transaction.getJSONArray("op").getJSONObject(j).getJSONObject("memo")));
                                if (transaction.getJSONArray("op").getJSONObject(j).has("memo")) {
                                    if (memosToDecodeHm.get(transaction.getJSONArray("op").getJSONObject(j).getJSONObject("memo")) != null) {
                                        myTransactionDetails.Memo = memosToDecodeHm.get(transaction.getJSONArray("op").getJSONObject(j).getJSONObject("memo"));
                                    } else {
                                        myTransactionDetails.Memo = "";
                                    }
                                } else {
                                    myTransactionDetails.Memo = "";
                                }

                                myTransactionDetails.eReceipt = transaction.toString();

                                myTransactions.add(myTransactionDetails);
                                if (context == null) return;
                                assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.generating_list_of_transactions) + myTransactions.size());
                            }
                        }
                    }
                }
            }

            List<TransactionDetails> completeListOfTransactionsToDisplay = new ArrayList<>();
            completeListOfTransactionsToDisplay.addAll(myTransactions);
            completeListOfTransactionsToDisplay.addAll(alreadyLoadedTransactions);

            // remove any duplications if exists
            HashMap<String, TransactionDetails> finalHm = new HashMap<>();

            for (TransactionDetails td : completeListOfTransactionsToDisplay) {
                finalHm.put(td.id, td);
            }

            completeListOfTransactionsToDisplay.clear();
            completeListOfTransactionsToDisplay.addAll(finalHm.values());

            Collections.sort(completeListOfTransactionsToDisplay, new transactionsDateComparator());

            if (context == null) return;
            assetDelegate.transactionsLoadComplete(completeListOfTransactionsToDisplay, myTransactions.size());
        } catch (Exception e) {
            Log.e(TAG, "Exception in generateTransactionsDetailArray. Msg: " + e.getMessage());
            if (context == null) return;
            assetDelegate.transactionsLoadFailure(context.getString(R.string.failure) + e.getMessage());
        }
    }

    private void reTryGetEquivalentComponents(final String fc) {
        if (retryGetEquivalentRates++ < 1) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    getEquivalentFiatRates(fc);
                }
            }, 100);
        } else {
            if (context == null) return;
            EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
            equivalentRatesHm = myFiatStorage.getEqHM(fc);
            decodeReceivedMemos();
        }
    }

    private void getEquivalentFiatRates(final String fiatCurrency) {
        if (context == null) return;
        EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
        //myFiatStorage.saveEqHM(fiatCurrency,equivalentRatesHm);
        equivalentRatesHm = myFiatStorage.getEqHM(fiatCurrency);
        decodeReceivedMemos();
    }

    private void reTryGetIndirectEquivalentComponents(final List<String> leftOvers, final String fiatCurrency) {
        if (retryGetIndirectEquivalentRates++ < 1) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    getIndirectEquivalentFiatRates(leftOvers, fiatCurrency);
                }
            }, 100);
        } else {
            if (context == null) return;
            EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
            equivalentRatesHm = myFiatStorage.getEqHM(fiatCurrency);
            decodeReceivedMemos();
        }
    }

    private void getIndirectEquivalentFiatRates(final List<String> leftOvers, final String fiatCurrency) {
        List<String> newPairs = new ArrayList<>();

        for (String pair : leftOvers) {
            String firstHalf = pair.split(":")[0];
            newPairs.add(firstHalf + ":" + "BTS");
        }

        HashMap<String, ArrayList<String>> currenciesChange = new HashMap();
        for (String pair : leftOvers) {
            String firstHalf = pair.split(":")[0];
            if (!currenciesChange.containsKey(firstHalf)) {
                currenciesChange.put(firstHalf, new ArrayList());
            }
            currenciesChange.get(firstHalf).add("BTS");
        }

        if (!currenciesChange.containsKey("BTS")) {
            currenciesChange.put("BTS", new ArrayList());
        }
        currenciesChange.get("BTS").add(fiatCurrency);

        this.getEquivalentComponent(currenciesChange, fiatCurrency);


        newPairs.add("BTS" + ":" + fiatCurrency);

        String values = "";

        for (String pair : newPairs) {
            values += pair + ",";
        }

        values = values.substring(0, values.length() - 1);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("method", "equivalent_component");
        hashMap.put("values", values);

        if (context == null) return;
        //TODO evaluate removal
        /*ServiceGenerator sg = new ServiceGenerator(context.getString(R.string.account_from_brainkey_url));
        IWebService service = sg.getService(IWebService.class);
        final Call<EquivalentComponentResponse> postingService = service.getEquivalentComponent(hashMap);

        postingService.enqueue(new Callback<EquivalentComponentResponse>() {
            @Override
            public void onResponse(Response<EquivalentComponentResponse> response)
            {
                if (response.isSuccess())
                {
                    EquivalentComponentResponse resp = response.body();

                    if (resp.status.equals("success"))
                    {
                        try
                        {

                            JSONObject rates = new JSONObject(resp.rates);
                            Iterator<String> keys = rates.keys();
                            String btsTofiat = "";

                            while (keys.hasNext())
                            {
                                String key = keys.next();

                                if (key.equals("BTS:" + fiatCurrency)) {
                                    btsTofiat = rates.get("BTS:" + fiatCurrency).toString();
                                    break;
                                }
                            }

                            if (!btsTofiat.isEmpty())
                            {
                                keys = rates.keys();


                                while (keys.hasNext())
                                {
                                    String key = keys.next();

                                    if (!key.equals("BTS:" + fiatCurrency))
                                    {
                                        String asset = key.split(":")[0];

                                        String assetConversionToBTS = rates.get(key).toString();

                                        double newConversionRate = convertLocalizeStringToDouble(assetConversionToBTS) * convertLocalizeStringToDouble(btsTofiat);

                                        String assetTofiatConversion = Double.toString(newConversionRate);

                                        equivalentRatesHm.put(asset, assetTofiatConversion);
                                    }
                                }

                                if ( context == null ) return;
                                EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
                                myFiatStorage.saveEqHM(fiatCurrency,equivalentRatesHm);
                            }

                            if ( context == null ) return;
                            EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
                            equivalentRatesHm = myFiatStorage.getEqHM(fiatCurrency);

                            if ( context == null ) return;
                            assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.fiat_exchange_rate_received));

                            decodeReceivedMemos();
                        }
                        catch (Exception e)
                        {
                            reTryGetIndirectEquivalentComponents(leftOvers,fiatCurrency);
                        }
                    }
                    else
                    {
                        reTryGetIndirectEquivalentComponents(leftOvers,fiatCurrency);
                    }
                }
                else
                {
                    reTryGetIndirectEquivalentComponents(leftOvers,fiatCurrency);
                }
            }

            @Override
            public void onFailure(Throwable t)
            {
                reTryGetIndirectEquivalentComponents(leftOvers,fiatCurrency);
            }
        });*/
    }

    public void getEquivalentComponent(final HashMap<String, ArrayList<String>> currencies, final String fiatCurrency) {
        ArrayList<String> assetList = new ArrayList();
        for (String key : currencies.keySet()) {
            if (!assetList.contains(key)) {
                assetList.add(key);
            }
            for (String values : currencies.get(key)) {
                if (!assetList.contains(values)) {
                    assetList.add(values);
                }
            }
        }

        WebsocketWorkerThread wwThread = new WebsocketWorkerThread(new GetAssets(assetList, new WitnessResponseListener() {
            @Override
            public void onSuccess(WitnessResponse response) {
                if (response.result.getClass() == ArrayList.class) {
                    ArrayList list = (ArrayList) response.result;
                    final HashMap<String, Asset> assets = new HashMap();
                    for (Object listObject : list) {
                        if (listObject.getClass() == Asset.class) {
                            Asset asset = (Asset) listObject;
                            assets.put(asset.getSymbol(), asset);
                        }
                    }
                    final HashMap<String, HashMap<String, Double>> rates = new HashMap();
                    List<WebsocketWorkerThread> threads = new ArrayList();
                    for (final String base : currencies.keySet()) {
                        if (!rates.containsKey(base)) {
                            rates.put(base, new HashMap());
                        }
                        for (final String quote : currencies.get(base)) {
                            WebsocketWorkerThread glo = new WebsocketWorkerThread(new GetLimitOrders(assets.get(base).getObjectId(), assets.get(quote).getObjectId(), 20, new WitnessResponseListener() {
                                @Override
                                public void onSuccess(WitnessResponse response) {
                                    if (response.result.getClass() == ArrayList.class) {
                                        ArrayList list = (ArrayList) response.result;
                                        for (Object listObject : list) {
                                            if (listObject.getClass() == Market.class) {
                                                Market market = ((Market) listObject);
                                                if (!market.sell_price.base.asset_id.equalsIgnoreCase(assets.get(base).getObjectId())) {
                                                    double price = market.sell_price.quote.amount / market.sell_price.base.amount;
                                                    int exp = assets.get(quote).getPrecision() - assets.get(base).getPrecision();
                                                    price = price * Math.pow(10, exp);
                                                    rates.get(base).put(quote, price);
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onError(BaseResponse.Error error) {

                                }
                            }));
                            glo.start();
                            threads.add(glo);
                        }
                    }
                    for (WebsocketWorkerThread thread : threads) {
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                        }
                    }

                    String btsTofiat = "";

                    for (String key : rates.keySet()) {
                        if (key.equals("BTS")) {
                            for (String keypair : rates.get(key).keySet()) {
                                if (fiatCurrency.equals(keypair)) {
                                    btsTofiat = rates.get(key).get(keypair).toString();
                                    break;
                                }
                            }
                        }
                    }

                    if (!btsTofiat.isEmpty()) {
                        for (String asset : rates.keySet()) {
                            for (String keypair : rates.get(asset).keySet()) {
                                if (!asset.equals("BTS") && !keypair.equals(fiatCurrency)) {
                                    String assetConversionToBTS = rates.get(asset).get(keypair).toString();

                                    double newConversionRate = convertLocalizeStringToDouble(assetConversionToBTS) * convertLocalizeStringToDouble(btsTofiat);

                                    String assetTofiatConversion = Double.toString(newConversionRate);

                                    equivalentRatesHm.put(asset, assetTofiatConversion);
                                }
                            }
                        }
                        if (context == null) return;
                        EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
                        myFiatStorage.saveEqHM(fiatCurrency, equivalentRatesHm);
                    }

                    if (context == null) return;
                    EquivalentFiatStorage myFiatStorage = new EquivalentFiatStorage(context);
                    equivalentRatesHm = myFiatStorage.getEqHM(fiatCurrency);

                    if (context == null) return;
                    assetDelegate.transactionsLoadMessageStatus(context.getString(R.string.fiat_exchange_rate_received));

                    decodeReceivedMemos();
                }
            }

            @Override
            public void onError(BaseResponse.Error error) {
                //TODO error
            }
        }));
        wwThread.start();

        try {
            wwThread.join();
        } catch (InterruptedException e) {
        }

    }

    private double convertLocalizeStringToDouble(String text) {
        double txtAmount_d = 0;
        try {
            NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
            Number number = format.parse(text);
            txtAmount_d = number.doubleValue();
        } catch (Exception e) {
            try {
                if (context == null) return txtAmount_d;
                String language = Helper.fetchStringSharePref(context, context.getString(R.string.pref_language));
                Locale locale = new Locale(language);
                NumberFormat format = NumberFormat.getInstance(locale);
                Number number = format.parse(text);
                txtAmount_d = number.doubleValue();

            } catch (Exception e1) {

            }
        }
        return txtAmount_d;
    }

    public enum api_identifier {
        database,
        history,
        network,
        none
    }
}
